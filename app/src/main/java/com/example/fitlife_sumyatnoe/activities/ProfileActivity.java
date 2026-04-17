package com.example.fitlife_sumyatnoe.activities;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.example.fitlife_sumyatnoe.R;
import com.example.fitlife_sumyatnoe.models.User;
import com.example.fitlife_sumyatnoe.models.UserBMIHistory;
import com.example.fitlife_sumyatnoe.models.UserBodyInfo;
import com.example.fitlife_sumyatnoe.utils.FirebaseHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ProfileActivity extends BaseActivity {

    private ImageView profileImage;
    private TextView userName, userEmail;
    private TextView bmiValue, bmiStatus, bmiDescription;
    private TextView calorieValue, calorieDescription;
    private TextView tipText;
    private LinearLayout editProfileBtn;
    private MaterialButton logoutBtn;
    private BottomNavigationView bottomNavigation;
    private MaterialCardView bmiCard, bmiHistoryCard;
    private TextView editBodyInfoBtn;
    private TextView viewHistoryBtn;
    private TextView lastBMIText, currentBmiPreview;

    private FirebaseHelper firebaseHelper;
    private FirebaseUser currentUser;
    private User user;
    private UserBodyInfo userBodyInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        firebaseHelper = new FirebaseHelper();
        currentUser = firebaseHelper.getCurrentUser();

        initViews();
        setupToolbar(false,"My Profile");
        setupBottomNavigation();
        setupClickListeners();
        loadUserData();
        loadBodyInfo();
        loadCalorieRecommendation();
        loadTipOfTheDay();

        setSelectedNavigationItem(R.id.navigation_profile);
    }

    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        profileImage = findViewById(R.id.profileImage);
        userName = findViewById(R.id.userName);
        userEmail = findViewById(R.id.userEmail);

        editProfileBtn = findViewById(R.id.editProfileBtn);
        logoutBtn = findViewById(R.id.logoutBtn);
        bottomNavigation = findViewById(R.id.bottomNavigation);

        // BMI Views
        bmiCard = findViewById(R.id.bmiCard);
        bmiValue = findViewById(R.id.bmiValue);
        bmiStatus = findViewById(R.id.bmiStatus);
        bmiDescription = findViewById(R.id.bmiDescription);
        editBodyInfoBtn = findViewById(R.id.editBodyInfoBtn);

        // Calorie Views
        calorieValue = findViewById(R.id.calorieValue);
        calorieDescription = findViewById(R.id.calorieDescription);

        // BMI History Views
        bmiHistoryCard = findViewById(R.id.bmiHistoryCard);
        viewHistoryBtn = findViewById(R.id.viewHistoryBtn);
        lastBMIText = findViewById(R.id.lastBMIText);
        currentBmiPreview = findViewById(R.id.currentBmiPreview);

        // Tip Views
        tipText = findViewById(R.id.tipText);
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.profile_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupBottomNavigation() {
        setupBottomNavigation(bottomNavigation);
    }

    private void setupClickListeners() {
        editProfileBtn.setOnClickListener(v -> {
            if (isGuest()) {
                showGuestRestriction("edit profile");
            } else {
                startActivity(new Intent(this, EditProfileActivity.class));
            }
        });

        if (bmiCard != null) {
            bmiCard.setOnClickListener(v -> {
                if (!isGuest()) {
                    navigateToEditBodyInfo();
                }
            });
        }

        if (editBodyInfoBtn != null) {
            editBodyInfoBtn.setOnClickListener(v -> navigateToEditBodyInfo());
        }

        if (viewHistoryBtn != null) {
            viewHistoryBtn.setOnClickListener(v -> showBMIHistoryDialog());
        }

        if (bmiHistoryCard != null) {
            bmiHistoryCard.setOnClickListener(v -> showBMIHistoryDialog());
        }

        logoutBtn.setOnClickListener(v -> {
            if (isGuest()) {
                showGuestLogoutDialog();
            } else {
                showLogoutDialog();
            }
        });
    }

    private void loadUserData() {
        if (isGuest()) {
            userName.setText("Guest User");
            userEmail.setText("guest@fitlife.com");
            profileImage.setImageResource(R.drawable.ic_profile_placeholder);
            return;
        }

        if (currentUser != null) {
            String name = currentUser.getDisplayName();
            userName.setText(name != null ? name : "User");
            userEmail.setText(currentUser.getEmail());

            if (currentUser.getPhotoUrl() != null) {
                Glide.with(this).load(currentUser.getPhotoUrl()).circleCrop().into(profileImage);
            }
        }
    }

    private void loadBodyInfo() {
        if (isGuest()) {
            if (bmiValue != null) bmiValue.setText("--");
            if (bmiStatus != null) bmiStatus.setText("Not available");
            if (bmiDescription != null) bmiDescription.setText("Sign up to track your body metrics");
            return;
        }

        firebaseHelper.getUserBodyInfo(getUserId(), new FirebaseHelper.FirestoreCallback<UserBodyInfo>() {
            @Override
            public void onSuccess(UserBodyInfo bodyInfo) {
                userBodyInfo = bodyInfo;
                if (bodyInfo != null) {
                    displayBodyInfo(bodyInfo);
                    setupBMIHistory();
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
        float bmi = bodyInfo.getBmi();
        if (bmiValue != null) {
            bmiValue.setText(String.format("%.1f", bmi));
        }

        String status;
        String description;
        int color;

        if (bmi < 18.5) {
            status = "Underweight";
            description = "Focus on nutrient-rich foods and strength training to build muscle mass.";
            color = getColor(R.color.info);
        } else if (bmi < 25) {
            status = "Normal";
            description = "Great! You're in the healthy range. Keep up the balanced lifestyle.";
            color = getColor(R.color.success);
        } else if (bmi < 30) {
            status = "Overweight";
            description = "Cardio workouts and a balanced diet can help you reach your goals.";
            color = getColor(R.color.warning);
        } else {
            status = "Obese";
            description = "Start with low-impact exercises. Consult a professional for guidance.";
            color = getColor(R.color.error);
        }

        if (bmiStatus != null) {
            bmiStatus.setText(status);
            bmiStatus.setTextColor(color);
        }
        if (bmiDescription != null) {
            bmiDescription.setText(description);
        }
    }

    private void setupBMIHistory() {
        if (userBodyInfo != null) {
            // Update current BMI preview
            if (currentBmiPreview != null) {
                currentBmiPreview.setText(String.format("%.1f", userBodyInfo.getBmi()));
            }

            // Update last recorded date
            if (lastBMIText != null) {
                String lastDate = getFormattedDate(userBodyInfo.getUpdatedAt());
                lastBMIText.setText("Last updated: " + lastDate);
            }
        } else {
            if (currentBmiPreview != null) {
                currentBmiPreview.setText("--");
            }
            if (lastBMIText != null) {
                lastBMIText.setText("No data recorded yet");
            }
        }
    }

    private String getFormattedDate(long timestamp) {
        if (timestamp == 0) return "Never";
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    private void showBMIHistoryDialog() {
        if (userBodyInfo == null) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("BMI History")
                    .setMessage("No BMI data available yet. Update your body information to start tracking!")
                    .setPositiveButton("Update Now", (d, w) -> navigateToEditBodyInfo())
                    .setNegativeButton("Cancel", null)
                    .show();
            return;
        }

        // Show loading
        showLoading();

        // Fetch BMI history from Firestore
        firebaseHelper.getBMIHistory(getUserId(), new FirebaseHelper.FirestoreCallback<List<UserBMIHistory>>() {
            @Override
            public void onSuccess(List<UserBMIHistory> historyList) {
                hideLoading();
                showBMIHistoryDialogWithData(historyList);
            }

            @Override
            public void onFailure(String error) {
                hideLoading();
                showErrorToast("Error loading history: " + error);
                // Show dialog with just current data
                showBMIHistoryDialogWithData(new ArrayList<>());
            }
        });
    }

    private void showBMIHistoryDialogWithData(List<UserBMIHistory> historyList) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_bmi_history, null);

        TextView currentBmi = dialogView.findViewById(R.id.currentBmi);
        TextView currentStatus = dialogView.findViewById(R.id.currentStatus);
        TextView lastUpdated = dialogView.findViewById(R.id.lastUpdated);
        LinearLayout historyContainer = dialogView.findViewById(R.id.historyContainer);

        // Set current data
        if (userBodyInfo != null) {
            currentBmi.setText(String.format("%.1f", userBodyInfo.getBmi()));
            currentStatus.setText(userBodyInfo.getBmiStatus());
            lastUpdated.setText(getFormattedDate(userBodyInfo.getUpdatedAt()));
        }

        historyContainer.removeAllViews();

        if (historyList == null || historyList.isEmpty()) {
            TextView emptyText = new TextView(this);
            emptyText.setText("No BMI history yet.\nUpdate your body info to start tracking!");
            emptyText.setTextSize(14);
            emptyText.setTextColor(getColor(R.color.text_secondary));
            emptyText.setPadding(0, 32, 0, 32);
            emptyText.setGravity(android.view.Gravity.CENTER);
            historyContainer.addView(emptyText);
        } else {
            for (UserBMIHistory history : historyList) {
                View itemView = getLayoutInflater().inflate(R.layout.item_bmi_history, historyContainer, false);
                TextView dateText = itemView.findViewById(R.id.dateText);
                TextView bmiValueText = itemView.findViewById(R.id.bmiValueText);
                TextView statusText = itemView.findViewById(R.id.statusText);

                dateText.setText(history.getFormattedDate());
                bmiValueText.setText(String.format("%.1f", history.getBmi()));
                statusText.setText(history.getStatus());

                historyContainer.addView(itemView);
            }
        }

        new MaterialAlertDialogBuilder(this)
                .setTitle("BMI History")
                .setView(dialogView)
                .setPositiveButton("Update Body Info", (d, w) -> navigateToEditBodyInfo())
                .setNegativeButton("Close", null)
                .show();
    }


    private void showNoBodyInfoState() {
        if (bmiValue != null) bmiValue.setText("--");
        if (bmiStatus != null) bmiStatus.setText("Not set");
        if (bmiDescription != null) bmiDescription.setText("Add your body information");
        if (currentBmiPreview != null) currentBmiPreview.setText("--");
        if (lastBMIText != null) lastBMIText.setText("No data recorded");
    }

    private void loadCalorieRecommendation() {
        float height = getPrefs().getFloat("user_height", 0);
        float weight = getPrefs().getFloat("user_weight", 0);
        int age = getPrefs().getInt("user_age", 25);
        String gender = getPrefs().getString("user_gender", "Male");

        if (height > 0 && weight > 0) {
            float bmr;
            if (gender.equals("Male")) {
                bmr = (10 * weight) + (6.25f * height) - (5 * age) + 5;
            } else {
                bmr = (10 * weight) + (6.25f * height) - (5 * age) - 161;
            }

            float dailyCalories = bmr * 1.55f;

            if (calorieValue != null) {
                calorieValue.setText(Math.round(dailyCalories) + "");
            }
            if (calorieDescription != null) {
                calorieDescription.setText("kcal per day • Based on your profile");
            }
        } else {
            if (calorieValue != null) calorieValue.setText("--");
            if (calorieDescription != null) calorieDescription.setText("Complete your profile to see recommendations");
        }
    }

    private void loadTipOfTheDay() {
        String[] tips = {
                "💧 Drink water before meals to feel fuller and eat less.",
                "🏃‍♂️ Short workouts are better than no workouts. Even 10 minutes counts!",
                "😴 Sleep is crucial for muscle recovery. Aim for 7-9 hours.",
                "📱 Track your progress to stay motivated. Small wins add up!",
                "🍎 Eat protein within 30 minutes after workout for better recovery.",
                "🧘 Stretch after every workout to prevent injury and improve flexibility.",
                "📅 Consistency beats intensity. Show up every day, even if it's light.",
                "🥗 Meal prep on Sundays to stay on track during busy weekdays."
        };

        int randomIndex = (int) (System.currentTimeMillis() / 86400000) % tips.length;
        if (tipText != null) {
            tipText.setText(tips[randomIndex]);
        }
    }

    private void navigateToEditBodyInfo() {
        Intent intent = new Intent(ProfileActivity.this, BodyInfoActivity.class);
        intent.putExtra("edit_mode", true);
        startActivity(intent);
    }

    private void showGuestLogoutDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Exit Guest Mode")
                .setMessage("Are you sure you want to exit guest mode?")
                .setPositiveButton("Exit", (dialog, which) -> {
                    clearGuestMode();
                    getPrefs().edit().clear().apply();
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showLogoutDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    firebaseHelper.logout();
                    getPrefs().edit().clear().apply();
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("No", null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserData();
        loadBodyInfo();
        loadCalorieRecommendation();
        loadTipOfTheDay();
    }
}