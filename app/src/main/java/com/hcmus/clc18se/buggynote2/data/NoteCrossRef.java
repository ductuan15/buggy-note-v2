package com.hcmus.clc18se.buggynote2.data;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;

@Entity(primaryKeys = {"note_id", "tag_id"},
        foreignKeys = {
                @ForeignKey(entity = Note.class,
                        parentColumns = "note_id",
                        childColumns = "note_id",
                        onDelete = ForeignKey.CASCADE),
                @ForeignKey(entity = Tag.class,
                        parentColumns = "tag_id",
                        childColumns = "tag_id",
                        onDelete = ForeignKey.CASCADE),
        }
)
public class NoteCrossRef {
    @ColumnInfo(name = "note_id")
    public long noteId;

    @ColumnInfo(name = "tag_id", index = true)
    public long tagId;

    public NoteCrossRef(long noteId, long tagId) {
        this.noteId = noteId;
        this.tagId = tagId;
    }
}
