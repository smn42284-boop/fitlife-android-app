package com.example.fitlife_sumyatnoe.models;

import android.text.TextUtils;

import com.example.fitlife_sumyatnoe.utils.FirebaseHelper;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserWorkout implements Serializable {
    private String id;
    private String userId;
    private String workoutId;
    private boolean isCustom;
    private boolean isAdded;
    private List<String> daysOfWeek;

    private Map<String, Boolean> completedByDay;
    private Map<String, Long> completedDateByDay;
    private Map<String, Map<String, Boolean>> exerciseProgressByDay;
    private int currentProgress;
    private long lastUpdated;

    private boolean isCompleted;
    private long completedDate;
    private Map<String, Boolean> exerciseProgress;


    public UserWorkout() {
        this.completedByDay = new HashMap<>();
        this.completedDateByDay = new HashMap<>();
        this.exerciseProgressByDay = new HashMap<>();
        this.exerciseProgress = new HashMap<>();
        this.daysOfWeek = new ArrayList<>();
        this.currentProgress = 0;
        this.lastUpdated = System.currentTimeMillis();
        this.isAdded = false;
        this.isCompleted = false;
        this.isCustom = false;
    }

    public UserWorkout(String userId, String workoutId, boolean isCustom) {
        this();
        this.userId = userId;
        this.workoutId = workoutId;
        this.isCustom = isCustom;
    }

    public boolean getIsAdded() {
        return isAdded;
    }
    public boolean getIsCustom() {
        return isCustom;
    }
    public boolean getIsCompleted() {
        return isCompleted;
    }
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getWorkoutId() { return workoutId; }
    public void setWorkoutId(String workoutId) { this.workoutId = workoutId; }

    // Boolean fields - ONLY isXxx() methods
    public boolean isCustom() { return isCustom; }
    public void setCustom(boolean custom) { isCustom = custom; }

    public boolean isAdded() { return isAdded; }
    public void setAdded(boolean added) { this.isAdded = added; }

    public boolean isCompleted() { return isCompleted; }
    public void setCompleted(boolean completed) { this.isCompleted = completed; }

    public List<String> getDaysOfWeek() { return daysOfWeek != null ? daysOfWeek : new ArrayList<>(); }
    public void setDaysOfWeek(List<String> daysOfWeek) { this.daysOfWeek = daysOfWeek; }

    public Map<String, Boolean> getCompletedByDay() { return completedByDay != null ? completedByDay : new HashMap<>(); }
    public void setCompletedByDay(Map<String, Boolean> completedByDay) { this.completedByDay = completedByDay; }

    public Map<String, Long> getCompletedDateByDay() { return completedDateByDay != null ? completedDateByDay : new HashMap<>(); }
    public void setCompletedDateByDay(Map<String, Long> completedDateByDay) { this.completedDateByDay = completedDateByDay; }

    public Map<String, Map<String, Boolean>> getExerciseProgressByDay() {
        return exerciseProgressByDay != null ? exerciseProgressByDay : new HashMap<>();
    }
    public void setExerciseProgressByDay(Map<String, Map<String, Boolean>> exerciseProgressByDay) {
        this.exerciseProgressByDay = exerciseProgressByDay;
    }

    public int getCurrentProgress() { return currentProgress; }
    public void setCurrentProgress(int currentProgress) { this.currentProgress = currentProgress; }

    public long getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(long lastUpdated) { this.lastUpdated = lastUpdated; }

    public long getCompletedDate() { return completedDate; }
    public void setCompletedDate(long completedDate) { this.completedDate = completedDate; }

    public Map<String, Boolean> getExerciseProgress() { return exerciseProgress != null ? exerciseProgress : new HashMap<>(); }
    public void setExerciseProgress(Map<String, Boolean> exerciseProgress) { this.exerciseProgress = exerciseProgress; }

    // ========== PER-DAY COMPLETION METHODS ==========

    // In UserWorkout.java
    public boolean isCompletedForDay(String day) {
        if (completedByDay == null) return false;
        return completedByDay.getOrDefault(day, false);
    }

    public void setCompletedForDay(String day, boolean completed) {
        if (completedByDay == null) {
            completedByDay = new HashMap<>();
            completedDateByDay = new HashMap<>();
        }
        completedByDay.put(day, completed);
        if (completed) {
            completedDateByDay.put(day, System.currentTimeMillis());
        } else {
            completedDateByDay.remove(day);
        }
    }


    public long getCompletedDateForDay(String day) {
        if (completedDateByDay == null) return 0;
        return completedDateByDay.getOrDefault(day, 0L);
    }

    public List<String> getCompletedDays() {
        if (completedByDay == null) return new ArrayList<>();
        return new ArrayList<>(completedByDay.keySet());
    }

    public Map<String, Boolean> getExerciseProgressForDay(String day) {
        if (exerciseProgressByDay == null) return null;
        return exerciseProgressByDay.get(day);
    }

    public void setExerciseProgressForDay(String day, Map<String, Boolean> progress) {
        if (exerciseProgressByDay == null) {
            exerciseProgressByDay = new HashMap<>();
        }
        exerciseProgressByDay.put(day, progress);
    }

    public void addDay(String day) {
        if (daysOfWeek == null) {
            daysOfWeek = new ArrayList<>();
        }
        if (!daysOfWeek.contains(day)) {
            daysOfWeek.add(day);
            if (completedByDay != null) {
                completedByDay.put(day, false);
            }
        }
    }

    public void removeDay(String day) {
        if (daysOfWeek != null) {
            daysOfWeek.remove(day);
            if (completedByDay != null) {
                completedByDay.remove(day);
            }
            if (completedDateByDay != null) {
                completedDateByDay.remove(day);
            }
            if (exerciseProgressByDay != null) {
                exerciseProgressByDay.remove(day);
            }
        }
    }

    public boolean isScheduledForDay(String day) {
        return daysOfWeek != null && daysOfWeek.contains(day);
    }

    private void updateLegacyCompletedFlag() {
        if (daysOfWeek == null || daysOfWeek.isEmpty()) {
            this.isCompleted = false;
            return;
        }

        boolean allCompleted = true;
        for (String day : daysOfWeek) {
            if (!isCompletedForDay(day)) {
                allCompleted = false;
                break;
            }
        }
        this.isCompleted = allCompleted;
    }

    public int getCompletedDaysCount() {
        if (completedByDay == null) return 0;
        int count = 0;
        for (Boolean completed : completedByDay.values()) {
            if (completed) count++;
        }
        return count;
    }

    public int getCompletionPercentage() {
        if (daysOfWeek == null || daysOfWeek.isEmpty()) return 0;
        return (getCompletedDaysCount() * 100) / daysOfWeek.size();
    }

    public void resetDayCompletion(String day) {
        setCompletedForDay(day, false);
        if (exerciseProgressByDay != null) {
            exerciseProgressByDay.remove(day);
        }
    }

    public void resetAllCompletion() {
        if (completedByDay != null) {
            completedByDay.clear();
        }
        if (completedDateByDay != null) {
            completedDateByDay.clear();
        }
        if (exerciseProgressByDay != null) {
            exerciseProgressByDay.clear();
        }
        this.isCompleted = false;
        this.currentProgress = 0;
    }

    @Override
    public String toString() {
        return "UserWorkout{" +
                "id='" + id + '\'' +
                ", workoutId='" + workoutId + '\'' +
                ", isAdded=" + isAdded +
                ", daysOfWeek=" + daysOfWeek +
                '}';
    }
}