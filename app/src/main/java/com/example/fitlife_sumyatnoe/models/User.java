package com.example.fitlife_sumyatnoe.models;

import java.io.Serializable;

public class User implements Serializable {
    private String id;
    private String name;
    private String email;
    private String theme;

    private String fontSize;
    private String profileImagePath;
    private String profileImageUrl;
    private long createdAt;

    // 2FA Fields
    private boolean twoFactorEnabled;
    private String twoFactorMethod; // "sms", "email", or "authenticator"
    private String phoneNumber;
    private boolean phoneVerified;
    private String secretKey; // For authenticator app

    public User() {
        this.theme = "light";
        this.fontSize = "Medium";
        this.twoFactorEnabled = false;
        this.twoFactorMethod = "";
        this.phoneVerified = false;
    }

    public User(String name, String email) {
        this();
        this.name = name;
        this.email = email;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return id; }
    public void setUserId(String userId) { this.id = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTheme() { return theme; }
    public void setTheme(String theme) { this.theme = theme; }

    public String getFontSize() { return fontSize; }
    public void setFontSize(String fontSize) { this.fontSize = fontSize; }

    public String getProfileImagePath() { return profileImagePath; }
    public void setProfileImagePath(String profileImagePath) { this.profileImagePath = profileImagePath; }

    public String getProfileImageUrl() { return profileImageUrl; }
    public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }

    public void setPhotoUrl(String photoUrl) { this.profileImageUrl = photoUrl; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    // 2FA Getters and Setters
    public boolean isTwoFactorEnabled() { return twoFactorEnabled; }
    public void setTwoFactorEnabled(boolean twoFactorEnabled) { this.twoFactorEnabled = twoFactorEnabled; }

    public String getTwoFactorMethod() { return twoFactorMethod; }
    public void setTwoFactorMethod(String twoFactorMethod) { this.twoFactorMethod = twoFactorMethod; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public void setPhone(String phone) { this.phoneNumber = phone; }

    public boolean isPhoneVerified() { return phoneVerified; }
    public void setPhoneVerified(boolean phoneVerified) { this.phoneVerified = phoneVerified; }

    public String getSecretKey() { return secretKey; }
    public void setSecretKey(String secretKey) { this.secretKey = secretKey; }
}