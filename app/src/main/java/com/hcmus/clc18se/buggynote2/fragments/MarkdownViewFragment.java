package com.hcmus.clc18se.buggynote2.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.hcmus.clc18se.buggynote2.data.NoteWithTags;
import com.hcmus.clc18se.buggynote2.databinding.FragmentMarkdownViewBinding;

public class MarkdownViewFragment extends Fragment {

    public FragmentMarkdownViewBinding binding = null;
    public NoteWithTags noteWithTags;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentMarkdownViewBinding.inflate(inflater, container, false);
        binding.setNoteWithTags(noteWithTags);

        return binding.getRoot();
    }
}
