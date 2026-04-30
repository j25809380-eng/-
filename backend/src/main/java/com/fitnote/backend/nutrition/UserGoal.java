package com.fitnote.backend.nutrition;

import com.fitnote.backend.common.BaseEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "user_goal")
public class UserGoal extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(name = "goal_type", nullable = false, length = 16)
    private String goalType;

    @Column(name = "target_kcal", nullable = false)
    private Integer targetKcal;

    @Column(name = "target_protein", nullable = false)
    private Integer targetProtein;

    @Column(name = "target_carbs", nullable = false)
    private Integer targetCarbs;

    @Column(name = "target_fat", nullable = false)
    private Integer targetFat;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getGoalType() { return goalType; }
    public void setGoalType(String goalType) { this.goalType = goalType; }
    public Integer getTargetKcal() { return targetKcal; }
    public void setTargetKcal(Integer targetKcal) { this.targetKcal = targetKcal; }
    public Integer getTargetProtein() { return targetProtein; }
    public void setTargetProtein(Integer targetProtein) { this.targetProtein = targetProtein; }
    public Integer getTargetCarbs() { return targetCarbs; }
    public void setTargetCarbs(Integer targetCarbs) { this.targetCarbs = targetCarbs; }
    public Integer getTargetFat() { return targetFat; }
    public void setTargetFat(Integer targetFat) { this.targetFat = targetFat; }
}
