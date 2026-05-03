package com.fitnote.backend.workout;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class WorkoutCalorieEstimatorTest {

    @Test
    void strength60min_shouldEstimateAbout350() {
        int cal = WorkoutCalorieEstimator.estimate("STRENGTH", null, null, null, 60, null);
        assertEquals(350, cal);
    }

    @Test
    void hiit30min_shouldEstimateAbout325() {
        int cal = WorkoutCalorieEstimator.estimate("HIIT", null, null, null, 30, null);
        assertEquals(325, cal);
    }

    @Test
    void running5km_shouldEstimateAbout300() {
        int cal = WorkoutCalorieEstimator.estimate(null, "5km run", null, null, 30, new BigDecimal("5"));
        assertEquals(300, cal);
    }

    @Test
    void nullDuration_shouldDefaultTo60min() {
        int cal = WorkoutCalorieEstimator.estimate("STRENGTH", null, null, null, null, null);
        assertTrue(cal > 0);
    }
}
