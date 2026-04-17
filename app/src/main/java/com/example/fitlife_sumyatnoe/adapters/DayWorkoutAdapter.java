package com.example.fitlife_sumyatnoe.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.fitlife_sumyatnoe.R;
import com.example.fitlife_sumyatnoe.models.Workout;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.imageview.ShapeableImageView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DayWorkoutAdapter extends RecyclerView.Adapter<DayWorkoutAdapter.WorkoutViewHolder> {

    private List<Workout> workouts;
    private String selectedDay;
    private OnWorkoutClickListener listener;
    private boolean isGesturing = false;
    private View currentSwipingView = null;

    public interface OnWorkoutClickListener {
        void onWorkoutClick(Workout workout);
        void onStartClick(Workout workout);
        void onMenuClick(Workout workout, View anchor);
        void onRemoveFromDay(Workout workout);
    }

    public DayWorkoutAdapter(List<Workout> workouts, String selectedDay, OnWorkoutClickListener listener) {
        this.workouts = workouts;
        this.selectedDay = selectedDay;
        this.listener = listener;
    }

    public void setGesturing(boolean gesturing) {
        this.isGesturing = gesturing;
    }

    public void resetSwipedView() {
        if (currentSwipingView != null) {
            currentSwipingView.animate()
                    .translationX(0)
                    .alpha(1)
                    .setDuration(200)
                    .start();
            currentSwipingView = null;
        }
    }

    @NonNull
    @Override
    public WorkoutViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_day_workout, parent, false);
        return new WorkoutViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WorkoutViewHolder holder, int position) {
        Workout workout = workouts.get(position);
        currentSwipingView = holder.itemView;

        boolean isToday = isToday(selectedDay);
        boolean isCompletedForSelectedDay = workout.isCompletedForDay(selectedDay);

        holder.workoutName.setText(workout.getName());
        holder.workoutDuration.setText(workout.getDuration() + " min");

        int exerciseCount = workout.getExercisesCount();
        holder.exerciseCount.setText(exerciseCount + " exercise" + (exerciseCount != 1 ? "s" : ""));
        if (workout.isCustom()) {
            holder.customBadge.setVisibility(View.VISIBLE);
        } else {
            holder.customBadge.setVisibility(View.GONE);
        }
        if (isCompletedForSelectedDay) {
            holder.completedText.setVisibility(View.VISIBLE);
            holder.completedText.setText("✓ Completed for " + selectedDay);
            holder.startButton.setVisibility(View.GONE);
            holder.detailsButton.setVisibility(View.VISIBLE);
            holder.detailsButton.setText("Details");
            holder.cardView.setAlpha(0.7f);
        } else if (isToday) {
            holder.completedText.setVisibility(View.GONE);
            holder.startButton.setVisibility(View.VISIBLE);
            holder.detailsButton.setVisibility(View.VISIBLE);
            holder.detailsButton.setText("Details");
            holder.cardView.setAlpha(1.0f);
        } else {
            holder.completedText.setVisibility(View.GONE);
            holder.startButton.setVisibility(View.GONE);
            holder.detailsButton.setVisibility(View.VISIBLE);
            holder.detailsButton.setText("Details");
            holder.cardView.setAlpha(1.0f);
        }

        loadWorkoutImage(holder, workout);
        holder.cardView.setClickable(false);
        holder.itemView.setClickable(false);
        holder.startButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onStartClick(workout);
            }
        });

        holder.detailsButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onWorkoutClick(workout);
            }
        });

        holder.menuButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onMenuClick(workout, v);
            }
        });
    }

    private boolean isToday(String day) {
        SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", Locale.getDefault());
        String currentDay = dayFormat.format(new Date());
        return day.equals(currentDay);
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
            int resId = holder.itemView.getContext().getResources()
                    .getIdentifier(workout.getImageUrl(), "drawable",
                            holder.itemView.getContext().getPackageName());
            if (resId != 0) {
                Glide.with(holder.itemView.getContext())
                        .load(resId)
                        .placeholder(R.drawable.workout_placeholder)
                        .error(R.drawable.workout_placeholder)
                        .centerCrop()
                        .into(holder.workoutImage);
                return;
            }
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

    public void setSelectedDay(String day) {
        this.selectedDay = day;
        notifyDataSetChanged();
    }

    public Workout getWorkoutAt(int position) {
        if (workouts != null && position >= 0 && position < workouts.size()) {
            return workouts.get(position);
        }
        return null;
    }

    static class WorkoutViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardView;
        ShapeableImageView workoutImage;
        TextView workoutName;
        TextView workoutDuration;
        TextView exerciseCount;
        TextView customBadge;
        TextView completedText;
        MaterialButton startButton;
        MaterialButton detailsButton;
        ImageView menuButton;

        WorkoutViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            workoutImage = itemView.findViewById(R.id.workoutImage);
            workoutName = itemView.findViewById(R.id.workoutName);
            workoutDuration = itemView.findViewById(R.id.workoutDuration);
            customBadge = itemView.findViewById(R.id.customBadge);
            exerciseCount = itemView.findViewById(R.id.exerciseCount);
            completedText = itemView.findViewById(R.id.completedText);
            startButton = itemView.findViewById(R.id.startButton);
            detailsButton = itemView.findViewById(R.id.detailsButton);
            menuButton = itemView.findViewById(R.id.menuButton);
        }
    }
}