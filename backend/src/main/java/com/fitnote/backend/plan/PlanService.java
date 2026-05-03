package com.fitnote.backend.plan;

import com.fitnote.backend.common.BusinessException;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PlanService {

    private final TrainingPlanRepository trainingPlanRepository;
    private final TrainingPlanDayRepository trainingPlanDayRepository;
    private final TrainingPlanItemRepository trainingPlanItemRepository;

    public PlanService(TrainingPlanRepository trainingPlanRepository,
                       TrainingPlanDayRepository trainingPlanDayRepository,
                       TrainingPlanItemRepository trainingPlanItemRepository) {
        this.trainingPlanRepository = trainingPlanRepository;
        this.trainingPlanDayRepository = trainingPlanDayRepository;
        this.trainingPlanItemRepository = trainingPlanItemRepository;
    }

    public List<Map<String, Object>> listPlans(String targetType) {
        return trainingPlanRepository.findByTargetTypeContainingIgnoreCase(targetType)
            .stream()
            .map(plan -> Map.<String, Object>of(
                "id", plan.getId(),
                "title", plan.getTitle(),
                "subtitle", plan.getSubtitle(),
                "targetType", plan.getTargetType(),
                "difficulty", plan.getDifficulty(),
                "durationWeeks", plan.getDurationWeeks(),
                "daysPerWeek", plan.getDaysPerWeek(),
                "summary", plan.getSummary(),
                "coverImage", plan.getCoverImage() == null ? "" : plan.getCoverImage()
            ))
            .toList();
    }

    public Map<String, Object> getPlanDetail(Long id) {
        TrainingPlan plan = trainingPlanRepository.findById(id)
            .orElseThrow(() -> BusinessException.notFound("计划不存在"));
        List<Map<String, Object>> days = trainingPlanDayRepository.findByPlanIdOrderByDayNoAsc(id).stream()
            .map(day -> Map.<String, Object>of(
                "id", day.getId(),
                "dayNo", day.getDayNo(),
                "title", day.getTitle(),
                "focus", day.getFocus(),
                "items", trainingPlanItemRepository.findByDayIdOrderBySortNoAsc(day.getId()).stream()
                    .map(item -> Map.<String, Object>of(
                        "id", item.getId(),
                        "exerciseId", item.getExerciseId(),
                        "exerciseName", item.getExerciseName(),
                        "setsCount", item.getSetsCount(),
                        "reps", item.getReps(),
                        "restSeconds", item.getRestSeconds(),
                        "weightMode", item.getWeightMode()
                    ))
                    .toList()
            ))
            .toList();

        return Map.of(
            "id", plan.getId(),
            "title", plan.getTitle(),
            "subtitle", plan.getSubtitle(),
            "targetType", plan.getTargetType(),
            "difficulty", plan.getDifficulty(),
            "durationWeeks", plan.getDurationWeeks(),
            "daysPerWeek", plan.getDaysPerWeek(),
            "summary", plan.getSummary(),
            "days", days
        );
    }

    public List<Map<String, Object>> getMyPlans(Long userId) {
        return trainingPlanRepository.findByCustomPlanTrueAndCreatorUserId(userId).stream()
            .map(plan -> Map.<String, Object>of(
                "id", plan.getId(),
                "title", plan.getTitle(),
                "targetType", plan.getTargetType(),
                "difficulty", plan.getDifficulty(),
                "durationWeeks", plan.getDurationWeeks(),
                "daysPerWeek", plan.getDaysPerWeek()
            ))
            .toList();
    }

    @Transactional
    public Map<String, Object> createCustomPlan(Long userId, String title, String subtitle,
                                                 String targetType, String difficulty,
                                                 Integer durationWeeks, Integer daysPerWeek,
                                                 String summary, List<PlanDayInput> dayInputs) {
        TrainingPlan plan = new TrainingPlan();
        plan.setTitle(title);
        plan.setSubtitle(subtitle);
        plan.setTargetType(targetType);
        plan.setDifficulty(difficulty);
        plan.setDurationWeeks(durationWeeks);
        plan.setDaysPerWeek(daysPerWeek);
        plan.setSummary(summary);
        plan.setCustomPlan(true);
        plan.setCreatorUserId(userId);
        plan = trainingPlanRepository.save(plan);

        int dayNo = 1;
        for (PlanDayInput dayInput : dayInputs) {
            TrainingPlanDay day = new TrainingPlanDay();
            day.setPlanId(plan.getId());
            day.setDayNo(dayNo++);
            day.setTitle(dayInput.title);
            day.setFocus(dayInput.focus);
            day = trainingPlanDayRepository.save(day);

            int sortNo = 1;
            for (PlanItemInput itemInput : dayInput.items) {
                TrainingPlanItem item = new TrainingPlanItem();
                item.setDayId(day.getId());
                item.setExerciseId(itemInput.exerciseId);
                item.setExerciseName(itemInput.exerciseName);
                item.setSetsCount(itemInput.setsCount);
                item.setReps(itemInput.reps);
                item.setRestSeconds(itemInput.restSeconds);
                item.setWeightMode(itemInput.weightMode);
                item.setSortNo(sortNo++);
                trainingPlanItemRepository.save(item);
            }
        }

        return Map.of("created", true, "planId", plan.getId());
    }

    public record PlanDayInput(
        String title,
        String focus,
        List<PlanItemInput> items
    ) {
    }

    public record PlanItemInput(
        Long exerciseId,
        String exerciseName,
        Integer setsCount,
        String reps,
        Integer restSeconds,
        String weightMode
    ) {
    }
}
