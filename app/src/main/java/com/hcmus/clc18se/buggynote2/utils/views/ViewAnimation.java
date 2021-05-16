package com.hcmus.clc18se.buggynote2.utils.views;

import android.animation.Animator;
import android.view.View;
import android.animation.AnimatorListenerAdapter;

public class ViewAnimation {
    public static Boolean rotateFab(View v, Boolean rotate) {
        v.animate().setDuration(200)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                    }
        }).rotation(rotate? 135f : 0f);
        return rotate;
    }

    public void showIn(View v) {
        v.setVisibility(View.VISIBLE);
        v.setAlpha(0f);
        v.setTranslationY((float) v.getHeight());
        v.animate()
                .setDuration(200)
                .translationY(-20f)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                    }
                })
            .alpha(1f)
                .start();
    }

    public void showOut(View v) {
        v.setVisibility(View.VISIBLE);
        v.setAlpha(1f);
        v.setTranslationY(0f);
        v.animate()
                .setDuration(200)
                .translationY((float) v.getHeight())
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        v.setVisibility(View.GONE);
                        super.onAnimationEnd(animation);
                    }
                }).alpha(0f)
                .start();
    }

    public void init(View v) {
        v.setVisibility(View.GONE);
        v.setAlpha((float) v.getHeight());
        v.setTranslationY(0f);
    }
}
