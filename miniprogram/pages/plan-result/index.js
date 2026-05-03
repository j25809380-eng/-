const { request } = require('../../utils/request');
const { navigateBack } = require('../../utils/nav');

Page({
  data: {
    plan: null,
    mode: 'single',
    goal: '',
    level: '',
    completedMap: {},
    refreshing: false
  },

  handleBack() {
    navigateBack('/pages/home/index');
  },

  onLoad(options) {
    try {
      var plan = wx.getStorageSync('fitnote_generated_plan');
      wx.removeStorageSync('fitnote_generated_plan');

      if (!plan) {
        wx.showToast({ title: '计划数据为空，请重新生成', icon: 'none' });
        return;
      }

      this.setData({
        plan: plan,
        mode: options.mode || 'single',
        goal: options.goal || '',
        level: options.level || '',
        completedMap: {}
      });
    } catch (e) {
      wx.showToast({ title: '数据加载失败', icon: 'none' });
    }
  },

  toggleComplete(event) {
    const id = event.currentTarget.dataset.id;
    const completedMap = Object.assign({}, this.data.completedMap);
    completedMap[id] = !completedMap[id];
    this.setData({ completedMap });
  },

  refreshPlan() {
    if (this.data.refreshing) return;

    const { mode, goal, level, plan } = this.data;
    this.setData({ refreshing: true });
    wx.showLoading({ title: '重新生成...' });

    let apiUrl, apiData;

    if (mode === 'split') {
      apiUrl = '/plans/generate/split';
      apiData = {
        goal, level,
        splitType: plan.splitType
      };
    } else {
      apiUrl = '/plans/generate/refresh';
      apiData = {
        goal,
        muscleGroup: plan.muscleGroup,
        level
      };
    }

    request({
      url: apiUrl,
      method: 'POST',
      data: apiData
    }).then((newPlan) => {
      wx.hideLoading();
      this.setData({
        plan: newPlan,
        completedMap: {},
        refreshing: false
      });
      wx.showToast({ title: '已换一组', icon: 'success' });
    }).catch(() => {
      wx.hideLoading();
      this.setData({ refreshing: false });
      wx.showToast({ title: '刷新失败', icon: 'none' });
    });
  },

  startWorkout() {
    const converter = require('../../utils/plan-converter');
    const blueprint = converter.fromGeneratedPlan(this.data.plan);
    if (blueprint) {
      wx.setStorageSync('fitnote_plan_blueprint', blueprint);
      wx.navigateTo({ url: '/pages/workout-editor/index?fromPlan=1' });
    } else {
      wx.showToast({ title: '暂无训练数据', icon: 'none' });
    }
  },

  getCompletedCount() {
    return Object.values(this.data.completedMap).filter(Boolean).length;
  },

  getTotalCount() {
    const plan = this.data.plan;
    return plan && plan.exercises ? plan.exercises.length : 0;
  }
});
