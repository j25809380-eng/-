package com.fitnote.backend.community;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserFollowRepository extends JpaRepository<UserFollow, Long> {

    Optional<UserFollow> findByFollowerIdAndFollowingId(Long followerId, Long followingId);

    List<UserFollow> findByFollowerId(Long followerId);

    List<UserFollow> findByFollowingId(Long followingId);

    long countByFollowingId(Long followingId);

    long countByFollowerId(Long followerId);

    void deleteByFollowerIdAndFollowingId(Long followerId, Long followingId);
}
