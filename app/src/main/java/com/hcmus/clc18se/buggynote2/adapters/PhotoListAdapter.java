package com.hcmus.clc18se.buggynote2.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.hcmus.clc18se.buggynote2.adapters.callbacks.PhotoListAdapterCallback;
import com.hcmus.clc18se.buggynote2.data.Photo;
import com.hcmus.clc18se.buggynote2.databinding.ItemPhotoBinding;

public class PhotoListAdapter extends ListAdapter<Photo, PhotoListAdapter.ViewHolder> {

     private final PhotoListAdapterCallback callback;

    public PhotoListAdapter(PhotoListAdapterCallback callback) {
        super(Photo.diffCallBacks);
         this.callback = callback;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        return new ViewHolder(
                ItemPhotoBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Photo photo = getItem(position);
        holder.bind(photo);
        holder.itemView.setOnClickListener(v -> callback.onItemClick(position));
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final ItemPhotoBinding binding;

        public ViewHolder(ItemPhotoBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Photo photo) {
            binding.setPhoto(photo);
            binding.executePendingBindings();
        }
    }
}
