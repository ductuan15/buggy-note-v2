package com.hcmus.clc18se.buggynote2.adapters;

import android.widget.TextView;

import androidx.databinding.BindingAdapter;

import com.hcmus.clc18se.buggynote2.R;
import java.util.Random;

public class BindingAdapters {

    @BindingAdapter("placeHolderEmoticon")
    public static void setPlaceHolderEmoticon(TextView textView, Object nothing) {
        String[] emoticons = textView.getContext().getResources().getStringArray(R.array.emoticons);
        textView.setText(
                emoticons[new Random().nextInt(emoticons.length)]
        );
    }
}