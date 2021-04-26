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
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.hcmus.clc18se.buggynote2.BuggyNoteActivity;
import com.hcmus.clc18se.buggynote2.R;
import com.hcmus.clc18se.buggynote2.adapters.NoteAdapter;
import com.hcmus.clc18se.buggynote2.adapters.callbacks.NoteAdapterCallbacks;
import com.hcmus.clc18se.buggynote2.data.Note;
import com.hcmus.clc18se.buggynote2.data.NoteWithTags;
import com.hcmus.clc18se.buggynote2.database.BuggyNoteDao;
import com.hcmus.clc18se.buggynote2.database.BuggyNoteDatabase;
import com.hcmus.clc18se.buggynote2.databinding.FragmentNotesBinding;
import com.hcmus.clc18se.buggynote2.utils.OnBackPressed;
import com.hcmus.clc18se.buggynote2.viewmodels.NotesViewModel;
import com.hcmus.clc18se.buggynote2.viewmodels.callbacks.NotesViewModelCallBacks;
import com.hcmus.clc18se.buggynote2.viewmodels.factories.NotesViewModelFactory;

import java.util.List;
import java.util.concurrent.ExecutionException;

import timber.log.Timber;

public class NotesFragment extends Fragment implements OnBackPressed {

    private SharedPreferences preferences = null;
    private FragmentNotesBinding binding = null;

    private BuggyNoteDao database;

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

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        database = BuggyNoteDatabase.getInstance(requireContext()).buggyNoteDatabaseDao();

        Timber.d("ping");
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
        notesViewModel = new ViewModelProvider(
                requireActivity(),
                new NotesViewModelFactory(
                        database,
                        requireActivity().getApplication(),
                        noteViewModelCallbacks))
                .get(NotesViewModel.class);

        binding.fab.setOnClickListener(view -> {
            notesViewModel.insertNewNote(Note.emptyInstance());
        });
        // TODO: bind view models.
        binding.setNoteViewModel(notesViewModel);
        initRecyclerViews();
        initObservers();

        return binding.getRoot();
    }

    void initNoteAdapter(RecyclerView recyclerView,
                         RecyclerView.Adapter adapter,
                         ItemTouchHelper touchHelper,
                         boolean addItemDecoration) {
        // TODO: set layout manager
        recyclerView.setAdapter(adapter);
        if (addItemDecoration) {
            // TODO: add item decoration
            // recyclerView.addItemDecoration();
        }
        if (touchHelper != null) {
            touchHelper.attachToRecyclerView(recyclerView);
        }
    }

    void initRecyclerViews() {
        initNoteAdapter(binding.noteList, noteAdapter, null, false);
    }

    // TODO: set adapter to the recycler views
    //       set layout manager
    //       attach ItemTouchHelper
    //       add ItemDecoration.

    // TODO: set observers from view models.
    void initObservers() {

        notesViewModel.getNoteList().observe(getViewLifecycleOwner(), noteWithTags -> {
            if (noteWithTags != null) {
                noteAdapter.notifyDataSetChanged();
                Timber.d("loaded");
            }
        });

        // noteViewModel.headerLabelVisibility
        // tagViewModel.tags

        notesViewModel.getReloadDataRequest().observe(getViewLifecycleOwner(), state -> {
            if (state != null && state) {
                // TODO: filter by tags
                notesViewModel.loadNotes();
                notesViewModel.doneRequestingLoadData();
                binding.noteList.invalidate();
                binding.noteList.requestLayout();

            }
        });

        notesViewModel.getNavigateToNoteDetails().observe(getViewLifecycleOwner(), id -> {
            if (id != null) {
                Navigation.findNavController(binding.getRoot()).navigate(
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
        // TODO: re-setup the layout manager of the note list.
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        super.onPrepareOptionsMenu(menu);

        // TODO: init the state of pinned icon in the option menu
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.main, menu);

        // TODO: handle searching

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // TODO: handle buttons
        return super.onOptionsItemSelected(item);
    }

    // TODO: refresh the recycler view


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setUpNavigation();
        // initObservers();
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
