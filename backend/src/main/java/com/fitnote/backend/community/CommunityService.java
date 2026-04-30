package com.fitnote.backend.community;

import com.fitnote.backend.user.User;
import com.fitnote.backend.user.UserRepository;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CommunityService {

    private final CommunityPostRepository communityPostRepository;
    private final CommunityCommentRepository communityCommentRepository;
    private final CommunityLikeRepository communityLikeRepository;
    private final UserRepository userRepository;

    public CommunityService(CommunityPostRepository communityPostRepository,
                            CommunityCommentRepository communityCommentRepository,
                            CommunityLikeRepository communityLikeRepository,
                            UserRepository userRepository) {
        this.communityPostRepository = communityPostRepository;
        this.communityCommentRepository = communityCommentRepository;
        this.communityLikeRepository = communityLikeRepository;
        this.userRepository = userRepository;
    }

    public List<Map<String, Object>> listPosts(Long currentUserId) {
        return communityPostRepository.findAll().stream()
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
                Map.entry("liked", currentUserId != null
                    && communityLikeRepository.findByPostIdAndUserId(post.getId(), currentUserId).isPresent()),
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
    }

    @Transactional
    public Map<String, Object> createPost(Long userId, String content, String coverImage,
                                           String postType, String topicTags) {
        User user = userRepository.findById(userId).orElseThrow();

        CommunityPost post = new CommunityPost();
        post.setUserId(userId);
        post.setAuthorName(user.getNickname());
        post.setAuthorAvatar(user.getAvatarUrl());
        post.setContent(content);
        post.setCoverImage(coverImage);
        post.setPostType(postType == null ? "TRAINING" : postType);
        post.setTopicTags(topicTags);
        post.setAuditStatus("PENDING");
        communityPostRepository.save(post);

        return Map.of(
            "created", true,
            "postId", post.getId(),
            "auditStatus", post.getAuditStatus()
        );
    }

    public List<Map<String, Object>> getComments(Long postId) {
        return communityCommentRepository.findByPostIdOrderByCreatedAtAsc(postId).stream()
            .map(comment -> Map.<String, Object>ofEntries(
                Map.entry("id", comment.getId()),
                Map.entry("authorName", comment.getAuthorName()),
                Map.entry("content", comment.getContent()),
                Map.entry("createdAt", comment.getCreatedAt())
            ))
            .toList();
    }

    @Transactional
    public Map<String, Object> createComment(Long userId, Long postId, String content, Long parentId) {
        User user = userRepository.findById(userId).orElseThrow();
        CommunityPost post = communityPostRepository.findById(postId).orElseThrow();

        CommunityComment comment = new CommunityComment();
        comment.setPostId(postId);
        comment.setUserId(userId);
        comment.setParentId(parentId);
        comment.setContent(content);
        comment.setAuthorName(user.getNickname());
        communityCommentRepository.save(comment);

        post.setCommentCount(communityCommentRepository.findByPostIdOrderByCreatedAtAsc(postId).size());
        communityPostRepository.save(post);

        return Map.of("created", true, "commentId", comment.getId(), "commentCount", post.getCommentCount());
    }

    @Transactional
    public Map<String, Object> toggleLike(Long userId, Long postId) {
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
        return Map.of("liked", liked, "likeCount", likeCount);
    }
}
