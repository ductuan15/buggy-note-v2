package com.hcmus.clc18se.buggynote2.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.ConcatAdapter;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.hcmus.clc18se.buggynote2.BuggyNoteActivity;
import com.hcmus.clc18se.buggynote2.R;
import com.hcmus.clc18se.buggynote2.adapters.NoteAdapter;
import com.hcmus.clc18se.buggynote2.adapters.NoteHeaderAdapter;
import com.hcmus.clc18se.buggynote2.adapters.TagFilterAdapter;
import com.hcmus.clc18se.buggynote2.adapters.callbacks.NoteAdapterCallbacks;
import com.hcmus.clc18se.buggynote2.adapters.callbacks.NoteItemTouchHelperCallback;
import com.hcmus.clc18se.buggynote2.adapters.callbacks.TagFilterAdapterCallbacks;
import com.hcmus.clc18se.buggynote2.data.Note;
import com.hcmus.clc18se.buggynote2.data.NoteWithTags;
import com.hcmus.clc18se.buggynote2.data.Tag;
import com.hcmus.clc18se.buggynote2.database.BuggyNoteDao;
import com.hcmus.clc18se.buggynote2.database.BuggyNoteDatabase;
import com.hcmus.clc18se.buggynote2.databinding.FragmentNotesBinding;
import com.hcmus.clc18se.buggynote2.utils.SpaceItemDecoration;
import com.hcmus.clc18se.buggynote2.utils.interfaces.ControllableDrawerActivity;
import com.hcmus.clc18se.buggynote2.utils.interfaces.OnBackPressed;
import com.hcmus.clc18se.buggynote2.utils.views.PropertiesBSFragment;
import com.hcmus.clc18se.buggynote2.utils.views.ViewAnimation;
import com.hcmus.clc18se.buggynote2.utils.views.ViewUtils;
import com.hcmus.clc18se.buggynote2.viewmodels.NotesViewModel;
import com.hcmus.clc18se.buggynote2.viewmodels.TagsViewModel;
import com.hcmus.clc18se.buggynote2.viewmodels.factories.NotesViewModelFactory;
import com.hcmus.clc18se.buggynote2.viewmodels.factories.TagsViewModelFactory;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public class NotesFragment extends Fragment implements OnBackPressed {

    private SharedPreferences preferences = null;

    private FragmentNotesBinding binding = null;

    private NotesViewModel notesViewModel;

    private boolean isFabRotate = false;

    private final NoteListActionMode actionModeCallback = new NoteListActionMode();

    private final NoteAdapterCallbacks noteAdapterCallbacks = new NoteAdapterCallbacks() {
        @Override
        public void onClick(NoteWithTags note) {
            notesViewModel.startNavigatingToNoteDetails(note.note.id);
        }

        @Override
        public boolean onMultipleSelect(NoteWithTags note) {
            if (requireActivity() instanceof ControllableDrawerActivity) {
                ((ControllableDrawerActivity) requireActivity()).lockTheDrawer();
            }
            invalidateCab();
            return false;
        }

        @Override
        public void onPostReordered(List<NoteWithTags> notes) {
            notesViewModel.requestReordering();
        }

        @Override
        public void onItemSwiped(NoteWithTags note) {
            notesViewModel.moveToArchived(note);
            Snackbar.make(binding.getRoot(), R.string.moved_to_archive, Snackbar.LENGTH_LONG)
                    .setAction(R.string.undo, v -> notesViewModel.moveToNoteList(note))
                    .show();

            actionModeCallback.finishActionMode();
        }
    };

    private final NoteAdapter pinnedNotesAdapter = new NoteAdapter(noteAdapterCallbacks, NoteAdapter.PIN_TAG);

    private final NoteAdapter unpinnedNotesAdapter = new NoteAdapter(noteAdapterCallbacks, NoteAdapter.UNPIN_TAG);

    private ConcatAdapter concatAdapter;

    private TagsViewModel tagsViewModel;

    private final TagFilterAdapterCallbacks tagFilterAdapterCallbacks = new TagFilterAdapterCallbacks() {
        @Override
        public void onCheckChanged(boolean isChecked, Tag tag) {
            if (tag.isSelectedState() != isChecked) {
                pinnedNotesAdapter.finishSelection();
                unpinnedNotesAdapter.finishSelection();

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
                    notesViewModel.reorderNotes(pinnedNotesAdapter.getCurrentList());
                    notesViewModel.reorderNotes(unpinnedNotesAdapter.getCurrentList());
                    notesViewModel.finishReordering();
                }

                tag.setSelectedState(isChecked);
                notesViewModel.filterByTags(tagsViewModel.getTags().getValue());
            }
        }

        @Override
        public boolean onLongClick(Tag tag) {
            TagFilterPreviewDialogFragment.display(tag, getChildFragmentManager());
            return true;
        }
    };
    private final TagFilterAdapter tagFilterAdapter = new TagFilterAdapter(tagFilterAdapterCallbacks);

    private ItemTouchHelper noteListTouchHelper;

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

        ConcatAdapter.Config config = new ConcatAdapter.Config.Builder()
                .setIsolateViewTypes(false)
                .build();

        concatAdapter = new ConcatAdapter(config,
                new NoteHeaderAdapter(getString(R.string.pinned),
                        notesViewModel.headerLabelVisibility,
                        R.drawable.ic_outline_push_pin_24),
                pinnedNotesAdapter,
                new NoteHeaderAdapter(getString(R.string.others),
                        notesViewModel.headerLabelVisibility,
                        null),
                unpinnedNotesAdapter
        );


        NoteItemTouchHelperCallback callback = new NoteItemTouchHelperCallback(
                pinnedNotesAdapter,
                unpinnedNotesAdapter
        );
        noteListTouchHelper = new ItemTouchHelper(callback);

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentNotesBinding.inflate(inflater, container, false);

        setHasOptionsMenu(true);

        binding.setLifecycleOwner(this);

        binding.setNoteViewModel(notesViewModel);
        binding.setTagViewModel(tagsViewModel);
        setUpNavigationBar();

        initRecyclerViews();
        initObservers();

        binding.executePendingBindings();
        return binding.getRoot();
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
        initNoteAdapter(binding.noteList, concatAdapter, noteListTouchHelper, true);

        binding.tagFilterList.setAdapter(tagFilterAdapter);
        binding.tagFilterList.addItemDecoration(
                new SpaceItemDecoration((int) getResources().getDimension(R.dimen.item_note_margin))
        );
    }

    void initObservers() {

        notesViewModel.getReloadDataRequest().observe(getViewLifecycleOwner(), state -> {
            if (state != null && state) {

                if (tagsViewModel.getTags().getValue() != null) {
                    notesViewModel.filterByTags(tagsViewModel.getTags().getValue());
                } else {
                    notesViewModel.loadNotes();
                }

                binding.noteList.invalidate();
                binding.noteList.requestLayout();

                notesViewModel.doneRequestingLoadData();
            }
        });

        notesViewModel.getReloadItemRequest().observe(getViewLifecycleOwner(), id -> {
            if (id != null) {
                // get the adapter position of the note
                notesViewModel.loadNoteId(id);

                findItemToNotifyChange(id, pinnedNotesAdapter);
                findItemToNotifyChange(id, unpinnedNotesAdapter);

                notesViewModel.doneRequestReloadingItem();
            }
        });

        notesViewModel.getNoteList().observe(getViewLifecycleOwner(), noteWithTags -> {
            if (noteWithTags != null) {
                concatAdapter.notifyDataSetChanged();
                Timber.d("loaded");
            }
        });

        notesViewModel.headerLabelVisibility.observe(getViewLifecycleOwner(), visibility -> {
            concatAdapter.notifyDataSetChanged();
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

    private void findItemToNotifyChange(Long id, NoteAdapter adapter) {
        List<NoteWithTags> noteList = adapter.getCurrentList();

        for (int i = 0; i < noteList.size(); ++i) {
            if (noteList.get(i).note.getId() == id) {
                adapter.notifyItemChanged(i);
                return;
            }
        }
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
                notesViewModel.reorderNotes(pinnedNotesAdapter.getCurrentList());
                notesViewModel.reorderNotes(unpinnedNotesAdapter.getCurrentList());
                notesViewModel.loadNotes();
                notesViewModel.finishReordering();
            }
        }
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
            noteListDisplayItem.setIcon(R.drawable.ic_outline_view_agenda_24);
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.main, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();

        searchItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                noteListTouchHelper.attachToRecyclerView(null);
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                if (tagsViewModel.getTags().getValue() != null) {
                    notesViewModel.filterByTags(tagsViewModel.getTags().getValue());
                }
                noteListTouchHelper.attachToRecyclerView(binding.noteList);
                return true;
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (query != null) {
                    List<Tag> tagList = tagsViewModel.getTags().getValue();
                    if (tagList != null) {
                        notesViewModel.searchWithSelectedTags(query, tagList);
                    } else {
                        notesViewModel.search(query);
                    }
                    return true;
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                List<Tag> tagList = tagsViewModel.getTags().getValue();
                if (newText != null) {
                    if (tagList != null) {
                        notesViewModel.searchWithSelectedTags(newText, tagList);
                    } else {
                        notesViewModel.search(newText);
                    }
                } else if (tagList != null) {
                    notesViewModel.filterByTags(tagsViewModel.getTags().getValue());
                }
                return true;
            }
        });
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
        initNoteAdapter(binding.noteList, concatAdapter, noteListTouchHelper, false);

        notesViewModel.requestReordering();
        concatAdapter.notifyDataSetChanged();
        binding.noteList.startLayoutAnimation();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.note_list_item_view_type) {
            onItemTypeOptionClicked();
            if (preferences.getString(getString(R.string.note_list_view_type_key), "0").equals("0")) {
                item.setIcon(R.drawable.ic_outline_view_agenda_24);
            } else {
                item.setIcon(R.drawable.ic_baseline_grid_view_24);
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
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
        }
    }

    @Override
    public boolean onBackPressed() {
        if (actionModeCallback.actionMode != null) {
            actionModeCallback.finishActionMode();
            return true;
        }
        return false;
    }

    @Override
    public void onDestroy() {
        actionModeCallback.finishActionMode();
        super.onDestroy();
    }

    private void invalidateCab() {

        int nSelectedItems = pinnedNotesAdapter.numberOfSelectedItems()
                + unpinnedNotesAdapter.numberOfSelectedItems();
        if (nSelectedItems == 0) {
            actionModeCallback.finishActionMode();

            return;
        }

        if (actionModeCallback.actionMode == null) {
            binding.appBar.toolbar.startActionMode(actionModeCallback);
        }

        pinnedNotesAdapter.enableSelection();
        unpinnedNotesAdapter.enableSelection();

        actionModeCallback.actionMode.invalidate();

    }

    void setUpNavigationBar() {
        List<FloatingActionButton> fabs = new ArrayList<FloatingActionButton>();
        fabs.add(binding.fabAddNormalNote);
        fabs.add(binding.fabAddCheckListNote);
        fabs.add(binding.fabAddMarkdownNote);
        ViewAnimation viewAnimation = new ViewAnimation();
        for (FloatingActionButton i : fabs) {
            viewAnimation.init(i);
        }
        binding.fab.setOnClickListener(v -> {
            isFabRotate = ViewAnimation.rotateFab(binding.fab, !isFabRotate);
            if (isFabRotate) {
                for (FloatingActionButton i : fabs) {
                    viewAnimation.showIn(i);
                }
            } else {
                for (FloatingActionButton i : fabs) {
                    viewAnimation.showOut(i);
                }
            }
        });

        binding.fabAddCheckListNote.setOnClickListener(view -> {
            Note note = Note.emptyInstance();
            note.type = Note.NOTE_TYPE_CHECK_LIST;
            long newId = notesViewModel.insertNewNote(note);
            isFabRotate = false;
            notesViewModel.startNavigatingToNoteDetails(newId);
        });

        binding.fabAddNormalNote.setOnClickListener(view -> {
            Note note = Note.emptyInstance();
            note.type = Note.NOTE_TYPE_PLAIN_TEXT;
            long newId = notesViewModel.insertNewNote(note);
            isFabRotate = false;
            notesViewModel.startNavigatingToNoteDetails(newId);
        });

        binding.fabAddMarkdownNote.setOnClickListener(view -> {
            Note note = Note.emptyInstance();
            note.type = Note.NOTE_TYPE_MARKDOWN;
            long newId = notesViewModel.insertNewNote(note);
            isFabRotate = false;
            notesViewModel.startNavigatingToNoteDetails(newId);
        });
    }

    class NoteListActionMode implements ActionMode.Callback {
        public ActionMode actionMode;

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            actionMode = mode;

            requireActivity().getMenuInflater().inflate(R.menu.main_context, menu);

            requireActivity().getWindow().setStatusBarColor(ViewUtils.getColorAttr(
                    requireContext(), R.attr.colorPrimaryVariant
            ));

            ViewUtils.setLightStatusBarFlagFromColor(requireActivity().getWindow().getDecorView(), requireActivity());
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            int nSelectedUnpinnedNotes = unpinnedNotesAdapter.numberOfSelectedItems();
            int numberOfSelectedItems = pinnedNotesAdapter.numberOfSelectedItems() +
                    unpinnedNotesAdapter.numberOfSelectedItems();

            mode.setTitle(Integer.valueOf(numberOfSelectedItems).toString());

            MenuItem itemPin = menu.findItem(R.id.action_toggle_pin);
            if (itemPin != null) {
                @DrawableRes int pinRes;
                String pinTitle;

                if (nSelectedUnpinnedNotes == 0) {
                    pinRes = R.drawable.ic_outline_push_pin_24;
                    pinTitle = getString(R.string.unpin_selected_notes);
                } else {
                    pinRes = R.drawable.ic_baseline_push_pin_24;
                    pinTitle = getString(R.string.pin_selected_notes);
                }

                itemPin.setIcon(pinRes);
                itemPin.setTitle(pinTitle);
            }

            return true;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_archive: {
                    if (onActionArchive()) return true;

                    return true;
                }

                case R.id.action_select_all: {

                    pinnedNotesAdapter.selectAll();
                    unpinnedNotesAdapter.selectAll();

                    invalidateCab();

                    return true;
                }

                case R.id.action_toggle_pin: {
                    if (onActionTogglePin()) return true;

                    return true;
                }

                case R.id.action_remove_note: {
                    if (onActionRemoveNotes()) return true;
                    return true;
                }

                case R.id.action_change_colour: {
                    return onActionChangeColour();
                }
            }
            return false;
        }

        private boolean onActionChangeColour() {
            PropertiesBSFragment propertiesBSFragment = new PropertiesBSFragment();
            propertiesBSFragment.setPropertiesChangeListener(colorCode -> {
                int[] colors = getResources().getIntArray(R.array.note_color);

                int currentColorIdx = 0;
                for (int i = 0; i < colors.length; i++) {
                    if (colors[i] == colorCode) {
                        currentColorIdx = i;
                        break;
                    }
                }

                int nSelectedItems = pinnedNotesAdapter.numberOfSelectedItems()
                        + unpinnedNotesAdapter.numberOfSelectedItems();

                if (nSelectedItems == 0) {
                    return;
                }

                List<NoteWithTags> selectedItems = new ArrayList<>();
                selectedItems.addAll(pinnedNotesAdapter.getSelectedItems());
                selectedItems.addAll(unpinnedNotesAdapter.getSelectedItems());

                NoteWithTags[] noteToChangeColour = selectedItems.toArray(new NoteWithTags[]{});
                notesViewModel.changeColors(currentColorIdx, noteToChangeColour);
                actionModeCallback.finishActionMode();

            });
            showBottomSheetDialogFragment(propertiesBSFragment);
            return true;
        }

        private boolean onActionRemoveNotes() {
            List<NoteWithTags> selectedPinnedItems = pinnedNotesAdapter.getSelectedItems();
            List<NoteWithTags> selectedUnpinnedItems = unpinnedNotesAdapter.getSelectedItems();

            List<NoteWithTags> selectedItems = new ArrayList<>();
            selectedItems.addAll(pinnedNotesAdapter.getSelectedItems());
            selectedItems.addAll(unpinnedNotesAdapter.getSelectedItems());

            if (selectedPinnedItems.isEmpty() && selectedUnpinnedItems.isEmpty()) {
                return true;
            }

            NoteWithTags[] trashedNotes = selectedItems.toArray(new NoteWithTags[]{});
            notesViewModel.moveToTrash(trashedNotes);

            Snackbar.make(binding.getRoot(), R.string.restore_status, Snackbar.LENGTH_LONG)
                    .setAction(R.string.undo, v -> notesViewModel.restoreNoteFromTrash(trashedNotes))
                    .show();

            actionModeCallback.finishActionMode();
            return true;
        }

        private boolean onActionTogglePin() {
            List<NoteWithTags> selectedPinnedItems = pinnedNotesAdapter.getSelectedItems();
            List<NoteWithTags> selectedUnpinnedItems = unpinnedNotesAdapter.getSelectedItems();

            boolean actionPinAll = !selectedUnpinnedItems.isEmpty();
            if (selectedPinnedItems.isEmpty() && selectedUnpinnedItems.isEmpty()) {
                return true;
            }

            List<NoteWithTags> selectedItems = new ArrayList<>();
            selectedItems.addAll(pinnedNotesAdapter.getSelectedItems());
            selectedItems.addAll(unpinnedNotesAdapter.getSelectedItems());

            NoteWithTags[] notes = selectedItems.toArray(new NoteWithTags[]{});
            notesViewModel.togglePin(actionPinAll, notes);
            notesViewModel.requestReloadingData();

            actionModeCallback.finishActionMode();
            return true;
        }

        private boolean onActionArchive() {
            int nSelectedItems = pinnedNotesAdapter.numberOfSelectedItems()
                    + unpinnedNotesAdapter.numberOfSelectedItems();

            if (nSelectedItems == 0) {
                return true;
            }

            List<NoteWithTags> selectedItems = new ArrayList<>();
            selectedItems.addAll(pinnedNotesAdapter.getSelectedItems());
            selectedItems.addAll(unpinnedNotesAdapter.getSelectedItems());

            NoteWithTags[] archiveNotes = selectedItems.toArray(new NoteWithTags[]{});

            notesViewModel.moveToArchived(archiveNotes);
            actionModeCallback.finishActionMode();
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {

            requireActivity().getWindow().setStatusBarColor(
                    ViewUtils.getColorAttr(requireContext(), R.attr.colorSurface)
            );

            ViewUtils.setLightStatusBarFlagFromColor(requireActivity().getWindow().getDecorView(), requireActivity());

            requireActivity().getWindow().setStatusBarColor(
                    ViewUtils.getColorAttr(requireContext(), R.attr.colorSurface)
            );

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                requireActivity().getWindow().setNavigationBarColor(ViewUtils.getColorAttr(
                        requireContext(), R.attr.colorSurface
                ));
            }

            binding.outer.setBackgroundColor(ViewUtils.getColorAttr(
                    requireContext(), R.attr.colorSurface
            ));

            pinnedNotesAdapter.finishSelection();
            unpinnedNotesAdapter.finishSelection();

            if (requireActivity() instanceof ControllableDrawerActivity) {
                ((ControllableDrawerActivity) requireActivity()).unlockTheDrawer();
            }

            actionMode = null;
        }

        private void showBottomSheetDialogFragment(BottomSheetDialogFragment fragment) {
            if (fragment == null || fragment.isAdded()) {
                return;
            }
            fragment.show(getChildFragmentManager(), fragment.getTag());
        }

        void finishActionMode() {
            if (actionMode != null) {
                actionMode.finish();
            }
        }

    }
}

