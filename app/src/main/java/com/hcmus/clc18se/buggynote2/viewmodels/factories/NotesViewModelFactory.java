package com.hcmus.clc18se.buggynote2.viewmodels.factories;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.hcmus.clc18se.buggynote2.database.BuggyNoteDao;
import com.hcmus.clc18se.buggynote2.viewmodels.NotesViewModel;
import com.hcmus.clc18se.buggynote2.viewmodels.callbacks.NotesViewModelCallBacks;

public class NotesViewModelFactory implements ViewModelProvider.Factory {

    private final BuggyNoteDao database;
    private final Application application;
    private final NotesViewModelCallBacks callBacks;

    public NotesViewModelFactory(BuggyNoteDao database,
                                 Application application,
                                 NotesViewModelCallBacks callBacks) {
        this.database = database;
        this.application = application;
        this.callBacks = callBacks;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(NotesViewModel.class)) {
            return (T) new NotesViewModel(database, application, callBacks);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
