package com.example.fitlife_sumyatnoe.activities;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.fitlife_sumyatnoe.R;
import com.example.fitlife_sumyatnoe.adapters.ActiveExerciseAdapter;
import com.example.fitlife_sumyatnoe.models.Exercise;
import com.example.fitlife_sumyatnoe.models.UserWorkout;
import com.example.fitlife_sumyatnoe.models.Workout;
import com.example.fitlife_sumyatnoe.utils.ConfettiHelper;
import com.example.fitlife_sumyatnoe.utils.FirebaseHelper;
import com.example.fitlife_sumyatnoe.utils.GestureHelper;
import com.example.fitlife_sumyatnoe.utils.HintManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.gson.Gson;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ActiveWorkoutActivity extends BaseActivity {

    private ShapeableImageView workoutImage;
    private TextView workoutNameText, workoutProgressText, completedBadge;
    private LinearProgressIndicator progressIndicator;
    private RecyclerView exercisesRecycler;
    private GestureHelper gestureHelper;

    private MaterialButton saveProgressBtn;
    private FrameLayout confettiContainer;
    private Toolbar toolbar;

    private ActiveExerciseAdapter adapter;
    private List<Exercise> exercises;
    private Workout workout;
    private UserWorkout userWorkout;
    private int completedCount = 0;
    private FirebaseHelper firebaseHelper;
    private ConfettiHelper confettiHelper;
    private boolean isWorkoutCompleted = false;
    private String currentDay;
    private Handler uiHandler = new Handler(Looper.getMainLooper());


    // ✅ Optimization: Debounce saving
    private Handler saveHandler = new Handler();
    private Runnable saveRunnable;
    private static final long SAVE_DELAY_MS = 2000; // Save after 2 seconds of no changes

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_active_workout);

        firebaseHelper = new FirebaseHelper();
        confettiHelper = ConfettiHelper.getInstance();

        workout = (Workout) getIntent().getSerializableExtra("workout");

        if (workout == null) {
            showWarningToast("Workout not found");
            finish();
            return;
        }

        SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", Locale.getDefault());
        currentDay = dayFormat.format(new Date());

        initViews();
        setupToolbar();
        loadWorkoutImage();
        setupExercises();
        loadUserWorkoutProgress();
        setupRecyclerView();
        updateUI();
        setupSaveButton();
        setupGestures();
        showWorkoutHints();
    }
    private void showShakeHint() {
        HintManager hintManager = new HintManager(this);

        if (!hintManager.hasSeenShakeHint()) {
            progressIndicator.postDelayed(() -> {
                hintManager.showTooltip(this, progressIndicator,
                        "📱 Shake to Reset",
                        "Shake your phone to reset all progress");
                hintManager.markShakeHintSeen();
            }, 3000);
        }
    }
    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        workoutImage = findViewById(R.id.workoutImage);
        workoutNameText = findViewById(R.id.workoutNameText);
        workoutProgressText = findViewById(R.id.workoutProgressText);
        progressIndicator = findViewById(R.id.progressIndicator);
        exercisesRecycler = findViewById(R.id.exercisesRecycler);
        saveProgressBtn = findViewById(R.id.saveProgressBtn);
        completedBadge = findViewById(R.id.completedBadge);
        confettiContainer = findViewById(R.id.confettiContainer);
    }

    @Override
    public void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(workout.getName());
        }
        toolbar.setNavigationOnClickListener(v -> showExitDialog());
    }

    private void loadWorkoutImage() {
        if (workout.getLocalImagePath() != null && !workout.getLocalImagePath().isEmpty()) {
            File imageFile = new File(workout.getLocalImagePath());
            if (imageFile.exists()) {
                Glide.with(this).load(imageFile).centerCrop().into(workoutImage);
                return;
            }
        }

        if (workout.getImageUrl() != null && !workout.getImageUrl().isEmpty()) {
            int resId = getResources().getIdentifier(workout.getImageUrl(), "drawable", getPackageName());
            if (resId != 0) {
                Glide.with(this).load(resId).centerCrop().into(workoutImage);
                return;
            }
        }

        workoutImage.setImageResource(R.drawable.workout_placeholder);
    }
    private void showWorkoutHints() {
        boolean hasSeenWorkoutHint = getPrefs().getBoolean("hasSeenWorkoutHint", false);

        if (!hasSeenWorkoutHint) {
            new Handler().postDelayed(() -> {
                new MaterialAlertDialogBuilder(this)
                        .setTitle("💡 Quick Tip")
                        .setMessage("Shake your phone to reset all exercise progress if you need to start over.")
                        .setPositiveButton("Got it", (dialog, which) -> {
                            getPrefs().edit().putBoolean("hasSeenWorkoutHint", true).apply();
                        })
                        .show();
            }, 2000);
        }
    }

    // Call this in onCreate after setting up
    private void setupExercises() {
        exercises = new ArrayList<>();

        if (workout.getExercises() == null || workout.getExercises().isEmpty()) {
            showInfoToast("No exercises in this workout");
            finish();
            return;
        }

        for (Exercise exercise : workout.getExercises()) {
            Exercise copy = new Exercise(
                    exercise.getName(),
                    exercise.getSets(),
                    exercise.getReps()
            );
            copy.setTimed(exercise.isTimed());
            copy.setDuration(exercise.getDuration());
            copy.setInstructions(exercise.getInstructions());
            copy.setLocalImagePath(exercise.getLocalImagePath());
            copy.setImageUrl(exercise.getImageUrl());
            copy.setCompleted(false);
            exercises.add(copy);
        }

        updateCompletedCount();
    }
    private void setupGestures() {
        gestureHelper = new GestureHelper(this, new GestureHelper.OnGestureListener() {
            private int currentSwipingPosition = -1;

            @Override
            public void onSwipeStart(View view, int position, float startX) {
                currentSwipingPosition = position;
            }

            @Override
            public void onSwipeMove(View view, int position, float deltaX) {
                if (position == currentSwipingPosition && view != null) {
                    view.setTranslationX(deltaX);
                    float alpha = 1 - Math.min(Math.abs(deltaX) / 500f, 0.7f);
                    view.setAlpha(alpha);
                }
            }

            @Override
            public void onSwipeLeft(View view, int position, Object item) {
                resetSwipeState(view);
            }

            @Override
            public void onSwipeRight(View view, int position, Object item) {
                resetSwipeState(view);
            }

            @Override
            public void onSwipeCancel(View view, int position) {
                resetSwipeState(view);
            }

            @Override
            public void onDoubleTap(View view, int position, Object item) {
                if (position >= 0 && position < exercises.size()) {
                    Exercise exercise = exercises.get(position);
                    if (exercise != null) {
                        showExerciseInstructions(exercise);
                    }
                }
            }

            @Override
            public void onLongPress(View view, int position, Object item) {
                if (gestureHelper != null && gestureHelper.isGesturesEnabled()) {
                    if (position >= 0 && position < exercises.size()) {
                        Exercise exercise = exercises.get(position);
                        if (exercise != null) {
                            showExerciseInstructions(exercise);
                        }
                    }
                }
            }

            @Override
            public void onShake() {
                if (gestureHelper != null && gestureHelper.isGesturesEnabled()) {
                    if (!isWorkoutCompleted && exercises != null && exercises.size() > 0) {
                        showResetProgressDialog();
                    }
                }
            }

            private void resetSwipeState(View view) {
                if (view != null) {
                    view.animate()
                            .translationX(0)
                            .alpha(1)
                            .setDuration(200)
                            .start();
                }
                currentSwipingPosition = -1;
            }
        });

        gestureHelper.attachToRecyclerView(exercisesRecycler);
    }
    private void showResetProgressDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Reset Workout Progress")
                .setMessage("Shake detected! Do you want to reset all exercise progress for this workout?")
                .setPositiveButton("Reset", (dialog, which) -> {
                    resetAllProgress();
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    showInfoToast("Reset cancelled");
                })
                .show();
    }
    private void resetAllProgress() {
        for (Exercise exercise : exercises) {
            exercise.setCompleted(false);
        }
        completedCount = 0;
        updateCompletedCount();
        updateUI();

        if (adapter != null) {
            adapter.updateExercises(exercises);
        }

        showSuccessToast("Workout progress has been reset!");

        if (userWorkout != null && !isGuest()) {
            Map<String, Boolean> resetProgress = new HashMap<>();
            for (Exercise exercise : exercises) {
                resetProgress.put(exercise.getName(), false);
            }
            userWorkout.setExerciseProgressForDay(currentDay, resetProgress);
            userWorkout.setCurrentProgress(0);
            userWorkout.setLastUpdated(System.currentTimeMillis());

            firebaseHelper.updateUserWorkoutProgress(userWorkout, new FirebaseHelper.FirestoreCallback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    Log.d("ActiveWorkout", "Progress reset successfully");
                }
                @Override
                public void onFailure(String error) {
                    Log.e("ActiveWorkout", "Failed to reset progress: " + error);
                }
            });
        }
    }

    private void loadUserWorkoutProgress() {
        if (isGuest()) return;

        String userId = getUserId();
        firebaseHelper.getUserAddedWorkouts(userId, new FirebaseHelper.FirestoreCallback<List<UserWorkout>>() {
            @Override
            public void onSuccess(List<UserWorkout> userWorkouts) {
                for (UserWorkout uw : userWorkouts) {
                    if (uw.getWorkoutId().equals(workout.getId())) {
                        userWorkout = uw;
                        break;
                    }
                }

                if (userWorkout != null) {
                    boolean completedToday = userWorkout.isCompletedForDay(currentDay);

                    if (completedToday) {
                        isWorkoutCompleted = true;
                        for (Exercise exercise : exercises) {
                            exercise.setCompleted(true);
                        }
                        updateCompletedCount();
                        updateUI();
                        showInfoToast("You've already completed this workout today!");
                    } else {
                        Map<String, Boolean> progressMap = userWorkout.getExerciseProgressForDay(currentDay);
                        if (progressMap != null) {
                            for (Exercise exercise : exercises) {
                                Boolean completed = progressMap.get(exercise.getName());
                                if (completed != null) {
                                    exercise.setCompleted(completed);
                                }
                            }
                            updateCompletedCount();
                            updateUI();
                        }
                    }
                }
            }
            @Override
            public void onFailure(String error) { }
        });
    }

    private void setupRecyclerView() {
        adapter = new ActiveExerciseAdapter(exercises,
                exercise -> showExerciseInstructions(exercise),
                exercise -> {
                    if (!isWorkoutCompleted) {
                        // Toggle the exercise completion
                        exercise.setCompleted(!exercise.isCompleted());
                        updateCompletedCount();

                        updateUI();

                        debouncedAutoSaveProgress();

                        if (completedCount == exercises.size() && exercises.size() > 0 && !isWorkoutCompleted) {
                            completeWorkout();
                        }
                    }
                });

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        exercisesRecycler.setLayoutManager(layoutManager);
        exercisesRecycler.setAdapter(adapter);
        exercisesRecycler.setVisibility(View.VISIBLE);
    }

    private void setupSaveButton() {
        saveProgressBtn.setOnClickListener(v -> saveProgressAndExit());
    }

    private void debouncedAutoSaveProgress() {
        if (isGuest() || isWorkoutCompleted) return;

        if (saveRunnable != null) {
            saveHandler.removeCallbacks(saveRunnable);
        }

        saveRunnable = () -> {
            if (!isWorkoutCompleted) {
                performAutoSave();
            }
        };
        saveHandler.postDelayed(saveRunnable, SAVE_DELAY_MS);
    }

    private void performAutoSave() {
        if (userWorkout == null) {
            userWorkout = new UserWorkout(getUserId(), workout.getId(), workout.isCustom());
        }

        Map<String, Boolean> progressMap = new HashMap<>();
        for (Exercise exercise : exercises) {
            progressMap.put(exercise.getName(), exercise.isCompleted());
        }
        userWorkout.setExerciseProgressForDay(currentDay, progressMap);

        int completed = completedCount;
        int progress = exercises.size() > 0 ? (completed * 100) / exercises.size() : 0;
        userWorkout.setCurrentProgress(progress);
        userWorkout.setLastUpdated(System.currentTimeMillis());

        if (!isNetworkAvailable()) {
            saveProgressLocally();
            return;
        }

        firebaseHelper.updateUserWorkoutProgress(userWorkout, new FirebaseHelper.FirestoreCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                Log.d("ActiveWorkout", "Progress auto-saved");
            }
            @Override
            public void onFailure(String error) {
                Log.e("ActiveWorkout", "Auto-save failed: " + error);
                saveProgressLocally();
            }
        });
    }

    private void saveProgressAndExit() {
        if (completedCount == 0 || isWorkoutCompleted || isGuest()) {
            finish();
            return;
        }

        showLoading();

        if (userWorkout == null) {
            userWorkout = new UserWorkout(getUserId(), workout.getId(), workout.isCustom());
        }

        Map<String, Boolean> progressMap = new HashMap<>();
        for (Exercise exercise : exercises) {
            progressMap.put(exercise.getName(), exercise.isCompleted());
        }
        userWorkout.setExerciseProgressForDay(currentDay, progressMap);
        userWorkout.setCurrentProgress((completedCount * 100) / exercises.size());
        userWorkout.setLastUpdated(System.currentTimeMillis());

        firebaseHelper.updateUserWorkoutProgress(userWorkout, new FirebaseHelper.FirestoreCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                hideLoading();
                showSuccessToast("Progress saved!");
                finish();
            }
            @Override
            public void onFailure(String error) {
                hideLoading();
                showErrorToast("Failed to save: " + error);
                finish();
            }
        });
    }

    private void updateUI() {
        if (exercises.size() > 0) {
            int progress = (completedCount * 100) / exercises.size();
            progressIndicator.setProgress(progress);
            workoutProgressText.setText(completedCount + "/" + exercises.size() + " completed");
            completedBadge.setText(progress + "%");
        } else {
            workoutProgressText.setText("No exercises");
            completedBadge.setText("0%");
        }

        // Update adapter
        if (adapter != null) {
            exercisesRecycler.post(() -> {
                adapter.updateExercises(exercises);
            });
        }

        if (completedCount > 0 && completedCount < exercises.size() && !isWorkoutCompleted && !isGuest()) {
            saveProgressBtn.setVisibility(View.VISIBLE);
            saveProgressBtn.setText("Save Progress (" + completedCount + "/" + exercises.size() + ")");
        } else {
            saveProgressBtn.setVisibility(View.GONE);
        }

    }
    private void updateCompletedCount() {
        completedCount = 0;
        for (Exercise exercise : exercises) {
            if (exercise.isCompleted()) {
                completedCount++;
            }
        }
    }

    private void showExerciseInstructions(Exercise exercise) {
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

        // Load exercise image
        if (exercise.getImageUrl() != null && !exercise.getImageUrl().isEmpty()) {
            int resId = getResources().getIdentifier(exercise.getImageUrl(), "drawable", getPackageName());
            if (resId != 0) {
                Glide.with(this).load(resId).centerCrop().into(exerciseImage);
            } else {
                exerciseImage.setImageResource(R.drawable.ic_fitness);
            }
        } else {
            exerciseImage.setImageResource(R.drawable.ic_fitness);
        }

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setView(dialogView);
        builder.setCancelable(true);

        AlertDialog dialog = builder.create();
        dialog.show();

        if (closeBtn != null) {
            closeBtn.setOnClickListener(v -> dialog.dismiss());
        }
        if (gotItBtn != null) {
            gotItBtn.setOnClickListener(v -> dialog.dismiss());
        }
    }

    private void completeWorkout() {
        if (isWorkoutCompleted) return;

        isWorkoutCompleted = true;

        if (saveRunnable != null) {
            saveHandler.removeCallbacks(saveRunnable);
        }

        if (!isGuest() && userWorkout != null) {
            userWorkout.setCompletedForDay(currentDay, true);

            Map<String, Boolean> progressMap = new HashMap<>();
            for (Exercise exercise : exercises) {
                progressMap.put(exercise.getName(), true);
            }
            userWorkout.setExerciseProgressForDay(currentDay, progressMap);
            userWorkout.setCurrentProgress(100);
            userWorkout.setLastUpdated(System.currentTimeMillis());

            firebaseHelper.updateUserWorkoutProgress(userWorkout, new FirebaseHelper.FirestoreCallback<Void>() {
                @Override
                public void onSuccess(Void result) { }
                @Override
                public void onFailure(String error) { }
            });
        }

        exercisesRecycler.post(() -> {
            if (adapter != null) {
                adapter.setCheckboxesEnabled(false);
            }
        });

        showConfettiCelebration();
        showSuccessToast("🎉 Amazing! You completed " + workout.getName() + "! 🎉");

        new Handler().postDelayed(() -> {
            finish();
        }, 3000);
    }

    private void showConfettiCelebration() {
        if (confettiContainer != null) {
            confettiContainer.setVisibility(View.VISIBLE);
            confettiHelper.celebrate(confettiContainer);
            new Handler().postDelayed(() -> {
                if (confettiContainer != null) {
                    confettiContainer.setVisibility(View.GONE);
                }
                confettiHelper.stop();
            }, 3000);
        }
    }

    private void showExitDialog() {
        if (isWorkoutCompleted || completedCount == 0) {
            finish();
            return;
        }

        if (completedCount == exercises.size()) {
            finish();
            return;
        }

        new MaterialAlertDialogBuilder(this)
                .setTitle("Exit Workout")
                .setMessage("You've completed " + completedCount + " out of " + exercises.size() +
                        " exercises.\n\nDo you want to save your progress?")
                .setPositiveButton("Save & Exit", (dialog, which) -> saveProgressAndExit())
                .setNegativeButton("Discard & Exit", (dialog, which) -> {
                    showInfoToast("Progress discarded");
                    finish();
                })
                .setNeutralButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            showExitDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    private void saveProgressLocally() {
        if (userWorkout == null) {
            userWorkout = new UserWorkout(getUserId(), workout.getId(), workout.isCustom());
        }

        // Save to SharedPreferences as fallback
        String progressJson = new Gson().toJson(userWorkout);
        getPrefs().edit().putString("pending_progress_" + workout.getId(), progressJson).apply();

        Log.d("ActiveWorkout", "Progress saved locally");
    }


    @Override
    public void onBackPressed() {
        showExitDialog();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (gestureHelper != null) {
            gestureHelper.cleanup();
        }
        if (saveRunnable != null) {
            saveHandler.removeCallbacks(saveRunnable);
        }
    }
}