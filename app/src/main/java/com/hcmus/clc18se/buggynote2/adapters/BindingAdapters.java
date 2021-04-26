package com.hcmus.clc18se.buggynote2.adapters;

import android.graphics.Typeface;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.BindingAdapter;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.hcmus.clc18se.buggynote2.R;
import com.hcmus.clc18se.buggynote2.data.NoteWithTags;
import com.hcmus.clc18se.buggynote2.utils.TextFormatter;

import java.util.List;
import java.util.Random;

import static com.hcmus.clc18se.buggynote2.utils.Utils.convertLongToDateString;

public class BindingAdapters {

    @BindingAdapter("placeHolderEmoticon")
    public static void setPlaceHolderEmoticon(TextView textView, Object nothing) {
        String[] emoticons = textView.getContext().getResources().getStringArray(R.array.emoticons);
        textView.setText(
                emoticons[new Random().nextInt(emoticons.length)]
        );
    }

    @BindingAdapter("timeStampFromLong")
    public static void setTimeStampFromLong(@NonNull TextView textView, long value) {
        String text = convertLongToDateString(value);
        textView.setText(text);
    }

    @BindingAdapter("noteTitleFormat")
    public static void setNoteTitleFormat(@NonNull TextView textView, @Nullable NoteWithTags noteWithTags) {
        if (noteWithTags != null) {
            TextFormatter formatter = noteWithTags.getTitleFormat();

            Typeface typeface;
            switch (formatter.getTypefaceType()) {
                case TextFormatter.TYPEFACE_SERIF: typeface = Typeface.SERIF; break;
                case TextFormatter.TYPEFACE_SANS_SERIF: typeface = Typeface.SANS_SERIF; break;
                case TextFormatter.TYPEFACE_MONOSPACE: typeface = Typeface.MONOSPACE; break;
                default:
                    typeface = Typeface.SERIF; break;
            }

            textView.setGravity(formatter.getGravity());
            textView.setTypeface(typeface, formatter.getTypefaceStyle());

        }
    }

    @BindingAdapter("noteContentFormat")
    public static void setNoteContentFormat(@NonNull TextView textView, @Nullable NoteWithTags noteWithTags) {
        if (noteWithTags != null) {
            TextFormatter formatter = noteWithTags.getContentFormat();

            Typeface typeface;
            switch (formatter.getTypefaceType()) {
                case TextFormatter.TYPEFACE_SERIF: typeface = Typeface.SERIF; break;
                case TextFormatter.TYPEFACE_SANS_SERIF: typeface = Typeface.SANS_SERIF; break;
                case TextFormatter.TYPEFACE_MONOSPACE: typeface = Typeface.MONOSPACE; break;
                default:
                    typeface = Typeface.SERIF; break;
            }

            textView.setGravity(formatter.getGravity());
            textView.setTypeface(typeface, formatter.getTypefaceStyle());

        }
    }

    @BindingAdapter("loadNotes")
    public static void loadNotes(@NonNull RecyclerView recyclerView, @Nullable List<NoteWithTags> notes) {
        if (recyclerView.getAdapter() instanceof ListAdapter && notes != null) {
            ((NoteAdapter) recyclerView.getAdapter()).submitList(notes);
        }
    }
}