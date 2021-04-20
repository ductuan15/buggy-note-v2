package com.hcmus.clc18se.buggynote2.utils;

import android.graphics.Typeface;
import android.view.Gravity;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public class TextFormatter {

    private int gravity = Gravity.START;
    private int typefaceStyle = Typeface.NORMAL;
    private int typefaceType = TYPEFACE_REGULAR;

    @Override
    public @NotNull String toString() {
        return gravity + "|" + typefaceStyle + "|" + typefaceType;
    }

    public void toggleFontType() {
        switch (typefaceType) {
            case TYPEFACE_SERIF: typefaceType = TYPEFACE_MONOSPACE; break;
            case TYPEFACE_MONOSPACE: typefaceType = TYPEFACE_REGULAR; break;
            default: typefaceType = TYPEFACE_SERIF;
        }
    }

    public int getGravity() {
        return gravity;
    }

    public void setGravity(int gravity) {
        this.gravity = gravity;
    }

    public int getTypefaceStyle() {
        return typefaceStyle;
    }

    public void setTypefaceStyle(int typefaceStyle) {
        this.typefaceStyle = typefaceStyle;
    }

    public int getTypefaceType() {
        return typefaceType;
    }

    public void setTypefaceType(int typefaceType) {
        this.typefaceType = typefaceType;
    }

    public TextFormatter() {
    }

    public TextFormatter(int gravity, int typefaceStyle, int typefaceType) {
        this.gravity = gravity;
        this.typefaceStyle = typefaceStyle;
        this.typefaceType = typefaceType;
    }

    static final String DELIM = "|";

    public static final String DEFAULT_FORMAT_STRING = "8388611|0|0";

    static final int TYPEFACE_REGULAR = 0;
    static final int TYPEFACE_SANS_SERIF = 1;
    static final int TYPEFACE_SERIF = 2;
    static final int TYPEFACE_MONOSPACE = 4;

    public static TextFormatter parseFormat(String format) {
        try {
            // val ints = format.split(DELIM).map { it.toInt() }
            String[] strs = format.split(DELIM);
            List<Integer> ints = new ArrayList<Integer>();
            for (String str : strs) {
                ints.add(Integer.valueOf(str));
            }
            return new TextFormatter(ints.get(0), ints.get(1), ints.get(2));

        } catch (Exception ex) {
            Timber.e(ex);
            return new TextFormatter();
        }
    }
}
