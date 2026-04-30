package com.fitnote.backend.workout;

import com.fitnote.backend.common.ApiResponse;
import com.fitnote.backend.common.CurrentUser;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/workouts")
public class WorkoutController {

    private final WorkoutService workoutService;

    public WorkoutController(WorkoutService workoutService) {
        this.workoutService = workoutService;
    }

    @GetMapping("/history")
    public ApiResponse<List<Map<String, Object>>> history() {
        return ApiResponse.ok(workoutService.getHistory(CurrentUser.id()));
    }

    @GetMapping("/{id}")
    public ApiResponse<Map<String, Object>> detail(@PathVariable Long id) {
        return ApiResponse.ok(workoutService.getDetail(id));
    }

    @PostMapping
    public ApiResponse<Map<String, Object>> create(@Valid @RequestBody CreateWorkoutRequest request) {
        List<WorkoutService.WorkoutSetInput> setInputs = request.sets().stream()
            .map(s -> new WorkoutService.WorkoutSetInput(
                s.exerciseId(), s.exerciseName(), s.setNo(),
                s.weightKg(), s.reps(), s.rir(), s.remark()))
            .toList();
        return ApiResponse.ok(workoutService.createWorkout(
            CurrentUser.id(), request.title(), request.focus(),
            request.sessionDate(), request.durationMinutes(),
            request.calories(), request.feelingScore(),
            request.notes(), setInputs));
    }

    public record CreateWorkoutRequest(
        @NotBlank String title,
        String focus,
        LocalDate sessionDate,
        Integer durationMinutes,
        Integer calories,
        Integer feelingScore,
        String notes,
        @NotEmpty List<WorkoutSetInput> sets
    ) {
    }

    public record WorkoutSetInput(
        Long exerciseId,
        String exerciseName,
        Integer setNo,
        BigDecimal weightKg,
        Integer reps,
        Integer rir,
        String remark
    ) {
    }
}
