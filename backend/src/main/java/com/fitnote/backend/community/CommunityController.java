package com.fitnote.backend.community;

import com.fitnote.backend.common.ApiResponse;
import com.fitnote.backend.common.CurrentUser;
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

    private final CommunityService communityService;

    public CommunityController(CommunityService communityService) {
        this.communityService = communityService;
    }

    @GetMapping
    public ApiResponse<List<Map<String, Object>>> list() {
        Long currentUserId = CurrentUser.optionalId().orElse(null);
        return ApiResponse.ok(communityService.listPosts(currentUserId));
    }

    @PostMapping
    public ApiResponse<Map<String, Object>> create(@RequestBody CreatePostRequest request) {
        return ApiResponse.ok(communityService.createPost(
            CurrentUser.id(), request.content(), request.coverImage(),
            request.postType(), request.topicTags()));
    }

    @GetMapping("/{postId}/comments")
    public ApiResponse<List<Map<String, Object>>> comments(@PathVariable Long postId) {
        return ApiResponse.ok(communityService.getComments(postId));
    }

    @PostMapping("/{postId}/comments")
    public ApiResponse<Map<String, Object>> createComment(@PathVariable Long postId,
                                                          @RequestBody CreateCommentRequest request) {
        return ApiResponse.ok(communityService.createComment(
            CurrentUser.id(), postId, request.content(), request.parentId()));
    }

    @PostMapping("/{postId}/like")
    public ApiResponse<Map<String, Object>> toggleLike(@PathVariable Long postId) {
        return ApiResponse.ok(communityService.toggleLike(CurrentUser.id(), postId));
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
