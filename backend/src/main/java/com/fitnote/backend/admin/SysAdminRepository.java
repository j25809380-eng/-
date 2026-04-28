package com.fitnote.backend.admin;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SysAdminRepository extends JpaRepository<SysAdmin, Long> {

    Optional<SysAdmin> findByUsername(String username);
}
