package com.fitnote.backend.nutrition;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FoodPresetRepository extends JpaRepository<FoodPreset, Long> {

    List<FoodPreset> findByNameContainingIgnoreCase(String keyword);

    List<FoodPreset> findByCategoryIgnoreCase(String category);
}
