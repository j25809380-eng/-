package com.fitnote.backend.plan;

import com.fitnote.backend.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "training_plan_item")
public class TrainingPlanItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "day_id", nullable = false)
    private Long dayId;

    @Column(name = "exercise_id")
    private Long exerciseId;

    @Column(name = "exercise_name", length = 64)
    private String exerciseName;

    @Column(name = "sets_count")
    private Integer setsCount;

    @Column(length = 32)
    private String reps;

    @Column(name = "rest_seconds")
    private Integer restSeconds;

    @Column(name = "weight_mode", length = 32)
    private String weightMode;

    @Column(name = "sort_no")
    private Integer sortNo;

    public Long getId() {
        return id;
    }

    public Long getDayId() {
        return dayId;
    }

    public void setDayId(Long dayId) {
        this.dayId = dayId;
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

    public Integer getSetsCount() {
        return setsCount;
    }

    public void setSetsCount(Integer setsCount) {
        this.setsCount = setsCount;
    }

    public String getReps() {
        return reps;
    }

    public void setReps(String reps) {
        this.reps = reps;
    }

    public Integer getRestSeconds() {
        return restSeconds;
    }

    public void setRestSeconds(Integer restSeconds) {
        this.restSeconds = restSeconds;
    }

    public String getWeightMode() {
        return weightMode;
    }

    public void setWeightMode(String weightMode) {
        this.weightMode = weightMode;
    }

    public Integer getSortNo() {
        return sortNo;
    }

    public void setSortNo(Integer sortNo) {
        this.sortNo = sortNo;
    }
}
