package com.example.fitlife_sumyatnoe.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.viewpager2.widget.ViewPager2;

import com.example.fitlife_sumyatnoe.R;
import com.example.fitlife_sumyatnoe.adapters.IntroViewPagerAdapter;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class IntroActivity extends BaseActivity {

    private ViewPager2 viewPager;
    private MaterialButton nextBtn, getStartedBtn;
    private TextView skipBtn;
    private View progress1, progress2, progress3, progress4;

    private IntroViewPagerAdapter adapter;

    private final int[] images = {
            R.drawable.ic_fitness,
            R.drawable.ic_plan,
            R.drawable.ic_checklist,
            R.drawable.ic_shield
    };

    private final String[] titles = {
            "Discover Workouts",
            "Plan Your Week",
            "Share & Prepare",
            "Stay Protected"
    };

    private final String[] descriptions = {
            "Browse hundreds of workouts. From strength to yoga, find what suits you best.",
            "Create your weekly schedule. Add workouts to specific days and never miss a session.",
            "Keep track of equipment you need. Share lists with friends and stay prepared.",
            "Enable two-factor authentication to keep your account and progress safe."
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        boolean hasSeenIntro = getPrefs().getBoolean("hasSeenIntro", false);
        if (hasSeenIntro) {
            navigateToBodyInfo();
            return;
        }

        setContentView(R.layout.activity_intro);

        initViews();
        setupViewPager();
        setupClickListeners();
        getStartedBtn.setOnClickListener(v -> {
            getPrefs().edit().putBoolean("hasSeenIntro", true).apply();

            show2FAEncouragementDialog();
        });
        new Handler().postDelayed(() -> animateCurrentPage(), 300);
    }

    private void initViews() {
        viewPager = findViewById(R.id.viewPager);
        nextBtn = findViewById(R.id.nextBtn);
        getStartedBtn = findViewById(R.id.getStartedBtn);
        skipBtn = findViewById(R.id.skipBtn);

        progress1 = findViewById(R.id.progress1);
        progress2 = findViewById(R.id.progress2);
        progress3 = findViewById(R.id.progress3);
        progress4 = findViewById(R.id.progress4);
    }

    private void setupViewPager() {
        adapter = new IntroViewPagerAdapter(images, titles, descriptions);
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(4);

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updateProgress(position);
                updateButtons(position);
                animateCurrentPage();
            }
        });
    }
    private void show2FAEncouragementDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_2fa_encouragement, null);

        MaterialButton setupNowBtn = dialogView.findViewById(R.id.setupNowBtn);
        MaterialButton laterBtn = dialogView.findViewById(R.id.laterBtn);

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this, R.style.MaterialDialog);
        builder.setView(dialogView);
        builder.setCancelable(false);

        AlertDialog dialog = builder.create();
        dialog.show();

        setupNowBtn.setOnClickListener(v -> {
            dialog.dismiss();
            Intent intent = new Intent(IntroActivity.this, TwoFactorAuthActivity.class);
            startActivity(intent);
        });

        laterBtn.setOnClickListener(v -> {
            dialog.dismiss();
            navigateToBodyInfo();
        });
    }

    private void updateProgress(int position) {
        // Reset all progress bars
        progress1.setBackgroundResource(R.drawable.progress_inactive);
        progress2.setBackgroundResource(R.drawable.progress_inactive);
        progress3.setBackgroundResource(R.drawable.progress_inactive);
        progress4.setBackgroundResource(R.drawable.progress_inactive);

        // Set active for current position
        switch (position) {
            case 0:
                progress1.setBackgroundResource(R.drawable.progress_active);
                break;
            case 1:
                progress2.setBackgroundResource(R.drawable.progress_active);
                break;
            case 2:
                progress3.setBackgroundResource(R.drawable.progress_active);
                break;
            case 3:
                progress4.setBackgroundResource(R.drawable.progress_active);
                break;
        }
    }

    private void updateButtons(int position) {
        if (position == 3) {
            nextBtn.setVisibility(View.GONE);
            getStartedBtn.setVisibility(View.VISIBLE);
            skipBtn.setVisibility(View.GONE);

            getStartedBtn.setAlpha(0f);
            getStartedBtn.animate()
                    .alpha(1f)
                    .setDuration(400)
                    .start();
        } else {
            nextBtn.setVisibility(View.VISIBLE);
            getStartedBtn.setVisibility(View.GONE);
            skipBtn.setVisibility(View.VISIBLE);
        }
    }

    private void animateCurrentPage() {
        View currentPage = viewPager.getChildAt(0);
        if (currentPage != null) {
            TextView titleView = currentPage.findViewById(R.id.titleText);
            TextView descriptionView = currentPage.findViewById(R.id.descriptionText);
            ImageView illustration = currentPage.findViewById(R.id.illustration);

            if (titleView != null) {
                titleView.setAlpha(0f);
                titleView.setTranslationY(20f);
                titleView.animate()
                        .alpha(1f)
                        .translationY(0f)
                        .setDuration(400)
                        .start();
            }

            if (descriptionView != null) {
                descriptionView.setAlpha(0f);
                descriptionView.setTranslationY(20f);
                descriptionView.animate()
                        .alpha(1f)
                        .translationY(0f)
                        .setDuration(500)
                        .start();
            }

            if (illustration != null) {
                illustration.setScaleX(0.9f);
                illustration.setScaleY(0.9f);
                illustration.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(300)
                        .start();
            }
        }
    }

    private void setupClickListeners() {
        nextBtn.setOnClickListener(v -> {
            if (viewPager.getCurrentItem() < 3) {
                viewPager.setCurrentItem(viewPager.getCurrentItem() + 1, true);
            }
        });

        skipBtn.setOnClickListener(v -> {
            getPrefs().edit().putBoolean("hasSeenIntro", true).apply();
            navigateToBodyInfo();
        });

        getStartedBtn.setOnClickListener(v -> {
            getPrefs().edit().putBoolean("hasSeenIntro", true).apply();
            navigateToBodyInfo();
        });
    }

    private void navigateToBodyInfo() {
        Intent intent = new Intent(IntroActivity.this, BodyInfoActivity.class);
        startActivity(intent);
        finish();
    }
}