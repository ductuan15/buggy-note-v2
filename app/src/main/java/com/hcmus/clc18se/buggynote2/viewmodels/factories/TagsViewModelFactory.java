package com.hcmus.clc18se.buggynote2.viewmodels.factories;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.hcmus.clc18se.buggynote2.database.BuggyNoteDao;
import com.hcmus.clc18se.buggynote2.viewmodels.TagsViewModel;

public class TagsViewModelFactory implements ViewModelProvider.Factory {
    final BuggyNoteDao database;

    public TagsViewModelFactory(BuggyNoteDao database) {
        this.database = database;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(TagsViewModel.class)) {
            return (T) new TagsViewModel(database);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
