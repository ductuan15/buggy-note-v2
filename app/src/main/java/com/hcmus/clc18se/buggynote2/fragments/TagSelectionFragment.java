package com.hcmus.clc18se.buggynote2.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavBackStackEntry;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.appbar.MaterialToolbar;
import com.hcmus.clc18se.buggynote2.BuggyNoteActivity;
import com.hcmus.clc18se.buggynote2.R;
import com.hcmus.clc18se.buggynote2.adapters.TagSelectionAdapter;
import com.hcmus.clc18se.buggynote2.adapters.callbacks.TagSelectionAdapterCallbacks;
import com.hcmus.clc18se.buggynote2.database.BuggyNoteDao;
import com.hcmus.clc18se.buggynote2.database.BuggyNoteDatabase;
import com.hcmus.clc18se.buggynote2.databinding.FragmentSelectTagsBinding;
import com.hcmus.clc18se.buggynote2.viewmodels.NoteDetailsViewModel;
import com.hcmus.clc18se.buggynote2.viewmodels.TagSelectionViewModel;
import com.hcmus.clc18se.buggynote2.viewmodels.factories.NoteDetailsViewModelFactory;
import com.hcmus.clc18se.buggynote2.viewmodels.factories.TagSelectionViewModelFactory;

public class TagSelectionFragment extends Fragment {

    private FragmentSelectTagsBinding binding = null;

    private TagSelectionViewModel viewModel;
    private NoteDetailsViewModel noteDetailsViewModel;

    BuggyNoteDao database;
    TagSelectionFragmentArgs args;

    private final TagSelectionAdapterCallbacks callbacks = (itemView, isChecked, tag) -> {
        if (tag.isSelectedState() == isChecked) {
            return;
        }

        tag.setSelectedState(isChecked);
        if (isChecked) {
            viewModel.addSelectedTags(tag);
        } else {
            viewModel.removeSelectedTags(tag);
        }
    };

    private final TagSelectionAdapter adapter = new TagSelectionAdapter(callbacks);

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        database = BuggyNoteDatabase.getInstance(requireContext()).buggyNoteDatabaseDao();
        args = TagSelectionFragmentArgs.fromBundle(requireArguments());

        NavBackStackEntry backStackEntry = Navigation.findNavController(
                requireActivity().findViewById(R.id.nav_host_fragment))
                .getBackStackEntry(R.id.nav_note_details);

        viewModel = new ViewModelProvider(
                backStackEntry,
                new TagSelectionViewModelFactory(database, args.getNoteId())
        ).get(TagSelectionViewModel.class);

        noteDetailsViewModel = new ViewModelProvider(
                backStackEntry,
                new NoteDetailsViewModelFactory(
                        requireActivity().getApplication(),
                        args.getNoteId(),
                        database
                )
        ).get(NoteDetailsViewModel.class);

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentSelectTagsBinding.inflate(inflater, container, false);

        binding.setLifecycleOwner(this);
        binding.setViewModel(viewModel);

        viewModel.getChangesOccurred().observe(getViewLifecycleOwner(), isChanged -> {
                    if (isChanged) {
                        noteDetailsViewModel.requestReloadingData();
                    }
                }
        );

        binding.tagList.setAdapter(adapter);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setUpNavigation();
    }

    private void setUpNavigation() {
        MaterialToolbar toolbar = binding.appBar.toolbar;
        Activity parentActivity = requireActivity();
        if (parentActivity instanceof BuggyNoteActivity) {
            ((BuggyNoteActivity) parentActivity).setSupportActionBar(toolbar);
            NavigationUI.setupActionBarWithNavController(
                    (AppCompatActivity) parentActivity,
                    Navigation.findNavController(binding.getRoot()),
                    ((BuggyNoteActivity) parentActivity).getAppBarConfiguration()
            );

            ActionBar actionBar = ((BuggyNoteActivity) requireActivity()).getSupportActionBar();
            if (actionBar != null) {
                actionBar.setTitle("");
            }
        }
    }
}
