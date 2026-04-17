package com.example.fitlife_sumyatnoe.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitlife_sumyatnoe.R;
import com.example.fitlife_sumyatnoe.models.Exercise;

import java.util.List;

public class ExerciseSelectAdapter extends RecyclerView.Adapter<ExerciseSelectAdapter.ViewHolder> {

    private List<Exercise> exercises;
    private List<Exercise> selectedExercises;

    public ExerciseSelectAdapter(List<Exercise> exercises, List<Exercise> selectedExercises) {
        this.exercises = exercises;
        this.selectedExercises = selectedExercises;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_exercise_select, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Exercise exercise = exercises.get(position);

        holder.exerciseName.setText(exercise.getName());
        holder.exerciseDetails.setText(exercise.getDisplayText());

        boolean isSelected = false;
        for (Exercise selected : selectedExercises) {
            if (selected.getName().equals(exercise.getName())) {
                isSelected = true;
                break;
            }
        }

        holder.checkBox.setChecked(isSelected);

        holder.itemView.setOnClickListener(v -> {
            holder.checkBox.toggle();
        });

        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                if (!selectedExercises.contains(exercise)) {
                    selectedExercises.add(exercise);
                }
            } else {
                selectedExercises.remove(exercise);
            }
        });
    }

    @Override
    public int getItemCount() {
        return exercises.size();
    }

    public List<Exercise> getSelectedExercises() {
        return selectedExercises;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView exerciseName, exerciseDetails;
        CheckBox checkBox;

        ViewHolder(View itemView) {
            super(itemView);
            exerciseName = itemView.findViewById(R.id.exerciseName);
            exerciseDetails = itemView.findViewById(R.id.exerciseDetails);
            checkBox = itemView.findViewById(R.id.checkBox);
        }
    }
}