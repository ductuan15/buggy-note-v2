package com.hcmus.clc18se.buggynote2.data;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.room.Embedded;
import androidx.room.Junction;
import androidx.room.Relation;

import com.hcmus.clc18se.buggynote2.utils.TextFormatter;

import java.util.List;

public class NoteWithTags {

    @Embedded
    public Note note;

    @Relation(
            parentColumn = "note_id",
            entityColumn = "tag_id",
            associateBy =  @Junction(NoteCrossRef.class)
    )
    public List<Tag> tags;

    TextFormatter getTitleFormat() {
        if (this.note.titleFormat.isEmpty()) {
            note.titleFormat = TextFormatter.DEFAULT_FORMAT_STRING;
        }
        return TextFormatter.parseFormat(note.titleFormat);
    }

    TextFormatter getContentFormat() {
        if (this.note.contentFormat.isEmpty()) {
            note.contentFormat = TextFormatter.DEFAULT_FORMAT_STRING;
        }
        return TextFormatter.parseFormat(note.contentFormat);
    }

    static DiffUtil.ItemCallback<NoteWithTags> diffCallBacks = new DiffUtil.ItemCallback<NoteWithTags>() {
        @Override
        public boolean areItemsTheSame(@NonNull NoteWithTags oldItem, @NonNull NoteWithTags newItem) {
            return false;
        }

        @Override
        public boolean areContentsTheSame(@NonNull NoteWithTags oldItem, @NonNull NoteWithTags newItem) {
            return false;
        }
    };
}
