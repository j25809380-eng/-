package com.fitnote.backend.workout;

import com.fitnote.backend.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "personal_record")
public class PersonalRecord extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "exercise_id", nullable = false)
    private Long exerciseId;

    @Column(name = "exercise_name", length = 64)
    private String exerciseName;

    @Column(name = "record_type", length = 32)
    private String recordType;

    @Column(name = "record_value", precision = 10, scale = 2)
    private BigDecimal recordValue;

    @Column(name = "achieved_at")
    private LocalDateTime achievedAt;

    @Column(name = "source_session_id")
    private Long sourceSessionId;

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getExerciseId() {
        return exerciseId;
    }

    public void setExerciseId(Long exerciseId) {
        this.exerciseId = exerciseId;
    }

    public String getExerciseName() {
        return exerciseName;
    }

    public void setExerciseName(String exerciseName) {
        this.exerciseName = exerciseName;
    }

    public String getRecordType() {
        return recordType;
    }

    public void setRecordType(String recordType) {
        this.recordType = recordType;
    }

    public BigDecimal getRecordValue() {
        return recordValue;
    }

    public void setRecordValue(BigDecimal recordValue) {
        this.recordValue = recordValue;
    }

    public LocalDateTime getAchievedAt() {
        return achievedAt;
    }

    public void setAchievedAt(LocalDateTime achievedAt) {
        this.achievedAt = achievedAt;
    }

    public Long getSourceSessionId() {
        return sourceSessionId;
    }

    public void setSourceSessionId(Long sourceSessionId) {
        this.sourceSessionId = sourceSessionId;
    }
}
