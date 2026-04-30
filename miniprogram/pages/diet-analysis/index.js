const { request } = require('../../utils/request');
const { navigateBack } = require('../../utils/nav');

Page({
  data: {
    goalType: 'maintain',
    goal: null,
    analysis: null,
    logs: [],
    showForm: false,
    submitting: false,
    form: {
      name: '',
      mealType: 'breakfast',
      kcal: '',
      protein: '',
      carbs: '',
      fat: ''
    }
  },

  handleBack() {
    navigateBack('/pages/home/index');
  },

  onShow() {
    this.loadAll();
  },

  loadAll() {
    Promise.all([
      request({ url: '/nutrition/goal' }),
      request({ url: '/nutrition/today' }),
      request({ url: '/nutrition/logs' })
    ]).then(([goal, analysis, logs]) => {
      this.setData({
        goalType: goal.goalType,
        goal,
        analysis,
        logs: logs || []
      });
    });
  },

  switchGoal(event) {
    const goalType = event.currentTarget.dataset.type;
    this.setData({ goalType });
    request({
      url: '/nutrition/goal',
      method: 'PUT',
      data: { goalType }
    }).then(() => {
      this.loadAll();
    });
  },

  toggleForm() {
    this.setData({ showForm: !this.data.showForm });
  },

  updateFormField(event) {
    const field = event.currentTarget.dataset.field;
    this.setData({ [`form.${field}`]: event.detail.value });
  },

  submitLog() {
    const { name, mealType, kcal, protein, carbs, fat } = this.data.form;
    if (!name || !kcal) {
      wx.showToast({ title: '请填写食物名称和热量', icon: 'none' });
      return;
    }

    this.setData({ submitting: true });
    request({
      url: '/nutrition/log',
      method: 'POST',
      data: {
        name,
        mealType,
        kcal: parseInt(kcal) || 0,
        protein: parseFloat(protein) || 0,
        carbs: parseFloat(carbs) || 0,
        fat: parseFloat(fat) || 0
      }
    }).then(() => {
      this.setData({
        showForm: false,
        form: { name: '', mealType: 'breakfast', kcal: '', protein: '', carbs: '', fat: '' }
      });
      this.loadAll();
      wx.showToast({ title: '已添加', icon: 'success' });
    }).finally(() => {
      this.setData({ submitting: false });
    });
  },

  deleteLog(event) {
    const id = event.currentTarget.dataset.id;
    request({
      url: `/nutrition/log/${id}`,
      method: 'DELETE'
    }).then(() => {
      this.loadAll();
      wx.showToast({ title: '已删除', icon: 'success' });
    });
  },

  getStatusColor(status) {
    if (status === '达标') return '#c3f400';
    if (status === '偏低') return '#ff8c42';
    return '#ff4d4f';
  },

  getScoreColor(score) {
    if (score >= 85) return '#c3f400';
    if (score >= 70) return '#ffc107';
    if (score >= 50) return '#ff8c42';
    return '#ff4d4f';
  }
});
