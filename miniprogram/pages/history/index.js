const { request, resolveFileUrl } = require('../../utils/request');
const { navigateBack } = require('../../utils/nav');

function normalizeMonthlyReport(report) {
  if (!report) return null;

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
    volumeTrendValues: [],
    // 分页
    page: 0,
    totalPages: 0,
    hasMore: true,
    loadingMore: false
  },

  handleBack() {
    navigateBack('/pages/home/index');
  },

  onShow() {
    this.setData({ page: 0, history: [], hasMore: true });
    Promise.all([
      request({ url: '/workouts/history', data: { page: 0, size: 20 } }),
      request({ url: '/analytics/overview' }),
      request({ url: '/reports/monthly' })
    ]).then(([pageResult, overview, monthlyReport]) => {
      const charts = toChartData(overview);
      const history = pageResult && pageResult.items ? pageResult.items : (Array.isArray(pageResult) ? pageResult : []);
      this.setData(Object.assign({
        history,
        overview: overview || null,
        monthlyReport: normalizeMonthlyReport(monthlyReport),
        page: 0,
        totalPages: pageResult ? (pageResult.totalPages || 0) : 0,
        hasMore: pageResult ? (pageResult.page < pageResult.totalPages - 1) : false
      }, charts));
    });
  },

  onReachBottom() {
    if (!this.data.hasMore || this.data.loadingMore) return;
    const nextPage = this.data.page + 1;
    this.setData({ loadingMore: true });

    request({
      url: '/workouts/history',
      data: { page: nextPage, size: 20 }
    }).then((pageResult) => {
      const newItems = pageResult && pageResult.items ? pageResult.items : (Array.isArray(pageResult) ? pageResult : []);
      this.setData({
        history: this.data.history.concat(newItems),
        page: nextPage,
        totalPages: pageResult ? (pageResult.totalPages || 0) : 0,
        hasMore: pageResult ? (pageResult.page < pageResult.totalPages - 1) : false,
        loadingMore: false
      });
    }).catch(() => {
      this.setData({ loadingMore: false });
    });
  },

  goRanking() {
    wx.navigateTo({ url: '/pages/ranking/index' });
  },

  goWorkout(event) {
    wx.navigateTo({ url: `/pages/workout-complete/index?id=${event.currentTarget.dataset.id}` });
  },

  exportMonthlyReport() {
    if (this.data.exportingReport) return;

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
            wx.showToast({ title: '导出失败', icon: 'none' });
            return;
          }
          wx.saveFile({
            tempFilePath: downloadRes.tempFilePath,
            success: () => wx.showToast({ title: '报告已导出', icon: 'success' }),
            fail: () => wx.showToast({ title: '链接已复制', icon: 'none' })
          });
        },
        fail: () => wx.showToast({ title: '链接已复制', icon: 'none' })
      });
    }).catch(() => {
      wx.showToast({ title: '导出失败', icon: 'none' });
    }).finally(() => {
      this.setData({ exportingReport: false });
    });
  }
});
