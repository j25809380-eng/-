package com.fitnote.backend.recommendation;

import java.util.List;
import java.util.Map;

public interface IRecommendationService {
    List<RecommendationService.RecommendationResult> recommend(String goal, String muscleGroup, String level, int count);
    Map<String, Object> generateSplitPlan(String goal, String level, String splitType);
    Map<String, Object> getOptions();
}
