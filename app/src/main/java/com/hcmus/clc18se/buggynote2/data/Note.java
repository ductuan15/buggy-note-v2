package com.hcmus.clc18se.buggynote2.data;

import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.hcmus.clc18se.buggynote2.utils.TextFormatter;

@Entity(tableName = "note")
public class Note {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "note_id", index = true)
    public long id = 0L;

    public String name = "";

    public String title = "";

    @ColumnInfo(name = "note_content")
    public String noteContent = "";

    @ColumnInfo(name = "last_modify")
    public long lastModify = System.currentTimeMillis();

    @ColumnInfo(name = "title_format", defaultValue = TextFormatter.DEFAULT_FORMAT_STRING)
    public String titleFormat = TextFormatter.DEFAULT_FORMAT_STRING;

    @ColumnInfo(name = "content_format", defaultValue = TextFormatter.DEFAULT_FORMAT_STRING)
    public String contentFormat = TextFormatter.DEFAULT_FORMAT_STRING;

    @ColumnInfo(defaultValue = "0")
    public int order = 0;

    @ColumnInfo(name = "is_pinned", defaultValue = "0")
    public boolean isPinned = false;

    @ColumnInfo(name = "is_archived", defaultValue = "0")
    public boolean isArchived = false;

    @ColumnInfo(name = "removing_date", defaultValue = "null")
    @Nullable
    public Long removingDate = null;

    @Ignore
    public static final int N_REMOVING_DAYS = 30;

    @Ignore
    public Note() {}

    public Note(long id,
                String name,
                String noteContent,
                long lastModify,
                String titleFormat,
                String contentFormat,
                int order,
                boolean isPinned,
                boolean isArchived) {
        this.id = id;
        this.name = name;
        this.noteContent = noteContent;
        this.lastModify = lastModify;
        this.titleFormat = titleFormat;
        this.contentFormat = contentFormat;
        this.order = order;
        this.isPinned = isPinned;
        this.isArchived = isArchived;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNoteContent() {
        return noteContent;
    }

    public void setNoteContent(String noteContent) {
        this.noteContent = noteContent;
    }

    public Long getLastModify() {
        return lastModify;
    }

    public static Note emptyInstance() {
        return new Note(0L,
                "",
                "",
                System.currentTimeMillis(),
                "",
                "",
                0,
                false,
                false);
    }
}
