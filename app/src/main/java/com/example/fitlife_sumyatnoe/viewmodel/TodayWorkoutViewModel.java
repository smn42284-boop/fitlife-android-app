package com.example.fitlife_sumyatnoe.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.fitlife_sumyatnoe.models.UserWorkout;
import com.example.fitlife_sumyatnoe.models.Workout;
import com.example.fitlife_sumyatnoe.repository.WorkoutRepository;
import com.example.fitlife_sumyatnoe.utils.FirebaseHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TodayWorkoutViewModel extends AndroidViewModel {

    private WorkoutRepository repository;
    private FirebaseHelper firebaseHelper;

    // LiveData for UI to observe
    private MutableLiveData<List<Workout>> todayWorkouts = new MutableLiveData<>();
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();

    private String userId;
    private String today;
    private List<Workout> allWorkouts = new ArrayList<>();

    public TodayWorkoutViewModel(@NonNull Application application) {
        super(application);
        repository = WorkoutRepository.getInstance(application);
        firebaseHelper = new FirebaseHelper();

        SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", Locale.getDefault());
        today = dayFormat.format(new Date());

        if (firebaseHelper.getCurrentUser() != null) {
            userId = firebaseHelper.getCurrentUser().getUid();
        }
    }

    public LiveData<List<Workout>> getTodayWorkouts() {
        return todayWorkouts;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void loadTodayWorkouts() {
        if (userId == null || userId.isEmpty()) {
            todayWorkouts.postValue(new ArrayList<>());
            return;
        }

        isLoading.setValue(true);
        firebaseHelper.getUserAddedWorkouts(userId, new FirebaseHelper.FirestoreCallback<List<UserWorkout>>() {
            @Override
            public void onSuccess(List<UserWorkout> userWorkouts) {
                if (userWorkouts == null || userWorkouts.isEmpty()) {
                    isLoading.postValue(false);
                    todayWorkouts.postValue(new ArrayList<>());
                    return;
                }

                Map<String, UserWorkout> userWorkoutMap = new HashMap<>();
                List<String> workoutIdsForToday = new ArrayList<>();

                for (UserWorkout uw : userWorkouts) {
                    userWorkoutMap.put(uw.getWorkoutId(), uw);
                    if (uw.getDaysOfWeek() != null && uw.getDaysOfWeek().contains(today)) {
                        workoutIdsForToday.add(uw.getWorkoutId());
                    }
                }

                if (workoutIdsForToday.isEmpty()) {
                    isLoading.postValue(false);
                    todayWorkouts.postValue(new ArrayList<>());
                    return;
                }

                loadAllWorkoutsAndFilter(workoutIdsForToday, userWorkoutMap);
            }

            @Override
            public void onFailure(String error) {
                isLoading.postValue(false);
                errorMessage.postValue(error);
            }
        });
    }

    private void loadAllWorkoutsAndFilter(List<String> workoutIdsForToday, Map<String, UserWorkout> userWorkoutMap) {
        firebaseHelper.getWorkoutTemplates(new FirebaseHelper.FirestoreCallback<List<Workout>>() {
            @Override
            public void onSuccess(List<Workout> templateWorkouts) {
                firebaseHelper.getUserCustomWorkouts(userId, new FirebaseHelper.FirestoreCallback<List<Workout>>() {
                    @Override
                    public void onSuccess(List<Workout> customWorkouts) {
                        allWorkouts.clear();
                        if (templateWorkouts != null) allWorkouts.addAll(templateWorkouts);
                        if (customWorkouts != null) allWorkouts.addAll(customWorkouts);

                        List<Workout> todaysWorkouts = new ArrayList<>();
                        for (Workout workout : allWorkouts) {
                            if (workoutIdsForToday.contains(workout.getId())) {
                                UserWorkout uw = userWorkoutMap.get(workout.getId());
                                if (uw != null) {
                                    workout.setAdded(true);
                                    workout.setCompletedForDay(today, uw.isCompletedForDay(today));
                                    workout.setDaysOfWeek(uw.getDaysOfWeek());
                                }
                                todaysWorkouts.add(workout);
                            }
                        }

                        isLoading.postValue(false);
                        todayWorkouts.postValue(todaysWorkouts);
                    }

                    @Override
                    public void onFailure(String error) {
                        isLoading.postValue(false);
                        errorMessage.postValue(error);
                    }
                });
            }

            @Override
            public void onFailure(String error) {
                isLoading.postValue(false);
                errorMessage.postValue(error);
            }
        });
    }

    public void markWorkoutCompleted(Workout workout, String day, boolean completed) {
        workout.setCompletedForDay(day, completed);

        // Update Firestore
        firebaseHelper.getUserAddedWorkouts(userId, new FirebaseHelper.FirestoreCallback<List<UserWorkout>>() {
            @Override
            public void onSuccess(List<UserWorkout> userWorkouts) {
                for (UserWorkout uw : userWorkouts) {
                    if (uw.getWorkoutId().equals(workout.getId())) {
                        uw.setCompletedForDay(day, completed);
                        firebaseHelper.updateUserWorkoutProgress(uw, new FirebaseHelper.FirestoreCallback<Void>() {
                            @Override
                            public void onSuccess(Void result) {
                                // Refresh the list
                                loadTodayWorkouts();
                            }
                            @Override
                            public void onFailure(String error) { }
                        });
                        break;
                    }
                }
            }
            @Override
            public void onFailure(String error) { }
        });
    }

    public void refresh() {
        loadTodayWorkouts();
    }
}