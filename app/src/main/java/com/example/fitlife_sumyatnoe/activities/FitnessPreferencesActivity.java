package com.example.fitlife_sumyatnoe.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitlife_sumyatnoe.R;
import com.example.fitlife_sumyatnoe.adapters.GoalChipAdapter;
import com.example.fitlife_sumyatnoe.adapters.WorkoutTypeAdapter;
import com.example.fitlife_sumyatnoe.models.UserPreferences;
import com.example.fitlife_sumyatnoe.utils.FirebaseHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.slider.Slider;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.ArrayList;
import java.util.List;

public class FitnessPreferencesActivity extends BaseActivity {

    private RecyclerView goalsRecycler, workoutTypesRecycler;
    private TextView selectedGoalsCount, selectedWorkoutsCount;
    private Slider experienceSlider, frequencySlider;
    private TextView experienceValue, frequencyValue;
    private SwitchMaterial nutritionSwitch;
    private MaterialButton nextBtn, backBtn;
    private MaterialCardView activityCard;

    private GoalChipAdapter goalsAdapter;
    private WorkoutTypeAdapter workoutTypeAdapter;
    private List<String> selectedGoals = new ArrayList<>();
    private List<String> selectedWorkoutTypes = new ArrayList<>();
    private int experienceLevel = 1;
    private int workoutFrequency = 3;
    private boolean nutritionRecommendations = true;
    private String activityLevel = "Moderately Active";

    private FirebaseHelper firebaseHelper;

    private final String[] fitnessGoals = {
            "Weight Loss", "Muscle Gain", "Build Endurance",
            "Get Stronger", "Improve Flexibility", "Overall Health"
    };

    private final String[] workoutTypes = {
            "Strength Training", "Cardio", "HIIT", "Yoga",
            "Pilates", "Calisthenics", "Dance", "Walking"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fitness_preferences);
        firebaseHelper = new FirebaseHelper();
        initViews();
        setupToolbar(false
                , "Fitness Preferences");
        setupRecyclers();
        setupClickListeners();
        setupSliders();
        getPrefs().edit().putBoolean("hasPreferences", true).apply();
    }

    private void initViews() {
        goalsRecycler = findViewById(R.id.goalsRecycler);
        workoutTypesRecycler = findViewById(R.id.workoutTypesRecycler);
        selectedGoalsCount = findViewById(R.id.selectedGoalsCount);
        selectedWorkoutsCount = findViewById(R.id.selectedWorkoutsCount);
        experienceSlider = findViewById(R.id.experienceSlider);
        frequencySlider = findViewById(R.id.frequencySlider);
        experienceValue = findViewById(R.id.experienceValue);
        frequencyValue = findViewById(R.id.frequencyValue);
        nutritionSwitch = findViewById(R.id.nutritionSwitch);
        nextBtn = findViewById(R.id.nextBtn);
        backBtn = findViewById(R.id.backBtn);
        activityCard = findViewById(R.id.activityCard);
    }


    private void setupRecyclers() {
        // Goals Recycler
        goalsAdapter = new GoalChipAdapter(fitnessGoals, selectedGoals, new GoalChipAdapter.OnGoalSelectedListener() {
            @Override
            public void onGoalSelected(String goal, boolean isSelected) {
                if (isSelected) {
                    if (!selectedGoals.contains(goal)) {
                        selectedGoals.add(goal);
                    }
                } else {
                    selectedGoals.remove(goal);
                }
                updateSelectedCounts();
            }
        });

        LinearLayoutManager goalsLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        goalsRecycler.setLayoutManager(goalsLayoutManager);
        goalsRecycler.setAdapter(goalsAdapter);

        workoutTypeAdapter = new WorkoutTypeAdapter(workoutTypes, selectedWorkoutTypes, new WorkoutTypeAdapter.OnWorkoutSelectedListener() {
            @Override
            public void onWorkoutSelected(String workout, boolean isSelected) {
                if (isSelected) {
                    if (!selectedWorkoutTypes.contains(workout)) {
                        selectedWorkoutTypes.add(workout);
                    }
                } else {
                    selectedWorkoutTypes.remove(workout);
                }
                updateSelectedCounts();
            }
        });

        LinearLayoutManager workoutLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        workoutTypesRecycler.setLayoutManager(workoutLayoutManager);
        workoutTypesRecycler.setAdapter(workoutTypeAdapter);
    }
    private void setupSliders() {
        experienceSlider.addOnChangeListener((slider, value, fromUser) -> {
            experienceLevel = Math.round(value);
            String experienceText;
            switch (experienceLevel) {
                case 1:
                    experienceText = "Beginner";
                    break;
                case 2:
                    experienceText = "Intermediate";
                    break;
                default:
                    experienceText = "Advanced";
                    break;
            }
            experienceValue.setText(experienceText);
        });

        frequencySlider.addOnChangeListener((slider, value, fromUser) -> {
            workoutFrequency = Math.round(value);
            frequencyValue.setText(workoutFrequency + " days/week");
        });

        activityCard.setOnClickListener(v -> showActivityLevelDialog());

        nutritionSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            nutritionRecommendations = isChecked;
        });
    }

    private void showActivityLevelDialog() {
        String[] levels = {"Sedentary", "Lightly Active", "Moderately Active", "Very Active", "Extremely Active"};

        new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                .setTitle("Activity Level")
                .setItems(levels, (dialog, which) -> {
                    activityLevel = levels[which];
                    TextView activityText = findViewById(R.id.activityLevelText);
                    if (activityText != null) {
                        activityText.setText(activityLevel);
                    }
                })
                .show();
    }

    private void updateSelectedCounts() {
        selectedGoalsCount.setText(selectedGoals.size() + " selected");
        selectedWorkoutsCount.setText(selectedWorkoutTypes.size() + " selected");
    }

    private void setupClickListeners() {
        nextBtn.setOnClickListener(v -> savePreferences());
        backBtn.setOnClickListener(v -> finish());
    }

    private void savePreferences() {
        if (selectedGoals.isEmpty()) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Selection Required")
                    .setMessage("Please select at least one fitness goal to continue.")
                    .setPositiveButton("OK", null)
                    .show();
            return;
        }

        if (selectedWorkoutTypes.isEmpty()) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Selection Required")
                    .setMessage("Please select at least one workout type you enjoy.")
                    .setPositiveButton("OK", null)
                    .show();
            return;
        }

        showLoading();

        float bmr = calculateBMR();
        float dailyCalories = calculateDailyCalories(bmr);

        // Create preferences object
        UserPreferences preferences = new UserPreferences();
        preferences.setUserId(getUserId());
        preferences.setFitnessGoals(selectedGoals);
        preferences.setPreferredWorkouts(selectedWorkoutTypes);
        preferences.setExperienceLevel(getExperienceString());
        preferences.setWorkoutFrequency(workoutFrequency);
        preferences.setActivityLevel(activityLevel);
        preferences.setNutritionRecommendations(nutritionRecommendations);
        preferences.setBmr(bmr);
        preferences.setDailyCalorieRecommendation(dailyCalories);
        preferences.setUpdatedAt(System.currentTimeMillis());

        // Save to Firestore
        firebaseHelper.saveUserPreferences(preferences, new FirebaseHelper.FirestoreCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                hideLoading();
                showSuccessToast("Preferences saved!");

                getPrefs().edit()
                        .putBoolean("hasCompletedOnboarding", true)
                        .putInt("workoutFrequency", workoutFrequency)
                        .putString("experienceLevel", getExperienceString())
                        .apply();

                Intent intent = new Intent(FitnessPreferencesActivity.this, HomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }

            @Override
            public void onFailure(String error) {
                hideLoading();
                showErrorToast("Error: " + error);
            }
        });
    }

    private float calculateBMR() {
        // Get user info from preferences (you'd need to pass this from BodyInfoActivity)
        float height = getPrefs().getFloat("user_height", 170);
        float weight = getPrefs().getFloat("user_weight", 70);
        int age = getPrefs().getInt("user_age", 25);
        String gender = getPrefs().getString("user_gender", "Male");

        // Mifflin-St Jeor Equation
        if (gender.equals("Male")) {
            return (float) ((10 * weight) + (6.25 * height) - (5 * age) + 5);
        } else {
            return (float) ((10 * weight) + (6.25 * height) - (5 * age) - 161);
        }
    }

    private float calculateDailyCalories(float bmr) {
        float multiplier;
        switch (activityLevel) {
            case "Sedentary":
                multiplier = 1.2f;
                break;
            case "Lightly Active":
                multiplier = 1.375f;
                break;
            case "Moderately Active":
                multiplier = 1.55f;
                break;
            case "Very Active":
                multiplier = 1.725f;
                break;
            default:
                multiplier = 1.2f;
                break;
        }
        return bmr * multiplier;
    }

    private String getExperienceString() {
        switch (experienceLevel) {
            case 1: return "Beginner";
            case 2: return "Intermediate";
            default: return "Advanced";
        }
    }
}