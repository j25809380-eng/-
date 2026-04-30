package com.fitnote.backend.nutrition;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserGoalRepository extends JpaRepository<UserGoal, Long> {

    Optional<UserGoal> findByUserId(Long userId);
}
