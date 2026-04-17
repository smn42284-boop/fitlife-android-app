package com.example.fitlife_sumyatnoe.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitlife_sumyatnoe.R;
import com.example.fitlife_sumyatnoe.activities.ActiveWorkoutActivity;
import com.example.fitlife_sumyatnoe.activities.BaseActivity;
import com.example.fitlife_sumyatnoe.activities.RegisterActivity;
import com.example.fitlife_sumyatnoe.activities.WorkoutDetailActivity;
import com.example.fitlife_sumyatnoe.adapters.TodayWorkoutAdapter;
import com.example.fitlife_sumyatnoe.models.Workout;
import com.example.fitlife_sumyatnoe.viewmodel.TodayWorkoutViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TodayFragment extends Fragment {

    private RecyclerView todayWorkoutRecycler;
    private MaterialCardView emptyTodayView;
    private MaterialCardView equipmentCard;
    private LinearLayout equipmentContent;
    private TextView equipmentCountText;
    private LinearLayout shareContainer;
    private View loadingContainer;
    private View mainContent;

    private TodayWorkoutAdapter adapter;
    private List<Workout> todayWorkouts = new ArrayList<>();
    private TodayWorkoutViewModel viewModel;
    private boolean hasShownFirstTimeDialog = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_today, container, false);

        viewModel = new ViewModelProvider(this).get(TodayWorkoutViewModel.class);

        initViews(view);
        setupRecyclerView();
        setupObservers();
        setupClickListeners();

        viewModel.loadTodayWorkouts();

        return view;
    }

    private void initViews(View view) {
        todayWorkoutRecycler = view.findViewById(R.id.todayWorkoutRecycler);
        emptyTodayView = view.findViewById(R.id.emptyTodayView);
        equipmentCard = view.findViewById(R.id.equipmentCard);
        equipmentContent = view.findViewById(R.id.equipmentContent);
        equipmentCountText = view.findViewById(R.id.equipmentCountText);
        shareContainer = view.findViewById(R.id.shareContainer);
        loadingContainer = view.findViewById(R.id.loadingContainer);
        mainContent = view.findViewById(R.id.mainContent);
    }

    private void setupRecyclerView() {
        adapter = new TodayWorkoutAdapter(todayWorkouts, new TodayWorkoutAdapter.OnWorkoutClickListener() {
            @Override
            public void onWorkoutClick(Workout workout) {
                Intent intent = new Intent(getActivity(), WorkoutDetailActivity.class);
                intent.putExtra("workout_id", workout.getId());
                startActivity(intent);
            }

            @Override
            public void onMenuClick(Workout workout, View anchor) {}

            @Override
            public void onStartClick(Workout workout, int position) {
                Intent intent = new Intent(getActivity(), ActiveWorkoutActivity.class);
                intent.putExtra("workout", workout);
                startActivity(intent);
            }

            @Override
            public void onDetailsClick(Workout workout) {
                Intent intent = new Intent(getActivity(), WorkoutDetailActivity.class);
                intent.putExtra("workout_id", workout.getId());
                startActivity(intent);
            }
        });

        todayWorkoutRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        todayWorkoutRecycler.setAdapter(adapter);
    }

    private void setupClickListeners() {
        if (shareContainer != null) {
            shareContainer.setOnClickListener(v -> shareTodayEquipment());
        }
    }

    private void showLoading() {
        if (loadingContainer != null) {
            loadingContainer.setVisibility(View.VISIBLE);
        }
        if (mainContent != null) {
            mainContent.setVisibility(View.GONE);
        }
    }

    private void hideLoading() {
        if (loadingContainer != null) {
            loadingContainer.setVisibility(View.GONE);
        }
        if (mainContent != null) {
            mainContent.setVisibility(View.VISIBLE);
        }
    }

    private String getTodayDay() {
        return new java.text.SimpleDateFormat("EEEE", java.util.Locale.getDefault())
                .format(new java.util.Date());
    }

    private void setupObservers() {
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading != null && isLoading) {
                showLoading();
            } else {
                hideLoading();
            }
        });

        viewModel.getTodayWorkouts().observe(getViewLifecycleOwner(), workouts -> {
            if (workouts != null) {
                todayWorkouts.clear();
                todayWorkouts.addAll(workouts);

                String today = getTodayDay();
                List<Workout> incompleteWorkouts = new ArrayList<>();
                for (Workout workout : workouts) {
                    if (!workout.isCompletedForDay(today)) {
                        incompleteWorkouts.add(workout);
                    }
                }

                adapter.updateWorkouts(incompleteWorkouts);
                updateUI();

                // ✅ Show first-time dialog if no workouts and first time
                checkAndShowFirstTimeDialog();
            }
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                showEmptyState();
            }
        });
    }

    private void checkAndShowFirstTimeDialog() {
        if (hasShownFirstTimeDialog) return;

        boolean isFirstTime = false;
        if (getActivity() != null) {
            isFirstTime = getActivity().getSharedPreferences("FitLifePrefs", 0)
                    .getBoolean("isFirstTimeOnToday", true);
        }

        if (isFirstTime && todayWorkouts.isEmpty() && !isGuest()) {
            hasShownFirstTimeDialog = true;

            if (getActivity() != null) {
                getActivity().getSharedPreferences("FitLifePrefs", 0)
                        .edit().putBoolean("isFirstTimeOnToday", false).apply();
            }
        }
    }



    private void updateUI() {
        if (isGuest()) {
            showGuestWelcomeState();
            return;
        }

        String today = getTodayDay();

        List<Workout> incompleteWorkouts = new ArrayList<>();
        for (Workout workout : todayWorkouts) {
            if (!workout.isCompletedForDay(today)) {
                incompleteWorkouts.add(workout);
            }
        }

        if (todayWorkouts.isEmpty()) {
            showEmptyState();
        } else if (incompleteWorkouts.isEmpty()) {
            showAllCompletedState();
        } else {
            showWorkoutsUI();
            adapter.updateWorkouts(incompleteWorkouts);
            updateEquipmentDisplay();
        }
    }

    private void showGuestWelcomeState() {
        if (todayWorkoutRecycler != null) {
            todayWorkoutRecycler.setVisibility(View.GONE);
        }
        if (equipmentCard != null) {
            equipmentCard.setVisibility(View.GONE);
        }

        if (emptyTodayView != null) {
            emptyTodayView.removeAllViews();
            View guestView = LayoutInflater.from(getContext())
                    .inflate(R.layout.dialog_guest_welcome, emptyTodayView, false);

            MaterialButton signUpBtn = guestView.findViewById(R.id.btnSignUp);
            MaterialButton continueBtn = guestView.findViewById(R.id.btnContinueAsGuest);

            signUpBtn.setOnClickListener(v -> {
                if (getActivity() != null) {
                    Intent intent = new Intent(getActivity(), RegisterActivity.class);
                    getActivity().startActivity(intent);
                }
            });

            continueBtn.setOnClickListener(v -> {
                if (getActivity() != null) {
                    android.widget.Toast.makeText(getContext(), "Continue as Guest", android.widget.Toast.LENGTH_SHORT).show();
                }
            });

            emptyTodayView.addView(guestView);
            emptyTodayView.setVisibility(View.VISIBLE);
        }
    }

    private boolean isGuest() {
        if (getActivity() != null) {
            return getActivity().getSharedPreferences("FitLifePrefs", 0)
                    .getBoolean("isGuest", false);
        }
        return false;
    }

    private void showAllCompletedState() {
        if (todayWorkoutRecycler != null) {
            todayWorkoutRecycler.setVisibility(View.GONE);
        }
        if (equipmentCard != null) {
            equipmentCard.setVisibility(View.GONE);
        }

        if (emptyTodayView != null) {
            emptyTodayView.removeAllViews();
            View completedView = LayoutInflater.from(getContext())
                    .inflate(R.layout.empty_state_all_completed, emptyTodayView, false);

            MaterialButton browseBtn = completedView.findViewById(R.id.browseWorkoutsBtn);
            MaterialButton celebrateBtn = completedView.findViewById(R.id.celebrateBtn);

            browseBtn.setOnClickListener(v -> {
                if (getActivity() != null) {
                    ((BaseActivity) getActivity()).setSelectedNavigationItem(R.id.navigation_workout);
                }
            });

            celebrateBtn.setOnClickListener(v -> shareAchievement());

            emptyTodayView.addView(completedView);
            emptyTodayView.setVisibility(View.VISIBLE);
        }
    }

    private void shareAchievement() {
        String today = getTodayDay();
        int completedCount = todayWorkouts.size();

        String message = "🎉 I completed all " + completedCount + " workouts for " + today + "! 💪\n\n" +
                "Track your fitness journey with FitLife App!";

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, message);
        startActivity(Intent.createChooser(shareIntent, "Share Your Achievement"));
    }

    private void showEmptyState() {
        if (todayWorkoutRecycler != null) {
            todayWorkoutRecycler.setVisibility(View.GONE);
        }
        if (equipmentCard != null) {
            equipmentCard.setVisibility(View.GONE);
        }

        if (emptyTodayView != null) {
            emptyTodayView.removeAllViews();

            // ✅ Check if first time user (has no workouts ever added)
            boolean isFirstTime = false;
            if (getActivity() != null) {
                isFirstTime = getActivity().getSharedPreferences("FitLifePrefs", 0)
                        .getBoolean("isFirstTimeOnToday", true);
            }

            View emptyView;
            if (isFirstTime && !isGuest()) {
                emptyView = LayoutInflater.from(getContext())
                        .inflate(R.layout.empty_state_no_workouts, emptyTodayView, false);

                MaterialButton browseBtn = emptyView.findViewById(R.id.browseWorkoutsBtn);
                browseBtn.setOnClickListener(v -> {
                    if (getActivity() != null) {
                        getActivity().getSharedPreferences("FitLifePrefs", 0)
                                .edit().putBoolean("isFirstTimeOnToday", false).apply();
                        ((BaseActivity) getActivity()).setSelectedNavigationItem(R.id.navigation_workout);
                    }
                });
            } else {
                emptyView = LayoutInflater.from(getContext())
                        .inflate(R.layout.empty_state_rest_day, emptyTodayView, false);
            }

            emptyTodayView.addView(emptyView);
            emptyTodayView.setVisibility(View.VISIBLE);
        }
    }

    private void showWorkoutsUI() {
        if (todayWorkoutRecycler != null) {
            todayWorkoutRecycler.setVisibility(View.VISIBLE);
        }
        if (emptyTodayView != null) {
            emptyTodayView.setVisibility(View.GONE);
        }
        if (equipmentCard != null) {
            equipmentCard.setVisibility(View.VISIBLE);
        }
    }

    private void updateEquipmentDisplay() {
        if (equipmentContent == null) return;

        equipmentContent.removeAllViews();

        Map<String, List<String>> categorizedEquipment = new LinkedHashMap<>();
        int totalItems = 0;

        for (Workout workout : todayWorkouts) {
            if (workout.getEquipment() != null) {
                for (String equipment : workout.getEquipment()) {
                    String category = getEquipmentCategory(equipment);
                    if (!categorizedEquipment.containsKey(category)) {
                        categorizedEquipment.put(category, new ArrayList<>());
                    }
                    if (!categorizedEquipment.get(category).contains(equipment)) {
                        categorizedEquipment.get(category).add(equipment);
                        totalItems++;
                    }
                }
            }
        }

        categorizedEquipment.entrySet().removeIf(entry -> entry.getValue().isEmpty());

        for (Map.Entry<String, List<String>> entry : categorizedEquipment.entrySet()) {
            View categoryHeader = getLayoutInflater().inflate(R.layout.item_equipment_category, equipmentContent, false);
            TextView categoryTitle = categoryHeader.findViewById(R.id.categoryTitle);
            categoryTitle.setText(entry.getKey());
            equipmentContent.addView(categoryHeader);

            for (String equipment : entry.getValue()) {
                View itemView = getLayoutInflater().inflate(R.layout.item_equipment, equipmentContent, false);
                TextView equipmentName = itemView.findViewById(R.id.equipmentName);
                equipmentName.setText(equipment);
                equipmentContent.addView(itemView);
            }
        }

        if (equipmentCountText != null) {
            if (totalItems == 0) {
                equipmentCountText.setText("No equipment needed");
                if (equipmentCard != null) {
                    equipmentCard.setVisibility(View.GONE);
                }
            } else {
                equipmentCountText.setText(totalItems + " item" + (totalItems != 1 ? "s" : ""));
            }
        }
    }

    private String getEquipmentCategory(String equipment) {
        String lower = equipment.toLowerCase();
        if (lower.contains("dumbbell") || lower.contains("barbell") || lower.contains("kettlebell") ||
                lower.contains("weight") || lower.contains("bench") || lower.contains("rack")) {
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

    private void shareTodayEquipment() {
        if (todayWorkouts.isEmpty()) {
            if (getActivity() != null) {
                android.widget.Toast.makeText(getActivity(), "No workouts scheduled today", android.widget.Toast.LENGTH_SHORT).show();
            }
            return;
        }

        Map<String, List<String>> categorizedEquipment = new LinkedHashMap<>();
        categorizedEquipment.put("🏋️ Strength", new ArrayList<>());
        categorizedEquipment.put("🏃 Cardio", new ArrayList<>());
        categorizedEquipment.put("🧘 Yoga & Flexibility", new ArrayList<>());
        categorizedEquipment.put("🔧 Accessories", new ArrayList<>());

        for (Workout workout : todayWorkouts) {
            if (workout.getEquipment() != null && !workout.getEquipment().isEmpty()) {
                for (String equipment : workout.getEquipment()) {
                    String category = getEquipmentCategoryForShare(equipment);
                    List<String> categoryList = categorizedEquipment.get(category);
                    if (categoryList != null && !categoryList.contains(equipment)) {
                        categoryList.add(equipment);
                    }
                }
            }
        }

        categorizedEquipment.entrySet().removeIf(entry -> entry.getValue().isEmpty());

        // Build message
        String today = new java.text.SimpleDateFormat("EEEE", java.util.Locale.getDefault()).format(new java.util.Date());
        String date = new java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault()).format(new java.util.Date());

        StringBuilder message = new StringBuilder();
        message.append("🏋️‍♀️ Equipment for ").append(today).append(" (").append(date).append(")\n\n");

        for (Map.Entry<String, List<String>> entry : categorizedEquipment.entrySet()) {
            message.append(entry.getKey()).append(":\n");
            for (String equipment : entry.getValue()) {
                message.append("  • ").append(equipment).append("\n");
            }
            message.append("\n");
        }

        message.append("Workouts today:\n");
        for (Workout workout : todayWorkouts) {
            message.append("  • ").append(workout.getName()).append("\n");
        }

        message.append("\nSent from FitLife App 💪");

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, message.toString());
        startActivity(Intent.createChooser(shareIntent, "Share Equipment List for " + today));
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
                lower.contains("block") || lower.contains("foam roller") || lower.contains("pilates") ||
                lower.contains("yoga mat")) {
            return "🧘 Yoga & Flexibility";
        } else {
            return "🔧 Accessories";
        }
    }

    public void refreshData() {
        viewModel.refresh();
    }
}