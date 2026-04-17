package com.example.fitlife_sumyatnoe.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.fitlife_sumyatnoe.R;
import com.example.fitlife_sumyatnoe.models.Exercise;
import com.example.fitlife_sumyatnoe.utils.ImageLoader;
import com.google.android.material.imageview.ShapeableImageView;

import java.io.File;
import java.util.List;

public class ExerciseAdapter extends RecyclerView.Adapter<ExerciseAdapter.ViewHolder> {

    private List<Exercise> exercises;
    private OnExerciseClickListener listener;

    public interface OnExerciseClickListener {
        void onItemClick(Exercise exercise);
        void onMenuClick(Exercise exercise, View anchor);
    }

    public ExerciseAdapter(List<Exercise> exercises, OnExerciseClickListener listener) {
        this.exercises = exercises;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_exercise, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Exercise exercise = exercises.get(position);
        Log.d("ExerciseAdapter", "Exercise: " + exercise.getName());
        Log.d("ExerciseAdapter", "Image URL: " + exercise.getImageUrl());
        Log.d("ExerciseAdapter", "Local path: " + exercise.getLocalImagePath());
        holder.nameText.setText(exercise.getName());
        holder.detailsText.setText(exercise.getDisplayText());

        loadExerciseImage(holder, exercise);
        if (holder.exerciseImage != null) {
            if (exercise.getLocalImagePath() != null && !exercise.getLocalImagePath().isEmpty()) {
                ImageLoader.loadExerciseImage(holder.itemView.getContext(), exercise.getLocalImagePath(), holder.exerciseImage);
            } else if (exercise.getImageUrl() != null && !exercise.getImageUrl().isEmpty()) {
                ImageLoader.loadExerciseImage(holder.itemView.getContext(), exercise.getImageUrl(), holder.exerciseImage);
            } else {
                holder.exerciseImage.setImageResource(R.drawable.ic_fitness);
            }
        }

        // Item click
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(exercise);
            }
        });

        if (holder.menuButton != null) {
            holder.menuButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onMenuClick(exercise, v);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return exercises != null ? exercises.size() : 0;
    }
    private void loadExerciseImage(ViewHolder holder, Exercise exercise) {
        String imageSource = exercise.getImageUrl();
        String localPath = exercise.getLocalImagePath();

        // Try local path
        if (localPath != null && !localPath.isEmpty()) {
            File imageFile = new File(localPath);
            if (imageFile.exists()) {
                Glide.with(holder.itemView.getContext())
                        .load(imageFile)
                        .placeholder(R.drawable.ic_fitness)
                        .error(R.drawable.ic_fitness)
                        .centerCrop()
                        .into(holder.exerciseImage);
                return;
            }
        }

        // Try drawable resource
        if (imageSource != null && !imageSource.isEmpty()) {
            int resId = holder.itemView.getContext().getResources()
                    .getIdentifier(imageSource, "drawable",
                            holder.itemView.getContext().getPackageName());
            if (resId != 0) {
                Glide.with(holder.itemView.getContext())
                        .load(resId)
                        .placeholder(R.drawable.ic_fitness)
                        .error(R.drawable.ic_fitness)
                        .centerCrop()
                        .into(holder.exerciseImage);
                return;
            }
        }

        // Fallback
        holder.exerciseImage.setImageResource(R.drawable.ic_fitness);
    }

    public void updateExercises(List<Exercise> newExercises) {
        this.exercises = newExercises;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ShapeableImageView exerciseImage;
        TextView nameText;
        TextView detailsText;
        ImageView menuButton;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            exerciseImage = itemView.findViewById(R.id.exerciseImage);
            nameText = itemView.findViewById(R.id.exerciseName);
            detailsText = itemView.findViewById(R.id.exerciseDetails);
            menuButton = itemView.findViewById(R.id.menuButton);
        }
    }
}