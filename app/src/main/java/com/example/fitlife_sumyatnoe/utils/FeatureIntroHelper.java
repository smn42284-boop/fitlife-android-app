package com.example.fitlife_sumyatnoe.utils;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.example.fitlife_sumyatnoe.R;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class FeatureIntroHelper {

    private Activity activity;
    private List<FeatureStep> steps = new ArrayList<>();
    private int currentStep = 0;
    private View overlay;
    private PopupWindow tooltipPopup;
    private OnIntroCompleteListener listener;

    public interface OnIntroCompleteListener {
        void onIntroComplete();
    }

    public static class FeatureStep {
        public View targetView;
        public String title;
        public String description;
        public int iconRes;

        public FeatureStep(View targetView, String title, String description, int iconRes) {
            this.targetView = targetView;
            this.title = title;
            this.description = description;
            this.iconRes = iconRes;
        }
    }

    public FeatureIntroHelper(Activity activity) {
        this.activity = activity;
    }

    public void addStep(FeatureStep step) {
        steps.add(step);
    }

    public void start(OnIntroCompleteListener listener) {
        this.listener = listener;
        if (steps.isEmpty()) {
            if (listener != null) listener.onIntroComplete();
            return;
        }
        showStep(0);
    }

    private void showStep(int index) {
        if (index >= steps.size()) {
            if (listener != null) listener.onIntroComplete();
            return;
        }

        currentStep = index;
        FeatureStep step = steps.get(index);

        if (overlay == null) {
            overlay = new View(activity);
            overlay.setBackgroundColor(Color.parseColor("#80000000"));
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            );
            ((ViewGroup) activity.getWindow().getDecorView()).addView(overlay, params);
        } else {
            overlay.setVisibility(View.VISIBLE);
        }

        step.targetView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                step.targetView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                showTooltip(step);
            }
        });
    }

    private void showTooltip(FeatureStep step) {
        // Create tooltip view
        View tooltipView = LayoutInflater.from(activity).inflate(R.layout.view_feature_tooltip, null);

        TextView titleText = tooltipView.findViewById(R.id.titleText);
        TextView descriptionText = tooltipView.findViewById(R.id.descriptionText);
        TextView stepIndicator = tooltipView.findViewById(R.id.stepIndicator);
        MaterialButton nextBtn = tooltipView.findViewById(R.id.nextBtn);
        MaterialButton skipBtn = tooltipView.findViewById(R.id.skipBtn);

        titleText.setText(step.title);
        descriptionText.setText(step.description);
        stepIndicator.setText((currentStep + 1) + "/" + steps.size());

        nextBtn.setOnClickListener(v -> {
            closeTooltip();
            showStep(currentStep + 1);
        });

        skipBtn.setOnClickListener(v -> {
            closeTooltip();
            if (listener != null) listener.onIntroComplete();
        });

        int[] location = new int[2];
        step.targetView.getLocationOnScreen(location);
        Rect rect = new Rect(location[0], location[1],
                location[0] + step.targetView.getWidth(),
                location[1] + step.targetView.getHeight());

        tooltipPopup = new PopupWindow(tooltipView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true);
        tooltipPopup.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        tooltipPopup.setOutsideTouchable(false);

        int x = rect.centerX() - (tooltipView.getMeasuredWidth() / 2);
        int y = rect.bottom + 16;

        // If not enough space below, show above
        if (y + tooltipView.getMeasuredHeight() > activity.getWindow().getDecorView().getHeight()) {
            y = rect.top - tooltipView.getMeasuredHeight() - 16;
        }

        tooltipPopup.showAtLocation(activity.getWindow().getDecorView(), Gravity.NO_GRAVITY, x, y);
    }

    private void closeTooltip() {
        if (tooltipPopup != null && tooltipPopup.isShowing()) {
            tooltipPopup.dismiss();
        }
        if (overlay != null) {
            overlay.setVisibility(View.GONE);
        }
    }

    public void cleanup() {
        closeTooltip();
        if (overlay != null) {
            ((ViewGroup) overlay.getParent()).removeView(overlay);
        }
    }
}