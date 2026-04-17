package com.example.fitlife_sumyatnoe.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitlife_sumyatnoe.R;
import com.example.fitlife_sumyatnoe.models.EquipmentItem;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

public class EquipmentAdapter extends RecyclerView.Adapter<EquipmentAdapter.ViewHolder> {

    private Context context;
    private List<EquipmentItem> equipmentList;
    private List<EquipmentItem> selectedItems = new ArrayList<>(); // Track selected items
    private OnEquipmentClickListener listener;
    private boolean isSelectionMode = false;


    public interface OnEquipmentClickListener {
        void onItemClick(EquipmentItem equipment, int position);
        void onItemLongClick(EquipmentItem equipment, int position);
        void onMoreClick(EquipmentItem equipment, int position);
        void onSelectionChanged(List<EquipmentItem> selectedItems);
    }

    public EquipmentAdapter(Context context, List<EquipmentItem> equipmentList, OnEquipmentClickListener listener) {
        this.context = context;
        this.equipmentList = equipmentList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_equipment_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        EquipmentItem equipment = equipmentList.get(position);

        holder.name.setText(equipment.getName());
        holder.category.setText(equipment.getCategory());

        // Check if this item is selected
        boolean isSelected = selectedItems.contains(equipment);

        // Update UI based on selection
        if (isSelected) {
            holder.cardView.setStrokeColor(context.getColor(R.color.primary));
            holder.cardView.setStrokeWidth(4);
            holder.selectionIndicator.setVisibility(View.VISIBLE);
        } else {
            holder.cardView.setStrokeColor(context.getColor(R.color.outline_variant));
            holder.cardView.setStrokeWidth(1);
            holder.selectionIndicator.setVisibility(View.GONE);
        }

        // Regular click - for selection
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                // Toggle selection
                if (selectedItems.contains(equipment)) {
                    selectedItems.remove(equipment);
                } else {
                    selectedItems.add(equipment);
                }

                // Update selection mode
                isSelectionMode = selectedItems.size() > 0;

                // Notify listener
                listener.onItemClick(equipment, position);
                listener.onSelectionChanged(selectedItems);

                // Refresh this item to show selection
                notifyItemChanged(position);
            }
        });

        // Long click - also selects and enters selection mode
        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) {
                // Add to selection if not already selected
                if (!selectedItems.contains(equipment)) {
                    selectedItems.add(equipment);
                    isSelectionMode = true;
                    notifyItemChanged(position);
                    listener.onItemLongClick(equipment, position);
                    listener.onSelectionChanged(selectedItems);
                }
            }
            return true;
        });

        // 3-dots menu click
        holder.moreBtn.setOnClickListener(v -> {
            if (listener != null) {
                listener.onMoreClick(equipment, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return equipmentList.size();
    }

    // Get all selected items
    public List<EquipmentItem> getSelectedItems() {
        return selectedItems;
    }

    // Clear all selections
    public void clearSelections() {
        selectedItems.clear();
        isSelectionMode = false;
        notifyDataSetChanged();
        if (listener != null) {
            listener.onSelectionChanged(selectedItems);
        }
    }

    // Select all items
    public void selectAll() {
        selectedItems.clear();
        selectedItems.addAll(equipmentList);
        isSelectionMode = true;
        notifyDataSetChanged();
        if (listener != null) {
            listener.onSelectionChanged(selectedItems);
        }
    }

    // Check if in selection mode
    public boolean isInSelectionMode() {
        return isSelectionMode;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardView;
        ImageView equipmentIcon, selectionIndicator, moreBtn;
        TextView name, category;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (MaterialCardView) itemView;

            name = itemView.findViewById(R.id.equipmentName);
            category = itemView.findViewById(R.id.equipmentCategory);
        }
    }
}