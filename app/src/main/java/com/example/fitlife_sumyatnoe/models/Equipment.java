package com.example.fitlife_sumyatnoe.models;

import java.io.Serializable;

public class Equipment implements Serializable {
    private String id;
    private String name;
    private String category; // "strength", "cardio", "yoga", "accessories"
    private String imageUrl;
    private String localImagePath;
    private boolean isCustom;
    private boolean isDefault;

    public Equipment() {
        this.isCustom = false;
        this.isDefault = false;
        this.category = "accessories";
    }

    public Equipment(String name, String category) {
        this();
        this.name = name;
        this.category = category;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getLocalImagePath() { return localImagePath; }
    public void setLocalImagePath(String localImagePath) { this.localImagePath = localImagePath; }

    public boolean isCustom() { return isCustom; }
    public void setCustom(boolean custom) { isCustom = custom; }

    public boolean isDefault() { return isDefault; }
    public void setDefault(boolean aDefault) { isDefault = aDefault; }
}