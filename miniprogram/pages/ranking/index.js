const { request } = require('../../utils/request');
const mock = require('../../utils/mock');
const { navigateBack } = require('../../utils/nav');

Page({
  data: {
    rankings: [],
    topThree: [],
    activeTab: 'global',
    displayRankings: []
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
        topThree: rankings.slice(0, 3),
        displayRankings: rankings
      });
    });
  },

  switchTab(event) {
    const tab = event.currentTarget.dataset.tab;
    if (tab === this.data.activeTab) return;

    const rankings = this.data.rankings;
    let displayRankings;

    if (tab === 'friends') {
      displayRankings = rankings.slice(0, 5);
    } else {
      displayRankings = rankings;
    }

    this.setData({
      activeTab: tab,
      displayRankings
    });
  }
});
