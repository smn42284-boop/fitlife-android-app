package com.example.fitlife_sumyatnoe.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Workout implements Serializable {
    private String id;
    private String name;
    private String description;
    private int duration;
    private List<Exercise> exercises;
    private List<String> equipment;
    private String userId;
    private String imageUrl;
    private String localImagePath;
    private boolean isDefault;
    private boolean isCustom;
    private long createdAt;
    private boolean needsSync = false;

    private long lastModified;

    // Runtime fields
    private boolean isAdded;
    private boolean isCompleted;
    private long completedDate;
    private List<String> daysOfWeek;
    private int currentProgress;
    private Map<String, Boolean> dayCompletion = new HashMap<>();
    private Map<String, Boolean> exerciseProgress;

    public Workout() {
        this.exercises = new ArrayList<>();
        this.equipment = new ArrayList<>();
        this.daysOfWeek = new ArrayList<>();
        this.exerciseProgress = new HashMap<>();
        this.dayCompletion = new HashMap<>();
        this.isDefault = false;
        this.isCustom = false;
        this.isAdded = false;
        this.isCompleted = false;
        this.currentProgress = 0;
    }

    public Workout(String name, String description, int duration) {
        this();
        this.name = name;
        this.description = description;
        this.duration = duration;
    }


    public void setDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }

    public boolean isNeedsSync() { return needsSync; }
    public void setNeedsSync(boolean needsSync) { this.needsSync = needsSync; }
    public boolean isCompletedForDay(String day) {
        return dayCompletion != null && dayCompletion.getOrDefault(day, false);
    }

    public void setCompletedForDay(String day, boolean completed) {
        if (dayCompletion == null) {
            dayCompletion = new HashMap<>();
        }
        dayCompletion.put(day, completed);
    }

    public Map<String, Boolean> getDayCompletion() {
        return dayCompletion != null ? dayCompletion : new HashMap<>();
    }

    public void setDayCompletion(Map<String, Boolean> dayCompletion) {
        this.dayCompletion = dayCompletion != null ? dayCompletion : new HashMap<>();
    }

    // ========== EXERCISE METHODS ==========

    public boolean hasExercises() {
        return exercises != null && !exercises.isEmpty();
    }

    public int getExercisesCount() {
        return exercises != null ? exercises.size() : 0;
    }

    public List<Exercise> getExercises() {
        if (exercises == null) {
            exercises = new ArrayList<>();
        }
        return exercises;
    }

    public void setExercises(List<Exercise> exercises) {
        if (exercises == null) {
            this.exercises = new ArrayList<>();
        } else {
            this.exercises = exercises;
        }
    }

    public void addExercise(Exercise exercise) {
        if (exercises == null) {
            exercises = new ArrayList<>();
        }
        exercises.add(exercise);
    }

    public String getExercisesPreview() {
        if (exercises == null || exercises.isEmpty()) {
            return "0 exercises";
        }
        int count = exercises.size();
        if (count == 1) {
            return exercises.get(0).getName();
        }
        return count + " exercises";
    }

    public String getExercisesDetailedPreview() {
        if (exercises == null || exercises.isEmpty()) {
            return "No exercises";
        }
        StringBuilder preview = new StringBuilder();
        int max = Math.min(exercises.size(), 3);
        for (int i = 0; i < max; i++) {
            if (i > 0) preview.append(" • ");
            preview.append(exercises.get(i).getName());
        }
        if (exercises.size() > 3) {
            preview.append(" +").append(exercises.size() - 3).append(" more");
        }
        return preview.toString();
    }

    // ========== EQUIPMENT METHODS ==========

    public String getEquipmentPreview() {
        if (equipment == null || equipment.isEmpty()) {
            return "No equipment needed";
        }
        StringBuilder preview = new StringBuilder();
        int max = Math.min(equipment.size(), 2);
        for (int i = 0; i < max; i++) {
            if (i > 0) preview.append(" • ");
            preview.append(equipment.get(i));
        }
        if (equipment.size() > 2) {
            preview.append(" +").append(equipment.size() - 2).append(" more");
        }
        return preview.toString();
    }

    // ========== PROGRESS METHODS ==========

    public void saveProgress(List<Exercise> currentExercises) {
        if (exerciseProgress == null) {
            exerciseProgress = new HashMap<>();
        }
        exerciseProgress.clear();
        int completed = 0;
        for (Exercise exercise : currentExercises) {
            exerciseProgress.put(exercise.getName(), exercise.isCompleted());
            if (exercise.isCompleted()) completed++;
        }
        if (currentExercises.size() > 0) {
            currentProgress = (completed * 100) / currentExercises.size();
        }
    }

    public void loadProgress(List<Exercise> exercisesToLoad) {
        if (exerciseProgress != null && !exerciseProgress.isEmpty()) {
            for (Exercise exercise : exercisesToLoad) {
                Boolean completed = exerciseProgress.get(exercise.getName());
                if (completed != null) {
                    exercise.setCompleted(completed);
                } else {
                    exercise.setCompleted(false);
                }
            }
        } else {
            for (Exercise exercise : exercisesToLoad) {
                exercise.setCompleted(false);
            }
        }
    }

    // ========== GETTERS AND SETTERS ==========

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getDuration() { return duration; }
    public void setDuration(int duration) { this.duration = duration; }

    public List<String> getEquipment() {
        return equipment != null ? equipment : new ArrayList<>();
    }
    public void setEquipment(List<String> equipment) {
        this.equipment = equipment != null ? equipment : new ArrayList<>();
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getLocalImagePath() { return localImagePath; }
    public void setLocalImagePath(String localImagePath) { this.localImagePath = localImagePath; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public long getLastModified() { return lastModified; }
    public void setLastModified(long lastModified) { this.lastModified = lastModified; }

    public boolean isAdded() { return isAdded; }
    public void setAdded(boolean added) { this.isAdded = added; }
    public boolean getIsAdded() {
        return isAdded;
    }
    public boolean getIsCompleted() {
        return isCompleted;
    }

    public boolean getIsCustom() {
        return isCustom;
    }

    public boolean getIsDefault() {
        return isDefault;
    }
    public boolean isCompleted() { return isCompleted; }
    public void setCompleted(boolean completed) {
        this.isCompleted = completed;
        if (completed) {
            completedDate = System.currentTimeMillis();
        }
    }

    public boolean isCustom() { return isCustom; }
    public void setCustom(boolean custom) { this.isCustom = custom; }

    public boolean isDefault() { return isDefault; }
    public void setDefaultWorkout(boolean isDefault) {
        this.isDefault = isDefault;
    }

    public long getCompletedDate() { return completedDate; }
    public void setCompletedDate(long completedDate) { this.completedDate = completedDate; }

    public List<String> getDaysOfWeek() {
        return daysOfWeek != null ? daysOfWeek : new ArrayList<>();
    }
    public void setDaysOfWeek(List<String> daysOfWeek) {
        this.daysOfWeek = daysOfWeek != null ? daysOfWeek : new ArrayList<>();
    }

    public int getCurrentProgress() { return currentProgress; }
    public void setCurrentProgress(int currentProgress) { this.currentProgress = currentProgress; }

    public Map<String, Boolean> getExerciseProgress() {
        return exerciseProgress != null ? exerciseProgress : new HashMap<>();
    }
    public void setExerciseProgress(Map<String, Boolean> exerciseProgress) {
        this.exerciseProgress = exerciseProgress != null ? exerciseProgress : new HashMap<>();
    }

    @Override
    public String toString() {
        return "Workout{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", isAdded=" + isAdded +
                ", daysOfWeek=" + daysOfWeek +
                '}';
    }
}