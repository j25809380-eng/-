package com.fitnote.backend.workout;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class OneRmCalculator {

    private OneRmCalculator() {
    }

    /**
     * Epley 公式: 1RM = weight × (1 + reps / 30)
     * 适用于 reps <= 12 的场景，reps > 12 时精度下降
     */
    public static BigDecimal epley(BigDecimal weightKg, int reps) {
        if (weightKg == null || weightKg.compareTo(BigDecimal.ZERO) <= 0 || reps <= 0) {
            return BigDecimal.ZERO;
        }
        double factor = 1.0 + (Math.min(reps, 12) / 30.0);
        return weightKg.multiply(BigDecimal.valueOf(factor)).setScale(1, RoundingMode.HALF_UP);
    }

    /**
     * Brzycki 公式: 1RM = weight × 36 / (37 - reps)
     * 适用于 reps < 37
     */
    public static BigDecimal brzycki(BigDecimal weightKg, int reps) {
        if (weightKg == null || weightKg.compareTo(BigDecimal.ZERO) <= 0 || reps <= 0 || reps >= 37) {
            return BigDecimal.ZERO;
        }
        double factor = 36.0 / (37.0 - reps);
        return weightKg.multiply(BigDecimal.valueOf(factor)).setScale(1, RoundingMode.HALF_UP);
    }

    /**
     * 综合估算：取 Epley 和 Brzycki 的平均值
     */
    public static BigDecimal estimate(BigDecimal weightKg, int reps) {
        if (reps == 1) return weightKg.setScale(1, RoundingMode.HALF_UP);
        BigDecimal e = epley(weightKg, reps);
        BigDecimal b = brzycki(weightKg, reps);
        if (e.compareTo(BigDecimal.ZERO) <= 0) return b;
        if (b.compareTo(BigDecimal.ZERO) <= 0) return e;
        return e.add(b).divide(BigDecimal.valueOf(2), 1, RoundingMode.HALF_UP);
    }
}
