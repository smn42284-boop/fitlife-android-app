package com.example.fitlife_sumyatnoe.models;

import org.junit.Before;
import org.junit.Test;
import org.junit.Assert;

import java.util.HashMap;
import java.util.Map;

public class UserWorkoutTest {

    private UserWorkout userWorkout;

    @Before
    public void setUp() {
        userWorkout = new UserWorkout("user123", "workout456", false);
        userWorkout.setId("uw789");
    }

    @Test
    public void testPerDayCompletion() {
        userWorkout.setCompletedForDay("Monday", true);
        Assert.assertTrue(userWorkout.isCompletedForDay("Monday"));
        Assert.assertFalse(userWorkout.isCompletedForDay("Tuesday"));
        userWorkout.setCompletedForDay("Tuesday", true);
        Assert.assertTrue(userWorkout.isCompletedForDay("Tuesday"));
        userWorkout.setCompletedForDay("Monday", false);
        Assert.assertFalse(userWorkout.isCompletedForDay("Monday"));
    }

    @Test
    public void testExerciseProgressPerDay() {
        Map<String, Boolean> mondayProgress = new HashMap<>();
        mondayProgress.put("Push-ups", true);
        mondayProgress.put("Squats", false);
        userWorkout.setExerciseProgressForDay("Monday", mondayProgress);
        Map<String, Boolean> retrieved = userWorkout.getExerciseProgressForDay("Monday");
        Assert.assertNotNull(retrieved);
        Assert.assertTrue(retrieved.get("Push-ups"));
        Assert.assertFalse(retrieved.get("Squats"));
    }

    @Test
    public void testGetCompletedDays() {
        userWorkout.setCompletedForDay("Monday", true);
        userWorkout.setCompletedForDay("Wednesday", true);
        Assert.assertEquals(2, userWorkout.getCompletedDays().size());
        Assert.assertTrue(userWorkout.getCompletedDays().contains("Monday"));
        Assert.assertTrue(userWorkout.getCompletedDays().contains("Wednesday"));
    }
}