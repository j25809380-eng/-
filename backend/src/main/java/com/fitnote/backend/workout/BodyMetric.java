package com.fitnote.backend.workout;

import com.fitnote.backend.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "user_body_metric")
public class BodyMetric extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "metric_date", nullable = false)
    private LocalDate metricDate;

    @Column(name = "weight_kg", precision = 5, scale = 2)
    private BigDecimal weightKg;

    @Column(name = "body_fat_rate", precision = 5, scale = 2)
    private BigDecimal bodyFatRate;

    @Column(name = "skeletal_muscle_kg", precision = 5, scale = 2)
    private BigDecimal skeletalMuscleKg;

    @Column(length = 255)
    private String remark;

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public LocalDate getMetricDate() {
        return metricDate;
    }

    public void setMetricDate(LocalDate metricDate) {
        this.metricDate = metricDate;
    }

    public BigDecimal getWeightKg() {
        return weightKg;
    }

    public void setWeightKg(BigDecimal weightKg) {
        this.weightKg = weightKg;
    }

    public BigDecimal getBodyFatRate() {
        return bodyFatRate;
    }

    public void setBodyFatRate(BigDecimal bodyFatRate) {
        this.bodyFatRate = bodyFatRate;
    }

    public BigDecimal getSkeletalMuscleKg() {
        return skeletalMuscleKg;
    }

    public void setSkeletalMuscleKg(BigDecimal skeletalMuscleKg) {
        this.skeletalMuscleKg = skeletalMuscleKg;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
