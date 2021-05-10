package com.hcmus.clc18se.buggynote2.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.hcmus.clc18se.buggynote2.BuggyNoteActivity;
import com.hcmus.clc18se.buggynote2.R;
import com.hcmus.clc18se.buggynote2.adapters.TagsAdapter;
import com.hcmus.clc18se.buggynote2.adapters.callbacks.TagsAdapterCallbacks;
import com.hcmus.clc18se.buggynote2.data.Tag;
import com.hcmus.clc18se.buggynote2.database.BuggyNoteDao;
import com.hcmus.clc18se.buggynote2.database.BuggyNoteDatabase;
import com.hcmus.clc18se.buggynote2.databinding.FragmentTagsBinding;
import com.hcmus.clc18se.buggynote2.databinding.ItemTagBinding;
import com.hcmus.clc18se.buggynote2.viewmodels.NotesViewModel;
import com.hcmus.clc18se.buggynote2.viewmodels.TagsViewModel;
import com.hcmus.clc18se.buggynote2.viewmodels.factories.NotesViewModelFactory;
import com.hcmus.clc18se.buggynote2.viewmodels.factories.TagsViewModelFactory;

import timber.log.Timber;

public class TagsFragment extends Fragment {

    private FragmentTagsBinding binding = null;

    private TagsViewModel viewModel;

    private NotesViewModel notesViewModel;

    private final TagsAdapterCallbacks tagsAdapterCallbacks = (itemTagBinding, hasFocus, tag) -> {
        Timber.d("On focus listener is called");

        int removeIcon = R.drawable.ic_outline_label_24;
        int checkIcon = R.drawable.ic_baseline_mode_edit_24;

        if (hasFocus) {
            removeIcon = R.drawable.ic_baseline_delete_24;
            checkIcon = R.drawable.ic_baseline_done_24;

            itemTagBinding.checkButton.setOnClickListener(v -> updateATag(tag, itemTagBinding));
            itemTagBinding.removeButton.setOnClickListener(v -> removeATag(tag));
        } else {
            itemTagBinding.checkButton.setOnClickListener(v -> itemTagBinding.tagContent.requestFocus());
            itemTagBinding.removeButton.setOnClickListener(v -> itemTagBinding.tagContent.requestFocus());
        }
        // set drawable based on the current focus state
        itemTagBinding.checkButton.setImageDrawable(
                ResourcesCompat.getDrawable(getResources(), checkIcon, requireContext().getTheme())
        );
        itemTagBinding.checkButton.invalidate();

        itemTagBinding.removeButton.setImageDrawable(
                ResourcesCompat.getDrawable(getResources(), removeIcon, requireContext().getTheme())
        );
        itemTagBinding.removeButton.invalidate();

        itemTagBinding.getRoot().requestLayout();
    };

    private final TagsAdapter tagsAdapter = new TagsAdapter(tagsAdapterCallbacks);

    private void updateATag(Tag tag, @NonNull ItemTagBinding itemTagBinding) {
        String newTag = itemTagBinding.tagContent.getText().toString().trim();

        if (tag.getName().equals(newTag)) {
            return;
        }

        Tag updatedTag = new Tag(tag.getId(), newTag);
        boolean succeed = viewModel.updateTag(updatedTag);
        if (!succeed) {
            Toast.makeText(requireContext(), "Failed", Toast.LENGTH_SHORT).show();
            itemTagBinding.tagContent.setText(tag.getName());
        } else {
            notesViewModel.requestReloadingData();
        }
    }

    private void performRemovingTag(Tag tag) {
        hideTheKeyboard();
        viewModel.deleteTag(tag);
        notesViewModel.requestReloadingData();
    }

    private void removeATag(Tag tag) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.remove_tag_dialog_title)
                .setMessage(R.string.remove_tag_dialog_content)
                .setPositiveButton(R.string.remove, (v, u) -> {
                    performRemovingTag(tag);
                })
                .setNegativeButton(R.string.cancel, (v, u) -> {
                })
                .show();
    }

    private void hideTheKeyboard() {
        InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(binding.getRoot().getWindowToken(), 0);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        BuggyNoteDao dao = BuggyNoteDatabase.getInstance(requireContext()).buggyNoteDatabaseDao();
        viewModel = new ViewModelProvider(
                requireActivity(),
                new TagsViewModelFactory(dao)
        ).get(TagsViewModel.class);

        notesViewModel = new ViewModelProvider(
                requireActivity(),
                new NotesViewModelFactory(dao, requireActivity().getApplication())
        ).get(NotesViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentTagsBinding.inflate(inflater, container, false);

        setHasOptionsMenu(true);

        binding.setLifecycleOwner(this);
        binding.setTagViewModel(viewModel);
        binding.tagList.setAdapter(tagsAdapter);

        viewModel.getTags().observe(getViewLifecycleOwner(), tags -> {
            tagsAdapter.notifyDataSetChanged();
        });

        return binding.getRoot();

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setUpNavigation();

        binding.addTagLayout.setOnFocusChangeListener(
                (v, hasFocus) -> binding.addTagIcon.setVisibility(hasFocus ? View.VISIBLE : View.INVISIBLE));

        binding.addTagDone.setOnClickListener(v -> {
            String tagContent = binding.addTagContent.getText().toString().trim();
            if (tagContent.isEmpty()) {
                binding.addTagLayout.getEditText().getText().clear();
                return;
            }

            binding.addTagLayout.setErrorEnabled(false);
            boolean succeed = viewModel.insertTag(tagContent);
            if (!succeed) {
                binding.addTagLayout.setErrorEnabled(true);
                binding.addTagLayout.setError(getString(R.string.exist_tag));
            }
            binding.addTagLayout.getEditText().getText().clear();

        });
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
        }
    }
}
