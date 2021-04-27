package com.hcmus.clc18se.buggynote2.adapters.callbacks;

import com.hcmus.clc18se.buggynote2.data.Tag;
import com.hcmus.clc18se.buggynote2.databinding.ItemTagBinding;

public interface TagsAdapterCallbacks {
    void onFocus(ItemTagBinding binding, boolean hasFocus, Tag tag);
}
