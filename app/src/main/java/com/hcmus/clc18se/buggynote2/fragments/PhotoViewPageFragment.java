package com.hcmus.clc18se.buggynote2.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.hcmus.clc18se.buggynote2.data.Photo;
import com.hcmus.clc18se.buggynote2.databinding.FragmentPhotoViewPageBinding;

public class PhotoViewPageFragment extends Fragment {

    Photo photo;
    FragmentPhotoViewPageBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        if (savedInstanceState != null && savedInstanceState.containsKey(BUNDLE_PHOTO)) {
            photo = (Photo) savedInstanceState.getSerializable(BUNDLE_PHOTO);
        }
        binding = FragmentPhotoViewPageBinding.inflate(inflater, container, false);
        binding.setPhoto(photo);

        binding.executePendingBindings();
        return binding.getRoot();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(BUNDLE_PHOTO, photo);
    }

    public static final String BUNDLE_PHOTO = "BUNDLE_PHOTO";

}
