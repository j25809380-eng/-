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
    wx.showToast({
      title: '已准备分享到社区',
      icon: 'success'
    });
  },

  backHome() {
    wx.reLaunch({ url: '/pages/home/index' });
  }
});
