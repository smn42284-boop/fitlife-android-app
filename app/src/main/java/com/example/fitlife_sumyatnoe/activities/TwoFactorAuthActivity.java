package com.example.fitlife_sumyatnoe.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.fitlife_sumyatnoe.R;
import com.example.fitlife_sumyatnoe.models.User;
import com.example.fitlife_sumyatnoe.utils.FirebaseHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseUser;

import java.util.Random;

public class TwoFactorAuthActivity extends BaseActivity {

    private LinearLayout setupContainer, verifyContainer;
    private TextInputEditText codeInput;
    private MaterialButton sendCodeBtn, verifyBtn;
    private Button cancelBtn, backBtn;
    private TextView timerText, resendText, demoCodeText;

    private FirebaseHelper firebaseHelper;
    private FirebaseUser currentUser;  
    private User user;
    private String generatedCode;
    private CountDownTimer countDownTimer;
    private boolean isEnabling = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_two_factor_auth);

        firebaseHelper = new FirebaseHelper();
        currentUser = firebaseHelper.getCurrentUser();  // Initialize currentUser

        initViews();
        setupToolbar(true, "Two-Factor Authentication");
        setupClickListeners();

        loadUserData();
    }

    private void initViews() {
        setupContainer = findViewById(R.id.setupContainer);
        verifyContainer = findViewById(R.id.verifyContainer);
        codeInput = findViewById(R.id.codeInput);
        sendCodeBtn = findViewById(R.id.sendCodeBtn);
        verifyBtn = findViewById(R.id.verifyBtn);
        cancelBtn = findViewById(R.id.cancelBtn);
        backBtn = findViewById(R.id.backBtn);
        timerText = findViewById(R.id.timerText);
        resendText = findViewById(R.id.resendText);
        demoCodeText = findViewById(R.id.demoCodeText);
        // Removed enableBtn - not needed
    }

    private void setupClickListeners() {
        sendCodeBtn.setOnClickListener(v -> sendDemoCode());
        verifyBtn.setOnClickListener(v -> verifyCode());
        cancelBtn.setOnClickListener(v -> finish());
        backBtn.setOnClickListener(v -> {
            setupContainer.setVisibility(View.VISIBLE);
            verifyContainer.setVisibility(View.GONE);
            stopTimer();
        });
        resendText.setOnClickListener(v -> sendDemoCode());
    }

    private void loadUserData() {
        if (currentUser != null) {
            firebaseHelper.getUser(currentUser.getUid(), new FirebaseHelper.FirestoreCallback<User>() {
                @Override
                public void onSuccess(User loadedUser) {
                    user = loadedUser;
                    if (user != null && user.isTwoFactorEnabled()) {
                        isEnabling = false;
                        sendCodeBtn.setText("Disable 2FA");
                        setupContainer.setVisibility(View.VISIBLE);
                        verifyContainer.setVisibility(View.GONE);
                        demoCodeText.setVisibility(View.GONE);
                    } else {
                        showDemoMode();
                    }
                }

                @Override
                public void onFailure(String error) {
                    showDemoMode();
                }
            });
        } else {
            showDemoMode();
        }
    }

    private void showDemoMode() {
        demoCodeText.setVisibility(View.VISIBLE);
        demoCodeText.setText("📱 DEMO MODE\n\nFor demonstration purposes, use code: 123456");
    }

    private void sendDemoCode() {
        if (isEnabling) {
            // Generate random 6-digit code for demo
            Random random = new Random();
            generatedCode = String.format("%06d", random.nextInt(999999));

            // Show demo code
            demoCodeText.setVisibility(View.VISIBLE);
            demoCodeText.setText("📱 DEMO CODE\n\nYour verification code is: " + generatedCode + "\n\n(For demo purposes)");

            setupContainer.setVisibility(View.GONE);
            verifyContainer.setVisibility(View.VISIBLE);
            startTimer();
        } else {
            // Disable 2FA
            showDisableConfirmDialog();
        }
    }

    private void verifyCode() {
        String code = codeInput.getText().toString().trim();

        if (code.isEmpty()) {
            codeInput.setError("Enter code");
            return;
        }

        if (code.equals(generatedCode) || code.equals("123456")) {
            if (isEnabling) {
                enableTwoFactor();
            } else {
                disableTwoFactor();
            }
        } else {
            codeInput.setError("Invalid code");
        }
    }

    private void enableTwoFactor() {
        if (user == null) {
            user = new User();
            user.setId(currentUser.getUid());
        }

        user.setTwoFactorEnabled(true);
        user.setTwoFactorMethod("demo");

        firebaseHelper.updateUser(user, new FirebaseHelper.FirestoreCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                showToast("✅ Two-Factor Authentication enabled!");
                showConfetti();
                finish();
            }

            @Override
            public void onFailure(String error) {
                showToast("Error: " + error);
            }
        });
    }

    private void disableTwoFactor() {
        user.setTwoFactorEnabled(false);
        user.setTwoFactorMethod("");

        firebaseHelper.updateUser(user, new FirebaseHelper.FirestoreCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                showToast("Two-Factor Authentication disabled");
                finish();
            }

            @Override
            public void onFailure(String error) {
                showToast("Error: " + error);
            }
        });
    }

    private void showDisableConfirmDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Disable 2FA")
                .setMessage("Are you sure you want to disable two-factor authentication?")
                .setPositiveButton("Disable", (dialog, which) -> {
                    disableTwoFactor();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void startTimer() {
        resendText.setEnabled(false);
        countDownTimer = new CountDownTimer(60000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timerText.setText("Resend in " + millisUntilFinished / 1000 + "s");
            }

            @Override
            public void onFinish() {
                timerText.setText("");
                resendText.setEnabled(true);
            }
        }.start();
    }

    private void stopTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        timerText.setText("");
        resendText.setEnabled(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopTimer();
    }
}