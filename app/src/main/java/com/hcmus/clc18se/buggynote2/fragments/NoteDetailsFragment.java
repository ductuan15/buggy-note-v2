package com.hcmus.clc18se.buggynote2.fragments;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavBackStackEntry;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.tabs.TabLayoutMediator;
import com.hcmus.clc18se.buggynote2.BuggyNoteActivity;
import com.hcmus.clc18se.buggynote2.R;
import com.hcmus.clc18se.buggynote2.adapters.AudioListAdapter;
import com.hcmus.clc18se.buggynote2.adapters.BindingAdapters;
import com.hcmus.clc18se.buggynote2.adapters.CheckListAdapter;
import com.hcmus.clc18se.buggynote2.adapters.MarkdownPagerAdapter;
import com.hcmus.clc18se.buggynote2.adapters.PhotoListAdapter;
import com.hcmus.clc18se.buggynote2.adapters.callbacks.AudioListAdapterCallback;
import com.hcmus.clc18se.buggynote2.adapters.callbacks.CheckListAdapterCallbacks;
import com.hcmus.clc18se.buggynote2.adapters.callbacks.CheckListTouchHelperCallback;
import com.hcmus.clc18se.buggynote2.adapters.callbacks.PhotoListAdapterCallback;
import com.hcmus.clc18se.buggynote2.data.Audio;
import com.hcmus.clc18se.buggynote2.data.CheckListItem;
import com.hcmus.clc18se.buggynote2.data.NoteWithTags;
import com.hcmus.clc18se.buggynote2.data.Photo;
import com.hcmus.clc18se.buggynote2.database.BuggyNoteDao;
import com.hcmus.clc18se.buggynote2.database.BuggyNoteDatabase;
import com.hcmus.clc18se.buggynote2.databinding.FragmentNoteDetailsBinding;
import com.hcmus.clc18se.buggynote2.databinding.ItemCheckListBinding;
import com.hcmus.clc18se.buggynote2.utils.FileUtils;
import com.hcmus.clc18se.buggynote2.utils.ReminderReceiver;
import com.hcmus.clc18se.buggynote2.utils.TextFormatter;
import com.hcmus.clc18se.buggynote2.utils.Utils;
import com.hcmus.clc18se.buggynote2.utils.interfaces.OnBackPressed;
import com.hcmus.clc18se.buggynote2.utils.views.PropertiesBSFragment;
import com.hcmus.clc18se.buggynote2.utils.views.ViewUtils;
import com.hcmus.clc18se.buggynote2.viewmodels.NoteDetailsViewModel;
import com.hcmus.clc18se.buggynote2.viewmodels.NotesViewModel;
import com.hcmus.clc18se.buggynote2.viewmodels.factories.NoteDetailsViewModelFactory;
import com.hcmus.clc18se.buggynote2.viewmodels.factories.NotesViewModelFactory;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import timber.log.Timber;

import static android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION;
import static android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION;

public class NoteDetailsFragment extends Fragment
        implements PropertiesBSFragment.Properties, OnBackPressed {

    private FragmentNoteDetailsBinding binding = null;
    private NoteDetailsFragmentArgs arguments;

    private NotesViewModel notesViewModel;
    private NoteDetailsViewModel viewModel;

    private BuggyNoteDao db;

    private final View.OnClickListener tagOnClickListener = v -> viewModel.navigateToTagSelection();

    private Menu menu;

    private PropertiesBSFragment propertiesBSFragment;
    Integer currentColorIdx = null;

    private Date dateReminder;

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

            if (binding.listContent.getText() == null) {
                return;
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

    private final PhotoListAdapterCallback photoListAdapterCallback = idx -> {
        viewModel.photoIndex = idx;
        viewModel.navigateToPhotoView();
    };

    private final PhotoListAdapter photoListAdapter = new PhotoListAdapter(photoListAdapterCallback);

    private final AudioListAdapterCallback audioListAdapterCallback = idx -> {
        viewModel.photoIndex = idx;
        viewModel.navigateToAudioView();
    };

    private final AudioListAdapter audioListAdapter = new AudioListAdapter(audioListAdapterCallback);

    private final ViewPager2.OnPageChangeCallback callback = new ViewPager2.OnPageChangeCallback() {
        Integer previousPos = null;

        @Override
        public void onPageSelected(int position) {
            super.onPageSelected(position);
            if (previousPos != null && previousPos == MarkdownPagerAdapter.PAGE_EDITOR) {
                saveNote(false, false);
            }
            previousPos = position;

            // Update the height when changing page

            // Because the fragment might or might not be created yet,
            // we need to check for the size of the fragmentManager
            // before accessing it.
            if (getChildFragmentManager().getFragments().size() > position) {
                updatePagerHeightForChild(position);
            }
        }

        private void updatePagerHeightForChild(int position) {
            Fragment fragment = getChildFragmentManager().getFragments().get(position);

            if (fragment != null && fragment.getView() != null) {

                View view = fragment.getView();
                view.post(() -> {

                    int wMeasureSpec = View.MeasureSpec.makeMeasureSpec(view.getWidth(),
                            View.MeasureSpec.EXACTLY
                    );
                    int hMeasureSpec = View.MeasureSpec.makeMeasureSpec(0,
                            View.MeasureSpec.UNSPECIFIED
                    );

                    view.measure(wMeasureSpec, hMeasureSpec);
                    ViewPager2 pager = binding.markdownViewPager;
                    if (pager.getHeight() != view.getMeasuredHeight()) {
                        ViewGroup.LayoutParams lp = pager.getLayoutParams();
                        lp.height = view.getMeasuredHeight();
                        pager.setLayoutParams(lp);
                    }
                });

            }
        }
    };


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
        if (binding.addCheckListContent.getText() == null) {
            return;
        }

        String content = binding.addCheckListContent.getText().toString().trim();
        binding.addCheckListTextLayout.getEditText().getText().clear();

        if (content.isEmpty()) {
            return;
        }

        binding.addCheckListTextLayout.setErrorEnabled(false);

        List<CheckListItem> currentList = checkListAdapter.getCurrentList();

        List<CheckListItem> items = new ArrayList<>(currentList);

        items.add(0, new CheckListItem(items.size(), false, content));
        checkListAdapter.submitList(items);
    }

    private void initObservers() {
        viewModel.getNote().observe(getViewLifecycleOwner(), noteWithTags -> {
            if (noteWithTags != null) {
                updateMenu();
                setUpViewPagerWhenNoteIsMarkDown(noteWithTags);

                Integer color = noteWithTags.note.getColor(requireContext());
                if (color != null) {
                    requireActivity().getWindow().setStatusBarColor(color);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                        requireActivity().getWindow().setNavigationBarColor(color);
                    }
                }

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

        viewModel.getNavigateToAudioView().observe(getViewLifecycleOwner(), id -> {
            if (id != null) {
                Navigation.findNavController(requireActivity().findViewById(R.id.nav_host_fragment)).navigate(
                        NoteDetailsFragmentDirections.actionNavNoteDetailsToAudioViewFragment(
                                arguments.getNoteId()
                        )
                );
                viewModel.doneNavigatingToAudioView();
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

    private void setUpViewPagerWhenNoteIsMarkDown(@NonNull NoteWithTags noteWithTags) {

        if (noteWithTags.note.isMarkdown()) {
            ViewPager2 pager = binding.markdownViewPager;
            pager.unregisterOnPageChangeCallback(callback);
            pager.setOffscreenPageLimit(2);

            pager.setAdapter(new MarkdownPagerAdapter(this, noteWithTags));
            new TabLayoutMediator(binding.tabs, binding.markdownViewPager, (tab, page) -> {
                tab.setText(getTabText(page));
            }).attach();

            pager.registerOnPageChangeCallback(callback);
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

        ItemTouchHelper touchHelper = new ItemTouchHelper(new CheckListTouchHelperCallback());
        touchHelper.attachToRecyclerView(binding.checkListRecyclerView);

        binding.photoList.setAdapter(photoListAdapter);
        binding.audioList.setAdapter(audioListAdapter);
    }

    @Override
    public void onPause() {
        super.onPause();
        InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(binding.getRoot().getWindowToken(), 0);
        }

        saveNote(false, false);
    }

    @Override
    public boolean onBackPressed() {

        return false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        requireActivity().getWindow().setStatusBarColor(
                ViewUtils.getColorAttr(requireContext(), R.attr.colorSurface)
        );

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            requireActivity().getWindow().setNavigationBarColor(
                    ViewUtils.getColorAttr(requireContext(), R.attr.colorSurface)
            );

        }
    }

    void saveNote(boolean require, boolean forceReloadingData) {

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

                    if (currentColorIdx != null) {
                        noteWithTags.note.colorIdx = currentColorIdx;
                    }

                    // Timber.d("Set new note content");
                    db.updateNote(noteWithTags.note);
                    if (forceReloadingData) {
                        notesViewModel.requestReloadingData();
                    }
                    else {
                        notesViewModel.requestReloadingItem(noteWithTags.note.id);
                    }
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
        int itemId = item.getItemId();
        switch (itemId) {
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
        SimpleDateFormat formatterDateTime = new SimpleDateFormat("yyyy-MMMM-dd HH:mm");
        SimpleDateFormat formatterDate = new SimpleDateFormat("yyyy-MMMM-dd");
        SimpleDateFormat formatterTime = new SimpleDateFormat("HH:mm");
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
                timePicker.setIs24HourView(true);

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
        listModeChoose.add("No repeat");
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, listModeChoose);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        typeSpinner.setAdapter(arrayAdapter);
        builder.setView(promptsView);
        builder.setPositiveButton(getString(R.string.save), (dialog, which) -> {
            int repeatType = typeSpinner.getSelectedItemPosition();
            NoteWithTags noteWithTags = viewModel.getNote().getValue();
            SharedPreferences sharedPreferences = requireContext().getSharedPreferences("REMINDER", Context.MODE_PRIVATE);
            long oldDateString = 0;
            if (noteWithTags != null) {
                oldDateString = sharedPreferences.getLong(String.valueOf(noteWithTags.note.id), -1);
                Date oldDate = null;
                if (oldDateString != -1) {
                    oldDate = new Date(oldDateString);
                    if (oldDate.getTime() > System.currentTimeMillis()) {
                        new MaterialAlertDialogBuilder(requireContext())
                                .setTitle(R.string.existed_notification_dialog_title)
                                .setMessage(getString(R.string.existed_notification_dialog_msg, formatterDateTime.format(oldDate)))
                                .setPositiveButton("Override", (dialog2, which1) -> {
                                    dateReminder = (Date) date.clone();
                                    saveReminder(date, noteWithTags, sharedPreferences, repeatType);
                                    dialog2.dismiss();
                                })
                                .setNegativeButton(getString(R.string.cancel), (dialog2, which12) -> {
                                    dialog.dismiss();
                                    dialog2.dismiss();
                                }).show();
                    } else {
                        saveReminder(date, noteWithTags, sharedPreferences, repeatType);
                    }
                } else {
                    saveReminder(date, noteWithTags, sharedPreferences, repeatType);
                }
            }
        });
        builder.setNegativeButton(getString(R.string.cancel), null);
        builder.setNeutralButton(R.string.delete, (dialog, which) -> new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.delete_reminder_opt)
                .setMessage(R.string.delete_reminder_msg)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.delete, (dialog1, which13) -> {
                    deleteReminder(requireContext(), arguments.getNoteId());
                }).show());
        AlertDialog addNotification = builder.create();
        addNotification.show();
    }

    private void deleteReminder(Context context, long noteID) {
        AlarmManager alarmManager = (AlarmManager) requireContext().getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(requireContext(), ReminderReceiver.class);
        intent.setAction(ReminderReceiver.ACTION_REMINDER);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(requireContext(), (int) arguments.getNoteId(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.cancel(pendingIntent);
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("REMINDER", Context.MODE_PRIVATE);
        sharedPreferences.edit().remove(String.valueOf(arguments.getNoteId())).apply();
        Toast.makeText(requireContext(), getString(R.string.deleted), Toast.LENGTH_LONG).show();
    }

    private void saveReminder(Date date, NoteWithTags noteWithTags, SharedPreferences sharedPreferences, int repeatType) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(String.valueOf(noteWithTags.note.id), date.getTime())
                .apply();
        dateReminder = (Date) date.clone();
        // set time reminder
        Calendar reminderCalendar = Utils.setReminder(dateReminder, requireContext(), noteWithTags, arguments.getNoteId(), repeatType);
        if (reminderCalendar == null) {
            Toast.makeText(requireContext(), "Invalid time", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(requireContext(), "Set reminder at:" + Utils.getDateTimeStringFromCalender(reminderCalendar), Toast.LENGTH_SHORT).show();
        }
    }

    private void onActionShare() {
        String[] items = {"Text", "Image", "Audio"};
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.choose_an_item);
        builder.setItems(items, (dialog, which) -> {
            switch (which) {
                case 0: {
                    shareText();
                    break;
                }
                case 1: {
                    sharePhotos();
                    break;
                }
                case 2: {
                    shareAudios();
                    break;
                }
            }
        });

        builder.create().show();
    }

    private void sharePhotos() {
        NoteWithTags noteWithTags = viewModel.getNote().getValue();
        if (noteWithTags != null) {

            List<Photo> photos = noteWithTags.photos;
            File filesDir = requireContext().getFilesDir();
            File shareDir = new File(filesDir, "share");
            shareDir.mkdir();

            Timber.d(filesDir.toString());

            ArrayList<Uri> uris = new ArrayList<>();
            for (Photo image : photos) {
                File fileInPrivate = new File(Uri.parse(image.uri).getPath());
                File shareFile = new File(shareDir, fileInPrivate.getName());

                if (!shareFile.exists()) {
                    try {
                        FileUtils.copy(fileInPrivate, shareFile);
                        Uri uri = FileProvider.getUriForFile(requireContext(), requireActivity().getPackageName(), shareFile);
                        uris.add(uri);

                    } catch (Exception ie) {
                        Timber.d(ie);
                    }
                } else {
                    Uri uri = FileProvider.getUriForFile(requireContext(), requireActivity().getPackageName(), shareFile);
                    uris.add(uri);
                }
            }

            if (!uris.isEmpty()) {
                Intent shareMultiImageIntent = new Intent()
                        .setAction(Intent.ACTION_SEND_MULTIPLE)
                        .putExtra(Intent.EXTRA_TITLE, "Here are some files.")
                        .setType("image/*")
                        .setFlags(FLAG_GRANT_READ_URI_PERMISSION | FLAG_GRANT_WRITE_URI_PERMISSION)
                        .putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
                startActivity(Intent.createChooser(shareMultiImageIntent, getString(R.string.share_to)));
            }

        }
    }

    private void shareText() {
        String title = ((EditText) binding.layout.findViewById(R.id.text_view_title)).getText().toString();
        String content = ((EditText) binding.layout.findViewById(R.id.note_content)).getText().toString();
        String contentShare = title + "\n" + content;

        NoteWithTags noteWithTags = viewModel.getNote().getValue();
        String mimeType = "text/plain";

        if (noteWithTags != null) {

            if (noteWithTags.note.isCheckList()) {
                // mimeType = "text/*";
                contentShare = title + "\n" + CheckListItem.toReadableString(
                        CheckListItem.compileFromNoteContent(content)
                );
            } else {
                noteWithTags.note.isMarkdown();// mimeType = "text/plain";
            }

            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TITLE, getString(R.string.share_note));
            sendIntent.putExtra(Intent.EXTRA_TEXT, contentShare);
            sendIntent.setType(mimeType);
            startActivity(Intent.createChooser(sendIntent, getString(R.string.share_to)));
        }
    }

    private void shareAudios() {
        NoteWithTags noteWithTags = viewModel.getNote().getValue();
        if (noteWithTags != null) {
            List<Audio> audios = noteWithTags.audios;
            File filesDir = requireContext().getFilesDir();
            File shareDir = new File(filesDir, "share");
            shareDir.mkdir();

            Timber.d(filesDir.toString());

            ArrayList<Uri> uris = new ArrayList<>();
            for (Audio audio : audios) {
                File fileInPrivate = new File(Uri.parse(audio.uri).getPath());
                File shareFile = new File(shareDir, fileInPrivate.getName());

                if (!shareFile.exists()) {
                    try {
                        FileUtils.copy(fileInPrivate, shareFile);
                        Uri uri = FileProvider.getUriForFile(requireContext(), requireActivity().getPackageName(), shareFile);
                        uris.add(uri);

                    } catch (Exception ie) {
                        Timber.d(ie);
                    }
                } else {
                    Uri uri = FileProvider.getUriForFile(requireContext(), requireActivity().getPackageName(), shareFile);
                    uris.add(uri);
                }
            }

            if (!uris.isEmpty()) {
                Intent shareMultiAudioIntent = new Intent()
                        .setAction(Intent.ACTION_SEND_MULTIPLE)
                        .putExtra(Intent.EXTRA_TITLE, "Here are some files.")
                        .setType("audio/*")
                        .setFlags(FLAG_GRANT_READ_URI_PERMISSION | FLAG_GRANT_WRITE_URI_PERMISSION)
                        .putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
                startActivity(Intent.createChooser(shareMultiAudioIntent, getString(R.string.share_to)));
            }

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

            if (itemId == R.id.action_set_bold) {
                formatter.toggleBold();
            } else if (itemId == R.id.action_set_italic) {
                formatter.toggleItalic();
            } else if (itemId == R.id.action_set_font_type) {
                formatter.toggleFontType();
            } else if (itemId == R.id.action_alignment) {
                formatter.toggleAlignment();
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

            saveNote(true, false);
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

    public static final int REQUEST_READ_PERMISSION_FROM_PICK_SOUND = 0x4545;

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.action_pin) {
            if (onActionPin(item)) return true;
            onActionShare();
            return true;
        } else if (itemId == R.id.action_share) {
            onActionShare();
            return true;
        } else if (itemId == R.id.action_add_notification) {
            onActionNotification();
            return true;
        } else if (itemId == R.id.action_add_photo) {
            onActionAddPhoto();
            return true;
        } else if (itemId == R.id.action_add_sound) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                if (shouldShowRequestPermissionRationale(
                        Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            REQUEST_READ_PERMISSION_FROM_PICK_SOUND);
                    return true;
                }
            }
            onActionAddSound();
            return true;
        }
        return false;
    }

    public static final int PICK_IMAGE_REQUEST_CODE = 0x6969;

    private void onActionAddPhoto() {
        Intent intent = new Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        );
        try {
            startActivityForResult(intent, PICK_IMAGE_REQUEST_CODE);
        } catch (ActivityNotFoundException activityNotFoundException) {
            Toast.makeText(requireContext(), "App not found", Toast.LENGTH_SHORT).show();
        }
    }

    public static final int PICK_AUDIO_REQUEST_CODE = 0x4949;

    private void onActionAddSound() {
        Intent intent = new Intent(
                Intent.ACTION_GET_CONTENT
        ).setType("audio/*");
        try {
            startActivityForResult(intent, PICK_AUDIO_REQUEST_CODE);
        } catch (ActivityNotFoundException activityNotFoundException) {
            Toast.makeText(requireContext(), "App not found", Toast.LENGTH_SHORT).show();
        }
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
            saveNote(true, true);
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
        int[] colors = getResources().getIntArray(R.array.note_color);

        for (int i = 0; i < colors.length; i++) {
            if (colors[i] == colorCode) {
                currentColorIdx = i;

                // Change the checkbox tint
                NoteWithTags noteWithTags = viewModel.getNote().getValue();
                if (noteWithTags != null && noteWithTags.note.isCheckList()) {
                    noteWithTags.note.colorIdx = currentColorIdx;
                    BindingAdapters.setCheckboxTintColorForAdapter(binding.checkListRecyclerView, noteWithTags.note);
                    checkListAdapter.notifyDataSetChanged();

                }

                saveNote(true, false);
                return;
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null && data.getData() != null) {
            if (requestCode == PICK_IMAGE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
                Uri uri = data.getData();
                viewModel.addPhoto(uri);
            }
            if (requestCode == PICK_AUDIO_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
                Uri uri = data.getData();
                viewModel.addAudio(uri);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode, @NonNull final String[] permissions, @NonNull final int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_READ_PERMISSION_FROM_PICK_SOUND) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                onActionAddSound();
            }
        }
    }

}
