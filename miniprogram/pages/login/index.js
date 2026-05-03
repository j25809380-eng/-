const { request } = require('../../utils/request');

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

  async handleLogin() {
    if (this.data.loading) return;

    this.setData({ loading: true });
    wx.showLoading({ title: '登录中...', mask: true });

    try {
      // 获取微信登录 code
      let code = '';
      try {
        const loginRes = await this.wxLogin();
        code = loginRes.code;
      } catch (e) {
        // wx.login 失败时使用降级 code
        code = 'dev_fallback_code';
      }

      // 获取用户信息（新版微信基础库已废弃 getUserProfile，使用头像昵称填写组件替代）
      let nickname = '';
      let avatarUrl = '';
      try {
        const userInfo = await this.getUserInfo();
        nickname = userInfo.nickName || '';
        avatarUrl = userInfo.avatarUrl || '';
      } catch (e) {
        // 获取用户信息失败不阻塞登录
      }

      // 调用后端登录接口（带 mock 降级）
      const res = await request({
        url: '/auth/wechat-login',
        method: 'POST',
        data: { code, nickname, avatarUrl },
        mockData: {
          token: 'fitnote-demo-token',
          user: { id: 1, nickname: nickname || '健身达人', avatarUrl: '' }
        }
      });

      this.saveLoginState(res);
    } catch (error) {
      // 所有方案都失败时，直接使用本地 mock 登录
      this.mockLoginDirect();
    } finally {
      this.setData({ loading: false });
      wx.hideLoading();
    }
  },

  // 直接本地 mock 登录（终极降级方案）
  mockLoginDirect() {
    const token = 'fitnote-demo-token';
    const user = { id: 1, nickname: '健身达人', avatarUrl: '' };

    const app = getApp();
    app.globalData.token = token;
    app.globalData.user = user;
    wx.setStorageSync('fitnote_token', token);
    wx.setStorageSync('fitnote_user', user);

    wx.showToast({ title: '离线模式已进入', icon: 'success', duration: 1500 });
    setTimeout(() => wx.reLaunch({ url: '/pages/home/index' }), 800);
  },

  saveLoginState(res) {
    const app = getApp();
    app.globalData.token = res.token;
    app.globalData.user = res.user;
    wx.setStorageSync('fitnote_token', res.token);
    wx.setStorageSync('fitnote_user', res.user);
    wx.reLaunch({ url: '/pages/home/index' });
  },

  wxLogin() {
    return new Promise((resolve, reject) => {
      wx.login({
        success: (res) => {
          if (res && res.code) {
            resolve(res);
          } else {
            reject(new Error('wx.login 未返回 code'));
          }
        },
        fail: reject
      });
    });
  },

  getUserInfo() {
    return new Promise((resolve, reject) => {
      // 新版基础库：使用 wx.getUserInfo 获取匿名信息
      if (typeof wx.getUserInfo === 'function') {
        wx.getUserInfo({
          success: (res) => resolve(res.userInfo || {}),
          fail: () => resolve({})
        });
        return;
      }

      // 旧版基础库：使用已废弃的 getUserProfile
      if (typeof wx.getUserProfile === 'function') {
        wx.getUserProfile({
          desc: '用于完善个人资料',
          success: (res) => resolve(res.userInfo || {}),
          fail: () => resolve({})
        });
        return;
      }

      // 都不支持则跳过
      resolve({});
    });
  }
});
