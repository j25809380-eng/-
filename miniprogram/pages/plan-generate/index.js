const { request } = require('../../utils/request');
const { navigateBack } = require('../../utils/nav');

function findByKey(list, key) {
  if (!list || !key) return null;
  for (var i = 0; i < list.length; i++) {
    if (list[i].key === key) return list[i];
  }
  return null;
}

Page({
  data: {
    goals: [],
    muscleGroups: [],
    levels: [],
    splitTypes: [],
    selectedGoal: '',
    selectedMuscle: '',
    selectedLevel: '',
    selectedSplit: '',
    mode: 'single',
    generating: false,
    previewText: ''
  },

  handleBack() {
    navigateBack('/pages/home/index');
  },

  onLoad() {
    request({
      url: '/plans/generate/options',
      mockData: {
        goals: ['增肌', '减脂', '维持'],
        muscleGroups: [
          { key: '胸', label: '胸部' }, { key: '背', label: '背部' },
          { key: '腿', label: '腿部' }, { key: '肩', label: '肩部' },
          { key: '手臂', label: '手臂' }, { key: '核心', label: '核心' },
          { key: '全身', label: '全身' }
        ],
        levels: [
          { key: '新手', label: '新手' },
          { key: '进阶', label: '进阶' },
          { key: '高级', label: '高级' }
        ],
        splitTypes: [
          { key: 'push', label: '推日（胸+肩+三头）' },
          { key: 'pull', label: '拉日（背+二头）' },
          { key: 'legs', label: '腿日（股四+腘绳+臀）' },
          { key: 'shoulders_arms', label: '肩臂日' },
          { key: 'full', label: '全身训练' }
        ]
      }
    }).then((opts) => {
      this.setData({
        goals: opts.goals || [],
        muscleGroups: opts.muscleGroups || [],
        levels: opts.levels || [],
        splitTypes: opts.splitTypes || []
      });
    });
  },

  selectGoal(event) {
    this.setData({ selectedGoal: event.currentTarget.dataset.value }, () => this.updatePreview());
  },

  selectMuscle(event) {
    this.setData({ selectedMuscle: event.currentTarget.dataset.value }, () => this.updatePreview());
  },

  selectLevel(event) {
    this.setData({ selectedLevel: event.currentTarget.dataset.value }, () => this.updatePreview());
  },

  selectSplit(event) {
    this.setData({ selectedSplit: event.currentTarget.dataset.value }, () => this.updatePreview());
  },

  switchMode(event) {
    this.setData({ mode: event.currentTarget.dataset.mode }, () => this.updatePreview());
  },

  updatePreview() {
    var d = this.data;
    var parts = [];
    if (d.selectedGoal) parts.push('目标: ' + d.selectedGoal);

    if (d.mode === 'split') {
      if (d.selectedSplit) {
        var s = findByKey(d.splitTypes, d.selectedSplit);
        parts.push(s ? s.label : d.selectedSplit);
      } else {
        parts.push('请选择分训类型');
      }
    } else {
      if (d.selectedMuscle) {
        var m = findByKey(d.muscleGroups, d.selectedMuscle);
        parts.push(m ? m.label : d.selectedMuscle);
      } else {
        parts.push('请选择部位');
      }
    }

    if (d.selectedLevel) {
      var l = findByKey(d.levels, d.selectedLevel);
      parts.push(l ? l.label : d.selectedLevel);
    }

    this.setData({ previewText: parts.join(' · ') });
  },

  generatePlan() {
    if (this.data.generating) return;

    var d = this.data;
    if (!d.selectedGoal) {
      wx.showToast({ title: '请选择训练目标', icon: 'none' });
      return;
    }
    if (!d.selectedLevel) {
      wx.showToast({ title: '请选择训练水平', icon: 'none' });
      return;
    }

    this.setData({ generating: true });
    wx.showLoading({ title: '智能生成中...' });

    var apiUrl, apiData;

    if (d.mode === 'split') {
      if (!d.selectedSplit) {
        wx.showToast({ title: '请选择分训类型', icon: 'none' });
        this.setData({ generating: false });
        wx.hideLoading();
        return;
      }
      apiUrl = '/plans/generate/split';
      apiData = { goal: d.selectedGoal, level: d.selectedLevel, splitType: d.selectedSplit };
    } else {
      if (!d.selectedMuscle) {
        wx.showToast({ title: '请选择训练部位', icon: 'none' });
        this.setData({ generating: false });
        wx.hideLoading();
        return;
      }
      apiUrl = '/plans/generate';
      apiData = { goal: d.selectedGoal, muscleGroup: d.selectedMuscle, level: d.selectedLevel };
    }

    request({
      url: apiUrl,
      method: 'POST',
      data: apiData,
      mockData: this.buildMockPlan()
    }).then((plan) => {
      wx.hideLoading();
      wx.setStorageSync('fitnote_generated_plan', plan);
      wx.navigateTo({
        url: '/pages/plan-result/index?mode=' + d.mode + '&goal=' + d.selectedGoal + '&level=' + d.selectedLevel
      });
    }).catch(() => {
      wx.hideLoading();
      wx.showToast({ title: '生成失败，请重试', icon: 'none' });
    }).finally(() => {
      this.setData({ generating: false });
    });
  },

  buildMockPlan() {
    var d = this.data;
    var focus = d.mode === 'split' ? '自动分训日' : (d.selectedMuscle || '全身');
    return {
      goal: d.selectedGoal || '增肌',
      muscleGroup: focus,
      level: d.selectedLevel || '进阶',
      title: focus + '训练',
      focus: (d.selectedGoal || '增肌') + ' · ' + focus,
      totalExercises: 5,
      totalSets: 20,
      exercises: [
        { exerciseId: 1, exerciseName: '杠铃卧推', category: '胸部', equipment: '杠铃', difficulty: '进阶', primaryMuscle: '胸大肌', isCompound: true, priority: 10, sets: 4, reps: 10, repsDisplay: '8-10', restSeconds: 90, weightMode: '渐进超负荷', description: '杠铃卧推是上肢力量的王牌动作。', movementSteps: '仰卧于平板凳\n肩胛骨收紧\n下放至胸部\n推起', tips: '保持肩胛骨收紧' },
        { exerciseId: 2, exerciseName: '上斜哑铃卧推', category: '胸部', equipment: '哑铃', difficulty: '中级', primaryMuscle: '胸大肌上部', isCompound: true, priority: 8, sets: 3, reps: 12, repsDisplay: '10-12', restSeconds: 75, weightMode: '中等重量', description: '针对上胸的经典动作。', movementSteps: '凳面30-45°\n双手持哑铃\n推起', tips: '角度不宜超过45°' },
        { exerciseId: 3, exerciseName: '龙门架夹胸', category: '胸部', equipment: '龙门架', difficulty: '初级', primaryMuscle: '胸大肌', isCompound: false, priority: 6, sets: 3, reps: 12, repsDisplay: '12-15', restSeconds: 60, weightMode: '轻重量', description: '孤立刺激胸大肌。', movementSteps: '滑轮肩高\n抓握手柄\n向前夹胸', tips: '肘部角度不变' },
        { exerciseId: 4, exerciseName: '杠铃推举', category: '肩部', equipment: '杠铃', difficulty: '进阶', primaryMuscle: '三角肌前束', isCompound: true, priority: 9, sets: 4, reps: 10, repsDisplay: '8-10', restSeconds: 90, weightMode: '渐进超负荷', description: '肩部力量和维度的核心动作。', movementSteps: '坐姿杠铃于锁骨前\n推至头顶', tips: '核心收紧' },
        { exerciseId: 5, exerciseName: '哑铃侧平举', category: '肩部', equipment: '哑铃', difficulty: '初级', primaryMuscle: '三角肌中束', isCompound: false, priority: 8, sets: 4, reps: 12, repsDisplay: '12-15', restSeconds: 45, weightMode: '轻重量', description: '打造肩宽的经典动作。', movementSteps: '双手持哑铃\n平举至肩高', tips: '不借力摆动' }
      ]
    };
  }
});
