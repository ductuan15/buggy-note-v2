package com.hcmus.clc18se.buggynote2.adapters.callbacks;

import android.widget.CompoundButton;

import com.hcmus.clc18se.buggynote2.data.Tag;

public interface TagSelectionAdapterCallbacks {
    void onCheckedChanged(CompoundButton itemView, boolean isChecked, Tag tag);
}
