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
import java.util.Map;

public class PlanAdapter extends RecyclerView.Adapter<PlanAdapter.ViewHolder> {

    private Map<String, List<Workout>> weeklyPlan;
    private String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};

    public PlanAdapter(Map<String, List<Workout>> weeklyPlan) {
        this.weeklyPlan = weeklyPlan;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_plan_day, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String day = days[position];
        List<Workout> workouts = weeklyPlan.get(day);

        holder.dayText.setText(day);

        if (workouts == null || workouts.isEmpty()) {
            holder.workoutText.setText("Rest Day");
            holder.workoutText.setTextColor(holder.itemView.getContext()
                    .getColor(R.color.text_secondary));
        } else {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < workouts.size(); i++) {
                sb.append(workouts.get(i).getName());
                if (i < workouts.size() - 1) sb.append(", ");
            }
            holder.workoutText.setText(sb.toString());
            holder.workoutText.setTextColor(holder.itemView.getContext()
                    .getColor(R.color.primary));
        }
    }

    @Override
    public int getItemCount() {
        return days.length;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView dayText, workoutText;

        ViewHolder(View itemView) {
            super(itemView);
            dayText = itemView.findViewById(R.id.dayText);
            workoutText = itemView.findViewById(R.id.workoutText);
        }
    }
}