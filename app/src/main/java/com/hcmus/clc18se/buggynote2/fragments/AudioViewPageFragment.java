package com.hcmus.clc18se.buggynote2.fragments;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.MediaController;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.hcmus.clc18se.buggynote2.data.Audio;
import com.hcmus.clc18se.buggynote2.data.Photo;
import com.hcmus.clc18se.buggynote2.databinding.FragmentAudioViewPageBinding;
import com.hcmus.clc18se.buggynote2.databinding.FragmentPhotoViewPageBinding;

import java.io.Serializable;

public class AudioViewPageFragment extends Fragment {

    Audio audio;
    FragmentAudioViewPageBinding binding;
    private int position = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        if (savedInstanceState != null && savedInstanceState.containsKey(BUNDLE_AUDIO)) {
            audio = (Audio) savedInstanceState.getSerializable(BUNDLE_AUDIO);
            position = savedInstanceState.getInt(POSITION_AUDIO);
        }
        binding = FragmentAudioViewPageBinding.inflate(inflater, container, false);
        binding.setAudio(audio);

        VideoView videoView =  binding.videoView;
        MediaController mediaController = null;
        if (mediaController == null) {
            mediaController = new MediaController(requireContext());
            mediaController.setAnchorView(videoView);
            videoView.setMediaController(mediaController);
        }
        try {
            videoView.setVideoURI(Uri.parse(audio.uri));
        } catch (Exception e) {
            e.printStackTrace();
        }
        videoView.requestFocus();
        MediaController finalMediaController = mediaController;
        videoView.setOnPreparedListener(mediaPlayer -> {
            videoView.seekTo(position);
            videoView.start();
            mediaPlayer.setOnVideoSizeChangedListener((mp, width, height) -> finalMediaController.setAnchorView(videoView));
        });

        binding.executePendingBindings();
        return binding.getRoot();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(BUNDLE_AUDIO, audio);
        position = binding.videoView.getCurrentPosition();
        outState.putInt(POSITION_AUDIO,position);
    }

    public static final String BUNDLE_AUDIO = "BUNDLE_AUDIO";
    public static final String POSITION_AUDIO = "POSITION_AUDIO";
}
