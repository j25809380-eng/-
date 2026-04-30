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

    private final PlanService planService;

    public PlanController(PlanService planService) {
        this.planService = planService;
    }

    @GetMapping
    public ApiResponse<List<Map<String, Object>>> list(@RequestParam(defaultValue = "") String targetType) {
        return ApiResponse.ok(planService.listPlans(targetType));
    }

    @GetMapping("/{id}")
    public ApiResponse<Map<String, Object>> detail(@PathVariable Long id) {
        return ApiResponse.ok(planService.getPlanDetail(id));
    }

    @GetMapping("/mine")
    public ApiResponse<List<Map<String, Object>>> mine() {
        return ApiResponse.ok(planService.getMyPlans(CurrentUser.id()));
    }

    @PostMapping("/custom")
    public ApiResponse<Map<String, Object>> createCustom(@Valid @RequestBody CreateCustomPlanRequest request) {
        List<PlanService.PlanDayInput> dayInputs = request.days().stream()
            .map(d -> new PlanService.PlanDayInput(d.title(), d.focus(),
                d.items().stream()
                    .map(i -> new PlanService.PlanItemInput(
                        i.exerciseId(), i.exerciseName(),
                        i.setsCount(), i.reps(),
                        i.restSeconds(), i.weightMode()))
                    .toList()))
            .toList();
        return ApiResponse.ok(planService.createCustomPlan(
            CurrentUser.id(), request.title(), request.subtitle(),
            request.targetType(), request.difficulty(),
            request.durationWeeks(), request.daysPerWeek(),
            request.summary(), dayInputs));
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
