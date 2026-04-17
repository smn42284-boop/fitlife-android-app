package com.example.fitlife_sumyatnoe;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;
import androidx.test.uiautomator.UiDevice;

public class TestUtils {

    public static void disableAnimations(ActivityTestRule rule) {
        try {
            UiDevice device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
            device.executeShellCommand("settings put global window_animation_scale 0");
            device.executeShellCommand("settings put global transition_animation_scale 0");
            device.executeShellCommand("settings put global animator_duration_scale 0");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}