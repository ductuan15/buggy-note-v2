package com.hcmus.clc18se.buggynote2.viewmodels.factories;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.hcmus.clc18se.buggynote2.database.BuggyNoteDao;
import com.hcmus.clc18se.buggynote2.viewmodels.TagSelectionViewModel;

public class TagSelectionViewModelFactory implements ViewModelProvider.Factory {

    private final BuggyNoteDao database;
    private final long noteId;

    public TagSelectionViewModelFactory(BuggyNoteDao database, long noteId) {
        this.database = database;
        this.noteId = noteId;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(TagSelectionViewModel.class)) {
            return (T) new TagSelectionViewModel(noteId, database);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
