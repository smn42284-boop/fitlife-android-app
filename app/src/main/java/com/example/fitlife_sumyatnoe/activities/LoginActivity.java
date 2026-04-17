package com.example.fitlife_sumyatnoe.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.fitlife_sumyatnoe.R;
import com.example.fitlife_sumyatnoe.models.UserBodyInfo;
import com.example.fitlife_sumyatnoe.utils.FirebaseHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends BaseActivity {

    private EditText emailInput, passwordInput;
    private TextInputLayout emailLayout, passwordLayout;
    private Button loginBtn;
    private TextView registerLink, forgotPasswordLink;

    private FirebaseHelper firebaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loginactivity);

        firebaseHelper = new FirebaseHelper();
        emailLayout = findViewById(R.id.emailLayout);
        passwordLayout = findViewById(R.id.passwordLayout);



        initViews();
        setupToolbar(true, "Login");
        showToolbarBackButton();
        setupClickListeners();
    }

    private void initViews() {
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        loginBtn = findViewById(R.id.loginBtn);
        registerLink = findViewById(R.id.signupLink);
        forgotPasswordLink = findViewById(R.id.forgotPasswordText);
    }

    private void setupClickListeners() {
        loginBtn.setOnClickListener(v -> loginUser());
        registerLink.setOnClickListener(v -> {
            navigateAndFinish(RegisterActivity.class);
        });
        forgotPasswordLink.setOnClickListener(v -> showForgotPasswordDialog());
    }

    private void loginUser() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            emailLayout.setError("Email is required");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            passwordLayout.setError("Password is required");
            return;
        }

        showLoading();

        firebaseHelper.loginUser(email, password, new FirebaseHelper.AuthCallback() {
            @Override
            public void onSuccess(FirebaseUser user) {
                if (user != null) {
                    getPrefs().edit()
                            .putBoolean("isLoggedIn", true)
                            .putBoolean("isGuest", false)
                            .putString("userId", user.getUid())
                            .putString("userEmail", email)
                            .apply();

                    checkFirstTimeUser(user.getUid());
                }
            }

            @Override
            public void onFailure(String error) {
                hideLoading();
                showErrorToast("Login failed: " + error);
            }
        });
    }

    private void checkFirstTimeUser(String userId) {
        boolean hasSeenIntro = getPrefs().getBoolean("hasSeenIntro", false);

        firebaseHelper.getUserBodyInfo(userId, new FirebaseHelper.FirestoreCallback<UserBodyInfo>() {
            @Override
            public void onSuccess(UserBodyInfo bodyInfo) {
                hideLoading();

                if (!hasSeenIntro) {
                    Intent intent = new Intent(LoginActivity.this, IntroActivity.class);
                    startActivity(intent);
                    finish();
                } else if (bodyInfo == null) {
                    Intent intent = new Intent(LoginActivity.this, BodyInfoActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                    startActivity(intent);
                    finish();
                }
            }

            @Override
            public void onFailure(String error) {
                hideLoading();
                // On error, treat as no body info
                if (!hasSeenIntro) {
                    Intent intent = new Intent(LoginActivity.this, IntroActivity.class);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(LoginActivity.this, BodyInfoActivity.class);
                    startActivity(intent);
                }
                finish();
            }
        });
    }

    private void showForgotPasswordDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Reset Password");

        final EditText input = new EditText(this);
        input.setHint("Enter your email");
        input.setInputType(android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        builder.setView(input);

        builder.setPositiveButton("Send", (dialog, which) -> {
            String email = input.getText().toString().trim();
            if (!TextUtils.isEmpty(email)) {
                firebaseHelper.resetPassword(email, new FirebaseHelper.AuthCallback() {
                    @Override
                    public void onSuccess(FirebaseUser user) {
                        showInfoToast("Password reset email sent to " + email);
                    }

                    @Override
                    public void onFailure(String error) {
                        showErrorToast("Error: " + error);
                    }
                });
            } else {
                showWarningToast("Please enter your email");
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void loadUserPreferencesInBackground(String userId) {
        firebaseHelper.getUser(userId, new FirebaseHelper.FirestoreCallback<com.example.fitlife_sumyatnoe.models.User>() {
            @Override
            public void onSuccess(com.example.fitlife_sumyatnoe.models.User userProfile) {
                if (userProfile != null) {
                    getPrefs().edit()
                            .putString("theme", userProfile.getTheme())
                            .putString("fontSize", userProfile.getFontSize())
                            .apply();
                }
            }
            @Override
            public void onFailure(String error) { }
        });
    }
}