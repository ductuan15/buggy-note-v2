package com.hcmus.clc18se.buggynote2.adapters.callbacks;

import android.widget.CompoundButton;

import com.hcmus.clc18se.buggynote2.data.CheckListItem;
import com.hcmus.clc18se.buggynote2.databinding.ItemCheckListBinding;

public interface CheckListAdapterCallbacks {
    void onFocus(ItemCheckListBinding binding, boolean hasFocus, CheckListItem item);

    void onCheckedChanged(CompoundButton itemView, boolean isChecked, CheckListItem item);
}