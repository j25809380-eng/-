package com.fitnote.backend.community;

import com.fitnote.backend.common.ApiResponse;
import com.fitnote.backend.common.CurrentUser;
import com.fitnote.backend.user.User;
import com.fitnote.backend.user.UserRepository;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import java.util.Map;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/community/posts")
public class CommunityController {

    private final CommunityPostRepository communityPostRepository;
    private final CommunityCommentRepository communityCommentRepository;
    private final CommunityLikeRepository communityLikeRepository;
    private final UserRepository userRepository;

    public CommunityController(CommunityPostRepository communityPostRepository,
                               CommunityCommentRepository communityCommentRepository,
                               CommunityLikeRepository communityLikeRepository,
                               UserRepository userRepository) {
        this.communityPostRepository = communityPostRepository;
        this.communityCommentRepository = communityCommentRepository;
        this.communityLikeRepository = communityLikeRepository;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ApiResponse<List<Map<String, Object>>> list() {
        Long currentUserId = CurrentUser.optionalId().orElse(null);
        List<Map<String, Object>> result = communityPostRepository.findAll().stream()
            .filter(post -> "APPROVED".equalsIgnoreCase(post.getAuditStatus())
                || (currentUserId != null && currentUserId.equals(post.getUserId())))
            .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
            .map(post -> Map.<String, Object>ofEntries(
                Map.entry("id", post.getId()),
                Map.entry("authorName", post.getAuthorName()),
                Map.entry("authorAvatar", post.getAuthorAvatar() == null ? "" : post.getAuthorAvatar()),
                Map.entry("content", post.getContent()),
                Map.entry("coverImage", post.getCoverImage() == null ? "" : post.getCoverImage()),
                Map.entry("postType", post.getPostType()),
                Map.entry("topicTags", post.getTopicTags() == null ? "" : post.getTopicTags()),
                Map.entry("auditStatus", post.getAuditStatus() == null ? "" : post.getAuditStatus()),
                Map.entry("likeCount", post.getLikeCount()),
                Map.entry("commentCount", post.getCommentCount()),
                Map.entry("collectCount", post.getCollectCount()),
                Map.entry("liked", currentUserId != null && communityLikeRepository.findByPostIdAndUserId(post.getId(), currentUserId).isPresent()),
                Map.entry("commentsPreview", communityCommentRepository.findByPostIdOrderByCreatedAtAsc(post.getId()).stream()
                    .limit(2)
                    .map(comment -> Map.<String, Object>of(
                        "id", comment.getId(),
                        "authorName", comment.getAuthorName(),
                        "content", comment.getContent(),
                        "createdAt", comment.getCreatedAt()
                    ))
                    .toList()),
                Map.entry("createdAt", post.getCreatedAt())
            ))
            .toList();
        return ApiResponse.ok(result);
    }

    @PostMapping
    public ApiResponse<Map<String, Object>> create(@RequestBody CreatePostRequest request) {
        Long userId = CurrentUser.id();
        User user = userRepository.findById(userId).orElseThrow();

        CommunityPost post = new CommunityPost();
        post.setUserId(userId);
        post.setAuthorName(user.getNickname());
        post.setAuthorAvatar(user.getAvatarUrl());
        post.setContent(request.content());
        post.setCoverImage(request.coverImage());
        post.setPostType(request.postType() == null ? "TRAINING" : request.postType());
        post.setTopicTags(request.topicTags());
        post.setAuditStatus("PENDING");
        communityPostRepository.save(post);

        return ApiResponse.ok(Map.of(
            "created", true,
            "postId", post.getId(),
            "auditStatus", post.getAuditStatus()
        ));
    }

    @GetMapping("/{postId}/comments")
    public ApiResponse<List<Map<String, Object>>> comments(@PathVariable Long postId) {
        List<Map<String, Object>> comments = communityCommentRepository.findByPostIdOrderByCreatedAtAsc(postId).stream()
            .map(comment -> Map.<String, Object>ofEntries(
                Map.entry("id", comment.getId()),
                Map.entry("authorName", comment.getAuthorName()),
                Map.entry("content", comment.getContent()),
                Map.entry("createdAt", comment.getCreatedAt())
            ))
            .toList();
        return ApiResponse.ok(comments);
    }

    @PostMapping("/{postId}/comments")
    public ApiResponse<Map<String, Object>> createComment(@PathVariable Long postId,
                                                          @RequestBody CreateCommentRequest request) {
        Long userId = CurrentUser.id();
        User user = userRepository.findById(userId).orElseThrow();
        CommunityPost post = communityPostRepository.findById(postId).orElseThrow();

        CommunityComment comment = new CommunityComment();
        comment.setPostId(postId);
        comment.setUserId(userId);
        comment.setParentId(request.parentId());
        comment.setContent(request.content());
        comment.setAuthorName(user.getNickname());
        communityCommentRepository.save(comment);

        post.setCommentCount(communityCommentRepository.findByPostIdOrderByCreatedAtAsc(postId).size());
        communityPostRepository.save(post);

        return ApiResponse.ok(Map.of("created", true, "commentId", comment.getId(), "commentCount", post.getCommentCount()));
    }

    @PostMapping("/{postId}/like")
    public ApiResponse<Map<String, Object>> toggleLike(@PathVariable Long postId) {
        Long userId = CurrentUser.id();
        CommunityPost post = communityPostRepository.findById(postId).orElseThrow();
        boolean liked;

        var existing = communityLikeRepository.findByPostIdAndUserId(postId, userId);
        if (existing.isPresent()) {
            communityLikeRepository.delete(existing.get());
            liked = false;
        } else {
            CommunityLike like = new CommunityLike();
            like.setPostId(postId);
            like.setUserId(userId);
            communityLikeRepository.save(like);
            liked = true;
        }

        int likeCount = (int) communityLikeRepository.countByPostId(postId);
        post.setLikeCount(likeCount);
        communityPostRepository.save(post);
        return ApiResponse.ok(Map.of("liked", liked, "likeCount", likeCount));
    }

    public record CreatePostRequest(
        @NotBlank String content,
        String coverImage,
        String postType,
        String topicTags
    ) {
    }

    public record CreateCommentRequest(
        @NotBlank String content,
        Long parentId
    ) {
    }
}
