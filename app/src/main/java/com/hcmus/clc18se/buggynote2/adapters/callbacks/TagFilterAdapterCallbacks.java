package com.hcmus.clc18se.buggynote2.adapters.callbacks;

import com.hcmus.clc18se.buggynote2.data.Tag;

public interface TagFilterAdapterCallbacks {
    void onCheckChanged(boolean isChecked, Tag tag);
    boolean onLongClick(Tag tag);
}
