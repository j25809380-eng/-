package com.fitnote.backend.analytics;

import com.fitnote.backend.nutrition.DietLogRepository;
import com.fitnote.backend.plan.TrainingPlanRepository;
import com.fitnote.backend.user.User;
import com.fitnote.backend.user.UserProfile;
import com.fitnote.backend.user.UserProfileRepository;
import com.fitnote.backend.user.UserRepository;
import com.fitnote.backend.workout.WorkoutSession;
import com.fitnote.backend.workout.WorkoutSessionRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class DashboardService {

    private static final Logger log = LoggerFactory.getLogger(DashboardService.class);

    private final UserProfileRepository userProfileRepository;
    private final WorkoutSessionRepository workoutSessionRepository;
    private final TrainingPlanRepository trainingPlanRepository;
    private final DietLogRepository dietLogRepository;
    private final UserRepository userRepository;

    public DashboardService(UserProfileRepository userProfileRepository,
                            WorkoutSessionRepository workoutSessionRepository,
                            TrainingPlanRepository trainingPlanRepository,
                            DietLogRepository dietLogRepository,
                            UserRepository userRepository) {
        this.userProfileRepository = userProfileRepository;
        this.workoutSessionRepository = workoutSessionRepository;
        this.trainingPlanRepository = trainingPlanRepository;
        this.dietLogRepository = dietLogRepository;
        this.userRepository = userRepository;
    }

    public Map<String, Object> buildHomeDashboard(Long userId) {
        // 安全获取 UserProfile，不存在则用默认值
        UserProfile profile = userProfileRepository.findByUserId(userId).orElse(null);
        User user = userRepository.findById(userId).orElse(null);

        String targetType = safeStr(profile != null ? profile.getTargetType() : null, "未设置");
        String trainingLevel = safeStr(profile != null ? profile.getTrainingLevel() : null, "新手");
        Object goalWeight = profile != null && profile.getTargetWeightKg() != null
            ? profile.getTargetWeightKg() : 0;

        long recentSessions = 0;
        try {
            recentSessions = workoutSessionRepository.countByUserIdAndSessionDateBetween(
                userId, LocalDate.now().minusDays(6), LocalDate.now());
        } catch (Exception e) {
            log.warn("查询本周训练次数失败: {}", e.getMessage());
        }

        long planCount = 0;
        try {
            planCount = trainingPlanRepository.count();
        } catch (Exception e) {
            log.warn("查询计划数量失败: {}", e.getMessage());
        }

        String mealReady = evaluateMealReadiness(userId);
        String hydration = evaluateHydration(userId);
        String recovery = evaluateRecovery(userId);

        String displayName = user != null && user.getNickname() != null
            ? user.getNickname() : "健身达人";

        return Map.of(
            "hero", Map.of(
                "title", "选择你的动能",
                "subtitle", "保持训练节奏，今天也把状态拉满",
                "targetType", targetType,
                "trainingLevel", trainingLevel,
                "displayName", displayName
            ),
            "readiness", Map.of(
                "mealReady", mealReady,
                "hydration", hydration,
                "recovery", recovery
            ),
            "overview", Map.of(
                "weeklySessions", recentSessions,
                "activePlans", planCount,
                "goalWeight", goalWeight
            ),
            "quickPlans", getQuickPlans(),
            "quickActions", List.of(
                Map.of("name", "开始训练", "path", "/pages/workout-editor/index"),
                Map.of("name", "饮食分析", "path", "/pages/diet-analysis/index"),
                Map.of("name", "训练历史", "path", "/pages/history/index"),
                Map.of("name", "动作库", "path", "/pages/exercise/index")
            )
        );
    }

    private List<Map<String, Object>> getQuickPlans() {
        try {
            return trainingPlanRepository.findAll().stream().limit(3).map(plan -> {
                Map<String, Object> item = new java.util.LinkedHashMap<>();
                item.put("id", plan.getId() != null ? plan.getId() : 0L);
                item.put("title", safeStr(plan.getTitle(), "训练计划"));
                item.put("difficulty", safeStr(plan.getDifficulty(), "中级"));
                item.put("durationWeeks", plan.getDurationWeeks() != null ? plan.getDurationWeeks() : 4);
                return item;
            }).toList();
        } catch (Exception e) {
            log.warn("查询推荐计划失败: {}", e.getMessage());
            return List.of();
        }
    }

    private String evaluateMealReadiness(Long userId) {
        try {
            boolean hasRecentMeal = dietLogRepository.findByUserIdAndLogDate(userId, LocalDate.now())
                .stream()
                .anyMatch(log -> {
                    LocalDateTime logTime = log.getCreatedAt();
                    return logTime != null
                        && ChronoUnit.HOURS.between(logTime, LocalDateTime.now()) < 2;
                });
            return hasRecentMeal ? "训练前已进食" : "建议训练前进食";
        } catch (Exception e) {
            log.warn("饮食状态评估失败: {}", e.getMessage());
            return "建议训练前进食";
        }
    }

    private String evaluateHydration(Long userId) {
        try {
            long todayMeals = dietLogRepository.findByUserIdAndLogDate(userId, LocalDate.now()).size();
            if (todayMeals >= 3) return "饮食规律，水分充足";
            if (todayMeals >= 1) return "建议补充饮水";
            return "训练前请充分补水";
        } catch (Exception e) {
            log.warn("水分评估失败: {}", e.getMessage());
            return "训练前请充分补水";
        }
    }

    private String evaluateRecovery(Long userId) {
        try {
            List<WorkoutSession> recent = workoutSessionRepository
                .findByUserIdOrderBySessionDateDesc(userId);
            if (recent == null || recent.isEmpty()) return "准备就绪";

            WorkoutSession last = recent.get(0);
            if (last == null || last.getSessionDate() == null) return "准备就绪";

            long daysSince = ChronoUnit.DAYS.between(last.getSessionDate(), LocalDate.now());

            if (daysSince < 1) return "今日已训练，注意恢复";

            Integer feeling = last.getFeelingScore();
            if (daysSince >= 2 && (feeling == null || feeling >= 4)) return "高性能表现";
            if (daysSince >= 2 && feeling != null && feeling <= 2) return "建议充分休息";
            if (daysSince >= 1) return "可以训练";
            return "准备就绪";
        } catch (Exception e) {
            log.warn("恢复状态评估失败: {}", e.getMessage());
            return "准备就绪";
        }
    }

    private static String safeStr(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
