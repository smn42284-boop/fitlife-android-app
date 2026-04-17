package com.example.fitlife_sumyatnoe.repository;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;
import android.util.Log;

import com.example.fitlife_sumyatnoe.database.AppDatabase;
import com.example.fitlife_sumyatnoe.database.entities.ExerciseEntity;
import com.example.fitlife_sumyatnoe.database.entities.WorkoutEntity;
import com.example.fitlife_sumyatnoe.models.Equipment;
import com.example.fitlife_sumyatnoe.models.Exercise;
import com.example.fitlife_sumyatnoe.models.UserWorkout;
import com.example.fitlife_sumyatnoe.models.Workout;
import com.example.fitlife_sumyatnoe.utils.FirebaseHelper;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WorkoutRepository {
    private static WorkoutRepository instance;
    private AppDatabase database;
    private FirebaseHelper firebaseHelper;
    private SharedPreferences prefs;
    private ExecutorService executorService;
    private boolean isLoading = false;
    private long lastCacheUpdate = 0;
    private static final long CACHE_EXPIRY = 5 * 60 * 1000; // 5 minutes
    private Context context;

    private List<UserWorkout> pendingSync = new ArrayList<>();
    private Gson gson = new Gson();

    private WorkoutRepository(Context context) {
        this.context = context.getApplicationContext();
        database = AppDatabase.getInstance(context);
        firebaseHelper = new FirebaseHelper();
        prefs = context.getSharedPreferences("FitLifePrefs", Context.MODE_PRIVATE);
        executorService = Executors.newSingleThreadExecutor();

        loadPendingSyncFromDisk();
    }

    public static synchronized WorkoutRepository getInstance(Context context) {
        if (instance == null) {
            instance = new WorkoutRepository(context.getApplicationContext());
        }
        return instance;
    }

    public interface DataCallback<T> {
        void onSuccess(T data);
        void onFailure(String error);
    }

    // ========== NETWORK CHECK ==========

    private boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }


    private void savePendingSyncToDisk() {
        String json = gson.toJson(pendingSync);
        prefs.edit().putString("pending_sync", json).apply();
        Log.d("WorkoutRepository", "Saved " + pendingSync.size() + " pending sync items");
    }

    private void loadPendingSyncFromDisk() {
        String json = prefs.getString("pending_sync", "[]");
        Type type = new TypeToken<List<UserWorkout>>(){}.getType();
        pendingSync = gson.fromJson(json, type);
        if (pendingSync == null) {
            pendingSync = new ArrayList<>();
        }
        Log.d("WorkoutRepository", "Loaded " + pendingSync.size() + " pending sync items");
    }

    private void storePendingSync(UserWorkout userWorkout) {
        pendingSync.add(userWorkout);
        savePendingSyncToDisk();
        Log.d("WorkoutRepository", "Stored pending sync for workout: " + userWorkout.getWorkoutId());
    }

    public void syncPendingOperations() {
        if (!isNetworkAvailable() || pendingSync.isEmpty()) {
            Log.d("WorkoutRepository", "No network or no pending sync items");
            return;
        }

        Log.d("WorkoutRepository", "Syncing " + pendingSync.size() + " pending operations...");

        List<UserWorkout> itemsToSync = new ArrayList<>(pendingSync);
        for (UserWorkout uw : itemsToSync) {
            firebaseHelper.addWorkoutToUserPlan(uw, new FirebaseHelper.FirestoreCallback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    pendingSync.remove(uw);
                    savePendingSyncToDisk();
                    Log.d("WorkoutRepository", "Synced: " + uw.getWorkoutId());
                }

                @Override
                public void onFailure(String error) {
                    Log.e("WorkoutRepository", "Failed to sync: " + error);
                    // Keep in queue for next time
                }
            });
        }
    }

    // ========== LOAD WORKOUTS ==========

    public void loadWorkouts(DataCallback<List<Workout>> callback) {
        executorService.execute(() -> {
            try {
                List<WorkoutEntity> cached = database.workoutDao().getAll();

                if (!cached.isEmpty()) {
                    List<Workout> workouts = convertToWorkoutsWithExercises(cached);
                    if (callback != null) {
                        callback.onSuccess(workouts);
                    }

                    refreshCache();
                } else {
                    fetchFromFirestore(callback);
                }
            } catch (Exception e) {
                Log.e("WorkoutRepository", "Error loading workouts", e);
                if (callback != null) {
                    callback.onFailure(e.getMessage());
                }
            }
        });
    }

    public void loadWorkoutsFast(DataCallback<List<Workout>> callback) {
        executorService.execute(() -> {
            List<WorkoutEntity> cached = database.workoutDao().getAll();

            if (!cached.isEmpty()) {
                List<Workout> workouts = convertToWorkoutsWithExercises(cached);
                if (callback != null) {
                    callback.onSuccess(workouts);
                }
                if (isNetworkAvailable()) {
                    refreshCache();
                }
            } else {
                if (isNetworkAvailable()) {
                    fetchFromFirestore(callback);
                } else {
                    if (callback != null) {
                        callback.onFailure("No internet connection and no cached data");
                    }
                }
            }
        });
    }

    public void saveWorkoutLocally(Workout workout, DataCallback<String> callback) {
        executorService.execute(() -> {
            try {
                WorkoutEntity entity = convertToEntity(workout);
                database.workoutDao().insert(entity);

                if (workout.getExercises() != null) {
                    List<ExerciseEntity> exerciseEntities = new ArrayList<>();
                    int position = 0;
                    for (Exercise exercise : workout.getExercises()) {
                        ExerciseEntity exerciseEntity = convertToExerciseEntity(exercise, workout.getId(), position++);
                        exerciseEntities.add(exerciseEntity);
                    }
                    database.exerciseDao().insertAll(exerciseEntities);
                }

                queueForSync(workout);

                if (callback != null) {
                    callback.onSuccess(workout.getId());
                }
            } catch (Exception e) {
                if (callback != null) {
                    callback.onFailure(e.getMessage());
                }
            }
        });
    }

    public void addWorkoutToPlanLocally(Workout workout, DataCallback<Void> callback) {
        executorService.execute(() -> {
            try {
                WorkoutEntity entity = database.workoutDao().getById(workout.getId());
                if (entity != null) {
                    entity.setAdded(true);
                    entity.setDaysOfWeek(workout.getDaysOfWeek());
                    database.workoutDao().update(entity);
                }
                queueWorkoutPlanForSync(workout);
                if (callback != null) {
                    callback.onSuccess(null);
                }
            } catch (Exception e) {
                if (callback != null) {
                    callback.onFailure(e.getMessage());
                }
            }
        });
    }

    private void queueWorkoutPlanForSync(Workout workout) {
        String json = gson.toJson(workout);
        prefs.edit().putString("pending_plan_" + workout.getId(), json).apply();

        int pendingPlanCount = prefs.getInt("pending_plan_count", 0);
        prefs.edit().putInt("pending_plan_count", pendingPlanCount + 1).apply();
    }
    private void queueForSync(Workout workout) {
        String json = gson.toJson(workout);
        prefs.edit().putString("pending_workout_" + workout.getId(), json).apply();

        int pendingCount = prefs.getInt("pending_workout_count", 0);
        prefs.edit().putInt("pending_workout_count", pendingCount + 1).apply();

    }

    public void syncPendingWorkouts(DataCallback<Void> callback) {
        if (!isNetworkAvailable()) {
            if (callback != null) callback.onFailure("No network");
            return;
        }
        executorService.execute(() -> {
            int pendingCount = prefs.getInt("pending_workout_count", 0);
            if (pendingCount == 0) {
                if (callback != null) callback.onSuccess(null);
                return;
            }
            List<Workout> pendingWorkouts = new ArrayList<>();
            for (int i = 0; i < pendingCount; i++) {
                String json = prefs.getString("pending_workout_" + i, null);
                if (json != null) {
                    Workout workout = gson.fromJson(json, Workout.class);
                    pendingWorkouts.add(workout);
                }
            }
            for (Workout workout : pendingWorkouts) {
                firebaseHelper.saveCustomWorkout(workout, new FirebaseHelper.FirestoreCallback<String>() {
                    @Override
                    public void onSuccess(String cloudId) {
                        updateLocalWorkoutId(workout.getId(), cloudId);
                        prefs.edit().remove("pending_workout_" + workout.getId()).apply();

                        int remaining = prefs.getInt("pending_workout_count", 0) - 1;
                        prefs.edit().putInt("pending_workout_count", remaining).apply();

                    }

                    @Override
                    public void onFailure(String error) {
                        Log.e("WorkoutRepository", "Failed to sync: " + error);
                    }
                });
            }

            if (callback != null) callback.onSuccess(null);
        });
    }

    private void updateLocalWorkoutId(String oldId, String newId) {
        executorService.execute(() -> {
            WorkoutEntity entity = database.workoutDao().getById(oldId);
            if (entity != null) {
                entity.setFirestoreId(newId);
                database.workoutDao().update(entity);
            }
        });
    }
    public void loadExercises(DataCallback<List<Exercise>> callback) {
        executorService.execute(() -> {

        });
    }

    public void loadEquipment(DataCallback<List<Equipment>> callback) {
        firebaseHelper.getDefaultEquipment(new FirebaseHelper.FirestoreCallback<List<Equipment>>() {
            @Override
            public void onSuccess(List<Equipment> equipment) {
                callback.onSuccess(equipment);
            }

            @Override
            public void onFailure(String error) {
                callback.onFailure(error);
            }
        });
    }
    public void getWorkout(String workoutId, DataCallback<Workout> callback) {
        executorService.execute(() -> {
            WorkoutEntity entity = database.workoutDao().getById(workoutId);

            if (entity != null) {
                Workout workout = convertToWorkoutWithExercises(entity);
                if (callback != null) {
                    callback.onSuccess(workout);
                }

                // Refresh from Firestore in background if needed
                if (isNetworkAvailable()) {
                    refreshSingleWorkout(workoutId);
                }
            } else {
                // Not in cache, fetch from Firestore
                if (isNetworkAvailable()) {
                    fetchSingleWorkoutFromFirestore(workoutId, callback);
                } else {
                    if (callback != null) {
                        callback.onFailure("Workout not found in cache and no internet");
                    }
                }
            }
        });
    }

    private void refreshSingleWorkout(String workoutId) {
        firebaseHelper.getWorkoutTemplate(workoutId, new FirebaseHelper.FirestoreCallback<Workout>() {
            @Override
            public void onSuccess(Workout workout) {
                if (workout != null) {
                    cacheSingleWorkout(workout);
                }
            }
            @Override
            public void onFailure(String error) { }
        });
    }

    private void fetchSingleWorkoutFromFirestore(String workoutId, DataCallback<Workout> callback) {
        firebaseHelper.getWorkoutTemplate(workoutId, new FirebaseHelper.FirestoreCallback<Workout>() {
            @Override
            public void onSuccess(Workout workout) {
                if (workout != null) {
                    cacheSingleWorkout(workout);
                    if (callback != null) {
                        callback.onSuccess(workout);
                    }
                } else {
                    firebaseHelper.getCustomWorkout(workoutId, new FirebaseHelper.FirestoreCallback<Workout>() {
                        @Override
                        public void onSuccess(Workout customWorkout) {
                            cacheSingleWorkout(customWorkout);
                            if (callback != null) {
                                callback.onSuccess(customWorkout);
                            }
                        }
                        @Override
                        public void onFailure(String error) {
                            if (callback != null) {
                                callback.onFailure(error);
                            }
                        }
                    });
                }
            }
            @Override
            public void onFailure(String error) {
                if (callback != null) {
                    callback.onFailure(error);
                }
            }
        });
    }

    private void cacheSingleWorkout(Workout workout) {
        executorService.execute(() -> {
            WorkoutEntity entity = convertToEntity(workout);
            database.workoutDao().insert(entity);

            if (workout.getExercises() != null) {
                List<ExerciseEntity> exerciseEntities = new ArrayList<>();
                int position = 0;
                for (Exercise exercise : workout.getExercises()) {
                    ExerciseEntity exerciseEntity = convertToExerciseEntity(exercise, workout.getId(), position++);
                    exerciseEntities.add(exerciseEntity);
                }
                database.exerciseDao().deleteByWorkoutId(workout.getId());
                database.exerciseDao().insertAll(exerciseEntities);
            }
        });
    }

    public void forceRefreshCache(DataCallback<List<Workout>> callback) {
        executorService.execute(() -> {
            database.workoutDao().deleteAll();
            database.exerciseDao().deleteAll();

            fetchFromFirestore(new DataCallback<List<Workout>>() {
                @Override
                public void onSuccess(List<Workout> freshWorkouts) {
                    cacheWorkoutsWithExercises(freshWorkouts);
                    if (callback != null) callback.onSuccess(freshWorkouts);
                }

                @Override
                public void onFailure(String error) {
                    if (callback != null) callback.onFailure(error);
                }
            });
        });
    }
    public void getUserWorkouts(String userId, DataCallback<List<UserWorkout>> callback) {
        firebaseHelper.getUserAddedWorkouts(userId, new FirebaseHelper.FirestoreCallback<List<UserWorkout>>() {
            @Override
            public void onSuccess(List<UserWorkout> userWorkouts) {
                if (callback != null) {
                    callback.onSuccess(userWorkouts);
                }
            }
            @Override
            public void onFailure(String error) {
                if (callback != null) {
                    callback.onFailure(error);
                }
            }
        });
    }

    // ========== UPDATE WORKOUT COMPLETION ==========

    public void updateWorkoutCompletion(Workout workout, String day, boolean completed, DataCallback<Void> callback) {
        String userId = prefs.getString("userId", "");

        // Update local cache first
        executorService.execute(() -> {
            WorkoutEntity entity = database.workoutDao().getById(workout.getId());
            if (entity != null) {
                // Update local workout
                database.workoutDao().update(entity);
            }
        });

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
                                if (callback != null) callback.onSuccess(null);
                            }
                            @Override
                            public void onFailure(String error) {
                                if (callback != null) callback.onFailure(error);
                            }
                        });
                        break;
                    }
                }
            }
            @Override
            public void onFailure(String error) {
                if (callback != null) callback.onFailure(error);
            }
        });
    }

    // ========== UPDATE WORKOUT DAYS ==========

    public void updateWorkoutDays(Workout workout, List<String> days, DataCallback<Void> callback) {
        String userId = prefs.getString("userId", "");

        // Update local cache
        executorService.execute(() -> {
            WorkoutEntity entity = database.workoutDao().getById(workout.getId());
            if (entity != null) {
                entity.setDaysOfWeek(days);
                database.workoutDao().update(entity);
            }
        });

        // Update Firestore
        firebaseHelper.getUserAddedWorkouts(userId, new FirebaseHelper.FirestoreCallback<List<UserWorkout>>() {
            @Override
            public void onSuccess(List<UserWorkout> userWorkouts) {
                for (UserWorkout uw : userWorkouts) {
                    if (uw.getWorkoutId().equals(workout.getId())) {
                        uw.setDaysOfWeek(days);
                        firebaseHelper.updateUserWorkoutDays(uw.getId(), workout.getId(), days,
                                new FirebaseHelper.FirestoreCallback<Void>() {
                                    @Override
                                    public void onSuccess(Void result) {
                                        if (callback != null) callback.onSuccess(null);
                                    }
                                    @Override
                                    public void onFailure(String error) {
                                        if (callback != null) callback.onFailure(error);
                                    }
                                });
                        break;
                    }
                }
            }
            @Override
            public void onFailure(String error) {
                if (callback != null) callback.onFailure(error);
            }
        });
    }

    // ========== DELETE WORKOUT ==========

    public void deleteWorkout(Workout workout, DataCallback<Void> callback) {
        if (!workout.isCustom()) {
            if (callback != null) {
                callback.onFailure("Cannot delete default workouts");
            }
            return;
        }

        // Delete from Firestore
        firebaseHelper.deleteCustomWorkout(workout.getId(), new FirebaseHelper.FirestoreCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                // Delete from local cache
                executorService.execute(() -> {
                    database.workoutDao().delete(convertToEntity(workout));
                    database.exerciseDao().deleteByWorkoutId(workout.getId());
                });

                if (callback != null) callback.onSuccess(null);
            }
            @Override
            public void onFailure(String error) {
                if (callback != null) callback.onFailure(error);
            }
        });
    }

    // ========== SAVE CUSTOM WORKOUT ==========

    public void saveCustomWorkout(Workout workout, DataCallback<String> callback) {
        firebaseHelper.saveCustomWorkout(workout, new FirebaseHelper.FirestoreCallback<String>() {
            @Override
            public void onSuccess(String id) {
                workout.setId(id);
                // Save to local cache
                cacheSingleWorkout(workout);
                if (callback != null) callback.onSuccess(id);
            }
            @Override
            public void onFailure(String error) {
                if (callback != null) callback.onFailure(error);
            }
        });
    }

    public void updateCustomWorkout(Workout workout, DataCallback<Void> callback) {
        firebaseHelper.updateCustomWorkout(workout, new FirebaseHelper.FirestoreCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                cacheSingleWorkout(workout);
                if (callback != null) callback.onSuccess(null);
            }
            @Override
            public void onFailure(String error) {
                if (callback != null) callback.onFailure(error);
            }
        });
    }

    // ========== REFRESH METHODS ==========

    public void refresh(DataCallback<List<Workout>> callback) {
        executorService.execute(() -> {
            List<WorkoutEntity> cached = database.workoutDao().getAll();
            if (!cached.isEmpty()) {
                List<Workout> workouts = convertToWorkoutsWithExercises(cached);
                if (callback != null) {
                    callback.onSuccess(workouts);
                }
            }
        });

        if (isNetworkAvailable()) {
            refreshCache();

            fetchFromFirestore(new DataCallback<List<Workout>>() {
                @Override
                public void onSuccess(List<Workout> freshWorkouts) {
                    cacheWorkoutsWithExercises(freshWorkouts);
                    if (callback != null) {
                        callback.onSuccess(freshWorkouts);
                    }
                }

                @Override
                public void onFailure(String error) {
                    Log.e("WorkoutRepository", "Refresh failed: " + error);
                }
            });
        } else {
            Log.d("WorkoutRepository", "No network - using cached data only");
        }
    }

    // ========== PRIVATE METHODS ==========

    private void refreshCache() {
        if (isLoading) return;
        isLoading = true;

        String userId = prefs.getString("userId", "");

        firebaseHelper.getWorkoutTemplates(new FirebaseHelper.FirestoreCallback<List<Workout>>() {
            @Override
            public void onSuccess(List<Workout> templateWorkouts) {
                firebaseHelper.getUserCustomWorkouts(userId, new FirebaseHelper.FirestoreCallback<List<Workout>>() {
                    @Override
                    public void onSuccess(List<Workout> customWorkouts) {
                        firebaseHelper.getUserAddedWorkouts(userId, new FirebaseHelper.FirestoreCallback<List<UserWorkout>>() {
                            @Override
                            public void onSuccess(List<UserWorkout> userWorkouts) {
                                List<Workout> allWorkouts = new ArrayList<>();
                                allWorkouts.addAll(templateWorkouts);
                                allWorkouts.addAll(customWorkouts);

                                Map<String, UserWorkout> progressMap = new HashMap<>();
                                for (UserWorkout uw : userWorkouts) {
                                    progressMap.put(uw.getWorkoutId(), uw);
                                }

                                for (Workout workout : allWorkouts) {
                                    UserWorkout progress = progressMap.get(workout.getId());
                                    if (progress != null) {
                                        workout.setAdded(progress.isAdded());
                                        workout.setCompleted(progress.isCompleted());
                                        workout.setDaysOfWeek(progress.getDaysOfWeek());
                                    }
                                }

                                cacheWorkoutsWithExercises(allWorkouts);
                                lastCacheUpdate = System.currentTimeMillis();
                                isLoading = false;
                            }

                            @Override
                            public void onFailure(String error) {
                                isLoading = false;
                                Log.e("WorkoutRepository", "Error loading user workouts: " + error);
                            }
                        });
                    }

                    @Override
                    public void onFailure(String error) {
                        isLoading = false;
                        Log.e("WorkoutRepository", "Error loading custom workouts: " + error);
                    }
                });
            }

            @Override
            public void onFailure(String error) {
                isLoading = false;
                Log.e("WorkoutRepository", "Error loading templates: " + error);
            }
        });
    }

    private void fetchFromFirestore(DataCallback<List<Workout>> callback) {
        String userId = prefs.getString("userId", "");

        firebaseHelper.getWorkoutTemplates(new FirebaseHelper.FirestoreCallback<List<Workout>>() {
            @Override
            public void onSuccess(List<Workout> templateWorkouts) {
                firebaseHelper.getUserCustomWorkouts(userId, new FirebaseHelper.FirestoreCallback<List<Workout>>() {
                    @Override
                    public void onSuccess(List<Workout> customWorkouts) {
                        firebaseHelper.getUserAddedWorkouts(userId, new FirebaseHelper.FirestoreCallback<List<UserWorkout>>() {
                            @Override
                            public void onSuccess(List<UserWorkout> userWorkouts) {
                                List<Workout> allWorkouts = new ArrayList<>();
                                allWorkouts.addAll(templateWorkouts);
                                allWorkouts.addAll(customWorkouts);

                                Map<String, UserWorkout> progressMap = new HashMap<>();
                                for (UserWorkout uw : userWorkouts) {
                                    progressMap.put(uw.getWorkoutId(), uw);
                                }

                                for (Workout workout : allWorkouts) {
                                    UserWorkout progress = progressMap.get(workout.getId());
                                    if (progress != null) {
                                        workout.setAdded(progress.isAdded());
                                        workout.setCompleted(progress.isCompleted());
                                        workout.setDaysOfWeek(progress.getDaysOfWeek());
                                    }
                                }

                                cacheWorkoutsWithExercises(allWorkouts);
                                lastCacheUpdate = System.currentTimeMillis();

                                if (callback != null) {
                                    callback.onSuccess(allWorkouts);
                                }
                            }

                            @Override
                            public void onFailure(String error) {
                                if (callback != null) callback.onFailure(error);
                            }
                        });
                    }

                    @Override
                    public void onFailure(String error) {
                        if (callback != null) callback.onFailure(error);
                    }
                });
            }

            @Override
            public void onFailure(String error) {
                if (callback != null) callback.onFailure(error);
            }
        });
    }

    private void cacheWorkoutsWithExercises(List<Workout> workouts) {
        executorService.execute(() -> {
            database.workoutDao().deleteAll();
            database.exerciseDao().deleteAll();

            for (Workout workout : workouts) {
                WorkoutEntity entity = convertToEntity(workout);
                database.workoutDao().insert(entity);

                if (workout.getExercises() != null && !workout.getExercises().isEmpty()) {
                    List<ExerciseEntity> exerciseEntities = new ArrayList<>();
                    int position = 0;
                    for (Exercise exercise : workout.getExercises()) {
                        ExerciseEntity exerciseEntity = convertToExerciseEntity(exercise, workout.getId(), position++);
                        exerciseEntities.add(exerciseEntity);
                    }
                    database.exerciseDao().insertAll(exerciseEntities);
                }
            }
            Log.d("WorkoutRepository", "Cached " + workouts.size() + " workouts with exercises");
        });
    }

    private List<Workout> convertToWorkoutsWithExercises(List<WorkoutEntity> entities) {
        List<Workout> workouts = new ArrayList<>();
        for (WorkoutEntity entity : entities) {
            Workout workout = convertToWorkout(entity);

            List<ExerciseEntity> exerciseEntities = database.exerciseDao().getByWorkoutId(workout.getId());
            List<Exercise> exercises = new ArrayList<>();
            for (ExerciseEntity exEntity : exerciseEntities) {
                Exercise exercise = convertToExercise(exEntity);
                exercises.add(exercise);
            }
            workout.setExercises(exercises);

            workouts.add(workout);
        }
        return workouts;
    }

    private Workout convertToWorkoutWithExercises(WorkoutEntity entity) {
        Workout workout = convertToWorkout(entity);

        List<ExerciseEntity> exerciseEntities = database.exerciseDao().getByWorkoutId(workout.getId());
        List<Exercise> exercises = new ArrayList<>();
        for (ExerciseEntity exEntity : exerciseEntities) {
            Exercise exercise = convertToExercise(exEntity);
            exercises.add(exercise);
        }
        workout.setExercises(exercises);

        return workout;
    }
    public LiveData<List<Workout>> getWorkoutsLive() {
        MutableLiveData<List<Workout>> liveData = new MutableLiveData<>();

        executorService.execute(() -> {
            List<WorkoutEntity> cached = database.workoutDao().getAll();
            if (!cached.isEmpty()) {
                List<Workout> workouts = convertToWorkoutsWithExercises(cached);
                liveData.postValue(workouts);
            }
        });

        return liveData;
    }

    // ========== ADD/REMOVE WORKOUT TO/FROM PLAN ==========

    public void addWorkoutToPlan(Workout workout, DataCallback<Void> callback) {
        String userId = prefs.getString("userId", "");

        UserWorkout userWorkout = new UserWorkout(userId, workout.getId(), workout.isCustom());
        userWorkout.setAdded(true);
        userWorkout.setDaysOfWeek(workout.getDaysOfWeek());
        userWorkout.setLastUpdated(System.currentTimeMillis());

        // Initialize completion tracking
        Map<String, Boolean> completedByDay = new HashMap<>();
        for (String day : workout.getDaysOfWeek()) {
            completedByDay.put(day, false);
        }
        userWorkout.setCompletedByDay(completedByDay);

        // Update local cache
        executorService.execute(() -> {
            WorkoutEntity entity = database.workoutDao().getById(workout.getId());
            if (entity != null) {
                entity.setAdded(true);
                entity.setDaysOfWeek(workout.getDaysOfWeek());
                database.workoutDao().update(entity);
            }
        });

        // Save to Firestore
        firebaseHelper.addWorkoutToUserPlan(userWorkout, new FirebaseHelper.FirestoreCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                if (callback != null) callback.onSuccess(null);
            }

            @Override
            public void onFailure(String error) {
                // If offline, store for later sync
                if (!isNetworkAvailable()) {
                    storePendingSync(userWorkout);
                    if (callback != null) callback.onSuccess(null); // Success locally
                } else {
                    if (callback != null) callback.onFailure(error);
                }
            }
        });
    }

    public void removeWorkoutFromPlan(Workout workout, DataCallback<Void> callback) {
        String userId = prefs.getString("userId", "");

        firebaseHelper.getUserAddedWorkouts(userId, new FirebaseHelper.FirestoreCallback<List<UserWorkout>>() {
            @Override
            public void onSuccess(List<UserWorkout> userWorkouts) {
                String userWorkoutId = null;
                for (UserWorkout uw : userWorkouts) {
                    if (uw.getWorkoutId().equals(workout.getId())) {
                        userWorkoutId = uw.getId();
                        break;
                    }
                }

                if (userWorkoutId != null) {
                    executorService.execute(() -> {
                        WorkoutEntity entity = database.workoutDao().getById(workout.getId());
                        if (entity != null) {
                            entity.setAdded(false);
                            entity.setDaysOfWeek(new ArrayList<>());
                            database.workoutDao().update(entity);
                        }
                    });

                    firebaseHelper.removeWorkoutFromUserPlan(userWorkoutId, new FirebaseHelper.FirestoreCallback<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                            if (callback != null) callback.onSuccess(null);
                        }

                        @Override
                        public void onFailure(String error) {
                            if (callback != null) callback.onFailure(error);
                        }
                    });
                } else {
                    if (callback != null) callback.onFailure("Workout not found in plan");
                }
            }

            @Override
            public void onFailure(String error) {
                if (callback != null) callback.onFailure(error);
            }
        });
    }
    private Exercise convertToExercise(ExerciseEntity entity) {
        Exercise exercise;
        if (entity.isTimed()) {
            exercise = new Exercise(entity.getName(), entity.getSets(), entity.getDuration(), true);
        } else {
            exercise = new Exercise(entity.getName(), entity.getSets(), entity.getReps());
        }
        exercise.setInstructions(entity.getInstructions());
        exercise.setImageUrl(entity.getImageUrl());
        exercise.setLocalImagePath(entity.getLocalImagePath());
        exercise.setCategory(entity.getCategory());
        return exercise;
    }

    private WorkoutEntity convertToEntity(Workout workout) {
        WorkoutEntity entity = new WorkoutEntity();
        entity.setFirestoreId(workout.getId());
        entity.setName(workout.getName());
        entity.setDescription(workout.getDescription());
        entity.setDuration(workout.getDuration());
        entity.setImageUrl(workout.getImageUrl());
        entity.setLocalImagePath(workout.getLocalImagePath());
        entity.setDefault(workout.isDefault());
        entity.setCustom(workout.isCustom());
        entity.setAdded(workout.isAdded());
        entity.setCompleted(workout.isCompleted());
        entity.setCompletedDate(workout.getCompletedDate());
        entity.setDaysOfWeek(workout.getDaysOfWeek());
        entity.setEquipment(workout.getEquipment());
        entity.setLastUpdated(System.currentTimeMillis());
        return entity;
    }

    private Workout convertToWorkout(WorkoutEntity entity) {
        Workout workout = new Workout();
        workout.setId(entity.getFirestoreId());
        workout.setName(entity.getName());
        workout.setDescription(entity.getDescription());
        workout.setDuration(entity.getDuration());
        workout.setImageUrl(entity.getImageUrl());
        workout.setLocalImagePath(entity.getLocalImagePath());
        workout.setDefaultWorkout(entity.isDefault());
        workout.setAdded(entity.isAdded());
        workout.setCustom(entity.isCustom());
        workout.setCompleted(entity.isCompleted());
        workout.setCompletedDate(entity.getCompletedDate());
        workout.setDaysOfWeek(entity.getDaysOfWeek());
        workout.setEquipment(entity.getEquipment());
        return workout;
    }

    private ExerciseEntity convertToExerciseEntity(Exercise exercise, String workoutId, int position) {
        ExerciseEntity entity = new ExerciseEntity();
        entity.setWorkoutId(workoutId);
        entity.setName(exercise.getName());
        entity.setSets(exercise.getSets());
        entity.setReps(exercise.getReps());
        entity.setDuration(exercise.getDuration());
        entity.setTimed(exercise.isTimed());
        entity.setInstructions(exercise.getInstructions());
        entity.setImageUrl(exercise.getImageUrl());
        entity.setLocalImagePath(exercise.getLocalImagePath());
        entity.setCategory(exercise.getCategory());
        entity.setPosition(position);
        return entity;
    }
}