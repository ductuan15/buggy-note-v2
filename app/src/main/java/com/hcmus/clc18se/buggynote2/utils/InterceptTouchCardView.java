package com.hcmus.clc18se.buggynote2.utils;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.google.android.material.card.MaterialCardView;

public class InterceptTouchCardView extends MaterialCardView {
    public InterceptTouchCardView(Context context) {
        super(context);
    }

    public InterceptTouchCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public InterceptTouchCardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return true;
    }
}
