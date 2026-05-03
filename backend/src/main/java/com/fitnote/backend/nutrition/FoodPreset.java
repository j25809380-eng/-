package com.fitnote.backend.nutrition;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "food_preset")
public class FoodPreset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64)
    private String name;

    @Column(length = 16)
    private String category;

    @Column(name = "meal_type", length = 16)
    private String mealType;

    @Column(nullable = false)
    private Integer kcal;

    @Column(precision = 6, scale = 1)
    private java.math.BigDecimal protein;

    @Column(precision = 6, scale = 1)
    private java.math.BigDecimal carbs;

    @Column(precision = 6, scale = 1)
    private java.math.BigDecimal fat;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getMealType() { return mealType; }
    public void setMealType(String mealType) { this.mealType = mealType; }
    public Integer getKcal() { return kcal; }
    public void setKcal(Integer kcal) { this.kcal = kcal; }
    public java.math.BigDecimal getProtein() { return protein; }
    public void setProtein(java.math.BigDecimal protein) { this.protein = protein; }
    public java.math.BigDecimal getCarbs() { return carbs; }
    public void setCarbs(java.math.BigDecimal carbs) { this.carbs = carbs; }
    public java.math.BigDecimal getFat() { return fat; }
    public void setFat(java.math.BigDecimal fat) { this.fat = fat; }
}
