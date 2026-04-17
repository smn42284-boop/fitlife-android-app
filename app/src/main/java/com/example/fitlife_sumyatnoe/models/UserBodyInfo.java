package com.example.fitlife_sumyatnoe.models;

import java.io.Serializable;

public class UserBodyInfo implements Serializable {
    private String id;
    private String userId;
    private long birthday;
    private float heightCm;
    private float weightKg;
    private String gender;
    private float bmi;
    private int age;
    private long updatedAt;

    public UserBodyInfo() {
        this.gender = "Not specified";
    }

    public int calculateAge() {
        if (birthday == 0) return 0;
        long now = System.currentTimeMillis();
        long ageMillis = now - birthday;
        return (int) (ageMillis / 31557600000L); // Approximate years
    }

    public String getBmiStatus() {
        if (bmi < 18.5) return "Underweight";
        if (bmi < 25) return "Normal";
        if (bmi < 30) return "Overweight";
        return "Obese";
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public long getBirthday() { return birthday; }
    public void setBirthday(long birthday) { this.birthday = birthday; }

    public float getHeightCm() { return heightCm; }
    public void setHeightCm(float heightCm) { this.heightCm = heightCm; }

    public float getWeightKg() { return weightKg; }
    public void setWeightKg(float weightKg) { this.weightKg = weightKg; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public float getBmi() { return bmi; }
    public void setBmi(float bmi) { this.bmi = bmi; }

    public int getAge() { return calculateAge(); }

    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
}