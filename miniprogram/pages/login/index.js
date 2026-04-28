const { request } = require('../../utils/request');
const { navigateBack } = require('../../utils/nav');

Page({
  data: {
    loading: false
  },

  onShow() {
    const app = getApp();
    const token = app.globalData.token || wx.getStorageSync('fitnote_token') || '';
    if (token) {
      wx.reLaunch({ url: '/pages/home/index' });
    }
  },

  handleBack() {
    navigateBack('/pages/login/index');
  },

  async handleLogin() {
    if (this.data.loading) {
      return;
    }

    this.setData({ loading: true });

    try {
      const loginRes = await this.wxLogin();
      const profile = await this.tryGetUserProfile();
      const userInfo = profile.userInfo || {};

      const res = await request({
        url: '/auth/wechat-login',
        method: 'POST',
        data: {
          code: loginRes.code,
          nickname: userInfo.nickName || '',
          avatarUrl: userInfo.avatarUrl || ''
        }
      });

      const app = getApp();
      app.globalData.token = res.token;
      app.globalData.user = res.user;
      wx.setStorageSync('fitnote_token', res.token);
      wx.setStorageSync('fitnote_user', res.user);
      wx.reLaunch({ url: '/pages/home/index' });
    } catch (error) {
      wx.showToast({
        title: 'Login failed, please retry',
        icon: 'none'
      });
    } finally {
      this.setData({ loading: false });
    }
  },

  wxLogin() {
    return new Promise((resolve, reject) => {
      wx.login({
        success: (res) => {
          if (res && res.code) {
            resolve(res);
            return;
          }
          reject(new Error('wx.login did not return code'));
        },
        fail: reject
      });
    });
  },

  tryGetUserProfile() {
    return new Promise((resolve) => {
      if (typeof wx.getUserProfile !== 'function') {
        resolve({});
        return;
      }

      wx.getUserProfile({
        desc: 'Used to complete your FitNote profile',
        success: resolve,
        fail: () => resolve({})
      });
    });
  }
});
