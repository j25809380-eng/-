/**
 * 训练计划转换工具
 *
 * 统一三种不同来源的训练计划数据格式：
 *   1. 标准计划 (plan/index → days[n].items[])
 *   2. 生成计划 (plan-generate → exercises[])
 *   3. 分训日计划 (plan-generate/split → exercises[])
 *
 * 统一输出格式：
 * {
 *   title: "推日",
 *   focus: "胸部 + 肩部 + 肱三头肌",
 *   exercises: [
 *     { exerciseId, exerciseName, sets, reps, repsDisplay, restSeconds, weightMode }
 *   ]
 * }
 */

/**
 * 将标准计划（含 days）转为统一格式
 * selectedPlan: { title, days: [{ items: [{ exerciseId, exerciseName, setsCount, reps, weightMode }] }] }
 * dayIndex: 选取第几个训练日（默认 0）
 */
function fromStandardPlan(selectedPlan, dayIndex) {
  if (!selectedPlan || !selectedPlan.days || !selectedPlan.days.length) {
    return null;
  }

  var day = selectedPlan.days[dayIndex || 0];
  if (!day || !day.items || !day.items.length) {
    return null;
  }

  var exercises = day.items.map(function(item) {
    return {
      exerciseId: item.exerciseId || 0,
      exerciseName: item.exerciseName || '动作',
      sets: item.setsCount || 3,
      reps: parseReps(item.reps),
      repsDisplay: item.reps || '8-10',
      restSeconds: item.restSeconds || 90,
      weightMode: item.weightMode || '中等重量',
      remark: ''
    };
  });

  return {
    title: selectedPlan.title || '今日训练',
    focus: day.focus || selectedPlan.targetType || '计划执行',
    exercises: exercises
  };
}

/**
 * 将生成计划（含 exercises）转为统一格式
 * plan: { title, focus, exercises: [{ exerciseId, exerciseName, sets, reps, repsDisplay, restSeconds, weightMode }] }
 */
function fromGeneratedPlan(plan) {
  if (!plan || !plan.exercises || !plan.exercises.length) {
    return null;
  }

  var exercises = plan.exercises.map(function(item) {
    return {
      exerciseId: item.exerciseId || 0,
      exerciseName: item.exerciseName || '动作',
      sets: item.sets || 3,
      reps: parseReps(item.repsDisplay || item.reps),
      repsDisplay: item.repsDisplay || '8-10',
      restSeconds: item.restSeconds || 90,
      weightMode: item.weightMode || '中等重量',
      remark: ''
    };
  });

  return {
    title: plan.title || '今日训练',
    focus: plan.focus || plan.goal || '计划执行',
    exercises: exercises
  };
}

/**
 * 将统一格式展开为 workout-editor 需要的 sets 数组
 * 每组一个 set 对象
 */
function expandToSets(blueprint) {
  if (!blueprint || !blueprint.exercises) return [];

  var sets = [];
  var setNo = 1;

  blueprint.exercises.forEach(function(ex) {
    for (var i = 0; i < ex.sets; i++) {
      sets.push({
        exerciseId: ex.exerciseId || 0,
        exerciseName: ex.exerciseName || '动作',
        setNo: setNo,
        weightKg: 60,
        reps: ex.reps || 10,
        rir: 2,
        remark: ex.remark || ''
      });
      setNo++;
    }
  });

  return sets;
}

/**
 * 解析 reps 字符串或数字，返回数字
 */
function parseReps(val) {
  if (typeof val === 'number') return val;
  if (!val) return 10;
  // "8-10" → 10, "12-15" → 15, "5-8" → 8
  var parts = String(val).split('-');
  var num = parseInt(parts[parts.length - 1], 10);
  return isNaN(num) ? 10 : num;
}

module.exports = {
  fromStandardPlan: fromStandardPlan,
  fromGeneratedPlan: fromGeneratedPlan,
  expandToSets: expandToSets
};
