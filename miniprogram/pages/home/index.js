const { request } = require('../../utils/request');
const mock = require('../../utils/mock');
const { navigateBack } = require('../../utils/nav');

Page({
  data: {
    dashboard: null
  },

  handleBack() {
    navigateBack('/pages/home/index');
  },

  onShow() {
    request({
      url: '/dashboard/home',
      mockData: mock.dashboardHome
    }).then((dashboard) => {
      this.setData({ dashboard });
    });
  },

  goPath(event) {
    const { path } = event.currentTarget.dataset;
    wx.navigateTo({ url: path });
  },

  goPlan() {
    wx.reLaunch({ url: '/pages/plan/index' });
  }
});
