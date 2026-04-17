package com.example.fitlife_sumyatnoe.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitlife_sumyatnoe.R;
import com.google.android.material.chip.Chip;

import java.util.List;

public class WorkoutTypeAdapter extends RecyclerView.Adapter<WorkoutTypeAdapter.ViewHolder> {

    private String[] workoutTypes;
    private List<String> selectedWorkouts;
    private OnWorkoutSelectedListener listener;

    public interface OnWorkoutSelectedListener {
        void onWorkoutSelected(String workout, boolean isSelected);
    }

    public WorkoutTypeAdapter(String[] workoutTypes, List<String> selectedWorkouts, OnWorkoutSelectedListener listener) {
        this.workoutTypes = workoutTypes;
        this.selectedWorkouts = selectedWorkouts;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_workout_chip, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String workout = workoutTypes[position];
        holder.chip.setText(workout);

        boolean isSelected = selectedWorkouts != null && selectedWorkouts.contains(workout);
        holder.chip.setChecked(isSelected);

        if (isSelected) {
            holder.chip.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.on_primary));
        } else {
            holder.chip.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.text_primary));
        }

        holder.chip.setOnCheckedChangeListener(null);
        holder.chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (listener != null) {
                listener.onWorkoutSelected(workout, isChecked);
            }
            // Update text color
            if (isChecked) {
                holder.chip.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.on_primary));
            } else {
                holder.chip.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.text_primary));
            }
        });
    }

    @Override
    public int getItemCount() {
        return workoutTypes.length;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        Chip chip;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            chip = itemView.findViewById(R.id.chip);
        }
    }
}