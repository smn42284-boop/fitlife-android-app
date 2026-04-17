package com.example.fitlife_sumyatnoe.models;

import java.io.Serializable;
import java.util.List;

public class UserPreferences implements Serializable {
    private String id;
    private String userId;
    private List<String> fitnessGoals;
    private List<String> preferredWorkouts;
    private String experienceLevel;
    private int workoutFrequency;
    private String activityLevel;
    private boolean nutritionRecommendations;
    private float bmr;
    private float dailyCalorieRecommendation;
    private long updatedAt;

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public List<String> getFitnessGoals() { return fitnessGoals; }
    public void setFitnessGoals(List<String> fitnessGoals) { this.fitnessGoals = fitnessGoals; }

    public List<String> getPreferredWorkouts() { return preferredWorkouts; }
    public void setPreferredWorkouts(List<String> preferredWorkouts) { this.preferredWorkouts = preferredWorkouts; }

    public String getExperienceLevel() { return experienceLevel; }
    public void setExperienceLevel(String experienceLevel) { this.experienceLevel = experienceLevel; }

    public int getWorkoutFrequency() { return workoutFrequency; }
    public void setWorkoutFrequency(int workoutFrequency) { this.workoutFrequency = workoutFrequency; }

    public String getActivityLevel() { return activityLevel; }
    public void setActivityLevel(String activityLevel) { this.activityLevel = activityLevel; }

    public boolean isNutritionRecommendations() { return nutritionRecommendations; }
    public void setNutritionRecommendations(boolean nutritionRecommendations) { this.nutritionRecommendations = nutritionRecommendations; }

    public float getBmr() { return bmr; }
    public void setBmr(float bmr) { this.bmr = bmr; }

    public float getDailyCalorieRecommendation() { return dailyCalorieRecommendation; }
    public void setDailyCalorieRecommendation(float dailyCalorieRecommendation) { this.dailyCalorieRecommendation = dailyCalorieRecommendation; }

    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
}