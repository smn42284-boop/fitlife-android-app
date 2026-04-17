package com.example.fitlife_sumyatnoe.utils;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ConfettiHelper {

    private static ConfettiHelper instance;
    private ValueAnimator animator;
    private List<ConfettiParticle> particles = new ArrayList<>();
    private Random random = new Random();
    private ViewGroup container;
    private ConfettiView confettiView;

    private ConfettiHelper() {}

    public static synchronized ConfettiHelper getInstance() {
        if (instance == null) {
            instance = new ConfettiHelper();
        }
        return instance;
    }

    public void celebrate(ViewGroup container) {
        this.container = container;

        // Create confetti view if not exists
        if (confettiView == null) {
            confettiView = new ConfettiView(container.getContext());
            confettiView.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            ));
            container.addView(confettiView);
        }

        confettiView.setVisibility(View.VISIBLE);

        // Get container dimensions safely
        int width = container.getWidth();
        int height = container.getHeight();

        if (width <= 0) width = 1080;
        if (height <= 0) height = 1920;

        // Create particles
        particles.clear();
        int[] colors = new int[]{
                Color.RED, Color.YELLOW, Color.GREEN, Color.BLUE,
                Color.MAGENTA, Color.CYAN,
                Color.parseColor("#8A4F7D"), Color.parseColor("#F4B740")
        };

        for (int i = 0; i < 100; i++) {
            ConfettiParticle particle = new ConfettiParticle();
            // FIX: Ensure bounds are positive
            particle.x = random.nextInt(Math.max(width, 1));
            particle.y = -random.nextInt(Math.max(500, 1));
            particle.size = random.nextInt(15) + 5;
            particle.color = colors[random.nextInt(colors.length)];
            particle.velocityX = random.nextInt(10) - 5;
            particle.velocityY = random.nextInt(15) + 5;
            particle.rotation = random.nextInt(360);
            particle.rotationSpeed = random.nextInt(10) - 5;
            particles.add(particle);
        }

        // Start animation
        animator = ValueAnimator.ofFloat(0, 1);
        animator.setDuration(2000);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.addUpdateListener(animation -> {
            for (ConfettiParticle p : particles) {
                p.x += p.velocityX;
                p.y += p.velocityY;
                p.rotation += p.rotationSpeed;
            }
            confettiView.invalidate();
        });
        animator.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                confettiView.setVisibility(View.GONE);
            }
        });
        animator.start();
    }

    public void celebrateWithDelay(ViewGroup container) {
        // Alternative: Delay confetti until container is measured
        container.post(() -> celebrate(container));
    }

    public void stop() {
        if (animator != null && animator.isRunning()) {
            animator.cancel();
        }
        if (confettiView != null) {
            confettiView.setVisibility(View.GONE);
        }
    }

    private class ConfettiParticle {
        float x, y;
        int size;
        int color;
        float velocityX, velocityY;
        float rotation, rotationSpeed;
    }

    private class ConfettiView extends View {
        private Paint paint = new Paint();

        public ConfettiView(Context context) {
            super(context);
            paint.setStyle(Paint.Style.FILL);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            for (ConfettiParticle p : particles) {
                paint.setColor(p.color);
                canvas.save();
                canvas.rotate(p.rotation, p.x, p.y);
                canvas.drawRect(p.x, p.y, p.x + p.size, p.y + p.size, paint);
                canvas.restore();
            }
        }
    }
}