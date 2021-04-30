package com.hcmus.clc18se.buggynote2.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.hcmus.clc18se.buggynote2.adapters.callbacks.TagSelectionAdapterCallbacks;
import com.hcmus.clc18se.buggynote2.data.Tag;
import com.hcmus.clc18se.buggynote2.databinding.ItemTagSelectionBinding;

public class TagSelectionAdapter extends ListAdapter<Tag, TagSelectionAdapter.ViewHolder> {

    private TagSelectionAdapterCallbacks callbacks;

    public TagSelectionAdapter(TagSelectionAdapterCallbacks callbacks) {
        super(Tag.diffCallbacks);
        this.callbacks = callbacks;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return ViewHolder.from(parent);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Tag tag = getItem(position);
        holder.bind(tag);
        holder.binding.tagCheckBox.setOnCheckedChangeListener((view, isChecked) -> {
            callbacks.onCheckedChanged(view, isChecked, tag);
        });
        holder.binding.tagContent.setOnClickListener(v -> holder.binding.tagCheckBox.performClick());
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private ItemTagSelectionBinding binding;

        public ViewHolder(@NonNull ItemTagSelectionBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Tag tag) {
            binding.setTag(tag);
            binding.tagContent.setTextIsSelectable(true);
            binding.executePendingBindings();
        }

        public static ViewHolder from(@NonNull ViewGroup parent) {
            return new ViewHolder(ItemTagSelectionBinding.inflate(LayoutInflater.from(parent.getContext()),
                    parent,
                    false)
            );
        }
    }
}
