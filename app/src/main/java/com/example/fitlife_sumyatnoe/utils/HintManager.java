package com.example.fitlife_sumyatnoe.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.example.fitlife_sumyatnoe.R;

public class HintManager {
    private static final String PREF_NAME = "hints";
    private static final String KEY_SWIPE_HINT = "swipe_hint_shown";
    private static final String KEY_SHAKE_HINT = "shake_hint_shown";

    private final SharedPreferences prefs;

    public HintManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public boolean hasSeenSwipeHint() {
        return prefs.getBoolean(KEY_SWIPE_HINT, false);
    }

    public void markSwipeHintSeen() {
        prefs.edit().putBoolean(KEY_SWIPE_HINT, true).apply();
    }

    public boolean hasSeenShakeHint() {
        return prefs.getBoolean(KEY_SHAKE_HINT, false);
    }

    public void markShakeHintSeen() {
        prefs.edit().putBoolean(KEY_SHAKE_HINT, true).apply();
    }

    public void showTooltip(Activity activity, View anchor, String title, String message) {
        View tooltipView = LayoutInflater.from(activity).inflate(R.layout.tooltip_hint, null);

        TextView titleText = tooltipView.findViewById(R.id.tooltip_title);
        TextView messageText = tooltipView.findViewById(R.id.tooltip_message);

        titleText.setText(title);
        messageText.setText(message);

        PopupWindow popupWindow = new PopupWindow(tooltipView,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                true);

        popupWindow.setElevation(16f);
        popupWindow.setOutsideTouchable(true);

        int[] location = new int[2];
        anchor.getLocationOnScreen(location);

        popupWindow.showAtLocation(anchor, Gravity.NO_GRAVITY,
                location[0] + anchor.getWidth() / 2 - 100,
                location[1] - 100);

        tooltipView.setOnClickListener(v -> popupWindow.dismiss());

        popupWindow.setOnDismissListener(() -> {
            // Auto-dismiss after 3 seconds
        });

        tooltipView.postDelayed(popupWindow::dismiss, 4000);
    }
}