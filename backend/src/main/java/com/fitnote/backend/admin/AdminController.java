package com.fitnote.backend.admin;

import com.fitnote.backend.common.ApiResponse;
import com.fitnote.backend.common.CurrentUser;
import com.fitnote.backend.community.CommunityPost;
import com.fitnote.backend.community.CommunityPostRepository;
import com.fitnote.backend.plan.TrainingPlan;
import com.fitnote.backend.plan.TrainingPlanRepository;
import com.fitnote.backend.user.User;
import com.fitnote.backend.user.UserProfile;
import com.fitnote.backend.user.UserProfileRepository;
import com.fitnote.backend.user.UserRepository;
import com.fitnote.backend.workout.PersonalRecordRepository;
import com.fitnote.backend.workout.WorkoutSession;
import com.fitnote.backend.workout.WorkoutSessionRepository;
import com.fitnote.backend.workout.WorkoutSetRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final WorkoutSessionRepository workoutSessionRepository;
    private final WorkoutSetRepository workoutSetRepository;
    private final CommunityPostRepository communityPostRepository;
    private final PersonalRecordRepository personalRecordRepository;
    private final TrainingPlanRepository trainingPlanRepository;
    private final ContentAuditLogRepository contentAuditLogRepository;
    private final SysAdminRepository sysAdminRepository;

    public AdminController(UserRepository userRepository,
                           UserProfileRepository userProfileRepository,
                           WorkoutSessionRepository workoutSessionRepository,
                           WorkoutSetRepository workoutSetRepository,
                           CommunityPostRepository communityPostRepository,
                           PersonalRecordRepository personalRecordRepository,
                           TrainingPlanRepository trainingPlanRepository,
                           ContentAuditLogRepository contentAuditLogRepository,
                           SysAdminRepository sysAdminRepository) {
        this.userRepository = userRepository;
        this.userProfileRepository = userProfileRepository;
        this.workoutSessionRepository = workoutSessionRepository;
        this.workoutSetRepository = workoutSetRepository;
        this.communityPostRepository = communityPostRepository;
        this.personalRecordRepository = personalRecordRepository;
        this.trainingPlanRepository = trainingPlanRepository;
        this.contentAuditLogRepository = contentAuditLogRepository;
        this.sysAdminRepository = sysAdminRepository;
    }

    @GetMapping("/dashboard")
    public ApiResponse<Map<String, Object>> dashboard() {
        long userCount = userRepository.count();
        long workoutCount = workoutSessionRepository.count();
        long postCount = communityPostRepository.count();
        long pendingPosts = communityPostRepository.findByAuditStatusOrderByCreatedAtDesc("PENDING").size();
        BigDecimal totalVolume = workoutSessionRepository.findAll().stream()
            .map(WorkoutSession::getTotalVolume)
            .filter(value -> value != null)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        LocalDate today = LocalDate.now();
        List<Map<String, Object>> weeklyTrend = java.util.stream.IntStream.rangeClosed(0, 6)
            .mapToObj(offset -> today.minusDays(6L - offset))
            .map(day -> Map.<String, Object>of(
                "label", day.getMonthValue() + "/" + day.getDayOfMonth(),
                "value", workoutSessionRepository.findAll().stream()
                    .filter(session -> day.equals(session.getSessionDate()))
                    .count()
            ))
            .toList();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("summary", Map.of(
            "userCount", userCount,
            "workoutCount", workoutCount,
            "postCount", postCount,
            "prCount", personalRecordRepository.count(),
            "activePlans", trainingPlanRepository.count(),
            "pendingPosts", pendingPosts,
            "totalVolume", totalVolume
        ));
        result.put("recentUsers", userRepository.findAll().stream().limit(5).map(this::toUserRow).toList());
        result.put("recentWorkouts", workoutSessionRepository.findAll().stream()
            .sorted((a, b) -> b.getSessionDate().compareTo(a.getSessionDate()))
            .limit(6)
            .map(this::toWorkoutRow)
            .toList());
        result.put("auditQueue", communityPostRepository.findAll().stream()
            .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
            .limit(6)
            .map(this::toPostRow)
            .toList());
        result.put("weeklyTrend", weeklyTrend);
        return ApiResponse.ok(result);
    }

    @GetMapping("/users")
    public ApiResponse<List<Map<String, Object>>> users() {
        return ApiResponse.ok(userRepository.findAll().stream().map(this::toUserRow).toList());
    }

    @GetMapping("/workouts")
    public ApiResponse<List<Map<String, Object>>> workouts() {
        List<Map<String, Object>> workouts = workoutSessionRepository.findAll().stream()
            .sorted((a, b) -> b.getSessionDate().compareTo(a.getSessionDate()))
            .map(this::toWorkoutRow)
            .toList();
        return ApiResponse.ok(workouts);
    }

    @GetMapping("/posts")
    public ApiResponse<List<Map<String, Object>>> posts(@RequestParam(defaultValue = "") String status) {
        List<CommunityPost> posts = status.isBlank()
            ? communityPostRepository.findAll().stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .toList()
            : communityPostRepository.findByAuditStatusOrderByCreatedAtDesc(status);
        return ApiResponse.ok(posts.stream().map(this::toPostRow).toList());
    }

    @PutMapping("/posts/{id}/audit")
    public ApiResponse<Map<String, Object>> audit(@PathVariable Long id,
                                                  @RequestParam String status,
                                                  @RequestParam(defaultValue = "") String reason) {
        CommunityPost post = communityPostRepository.findById(id).orElseThrow();
        String previousStatus = safeText(post.getAuditStatus(), "PENDING");
        post.setAuditStatus(status);
        communityPostRepository.save(post);

        Long operatorId = CurrentUser.id();
        String operatorName = sysAdminRepository.findById(operatorId)
            .map(admin -> safeText(admin.getNickname(), admin.getUsername()))
            .orElse("admin#" + operatorId);

        ContentAuditLog log = new ContentAuditLog();
        log.setTargetType("COMMUNITY_POST");
        log.setTargetId(post.getId());
        log.setPreviousStatus(previousStatus);
        log.setAuditStatus(status);
        log.setReason(reason);
        log.setOperatorId(operatorId);
        log.setOperatorName(operatorName);
        log.setTargetSnapshot(buildPostSnapshot(post));
        log.setExtraPayload("postType=" + safeText(post.getPostType(), "UNKNOWN")
            + ";topicTags=" + safeText(post.getTopicTags(), ""));
        contentAuditLogRepository.save(log);

        return ApiResponse.ok(Map.of(
            "updated", true,
            "id", post.getId(),
            "status", post.getAuditStatus(),
            "previousStatus", previousStatus
        ));
    }

    @GetMapping("/plans")
    public ApiResponse<List<Map<String, Object>>> plans() {
        return ApiResponse.ok(trainingPlanRepository.findAll().stream().map(this::toPlanRow).toList());
    }

    @GetMapping("/audit-logs")
    public ApiResponse<List<Map<String, Object>>> auditLogs(@RequestParam(defaultValue = "") String status,
                                                            @RequestParam(defaultValue = "") String targetType,
                                                            @RequestParam(defaultValue = "30") Integer days,
                                                            @RequestParam(defaultValue = "100") Integer limit) {
        List<ContentAuditLog> source = days != null && days > 0
            ? contentAuditLogRepository.findByCreatedAtGreaterThanEqualOrderByCreatedAtDesc(LocalDateTime.now().minusDays(days))
            : contentAuditLogRepository.findAll().stream()
                .sorted(Comparator.comparing(ContentAuditLog::getCreatedAt).reversed())
                .toList();

        Stream<ContentAuditLog> filtered = source.stream();
        if (status != null && !status.isBlank()) {
            filtered = filtered.filter(log -> status.equalsIgnoreCase(safeText(log.getAuditStatus(), "")));
        }
        if (targetType != null && !targetType.isBlank()) {
            filtered = filtered.filter(log -> targetType.equalsIgnoreCase(safeText(log.getTargetType(), "")));
        }

        int safeLimit = limit == null || limit <= 0 ? 100 : Math.min(limit, 500);
        List<Map<String, Object>> result = filtered
            .sorted(Comparator.comparing(ContentAuditLog::getCreatedAt).reversed())
            .limit(safeLimit)
            .map(log -> Map.<String, Object>ofEntries(
                Map.entry("id", log.getId()),
                Map.entry("targetType", safeText(log.getTargetType(), "")),
                Map.entry("targetId", log.getTargetId() == null ? 0L : log.getTargetId()),
                Map.entry("previousStatus", safeText(log.getPreviousStatus(), "")),
                Map.entry("auditStatus", safeText(log.getAuditStatus(), "")),
                Map.entry("reason", safeText(log.getReason(), "")),
                Map.entry("operatorId", log.getOperatorId() == null ? 0L : log.getOperatorId()),
                Map.entry("operatorName", safeText(log.getOperatorName(), "")),
                Map.entry("targetSnapshot", safeText(log.getTargetSnapshot(), "")),
                Map.entry("extraPayload", safeText(log.getExtraPayload(), "")),
                Map.entry("createdAt", log.getCreatedAt())
            ))
            .toList();

        return ApiResponse.ok(result);
    }

    private String buildPostSnapshot(CommunityPost post) {
        String content = safeText(post.getContent(), "");
        String excerpt = content.length() > 120 ? content.substring(0, 120) + "..." : content;
        return "author=" + safeText(post.getAuthorName(), "")
            + ";postType=" + safeText(post.getPostType(), "")
            + ";content=" + excerpt;
    }

    private Map<String, Object> toUserRow(User user) {
        UserProfile profile = userProfileRepository.findByUserId(user.getId()).orElse(null);
        long sessions = workoutSessionRepository.findByUserIdOrderBySessionDateDesc(user.getId()).size();
        long prs = personalRecordRepository.findByUserIdOrderByAchievedAtDesc(user.getId()).size();
        return Map.ofEntries(
            Map.entry("id", user.getId()),
            Map.entry("nickname", safeText(user.getNickname(), "")),
            Map.entry("avatarUrl", safeText(user.getAvatarUrl(), "")),
            Map.entry("status", user.getStatus()),
            Map.entry("phone", safeText(user.getPhone(), "")),
            Map.entry("trainingLevel", profile == null ? "-" : safeText(profile.getTrainingLevel(), "-")),
            Map.entry("targetType", profile == null ? "-" : safeText(profile.getTargetType(), "-")),
            Map.entry("weightKg", profile == null || profile.getWeightKg() == null ? "-" : profile.getWeightKg()),
            Map.entry("sessionCount", sessions),
            Map.entry("prCount", prs),
            Map.entry("createdAt", user.getCreatedAt())
        );
    }

    private Map<String, Object> toWorkoutRow(WorkoutSession workout) {
        String userName = userRepository.findById(workout.getUserId()).map(User::getNickname).orElse("Unknown");
        long setCount = workoutSetRepository.findBySessionIdOrderByExerciseNameAscSetNoAsc(workout.getId()).size();
        return Map.ofEntries(
            Map.entry("id", workout.getId()),
            Map.entry("title", safeText(workout.getTitle(), "")),
            Map.entry("focus", safeText(workout.getFocus(), "")),
            Map.entry("sessionDate", workout.getSessionDate()),
            Map.entry("durationMinutes", workout.getDurationMinutes() == null ? 0 : workout.getDurationMinutes()),
            Map.entry("totalVolume", workout.getTotalVolume() == null ? BigDecimal.ZERO : workout.getTotalVolume()),
            Map.entry("calories", workout.getCalories() == null ? 0 : workout.getCalories()),
            Map.entry("completionStatus", safeText(workout.getCompletionStatus(), "")),
            Map.entry("setCount", setCount),
            Map.entry("userName", userName)
        );
    }

    private Map<String, Object> toPostRow(CommunityPost post) {
        return Map.ofEntries(
            Map.entry("id", post.getId()),
            Map.entry("authorName", safeText(post.getAuthorName(), "")),
            Map.entry("content", safeText(post.getContent(), "")),
            Map.entry("postType", safeText(post.getPostType(), "")),
            Map.entry("topicTags", safeText(post.getTopicTags(), "")),
            Map.entry("auditStatus", safeText(post.getAuditStatus(), "")),
            Map.entry("likeCount", post.getLikeCount() == null ? 0 : post.getLikeCount()),
            Map.entry("commentCount", post.getCommentCount() == null ? 0 : post.getCommentCount()),
            Map.entry("createdAt", post.getCreatedAt())
        );
    }

    private Map<String, Object> toPlanRow(TrainingPlan plan) {
        return Map.ofEntries(
            Map.entry("id", plan.getId()),
            Map.entry("title", safeText(plan.getTitle(), "")),
            Map.entry("targetType", safeText(plan.getTargetType(), "")),
            Map.entry("difficulty", safeText(plan.getDifficulty(), "")),
            Map.entry("durationWeeks", plan.getDurationWeeks() == null ? 0 : plan.getDurationWeeks()),
            Map.entry("daysPerWeek", plan.getDaysPerWeek() == null ? 0 : plan.getDaysPerWeek()),
            Map.entry("isCustom", Boolean.TRUE.equals(plan.getCustomPlan())),
            Map.entry("summary", safeText(plan.getSummary(), ""))
        );
    }

    private String safeText(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
