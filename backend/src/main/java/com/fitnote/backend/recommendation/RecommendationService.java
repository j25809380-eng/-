package com.fitnote.backend.recommendation;

import com.fitnote.backend.exercise.Exercise;
import com.fitnote.backend.exercise.ExerciseRepository;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class RecommendationService implements IRecommendationService {

    private final ExerciseRepository exerciseRepository;
    private final Random random;

    public RecommendationService(ExerciseRepository exerciseRepository) {
        this.exerciseRepository = exerciseRepository;
        this.random = new Random();
    }

    private static final Map<String, List<String>> SPLIT_MUSCLES = new LinkedHashMap<>();
    static {
        // 推日（胸、肩前中束、肱三头肌）
        SPLIT_MUSCLES.put("push", List.of("胸部", "肩部", "手臂"));
        // 拉日（背、肱二头肌）
        SPLIT_MUSCLES.put("pull", List.of("背部", "手臂"));
        // 腿日（腿部、核心）
        SPLIT_MUSCLES.put("legs", List.of("腿部", "核心"));
        // 肩臂日
        SPLIT_MUSCLES.put("shoulders_arms", List.of("肩部", "手臂"));
        // 全身
        SPLIT_MUSCLES.put("full", List.of("胸部", "背部", "腿部", "肩部", "手臂", "核心"));
    }

    private static final Map<String, String> MUSCLE_TRANSLATION = new LinkedHashMap<>();
    static {
        MUSCLE_TRANSLATION.put("胸", "胸部");
        MUSCLE_TRANSLATION.put("胸部", "胸部");
        MUSCLE_TRANSLATION.put("背", "背部");
        MUSCLE_TRANSLATION.put("背部", "背部");
        MUSCLE_TRANSLATION.put("腿", "腿部");
        MUSCLE_TRANSLATION.put("腿部", "腿部");
        MUSCLE_TRANSLATION.put("肩", "肩部");
        MUSCLE_TRANSLATION.put("肩部", "肩部");
        MUSCLE_TRANSLATION.put("手臂", "手臂");
        MUSCLE_TRANSLATION.put("核心", "核心");
        MUSCLE_TRANSLATION.put("全身", "全身");
    }

    private static final Map<String, List<String>> GOAL_EXERCISE_TYPES = new LinkedHashMap<>();
    static {
        GOAL_EXERCISE_TYPES.put("增肌", List.of("胸部", "背部", "腿部", "肩部", "手臂"));
        GOAL_EXERCISE_TYPES.put("减脂", List.of("腿部", "核心", "全身"));
        GOAL_EXERCISE_TYPES.put("维持", List.of("胸部", "背部", "腿部", "肩部", "手臂", "核心"));
    }

    private static final Map<String, Integer> GOAL_REPS = new LinkedHashMap<>();
    static {
        GOAL_REPS.put("增肌", 10);
        GOAL_REPS.put("减脂", 15);
        GOAL_REPS.put("维持", 12);
    }

    /**
     * 根据目标/部位/等级推荐动作列表
     */
    public List<RecommendationResult> recommend(String goal, String muscleGroup, String level, int count) {
        String category = resolveMuscleCategory(muscleGroup);

        // 第1步：按部位和等级筛选
        List<Exercise> candidates = exerciseRepository.findAll().stream()
            .filter(e -> matchesCategory(e, category))
            .filter(e -> matchesLevel(e, level))
            .collect(Collectors.toList());

        if (candidates.isEmpty()) {
            // 放宽等级限制
            candidates = exerciseRepository.findAll().stream()
                .filter(e -> matchesCategory(e, category))
                .collect(Collectors.toList());
        }

        // 第2步：按目标调整推荐策略
        if ("减脂".equals(goal)) {
            candidates.sort(Comparator
                .comparing(Exercise::getCompound).reversed()
                .thenComparing(Exercise::getPriority, Comparator.reverseOrder()));
        } else {
            candidates.sort(Comparator
                .comparing(Exercise::getCompound).reversed()
                .thenComparing(Exercise::getPriority, Comparator.reverseOrder()));
        }

        // 第3步：优先级加权随机（核心动作更大概率排前面）
        List<Exercise> shuffled = weightedShuffle(candidates);
        List<Exercise> selected = new ArrayList<>();
        List<Long> usedIds = new ArrayList<>();

        for (Exercise e : shuffled) {
            if (selected.size() >= count) break;
            // 避免重复类型过度集中
            if (usedIds.contains(e.getId())) continue;
            usedIds.add(e.getId());
            selected.add(e);
        }

        // 第4步：生成推荐结果（含组数次数组重建议）
        int baseReps = GOAL_REPS.getOrDefault(goal, 10);
        return selected.stream()
            .map(e -> buildRecommendation(e, goal, baseReps, selected.indexOf(e)))
            .collect(Collectors.toList());
    }

    /**
     * 自动生成分训练日计划（推/拉/腿/肩臂/全身）
     */
    public Map<String, Object> generateSplitPlan(String goal, String level, String splitType) {
        List<String> categories = SPLIT_MUSCLES.getOrDefault(
            splitType != null ? splitType : "full", SPLIT_MUSCLES.get("full"));

        int exercisesPerDay = switch (splitType != null ? splitType : "full") {
            case "push", "pull", "legs", "shoulders_arms" -> 5;
            case "full" -> 8;
            default -> 5;
        };

        List<RecommendationResult> exercises = new ArrayList<>();
        for (String cat : categories) {
            List<RecommendationResult> forCategory = recommend(goal, cat, level, 2);
            exercises.addAll(forCategory);
        }

        // 去重、排序、限制数量
        List<RecommendationResult> result = exercises.stream()
            .collect(Collectors.toMap(
                RecommendationResult::exerciseId,
                e -> e,
                (a, b) -> a,
                LinkedHashMap::new))
            .values().stream()
            .sorted(Comparator.comparingInt(RecommendationResult::priority).reversed())
            .limit(exercisesPerDay)
            .collect(Collectors.toList());

        String title = switch (splitType != null ? splitType : "full") {
            case "push" -> "推日";
            case "pull" -> "拉日";
            case "legs" -> "腿日";
            case "shoulders_arms" -> "肩臂日";
            default -> "全身训练";
        };
        String focus = switch (splitType != null ? splitType : "full") {
            case "push" -> "胸部 + 肩部 + 肱三头肌";
            case "pull" -> "背部 + 肱二头肌";
            case "legs" -> "股四头肌 + 腘绳肌 + 臀大肌";
            case "shoulders_arms" -> "三角肌 + 肱二头肌 + 肱三头肌";
            default -> "全身均衡发展";
        };

        int totalSets = result.stream().mapToInt(RecommendationResult::sets).sum();

        return Map.of(
            "title", title,
            "focus", focus,
            "splitType", splitType,
            "goal", goal,
            "level", level,
            "totalExercises", result.size(),
            "totalSets", totalSets,
            "exercises", result
        );
    }

    /**
     * 获取可用的生成选项
     */
    public Map<String, Object> getOptions() {
        return Map.of(
            "goals", List.of("增肌", "减脂", "维持"),
            "muscleGroups", List.of(
                Map.of("key", "胸", "label", "胸部"),
                Map.of("key", "背", "label", "背部"),
                Map.of("key", "腿", "label", "腿部"),
                Map.of("key", "肩", "label", "肩部"),
                Map.of("key", "手臂", "label", "手臂"),
                Map.of("key", "核心", "label", "核心"),
                Map.of("key", "全身", "label", "全身")
            ),
            "levels", List.of(
                Map.of("key", "新手", "label", "新手"),
                Map.of("key", "进阶", "label", "进阶"),
                Map.of("key", "高级", "label", "高级")
            ),
            "splitTypes", List.of(
                Map.of("key", "push", "label", "推日（胸+肩+三头）"),
                Map.of("key", "pull", "label", "拉日（背+二头）"),
                Map.of("key", "legs", "label", "腿日（股四+腘绳+臀）"),
                Map.of("key", "shoulders_arms", "label", "肩臂日"),
                Map.of("key", "full", "label", "全身训练")
            )
        );
    }

    // ========== 辅助方法 ==========

    private String resolveMuscleCategory(String input) {
        if (input == null || input.isBlank()) return "全身";
        return MUSCLE_TRANSLATION.getOrDefault(input, input);
    }

    private boolean matchesCategory(Exercise exercise, String category) {
        if ("全身".equals(category)) return true;
        String exCat = exercise.getCategory();
        if (exCat == null) return false;
        // 手臂匹配：肱二头肌/肱三头肌 属于 "手臂"
        if ("手臂".equals(category)) {
            return exCat.contains("手臂") || exCat.equals("手臂")
                || (exercise.getPrimaryMuscle() != null
                    && (exercise.getPrimaryMuscle().contains("肱二")
                        || exercise.getPrimaryMuscle().contains("三头")));
        }
        return exCat.contains(category) || category.contains(exCat)
            || exCat.equals(category);
    }

    private boolean matchesLevel(Exercise exercise, String level) {
        if (level == null || level.isBlank()) return true;
        String suitable = exercise.getSuitableLevel();
        if (suitable == null || suitable.isBlank()) return true;
        return suitable.contains(level);
    }

    private List<Exercise> weightedShuffle(List<Exercise> list) {
        List<Exercise> result = new ArrayList<>(list);
        // 按优先级权重抽样（priority 越高的动作越大概率排前面）
        result.sort((a, b) -> {
            int pa = a.getPriority() != null ? a.getPriority() : 5;
            int pb = b.getPriority() != null ? b.getPriority() : 5;
            // 加随机噪声
            double scoreA = pa + random.nextDouble() * 3;
            double scoreB = pb + random.nextDouble() * 3;
            return Double.compare(scoreB, scoreA);
        });
        return result;
    }

    private RecommendationResult buildRecommendation(Exercise e, String goal, int baseReps, int index) {
        int sets = switch (goal) {
            case "增肌" -> (e.getCompound() ? 4 : 3);
            case "减脂" -> 3;
            default -> 3;
        };

        int reps = switch (goal) {
            case "增肌" -> (e.getCompound() ? "8-10" : "10-12").equals("8-10") ? 10 : 12;
            case "减脂" -> 15;
            default -> 12;
        };
        String repsDisplay = switch (goal) {
            case "增肌" -> e.getCompound() ? "8-10" : "10-12";
            case "减脂" -> "12-15";
            default -> "10-12";
        };

        int restSeconds = switch (goal) {
            case "增肌" -> e.getCompound() ? 120 : 90;
            case "减脂" -> 45;
            default -> 60;
        };

        String weightMode = switch (goal) {
            case "增肌" -> e.getCompound() ? "渐进超负荷" : "中等重量";
            case "减脂" -> "轻重量";
            default -> "中等重量";
        };

        // 生成推荐理由
        StringBuilder reason = new StringBuilder();
        if (e.getCompound() != null && e.getCompound()) {
            reason.append("复合动作，刺激多个肌群");
        } else {
            reason.append("孤立动作，精准刺激目标肌群");
        }
        if (e.getPriority() != null && e.getPriority() >= 9) {
            reason.append(" · 核心推荐动作");
        } else if (e.getPriority() != null && e.getPriority() >= 7) {
            reason.append(" · 高效选择");
        }
        if ("增肌".equals(goal)) {
            reason.append(" · 适合增肌期大重量训练");
        } else if ("减脂".equals(goal)) {
            reason.append(" · 适合减脂期热量消耗");
        }
        if (e.getSuitableLevel() != null && e.getSuitableLevel().contains("新手")) {
            reason.append(" · 新手友好");
        }

        return new RecommendationResult(
            e.getId(),
            e.getName(),
            e.getCategory(),
            e.getEquipment() != null ? e.getEquipment() : "",
            e.getDifficulty() != null ? e.getDifficulty() : "初级",
            e.getPrimaryMuscle() != null ? e.getPrimaryMuscle() : "",
            e.getSecondaryMuscles() != null ? e.getSecondaryMuscles() : "",
            e.getCompound() != null && e.getCompound(),
            e.getPriority() != null ? e.getPriority() : 5,
            e.getSuitableLevel() != null ? e.getSuitableLevel() : "",
            e.getDescription() != null ? e.getDescription() : "",
            e.getMovementSteps() != null ? e.getMovementSteps() : "",
            e.getTips() != null ? e.getTips() : "",
            sets,
            reps,
            repsDisplay,
            restSeconds,
            weightMode,
            reason.toString()
        );
    }

    public record RecommendationResult(
        Long exerciseId,
        String exerciseName,
        String category,
        String equipment,
        String difficulty,
        String primaryMuscle,
        String secondaryMuscles,
        boolean isCompound,
        int priority,
        String suitableLevel,
        String description,
        String movementSteps,
        String tips,
        int sets,
        int reps,
        String repsDisplay,
        int restSeconds,
        String weightMode,
        String reason
    ) {
    }
}
