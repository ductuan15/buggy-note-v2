package com.hcmus.clc18se.buggynote2.adapters;

import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.hcmus.clc18se.buggynote2.R;
import com.hcmus.clc18se.buggynote2.databinding.ItemNoteHeaderBinding;

public class NoteHeaderAdapter extends RecyclerView.Adapter<NoteHeaderAdapter.ViewHolder> {

    private Integer heightWhenVisible = null;
    private final String title;
    private final LiveData<Integer> visibility;
    private final @DrawableRes Integer iconRes;

    public NoteHeaderAdapter(String title, LiveData<Integer> visibility, Integer iconRes) {
        this.title = title;
        this.visibility = visibility;
        this.iconRes = iconRes;
    }

    @Override
    public void onViewAttachedToWindow(@NonNull ViewHolder holder) {
        super.onViewAttachedToWindow(holder);

        ViewGroup.LayoutParams layoutParams = holder.itemView.getLayoutParams();
        if (layoutParams instanceof StaggeredGridLayoutManager.LayoutParams) {
            ((StaggeredGridLayoutManager.LayoutParams) layoutParams).setFullSpan(true);
        }
        else if (layoutParams instanceof GridLayoutManager.LayoutParams) {
            holder.itemView.getResources().getInteger(R.integer.note_item_span_count_list);
        }

        if (visibility.getValue() != null && visibility.getValue() == View.GONE) {
            if (heightWhenVisible == null) {
                heightWhenVisible = layoutParams.height;
            }
            layoutParams.height = 0;
        }
        else {
            if (heightWhenVisible != null) {
                layoutParams.height = heightWhenVisible;
            }
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(
                ItemNoteHeaderBinding.inflate(
                        LayoutInflater.from(parent.getContext()),
                        parent,
                        false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(title, iconRes);
    }

    @Override
    public int getItemCount() {
        return 1;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final ItemNoteHeaderBinding binding;

        public ViewHolder(ItemNoteHeaderBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(String title, @DrawableRes Integer iconRes){
            Drawable drawable = null;

            if (iconRes != null) {
                drawable = ResourcesCompat.getDrawable(
                        binding.getRoot().getResources(),
                        iconRes,
                        binding.getRoot().getContext().getTheme()
                );
                drawable.setBounds(0, 0, 0, 0);
            }

            binding.header.setText(title);
            binding.header.setCompoundDrawablesRelative(
                    drawable,
                    null,
                    null,
                    null
            );
        }
    }
}
