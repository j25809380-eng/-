package com.fitnote.backend.workout;

import com.fitnote.backend.common.BusinessException;
import com.fitnote.backend.common.PageResult;
import com.fitnote.backend.exercise.Exercise;
import com.fitnote.backend.exercise.ExerciseRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WorkoutService implements IWorkoutService {

    private final WorkoutSessionRepository workoutSessionRepository;
    private final WorkoutSetRepository workoutSetRepository;
    private final ExerciseRepository exerciseRepository;
    private final PersonalRecordRepository personalRecordRepository;

    public WorkoutService(WorkoutSessionRepository workoutSessionRepository,
                          WorkoutSetRepository workoutSetRepository,
                          ExerciseRepository exerciseRepository,
                          PersonalRecordRepository personalRecordRepository) {
        this.workoutSessionRepository = workoutSessionRepository;
        this.workoutSetRepository = workoutSetRepository;
        this.exerciseRepository = exerciseRepository;
        this.personalRecordRepository = personalRecordRepository;
    }

    public PageResult<Map<String, Object>> getHistory(Long userId, int page, int size) {
        List<Map<String, Object>> all = workoutSessionRepository.findByUserIdOrderBySessionDateDesc(userId).stream()
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
        return PageResult.fromList(all, page, size);
    }

    public Map<String, Object> getDetail(Long id) {
        WorkoutSession session = workoutSessionRepository.findById(id)
            .orElseThrow(() -> BusinessException.notFound("训练记录不存在"));
        List<Map<String, Object>> sets = workoutSetRepository.findBySessionIdOrderByExerciseNameAscSetNoAsc(id).stream()
            .map(set -> {
                BigDecimal estimatedOneRm = OneRmCalculator.estimate(set.getWeightKg(), set.getReps() != null ? set.getReps() : 1);
                return Map.<String, Object>of(
                    "id", set.getId(),
                    "exerciseId", set.getExerciseId(),
                    "exerciseName", set.getExerciseName(),
                    "setNo", set.getSetNo(),
                    "weightKg", set.getWeightKg(),
                    "reps", set.getReps(),
                    "rir", set.getRir(),
                    "remark", set.getRemark() == null ? "" : set.getRemark(),
                    "isPr", set.getPr(),
                    "estimatedOneRm", estimatedOneRm
                );
            })
            .toList();
        return Map.of(
            "id", session.getId(),
            "title", session.getTitle(),
            "focus", session.getFocus(),
            "sessionDate", session.getSessionDate(),
            "durationMinutes", session.getDurationMinutes(),
            "totalVolume", session.getTotalVolume(),
            "calories", session.getCalories(),
            "notes", session.getNotes() == null ? "" : session.getNotes(),
            "sets", sets
        );
    }

    @Transactional
    public Map<String, Object> createWorkout(Long userId, String title, String focus,
                                              LocalDate sessionDate, Integer durationMinutes,
                                              Integer calories, Integer feelingScore,
                                              String notes, List<WorkoutSetInput> setInputs) {
        WorkoutSession session = new WorkoutSession();
        session.setUserId(userId);
        session.setTitle(title);
        session.setFocus(focus);
        session.setSessionDate(sessionDate == null ? LocalDate.now() : sessionDate);
        session.setStartedAt(LocalDateTime.now().minusMinutes(durationMinutes == null ? 60 : durationMinutes));
        session.setFinishedAt(LocalDateTime.now());
        session.setDurationMinutes(durationMinutes == null ? 60 : durationMinutes);
        session.setCalories(calories == null ? 480 : calories);
        session.setFeelingScore(feelingScore == null ? 4 : feelingScore);
        session.setNotes(notes);
        session.setCompletionStatus("COMPLETED");

        BigDecimal totalVolume = setInputs.stream()
            .map(item -> item.weightKg.multiply(BigDecimal.valueOf(item.reps)))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        session.setTotalVolume(totalVolume);
        session = workoutSessionRepository.save(session);

        for (WorkoutSetInput item : setInputs) {
            WorkoutSet set = new WorkoutSet();
            set.setSessionId(session.getId());
            set.setUserId(userId);
            set.setExerciseId(item.exerciseId);
            set.setExerciseName(resolveExerciseName(item.exerciseId, item.exerciseName));
            set.setSetNo(item.setNo);
            set.setWeightKg(item.weightKg);
            set.setReps(item.reps);
            set.setRir(item.rir);
            set.setRemark(item.remark);

            BigDecimal previousBest = workoutSetRepository.findByUserIdAndExerciseId(userId, item.exerciseId).stream()
                .map(WorkoutSet::getWeightKg)
                .max(Comparator.naturalOrder())
                .orElse(BigDecimal.ZERO);
            set.setPr(item.weightKg.compareTo(previousBest) > 0);
            WorkoutSet savedSet = workoutSetRepository.save(set);

            if (Boolean.TRUE.equals(savedSet.getPr())) {
                upsertPersonalRecord(userId, savedSet, session.getId());
            }
        }

        return Map.of(
            "sessionId", session.getId(),
            "totalVolume", totalVolume,
            "completed", true
        );
    }

    private String resolveExerciseName(Long exerciseId, String exerciseName) {
        if (exerciseName != null && !exerciseName.isBlank()) {
            return exerciseName;
        }
        Exercise exercise = exerciseRepository.findById(exerciseId)
            .orElseThrow(() -> BusinessException.notFound("动作不存在"));
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
