package com.hcmus.clc18se.buggynote2.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.hcmus.clc18se.buggynote2.data.Tag;
import com.hcmus.clc18se.buggynote2.database.BuggyNoteDao;
import com.hcmus.clc18se.buggynote2.database.BuggyNoteDatabase;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class TagsViewModel extends ViewModel {
    final BuggyNoteDao database;

    public TagsViewModel(BuggyNoteDao database) {
        this.database = database;
        loadTags();
    }

    private void loadTags() {
        BuggyNoteDatabase.databaseWriteExecutor.execute(() -> tags.postValue(database.getAllTags()));
    }

    private final MutableLiveData<List<Tag>> tags = new MutableLiveData<>();

    public LiveData<List<Tag>> getTags() {
        return tags;
    }

    public boolean insertTag(String tagContent) {
        try {
            return BuggyNoteDatabase.databaseWriteExecutor.submit(
                    () -> {
                        if (database.containsTag(tagContent)) {
                            return false;
                        } else {
                            database.insertTag(new Tag(0L, tagContent));
                            loadTags();
                            return true;
                        }
                    }
            ).get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateTag(Tag tag) {
        try {
            return BuggyNoteDatabase.databaseWriteExecutor.submit(
                    () -> {
                        tag.setName(tag.getName().trim());
                        if (tag.getName().isEmpty()) {
                            return false;
                        }
                        if (database.containsTag(tag.getName())) {
                            return false;
                        }
                        database.updateTag(tag);
                        loadTags();
                        return true;
                    }).get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void deleteTag(Tag tag) {
        BuggyNoteDatabase.databaseWriteExecutor.execute(
                () -> {
                    database.deleteTag(tag);
                    loadTags();
                }
        );
    }
}