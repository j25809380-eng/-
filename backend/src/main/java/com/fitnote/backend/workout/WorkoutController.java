package com.fitnote.backend.workout;

import com.fitnote.backend.common.ApiResponse;
import com.fitnote.backend.common.CurrentUser;
import com.fitnote.backend.exercise.Exercise;
import com.fitnote.backend.exercise.ExerciseRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
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

    private final WorkoutSessionRepository workoutSessionRepository;
    private final WorkoutSetRepository workoutSetRepository;
    private final ExerciseRepository exerciseRepository;
    private final PersonalRecordRepository personalRecordRepository;

    public WorkoutController(WorkoutSessionRepository workoutSessionRepository,
                             WorkoutSetRepository workoutSetRepository,
                             ExerciseRepository exerciseRepository,
                             PersonalRecordRepository personalRecordRepository) {
        this.workoutSessionRepository = workoutSessionRepository;
        this.workoutSetRepository = workoutSetRepository;
        this.exerciseRepository = exerciseRepository;
        this.personalRecordRepository = personalRecordRepository;
    }

    @GetMapping("/history")
    public ApiResponse<List<Map<String, Object>>> history() {
        Long userId = CurrentUser.id();
        List<Map<String, Object>> result = workoutSessionRepository.findByUserIdOrderBySessionDateDesc(userId).stream()
            .map(session -> Map.<String, Object>of(
                "id", session.getId(),
                "title", session.getTitle(),
                "focus", session.getFocus(),
                "sessionDate", session.getSessionDate(),
                "durationMinutes", session.getDurationMinutes(),
                "totalVolume", session.getTotalVolume(),
                "calories", session.getCalories(),
                "completionStatus", session.getCompletionStatus()
            ))
            .toList();
        return ApiResponse.ok(result);
    }

    @GetMapping("/{id}")
    public ApiResponse<Map<String, Object>> detail(@PathVariable Long id) {
        WorkoutSession session = workoutSessionRepository.findById(id).orElseThrow();
        List<Map<String, Object>> sets = workoutSetRepository.findBySessionIdOrderByExerciseNameAscSetNoAsc(id).stream()
            .map(set -> Map.<String, Object>of(
                "id", set.getId(),
                "exerciseId", set.getExerciseId(),
                "exerciseName", set.getExerciseName(),
                "setNo", set.getSetNo(),
                "weightKg", set.getWeightKg(),
                "reps", set.getReps(),
                "rir", set.getRir(),
                "remark", set.getRemark() == null ? "" : set.getRemark(),
                "isPr", set.getPr()
            ))
            .toList();
        return ApiResponse.ok(Map.of(
            "id", session.getId(),
            "title", session.getTitle(),
            "focus", session.getFocus(),
            "sessionDate", session.getSessionDate(),
            "durationMinutes", session.getDurationMinutes(),
            "totalVolume", session.getTotalVolume(),
            "calories", session.getCalories(),
            "notes", session.getNotes() == null ? "" : session.getNotes(),
            "sets", sets
        ));
    }

    @PostMapping
    public ApiResponse<Map<String, Object>> create(@Valid @RequestBody CreateWorkoutRequest request) {
        Long userId = CurrentUser.id();
        WorkoutSession session = new WorkoutSession();
        session.setUserId(userId);
        session.setTitle(request.title());
        session.setFocus(request.focus());
        session.setSessionDate(request.sessionDate() == null ? LocalDate.now() : request.sessionDate());
        session.setStartedAt(LocalDateTime.now().minusMinutes(request.durationMinutes() == null ? 60 : request.durationMinutes()));
        session.setFinishedAt(LocalDateTime.now());
        session.setDurationMinutes(request.durationMinutes() == null ? 60 : request.durationMinutes());
        session.setCalories(request.calories() == null ? 480 : request.calories());
        session.setFeelingScore(request.feelingScore() == null ? 4 : request.feelingScore());
        session.setNotes(request.notes());
        session.setCompletionStatus("COMPLETED");

        BigDecimal totalVolume = request.sets().stream()
            .map(item -> item.weightKg().multiply(BigDecimal.valueOf(item.reps())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        session.setTotalVolume(totalVolume);
        session = workoutSessionRepository.save(session);

        for (WorkoutSetInput item : request.sets()) {
            WorkoutSet set = new WorkoutSet();
            set.setSessionId(session.getId());
            set.setUserId(userId);
            set.setExerciseId(item.exerciseId());
            set.setExerciseName(resolveExerciseName(item.exerciseId(), item.exerciseName()));
            set.setSetNo(item.setNo());
            set.setWeightKg(item.weightKg());
            set.setReps(item.reps());
            set.setRir(item.rir());
            set.setRemark(item.remark());

            BigDecimal previousBest = workoutSetRepository.findByUserIdAndExerciseId(userId, item.exerciseId()).stream()
                .map(WorkoutSet::getWeightKg)
                .max(Comparator.naturalOrder())
                .orElse(BigDecimal.ZERO);
            set.setPr(item.weightKg().compareTo(previousBest) > 0);
            WorkoutSet savedSet = workoutSetRepository.save(set);

            if (Boolean.TRUE.equals(savedSet.getPr())) {
                upsertPersonalRecord(userId, savedSet, session.getId());
            }
        }

        return ApiResponse.ok(Map.of(
            "sessionId", session.getId(),
            "totalVolume", totalVolume,
            "completed", true
        ));
    }

    private String resolveExerciseName(Long exerciseId, String exerciseName) {
        if (exerciseName != null && !exerciseName.isBlank()) {
            return exerciseName;
        }
        Exercise exercise = exerciseRepository.findById(exerciseId).orElseThrow();
        return exercise.getName();
    }

    private void upsertPersonalRecord(Long userId, WorkoutSet set, Long sessionId) {
        PersonalRecord record = personalRecordRepository
            .findTopByUserIdAndExerciseIdAndRecordTypeOrderByRecordValueDesc(userId, set.getExerciseId(), "MAX_WEIGHT")
            .orElseGet(PersonalRecord::new);
        record.setUserId(userId);
        record.setExerciseId(set.getExerciseId());
        record.setExerciseName(set.getExerciseName());
        record.setRecordType("MAX_WEIGHT");
        record.setRecordValue(set.getWeightKg());
        record.setAchievedAt(LocalDateTime.now());
        record.setSourceSessionId(sessionId);
        personalRecordRepository.save(record);
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
