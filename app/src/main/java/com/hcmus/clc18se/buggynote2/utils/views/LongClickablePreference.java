package com.hcmus.clc18se.buggynote2.utils.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;

public class LongClickablePreference extends Preference {

    private View.OnLongClickListener longClickListener;

    public LongClickablePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setLongClickListener(View.OnLongClickListener longClickListener) {
        this.longClickListener = longClickListener;
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        holder.itemView.setOnLongClickListener(longClickListener);
    }
}
