const { request, uploadFile, resolveFileUrl } = require('../../utils/request');
const mock = require('../../utils/mock');
const { navigateBack } = require('../../utils/nav');

Page({
  data: {
    user: {},
    profile: {},
    stats: {},
    avatarPreview: '',
    uploadingAvatar: false
  },

  handleBack() {
    navigateBack('/pages/home/index');
  },

  onShow() {
    request({
      url: '/users/me',
      mockData: mock.userProfile
    }).then((res) => {
      this.setData({
        user: res.user,
        profile: res.profile,
        stats: res.stats,
        avatarPreview: resolveFileUrl(res.user.avatarUrl)
      });
    });
  },

  updateProfileField(event) {
    const field = event.currentTarget.dataset.field;
    this.setData({
      [`profile.${field}`]: event.detail.value
    });
  },

  updateUserField(event) {
    const field = event.currentTarget.dataset.field;
    this.setData({
      [`user.${field}`]: event.detail.value
    });
  },

  chooseAvatar() {
    if (this.data.uploadingAvatar) {
      return;
    }

    wx.chooseMedia({
      count: 1,
      mediaType: ['image'],
      sourceType: ['album', 'camera'],
      success: (res) => {
        const file = res.tempFiles && res.tempFiles[0];
        if (!file) {
          return;
        }

        this.setData({ uploadingAvatar: true });
        uploadFile({
          url: '/files/avatar',
          filePath: file.tempFilePath,
          mockData: {
            fileName: `mock-avatar-${Date.now()}.png`,
            url: file.tempFilePath
          }
        }).then((uploadRes) => {
          this.setData({
            'user.avatarUrl': uploadRes.url,
            avatarPreview: resolveFileUrl(uploadRes.url)
          });
          this.syncUserState();
          wx.showToast({
            title: '头像已更新',
            icon: 'success'
          });
        }).catch(() => {
          wx.showToast({
            title: '上传失败',
            icon: 'none'
          });
        }).finally(() => {
          this.setData({ uploadingAvatar: false });
        });
      }
    });
  },

  saveProfile() {
    request({
      url: '/users/me/profile',
      method: 'PUT',
      data: {
        nickname: this.data.user.nickname,
        gender: this.data.profile.gender,
        heightCm: Number(this.data.profile.heightCm),
        weightKg: Number(this.data.profile.weightKg),
        bodyFatRate: Number(this.data.profile.bodyFatRate),
        targetType: this.data.profile.targetType,
        targetWeightKg: Number(this.data.profile.targetWeightKg),
        trainingLevel: this.data.profile.trainingLevel,
        bio: this.data.profile.bio,
        avatarUrl: this.data.user.avatarUrl
      },
      mockData: { updated: true }
    }).then(() => {
      this.syncUserState();
      wx.showToast({
        title: '资料已保存',
        icon: 'success'
      });
    });
  },

  syncUserState() {
    const app = getApp();
    const user = Object.assign({}, app.globalData.user || {}, this.data.user, {
      avatarUrl: this.data.user.avatarUrl
    });
    app.globalData.user = user;
    wx.setStorageSync('fitnote_user', user);
  },

  goRanking() {
    wx.navigateTo({ url: '/pages/ranking/index' });
  }
});
