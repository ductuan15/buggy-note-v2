package com.hcmus.clc18se.buggynote2.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.hcmus.clc18se.buggynote2.adapters.callbacks.AudioListAdapterCallback;
import com.hcmus.clc18se.buggynote2.data.Audio;
import com.hcmus.clc18se.buggynote2.databinding.ItemAudioBinding;

public class AudioListAdapter extends ListAdapter<Audio, AudioListAdapter.ViewHolder> {

     private final AudioListAdapterCallback callback;

    public AudioListAdapter(AudioListAdapterCallback callback) {
        super(Audio.diffCallBacks);
         this.callback = callback;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        return new ViewHolder(
                ItemAudioBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Audio Audio = getItem(position);
        holder.bind(Audio);
        holder.binding.setChipOnClickListener(v -> callback.onItemClick(position));
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final ItemAudioBinding binding;

        public ViewHolder(ItemAudioBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

        }

        public void bind(Audio Audio) {
            binding.setAudio(Audio);

        }
    }
}
