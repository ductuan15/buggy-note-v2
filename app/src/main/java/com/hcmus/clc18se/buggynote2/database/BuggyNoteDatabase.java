package com.hcmus.clc18se.buggynote2.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.hcmus.clc18se.buggynote2.data.Note;
import com.hcmus.clc18se.buggynote2.data.NoteCrossRef;
import com.hcmus.clc18se.buggynote2.data.Tag;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {Note.class, Tag.class, NoteCrossRef.class}, version = 2)
public abstract class BuggyNoteDatabase extends RoomDatabase {

    public abstract BuggyNoteDao buggyNoteDatabaseDao();

    static volatile BuggyNoteDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    public static BuggyNoteDatabase getInstance(final Context context) {
        if (INSTANCE == null) {
            synchronized (BuggyNoteDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            BuggyNoteDatabase.class,
                            "buggy_note2_database")
                            .addMigrations(MIGRATION_1_2)
                            .addMigrations(MIGRATION_2_3)
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    public static Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE note ADD COLUMN `removing_date` INTEGER DEFAULT null");
        }
    };

    public static Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE note ADD COLUMN `color` INTEGER DEFAULT null");
        }
    };

}
