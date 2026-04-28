package com.fitnote.backend.analytics;

import com.fitnote.backend.common.ApiResponse;
import com.fitnote.backend.common.CurrentUser;
import com.fitnote.backend.plan.TrainingPlanRepository;
import com.fitnote.backend.user.UserProfile;
import com.fitnote.backend.user.UserProfileRepository;
import com.fitnote.backend.workout.WorkoutSessionRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class DashboardController {

    private final UserProfileRepository userProfileRepository;
    private final WorkoutSessionRepository workoutSessionRepository;
    private final TrainingPlanRepository trainingPlanRepository;

    public DashboardController(UserProfileRepository userProfileRepository,
                               WorkoutSessionRepository workoutSessionRepository,
                               TrainingPlanRepository trainingPlanRepository) {
        this.userProfileRepository = userProfileRepository;
        this.workoutSessionRepository = workoutSessionRepository;
        this.trainingPlanRepository = trainingPlanRepository;
    }

    @GetMapping("/dashboard/home")
    public ApiResponse<Map<String, Object>> home() {
        Long userId = CurrentUser.id();
        UserProfile profile = userProfileRepository.findByUserId(userId).orElseThrow();
        long recentSessions = workoutSessionRepository.countByUserIdAndSessionDateBetween(
            userId, LocalDate.now().minusDays(6), LocalDate.now());

        return ApiResponse.ok(Map.of(
            "hero", Map.of(
                "title", "选择你的动能",
                "subtitle", "保持训练节奏，今天也把状态拉满",
                "targetType", profile.getTargetType(),
                "trainingLevel", profile.getTrainingLevel()
            ),
            "readiness", Map.of(
                "mealReady", "训练前 1 小时已进食",
                "hydration", "85% 最佳",
                "recovery", "高性能表现"
            ),
            "overview", Map.of(
                "weeklySessions", recentSessions,
                "activePlans", trainingPlanRepository.findAll().size(),
                "goalWeight", profile.getTargetWeightKg()
            ),
            "quickPlans", trainingPlanRepository.findAll().stream().limit(3).map(plan -> Map.of(
                "id", plan.getId(),
                "title", plan.getTitle(),
                "difficulty", plan.getDifficulty(),
                "durationWeeks", plan.getDurationWeeks()
            )).toList(),
            "quickActions", List.of(
                Map.of("name", "开始训练", "path", "/pages/workout-editor/index"),
                Map.of("name", "查看历史", "path", "/pages/history/index"),
                Map.of("name", "动作库", "path", "/pages/exercise/index")
            )
        ));
    }
}
