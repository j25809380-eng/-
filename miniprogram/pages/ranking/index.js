const { request } = require('../../utils/request');
const mock = require('../../utils/mock');
const { navigateBack } = require('../../utils/nav');

Page({
  data: {
    rankings: [],
    topThree: []
  },

  handleBack() {
    navigateBack('/pages/home/index');
  },

  onShow() {
    request({
      url: '/analytics/rankings',
      mockData: mock.rankings
    }).then((rankings) => {
      this.setData({
        rankings,
        topThree: rankings.slice(0, 3)
      });
    });
  }
});
