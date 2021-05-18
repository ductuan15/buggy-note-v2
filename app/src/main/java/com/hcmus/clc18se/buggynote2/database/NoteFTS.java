package com.hcmus.clc18se.buggynote2.database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Fts4;

import com.hcmus.clc18se.buggynote2.data.Note;

@Entity(tableName = "note_fts")
@Fts4(contentEntity = Note.class)
public class NoteFTS {

    public String title = "";

    @ColumnInfo(name = "note_content")
    public String noteContent = "";
}
