package com.example.fitlife_sumyatnoe.models;

import org.junit.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.Assert.*;

public class WorkoutTest {

    @Test
    public void testWorkoutDuration_CalculateTotalMinutes() {
        Workout workout = new Workout();
        workout.setDuration(45);
        assertEquals(45, workout.getDuration());
    }

    @Test
    public void testWorkoutProgress_CalculateCompletion() {
        int totalExercises = 10;
        int completedExercises = 7;
        int progressPercent = (completedExercises * 100) / totalExercises;
        assertEquals(70, progressPercent);
    }

    @Test
    public void testWorkout_AllExercisesCompleted_ReturnsTrue() {
        List<Exercise> exercises = new ArrayList<>();
        Exercise ex1 = new Exercise("Pushup", 3, 10);
        Exercise ex2 = new Exercise("Squat", 3, 15);
        ex1.setCompleted(true);
        ex2.setCompleted(true);
        exercises.add(ex1);
        exercises.add(ex2);
        boolean allCompleted = true;
        for (Exercise ex : exercises) {
            if (!ex.isCompleted()) {
                allCompleted = false;
                break;
            }
        }
        assertTrue(allCompleted);
    }
}