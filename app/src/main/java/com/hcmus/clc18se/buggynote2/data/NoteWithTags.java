package com.hcmus.clc18se.buggynote2.data;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.room.Embedded;
import androidx.room.Junction;
import androidx.room.Relation;

import com.hcmus.clc18se.buggynote2.utils.TextFormatter;

import java.util.List;
import java.util.Objects;

public class NoteWithTags {

    @Embedded
    public Note note;

    @Relation(
            parentColumn = "note_id",
            entityColumn = "tag_id",
            associateBy = @Junction(NoteCrossRef.class)
    )
    public List<Tag> tags;

    @Relation(
            parentColumn = "note_id",
            entityColumn = "note_id",
            entity = Photo.class
    )
    public List<Photo> photos;

    @Relation(
            parentColumn = "note_id",
            entityColumn = "note_id",
            entity = Photo.class
    )
    public List<Photo> audios;

    public TextFormatter getTitleFormat() {
        if (this.note.titleFormat.isEmpty()) {
            note.titleFormat = TextFormatter.DEFAULT_FORMAT_STRING;
        }
        return TextFormatter.parseFormat(note.titleFormat);
    }

    public TextFormatter getContentFormat() {
        if (this.note.contentFormat.isEmpty()) {
            note.contentFormat = TextFormatter.DEFAULT_FORMAT_STRING;
        }
        return TextFormatter.parseFormat(note.contentFormat);
    }

    public static DiffUtil.ItemCallback<NoteWithTags> diffCallBacks = new DiffUtil.ItemCallback<NoteWithTags>() {
        @Override
        public boolean areItemsTheSame(@NonNull NoteWithTags oldItem, @NonNull NoteWithTags newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull NoteWithTags oldItem, @NonNull NoteWithTags newItem) {
            return oldItem.equals(newItem);
        }
    };

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NoteWithTags that = (NoteWithTags) o;
        return note.equals(that.note) &&
                Objects.equals(tags, that.tags) &&
                Objects.equals(photos, that.photos) &&
                Objects.equals(audios, that.audios);
    }

    @Override
    public int hashCode() {
        return Objects.hash(note, tags);
    }

    private long getId() {
        return note.id;
    }
}
