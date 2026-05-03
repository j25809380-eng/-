package com.fitnote.backend.admin;

import com.fitnote.backend.common.BusinessException;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final WorkoutSessionRepository workoutSessionRepository;
    private final WorkoutSetRepository workoutSetRepository;
    private final CommunityPostRepository communityPostRepository;
    private final PersonalRecordRepository personalRecordRepository;
    private final TrainingPlanRepository trainingPlanRepository;
    private final ContentAuditLogRepository contentAuditLogRepository;
    private final SysAdminRepository sysAdminRepository;

    public AdminService(UserRepository userRepository,
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

    public Map<String, Object> getDashboard() {
        long userCount = userRepository.count();
        long workoutCount = workoutSessionRepository.count();
        long postCount = communityPostRepository.count();
        long pendingPosts = communityPostRepository.findByAuditStatusOrderByCreatedAtDesc("PENDING").size();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("summary", Map.of(
            "userCount", userCount,
            "workoutCount", workoutCount,
            "postCount", postCount,
            "prCount", personalRecordRepository.count(),
            "activePlans", trainingPlanRepository.count(),
            "pendingPosts", pendingPosts,
            "totalVolume", workoutSessionRepository.findTotalVolume()
        ));
        result.put("recentUsers", userRepository.findTop5ByOrderByCreatedAtDesc().stream().map(this::toUserRow).toList());
        result.put("recentWorkouts", workoutSessionRepository.findTop6ByOrderBySessionDateDesc().stream().map(this::toWorkoutRow).toList());
        result.put("auditQueue", communityPostRepository.findTop6ByOrderByCreatedAtDesc().stream().map(this::toPostRow).toList());
        result.put("weeklyTrend", buildWeeklyTrend());
        return result;
    }

    public List<Map<String, Object>> getUsers() {
        return userRepository.findAll().stream().map(this::toUserRow).toList();
    }

    public List<Map<String, Object>> getWorkouts() {
        return workoutSessionRepository.findByOrderBySessionDateDesc().stream().map(this::toWorkoutRow).toList();
    }

    public List<Map<String, Object>> getPosts(String status) {
        List<CommunityPost> posts = status.isBlank()
            ? communityPostRepository.findByOrderByCreatedAtDesc()
            : communityPostRepository.findByAuditStatusOrderByCreatedAtDesc(status);
        return posts.stream().map(this::toPostRow).toList();
    }

    @Transactional
    public Map<String, Object> auditPost(Long postId, String status, String reason, Long operatorId) {
        CommunityPost post = communityPostRepository.findById(postId)
            .orElseThrow(() -> BusinessException.notFound("帖子不存在"));
        String previousStatus = safeText(post.getAuditStatus(), "PENDING");
        post.setAuditStatus(status);
        communityPostRepository.save(post);

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

        return Map.of("updated", true, "id", post.getId(), "status", post.getAuditStatus(), "previousStatus", previousStatus);
    }

    public List<Map<String, Object>> getPlans() {
        return trainingPlanRepository.findAll().stream().map(this::toPlanRow).toList();
    }

    public List<Map<String, Object>> getAuditLogs(String status, String targetType, Integer days, Integer limit) {
        List<ContentAuditLog> source = days != null && days > 0
            ? contentAuditLogRepository.findByCreatedAtGreaterThanEqualOrderByCreatedAtDesc(LocalDateTime.now().minusDays(days))
            : contentAuditLogRepository.findByOrderByCreatedAtDesc();

        Stream<ContentAuditLog> filtered = source.stream();
        if (status != null && !status.isBlank())
            filtered = filtered.filter(log -> status.equalsIgnoreCase(safeText(log.getAuditStatus(), "")));
        if (targetType != null && !targetType.isBlank())
            filtered = filtered.filter(log -> targetType.equalsIgnoreCase(safeText(log.getTargetType(), "")));

        int safeLimit = limit == null || limit <= 0 ? 100 : Math.min(limit, 500);
        return filtered.sorted(Comparator.comparing(ContentAuditLog::getCreatedAt).reversed())
            .limit(safeLimit).map(this::toAuditLogRow).toList();
    }

    // ========== mapper helpers ==========

    private List<Map<String, Object>> buildWeeklyTrend() {
        LocalDate today = LocalDate.now();
        return java.util.stream.IntStream.rangeClosed(0, 6)
            .mapToObj(offset -> today.minusDays(6L - offset))
            .map(day -> Map.<String, Object>of(
                "label", day.getMonthValue() + "/" + day.getDayOfMonth(),
                "value", workoutSessionRepository.countBySessionDate(day)))
            .toList();
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
            Map.entry("trainingLevel", profile == null ? "-" : safeText(profile.getTrainingLevel(), "-")),
            Map.entry("targetType", profile == null ? "-" : safeText(profile.getTargetType(), "-")),
            Map.entry("sessionCount", sessions),
            Map.entry("prCount", prs),
            Map.entry("createdAt", user.getCreatedAt())
        );
    }

    private Map<String, Object> toWorkoutRow(WorkoutSession w) {
        String userName = userRepository.findById(w.getUserId()).map(User::getNickname).orElse("Unknown");
        long setCount = workoutSetRepository.findBySessionIdOrderByExerciseNameAscSetNoAsc(w.getId()).size();
        return Map.ofEntries(
            Map.entry("id", w.getId()),
            Map.entry("title", safeText(w.getTitle(), "")),
            Map.entry("focus", safeText(w.getFocus(), "")),
            Map.entry("sessionDate", w.getSessionDate()),
            Map.entry("durationMinutes", w.getDurationMinutes() == null ? 0 : w.getDurationMinutes()),
            Map.entry("totalVolume", w.getTotalVolume() == null ? BigDecimal.ZERO : w.getTotalVolume()),
            Map.entry("calories", w.getCalories() == null ? 0 : w.getCalories()),
            Map.entry("completionStatus", safeText(w.getCompletionStatus(), "")),
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

    private Map<String, Object> toAuditLogRow(ContentAuditLog log) {
        return Map.ofEntries(
            Map.entry("id", log.getId()),
            Map.entry("targetType", safeText(log.getTargetType(), "")),
            Map.entry("targetId", log.getTargetId() == null ? 0L : log.getTargetId()),
            Map.entry("previousStatus", safeText(log.getPreviousStatus(), "")),
            Map.entry("auditStatus", safeText(log.getAuditStatus(), "")),
            Map.entry("reason", safeText(log.getReason(), "")),
            Map.entry("operatorId", log.getOperatorId() == null ? 0L : log.getOperatorId()),
            Map.entry("operatorName", safeText(log.getOperatorName(), "")),
            Map.entry("createdAt", log.getCreatedAt())
        );
    }

    private String safeText(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
