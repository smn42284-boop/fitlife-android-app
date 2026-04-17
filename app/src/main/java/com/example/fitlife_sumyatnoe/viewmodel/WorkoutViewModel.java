package com.example.fitlife_sumyatnoe.viewmodel;

import android.app.Application;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.fitlife_sumyatnoe.models.UserWorkout;
import com.example.fitlife_sumyatnoe.models.Workout;
import com.example.fitlife_sumyatnoe.repository.WorkoutRepository;
import com.example.fitlife_sumyatnoe.utils.FirebaseHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WorkoutViewModel extends AndroidViewModel {

    private WorkoutRepository repository;
    private FirebaseHelper firebaseHelper;
    private SharedPreferences prefs;

    private MutableLiveData<List<Workout>> workouts = new MutableLiveData<>();
    private MutableLiveData<List<Workout>> filteredWorkouts = new MutableLiveData<>();
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();

    private String userId;
    private boolean isGuest;

    public WorkoutViewModel(@NonNull Application application) {
        super(application);
        repository = WorkoutRepository.getInstance(application);
        firebaseHelper = new FirebaseHelper();
        prefs = application.getSharedPreferences("FitLifePrefs", 0);

        isGuest = prefs.getBoolean("isGuest", false);

        if (!isGuest && firebaseHelper.getCurrentUser() != null) {
            userId = firebaseHelper.getCurrentUser().getUid();
        }
    }

    public LiveData<List<Workout>> getWorkouts() {
        return workouts;
    }

    public LiveData<List<Workout>> getFilteredWorkouts() {
        return filteredWorkouts;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void loadWorkouts() {
        Log.d("WorkoutViewModel", "loadWorkouts called - isGuest: " + isGuest + ", userId: " + userId);
        isLoading.postValue(true);  // ✅ FIXED

        if (isGuest) {
            firebaseHelper.getWorkoutTemplates(new FirebaseHelper.FirestoreCallback<List<Workout>>() {
                @Override
                public void onSuccess(List<Workout> templateWorkouts) {
                    isLoading.postValue(false);
                    if (templateWorkouts != null) {
                        for (Workout w : templateWorkouts) {
                            w.setCustom(false);
                            Log.d("WorkoutViewModel", "Template workout: " + w.getName() + ", isCustom: " + w.isCustom());
                        }
                        workouts.postValue(templateWorkouts);
                        filteredWorkouts.postValue(templateWorkouts);
                    } else {
                        workouts.postValue(new ArrayList<>());
                        filteredWorkouts.postValue(new ArrayList<>());
                    }
                }

                @Override
                public void onFailure(String error) {
                    isLoading.postValue(false);
                    errorMessage.postValue(error);
                    workouts.postValue(new ArrayList<>());
                    filteredWorkouts.postValue(new ArrayList<>());
                }
            });
            return;
        }

        if (userId == null || userId.isEmpty()) {
            isLoading.postValue(false);  // ✅ FIXED
            workouts.postValue(new ArrayList<>());  // ✅ FIXED
            filteredWorkouts.postValue(new ArrayList<>());  // ✅ FIXED
            return;
        }

        repository.loadWorkouts(new WorkoutRepository.DataCallback<List<Workout>>() {
            @Override
            public void onSuccess(List<Workout> data) {
                isLoading.postValue(false);
                if (data != null) {
                    for (Workout w : data) {
                        Log.d("WorkoutViewModel", "Workout: " + w.getName() +
                                ", isCustom: " + w.isCustom() +
                                ", isAdded: " + w.isAdded());
                    }
                    workouts.postValue(data);
                    filteredWorkouts.postValue(data);
                }
            }

            @Override
            public void onFailure(String error) {
                isLoading.postValue(false);  // ✅ FIXED
                errorMessage.postValue(error);  // ✅ FIXED
                workouts.postValue(new ArrayList<>());  // ✅ FIXED
                filteredWorkouts.postValue(new ArrayList<>());  // ✅ FIXED
            }
        });
    }

    public void filterWorkouts(String query) {
        List<Workout> currentWorkouts = workouts.getValue();
        if (currentWorkouts == null) {
            filteredWorkouts.postValue(new ArrayList<>());  // ✅ FIXED
            return;
        }

        if (query == null || query.trim().isEmpty()) {
            filteredWorkouts.postValue(currentWorkouts);  // ✅ FIXED
            return;
        }

        String lowerQuery = query.toLowerCase().trim();
        List<Workout> filtered = new ArrayList<>();
        for (Workout workout : currentWorkouts) {
            if (workout.getName().toLowerCase().contains(lowerQuery) ||
                    (workout.getDescription() != null && workout.getDescription().toLowerCase().contains(lowerQuery))) {
                filtered.add(workout);
            }
        }
        filteredWorkouts.postValue(filtered);  // ✅ FIXED
    }

    public void addWorkoutToPlan(Workout workout, List<String> days, WorkoutRepository.DataCallback<Void> callback) {
        workout.setDaysOfWeek(days);
        workout.setAdded(true);
        repository.addWorkoutToPlan(workout, callback);
    }

    public void removeWorkoutFromPlan(Workout workout, WorkoutRepository.DataCallback<Void> callback) {
        repository.removeWorkoutFromPlan(workout, callback);
    }
    public void addWorkoutToList(Workout workout) {
        List<Workout> current = workouts.getValue();
        if (current != null) {
            List<Workout> newList = new ArrayList<>(current);
            newList.add(0, workout);
            workouts.postValue(newList);
            filteredWorkouts.postValue(newList);
        }
    }
    public void deleteCustomWorkout(Workout workout, FirebaseHelper.FirestoreCallback<Void> callback) {
        if (workout.isCustom()) {
            firebaseHelper.deleteCustomWorkout(workout.getId(), callback);
        }
    }
    public void forceRefresh() {
        repository.forceRefreshCache(new WorkoutRepository.DataCallback<List<Workout>>() {
            @Override
            public void onSuccess(List<Workout> data) {
                if (data != null) {
                    workouts.postValue(data);
                    filteredWorkouts.postValue(data);
                }
            }

            @Override
            public void onFailure(String error) {
                errorMessage.postValue(error);
            }
        });
    }
    public void refresh() {
        workouts.postValue(null);
        filteredWorkouts.postValue(null);
        loadWorkouts();
    }}