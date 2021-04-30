package com.hcmus.clc18se.buggynote2.viewmodels.factories;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.hcmus.clc18se.buggynote2.database.BuggyNoteDao;
import com.hcmus.clc18se.buggynote2.viewmodels.NotesViewModel;
import com.hcmus.clc18se.buggynote2.viewmodels.TagsViewModel;
import com.hcmus.clc18se.buggynote2.viewmodels.callbacks.TagsViewModelCallbacks;

public class TagsViewModelFactory implements ViewModelProvider.Factory {
    final BuggyNoteDao database;
    TagsViewModelCallbacks callbacks;

    public TagsViewModelFactory(BuggyNoteDao database, TagsViewModelCallbacks callbacks) {
        this.database = database;
        this.callbacks = callbacks;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(TagsViewModel.class)) {
            return (T) new TagsViewModel(database, callbacks);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
