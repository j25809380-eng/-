package com.fitnote.backend.workout;

import com.fitnote.backend.common.PageResult;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface IWorkoutService {
    PageResult<Map<String, Object>> getHistory(Long userId, int page, int size);
    Map<String, Object> getDetail(Long id);
    Map<String, Object> createWorkout(Long userId, String title, String focus, LocalDate sessionDate,
                                      Integer durationMinutes, Integer calories, Integer feelingScore,
                                      String notes, List<WorkoutService.WorkoutSetInput> setInputs);
}
