const { request } = require('../../utils/request');
const mock = require('../../utils/mock');
const { navigateBack } = require('../../utils/nav');

Page({
  data: {
    title: '今日训练',
    focus: '力量输出',
    notes: '',
    exercises: [],
    sets: [],
    // 计时器
    timerRunning: false,
    timerSeconds: 0,
    timerDisplay: '00:00',
    // 感受评分 1-5
    feelingScore: 0,
    feelLabels: ['很差', '较差', '一般', '不错', '超棒'],
    // 预估热量
    estimatedCalories: 0,
    saving: false
  },

  _timerInterval: null,

  handleBack() {
    navigateBack('/pages/home/index');
  },

  onLoad(options) {
    request({
      url: '/exercises',
      mockData: mock.exercises
    }).then((exercises) => {
      this.setData({ exercises });
    });

    // 检查是否从训练计划页面跳转过来（有完整计划蓝图）
    var blueprint = wx.getStorageSync('fitnote_plan_blueprint');
    if (blueprint && options.fromPlan === '1') {
      // 清除已读取的计划数据
      wx.removeStorageSync('fitnote_plan_blueprint');

      var converter = require('../../utils/plan-converter');
      var sets = converter.expandToSets(blueprint);

      this.setData({
        title: decodeURIComponent(options.title || blueprint.title || '今日训练'),
        focus: decodeURIComponent(options.focus || blueprint.focus || '力量输出'),
        sets: sets
      });
    } else if (options.exerciseId && options.exerciseName) {
      // 旧方式：从单动作跳转（兼容旧逻辑）
      this.setData({
        title: decodeURIComponent(options.title || '今日训练'),
        focus: decodeURIComponent(options.focus || '力量输出'),
        sets: [
          { exerciseId: Number(options.exerciseId), exerciseName: decodeURIComponent(options.exerciseName), setNo: 1, weightKg: 60, reps: 10, rir: 2, remark: '' }
        ]
      });
    } else {
      // 默认：手动训练
      this.setData({
        title: decodeURIComponent(options.title || '今日训练'),
        focus: decodeURIComponent(options.focus || '力量输出'),
        sets: [
          { exerciseId: 1, exerciseName: '杠铃卧推', setNo: 1, weightKg: 60, reps: 10, rir: 2, remark: '' }
        ]
      });
    }
  },

  onUnload() {
    this.stopTimer();
  },

  // ========== 计时器 ==========

  toggleTimer() {
    if (this.data.timerRunning) {
      this.pauseTimer();
    } else {
      this.startTimer();
    }
  },

  startTimer() {
    this.setData({ timerRunning: true });
    this._timerInterval = setInterval(() => {
      const seconds = this.data.timerSeconds + 1;
      this.setData({
        timerSeconds: seconds,
        timerDisplay: this.formatDuration(seconds)
      });
    }, 1000);
  },

  pauseTimer() {
    if (this._timerInterval) {
      clearInterval(this._timerInterval);
      this._timerInterval = null;
    }
    this.setData({ timerRunning: false });
  },

  stopTimer() {
    this.pauseTimer();
  },

  resetTimer() {
    this.pauseTimer();
    this.setData({ timerSeconds: 0, timerDisplay: '00:00' });
  },

  formatDuration(totalSeconds) {
    const m = Math.floor(totalSeconds / 60);
    const s = totalSeconds % 60;
    return String(m).padStart(2, '0') + ':' + String(s).padStart(2, '0');
  },

  // ========== 感受评分 ==========

  rateFeeling(event) {
    this.setData({ feelingScore: Number(event.currentTarget.dataset.score) });
  },

  // ========== 表单更新 ==========

  updateField(event) {
    const field = event.currentTarget.dataset.field;
    this.setData({ [field]: event.detail.value });
  },

  updateSet(event) {
    const index = event.currentTarget.dataset.index;
    const field = event.currentTarget.dataset.field;
    const sets = this.data.sets.slice();
    sets[index][field] = field === 'exerciseName' || field === 'remark'
      ? event.detail.value
      : Number(event.detail.value);
    this.setData({ sets });
  },

  addSet() {
    const last = this.data.sets[this.data.sets.length - 1] || { exerciseId: 1, exerciseName: '杠铃卧推', weightKg: 60, reps: 10, rir: 2 };
    const sets = this.data.sets.concat({
      exerciseId: last.exerciseId,
      exerciseName: last.exerciseName,
      setNo: this.data.sets.length + 1,
      weightKg: last.weightKg,
      reps: last.reps,
      rir: last.rir,
      remark: ''
    });
    this.setData({ sets });
  },

  removeSet(event) {
    const index = event.currentTarget.dataset.index;
    if (this.data.sets.length <= 1) return;
    const sets = this.data.sets.filter((_, i) => i !== index).map((item, i) => ({ ...item, setNo: i + 1 }));
    this.setData({ sets });
  },

  // ========== 保存 ==========

  async saveWorkout() {
    if (this.data.saving) return;
    if (!this.data.sets.length) {
      wx.showToast({ title: '请至少添加一组训练', icon: 'none' });
      return;
    }

    this.setData({ saving: true });

    // 停止计时
    if (this.data.timerRunning) {
      this.pauseTimer();
    }

    const durationMinutes = Math.max(1, Math.round(this.data.timerSeconds / 60));

    // 调用后端估算热量
    let calories = 350; // fallback
    try {
      const calResult = await request({
        url: '/workouts/estimate-calories',
        method: 'POST',
        data: {
          title: this.data.title,
          focus: this.data.focus,
          notes: this.data.notes,
          durationMinutes
        }
      });
      calories = calResult.calories || calories;
    } catch (e) {
      // 降级：用时长估算
      calories = Math.round(durationMinutes * 5.8);
    }

    const feelingScore = this.data.feelingScore || 4;

    const payload = {
      title: this.data.title,
      focus: this.data.focus,
      notes: this.data.notes,
      sessionDate: new Date().toISOString().slice(0, 10),
      durationMinutes,
      calories,
      feelingScore,
      sets: this.data.sets.map((item, index) => ({
        exerciseId: item.exerciseId || 1,
        exerciseName: item.exerciseName || '自定义动作',
        setNo: index + 1,
        weightKg: Number(item.weightKg || 0),
        reps: Number(item.reps || 0),
        rir: Number(item.rir || 0),
        remark: item.remark || ''
      }))
    };

    request({
      url: '/workouts',
      method: 'POST',
      data: payload,
      mockData: { sessionId: Date.now(), totalVolume: 12000, completed: true }
    }).then((res) => {
      wx.navigateTo({ url: `/pages/workout-complete/index?id=${res.sessionId}` });
    }).catch(() => {
      wx.showToast({ title: '保存失败，请重试', icon: 'none' });
    }).finally(() => {
      this.setData({ saving: false });
    });
  }
});
