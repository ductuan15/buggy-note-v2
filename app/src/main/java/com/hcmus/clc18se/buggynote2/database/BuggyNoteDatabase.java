package com.hcmus.clc18se.buggynote2.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.hcmus.clc18se.buggynote2.data.Note;
import com.hcmus.clc18se.buggynote2.data.NoteCrossRef;
import com.hcmus.clc18se.buggynote2.data.Tag;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {Note.class, Tag.class, NoteCrossRef.class}, version = 1)
public abstract class BuggyNoteDatabase extends RoomDatabase {

    public abstract BuggyNoteDao buggyNoteDatabaseDao();

    public static volatile BuggyNoteDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;
    static final ExecutorService databaseWriteExecutor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    static BuggyNoteDatabase getInstance(final Context context) {
        if (INSTANCE == null) {
            synchronized (BuggyNoteDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE =
                            Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    BuggyNoteDatabase.class,
                                    "buggy_note2_database")
                                    .fallbackToDestructiveMigration()
                                    .build();
                }
            }
        }
        return INSTANCE;
    }

}
