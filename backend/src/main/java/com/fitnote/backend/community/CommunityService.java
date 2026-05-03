package com.fitnote.backend.community;

import com.fitnote.backend.common.BusinessException;
import com.fitnote.backend.common.PageResult;
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

    private final UserFollowRepository userFollowRepository;

    public CommunityService(CommunityPostRepository communityPostRepository,
                            CommunityCommentRepository communityCommentRepository,
                            CommunityLikeRepository communityLikeRepository,
                            UserRepository userRepository,
                            UserFollowRepository userFollowRepository) {
        this.communityPostRepository = communityPostRepository;
        this.communityCommentRepository = communityCommentRepository;
        this.communityLikeRepository = communityLikeRepository;
        this.userRepository = userRepository;
        this.userFollowRepository = userFollowRepository;
    }

    public PageResult<Map<String, Object>> listPosts(Long currentUserId, int page, int size) {
        List<Map<String, Object>> all = communityPostRepository.findByOrderByCreatedAtDesc().stream()
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
        return PageResult.fromList(all, page, size);
    }

    @Transactional
    public Map<String, Object> createPost(Long userId, String content, String coverImage,
                                           String postType, String topicTags) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> BusinessException.notFound("用户不存在"));

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
        User user = userRepository.findById(userId)
            .orElseThrow(() -> BusinessException.notFound("用户不存在"));
        CommunityPost post = communityPostRepository.findById(postId)
            .orElseThrow(() -> BusinessException.notFound("帖子不存在"));

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
        CommunityPost post = communityPostRepository.findById(postId)
            .orElseThrow(() -> BusinessException.notFound("帖子不存在"));
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

    @Transactional
    public Map<String, Object> toggleFollow(Long followerId, Long followingId) {
        if (followerId.equals(followingId)) {
            return Map.of("following", false, "error", "不能关注自己");
        }
        var existing = userFollowRepository.findByFollowerIdAndFollowingId(followerId, followingId);
        boolean following;
        if (existing.isPresent()) {
            userFollowRepository.delete(existing.get());
            following = false;
        } else {
            UserFollow follow = new UserFollow();
            follow.setFollowerId(followerId);
            follow.setFollowingId(followingId);
            userFollowRepository.save(follow);
            following = true;
        }
        return Map.of("following", following,
            "followerCount", userFollowRepository.countByFollowingId(followingId),
            "followingCount", userFollowRepository.countByFollowerId(followerId));
    }

    public Map<String, Object> getFollowStatus(Long userId) {
        List<Long> followingIds = userFollowRepository.findByFollowerId(userId).stream()
            .map(UserFollow::getFollowingId).toList();
        return Map.of("following", followingIds, "followingCount", followingIds.size());
    }

    public PageResult<Map<String, Object>> listFollowingPosts(Long userId, int page, int size) {
        List<Long> followingIds = userFollowRepository.findByFollowerId(userId).stream()
            .map(UserFollow::getFollowingId).toList();
        if (followingIds.isEmpty()) return PageResult.of(List.of(), page, size, 0);

        List<Map<String, Object>> all = communityPostRepository.findByUserIdInOrderByCreatedAtDesc(followingIds).stream()
            .filter(p -> "APPROVED".equalsIgnoreCase(p.getAuditStatus()))
            .map(this::toPostMap)
            .toList();
        return PageResult.fromList(all, page, size);
    }

    private Map<String, Object> toPostMap(CommunityPost post) {
        return Map.<String, Object>ofEntries(
            Map.entry("id", post.getId()),
            Map.entry("authorName", post.getAuthorName()),
            Map.entry("authorAvatar", post.getAuthorAvatar() == null ? "" : post.getAuthorAvatar()),
            Map.entry("content", post.getContent()),
            Map.entry("coverImage", post.getCoverImage() == null ? "" : post.getCoverImage()),
            Map.entry("postType", post.getPostType()),
            Map.entry("topicTags", post.getTopicTags() == null ? "" : post.getTopicTags()),
            Map.entry("likeCount", post.getLikeCount()),
            Map.entry("commentCount", post.getCommentCount()),
            Map.entry("liked", false),
            Map.entry("commentsPreview", List.of()),
            Map.entry("createdAt", post.getCreatedAt())
        );
    }
}
