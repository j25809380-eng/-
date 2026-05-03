package com.fitnote.backend.workout;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkoutSetRepository extends JpaRepository<WorkoutSet, Long> {

    List<WorkoutSet> findBySessionIdOrderByExerciseNameAscSetNoAsc(Long sessionId);

    List<WorkoutSet> findByUserIdAndExerciseId(Long userId, Long exerciseId);

    List<WorkoutSet> findTop200ByOrderByCreatedAtDesc();
}
