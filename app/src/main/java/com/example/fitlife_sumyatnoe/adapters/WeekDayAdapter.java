package com.example.fitlife_sumyatnoe.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitlife_sumyatnoe.R;
import com.example.fitlife_sumyatnoe.models.WeekDayItem;
import com.google.android.material.card.MaterialCardView;

import java.util.List;
import java.util.Map;

public class WeekDayAdapter extends RecyclerView.Adapter<WeekDayAdapter.WeekDayViewHolder> {

    private List<WeekDayItem> days;
    private String selectedDay;
    private Map<String, Integer> workoutCounts;
    private OnDayClickListener listener;

    public interface OnDayClickListener {
        void onDayClick(String day);
    }

    public WeekDayAdapter(List<WeekDayItem> days, String selectedDay,
                          Map<String, Integer> workoutCounts,
                          OnDayClickListener listener) {
        this.days = days;
        this.selectedDay = selectedDay;
        this.workoutCounts = workoutCounts;
        this.listener = listener;
    }

    public void setSelectedDay(String selectedDay) {
        this.selectedDay = selectedDay;
        notifyDataSetChanged();
    }

    public void updateWorkoutCounts(Map<String, Integer> workoutCounts) {
        this.workoutCounts = workoutCounts;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public WeekDayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_week_day, parent, false);
        return new WeekDayViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WeekDayViewHolder holder, int position) {
        WeekDayItem item = days.get(position);
        String dayName = item.dayName;
        String dateStr = item.date;

        holder.dayName.setText(dayName.substring(0, 3).toUpperCase());
        holder.dayDate.setText(dateStr);

        // Set workout count
        int count = workoutCounts != null && workoutCounts.containsKey(dayName) ?
                workoutCounts.get(dayName) : 0;
        holder.workoutCount.setText(String.valueOf(count));

        // Check if this is the selected day
        boolean isSelected = dayName.equals(selectedDay);
        if (isSelected) {
            holder.dayCard.setCardBackgroundColor(holder.itemView.getContext().getColor(R.color.primary));
            holder.dayName.setTextColor(holder.itemView.getContext().getColor(R.color.on_primary));
            holder.dayDate.setTextColor(holder.itemView.getContext().getColor(R.color.on_primary));
            holder.workoutCount.setTextColor(holder.itemView.getContext().getColor(R.color.on_primary));
            holder.activeDot.setVisibility(View.VISIBLE);
        } else {
            holder.dayCard.setCardBackgroundColor(holder.itemView.getContext().getColor(R.color.surface));
            holder.dayName.setTextColor(holder.itemView.getContext().getColor(R.color.primary));
            holder.dayDate.setTextColor(holder.itemView.getContext().getColor(R.color.text_secondary));
            holder.workoutCount.setTextColor(holder.itemView.getContext().getColor(R.color.text_primary));
            holder.activeDot.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDayClick(dayName);
            }
        });

        holder.dayCard.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDayClick(dayName);
            }
        });
    }

    @Override
    public int getItemCount() {
        return days.size();
    }

    static class WeekDayViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView dayCard;
        TextView dayName;
        TextView dayDate;
        TextView workoutCount;
        View activeDot;

        WeekDayViewHolder(@NonNull View itemView) {
            super(itemView);
            dayCard = itemView.findViewById(R.id.dayCard);
            dayName = itemView.findViewById(R.id.dayName);
            dayDate = itemView.findViewById(R.id.dayDate);
            workoutCount = itemView.findViewById(R.id.workoutCount);
            activeDot = itemView.findViewById(R.id.activeDot);
        }
    }
}