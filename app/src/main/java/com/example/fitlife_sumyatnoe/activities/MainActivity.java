package com.example.fitlife_sumyatnoe.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.fitlife_sumyatnoe.R;
import com.example.fitlife_sumyatnoe.models.UserBodyInfo;
import com.example.fitlife_sumyatnoe.utils.DataSeeder;

public class MainActivity extends BaseActivity {

    private LinearLayout mainContent;
    private android.widget.ImageView logoImage;
    private Button loginBtn, signupBtn;
    private LinearLayout dividerContainer;
    private TextView guestLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        boolean isLoggedIn = getPrefs().getBoolean("isLoggedIn", false);
        boolean isGuest = getPrefs().getBoolean("isGuest", false);

        Log.d("AppFlow", "MainActivity - isLoggedIn: " + isLoggedIn + ", isGuest: " + isGuest);

        if (isLoggedIn || isGuest) {
            startActivity(new Intent(this, HomeActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        initViews();
        startEntryAnimation();
        setupClickListeners();

        new DataSeeder().updateExistingWorkoutsWithExerciseImages();    }

    private void initViews() {
        mainContent = findViewById(R.id.mainContent);
        logoImage = findViewById(R.id.logoImage);
        loginBtn = findViewById(R.id.loginButton);
        signupBtn = findViewById(R.id.signupButton);
        dividerContainer = findViewById(R.id.dividerContainer);
        guestLink = findViewById(R.id.guestLink);

        // Set initial animation states
        if (mainContent != null) {
            mainContent.setAlpha(0f);
            mainContent.setTranslationY(50f);
        }
        if (logoImage != null) {
            logoImage.setScaleX(0.8f);
            logoImage.setScaleY(0.8f);
            logoImage.setAlpha(0f);
        }
        if (loginBtn != null) {
            loginBtn.setAlpha(0f);
            loginBtn.setTranslationY(20f);
        }
        if (signupBtn != null) {
            signupBtn.setAlpha(0f);
            signupBtn.setTranslationY(20f);
        }
        if (dividerContainer != null) {
            dividerContainer.setAlpha(0f);
            dividerContainer.setTranslationY(20f);
        }
        if (guestLink != null) {
            guestLink.setAlpha(0f);
            guestLink.setTranslationY(20f);
        }
    }

    private void startEntryAnimation() {
        if (mainContent != null) {
            mainContent.animate().alpha(1f).translationY(0f).setDuration(400).setStartDelay(100).start();
        }
        if (logoImage != null) {
            logoImage.animate().scaleX(1f).scaleY(1f).alpha(1f).setDuration(500).setStartDelay(200).setInterpolator(new OvershootInterpolator()).start();
        }
        if (loginBtn != null) {
            loginBtn.animate().alpha(1f).translationY(0f).setDuration(400).setStartDelay(500).start();
        }
        if (signupBtn != null) {
            signupBtn.animate().alpha(1f).translationY(0f).setDuration(400).setStartDelay(600).start();
        }
        if (dividerContainer != null) {
            dividerContainer.animate().alpha(1f).translationY(0f).setDuration(400).setStartDelay(700).start();
        }
        if (guestLink != null) {
            guestLink.animate().alpha(1f).translationY(0f).setDuration(400).setStartDelay(800).start();
        }
    }

    private void setupClickListeners() {
        if (loginBtn != null) {
            loginBtn.setOnClickListener(v -> {
                v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100).withEndAction(() -> {
                    v.animate().scaleX(1f).scaleY(1f).setDuration(100).start();
                    startActivity(new Intent(this, LoginActivity.class));
                }).start();
            });
        }

        if (signupBtn != null) {
            signupBtn.setOnClickListener(v -> {
                v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100).withEndAction(() -> {
                    v.animate().scaleX(1f).scaleY(1f).setDuration(100).start();
                    startActivity(new Intent(this, RegisterActivity.class));
                }).start();
            });
        }

        if (guestLink != null) {
            guestLink.setOnClickListener(v -> {
                v.animate().scaleX(1.05f).scaleY(1.05f).setDuration(150).withEndAction(() -> {
                    v.animate().scaleX(1f).scaleY(1f).setDuration(150).start();
                    continueAsGuest();
                }).start();
            });
        }
    }

    private void continueAsGuest() {
        Log.d("AppFlow", "Continuing as Guest");

        getPrefs().edit()
                .putBoolean("isLoggedIn", true)
                .putBoolean("isGuest", true)
                .putString("userName", "Guest")
                .putString("userEmail", "guest@fitlife.com")
                .putString("userId", "guest_" + System.currentTimeMillis())
                .apply();

        startActivity(new Intent(MainActivity.this, HomeActivity.class));
        finish();
    }
}