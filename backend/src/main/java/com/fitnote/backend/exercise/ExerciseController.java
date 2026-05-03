package com.fitnote.backend.exercise;

import com.fitnote.backend.common.ApiResponse;
import com.fitnote.backend.common.BusinessException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/exercises")
public class ExerciseController {

    private final ExerciseRepository exerciseRepository;

    public ExerciseController(ExerciseRepository exerciseRepository) {
        this.exerciseRepository = exerciseRepository;
    }

    @GetMapping
    public ApiResponse<List<Map<String, Object>>> list(@RequestParam(defaultValue = "") String keyword,
                                                       @RequestParam(defaultValue = "") String category) {
        List<Map<String, Object>> result = exerciseRepository
            .findByCategoryContainingIgnoreCaseAndNameContainingIgnoreCase(category, keyword)
            .stream()
            .map(exercise -> Map.<String, Object>of(
                "id", exercise.getId(),
                "name", exercise.getName(),
                "category", exercise.getCategory(),
                "difficulty", exercise.getDifficulty(),
                "equipment", exercise.getEquipment(),
                "primaryMuscle", exercise.getPrimaryMuscle(),
                "coverImage", exercise.getCoverImage() == null ? "" : exercise.getCoverImage()
            ))
            .toList();
        return ApiResponse.ok(result);
    }

    @GetMapping("/{id}")
    public ApiResponse<Map<String, Object>> detail(@PathVariable Long id) {
        Exercise exercise = exerciseRepository.findById(id)
            .orElseThrow(() -> BusinessException.notFound("动作不存在"));
        return ApiResponse.ok(Map.ofEntries(
            Map.entry("id", exercise.getId()),
            Map.entry("name", exercise.getName()),
            Map.entry("category", exercise.getCategory()),
            Map.entry("difficulty", exercise.getDifficulty()),
            Map.entry("equipment", exercise.getEquipment()),
            Map.entry("primaryMuscle", exercise.getPrimaryMuscle()),
            Map.entry("secondaryMuscles", exercise.getSecondaryMuscles()),
            Map.entry("coverImage", exercise.getCoverImage() == null ? "" : exercise.getCoverImage()),
            Map.entry("description", exercise.getDescription()),
            Map.entry("movementSteps", splitText(exercise.getMovementSteps())),
            Map.entry("tips", splitText(exercise.getTips()))
        ));
    }

    private List<String> splitText(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }
        return Arrays.stream(value.split("\\n")).map(String::trim).filter(line -> !line.isBlank()).toList();
    }
}
