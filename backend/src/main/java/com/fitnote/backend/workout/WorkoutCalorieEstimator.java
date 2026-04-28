package com.fitnote.backend.workout;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class WorkoutCalorieEstimator {

    private static final Pattern DISTANCE_PATTERN = Pattern.compile("([0-9]+(?:\\.[0-9]+)?)\\s*(km|公里)");

    private WorkoutCalorieEstimator() {
    }

    public static int estimate(String trainingType,
                               String title,
                               String focus,
                               String notes,
                               Integer durationMinutes,
                               BigDecimal distanceKm) {
        int safeDuration = durationMinutes == null || durationMinutes <= 0 ? 60 : durationMinutes;
        String normalizedType = normalizeType(trainingType, title, focus, notes);

        if ("RUNNING".equals(normalizedType)) {
            BigDecimal distance = distanceKm;
            if (distance == null || distance.compareTo(BigDecimal.ZERO) <= 0) {
                distance = parseDistance(title, focus, notes);
            }
            if (distance != null && distance.compareTo(BigDecimal.ZERO) > 0) {
                return distance.multiply(new BigDecimal("60"))
                    .setScale(0, RoundingMode.HALF_UP)
                    .intValue();
            }
            return hourlyCalories(520, safeDuration);
        }

        if ("HIIT".equals(normalizedType)) {
            return hourlyCalories(650, safeDuration);
        }

        return hourlyCalories(350, safeDuration);
    }

    private static String normalizeType(String trainingType, String title, String focus, String notes) {
        if (trainingType != null && !trainingType.isBlank()) {
            String type = trainingType.trim().toUpperCase(Locale.ROOT);
            if ("RUNNING".equals(type) || "HIIT".equals(type) || "STRENGTH".equals(type)) {
                return type;
            }
        }
        String merged = (safe(title) + " " + safe(focus) + " " + safe(notes)).toLowerCase(Locale.ROOT);
        if (merged.contains("跑") || merged.contains("run") || merged.contains("jog") || merged.contains("cardio")) {
            return "RUNNING";
        }
        if (merged.contains("hiit") || merged.contains("间歇") || merged.contains("冲刺")) {
            return "HIIT";
        }
        return "STRENGTH";
    }

    private static BigDecimal parseDistance(String title, String focus, String notes) {
        String merged = safe(title) + " " + safe(focus) + " " + safe(notes);
        Matcher matcher = DISTANCE_PATTERN.matcher(merged.toLowerCase(Locale.ROOT));
        if (!matcher.find()) {
            return null;
        }
        try {
            return new BigDecimal(matcher.group(1));
        } catch (Exception ex) {
            return null;
        }
    }

    private static int hourlyCalories(int perHour, int durationMinutes) {
        return BigDecimal.valueOf(perHour)
            .multiply(BigDecimal.valueOf(durationMinutes))
            .divide(BigDecimal.valueOf(60), 0, RoundingMode.HALF_UP)
            .intValue();
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}
