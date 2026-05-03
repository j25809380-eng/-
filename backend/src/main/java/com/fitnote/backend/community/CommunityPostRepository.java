package com.fitnote.backend.community;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommunityPostRepository extends JpaRepository<CommunityPost, Long> {

    List<CommunityPost> findByAuditStatusOrderByCreatedAtDesc(String auditStatus);

    List<CommunityPost> findTop6ByOrderByCreatedAtDesc();

    List<CommunityPost> findByOrderByCreatedAtDesc();

    List<CommunityPost> findByUserIdInOrderByCreatedAtDesc(List<Long> userIds);
}
