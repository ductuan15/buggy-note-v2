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

import java.util.concurrent.Executors;

import io.noties.markwon.Markwon;
import io.noties.markwon.editor.MarkwonEditor;
import io.noties.markwon.editor.MarkwonEditorTextWatcher;

public class MarkdownEditorFragment extends Fragment {

    public FragmentMarkdownEditorBinding binding = null;
    public NoteWithTags noteWithTags;

    private Markwon markwon;
    private MarkwonEditor editor;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentMarkdownEditorBinding.inflate(inflater, container, false);
        if (noteWithTags != null) {
            binding.setNoteWithTags(noteWithTags);
        }

        markwon = Markwon.create(requireContext());
        editor = MarkwonEditor.create(markwon);

        binding.executePendingBindings();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.noteContent.addTextChangedListener(MarkwonEditorTextWatcher.withPreRender(
                editor,
                Executors.newCachedThreadPool(),
                binding.noteContent)
        );

    }
}