package com.fitnote.backend.plan;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrainingPlanRepository extends JpaRepository<TrainingPlan, Long> {

    List<TrainingPlan> findByTargetTypeContainingIgnoreCase(String targetType);
}
