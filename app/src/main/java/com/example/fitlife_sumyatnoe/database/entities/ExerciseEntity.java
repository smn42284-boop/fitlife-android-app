package com.example.fitlife_sumyatnoe.database.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "exercises")
public class ExerciseEntity {

    @PrimaryKey(autoGenerate = true)
    private long id;
    private String workoutId;
    private String name;
    private int sets;
    private int reps;
    private int duration;
    private boolean isTimed;
    private String instructions;
    private String imageUrl;
    private String localImagePath;
    private String category;
    private int position;

    // Getters
    public long getId() { return id; }
    public String getWorkoutId() { return workoutId; }
    public String getName() { return name; }
    public int getSets() { return sets; }
    public int getReps() { return reps; }
    public int getDuration() { return duration; }
    public boolean isTimed() { return isTimed; }
    public String getInstructions() { return instructions; }
    public String getImageUrl() { return imageUrl; }
    public String getLocalImagePath() { return localImagePath; }
    public String getCategory() { return category; }
    public int getPosition() { return position; }

    // Setters
    public void setId(long id) { this.id = id; }
    public void setWorkoutId(String workoutId) { this.workoutId = workoutId; }
    public void setName(String name) { this.name = name; }
    public void setSets(int sets) { this.sets = sets; }
    public void setReps(int reps) { this.reps = reps; }
    public void setDuration(int duration) { this.duration = duration; }
    public void setTimed(boolean timed) { this.isTimed = timed; }
    public void setInstructions(String instructions) { this.instructions = instructions; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setLocalImagePath(String localImagePath) { this.localImagePath = localImagePath; }
    public void setCategory(String category) { this.category = category; }
    public void setPosition(int position) { this.position = position; }
}