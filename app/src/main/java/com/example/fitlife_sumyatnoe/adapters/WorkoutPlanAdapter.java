package com.example.fitlife_sumyatnoe.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitlife_sumyatnoe.R;
import com.example.fitlife_sumyatnoe.models.Workout;

import java.util.List;

public class WorkoutPlanAdapter extends RecyclerView.Adapter<WorkoutPlanAdapter.ViewHolder> {

    private List<Workout> workouts;

    public WorkoutPlanAdapter(List<Workout> workouts) {
        this.workouts = workouts;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_workout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Workout workout = workouts.get(position);

        holder.workoutName.setText(workout.getName());

        // Fix: Use getExercises().size() directly without workoutDetails TextView
        // You can combine this with the duration if needed
        String details = workout.getExercises().size() + " exercises • " + workout.getDuration() + " min";

        // If you have a details TextView, use it
        if (holder.workoutDetails != null) {
            holder.workoutDetails.setText(details);
        }

        // If you have a separate duration TextView
        if (holder.workoutDuration != null) {
            holder.workoutDuration.setText(workout.getDuration() + " min");
        }
    }

    @Override
    public int getItemCount() {
        return workouts.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView workoutName, workoutDetails, workoutDuration;

        ViewHolder(View itemView) {
            super(itemView);
            workoutName = itemView.findViewById(R.id.workoutName);

            workoutDetails = itemView.findViewById(R.id.workoutDescription);
            workoutDuration = itemView.findViewById(R.id.workoutDuration);
        }
    }
}