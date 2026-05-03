package com.fitnote.backend.nutrition;

import com.fitnote.backend.common.ApiResponse;
import com.fitnote.backend.common.CurrentUser;
import com.fitnote.backend.common.PageResult;
import java.math.BigDecimal;
import java.time.LocalDate;
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
    public ApiResponse<PageResult<Map<String, Object>>> logs(
            @RequestParam(required = false) String date,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        LocalDate logDate = (date != null && !date.isBlank()) ? LocalDate.parse(date) : LocalDate.now();
        return ApiResponse.ok(nutritionService.getLogs(CurrentUser.id(), logDate, page, size));
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

    @GetMapping("/food-presets")
    public ApiResponse<List<Map<String, Object>>> foodPresets(@RequestParam(defaultValue = "") String keyword) {
        return ApiResponse.ok(nutritionService.getFoodPresets(keyword));
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
