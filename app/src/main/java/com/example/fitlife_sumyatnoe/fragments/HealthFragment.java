package com.example.fitlife_sumyatnoe.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.fitlife_sumyatnoe.R;
import com.example.fitlife_sumyatnoe.activities.BodyInfoActivity;
import com.example.fitlife_sumyatnoe.activities.WorkoutDetailActivity;
import com.example.fitlife_sumyatnoe.models.UserBodyInfo;
import com.example.fitlife_sumyatnoe.models.Workout;
import com.example.fitlife_sumyatnoe.utils.FirebaseHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

public class HealthFragment extends Fragment {

    private TextView heightValue, weightValue, bmiValue, bmiStatus, bmiDescription, calorieValue;
    private TextView editBodyInfoBtn;
    private MaterialCardView bodyStatusCard, recommendedCard, noRecommendationsCard;
    private TextView recommendedName, recommendedDetails, recommendedReason;
    private ShapeableImageView recommendedImage;
    private MaterialButton addBodyInfoBtn;

    private FirebaseHelper firebaseHelper;
    private FirebaseUser currentUser;
    private UserBodyInfo userBodyInfo;
    private List<Workout> allWorkouts = new ArrayList<>();
    private Workout recommendedWorkout;
    private String userId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_health, container, false);

        firebaseHelper = new FirebaseHelper();
        if (getActivity() != null) {
            currentUser = firebaseHelper.getCurrentUser();
            if (currentUser != null) {
                userId = currentUser.getUid();
            }
        }

        initViews(view);
        setupClickListeners();
        loadBodyInfo();
        loadWorkouts();

        return view;
    }

    private void initViews(View view) {
        // Body Metrics Views
        bodyStatusCard = view.findViewById(R.id.bodyStatusCard);
        heightValue = view.findViewById(R.id.heightValue);
        weightValue = view.findViewById(R.id.weightValue);
        bmiValue = view.findViewById(R.id.bmiValue);
        bmiStatus = view.findViewById(R.id.bmiStatus);
        bmiDescription = view.findViewById(R.id.bmiDescription);
        calorieValue = view.findViewById(R.id.calorieValue);
        editBodyInfoBtn = view.findViewById(R.id.editBodyInfoBtn);

        // Recommendation Views
        recommendedCard = view.findViewById(R.id.recommendedCard);
        noRecommendationsCard = view.findViewById(R.id.noRecommendationsCard);
        recommendedName = view.findViewById(R.id.recommendedName);
        recommendedDetails = view.findViewById(R.id.recommendedDetails);
        recommendedReason = view.findViewById(R.id.recommendedReason);
        recommendedImage = view.findViewById(R.id.recommendedImage);
        addBodyInfoBtn = view.findViewById(R.id.addBodyInfoBtn);
    }

    private void setupClickListeners() {
        if (bodyStatusCard != null) {
            bodyStatusCard.setOnClickListener(v -> navigateToBodyInfo());
        }

        if (editBodyInfoBtn != null) {
            editBodyInfoBtn.setOnClickListener(v -> navigateToBodyInfo());
        }

        if (addBodyInfoBtn != null) {
            addBodyInfoBtn.setOnClickListener(v -> navigateToBodyInfo());
        }

        if (recommendedCard != null) {
            recommendedCard.setOnClickListener(v -> {
                if (recommendedWorkout != null && getActivity() != null) {
                    Intent intent = new Intent(getActivity(), WorkoutDetailActivity.class);
                    intent.putExtra("workout_id", recommendedWorkout.getId());
                    startActivity(intent);
                }
            });
        }
    }

    private void loadBodyInfo() {
        if (getActivity() == null) return;

        if (isGuest()) {
            showGuestBodyInfoState();
            return;
        }

        firebaseHelper.getUserBodyInfo(userId, new FirebaseHelper.FirestoreCallback<UserBodyInfo>() {
            @Override
            public void onSuccess(UserBodyInfo bodyInfo) {
                userBodyInfo = bodyInfo;
                if (bodyInfo != null) {
                    displayBodyInfo(bodyInfo);
                    showRecommendation(bodyInfo);
                } else {
                    showNoBodyInfoState();
                }
            }

            @Override
            public void onFailure(String error) {
                showNoBodyInfoState();
            }
        });
    }

    private void displayBodyInfo(UserBodyInfo bodyInfo) {
        // Display height, weight, BMI
        if (heightValue != null) {
            heightValue.setText(String.format("%.0f", bodyInfo.getHeightCm()));
        }
        if (weightValue != null) {
            weightValue.setText(String.format("%.0f", bodyInfo.getWeightKg()));
        }

        float bmi = bodyInfo.getBmi();
        if (bmiValue != null) {
            bmiValue.setText(String.format("%.1f", bmi));
        }

        String status;
        String description;
        int color;

        if (bmi < 18.5) {
            status = "Underweight";
            description = "Focus on nutrient-rich foods and strength training.";
            color = getResources().getColor(R.color.info);
        } else if (bmi < 25) {
            status = "Normal";
            description = "Great! You're in the healthy range.";
            color = getResources().getColor(R.color.success);
        } else if (bmi < 30) {
            status = "Overweight";
            description = "Cardio workouts can help you reach your goals.";
            color = getResources().getColor(R.color.warning);
        } else {
            status = "Obese";
            description = "Start with low-impact exercises.";
            color = getResources().getColor(R.color.error);
        }

        if (bmiStatus != null) {
            bmiStatus.setText(status);
            bmiStatus.setTextColor(color);
        }
        if (bmiDescription != null) {
            bmiDescription.setText(description);
        }

        // Calculate and display daily calorie recommendation
        calculateAndDisplayCalories(bodyInfo);
    }

    private void calculateAndDisplayCalories(UserBodyInfo bodyInfo) {
        // Mifflin-St Jeor Equation
        float bmr;
        String gender = bodyInfo.getGender();
        float height = bodyInfo.getHeightCm();
        float weight = bodyInfo.getWeightKg();
        int age = bodyInfo.getAge();

        if ("Male".equals(gender)) {
            bmr = (10 * weight) + (6.25f * height) - (5 * age) + 5;
        } else {
            bmr = (10 * weight) + (6.25f * height) - (5 * age) - 161;
        }

        // Assume moderately active for now
        float dailyCalories = bmr * 1.55f;

        if (calorieValue != null) {
            calorieValue.setText(Math.round(dailyCalories) + " kcal/day");
        }
    }

    private void showNoBodyInfoState() {
        if (heightValue != null) heightValue.setText("--");
        if (weightValue != null) weightValue.setText("--");
        if (bmiValue != null) bmiValue.setText("--");
        if (bmiStatus != null) bmiStatus.setText("Not set");
        if (bmiDescription != null) bmiDescription.setText("Add your body information to get personalized recommendations");
        if (calorieValue != null) calorieValue.setText("--");

        if (noRecommendationsCard != null) {
            noRecommendationsCard.setVisibility(View.VISIBLE);
        }
        if (recommendedCard != null) {
            recommendedCard.setVisibility(View.GONE);
        }
    }

    private void showGuestBodyInfoState() {
        if (heightValue != null) heightValue.setText("--");
        if (weightValue != null) weightValue.setText("--");
        if (bmiValue != null) bmiValue.setText("--");
        if (bmiStatus != null) bmiStatus.setText("Guest mode");
        if (bmiDescription != null) bmiDescription.setText("Sign up to track your body metrics");
        if (calorieValue != null) calorieValue.setText("--");

        if (noRecommendationsCard != null) {
            noRecommendationsCard.setVisibility(View.VISIBLE);
        }
        if (recommendedCard != null) {
            recommendedCard.setVisibility(View.GONE);
        }
    }

    private void loadWorkouts() {
        if (isGuest() || getActivity() == null) return;

        firebaseHelper.getWorkoutTemplates(new FirebaseHelper.FirestoreCallback<List<Workout>>() {
            @Override
            public void onSuccess(List<Workout> templateWorkouts) {
                firebaseHelper.getUserCustomWorkouts(userId, new FirebaseHelper.FirestoreCallback<List<Workout>>() {
                    @Override
                    public void onSuccess(List<Workout> customWorkouts) {
                        allWorkouts.clear();
                        allWorkouts.addAll(templateWorkouts);
                        allWorkouts.addAll(customWorkouts);

                        if (userBodyInfo != null) {
                            showRecommendation(userBodyInfo);
                        }
                    }

                    @Override
                    public void onFailure(String error) {
                        Log.e("HealthFragment", "Error loading custom workouts: " + error);
                    }
                });
            }

            @Override
            public void onFailure(String error) {
                Log.e("HealthFragment", "Error loading templates: " + error);
            }
        });
    }

    private void showRecommendation(UserBodyInfo bodyInfo) {
        if (allWorkouts.isEmpty()) {
            if (noRecommendationsCard != null) {
                noRecommendationsCard.setVisibility(View.VISIBLE);
                recommendedCard.setVisibility(View.GONE);
            }
            return;
        }

        float bmi = bodyInfo.getBmi();
        String reason;

        if (bmi < 18.5) {
            reason = "Perfect for building muscle mass 💪";
            recommendedWorkout = findWorkoutByType("strength", "muscle", "build");
        } else if (bmi < 25) {
            reason = "Great for maintaining your healthy lifestyle 🌟";
            recommendedWorkout = findWorkoutByType("full body", "balanced", "core");
        } else if (bmi < 30) {
            reason = "Helps burn calories and improve heart health 🔥";
            recommendedWorkout = findWorkoutByType("cardio", "hiit", "fat burn");
        } else {
            reason = "Low-impact exercises to start your journey 🧘";
            recommendedWorkout = findWorkoutByType("beginner", "low impact", "gentle");
        }

        if (recommendedWorkout != null) {
            showRecommendedCard(recommendedWorkout, reason);
        } else if (!allWorkouts.isEmpty()) {
            recommendedWorkout = allWorkouts.get(0);
            showRecommendedCard(recommendedWorkout, "Recommended for you");
        } else {
            noRecommendationsCard.setVisibility(View.VISIBLE);
            recommendedCard.setVisibility(View.GONE);
        }
    }

    private Workout findWorkoutByType(String... keywords) {
        for (Workout workout : allWorkouts) {
            String name = workout.getName().toLowerCase();
            for (String keyword : keywords) {
                if (name.contains(keyword)) {
                    return workout;
                }
            }
        }
        return null;
    }

    private void showRecommendedCard(Workout workout, String reason) {
        if (recommendedCard == null) return;

        recommendedCard.setVisibility(View.VISIBLE);
        if (noRecommendationsCard != null) {
            noRecommendationsCard.setVisibility(View.GONE);
        }

        if (recommendedName != null) {
            recommendedName.setText(workout.getName());
        }

        int exerciseCount = workout.getExercisesCount();
        if (recommendedDetails != null) {
            recommendedDetails.setText(workout.getDuration() + " min • " + exerciseCount + " exercises");
        }
        if (recommendedReason != null) {
            recommendedReason.setText(reason);
        }

        // Load image
        if (recommendedImage != null) {
            if (workout.getLocalImagePath() != null && !workout.getLocalImagePath().isEmpty()) {
                java.io.File imageFile = new java.io.File(workout.getLocalImagePath());
                if (imageFile.exists()) {
                    Glide.with(this).load(imageFile).centerCrop().into(recommendedImage);
                } else {
                    recommendedImage.setImageResource(R.drawable.workout_placeholder);
                }
            } else {
                recommendedImage.setImageResource(R.drawable.workout_placeholder);
            }
        }
    }

    private void navigateToBodyInfo() {
        if (getActivity() != null) {
            Intent intent = new Intent(getActivity(), BodyInfoActivity.class);
            startActivity(intent);
        }
    }

    private boolean isGuest() {
        if (getActivity() != null) {
            return getActivity().getSharedPreferences("FitLifePrefs", 0).getBoolean("isGuest", false);
        }
        return false;
    }

    private String getUserId() {
        return userId;
    }

    public void refreshData() {
        loadBodyInfo();
        loadWorkouts();
    }
}