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

public class GoalChipAdapter extends RecyclerView.Adapter<GoalChipAdapter.ViewHolder> {

    private String[] goals;
    private List<String> selectedGoals;
    private OnGoalSelectedListener listener;

    public interface OnGoalSelectedListener {
        void onGoalSelected(String goal, boolean isSelected);
    }

    public GoalChipAdapter(String[] goals, List<String> selectedGoals, OnGoalSelectedListener listener) {
        this.goals = goals;
        this.selectedGoals = selectedGoals;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_goal_chip, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String goal = goals[position];
        holder.chip.setText(goal);

        boolean isSelected = selectedGoals != null && selectedGoals.contains(goal);
        holder.chip.setChecked(isSelected);

        if (isSelected) {
            holder.chip.setChipBackgroundColor(android.content.res.ColorStateList.valueOf(
                    holder.itemView.getContext().getColor(R.color.primary)
            ));
            holder.chip.setTextColor(holder.itemView.getContext().getColor(R.color.on_primary));
        } else {
            holder.chip.setChipBackgroundColor(android.content.res.ColorStateList.valueOf(
                    holder.itemView.getContext().getColor(R.color.surface_variant)
            ));
            holder.chip.setTextColor(holder.itemView.getContext().getColor(R.color.text_primary));
        }

        holder.chip.setOnCheckedChangeListener(null);
        holder.chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (listener != null) {
                listener.onGoalSelected(goal, isChecked);
            }
            if (isChecked) {
                holder.chip.setChipBackgroundColor(android.content.res.ColorStateList.valueOf(
                        holder.itemView.getContext().getColor(R.color.primary)
                ));
                holder.chip.setTextColor(holder.itemView.getContext().getColor(R.color.on_primary));
            } else {
                holder.chip.setChipBackgroundColor(android.content.res.ColorStateList.valueOf(
                        holder.itemView.getContext().getColor(R.color.surface_variant)
                ));
                holder.chip.setTextColor(holder.itemView.getContext().getColor(R.color.text_primary));
            }
        });
    }

    @Override
    public int getItemCount() {
        return goals.length;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        Chip chip;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            chip = itemView.findViewById(R.id.chip);
        }
    }
}