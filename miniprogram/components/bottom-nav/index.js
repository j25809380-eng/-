Component({
  properties: {
    active: {
      type: String,
      value: 'home'
    }
  },

  data: {
    navs: [
      { key: 'home', label: '首页', path: '/pages/home/index' },
      { key: 'plan', label: '计划', path: '/pages/plan/index' },
      { key: 'ai', label: '智能', path: '/pages/ai/index' },
      { key: 'community', label: '社区', path: '/pages/community/index' },
      { key: 'profile', label: '我的', path: '/pages/profile/index' }
    ]
  },

  methods: {
    handleTap(event) {
      const { path, key } = event.currentTarget.dataset;
      if (key === this.properties.active) {
        return;
      }
      wx.reLaunch({ url: path });
    }
  }
});
