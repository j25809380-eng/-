const { request } = require('../../utils/request');
const mock = require('../../utils/mock');
const { navigateBack } = require('../../utils/nav');

Page({
  data: {
    keyword: '',
    activeCategory: '全部',
    categories: ['全部', '胸部', '背部', '腿部', '肩部', '手臂'],
    exercises: []
  },

  handleBack() {
    navigateBack('/pages/home/index');
  },

  onShow() {
    request({
      url: '/exercises',
      mockData: mock.exercises
    }).then((exercises) => {
      this.setData({ exercises });
    });
  },

  handleInput(event) {
    this.setData({ keyword: event.detail.value });
  },

  selectCategory(event) {
    this.setData({ activeCategory: event.currentTarget.dataset.value });
  },

  goHistory() {
    wx.navigateTo({ url: '/pages/history/index' });
  },

  goDetail(event) {
    wx.navigateTo({ url: `/pages/exercise-detail/index?id=${event.currentTarget.dataset.id}` });
  }
});
