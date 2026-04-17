package com.example.fitlife_sumyatnoe.adapters;

import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.fitlife_sumyatnoe.R;
import com.example.fitlife_sumyatnoe.models.Workout;
import com.google.android.material.card.MaterialCardView;

import java.io.File;
import java.util.List;

public class WorkoutAdapter extends RecyclerView.Adapter<WorkoutAdapter.WorkoutViewHolder> {

    private List<Workout> workouts;
    private OnWorkoutClickListener listener;

    public interface OnWorkoutClickListener {
        void onWorkoutClick(Workout workout);
        void onMenuClick(Workout workout, View anchor);
        void onAddToPlanClick(Workout workout);
        void onRemoveFromPlanClick(Workout workout);
    }

    public WorkoutAdapter(List<Workout> workouts, OnWorkoutClickListener listener) {
        this.workouts = workouts;
        this.listener = listener;
    }

    @NonNull
    @Override
    public WorkoutViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_workout, parent, false);
        return new WorkoutViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WorkoutViewHolder holder, int position) {
        Workout workout = workouts.get(position);
        String workoutId = workout.getId();
        holder.workoutImage.setTag(workoutId);

        holder.workoutImage.setImageDrawable(null);
        boolean isCustomWorkout = workout.isCustom() ||
                (workout.getId() != null && workout.getId().startsWith("offline_"));

        if (holder.customBadge != null) {
            if (isCustomWorkout) {
                holder.customBadge.setVisibility(View.VISIBLE);
                Log.d("WorkoutAdapter", "Showing custom badge for: " + workout.getName());
            } else {
                holder.customBadge.setVisibility(View.GONE);
            }
        }
        loadWorkoutImage(holder, workout);
        // Set text data
        holder.workoutName.setText(workout.getName());
        holder.workoutDuration.setText(workout.getDuration() + " min");

        String exercisesPreview = workout.getExercisesPreview();
        holder.exercisesPreview.setText(exercisesPreview);

        String equipmentPreview = workout.getEquipmentPreview();
        holder.equipmentPreview.setText(equipmentPreview);
        if (workout.isCustom()) {
            if (holder.customBadge != null) {
                holder.customBadge.setVisibility(View.VISIBLE);
            }
        } else {
            if (holder.customBadge != null) {
                holder.customBadge.setVisibility(View.GONE);
            }
        }

        loadWorkoutImage(holder, workout);
        if (workout.isAdded()) {
            holder.cardView.setCardBackgroundColor(holder.itemView.getContext().getColor(R.color.surface));
            holder.addButton.setVisibility(View.GONE);
            holder.removeButton.setVisibility(View.VISIBLE);
            holder.removeButton.setText("Remove from Plan");
        } else {
            holder.cardView.setCardBackgroundColor(holder.itemView.getContext().getColor(R.color.surface));
            holder.addButton.setVisibility(View.VISIBLE);
            holder.removeButton.setVisibility(View.GONE);
            holder.addButton.setText("+ Add to Plan");
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onWorkoutClick(workout);
            }
        });


        holder.menuButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onMenuClick(workout, v);
            }
        });

        holder.addButton.setOnClickListener(v -> {
            if (listener != null && !workout.isCompleted() && !workout.isAdded()) {
                listener.onAddToPlanClick(workout);
            }
        });

        holder.removeButton.setOnClickListener(v -> {
            if (listener != null && workout.isAdded()) {
                listener.onRemoveFromPlanClick(workout);
            }
        });
    }

    private void loadWorkoutImage(WorkoutViewHolder holder, Workout workout) {
        String localPath = workout.getLocalImagePath();
        String imageUrl = workout.getImageUrl();

        RequestOptions options = new RequestOptions()
                .placeholder(R.drawable.workout_placeholder)
                .error(R.drawable.workout_placeholder)
                .centerCrop()
                .override(400, 400);

        if (localPath != null && !localPath.isEmpty()) {
            File imageFile = new File(localPath);
            if (imageFile.exists()) {
                Log.d("WorkoutAdapter", "Loading from local: " + localPath +
                        " size: " + imageFile.length() / 1024 + "KB");
                Glide.with(holder.itemView.getContext())
                        .load(imageFile)
                        .apply(options)
                        .into(holder.workoutImage);
                return;
            }
        }

        if (imageUrl != null && !imageUrl.isEmpty()) {
            int resId = holder.itemView.getContext().getResources()
                    .getIdentifier(imageUrl, "drawable",
                            holder.itemView.getContext().getPackageName());
            if (resId != 0) {
                Glide.with(holder.itemView.getContext())
                        .load(resId)
                        .apply(options)
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
        // Sort: not added first, then added
        newWorkouts.sort((w1, w2) -> {
            if (w1.isAdded() == w2.isAdded()) return 0;
            return w1.isAdded() ? 1 : -1;
        });
        this.workouts = newWorkouts;
        notifyDataSetChanged();
    }

    static class WorkoutViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardView;
        ImageView workoutImage, menuButton;
        TextView workoutName, workoutDuration, exercisesPreview, equipmentPreview, completedDateText;
        TextView addButton, removeButton ,customBadge;


        WorkoutViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            workoutImage = itemView.findViewById(R.id.workoutImage);
            workoutName = itemView.findViewById(R.id.workoutName);
            workoutDuration = itemView.findViewById(R.id.workoutDuration);
            exercisesPreview = itemView.findViewById(R.id.exercisesPreview);
            equipmentPreview = itemView.findViewById(R.id.equipmentPreview);
            menuButton = itemView.findViewById(R.id.menuButton);
            addButton = itemView.findViewById(R.id.addButton);
            removeButton = itemView.findViewById(R.id.removeButton);
            customBadge = itemView.findViewById(R.id.customBadge);
        }
    }

}