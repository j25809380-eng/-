const now = new Date().toISOString().slice(0, 10);

module.exports = {
  login: {
    token: 'fitnote-demo-token',
    user: {
      id: 1,
      nickname: '林燃',
      avatarUrl: ''
    }
  },
  dashboardHome: {
    hero: {
      title: '选择你的动能',
      subtitle: '保持训练节奏，今天也把状态拉满',
      targetType: '增肌',
      trainingLevel: '进阶'
    },
    readiness: {
      mealReady: '训练前 1 小时已进食',
      hydration: '85% 最佳',
      recovery: '高性能表现'
    },
    overview: {
      weeklySessions: 4,
      activePlans: 3,
      goalWeight: 80
    },
    quickPlans: [
      { id: 1, title: '增肌训练', difficulty: '进阶', durationWeeks: 8 },
      { id: 2, title: '力量进阶', difficulty: '高阶', durationWeeks: 6 },
      { id: 3, title: '代谢提升', difficulty: '中级', durationWeeks: 4 }
    ],
    quickActions: [
      { name: '开始训练', path: '/pages/workout-editor/index' },
      { name: '训练历史', path: '/pages/history/index' },
      { name: '动作库', path: '/pages/exercise/index' }
    ]
  },
  plans: [
    {
      id: 1,
      title: '增肌训练',
      subtitle: '上肢与下肢交替推进',
      targetType: '增肌',
      difficulty: '进阶',
      durationWeeks: 8,
      daysPerWeek: 4,
      summary: '适合已经具备基础动作模式的训练者，重点提升容量与力量输出。'
    },
    {
      id: 2,
      title: 'HIIT 巅峰燃脂',
      subtitle: '高密度短时训练',
      targetType: '减脂',
      difficulty: '中级',
      durationWeeks: 6,
      daysPerWeek: 5,
      summary: '通过高强度循环训练提升热量消耗与代谢效率。'
    },
    {
      id: 3,
      title: '力量举专项',
      subtitle: '三大项稳定突破',
      targetType: '力量',
      difficulty: '高阶',
      durationWeeks: 12,
      daysPerWeek: 4,
      summary: '围绕卧推、深蹲、硬拉建立结构化周期。'
    }
  ],
  myPlans: [
    {
      id: 101,
      title: 'PPL 自定义增肌',
      targetType: '增肌',
      difficulty: '进阶',
      durationWeeks: 8,
      daysPerWeek: 4
    }
  ],
  planDetail: {
    id: 1,
    title: '增肌训练',
    subtitle: '上肢与下肢交替推进',
    targetType: '增肌',
    difficulty: '进阶',
    durationWeeks: 8,
    daysPerWeek: 4,
    summary: '适合已具备基础动作模式的训练者，重点提升容量与力量输出。',
    days: [
      {
        id: 101,
        dayNo: 1,
        title: '胸肩推进',
        focus: '胸部 + 三角肌前束',
        items: [
          { id: 1, exerciseId: 1, exerciseName: '杠铃卧推', setsCount: 5, reps: '8-10', restSeconds: 90, weightMode: '渐进超负荷' },
          { id: 2, exerciseId: 4, exerciseName: '器械肩推', setsCount: 4, reps: '10-12', restSeconds: 75, weightMode: '中等重量' }
        ]
      },
      {
        id: 102,
        dayNo: 2,
        title: '背部拉力',
        focus: '背阔肌 + 肱二头',
        items: [
          { id: 3, exerciseId: 3, exerciseName: '引体向上', setsCount: 4, reps: '8-10', restSeconds: 60, weightMode: '自重或负重' },
          { id: 4, exerciseId: 5, exerciseName: '杠铃划船', setsCount: 4, reps: '8-10', restSeconds: 90, weightMode: '渐进超负荷' }
        ]
      }
    ]
  },
  customPlanDetail: {
    id: 101,
    title: 'PPL 自定义增肌',
    subtitle: '推拉腿三分化循环',
    targetType: '增肌',
    difficulty: '进阶',
    durationWeeks: 8,
    daysPerWeek: 4,
    summary: '围绕推拉腿结构安排容量与动作质量，适合稳定增肌周期。',
    days: [
      {
        id: 201,
        dayNo: 1,
        title: '推',
        focus: '胸肩三头',
        items: [
          { id: 21, exerciseName: '杠铃卧推', setsCount: 5, reps: '5-8', restSeconds: 120, weightMode: '渐进超负荷' },
          { id: 22, exerciseName: '器械肩推', setsCount: 4, reps: '8-10', restSeconds: 90, weightMode: '中高重量' }
        ]
      },
      {
        id: 202,
        dayNo: 2,
        title: '拉',
        focus: '背阔肌 + 肱二头',
        items: [
          { id: 23, exerciseName: '引体向上', setsCount: 4, reps: '8-10', restSeconds: 75, weightMode: '自重或负重' },
          { id: 24, exerciseName: '杠铃划船', setsCount: 4, reps: '8-10', restSeconds: 90, weightMode: '渐进超负荷' }
        ]
      },
      {
        id: 203,
        dayNo: 3,
        title: '腿',
        focus: '股四头 + 臀腿后侧',
        items: [
          { id: 25, exerciseName: '杠铃深蹲', setsCount: 5, reps: '5-8', restSeconds: 150, weightMode: '力量优先' },
          { id: 26, exerciseName: '罗马尼亚硬拉', setsCount: 4, reps: '8-10', restSeconds: 90, weightMode: '中高重量' }
        ]
      }
    ]
  },
  exercises: [
    { id: 1, name: '杠铃卧推', category: '胸部', difficulty: '进阶', equipment: '杠铃', primaryMuscle: '胸大肌' },
    { id: 2, name: '杠铃深蹲', category: '腿部', difficulty: '进阶', equipment: '杠铃', primaryMuscle: '股四头肌' },
    { id: 3, name: '引体向上', category: '背部', difficulty: '中级', equipment: '单杠', primaryMuscle: '背阔肌' },
    { id: 4, name: '器械肩推', category: '肩部', difficulty: '中级', equipment: '器械', primaryMuscle: '三角肌' },
    { id: 5, name: '杠铃划船', category: '背部', difficulty: '进阶', equipment: '杠铃', primaryMuscle: '背阔肌' }
  ],
  exerciseDetail: {
    id: 2,
    name: '杠铃深蹲',
    category: '腿部',
    difficulty: '进阶',
    equipment: '杠铃',
    primaryMuscle: '股四头肌',
    secondaryMuscles: '臀大肌, 腘绳肌, 竖脊肌',
    description: '下肢力量王牌动作，适合提升整体力量输出与训练容量。',
    movementSteps: ['站距稳定', '吸气下蹲', '深度确认', '脚跟发力还原'],
    tips: ['核心全程保持收紧', '膝盖方向与脚尖一致', '避免塌腰与重心前移']
  },
  workoutHistory: [
    { id: 1, title: '胸部力量训练', focus: '卧推峰值输出', sessionDate: '2026-04-20', durationMinutes: 68, totalVolume: 24500, calories: 842, completionStatus: 'COMPLETED' },
    { id: 2, title: '背部拉力训练', focus: '划船与引体', sessionDate: '2026-04-18', durationMinutes: 72, totalVolume: 22100, calories: 760, completionStatus: 'COMPLETED' }
  ],
  analyticsOverview: {
    summary: {
      totalSessions: 14,
      weeklyFrequency: 4,
      totalVolume: 128500,
      prCount: 6
    },
    weightTrend: [
      { date: '2026-03-31', weightKg: 77.8 },
      { date: '2026-04-07', weightKg: 77.1 },
      { date: '2026-04-14', weightKg: 76.8 },
      { date: '2026-04-21', weightKg: 76.5 }
    ],
    monthlyReport: {
      month: 4,
      highlight: '本月已完成 14 次训练，深蹲与卧推均刷新阶段峰值。',
      focus: '建议下阶段继续强化下肢力量与恢复管理。'
    }
  },
  monthlyReport: {
    month: '2026-04',
    sessionsCount: 14,
    totalVolume: 128500,
    prCount: 6,
    highlight: '本月已完成 14 次训练，卧推和深蹲都刷新了阶段峰值。',
    focus: '下阶段建议继续保持 4 天训练频率，并加强恢复日与腿部容量安排。',
    weeklyBreakdown: [
      { label: '第 1 周', count: 3 },
      { label: '第 2 周', count: 4 },
      { label: '第 3 周', count: 3 },
      { label: '第 4 周', count: 4 }
    ],
    topExercises: [
      { exerciseName: '杠铃卧推', volume: 34500 },
      { exerciseName: '杠铃深蹲', volume: 31200 },
      { exerciseName: '引体向上', volume: 18600 },
      { exerciseName: '杠铃划船', volume: 17200 }
    ]
  },
  workoutDetail: {
    id: 1,
    title: '胸部力量训练',
    focus: '卧推峰值输出',
    sessionDate: now,
    durationMinutes: 68,
    totalVolume: 24500,
    calories: 842,
    notes: '卧推最后两组状态很好。',
    sets: [
      { id: 1, exerciseId: 1, exerciseName: '杠铃卧推', setNo: 1, weightKg: 80, reps: 10, rir: 2, remark: '', isPr: false },
      { id: 2, exerciseId: 1, exerciseName: '杠铃卧推', setNo: 2, weightKg: 85, reps: 8, rir: 1, remark: '', isPr: false },
      { id: 3, exerciseId: 1, exerciseName: '杠铃卧推', setNo: 3, weightKg: 90, reps: 6, rir: 0, remark: '阶段新高', isPr: true }
    ]
  },
  aiPrompts: {
    assistantName: 'Volt AI',
    status: '已就绪，帮助你突破极限',
    welcomeMessage: '欢迎回来。我已根据你的最近训练与恢复数据，为你准备了今日训练建议。',
    cards: [
      { title: '我今天该练什么？', type: 'training' },
      { title: '练后饮食建议', type: 'nutrition' },
      { title: '恢复评估报告', type: 'recovery' },
      { title: '近 30 天进展', type: 'trend' }
    ]
  },
  communityPosts: [
    {
      id: 1,
      authorName: '林燃',
      authorAvatar: '',
      content: '极限从来不是终点。今天卧推 90kg 刷新阶段 PR，下一次继续冲击更稳定的 5x5。#训练打卡 #卧推',
      coverImage: '',
      postType: 'PR',
      topicTags: '训练打卡,卧推,PR',
      likeCount: 128,
      commentCount: 18,
      collectCount: 6,
      liked: true,
      commentsPreview: [
        { id: 1001, authorName: 'Echo', content: '这一条卧推动作质量很稳，90kg 的控制感已经很好了。', createdAt: '2026-04-21 10:10' },
        { id: 1002, authorName: '林燃', content: '谢谢，下一次准备冲击更扎实的 5x5。', createdAt: '2026-04-21 10:26' }
      ],
      createdAt: '2026-04-21 09:30'
    },
    {
      id: 2,
      authorName: 'Echo',
      authorAvatar: '',
      content: '清晨 5:30 的训练房很安静，但每一组都在把状态往上推。今天继续稳稳打卡。',
      coverImage: '',
      postType: 'STREAK',
      topicTags: '连续训练,早训',
      likeCount: 96,
      commentCount: 12,
      collectCount: 4,
      liked: false,
      commentsPreview: [
        { id: 1003, authorName: '林燃', content: '早训执行力太强了，腿日节奏看起来很舒服。', createdAt: '2026-04-21 06:35' }
      ],
      createdAt: '2026-04-21 06:10'
    }
  ],
  communityComments: {
    1: [
      { id: 1001, authorName: 'Echo', content: '这一条卧推动作质量很稳，90kg 的控制感已经很好了。', createdAt: '2026-04-21 10:10' },
      { id: 1002, authorName: '林燃', content: '谢谢，下一次准备冲击更扎实的 5x5。', createdAt: '2026-04-21 10:26' }
    ],
    2: [
      { id: 1003, authorName: '林燃', content: '早训执行力太强了，腿日节奏看起来很舒服。', createdAt: '2026-04-21 06:35' }
    ]
  },
  rankings: [
    { rank: 1, nickname: 'Sarah.K', score: 15820, label: '王者段位' },
    { rank: 2, nickname: 'Alex.W', score: 12490, label: '稳定输出' },
    { rank: 3, nickname: 'Marcus', score: 11200, label: '爆发训练' },
    { rank: 4, nickname: '林燃', score: 9845, label: '你 (You)' }
  ],
  userProfile: {
    user: {
      id: 1,
      nickname: '林燃',
      avatarUrl: '',
      phone: ''
    },
    profile: {
      gender: '男',
      heightCm: 178,
      weightKg: 76.5,
      bodyFatRate: 16.5,
      targetType: '增肌',
      targetWeightKg: 80,
      trainingLevel: '进阶',
      bio: '每一次训练，都是写给未来自己的证明。'
    },
    stats: {
      totalSessions: 14,
      prCount: 6,
      bodyMetricCount: 8
    }
  }
};
