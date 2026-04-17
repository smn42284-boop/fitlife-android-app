package com.example.fitlife_sumyatnoe.models;

import java.io.Serializable;

public class EquipmentItem implements Serializable {
    private String name;
    private String category;
    private String subType;
    private boolean isSelected;

    public EquipmentItem(String name, String category) {
        this.name = name;
        this.category = category;
        this.subType = getDefaultSubType(category);
        this.isSelected = false;
    }

    public EquipmentItem(String name, String category, String subType) {
        this.name = name;
        this.category = category;
        this.subType = subType;
        this.isSelected = false;
    }

    private String getDefaultSubType(String category) {
        switch (category) {
            case "Strength":
                return "Free Weights";
            case "Cardio":
                return "Machines";
            case "Yoga":
                return "Accessories";
            case "Accessories":
                return "Misc";
            default:
                return "Other";
        }
    }


    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }

    public boolean isSelected() { return isSelected; }
    public void setSelected(boolean selected) { isSelected = selected; }
}