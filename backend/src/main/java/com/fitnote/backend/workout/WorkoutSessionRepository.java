package com.fitnote.backend.workout;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkoutSessionRepository extends JpaRepository<WorkoutSession, Long> {

    List<WorkoutSession> findByUserIdOrderBySessionDateDesc(Long userId);

    long countByUserIdAndSessionDateBetween(Long userId, LocalDate start, LocalDate end);
}
