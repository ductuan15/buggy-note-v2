package com.hcmus.clc18se.buggynote2.data;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Objects;

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

    public static DiffUtil.ItemCallback<Photo> diffCallBacks = new DiffUtil.ItemCallback<Photo>() {
        @Override
        public boolean areItemsTheSame(@NonNull Photo oldItem, @NonNull Photo newItem) {
            return oldItem.id == newItem.id;
        }

        @Override
        public boolean areContentsTheSame(@NonNull Photo oldItem, @NonNull Photo newItem) {
            return oldItem.equals(newItem);
        }
    };

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Photo photo = (Photo) o;
        return id == photo.id &&
                noteId == photo.noteId &&
                uri.equals(photo.uri);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, uri, noteId);
    }

    public String getUri() {
        return uri;
    }
}
