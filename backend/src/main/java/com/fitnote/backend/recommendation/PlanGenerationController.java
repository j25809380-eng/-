package com.fitnote.backend.recommendation;

import com.fitnote.backend.common.ApiResponse;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/plans")
public class PlanGenerationController {

    private final RecommendationService recommendationService;

    public PlanGenerationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @GetMapping("/generate/options")
    public ApiResponse<Map<String, Object>> options() {
        return ApiResponse.ok(recommendationService.getOptions());
    }

    @PostMapping("/generate")
    public ApiResponse<Map<String, Object>> generate(@RequestBody GenerateRequest request) {
        // 如果是单个部位推荐
        List<RecommendationService.RecommendationResult> exercises = recommendationService.recommend(
            request.goal(), request.muscleGroup(), request.level(), 6);

        return ApiResponse.ok(Map.of(
            "goal", request.goal(),
            "muscleGroup", request.muscleGroup(),
            "level", request.level(),
            "exercises", exercises,
            "totalExercises", exercises.size(),
            "totalSets", exercises.stream().mapToInt(RecommendationService.RecommendationResult::sets).sum()
        ));
    }

    @PostMapping("/generate/split")
    public ApiResponse<Map<String, Object>> generateSplit(@RequestBody GenerateSplitRequest request) {
        return ApiResponse.ok(recommendationService.generateSplitPlan(
            request.goal(), request.level(), request.splitType()));
    }

    @PostMapping("/generate/refresh")
    public ApiResponse<Map<String, Object>> refresh(@RequestBody GenerateRequest request) {
        // 和 generate 相同但加更多随机性
        List<RecommendationService.RecommendationResult> exercises = recommendationService.recommend(
            request.goal(), request.muscleGroup(), request.level(), 6);

        return ApiResponse.ok(Map.of(
            "goal", request.goal(),
            "muscleGroup", request.muscleGroup(),
            "level", request.level(),
            "exercises", exercises,
            "totalExercises", exercises.size(),
            "totalSets", exercises.stream().mapToInt(RecommendationService.RecommendationResult::sets).sum(),
            "refreshed", true
        ));
    }

    public record GenerateRequest(
        String goal,
        String muscleGroup,
        String level
    ) {
    }

    public record GenerateSplitRequest(
        String goal,
        String level,
        String splitType
    ) {
    }
}
