package com.hcmus.clc18se.buggynote2.data;

import android.content.Context;

import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.hcmus.clc18se.buggynote2.R;
import com.hcmus.clc18se.buggynote2.utils.TextFormatter;

import java.util.Random;

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

    /**
     * Prior to database schema version 6:
     * - Color value of the note.
     * From version 6:
     * - Indicate the index of the color from `R.array.note_color` to support dark mode.
     * - When the value is `null`, the color of the note is control by R.attr.colorSurface.
     */
    @ColumnInfo(name = "color", defaultValue = "0")
    public Integer colorIdx = 0;

    @ColumnInfo(defaultValue = "0")
    public int type = 0;

    @Ignore
    public static final int N_REMOVING_DAYS = 30;

    @Ignore
    public Note() {
    }

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

    public Integer getColor(Context context) {
        if (colorIdx == null) return null;
        int[] colorArr = context.getResources().getIntArray(R.array.note_color);
        if (colorIdx >= 0 && colorIdx < colorArr.length) {
            return colorArr[colorIdx];
        }

        return null;
    }

    public Integer getTitleColor(Context context) {
        if (colorIdx == null) return null;
        int[] titleColorArr = context.getResources().getIntArray(R.array.note_title_color);
        if (colorIdx >= 0 && colorIdx < titleColorArr.length) {
            return titleColorArr[colorIdx];
        }
        return null;
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

    public boolean isPlainText() {
        return type == NOTE_TYPE_PLAIN_TEXT;
    }

    public boolean isMarkdown() {
        return type == NOTE_TYPE_MARKDOWN;
    }

    public boolean isCheckList() {
        return type == NOTE_TYPE_CHECK_LIST;
    }

    public static final int NOTE_TYPE_PLAIN_TEXT = 0;

    public static final int NOTE_TYPE_CHECK_LIST = 1;

    public static final int NOTE_TYPE_MARKDOWN = 2;
}
