const { request } = require('../../utils/request');
const mock = require('../../utils/mock');
const { navigateBack } = require('../../utils/nav');

Page({
  data: {
    keyword: '',
    activeCategory: '全部',
    allPlans: [],
    plans: [],
    myPlans: [],
    selectedPlan: null,
    categories: ['全部', '增肌', '减脂', '力量', '塑形']
  },

  handleBack() {
    navigateBack('/pages/home/index');
  },

  onShow() {
    this.loadPlans();
  },

  loadPlans() {
    Promise.all([
      request({
        url: '/plans',
        mockData: mock.plans
      }),
      request({
        url: '/plans/mine',
        mockData: mock.myPlans
      })
    ]).then(([plans, myPlans]) => {
      const nextSelectedId =
        (this.data.selectedPlan && this.data.selectedPlan.id) ||
        (myPlans && myPlans[0] && myPlans[0].id) ||
        (plans && plans[0] && plans[0].id);

      this.setData({
        allPlans: plans || [],
        myPlans: (myPlans || []).map((item) => Object.assign({ isCustom: true }, item))
      }, () => {
        this.applyFilters();
        if (nextSelectedId) {
          this.loadPlanDetail(nextSelectedId);
        }
      });
    });
  },

  loadPlanDetail(id) {
    request({
      url: `/plans/${id}`,
      mockData: id === 101 ? mock.customPlanDetail : mock.planDetail
    }).then((selectedPlan) => {
      this.setData({ selectedPlan });
    });
  },

  applyFilters() {
    const keyword = (this.data.keyword || '').trim().toLowerCase();
    const category = this.data.activeCategory;
    const plans = (this.data.allPlans || []).filter((plan) => {
      const matchesCategory = category === '全部' || String(plan.targetType || '').indexOf(category) > -1;
      const text = `${plan.title || ''}${plan.subtitle || ''}${plan.summary || ''}`.toLowerCase();
      const matchesKeyword = !keyword || text.indexOf(keyword) > -1;
      return matchesCategory && matchesKeyword;
    });

    this.setData({ plans });
  },

  handleInput(event) {
    this.setData({ keyword: event.detail.value }, () => {
      this.applyFilters();
    });
  },

  selectCategory(event) {
    this.setData({ activeCategory: event.currentTarget.dataset.value }, () => {
      this.applyFilters();
    });
  },

  selectPlan(event) {
    this.loadPlanDetail(event.currentTarget.dataset.id);
  },

  goGeneratePlan() {
    wx.navigateTo({ url: '/pages/plan-generate/index' });
  },

  goCreatePlan() {
    wx.navigateTo({ url: '/pages/plan-create/index' });
  },

  startPlan() {
    const converter = require('../../utils/plan-converter');
    const blueprint = converter.fromStandardPlan(this.data.selectedPlan, 0);
    if (blueprint) {
      wx.setStorageSync('fitnote_plan_blueprint', blueprint);
      wx.navigateTo({ url: '/pages/workout-editor/index?fromPlan=1' });
    } else {
      wx.navigateTo({ url: '/pages/workout-editor/index?title=' + encodeURIComponent('今日训练') + '&focus=' + encodeURIComponent('计划执行') });
    }
  }
});
