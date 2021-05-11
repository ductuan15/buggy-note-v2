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

import io.noties.markwon.Markwon;

public class MarkdownViewFragment extends Fragment {

    public FragmentMarkdownViewBinding binding = null;
    public NoteWithTags noteWithTags;

    private Markwon markwon;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        markwon = Markwon.builder(requireContext()).build();

        binding = FragmentMarkdownViewBinding.inflate(inflater, container, false);
        binding.setNoteWithTags(noteWithTags);
        binding.setMarkwon(markwon);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // markwon.setMarkdown(binding.editText, noteWithTags.note.noteContent);
    }
}
