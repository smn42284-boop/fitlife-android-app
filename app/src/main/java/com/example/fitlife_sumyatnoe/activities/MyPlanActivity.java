package com.example.fitlife_sumyatnoe.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitlife_sumyatnoe.R;
import com.example.fitlife_sumyatnoe.adapters.DayWorkoutAdapter;
import com.example.fitlife_sumyatnoe.adapters.WeekDayAdapter;
import com.example.fitlife_sumyatnoe.models.Exercise;
import com.example.fitlife_sumyatnoe.models.UserWorkout;
import com.example.fitlife_sumyatnoe.models.WeekDayItem;
import com.example.fitlife_sumyatnoe.models.Workout;
import com.example.fitlife_sumyatnoe.repository.WorkoutRepository;
import com.example.fitlife_sumyatnoe.utils.FirebaseHelper;
import com.example.fitlife_sumyatnoe.utils.GestureHelper;
import com.example.fitlife_sumyatnoe.utils.HintManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MyPlanActivity extends BaseActivity {

    private TextView todayDateText, todaySummaryText, weekTotalText, selectedDayTitle;
    private MaterialButton todayShareBtn;
    private RecyclerView dayWorkoutsRecycler, daysRecyclerView;
    private LinearLayout emptyWorkoutsView;
    private MaterialButton browseWorkoutsEmptyBtn;
    private BottomNavigationView bottomNavigation;

    private FirebaseHelper firebaseHelper;
    private boolean hasSeenGestureTutorial = false;

    private List<Workout> allUserWorkouts = new ArrayList<>();
    private Map<String, List<Workout>> weeklyPlan = new HashMap<>();
    private Map<String, Integer> workoutCounts = new HashMap<>();
    private String selectedDay;
    private DayWorkoutAdapter adapter;
    private WeekDayAdapter weekDayAdapter;
    private List<WeekDayItem> weekDays = new ArrayList<>();

    private GestureHelper gestureHelper;

    private final String[] DAYS_OF_WEEK = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_plan);
        forceRefreshPlan();
        firebaseHelper = new FirebaseHelper();

        if (isGuest()) {
            showGuestAccessDialog();
            return;
        }

        initViews();
        setupToolbar(false, "My Plan");
        setToolbarElevation(false);
        hideToolbarBackButton();
        setupWeekDaysOrder();
        setupRecyclerView();
        setupDaysRecyclerView();
        setupBottomNavigation();
        setupGestures();
        loadUserPlan();
        showGestureTutorialIfNeeded();
        setSelectedNavigationItem(R.id.navigation_plan);
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        todayDateText = findViewById(R.id.todayDateText);
        todaySummaryText = findViewById(R.id.todaySummaryText);
        todayShareBtn = findViewById(R.id.todayShareBtn);
        dayWorkoutsRecycler = findViewById(R.id.dayWorkoutsRecycler);
        weekTotalText = findViewById(R.id.weekTotalText);
        bottomNavigation = findViewById(R.id.bottomNavigation);
        daysRecyclerView = findViewById(R.id.daysRecyclerView);
        selectedDayTitle = findViewById(R.id.selectedDayTitle);
        emptyWorkoutsView = findViewById(R.id.emptyWorkoutsView);
        browseWorkoutsEmptyBtn = findViewById(R.id.browseWorkoutsEmptyBtn);

        todayShareBtn.setOnClickListener(v -> shareSelectedDayEquipment());
    }
    private void showGestureTutorialIfNeeded() {
        hasSeenGestureTutorial = getPrefs().getBoolean("hasSeenGestureTutorial", false);

        if (!hasSeenGestureTutorial) {
            new Handler().postDelayed(() -> {
                View dialogView = getLayoutInflater().inflate(R.layout.dialog_gesture_tutorial, null);

                new MaterialAlertDialogBuilder(this)
                        .setView(dialogView)
                        .setPositiveButton("Got it!", (dialog, which) -> {
                            getPrefs().edit().putBoolean("hasSeenGestureTutorial", true).apply();
                        })
                        .setNegativeButton("Remind Later", (dialog, which) -> {
                        })
                        .show();
            }, 1000);
        }
    }
    private void setupWeekDaysOrder() {
        weekDays.clear();

        Calendar today = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d", Locale.getDefault());
        SimpleDateFormat fullDateFormat = new SimpleDateFormat("EEEE, MMMM d", Locale.getDefault());

        for (int i = 0; i < 7; i++) {
            Calendar dayCal = (Calendar) today.clone();
            dayCal.add(Calendar.DAY_OF_MONTH, i);

            SimpleDateFormat dayNameFormat = new SimpleDateFormat("EEEE", Locale.getDefault());
            String dayName = dayNameFormat.format(dayCal.getTime());
            String dateStr = dateFormat.format(dayCal.getTime());
            String fullDateStr = fullDateFormat.format(dayCal.getTime());

            weekDays.add(new WeekDayItem(dayName, dateStr, fullDateStr, dayCal));
        }

        selectedDay = getCurrentDayName();
        updateSelectedDayCard();
    }

    private String getCurrentDayName() {
        SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", Locale.getDefault());
        return dayFormat.format(new Date());
    }

    private void setupDaysRecyclerView() {
        for (WeekDayItem item : weekDays) {
            workoutCounts.put(item.dayName, 0);
        }

        weekDayAdapter = new WeekDayAdapter(weekDays, selectedDay, workoutCounts, new WeekDayAdapter.OnDayClickListener() {
            @Override
            public void onDayClick(String day) {
                selectedDay = day;
                weekDayAdapter.setSelectedDay(day);
                updateSelectedDayCard();
                displayDayWorkouts();
            }
        });

        daysRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        daysRecyclerView.setAdapter(weekDayAdapter);
        daysRecyclerView.setHasFixedSize(true);

        daysRecyclerView.post(() -> {
            daysRecyclerView.smoothScrollToPosition(0);
        });
    }

    private void updateSelectedDayCard() {
        String fullDate = "";
        for (WeekDayItem item : weekDays) {
            if (item.dayName.equals(selectedDay)) {
                fullDate = item.fullDate;
                break;
            }
        }

        if (!fullDate.isEmpty()) {
            todayDateText.setText(fullDate);
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("EEEE, MMMM d", Locale.getDefault());
            todayDateText.setText(sdf.format(new Date()));
        }

        List<Workout> dayWorkouts = weeklyPlan.get(selectedDay);
        if (dayWorkouts != null && !dayWorkouts.isEmpty()) {
            int completedCount = 0;
            int totalMins = 0;
            for (Workout w : dayWorkouts) {
                if (w.isCompletedForDay(selectedDay)) completedCount++;
                totalMins += w.getDuration();
            }

            todaySummaryText.setText(completedCount + "/" + dayWorkouts.size() + " completed • " + totalMins + " min");

            if (completedCount == dayWorkouts.size()) {
                selectedDayTitle.setText(selectedDay + "'s Workouts ✓ All Done!");
            } else if (completedCount > 0) {
                selectedDayTitle.setText(selectedDay + "'s Workouts (" + completedCount + "/" + dayWorkouts.size() + " completed)");
            } else {
                selectedDayTitle.setText(selectedDay + "'s Workouts");
            }
        } else {
            todaySummaryText.setText("No workouts scheduled");
            selectedDayTitle.setText(selectedDay + "'s Workouts");
        }
    }

    private void setupRecyclerView() {
        adapter = new DayWorkoutAdapter(new ArrayList<>(), selectedDay, new DayWorkoutAdapter.OnWorkoutClickListener() {
            @Override
            public void onWorkoutClick(Workout workout) {
                Intent intent = new Intent(MyPlanActivity.this, WorkoutDetailActivity.class);
                intent.putExtra("workout_id", workout.getId());
                startActivityForResult(intent, 100);
            }

            @Override
            public void onStartClick(Workout workout) {
                if (workout.isCompletedForDay(selectedDay)) {
                    showSuccessToast("You've already completed this workout today!");
                    return;
                }
                Intent intent = new Intent(MyPlanActivity.this, ActiveWorkoutActivity.class);
                intent.putExtra("workout", workout);
                startActivity(intent);
            }

            @Override
            public void onMenuClick(Workout workout, View anchor) {
                showWorkoutMenu(anchor, workout);
            }

            @Override
            public void onRemoveFromDay(Workout workout) {
                removeWorkoutFromDay(workout);
            }
        });

        dayWorkoutsRecycler.setLayoutManager(new LinearLayoutManager(this));
        dayWorkoutsRecycler.setAdapter(adapter);
    }
    private void showSubtleHints() {
        HintManager hintManager = new HintManager(this);

        if (!hintManager.hasSeenSwipeHint() && adapter.getItemCount() > 0) {
            dayWorkoutsRecycler.postDelayed(() -> {
                View firstWorkout = dayWorkoutsRecycler.getLayoutManager()
                        .findViewByPosition(0);

                if (firstWorkout != null) {
                    // Get the workout at position 0
                    Workout firstWorkoutData = adapter.getWorkoutAt(0);

                    // First hint: Swipe Right
                    hintManager.showTooltip(this, firstWorkout,
                            "👉 Swipe Right",
                            "Mark workout as completed");

                    // Second hint: Swipe Left (after first dismisses)
                    firstWorkout.postDelayed(() -> {
                        hintManager.showTooltip(this, firstWorkout,
                                "👈 Swipe Left",
                                "Remove from this day");
                    }, 4500);

                    firstWorkout.postDelayed(() -> {
                        if (firstWorkoutData != null && firstWorkoutData.isCustom()) {
                            hintManager.showTooltip(this, firstWorkout,
                                    "👆👆 Double Tap",
                                    "Edit this workout");
                        } else {
                            hintManager.showTooltip(this, firstWorkout,
                                    "👆👆 Double Tap",
                                    "Default workouts cannot be edited");
                        }
                    }, 9000);
                }
                hintManager.markSwipeHintSeen();
            }, 1000);
        }
    }
    private void setupBottomNavigation() {
        setupBottomNavigation(bottomNavigation);
    }

    private void setupGestures() {
        gestureHelper = new GestureHelper(this, new GestureHelper.OnGestureListener() {
            private int currentSwipingPosition = -1;
            private View currentSwipingView = null;

            @Override
            public void onSwipeStart(View view, int position, float startX) {
                Log.d("Gesture", "onSwipeStart - position: " + position);
                currentSwipingPosition = position;
                currentSwipingView = view;
                adapter.setGesturing(true);
            }

            @Override
            public void onSwipeMove(View view, int position, float deltaX) {
                Log.d("Gesture", "onSwipeMove - position: " + position + ", deltaX: " + deltaX);
                if (position == currentSwipingPosition && view != null) {
                    view.setTranslationX(deltaX);
                    float alpha = 1 - Math.min(Math.abs(deltaX) / 500f, 0.7f);
                    view.setAlpha(alpha);
                }
            }

            @Override
            public void onSwipeLeft(View view, int position, Object item) {
                if (gestureHelper != null && gestureHelper.isGesturesEnabled()) {
                    if (position >= 0 && position < adapter.getItemCount()) {
                        Workout workout = adapter.getWorkoutAt(position);
                        if (workout != null && !workout.isCompletedForDay(selectedDay)) {
                            showRemoveWorkoutConfirmation(workout, position, view);
                        }
                    }
                }
                resetSwipeState(view);
                adapter.setGesturing(false);
            }

            @Override
            public void onSwipeRight(View view, int position, Object item) {
                if (gestureHelper != null && gestureHelper.isGesturesEnabled()) {
                    if (position >= 0 && position < adapter.getItemCount()) {
                        Workout workout = adapter.getWorkoutAt(position);
                        if (workout != null && !workout.isCompletedForDay(selectedDay)) {
                            showCompleteWorkoutConfirmation(workout, position, view);
                        }
                    }
                }
                resetSwipeState(view);
                adapter.setGesturing(false);
            }

            @Override
            public void onSwipeCancel(View view, int position) {
                Log.d("Gesture", "onSwipeCancel - position: " + position);
                resetSwipeState(view);
                adapter.setGesturing(false);
            }

            @Override
            public void onDoubleTap(View view, int position, Object item) {
                Log.d("GestureDebug", "MyPlanActivity.onDoubleTap called - position: " + position);

                if (gestureHelper != null && gestureHelper.isGesturesEnabled()) {
                    if (position >= 0 && position < adapter.getItemCount()) {
                        Workout workout = adapter.getWorkoutAt(position);
                        Log.d("GestureDebug", "Workout: " + (workout != null ? workout.getName() : "null"));
                        Log.d("GestureDebug", "isCustom: " + (workout != null ? workout.isCustom() : "null"));

                        if (workout != null) {
                            if (workout.isCustom()) {
                                Log.d("GestureDebug", "Calling editWorkout for: " + workout.getName());
                                editWorkout(workout);
                                showSuccessToast("Editing: " + workout.getName());
                            } else {
                                showInfoToast("Default workouts cannot be edited");
                            }
                        }
                    } else {
                        Log.d("GestureDebug", "Invalid position: " + position);
                    }
                }
                adapter.setGesturing(false);
            }

            public void onLongPress(View view, int position, Object item) {
                Log.d("Gesture", "onLongPress TRIGGERED - position: " + position);
                if (gestureHelper != null && gestureHelper.isGesturesEnabled()) {
                    if (position >= 0 && position < adapter.getItemCount()) {
                        Workout workout = adapter.getWorkoutAt(position);
                        if (workout != null) {
                            shareWorkout(workout);
                        }
                    }
                }
                adapter.setGesturing(false);
            }

            @Override
            public void onShake() {
                Log.d("Gesture", "onShake TRIGGERED");
                if (gestureHelper != null && gestureHelper.isGesturesEnabled()) {
                    showToast("🔄 Refreshing plan...");
                    refreshPlan();
                    showConfetti();
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
                currentSwipingView = null;
            }
        });

        gestureHelper.attachToRecyclerView(dayWorkoutsRecycler);
    }
    private void showCompleteWorkoutConfirmation(Workout workout, int position, View targetView) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Complete Workout")
                .setMessage("Mark \"" + workout.getName() + "\" as completed for " + selectedDay + "?")
                .setPositiveButton("Yes, Complete", (dialog, which) -> {
                    // Show loading indicator
                    showLoading();

                    String currentDay = selectedDay;
                    workout.setCompletedForDay(currentDay, true);

                    String userId = getUserId();
                    firebaseHelper.getUserAddedWorkouts(userId, new FirebaseHelper.FirestoreCallback<List<UserWorkout>>() {
                        @Override
                        public void onSuccess(List<UserWorkout> userWorkouts) {
                            for (UserWorkout uw : userWorkouts) {
                                if (uw.getWorkoutId().equals(workout.getId())) {
                                    uw.setCompletedForDay(currentDay, true);
                                    firebaseHelper.updateUserWorkoutProgress(uw, new FirebaseHelper.FirestoreCallback<Void>() {
                                        @Override
                                        public void onSuccess(Void result) {
                                            hideLoading();
                                            showSuccessToast("✓ " + workout.getName() + " completed!");
                                            refreshCurrentDayOnly();
                                            if (targetView != null) {
                                                targetView.setVisibility(View.GONE);
                                            }
                                        }

                                        @Override
                                        public void onFailure(String error) {
                                            hideLoading();
                                            showErrorToast("Failed to save: " + error);
                                            workout.setCompletedForDay(currentDay, false);
                                        }
                                    });
                                    break;
                                }
                            }
                        }

                        @Override
                        public void onFailure(String error) {
                            hideLoading();
                            showErrorToast("Failed to save: " + error);
                            workout.setCompletedForDay(currentDay, false);
                        }
                    });

                    showConfetti();
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    showInfoToast("Cancelled");
                    if (targetView != null) {
                        targetView.animate()
                                .translationX(0)
                                .alpha(1)
                                .setDuration(200)
                                .start();
                    }
                })
                .show();
    }

    private void showRemoveWorkoutConfirmation(Workout workout, int position, View targetView) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Remove Workout")
                .setMessage("Remove \"" + workout.getName() + "\" from " + selectedDay + " only?")
                .setPositiveButton("Yes, Remove from " + selectedDay, (dialog, which) -> {
                    // Remove from this day only
                    List<String> currentDays = new ArrayList<>(workout.getDaysOfWeek());
                    currentDays.remove(selectedDay);

                    if (currentDays.isEmpty()) {
                        // If no days left, ask if user wants to remove completely
                        new MaterialAlertDialogBuilder(this)
                                .setTitle("Remove Completely")
                                .setMessage("This workout is no longer scheduled on any day. Remove it from your plan completely?")
                                .setPositiveButton("Yes, Remove Completely", (dialog2, which2) -> {
                                    removeWorkoutFromPlan(workout);
                                })
                                .setNegativeButton("Keep", null)
                                .show();
                    } else {
                        updateWorkoutDays(workout, currentDays);
                        showInfoToast("Removed from " + selectedDay);
                        removeWorkoutFromCurrentDayDisplay(workout);
                    }
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    if (targetView != null) {
                        targetView.animate().translationX(0).alpha(1).setDuration(200).start();
                    }
                })
                .show();
    }
    private void markWorkoutCompleted(Workout workout, int position) {
        String currentDay = selectedDay;

        workout.setCompletedForDay(currentDay, true);

        String userId = getUserId();
        firebaseHelper.getUserAddedWorkouts(userId, new FirebaseHelper.FirestoreCallback<List<UserWorkout>>() {
            @Override
            public void onSuccess(List<UserWorkout> userWorkouts) {
                for (UserWorkout uw : userWorkouts) {
                    if (uw.getWorkoutId().equals(workout.getId())) {
                        uw.setCompletedForDay(currentDay, true);
                        firebaseHelper.updateUserWorkoutProgress(uw, new FirebaseHelper.FirestoreCallback<Void>() {
                            @Override
                            public void onSuccess(Void result) {
                                Log.d("MyPlanActivity", "Workout completion saved to Firestore");
                                // Only refresh after successful save
                                refreshCurrentDayOnly();
                            }

                            @Override
                            public void onFailure(String error) {
                                Log.e("MyPlanActivity", "Failed to save completion: " + error);
                                showErrorToast("Failed to save completion: " + error);
                                // Revert the local change
                                workout.setCompletedForDay(currentDay, false);
                                refreshCurrentDayOnly();
                            }
                        });
                        break;
                    }
                }
            }

            @Override
            public void onFailure(String error) {
                Log.e("MyPlanActivity", "Failed to get user workouts: " + error);
                showErrorToast("Failed to save completion");
                workout.setCompletedForDay(currentDay, false);
                refreshCurrentDayOnly();
            }
        });

        showConfetti();
    }

    private void refreshCurrentDayOnly() {
        updateSelectedDayCard();
        displayDayWorkouts();

        // Update week day adapter counts
        if (weekDayAdapter != null) {
            weekDayAdapter.updateWorkoutCounts(workoutCounts);
        }
    }

    private void refreshPlan() {
        loadUserPlan();
    }

    private void updateWorkoutCompletionInFirestore(Workout workout, String day, boolean completed) {
        String userId = getUserId();
        firebaseHelper.getUserAddedWorkouts(userId, new FirebaseHelper.FirestoreCallback<List<UserWorkout>>() {
            @Override
            public void onSuccess(List<UserWorkout> userWorkouts) {
                for (UserWorkout uw : userWorkouts) {
                    if (uw.getWorkoutId().equals(workout.getId())) {
                        uw.setCompletedForDay(day, completed);
                        firebaseHelper.updateUserWorkoutProgress(uw, new FirebaseHelper.FirestoreCallback<Void>() {
                            @Override
                            public void onSuccess(Void result) {
                                // ✅ Update local workout status immediately
                                workout.setCompletedForDay(day, completed);
                                refreshCurrentDayOnly();
                            }
                            @Override
                            public void onFailure(String error) { }
                        });
                        break;
                    }
                }
            }
            @Override
            public void onFailure(String error) { }
        });
    }

    private void viewWorkoutDetails(Workout workout) {
        Intent intent = new Intent(this, WorkoutDetailActivity.class);
        intent.putExtra("workout_id", workout.getId());
        startActivityForResult(intent, 100);
    }

    private void shareWorkout(Workout workout) {
        StringBuilder message = new StringBuilder();

        // Header with emoji
        message.append("🏋️‍♂️ *").append(workout.getName()).append("*\n\n");

        // Description
        if (workout.getDescription() != null && !workout.getDescription().isEmpty()) {
            message.append("📝 *Description:*\n");
            message.append(workout.getDescription()).append("\n\n");
        }

        // Duration
        message.append("⏱️ *Duration:* ").append(workout.getDuration()).append(" minutes\n\n");

        // Schedule - which days this workout is scheduled
        if (workout.getDaysOfWeek() != null && !workout.getDaysOfWeek().isEmpty()) {
            message.append("📅 *Scheduled Days:*\n");
            for (String day : workout.getDaysOfWeek()) {
                message.append("  • ").append(day).append("\n");
            }
            message.append("\n");
        }

        // Completion status for the selected day
        boolean isCompleted = workout.isCompletedForDay(selectedDay);
        if (isCompleted) {
            message.append("✅ *Status:* Completed for ").append(selectedDay).append("\n\n");
        } else {
            message.append("⏳ *Status:* Pending for ").append(selectedDay).append("\n\n");
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
                    // Truncate long instructions
                    String instructions = ex.getInstructions();
                    if (instructions.length() > 100) {
                        instructions = instructions.substring(0, 97) + "...";
                    }
                    message.append("     📖 ").append(instructions).append("\n");
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
                List<String> categoryList = categorized.get(category);
                if (categoryList != null) {
                    categoryList.add(equipment);
                }
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
        } else {
            message.append("🎒 *Equipment Needed:*\n");
            message.append("  • No equipment needed - bodyweight only!\n\n");
        }

        // Workout type badge (Custom or Default)
        if (workout.isCustom()) {
            message.append("🏷️ *Type:* Custom Workout\n");
        } else {
            message.append("🏷️ *Type:* Default Template\n");
        }

        // Footer
        message.append("\n---\n");
        message.append("💪 Track your fitness journey with FitLife App!\n");
        message.append("📱 Available on Android");

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, workout.getName() + " - FitLife Workout");
        shareIntent.putExtra(Intent.EXTRA_TEXT, message.toString());
        startActivity(Intent.createChooser(shareIntent, "Share Workout"));
    }

    private String getEquipmentCategoryForShare(String equipment) {
        String lower = equipment.toLowerCase();
        if (lower.contains("dumbbell") || lower.contains("barbell") || lower.contains("kettlebell") ||
                lower.contains("weight") || lower.contains("bench") || lower.contains("rack") ||
                lower.contains("pull up") || lower.contains("resistance band") || lower.contains("cable")) {
            return "🏋️ Strength";
        } else if (lower.contains("jump rope") || lower.contains("bike") || lower.contains("treadmill") ||
                lower.contains("rower") || lower.contains("elliptical") || lower.contains("skipping") ||
                lower.contains("stationary bike")) {
            return "🏃 Cardio";
        } else if (lower.contains("yoga") || lower.contains("mat") || lower.contains("strap") ||
                lower.contains("block") || lower.contains("foam roller") || lower.contains("pilates")) {
            return "🧘 Yoga & Flexibility";
        } else {
            return "🔧 Accessories";
        }
    }

    private void shareSelectedDayEquipment() {
        List<Workout> dayWorkouts = weeklyPlan.get(selectedDay);

        if (dayWorkouts == null || dayWorkouts.isEmpty()) {
            showToast("No workouts scheduled for " + selectedDay);
            return;
        }

        // Categorize
        Map<String, List<String>> categorizedEquipment = new LinkedHashMap<>();
        categorizedEquipment.put("🏋️ Strength", new ArrayList<>());
        categorizedEquipment.put("🏃 Cardio", new ArrayList<>());
        categorizedEquipment.put("🧘 Yoga & Flexibility", new ArrayList<>());
        categorizedEquipment.put("🔧 Accessories", new ArrayList<>());

        for (Workout workout : dayWorkouts) {
            if (workout.getEquipment() != null) {
                for (String equipment : workout.getEquipment()) {
                    String category = getEquipmentCategoryForShare(equipment);
                    List<String> categoryList = categorizedEquipment.get(category);
                    if (categoryList != null && !categoryList.contains(equipment)) {
                        categoryList.add(equipment);
                    }
                }
            }
        }

        // Remove empty categories
        categorizedEquipment.entrySet().removeIf(entry -> entry.getValue().isEmpty());

        // Build message
        String dateStr = "";
        for (WeekDayItem item : weekDays) {
            if (item.dayName.equals(selectedDay)) {
                dateStr = item.date;
                break;
            }
        }

        StringBuilder msg = new StringBuilder();
        msg.append("🏋️‍♀️ Equipment for ").append(selectedDay);
        if (!dateStr.isEmpty()) {
            msg.append(" (").append(dateStr).append(")");
        }
        msg.append("\n\n");

        for (Map.Entry<String, List<String>> entry : categorizedEquipment.entrySet()) {
            msg.append(entry.getKey()).append(":\n");
            for (String equipment : entry.getValue()) {
                msg.append("  • ").append(equipment).append("\n");
            }
            msg.append("\n");
        }

        msg.append("Sent from FitLife App 💪");

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, msg.toString());
        startActivity(Intent.createChooser(shareIntent, "Share Equipment for " + selectedDay));
    }


    private void loadUserPlan() {
        WorkoutRepository repository = WorkoutRepository.getInstance(this);
        repository.loadWorkouts(new WorkoutRepository.DataCallback<List<Workout>>() {
            @Override
            public void onSuccess(List<Workout> allWorkouts) {
                allUserWorkouts = allWorkouts;
                loadCompletionStatus(allWorkouts);
            }

            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> {
                    showToast("Error: " + error);
                });
            }
        });
    }

    private void loadCompletionStatus(List<Workout> workouts) {
        String userId = getUserId();
        firebaseHelper.getUserAddedWorkouts(userId, new FirebaseHelper.FirestoreCallback<List<UserWorkout>>() {
            @Override
            public void onSuccess(List<UserWorkout> userWorkouts) {
                Map<String, UserWorkout> userWorkoutMap = new HashMap<>();
                for (UserWorkout uw : userWorkouts) {
                    userWorkoutMap.put(uw.getWorkoutId(), uw);
                }

                for (Workout workout : workouts) {
                    UserWorkout uw = userWorkoutMap.get(workout.getId());
                    if (uw != null) {
                        for (String day : DAYS_OF_WEEK) {
                            boolean completed = uw.isCompletedForDay(day);
                            workout.setCompletedForDay(day, completed);
                        }
                    }
                }

                buildWeeklyPlan(workouts);

                runOnUiThread(() -> {
                    updateSelectedDayCard();
                    displayDayWorkouts();
                    updateWeekTotal();
                    if (weekDayAdapter != null) {
                        weekDayAdapter.updateWorkoutCounts(workoutCounts);
                    }
                });
            }

            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> {
                    showToast("Error loading completion status: " + error);
                });
            }
        });
    }

    private void buildWeeklyPlan(List<Workout> allWorkouts) {
        for (String day : DAYS_OF_WEEK) {
            weeklyPlan.put(day, new ArrayList<>());
            workoutCounts.put(day, 0);
        }

        for (Workout workout : allWorkouts) {
            if (workout.isAdded() && workout.getDaysOfWeek() != null) {
                for (String day : workout.getDaysOfWeek()) {
                    if (weeklyPlan.containsKey(day)) {
                        weeklyPlan.get(day).add(workout);
                        workoutCounts.put(day, workoutCounts.get(day) + 1);
                    }
                }
            }
        }
    }

    private void displayDayWorkouts() {
        List<Workout> dayWorkouts = weeklyPlan.get(selectedDay);

        if (dayWorkouts == null || dayWorkouts.isEmpty()) {
            if (dayWorkoutsRecycler != null) {
                dayWorkoutsRecycler.setVisibility(View.GONE);
            }
            if (emptyWorkoutsView != null) {
                emptyWorkoutsView.setVisibility(View.VISIBLE);
            }
            return;
        }

        if (dayWorkoutsRecycler != null) {
            dayWorkoutsRecycler.setVisibility(View.VISIBLE);
        }
        if (emptyWorkoutsView != null) {
            emptyWorkoutsView.setVisibility(View.GONE);
        }

        adapter.updateWorkouts(dayWorkouts);
        adapter.setSelectedDay(selectedDay);
    }

    private void updateWeekTotal() {
        int total = 0;
        for (List<Workout> workouts : weeklyPlan.values()) {
            if (workouts != null) total += workouts.size();
        }
        if (weekTotalText != null) {
            weekTotalText.setText(total + " workout" + (total != 1 ? "s" : ""));
        }
    }

    private void removeWorkoutFromDay(Workout workout) {
        List<String> currentDays = new ArrayList<>(workout.getDaysOfWeek());
        currentDays.remove(selectedDay);

        if (currentDays.isEmpty()) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Remove from Plan")
                    .setMessage("This workout is no longer scheduled on any day. Remove it from your plan completely?")
                    .setPositiveButton("Remove Completely", (dialog, which) -> {
                        removeWorkoutFromPlan(workout);  // Only call this, not both
                    })
                    .setNegativeButton("Keep", (dialog, which) -> refreshPlan())
                    .show();
        } else {
            updateWorkoutDays(workout, currentDays);
            showInfoToast("Removed from " + selectedDay);
            removeWorkoutFromCurrentDayDisplay(workout);  // This updates UI immediately
        }
    }
    private void removeWorkoutFromCurrentDayDisplay(Workout workout) {
        List<Workout> currentDayWorkouts = weeklyPlan.get(selectedDay);
        if (currentDayWorkouts != null) {
            forceRefreshPlan();
            currentDayWorkouts.remove(workout);
            displayDayWorkouts();
            updateSelectedDayCard();
            updateWeekTotal();
        }
    }

    private void removeWorkoutFromLocalList(Workout workout) {
        // Remove from allUserWorkouts
        allUserWorkouts.remove(workout);

        // Remove from weeklyPlan for all days
        for (String day : DAYS_OF_WEEK) {
            List<Workout> dayWorkouts = weeklyPlan.get(day);
            if (dayWorkouts != null) {
                dayWorkouts.remove(workout);
            }
        }

        // Update UI
        displayDayWorkouts();
        updateSelectedDayCard();
        updateWeekTotal();
    }
    private void updateWorkoutDays(Workout workout, List<String> days) {
        showLoading();

        String userId = getUserId();
        firebaseHelper.getUserAddedWorkouts(userId, new FirebaseHelper.FirestoreCallback<List<UserWorkout>>() {
            @Override
            public void onSuccess(List<UserWorkout> userWorkouts) {
                for (UserWorkout uw : userWorkouts) {
                    if (uw.getWorkoutId().equals(workout.getId())) {
                        uw.setDaysOfWeek(days);
                        forceRefreshPlan();
                        firebaseHelper.updateUserWorkoutDays(uw.getId(), workout.getId(), days,
                                new FirebaseHelper.FirestoreCallback<Void>() {
                                    @Override
                                    public void onSuccess(Void result) {
                                        hideLoading();
                                        showToast("Removed from " + selectedDay);
                                        workout.setDaysOfWeek(days);
                                        refreshPlan();
                                    }

                                    @Override
                                    public void onFailure(String error) {
                                        hideLoading();
                                        showToast("Error: " + error);
                                    }
                                });
                        break;
                    }
                }
            }

            @Override
            public void onFailure(String error) {
                hideLoading();
                showToast("Error: " + error);
            }
        });
    }


    private void showWorkoutMenu(View anchor, Workout workout) {
        android.widget.PopupMenu popup = new android.widget.PopupMenu(this, anchor);
        popup.getMenuInflater().inflate(R.menu.workout_menu, popup.getMenu());

        MenuItem editItem = popup.getMenu().findItem(R.id.action_edit);
        MenuItem shareItem = popup.getMenu().findItem(R.id.action_share);
        MenuItem deleteItem = popup.getMenu().findItem(R.id.action_delete);
        MenuItem removeFromPlanItem = popup.getMenu().findItem(R.id.action_remove_from_plan);

        if (editItem != null) {
            editItem.setVisible(workout.isCustom());
        }

        if (shareItem != null) {
            shareItem.setVisible(true);
        }

        if (deleteItem != null) {
            deleteItem.setVisible(workout.isCustom());
        }

        if (removeFromPlanItem != null) {
            removeFromPlanItem.setVisible(true);
            removeFromPlanItem.setTitle("Remove from " + selectedDay + " only");
        }

        popup.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.action_edit) {
                editWorkout(workout);
                return true;
            } else if (id == R.id.action_share) {
                shareWorkout(workout);
                return true;
            } else if (id == R.id.action_delete) {
                deleteCustomWorkout(workout);
                return true;
            } else if (id == R.id.action_remove_from_plan) {
                showRemoveWorkoutConfirmation(workout, -1, null);
                return true;
            }
            return false;
        });
        popup.show();
    }

    private void removeWorkoutFromPlan(Workout workout) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Remove from Plan")
                .setMessage("Remove \"" + workout.getName() + "\" from your plan entirely?\n\nThis will remove it from ALL scheduled days.")
                .setPositiveButton("Remove from All Days", (dialog, which) -> {
                    showLoading();

                    String userId = getUserId();
                    firebaseHelper.getUserAddedWorkouts(userId, new FirebaseHelper.FirestoreCallback<List<UserWorkout>>() {
                        @Override
                        public void onSuccess(List<UserWorkout> userWorkouts) {
                            String userWorkoutId = null;
                            for (UserWorkout uw : userWorkouts) {
                                if (uw.getWorkoutId().equals(workout.getId())) {
                                    userWorkoutId = uw.getId();
                                    break;
                                }
                            }

                            if (userWorkoutId != null) {
                                firebaseHelper.removeWorkoutFromUserPlan(userWorkoutId,
                                        new FirebaseHelper.FirestoreCallback<Void>() {
                                            @Override
                                            public void onSuccess(Void result) {
                                                runOnUiThread(() -> {
                                                    hideLoading();
                                                    showToast("Removed from plan");
                                                    workout.setAdded(false);
                                                    workout.setDaysOfWeek(new ArrayList<>());
                                                    loadUserPlan();
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
                            } else {
                                hideLoading();
                                showToast("Workout not found in plan");
                            }
                        }

                        @Override
                        public void onFailure(String error) {
                            hideLoading();
                            showToast("Error: " + error);
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    private void editWorkout(Workout workout) {
        Intent intent = new Intent(MyPlanActivity.this, CreateWorkoutActivity.class);
        intent.putExtra("is_edit_mode", true);  // ✅ MUST HAVE THIS
        intent.putExtra("workout_id", workout.getId());
        intent.putExtra("workout_name", workout.getName());
        intent.putExtra("workout_description", workout.getDescription());
        intent.putExtra("workout_duration", workout.getDuration());

        if (workout.getDaysOfWeek() != null) {
            intent.putStringArrayListExtra("workout_days", new ArrayList<>(workout.getDaysOfWeek()));
        }
        if (workout.getExercises() != null) {
            intent.putExtra("workout_exercises", new ArrayList<>(workout.getExercises()));
        }
        if (workout.getEquipment() != null) {
            intent.putStringArrayListExtra("workout_equipment", new ArrayList<>(workout.getEquipment()));
        }
        if (workout.getLocalImagePath() != null) {
            intent.putExtra("workout_image_path", workout.getLocalImagePath());
        }
        forceRefreshPlan();
        startActivity(intent);
        forceRefreshPlan();

    }

    private void deleteCustomWorkout(Workout workout) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Delete Workout")
                .setMessage("Delete \"" + workout.getName() + "\" permanently? This cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    showLoading();
                    firebaseHelper.deleteCustomWorkout(workout.getId(), new FirebaseHelper.FirestoreCallback<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                            hideLoading();
                            forceRefreshPlan();
                            showToast("Workout deleted");
                            refreshPlan();
                        }
                        @Override
                        public void onFailure(String error) {
                            hideLoading();
                            showToast("Error: " + error);
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showGuestAccessDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_guest_access, null);

        MaterialButton signUpBtn = dialogView.findViewById(R.id.signUpBtn);
        MaterialButton browseWorkoutsBtn = dialogView.findViewById(R.id.browseWorkoutsBtn);
        MaterialButton cancelBtn = dialogView.findViewById(R.id.cancelBtn);

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setView(dialogView);
        builder.setCancelable(false);

        androidx.appcompat.app.AlertDialog dialog = builder.create();
        dialog.show();

        signUpBtn.setOnClickListener(v -> {
            dialog.dismiss();
            Intent intent = new Intent(MyPlanActivity.this, RegisterActivity.class);
            startActivity(intent);
            finish();
        });

        browseWorkoutsBtn.setOnClickListener(v -> {
            dialog.dismiss();
            bottomNavigation.setSelectedItemId(R.id.navigation_workout);
        });

        cancelBtn.setOnClickListener(v -> {
            dialog.dismiss();
            finish();
        });
    }
    private void forceRefreshPlan() {
        weeklyPlan.clear();
        workoutCounts.clear();
        loadUserPlan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            refreshPlan();
            forceRefreshPlan();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (gestureHelper != null) {
            gestureHelper.cleanup();
        }
        refreshPlan();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshPlan();
        forceRefreshPlan();
    }
}