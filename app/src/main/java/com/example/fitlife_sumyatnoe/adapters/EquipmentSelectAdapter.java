package com.example.fitlife_sumyatnoe.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitlife_sumyatnoe.R;
import com.example.fitlife_sumyatnoe.models.Equipment;

import java.util.List;

public class EquipmentSelectAdapter extends RecyclerView.Adapter<EquipmentSelectAdapter.ViewHolder> {

    private List<Equipment> equipmentList;
    private List<String> selectedEquipmentNames;

    public EquipmentSelectAdapter(List<Equipment> equipmentList, List<String> selectedEquipmentNames) {
        this.equipmentList = equipmentList;
        this.selectedEquipmentNames = selectedEquipmentNames;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_equipment_select, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Equipment equipment = equipmentList.get(position);

        holder.equipmentName.setText(equipment.getName());

        boolean isSelected = selectedEquipmentNames.contains(equipment.getName());
        holder.checkBox.setChecked(isSelected);

        holder.itemView.setOnClickListener(v -> {
            holder.checkBox.toggle();
        });

        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                if (!selectedEquipmentNames.contains(equipment.getName())) {
                    selectedEquipmentNames.add(equipment.getName());
                }
            } else {
                selectedEquipmentNames.remove(equipment.getName());
            }
        });
    }

    @Override
    public int getItemCount() {
        return equipmentList.size();
    }

    public List<String> getSelectedEquipment() {
        return selectedEquipmentNames;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView equipmentName;
        CheckBox checkBox;

        ViewHolder(View itemView) {
            super(itemView);
            equipmentName = itemView.findViewById(R.id.equipmentName);
            checkBox = itemView.findViewById(R.id.checkBox);
        }
    }
}