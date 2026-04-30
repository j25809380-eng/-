const { request } = require('../../utils/request');
const mock = require('../../utils/mock');
const { navigateBack } = require('../../utils/nav');

Page({
  data: {
    ai: null,
    inputValue: '',
    messages: [],
    loading: false
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
      if (!this.data.messages.length && ai && ai.welcomeMessage) {
        this.setData({
          messages: [{
            role: 'assistant',
            content: ai.welcomeMessage,
            time: this.formatTime(new Date())
          }]
        });
      }
    });
  },

  fillPrompt(event) {
    const title = event.currentTarget.dataset.title;
    this.setData({ inputValue: title });
    this.sendMessage(title);
  },

  fillPromptInput(event) {
    this.setData({ inputValue: event.detail.value });
  },

  sendMessage(text) {
    const content = text || this.data.inputValue;
    if (!content || this.data.loading) {
      return;
    }

    const messages = this.data.messages.concat([{
      role: 'user',
      content: content,
      time: this.formatTime(new Date())
    }]);

    this.setData({
      messages,
      inputValue: '',
      loading: true
    });

    this.scrollToBottom();

    request({
      url: '/ai/chat',
      method: 'POST',
      data: { message: content }
    }).then((result) => {
      const reply = result.reply || '抱歉，暂时无法回复，请稍后再试。';
      this.setData({
        messages: this.data.messages.concat([{
          role: 'assistant',
          content: reply,
          time: this.formatTime(new Date())
        }]),
        loading: false
      });
      this.scrollToBottom();
    }).catch(() => {
      this.setData({
        messages: this.data.messages.concat([{
          role: 'assistant',
          content: '网络连接失败，请稍后重试。',
          time: this.formatTime(new Date())
        }]),
        loading: false
      });
      this.scrollToBottom();
    });
  },

  scrollToBottom() {
    setTimeout(() => {
      wx.createSelectorQuery()
        .select('#chat-bottom')
        .boundingClientRect()
        .exec((res) => {
          if (res && res[0]) {
            wx.pageScrollTo({
              scrollTop: 99999,
              duration: 200
            });
          }
        });
    }, 100);
  },

  formatTime(date) {
    const h = date.getHours().toString().padStart(2, '0');
    const m = date.getMinutes().toString().padStart(2, '0');
    return h + ':' + m;
  }
});
