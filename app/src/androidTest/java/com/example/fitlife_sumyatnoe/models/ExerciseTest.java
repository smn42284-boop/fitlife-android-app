package com.example.fitlife_sumyatnoe.models;

import org.junit.Before;
import org.junit.Test;
import org.junit.Assert;

public class ExerciseTest {

    private Exercise exercise;

    @Before
    public void setUp() {
        exercise = new Exercise("Push-ups", 3, 10);
    }

    @Test
    public void testExerciseCreation() {
        Assert.assertEquals("Push-ups", exercise.getName());
        Assert.assertEquals(3, exercise.getSets());
        Assert.assertEquals(10, exercise.getReps());
        Assert.assertFalse(exercise.isTimed());
    }

    @Test
    public void testTimedExerciseCreation() {
        Exercise timedExercise = new Exercise("Plank", 3, 60, true);
        Assert.assertTrue(timedExercise.isTimed());
        Assert.assertEquals(60, timedExercise.getDuration());
    }

    @Test
    public void testDisplayText() {
        Assert.assertEquals("3 sets × 10 reps", exercise.getDisplayText());
        Exercise timedExercise = new Exercise("Plank", 3, 60, true);
        Assert.assertEquals("3 sets × 60 sec", timedExercise.getDisplayText());
    }

    @Test
    public void testExerciseCompletion() {
        Assert.assertFalse(exercise.isCompleted());
        exercise.setCompleted(true);
        Assert.assertTrue(exercise.isCompleted());
        Assert.assertTrue(exercise.getCompletedAt() > 0);
    }

    @Test
    public void testInstructions() {
        String instructions = "Keep your back straight and lower chest to ground";
        exercise.setInstructions(instructions);
        Assert.assertEquals(instructions, exercise.getInstructions());
    }
}