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
import com.hcmus.clc18se.buggynote2.data.NoteCrossRef;
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
    int deleteTag(Tag tag);

    @Query("select * from tag order by name asc")
    List<Tag> getAllTags();

    @Query("select count(*) from tag where name = :content")
    boolean containsTag(String content);

    @Query("select count(*) from notecrossref where note_id = :noteId and tag_id = :tagId")
    boolean containsNoteCrossRef(long noteId, long tagId);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void addNoteCrossRef(NoteCrossRef noteCrossRef);

    @Delete
    void deleteNoteCrossRef(NoteCrossRef noteCrossRef);
    //
//    @Query("select * from notecrossref where note_id = :noteId")
//    suspend fun getNoteCrossRef(noteId: Long): List<NoteCrossRef>
}
