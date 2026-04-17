package com.example.fitlife_sumyatnoe.models;

import java.io.Serializable;

public class UserBMIHistory implements Serializable {
    private String id;
    private String userId;
    private float bmi;
    private String status;
    private float heightCm;
    private float weightKg;
    private long recordedAt;

    public UserBMIHistory() {}

    public UserBMIHistory(String userId, float bmi, String status, float heightCm, float weightKg) {
        this.userId = userId;
        this.bmi = bmi;
        this.status = status;
        this.heightCm = heightCm;
        this.weightKg = weightKg;
        this.recordedAt = System.currentTimeMillis();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public float getBmi() { return bmi; }
    public void setBmi(float bmi) { this.bmi = bmi; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public float getHeightCm() { return heightCm; }
    public void setHeightCm(float heightCm) { this.heightCm = heightCm; }

    public float getWeightKg() { return weightKg; }
    public void setWeightKg(float weightKg) { this.weightKg = weightKg; }

    public long getRecordedAt() { return recordedAt; }
    public void setRecordedAt(long recordedAt) { this.recordedAt = recordedAt; }

    public String getFormattedDate() {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault());
        return sdf.format(new java.util.Date(recordedAt));
    }
}