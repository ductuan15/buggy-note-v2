package com.hcmus.clc18se.buggynote2;

import android.app.Application;

import timber.log.Timber;

public class BuggyNoteApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Timber.plant(new Timber.DebugTree());
    }
}
