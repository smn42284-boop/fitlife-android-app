package com.example.fitlife_sumyatnoe.utils;

import com.example.fitlife_sumyatnoe.models.Workout;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class WorkflowManager {

    // Get today's day name
    public static String getTodayDayName() {
        String[] days = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
        Calendar calendar = Calendar.getInstance();
        return days[calendar.get(Calendar.DAY_OF_WEEK) - 1];
    }

    public static List<Workout> getTodayWorkouts(List<Workout> allWorkouts) {
        List<Workout> todayWorkouts = new ArrayList<>();
        String today = getTodayDayName();

        for (Workout workout : allWorkouts) {
            if (workout.isAdded() && workout.getDaysOfWeek() != null
                    && workout.getDaysOfWeek().contains(today) && !workout.isCompleted()) {
                todayWorkouts.add(workout);
            }
        }
        return todayWorkouts;
    }

    // Check if all today's workouts are completed
    public static boolean areAllTodayWorkoutsCompleted(List<Workout> allWorkouts) {
        List<Workout> todayWorkouts = new ArrayList<>();
        String today = getTodayDayName();
        int totalToday = 0;
        int completedToday = 0;

        for (Workout workout : allWorkouts) {
            if (workout.isAdded() && workout.getDaysOfWeek() != null
                    && workout.getDaysOfWeek().contains(today)) {
                totalToday++;
                if (workout.isCompleted()) completedToday++;
            }
        }

        return totalToday > 0 && totalToday == completedToday;
    }

    // Get workouts for a specific day
    public static List<Workout> getWorkoutsForDay(List<Workout> allWorkouts, String day) {
        List<Workout> dayWorkouts = new ArrayList<>();
        for (Workout workout : allWorkouts) {
            if (workout.isAdded() && workout.getDaysOfWeek() != null
                    && workout.getDaysOfWeek().contains(day)) {
                dayWorkouts.add(workout);
            }
        }
        return dayWorkouts;
    }

    // Get completed workouts for a specific day
    public static List<Workout> getCompletedWorkoutsForDay(List<Workout> allWorkouts, String day) {
        List<Workout> completedWorkouts = new ArrayList<>();
        for (Workout workout : allWorkouts) {
            if (workout.isAdded() && workout.getDaysOfWeek() != null
                    && workout.getDaysOfWeek().contains(day) && workout.isCompleted()) {
                completedWorkouts.add(workout);
            }
        }
        return completedWorkouts;
    }

    // Get incomplete workouts for a specific day
    public static List<Workout> getIncompleteWorkoutsForDay(List<Workout> allWorkouts, String day) {
        List<Workout> incompleteWorkouts = new ArrayList<>();
        for (Workout workout : allWorkouts) {
            if (workout.isAdded() && workout.getDaysOfWeek() != null
                    && workout.getDaysOfWeek().contains(day) && !workout.isCompleted()) {
                incompleteWorkouts.add(workout);
            }
        }
        return incompleteWorkouts;
    }
}