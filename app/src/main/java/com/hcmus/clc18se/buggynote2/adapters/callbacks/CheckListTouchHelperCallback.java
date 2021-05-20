package com.hcmus.clc18se.buggynote2.adapters.callbacks;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.hcmus.clc18se.buggynote2.adapters.CheckListAdapter;

public class CheckListTouchHelperCallback extends ItemTouchHelper.SimpleCallback {

    public CheckListTouchHelperCallback() {
        super(ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0);
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView,
                          @NonNull RecyclerView.ViewHolder viewHolder,
                          @NonNull RecyclerView.ViewHolder target) {
        RecyclerView.Adapter<?> adapter = recyclerView.getAdapter();
        if (adapter instanceof CheckListAdapter) {
            return ((CheckListAdapter) adapter).onItemMove(
                    viewHolder.getBindingAdapterPosition(),
                    target.getBindingAdapterPosition()
            );
        }
        return false;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
    }
}
