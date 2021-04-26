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
import com.hcmus.clc18se.buggynote2.database.BuggyNoteDao;
import com.hcmus.clc18se.buggynote2.database.BuggyNoteDatabase;
import com.hcmus.clc18se.buggynote2.viewmodels.callbacks.NotesViewModelCallBacks;

import java.util.ArrayList;
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
                }
                else {
                    visibility = View.VISIBLE;
                }
                noteListVisibility.postValue(visibility);
            }
        });

        loadNotes();
    }

    public boolean containsAnyNonArchivedNotes(List<NoteWithTags> noteList) {
        for (NoteWithTags note: noteList) {
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
        // new InsertNoteAsyncTask(new WeakReference<>(this)).execute(note);
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
            if (!note.note.isPinned) {
                filtered.add(note);
            }
        }
        return filtered;
    });


    public final LiveData<List<NoteWithTags>> pinnedNotes = Transformations.map(noteList, (noteList) -> {
        List<NoteWithTags> filtered = new ArrayList<>();
        for (NoteWithTags note : noteList) {
            if (note.note.isPinned) {
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
}