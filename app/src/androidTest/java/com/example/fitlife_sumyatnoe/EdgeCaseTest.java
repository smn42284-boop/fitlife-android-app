package com.example.fitlife_sumyatnoe;

import org.junit.Test;
import static org.junit.Assert.*;

import com.example.fitlife_sumyatnoe.models.Exercise;
import com.example.fitlife_sumyatnoe.models.Workout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EdgeCaseTest {

    @Test
    public void testEmptyWorkoutList() {
        List<Workout> emptyList = new ArrayList<>();
        assertTrue(emptyList.isEmpty());
        assertEquals(0, emptyList.size());
    }

    @Test
    public void testNullWorkoutHandling() {
        Workout nullWorkout = null;
        assertNull(nullWorkout);
    }

    @Test
    public void testMaximumDaysSelection() {
        List<String> allDays = Arrays.asList(
                "Monday", "Tuesday", "Wednesday", "Thursday",
                "Friday", "Saturday", "Sunday"
        );
        assertEquals(7, allDays.size());
    }

    @Test
    public void testZeroExercisesWorkout() {
        Workout workout = new Workout("No Exercises", "Description", 30);
        workout.setExercises(new ArrayList<>());
        assertEquals(0, workout.getExercisesCount());
    }

    @Test
    public void testMaximumExerciseCount() {
        List<Exercise> manyExercises = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            manyExercises.add(new Exercise("Exercise " + i, 3, 10));
        }
        Workout workout = new Workout("Many Exercises", "Description", 60);
        workout.setExercises(manyExercises);
        assertEquals(20, workout.getExercisesCount());
    }

    @Test
    public void testSpecialCharactersInWorkoutName() {
        String specialName = "Workout! @#$%^&*()_+";
        Workout workout = new Workout(specialName, "Description", 30);
        assertEquals(specialName, workout.getName());
    }

    @Test
    public void testVeryLongDescription() {
        String longDescription = "A".repeat(1000);
        Workout workout = new Workout("Test", longDescription, 30);
        assertEquals(1000, workout.getDescription().length());
    }
}