package com.hcmus.clc18se.buggynote2.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Checkable;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.hcmus.clc18se.buggynote2.R;
import com.hcmus.clc18se.buggynote2.adapters.callbacks.NoteAdapterCallbacks;
import com.hcmus.clc18se.buggynote2.data.NoteWithTags;
import com.hcmus.clc18se.buggynote2.databinding.ItemNoteBinding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.noties.markwon.AbstractMarkwonPlugin;
import io.noties.markwon.Markwon;
import io.noties.markwon.core.MarkwonTheme;
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin;
import io.noties.markwon.ext.tables.TablePlugin;

public class NoteAdapter extends ListAdapter<NoteWithTags, NoteAdapter.ViewHolder> {

    // Dirty flags
    // position of the NoteAdapter containing a list of pinned notes in a ConcatAdapter
    public static final int PINNED_POSITION = 1;
    // position of the NoteAdapter containing a list of unpinned notes in a ConcatAdapter
    public static final int UNPINNED_POSITION = 3;

    public static final String PIN_TAG = "PIN";
    public static final String UNPIN_TAG = "UNPIN";
    public static final String ARCHIVE_TAG = "ARCHIVE";
    public static final String TRASH_TAG = "TRASH";

    public NoteAdapter(NoteAdapterCallbacks callbacks, String tag) {
        super(NoteWithTags.diffCallBacks);
        this.tag = tag;
        this.callbacks = callbacks;
    }

    public final String tag;

    private boolean multiSelected = false;

    private final NoteAdapterCallbacks callbacks;

    public List<NoteWithTags> getSelectedItems() {
        return selectedItems;
    }

    public int numberOfSelectedItems() {
        return selectedItems.size();
    }

    public void finishSelection() {
        multiSelected = false;
        selectedItems.clear();
        notifyDataSetChanged();
    }

    public void selectAll() {
        multiSelected = true;
        selectedItems.clear();
        selectedItems.addAll(getCurrentList());
        notifyDataSetChanged();
    }

    public void unSelectAll() {
        multiSelected = false;
        selectedItems.clear();
        notifyDataSetChanged();
    }

    public void enableSelection() {
        multiSelected = true;
    }

    private final List<NoteWithTags> selectedItems = new ArrayList<>();

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        NoteWithTags item = getItem(position);
        holder.bindFrom(item, tag);

        holder.itemView.setOnClickListener(view -> {
            if (multiSelected) {
                selectItem(holder, item);
                callbacks.onMultipleSelect(item);
            } else {
                callbacks.onClick(item);
            }

        });

        holder.itemView.setOnLongClickListener(v -> {
            if (!multiSelected) {
                multiSelected = true;
                selectItem(holder, item);
                callbacks.onMultipleSelect(item);
            }
            return true;
        });

        if (holder.itemView instanceof Checkable) {
            ((Checkable) holder.itemView).setChecked(selectedItems.contains(item));
        }

    }

    private void selectItem(ViewHolder holder, NoteWithTags item) {
        if (selectedItems.contains(item)) {
            selectedItems.remove(item);
            if (holder.itemView instanceof Checkable) {
                ((Checkable) holder.itemView).setChecked(false);
            }
        } else {
            selectedItems.add(item);
            if (holder.itemView instanceof Checkable) {
                ((Checkable) holder.itemView).setChecked(true);
            }
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        return new ViewHolder(
                ItemNoteBinding.inflate(layoutInflater, parent, false)
        );
    }

    @Override
    public int getItemViewType(int position) {
        return R.layout.item_note;
    }

    public void onItemSwipe(int position) {
        callbacks.onItemSwiped(getItem(position));
    }

    public boolean onItemMove(int fromPosition, int toPosition) {
        // Timber.d("$fromPosition to $toPosition")
        if (fromPosition == RecyclerView.NO_POSITION
                || toPosition == RecyclerView.NO_POSITION
                || toPosition >= getCurrentList().size()
        ) {
            return false;
        }

        List<NoteWithTags> items = new ArrayList<>(getCurrentList().size());
        items.addAll(getCurrentList());
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(items, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i >= toPosition + 1; i--) {
                Collections.swap(items, i, i - 1);
            }
        }

        submitList(items);
        callbacks.onPostReordered(items);
        return true;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        ItemNoteBinding binding;
        public String tag;

        public ViewHolder(@NonNull ItemNoteBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bindFrom(NoteWithTags note, String tag) {
            if (note.note.isMarkdown()) {
                Markwon markwon =
                        Markwon.builder(itemView.getContext())
                                .usePlugin(StrikethroughPlugin.create())
                                .usePlugin(TablePlugin.create(itemView.getContext()))
                                .usePlugin(
                        new AbstractMarkwonPlugin() {
                            @Override
                            public void configureTheme(@NonNull MarkwonTheme.Builder builder) {
                                Integer titleColor = note.note.getTitleColor(itemView.getContext());
                                if (titleColor != null) {
                                    builder.linkColor(titleColor)
                                            .codeTextColor(titleColor)
                                            .codeBlockTextColor(titleColor);
                                }
                            }
                        })
                        .build();

                binding.setMarkwon(markwon);
            }
            binding.setNote(note);
            this.tag = tag;
        }
    }
}