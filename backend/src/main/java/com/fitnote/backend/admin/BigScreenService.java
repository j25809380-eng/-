package com.fitnote.backend.admin;

import com.fitnote.backend.community.CommunityPost;
import com.fitnote.backend.community.CommunityPostRepository;
import com.fitnote.backend.plan.TrainingPlanRepository;
import com.fitnote.backend.user.User;
import com.fitnote.backend.user.UserProfile;
import com.fitnote.backend.user.UserProfileRepository;
import com.fitnote.backend.user.UserRepository;
import com.fitnote.backend.workout.PersonalRecordRepository;
import com.fitnote.backend.workout.WorkoutSession;
import com.fitnote.backend.workout.WorkoutSessionRepository;
import com.fitnote.backend.workout.WorkoutSet;
import com.fitnote.backend.workout.WorkoutSetRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class BigScreenService {

    private static final DateTimeFormatter DAY_FORMAT = DateTimeFormatter.ofPattern("MM-dd");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final WorkoutSessionRepository workoutSessionRepository;
    private final WorkoutSetRepository workoutSetRepository;
    private final CommunityPostRepository communityPostRepository;
    private final PersonalRecordRepository personalRecordRepository;
    private final TrainingPlanRepository trainingPlanRepository;
    private final ContentAuditLogRepository contentAuditLogRepository;

    public BigScreenService(UserRepository userRepository,
                            UserProfileRepository userProfileRepository,
                            WorkoutSessionRepository workoutSessionRepository,
                            WorkoutSetRepository workoutSetRepository,
                            CommunityPostRepository communityPostRepository,
                            PersonalRecordRepository personalRecordRepository,
                            TrainingPlanRepository trainingPlanRepository,
                            ContentAuditLogRepository contentAuditLogRepository) {
        this.userRepository = userRepository;
        this.userProfileRepository = userProfileRepository;
        this.workoutSessionRepository = workoutSessionRepository;
        this.workoutSetRepository = workoutSetRepository;
        this.communityPostRepository = communityPostRepository;
        this.personalRecordRepository = personalRecordRepository;
        this.trainingPlanRepository = trainingPlanRepository;
        this.contentAuditLogRepository = contentAuditLogRepository;
    }

    public Map<String, Object> getBigScreen() {
        long userCount = userRepository.count();
        long sessionCount = workoutSessionRepository.count();
        BigDecimal totalVolume = workoutSessionRepository.findTotalVolume();
        long postCount = communityPostRepository.count();
        long pendingPosts = communityPostRepository.findByAuditStatusOrderByCreatedAtDesc("PENDING").size();
        long customPlans = trainingPlanRepository.countByIsCustomTrue();

        List<UserProfile> profiles = userProfileRepository.findAll();
        List<WorkoutSession> recentSessions = workoutSessionRepository.findByOrderBySessionDateDesc()
            .stream().limit(200).toList();
        List<WorkoutSet> recentSets = workoutSetRepository.findTop200ByOrderByCreatedAtDesc();
        List<ContentAuditLog> auditLogs = contentAuditLogRepository
            .findByCreatedAtGreaterThanEqualOrderByCreatedAtDesc(LocalDateTime.now().minusDays(30));
        List<User> users = userRepository.findAll();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("summary", Map.of(
            "userCount", userCount,
            "workoutCount", sessionCount,
            "postCount", postCount,
            "prCount", personalRecordRepository.count(),
            "pendingPosts", pendingPosts,
            "customPlans", customPlans,
            "totalVolume", totalVolume,
            "auditEvents30d", auditLogs.size()
        ));
        result.put("sessionTrend", buildSessionTrend(recentSessions));
        result.put("volumeTrend", buildVolumeTrend(recentSessions));
        result.put("goalDistribution", buildDistribution(profiles, true));
        result.put("levelDistribution", buildDistribution(profiles, false));
        result.put("topUsers", buildTopUsers(users, recentSessions));
        result.put("hotExercises", buildHotExercises(recentSets));
        result.put("auditAlerts", communityPostRepository.findByAuditStatusOrderByCreatedAtDesc("PENDING")
            .stream().limit(5).map(this::toAlertRow).toList());
        result.put("auditStatusDistribution", buildAuditDist(auditLogs));
        result.put("recentAuditLogs", auditLogs.stream().limit(8).map(this::toLogRow).toList());
        result.put("heatmap", buildHeatmap(recentSessions));
        result.put("generatedAt", LocalDateTime.now().format(TIME_FORMAT));
        return result;
    }

    // ========== sub-builders ==========

    private List<Map<String, Object>> buildSessionTrend(List<WorkoutSession> sessions) {
        LocalDate today = LocalDate.now();
        return buildRecentDays(today, 7).stream()
            .map(date -> Map.<String, Object>of(
                "label", date.format(DAY_FORMAT),
                "value", sessions.stream().filter(s -> date.equals(s.getSessionDate())).count()))
            .toList();
    }

    private List<Map<String, Object>> buildVolumeTrend(List<WorkoutSession> sessions) {
        LocalDate today = LocalDate.now();
        return buildRecentDays(today, 7).stream()
            .map(date -> {
                BigDecimal volume = sessions.stream()
                    .filter(s -> date.equals(s.getSessionDate()))
                    .map(WorkoutSession::getTotalVolume).filter(v -> v != null)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                return Map.<String, Object>of("label", date.format(DAY_FORMAT), "value", volume);
            }).toList();
    }

    private List<Map<String, Object>> buildDistribution(List<UserProfile> profiles, boolean goalMode) {
        return profiles.stream()
            .collect(Collectors.groupingBy(
                p -> safe(goalMode ? p.getTargetType() : p.getTrainingLevel(), "未指定"),
                LinkedHashMap::new, Collectors.counting()))
            .entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue(Comparator.reverseOrder()))
            .map(e -> Map.<String, Object>of("label", e.getKey(), "value", e.getValue()))
            .toList();
    }

    private List<Map<String, Object>> buildTopUsers(List<User> users, List<WorkoutSession> sessions) {
        return users.stream().map(user -> {
            List<WorkoutSession> userSessions = sessions.stream()
                .filter(s -> user.getId().equals(s.getUserId())).toList();
            BigDecimal volume = userSessions.stream()
                .map(WorkoutSession::getTotalVolume).filter(v -> v != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            return Map.<String, Object>of(
                "nickname", safe(user.getNickname(), "Unknown"),
                "sessionCount", userSessions.size(), "totalVolume", volume);
        }).sorted((a, b) -> Integer.compare(
            Integer.parseInt(String.valueOf(b.get("sessionCount"))),
            Integer.parseInt(String.valueOf(a.get("sessionCount")))))
            .limit(5).toList();
    }

    private List<Map<String, Object>> buildHotExercises(List<WorkoutSet> sets) {
        return sets.stream()
            .filter(set -> set.getWeightKg() != null && set.getReps() != null)
            .collect(Collectors.groupingBy(
                set -> safe(set.getExerciseName(), "Unnamed"),
                LinkedHashMap::new,
                Collectors.reducing(BigDecimal.ZERO,
                    set -> set.getWeightKg().multiply(BigDecimal.valueOf(set.getReps())),
                    BigDecimal::add)))
            .entrySet().stream()
            .sorted(Map.Entry.<String, BigDecimal>comparingByValue(Comparator.reverseOrder()))
            .limit(6).map(e -> Map.<String, Object>of("exerciseName", e.getKey(), "volume", e.getValue()))
            .toList();
    }

    private List<Map<String, Object>> buildAuditDist(List<ContentAuditLog> logs) {
        return logs.stream()
            .collect(Collectors.groupingBy(
                log -> safe(log.getAuditStatus(), "UNKNOWN"),
                LinkedHashMap::new, Collectors.counting()))
            .entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue(Comparator.reverseOrder()))
            .map(e -> Map.<String, Object>of("label", e.getKey(), "value", e.getValue()))
            .toList();
    }

    private List<Map<String, Object>> buildHeatmap(List<WorkoutSession> sessions) {
        LocalDate today = LocalDate.now();
        return buildRecentDays(today, 14).stream()
            .map(date -> Map.<String, Object>of(
                "label", date.format(DAY_FORMAT),
                "value", sessions.stream().filter(s -> date.equals(s.getSessionDate())).count()))
            .toList();
    }

    private List<LocalDate> buildRecentDays(LocalDate today, int count) {
        return java.util.stream.IntStream.rangeClosed(0, count - 1)
            .mapToObj(index -> today.minusDays(count - index - 1L)).toList();
    }

    private Map<String, Object> toAlertRow(CommunityPost post) {
        return Map.<String, Object>of(
            "authorName", safe(post.getAuthorName(), "Anonymous"),
            "content", safe(post.getContent(), ""),
            "postType", safe(post.getPostType(), "TRAINING"),
            "createdAt", post.getCreatedAt() == null ? "" : post.getCreatedAt().format(TIME_FORMAT));
    }

    private Map<String, Object> toLogRow(ContentAuditLog log) {
        return Map.<String, Object>of(
            "targetType", safe(log.getTargetType(), ""),
            "targetId", log.getTargetId() == null ? 0L : log.getTargetId(),
            "previousStatus", safe(log.getPreviousStatus(), ""),
            "auditStatus", safe(log.getAuditStatus(), ""),
            "operatorName", safe(log.getOperatorName(), ""),
            "createdAt", log.getCreatedAt() == null ? "" : log.getCreatedAt().format(TIME_FORMAT));
    }

    private String safe(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
