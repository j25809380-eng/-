package com.fitnote.backend.workout;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class OneRmCalculatorTest {

    @Test
    void epley_100kg_10reps_shouldReturnAbout133() {
        BigDecimal result = OneRmCalculator.epley(new BigDecimal("100"), 10);
        assertTrue(result.compareTo(new BigDecimal("130")) > 0);
        assertTrue(result.compareTo(new BigDecimal("135")) < 0);
    }

    @Test
    void epley_1rep_shouldReturnSameWeight() {
        BigDecimal result = OneRmCalculator.epley(new BigDecimal("80"), 1);
        assertTrue(result.compareTo(new BigDecimal("82")) > 0);
        assertTrue(result.compareTo(new BigDecimal("84")) < 0);
    }

    @Test
    void epley_zeroWeight_shouldReturnZero() {
        assertEquals(BigDecimal.ZERO, OneRmCalculator.epley(BigDecimal.ZERO, 10));
    }

    @Test
    void brzycki_100kg_5reps_shouldReturnAbout112() {
        BigDecimal result = OneRmCalculator.brzycki(new BigDecimal("100"), 5);
        assertTrue(result.compareTo(new BigDecimal("110")) > 0);
        assertTrue(result.compareTo(new BigDecimal("114")) < 0);
    }

    @Test
    void estimate_combinesBothFormulas() {
        BigDecimal result = OneRmCalculator.estimate(new BigDecimal("80"), 8);
        assertTrue(result.compareTo(BigDecimal.ZERO) > 0);
    }
}
