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

import com.bumptech.glide.Glide;
import com.example.fitlife_sumyatnoe.R;
import com.example.fitlife_sumyatnoe.utils.FirebaseHelper;
import com.example.fitlife_sumyatnoe.utils.ImagePickerHelper;
import com.example.fitlife_sumyatnoe.utils.LocalImageHelper;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import java.util.HashMap;
import java.util.Map;

public class EditProfileActivity extends BaseActivity {

    private ImageView profileImage;
    private EditText nameInput, emailInput, phoneInput;
    private MaterialButton saveBtn, cancelBtn, changePhotoBtn;

    private FirebaseHelper firebaseHelper;
    private FirebaseUser currentUser;
    private LocalImageHelper localImageHelper;
    private ImagePickerHelper imagePickerHelper;
    private Uri selectedImageUri;
    private static final int PICK_IMAGE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        firebaseHelper = new FirebaseHelper();
        localImageHelper = new LocalImageHelper(this);
        currentUser = firebaseHelper.getCurrentUser();

        if (isGuest()) {
            showWarningToast("Please sign up to edit profile");
            finish();
            return;
        }

        initViews();
        setupToolbar(); // Use BaseActivity's setupToolbar
        setToolbarTitle("Edit Profile");
        setToolbarElevation(false);
        showToolbarBackButton();
        setupImagePicker();
        loadUserData();
        setupClickListeners();
    }

    private void initViews() {
        profileImage = findViewById(R.id.profileImage);
        nameInput = findViewById(R.id.nameInput);
        emailInput = findViewById(R.id.emailInput);
        phoneInput = findViewById(R.id.phoneInput);
        saveBtn = findViewById(R.id.saveBtn);
        cancelBtn = findViewById(R.id.cancelBtn);
        changePhotoBtn = findViewById(R.id.changePhotoBtn);
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

    private void loadUserData() {
        if (currentUser != null) {
            String name = currentUser.getDisplayName();
            String email = currentUser.getEmail();

            if (name != null && !name.isEmpty()) {
                nameInput.setText(name);
            }
            if (email != null && !email.isEmpty()) {
                emailInput.setText(email);
            }

            loadProfileImage();
        }
    }

    private void loadProfileImage() {
        if (currentUser != null) {
            // Try to load from local storage first
            String imagePath = localImageHelper.getProfileImagePath(currentUser.getUid());
            if (imagePath != null && !imagePath.isEmpty()) {
                java.io.File imageFile = new java.io.File(imagePath);
                if (imageFile.exists()) {
                    Glide.with(this)
                            .load(imageFile)
                            .circleCrop()
                            .placeholder(R.drawable.ic_profile_placeholder)
                            .error(R.drawable.ic_profile_placeholder)
                            .into(profileImage);
                    return;
                }
            }

            // Fallback to Firebase photo URL
            if (currentUser.getPhotoUrl() != null) {
                Glide.with(this)
                        .load(currentUser.getPhotoUrl())
                        .circleCrop()
                        .placeholder(R.drawable.ic_profile_placeholder)
                        .error(R.drawable.ic_profile_placeholder)
                        .into(profileImage);
            } else {
                profileImage.setImageResource(R.drawable.ic_profile_placeholder);
            }
        }
    }

    private void setupClickListeners() {
        saveBtn.setOnClickListener(v -> saveProfile());
        cancelBtn.setOnClickListener(v -> finish());

        if (changePhotoBtn != null) {
            changePhotoBtn.setOnClickListener(v -> openImagePicker());
        }

        profileImage.setOnClickListener(v -> openImagePicker());
    }

    private void openImagePicker() {
        if (imagePickerHelper != null) {
            imagePickerHelper.showImagePickerDialog();
        }
    }

    private void saveProfile() {

        String name = nameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String phone = phoneInput != null ? phoneInput.getText().toString().trim() : "";

        if (TextUtils.isEmpty(name)) {
            nameInput.setError("Name is required");
            nameInput.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(email)) {
            emailInput.setError("Email is required");
            emailInput.requestFocus();
            return;
        }


        showLoading();
        if (!isNetworkAvailable()) {
            saveProfileLocally(name, email, phone);
            return;
        }

        FirebaseUser user = currentUser;
        if (user != null) {
            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setDisplayName(name)
                    .build();

            user.updateProfile(profileUpdates)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Save profile image locally if selected
                            if (selectedImageUri != null) {
                                String imagePath = localImageHelper.saveProfileImage(selectedImageUri, user.getUid());
                                if (imagePath != null) {
                                    // Update user's profile image path in Firestore
                                    updateUserInFirestore(user.getUid(), name, email, phone, imagePath);
                                } else {
                                    updateUserInFirestore(user.getUid(), name, email, phone, null);
                                }
                            } else {
                                // Get existing image path
                                String existingImagePath = localImageHelper.getProfileImagePath(user.getUid());
                                updateUserInFirestore(user.getUid(), name, email, phone, existingImagePath);
                            }

                            // Check if email was changed
                            if (!email.equals(user.getEmail())) {
                                // Update email (requires verification)
                                user.verifyBeforeUpdateEmail(email)
                                        .addOnCompleteListener(emailTask -> {
                                            if (emailTask.isSuccessful()) {
                                                showInfoToast("Verification email sent to new address");
                                            } else {
                                                showErrorToast("Failed to update email: " + emailTask.getException().getMessage());
                                            }
                                        });
                            }
                        } else {
                            hideLoading();
                            showErrorToast("Failed to update profile: " + task.getException().getMessage());
                        }
                    });
        }
    }

    private void updateUserInFirestore(String userId, String name, String email, String phone, String imagePath) {
        // Get current preferences from SharedPreferences
        String theme = getPrefs().getString("theme", "light");
        String fontSize = getPrefs().getString("fontSize", "Medium");

        firebaseHelper.updateUserProfile(userId, name, email, phone, theme, fontSize, imagePath,
                new FirebaseHelper.FirestoreCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        hideLoading();
                        showSuccessToast("Profile updated successfully!");

                        // Update local SharedPreferences
                        getPrefs().edit()
                                .putString("userName", name)
                                .putString("userEmail", email)
                                .apply();

                        finish();
                    }

                    @Override
                    public void onFailure(String error) {
                        hideLoading();
                        showSuccessToast("Error: " + error);
                    }
                });
    }
    private void saveProfileLocally(String name, String email, String phone) {
        getPrefs().edit()
                .putString("userName", name)
                .putString("userEmail", email)
                .putString("userPhone", phone)
                .putBoolean("needsProfileSync", true)
                .apply();

        hideLoading();
        showSuccessToast("Profile saved locally! Will sync when online.");
        finish();
    }
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (imagePickerHelper != null) {
            imagePickerHelper.handleActivityResult(requestCode, resultCode, data);
        }
    }
}