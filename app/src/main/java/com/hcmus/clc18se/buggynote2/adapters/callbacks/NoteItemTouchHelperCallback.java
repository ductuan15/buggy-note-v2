package com.hcmus.clc18se.buggynote2.adapters.callbacks;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.hcmus.clc18se.buggynote2.adapters.NoteAdapter;

import java.util.List;

public class NoteItemTouchHelperCallback extends ItemTouchHelper.SimpleCallback {

    NoteAdapter[] adapters;

    public NoteItemTouchHelperCallback(NoteAdapter... adapters) {
        super(ItemTouchHelper.UP
                        | ItemTouchHelper.DOWN
                        | ItemTouchHelper.LEFT
                        | ItemTouchHelper.RIGHT,
                ItemTouchHelper.LEFT
                        | ItemTouchHelper.RIGHT
        );
        this.adapters = adapters;
    }

    @Override
    public boolean isLongPressDragEnabled() {

        int nSelectedItems = 0;
        for (NoteAdapter adapter : adapters) {
            if (adapter.tag.equals(NoteAdapter.TRASH_TAG)) {
                return false;
            }
            nSelectedItems += adapter.numberOfSelectedItems();
        }
        return nSelectedItems <= 1;
    }

    @Override
    public boolean isItemViewSwipeEnabled() {
        for (NoteAdapter adapter : adapters) {
            if (adapter.tag.equals(NoteAdapter.TRASH_TAG)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int getSwipeDirs(@NonNull RecyclerView recyclerView,
                            @NonNull RecyclerView.ViewHolder viewHolder) {
        if (viewHolder instanceof NoteAdapter.ViewHolder) {
            return super.getSwipeDirs(recyclerView, viewHolder);
        }
        return 0;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        if (viewHolder instanceof NoteAdapter.ViewHolder) {
            for (NoteAdapter adapter : adapters) {
                if (adapter.tag.equals(((NoteAdapter.ViewHolder) viewHolder).tag)) {
                    adapter.onItemSwipe(viewHolder.getBindingAdapterPosition());
                }
            }
        }
    }


    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView,
                          @NonNull RecyclerView.ViewHolder viewHolder,
                          @NonNull RecyclerView.ViewHolder target) {
        if (viewHolder instanceof NoteAdapter.ViewHolder) {
            for (NoteAdapter adapter : adapters) {
                if (adapter.tag.equals(((NoteAdapter.ViewHolder) viewHolder).tag)) {
                    return adapter.onItemMove(viewHolder.getBindingAdapterPosition(),
                            target.getBindingAdapterPosition());
                }
            }
        }
        return false;
    }
}
