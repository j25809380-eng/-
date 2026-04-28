const { request } = require('../../utils/request');
const mock = require('../../utils/mock');
const { navigateBack } = require('../../utils/nav');

Page({
  data: {
    detail: null
  },

  handleBack() {
    navigateBack('/pages/exercise/index');
  },

  onLoad(options) {
    const id = options.id || 2;
    request({
      url: `/exercises/${id}`,
      mockData: mock.exerciseDetail
    }).then((detail) => {
      this.setData({ detail });
    });
  },

  startWorkout() {
    const { detail } = this.data;
    wx.navigateTo({
      url: `/pages/workout-editor/index?exerciseId=${detail.id}&exerciseName=${detail.name}&focus=${detail.primaryMuscle}`
    });
  }
});
