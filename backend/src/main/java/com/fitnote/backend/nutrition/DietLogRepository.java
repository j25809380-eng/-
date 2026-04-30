package com.fitnote.backend.nutrition;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DietLogRepository extends JpaRepository<DietLog, Long> {

    List<DietLog> findByUserIdAndLogDate(Long userId, LocalDate logDate);
}
