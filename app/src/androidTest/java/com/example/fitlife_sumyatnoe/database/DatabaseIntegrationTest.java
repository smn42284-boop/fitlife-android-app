package com.example.fitlife_sumyatnoe.database;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.fitlife_sumyatnoe.database.entities.ExerciseEntity;
import com.example.fitlife_sumyatnoe.database.entities.WorkoutEntity;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class DatabaseIntegrationTest {

    private AppDatabase database;

    @Before
    public void setUp() {
        database = Room.inMemoryDatabaseBuilder(
                ApplicationProvider.getApplicationContext(),
                AppDatabase.class
        ).build();
    }

    @After
    public void tearDown() {
        database.close();
    }

    @Test
    public void testInsertAndRetrieveWorkout() {
        WorkoutEntity workout = new WorkoutEntity();
        workout.setFirestoreId("test123");
        workout.setName("Test Workout");
        workout.setDuration(30);
        workout.setAdded(true);

        database.workoutDao().insert(workout);

        List<WorkoutEntity> workouts = database.workoutDao().getAll();

        assertNotNull(workouts);
        assertEquals(1, workouts.size());
        assertEquals("Test Workout", workouts.get(0).getName());
    }

    @Test
    public void testInsertAndRetrieveExercises() {
        WorkoutEntity workout = new WorkoutEntity();
        workout.setFirestoreId("workout123");
        workout.setName("Workout with Exercises");
        long workoutId = database.workoutDao().insert(workout);

        ExerciseEntity exercise1 = new ExerciseEntity();
        exercise1.setWorkoutId(String.valueOf(workoutId));
        exercise1.setName("Push-ups");
        exercise1.setSets(3);
        exercise1.setReps(10);
        exercise1.setPosition(0);

        ExerciseEntity exercise2 = new ExerciseEntity();
        exercise2.setWorkoutId(String.valueOf(workoutId));
        exercise2.setName("Squats");
        exercise2.setSets(3);
        exercise2.setReps(15);
        exercise2.setPosition(1);

        database.exerciseDao().insertAll(Arrays.asList(exercise1, exercise2));

        List<ExerciseEntity> exercises = database.exerciseDao().getByWorkoutId(String.valueOf(workoutId));

        assertNotNull(exercises);
        assertEquals(2, exercises.size());
        assertEquals("Push-ups", exercises.get(0).getName());
        assertEquals("Squats", exercises.get(1).getName());
    }

    @Test
    public void testUpdateWorkout() {
        WorkoutEntity workout = new WorkoutEntity();
        workout.setFirestoreId("update123");
        workout.setName("Original Name");
        database.workoutDao().insert(workout);

        List<WorkoutEntity> workouts = database.workoutDao().getAll();
        WorkoutEntity inserted = workouts.get(0);

        inserted.setName("Updated Name");
        database.workoutDao().update(inserted);

        WorkoutEntity updated = database.workoutDao().getById(String.valueOf(inserted.getId()));
        assertEquals("Updated Name", updated.getName());
    }

    @Test
    public void testDeleteWorkout() {
        WorkoutEntity workout = new WorkoutEntity();
        workout.setFirestoreId("delete123");
        workout.setName("To Delete");
        database.workoutDao().insert(workout);

        List<WorkoutEntity> before = database.workoutDao().getAll();
        assertEquals(1, before.size());

        database.workoutDao().deleteAll();

        List<WorkoutEntity> after = database.workoutDao().getAll();
        assertEquals(0, after.size());
    }
}