package com.hcmus.clc18se.buggynote2.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.hcmus.clc18se.buggynote2.R;
import com.hcmus.clc18se.buggynote2.adapters.callbacks.NoteAdapterCallbacks;
import com.hcmus.clc18se.buggynote2.data.NoteWithTags;
import com.hcmus.clc18se.buggynote2.databinding.ItemNoteBinding;

import java.util.ArrayList;
import java.util.List;

public class NoteAdapter extends ListAdapter<NoteWithTags, NoteAdapter.ViewHolder> {

    // Dirty flags
    // position of the NoteAdapter containing a list of pinned notes in a ConcatAdapter
    public static final int PINNED_POSITION = 1;
    // position of the NoteAdapter containing a list of unpinned notes in a ConcatAdapter
    public static final int UNPINNED_POSITION = 3;

    public static final String PIN_TAG = "PIN";
    public static final String UNPIN_TAG = "UNPIN";
    public static final String ARCHIVE_TAG = "ARCHIVE";

    public NoteAdapter(NoteAdapterCallbacks callbacks, String tag) {
        super(NoteWithTags.diffCallBacks);
        this.tag = tag;
        this.callbacks = callbacks;
    }

    private final String tag;

    private boolean multiSelected = false;

    private final NoteAdapterCallbacks callbacks;

    public List<NoteWithTags> getSelectedItems() {
        return selectedItems;
    }

    public int numberOfSelectedItems() {
        return selectedItems.size();
    }

    // TODO: multi selection
    public void finishSelection() {
        multiSelected = false;
        selectedItems.clear();
        notifyDataSetChanged();
    }

    public void selectAll() {
        multiSelected = true;
        selectedItems.addAll(getCurrentList());
    }

    public void unSelectAll() {
        multiSelected = false;
        selectedItems.clear();
    }

    public void enableSelection() {
        multiSelected = true;
    }

    private final List<NoteWithTags> selectedItems = new ArrayList<>();

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        NoteWithTags item = getItem(position);
        holder.bindFrom(item);

        holder.itemView.setOnClickListener(view -> {
            callbacks.onClick(item);
        });
        //
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        return new ViewHolder(
                ItemNoteBinding.inflate(layoutInflater)
        );
    }

    @Override
    public int getItemViewType(int position) {
        return R.layout.item_note;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        ItemNoteBinding binding;
        public ViewHolder(@NonNull ItemNoteBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bindFrom(NoteWithTags note) {
            binding.setNote(note);

        }
    }
}

