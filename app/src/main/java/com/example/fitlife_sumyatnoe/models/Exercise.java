package com.example.fitlife_sumyatnoe.models;

import java.io.Serializable;

public class Exercise implements Serializable {
    private String id;
    private String name;
    private int sets;
    private int reps;
    private int duration;
    private boolean isTimed;
    private String instructions;
    private String imageUrl;
    private String localImagePath;
    private String category;
    private boolean isCompleted;
    private long completedAt;

    // Default constructor (required for Firestore)
    public Exercise() {}

    // Constructor for sets/reps exercise
    public Exercise(String name, int sets, int reps) {
        this.name = name;
        this.sets = sets;
        this.reps = reps;
        this.isTimed = false;
        this.isCompleted = false;
    }

    // Constructor for timed exercise
    public Exercise(String name, int sets, int duration, boolean isTimed) {
        this.name = name;
        this.sets = sets;
        this.duration = duration;
        this.isTimed = isTimed;
        this.isCompleted = false;
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getSets() { return sets; }
    public void setSets(int sets) { this.sets = sets; }

    public int getReps() { return reps; }
    public void setReps(int reps) { this.reps = reps; }

    public int getDuration() { return duration; }
    public void setDuration(int duration) { this.duration = duration; }

    public boolean isTimed() { return isTimed; }
    public void setTimed(boolean timed) { isTimed = timed; }

    public String getInstructions() { return instructions; }
    public void setInstructions(String instructions) { this.instructions = instructions; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getLocalImagePath() { return localImagePath; }
    public void setLocalImagePath(String localImagePath) { this.localImagePath = localImagePath; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public boolean isCompleted() { return isCompleted; }
    public void setCompleted(boolean completed) {
        this.isCompleted = completed;
        if (completed) {
            this.completedAt = System.currentTimeMillis();
        }
    }

    public long getCompletedAt() { return completedAt; }
    public void setCompletedAt(long completedAt) { this.completedAt = completedAt; }

    public String getDisplayText() {
        if (isTimed) {
            return sets + " sets × " + duration + " sec";
        } else {
            return sets + " sets × " + reps + " reps";
        }
    }
}