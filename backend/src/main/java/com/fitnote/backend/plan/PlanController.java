package com.fitnote.backend.plan;

import com.fitnote.backend.common.ApiResponse;
import com.fitnote.backend.common.CurrentUser;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/plans")
public class PlanController {

    private final TrainingPlanRepository trainingPlanRepository;
    private final TrainingPlanDayRepository trainingPlanDayRepository;
    private final TrainingPlanItemRepository trainingPlanItemRepository;

    public PlanController(TrainingPlanRepository trainingPlanRepository,
                          TrainingPlanDayRepository trainingPlanDayRepository,
                          TrainingPlanItemRepository trainingPlanItemRepository) {
        this.trainingPlanRepository = trainingPlanRepository;
        this.trainingPlanDayRepository = trainingPlanDayRepository;
        this.trainingPlanItemRepository = trainingPlanItemRepository;
    }

    @GetMapping
    public ApiResponse<List<Map<String, Object>>> list(@RequestParam(defaultValue = "") String targetType) {
        List<Map<String, Object>> plans = trainingPlanRepository.findByTargetTypeContainingIgnoreCase(targetType)
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
        return ApiResponse.ok(plans);
    }

    @GetMapping("/{id}")
    public ApiResponse<Map<String, Object>> detail(@PathVariable Long id) {
        TrainingPlan plan = trainingPlanRepository.findById(id).orElseThrow();
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

        return ApiResponse.ok(Map.of(
            "id", plan.getId(),
            "title", plan.getTitle(),
            "subtitle", plan.getSubtitle(),
            "targetType", plan.getTargetType(),
            "difficulty", plan.getDifficulty(),
            "durationWeeks", plan.getDurationWeeks(),
            "daysPerWeek", plan.getDaysPerWeek(),
            "summary", plan.getSummary(),
            "days", days
        ));
    }

    @GetMapping("/mine")
    public ApiResponse<List<Map<String, Object>>> mine() {
        Long userId = CurrentUser.id();
        List<Map<String, Object>> mine = trainingPlanRepository.findAll().stream()
            .filter(plan -> Boolean.TRUE.equals(plan.getCustomPlan()) && userId.equals(plan.getCreatorUserId()))
            .map(plan -> Map.<String, Object>of(
                "id", plan.getId(),
                "title", plan.getTitle(),
                "targetType", plan.getTargetType(),
                "difficulty", plan.getDifficulty(),
                "durationWeeks", plan.getDurationWeeks(),
                "daysPerWeek", plan.getDaysPerWeek()
            ))
            .toList();
        return ApiResponse.ok(mine);
    }

    @PostMapping("/custom")
    public ApiResponse<Map<String, Object>> createCustom(@Valid @RequestBody CreateCustomPlanRequest request) {
        Long userId = CurrentUser.id();
        TrainingPlan plan = new TrainingPlan();
        plan.setTitle(request.title());
        plan.setSubtitle(request.subtitle());
        plan.setTargetType(request.targetType());
        plan.setDifficulty(request.difficulty());
        plan.setDurationWeeks(request.durationWeeks());
        plan.setDaysPerWeek(request.daysPerWeek());
        plan.setSummary(request.summary());
        plan.setCustomPlan(true);
        plan.setCreatorUserId(userId);
        plan = trainingPlanRepository.save(plan);

        int dayNo = 1;
        for (PlanDayInput dayInput : request.days()) {
            TrainingPlanDay day = new TrainingPlanDay();
            day.setPlanId(plan.getId());
            day.setDayNo(dayNo++);
            day.setTitle(dayInput.title());
            day.setFocus(dayInput.focus());
            day = trainingPlanDayRepository.save(day);

            int sortNo = 1;
            for (PlanItemInput itemInput : dayInput.items()) {
                TrainingPlanItem item = new TrainingPlanItem();
                item.setDayId(day.getId());
                item.setExerciseId(itemInput.exerciseId());
                item.setExerciseName(itemInput.exerciseName());
                item.setSetsCount(itemInput.setsCount());
                item.setReps(itemInput.reps());
                item.setRestSeconds(itemInput.restSeconds());
                item.setWeightMode(itemInput.weightMode());
                item.setSortNo(sortNo++);
                trainingPlanItemRepository.save(item);
            }
        }

        return ApiResponse.ok(Map.of("created", true, "planId", plan.getId()));
    }

    public record CreateCustomPlanRequest(
        @NotBlank String title,
        String subtitle,
        String targetType,
        String difficulty,
        Integer durationWeeks,
        Integer daysPerWeek,
        String summary,
        @NotEmpty List<PlanDayInput> days
    ) {
    }

    public record PlanDayInput(
        @NotBlank String title,
        String focus,
        @NotEmpty List<PlanItemInput> items
    ) {
    }

    public record PlanItemInput(
        Long exerciseId,
        @NotBlank String exerciseName,
        Integer setsCount,
        String reps,
        Integer restSeconds,
        String weightMode
    ) {
    }
}
