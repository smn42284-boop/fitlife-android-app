package com.example.fitlife_sumyatnoe.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;

import com.bumptech.glide.Glide;
import com.example.fitlife_sumyatnoe.R;
import com.example.fitlife_sumyatnoe.models.User;
import com.example.fitlife_sumyatnoe.utils.FirebaseHelper;
import com.example.fitlife_sumyatnoe.utils.ImagePickerHelper;
import com.example.fitlife_sumyatnoe.utils.LocalImageHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends BaseActivity {

    private ImageView profileImage;
    private EditText nameInput, emailInput, passwordInput, confirmPasswordInput;
    private MaterialButton signupBtn;
    private TextView loginLink;
    private ImageView cameraIcon;
    private TextInputLayout nameLayout, emailLayout, passwordLayout, confirmPasswordLayout;
    private FirebaseHelper firebaseHelper;
    private LocalImageHelper localImageHelper;
    private Uri selectedImageUri;
    private ImagePickerHelper imagePickerHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registeractivity);

        firebaseHelper = new FirebaseHelper();
        localImageHelper = new LocalImageHelper(this);

        initViews();
        setupToolbar(true, "Register");
        showToolbarBackButton();
        setupImagePicker();
        setupClickListeners();
    }

    private void initViews() {
        profileImage = findViewById(R.id.profileImage);
        nameInput = findViewById(R.id.nameInput);
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        confirmPasswordInput = findViewById(R.id.confirmPasswordInput);
        signupBtn = findViewById(R.id.signupBtn);
        loginLink = findViewById(R.id.loginLink);
        cameraIcon = findViewById(R.id.cameraIcon);
        nameLayout = findViewById(R.id.nameLayout);
        emailLayout = findViewById(R.id.emailLayout);
        passwordLayout = findViewById(R.id.passwordLayout);
        confirmPasswordLayout = findViewById(R.id.confirmPasswordLayout);
    }

    private void setupImagePicker() {
        imagePickerHelper = new ImagePickerHelper(this, new ImagePickerHelper.ImagePickerCallback() {
            @Override
            public void onImageSelected(Uri imageUri) {
                selectedImageUri = imageUri;
                displaySelectedImage();
                showInfoToast("Image selected");
            }

            @Override
            public void onError(String error) {
                showErrorToast("Error: " + error);
            }
        });
    }

    private void displaySelectedImage() {
        if (selectedImageUri != null && profileImage != null) {
            Glide.with(this)
                    .load(selectedImageUri)
                    .circleCrop()
                    .placeholder(R.drawable.ic_profile_placeholder)
                    .error(R.drawable.ic_profile_placeholder)
                    .into(profileImage);
        }
    }

    private void setupClickListeners() {
        signupBtn.setOnClickListener(v -> registerUser());

        loginLink.setOnClickListener(v -> {
            navigateAndFinish(LoginActivity.class);
        });

        if (cameraIcon != null) {
            cameraIcon.setOnClickListener(v -> {
                if (imagePickerHelper != null) {
                    imagePickerHelper.showImagePickerDialog();
                }
            });
        }
    }

    private void registerUser() {
        String name = nameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String confirmPassword = confirmPasswordInput.getText().toString().trim();
        nameLayout.setError(null);
        emailLayout.setError(null);
        passwordLayout.setError(null);
        confirmPasswordLayout.setError(null);

        if (TextUtils.isEmpty(name)) {
            nameLayout.setError("Name is required");
            return;
        }
        if (TextUtils.isEmpty(email)) {
            emailLayout.setError("Email is required");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            passwordLayout.setError("Password is required");
            return;
        }
        if (password.length() < 6) {
            passwordLayout.setError("Password must be at least 6 characters");
            return;
        }
        if (!password.equals(confirmPassword)) {
            confirmPasswordInput.setError("Passwords do not match");
            return;
        }

        showLoading();

        firebaseHelper.registerUser(email, password, name, new FirebaseHelper.AuthCallback() {
            @Override
            public void onSuccess(FirebaseUser user) {
                if (user != null) {
                    User newUser = new User(name, email);
                    newUser.setId(user.getUid());

                    if (selectedImageUri != null) {
                        String imagePath = localImageHelper.saveProfileImage(selectedImageUri, user.getUid());
                        if (imagePath != null) {
                            newUser.setProfileImagePath(imagePath);
                        }
                    }

                    firebaseHelper.saveUser(newUser, new FirebaseHelper.FirestoreCallback<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                            getPrefs().edit()
                                    .clear()
                                    .putBoolean("isLoggedIn", true)
                                    .putBoolean("isGuest", false)
                                    .putString("userId", user.getUid())
                                    .putString("userName", name)
                                    .putString("userEmail", email)
                                    .apply();

                            hideLoading();
                            showSuccessToast("Registration successful!");
                            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();                        }

                        @Override
                        public void onFailure(String error) {
                            hideLoading();
                            showErrorToast("Error saving profile: " + error);
                        }
                    });
                }
            }

            @Override
            public void onFailure(String error) {
                hideLoading();
                showErrorToast("Registration failed: " + error);
            }
        });
    }

    private void applyDefaultThemeAndFont() {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        applyFontSizeGlobal();
    }
}