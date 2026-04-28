const { request } = require('../../utils/request');
const mock = require('../../utils/mock');
const { navigateBack } = require('../../utils/nav');

Page({
  data: {
    title: '今日训练',
    focus: '胸部推进',
    notes: '',
    exercises: [],
    sets: [
      { exerciseId: 1, exerciseName: '杠铃卧推', setNo: 1, weightKg: 80, reps: 10, rir: 2, remark: '' },
      { exerciseId: 1, exerciseName: '杠铃卧推', setNo: 2, weightKg: 85, reps: 8, rir: 1, remark: '' }
    ]
  },

  handleBack() {
    navigateBack('/pages/home/index');
  },

  onLoad(options) {
    this.setData({
      title: options.title || '今日训练',
      focus: options.focus || '力量输出'
    });
    request({
      url: '/exercises',
      mockData: mock.exercises
    }).then((exercises) => {
      this.setData({ exercises });
      if (options.exerciseId && options.exerciseName) {
        this.setData({
          sets: [
            { exerciseId: Number(options.exerciseId), exerciseName: options.exerciseName, setNo: 1, weightKg: 60, reps: 10, rir: 2, remark: '' }
          ]
        });
      }
    });
  },

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
    if (this.data.sets.length <= 1) {
      return;
    }
    const sets = this.data.sets.filter((_, i) => i !== index).map((item, i) => ({ ...item, setNo: i + 1 }));
    this.setData({ sets });
  },

  saveWorkout() {
    const payload = {
      title: this.data.title,
      focus: this.data.focus,
      notes: this.data.notes,
      durationMinutes: 60,
      calories: 520,
      feelingScore: 4,
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
      mockData: { sessionId: 1, completed: true }
    }).then((res) => {
      wx.navigateTo({ url: `/pages/workout-complete/index?id=${res.sessionId}` });
    });
  }
});
