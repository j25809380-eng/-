const { request } = require('../../utils/request');
const { navigateBack } = require('../../utils/nav');

// 常见食物预设库
const FOOD_PRESETS = [
  { name: '鸡胸肉(100g)', mealType: 'lunch', kcal: 133, protein: 31, carbs: 0, fat: 1.2 },
  { name: '鸡蛋(2个)', mealType: 'breakfast', kcal: 144, protein: 12.6, carbs: 2.8, fat: 9 },
  { name: '米饭(1碗 200g)', mealType: 'lunch', kcal: 232, protein: 4.4, carbs: 51.6, fat: 0.6 },
  { name: '燕麦(50g)', mealType: 'breakfast', kcal: 189, protein: 6.7, carbs: 33, fat: 3.4 },
  { name: '全麦面包(2片)', mealType: 'breakfast', kcal: 160, protein: 6, carbs: 30, fat: 2 },
  { name: '牛肉(150g)', mealType: 'lunch', kcal: 250, protein: 36, carbs: 0, fat: 12 },
  { name: '三文鱼(150g)', mealType: 'lunch', kcal: 312, protein: 30, carbs: 0, fat: 21 },
  { name: '西兰花(200g)', mealType: 'dinner', kcal: 68, protein: 5.6, carbs: 12, fat: 0.6 },
  { name: '香蕉(1根)', mealType: 'snack', kcal: 105, protein: 1.3, carbs: 27, fat: 0.4 },
  { name: '乳清蛋白(1勺 30g)', mealType: 'snack', kcal: 120, protein: 24, carbs: 2, fat: 1.5 },
  { name: '牛奶(250ml)', mealType: 'breakfast', kcal: 155, protein: 8, carbs: 12, fat: 8 },
  { name: '红薯(200g)', mealType: 'lunch', kcal: 172, protein: 2.4, carbs: 40, fat: 0.2 },
  { name: '坚果(30g)', mealType: 'snack', kcal: 180, protein: 5, carbs: 6, fat: 16 },
  { name: '意面(150g 熟)', mealType: 'lunch', kcal: 198, protein: 7.2, carbs: 39, fat: 1.2 },
  { name: '豆腐(200g)', mealType: 'dinner', kcal: 120, protein: 12, carbs: 4, fat: 6 },
  { name: '虾仁(150g)', mealType: 'lunch', kcal: 150, protein: 30, carbs: 0, fat: 2.3 },
  { name: '牛油果(半个)', mealType: 'snack', kcal: 160, protein: 2, carbs: 9, fat: 15 },
  { name: '希腊酸奶(200g)', mealType: 'breakfast', kcal: 120, protein: 20, carbs: 8, fat: 0.7 },
  { name: '糙米饭(1碗 200g)', mealType: 'lunch', kcal: 246, protein: 5, carbs: 53, fat: 2 },
  { name: '金枪鱼罐头(100g)', mealType: 'lunch', kcal: 116, protein: 26, carbs: 0, fat: 0.8 },
  { name: '蛋白粉奶昔', mealType: 'snack', kcal: 220, protein: 30, carbs: 15, fat: 4 },
  { name: '煎牛排(200g)', mealType: 'dinner', kcal: 380, protein: 44, carbs: 0, fat: 22 },
  { name: '蔬菜沙拉(大份)', mealType: 'dinner', kcal: 80, protein: 3, carbs: 10, fat: 4 },
  { name: '黑巧克力(20g)', mealType: 'snack', kcal: 110, protein: 1.5, carbs: 10, fat: 7.5 },
  { name: '紫薯(200g)', mealType: 'lunch', kcal: 164, protein: 2.8, carbs: 38, fat: 0.2 },
  { name: '鳕鱼(150g)', mealType: 'dinner', kcal: 135, protein: 27, carbs: 0, fat: 2 },
  { name: '玉米(1根)', mealType: 'snack', kcal: 140, protein: 4, carbs: 30, fat: 2 },
  { name: '豆浆(300ml)', mealType: 'breakfast', kcal: 90, protein: 9, carbs: 6, fat: 3 },
  { name: '蛋白棒(1根)', mealType: 'snack', kcal: 200, protein: 20, carbs: 20, fat: 6 },
  { name: '炒蛋(3个)', mealType: 'breakfast', kcal: 240, protein: 18, carbs: 2, fat: 18 }
];

Page({
  data: {
    goalType: 'maintain',
    goal: null,
    analysis: null,
    logs: [],
    showForm: false,
    showFoodPresets: false,
    foodPresets: FOOD_PRESETS,
    filteredPresets: FOOD_PRESETS,
    presetKeyword: '',
    submitting: false,
    selectedDate: '',
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
    this.setData({ selectedDate: this.formatDate(new Date()) });
    this.loadAll();
  },

  formatDate(date) {
    const y = date.getFullYear();
    const m = String(date.getMonth() + 1).padStart(2, '0');
    const d = String(date.getDate()).padStart(2, '0');
    return `${y}-${m}-${d}`;
  },

  loadAll() {
    const dateParam = this.data.selectedDate;
    const isToday = dateParam === this.formatDate(new Date());

    Promise.all([
      request({ url: '/nutrition/goal' }),
      isToday ? request({ url: '/nutrition/today' }) : Promise.resolve(null),
      request({ url: '/nutrition/logs', data: { date: dateParam, page: 0, size: 50 } })
    ]).then(([goal, analysis, logPage]) => {
      const logs = logPage && logPage.items ? logPage.items : (Array.isArray(logPage) ? logPage : []);
      this.setData({
        goalType: goal.goalType,
        goal,
        analysis: analysis || this.buildBasicAnalysis(logs, goal),
        logs
      });
    });
  },

  buildBasicAnalysis(logs, goal) {
    if (!logs.length || !goal) return null;
    let totalKcal = 0, totalProtein = 0, totalCarbs = 0, totalFat = 0;
    logs.forEach((log) => {
      totalKcal += log.kcal || 0;
      totalProtein += log.protein || 0;
      totalCarbs += log.carbs || 0;
      totalFat += log.fat || 0;
    });
    return {
      totalKcal: Math.round(totalKcal),
      totalProtein: Math.round(totalProtein * 10) / 10,
      totalCarbs: Math.round(totalCarbs * 10) / 10,
      totalFat: Math.round(totalFat * 10) / 10,
      kcalPct: goal.targetKcal > 0 ? Math.round(totalKcal / goal.targetKcal * 100) : 0,
      proteinPct: goal.targetProtein > 0 ? Math.round(totalProtein / goal.targetProtein * 100) : 0,
      carbsPct: goal.targetCarbs > 0 ? Math.round(totalCarbs / goal.targetCarbs * 100) : 0,
      fatPct: goal.targetFat > 0 ? Math.round(totalFat / goal.targetFat * 100) : 0,
      score: 0,
      grade: '无数据',
      suggestions: ['尚未记录今日饮食，添加食物后即可获得分析。'],
      goal: { goalType: goal.goalType, targetKcal: goal.targetKcal, targetProtein: goal.targetProtein, targetCarbs: goal.targetCarbs, targetFat: goal.targetFat },
      status: { kcal: '无数据', protein: '无数据', carbs: '无数据', fat: '无数据' }
    };
  },

  onDateChange(event) {
    this.setData({ selectedDate: event.detail.value }, () => {
      this.loadAll();
    });
  },

  goPrevDay() {
    const d = new Date(this.data.selectedDate);
    d.setDate(d.getDate() - 1);
    this.setData({ selectedDate: this.formatDate(d) }, () => { this.loadAll(); });
  },

  goNextDay() {
    const d = new Date(this.data.selectedDate);
    d.setDate(d.getDate() + 1);
    const today = this.formatDate(new Date());
    if (this.formatDate(d) > today) return;
    this.setData({ selectedDate: this.formatDate(d) }, () => { this.loadAll(); });
  },

  switchGoal(event) {
    const goalType = event.currentTarget.dataset.type;
    this.setData({ goalType });
    request({
      url: '/nutrition/goal',
      method: 'PUT',
      data: { goalType }
    }).then(() => { this.loadAll(); });
  },

  toggleForm() {
    this.setData({ showForm: !this.data.showForm, showFoodPresets: false });
  },

  toggleFoodPresets() {
    this.setData({ showFoodPresets: !this.data.showFoodPresets, filteredPresets: FOOD_PRESETS, presetKeyword: '' });
  },

  searchPreset(event) {
    const keyword = (event.detail.value || '').toLowerCase().trim();
    this.setData({
      presetKeyword: keyword,
      filteredPresets: keyword
        ? FOOD_PRESETS.filter((f) => f.name.toLowerCase().includes(keyword))
        : FOOD_PRESETS
    });
  },

  selectPreset(event) {
    const food = FOOD_PRESETS[event.currentTarget.dataset.index];
    if (!food) return;
    this.setData({
      form: {
        name: food.name,
        mealType: food.mealType,
        kcal: String(food.kcal),
        protein: String(food.protein),
        carbs: String(food.carbs),
        fat: String(food.fat)
      },
      showFoodPresets: false
    });
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
