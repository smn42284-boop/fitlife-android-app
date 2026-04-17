package com.example.fitlife_sumyatnoe.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.fitlife_sumyatnoe.R;
import com.example.fitlife_sumyatnoe.adapters.ExerciseAdapter;
import com.example.fitlife_sumyatnoe.models.Exercise;
import com.example.fitlife_sumyatnoe.models.UserWorkout;
import com.example.fitlife_sumyatnoe.models.Workout;
import com.example.fitlife_sumyatnoe.utils.FirebaseHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseUser;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class WorkoutDetailActivity extends BaseActivity {

    private ShapeableImageView workoutImage;
    private TextView workoutName, workoutDescription, selectedDaysText;
    private Chip workoutDuration;
    private RecyclerView exercisesRecycler;
    private LinearLayout equipmentContainer;
    private TextView equipmentPlaceholder;
    private MaterialButton addToPlanBtn;
    private Toolbar toolbar;  // Add toolbar reference

    private ExerciseAdapter exerciseAdapter;
    private FirebaseHelper firebaseHelper;
    private Workout workout;
    private UserWorkout userWorkout;
    private boolean isInPlan = false;
    private boolean canEdit = false;
    private String workoutId;
    private String userId;
    private ImageView deleteButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout_detail);

        firebaseHelper = new FirebaseHelper();
        workoutId = getIntent().getStringExtra("workout_id");

        FirebaseUser currentUser = firebaseHelper.getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();
        }

        initViews();
        setupToolbar();
        setupRecyclerView();
        loadWorkoutData();
    }
    private void setupDeleteButton() {
        if (deleteButton != null) {
            deleteButton.setOnClickListener(v -> {
                if (workout != null && workout.isCustom()) {
                    deleteWorkout();
                } else {
                    showInfoToast("Cannot delete default workouts");
                }
            });
        }
    }

    private void initViews() {
        // Initialize toolbar from the included layout
        toolbar = findViewById(R.id.toolbar);

        workoutImage = findViewById(R.id.workoutImage);
        workoutName = findViewById(R.id.workoutName);
        workoutDescription = findViewById(R.id.workoutDescription);
        workoutDuration = findViewById(R.id.workoutDuration);
        selectedDaysText = findViewById(R.id.selectedDaysText);
        exercisesRecycler = findViewById(R.id.exercisesRecycler);
        equipmentContainer = findViewById(R.id.equipmentContainer);
        equipmentPlaceholder = findViewById(R.id.equipmentPlaceholder);
        addToPlanBtn = findViewById(R.id.addToPlanBtn);
    }
    @Override
    public void setupToolbar() {
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setDisplayShowHomeEnabled(true);
                getSupportActionBar().setTitle("");
            }
            toolbar.setNavigationOnClickListener(v -> onBackPressed());
        } else {
            Log.e("WorkoutDetail", "Toolbar is null!");
        }
    }

    private void setupRecyclerView() {
        exerciseAdapter = new ExerciseAdapter(new ArrayList<>(), new ExerciseAdapter.OnExerciseClickListener() {
            @Override
            public void onItemClick(Exercise exercise) {
                showExerciseDetailsDialog(exercise);
            }

            @Override
            public void onMenuClick(Exercise exercise, View anchor) {
                showExerciseMenu(anchor, exercise);
            }
        });

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        exercisesRecycler.setLayoutManager(layoutManager);
        exercisesRecycler.setAdapter(exerciseAdapter);
        exercisesRecycler.setHasFixedSize(false);
    }

    private void loadWorkoutData() {
        showLoading();

        // First, check if this workout is in the user's plan
        checkIfWorkoutIsInPlan();

        // Then load the workout details
        firebaseHelper.getWorkoutTemplate(workoutId, new FirebaseHelper.FirestoreCallback<Workout>() {
            @Override
            public void onSuccess(Workout loadedWorkout) {
                workout = loadedWorkout;
                Log.d("WorkoutDetail", "Loaded from templates: " + workout.getName());
                finishLoading();
            }

            @Override
            public void onFailure(String error) {
                Log.d("WorkoutDetail", "Not in templates, trying custom workouts...");

                firebaseHelper.getCustomWorkout(workoutId, new FirebaseHelper.FirestoreCallback<Workout>() {
                    @Override
                    public void onSuccess(Workout customWorkout) {
                        workout = customWorkout;
                        Log.d("WorkoutDetail", "Loaded from custom workouts: " + workout.getName());
                        finishLoading();
                    }

                    @Override
                    public void onFailure(String customError) {
                        hideLoading();
                        Log.e("WorkoutDetail", "Workout not found in templates or custom");
                        showToast("Workout not found");
                        finish();
                    }
                });
            }
        });
    }

    private void checkIfWorkoutIsInPlan() {
        if (isGuest() || userId == null) {
            isInPlan = false;
            return;
        }

        firebaseHelper.getUserAddedWorkouts(userId, new FirebaseHelper.FirestoreCallback<List<UserWorkout>>() {
            @Override
            public void onSuccess(List<UserWorkout> userWorkouts) {
                for (UserWorkout uw : userWorkouts) {
                    if (uw.getWorkoutId().equals(workoutId)) {
                        userWorkout = uw;
                        isInPlan = true;
                        Log.d("WorkoutDetail", "Workout is in plan! Days: " + uw.getDaysOfWeek());
                        break;
                    }
                }

                if (workout != null) {
                    updateAddToPlanButton();
                }
            }

            @Override
            public void onFailure(String error) {
                Log.e("WorkoutDetail", "Error checking plan: " + error);
                isInPlan = false;
                if (workout != null) {
                    updateAddToPlanButton();
                }
            }
        });
    }

    private void finishLoading() {
        canEdit = !isGuest() && workout != null && !workout.isDefault();

        if (userWorkout != null) {
            workout.setAdded(true);
            workout.setDaysOfWeek(userWorkout.getDaysOfWeek());
            isInPlan = true;
        }
        setupDeleteButton();
        if (deleteButton != null) {
            deleteButton.setVisibility(workout != null && workout.isCustom() ? View.VISIBLE : View.GONE);
        }
        displayWorkoutData();
        invalidateOptionsMenu();
        hideLoading();
    }
    private void deleteWorkout() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Delete Workout")
                .setMessage("Are you sure you want to delete \"" + workout.getName() + "\"? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    showLoading();
                    if (workout.isCustom()) {
                        firebaseHelper.deleteCustomWorkout(workout.getId(), new FirebaseHelper.FirestoreCallback<Void>() {
                            @Override
                            public void onSuccess(Void result) {
                                hideLoading();
                                showSuccessToast("Workout deleted");

                                // ✅ Pass result back to WorkoutActivity
                                Intent resultIntent = new Intent();
                                resultIntent.putExtra("workout_deleted", true);
                                resultIntent.putExtra("workout_id", workout.getId());
                                setResult(RESULT_OK, resultIntent);

                                finish();
                            }
                            @Override
                            public void onFailure(String error) {
                                hideLoading();
                                showErrorToast("Error: " + error);
                            }
                        });
                    } else {
                        showWarningToast("Cannot delete default workouts");
                        hideLoading();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }


    private void displayWorkoutData() {
        setToolbarTitle(workout.getName());
        loadWorkoutImage();

        workoutName.setText(workout.getName());
        workoutDescription.setText(workout.getDescription());
        workoutDuration.setText(workout.getDuration() + " minutes");

        updateDaysDisplay();

        if (workout.getExercises() != null && !workout.getExercises().isEmpty()) {
            exerciseAdapter.updateExercises(workout.getExercises());
            exercisesRecycler.setVisibility(View.VISIBLE);
        } else {
            exercisesRecycler.setVisibility(View.GONE);
        }

        displayEquipmentWithCategories();
        updateAddToPlanButton();
    }

    private void loadWorkoutImage() {
        String imageSource = workout.getImageUrl();
        String localPath = workout.getLocalImagePath();

        RequestOptions options = new RequestOptions()
                .placeholder(R.drawable.workout_placeholder)
                .error(R.drawable.workout_placeholder)
                .centerCrop();

        if (imageSource != null && !imageSource.isEmpty()) {
            int resId = getResources().getIdentifier(imageSource, "drawable", getPackageName());
            if (resId != 0) {
                Glide.with(this).load(resId).apply(options).into(workoutImage);
                return;
            }
        }

        if (localPath != null && !localPath.isEmpty()) {
            File imageFile = new File(localPath);
            if (imageFile.exists()) {
                Glide.with(this).load(imageFile).apply(options).into(workoutImage);
                return;
            }
        }

        workoutImage.setImageResource(R.drawable.workout_placeholder);
    }
    private void removeWorkoutFromPlan(Workout workout) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Remove from Plan")
                .setMessage("Remove \"" + workout.getName() + "\" from your plan?")
                .setPositiveButton("Remove", (dialog, which) -> {
                    showLoading();
                    workout.setAdded(false);
                    workout.setDaysOfWeek(new ArrayList<>());

                    firebaseHelper.updateWorkout(workout, new FirebaseHelper.FirestoreCallback<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                            runOnUiThread(() -> {
                                hideLoading();
                                showToast("Removed from plan");

                                // ✅ Set result to notify MyPlanActivity to refresh
                                Intent resultIntent = new Intent();
                                resultIntent.putExtra("workout_removed", true);
                                setResult(RESULT_OK, resultIntent);

                                finish();
                            });
                        }

                        @Override
                        public void onFailure(String error) {
                            runOnUiThread(() -> {
                                hideLoading();
                                showToast("Error: " + error);
                            });
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    private void displayEquipmentWithCategories() {
        equipmentContainer.removeAllViews();

        if (workout.getEquipment() == null || workout.getEquipment().isEmpty()) {
            equipmentPlaceholder.setVisibility(View.VISIBLE);
            return;
        }

        equipmentPlaceholder.setVisibility(View.GONE);

        Set<String> strengthEquipment = new HashSet<>();
        Set<String> cardioEquipment = new HashSet<>();
        Set<String> yogaEquipment = new HashSet<>();
        Set<String> accessoriesEquipment = new HashSet<>();

        for (String equipment : workout.getEquipment()) {
            categorizeEquipment(equipment, strengthEquipment, cardioEquipment,
                    yogaEquipment, accessoriesEquipment);
        }

        if (!strengthEquipment.isEmpty()) {
            equipmentContainer.addView(createCategoryHeader("Strength"));
            equipmentContainer.addView(createEquipmentChipGroup(strengthEquipment));
            addSpacing(equipmentContainer);
        }

        if (!cardioEquipment.isEmpty()) {
            equipmentContainer.addView(createCategoryHeader("Cardio"));
            equipmentContainer.addView(createEquipmentChipGroup(cardioEquipment));
            addSpacing(equipmentContainer);
        }

        if (!yogaEquipment.isEmpty()) {
            equipmentContainer.addView(createCategoryHeader("Yoga"));
            equipmentContainer.addView(createEquipmentChipGroup(yogaEquipment));
            addSpacing(equipmentContainer);
        }

        if (!accessoriesEquipment.isEmpty()) {
            equipmentContainer.addView(createCategoryHeader("Accessories"));
            equipmentContainer.addView(createEquipmentChipGroup(accessoriesEquipment));
        }
    }

    private TextView createCategoryHeader(String title) {
        TextView header = new TextView(this);
        header.setText(title);
        header.setTextSize(14);
        header.setTextColor(getColor(R.color.primary));
        header.setTypeface(null, android.graphics.Typeface.BOLD);
        header.setPadding(0, 0, 0, 8);
        return header;
    }

    private com.google.android.material.chip.ChipGroup createEquipmentChipGroup(Set<String> items) {
        com.google.android.material.chip.ChipGroup chipGroup = new com.google.android.material.chip.ChipGroup(this);
        chipGroup.setChipSpacing(8);

        for (String item : items) {
            com.google.android.material.chip.Chip chip = new com.google.android.material.chip.Chip(this);
            chip.setText(item);
            chip.setChipBackgroundColorResource(R.color.surface_variant);
            chip.setTextColor(getColor(R.color.text_primary));
            chip.setChipStrokeColorResource(R.color.outline);
            chip.setChipStrokeWidth(1f);
            chip.setClickable(false);
            chipGroup.addView(chip);
        }

        return chipGroup;
    }

    private void addSpacing(LinearLayout container) {
        View spacer = new View(this);
        spacer.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                16
        ));
        container.addView(spacer);
    }

    private void categorizeEquipment(String equipment, Set<String> strength, Set<String> cardio,
                                     Set<String> yoga, Set<String> accessories) {
        String lowerEquip = equipment.toLowerCase();

        if (lowerEquip.contains("dumbbell") || lowerEquip.contains("barbell") ||
                lowerEquip.contains("kettlebell") || lowerEquip.contains("weight") ||
                lowerEquip.contains("bench") || lowerEquip.contains("rack")) {
            strength.add(equipment);
        } else if (lowerEquip.contains("jump rope") || lowerEquip.contains("bike") ||
                lowerEquip.contains("treadmill") || lowerEquip.contains("rower")) {
            cardio.add(equipment);
        } else if (lowerEquip.contains("yoga mat") || lowerEquip.contains("yoga block") ||
                lowerEquip.contains("strap") || lowerEquip.contains("pilates")) {
            yoga.add(equipment);
        } else {
            accessories.add(equipment);
        }
    }

    private void updateDaysDisplay() {
        if (workout.getDaysOfWeek() != null && !workout.getDaysOfWeek().isEmpty()) {
            StringBuilder days = new StringBuilder();
            for (String day : workout.getDaysOfWeek()) {
                if (days.length() > 0) days.append(" • ");
                days.append(day);
            }
            selectedDaysText.setText(days.toString());
            selectedDaysText.setTextColor(getColor(R.color.text_primary));
        } else {
            selectedDaysText.setText("Not scheduled");
            selectedDaysText.setTextColor(getColor(R.color.text_hint));
        }
    }

    private void updateAddToPlanButton() {
        if (isGuest()) {
            addToPlanBtn.setText("Sign up to Add to Plan");
            addToPlanBtn.setIconResource(R.drawable.ic_add);
            addToPlanBtn.setBackgroundTintList(getColorStateList(R.color.text_hint));
            addToPlanBtn.setEnabled(false);
            addToPlanBtn.setOnClickListener(v -> showGuestRestriction("adding to plan"));
        } else if (workout != null && workout.isCompleted()) {
            addToPlanBtn.setText("Completed");
            addToPlanBtn.setIconResource(R.drawable.ic_check);
            addToPlanBtn.setBackgroundTintList(getColorStateList(R.color.success));
            addToPlanBtn.setEnabled(false);
            addToPlanBtn.setOnClickListener(null);
        } else if (isInPlan) {
            addToPlanBtn.setText("Start Workout");
            addToPlanBtn.setIconResource(R.drawable.ic_play);
            addToPlanBtn.setBackgroundTintList(getColorStateList(R.color.primary));
            addToPlanBtn.setOnClickListener(v -> startWorkout());
            addToPlanBtn.setEnabled(true);
        } else {
            addToPlanBtn.setText("Add to Plan");
            addToPlanBtn.setIconResource(R.drawable.ic_add);
            addToPlanBtn.setBackgroundTintList(getColorStateList(R.color.primary));
            addToPlanBtn.setOnClickListener(v -> showDaySelectionDialog());
            addToPlanBtn.setEnabled(true);
        }
    }

    private void startWorkout() {
        if (workout == null) {
            showToast("Workout not available");
            return;
        }

        Intent intent = new Intent(WorkoutDetailActivity.this, ActiveWorkoutActivity.class);
        intent.putExtra("workout", workout);
        startActivity(intent);
    }

    private void showDaySelectionDialog() {
        final String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
        final List<String> selectedDays = new ArrayList<>();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Days");

        builder.setMultiChoiceItems(days, null, (dialog, which, isChecked) -> {
            if (isChecked) {
                selectedDays.add(days[which]);
            } else {
                selectedDays.remove(days[which]);
            }
        });

        builder.setPositiveButton("Add to Plan", (dialog, which) -> {
            if (selectedDays.isEmpty()) {
                showToast("Please select at least one day");
                return;
            }
            workout.setDaysOfWeek(selectedDays);
            workout.setAdded(true);
            saveWorkoutToPlan();
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void saveWorkoutToPlan() {
        showLoading();

        workout.setAdded(true);

        if (workout.getDaysOfWeek() == null || workout.getDaysOfWeek().isEmpty()) {
            showToast("Please select at least one day");
            hideLoading();
            return;
        }

        firebaseHelper.updateWorkout(workout, new FirebaseHelper.FirestoreCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                runOnUiThread(() -> {
                    hideLoading();
                    isInPlan = true;
                    updateAddToPlanButton();
                    updateDaysDisplay();
                    showToast("Added to your plan!");
                    showConfetti();
                });
            }

            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> {
                    hideLoading();
                    showToast("Error: " + error);
                    workout.setAdded(false);
                });
            }
        });
    }

    private void editWorkout() {
        if (isGuest()) {
            showGuestRestriction("editing");
            return;
        }
        Intent intent = new Intent(WorkoutDetailActivity.this, CreateWorkoutActivity.class);
        intent.putExtra("workout_id", workout.getId());
        startActivity(intent);
    }

    private void showExerciseDetailsDialog(Exercise exercise) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_exercise_detail, null);

        ShapeableImageView exerciseImage = dialogView.findViewById(R.id.exerciseImage);
        TextView exerciseNameText = dialogView.findViewById(R.id.exerciseName);
        TextView exerciseDetailsText = dialogView.findViewById(R.id.exerciseDetails);
        TextView exerciseInstructionsText = dialogView.findViewById(R.id.exerciseInstructions);
        ImageView closeBtn = dialogView.findViewById(R.id.closeBtn);
        Button gotItBtn = dialogView.findViewById(R.id.gotItBtn);

        exerciseNameText.setText(exercise.getName());

        if (exercise.isTimed()) {
            exerciseDetailsText.setText(exercise.getSets() + " sets × " + exercise.getDuration() + " seconds");
        } else {
            exerciseDetailsText.setText(exercise.getSets() + " sets × " + exercise.getReps() + " reps");
        }

        if (exercise.getInstructions() != null && !exercise.getInstructions().isEmpty()) {
            exerciseInstructionsText.setText(exercise.getInstructions());
            exerciseInstructionsText.setVisibility(View.VISIBLE);
        } else {
            exerciseInstructionsText.setVisibility(View.GONE);
        }

        loadExerciseImage(exerciseImage, exercise);

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        dialog.show();

        if (closeBtn != null) {
            closeBtn.setOnClickListener(v -> dialog.dismiss());
        }
        if (gotItBtn != null) {
            gotItBtn.setOnClickListener(v -> dialog.dismiss());
        }
    }

    private void loadExerciseImage(ShapeableImageView imageView, Exercise exercise) {
        String imageSource = exercise.getImageUrl();
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

        if (imageSource != null && !imageSource.isEmpty()) {
            int resId = getResources().getIdentifier(imageSource, "drawable", getPackageName());
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

    private void showExerciseMenu(View anchor, Exercise exercise) {
        android.widget.PopupMenu popup = new android.widget.PopupMenu(this, anchor);
        popup.getMenuInflater().inflate(R.menu.exercise_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_edit) {
                Intent intent = new Intent(WorkoutDetailActivity.this, CreateExerciseActivity.class);
                intent.putExtra("exercise", exercise);
                intent.putExtra("edit_mode", true);
                startActivity(intent);
                return true;
            }
            return false;
        });
        popup.show();
    }

    private void shareWorkout() {
        StringBuilder message = new StringBuilder();

        // Header with emoji
        message.append("🏋️‍♂️ *").append(workout.getName()).append("*\n\n");

        // Description
        message.append("📝 *Description:*\n");
        message.append(workout.getDescription()).append("\n\n");

        // Duration
        message.append("⏱️ *Duration:* ").append(workout.getDuration()).append(" minutes\n\n");

        // Schedule/Days
        if (workout.getDaysOfWeek() != null && !workout.getDaysOfWeek().isEmpty()) {
            message.append("📅 *Scheduled Days:*\n");
            for (String day : workout.getDaysOfWeek()) {
                message.append("  • ").append(day).append("\n");
            }
            message.append("\n");
        }

        // Exercises Section
        if (workout.getExercises() != null && !workout.getExercises().isEmpty()) {
            message.append("📋 *Exercises:*\n");
            int exerciseNum = 1;
            for (Exercise ex : workout.getExercises()) {
                message.append("  ").append(exerciseNum).append(". *").append(ex.getName()).append("*\n");
                if (ex.isTimed()) {
                    message.append("     ⏱️ ").append(ex.getSets()).append(" sets × ").append(ex.getDuration()).append(" seconds\n");
                } else {
                    message.append("     💪 ").append(ex.getSets()).append(" sets × ").append(ex.getReps()).append(" reps\n");
                }
                if (ex.getInstructions() != null && !ex.getInstructions().isEmpty()) {
                    message.append("     📖 ").append(ex.getInstructions()).append("\n");
                }
                exerciseNum++;
            }
            message.append("\n");
        }

        // Equipment Section with Categories
        if (workout.getEquipment() != null && !workout.getEquipment().isEmpty()) {
            message.append("🎒 *Equipment Needed:*\n");

            // Categorize equipment
            Map<String, List<String>> categorized = new LinkedHashMap<>();
            categorized.put("🏋️ Strength", new ArrayList<>());
            categorized.put("🏃 Cardio", new ArrayList<>());
            categorized.put("🧘 Yoga & Flexibility", new ArrayList<>());
            categorized.put("🔧 Accessories", new ArrayList<>());

            for (String equipment : workout.getEquipment()) {
                String category = getEquipmentCategoryForShare(equipment);
                categorized.get(category).add(equipment);
            }

            // Remove empty categories
            categorized.entrySet().removeIf(entry -> entry.getValue().isEmpty());

            for (Map.Entry<String, List<String>> entry : categorized.entrySet()) {
                message.append("  ").append(entry.getKey()).append(":\n");
                for (String equipment : entry.getValue()) {
                    message.append("    • ").append(equipment).append("\n");
                }
            }
            message.append("\n");
        }

        int totalExercises = workout.getExercises() != null ? workout.getExercises().size() : 0;
        int totalSets = 0;
        int totalReps = 0;
        if (workout.getExercises() != null) {
            for (Exercise ex : workout.getExercises()) {
                totalSets += ex.getSets();
                if (!ex.isTimed()) {
                    totalReps += ex.getReps() * ex.getSets();
                }
            }
        }

        message.append("📊 *Workout Stats:*\n");
        message.append("  • ").append(totalExercises).append(" exercises\n");
        message.append("  • ").append(totalSets).append(" total sets\n");
        if (totalReps > 0) {
            message.append("  • ").append(totalReps).append(" total reps\n");
        }
        message.append("\n");

        message.append("---\n");
        message.append("💪 Track your fitness journey with FitLife App!\n");
        message.append("📱 Available on Android");

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, workout.getName() + " - FitLife Workout");
        shareIntent.putExtra(Intent.EXTRA_TEXT, message.toString());
        startActivity(Intent.createChooser(shareIntent, "Share Workout via"));
    }

    private String getEquipmentCategoryForShare(String equipment) {
        String lower = equipment.toLowerCase();
        if (lower.contains("dumbbell") || lower.contains("barbell") || lower.contains("kettlebell") ||
                lower.contains("weight") || lower.contains("bench") || lower.contains("rack") ||
                lower.contains("pull up") || lower.contains("resistance band")) {
            return "🏋️ Strength";
        } else if (lower.contains("jump rope") || lower.contains("bike") || lower.contains("treadmill") ||
                lower.contains("rower") || lower.contains("elliptical")) {
            return "🏃 Cardio";
        } else if (lower.contains("yoga") || lower.contains("mat") || lower.contains("strap") ||
                lower.contains("block") || lower.contains("foam roller")) {
            return "🧘 Yoga & Flexibility";
        } else {
            return "🔧 Accessories";
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!isGuest()) {
            getMenuInflater().inflate(R.menu.toolbar_menu, menu);
            MenuItem editItem = menu.findItem(R.id.action_edit);
            MenuItem deleteItem = menu.findItem(R.id.action_delete);
            MenuItem shareItem = menu.findItem(R.id.action_share);

            if (editItem != null) {
                editItem.setVisible(workout != null && workout.isCustom());
            }
            if (deleteItem != null) {
                deleteItem.setVisible(workout != null && workout.isCustom());
            }
            if (shareItem != null) {
                shareItem.setVisible(true);
            }
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (isGuest()) {
            showGuestRestriction("this action");
            return true;
        }

        int id = item.getItemId();
        if (id == R.id.action_edit) {
            editWorkout();
            return true;
        } else if (id == R.id.action_delete) {
            deleteWorkout();
            return true;
        } else if (id == R.id.action_share) {
            shareWorkout();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (workoutId != null) {
            loadWorkoutData();
        }
    }
}