App({
  globalData: {
    token: '',
    user: null
  },

  onLaunch() {
    const token = wx.getStorageSync('fitnote_token') || '';
    const user = wx.getStorageSync('fitnote_user') || null;
    this.globalData.token = token;
    this.globalData.user = user;
  }
});
