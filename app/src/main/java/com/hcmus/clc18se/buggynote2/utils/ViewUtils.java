package com.hcmus.clc18se.buggynote2.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.AttrRes;
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

    public static void setLightStatusBar(View view, Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            int flags = view.getSystemUiVisibility();
            flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            view.setSystemUiVisibility(flags);
        }
    }

    public static void unsetLightStatusBar(View view, Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            int flags = view.getSystemUiVisibility();
            flags &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            view.setSystemUiVisibility(flags);
        }
    }

    public static int getColorAttr(Context context, @AttrRes int attrId) {
        TypedValue typedValue = new TypedValue();

        context.getTheme().resolveAttribute(
                attrId,
                typedValue,
                true);

        return typedValue.data;
    }
}
