package com.hcmus.clc18se.buggynote2.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.hcmus.clc18se.buggynote2.data.NoteCrossRef;
import com.hcmus.clc18se.buggynote2.data.Tag;
import com.hcmus.clc18se.buggynote2.database.BuggyNoteDao;
import com.hcmus.clc18se.buggynote2.database.BuggyNoteDatabase;

import java.util.List;

public class TagSelectionViewModel extends ViewModel {

    private final long noteId;
    private final BuggyNoteDao database;

    public TagSelectionViewModel(long noteId, BuggyNoteDao database) {
        this.noteId = noteId;
        this.database = database;
        loadTags();
    }

    private final MutableLiveData<List<Tag>> tags = new MutableLiveData<>();

    public LiveData<List<Tag>> getTags() {
        return tags;
    }

    private final MutableLiveData<Boolean> changesOccurred = new MutableLiveData<>(false);
    public LiveData<Boolean> getChangesOccurred() {
        return changesOccurred;
    }

    private void loadTags() {
        BuggyNoteDatabase.databaseWriteExecutor.execute(() -> {
            tags.postValue(getAllTagWithSelectedState(noteId));
        });
    }

    private List<Tag> getAllTagWithSelectedState(long noteId) {
        List<Tag> tags = database.getAllTags();
        for (Tag tag : tags) {
            tag.setSelectedState(database.containsNoteCrossRef(noteId, tag.getId()));
        }
        return tags;
    }

    public void addSelectedTags(Tag tag) {
        BuggyNoteDatabase.databaseWriteExecutor.execute(() -> {
            database.addNoteCrossRef(new NoteCrossRef(noteId, tag.getId()));
            changesOccurred.postValue(true);
        });
    }

    public void removeSelectedTags(Tag tag) {
        BuggyNoteDatabase.databaseWriteExecutor.execute(() -> {
            database.deleteNoteCrossRef(new NoteCrossRef(noteId, tag.getId()));
            changesOccurred.postValue(true);
        });
    }
}
