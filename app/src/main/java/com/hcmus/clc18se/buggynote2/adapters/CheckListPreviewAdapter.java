package com.hcmus.clc18se.buggynote2.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.hcmus.clc18se.buggynote2.data.CheckListItem;
import com.hcmus.clc18se.buggynote2.databinding.ItemCheckListPreviewBinding;


public class CheckListPreviewAdapter extends ListAdapter<CheckListItem, CheckListPreviewAdapter.ViewHolder> {

    public static final int MAX_ITEM = 5;
    private Integer buttonTintColor;
    public void setButtonTint(Integer buttonTint) {
        this.buttonTintColor = buttonTint;
    }

    public CheckListPreviewAdapter() {
        super(CheckListItem.diffCallback);
    }

    @Override
    public int getItemCount() {
        if (getCurrentList().size() > MAX_ITEM) {
            return MAX_ITEM;
        }

        return super.getItemCount();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return ViewHolder.from(parent);
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CheckListItem item = getItem(position);
        holder.bind(item, buttonTintColor);
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        ItemCheckListPreviewBinding binding;

        public ViewHolder(@NonNull ItemCheckListPreviewBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(CheckListItem item, Integer checkboxColor) {
            binding.setCheckListItem(item);
            binding.setColor(checkboxColor);
            binding.executePendingBindings();
        }

        public static ViewHolder from(ViewGroup parent) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            return new ViewHolder(
                    ItemCheckListPreviewBinding.inflate(inflater, parent, false)
            );
        }
    }
}
