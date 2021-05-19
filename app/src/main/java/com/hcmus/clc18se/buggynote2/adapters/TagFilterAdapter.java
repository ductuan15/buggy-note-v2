package com.hcmus.clc18se.buggynote2.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.hcmus.clc18se.buggynote2.adapters.callbacks.TagFilterAdapterCallbacks;
import com.hcmus.clc18se.buggynote2.data.Tag;
import com.hcmus.clc18se.buggynote2.databinding.ItemTagFilterBinding;

public class TagFilterAdapter extends ListAdapter<Tag, TagFilterAdapter.ViewHolder> {

    private final TagFilterAdapterCallbacks callbacks;

    public TagFilterAdapter(TagFilterAdapterCallbacks callbacks) {
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
        holder.binding.tagFilter.setOnCheckedChangeListener((v, isChecked) -> {
            callbacks.onCheckChanged(isChecked, tag);
        });
        holder.binding.tagFilter.setOnLongClickListener(v -> callbacks.onLongClick(tag));
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemTagFilterBinding binding;

        public ViewHolder(ItemTagFilterBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Tag tag) {
            binding.setTag(tag);
            binding.executePendingBindings();
        }

        public static ViewHolder from(ViewGroup parent) {
            return new ViewHolder(
                    ItemTagFilterBinding.inflate(LayoutInflater.from(parent.getContext()),
                            parent, false)
            );
        }
    }
}
