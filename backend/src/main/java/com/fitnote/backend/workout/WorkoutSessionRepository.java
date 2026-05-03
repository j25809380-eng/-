package com.fitnote.backend.workout;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WorkoutSessionRepository extends JpaRepository<WorkoutSession, Long> {

    List<WorkoutSession> findByUserIdOrderBySessionDateDesc(Long userId);

    List<WorkoutSession> findByUserIdAndSessionDateBetween(Long userId, LocalDate start, LocalDate end);

    long countByUserIdAndSessionDateBetween(Long userId, LocalDate start, LocalDate end);

    @Query("SELECT COALESCE(SUM(w.totalVolume), 0) FROM WorkoutSession w")
    BigDecimal findTotalVolume();

    @Query("SELECT COUNT(w) FROM WorkoutSession w WHERE w.sessionDate = :date")
    long countBySessionDate(@Param("date") LocalDate date);

    List<WorkoutSession> findTop6ByOrderBySessionDateDesc();

    List<WorkoutSession> findByOrderBySessionDateDesc();
}
