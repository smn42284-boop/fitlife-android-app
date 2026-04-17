package com.example.fitlife_sumyatnoe.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.example.fitlife_sumyatnoe.R;
import com.example.fitlife_sumyatnoe.adapters.HomePagerAdapter;
import com.example.fitlife_sumyatnoe.fragments.HealthFragment;
import com.example.fitlife_sumyatnoe.fragments.TodayFragment;
import com.example.fitlife_sumyatnoe.models.User;
import com.example.fitlife_sumyatnoe.utils.FirebaseHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class HomeActivity extends BaseActivity {

    private ShapeableImageView profileImage;
    private TextView welcomeText, dateText;
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private BottomNavigationView bottomNavigation;
    private HomePagerAdapter pagerAdapter;

    private FirebaseHelper firebaseHelper;
    private FirebaseUser currentUser;

    private TodayFragment todayFragment;
    private HealthFragment healthFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        firebaseHelper = new FirebaseHelper();
        currentUser = firebaseHelper.getCurrentUser();

        initViews();
        setupToolbar();
        setupViewPager();
        setupBottomNavigation();
        loadUserData();
        checkAndShow2FAReminder();
        setSelectedNavigationItem(R.id.navigation_home);

        if (isGuest()) {
            boolean hasSeenGuestDialog = getPrefs().getBoolean("hasSeenGuestDialog", false);
            if (!hasSeenGuestDialog) {
                getPrefs().edit().putBoolean("hasSeenGuestDialog", true).apply();
                showGuestFeaturesDialog();
            }
        }
    }

    private void initViews() {
        profileImage = findViewById(R.id.profileImage);
        welcomeText = findViewById(R.id.welcomeText);
        dateText = findViewById(R.id.dateText);
        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);
        bottomNavigation = findViewById(R.id.bottomNavigation);

        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMMM d", Locale.getDefault());
        dateText.setText(dateFormat.format(new Date()));
    }

    private void setupViewPager() {
        pagerAdapter = new HomePagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);

        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> {
                    if (position == 0) {
                        tab.setText("Today");
                    } else {
                        tab.setText("Health");
                    }
                }
        ).attach();

        viewPager.post(() -> {
            todayFragment = (TodayFragment) pagerAdapter.getFragmentAtPosition(0);
            healthFragment = (HealthFragment) pagerAdapter.getFragmentAtPosition(1);
        });
    }

    private void setupBottomNavigation() {
        setupBottomNavigation(bottomNavigation);
    }
    private void checkAndShow2FAReminder() {
        if (isGuest()) return;
        String userId = getUserId();
        if (userId == null || userId.isEmpty()) {
            Log.d("HomeActivity", "UserId is empty, skipping 2FA check");
            return;
        }

        firebaseHelper.getUser(getUserId(), new FirebaseHelper.FirestoreCallback<User>() {
            @Override
            public void onSuccess(User user) {
                if (user != null && !user.isTwoFactorEnabled()) {
                    // Check if we should show the reminder (3rd login or after 7 days)
                    boolean shouldShow = shouldShow2FAReminder();
                    if (shouldShow) {
                        show2FAReminderDialog();
                    }
                }
            }

            @Override
            public void onFailure(String error) { }
        });
    }

    private void show2FAReminderDialog() {
        getPrefs().edit()
                .putLong("last2FAReminder", System.currentTimeMillis())
                .putInt("twoFAReminderCount", getPrefs().getInt("twoFAReminderCount", 0) + 1)
                .apply();

        new MaterialAlertDialogBuilder(this)
                .setTitle("🔒 Protect Your Account")
                .setMessage("Enable two-factor authentication to add an extra layer of security to your account. It only takes a minute!")
                .setPositiveButton("Enable Now", (dialog, which) -> {
                    Intent intent = new Intent(HomeActivity.this, TwoFactorAuthActivity.class);
                    startActivity(intent);
                })
                .setNegativeButton("Remind Later", null)
                .setNeutralButton("Don't Show Again", (dialog, which) -> {
                    getPrefs().edit().putInt("twoFAReminderCount", 5).apply();
                })
                .show();
    }


    private void loadUserData() {
        if (isGuest()) {
            welcomeText.setText("Hello, Guest!");
            profileImage.setImageResource(R.drawable.ic_profile_placeholder);
            return;
        }

        if (currentUser != null) {
            String name = currentUser.getDisplayName();
            welcomeText.setText("Hello, " + (name != null ? name.split(" ")[0] : "User") + "!");
            if (currentUser.getPhotoUrl() != null) {
                Glide.with(this).load(currentUser.getPhotoUrl()).circleCrop().into(profileImage);
            }
        }
    }

    private void showGuestFeaturesDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_guest_features, null);

        MaterialButton btnSignUp = dialogView.findViewById(R.id.btnSignUp);
        MaterialButton btnContinueAsGuest = dialogView.findViewById(R.id.btnContinueAsGuest);

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this, R.style.MaterialDialog);
        builder.setView(dialogView);
        builder.setCancelable(false);

        AlertDialog dialog = builder.create();
        dialog.show();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            dialog.getWindow().setLayout(
                    (int) (getResources().getDisplayMetrics().widthPixels * 0.85),
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }

        btnSignUp.setOnClickListener(v -> {
            dialog.dismiss();
            Intent intent = new Intent(HomeActivity.this, RegisterActivity.class);
            startActivity(intent);
            finish();
        });

        btnContinueAsGuest.setOnClickListener(v -> {
            dialog.dismiss();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserData();
        refreshFragments();
    }

    private void refreshFragments() {
        if (todayFragment != null) {
            todayFragment.refreshData();
        }
        if (healthFragment != null) {
            healthFragment.refreshData();
        }
    }
}