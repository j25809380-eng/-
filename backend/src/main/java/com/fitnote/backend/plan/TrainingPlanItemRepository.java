package com.fitnote.backend.plan;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrainingPlanItemRepository extends JpaRepository<TrainingPlanItem, Long> {

    List<TrainingPlanItem> findByDayIdOrderBySortNoAsc(Long dayId);
}
