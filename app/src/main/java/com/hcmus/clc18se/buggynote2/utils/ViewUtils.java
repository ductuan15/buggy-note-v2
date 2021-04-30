package com.hcmus.clc18se.buggynote2.utils;

import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.hcmus.clc18se.buggynote2.R;

public class ViewUtils {

    public static void setupLayoutManagerForNoteList(
            @NonNull RecyclerView recyclerView,
            @NonNull SharedPreferences preferences) {
        final String list = "1";
        RecyclerView.LayoutManager layoutManager;
        if (list.equals(preferences.getString(recyclerView.getContext().getString(R.string.note_list_view_type_key), "0"))) {
            layoutManager = new GridLayoutManager(recyclerView.getContext(),
                            ContextUtils.getSpanCountForNoteList(recyclerView.getContext(), preferences));
        } else {
            layoutManager = new StaggeredGridLayoutManager(
                            ContextUtils.getSpanCountForNoteList(recyclerView.getContext(), preferences),
                            StaggeredGridLayoutManager.VERTICAL);
        }

        recyclerView.setLayoutManager(layoutManager);
    }
}
