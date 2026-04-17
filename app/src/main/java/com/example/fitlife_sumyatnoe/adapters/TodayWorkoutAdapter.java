package com.example.fitlife_sumyatnoe.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.fitlife_sumyatnoe.R;
import com.example.fitlife_sumyatnoe.models.Workout;
import com.example.fitlife_sumyatnoe.utils.ImageLoader;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TodayWorkoutAdapter extends RecyclerView.Adapter<TodayWorkoutAdapter.WorkoutViewHolder> {

    private List<Workout> workouts;
    private OnWorkoutClickListener listener;
    private boolean isGuestMode = false;

    public interface OnWorkoutClickListener {
        void onWorkoutClick(Workout workout);
        void onMenuClick(Workout workout, View anchor);
        void onStartClick(Workout workout, int position);
        void onDetailsClick(Workout workout);
    }

    public TodayWorkoutAdapter(List<Workout> workouts, OnWorkoutClickListener listener) {
        this.workouts = workouts;
        this.listener = listener;
    }

    public void setGuestMode(boolean isGuest) {
        this.isGuestMode = isGuest;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public WorkoutViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_common_workout, parent, false);
        return new WorkoutViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WorkoutViewHolder holder, int position) {
        Workout workout = workouts.get(position);

        holder.workoutName.setText(workout.getName());
        holder.workoutDuration.setText(workout.getDuration() + " min");

        String exercisesPreview = workout.getExercisesPreview();
        holder.exercisesPreview.setText(exercisesPreview);

        String equipmentPreview = workout.getEquipmentPreview();
        holder.equipmentPreview.setText(equipmentPreview);

        loadWorkoutImage(holder, workout);

        // Clear previous buttons
        holder.actionButtonsContainer.removeAllViews();
        holder.actionButtonsContainer.setVisibility(View.VISIBLE);

        // Get today's date
        String today = new SimpleDateFormat("EEEE", Locale.getDefault()).format(new Date());
        boolean isCompletedForToday = workout.isCompletedForDay(today);

        if (isGuestMode) {
            // Guest mode - single button
            holder.completedBadge.setVisibility(View.GONE);
            holder.completedText.setVisibility(View.GONE);

            MaterialButton signUpBtn = createButton(holder, "Sign Up to Add to Plan", R.drawable.ic_person, true);
            signUpBtn.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    44));
            holder.actionButtonsContainer.addView(signUpBtn);

        } else if (isCompletedForToday) {
            // ✅ WORKOUT COMPLETED FOR TODAY - Show completed badge
            holder.completedBadge.setVisibility(View.VISIBLE);
            holder.completedText.setVisibility(View.VISIBLE);
            holder.completedText.setText("✓ Completed Today");
            holder.actionButtonsContainer.setVisibility(View.GONE);

        } else if (workout.isAdded()) {
            // In plan but not completed - Show Start and Details buttons
            holder.completedBadge.setVisibility(View.GONE);
            holder.completedText.setVisibility(View.GONE);

            View buttonsView = LayoutInflater.from(holder.itemView.getContext())
                    .inflate(R.layout.item_action_buttons, holder.actionButtonsContainer, false);

            MaterialButton startBtn = buttonsView.findViewById(R.id.btnPrimary);
            MaterialButton detailsBtn = buttonsView.findViewById(R.id.btnSecondary);

            // Style Start button
            startBtn.setText("Start");
            startBtn.setIconResource(R.drawable.ic_play);
            startBtn.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(
                            holder.itemView.getContext().getColor(R.color.primary)
                    )
            );
            startBtn.setTextColor(holder.itemView.getContext().getColor(R.color.on_primary));
            startBtn.setIconTint(
                    android.content.res.ColorStateList.valueOf(
                            holder.itemView.getContext().getColor(R.color.on_primary)
                    )
            );
            startBtn.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onStartClick(workout, position);
                }
            });

            // Style Details button
            detailsBtn.setText("Details");
            detailsBtn.setIconResource(R.drawable.ic_info);
            detailsBtn.setStrokeColor(
                    android.content.res.ColorStateList.valueOf(
                            holder.itemView.getContext().getColor(R.color.primary)
                    )
            );
            detailsBtn.setStrokeWidth(2);
            detailsBtn.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(
                            android.R.color.transparent
                    )
            );
            detailsBtn.setTextColor(holder.itemView.getContext().getColor(R.color.primary));
            detailsBtn.setIconTint(
                    android.content.res.ColorStateList.valueOf(
                            holder.itemView.getContext().getColor(R.color.primary)
                    )
            );
            detailsBtn.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDetailsClick(workout);
                }
            });

            holder.actionButtonsContainer.addView(buttonsView);

        } else {
            // Not in plan - Show Add to Plan and Details
            holder.completedBadge.setVisibility(View.GONE);
            holder.completedText.setVisibility(View.GONE);

            View buttonsView = LayoutInflater.from(holder.itemView.getContext())
                    .inflate(R.layout.item_action_buttons, holder.actionButtonsContainer, false);

            MaterialButton addBtn = buttonsView.findViewById(R.id.btnPrimary);
            MaterialButton detailsBtn = buttonsView.findViewById(R.id.btnSecondary);

            // Style Add button
            addBtn.setText("Add to Plan");
            addBtn.setIconResource(R.drawable.ic_add);
            addBtn.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(
                            holder.itemView.getContext().getColor(R.color.primary)
                    )
            );
            addBtn.setTextColor(holder.itemView.getContext().getColor(R.color.on_primary));
            addBtn.setIconTint(
                    android.content.res.ColorStateList.valueOf(
                            holder.itemView.getContext().getColor(R.color.on_primary)
                    )
            );
            addBtn.setOnClickListener(v -> {
                // Handle add to plan
                if (listener != null) {
                    // You can add a callback here or show day selection dialog
                }
            });

            // Style Details button
            detailsBtn.setText("Details");
            detailsBtn.setIconResource(R.drawable.ic_info);
            detailsBtn.setStrokeColor(
                    android.content.res.ColorStateList.valueOf(
                            holder.itemView.getContext().getColor(R.color.primary)
                    )
            );
            detailsBtn.setStrokeWidth(2);
            detailsBtn.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(
                            android.R.color.transparent
                    )
            );
            detailsBtn.setTextColor(holder.itemView.getContext().getColor(R.color.primary));
            detailsBtn.setIconTint(
                    android.content.res.ColorStateList.valueOf(
                            holder.itemView.getContext().getColor(R.color.primary)
                    )
            );
            detailsBtn.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDetailsClick(workout);
                }
            });

            holder.actionButtonsContainer.addView(buttonsView);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onWorkoutClick(workout);
            }
        });

        if (holder.menuButton != null) {
            holder.menuButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onMenuClick(workout, v);
                }
            });
        }
    }

    private MaterialButton createButton(WorkoutViewHolder holder, String text, int iconRes, boolean isPrimary) {
        MaterialButton button = new MaterialButton(holder.itemView.getContext());
        button.setText(text);
        button.setIconResource(iconRes);
        button.setIconGravity(MaterialButton.ICON_GRAVITY_TEXT_START);
        button.setCornerRadius(22);
        button.setTextSize(12);
        button.setAllCaps(false);

        if (isPrimary) {
            button.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(
                            holder.itemView.getContext().getColor(R.color.primary)
                    )
            );
            button.setTextColor(holder.itemView.getContext().getColor(R.color.on_primary));
            button.setIconTint(
                    android.content.res.ColorStateList.valueOf(
                            holder.itemView.getContext().getColor(R.color.on_primary)
                    )
            );
            button.setElevation(4f);
        } else {
            button.setStrokeColor(
                    android.content.res.ColorStateList.valueOf(
                            holder.itemView.getContext().getColor(R.color.primary)
                    )
            );
            button.setStrokeWidth(2);
            button.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(
                            android.R.color.transparent
                    )
            );
            button.setTextColor(holder.itemView.getContext().getColor(R.color.primary));
            button.setIconTint(
                    android.content.res.ColorStateList.valueOf(
                            holder.itemView.getContext().getColor(R.color.primary)
                    )
            );
            button.setElevation(2f);
        }

        return button;
    }

    private void loadWorkoutImage(WorkoutViewHolder holder, Workout workout) {
        if (workout.getLocalImagePath() != null && !workout.getLocalImagePath().isEmpty()) {
            File imageFile = new File(workout.getLocalImagePath());
            if (imageFile.exists()) {
                Glide.with(holder.itemView.getContext())
                        .load(imageFile)
                        .placeholder(R.drawable.workout_placeholder)
                        .error(R.drawable.workout_placeholder)
                        .centerCrop()
                        .into(holder.workoutImage);
                return;
            }
        }

        if (workout.getImageUrl() != null && !workout.getImageUrl().isEmpty()) {
            ImageLoader.loadWorkoutImage(holder.itemView.getContext(), workout.getImageUrl(), holder.workoutImage);
            return;
        }

        holder.workoutImage.setImageResource(R.drawable.workout_placeholder);
    }

    @Override
    public int getItemCount() {
        return workouts != null ? workouts.size() : 0;
    }

    public void updateWorkouts(List<Workout> newWorkouts) {
        this.workouts = newWorkouts;
        notifyDataSetChanged();
    }

    static class WorkoutViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardView;
        ImageView workoutImage, menuButton;
        TextView workoutName, workoutDuration, exercisesPreview, equipmentPreview, completedText;
        View completedBadge;
        LinearLayout actionButtonsContainer;

        WorkoutViewHolder(View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            workoutImage = itemView.findViewById(R.id.workoutImage);
            workoutName = itemView.findViewById(R.id.workoutName);
            workoutDuration = itemView.findViewById(R.id.workoutDuration);
            exercisesPreview = itemView.findViewById(R.id.exercisesPreview);
            equipmentPreview = itemView.findViewById(R.id.equipmentPreview);
            menuButton = itemView.findViewById(R.id.menuButton);
            completedBadge = itemView.findViewById(R.id.completedBadge);
            completedText = itemView.findViewById(R.id.completedText);
            actionButtonsContainer = itemView.findViewById(R.id.actionButtonsContainer);
        }
    }
}