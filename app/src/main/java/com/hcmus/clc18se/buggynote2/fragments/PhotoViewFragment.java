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
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.appbar.MaterialToolbar;
import com.hcmus.clc18se.buggynote2.BuggyNoteActivity;
import com.hcmus.clc18se.buggynote2.R;
import com.hcmus.clc18se.buggynote2.data.NoteWithTags;
import com.hcmus.clc18se.buggynote2.data.Photo;
import com.hcmus.clc18se.buggynote2.database.BuggyNoteDao;
import com.hcmus.clc18se.buggynote2.database.BuggyNoteDatabase;
import com.hcmus.clc18se.buggynote2.databinding.FragmentPhotoViewBinding;
import com.hcmus.clc18se.buggynote2.utils.OnBackPressed;
import com.hcmus.clc18se.buggynote2.viewmodels.NoteDetailsViewModel;
import com.hcmus.clc18se.buggynote2.viewmodels.factories.NoteDetailsViewModelFactory;

import java.util.List;

public class PhotoViewFragment extends Fragment {

    private FragmentPhotoViewBinding binding;
    private NoteDetailsViewModel viewModel;

    private int currentPos = 0;

    private final ViewPager2.OnPageChangeCallback callback = new ViewPager2.OnPageChangeCallback() {
        @Override
        public void onPageSelected(int position) {
            super.onPageSelected(position);
            currentPos = position;
        }
    };

    public PhotoViewFragment() {
        super();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentPhotoViewBinding.inflate(inflater, container, false);

        BuggyNoteDao db = BuggyNoteDatabase.getInstance(requireContext()).buggyNoteDatabaseDao();
        PhotoViewFragmentArgs args = PhotoViewFragmentArgs.fromBundle(requireArguments());

        NavBackStackEntry backStackEntry = Navigation.findNavController(
                requireActivity().findViewById(R.id.nav_host_fragment)
        ).getBackStackEntry(R.id.nav_note_details);

        viewModel = new ViewModelProvider(
                backStackEntry,
                new NoteDetailsViewModelFactory(
                        requireActivity().getApplication(),
                        args.getNoteId(),
                        db
                )
        ).get(NoteDetailsViewModel.class);

        binding.setLifecycleOwner(this);
        binding.setNoteDetailsViewModel(viewModel);

        initObservers();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setUpNavigation();

        binding.viewPager.registerOnPageChangeCallback(callback);
        binding.viewPager.setAdapter(new ScreenSlidePagerAdapter(this));

        if (savedInstanceState != null && savedInstanceState.containsKey(BUNDLE_CURRENT_POS)) {
            currentPos = savedInstanceState.getInt(BUNDLE_CURRENT_POS);
        }
        binding.viewPager.setCurrentItem(currentPos);

    }

    private void initObservers() { }

    private class ScreenSlidePagerAdapter extends FragmentStateAdapter {

        public ScreenSlidePagerAdapter(@NonNull Fragment fragment) {
            super(fragment);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            NoteWithTags note = viewModel.getNote().getValue();
            PhotoViewPageFragment fragment = new PhotoViewPageFragment();

            if (note != null) {
                List<Photo> photos = note.photos;
                fragment.photo = photos.get(position);
            }

            return fragment;
        }

        @Override
        public int getItemCount() {
            NoteWithTags note = viewModel.getNote().getValue();
            if (note != null) {
                return note.photos.size();
            }
            return 0;
        }
    }

    private static final String BUNDLE_CURRENT_POS = "CURRENT_POS";

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(BUNDLE_CURRENT_POS, currentPos);
    }

    @Override
    public void onDestroy() {
        binding.viewPager.unregisterOnPageChangeCallback(callback);
        super.onDestroy();
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
