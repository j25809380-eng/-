package com.fitnote.backend.analytics;

import com.fitnote.backend.common.ApiResponse;
import com.fitnote.backend.common.CurrentUser;
import com.fitnote.backend.user.User;
import com.fitnote.backend.user.UserRepository;
import com.fitnote.backend.workout.BodyMetricRepository;
import com.fitnote.backend.workout.PersonalRecord;
import com.fitnote.backend.workout.PersonalRecordRepository;
import com.fitnote.backend.workout.WorkoutSession;
import com.fitnote.backend.workout.WorkoutSessionRepository;
import com.fitnote.backend.workout.WorkoutSetRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final WorkoutSessionRepository workoutSessionRepository;
    private final WorkoutSetRepository workoutSetRepository;
    private final BodyMetricRepository bodyMetricRepository;
    private final PersonalRecordRepository personalRecordRepository;
    private final UserRepository userRepository;

    public AnalyticsController(WorkoutSessionRepository workoutSessionRepository,
                               WorkoutSetRepository workoutSetRepository,
                               BodyMetricRepository bodyMetricRepository,
                               PersonalRecordRepository personalRecordRepository,
                               UserRepository userRepository) {
        this.workoutSessionRepository = workoutSessionRepository;
        this.workoutSetRepository = workoutSetRepository;
        this.bodyMetricRepository = bodyMetricRepository;
        this.personalRecordRepository = personalRecordRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/overview")
    public ApiResponse<Map<String, Object>> overview() {
        Long userId = CurrentUser.id();
        List<WorkoutSession> sessions = workoutSessionRepository.findByUserIdOrderBySessionDateDesc(userId);
        BigDecimal totalVolume = sessions.stream()
            .map(WorkoutSession::getTotalVolume)
            .filter(value -> value != null)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        long prs = workoutSetRepository.findAll().stream()
            .filter(set -> userId.equals(set.getUserId()) && Boolean.TRUE.equals(set.getPr()))
            .count();

        return ApiResponse.ok(Map.of(
            "summary", Map.of(
                "totalSessions", sessions.size(),
                "weeklyFrequency", workoutSessionRepository.countByUserIdAndSessionDateBetween(
                    userId, LocalDate.now().minusDays(29), LocalDate.now()),
                "totalVolume", totalVolume,
                "prCount", prs
            ),
            "weightTrend", bodyMetricRepository.findByUserIdOrderByMetricDateAsc(userId).stream()
                .map(metric -> Map.<String, Object>of(
                    "date", metric.getMetricDate(),
                    "weightKg", metric.getWeightKg()
                ))
                .toList(),
            "volumeTrend", sessions.stream()
                .limit(6)
                .sorted(Comparator.comparing(WorkoutSession::getSessionDate))
                .map(session -> Map.<String, Object>of(
                    "date", session.getSessionDate(),
                    "volume", session.getTotalVolume() == null ? BigDecimal.ZERO : session.getTotalVolume()
                ))
                .toList(),
            "prBoard", personalRecordRepository.findTop10ByUserIdOrderByAchievedAtDesc(userId).stream()
                .map(record -> Map.<String, Object>ofEntries(
                    Map.entry("exerciseName", record.getExerciseName()),
                    Map.entry("recordType", record.getRecordType()),
                    Map.entry("recordValue", record.getRecordValue()),
                    Map.entry("achievedAt", record.getAchievedAt())
                ))
                .toList(),
            "monthlyReport", Map.of(
                "month", LocalDate.now().getMonthValue(),
                "highlight", "本月训练输出稳定，力量稳定性提升明显。",
                "focus", "保持每周3-4次训练，重视恢复质量。"
            )
        ));
    }

    @GetMapping("/personal-records")
    public ApiResponse<List<Map<String, Object>>> personalRecords() {
        Long userId = CurrentUser.id();
        List<Map<String, Object>> records = personalRecordRepository.findByUserIdOrderByAchievedAtDesc(userId).stream()
            .map(record -> Map.<String, Object>ofEntries(
                Map.entry("id", record.getId()),
                Map.entry("exerciseId", record.getExerciseId()),
                Map.entry("exerciseName", record.getExerciseName()),
                Map.entry("recordType", record.getRecordType()),
                Map.entry("recordValue", record.getRecordValue()),
                Map.entry("achievedAt", record.getAchievedAt())
            ))
            .toList();
        return ApiResponse.ok(records);
    }

    @GetMapping("/heatmap")
    public ApiResponse<List<Map<String, Object>>> heatmap(
            @RequestParam(defaultValue = "84") int days) {
        Long userId = CurrentUser.id();
        return ApiResponse.ok(workoutSessionRepository
            .findByUserIdAndSessionDateBetween(userId,
                LocalDate.now().minusDays(days), LocalDate.now())
            .stream()
            .collect(Collectors.groupingBy(
                WorkoutSession::getSessionDate,
                Collectors.counting()))
            .entrySet().stream()
            .map(e -> Map.<String, Object>of("date", e.getKey().toString(), "count", e.getValue()))
            .toList());
    }

    @GetMapping("/rankings")
    public ApiResponse<List<Map<String, Object>>> rankings() {
        Long currentUserId = CurrentUser.id();
        List<User> users = userRepository.findAll();

        Map<Long, List<WorkoutSession>> sessionsByUser = workoutSessionRepository.findAll().stream()
            .collect(Collectors.groupingBy(WorkoutSession::getUserId));
        Map<Long, Long> prByUser = personalRecordRepository.findAll().stream()
            .collect(Collectors.groupingBy(PersonalRecord::getUserId, Collectors.counting()));

        List<RankingRow> rows = new ArrayList<>();
        for (User user : users) {
            List<WorkoutSession> sessions = sessionsByUser.getOrDefault(user.getId(), List.of());
            BigDecimal totalVolume = sessions.stream()
                .map(WorkoutSession::getTotalVolume)
                .filter(value -> value != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            long sessionCount = sessions.size();
            long prCount = prByUser.getOrDefault(user.getId(), 0L);
            long score = totalVolume.longValue() + (sessionCount * 300L) + (prCount * 500L);
            String nickname = user.getNickname() == null || user.getNickname().isBlank() ? "User-" + user.getId() : user.getNickname();
            rows.add(new RankingRow(user.getId(), nickname, score, sessionCount, prCount));
        }

        List<RankingRow> sorted = rows.stream()
            .sorted(Comparator.comparingLong(RankingRow::score).reversed())
            .toList();

        List<Map<String, Object>> result = new ArrayList<>();
        for (int i = 0; i < sorted.size(); i++) {
            RankingRow row = sorted.get(i);
            result.add(Map.of(
                "rank", i + 1,
                "nickname", row.nickname(),
                "score", row.score(),
                "label", rankLabel(i + 1),
                "sessionCount", row.sessionCount(),
                "prCount", row.prCount(),
                "isMe", row.userId().equals(currentUserId)
            ));
        }

        return ApiResponse.ok(result.stream().limit(20).toList());
    }

    private String rankLabel(int rank) {
        if (rank == 1) {
            return "王者";
        }
        if (rank <= 3) {
            return "大师";
        }
        if (rank <= 10) {
            return "精英";
        }
        return "新锐";
    }

    private record RankingRow(Long userId, String nickname, long score, long sessionCount, long prCount) {
    }
}
