package com.example.fitlife_sumyatnoe.repository;

import com.example.fitlife_sumyatnoe.models.Exercise;
import com.example.fitlife_sumyatnoe.models.Workout;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

public class WorkoutRepositoryLogicTest {

    private List<Workout> mockWorkouts;

    @Before
    public void setUp() {
        mockWorkouts = new ArrayList<>();
        Workout workout1 = new Workout("Strength Training", "Build muscle", 45);
        workout1.setId("1");
        workout1.setAdded(true);
        mockWorkouts.add(workout1);
        Workout workout2 = new Workout("Cardio Blast", "HIIT workout", 30);
        workout2.setId("2");
        workout2.setAdded(false);
        mockWorkouts.add(workout2);
    }

    @Test
    public void testFilterAddedWorkouts() {
        List<Workout> addedWorkouts = new ArrayList<>();
        for (Workout w : mockWorkouts) {
            if (w.isAdded()) {
                addedWorkouts.add(w);
            }
        }
        assertEquals(1, addedWorkouts.size());
        assertEquals("Strength Training", addedWorkouts.get(0).getName());
    }

    @Test
    public void testFilterByDuration() {
        List<Workout> longWorkouts = new ArrayList<>();
        for (Workout w : mockWorkouts) {
            if (w.getDuration() > 40) {
                longWorkouts.add(w);
            }
        }
        assertEquals(1, longWorkouts.size());
        assertEquals(45, longWorkouts.get(0).getDuration());
    }

    @Test
    public void testSearchWorkoutByName() {
        String searchTerm = "Strength";
        List<Workout> results = new ArrayList<>();
        for (Workout w : mockWorkouts) {
            if (w.getName().toLowerCase().contains(searchTerm.toLowerCase())) {
                results.add(w);
            }
        }
        assertEquals(1, results.size());
        assertEquals("Strength Training", results.get(0).getName());
    }
}