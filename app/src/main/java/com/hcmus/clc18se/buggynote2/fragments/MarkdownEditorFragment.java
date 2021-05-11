package com.hcmus.clc18se.buggynote2.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.hcmus.clc18se.buggynote2.data.NoteWithTags;
import com.hcmus.clc18se.buggynote2.databinding.FragmentMarkdownEditorBinding;

public class MarkdownEditorFragment extends Fragment {

    public FragmentMarkdownEditorBinding binding = null;
    public NoteWithTags noteWithTags;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentMarkdownEditorBinding.inflate(inflater, container, false);
        if (noteWithTags != null) {
            binding.setNoteWithTags(noteWithTags);
        }
        return binding.getRoot();
    }
}