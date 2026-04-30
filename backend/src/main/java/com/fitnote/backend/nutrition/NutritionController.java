package com.fitnote.backend.nutrition;

import com.fitnote.backend.common.ApiResponse;
import com.fitnote.backend.common.CurrentUser;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/nutrition")
public class NutritionController {

    private final NutritionService nutritionService;

    public NutritionController(NutritionService nutritionService) {
        this.nutritionService = nutritionService;
    }

    @GetMapping("/goal")
    public ApiResponse<Map<String, Object>> getGoal() {
        UserGoal goal = nutritionService.getGoal(CurrentUser.id());
        return ApiResponse.ok(Map.of(
            "goalType", goal.getGoalType(),
            "targetKcal", goal.getTargetKcal(),
            "targetProtein", goal.getTargetProtein(),
            "targetCarbs", goal.getTargetCarbs(),
            "targetFat", goal.getTargetFat()
        ));
    }

    @PutMapping("/goal")
    public ApiResponse<Map<String, Object>> updateGoal(@RequestBody Map<String, String> body) {
        String goalType = body.getOrDefault("goalType", "maintain");
        UserGoal goal = nutritionService.updateGoal(CurrentUser.id(), goalType);
        return ApiResponse.ok(Map.of(
            "goalType", goal.getGoalType(),
            "targetKcal", goal.getTargetKcal(),
            "targetProtein", goal.getTargetProtein(),
            "targetCarbs", goal.getTargetCarbs(),
            "targetFat", goal.getTargetFat()
        ));
    }

    @GetMapping("/today")
    public ApiResponse<Map<String, Object>> today() {
        return ApiResponse.ok(nutritionService.getTodayAnalysis(CurrentUser.id()));
    }

    @GetMapping("/logs")
    public ApiResponse<List<Map<String, Object>>> logs() {
        List<DietLog> logs = nutritionService.getTodayLogs(CurrentUser.id());
        return ApiResponse.ok(logs.stream()
            .map(log -> Map.<String, Object>of(
                "id", log.getId(),
                "name", log.getName(),
                "mealType", log.getMealType(),
                "kcal", log.getKcal(),
                "protein", log.getProtein(),
                "carbs", log.getCarbs(),
                "fat", log.getFat(),
                "logDate", log.getLogDate().toString()
            ))
            .toList());
    }

    @PostMapping("/log")
    public ApiResponse<Map<String, Object>> addLog(@RequestBody AddLogRequest request) {
        DietLog log = nutritionService.addLog(
            CurrentUser.id(),
            request.name(),
            request.mealType(),
            request.kcal(),
            request.protein(),
            request.carbs(),
            request.fat()
        );
        return ApiResponse.ok(Map.of(
            "id", log.getId(),
            "name", log.getName(),
            "kcal", log.getKcal()
        ));
    }

    @DeleteMapping("/log/{id}")
    public ApiResponse<Map<String, Object>> deleteLog(@PathVariable Long id) {
        nutritionService.deleteLog(id);
        return ApiResponse.ok(Map.of("deleted", true));
    }

    public record AddLogRequest(
        String name,
        String mealType,
        Integer kcal,
        BigDecimal protein,
        BigDecimal carbs,
        BigDecimal fat
    ) {}
}
