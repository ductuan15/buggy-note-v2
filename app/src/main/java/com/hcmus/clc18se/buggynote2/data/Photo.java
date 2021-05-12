package com.hcmus.clc18se.buggynote2.data;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "photo")
public class Photo {

    @PrimaryKey(autoGenerate = true)
    public long id = 0L;

    public String uri = "";

    @ColumnInfo(name = "note_id")
    public long noteId = 0L;

    public Photo(long id, String uri, long noteId) {
        this.id = id;
        this.uri = uri;
        this.noteId = noteId;
    }
}
