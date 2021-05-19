package com.hcmus.clc18se.buggynote2.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.hcmus.clc18se.buggynote2.data.Audio;
import com.hcmus.clc18se.buggynote2.data.Note;
import com.hcmus.clc18se.buggynote2.data.NoteCrossRef;
import com.hcmus.clc18se.buggynote2.data.Photo;
import com.hcmus.clc18se.buggynote2.data.Tag;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {Note.class,
            Tag.class,
            NoteCrossRef.class,
            Photo.class,
            Audio.class,
            NoteFTS.class},
        version = 10)
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
                            .addMigrations(MIGRATION_3_4)
                            .addMigrations(MIGRATION_4_5)
                            .addMigrations(MIGRATION_5_6)
                            .addMigrations(MIGRATION_6_7)
                            .addMigrations(MIGRATION_7_8)
                            .addMigrations(MIGRATION_8_9)
                            .addMigrations(MIGRATION_9_10)
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

    public static Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE note ADD COLUMN `type` INTEGER NOT NULL DEFAULT 0");
        }
    };

    public static Migration MIGRATION_4_5 = new Migration(4, 5) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE IF NOT EXISTS `photo` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `uri` TEXT, `note_id` INTEGER NOT NULL)");
            database.execSQL("CREATE TABLE IF NOT EXISTS `audio` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `uri` TEXT, `note_id` INTEGER NOT NULL)");
        }
    };

    public static Migration MIGRATION_5_6 = new Migration(5, 6) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("UPDATE `note` SET `color` = NULL");
        }
    };
    public static Migration MIGRATION_6_7 = new Migration(6, 7) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE IF NOT EXISTS `note_new` " +
                    "(`note_id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`name` TEXT," +
                    " `title` TEXT, " +
                    "`note_content` TEXT, " +
                    "`last_modify` INTEGER NOT NULL, " +
                    "`title_format` TEXT DEFAULT '8388611|0|0', " +
                    "`content_format` TEXT DEFAULT '8388611|0|0', " +
                    "`order` INTEGER NOT NULL DEFAULT 0, " +
                    "`is_pinned` INTEGER NOT NULL DEFAULT 0, " +
                    "`is_archived` INTEGER NOT NULL DEFAULT 0, " +
                    "`removing_date` INTEGER DEFAULT null, " +
                    "`color` INTEGER DEFAULT 0, " +
                    "`type` INTEGER NOT NULL DEFAULT 0)");

            database.execSQL("INSERT INTO `note_new` SELECT * from `note`");
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_note_note_id_new` ON `note_new` (`note_id`)");

            database.execSQL("DROP TABLE IF EXISTS `note`");
            database.execSQL("DROP TABLE IF EXISTS `index_note_note_id`");

            database.execSQL("ALTER TABLE `note_new` RENAME TO `note`");
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_note_note_id` ON `note` (`note_id`)");

        }
    };

    public static Migration MIGRATION_7_8 = new Migration(7, 8) {

        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("UPDATE `note` SET `color`= 0 WHERE `color` IS NULL");
        }
    };

    public static Migration MIGRATION_8_9 = new Migration(8, 9) {

        @Override
        public void migrate(@NonNull SupportSQLiteDatabase _db) {
            _db.execSQL("CREATE VIRTUAL TABLE IF NOT EXISTS note_fts USING FTS4(title TEXT, note_content TEXT, content=`note`)");
            _db.execSQL("CREATE TRIGGER IF NOT EXISTS room_fts_content_sync_note_fts_BEFORE_UPDATE BEFORE UPDATE ON `note` BEGIN DELETE FROM `note_fts` WHERE `docid`=OLD.`rowid`; END");
            _db.execSQL("CREATE TRIGGER IF NOT EXISTS room_fts_content_sync_note_fts_BEFORE_DELETE BEFORE DELETE ON `note` BEGIN DELETE FROM `note_fts` WHERE `docid`=OLD.`rowid`; END");
            _db.execSQL("CREATE TRIGGER IF NOT EXISTS room_fts_content_sync_note_fts_AFTER_UPDATE AFTER UPDATE ON `note` BEGIN INSERT INTO `note_fts`(`docid`, `title`, `note_content`) VALUES (NEW.`rowid`, NEW.`title`, NEW.`note_content`); END");
            _db.execSQL("CREATE TRIGGER IF NOT EXISTS room_fts_content_sync_note_fts_AFTER_INSERT AFTER INSERT ON `note` BEGIN INSERT INTO `note_fts`(`docid`, `title`, `note_content`) VALUES (NEW.`rowid`, NEW.`title`, NEW.`note_content`); END");

            _db.execSQL("INSERT INTO `note_fts`(`note_fts`) VALUES ('rebuild')");
        }
    };

    public static Migration MIGRATION_9_10 = new Migration(9, 10) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase _db) {

            // Add foreign key constraint to photo & audio entities
            _db.execSQL("CREATE TABLE IF NOT EXISTS `photo_new` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `uri` TEXT, `note_id` INTEGER NOT NULL, FOREIGN KEY(`note_id`) REFERENCES `note`(`note_id`) ON UPDATE NO ACTION ON DELETE CASCADE )");
            _db.execSQL("CREATE TABLE IF NOT EXISTS `audio_new` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `uri` TEXT, `note_id` INTEGER NOT NULL, FOREIGN KEY(`note_id`) REFERENCES `note`(`note_id`) ON UPDATE NO ACTION ON DELETE CASCADE )");

            _db.execSQL("INSERT INTO photo_new " +
                    "SELECT * FROM photo WHERE EXISTS " +
                    "(SELECT 1 FROM note WHERE note.note_id = photo.note_id)");

            _db.execSQL("INSERT INTO audio_new " +
                    "SELECT * FROM audio WHERE EXISTS " +
                    "(SELECT 1 FROM note WHERE note.note_id = audio.note_id)");

            _db.execSQL("DROP TABLE IF EXISTS photo");
            _db.execSQL("DROP TABLE IF EXISTS audio");

            _db.execSQL("ALTER TABLE `photo_new` RENAME TO `photo`");
            _db.execSQL("ALTER TABLE `audio_new` RENAME TO `audio`");

            _db.execSQL("CREATE INDEX IF NOT EXISTS `index_photo_note_id` ON `photo` (`note_id`)");
            _db.execSQL("CREATE INDEX IF NOT EXISTS `index_audio_note_id` ON `audio` (`note_id`)");
        }
    };
}
