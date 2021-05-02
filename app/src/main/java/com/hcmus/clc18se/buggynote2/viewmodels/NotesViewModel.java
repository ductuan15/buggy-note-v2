package com.hcmus.clc18se.buggynote2.viewmodels;

import android.app.Application;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

import timber.log.Timber;

public class NotesViewModel extends AndroidViewModel {

    private final BuggyNoteDao database;

    public NotesViewModel(
            BuggyNoteDao database,
            @NonNull Application application) {
        super(application);
        this.database = database;

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
            if (!note.note.isArchived) {
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

    public long insertNewNote(Note note) {
        try {
            return BuggyNoteDatabase.databaseWriteExecutor.submit(() -> {
                        long id = database.addNewNote(note);
                        loadNotesFromDatabase();
                        return id;
                    }
            ).get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        return 0L;
    }

    public final LiveData<List<NoteWithTags>> getNoteList() {
        return noteList;
    }

    private final MutableLiveData<List<NoteWithTags>> noteList = new MutableLiveData<>();

    public final LiveData<List<NoteWithTags>> unpinnedNotes = Transformations.map(noteList, (noteList) -> {
        List<NoteWithTags> filtered = new ArrayList<>();
        for (NoteWithTags noteWithTags : noteList) {
            if (!noteWithTags.note.isPinned
                    && !noteWithTags.note.isArchived
                    && noteWithTags.note.removingDate == null
            ) {
                filtered.add(noteWithTags);
            }
        }
        return filtered;
    });

    public final LiveData<List<NoteWithTags>> pinnedNotes = Transformations.map(noteList, (noteList) -> {
        List<NoteWithTags> filtered = new ArrayList<>();
        for (NoteWithTags noteWithTags : noteList) {
            if (noteWithTags.note.isPinned
                    && !noteWithTags.note.isArchived
                    && noteWithTags.note.removingDate == null
            ) {
                filtered.add(noteWithTags);
            }
        }
        return filtered;
    });

    public final LiveData<List<NoteWithTags>> archivedNotes = Transformations.map(noteList, (noteList) -> {
        List<NoteWithTags> filtered = new ArrayList<>();
        for (NoteWithTags noteWithTags : noteList) {
            if (noteWithTags.note.isArchived && noteWithTags.note.removingDate == null) {
                filtered.add(noteWithTags);
            }
        }
        return filtered;
    });

    public final LiveData<List<NoteWithTags>> removedNotes = Transformations.map(noteList, (noteList) -> {

        List<NoteWithTags> filtered = new ArrayList<>();
        for (NoteWithTags noteWithTags : noteList) {
            if (noteWithTags.note.removingDate != null) {
                filtered.add(noteWithTags);
            }
        }

        Collections.sort(filtered, (o1, o2) -> o1.note.removingDate.compareTo(o2.note.removingDate));

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

    public void togglePin(boolean isPinned, NoteWithTags... noteList) {
        BuggyNoteDatabase.databaseWriteExecutor.execute(() -> {
            Note[] notes = new Note[noteList.length];
            for (int i = 0; i < noteList.length; ++i) {
                noteList[i].note.isPinned = isPinned;
                notes[i] = noteList[i].note;
            }

            database.updateNote(notes);
            reloadDataRequest.postValue(true);

        });
    }

    public void permanentlyRemoveNote(List<NoteWithTags> noteWithTags) {
        BuggyNoteDatabase.databaseWriteExecutor.execute(() -> {

            Note[] notes = new Note[noteWithTags.size()];
            for (int i = 0; i < noteWithTags.size(); ++i) {
                notes[i] = noteWithTags.get(i).note;
            }
            database.removeNote(notes);
            loadNotesFromDatabase();
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

    public void moveToTrash(NoteWithTags... noteList) {

        BuggyNoteDatabase.databaseWriteExecutor.execute(() -> {
            long removingTime = System.currentTimeMillis() + Note.N_REMOVING_DAYS * 24L * 60L * 60L * 1000L;

            Note[] notes = new Note[noteList.length];
            for (int i = 0; i < noteList.length; ++i) {
                noteList[i].note.removingDate = removingTime;
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

    public void restoreNoteFromTrash(NoteWithTags... noteList) {
        BuggyNoteDatabase.databaseWriteExecutor.execute(() -> {

            Note[] notes = new Note[noteList.length];

            for (int i = 0; i < noteList.length; ++i) {
                noteList[i].note.removingDate = null;
                noteList[i].note.isArchived = false;
                notes[i] = noteList[i].note;
            }

            database.updateNote(notes);
            reloadDataRequest.postValue(true);
        });

    }
}
