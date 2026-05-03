package com.fitnote.backend.plan;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrainingPlanRepository extends JpaRepository<TrainingPlan, Long> {

    List<TrainingPlan> findByTargetTypeContainingIgnoreCase(String targetType);

    List<TrainingPlan> findByCustomPlanTrueAndCreatorUserId(Long creatorUserId);

    @org.springframework.data.jpa.repository.Query("SELECT COUNT(t) FROM TrainingPlan t WHERE t.customPlan = true")
    long countByIsCustomTrue();
}
