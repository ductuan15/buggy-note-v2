package com.hcmus.clc18se.buggynote2.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.hcmus.clc18se.buggynote2.data.Note;
import com.hcmus.clc18se.buggynote2.data.NoteWithTags;
import com.hcmus.clc18se.buggynote2.data.Tag;

import java.util.List;

class DaoConstant {
    public static final String DEFAULT_SORT_ORDER = "is_pinned desc,`order` asc, note_id desc";
}

@Dao
public interface BuggyNoteDao {

    @Transaction
    @Query("select * from note order by " + DaoConstant.DEFAULT_SORT_ORDER)
    List<NoteWithTags> getAllNotesWithTag();

    @Transaction
    @Query("select * from note where note_id = :id")
    LiveData<NoteWithTags> getNoteFromId(Long id);

    @Transaction
    @Delete
    int removeNote(Note... notes);

    @Update
    void updateNote(Note... notes);

    @Insert
    long addNewNote(Note notes);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long insertTag(Tag tag);

    @Update
    void updateTag(Tag tag);

    @Transaction
    @Delete
    void deleteTag(Tag tag);

}
