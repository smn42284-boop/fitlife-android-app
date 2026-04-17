package com.example.fitlife_sumyatnoe.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitlife_sumyatnoe.R;
import com.example.fitlife_sumyatnoe.models.EquipmentItem;

import java.util.ArrayList;
import java.util.List;

public class EquipmentCardAdapter extends RecyclerView.Adapter<EquipmentCardAdapter.ViewHolder> {

    private List<EquipmentItem> equipmentList;
    private List<Boolean> checkedStates; // Track checked states separately
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(EquipmentItem item, int position);
        void onCheckChanged(EquipmentItem item, int position, boolean isChecked);
    }

    public EquipmentCardAdapter(List<EquipmentItem> equipmentList, OnItemClickListener listener) {
        this.equipmentList = equipmentList;
        this.listener = listener;
        this.checkedStates = new ArrayList<>();

        // Initialize all as unchecked
        for (int i = 0; i < equipmentList.size(); i++) {
            checkedStates.add(false);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_equipment_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        EquipmentItem equipment = equipmentList.get(position);

        holder.nameText.setText(equipment.getName());
        holder.categoryText.setText(equipment.getCategory());

        // Use the tracked checked state instead of equipment.isChecked()
        boolean isChecked = checkedStates.get(position);
        holder.checkBox.setChecked(isChecked);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(equipment, position);
            }
        });

        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked1) -> {
            // Update the tracked state
            checkedStates.set(position, isChecked1);
            if (listener != null) {
                listener.onCheckChanged(equipment, position, isChecked1);
            }
        });
    }

    @Override
    public int getItemCount() {
        return equipmentList.size();
    }

    // Method to get selected items
    public List<EquipmentItem> getSelectedItems() {
        List<EquipmentItem> selected = new ArrayList<>();
        for (int i = 0; i < equipmentList.size(); i++) {
            if (checkedStates.get(i)) {
                selected.add(equipmentList.get(i));
            }
        }
        return selected;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameText, categoryText;
        CheckBox checkBox;

        ViewHolder(View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.equipmentName);
            categoryText = itemView.findViewById(R.id.equipmentCategory);
            checkBox = itemView.findViewById(R.id.itemCheckbox);        }
    }
}