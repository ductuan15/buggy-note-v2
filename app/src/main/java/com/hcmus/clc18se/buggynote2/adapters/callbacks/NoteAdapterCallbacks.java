package com.hcmus.clc18se.buggynote2.adapters.callbacks;

import com.hcmus.clc18se.buggynote2.data.NoteWithTags;

import java.util.List;

public interface NoteAdapterCallbacks {

    void onClick(NoteWithTags note);

    boolean onMultipleSelect(NoteWithTags note);

    void onPostReordered(List<NoteWithTags> notes);

    void onItemSwiped(NoteWithTags note);
}
