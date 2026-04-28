package com.fitnote.backend.workout;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BodyMetricRepository extends JpaRepository<BodyMetric, Long> {

    List<BodyMetric> findByUserIdOrderByMetricDateAsc(Long userId);
}
