package com.hcmus.clc18se.buggynote2.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavBackStackEntry;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.hcmus.clc18se.buggynote2.BuggyNoteActivity;
import com.hcmus.clc18se.buggynote2.R;
import com.hcmus.clc18se.buggynote2.adapters.BindingAdapters;
import com.hcmus.clc18se.buggynote2.adapters.CheckListAdapter;
import com.hcmus.clc18se.buggynote2.adapters.callbacks.CheckListAdapterCallbacks;
import com.hcmus.clc18se.buggynote2.data.CheckListItem;
import com.hcmus.clc18se.buggynote2.data.NoteWithTags;
import com.hcmus.clc18se.buggynote2.database.BuggyNoteDao;
import com.hcmus.clc18se.buggynote2.database.BuggyNoteDatabase;
import com.hcmus.clc18se.buggynote2.databinding.FragmentNoteDetailsBinding;
import com.hcmus.clc18se.buggynote2.databinding.ItemCheckListBinding;
import com.hcmus.clc18se.buggynote2.utils.PropertiesBSFragment;
import com.hcmus.clc18se.buggynote2.utils.TextFormatter;
import com.hcmus.clc18se.buggynote2.viewmodels.NoteDetailsViewModel;
import com.hcmus.clc18se.buggynote2.viewmodels.NotesViewModel;
import com.hcmus.clc18se.buggynote2.viewmodels.factories.NoteDetailsViewModelFactory;
import com.hcmus.clc18se.buggynote2.viewmodels.factories.NotesViewModelFactory;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public class NoteDetailsFragment extends Fragment implements PropertiesBSFragment.Properties {

    private FragmentNoteDetailsBinding binding = null;
    private NoteDetailsFragmentArgs arguments;

    private NotesViewModel notesViewModel;
    private NoteDetailsViewModel viewModel;

    private BuggyNoteDao db;

    private final View.OnClickListener tagOnClickListener = v -> viewModel.navigateToTagSelection();

    private Menu menu;

    private PropertiesBSFragment propertiesBSFragment;
    int currentColor;

    private final CheckListAdapterCallbacks checkListAdapterCallbacks = new CheckListAdapterCallbacks() {
        @Override
        public void onFocus(ItemCheckListBinding binding,
                            boolean hasFocus,
                            CheckListItem item) {
            binding.removeButton.setVisibility(
                    hasFocus ? View.VISIBLE : View.INVISIBLE
            );

            if (hasFocus) {
                checkListAdapter.setCurrentFocusedView(binding, item);

                binding.removeButton.setOnClickListener(v -> {
                    List<CheckListItem> list = new ArrayList<>(checkListAdapter.getCurrentList());
                    list.remove(item);
                    checkListAdapter.submitList(list);
                });
            } else {
                checkListAdapter.setCurrentFocusedView(null, null);
            }

            String text = binding.listContent.getText().toString().trim();
            if (text.isEmpty()) {
                binding.listContent.setText(item.getContent());
            }

            if (!item.getContent().equals(text)) {
                item.setContent(text);
            }
        }

        @Override
        public void onCheckedChanged(CompoundButton itemView,
                                     boolean isChecked,
                                     CheckListItem item) {
            item.setChecked(isChecked);
        }
    };

    private final CheckListAdapter checkListAdapter = new CheckListAdapter(checkListAdapterCallbacks);

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        arguments = NoteDetailsFragmentArgs.fromBundle(requireArguments());
        db = BuggyNoteDatabase.getInstance(requireContext()).buggyNoteDatabaseDao();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentNoteDetailsBinding.inflate(inflater, container, false);

        binding.setLifecycleOwner(this);

        NavBackStackEntry backStackEntry = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment)
                .getBackStackEntry(R.id.nav_note_details);

        viewModel = new ViewModelProvider(
                backStackEntry,
                new NoteDetailsViewModelFactory(
                        arguments.getNoteId(),
                        db
                ))
                .get(NoteDetailsViewModel.class);

        notesViewModel = new ViewModelProvider(
                requireActivity(),
                new NotesViewModelFactory(
                        db,
                        requireActivity().getApplication()))
                .get(NotesViewModel.class);

        binding.setNoteDetailsViewModel(viewModel);
        binding.setChipOnClickListener(tagOnClickListener);

        propertiesBSFragment = new PropertiesBSFragment();
        propertiesBSFragment.setPropertiesChangeListener(this);

        binding.addCheckListDone.setOnClickListener(v -> {
            String content = binding.addCheckListContent.getText().toString().trim();
            binding.addCheckListTextLayout.getEditText().getText().clear();
            if (content.isEmpty()) {
                return;
            }

            binding.addCheckListTextLayout.setErrorEnabled(false);

            List<CheckListItem> currentList = checkListAdapter.getCurrentList();

            List<CheckListItem> items = new ArrayList<>();
            if (currentList != null) {
                items.addAll(currentList);
            }

            items.add(new CheckListItem(items.size(), false, content));
            checkListAdapter.submitList(items);

        });

        initRecyclerViews();
        initObservers();

        return binding.getRoot();
    }

    private void initObservers() {
        viewModel.getNote().observe(getViewLifecycleOwner(), noteWithTags -> {
            updateMenu();
        });

        viewModel.getReloadDataRequestState().observe(getViewLifecycleOwner(),
                state -> {
                    if (state) {
                        viewModel.reloadNote();
                        notesViewModel.requestReloadingData();
                        viewModel.doneRequestingReloadData();
                    }
                });

        viewModel.getNavigateToTagSelection().observe(getViewLifecycleOwner(),
                id -> {
                    if (id != null) {
                        Navigation.findNavController(requireActivity(), R.id.nav_host_fragment).navigate(
                                NoteDetailsFragmentDirections.actionNavNoteDetailsToTagSelectionFragment(
                                        arguments.getNoteId()
                                )
                        );
                        viewModel.doneNavigatingToTagSelection();
                    }
                });

        viewModel.getDeleteRequest().observe(getViewLifecycleOwner(), state -> {
                    if (state != null && state) {
                        notesViewModel.requestReloadingData();
                        requireActivity().onBackPressed();
                        viewModel.doneRequestingReloadData();
                    }
                }
        );

        viewModel.getCheckListItems().observe(getViewLifecycleOwner(), list -> {
            if (list != null) {
                checkListAdapter.submitList(list);
            }
        });
    }

    private void initRecyclerViews() {
        //RecyclerView checkListRecyclerView = binding.scrollLayout.findViewById()
        binding.checkListRecyclerView.setAdapter(checkListAdapter);
    }

    @Override
    public void onPause() {
        super.onPause();
        InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(binding.getRoot().getWindowToken(), 0);
        }

        saveNote(false);
    }

    void saveNote(boolean require) {

        String title = ((EditText) binding.layout.findViewById(R.id.text_view_title)).getText().toString();
        String content = ((EditText) binding.layout.findViewById(R.id.note_content)).getText().toString();
        NoteWithTags noteWithTags = viewModel.getNote().getValue();

        checkListAdapter.saveFocusedView();

        if (noteWithTags != null) {

            BuggyNoteDatabase.databaseWriteExecutor.execute(() -> {

                String encodedCheckListContent = "";
                if (noteWithTags.note.isCheckList()) {
                    encodedCheckListContent = CheckListItem.toNoteContent(checkListAdapter.getCurrentList());
                }

                if (!title.equals(noteWithTags.note.title) ||
                        (noteWithTags.note.isPlainText() && !content.equals(noteWithTags.note.noteContent)) ||
                        (noteWithTags.note.isCheckList() && !noteWithTags.note.noteContent.equals(encodedCheckListContent)) ||
                        require) {
                    noteWithTags.note.title = title;

                    if (noteWithTags.note.isPlainText()) {
                        noteWithTags.note.noteContent = content;
                    } else if (noteWithTags.note.isCheckList()) {
                        noteWithTags.note.noteContent = encodedCheckListContent;
                    }

                    noteWithTags.note.lastModify = System.currentTimeMillis();
                    noteWithTags.note.color = currentColor;

                    Timber.d("Set new note content");
                    db.updateNote(noteWithTags.note);
                    notesViewModel.requestReloadingData();
                }

            });
        }

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setUpNavigation();
    }

    private void setUpNavigation() {

        setHasOptionsMenu(true);

        MaterialToolbar toolbar = binding.appBar.toolbar;
        Activity parentActivity = requireActivity();

        if (parentActivity instanceof BuggyNoteActivity) {
            ((BuggyNoteActivity) parentActivity).setSupportActionBar(toolbar);

            BottomAppBar bottomAppBar = binding.coordinatorLayout.findViewById(R.id.bottom_bar);
            bottomAppBar.setOnMenuItemClickListener(bottomAppBarClickListener);

            NavigationUI.setupActionBarWithNavController(
                    (AppCompatActivity) parentActivity,
                    Navigation.findNavController(binding.getRoot()),
                    ((BuggyNoteActivity) parentActivity).getAppBarConfiguration()
            );
        }
    }

    private final Toolbar.OnMenuItemClickListener bottomAppBarClickListener = item -> {
        switch (item.getItemId()) {
            case R.id.action_add_tag: {
                viewModel.navigateToTagSelection();
                return true;
            }
            case R.id.action_remove_note: {
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle(getString(R.string.remove_from_device))
                        .setMessage(getString(R.string.remove_confirmation)).setNegativeButton(getResources().getString(R.string.cancel), (dialog, which) -> {
                })
                        .setPositiveButton(getResources().getString(R.string.remove), (dialog, which) -> {
                            viewModel.deleteMe();
                        })
                        .show();
                return true;
            }
            case R.id.action_set_bold:
            case R.id.action_set_italic:
            case R.id.action_set_font_type:
            case R.id.action_alignment: {
                actionFormat(item.getItemId());
                return true;
            }
            case R.id.action_set_color: {
                showBottomSheetDialogFragment(propertiesBSFragment);
                return true;
            }
            case R.id.action_share: {
                String title = ((EditText) binding.layout.findViewById(R.id.text_view_title)).getText().toString();
                String content = ((EditText) binding.layout.findViewById(R.id.note_content)).getText().toString();
                String contentShare = title + "\n" + content;
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TITLE, "Share note");
//                sendIntent.setData(contentUri);
//                sendIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                sendIntent.putExtra(Intent.EXTRA_TEXT, contentShare);
                sendIntent.setType("*/*");
                startActivity(Intent.createChooser(sendIntent, "Share to"));
                return true;
            }


        }
        return false;
    };

    private void actionFormat(int itemId) {
        int targetId = getActionFormatTarget();
        NoteWithTags noteWithTags = viewModel.getNote().getValue();
        if (noteWithTags != null) {
            TextFormatter formatter;
            if (targetId == R.id.text_view_title) {
                formatter = noteWithTags.getTitleFormat();
            } else {
                formatter = noteWithTags.getContentFormat();
            }

            switch (itemId) {
                case R.id.action_set_bold:
                    formatter.toggleBold();
                    break;
                case R.id.action_set_italic:
                    formatter.toggleItalic();
                    break;
                case R.id.action_set_font_type:
                    formatter.toggleFontType();
                    break;
                case R.id.action_alignment:
                    formatter.toggleAlignment();
                    break;
            }

            if (targetId == R.id.text_view_title) {
                EditText title = binding.layout.findViewById(R.id.text_view_title);
                BindingAdapters.setNoteTitleFormat(title, noteWithTags);
                noteWithTags.note.titleFormat = formatter.toString();
            } else {
                EditText content = binding.layout.findViewById(R.id.note_content);
                BindingAdapters.setNoteContentFormat(content, noteWithTags);
                noteWithTags.note.contentFormat = formatter.toString();
            }

            saveNote(true);
        }
    }

    private int getActionFormatTarget() {
        EditText title = binding.layout.findViewById(R.id.text_view_title);
        if (title.isFocused()) {
            return R.id.text_view_title;
        }
        return R.id.note_content;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.note_detail, menu);
        this.menu = menu;
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuItem pinnedItem = menu.findItem(R.id.action_pin);

        NoteWithTags note = viewModel.getNote().getValue();
        if (note != null && pinnedItem != null) {
            int pinIcon;
            if (note.note.isPinned) {
                pinIcon = R.drawable.ic_baseline_push_pin_24;
            } else {
                pinIcon = R.drawable.ic_outline_push_pin_24;
            }
            pinnedItem.setIcon(pinIcon);
        }
    }

    private void updateMenu() {
        if (menu != null) {
            onPrepareOptionsMenu(menu);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {

            case R.id.action_pin: {
                NoteWithTags note = viewModel.getNote().getValue();
                if (note != null) {
                    int pinIcon;
                    if (note.note.isPinned) {
                        pinIcon = R.drawable.ic_baseline_push_pin_24;
                    } else {
                        pinIcon = R.drawable.ic_outline_push_pin_24;
                    }
                    item.setIcon(pinIcon);

                    viewModel.togglePin();
                    saveNote(true);
                    return true;
                }
            }
        }
        return false;
    }

    private void showBottomSheetDialogFragment(BottomSheetDialogFragment fragment) {
        if (fragment == null || fragment.isAdded()) {
            return;
        }
        fragment.show(getChildFragmentManager(), fragment.getTag());
    }

    @Override
    public void onColorChanged(int colorCode) {
        binding.coordinatorLayout.findViewById(R.id.coordinator_layout).setBackgroundColor(colorCode);
        currentColor = colorCode;
        saveNote(true);
    }
}
