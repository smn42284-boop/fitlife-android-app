package com.example.fitlife_sumyatnoe.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.fitlife_sumyatnoe.R;
import com.example.fitlife_sumyatnoe.adapters.EquipmentSelectAdapter;
import com.example.fitlife_sumyatnoe.adapters.ExerciseSelectAdapter;
import com.example.fitlife_sumyatnoe.models.Equipment;
import com.example.fitlife_sumyatnoe.models.Exercise;
import com.example.fitlife_sumyatnoe.models.Workout;
import com.example.fitlife_sumyatnoe.repository.WorkoutRepository;
import com.example.fitlife_sumyatnoe.utils.FirebaseHelper;
import com.example.fitlife_sumyatnoe.utils.ImageLoader;
import com.example.fitlife_sumyatnoe.utils.ImagePickerHelper;
import com.example.fitlife_sumyatnoe.utils.LocalImageHelper;
import com.example.fitlife_sumyatnoe.utils.PermissionHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseUser;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CreateWorkoutActivity extends BaseActivity {
    private ImagePickerHelper imagePickerHelper;
    private TextInputLayout nameLayout, descriptionLayout, durationLayout;
    private TextInputEditText nameInput, descriptionInput, durationInput;
    private MaterialCardView daysCard;
    private MaterialButton addExerciseBtn, addEquipmentBtn;
    private TextView selectedDaysText;
    private LinearLayout selectedExercisesContainer, selectedEquipmentContainer;
    private Button cancelBtn, saveBtn;
    private FrameLayout imagePickerContainer;
    private ImageView workoutImage;
    private TextView addImageText;
    private LocalImageHelper imageHelper;
    private LinearLayout addImageContainer;

    private FirebaseHelper firebaseHelper;
    private FirebaseUser currentUser;
    private Workout workout;
    private static final int CAMERA_PERMISSION_CODE = 100;
    private boolean isEditMode = false;
    private TextView exerciseCount, equipmentCount;
    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<Intent> galleryLauncher;
    private List<String> selectedDays = new ArrayList<>();
    private List<Exercise> selectedExercises = new ArrayList<>();
    private List<String> selectedEquipment = new ArrayList<>();
    private Uri selectedImageUri;

    private List<Exercise> availableExercises = new ArrayList<>();
    private List<Equipment> availableEquipment = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_workout);

        firebaseHelper = new FirebaseHelper();
        currentUser = firebaseHelper.getCurrentUser();
        imageHelper = new LocalImageHelper(this);
        workout = new Workout();

        isEditMode = getIntent().getBooleanExtra("is_edit_mode", false);

        if (isGuest()) {
            showGuestPageRestriction();
            return;
        }

        initViews();

        // Setup toolbar
        setupToolbar(true, isEditMode ? "Edit Workout" : "Create Workout");
        setToolbarElevation(false);

        setupImagePicker();

        if (isEditMode) {
            String workoutId = getIntent().getStringExtra("workout_id");            String workoutName = getIntent().getStringExtra("workout_name");
            String workoutDescription = getIntent().getStringExtra("workout_description");
            int workoutDuration = getIntent().getIntExtra("workout_duration", 0);
            List<String> workoutDays = (List<String>) getIntent().getSerializableExtra("workout_days");
            List<Exercise> workoutExercises = (List<Exercise>) getIntent().getSerializableExtra("workout_exercises");
            List<String> workoutEquipment = (List<String>) getIntent().getSerializableExtra("workout_equipment");
            String workoutImagePath = getIntent().getStringExtra("workout_image_path");

            if (nameInput != null && workoutName != null) {
                nameInput.setText(workoutName);
                Log.d("EDIT_DEBUG", "Set name to: " + workoutName);
            }
            if (descriptionInput != null) {
                descriptionInput.setText(workoutDescription != null ? workoutDescription : "");
            }
            if (durationInput != null && workoutDuration > 0) {
                durationInput.setText(String.valueOf(workoutDuration));
            }
            workout.setId(workoutId);
            // Set days
            if (workoutDays != null && !workoutDays.isEmpty()) {
                selectedDays.clear();
                selectedDays.addAll(workoutDays);
                updateDaysText();
            }

            // Set exercises
            if (workoutExercises != null && !workoutExercises.isEmpty()) {
                selectedExercises.clear();
                selectedExercises.addAll(workoutExercises);
                displaySelectedExercises();
            }

            // Set equipment
            if (workoutEquipment != null && !workoutEquipment.isEmpty()) {
                selectedEquipment.clear();
                selectedEquipment.addAll(workoutEquipment);
                displaySelectedEquipment();
            }

            // Set image
            if (workoutImagePath != null && !workoutImagePath.isEmpty()) {
                File imageFile = new File(workoutImagePath);
                if (imageFile.exists()) {
                    selectedImageUri = Uri.fromFile(imageFile);
                    displaySelectedImage();
                }
            }
        }

        loadDataFromFirestore();
        setupClickListeners();

    }
    // Add this method right after initViews()

    private void loadEditDataFromIntent() {
        String workoutId = getIntent().getStringExtra("workout_id");
        String workoutName = getIntent().getStringExtra("workout_name");
        String workoutDescription = getIntent().getStringExtra("workout_description");
        int workoutDuration = getIntent().getIntExtra("workout_duration", 0);
        List<String> workoutDays = (List<String>) getIntent().getSerializableExtra("workout_days");
        List<Exercise> workoutExercises = (List<Exercise>) getIntent().getSerializableExtra("workout_exercises");
        List<String> workoutEquipment = (List<String>) getIntent().getSerializableExtra("workout_equipment");
        String workoutImagePath = getIntent().getStringExtra("workout_image_path");

        // Set workout object
        workout.setId(workoutId);
        workout.setName(workoutName);
        workout.setDescription(workoutDescription);
        workout.setDuration(workoutDuration);
        workout.setDaysOfWeek(workoutDays);
        workout.setExercises(workoutExercises);
        workout.setEquipment(workoutEquipment);
        workout.setLocalImagePath(workoutImagePath);
        workout.setCustom(true);

        // Populate UI (views are already initialized)
        if (nameInput != null && workoutName != null) {
            nameInput.setText(workoutName);
        }

        if (descriptionInput != null) {
            descriptionInput.setText(workoutDescription != null ? workoutDescription : "");
        }

        if (durationInput != null && workoutDuration > 0) {
            durationInput.setText(String.valueOf(workoutDuration));
        }

        // Load days
        if (workoutDays != null && !workoutDays.isEmpty()) {
            selectedDays.clear();
            selectedDays.addAll(workoutDays);
            updateDaysText();
        }

        // Load exercises
        if (workoutExercises != null && !workoutExercises.isEmpty()) {
            selectedExercises.clear();
            selectedExercises.addAll(workoutExercises);
            displaySelectedExercises();
        }

        // Load equipment
        if (workoutEquipment != null && !workoutEquipment.isEmpty()) {
            selectedEquipment.clear();
            selectedEquipment.addAll(workoutEquipment);
            displaySelectedEquipment();
        }

        // Load image
        if (workoutImagePath != null && !workoutImagePath.isEmpty()) {
            File imageFile = new File(workoutImagePath);
            if (imageFile.exists()) {
                selectedImageUri = Uri.fromFile(imageFile);
                displaySelectedImage();
            }
        }
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);

        nameLayout = findViewById(R.id.nameLayout);
        descriptionLayout = findViewById(R.id.descriptionLayout);
        durationLayout = findViewById(R.id.durationLayout);

        nameInput = findViewById(R.id.nameInput);
        descriptionInput = findViewById(R.id.descriptionInput);
        durationInput = findViewById(R.id.durationInput);

        daysCard = findViewById(R.id.daysCard);

        addExerciseBtn = findViewById(R.id.addExerciseBtn);
        addEquipmentBtn = findViewById(R.id.addEquipmentBtn);

        selectedDaysText = findViewById(R.id.selectedDaysText);
        selectedExercisesContainer = findViewById(R.id.selectedExercisesContainer);
        selectedEquipmentContainer = findViewById(R.id.selectedEquipmentContainer);
        cancelBtn = findViewById(R.id.cancelBtn);
        saveBtn = findViewById(R.id.saveBtn);
        exerciseCount = findViewById(R.id.exerciseCount);
        equipmentCount = findViewById(R.id.equipmentCount);

        imagePickerContainer = findViewById(R.id.imagePickerContainer);
        workoutImage = findViewById(R.id.workoutImage);
        addImageText = findViewById(R.id.addImageText);
        addImageContainer = findViewById(R.id.addImageContainer);

    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, open camera
                if (imagePickerHelper != null) {
                    imagePickerHelper.openCamera();
                }
            } else {
                showErrorToast("Camera permission is required to take photos");
            }
        }
    }


    private void setupImagePicker() {
        imagePickerHelper = new ImagePickerHelper(this, new ImagePickerHelper.ImagePickerCallback() {
            @Override
            public void onImageSelected(Uri imageUri) {

                selectedImageUri = imageUri;

                displaySelectedImage();

                saveCompressedImage(imageUri);
            }

            @Override
            public void onError(String error) {
                showErrorToast("Error: " + error);
            }
        });

        if (imagePickerContainer != null) {
            imagePickerContainer.setOnClickListener(v -> {
                if (imagePickerHelper != null) {
                    imagePickerHelper.showImagePickerDialog();
                }
            });
        }
    }
    // In CreateWorkoutActivity.java, add this method

    private void displaySelectedEquipment() {
        selectedEquipmentContainer.removeAllViews();

        if (selectedEquipment.isEmpty()) {
            TextView emptyText = new TextView(this);
            emptyText.setText("No equipment selected");
            emptyText.setTextColor(getColor(R.color.text_secondary));
            emptyText.setPadding(16, 16, 16, 16);
            selectedEquipmentContainer.addView(emptyText);
            return;
        }

        for (String equipment : selectedEquipment) {
            View itemView = getLayoutInflater().inflate(R.layout.item_selected_equipment, selectedEquipmentContainer, false);

            TextView nameText = itemView.findViewById(R.id.equipmentName);
            ImageView removeBtn = itemView.findViewById(R.id.removeEquipmentBtn);

            nameText.setText(equipment);

            removeBtn.setOnClickListener(v -> {
                selectedEquipment.remove(equipment);
                displaySelectedEquipment();
            });

            selectedEquipmentContainer.addView(itemView);
        }
    }

    private void saveCompressedImage(Uri imageUri) {
        new Thread(() -> {
            String compressedPath = ImageLoader.compressAndSaveImage(this, imageUri, "temp_workout_" + System.currentTimeMillis());
            if (compressedPath != null) {
                runOnUiThread(() -> {
                    selectedImageUri = Uri.fromFile(new File(compressedPath));
                    Glide.with(this)
                            .load(selectedImageUri)
                            .placeholder(R.drawable.workout_placeholder)
                            .error(R.drawable.workout_placeholder)
                            .centerCrop()
                            .into(workoutImage);
                });
            }
        }).start();
    }

    private void displaySelectedImage() {
        if (selectedImageUri != null) {

            if (addImageContainer != null) {
                addImageContainer.setVisibility(View.GONE);
            } else {
            }

            if (workoutImage != null) {
                workoutImage.setVisibility(View.VISIBLE);

                Glide.with(this)
                        .load(selectedImageUri)
                        .placeholder(R.drawable.workout_placeholder)
                        .error(R.drawable.workout_placeholder)
                        .centerCrop()
                        .into(workoutImage);
            }

            if (addImageText != null) {
                addImageText.setVisibility(View.GONE);
            }

        } else {
            if (workoutImage != null) {
                workoutImage.setVisibility(View.GONE);
                workoutImage.setImageDrawable(null);
            }

            if (addImageContainer != null) {
                addImageContainer.setVisibility(View.VISIBLE);
            }

            if (addImageText != null) {
                addImageText.setVisibility(View.VISIBLE);
            }
        }
    }

    private void loadDataFromFirestore() {
        showLoading();

        final int[] completed = {0};
        final int total = 2;

        // Load exercises
        firebaseHelper.getDefaultExercises(new FirebaseHelper.FirestoreCallback<List<Exercise>>() {
            @Override
            public void onSuccess(List<Exercise> defaultExercises) {
                availableExercises.clear();
                availableExercises.addAll(defaultExercises);

                if (!isGuest() && currentUser != null) {
                    firebaseHelper.getUserExercises(currentUser.getUid(), new FirebaseHelper.FirestoreCallback<List<Exercise>>() {
                        @Override
                        public void onSuccess(List<Exercise> userExercises) {
                            availableExercises.addAll(userExercises);
                            completed[0]++;
                            checkAllLoaded(completed[0], total);
                        }
                        @Override
                        public void onFailure(String error) {
                            completed[0]++;
                            checkAllLoaded(completed[0], total);
                        }
                    });
                } else {
                    completed[0]++;
                    checkAllLoaded(completed[0], total);
                }
            }
            @Override
            public void onFailure(String error) {
                completed[0]++;
                checkAllLoaded(completed[0], total);
            }
        });

        // Load equipment
        firebaseHelper.getDefaultEquipment(new FirebaseHelper.FirestoreCallback<List<Equipment>>() {
            @Override
            public void onSuccess(List<Equipment> defaultEquipment) {
                availableEquipment.clear();
                availableEquipment.addAll(defaultEquipment);

                if (!isGuest() && currentUser != null) {
                    firebaseHelper.getUserEquipment(currentUser.getUid(), new FirebaseHelper.FirestoreCallback<List<Equipment>>() {
                        @Override
                        public void onSuccess(List<Equipment> userEquipment) {
                            availableEquipment.addAll(userEquipment);
                            completed[0]++;
                            checkAllLoaded(completed[0], total);
                        }
                        @Override
                        public void onFailure(String error) {
                            completed[0]++;
                            checkAllLoaded(completed[0], total);
                        }
                    });
                } else {
                    completed[0]++;
                    checkAllLoaded(completed[0], total);
                }
            }
            @Override
            public void onFailure(String error) {
                completed[0]++;
                checkAllLoaded(completed[0], total);
            }
        });
    }

    private void checkAllLoaded(int completed, int total) {
        if (completed == total) {
            runOnUiThread(() -> {
                hideLoading();
            });
        }
    }
    private void setupClickListeners() {
        cancelBtn.setOnClickListener(v -> finish());
        saveBtn.setOnClickListener(v -> validateAndSaveWorkout());
        daysCard.setOnClickListener(v -> showDaysDialog());

        addExerciseBtn.setOnClickListener(v -> showExerciseOptionsDialog());
        addEquipmentBtn.setOnClickListener(v -> showEquipmentOptionsDialog());

        if (imagePickerContainer != null) {
            imagePickerContainer.setOnClickListener(v -> {
                if (imagePickerHelper != null) {
                    imagePickerHelper.showImagePickerDialog();
                }
            });
        }
    }


    private void showExerciseOptionsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Exercise");
        builder.setItems(new String[]{"Select from Library", "Create New Exercise"}, (dialog, which) -> {
            if (which == 0) {
                showSelectExercisesDialog();
            } else {
                showAddExerciseDialog();
            }
        });
        builder.show();
    }

    private void showSelectExercisesDialog() {
        if (availableExercises.isEmpty()) {
            showInfoToast("No exercises found. Create a new one!");
            return;
        }

        List<Exercise> tempSelected = new ArrayList<>(selectedExercises);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_select_exercise, null);
        RecyclerView recyclerView = dialogView.findViewById(R.id.exercisesRecyclerView);
        Button cancelDialogBtn = dialogView.findViewById(R.id.cancelBtn);
        Button doneBtn = dialogView.findViewById(R.id.doneBtn);

        ExerciseSelectAdapter adapter = new ExerciseSelectAdapter(availableExercises, tempSelected);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        androidx.appcompat.app.AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setView(dialogView)
                .create();

        dialog.show();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setDimAmount(0.7f);
        }

        cancelDialogBtn.setOnClickListener(v -> dialog.dismiss());

        doneBtn.setOnClickListener(v -> {
            selectedExercises.clear();
            selectedExercises.addAll(adapter.getSelectedExercises());
            displaySelectedExercises();
            dialog.dismiss();
        });
    }

    private void showAddExerciseDialog() {
        Intent intent = new Intent(CreateWorkoutActivity.this, CreateExerciseActivity.class);
        intent.putExtra("workout_id", workout.getId());
        startActivityForResult(intent, 100);
    }


    private void showEquipmentOptionsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this,R.style.CustomMaterialAlertDialog);
        builder.setTitle("Add Equipment");
        builder.setItems(new String[]{"Select from Library", "Create New Equipment"}, (dialog, which) -> {
            if (which == 0) {
                showSelectEquipmentDialog();
            } else {
                showAddEquipmentDialog();
            }
        });
        builder.show();
    }

    private void showSelectEquipmentDialog() {
        if (availableEquipment.isEmpty()) {
            showInfoToast("No equipment found. Create new equipment!");
            return;
        }

        List<String> tempSelected = new ArrayList<>(selectedEquipment);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_select_equipment, null);

        RecyclerView recyclerView = dialogView.findViewById(R.id.equipmentRecyclerView);
        Button cancelDialogBtn = dialogView.findViewById(R.id.cancelBtn);
        Button doneBtn = dialogView.findViewById(R.id.doneBtn);

        EquipmentSelectAdapter adapter = new EquipmentSelectAdapter(availableEquipment, tempSelected);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        dialog.show();

        cancelDialogBtn.setOnClickListener(v -> dialog.dismiss());

        doneBtn.setOnClickListener(v -> {
            selectedEquipment.clear();
            selectedEquipment.addAll(adapter.getSelectedEquipment());
            displaySelectedEquipment();
            dialog.dismiss();
        });
    }

    private void showAddEquipmentDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_equipment, null);

        EditText equipmentNameInput = dialogView.findViewById(R.id.equipmentNameInput);
        ChipGroup categoryChipGroup = dialogView.findViewById(R.id.categoryChipGroup);
        Chip strengthChip = dialogView.findViewById(R.id.strengthChip);
        Chip cardioChip = dialogView.findViewById(R.id.cardioChip);
        Chip yogaChip = dialogView.findViewById(R.id.yogaChip);
        Chip accessoriesChip = dialogView.findViewById(R.id.accessoriesChip);
        ImageView closeBtn = dialogView.findViewById(R.id.closeBtn);
        Button cancelBtn = dialogView.findViewById(R.id.cancelBtn);
        Button saveBtn = dialogView.findViewById(R.id.saveBtn);

        androidx.appcompat.app.AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setView(dialogView)
                .setBackground(getDrawable(R.drawable.dialog_background))
                .create();
        dialog.show();
        dialog.getWindow().setDimAmount(0.6f);


        final String[] selectedCategory = {"accessories"};

        strengthChip.setOnClickListener(v -> {
            selectedCategory[0] = "strength";
            updateChipSelection(strengthChip, cardioChip, yogaChip, accessoriesChip);
        });

        cardioChip.setOnClickListener(v -> {
            selectedCategory[0] = "cardio";
            updateChipSelection(cardioChip, strengthChip, yogaChip, accessoriesChip);
        });

        yogaChip.setOnClickListener(v -> {
            selectedCategory[0] = "yoga";
            updateChipSelection(yogaChip, strengthChip, cardioChip, accessoriesChip);
        });

        accessoriesChip.setOnClickListener(v -> {
            selectedCategory[0] = "accessories";
            updateChipSelection(accessoriesChip, strengthChip, cardioChip, yogaChip);
        });

        closeBtn.setOnClickListener(v -> dialog.dismiss());
        cancelBtn.setOnClickListener(v -> dialog.dismiss());

        saveBtn.setOnClickListener(v -> {
            String name = equipmentNameInput.getText().toString().trim();
            if (TextUtils.isEmpty(name)) {
                equipmentNameInput.setError("Equipment name required");
                return;
            }

            if (currentUser == null) {
                showWarningToast("User not logged in");
                return;
            }

            Equipment newEquipment = new Equipment(name, selectedCategory[0]);
            newEquipment.setCustom(true);

            firebaseHelper.saveUserEquipment(currentUser.getUid(), newEquipment, new FirebaseHelper.FirestoreCallback<String>() {
                @Override
                public void onSuccess(String id) {
                    newEquipment.setId(id);
                    availableEquipment.add(newEquipment);
                    selectedEquipment.add(name);
                    displaySelectedEquipment();
                    showInfoToast("Equipment added: " + name);
                    dialog.dismiss();
                }
                @Override
                public void onFailure(String error) {
                    showErrorToast("Error: " + error);
                }
            });
        });
    }

    private void updateChipSelection(Chip selected, Chip... others) {
        selected.setChipBackgroundColorResource(R.color.primary);
        for (Chip chip : others) {
            chip.setChipBackgroundColorResource(android.R.color.transparent);
        }
    }

    private void showExerciseMenu(View anchor, Exercise exercise) {
        android.widget.PopupMenu popup = new android.widget.PopupMenu(this, anchor);
        popup.getMenuInflater().inflate(R.menu.exercise_menu, popup.getMenu());

        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_edit) {
                int position = selectedExercises.indexOf(exercise);
                Log.d("EditExercise", "Editing exercise at position: " + position);
                Log.d("EditExercise", "Exercise name: " + exercise.getName());

                Intent intent = new Intent(CreateWorkoutActivity.this, CreateExerciseActivity.class);
                intent.putExtra("exercise", exercise);
                intent.putExtra("exercise_position", position);  // ✅ Pass the position
                intent.putExtra("edit_mode", true);
                startActivityForResult(intent, 101);
                return true;
            }
            return false;
        });
        popup.show();
    }


    private void displaySelectedExercises() {
        selectedExercisesContainer.removeAllViews();

        if (selectedExercises.isEmpty()) {
            TextView emptyText = new TextView(this);
            emptyText.setText("No exercises added");
            emptyText.setTextColor(getColor(R.color.text_secondary));
            emptyText.setPadding(16, 16, 16, 16);
            selectedExercisesContainer.addView(emptyText);
            return;
        }

        for (Exercise exercise : selectedExercises) {
            View itemView = getLayoutInflater().inflate(R.layout.item_selected_exercise, selectedExercisesContainer, false);

            ShapeableImageView exerciseImage = itemView.findViewById(R.id.exerciseImage);
            TextView nameText = itemView.findViewById(R.id.exerciseName);
            TextView detailsText = itemView.findViewById(R.id.exerciseDetails);
            ImageView menuButton = itemView.findViewById(R.id.menuButton);
            ImageView removeBtn = itemView.findViewById(R.id.removeExerciseBtn);

            nameText.setText(exercise.getName());
            detailsText.setText(exercise.getDisplayText());

            // Load image
            if (exercise.getLocalImagePath() != null && !exercise.getLocalImagePath().isEmpty()) {
                File imageFile = new File(exercise.getLocalImagePath());
                if (imageFile.exists()) {
                    Glide.with(this).load(imageFile).centerCrop().into(exerciseImage);
                }
            }

            menuButton.setOnClickListener(v -> showExerciseMenu(v, exercise));

            removeBtn.setOnClickListener(v -> {
                selectedExercises.remove(exercise);
                displaySelectedExercises();
                showInfoToast("Exercise removed");
            });

            selectedExercisesContainer.addView(itemView);
        }
    }
    private void loadExerciseImage(ShapeableImageView imageView, Exercise exercise) {
        String imageUrl = exercise.getImageUrl();
        String localPath = exercise.getLocalImagePath();

        if (localPath != null && !localPath.isEmpty()) {
            File imageFile = new File(localPath);
            if (imageFile.exists()) {
                Glide.with(this)
                        .load(imageFile)
                        .placeholder(R.drawable.ic_fitness)
                        .error(R.drawable.ic_fitness)
                        .centerCrop()
                        .into(imageView);
                return;
            }
        }

        if (imageUrl != null && !imageUrl.isEmpty()) {
            int resId = getResources().getIdentifier(imageUrl, "drawable", getPackageName());
            if (resId != 0) {
                Glide.with(this)
                        .load(resId)
                        .placeholder(R.drawable.ic_fitness)
                        .error(R.drawable.ic_fitness)
                        .centerCrop()
                        .into(imageView);
                return;
            }
        }

        imageView.setImageResource(R.drawable.ic_fitness);
    }
    private void showDaysDialog() {
        String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
        boolean[] checkedDays = new boolean[days.length];

        for (int i = 0; i < days.length; i++) {
            checkedDays[i] = selectedDays.contains(days[i]);
        }

        new AlertDialog.Builder(this)
                .setTitle("Select Days")
                .setMultiChoiceItems(days, checkedDays, (dialog, which, isChecked) -> {
                    if (isChecked) {
                        selectedDays.add(days[which]);
                    } else {
                        selectedDays.remove(days[which]);
                    }
                    updateDaysText();
                })
                .setPositiveButton("Done", null)
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updateDaysText() {
        if (selectedDays.isEmpty()) {
            selectedDaysText.setText("No days selected");
            selectedDaysText.setTextColor(getColor(R.color.text_secondary));
        } else {
            String text = selectedDays.size() + " day" + (selectedDays.size() > 1 ? "s" : "") + " selected";
            selectedDaysText.setText(text);
            selectedDaysText.setTextColor(getColor(R.color.primary));
        }
    }

    private boolean validateInputs() {
        boolean isValid = true;

        String name = nameInput.getText().toString().trim();
        if (TextUtils.isEmpty(name)) {
            nameLayout.setError("Workout name is required");
            isValid = false;
        } else {
            nameLayout.setError(null);
        }

        String description = descriptionInput.getText().toString().trim();
        if (TextUtils.isEmpty(description)) {
            descriptionLayout.setError("Description is required");
            isValid = false;
        } else {
            descriptionLayout.setError(null);
        }

        String durationStr = durationInput.getText().toString().trim();
        if (TextUtils.isEmpty(durationStr)) {
            durationLayout.setError("Duration is required");
            isValid = false;
        } else {
            try {
                int duration = Integer.parseInt(durationStr);
                if (duration <= 0) {
                    durationLayout.setError("Duration must be greater than 0");
                    isValid = false;
                } else {
                    durationLayout.setError(null);
                }
            } catch (NumberFormatException e) {
                durationLayout.setError("Please enter a valid number");
                isValid = false;
            }
        }

        if (selectedDays.isEmpty()) {
            selectedDaysText.setTextColor(getColor(R.color.error));
            showWarningToast("Please select at least one day");
            isValid = false;
        } else {
            selectedDaysText.setTextColor(getColor(R.color.text_secondary));
        }

        if (selectedExercises.isEmpty()) {
            showWarningToast("Please add at least one exercise");
            isValid = false;
        }

        return isValid;
    }

    private void validateAndSaveWorkout() {
        if (validateInputs()) {
            if (selectedImageUri != null) {
                uploadWorkoutImage(selectedImageUri);
            } else {
                saveWorkout();
            }
        }
    }

    private void uploadWorkoutImage(Uri imageUri) {
        showLoading();

        if (workout.getId() == null) {
            saveWorkoutWithImage(imageUri);
        } else {
            String localPath = imageHelper.saveWorkoutImage(imageUri, workout.getId());
            if (localPath != null) {
                workout.setLocalImagePath(localPath);
                saveWorkout();
            }
        }
    }

    private void saveWorkoutWithImage(Uri imageUri) {

        String name = nameInput.getText().toString().trim();
        String description = descriptionInput.getText().toString().trim();
        int duration = Integer.parseInt(durationInput.getText().toString().trim());

        workout.setName(name);
        workout.setDescription(description);
        workout.setDuration(duration);
        workout.setDaysOfWeek(selectedDays);
        workout.setExercises(selectedExercises);
        workout.setEquipment(selectedEquipment);
        workout.setUserId(firebaseHelper.getCurrentUser().getUid());
        workout.setCustom(true);
        workout.setDefault(false);
        workout.setCreatedAt(System.currentTimeMillis());

        showLoading();

        // Check if offline
        if (!isNetworkAvailable()) {
            // Save locally only
            saveWorkoutLocally(imageUri);
            return;
        }

        // Online - save to cloud
        firebaseHelper.saveCustomWorkout(workout, new FirebaseHelper.FirestoreCallback<String>() {
            @Override
            public void onSuccess(String id) {
                workout.setId(id);
                String localPath = imageHelper.saveWorkoutImage(imageUri, id);
                if (localPath != null) {
                    workout.setLocalImagePath(localPath);
                    firebaseHelper.updateCustomWorkout(workout, new FirebaseHelper.FirestoreCallback<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                            hideLoading();
                            WorkoutRepository repository = WorkoutRepository.getInstance(CreateWorkoutActivity.this);
                            repository.saveCustomWorkout(workout, new WorkoutRepository.DataCallback<String>() {
                                @Override
                                public void onSuccess(String savedId) {
                                    Log.d("CreateWorkout", "Workout saved to local cache");
                                }
                                @Override
                                public void onFailure(String error) {
                                    Log.e("CreateWorkout", "Failed to cache: " + error);
                                }
                            });
                            showSuccessToast(isEditMode ? "Workout updated!" : "Workout created!");
                            finish();
                        }
                        @Override
                        public void onFailure(String error) {
                            hideLoading();
                            showErrorToast("Workout saved but image failed: " + error);
                            finish();
                        }
                    });
                } else {
                    hideLoading();
                    showSuccessToast(isEditMode ? "Workout updated!" : "Workout created!");
                    finish();
                }
            }
            @Override
            public void onFailure(String error) {
                // If cloud fails but we have internet, try local save as fallback
                if (isNetworkAvailable()) {
                    showErrorToast("Error: " + error);
                    hideLoading();
                } else {
                    saveWorkoutLocally(imageUri);
                }
            }
        });
    }
    private void saveWorkout() {
        String name = nameInput.getText().toString().trim();
        String description = descriptionInput.getText().toString().trim();
        int duration = Integer.parseInt(durationInput.getText().toString().trim());

        workout.setName(name);
        workout.setDescription(description);
        workout.setDuration(duration);
        workout.setDaysOfWeek(selectedDays);
        workout.setExercises(selectedExercises);
        workout.setEquipment(selectedEquipment);
        workout.setUserId(firebaseHelper.getCurrentUser().getUid());
        workout.setCustom(true);
        workout.setDefault(false);
        workout.setAdded(true);
        workout.setCreatedAt(System.currentTimeMillis());

        showLoading();

        if (!isNetworkAvailable()) {
            saveWorkoutLocally(null);
            return;
        }

        if (isEditMode && workout.getId() != null) {
            // ✅ EDIT MODE - Update existing workout
            firebaseHelper.updateCustomWorkout(workout, new FirebaseHelper.FirestoreCallback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    hideLoading();
                    showSuccessToast("Workout updated!");

                    // Update local cache
                    WorkoutRepository repository = WorkoutRepository.getInstance(CreateWorkoutActivity.this);
                    repository.updateCustomWorkout(workout, null);

                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("workout_updated", true);
                    resultIntent.putExtra("workout_id", workout.getId());
                    resultIntent.putExtra("workout_name", workout.getName());
                    resultIntent.putExtra("workout_description", workout.getDescription());
                    resultIntent.putExtra("workout_duration", workout.getDuration());
                    resultIntent.putExtra("workout_custom", true);

                    if (selectedDays != null) {
                        resultIntent.putStringArrayListExtra("workout_days", new ArrayList<>(selectedDays));
                    }

                    setResult(RESULT_OK, resultIntent);
                    finish();
                }

                @Override
                public void onFailure(String error) {
                    hideLoading();
                    showErrorToast("Error updating workout: " + error);
                }
            });
        } else {
            // ✅ CREATE MODE - Save new workout
            firebaseHelper.saveCustomWorkout(workout, new FirebaseHelper.FirestoreCallback<String>() {
                @Override
                public void onSuccess(String id) {
                    workout.setId(id);
                    hideLoading();
                    showSuccessToast("Workout created!");

                    WorkoutRepository repository = WorkoutRepository.getInstance(CreateWorkoutActivity.this);
                    repository.saveCustomWorkout(workout, null);

                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("workout_created", true);
                    resultIntent.putExtra("workout_id", workout.getId());
                    resultIntent.putExtra("workout_name", workout.getName());
                    resultIntent.putExtra("workout_description", workout.getDescription());
                    resultIntent.putExtra("workout_duration", workout.getDuration());
                    resultIntent.putExtra("workout_added", workout.isAdded());
                    resultIntent.putExtra("workout_custom", true);

                    if (selectedDays != null) {
                        resultIntent.putStringArrayListExtra("workout_days", new ArrayList<>(selectedDays));
                    }

                    setResult(RESULT_OK, resultIntent);
                    finish();
                }

                @Override
                public void onFailure(String error) {
                    hideLoading();
                    showErrorToast("Error: " + error);
                }
            });
        }
    }
    private void addWorkoutToPlanAutomatically() {

        firebaseHelper.updateWorkout(workout, new FirebaseHelper.FirestoreCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                hideLoading();
                showSuccessToast("Workout created and added to your plan!");

                // Return result to previous activity
                Intent resultIntent = new Intent();
                resultIntent.putExtra("workout_created", true);
                resultIntent.putExtra("workout_id", workout.getId());
                resultIntent.putExtra("workout_name", workout.getName());
                resultIntent.putExtra("workout_added", true);
                resultIntent.putExtra("workout_days", new ArrayList<>(selectedDays));
                setResult(RESULT_OK, resultIntent);
                finish();
            }

            @Override
            public void onFailure(String error) {
                hideLoading();
                showWarningToast("Workout created but failed to add to plan: " + error);
                Intent resultIntent = new Intent();
                resultIntent.putExtra("workout_created", true);
                resultIntent.putExtra("workout_id", workout.getId());
                resultIntent.putExtra("workout_name", workout.getName());
                resultIntent.putExtra("workout_added", false);
                setResult(RESULT_OK, resultIntent);
                finish();
            }
        });
    }
    private void saveWorkoutLocally(Uri imageUri) {
        String tempId = "offline_" + System.currentTimeMillis();
        workout.setId(tempId);
        workout.setNeedsSync(true);

        if (imageUri != null) {
            String localPath = imageHelper.saveWorkoutImage(imageUri, tempId);
            if (localPath != null) {
                workout.setLocalImagePath(localPath);
            }
        }
        WorkoutRepository repository = WorkoutRepository.getInstance(this);
        repository.saveWorkoutLocally(workout, new WorkoutRepository.DataCallback<String>() {
            @Override
            public void onSuccess(String id) {
                hideLoading();
                showSuccessToast("Workout saved locally! Will sync when online.");

                if (!isEditMode && selectedDays != null && !selectedDays.isEmpty()) {
                    addWorkoutToPlanLocally();
                } else {
                    finish();
                }
            }

            @Override
            public void onFailure(String error) {
                hideLoading();
                showErrorToast("Failed to save workout: " + error);
            }
        });
    }

    // NEW METHOD: Add workout to plan locally for offline
    private void addWorkoutToPlanLocally() {
        workout.setAdded(true);
        workout.setDaysOfWeek(selectedDays);

        WorkoutRepository repository = WorkoutRepository.getInstance(this);
        repository.addWorkoutToPlanLocally(workout, new WorkoutRepository.DataCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                showSuccessToast("Added to your plan! Will sync when online.");
                Intent intent = new Intent(CreateWorkoutActivity.this, MyPlanActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }

            @Override
            public void onFailure(String error) {
                showWarningToast("Workout saved but not added to plan: " + error);
                finish();
            }
        });
    }
    private void showAddToPlanDialog() {
        String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
        List<String> selectedDaysList = new ArrayList<>();

        new MaterialAlertDialogBuilder(this)
                .setTitle("Add to Plan")
                .setMessage("Would you like to add this workout to your plan?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    showDaySelectionForNewWorkout(days, selectedDaysList);
                })
                .setNegativeButton("No", (dialog, which) -> finish())
                .show();
    }
    private void showDaySelectionForNewWorkout(String[] days, List<String> selectedDaysList) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Select Days")
                .setMultiChoiceItems(days, null, (dialog, which, isChecked) -> {
                    if (isChecked) {
                        selectedDaysList.add(days[which]);
                    } else {
                        selectedDaysList.remove(days[which]);
                    }
                })
                .setPositiveButton("Add to Plan", (dialog, which) -> {
                    if (selectedDaysList.isEmpty()) {
                        showWarningToast("Please select at least one day");
                        return;
                    }
                    addWorkoutToPlan(selectedDaysList);
                })
                .setNegativeButton("Cancel", (dialog, which) -> finish())
                .show();
    }

    private void addWorkoutToPlan(List<String> days) {
        showLoading();

        workout.setAdded(true);
        workout.setDaysOfWeek(days);

        firebaseHelper.updateWorkout(workout, new FirebaseHelper.FirestoreCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                hideLoading();
                showSuccessToast("Added to your plan!");
                finish();
            }

            @Override
            public void onFailure(String error) {
                hideLoading();
                showErrorToast("Error adding to plan: " + error);
                finish();
            }
        });
    }
    private void loadWorkoutData() {
        // Set text fields
        if (workout.getName() != null) {
            nameInput.setText(workout.getName());
        }

        if (workout.getDescription() != null && !workout.getDescription().isEmpty()) {
            descriptionInput.setText(workout.getDescription());
        } else {
            descriptionInput.setText("");
        }

        if (workout.getDuration() > 0) {
            durationInput.setText(String.valueOf(workout.getDuration()));
        } else {
            durationInput.setText("");
        }

        if (workout.getDaysOfWeek() != null && !workout.getDaysOfWeek().isEmpty()) {
            selectedDays.clear();
            selectedDays.addAll(workout.getDaysOfWeek());
            updateDaysText();
        }

        if (workout.getExercises() != null && !workout.getExercises().isEmpty()) {
            selectedExercises.clear();
            selectedExercises.addAll(workout.getExercises());
            displaySelectedExercises();
        }

        if (workout.getEquipment() != null && !workout.getEquipment().isEmpty()) {
            selectedEquipment.clear();
            selectedEquipment.addAll(workout.getEquipment());
            displaySelectedEquipment();
        }

        if (workout.getLocalImagePath() != null && !workout.getLocalImagePath().isEmpty()) {
            File imageFile = new File(workout.getLocalImagePath());
            if (imageFile.exists()) {
                selectedImageUri = Uri.fromFile(imageFile);
                displaySelectedImage();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100 && resultCode == RESULT_OK) {
            Exercise newExercise = (Exercise) data.getSerializableExtra("exercise");
            if (newExercise != null) {
                Log.d("CreateWorkout", "Exercise received: " + newExercise.getName());
                Log.d("CreateWorkout", "Exercise ID: " + newExercise.getId());

                // Add to available exercises list
                availableExercises.add(newExercise);

                // Also add to selected exercises
                selectedExercises.add(newExercise);

                // Update UI
                displaySelectedExercises();
                showSuccessToast("Exercise added: " + newExercise.getName());
            } else {
                showErrorToast("Failed to add exercise");
            }
        } else if (requestCode == 101 && resultCode == RESULT_OK) {
            Exercise editedExercise = (Exercise) data.getSerializableExtra("exercise");
            if (editedExercise != null) {
                int position = data.getIntExtra("exercise_position", -1);
                if (position >= 0 && position < selectedExercises.size()) {
                    selectedExercises.set(position, editedExercise);
                    displaySelectedExercises();
                    showSuccessToast("Exercise updated: " + editedExercise.getName());
                }
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}