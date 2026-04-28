package com.fitnote.backend.plan;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrainingPlanDayRepository extends JpaRepository<TrainingPlanDay, Long> {

    List<TrainingPlanDay> findByPlanIdOrderByDayNoAsc(Long planId);
}
