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
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.appbar.MaterialToolbar;
import com.hcmus.clc18se.buggynote2.BuggyNoteActivity;
import com.hcmus.clc18se.buggynote2.R;
import com.hcmus.clc18se.buggynote2.databinding.FragmentNotesBinding;
import com.hcmus.clc18se.buggynote2.utils.OnBackPressed;

public class NotesFragment extends Fragment implements OnBackPressed {

    private SharedPreferences preferences = null;
    private FragmentNotesBinding binding = null;

    // TODO: init đào
    //      view models
    //      adapters
    //      adapters' callbacks
    //      ItemTouchHelper.

    // TODO: main cab.

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentNotesBinding.inflate(inflater, container, false);

        setHasOptionsMenu(true);

        binding.setLifecycleOwner(this);

        binding.fab.setOnClickListener(view -> {
            // TODO: replace with live data observer
            Navigation.findNavController(binding.getRoot()).navigate(
                    NotesFragmentDirections.actionNavNotesToNoteDetailsFragment(0)
            );
        });
        // TODO: bind view models.

        return binding.getRoot();
    }

    // TODO: set adapter to the recycler views
    //       set layout manager
    //       attach ItemTouchHelper
    //       add ItemDecoration.

    // TODO: set observers from view models.


    @Override
    public void onPause() {
        super.onPause();

        // TODO: save the note to the database when changes occured.
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
