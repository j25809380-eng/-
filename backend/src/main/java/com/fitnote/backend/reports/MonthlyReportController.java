package com.fitnote.backend.reports;

import com.fitnote.backend.common.ApiResponse;
import com.fitnote.backend.common.CurrentUser;
import com.fitnote.backend.workout.PersonalRecordRepository;
import com.fitnote.backend.workout.WorkoutSession;
import com.fitnote.backend.workout.WorkoutSessionRepository;
import com.fitnote.backend.workout.WorkoutSet;
import com.fitnote.backend.workout.WorkoutSetRepository;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
public class MonthlyReportController {

    private final WorkoutSessionRepository workoutSessionRepository;
    private final WorkoutSetRepository workoutSetRepository;
    private final PersonalRecordRepository personalRecordRepository;

    public MonthlyReportController(WorkoutSessionRepository workoutSessionRepository,
                                   WorkoutSetRepository workoutSetRepository,
                                   PersonalRecordRepository personalRecordRepository) {
        this.workoutSessionRepository = workoutSessionRepository;
        this.workoutSetRepository = workoutSetRepository;
        this.personalRecordRepository = personalRecordRepository;
    }

    @GetMapping("/monthly")
    public ApiResponse<Map<String, Object>> monthly(@RequestParam(required = false) String month) {
        Long userId = CurrentUser.id();
        YearMonth yearMonth = parseMonth(month);
        return ApiResponse.ok(buildMonthlyReport(userId, yearMonth));
    }

    @GetMapping("/monthly/export")
    public ApiResponse<Map<String, Object>> exportMonthly(@RequestParam(required = false) String month) {
        Long userId = CurrentUser.id();
        YearMonth yearMonth = parseMonth(month);
        Map<String, Object> report = buildMonthlyReport(userId, yearMonth);

        String monthToken = yearMonth.toString().replace("-", "");
        String fileName = "fitnote-monthly-" + userId + "-" + monthToken + "-" + System.currentTimeMillis() + ".csv";
        Path reportDir = Paths.get("uploads", "reports");
        Path filePath = reportDir.resolve(fileName);

        try {
            Files.createDirectories(reportDir);
            Files.writeString(filePath, buildCsv(report), StandardCharsets.UTF_8);
        } catch (Exception ex) {
            throw new RuntimeException("failed to export monthly report", ex);
        }

        return ApiResponse.ok(Map.of(
            "month", yearMonth.toString(),
            "fileName", fileName,
            "url", "/uploads/reports/" + fileName,
            "generatedAt", LocalDateTime.now()
        ));
    }

    private YearMonth parseMonth(String month) {
        if (month == null || month.isBlank()) {
            return YearMonth.now();
        }
        return YearMonth.parse(month);
    }

    private Map<String, Object> buildMonthlyReport(Long userId, YearMonth yearMonth) {
        LocalDate start = yearMonth.atDay(1);
        LocalDate end = yearMonth.atEndOfMonth();

        List<WorkoutSession> sessions = workoutSessionRepository.findByUserIdOrderBySessionDateDesc(userId).stream()
            .filter(session -> !session.getSessionDate().isBefore(start) && !session.getSessionDate().isAfter(end))
            .toList();

        BigDecimal totalVolume = sessions.stream()
            .map(WorkoutSession::getTotalVolume)
            .filter(value -> value != null)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        long prCount = personalRecordRepository.findByUserIdOrderByAchievedAtDesc(userId).stream()
            .filter(record -> record.getAchievedAt() != null
                && !record.getAchievedAt().toLocalDate().isBefore(start)
                && !record.getAchievedAt().toLocalDate().isAfter(end))
            .count();

        Set<Long> sessionIds = sessions.stream().map(WorkoutSession::getId).collect(Collectors.toSet());
        List<WorkoutSet> monthSets = sessionIds.stream()
            .flatMap(sessionId -> workoutSetRepository.findBySessionIdOrderByExerciseNameAscSetNoAsc(sessionId).stream())
            .toList();

        List<Map<String, Object>> topExercises = monthSets.stream()
            .filter(set -> set.getWeightKg() != null && set.getReps() != null)
            .collect(Collectors.groupingBy(
                WorkoutSet::getExerciseName,
                LinkedHashMap::new,
                Collectors.reducing(
                    BigDecimal.ZERO,
                    set -> set.getWeightKg().multiply(BigDecimal.valueOf(set.getReps())),
                    BigDecimal::add
                )
            ))
            .entrySet()
            .stream()
            .sorted(Map.Entry.<String, BigDecimal>comparingByValue(Comparator.reverseOrder()))
            .limit(5)
            .map(entry -> Map.<String, Object>of(
                "exerciseName", entry.getKey() == null || entry.getKey().isBlank() ? "Unknown" : entry.getKey(),
                "volume", entry.getValue()
            ))
            .toList();

        String highlight = sessions.isEmpty()
            ? "No workout session recorded this month."
            : "Training consistency improved and output remained stable.";
        String focus = sessions.size() >= 12
            ? "Keep current frequency and optimize recovery quality."
            : "Increase weekly training frequency for better progression.";

        return Map.of(
            "month", yearMonth.toString(),
            "sessionsCount", sessions.size(),
            "totalVolume", totalVolume,
            "prCount", prCount,
            "highlight", highlight,
            "focus", focus,
            "weeklyBreakdown", buildWeeklyBreakdown(sessions, start),
            "topExercises", topExercises
        );
    }

    private List<Map<String, Object>> buildWeeklyBreakdown(List<WorkoutSession> sessions, LocalDate start) {
        return List.of(0, 1, 2, 3).stream()
            .map(index -> {
                LocalDate weekStart = start.plusDays(index * 7L);
                LocalDate weekEnd = weekStart.plusDays(6);
                long count = sessions.stream()
                    .filter(session -> !session.getSessionDate().isBefore(weekStart) && !session.getSessionDate().isAfter(weekEnd))
                    .count();
                return Map.<String, Object>of(
                    "label", "Week " + (index + 1),
                    "count", count
                );
            })
            .toList();
    }

    private String buildCsv(Map<String, Object> report) {
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> weekly = (List<Map<String, Object>>) report.getOrDefault("weeklyBreakdown", List.of());
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> topExercises = (List<Map<String, Object>>) report.getOrDefault("topExercises", List.of());

        StringBuilder sb = new StringBuilder();
        sb.append("FitNote Monthly Report").append('\n');
        sb.append("Month,").append(escapeCsv(String.valueOf(report.getOrDefault("month", "")))).append('\n');
        sb.append("Sessions,").append(escapeCsv(String.valueOf(report.getOrDefault("sessionsCount", 0)))).append('\n');
        sb.append("PR Count,").append(escapeCsv(String.valueOf(report.getOrDefault("prCount", 0)))).append('\n');
        sb.append("Total Volume,").append(escapeCsv(String.valueOf(report.getOrDefault("totalVolume", 0)))).append('\n');
        sb.append("Highlight,").append(escapeCsv(String.valueOf(report.getOrDefault("highlight", "")))).append('\n');
        sb.append("Focus,").append(escapeCsv(String.valueOf(report.getOrDefault("focus", "")))).append('\n');
        sb.append('\n');

        sb.append("Weekly Breakdown").append('\n');
        sb.append("Week,Count").append('\n');
        for (Map<String, Object> row : weekly) {
            sb.append(escapeCsv(String.valueOf(row.getOrDefault("label", "")))).append(',')
                .append(escapeCsv(String.valueOf(row.getOrDefault("count", 0)))).append('\n');
        }
        sb.append('\n');

        sb.append("Top Exercises").append('\n');
        sb.append("Exercise,Volume").append('\n');
        for (Map<String, Object> row : topExercises) {
            sb.append(escapeCsv(String.valueOf(row.getOrDefault("exerciseName", "")))).append(',')
                .append(escapeCsv(String.valueOf(row.getOrDefault("volume", 0)))).append('\n');
        }
        return sb.toString();
    }

    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        String normalized = value.replace("\"", "\"\"");
        if (normalized.contains(",") || normalized.contains("\n") || normalized.contains("\r")) {
            return "\"" + normalized + "\"";
        }
        return normalized;
    }
}
