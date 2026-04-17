package com.example.fitlife_sumyatnoe.utils;

import android.widget.CheckBox;
import androidx.core.content.ContextCompat;
import com.example.fitlife_sumyatnoe.R;

public class CheckboxHelper {

    public static void setupCheckbox(CheckBox checkBox, boolean isChecked) {
        checkBox.setChecked(isChecked);
        updateCheckboxColor(checkBox, isChecked);
    }

    public static void updateCheckboxColor(CheckBox checkBox, boolean isChecked) {
        int color;
        if (isChecked) {
            color = ContextCompat.getColor(checkBox.getContext(), R.color.secondary);
        } else {
            color = ContextCompat.getColor(checkBox.getContext(), R.color.primary);
        }
        checkBox.setButtonTintList(android.content.res.ColorStateList.valueOf(color));
    }

    public static void setCheckboxListener(CheckBox checkBox, OnCheckboxToggledListener listener) {
        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateCheckboxColor(checkBox, isChecked);
            if (listener != null) {
                listener.onToggled(isChecked);
            }
        });
    }

    public interface OnCheckboxToggledListener {
        void onToggled(boolean isChecked);
    }
}