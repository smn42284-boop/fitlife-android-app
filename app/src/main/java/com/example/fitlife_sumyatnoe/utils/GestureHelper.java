package com.example.fitlife_sumyatnoe.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Vibrator;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class GestureHelper {

    private Context context;
    private GestureDetector gestureDetector;
    private SensorManager sensorManager;
    private Vibrator vibrator;
    private OnGestureListener gestureListener;
    private long lastShakeTime = 0;
    private static final int SHAKE_THRESHOLD = 22;
    private static final int SHAKE_TIME_LIMIT = 2000;
    private RecyclerView attachedRecyclerView;
    private SensorEventListener shakeListener;
    private SharedPreferences prefs;
    private boolean isGesturesEnabled = true;
    private float startX = 0;
    private int trackingPosition = -1;
    private View trackingView = null;

    public interface OnGestureListener {
        void onSwipeLeft(View view, int position, Object item);
        void onSwipeRight(View view, int position, Object item);
        void onDoubleTap(View view, int position, Object item);
        void onLongPress(View view, int position, Object item);
        void onShake();
        void onSwipeStart(View view, int position, float startX);
        void onSwipeMove(View view, int position, float deltaX);
        void onSwipeCancel(View view, int position);
    }

    public GestureHelper(Context context, OnGestureListener listener) {
        this.context = context;
        this.gestureListener = listener;
        this.vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        this.prefs = context.getSharedPreferences("FitLifePrefs", Context.MODE_PRIVATE);

        setupGestureDetector();
        setupShakeDetection();
        loadGesturePreference();
    }

    private void loadGesturePreference() {
        isGesturesEnabled = prefs.getBoolean("gestures_enabled", true);
    }

    public void setGesturesEnabled(boolean enabled) {
        this.isGesturesEnabled = enabled;
        prefs.edit().putBoolean("gestures_enabled", enabled).apply();
    }

    public boolean isGesturesEnabled() {
        return isGesturesEnabled;
    }

    private void vibrate(long milliseconds) {
        if (vibrator != null && vibrator.hasVibrator()) {
            vibrator.vibrate(milliseconds);
        }
    }

    private void setupGestureDetector() {
        gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {

            @Override
            public boolean onDown(MotionEvent e) {
                if (!isGesturesEnabled) return false;

                startX = e.getX();
                trackingPosition = getPositionFromTouch(e.getRawX(), e.getRawY());
                trackingView = getViewAtPosition(trackingPosition);

                if (gestureListener != null && trackingPosition != -1) {
                    gestureListener.onSwipeStart(trackingView, trackingPosition, startX);
                }
                return true;
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                if (!isGesturesEnabled) return false;

                float currentX = e2.getX();
                float deltaX = currentX - startX;

                if (Math.abs(deltaX) > 10 && Math.abs(deltaX) > Math.abs(distanceY)) {
                    if (gestureListener != null && trackingPosition != -1) {
                        gestureListener.onSwipeMove(trackingView, trackingPosition, deltaX);
                    }
                    return true;
                }
                return false;
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if (!isGesturesEnabled) return false;
                if (e1 == null || e2 == null) return false;

                float diffX = e2.getX() - e1.getX();
                float diffY = e2.getY() - e1.getY();

                // Much lower thresholds for easier swipe
                if (Math.abs(diffX) > Math.abs(diffY) && Math.abs(diffX) > 20) {
                    vibrate(50);

                    int position = trackingPosition;
                    View targetView = trackingView;

                    if (diffX < -20 && gestureListener != null) {
                        gestureListener.onSwipeLeft(targetView, position, null);
                        return true;
                    } else if (diffX > 20 && gestureListener != null) {
                        gestureListener.onSwipeRight(targetView, position, null);
                        return true;
                    }
                }
                return false;
            }
            @Override
            public boolean onDoubleTap(MotionEvent e) {

                if (!isGesturesEnabled) return false;

                vibrate(40);

                int position = trackingPosition;
                View targetView = trackingView;


                if (gestureListener != null && targetView != null && position != -1) {
                    gestureListener.onDoubleTap(targetView, position, null);
                    return true;
                }
                return false;
            }
            @Override
            public void onLongPress(MotionEvent e) {
                if (!isGesturesEnabled) return;

                vibrate(30);
                int position = trackingPosition;
                View targetView = trackingView;
                if (gestureListener != null && targetView != null) {
                    gestureListener.onLongPress(targetView, position, null);
                }
            }
        });
    }

    private void setupShakeDetection() {
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            if (accelerometer != null) {
                shakeListener = new SensorEventListener() {
                    @Override
                    public void onSensorChanged(SensorEvent event) {
                        if (!isGesturesEnabled) return;

                        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                            float x = event.values[0];
                            float y = event.values[1];
                            float z = event.values[2];

                            float gForce = (float) Math.sqrt(x * x + y * y + z * z);

                            if (gForce > SHAKE_THRESHOLD) {
                                long now = System.currentTimeMillis();
                                if (now - lastShakeTime > SHAKE_TIME_LIMIT) {
                                    lastShakeTime = now;

                                    vibrate(100);

                                    if (gestureListener != null) {
                                        gestureListener.onShake();
                                    }
                                }
                            }
                        }
                    }

                    @Override
                    public void onAccuracyChanged(Sensor sensor, int accuracy) {}
                };
                sensorManager.registerListener(shakeListener, accelerometer, SensorManager.SENSOR_DELAY_UI);
            }
        }
    }

    public void attachToRecyclerView(RecyclerView recyclerView) {
        if (recyclerView == null) return;

        this.attachedRecyclerView = recyclerView;

        recyclerView.addOnItemTouchListener(new RecyclerView.SimpleOnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
                if (isGesturesEnabled) {
                    gestureDetector.onTouchEvent(e);

                    if (e.getAction() == MotionEvent.ACTION_UP || e.getAction() == MotionEvent.ACTION_CANCEL) {
                        if (gestureListener != null && trackingPosition != -1) {
                            gestureListener.onSwipeCancel(trackingView, trackingPosition);
                        }
                        trackingPosition = -1;
                        trackingView = null;
                        startX = 0;
                    }
                }
                // Return false to allow RecyclerView to handle scrolling
                return false;
            }

            @Override
            public void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
                if (isGesturesEnabled) {
                    gestureDetector.onTouchEvent(e);
                }
            }
        });
    }

    private int getPositionFromTouch(float rawX, float rawY) {
        if (attachedRecyclerView == null) return -1;

        int[] recyclerLocation = new int[2];
        attachedRecyclerView.getLocationOnScreen(recyclerLocation);

        float relativeX = rawX - recyclerLocation[0];
        float relativeY = rawY - recyclerLocation[1];

        View childView = attachedRecyclerView.findChildViewUnder(relativeX, relativeY);
        if (childView != null) {
            return attachedRecyclerView.getChildAdapterPosition(childView);
        }
        return -1;
    }

    private View getViewAtPosition(int position) {
        if (attachedRecyclerView == null || position == -1) return null;
        return attachedRecyclerView.getLayoutManager().findViewByPosition(position);
    }

    public void cleanup() {
        if (sensorManager != null && shakeListener != null) {
            sensorManager.unregisterListener(shakeListener);
        }
        if (attachedRecyclerView != null) {
            attachedRecyclerView.setOnTouchListener(null);
        }
    }
}