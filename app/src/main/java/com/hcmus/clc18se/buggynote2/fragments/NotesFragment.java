package com.hcmus.clc18se.buggynote2.fragments;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

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
import com.hcmus.clc18se.buggynote2.BuggyNoteActivity;
import com.hcmus.clc18se.buggynote2.R;
import com.hcmus.clc18se.buggynote2.adapters.NoteAdapter;
import com.hcmus.clc18se.buggynote2.adapters.TagFilterAdapter;
import com.hcmus.clc18se.buggynote2.adapters.TagsAdapter;
import com.hcmus.clc18se.buggynote2.adapters.callbacks.NoteAdapterCallbacks;
import com.hcmus.clc18se.buggynote2.adapters.callbacks.TagFilterAdapterCallbacks;
import com.hcmus.clc18se.buggynote2.adapters.callbacks.TagsAdapterCallbacks;
import com.hcmus.clc18se.buggynote2.data.Note;
import com.hcmus.clc18se.buggynote2.data.NoteWithTags;
import com.hcmus.clc18se.buggynote2.data.Tag;
import com.hcmus.clc18se.buggynote2.database.BuggyNoteDao;
import com.hcmus.clc18se.buggynote2.database.BuggyNoteDatabase;
import com.hcmus.clc18se.buggynote2.databinding.FragmentNotesBinding;
import com.hcmus.clc18se.buggynote2.databinding.ItemTagBinding;
import com.hcmus.clc18se.buggynote2.utils.ContextUtils;
import com.hcmus.clc18se.buggynote2.utils.OnBackPressed;
import com.hcmus.clc18se.buggynote2.utils.SpaceItemDecoration;
import com.hcmus.clc18se.buggynote2.utils.ViewUtils;
import com.hcmus.clc18se.buggynote2.viewmodels.NotesViewModel;
import com.hcmus.clc18se.buggynote2.viewmodels.TagsViewModel;
import com.hcmus.clc18se.buggynote2.viewmodels.callbacks.NotesViewModelCallBacks;
import com.hcmus.clc18se.buggynote2.viewmodels.factories.NotesViewModelFactory;
import com.hcmus.clc18se.buggynote2.viewmodels.factories.TagsViewModelFactory;

import java.util.List;
import java.util.concurrent.ExecutionException;

import timber.log.Timber;

public class NotesFragment extends Fragment implements OnBackPressed {

    private SharedPreferences preferences = null;
    private FragmentNotesBinding binding = null;

    // TODO: init đào
    //      view models
    //      adapters
    //      adapters' callbacks
    //      ItemTouchHelper.

    // TODO: main cab.

    private NotesViewModel notesViewModel;

    private final NoteAdapterCallbacks noteAdapterCallbacks = new NoteAdapterCallbacks() {
        @Override
        public void onClick(NoteWithTags note) {
            notesViewModel.startNavigatingToNoteDetails(note.note.id);
        }

        @Override
        public boolean onMultipleSelect(NoteWithTags note) {
            return false;
        }

        @Override
        public void onPostReordered(List<NoteWithTags> notes) {
            // TODO: implement me
        }

        @Override
        public void onItemSwiped(NoteWithTags note) {
            // TODO: implement me

        }
    };

    private final NoteAdapter noteAdapter = new NoteAdapter(noteAdapterCallbacks);

    private TagsViewModel tagsViewModel;

    private final TagFilterAdapterCallbacks tagFilterAdapterCallbacks = (isChecked, tag) -> {
        // TODO: complete this
        if (tag.isSelectedState() != isChecked) {
            noteAdapter.finishSelection();
            //
            tag.setSelectedState(isChecked);
            notesViewModel.filterByTags(tagsViewModel.getTags().getValue());
        }
    };

    private final TagFilterAdapter tagFilterAdapter = new TagFilterAdapter(tagFilterAdapterCallbacks);

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BuggyNoteDao database = BuggyNoteDatabase.getInstance(requireContext()).buggyNoteDatabaseDao();
        preferences = PreferenceManager.getDefaultSharedPreferences(requireContext());

        notesViewModel = new ViewModelProvider(
                requireActivity(),
                new NotesViewModelFactory(
                        database,
                        requireActivity().getApplication(),
                        noteViewModelCallbacks))
                .get(NotesViewModel.class);

        tagsViewModel = new ViewModelProvider(
                requireActivity(),
                new TagsViewModelFactory(database)
        ).get(TagsViewModel.class);
    }

    private final NotesViewModelCallBacks noteViewModelCallbacks = new NotesViewModelCallBacks() {
        @Override
        public void onNoteItemInserted(long newId) {
            notesViewModel.startNavigatingToNoteDetails(newId);
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentNotesBinding.inflate(inflater, container, false);

        setHasOptionsMenu(true);

        binding.setLifecycleOwner(this);

        binding.fab.setOnClickListener(view -> {
            notesViewModel.insertNewNote(Note.emptyInstance());
        });

        // TODO: bind view models.
        binding.setNoteViewModel(notesViewModel);
        binding.setTagViewModel(tagsViewModel);

        initRecyclerViews();
        initObservers();

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
        initNoteAdapter(binding.noteList, noteAdapter, null, true);

        binding.tagFilterList.setAdapter(tagFilterAdapter);
        binding.tagFilterList.addItemDecoration(
                new SpaceItemDecoration((int) getResources().getDimension(R.dimen.item_note_margin))
        );
    }

    // TODO: set adapter to the recycler views
    //       set layout manager
    //       attach ItemTouchHelper
    //       add ItemDecoration.

    void initObservers() {

        notesViewModel.getNoteList().observe(getViewLifecycleOwner(), noteWithTags -> {
            if (noteWithTags != null) {
                noteAdapter.notifyDataSetChanged();
                Timber.d("loaded");
            }
        });

        // noteViewModel.headerLabelVisibility

        tagsViewModel.getTags().observe(getViewLifecycleOwner(), tags -> {
            tagFilterAdapter.notifyDataSetChanged();
        });

        notesViewModel.getReloadDataRequest().observe(getViewLifecycleOwner(), state -> {
            if (state != null && state) {

                if (tagsViewModel.getTags().getValue() != null) {
                    notesViewModel.filterByTags(tagsViewModel.getTags().getValue());
                }
                else {
                    notesViewModel.loadNotes();
                }

                notesViewModel.doneRequestingLoadData();

                binding.noteList.invalidate();
                binding.noteList.requestLayout();

            }
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
    public void onPause() {
        super.onPause();

        // TODO: save the note to the database when changes occurred.
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
        inflater.inflate(R.menu.main, menu);

        // TODO: handle searching

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
        initNoteAdapter(binding.noteList, noteAdapter, null, false);

        // 
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

    // TODO: Handle cabs

    @Override
    public boolean onBackPressed() {
        // TODO: handle backpress
        return false;
    }
}
