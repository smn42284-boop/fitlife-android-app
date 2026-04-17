package com.example.fitlife_sumyatnoe.utils;

import android.text.TextUtils;
import android.util.Log;

import com.example.fitlife_sumyatnoe.models.Equipment;
import com.example.fitlife_sumyatnoe.models.Exercise;
import com.example.fitlife_sumyatnoe.models.User;
import com.example.fitlife_sumyatnoe.models.UserBMIHistory;
import com.example.fitlife_sumyatnoe.models.UserBodyInfo;
import com.example.fitlife_sumyatnoe.models.UserPreferences;
import com.example.fitlife_sumyatnoe.models.UserWorkout;
import com.example.fitlife_sumyatnoe.models.Workout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirebaseHelper {

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    public FirebaseHelper() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .setCacheSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
                .build();
        db.setFirestoreSettings(settings);

        auth = FirebaseAuth.getInstance();
    }

    public FirebaseUser getCurrentUser() {
        return auth.getCurrentUser();
    }

    // Add to FirebaseHelper.java
    public void saveUserPreferences(UserPreferences preferences, FirestoreCallback<Void> callback) {
        if (preferences == null || TextUtils.isEmpty(preferences.getUserId())) {
            callback.onFailure("Invalid preferences");
            return;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("userId", preferences.getUserId());
        data.put("fitnessGoals", preferences.getFitnessGoals());
        data.put("preferredWorkouts", preferences.getPreferredWorkouts());
        data.put("experienceLevel", preferences.getExperienceLevel());
        data.put("workoutFrequency", preferences.getWorkoutFrequency());
        data.put("activityLevel", preferences.getActivityLevel());
        data.put("nutritionRecommendations", preferences.isNutritionRecommendations());
        data.put("bmr", preferences.getBmr());
        data.put("dailyCalorieRecommendation", preferences.getDailyCalorieRecommendation());
        data.put("updatedAt", preferences.getUpdatedAt());

        db.collection("userPreferences")
                .document(preferences.getUserId())
                .set(data)
                .addOnSuccessListener(aVoid -> {
                    if (callback != null) callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onFailure(e.getMessage());
                });
    }
    public void registerUser(String email, String password, String name, AuthCallback callback) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(name)
                                    .build();
                            user.updateProfile(profileUpdates);

                            Map<String, Object> userData = new HashMap<>();
                            userData.put("name", name);
                            userData.put("email", email);
                            userData.put("theme", "light");
                            userData.put("fontSize", "Medium");
                            userData.put("createdAt", System.currentTimeMillis());

                            db.collection("users")
                                    .document(user.getUid())
                                    .set(userData)
                                    .addOnSuccessListener(aVoid -> {
                                        if (callback != null) callback.onSuccess(user);
                                    })
                                    .addOnFailureListener(e -> {
                                        if (callback != null) callback.onFailure(e.getMessage());
                                    });
                        } else {
                            if (callback != null) callback.onFailure("User is null");
                        }
                    } else {
                        if (callback != null) callback.onFailure(task.getException().getMessage());
                    }
                });
    }

    public void loginUser(String email, String password, AuthCallback callback) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        if (callback != null) callback.onSuccess(user);
                    } else {
                        if (callback != null) callback.onFailure(task.getException().getMessage());
                    }
                });
    }

    public void resetPassword(String email, AuthCallback callback) {
        auth.sendPasswordResetEmail(email)
                .addOnSuccessListener(aVoid -> {
                    if (callback != null) callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onFailure(e.getMessage());
                });
    }

    public void logout() {
        auth.signOut();
    }


    public void getUser(String userId, FirestoreCallback<User> callback) {
        db.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = new User();
                        user.setId(documentSnapshot.getId());
                        user.setName(documentSnapshot.getString("name"));
                        user.setEmail(documentSnapshot.getString("email"));
                        user.setTheme(documentSnapshot.getString("theme"));
                        user.setFontSize(documentSnapshot.getString("fontSize"));
                        callback.onSuccess(user);
                    } else {
                        callback.onFailure("User not found");
                    }
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }
    public void getCustomWorkout(String workoutId, FirestoreCallback<Workout> callback) {
        db.collection("custom_workouts")
                .document(workoutId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Workout workout = documentSnapshot.toObject(Workout.class);
                        if (workout != null) {
                            workout.setId(documentSnapshot.getId());
                            callback.onSuccess(workout);
                        } else {
                            callback.onFailure("Workout is null");
                        }
                    } else {
                        callback.onFailure("Workout not found in custom workouts");
                    }
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }
    public void updateUserWorkoutCompletion(String userWorkoutId, String day, boolean completed,
                                            FirestoreCallback<Void> callback) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("dayCompletion." + day, completed);
        updates.put("lastUpdated", System.currentTimeMillis());

        db.collection("user_workouts")
                .document(userWorkoutId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    if (callback != null) callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onFailure(e.getMessage());
                });
    }

    public void saveUser(User user, FirestoreCallback<Void> callback) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("name", user.getName());
        userData.put("email", user.getEmail());
        userData.put("theme", user.getTheme());
        userData.put("fontSize", user.getFontSize());
        userData.put("updatedAt", System.currentTimeMillis());

        db.collection("users")
                .document(user.getId())
                .set(userData)
                .addOnSuccessListener(aVoid -> {
                    if (callback != null) callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onFailure(e.getMessage());
                });
    }

    public void saveUserProfile(String userId, String name, String email, String theme, String fontSize,
                                FirestoreCallback<Void> callback) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("name", name);
        userData.put("email", email);
        userData.put("theme", theme);
        userData.put("fontSize", fontSize);
        userData.put("updatedAt", System.currentTimeMillis());

        db.collection("users")
                .document(userId)
                .set(userData)
                .addOnSuccessListener(aVoid -> {
                    if (callback != null) callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onFailure(e.getMessage());
                });
    }

    public void getUserProfile(String userId, FirestoreCallback<Map<String, Object>> callback) {
        db.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Map<String, Object> userData = new HashMap<>();
                        userData.put("name", documentSnapshot.getString("name"));
                        userData.put("email", documentSnapshot.getString("email"));
                        userData.put("theme", documentSnapshot.getString("theme"));
                        userData.put("fontSize", documentSnapshot.getString("fontSize"));
                        callback.onSuccess(userData);
                    } else {
                        callback.onSuccess(new HashMap<>());
                    }
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    // ========== WORKOUT TEMPLATES (DEFAULT WORKOUTS) ==========

    public void getWorkoutTemplates(FirestoreCallback<List<Workout>> callback) {
        db.collection("workout_templates")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Workout> workouts = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Workout workout = doc.toObject(Workout.class);
                        if (workout != null) {
                            workout.setId(doc.getId());
                            workouts.add(workout);
                        }
                    }
                    callback.onSuccess(workouts);
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

// In FirebaseHelper.java

    // Save BMI history record
    public void saveBMIHistory(UserBMIHistory history, FirestoreCallback<Void> callback) {
        if (history == null || TextUtils.isEmpty(history.getUserId())) {
            callback.onFailure("Invalid BMI history");
            return;
        }

        String id = db.collection("bmiHistory").document().getId();
        history.setId(id);

        db.collection("bmiHistory")
                .document(id)
                .set(history)
                .addOnSuccessListener(aVoid -> {
                    if (callback != null) callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onFailure(e.getMessage());
                });
    }

    // Get BMI history for user
    public void getBMIHistory(String userId, FirestoreCallback<List<UserBMIHistory>> callback) {
        if (TextUtils.isEmpty(userId)) {
            callback.onFailure("User ID is empty");
            return;
        }

        db.collection("bmiHistory")
                .whereEqualTo("userId", userId)
                .orderBy("recordedAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(10)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<UserBMIHistory> historyList = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        UserBMIHistory history = doc.toObject(UserBMIHistory.class);
                        if (history != null) {
                            history.setId(doc.getId());
                            historyList.add(history);
                        }
                    }
                    callback.onSuccess(historyList);
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }    public void getWorkoutTemplate(String templateId, FirestoreCallback<Workout> callback) {
        db.collection("workout_templates")
                .document(templateId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Workout workout = documentSnapshot.toObject(Workout.class);
                        if (workout != null) {
                            workout.setId(documentSnapshot.getId());

                            // Log to verify
                            Log.d("FirebaseHelper", "Loaded workout: " + workout.getName());
                            Log.d("FirebaseHelper", "Exercises: " + (workout.getExercises() != null ?
                                    workout.getExercises().size() : "null"));

                            callback.onSuccess(workout);
                        } else {
                            callback.onFailure("Workout is null");
                        }
                    } else {
                        callback.onFailure("Workout not found");
                    }
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }
    // In FirebaseHelper.java, add this method:
    public void updateUserProfile(String userId, String name, String email, String phone,
                                  String theme, String fontSize, String profileImagePath,
                                  FirestoreCallback<Void> callback) {
        if (TextUtils.isEmpty(userId)) {
            callback.onFailure("User ID is empty");
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        updates.put("email", email);
        updates.put("phone", phone);
        updates.put("theme", theme);
        updates.put("fontSize", fontSize);
        updates.put("updatedAt", System.currentTimeMillis());

        if (profileImagePath != null && !profileImagePath.isEmpty()) {
            updates.put("profileImagePath", profileImagePath);
        }

        db.collection("users")
                .document(userId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    if (callback != null) callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onFailure(e.getMessage());
                });
    }
    public void deleteWorkoutTemplate(String workoutId, FirestoreCallback<Void> callback) {
        db.collection("workout_templates")
                .document(workoutId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    if (callback != null) callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onFailure(e.getMessage());
                });
    }

    public void updateWorkout(Workout workout, FirestoreCallback<Void> callback) {
        if (workout == null || TextUtils.isEmpty(workout.getId())) {
            callback.onFailure("Invalid workout");
            return;
        }

        // Determine which collection to update
        String collection = workout.isCustom() ? "custom_workouts" : "workout_templates";

        Map<String, Object> updates = new HashMap<>();
        updates.put("name", workout.getName());
        updates.put("description", workout.getDescription());
        updates.put("duration", workout.getDuration());
        updates.put("exercises", workout.getExercises());
        updates.put("equipment", workout.getEquipment());
        updates.put("lastModified", System.currentTimeMillis());

        db.collection(collection)
                .document(workout.getId())
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    // IMPORTANT: Also update userWorkouts if added status changed
                    if (workout.isAdded()) {
                        addOrUpdateUserWorkout(workout, callback);
                    } else {
                        if (callback != null) callback.onSuccess(null);
                    }
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onFailure(e.getMessage());
                });
    }

    private void addOrUpdateUserWorkout(Workout workout, FirestoreCallback<Void> callback) {
        String userId = getCurrentUser().getUid();

        // First check if userWorkout already exists
        db.collection("userWorkouts")
                .whereEqualTo("userId", userId)
                .whereEqualTo("workoutId", workout.getId())
                .get()
                .addOnSuccessListener(query -> {
                    if (!query.isEmpty()) {
                        // Update existing
                        DocumentSnapshot doc = query.getDocuments().get(0);
                        Map<String, Object> updates = new HashMap<>();
                        updates.put("isAdded", workout.isAdded());
                        updates.put("daysOfWeek", workout.getDaysOfWeek());
                        updates.put("lastUpdated", System.currentTimeMillis());

                        // Initialize completion tracking for new days
                        Map<String, Boolean> completedByDay = new HashMap<>();
                        for (String day : workout.getDaysOfWeek()) {
                            completedByDay.put(day, false);
                        }
                        updates.put("completedByDay", completedByDay);

                        db.collection("userWorkouts")
                                .document(doc.getId())
                                .update(updates)
                                .addOnSuccessListener(aVoid -> {
                                    if (callback != null) callback.onSuccess(null);
                                })
                                .addOnFailureListener(e -> {
                                    if (callback != null) callback.onFailure(e.getMessage());
                                });
                    } else {
                        // Create new userWorkout
                        UserWorkout userWorkout = new UserWorkout(userId, workout.getId(), workout.isCustom());
                        userWorkout.setAdded(true);
                        userWorkout.setDaysOfWeek(workout.getDaysOfWeek());

                        // Initialize completion tracking
                        Map<String, Boolean> completedByDay = new HashMap<>();
                        for (String day : workout.getDaysOfWeek()) {
                            completedByDay.put(day, false);
                        }
                        userWorkout.setCompletedByDay(completedByDay);
                        userWorkout.setLastUpdated(System.currentTimeMillis());

                        String id = db.collection("userWorkouts").document().getId();
                        userWorkout.setId(id);

                        db.collection("userWorkouts")
                                .document(id)
                                .set(userWorkout)
                                .addOnSuccessListener(aVoid -> {
                                    if (callback != null) callback.onSuccess(null);
                                })
                                .addOnFailureListener(e -> {
                                    if (callback != null) callback.onFailure(e.getMessage());
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onFailure(e.getMessage());
                });
    }


    public void updateWorkoutTemplate(Workout workout, FirestoreCallback<Void> callback) {
        if (workout.getId() == null || workout.getId().isEmpty()) {
            if (callback != null) callback.onFailure("Workout ID is null");
            return;
        }

        db.collection("workout_templates")
                .document(workout.getId())
                .set(workout)
                .addOnSuccessListener(aVoid -> {
                    if (callback != null) callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onFailure(e.getMessage());
                });
    }

    public void saveCustomWorkout(Workout workout, FirestoreCallback<String> callback) {
        workout.setUserId(getCurrentUser().getUid());
        workout.setDefaultWorkout(workout.isDefault());

        String id = db.collection("custom_workouts").document().getId();
        workout.setId(id);
        db.collection("custom_workouts")
                .document(id)
                .set(workout)
                .addOnSuccessListener(aVoid -> {
                    if (callback != null) callback.onSuccess(id);
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onFailure(e.getMessage());
                });
    }

    public void getUserCustomWorkouts(String userId, FirestoreCallback<List<Workout>> callback) {
        db.collection("custom_workouts")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Workout> workouts = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Workout workout = doc.toObject(Workout.class);
                        if (workout != null) {
                            workout.setId(doc.getId());
                            workouts.add(workout);
                        }
                    }
                    callback.onSuccess(workouts);
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }
    public void updateUserWorkoutDays(String userWorkoutId, String workoutId, List<String> days,
                                      FirestoreCallback<Void> callback) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("daysOfWeek", days);
        updates.put("lastUpdated", System.currentTimeMillis());

        db.collection("userWorkouts")
                .document(userWorkoutId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    if (callback != null) callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onFailure(e.getMessage());
                });
    }
    public void updateCustomWorkout(Workout workout, FirestoreCallback<Void> callback) {
        if (workout.getId() == null) {
            if (callback != null) callback.onFailure("Workout ID is null");
            return;
        }

        workout.setLastModified(System.currentTimeMillis());

        db.collection("custom_workouts")
                .document(workout.getId())
                .set(workout)
                .addOnSuccessListener(aVoid -> {
                    if (callback != null) callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onFailure(e.getMessage());
                });
    }

    public void deleteCustomWorkout(String workoutId, FirestoreCallback<Void> callback) {
        db.collection("custom_workouts")
                .document(workoutId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    if (callback != null) callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onFailure(e.getMessage());
                });
    }

    // ========== USER'S ADDED WORKOUTS (FROM PLAN) ==========



    public void addWorkoutToUserPlan(UserWorkout userWorkout, FirestoreCallback<Void> callback) {
        if (userWorkout == null) {
            callback.onFailure("UserWorkout is null");
            return;
        }

        Log.d("FirebaseHelper", "Adding UserWorkout to Firestore");
        Log.d("FirebaseHelper", "WorkoutId: " + userWorkout.getWorkoutId());
        Log.d("FirebaseHelper", "isAdded: " + userWorkout.isAdded());
        Log.d("FirebaseHelper", "Days: " + userWorkout.getDaysOfWeek());

        userWorkout.setLastUpdated(System.currentTimeMillis());

        // Generate ID if not set
        if (userWorkout.getId() == null) {
            String id = db.collection("userWorkouts").document().getId();
            userWorkout.setId(id);
            Log.d("FirebaseHelper", "Generated new ID: " + id);
        }

        // Prepare data map
        Map<String, Object> data = new HashMap<>();
        data.put("userId", userWorkout.getUserId());
        data.put("workoutId", userWorkout.getWorkoutId());
        data.put("isCustom", userWorkout.isCustom());
        data.put("isAdded", userWorkout.isAdded());  // This should be true
        data.put("daysOfWeek", userWorkout.getDaysOfWeek());
        data.put("completedByDay", userWorkout.getCompletedByDay());
        data.put("completedDateByDay", userWorkout.getCompletedDateByDay());
        data.put("exerciseProgressByDay", userWorkout.getExerciseProgressByDay());
        data.put("currentProgress", userWorkout.getCurrentProgress());
        data.put("lastUpdated", userWorkout.getLastUpdated());

        // Also add legacy fields for backward compatibility
        data.put("isCompleted", false);
        data.put("completedDate", 0);

        Log.d("FirebaseHelper", "Data to save: " + data);

        db.collection("userWorkouts")
                .document(userWorkout.getId())
                .set(data)
                .addOnSuccessListener(aVoid -> {
                    Log.d("FirebaseHelper", "Successfully saved UserWorkout");
                    if (callback != null) callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    Log.e("FirebaseHelper", "Error saving UserWorkout: " + e.getMessage());
                    if (callback != null) callback.onFailure(e.getMessage());
                });
    }
    public void updateUser(User user, FirestoreCallback<Void> callback) {
        if (user.getId() == null) {
            if (callback != null) callback.onFailure("User ID is null");
            return;
        }

        Map<String, Object> userData = new HashMap<>();
        userData.put("name", user.getName());
        userData.put("email", user.getEmail());
        userData.put("theme", user.getTheme());
        userData.put("fontSize", user.getFontSize());
        userData.put("twoFactorEnabled", user.isTwoFactorEnabled());
        userData.put("twoFactorMethod", user.getTwoFactorMethod());
        userData.put("phoneNumber", user.getPhoneNumber());
        userData.put("phoneVerified", user.isPhoneVerified());
        userData.put("secretKey", user.getSecretKey());
        userData.put("updatedAt", System.currentTimeMillis());

        db.collection("users")
                .document(user.getId())
                .update(userData)
                .addOnSuccessListener(aVoid -> {
                    if (callback != null) callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onFailure(e.getMessage());
                });
    }
    public void saveUserBodyInfo(UserBodyInfo bodyInfo, FirestoreCallback<Void> callback) {
        if (bodyInfo == null || TextUtils.isEmpty(bodyInfo.getUserId())) {
            callback.onFailure("Invalid user info");
            return;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("userId", bodyInfo.getUserId());
        data.put("birthday", bodyInfo.getBirthday());
        data.put("heightCm", bodyInfo.getHeightCm());
        data.put("weightKg", bodyInfo.getWeightKg());
        data.put("gender", bodyInfo.getGender());
        data.put("bmi", bodyInfo.getBmi());
        data.put("updatedAt", bodyInfo.getUpdatedAt());

        db.collection("userBodyInfo")
                .document(bodyInfo.getUserId())
                .set(data)
                .addOnSuccessListener(aVoid -> {
                    if (callback != null) callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onFailure(e.getMessage());
                });
    }
    public void updateUserTheme(String userId, String theme, FirestoreCallback<Void> callback) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("theme", theme);
        updates.put("updatedAt", System.currentTimeMillis());

        db.collection("users")
                .document(userId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    if (callback != null) callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onFailure(e.getMessage());
                });
    }

    public void updateUserFontSize(String userId, String fontSize, FirestoreCallback<Void> callback) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("fontSize", fontSize);
        updates.put("updatedAt", System.currentTimeMillis());

        db.collection("users")
                .document(userId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    if (callback != null) callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onFailure(e.getMessage());
                });
    }




    public void removeWorkoutFromUserPlan(String userWorkoutId, FirestoreCallback<Void> callback) {
        if (TextUtils.isEmpty(userWorkoutId)) {
            if (callback != null) callback.onFailure("UserWorkout ID is empty");
            return;
        }

        Log.d("FirebaseHelper", "Removing user workout with ID: " + userWorkoutId);

        db.collection("userWorkouts")
                .document(userWorkoutId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d("FirebaseHelper", "Successfully deleted user workout: " + userWorkoutId);
                    if (callback != null) callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    Log.e("FirebaseHelper", "Failed to delete: " + e.getMessage());
                    if (callback != null) callback.onFailure(e.getMessage());
                });
    }


    public interface FirestoreCallback<T> {
        void onSuccess(T result);
        void onFailure(String error);
    }

    public interface AuthCallback {
        void onSuccess(FirebaseUser user);
        void onFailure(String error);
    }
    // ========== DEFAULT EXERCISES METHODS ==========

    public void getDefaultExercises(FirestoreCallback<List<Exercise>> callback) {
        db.collection("default_exercises")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Exercise> exercises = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Exercise exercise = doc.toObject(Exercise.class);
                        if (exercise != null) {
                            exercise.setId(doc.getId());
                            exercises.add(exercise);
                        }
                    }
                    callback.onSuccess(exercises);
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

// ========== USER EXERCISES METHODS ==========
// Add to FirebaseHelper.java
public void getUserBodyInfo(String userId, FirestoreCallback<UserBodyInfo> callback) {
    if (TextUtils.isEmpty(userId)) {
        callback.onFailure("User ID is empty");
        return;
    }

    db.collection("userBodyInfo")
            .document(userId)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    UserBodyInfo bodyInfo = documentSnapshot.toObject(UserBodyInfo.class);
                    callback.onSuccess(bodyInfo);
                } else {
                    callback.onSuccess(null);
                }
            })
            .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
}

    public void updateUserBodyInfo(UserBodyInfo bodyInfo, FirestoreCallback<Void> callback) {
        if (bodyInfo == null || TextUtils.isEmpty(bodyInfo.getUserId())) {
            callback.onFailure("Invalid body info");
            return;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("userId", bodyInfo.getUserId());
        data.put("birthday", bodyInfo.getBirthday());
        data.put("heightCm", bodyInfo.getHeightCm());
        data.put("weightKg", bodyInfo.getWeightKg());
        data.put("gender", bodyInfo.getGender());
        data.put("bmi", bodyInfo.getBmi());
        data.put("updatedAt", bodyInfo.getUpdatedAt());

        db.collection("userBodyInfo")
                .document(bodyInfo.getUserId())
                .set(data)
                .addOnSuccessListener(aVoid -> {
                    if (callback != null) callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onFailure(e.getMessage());
                });
    }
    public void getUserExercises(String userId, FirestoreCallback<List<Exercise>> callback) {
        db.collection("users")
                .document(userId)
                .collection("exercises")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Exercise> exercises = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Exercise exercise = doc.toObject(Exercise.class);
                        if (exercise != null) {
                            exercise.setId(doc.getId());
                            exercises.add(exercise);
                        }
                    }
                    callback.onSuccess(exercises);
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void updateUserWorkoutProgress(UserWorkout userWorkout, FirestoreCallback<Void> callback) {
        if (userWorkout == null || TextUtils.isEmpty(userWorkout.getId())) {
            callback.onFailure("Invalid user workout");
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("currentProgress", userWorkout.getCurrentProgress());
        updates.put("lastUpdated", userWorkout.getLastUpdated());
        updates.put("completedByDay", userWorkout.getCompletedByDay());
        updates.put("completedDateByDay", userWorkout.getCompletedDateByDay());
        updates.put("exerciseProgressByDay", userWorkout.getExerciseProgressByDay());


        // Also update the overall completed flag if needed
        boolean allDaysCompleted = checkAllDaysCompleted(userWorkout);
        updates.put("isCompleted", allDaysCompleted);

        db.collection("userWorkouts")
                .document(userWorkout.getId())
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    if (callback != null) callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onFailure(e.getMessage());
                });
    }
    public void getUserAddedWorkouts(String userId, FirestoreCallback<List<UserWorkout>> callback) {
        if (TextUtils.isEmpty(userId)) {
            callback.onFailure("User ID is empty");
            return;
        }

        Log.d("FirebaseHelper", "Getting user added workouts for userId: " + userId);

        db.collection("userWorkouts")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<UserWorkout> userWorkouts = new ArrayList<>();
                    Log.d("FirebaseHelper", "Found " + queryDocumentSnapshots.size() + " userWorkout documents");

                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        UserWorkout userWorkout = doc.toObject(UserWorkout.class);
                        if (userWorkout != null) {
                            userWorkout.setId(doc.getId());

                            // Log what we found
                            Log.d("FirebaseHelper", "UserWorkout: " + userWorkout.getWorkoutId() +
                                    " - isAdded: " + userWorkout.isAdded() +
                                    " - days: " + userWorkout.getDaysOfWeek());

                            // Ensure maps are initialized if null
                            if (userWorkout.getCompletedByDay() == null) {
                                userWorkout.setCompletedByDay(new HashMap<>());
                            }
                            if (userWorkout.getCompletedDateByDay() == null) {
                                userWorkout.setCompletedDateByDay(new HashMap<>());
                            }
                            if (userWorkout.getExerciseProgressByDay() == null) {
                                userWorkout.setExerciseProgressByDay(new HashMap<>());
                            }

                            userWorkouts.add(userWorkout);
                        }
                    }
                    callback.onSuccess(userWorkouts);
                })
                .addOnFailureListener(e -> {
                    Log.e("FirebaseHelper", "Error getting user workouts: " + e.getMessage());
                    callback.onFailure(e.getMessage());
                });
    }
    private boolean checkAllDaysCompleted(UserWorkout userWorkout) {
        if (userWorkout.getDaysOfWeek() == null || userWorkout.getCompletedByDay() == null) {
            return false;
        }
        for (String day : userWorkout.getDaysOfWeek()) {
            if (!userWorkout.isCompletedForDay(day)) {
                return false;
            }
        }
        return true;
    }

// ========== DEFAULT EQUIPMENT METHODS ==========

    public void getDefaultEquipment(FirestoreCallback<List<Equipment>> callback) {
        db.collection("default_equipment")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Equipment> equipmentList = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Equipment equipment = doc.toObject(Equipment.class);
                        if (equipment != null) {
                            equipment.setId(doc.getId());
                            equipmentList.add(equipment);
                        }
                    }
                    callback.onSuccess(equipmentList);
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

// ========== USER EQUIPMENT METHODS ==========

    public void getUserEquipment(String userId, FirestoreCallback<List<Equipment>> callback) {
        db.collection("users")
                .document(userId)
                .collection("equipment")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Equipment> equipmentList = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Equipment equipment = doc.toObject(Equipment.class);
                        if (equipment != null) {
                            equipment.setId(doc.getId());
                            equipmentList.add(equipment);
                        }
                    }
                    callback.onSuccess(equipmentList);
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void saveUserEquipment(String userId, Equipment equipment, FirestoreCallback<String> callback) {
        equipment.setCustom(true);
        String id = db.collection("users")
                .document(userId)
                .collection("equipment")
                .document().getId();
        equipment.setId(id);

        db.collection("users")
                .document(userId)
                .collection("equipment")
                .document(id)
                .set(equipment)
                .addOnSuccessListener(aVoid -> {
                    if (callback != null) callback.onSuccess(id);
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onFailure(e.getMessage());
                });
    }
    public void updateUserStatsWithTransaction(String userId, int pointsToAdd, FirestoreCallback<Void> callback) {
        db.runTransaction(transaction -> {
            DocumentReference userRef = db.collection("users").document(userId);
            DocumentSnapshot snapshot = transaction.get(userRef);

            long currentPoints = snapshot.getLong("totalPoints") != null ? snapshot.getLong("totalPoints") : 0;
            long currentStreak = snapshot.getLong("workoutStreak") != null ? snapshot.getLong("workoutStreak") : 0;

            transaction.update(userRef, "totalPoints", currentPoints + pointsToAdd);
            transaction.update(userRef, "workoutStreak", currentStreak + 1);
            transaction.update(userRef, "lastWorkoutDate", System.currentTimeMillis());

            return null;
        }).addOnSuccessListener(aVoid -> {
            if (callback != null) callback.onSuccess(null);
        }).addOnFailureListener(e -> {
            if (callback != null) callback.onFailure(e.getMessage());
        });
    }

}