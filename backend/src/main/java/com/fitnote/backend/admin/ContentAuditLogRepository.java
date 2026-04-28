package com.fitnote.backend.admin;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContentAuditLogRepository extends JpaRepository<ContentAuditLog, Long> {

    List<ContentAuditLog> findTop20ByOrderByCreatedAtDesc();

    List<ContentAuditLog> findByCreatedAtGreaterThanEqualOrderByCreatedAtDesc(LocalDateTime createdAt);
}
