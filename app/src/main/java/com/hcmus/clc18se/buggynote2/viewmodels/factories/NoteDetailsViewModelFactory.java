package com.hcmus.clc18se.buggynote2.viewmodels.factories;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.hcmus.clc18se.buggynote2.database.BuggyNoteDao;
import com.hcmus.clc18se.buggynote2.viewmodels.NoteDetailsViewModel;

public class NoteDetailsViewModelFactory implements ViewModelProvider.Factory {

    private final long id;
    private final BuggyNoteDao database;

    public NoteDetailsViewModelFactory(long id, BuggyNoteDao database) {
        this.id = id;
        this.database = database;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(NoteDetailsViewModel.class)) {
            return (T) new NoteDetailsViewModel(id, database);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
