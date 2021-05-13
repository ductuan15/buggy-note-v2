package com.hcmus.clc18se.buggynote2.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavBackStackEntry;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.tabs.TabLayoutMediator;
import com.hcmus.clc18se.buggynote2.BuggyNoteActivity;
import com.hcmus.clc18se.buggynote2.R;
import com.hcmus.clc18se.buggynote2.adapters.BindingAdapters;
import com.hcmus.clc18se.buggynote2.adapters.CheckListAdapter;
import com.hcmus.clc18se.buggynote2.adapters.MarkdownPagerAdapter;
import com.hcmus.clc18se.buggynote2.adapters.PhotoListAdapter;
import com.hcmus.clc18se.buggynote2.adapters.callbacks.CheckListAdapterCallbacks;
import com.hcmus.clc18se.buggynote2.adapters.callbacks.PhotoListAdapterCallback;
import com.hcmus.clc18se.buggynote2.data.CheckListItem;
import com.hcmus.clc18se.buggynote2.data.NoteWithTags;
import com.hcmus.clc18se.buggynote2.data.Photo;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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

    private final PhotoListAdapterCallback photoListAdapterCallback = photo -> {
        viewModel.navigateToPhotoView();
    };

    private final PhotoListAdapter photoListAdapter = new PhotoListAdapter(photoListAdapterCallback);

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
                        requireActivity().getApplication(),
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
            checkAListItem();
        });

        initRecyclerViews();
        initObservers();

        binding.executePendingBindings();
        return binding.getRoot();
    }

    private void checkAListItem() {
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
    }

    private void initObservers() {
        viewModel.getNote().observe(getViewLifecycleOwner(), noteWithTags -> {
            if (noteWithTags != null) {
                updateMenu();
                setUpViewPagerWhenNoteIsMarkDown(noteWithTags);
            }
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

        viewModel.getNavigateToPhotoView().observe(getViewLifecycleOwner(), id -> {
            if (id != null) {
                Navigation.findNavController(requireActivity().findViewById(R.id.nav_host_fragment)).navigate(
                        NoteDetailsFragmentDirections.actionNavNoteDetailsToPhotoViewFragment(
                                arguments.getNoteId()
                        )
                );
                viewModel.doneNavigatingToPhotoView();
            }
        });

        viewModel.getDeleteRequest().observe(getViewLifecycleOwner(), state -> {
            if (state != null && state) {
                notesViewModel.requestReloadingData();
                requireActivity().onBackPressed();
                viewModel.doneRequestingReloadData();
            }
        });

        viewModel.getCheckListItems().observe(getViewLifecycleOwner(), list -> {
            if (list != null) {
                checkListAdapter.submitList(list);
            }
        });
    }

    private final ViewPager2.OnPageChangeCallback callback = new ViewPager2.OnPageChangeCallback() {
        Integer previousPos = null;

        @Override
        public void onPageSelected(int position) {
            super.onPageSelected(position);
            if (previousPos != null && previousPos == MarkdownPagerAdapter.PAGE_EDITOR) {
                saveNote(false);
            }
            previousPos = position;
        }
    };

    private void setUpViewPagerWhenNoteIsMarkDown(@NonNull NoteWithTags noteWithTags) {

        if (noteWithTags.note.isMarkdown()) {
            binding.markdownViewPager.unregisterOnPageChangeCallback(callback);

            binding.markdownViewPager.setAdapter(new MarkdownPagerAdapter(this, noteWithTags));
            new TabLayoutMediator(binding.tabs, binding.markdownViewPager, (tab, page) -> {
                tab.setText(getTabText(page));
            }).attach();

            binding.markdownViewPager.registerOnPageChangeCallback(callback);
        }
    }

    private String getTabText(int position) {
        switch (position) {
            case MarkdownPagerAdapter.PAGE_PREVIEW:
                return getString(R.string.preview);
            case MarkdownPagerAdapter.PAGE_EDITOR:
                return getString(R.string.editor);
        }
        return "";
    }

    private void initRecyclerViews() {
        //RecyclerView checkListRecyclerView = binding.scrollLayout.findViewById()
        binding.checkListRecyclerView.setAdapter(checkListAdapter);
        binding.photoList.setAdapter(photoListAdapter);
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

        if (noteWithTags != null && noteWithTags.note.isMarkdown()) {
            MarkdownPagerAdapter adapter = (MarkdownPagerAdapter) binding.markdownViewPager.getAdapter();
            try {
                content = adapter.getEditTextContent().toString();
            } catch (NullPointerException ignore) {
            }
        }

        checkListAdapter.saveFocusedView();

        if (noteWithTags != null) {

            String finalContent = content;
            BuggyNoteDatabase.databaseWriteExecutor.execute(() -> {

                String encodedCheckListContent = "";
                if (noteWithTags.note.isCheckList()) {
                    encodedCheckListContent = CheckListItem.toNoteContent(checkListAdapter.getCurrentList());
                }

                if (!title.equals(noteWithTags.note.title) ||
                        (!noteWithTags.note.isCheckList() && !finalContent.equals(noteWithTags.note.noteContent)) ||
                        (noteWithTags.note.isCheckList() && !noteWithTags.note.noteContent.equals(encodedCheckListContent)) ||
                        require) {
                    noteWithTags.note.title = title;

                    if (!noteWithTags.note.isCheckList()) {
                        noteWithTags.note.noteContent = finalContent;
                    } else {
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
                        .setMessage(getString(R.string.remove_confirmation))
                        .setNegativeButton(getResources().getString(R.string.cancel), (dialog, which) -> {
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
                onActionShare();
                return true;
            }

        }
        return false;
    };

    private void onActionNotification() {
        final boolean[] onDialogDatePicker = {false};
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle(R.string.add_new_notification);
        LayoutInflater li = LayoutInflater.from(requireContext());
        View promptsView = li.inflate(R.layout.dialog_add_notification, null);
        Spinner dateSpinner = promptsView.findViewById(R.id.spinner_choose_day);
        List<String> listDateChoose = new ArrayList<>();
        SimpleDateFormat formatterDate = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat formatterTime = new SimpleDateFormat("hh:mm");
        Date date = new Date(System.currentTimeMillis());
        listDateChoose.add(formatterDate.format(date));
        ArrayAdapter<String> arrayAdapterChooseDate = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, listDateChoose);
        arrayAdapterChooseDate.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dateSpinner.setAdapter(arrayAdapterChooseDate);

        dateSpinner.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (onDialogDatePicker[0])
                    return false;

                onDialogDatePicker[0] = true;

                AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                LayoutInflater li = LayoutInflater.from(requireContext());
                View promptsView = li.inflate(R.layout.dialog_date_picker, null, false);

                DatePicker datePicker = promptsView.findViewById(R.id.date_picker);
                datePicker.setMinDate(System.currentTimeMillis());
                builder.setCancelable(false);

                builder.setView(promptsView);

                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        date.setDate(datePicker.getDayOfMonth());
                        date.setMonth(datePicker.getMonth());
                        date.setYear(datePicker.getYear() - 1900);
                        arrayAdapterChooseDate.clear();
                        arrayAdapterChooseDate.add(formatterDate.format(date));
                        dateSpinner.setAdapter(arrayAdapterChooseDate);
                        onDialogDatePicker[0] = false;
                        dialog.cancel();
                    }
                });
                builder.create().show();
                return true;
            }
        });

        Spinner timeSpinner = promptsView.findViewById(R.id.spinner_choose_time);
        List<String> listTimeChoose = new ArrayList<>();
        listTimeChoose.add(formatterTime.format(date));
        ArrayAdapter<String> arrayAdapterChooseTime = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, listTimeChoose);
        arrayAdapterChooseTime.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        timeSpinner.setAdapter(arrayAdapterChooseTime);

        timeSpinner.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (onDialogDatePicker[0])
                    return false;
                onDialogDatePicker[0] = true;
                AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                LayoutInflater li = LayoutInflater.from(requireContext());
                View promptsView = li.inflate(R.layout.dialog_time_picker, null);
                TimePicker timePicker = promptsView.findViewById(R.id.time_picker);

                builder.setCancelable(false);
                builder.setView(promptsView);
                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        date.setHours(timePicker.getCurrentHour());
                        date.setMinutes(timePicker.getCurrentMinute());
                        arrayAdapterChooseTime.clear();
                        arrayAdapterChooseTime.add(formatterTime.format(date));
                        timeSpinner.setAdapter(arrayAdapterChooseTime);
                        onDialogDatePicker[0] = false;
                        dialog.cancel();
                    }
                });
                builder.create().show();
                return true;
            }
        });

        Spinner typeSpinner = promptsView.findViewById(R.id.spinner_choose_relay_mode);
        List<String> listModeChoose = new ArrayList<>();
        listModeChoose.add("Day");
        listModeChoose.add("Week");
        listModeChoose.add("Month");
        listModeChoose.add("Year");
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, listModeChoose);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        typeSpinner.setAdapter(arrayAdapter);
        builder.setView(promptsView);
        builder.setPositiveButton(getString(R.string.save), null);
        builder.setNegativeButton(getString(R.string.cancel), null);
        AlertDialog addNotification = builder.create();
        addNotification.show();
    }

    private void onActionShare() {
        String[] items = {"Text", "Image"};
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Choose an item");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0: {
                        String title = ((EditText) binding.layout.findViewById(R.id.text_view_title)).getText().toString();
                        String content = ((EditText) binding.layout.findViewById(R.id.note_content)).getText().toString();
                        String contentShare = title + "\n" + content;
                        Intent sendIntent = new Intent();
                        sendIntent.setAction(Intent.ACTION_SEND);
                        sendIntent.putExtra(Intent.EXTRA_TITLE, "Share note");
                        sendIntent.putExtra(Intent.EXTRA_TEXT, contentShare);
                        sendIntent.setType("*/*");
                        startActivity(Intent.createChooser(sendIntent, "Share to"));
                        break;
                    }
                    case 1: {
                        NoteWithTags noteWithTags = viewModel.getNote().getValue();
                        if(noteWithTags != null) {
                            List<Photo> photos = noteWithTags.photos;
                            ArrayList<Uri> files = new ArrayList<Uri>();
                            for(Photo image: photos) {
                                files.add(Uri.parse(image.getUri().replace("file://", "content://")));
                            }
                            Intent shareMultiImageIntent = new Intent();
                            shareMultiImageIntent.setAction(Intent.ACTION_SEND_MULTIPLE);
                            shareMultiImageIntent.putExtra(Intent.EXTRA_TITLE, "Here are some files.");
                            shareMultiImageIntent.setType("*/*");
                            shareMultiImageIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, files);
                            startActivity(Intent.createChooser(shareMultiImageIntent, "Share to:"));
                        }
                        break;
                    }
                }
            }

            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TITLE, getString(R.string.share_note));
            sendIntent.putExtra(Intent.EXTRA_TEXT, contentShare);
            sendIntent.setType(mimeType);
            startActivity(Intent.createChooser(sendIntent, getString(R.string.share_to)));
        }
    }

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
                if (onActionPin(item)) return true;
            }
            case R.id.action_share: {
                onActionShare();
                return true;
            }
            case R.id.action_add_notification: {
                onActionNotification();
                return true;
            }
            case R.id.action_add_photo: {
                onActionAddPhoto();
                return true;
            }
        }
        return false;
    }

    public static final int PICK_IMAGE_REQUEST_CODE = 0x6969;

    private void onActionAddPhoto() {
        Intent intent = new Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        );
        startActivityForResult(intent, PICK_IMAGE_REQUEST_CODE);
    }

    private boolean onActionPin(@NonNull MenuItem item) {
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            Uri uri = data.getData();
            viewModel.addPhoto(uri);
        }
    }
}
