package com.hcmus.clc18se.buggynote2.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.hcmus.clc18se.buggynote2.adapters.callbacks.TagsAdapterCallbacks;
import com.hcmus.clc18se.buggynote2.data.Tag;
import com.hcmus.clc18se.buggynote2.databinding.ItemTagBinding;

public class TagsAdapter extends ListAdapter<Tag, TagsAdapter.ViewHolder> {

    TagsAdapterCallbacks callbacks;

    public TagsAdapter(TagsAdapterCallbacks callbacks) {
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
        holder.setOnFocusListenerForEditor(callbacks, tag);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

    ItemTagBinding binding;

    public ViewHolder(@NonNull ItemTagBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    public void bind(Tag tag) {
        binding.setTag(tag);
        binding.executePendingBindings();
    }

    void setOnFocusListenerForEditor(TagsAdapterCallbacks callbacks, Tag tag) {
        binding.tagContent.setOnFocusChangeListener((v, hasFocus) -> callbacks.onFocus(binding, hasFocus, tag));

        binding.checkButton.setOnClickListener(v -> binding.tagContent.requestFocus());
        binding.removeButton.setOnClickListener(v -> binding.tagContent.requestFocus());
    }

    public static ViewHolder from(ViewGroup parent) {
        ItemTagBinding binding = ItemTagBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new ViewHolder(binding);
    }
}
}

