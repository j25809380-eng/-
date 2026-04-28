package com.fitnote.backend.workout;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PersonalRecordRepository extends JpaRepository<PersonalRecord, Long> {

    List<PersonalRecord> findByUserIdOrderByAchievedAtDesc(Long userId);

    List<PersonalRecord> findTop10ByUserIdOrderByAchievedAtDesc(Long userId);

    Optional<PersonalRecord> findTopByUserIdAndExerciseIdAndRecordTypeOrderByRecordValueDesc(Long userId,
                                                                                               Long exerciseId,
                                                                                               String recordType);
}
