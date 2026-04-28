const { request } = require('../../utils/request');
const mock = require('../../utils/mock');
const { navigateBack } = require('../../utils/nav');

Page({
  data: {
    ai: null,
    inputValue: ''
  },

  handleBack() {
    navigateBack('/pages/home/index');
  },

  onShow() {
    request({
      url: '/ai/prompts',
      mockData: mock.aiPrompts
    }).then((ai) => {
      this.setData({ ai });
    });
  },

  fillPrompt(event) {
    this.setData({ inputValue: event.currentTarget.dataset.title });
  },

  fillPromptInput(event) {
    this.setData({ inputValue: event.detail.value });
  },

  sendMessage() {
    if (!this.data.inputValue) {
      return;
    }
    wx.showToast({
      title: 'AI 对话入口已预留',
      icon: 'none'
    });
  }
});
