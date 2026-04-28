package com.fitnote.backend.exercise;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExerciseRepository extends JpaRepository<Exercise, Long> {

    List<Exercise> findByCategoryContainingIgnoreCaseAndNameContainingIgnoreCase(String category, String name);
}
