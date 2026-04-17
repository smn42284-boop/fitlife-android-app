package com.example.fitlife_sumyatnoe.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitlife_sumyatnoe.R;
import com.example.fitlife_sumyatnoe.adapters.WorkoutAdapter;
import com.example.fitlife_sumyatnoe.models.Exercise;
import com.example.fitlife_sumyatnoe.models.UserWorkout;
import com.example.fitlife_sumyatnoe.models.Workout;
import com.example.fitlife_sumyatnoe.repository.WorkoutRepository;
import com.example.fitlife_sumyatnoe.utils.FirebaseHelper;
import com.example.fitlife_sumyatnoe.viewmodel.WorkoutViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WorkoutActivity extends BaseActivity {

    private RecyclerView workoutsRecycler;
    private WorkoutAdapter adapter;
    private List<Workout> filteredWorkoutList = new ArrayList<>();
    private FloatingActionButton fabAddWorkout;
    private BottomNavigationView bottomNavigation;

    private WorkoutViewModel viewModel;
    private String currentFilter = "";
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workouts);

        FirebaseHelper firebaseHelper = new FirebaseHelper();
        FirebaseUser currentUser = firebaseHelper.getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();
        }

        initViews();
        setupToolbar(false, "Workouts");
        setupBottomNavigation();
        setupRecyclerView();
        setupViewModel();
        setupClickListeners();

        setSelectedNavigationItem(R.id.navigation_workout);

        if (isGuest()) {
            fabAddWorkout.setVisibility(View.GONE);
        } else {
            fabAddWorkout.setVisibility(View.VISIBLE);
        }
    }


    private void initViews() {
        workoutsRecycler = findViewById(R.id.workoutsRecycler);
        fabAddWorkout = findViewById(R.id.fabAddWorkout);
        bottomNavigation = findViewById(R.id.bottomNavigation);
    }

    private void setupBottomNavigation() {
        setupBottomNavigation(bottomNavigation);
    }

    private void setupRecyclerView() {
        adapter = new WorkoutAdapter(filteredWorkoutList, new WorkoutAdapter.OnWorkoutClickListener() {
            @Override
            public void onWorkoutClick(Workout workout) {
                navigateToWorkoutDetail(workout);
            }

            @Override
            public void onMenuClick(Workout workout, View anchor) {
                if (!isGuest()) {
                    showWorkoutCardMenu(anchor, workout);
                }
            }

            @Override
            public void onAddToPlanClick(Workout workout) {
                if (isGuest()) {
                    showGuestRestriction("adding to plan");
                    return;
                }

                if (workout.isAdded()) {
                    showToast("Already in your plan");
                    return;
                }

                if (workout.getDaysOfWeek() != null && !workout.getDaysOfWeek().isEmpty()) {
                    addToPlanDirectly(workout);
                } else {
                    showDaySelectionDialog(workout);
                }
            }

            @Override
            public void onRemoveFromPlanClick(Workout workout) {
                if (isGuest()) {
                    showGuestRestriction("remove workouts from your plan");
                    return;
                }

                new MaterialAlertDialogBuilder(WorkoutActivity.this)
                        .setTitle("Remove from Plan")
                        .setMessage("Remove \"" + workout.getName() + "\" from your plan entirely?\n\nThis will remove it from ALL scheduled days.")
                        .setPositiveButton("Remove from All Days", (dialog, which) -> {
                            viewModel.removeWorkoutFromPlan(workout, new WorkoutRepository.DataCallback<Void>() {
                                @Override
                                public void onSuccess(Void result) {
                                    runOnUiThread(() -> {
                                        showSuccessToast("Removed from plan");
                                        viewModel.refresh();
                                        filterWorkouts(currentFilter);
                                    });
                                }

                                @Override
                                public void onFailure(String error) {
                                    runOnUiThread(() -> showErrorToast("Error: " + error));
                                }
                            });
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        });

        workoutsRecycler.setLayoutManager(new LinearLayoutManager(this));
        workoutsRecycler.setAdapter(adapter);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(WorkoutViewModel.class);

        // Show/hide loading
        viewModel.getIsLoading().observe(this, isLoading -> {
            if (isLoading != null && isLoading) {
                showLoading();  // Show your loading overlay
            } else {
                hideLoading();
            }
        });

        // Observe filtered workouts
        viewModel.getFilteredWorkouts().observe(this, workouts -> {
            if (workouts != null) {
                filteredWorkoutList.clear();
                filteredWorkoutList.addAll(workouts);
                adapter.updateWorkouts(filteredWorkoutList);

                TextView workoutCount = findViewById(R.id.workoutCount);
                if (workoutCount != null) {
                    workoutCount.setText(workouts.size() + " workouts");
                }
            }
        });

        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                showErrorToast(error);
            }
        });

        viewModel.loadWorkouts();
    }
    private void setupClickListeners() {
        fabAddWorkout.setOnClickListener(v -> {
            if (isGuest()) {
                showGuestRestriction("workouts");
                return;
            }
            Intent intent = new Intent(this, CreateWorkoutActivity.class);
            startActivityForResult(intent, 100);        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.workout_toolbar_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterWorkouts(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterWorkouts(newText);
                return true;
            }
        });
        return true;
    }

    private void filterWorkouts(String query) {
        currentFilter = query;
        viewModel.filterWorkouts(query);
    }

    private void navigateToWorkoutDetail(Workout workout) {
        Intent intent = new Intent(WorkoutActivity.this, WorkoutDetailActivity.class);
        intent.putExtra("workout_id", workout.getId());
        startActivity(intent);
    }

    private void editWorkout(Workout workout) {
        if (isGuest()) {
            showGuestRestriction("editing");
            return;
        }

        Intent intent = new Intent(WorkoutActivity.this, CreateWorkoutActivity.class);
        intent.putExtra("is_edit_mode", true);
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

        startActivity(intent);
    }

    private void deleteWorkout(Workout workout) {
        if (!workout.isCustom()) {
            showInfoToast("Default workouts cannot be deleted");
            return;
        }

        new MaterialAlertDialogBuilder(this)
                .setTitle("Delete Workout")
                .setMessage("Are you sure you want to delete \"" + workout.getName() + "\"? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    showLoading();

                    removeWorkoutFromLocalList(workout);

                    viewModel.deleteCustomWorkout(workout, new FirebaseHelper.FirestoreCallback<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                            runOnUiThread(() -> {
                                viewModel.forceRefresh();
                                showLoading();
                                showSuccessToast("Workout deleted");
                                viewModel.refresh();
                                filterWorkouts(currentFilter);
                            });
                        }

                        @Override
                        public void onFailure(String error) {
                            runOnUiThread(() -> {
                                hideLoading();
                                showErrorToast("Error: " + error);
                                // If failed, add back to list
                                addWorkoutBackToLocalList(workout);
                            });
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void removeWorkoutFromLocalList(Workout workout) {
        List<Workout> currentList = viewModel.getFilteredWorkouts().getValue();
        if (currentList != null) {
            List<Workout> updatedList = new ArrayList<>(currentList);
            updatedList.remove(workout);
            filteredWorkoutList.clear();
            filteredWorkoutList.addAll(updatedList);
            adapter.updateWorkouts(filteredWorkoutList);
            viewModel.forceRefresh();
            TextView workoutCount = findViewById(R.id.workoutCount);
            if (workoutCount != null) {
                workoutCount.setText(updatedList.size() + " workouts");
            }
        }
    }

    private void addWorkoutBackToLocalList(Workout workout) {
        List<Workout> currentList = viewModel.getFilteredWorkouts().getValue();
        if (currentList != null) {
            List<Workout> updatedList = new ArrayList<>(currentList);
            updatedList.add(workout);
            viewModel.forceRefresh();
            filteredWorkoutList.clear();
            filteredWorkoutList.addAll(updatedList);
            adapter.updateWorkouts(filteredWorkoutList);
        }
    }
    private void showWorkoutCardMenu(View anchor, Workout workout) {
        android.widget.PopupMenu popup = new android.widget.PopupMenu(this, anchor);
        popup.getMenuInflater().inflate(R.menu.workout_card_menu, popup.getMenu());

        MenuItem editItem = popup.getMenu().findItem(R.id.action_edit);
        MenuItem deleteItem = popup.getMenu().findItem(R.id.action_delete);
        MenuItem shareItem = popup.getMenu().findItem(R.id.action_share);
        MenuItem removeFromPlanItem = popup.getMenu().findItem(R.id.action_remove_from_plan);

        if (editItem != null) {
            editItem.setVisible(false);  // Hide Edit in workout page
        }

        if (shareItem != null) {
            shareItem.setVisible(true);
        }

        if (deleteItem != null) {
            deleteItem.setVisible(workout.isCustom());  // Show Delete only for custom
        }

        if (removeFromPlanItem != null) {
            removeFromPlanItem.setVisible(workout.isAdded());
        }

        popup.setOnMenuItemClickListener(item -> {

            int id = item.getItemId();

            if (id == R.id.action_share) {
                shareWorkout(workout);
                return true;
            } else if (id == R.id.action_delete) {
                if (workout.isCustom()) {
                    deleteWorkout(workout);
                } else {
                    showInfoToast("Default workouts cannot be deleted");
                }
                return true;
            } else if (id == R.id.action_remove_from_plan) {
                showRemoveFromPlanDialog(workout);
                return true;
            } else if (id==R.id.action_edit) {
                editWorkout(workout);

            }
            return false;
        });
        popup.show();
    }

    private void shareWorkout(Workout workout) {
        StringBuilder message = new StringBuilder();

        // Header
        message.append("🏋️‍♂️ *").append(workout.getName()).append("*\n\n");

        // Description
        message.append("📝 *Description:*\n");
        message.append(workout.getDescription()).append("\n\n");

        // Duration
        message.append("⏱️ *Duration:* ").append(workout.getDuration()).append(" minutes\n\n");

        if (workout.getExercises() != null && !workout.getExercises().isEmpty()) {
            message.append("📋 *Exercises:*\n");
            int exerciseNum = 1;
            int maxExercises = Math.min(5, workout.getExercises().size());
            for (int i = 0; i < maxExercises; i++) {
                Exercise ex = workout.getExercises().get(i);
                message.append("  ").append(exerciseNum).append(". ").append(ex.getName());
                if (ex.isTimed()) {
                    message.append(" (").append(ex.getSets()).append(" sets × ").append(ex.getDuration()).append(" sec)\n");
                } else {
                    message.append(" (").append(ex.getSets()).append(" sets × ").append(ex.getReps()).append(" reps)\n");
                }
                exerciseNum++;
            }
            if (workout.getExercises().size() > 5) {
                message.append("  ... and ").append(workout.getExercises().size() - 5).append(" more exercises\n");
            }
            message.append("\n");
        }

        // Equipment
        if (workout.getEquipment() != null && !workout.getEquipment().isEmpty()) {
            message.append("🎒 *Equipment Needed:*\n");
            for (String equipment : workout.getEquipment()) {
                message.append("  • ").append(equipment).append("\n");
            }
            message.append("\n");
        }

        message.append("---\n");
        message.append("💪 Track your fitness with FitLife App!");

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, workout.getName() + " - FitLife Workout");
        shareIntent.putExtra(Intent.EXTRA_TEXT, message.toString());
        startActivity(Intent.createChooser(shareIntent, "Share Workout"));
    }

    private void showDaySelectionDialog(Workout workout) {
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

            workout.setAdded(true);
            workout.setDaysOfWeek(selectedDays);
            updateWorkoutInLocalList(workout);

            viewModel.addWorkoutToPlan(workout, selectedDays, new WorkoutRepository.DataCallback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    runOnUiThread(() -> {
                        showSuccessToast("Added to your plan!");
                        adapter.updateWorkouts(filteredWorkoutList);
                    });
                }

                @Override
                public void onFailure(String error) {
                    runOnUiThread(() -> {
                        showErrorToast("Error: " + error);
                        // Revert
                        workout.setAdded(false);
                        updateWorkoutInLocalList(workout);
                    });
                }
            });
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {
            boolean workoutCreated = data.getBooleanExtra("workout_created", false);
            boolean workoutAdded = data.getBooleanExtra("workout_added", false);

            if (workoutCreated) {
                Workout newWorkout = new Workout();
                newWorkout.setId(data.getStringExtra("workout_id"));
                newWorkout.setName(data.getStringExtra("workout_name"));
                newWorkout.setDescription(data.getStringExtra("workout_description"));
                newWorkout.setDuration(data.getIntExtra("workout_duration", 0));
                newWorkout.setAdded(workoutAdded);
                newWorkout.setCustom(true);

                List<String> days = data.getStringArrayListExtra("workout_days");
                if (days != null) {
                    newWorkout.setDaysOfWeek(days);
                }

                // ✅ Add to local list immediately
                filteredWorkoutList.add(0, newWorkout);
                adapter.updateWorkouts(filteredWorkoutList);

                TextView workoutCount = findViewById(R.id.workoutCount);
                if (workoutCount != null) {
                    workoutCount.setText(filteredWorkoutList.size() + " workouts");
                }

                showSuccessToast("Workout added!");

                viewModel.refresh();
                filterWorkouts(currentFilter);
            }
        }
    }

    private void addToPlanDirectly(Workout workout) {
        workout.setAdded(true);

        updateWorkoutInLocalList(workout);

        viewModel.addWorkoutToPlan(workout, workout.getDaysOfWeek(), new WorkoutRepository.DataCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                runOnUiThread(() -> {
                    showSuccessToast("Added to your plan!");
                    viewModel.forceRefresh();
                    adapter.updateWorkouts(filteredWorkoutList);
                });
            }

            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> {
                    showErrorToast("Error: " + error);
                    // Revert on failure
                    workout.setAdded(false);
                    updateWorkoutInLocalList(workout);
                });
            }
        });
    }


    private void showRemoveFromPlanDialog(Workout workout) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Remove from Plan")
                .setMessage("Remove \"" + workout.getName() + "\" from your plan entirely?\n\nThis will remove it from ALL scheduled days.")
                .setPositiveButton("Remove from All Days", (dialog, which) -> {
                    showLoading();
                    viewModel.forceRefresh();
                    workout.setAdded(false);
                    workout.setDaysOfWeek(new ArrayList<>());

                    updateWorkoutInLocalList(workout);

                    viewModel.removeWorkoutFromPlan(workout, new WorkoutRepository.DataCallback<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                            runOnUiThread(() -> {
                                hideLoading();
                                showSuccessToast("Removed from plan");
                                // UI already updated, just ensure adapter shows correct
                                adapter.updateWorkouts(filteredWorkoutList);
                            });
                        }

                        @Override
                        public void onFailure(String error) {
                            runOnUiThread(() -> {
                                hideLoading();
                                showErrorToast("Error: " + error);
                                // Revert on failure
                                workout.setAdded(true);
                                updateWorkoutInLocalList(workout);
                            });
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updateWorkoutInLocalList(Workout updatedWorkout) {
        for (int i = 0; i < filteredWorkoutList.size(); i++) {
            Workout w = filteredWorkoutList.get(i);
            if (w.getId().equals(updatedWorkout.getId())) {
                w.setAdded(updatedWorkout.isAdded());
                w.setDaysOfWeek(updatedWorkout.getDaysOfWeek());
                adapter.notifyItemChanged(i);
                break;
            }
        }
    }


    private void removeFromPlanEntirely(Workout workout) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Remove from Plan")
                .setMessage("Remove \"" + workout.getName() + "\" from ALL days?")
                .setPositiveButton("Remove from All Days", (dialog, which) -> {
                    showLoading();

                    workout.setAdded(false);
                    workout.setDaysOfWeek(new ArrayList<>());
                    updateWorkoutInLocalList(workout);

                    viewModel.removeWorkoutFromPlan(workout, new WorkoutRepository.DataCallback<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                            runOnUiThread(() -> {
                                hideLoading();
                                showSuccessToast("Removed from plan");
                                adapter.updateWorkouts(filteredWorkoutList);
                            });
                        }

                        @Override
                        public void onFailure(String error) {
                            runOnUiThread(() -> {
                                hideLoading();
                                showErrorToast("Error: " + error);
                                // Revert
                                workout.setAdded(true);
                                updateWorkoutInLocalList(workout);
                            });
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (viewModel != null) {
            viewModel.refresh();
        }
        if (adapter != null) {
            filterWorkouts(currentFilter);
        }
    }
}