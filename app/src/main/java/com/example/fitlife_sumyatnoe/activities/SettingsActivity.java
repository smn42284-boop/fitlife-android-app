package com.example.fitlife_sumyatnoe.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.fitlife_sumyatnoe.R;
import com.example.fitlife_sumyatnoe.utils.FirebaseHelper;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseUser;

public class SettingsActivity extends BaseActivity {

    private LinearLayout changePasswordBtn, twoFactorBtn, themeBtn, fontSizeBtn;
    private TextView twoFactorStatus, themeValue, fontSizeValue;
    private SwitchMaterial twoFactorSwitch, gestureSwitch;

    private FirebaseHelper firebaseHelper;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        firebaseHelper = new FirebaseHelper();
        currentUser = firebaseHelper.getCurrentUser();

        initViews();
        setupToolbar(true, "Settings");
        setupClickListeners();
        loadPreferences();
        loadTwoFactorStatus();
    }

    private void initViews() {
        changePasswordBtn = findViewById(R.id.changePasswordBtn);
        twoFactorBtn = findViewById(R.id.twoFactorBtn);
        themeBtn = findViewById(R.id.themeBtn);
        fontSizeBtn = findViewById(R.id.fontSizeBtn);
        twoFactorStatus = findViewById(R.id.twoFactorStatus);
        themeValue = findViewById(R.id.themeValue);
        fontSizeValue = findViewById(R.id.fontSizeValue);
        twoFactorSwitch = findViewById(R.id.twoFactorSwitch);
        gestureSwitch = findViewById(R.id.gestureSwitch);
    }

    private void setupClickListeners() {
        changePasswordBtn.setOnClickListener(v -> showChangePasswordDialog());

        twoFactorSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                startActivityForResult(new Intent(this, TwoFactorAuthActivity.class), 100);
            } else {
                showDisable2FADialog();
            }
        });

        themeBtn.setOnClickListener(v -> showThemeDialog());
        fontSizeBtn.setOnClickListener(v -> showFontSizeDialog());

        gestureSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            getPrefs().edit().putBoolean("gestures_enabled", isChecked).apply();
            showInfoToast(isChecked ? "Gestures enabled" : "Gestures disabled");
        });
    }

    private void loadPreferences() {
        String theme = getPrefs().getString("theme", "system");
        String fontSize = getPrefs().getString("fontSize", "Medium");

        if (theme.equals("light")) themeValue.setText("Light");
        else if (theme.equals("dark")) themeValue.setText("Dark");
        else themeValue.setText("System");

        fontSizeValue.setText(fontSize);

        boolean gesturesEnabled = getPrefs().getBoolean("gestures_enabled", true);
        gestureSwitch.setChecked(gesturesEnabled);
    }

    private void loadTwoFactorStatus() {
        if (currentUser != null) {
            firebaseHelper.getUser(currentUser.getUid(), new FirebaseHelper.FirestoreCallback<com.example.fitlife_sumyatnoe.models.User>() {
                @Override
                public void onSuccess(com.example.fitlife_sumyatnoe.models.User user) {
                    if (user != null) {
                        boolean is2FAEnabled = user.isTwoFactorEnabled();
                        twoFactorSwitch.setChecked(is2FAEnabled);
                        twoFactorStatus.setText(is2FAEnabled ? "Enabled" : "Disabled");
                    }
                }

                @Override
                public void onFailure(String error) {
                    twoFactorSwitch.setChecked(false);
                    twoFactorStatus.setText("Disabled");
                }
            });
        }
    }

    private void showDisable2FADialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Disable Two-Factor Authentication")
                .setMessage("Are you sure you want to disable two-factor authentication? This will make your account less secure.")
                .setPositiveButton("Disable", (dialog, which) -> {
                    disableTwoFactor();
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    twoFactorSwitch.setChecked(true);
                })
                .show();
    }

    private void disableTwoFactor() {
        if (currentUser != null) {
            firebaseHelper.getUser(currentUser.getUid(), new FirebaseHelper.FirestoreCallback<com.example.fitlife_sumyatnoe.models.User>() {
                @Override
                public void onSuccess(com.example.fitlife_sumyatnoe.models.User user) {
                    if (user != null) {
                        user.setTwoFactorEnabled(false);
                        user.setTwoFactorMethod("");

                        firebaseHelper.updateUser(user, new FirebaseHelper.FirestoreCallback<Void>() {
                            @Override
                            public void onSuccess(Void result) {
                                twoFactorSwitch.setChecked(false);
                                twoFactorStatus.setText("Disabled");
                                showInfoToast("Two-Factor Authentication disabled");
                            }

                            @Override
                            public void onFailure(String error) {
                                twoFactorSwitch.setChecked(true);
                                showErrorToast("Failed to disable 2FA: " + error);
                            }
                        });
                    }
                }

                @Override
                public void onFailure(String error) {
                    twoFactorSwitch.setChecked(true);
                    showErrorToast("Error: " + error);
                }
            });
        }
    }

    private void showThemeDialog() {
        String[] themes = {"Light", "Dark", "System Default"};
        new MaterialAlertDialogBuilder(this)
                .setTitle("Select Theme")
                .setItems(themes, (dialog, which) -> {
                    String selectedTheme;
                    if (which == 0) selectedTheme = "light";
                    else if (which == 1) selectedTheme = "dark";
                    else selectedTheme = "system";

                    getPrefs().edit().putString("theme", selectedTheme).apply();
                    recreate();
                })
                .show();
    }

    private void showFontSizeDialog() {
        String[] sizes = {"Small", "Medium", "Large", "Extra Large"};
        new MaterialAlertDialogBuilder(this)
                .setTitle("Select Font Size")
                .setItems(sizes, (dialog, which) -> {
                    String selected = sizes[which];
                    changeFontSize(selected);
                    fontSizeValue.setText(selected);
                })
                .show();
    }

    private void showChangePasswordDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Change Password")
                .setMessage("A password reset link will be sent to your email.")
                .setPositiveButton("Send", (dialog, which) -> {
                    firebaseHelper.resetPassword(currentUser.getEmail(),
                            new FirebaseHelper.AuthCallback() {
                                @Override
                                public void onSuccess(FirebaseUser user) {
                                    showToast("Reset email sent");
                                }
                                @Override
                                public void onFailure(String error) {
                                    showToast("Error: " + error);
                                }
                            });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK) {
            loadTwoFactorStatus();
        }
    }
}