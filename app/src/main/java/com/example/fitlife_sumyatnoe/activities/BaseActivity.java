package com.example.fitlife_sumyatnoe.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;

import com.example.fitlife_sumyatnoe.FitLifeApplication;
import com.example.fitlife_sumyatnoe.R;
import com.example.fitlife_sumyatnoe.models.Workout;
import com.example.fitlife_sumyatnoe.repository.WorkoutRepository;
import com.example.fitlife_sumyatnoe.utils.ConfettiHelper;
import com.example.fitlife_sumyatnoe.utils.FirebaseHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;

public class BaseActivity extends AppCompatActivity {

    private AlertDialog progressDialog;
    private View loadingOverlay;
    private ConfettiHelper confettiHelper;
    private ViewGroup confettiContainer;
    protected Toolbar toolbar;
    protected FirebaseHelper firebaseHelper;
    private static final int CAMERA_PERMISSION_CODE = 100;

    private static boolean sGuestMode = false;
    @Override
    protected void onStart() {
        super.onStart();
        // Try to sync pending workouts when app opens
        if (isNetworkAvailable()) {
            WorkoutRepository repository = WorkoutRepository.getInstance(this);
            repository.syncPendingWorkouts(null);
        }
    }
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        applyTheme();
        applyFontSizeGlobal();
        super.onCreate(savedInstanceState);
        confettiHelper = ConfettiHelper.getInstance();
        firebaseHelper = new FirebaseHelper();
        registerNetworkCallback();
    }

    @Override
    protected void onResume() {
        super.onResume();
        applyFontSizeGlobal();
    }

    protected boolean isGuest() {
        if (sGuestMode) {
            return true;
        }
        // Fallback to SharedPreferences
        return getPrefs().getBoolean("isGuest", false);
    }

    protected void setGuestMode(boolean isGuest) {
        sGuestMode = isGuest;
        getPrefs().edit().putBoolean("isGuest", isGuest).apply();
    }

    protected void clearGuestMode() {
        sGuestMode = false;
        getPrefs().edit().remove("isGuest").apply();
        getPrefs().edit().remove("isLoggedIn").apply();
    }
    private void registerNetworkCallback() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkRequest networkRequest = new NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                .build();

        cm.registerNetworkCallback(networkRequest, new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(@NonNull Network network) {
                // Network is available - sync pending operations
                WorkoutRepository.getInstance(BaseActivity.this).syncPendingOperations();
            }
        });
    }
    protected boolean areGesturesEnabled() {
        return getPrefs().getBoolean("gestures_enabled", true);
    }
    protected boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return false;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            android.net.Network network = cm.getActiveNetwork();
            if (network == null) return false;

            android.net.NetworkCapabilities capabilities = cm.getNetworkCapabilities(network);
            return capabilities != null &&
                    (capabilities.hasTransport(android.net.NetworkCapabilities.TRANSPORT_WIFI) ||
                            capabilities.hasTransport(android.net.NetworkCapabilities.TRANSPORT_CELLULAR) ||
                            capabilities.hasTransport(android.net.NetworkCapabilities.TRANSPORT_ETHERNET));
        } else {
            android.net.NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        }
    }


    protected void requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(new String[]{
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.CAMERA
            }, 100);
        } else {
            requestPermissions(new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA
            }, 100);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            for (int i = 0; i < permissions.length; i++) {
                Log.d("Permission", permissions[i] + (grantResults[i] == PackageManager.PERMISSION_GRANTED ? " granted" : " denied"));
            }
        }
    }

    protected boolean isLoggedIn() {
        return getPrefs().getBoolean("isLoggedIn", false);
    }

    protected String getUserId() {
        if (isGuest()) {
            return getPrefs().getString("userId", "guest_" + System.currentTimeMillis());
        }
        return getPrefs().getString("userId", "");
    }

    protected boolean canEditWorkout(Workout workout) {
        return !isGuest() && !workout.isDefault();
    }

    protected boolean canDeleteWorkout(Workout workout) {
        return !isGuest() && !workout.isDefault();
    }

    protected void showGuestRestriction(String action) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Sign Up Required")
                .setMessage("You need to sign up to " + action + ". Would you like to sign up now?")
                .setPositiveButton("Sign Up", (dialog, which) -> {
                    Intent intent = new Intent(this, RegisterActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Continue as Guest", null)
                .show();
    }

    protected void showGuestPageRestriction() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Sign Up Required")
                .setMessage("This feature is only available for registered users. Sign up to unlock all features and track your fitness journey!")
                .setPositiveButton("Sign Up", (dialog, which) -> {
                    Intent intent = new Intent(this, RegisterActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Continue as Guest", (dialog, which) -> {
                    dialog.dismiss();
                })
                .show();
    }

    // ========== THEME METHODS ==========
    public void loadUserPreferences(String userId) {
        String theme = getPrefs().getString("theme", "light");
        String fontSize = getPrefs().getString("fontSize", "Medium");
        applyTheme();
        applyFontSizeGlobal();
    }

    public void changeTheme(String theme) {
        getPrefs().edit().putString("theme", theme).apply();
        applyTheme();
        recreate();
    }

    protected void applyTheme() {
        String theme = getPrefs().getString("theme", "system");
        switch (theme) {
            case "light":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case "dark":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            default:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
        }
    }

    // ========== FONT SIZE METHODS ==========
    private float getFontScale(String fontSize) {
        switch (fontSize) {
            case "Small": return 0.85f;
            case "Large": return 1.15f;
            case "Extra Large": return 1.3f;
            default: return 1.0f;
        }
    }

    public void applyFontSizeGlobal() {
        String fontSize = getPrefs().getString("fontSize", "Medium");
        float scale = getFontScale(fontSize);
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration config = res.getConfiguration();
        config.fontScale = scale;
        res.updateConfiguration(config, dm);
    }

    public void changeFontSize(String fontSize) {
        getPrefs().edit().putString("fontSize", fontSize).apply();
        applyFontSizeToCurrentActivity();
        showToast("Font size changed to " + fontSize);
        if (!isGuest() && getUserId() != null && !getUserId().isEmpty()) {
            firebaseHelper.updateUserFontSize(getUserId(), fontSize,
                    new FirebaseHelper.FirestoreCallback<Void>() {
                        @Override
                        public void onSuccess(Void result) { }
                        @Override
                        public void onFailure(String error) { }
                    });
        }
    }

    private void applyFontSizeToCurrentActivity() {
        String fontSize = getPrefs().getString("fontSize", "Medium");
        float scale = getFontScale(fontSize);
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration config = res.getConfiguration();
        config.fontScale = scale;
        res.updateConfiguration(config, dm);
        recreate();
    }

    // ========== TOOLBAR METHODS ==========
    protected void setupToolbar() {
        setupToolbar(false, null);
    }

    protected void setupToolbar(boolean showBackButton, String title) {
        toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(showBackButton);
                getSupportActionBar().setDisplayShowHomeEnabled(showBackButton);
                if (title != null) {
                    getSupportActionBar().setTitle(title);
                }
            }
            toolbar.setNavigationOnClickListener(v -> onBackPressed());
        }
    }

    protected void setToolbarTitle(String title) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
        } else if (toolbar != null) {
            toolbar.setTitle(title);
        }
    }

    protected void setToolbarSubtitle(String subtitle) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setSubtitle(subtitle);
        } else if (toolbar != null) {
            toolbar.setSubtitle(subtitle);
        }
    }

    protected void setToolbarElevation(boolean elevated) {
        if (toolbar != null) {
            toolbar.setElevation(elevated ? dpToPx(4) : 0);
        }
    }

    protected void hideToolbarBackButton() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }
    }

    protected void showToolbarBackButton() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private float dpToPx(int dp) {
        return dp * getResources().getDisplayMetrics().density;
    }

    protected void setupBottomNavigation(BottomNavigationView bottomNavigation) {
        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.navigation_plan && isGuest()) {
                showGuestPageRestriction();
                return true;
            }
            if (id == R.id.navigation_home && !(this instanceof HomeActivity)) {
                startActivity(new Intent(this, HomeActivity.class));
                finish();
            } else if (id == R.id.navigation_workout && !(this instanceof WorkoutActivity)) {
                startActivity(new Intent(this, WorkoutActivity.class));
                finish();
            } else if (id == R.id.navigation_plan && !(this instanceof MyPlanActivity)) {
                startActivity(new Intent(this, MyPlanActivity.class));
                finish();
            } else if (id == R.id.navigation_profile && !(this instanceof ProfileActivity)) {
                startActivity(new Intent(this, ProfileActivity.class));
                finish();
            }
            return true;
        });
    }

    public void setSelectedNavigationItem(int itemId) {
        BottomNavigationView nav = findViewById(R.id.bottomNavigation);
        if (nav != null) nav.setSelectedItemId(itemId);
    }


    protected void showLoading() {
        if (loadingOverlay == null) {
            loadingOverlay = getLayoutInflater().inflate(R.layout.loading_overlay, null);
            addContentView(loadingOverlay, new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
        }
        loadingOverlay.setVisibility(View.VISIBLE);
    }

    protected void hideLoading() {
        if (loadingOverlay != null) {
            loadingOverlay.setVisibility(View.GONE);
        }
    }

    protected void showSnackbar(String message) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT).show();
    }

    // ========== NAVIGATION ==========
    protected void navigateToWithTransition(Class<?> destination) {
        Intent intent = new Intent(this, destination);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    protected void navigateAndFinish(Class<?> destination) {
        Intent intent = new Intent(this, destination);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        finish();
    }



    protected void navigateToWithTransition(Intent intent) {
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
    protected void finishWithoutTransition() {
        super.finish();
        overridePendingTransition(0, 0);
    }


    protected void showConfirmDialog(String title, String message, Runnable onConfirm) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Yes", (dialog, which) -> onConfirm.run())
                .setNegativeButton("No", null)
                .show();
    }

    protected void showInfoDialog(String title, String message) {
        new MaterialAlertDialogBuilder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }

    protected SharedPreferences getPrefs() {
        return getSharedPreferences("FitLifePrefs", MODE_PRIVATE);
    }

    protected void showConfetti() {
        FrameLayout confettiContainer = findViewById(R.id.confettiContainer);
        if (confettiContainer != null) {
            confettiHelper.celebrate(confettiContainer);
        } else {
            showToast("🎉 Congratulations! 🎉");
        }
    }

    protected void stopConfetti() {
        confettiHelper.stop();
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        confettiContainer = findViewById(R.id.confettiContainer);
    }

    private LayoutInflater toastInflater;

    private LayoutInflater getToastInflater() {
        if (toastInflater == null) {
            toastInflater = LayoutInflater.from(this);
        }
        return toastInflater;
    }

    protected void showSuccessToast(String message) {
        View layout = getToastInflater().inflate(R.layout.custom_toast, null);
        layout.setBackgroundResource(R.drawable.toast_background_success);

        ImageView icon = layout.findViewById(R.id.toast_icon);
        TextView text = layout.findViewById(R.id.toast_text);

        icon.setImageResource(R.drawable.ic_check_circle);
        icon.setColorFilter(getColor(R.color.on_primary));
        text.setText(message);

        Toast toast = new Toast(getApplicationContext());
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(layout);
        toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 100);
        toast.show();
    }

    protected void showErrorToast(String message) {
        View layout = getToastInflater().inflate(R.layout.custom_toast, null);
        layout.setBackgroundResource(R.drawable.toast_background_error);

        ImageView icon = layout.findViewById(R.id.toast_icon);
        TextView text = layout.findViewById(R.id.toast_text);

        icon.setImageResource(R.drawable.ic_close);
        icon.setColorFilter(getColor(R.color.on_error));
        text.setText(message);
        text.setTextColor(getColor(R.color.on_error));

        Toast toast = new Toast(getApplicationContext());
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 100);
        toast.show();
    }


    protected void showWarningToast(String message) {
        View layout = getToastInflater().inflate(R.layout.custom_toast, null);
        layout.setBackgroundResource(R.drawable.toast_background_warning);

        ImageView icon = layout.findViewById(R.id.toast_icon);
        TextView text = layout.findViewById(R.id.toast_text);

        icon.setImageResource(R.drawable.ic_warning);
        icon.setColorFilter(getColor(R.color.on_background));
        text.setText(message);
        text.setTextColor(getColor(R.color.on_tertiary));

        Toast toast = new Toast(getApplicationContext());
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(layout);
        toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 100);
        toast.show();
    }

    protected void showInfoToast(String message) {
        View layout = getToastInflater().inflate(R.layout.custom_toast, null);
        layout.setBackgroundResource(R.drawable.toast_background_info);

        ImageView icon = layout.findViewById(R.id.toast_icon);
        TextView text = layout.findViewById(R.id.toast_text);

        icon.setImageResource(R.drawable.ic_info);
        icon.setColorFilter(getColor(R.color.on_tertiary));
        text.setText(message);

        Toast toast = new Toast(getApplicationContext());
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 100);
        toast.show();
    }


    protected void showToast(String message) {
        showInfoToast(message);
    }
    protected boolean shouldShow2FAReminder() {
        if (isGuest()) return false;
        SharedPreferences prefs = getPrefs();
        if (prefs.getBoolean("twoFactorEnabled", false)) return false;
        long lastReminder = prefs.getLong("last2FAReminder", 0);
        int reminderCount = prefs.getInt("twoFAReminderCount", 0);
        if (reminderCount >= 5) return false;
        if (System.currentTimeMillis() - lastReminder < 7 * 24 * 60 * 60 * 1000) return false;
        return true;
    }
    protected <T extends ViewModel> T getViewModel(Class<T> viewModelClass) {
        return new ViewModelProvider(this).get(viewModelClass);
    }

    protected <T extends ViewModel> T getViewModel(Class<T> viewModelClass, ViewModelProvider.Factory factory) {
        return new ViewModelProvider(this, factory).get(viewModelClass);
    }
}