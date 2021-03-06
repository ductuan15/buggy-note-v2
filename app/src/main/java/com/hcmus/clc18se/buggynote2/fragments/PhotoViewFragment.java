package com.hcmus.clc18se.buggynote2.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.hcmus.clc18se.buggynote2.BuggyNoteActivity;
import com.hcmus.clc18se.buggynote2.R;
import com.hcmus.clc18se.buggynote2.data.NoteWithTags;
import com.hcmus.clc18se.buggynote2.data.Photo;
import com.hcmus.clc18se.buggynote2.database.BuggyNoteDao;
import com.hcmus.clc18se.buggynote2.database.BuggyNoteDatabase;
import com.hcmus.clc18se.buggynote2.databinding.FragmentPhotoViewBinding;
import com.hcmus.clc18se.buggynote2.viewmodels.NoteDetailsViewModel;
import com.hcmus.clc18se.buggynote2.viewmodels.factories.NoteDetailsViewModelFactory;

import java.util.List;

public class PhotoViewFragment extends Fragment {

    private FragmentPhotoViewBinding binding;
    private NoteDetailsViewModel viewModel;

    private int currentPos = 1;

    private final ViewPager2.OnPageChangeCallback callback = new ViewPager2.OnPageChangeCallback() {
        @Override
        public void onPageSelected(int position) {
            super.onPageSelected(position);
            currentPos = position;
        }
    };

    private ScreenSlidePagerAdapter screenSlidePagerAdapter;

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

        screenSlidePagerAdapter = new ScreenSlidePagerAdapter(this);
        binding.viewPager.setAdapter(screenSlidePagerAdapter);
        if (viewModel.photoIndex >= 0) {
            currentPos = viewModel.photoIndex;
        }
        binding.viewPager.setCurrentItem(currentPos, false);

        binding.viewPager.registerOnPageChangeCallback(callback);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setUpNavigation();
        if (savedInstanceState != null && savedInstanceState.containsKey(BUNDLE_CURRENT_POS)) {
            currentPos = savedInstanceState.getInt(BUNDLE_CURRENT_POS);
            binding.viewPager.setCurrentItem(currentPos);

        }
    }

    private void initObservers() {
        viewModel.getPhotoRemovedState().observe(getViewLifecycleOwner(), state -> {
            if (state) {
                NoteWithTags note = viewModel.getNote().getValue();

                if (note != null) {

                    List<Photo> photos = note.photos;

                    if (photos != null) {
                        if (photos.isEmpty()) {
                            requireActivity().onBackPressed();
                        }
                        if (currentPos >= photos.size()) {
                            currentPos = photos.size() - 1;
                        }
                        screenSlidePagerAdapter.notifyItemRemoved(currentPos);
                    }
                } else {
                    requireActivity().onBackPressed();
                }

                viewModel.doneHandlingPhotoRemove();
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.photo, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_remove_photo) {
            onActionRemovePhoto();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void onActionRemovePhoto() {
        NoteWithTags note = viewModel.getNote().getValue();
        if (note != null) {
            List<Photo> photos = note.photos;
            Photo photo = photos.get(currentPos);

            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle(getString(R.string.remove_from_device))
                    .setMessage(getString(R.string.remove_confirmation))
                    .setNegativeButton(getResources().getString(R.string.cancel), (dialog, which) -> {
                    })
                    .setPositiveButton(getResources().getString(R.string.remove), (dialog, which) -> {
                        viewModel.removePhoto(photo);

                    })
                    .show();
        }
    }

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

        @Override
        public long getItemId(int position) {
            NoteWithTags note = viewModel.getNote().getValue();
            if (note != null) {
                return note.photos.get(position).id;
            }
            return super.getItemId(position);
        }

        @Override
        public boolean containsItem(long itemId) {
            NoteWithTags note = viewModel.getNote().getValue();
            if (note != null) {
                List<Photo> photos = note.photos;
                for (Photo photo : photos) {
                    if (photo.id == itemId) {
                        return true;
                    }
                }
            }
            return super.containsItem(itemId);
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

        setHasOptionsMenu(true);

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
