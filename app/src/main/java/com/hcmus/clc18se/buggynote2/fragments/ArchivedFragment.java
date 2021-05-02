package com.hcmus.clc18se.buggynote2.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.hcmus.clc18se.buggynote2.BuggyNoteActivity;
import com.hcmus.clc18se.buggynote2.R;
import com.hcmus.clc18se.buggynote2.adapters.NoteAdapter;
import com.hcmus.clc18se.buggynote2.adapters.TagFilterAdapter;
import com.hcmus.clc18se.buggynote2.adapters.callbacks.NoteAdapterCallbacks;
import com.hcmus.clc18se.buggynote2.adapters.callbacks.NoteItemTouchHelperCallback;
import com.hcmus.clc18se.buggynote2.adapters.callbacks.TagFilterAdapterCallbacks;
import com.hcmus.clc18se.buggynote2.data.NoteWithTags;
import com.hcmus.clc18se.buggynote2.data.Tag;
import com.hcmus.clc18se.buggynote2.database.BuggyNoteDao;
import com.hcmus.clc18se.buggynote2.database.BuggyNoteDatabase;
import com.hcmus.clc18se.buggynote2.databinding.FragmentArchivedBinding;
import com.hcmus.clc18se.buggynote2.utils.SpaceItemDecoration;
import com.hcmus.clc18se.buggynote2.utils.ViewUtils;
import com.hcmus.clc18se.buggynote2.viewmodels.NotesViewModel;
import com.hcmus.clc18se.buggynote2.viewmodels.TagsViewModel;
import com.hcmus.clc18se.buggynote2.viewmodels.factories.NotesViewModelFactory;
import com.hcmus.clc18se.buggynote2.viewmodels.factories.TagsViewModelFactory;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;


public class ArchivedFragment extends Fragment {
    private SharedPreferences preferences = null;
    private FragmentArchivedBinding binding = null;

    private NotesViewModel notesViewModel;

    private final ArchivedActionMode actionModeCallback = new ArchivedActionMode();

    private final NoteAdapterCallbacks noteAdapterCallbacks = new NoteAdapterCallbacks() {
        @Override
        public void onClick(NoteWithTags note) {
            notesViewModel.startNavigatingToNoteDetails(note.note.id);
        }

        @Override
        public boolean onMultipleSelect(NoteWithTags note) {
            // TODO: lock the drawer
            invalidateCab();
            return false;
        }

        @Override
        public void onPostReordered(List<NoteWithTags> notes) {
            notesViewModel.requestReordering();
        }

        @Override
        public void onItemSwiped(NoteWithTags note) {
            notesViewModel.moveToNoteList(note);
            Snackbar.make(binding.getRoot(), R.string.unarchived_moved_to_note_list, Snackbar.LENGTH_LONG)
                    .setAction(R.string.undo, v -> notesViewModel.moveToArchived(note))
                    .show();

            actionModeCallback.finishActionMode();
        }
    };

    private final NoteAdapter archivedNoteAdapter = new NoteAdapter(noteAdapterCallbacks, NoteAdapter.ARCHIVE_TAG);

    private TagsViewModel tagsViewModel;

    private final TagFilterAdapterCallbacks tagFilterAdapterCallbacks = (isChecked, tag) -> {
        if (tag.isSelectedState() != isChecked) {
            archivedNoteAdapter.finishSelection();

            actionModeCallback.finishActionMode();

            boolean notFilter = true;
            if (tagsViewModel.getTags().getValue() != null) {
                for (Tag t : tagsViewModel.getTags().getValue()) {
                    if (t.isSelectedState()) {
                        notFilter = false;
                        break;
                    }
                }
            }

            if (notesViewModel.getOrderChanged().getValue() != null
                    && notesViewModel.getOrderChanged().getValue()
                    && notFilter
            ) {
                notesViewModel.reorderNotes(archivedNoteAdapter.getCurrentList());
                notesViewModel.finishReordering();
            }

            tag.setSelectedState(isChecked);
            notesViewModel.filterByTags(tagsViewModel.getTags().getValue());
        }
    };

    private final TagFilterAdapter tagFilterAdapter = new TagFilterAdapter(tagFilterAdapterCallbacks);

    private ItemTouchHelper noteListTouchHelper;

    private void invalidateCab() {

        int nSelectedItems = archivedNoteAdapter.numberOfSelectedItems();
        if (nSelectedItems == 0) {
            actionModeCallback.finishActionMode();

            return;
        }

        if (actionModeCallback.actionMode == null) {
            binding.appBar.toolbar.startActionMode(actionModeCallback);
        }

        archivedNoteAdapter.enableSelection();

        actionModeCallback.actionMode.invalidate();

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        BuggyNoteDao database = BuggyNoteDatabase.getInstance(requireContext()).buggyNoteDatabaseDao();
        preferences = PreferenceManager.getDefaultSharedPreferences(requireContext());

        notesViewModel = new ViewModelProvider(
                requireActivity(),
                new NotesViewModelFactory(
                        database,
                        requireActivity().getApplication()))
                .get(NotesViewModel.class);

        tagsViewModel = new ViewModelProvider(
                requireActivity(),
                new TagsViewModelFactory(database)
        ).get(TagsViewModel.class);

        NoteItemTouchHelperCallback callback = new NoteItemTouchHelperCallback(archivedNoteAdapter);
        noteListTouchHelper = new ItemTouchHelper(callback);

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentArchivedBinding.inflate(inflater, container, false);

        setHasOptionsMenu(true);

        binding.setLifecycleOwner(this);
        binding.setNoteViewModel(notesViewModel);
        binding.setTagViewModel(tagsViewModel);

        initRecyclerViews();
        initObservers();

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setUpNavigation();
    }

    @Override
    public void onPause() {
        super.onPause();

        InputMethodManager imm =
                (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(binding.getRoot().getWindowToken(), 0);
        }

        if (notesViewModel.getOrderChanged().getValue() != null
                && notesViewModel.getOrderChanged().getValue()
        ) {
            boolean isSelected = false;
            if (tagsViewModel.getTags().getValue() != null) {
                for (Tag tag : tagsViewModel.getTags().getValue()) {
                    if (tag.isSelectedState()) {
                        isSelected = true;
                        break;
                    }
                }
            }
            if (!isSelected) {
                notesViewModel.reorderNotes(archivedNoteAdapter.getCurrentList());
                notesViewModel.loadNotes();
                notesViewModel.finishReordering();
            }
        }
    }

    void initNoteAdapter(RecyclerView recyclerView,
                         RecyclerView.Adapter<?> adapter,
                         ItemTouchHelper touchHelper,
                         boolean addItemDecoration) {

        ViewUtils.setupLayoutManagerForNoteList(binding.noteList, preferences);

        recyclerView.setAdapter(adapter);

        if (addItemDecoration) {
            recyclerView.addItemDecoration(new SpaceItemDecoration(
                    (int) requireContext().getResources().getDimension(R.dimen.item_note_margin)
            ));
        }

        if (touchHelper != null) {
            touchHelper.attachToRecyclerView(recyclerView);
        }
    }

    void initRecyclerViews() {
        initNoteAdapter(binding.noteList, archivedNoteAdapter, noteListTouchHelper, true);

        binding.tagFilterList.setAdapter(tagFilterAdapter);
        binding.tagFilterList.addItemDecoration(
                new SpaceItemDecoration((int) getResources().getDimension(R.dimen.item_note_margin))
        );
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

    private void initObservers() {
        notesViewModel.getReloadDataRequest().observe(getViewLifecycleOwner(), state -> {
            if (state != null && state) {

                if (tagsViewModel.getTags().getValue() != null) {
                    notesViewModel.filterByTags(tagsViewModel.getTags().getValue());
                } else {
                    notesViewModel.loadNotes();
                }

                notesViewModel.doneRequestingLoadData();

                binding.noteList.invalidate();
                binding.noteList.requestLayout();

            }
        });

        notesViewModel.getNoteList().observe(getViewLifecycleOwner(), noteWithTags -> {
            if (noteWithTags != null) {
                archivedNoteAdapter.notifyDataSetChanged();
                Timber.d("loaded");
            }
        });

        notesViewModel.headerLabelVisibility.observe(getViewLifecycleOwner(), visibility -> {
            archivedNoteAdapter.notifyDataSetChanged();
        });

        tagsViewModel.getTags().observe(getViewLifecycleOwner(), tags -> {
            tagFilterAdapter.notifyDataSetChanged();
        });

        notesViewModel.getNavigateToNoteDetails().observe(getViewLifecycleOwner(), id -> {
            if (id != null) {
                Navigation.findNavController(requireActivity(), R.id.nav_host_fragment).navigate(
                        NotesFragmentDirections.actionNavNotesToNoteDetailsFragment(id)
                );
                notesViewModel.doneNavigatingToNoteDetails();
            }
        });

    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        ViewUtils.setupLayoutManagerForNoteList(binding.noteList, preferences);
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        super.onPrepareOptionsMenu(menu);

        final String list = "1";
        String noteListDisplayType = preferences.getString(
                getString(R.string.note_list_view_type_key), "0");

        MenuItem noteListDisplayItem = menu.findItem(R.id.note_list_item_view_type);
        if (noteListDisplayType.equals(list)) {
            noteListDisplayItem.setIcon(R.drawable.ic_baseline_grid_view_24);
        } else {
            noteListDisplayItem.setIcon(R.drawable.ic_baseline_list_alt_24);
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.archive, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.note_list_item_view_type) {
            onItemTypeOptionClicked();
            if (preferences.getString(getString(R.string.note_list_view_type_key), "0").equals("0")) {
                item.setIcon(R.drawable.ic_baseline_list_alt_24);
            } else {
                item.setIcon(R.drawable.ic_baseline_grid_view_24);
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void onItemTypeOptionClicked() {
        String currentItemView = preferences.getString(getString(R.string.note_list_view_type_key), "0");
        String nextItemView = currentItemView.equals("0") ? "1" : "0";

        preferences.edit()
                .putString(getString(R.string.note_list_view_type_key), nextItemView)
                .apply();

        refreshNoteList();
    }

    private void refreshNoteList() {
        binding.noteList.setAdapter(null);
        initNoteAdapter(binding.noteList, archivedNoteAdapter, noteListTouchHelper, false);

        notesViewModel.requestReordering();
        archivedNoteAdapter.notifyDataSetChanged();
        // binding.noteList.startLayoutAnimation();
    }

    class ArchivedActionMode implements ActionMode.Callback {
        public ActionMode actionMode;

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            actionMode = mode;
            requireActivity().getMenuInflater().inflate(R.menu.archive_context, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_unarchived: {
                    int nSelectedItems = archivedNoteAdapter.numberOfSelectedItems();

                    if (nSelectedItems == 0) {
                        return true;
                    }

                    List<NoteWithTags> selectedItems = new ArrayList<>(archivedNoteAdapter.getSelectedItems());

                    NoteWithTags[] archiveNotes = selectedItems.toArray(new NoteWithTags[]{});

                    notesViewModel.moveToNoteList(archiveNotes);
                    actionModeCallback.finishActionMode();

                    return true;
                }

                case R.id.action_select_all: {

                    archivedNoteAdapter.selectAll();
                    invalidateCab();

                    return true;
                }

                case R.id.action_remove_note: {
                    List<NoteWithTags> selectedArchivedItems = archivedNoteAdapter.getSelectedItems();

                    List<NoteWithTags> selectedItems = new ArrayList<>(archivedNoteAdapter.getSelectedItems());

                    if (selectedArchivedItems.isEmpty()) {
                        return true;
                    }

                    new MaterialAlertDialogBuilder(requireContext())
                            .setTitle(getString(R.string.remove_from_device))
                            .setMessage(getString(R.string.remove_confirmation))
                            .setNegativeButton(getString(R.string.cancel), (v, u) -> {
                            })
                            .setPositiveButton(getString(R.string.remove), (v, u) -> {
                                notesViewModel.removeNote(selectedItems);
                                actionModeCallback.finishActionMode();
                            })
                            .show();
                    return true;
                }
            }
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            archivedNoteAdapter.finishSelection();

//            Activity parentActivity = requireActivity();
//            if (parentActivity instanceof ControllableDrawerActivity) {
//                parentActivity.unlockTheDrawer();
//            }

            actionMode = null;
        }

        void finishActionMode() {
            if (actionMode != null) {
                actionMode.finish();
            }
        }
    }

}

