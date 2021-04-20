package com.hcmus.clc18se.buggynote2.database;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.hcmus.clc18se.buggynote2.data.NoteWithTags;
import com.hcmus.clc18se.buggynote2.data.Tag;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class BuggyNoteRepository {

    private BuggyNoteDao buggyNoteDao;
    // private LiveData<NoteWithTags> allNotes;

    public BuggyNoteRepository(Application application) {
        BuggyNoteDatabase db = BuggyNoteDatabase.getInstance(application);
        buggyNoteDao = db.buggyNoteDatabaseDao();
    }

    public List<NoteWithTags> getAllNotesWithTag() {
        AtomicReference<List<NoteWithTags>> noteWithTags = new AtomicReference<>();
        BuggyNoteDatabase.databaseWriteExecutor.execute(() -> {
                    noteWithTags.set(buggyNoteDao.getAllNotesWithTag());
                }
        );
        return noteWithTags.get();
    }

    public LiveData<NoteWithTags> getNoteFromTagId(long id) {
        AtomicReference<LiveData<NoteWithTags>> noteWithTags = new AtomicReference<>();
        BuggyNoteDatabase.databaseWriteExecutor.execute(() -> {
                    noteWithTags.set(buggyNoteDao.getNoteFromId(id));
                }
        );
        return noteWithTags.get();
    }

    public void insertTag(Tag tag) {
        BuggyNoteDatabase.databaseWriteExecutor.execute(() -> buggyNoteDao.insertTag(tag));
    }

    public void deleteTag(Tag tag) {
        BuggyNoteDatabase.databaseWriteExecutor.execute(() -> buggyNoteDao.deleteTag(tag));
    }

    public void updateTag(Tag tag) {
        BuggyNoteDatabase.databaseWriteExecutor.execute(() -> buggyNoteDao.updateTag(tag));
    }


}
