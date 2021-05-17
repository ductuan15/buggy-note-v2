package com.hcmus.clc18se.buggynote2.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.PreferenceManager;

import com.hcmus.clc18se.buggynote2.R;

public class PreferenceUtils {

    public static int DEFAULT_COLOR = R.color.pink_500;
    public static final int THEME_USE_DEFAULT = 0;
    public static final int THEME_WHITE = 1;
    public static final int THEME_DARK = 2;

    public static int getCurrentThemeColor(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);

        int color = preferences.getInt(context.getString(R.string.app_color_key),
                DEFAULT_COLOR
        );

        if (color == context.getResources().getColor(R.color.green2_primary)) {
            return R.style.Theme_BuggyNote2_Green;

        } else if (color == context.getResources().getColor(R.color.blue_500)) {
            return R.style.Theme_BuggyNote2_Blue;

        } else if (color == context.getResources().getColor(R.color.indigo_500)) {
            return R.style.Theme_BuggyNote2_Indigo;

        } else if (color == context.getResources().getColor(R.color.blue_grey_500)) {
            return R.style.Theme_BuggyNote2_Grey;
        }

        return R.style.Theme_BuggyNote2;
    }

    public static void configColor(AppCompatActivity activity) {
        int themeRes = getCurrentThemeColor(activity);
        activity.setTheme(themeRes);
    }

    public static void configDefaultTheme(int uiMode) {
        switch (uiMode & Configuration.UI_MODE_NIGHT_MASK) {
            case Configuration.UI_MODE_NIGHT_NO: {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
            case Configuration.UI_MODE_NIGHT_YES: {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            }
        }
    }

    public static void configTheme(Integer uiMode, Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String themeOptions = preferences.getString(
                context.getString(R.string.app_theme_key),
                "0"
        );

        String[] options = context.getResources().getStringArray(R.array.theme_values);

        if (options[THEME_USE_DEFAULT].equals(themeOptions)) {
            if (uiMode == null) {
                uiMode = context.getResources().getConfiguration().uiMode;
            }
            configDefaultTheme(uiMode);
        } else if (options[THEME_WHITE].equals(themeOptions)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        } else if (options[THEME_DARK].equals(themeOptions)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }
    }

}
