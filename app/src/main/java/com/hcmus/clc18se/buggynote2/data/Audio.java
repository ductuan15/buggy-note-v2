package com.hcmus.clc18se.buggynote2.data;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "audio")
public class Audio implements Serializable {
    @PrimaryKey(autoGenerate = true)
    public long id = 0L;

    public String uri = "";

    @ColumnInfo(name = "note_id")
    public long noteId = 0L;

    public Audio(long id, String uri, long noteId) {
        this.id = id;
        this.uri = uri;
        this.noteId = noteId;
    }

    public static DiffUtil.ItemCallback<Audio> diffCallBacks = new DiffUtil.ItemCallback<Audio>() {
        @Override
        public boolean areItemsTheSame(@NonNull Audio oldItem, @NonNull Audio newItem) {
            return oldItem.id == newItem.id;
        }

        @Override
        public boolean areContentsTheSame(@NonNull Audio oldItem, @NonNull Audio newItem) {
            return oldItem.equals(newItem);
        }
    };

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Audio audio = (Audio) o;
        return id == audio.id &&
                noteId == audio.noteId &&
                uri.equals(audio.uri);
    }
}
