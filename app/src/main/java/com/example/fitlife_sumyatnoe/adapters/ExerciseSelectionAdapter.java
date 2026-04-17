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

import java.util.ArrayList;
import java.util.List;

public class ExerciseSelectionAdapter extends RecyclerView.Adapter<ExerciseSelectionAdapter.ViewHolder> {

    private List<Exercise> allExercises;
    private List<Exercise> selectedExercises;
    private List<Boolean> checkedStates;

    public ExerciseSelectionAdapter(List<Exercise> allExercises, List<Exercise> selectedExercises) {
        this.allExercises = allExercises;
        this.selectedExercises = selectedExercises;
        this.checkedStates = new ArrayList<>();

        for (Exercise exercise : allExercises) {
            boolean isSelected = false;
            for (Exercise selected : selectedExercises) {
                if (selected.getName().equals(exercise.getName())) {
                    isSelected = true;
                    break;
                }
            }
            checkedStates.add(isSelected);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_exercise_selection, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Exercise exercise = allExercises.get(position);

        holder.exerciseName.setText(exercise.getName());
        holder.exerciseDetails.setText(exercise.getDisplayText());
        holder.checkBox.setChecked(checkedStates.get(position));

        holder.itemView.setOnClickListener(v -> {
            boolean newState = !checkedStates.get(position);
            checkedStates.set(position, newState);
            holder.checkBox.setChecked(newState);
        });
    }

    @Override
    public int getItemCount() {
        return allExercises.size();
    }

    public List<Exercise> getSelectedExercises() {
        List<Exercise> selected = new ArrayList<>();
        for (int i = 0; i < allExercises.size(); i++) {
            if (checkedStates.get(i)) {
                selected.add(allExercises.get(i));
            }
        }
        return selected;
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