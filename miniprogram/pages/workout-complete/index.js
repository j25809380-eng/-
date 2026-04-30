const { request } = require('../../utils/request');
const mock = require('../../utils/mock');
const { navigateBack } = require('../../utils/nav');

Page({
  data: {
    detail: null
  },

  handleBack() {
    navigateBack('/pages/home/index');
  },

  onLoad(options) {
    request({
      url: `/workouts/${options.id || 1}`,
      mockData: mock.workoutDetail
    }).then((detail) => {
      this.setData({ detail });
    });
  },

  shareToCommunity() {
    const d = this.data.detail;
    if (!d) return;

    const prSets = (d.sets || []).filter(s => s.isPr);
    const prText = prSets.length > 0
      ? prSets.map(s => `${s.exerciseName} ${s.weightKg}kg × ${s.reps}`).join('、')
      : '';

    const content = [
      `完成训练：${d.title || '今日训练'}`,
      d.focus ? `训练重点：${d.focus}` : '',
      `总训练量 ${d.totalVolume || 0}kg · 时长 ${d.durationMinutes || 0}min · 消耗 ${d.calories || 0}kcal`,
      prText ? `刷新PR：${prText}` : ''
    ].filter(Boolean).join('\n');

    request({
      url: '/community/posts',
      method: 'POST',
      data: {
        content: content,
        postType: 'WORKOUT',
        topicTags: '训练打卡' + (prSets.length > 0 ? ',PR' : '')
      }
    }).then(() => {
      wx.showToast({ title: '已分享到社区', icon: 'success' });
      setTimeout(() => {
        wx.reLaunch({ url: '/pages/community/index' });
      }, 1200);
    }).catch(() => {
      wx.showToast({ title: '分享失败，请重试', icon: 'none' });
    });
  },

  backHome() {
    wx.reLaunch({ url: '/pages/home/index' });
  }
});
