package com.hcmus.clc18se.buggynote2.fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;

import com.hcmus.clc18se.buggynote2.R;
import com.hcmus.clc18se.buggynote2.adapters.NoteAdapter;
import com.hcmus.clc18se.buggynote2.adapters.callbacks.NoteAdapterCallbacks;
import com.hcmus.clc18se.buggynote2.data.NoteWithTags;
import com.hcmus.clc18se.buggynote2.data.Tag;
import com.hcmus.clc18se.buggynote2.database.BuggyNoteDao;
import com.hcmus.clc18se.buggynote2.database.BuggyNoteDatabase;
import com.hcmus.clc18se.buggynote2.databinding.DialogTagFilterPreviewBinding;
import com.hcmus.clc18se.buggynote2.viewmodels.NotesViewModel;
import com.hcmus.clc18se.buggynote2.viewmodels.factories.NotesViewModelFactory;

import java.util.List;

public class TagFilterPreviewDialogFragment extends DialogFragment {
    public static final String TAG = "dialog_tag_filter_preview";
    public static final String BUNDLE_TAG = "BUNDLE_TAG";

    private Tag tag;

    private NotesViewModel viewModel;

    DialogTagFilterPreviewBinding binding;


    private final NoteAdapterCallbacks noteAdapterCallbacks = new NoteAdapterCallbacks() {
        @Override
        public void onClick(NoteWithTags note) {
            viewModel.startNavigatingToNoteDetails(note.note.id);
        }

        @Override
        public boolean onMultipleSelect(NoteWithTags note) {
            return false;
        }

        @Override
        public void onPostReordered(List<NoteWithTags> notes) {
        }

        @Override
        public void onItemSwiped(NoteWithTags note) {
        }
    };

    private final NoteAdapter noteAdapter = new NoteAdapter(noteAdapterCallbacks,
            NoteAdapter.DIALOG,
            false);

    public TagFilterPreviewDialogFragment(Tag tag) {
        this.tag = tag;
    }

    public TagFilterPreviewDialogFragment(int contentLayoutId, Tag tag) {
        super(contentLayoutId);
        this.tag = tag;
    }

    public TagFilterPreviewDialogFragment() {
        super();
    }

    public static TagFilterPreviewDialogFragment display(Tag tag, FragmentManager fragmentManager) {
        TagFilterPreviewDialogFragment exampleDialog = new TagFilterPreviewDialogFragment(tag);
        exampleDialog.show(fragmentManager, TAG);
        return exampleDialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null && savedInstanceState.containsKey(BUNDLE_TAG)) {
            tag = (Tag) savedInstanceState.getSerializable(BUNDLE_TAG);
        }

        BuggyNoteDao database = BuggyNoteDatabase.getInstance(requireContext()).buggyNoteDatabaseDao();
        viewModel = new ViewModelProvider(
                requireActivity(),
                new NotesViewModelFactory(
                        database,
                        requireActivity().getApplication()))
                .get(NotesViewModel.class);

        // setStyle(DialogFragment.STYLE_NORMAL, R.style.AppTheme_FullScreenDialog);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = DialogTagFilterPreviewBinding.inflate(inflater, container, false);
        binding.setLifecycleOwner(this);
        binding.noteList.setAdapter(noteAdapter);

        viewModel.filterByTag(tag).observe(getViewLifecycleOwner(), list -> {
            if (list != null) {
                binding.setNoteList(list);
            }
        });

        binding.executePendingBindings();
        return binding.getRoot();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(BUNDLE_TAG, tag);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setUpToolbar();
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            dialog.getWindow().setLayout(width, height);
        }
    }

    private void setUpToolbar() {
        binding.toolbar.setNavigationOnClickListener(v -> dismiss());
        binding.toolbar.setTitle(R.string.dialog_tag_filter_preview_title);
    }
}
