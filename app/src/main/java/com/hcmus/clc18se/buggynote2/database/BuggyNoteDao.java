package com.hcmus.clc18se.buggynote2.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.hcmus.clc18se.buggynote2.data.Audio;
import com.hcmus.clc18se.buggynote2.data.Note;
import com.hcmus.clc18se.buggynote2.data.NoteCrossRef;
import com.hcmus.clc18se.buggynote2.data.NoteWithTags;
import com.hcmus.clc18se.buggynote2.data.Photo;
import com.hcmus.clc18se.buggynote2.data.Tag;

import java.util.List;

import static com.hcmus.clc18se.buggynote2.database.DaoConstant.DEFAULT_SORT_ORDER;

class DaoConstant {
    public static final String DEFAULT_SORT_ORDER = "is_pinned desc,`order` asc, note_id desc";
}

@Dao
public interface BuggyNoteDao {

    @Transaction
    @Query("select * from note order by " + DEFAULT_SORT_ORDER)
    List<NoteWithTags> getAllNotesWithTag();

    @Transaction
    @Query("select * from note where note_id = :id")
    LiveData<NoteWithTags> getNoteLiveDataFromId(Long id);

    @Query("select * from note where note_id = :id")
    Note getPlainNoteFromId(Long id);

    @Transaction
    @Query("select * from note where note_id = :id")
    NoteWithTags getNoteFromId(Long id);

    @Transaction
    @Delete
    int removeNote(Note... notes);

    @Update
    void updateNote(Note... notes);

    @Insert
    long addNewNote(Note notes);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertTag(Tag tag);

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

    @Transaction
    @Query(
            "select * from note where note_id in (" +
                    "select note_id from notecrossref where tag_id in (:tagIds)" +
                    ") order by " + DEFAULT_SORT_ORDER
    )
    List<NoteWithTags> filterNoteByTagList(List<Long> tagIds);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void addPhoto(Photo... photos);

    @Delete
    void deletePhoto(Photo... photos);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void addAudio(Audio... audios);

    @Delete
    void deleteAudio(Audio... audios);

    @Transaction
    @Query("select * " +
            "from note " +
            "join note_fts on note.rowid = note_fts.rowid " +
            "where note_fts match :query")
    List<NoteWithTags> searchNote(String query);
}