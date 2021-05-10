package com.hcmus.clc18se.buggynote2.adapters;

import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.hcmus.clc18se.buggynote2.adapters.callbacks.CheckListAdapterCallbacks;
import com.hcmus.clc18se.buggynote2.data.CheckListItem;
import com.hcmus.clc18se.buggynote2.databinding.ItemCheckListBinding;

public class CheckListAdapter extends ListAdapter<CheckListItem, CheckListAdapter.ViewHolder> {

    private final CheckListAdapterCallbacks callbacks;

    public CheckListAdapter(CheckListAdapterCallbacks callbacks) {
        super(CheckListItem.diffCallback);
        this.callbacks = callbacks;
    }

    private ItemCheckListBinding currentFocusedView = null;
    private CheckListItem currentFocusedItem = null;

    public void setCurrentFocusedView(
            ItemCheckListBinding currentFocusedView,
            CheckListItem item
    ) {
        if ((currentFocusedView == null && item == null) ||
                (currentFocusedView != null && item != null)
        ) {
            this.currentFocusedView = currentFocusedView;
            this.currentFocusedItem = item;
        }
    }

    public void saveFocusedView() {
        if (currentFocusedView != null && currentFocusedItem != null) {
            String text = currentFocusedView.listContent.getText().toString().trim();
            if (text.isEmpty()) {
                currentFocusedView.listContent.setText(currentFocusedItem.getContent());
            }

            if (!currentFocusedItem.getContent().equals(text)) {
                currentFocusedItem.setContent(text);
            }
            setCurrentFocusedView(null, null);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return ViewHolder.from(parent);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CheckListItem item = getItem(position);
        holder.binding.listContent.setOnFocusChangeListener((v, hasFocus) -> callbacks.onFocus(holder.binding, hasFocus, item));
        holder.binding.checkbox.setOnCheckedChangeListener((v, isChecked) -> callbacks.onCheckedChanged(v, isChecked, item));

        holder.bind(item);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ItemCheckListBinding binding;

        public ViewHolder(@NonNull ItemCheckListBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(CheckListItem item) {
            binding.setCheckListItem(item);
        }

        public static ViewHolder from(ViewGroup parent) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            return new ViewHolder(
                    ItemCheckListBinding.inflate(inflater, parent, false)
            );
        }
    }
}