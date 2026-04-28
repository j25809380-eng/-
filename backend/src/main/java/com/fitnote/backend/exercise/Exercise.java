package com.fitnote.backend.exercise;

import com.fitnote.backend.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

@Entity
@Table(name = "exercise")
public class Exercise extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64)
    private String name;

    @Column(length = 32)
    private String category;

    @Column(length = 32)
    private String equipment;

    @Column(length = 16)
    private String difficulty;

    @Column(name = "primary_muscle", length = 32)
    private String primaryMuscle;

    @Column(name = "secondary_muscles", length = 255)
    private String secondaryMuscles;

    @Column(name = "cover_image", length = 255)
    private String coverImage;

    @Lob
    private String description;

    @Lob
    @Column(name = "movement_steps")
    private String movementSteps;

    @Lob
    private String tips;

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getEquipment() {
        return equipment;
    }

    public void setEquipment(String equipment) {
        this.equipment = equipment;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public String getPrimaryMuscle() {
        return primaryMuscle;
    }

    public void setPrimaryMuscle(String primaryMuscle) {
        this.primaryMuscle = primaryMuscle;
    }

    public String getSecondaryMuscles() {
        return secondaryMuscles;
    }

    public void setSecondaryMuscles(String secondaryMuscles) {
        this.secondaryMuscles = secondaryMuscles;
    }

    public String getCoverImage() {
        return coverImage;
    }

    public void setCoverImage(String coverImage) {
        this.coverImage = coverImage;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getMovementSteps() {
        return movementSteps;
    }

    public void setMovementSteps(String movementSteps) {
        this.movementSteps = movementSteps;
    }

    public String getTips() {
        return tips;
    }

    public void setTips(String tips) {
        this.tips = tips;
    }
}
