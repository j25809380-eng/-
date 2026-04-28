const { request, resolveFileUrl } = require('../../utils/request');
const { navigateBack } = require('../../utils/nav');

function normalizeMonthlyReport(report) {
  if (!report) {
    return null;
  }

  const topExercises = report.topExercises || [];
  const maxVolume = topExercises.reduce((max, item) => Math.max(max, Number(item.volume) || 0), 1);

  return Object.assign({}, report, {
    monthLabel: String(report.month || '').replace('-', ' / '),
    weeklyBreakdown: (report.weeklyBreakdown || []).map((item) => Object.assign({}, item, {
      barHeight: 48 + ((item.count || 0) * 38)
    })),
    topExercises: topExercises.map((item) => Object.assign({}, item, {
      barWidth: Math.max(80, Math.round(((Number(item.volume) || 0) / maxVolume) * 360))
    }))
  });
}

function toChartData(overview) {
  const weightTrend = (overview && overview.weightTrend) || [];
  const volumeTrend = (overview && overview.volumeTrend) || [];
  return {
    weightTrendLabels: weightTrend.map((item) => String(item.date || '').slice(5)),
    weightTrendValues: weightTrend.map((item) => Number(item.weightKg) || 0),
    volumeTrendLabels: volumeTrend.map((item) => String(item.date || '').slice(5)),
    volumeTrendValues: volumeTrend.map((item) => Number(item.volume) || 0)
  };
}

Page({
  data: {
    history: [],
    overview: null,
    monthlyReport: null,
    exportingReport: false,
    weightTrendLabels: [],
    weightTrendValues: [],
    volumeTrendLabels: [],
    volumeTrendValues: []
  },

  handleBack() {
    navigateBack('/pages/home/index');
  },

  onShow() {
    Promise.all([
      request({ url: '/workouts/history' }),
      request({ url: '/analytics/overview' }),
      request({ url: '/reports/monthly' })
    ]).then(([history, overview, monthlyReport]) => {
      const charts = toChartData(overview);
      this.setData(Object.assign({
        history: history || [],
        overview: overview || null,
        monthlyReport: normalizeMonthlyReport(monthlyReport)
      }, charts));
    });
  },

  goRanking() {
    wx.navigateTo({ url: '/pages/ranking/index' });
  },

  goWorkout(event) {
    wx.navigateTo({ url: `/pages/workout-complete/index?id=${event.currentTarget.dataset.id}` });
  },

  exportMonthlyReport() {
    if (this.data.exportingReport) {
      return;
    }

    const month = this.data.monthlyReport && this.data.monthlyReport.month;
    const query = month ? `?month=${encodeURIComponent(month)}` : '';
    this.setData({ exportingReport: true });

    request({
      url: `/reports/monthly/export${query}`
    }).then((result) => {
      const url = resolveFileUrl(result.url);
      wx.setClipboardData({ data: url });

      wx.downloadFile({
        url,
        success: (downloadRes) => {
          if (downloadRes.statusCode !== 200) {
            wx.showToast({ title: 'Export failed', icon: 'none' });
            return;
          }

          wx.saveFile({
            tempFilePath: downloadRes.tempFilePath,
            success: () => {
              wx.showToast({ title: 'Report exported', icon: 'success' });
            },
            fail: () => {
              wx.showToast({ title: 'Link copied', icon: 'none' });
            }
          });
        },
        fail: () => {
          wx.showToast({ title: 'Link copied', icon: 'none' });
        }
      });
    }).catch(() => {
      wx.showToast({ title: 'Export failed', icon: 'none' });
    }).finally(() => {
      this.setData({ exportingReport: false });
    });
  }
});
