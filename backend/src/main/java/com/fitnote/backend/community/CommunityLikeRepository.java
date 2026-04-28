package com.fitnote.backend.community;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommunityLikeRepository extends JpaRepository<CommunityLike, Long> {

    Optional<CommunityLike> findByPostIdAndUserId(Long postId, Long userId);

    long countByPostId(Long postId);
}
