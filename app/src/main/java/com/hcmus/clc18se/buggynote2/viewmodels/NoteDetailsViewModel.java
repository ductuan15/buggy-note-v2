package com.hcmus.clc18se.buggynote2.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.hcmus.clc18se.buggynote2.data.NoteWithTags;
import com.hcmus.clc18se.buggynote2.database.BuggyNoteDao;
import com.hcmus.clc18se.buggynote2.database.BuggyNoteDatabase;

import timber.log.Timber;

public class NoteDetailsViewModel extends ViewModel {

    private final long id;
    private final BuggyNoteDao database;

    public NoteDetailsViewModel(long id, BuggyNoteDao database) {
        this.id = id;
        this.database = database;
    }

    private LiveData<NoteWithTags> note;

    public LiveData<NoteWithTags> getNote() {
        if (note == null) {
            note = database.getNoteFromId(id);
        }
        return note;
    }

    private MutableLiveData<Boolean> reloadDataRequest = new MutableLiveData<>(false);

    public final LiveData<Boolean> getReloadDataRequestState() {
        return (LiveData<Boolean>) reloadDataRequest;
    }

    public void requestReloadingData() {
        reloadDataRequest.setValue(true);
    }

    public void doneRequestingReloadData() {
        reloadDataRequest.setValue(false);
    }

    private final MutableLiveData<Long> navigateToTagSelection = new MutableLiveData<>(null);

    public final LiveData<Long> getNavigateToTagSelection() {
        return (LiveData<Long>) navigateToTagSelection;
    }

    public void navigateToTagSelection() {
        navigateToTagSelection.setValue(id);
    }

    public void doneNavigatingToTagSelection() {
        navigateToTagSelection.setValue(null);
    }

    private final MutableLiveData<Boolean> deleteRequest = new MutableLiveData<>(false);
    public LiveData<Boolean> getDeleteRequest() {
        return (LiveData<Boolean>) deleteRequest;
    }

    public void deleteMe() {
        if (note.getValue() != null) {
            BuggyNoteDatabase.databaseWriteExecutor.execute(() -> {
                int nCol = database.removeNote(note.getValue().note);
                Timber.d("Remove note" + nCol + " affected");

                deleteRequest.postValue(true);
            });
        }
    }

    public void reloadNote() {
        note = database.getNoteFromId(id);
    }

    public void togglePin() {
        if (note.getValue() != null) {
            note.getValue().note.isPinned = !note.getValue().note.isPinned;
        }
    }
}
