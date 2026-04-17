package com.example.fitlife_sumyatnoe.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.example.fitlife_sumyatnoe.R;
import com.example.fitlife_sumyatnoe.models.Exercise;
import com.example.fitlife_sumyatnoe.utils.FirebaseHelper;
import com.example.fitlife_sumyatnoe.utils.ImagePickerHelper;
import com.example.fitlife_sumyatnoe.utils.LocalImageHelper;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.File;

public class CreateExerciseActivity extends BaseActivity {

    private Toolbar toolbar;
    private FrameLayout imagePickerContainer;
    private ImageView exerciseImage;
    private LinearLayout addImageContainer;
    private TextView addImageText;
    private TextInputEditText nameInput, setsInput, repsInput, durationInput, instructionsInput;
    private TextInputLayout nameLayout, setsLayout, repsLayout, durationInputLayout;
    private MaterialButtonToggleGroup exerciseTypeToggle;
    private LinearLayout setsRepsLayout, durationLayout;
    private Button cancelBtn, saveBtn;

    private FirebaseHelper firebaseHelper;
    private Exercise exercise;
    private String workoutId;
    private int exercisePosition = -1;
    private Uri selectedImageUri;
    private LocalImageHelper imageHelper;
    private boolean isEditMode = false;
    private ImagePickerHelper imagePickerHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_exercise);

        firebaseHelper = new FirebaseHelper();
        imageHelper = new LocalImageHelper(this);

        workoutId = getIntent().getStringExtra("workout_id");
        exercisePosition = getIntent().getIntExtra("exercise_position", -1);

        if (getIntent().hasExtra("exercise")) {
            isEditMode = true;
            exercise = (Exercise) getIntent().getSerializableExtra("exercise");
        } else {
            exercise = new Exercise();
        }

        initViews();
        setupToolbar();
        setupImagePicker();
        setupToggleGroup();
        setupClickListeners();

        if (isEditMode) {
            populateData();
        }
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        imagePickerContainer = findViewById(R.id.imagePickerContainer);
        exerciseImage = findViewById(R.id.exerciseImage);
        addImageContainer = findViewById(R.id.addImageContainer);
        addImageText = findViewById(R.id.addImageText);
        nameInput = findViewById(R.id.nameInput);
        nameLayout = findViewById(R.id.nameLayout);
        setsInput = findViewById(R.id.setsInput);
        setsLayout = findViewById(R.id.setsLayout);
        repsInput = findViewById(R.id.repsInput);
        repsLayout = findViewById(R.id.repsLayout);
        durationInput = findViewById(R.id.durationInput);
        durationInputLayout = findViewById(R.id.durationInputLayout);
        instructionsInput = findViewById(R.id.instructionsInput);
        exerciseTypeToggle = findViewById(R.id.exerciseTypeToggle);
        setsRepsLayout = findViewById(R.id.setsRepsLayout);
        durationLayout = findViewById(R.id.durationLayout);
        cancelBtn = findViewById(R.id.cancelBtn);
        saveBtn = findViewById(R.id.saveBtn);
    }

    private void setupImagePicker() {
        // Create ImagePickerHelper with AppCompatActivity context
        imagePickerHelper = new ImagePickerHelper(this, new ImagePickerHelper.ImagePickerCallback() {
            @Override
            public void onImageSelected(Uri imageUri) {
                selectedImageUri = imageUri;
                displaySelectedImage();
            }

            @Override
            public void onError(String error) {
                showErrorToast("Error: " + error);
            }
        });

        // Set click listener for image picker
        if (imagePickerContainer != null) {
            imagePickerContainer.setOnClickListener(v -> {
                if (imagePickerHelper != null) {
                    imagePickerHelper.showImagePickerDialog();
                }
            });
        }
    }
    // In BaseActivity.java - Add this method for page restrictions
    protected void showGuestPageRestriction() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Sign Up Required")
                .setMessage("This feature is only available for registered users. Sign up to unlock all features and track your fitness journey!")
                .setPositiveButton("Sign Up", (dialog, which) -> {
                    Intent intent = new Intent(this, RegisterActivity.class);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Continue as Guest", (dialog, which) -> {
                    // Go back to Home
                    Intent intent = new Intent(this, HomeActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                })
                .show();
    }
    private void displaySelectedImage() {
        if (selectedImageUri != null) {
            exerciseImage.setVisibility(View.VISIBLE);
            if (addImageContainer != null) addImageContainer.setVisibility(View.GONE);
            if (addImageText != null) addImageText.setVisibility(View.GONE);

            imagePickerHelper.displayImage(selectedImageUri, exerciseImage);
        }
    }

    private void setupToggleGroup() {
        exerciseTypeToggle.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.setsRepsType) {
                    setsRepsLayout.setVisibility(View.VISIBLE);
                    durationLayout.setVisibility(View.GONE);
                } else {
                    setsRepsLayout.setVisibility(View.GONE);
                    durationLayout.setVisibility(View.VISIBLE);
                }
            }
        });
        exerciseTypeToggle.check(R.id.setsRepsType);
    }

    private void setupClickListeners() {
        cancelBtn.setOnClickListener(v -> finish());
        saveBtn.setOnClickListener(v -> validateAndSaveExercise());
    }

    private void populateData() {
        if (exercise.getName() != null) {
            nameInput.setText(exercise.getName());
        }

        if (exercise.isTimed()) {
            exerciseTypeToggle.check(R.id.durationType);
            if (exercise.getDuration() > 0) {
                durationInput.setText(String.valueOf(exercise.getDuration()));
            }
        } else {
            exerciseTypeToggle.check(R.id.setsRepsType);
            if (exercise.getSets() > 0) {
                setsInput.setText(String.valueOf(exercise.getSets()));
            }
            if (exercise.getReps() > 0) {
                repsInput.setText(String.valueOf(exercise.getReps()));
            }
        }

        if (exercise.getInstructions() != null) {
            instructionsInput.setText(exercise.getInstructions());
        }

        // Load existing image
        if (exercise.getLocalImagePath() != null && !exercise.getLocalImagePath().isEmpty()) {
            File imageFile = new File(exercise.getLocalImagePath());
            if (imageFile.exists()) {
                selectedImageUri = Uri.fromFile(imageFile);
                displaySelectedImage();
            }
        }
    }

    private boolean validateInputs() {
        boolean isValid = true;

        String name = nameInput.getText().toString().trim();
        if (TextUtils.isEmpty(name)) {
            nameLayout.setError("Exercise name is required");
            isValid = false;
        } else {
            nameLayout.setError(null);
        }

        int checkedId = exerciseTypeToggle.getCheckedButtonId();

        if (checkedId == R.id.setsRepsType) {
            String setsStr = setsInput.getText().toString().trim();
            if (TextUtils.isEmpty(setsStr)) {
                setsLayout.setError("Sets required");
                isValid = false;
            } else {
                try {
                    int sets = Integer.parseInt(setsStr);
                    if (sets <= 0) {
                        setsLayout.setError("Sets must be > 0");
                        isValid = false;
                    } else {
                        setsLayout.setError(null);
                    }
                } catch (NumberFormatException e) {
                    setsLayout.setError("Invalid number");
                    isValid = false;
                }
            }

            String repsStr = repsInput.getText().toString().trim();
            if (TextUtils.isEmpty(repsStr)) {
                repsLayout.setError("Reps required");
                isValid = false;
            } else {
                try {
                    int reps = Integer.parseInt(repsStr);
                    if (reps <= 0) {
                        repsLayout.setError("Reps must be > 0");
                        isValid = false;
                    } else {
                        repsLayout.setError(null);
                    }
                } catch (NumberFormatException e) {
                    repsLayout.setError("Invalid number");
                    isValid = false;
                }
            }
        } else if (checkedId == R.id.durationType) {
            String durationStr = durationInput.getText().toString().trim();
            if (TextUtils.isEmpty(durationStr)) {
                durationInputLayout.setError("Duration required");
                isValid = false;
            } else {
                try {
                    int duration = Integer.parseInt(durationStr);
                    if (duration <= 0) {
                        durationInputLayout.setError("Duration must be > 0");
                        isValid = false;
                    } else {
                        durationInputLayout.setError(null);
                    }
                } catch (NumberFormatException e) {
                    durationInputLayout.setError("Invalid number");
                    isValid = false;
                }
            }
        } else {
            showWarningToast("Please select exercise type");
            isValid = false;
        }

        return isValid;
    }

    private void validateAndSaveExercise() {
        if (validateInputs()) {
            saveExercise();
        }
    }

    private void saveExercise() {
        String name = nameInput.getText().toString().trim();
        int checkedId = exerciseTypeToggle.getCheckedButtonId();

        if (checkedId == R.id.setsRepsType) {
            int sets = Integer.parseInt(setsInput.getText().toString().trim());
            int reps = Integer.parseInt(repsInput.getText().toString().trim());
            exercise.setName(name);
            exercise.setSets(sets);
            exercise.setReps(reps);
            exercise.setTimed(false);
            exercise.setDuration(0);
        } else {
            int duration = Integer.parseInt(durationInput.getText().toString().trim());
            exercise.setName(name);
            exercise.setSets(1);
            exercise.setDuration(duration);
            exercise.setTimed(true);
            exercise.setReps(0);
        }

        String instructions = instructionsInput.getText().toString().trim();
        if (!TextUtils.isEmpty(instructions)) {
            exercise.setInstructions(instructions);
        }

        // Generate a unique ID for the exercise
        if (exercise.getId() == null || exercise.getId().isEmpty()) {
            exercise.setId("exercise_" + System.currentTimeMillis());
        }

        if (selectedImageUri != null) {
            uploadImageToLocal(selectedImageUri);
        } else {
            returnExerciseResult();
        }
    }

    private void uploadImageToLocal(Uri imageUri) {
        showLoading();
        String localPath = imageHelper.saveExerciseImage(imageUri, exercise.getId());
        if (localPath != null) {
            exercise.setLocalImagePath(localPath);
            hideLoading();
            returnExerciseResult();
        } else {
            hideLoading();
            showErrorToast("Failed to save image");
            returnExerciseResult();
        }
    }



    private void returnExerciseResult() {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("exercise", exercise);
        resultIntent.putExtra("exercise_position", exercisePosition);  // Make sure this is set
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}