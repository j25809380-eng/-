package com.fitnote.backend.user;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByOpenId(String openId);

    List<User> findTop5ByOrderByCreatedAtDesc();
}
