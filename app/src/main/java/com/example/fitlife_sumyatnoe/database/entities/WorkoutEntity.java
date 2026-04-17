package com.example.fitlife_sumyatnoe.database.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;
import com.example.fitlife_sumyatnoe.database.converters.Converters;
import java.util.List;

@Entity(tableName = "workouts")
public class WorkoutEntity {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @ColumnInfo(name = "firestoreId")
    private String firestoreId;

    @ColumnInfo(name = "name")
    private String name;
    @ColumnInfo(name = "isCustom")
    private boolean isCustom;

    @ColumnInfo(name = "description")
    private String description;

    @ColumnInfo(name = "duration")
    private int duration;

    @ColumnInfo(name = "imageUrl")
    private String imageUrl;

    @ColumnInfo(name = "localImagePath")
    private String localImagePath;

    @ColumnInfo(name = "isDefault")
    private boolean isDefault;



    @ColumnInfo(name = "isAdded")
    private boolean isAdded;

    @ColumnInfo(name = "isCompleted")
    private boolean isCompleted;

    @ColumnInfo(name = "completedDate")
    private long completedDate;

    @ColumnInfo(name = "lastUpdated")
    private long lastUpdated;

    @TypeConverters(Converters.class)
    @ColumnInfo(name = "daysOfWeek")
    private List<String> daysOfWeek;

    @TypeConverters(Converters.class)
    @ColumnInfo(name = "equipment")
    private List<String> equipment;

    // Default constructor
    public WorkoutEntity() {
        this.completedDate = 0;
        this.lastUpdated = System.currentTimeMillis();
    }

    // Getters and setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getFirestoreId() {
        return firestoreId;
    }

    public void setFirestoreId(String firestoreId) {
        this.firestoreId = firestoreId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getLocalImagePath() {
        return localImagePath;
    }

    public void setLocalImagePath(String localImagePath) {
        this.localImagePath = localImagePath;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean aDefault) {
        isDefault = aDefault;
    }

    public boolean isCustom() {
        return isCustom;
    }

    public void setCustom(boolean custom) {
        isCustom = custom;
    }

    public boolean isAdded() {
        return isAdded;
    }

    public void setAdded(boolean added) {
        isAdded = added;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    public long getCompletedDate() {
        return completedDate;
    }

    public void setCompletedDate(long completedDate) {
        this.completedDate = completedDate;
    }

    public long getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public List<String> getDaysOfWeek() {
        return daysOfWeek;
    }

    public void setDaysOfWeek(List<String> daysOfWeek) {
        this.daysOfWeek = daysOfWeek;
    }

    public List<String> getEquipment() {
        return equipment;
    }

    public void setEquipment(List<String> equipment) {
        this.equipment = equipment;
    }
}