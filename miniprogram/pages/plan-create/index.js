const { request } = require('../../utils/request');
const { navigateBack } = require('../../utils/nav');

function createPlanItem(name) {
  return {
    exerciseName: name || '',
    setsCount: '4',
    reps: '8-12',
    restSeconds: '90',
    weightMode: '渐进超负荷'
  };
}

function createPlanDay(index) {
  return {
    title: `训练日 ${index}`,
    focus: '',
    items: [createPlanItem('')]
  };
}

const presetTemplates = [
  {
    name: 'PPL 增肌',
    title: 'PPL 自定义增肌',
    subtitle: '推拉腿三分化循环',
    targetType: '增肌',
    difficulty: '进阶',
    durationWeeks: '8',
    daysPerWeek: '4',
    summary: '围绕推拉腿结构安排容量与动作质量，适合稳定增肌周期。',
    days: [
      {
        title: '推',
        focus: '胸肩三头',
        items: [createPlanItem('杠铃卧推'), createPlanItem('器械肩推')]
      },
      {
        title: '拉',
        focus: '背阔肌 + 肱二头',
        items: [createPlanItem('引体向上'), createPlanItem('杠铃划船')]
      },
      {
        title: '腿',
        focus: '股四头 + 臀腿后侧',
        items: [createPlanItem('杠铃深蹲'), createPlanItem('罗马尼亚硬拉')]
      }
    ]
  },
  {
    name: '减脂循环',
    title: '代谢提升循环',
    subtitle: '高密度燃脂计划',
    targetType: '减脂',
    difficulty: '中级',
    durationWeeks: '6',
    daysPerWeek: '5',
    summary: '结合力量基础动作与短间歇循环，提高代谢效率与出勤稳定性。',
    days: [
      {
        title: '全身循环',
        focus: '力量 + 心肺',
        items: [createPlanItem('壶铃深蹲'), createPlanItem('俯卧撑')]
      },
      {
        title: '下肢强化',
        focus: '臀腿与核心',
        items: [createPlanItem('保加利亚分腿蹲'), createPlanItem('登山跑')]
      }
    ]
  }
];

Page({
  data: {
    submitting: false,
    activeDayIndex: 0,
    form: {
      title: '我的自定义计划',
      subtitle: '围绕个人目标设计训练周期',
      targetType: '增肌',
      difficulty: '进阶',
      durationWeeks: '8',
      daysPerWeek: '4',
      summary: '根据当前目标安排训练重点、动作与容量，量身定制个性化训练计划。'
    },
    days: [createPlanDay(1), createPlanDay(2)],
    presetTemplates,
    quickExercises: ['杠铃卧推', '引体向上', '杠铃深蹲', '杠铃划船', '器械肩推', '罗马尼亚硬拉']
  },

  handleBack() {
    navigateBack('/pages/plan/index');
  },

  handleFormInput(event) {
    const field = event.currentTarget.dataset.field;
    const form = Object.assign({}, this.data.form, {
      [field]: event.detail.value
    });
    this.setData({ form });
  },

  activateDay(event) {
    this.setData({ activeDayIndex: event.currentTarget.dataset.dayIndex });
  },

  applyTemplate(event) {
    const template = this.data.presetTemplates[event.currentTarget.dataset.index];
    if (!template) {
      return;
    }

    this.setData({
      activeDayIndex: 0,
      form: {
        title: template.title,
        subtitle: template.subtitle,
        targetType: template.targetType,
        difficulty: template.difficulty,
        durationWeeks: template.durationWeeks,
        daysPerWeek: template.daysPerWeek,
        summary: template.summary
      },
      days: template.days.map((day) => ({
        title: day.title,
        focus: day.focus,
        items: day.items.map((item) => Object.assign({}, item))
      }))
    });
  },

  appendQuickExercise(event) {
    const name = event.currentTarget.dataset.name;
    const activeDayIndex = this.data.activeDayIndex || 0;
    const days = this.data.days.slice();
    days[activeDayIndex].items = days[activeDayIndex].items.concat([createPlanItem(name)]);
    this.setData({ days });
  },

  handleDayInput(event) {
    const { dayIndex, field } = event.currentTarget.dataset;
    const days = this.data.days.slice();
    days[dayIndex] = Object.assign({}, days[dayIndex], {
      [field]: event.detail.value
    });
    this.setData({ days });
  },

  handleItemInput(event) {
    const { dayIndex, itemIndex, field } = event.currentTarget.dataset;
    const days = this.data.days.slice();
    const items = days[dayIndex].items.slice();
    items[itemIndex] = Object.assign({}, items[itemIndex], {
      [field]: event.detail.value
    });
    days[dayIndex] = Object.assign({}, days[dayIndex], { items });
    this.setData({ days });
  },

  addDay() {
    const days = this.data.days.concat([createPlanDay(this.data.days.length + 1)]);
    const form = Object.assign({}, this.data.form, {
      daysPerWeek: String(Math.max(Number(this.data.form.daysPerWeek) || 0, days.length))
    });
    this.setData({
      days,
      form,
      activeDayIndex: days.length - 1
    });
  },

  removeDay(event) {
    if (this.data.days.length <= 1) {
      wx.showToast({ title: '至少保留 1 个训练日', icon: 'none' });
      return;
    }

    const dayIndex = event.currentTarget.dataset.dayIndex;
    const days = this.data.days.slice();
    days.splice(dayIndex, 1);
    this.setData({
      days,
      activeDayIndex: Math.max(0, Math.min(this.data.activeDayIndex, days.length - 1))
    });
  },

  addItem(event) {
    const dayIndex = event.currentTarget.dataset.dayIndex;
    const days = this.data.days.slice();
    days[dayIndex].items = days[dayIndex].items.concat([createPlanItem('')]);
    this.setData({ days });
  },

  removeItem(event) {
    const { dayIndex, itemIndex } = event.currentTarget.dataset;
    const days = this.data.days.slice();
    if (days[dayIndex].items.length <= 1) {
      wx.showToast({ title: '每个训练日至少保留 1 个动作', icon: 'none' });
      return;
    }
    days[dayIndex].items.splice(itemIndex, 1);
    this.setData({ days });
  },

  submitPlan() {
    if (this.data.submitting) {
      return;
    }

    const form = this.data.form;
    const days = this.data.days;
    if (!String(form.title || '').trim()) {
      wx.showToast({ title: '请输入计划标题', icon: 'none' });
      return;
    }

    const invalidDay = days.find((day) => !String(day.title || '').trim());
    if (invalidDay) {
      wx.showToast({ title: '请完善训练日标题', icon: 'none' });
      return;
    }

    const invalidItem = days.find((day) => day.items.find((item) => !String(item.exerciseName || '').trim()));
    if (invalidItem) {
      wx.showToast({ title: '请填写动作名称', icon: 'none' });
      return;
    }

    const payload = {
      title: form.title,
      subtitle: form.subtitle,
      targetType: form.targetType,
      difficulty: form.difficulty,
      durationWeeks: Number(form.durationWeeks) || 0,
      daysPerWeek: Number(form.daysPerWeek) || days.length,
      summary: form.summary,
      days: days.map((day) => ({
        title: day.title,
        focus: day.focus,
        items: day.items.map((item) => ({
          exerciseName: item.exerciseName,
          setsCount: Number(item.setsCount) || 0,
          reps: item.reps,
          restSeconds: Number(item.restSeconds) || 0,
          weightMode: item.weightMode
        }))
      }))
    };

    this.setData({ submitting: true });
    request({
      url: '/plans/custom',
      method: 'POST',
      data: payload,
      mockData: { created: true, planId: Date.now() }
    }).then(() => {
      wx.showToast({ title: '计划已创建', icon: 'success' });
      setTimeout(() => {
        wx.navigateBack();
      }, 600);
    }).finally(() => {
      this.setData({ submitting: false });
    });
  }
});
