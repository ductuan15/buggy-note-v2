package com.hcmus.clc18se.buggynote2.utils;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import com.hcmus.clc18se.buggynote2.R;

public class ContextUtils {
    public static int getSpanCountForNoteList(
            @NonNull Context context, @NonNull SharedPreferences preferences) {
        final String list = "1";
        if (list.equals(preferences.getString(context.getString(R.string.note_list_view_type_key), "0"))) {
            return context.getResources().getInteger(R.integer.note_item_span_count_list);
        }
        return context.getResources().getInteger(R.integer.note_item_span_count_grid);
    }
}
