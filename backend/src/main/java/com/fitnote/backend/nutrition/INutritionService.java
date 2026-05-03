package com.fitnote.backend.nutrition;

import com.fitnote.backend.common.PageResult;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface INutritionService {
    UserGoal getGoal(Long userId);
    UserGoal updateGoal(Long userId, String goalType);
    DietLog addLog(Long userId, String name, String mealType, Integer kcal, java.math.BigDecimal protein, java.math.BigDecimal carbs, java.math.BigDecimal fat);
    List<DietLog> getTodayLogs(Long userId);
    PageResult<Map<String, Object>> getLogs(Long userId, LocalDate logDate, int page, int size);
    void deleteLog(Long id);
    Map<String, Object> getTodayAnalysis(Long userId);
}
