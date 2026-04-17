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
import com.example.fitlife_sumyatnoe.utils.CheckboxHelper;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

public class ActiveExerciseAdapter extends RecyclerView.Adapter<ActiveExerciseAdapter.ViewHolder> {

    private List<Exercise> exercises;
    private OnExerciseClickListener clickListener;
    private OnCheckboxClickListener checkboxListener;
    private boolean checkboxesEnabled = true;

    public interface OnExerciseClickListener {
        void onExerciseClick(Exercise exercise);
    }

    public interface OnCheckboxClickListener {
        void onCheckboxClick(Exercise exercise);
    }

    public ActiveExerciseAdapter(List<Exercise> exercises,
                                 OnExerciseClickListener clickListener,
                                 OnCheckboxClickListener checkboxListener) {
        this.exercises = exercises;
        this.clickListener = clickListener;
        this.checkboxListener = checkboxListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_active_exercise, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Exercise exercise = exercises.get(position);

        holder.exerciseName.setText(exercise.getName());
        holder.exerciseNumber.setText(String.valueOf(position + 1));

        if (exercise.isTimed()) {
            holder.exerciseDetails.setText(exercise.getSets() + " sets × " + exercise.getDuration() + " sec");
        } else {
            holder.exerciseDetails.setText(exercise.getSets() + " sets × " + exercise.getReps() + " reps");
        }

        // Remove previous listener to avoid conflicts
        holder.completeCheckbox.setOnCheckedChangeListener(null);

        // Set checkbox state
        holder.completeCheckbox.setChecked(exercise.isCompleted());
        holder.completeCheckbox.setEnabled(checkboxesEnabled);

        // Set colors based on completion
        if (exercise.isCompleted()) {
            holder.completeCheckbox.setButtonTintList(
                    android.content.res.ColorStateList.valueOf(
                            holder.itemView.getContext().getColor(R.color.secondary)
                    )
            );
            holder.cardView.setCardBackgroundColor(
                    holder.itemView.getContext().getColor(R.color.primary_container)
            );
            holder.exerciseName.setTextColor(
                    holder.itemView.getContext().getColor(R.color.success)
            );
            holder.exerciseDetails.setTextColor(
                    holder.itemView.getContext().getColor(R.color.success)
            );
            holder.exerciseNumber.setTextColor(
                    holder.itemView.getContext().getColor(R.color.success)
            );
        } else {
            holder.completeCheckbox.setButtonTintList(
                    android.content.res.ColorStateList.valueOf(
                            holder.itemView.getContext().getColor(R.color.primary)
                    )
            );
            holder.cardView.setCardBackgroundColor(
                    holder.itemView.getContext().getColor(R.color.surface)
            );
            holder.exerciseName.setTextColor(
                    holder.itemView.getContext().getColor(R.color.text_primary)
            );
            holder.exerciseDetails.setTextColor(
                    holder.itemView.getContext().getColor(R.color.text_secondary)
            );
            holder.exerciseNumber.setTextColor(
                    holder.itemView.getContext().getColor(R.color.text_secondary)
            );
        }

        holder.completeCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (checkboxesEnabled && checkboxListener != null) {
                if (exercise.isCompleted() != isChecked) {
                    checkboxListener.onCheckboxClick(exercise);
                }
            }
        });

        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onExerciseClick(exercise);
            }
        });
    }

    @Override
    public int getItemCount() {
        return exercises != null ? exercises.size() : 0;
    }

    public void updateExercises(List<Exercise> newExercises) {
        this.exercises = newExercises;
        notifyDataSetChanged();
    }

    public void setCheckboxesEnabled(boolean enabled) {
        this.checkboxesEnabled = enabled;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardView;
        TextView exerciseName, exerciseDetails, exerciseNumber;
        CheckBox completeCheckbox;

        ViewHolder(View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            exerciseName = itemView.findViewById(R.id.exerciseName);
            exerciseDetails = itemView.findViewById(R.id.exerciseDetails);
            exerciseNumber = itemView.findViewById(R.id.exerciseNumber);
            completeCheckbox = itemView.findViewById(R.id.completeCheckbox);
        }
    }
}