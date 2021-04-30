package com.hcmus.clc18se.buggynote2.viewmodels;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.hcmus.clc18se.buggynote2.data.Note;
import com.hcmus.clc18se.buggynote2.data.NoteWithTags;
import com.hcmus.clc18se.buggynote2.data.Tag;
import com.hcmus.clc18se.buggynote2.database.BuggyNoteDao;
import com.hcmus.clc18se.buggynote2.database.BuggyNoteDatabase;
import com.hcmus.clc18se.buggynote2.viewmodels.callbacks.NotesViewModelCallBacks;

import java.sql.DriverPropertyInfo;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import timber.log.Timber;

public class NotesViewModel extends AndroidViewModel {

    private final BuggyNoteDao database;

    public void setCallBacks(NotesViewModelCallBacks callBacks) {
        this.callBacks = callBacks;
    }

    private NotesViewModelCallBacks callBacks;

    public NotesViewModel(
            BuggyNoteDao database,
            @NonNull Application application,
            NotesViewModelCallBacks callBacks) {
        super(application);
        this.database = database;
        this.callBacks = callBacks;

        this.noteList.observeForever(noteList -> {
            if (noteList != null) {
                int visibility;
                if (!noteList.isEmpty() &&
                        containsAnyNonArchivedNotes(noteList)) {
                    visibility = View.GONE;
                } else {
                    visibility = View.VISIBLE;
                }
                noteListVisibility.postValue(visibility);
            }
        });

        loadNotes();
    }

    public boolean containsAnyNonArchivedNotes(List<NoteWithTags> noteList) {
        for (NoteWithTags note : noteList) {
            if (!note.note.isArchived()) {
                return true;
            }
        }
        return false;
    }

    public void loadNotes() {
        Timber.d("load notes - ping");
        loadNotesFromDatabase();
    }

    private void loadNotesFromDatabase() {

        BuggyNoteDatabase.databaseWriteExecutor.execute(() -> {
            noteList.postValue(database.getAllNotesWithTag());
        });

    }

    public void insertNewNote(Note note) {
        BuggyNoteDatabase.databaseWriteExecutor.execute(() -> {
                    long id = database.addNewNote(note);
                    if (callBacks != null) {
                        new Handler(Looper.getMainLooper()).post(() -> {
                            loadNotesFromDatabase();
                            callBacks.onNoteItemInserted(id);

                        });
                    }
                }
        );

    }

    public final LiveData<List<NoteWithTags>> getNoteList() {
        return noteList;
    }

    private final MutableLiveData<List<NoteWithTags>> noteList = new MutableLiveData<>();

    public final LiveData<List<NoteWithTags>> unpinnedNotes = Transformations.map(noteList, (noteList) -> {
        List<NoteWithTags> filtered = new ArrayList<>();
        for (NoteWithTags note : noteList) {
            if (!note.note.isPinned && !note.note.isArchived) {
                filtered.add(note);
            }
        }
        return filtered;
    });


    public final LiveData<List<NoteWithTags>> pinnedNotes = Transformations.map(noteList, (noteList) -> {
        List<NoteWithTags> filtered = new ArrayList<>();
        for (NoteWithTags note : noteList) {
            if (note.note.isPinned && !note.note.isArchived) {
                filtered.add(note);
            }
        }
        return filtered;
    });


    public final LiveData<List<NoteWithTags>> archivedNotes = Transformations.map(noteList, (noteList) -> {
        List<NoteWithTags> filtered = new ArrayList<>();
        for (NoteWithTags note : noteList) {
            if (note.note.isArchived) {
                filtered.add(note);
            }
        }
        return filtered;
    });

    public final LiveData<Integer> headerLabelVisibility = Transformations.map(pinnedNotes, (pinnedNotes) -> {
        if (pinnedNotes.isEmpty()) return View.GONE;
        return View.VISIBLE;
    });

    private final MutableLiveData<Integer> noteListVisibility = new MutableLiveData<>(View.GONE);

    public final LiveData<Integer> getNoteListVisibility() {
        return noteListVisibility;
    }

    public final LiveData<Long> getNavigateToNoteDetails() {
        return navigateToNoteDetails;
    }

    private final MutableLiveData<Long> navigateToNoteDetails = new MutableLiveData<>();

    public void startNavigatingToNoteDetails(Long id) {
        navigateToNoteDetails.setValue(id);
    }

    public void doneNavigatingToNoteDetails() {
        navigateToNoteDetails.setValue(null);
    }

    public final LiveData<Boolean> getReloadDataRequest() {
        return reloadDataRequest;
    }

    private final MutableLiveData<Boolean> reloadDataRequest = new MutableLiveData<>(false);

    public void requestReloadingData() {
        reloadDataRequest.postValue(true);
    }

    public void doneRequestingLoadData() {
        reloadDataRequest.setValue(false);
    }

    private final MutableLiveData<Boolean> orderChanged = new MutableLiveData<>(false);

    public void requestReordering() {
        orderChanged.setValue(true);
    }

    public void reorderNotes(List<NoteWithTags> noteList) {
        BuggyNoteDatabase.databaseWriteExecutor.execute(() -> {
            Note[] notes = new Note[noteList.size()];

            for (int i = 0; i < noteList.size(); i++) {
                noteList.get(i).note.order = i;
                notes[i] = noteList.get(i).note;
            }
            database.updateNote(notes);
        });
    }

    public void filterByTags(List<Tag> tags) {
        BuggyNoteDatabase.databaseWriteExecutor.execute(() -> {
            boolean hasSelectedTags = false;
            for (Tag tag : tags) {
                if (tag.isSelectedState()) {
                    hasSelectedTags = true;
                    break;
                }
            }

            if (hasSelectedTags) {
                List<Long> tagIds = new ArrayList<>();
                for (Tag tag : tags) {
                    if (tag.isSelectedState()) {
                        tagIds.add(tag.getId());
                    }
                }

                noteList.postValue(database.filterNoteByTagList(tagIds));
            } else {
                loadNotesFromDatabase();
            }
        });
    }

    public void moveToArchived(NoteWithTags... notes) {
        updateArchivedStatus(true, notes);
    }

    public void moveToNoteList(NoteWithTags... notes) {
        updateArchivedStatus(false, notes);
    }

    private void updateArchivedStatus(boolean status, NoteWithTags... noteList) {
        BuggyNoteDatabase.databaseWriteExecutor.execute(() -> {
            Note[] notes = new Note[noteList.length];
            for (int i = 0; i < noteList.length; ++i) {
                noteList[i].note.isArchived = status;
                notes[i] = noteList[i].note;
            }

            database.updateNote(notes);
            reloadDataRequest.postValue(true);
        });
    }

    public LiveData<Boolean> getOrderChanged() {
        return orderChanged;
    }

    public void finishReordering() {
        orderChanged.setValue(false);
    }
}
