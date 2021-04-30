package com.hcmus.clc18se.buggynote2.adapters;

import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.BindingAdapter;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.hcmus.clc18se.buggynote2.R;
import com.hcmus.clc18se.buggynote2.data.NoteWithTags;
import com.hcmus.clc18se.buggynote2.data.Tag;
import com.hcmus.clc18se.buggynote2.databinding.TagChipBinding;
import com.hcmus.clc18se.buggynote2.utils.TextFormatter;

import java.util.List;
import java.util.Random;

import static com.hcmus.clc18se.buggynote2.utils.Utils.convertLongToDateString;

public class BindingAdapters {

    @BindingAdapter("placeholderVisibility")
    public static void setPlaceholderVisibility(@NonNull ViewGroup viewGroup,
                                                @Nullable List<?> list) {
        if (list != null && list.isEmpty()) {
            viewGroup.setVisibility(View.VISIBLE);
        } else {
            viewGroup.setVisibility(View.GONE);
        }
    }

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
    public static void setNoteTitleFormat(@NonNull TextView textView,
                                          @Nullable NoteWithTags noteWithTags) {
        if (noteWithTags != null) {
            TextFormatter formatter = noteWithTags.getTitleFormat();

            Typeface typeface;
            switch (formatter.getTypefaceType()) {
                case TextFormatter.TYPEFACE_SANS_SERIF:
                    typeface = Typeface.SANS_SERIF;
                    break;
                case TextFormatter.TYPEFACE_MONOSPACE:
                    typeface = Typeface.MONOSPACE;
                    break;
                default:
                    typeface = Typeface.SERIF;
                    break;
            }

            textView.setGravity(formatter.getGravity());
            textView.setTypeface(typeface, formatter.getTypefaceStyle());

        }
    }

    @BindingAdapter("noteContentFormat")
    public static void setNoteContentFormat(@NonNull TextView textView,
                                            @Nullable NoteWithTags noteWithTags) {
        if (noteWithTags != null) {
            TextFormatter formatter = noteWithTags.getContentFormat();

            Typeface typeface;
            switch (formatter.getTypefaceType()) {
                case TextFormatter.TYPEFACE_SERIF:
                    typeface = Typeface.SERIF;
                    break;
                case TextFormatter.TYPEFACE_SANS_SERIF:
                    typeface = Typeface.SANS_SERIF;
                    break;
                case TextFormatter.TYPEFACE_MONOSPACE:
                    typeface = Typeface.MONOSPACE;
                    break;
                default:
                    typeface = Typeface.SERIF;
                    break;
            }

            textView.setGravity(formatter.getGravity());
            textView.setTypeface(typeface, formatter.getTypefaceStyle());

        }
    }

    @BindingAdapter("loadNotes")
    public static void loadNotes(@NonNull RecyclerView recyclerView,
                                 @Nullable List<NoteWithTags> notes) {
        if (recyclerView.getAdapter() instanceof ListAdapter && notes != null) {
            ((NoteAdapter) recyclerView.getAdapter()).submitList(notes);
        }
    }

    @BindingAdapter("loadTags")
    public static void loadTags(@NonNull RecyclerView recyclerView,
                                @Nullable List<Tag> tags) {
        if (recyclerView.getAdapter() instanceof TagsAdapter && tags != null) {
            ((TagsAdapter) recyclerView.getAdapter()).submitList(tags);
        }
    }

    @BindingAdapter("loadSelectableTags")
    public static void loadSelectableTags(@NonNull RecyclerView recyclerView,
                                          @Nullable List<Tag> tags) {
        if (recyclerView.getAdapter() instanceof TagSelectionAdapter && tags != null) {
            ((TagSelectionAdapter) recyclerView.getAdapter()).submitList(tags);
        }
    }

    @BindingAdapter(value = {"loadTagList", "chipLimit", "setOnClickToChips"}, requireAll = false)
    public static void setTags(@NonNull ChipGroup chipGroup,
                               @Nullable List<Tag> tags,
                               @Nullable Integer limit,
                               @Nullable View.OnClickListener clickListener) {

        if (tags != null) {
            chipGroup.removeAllViewsInLayout();
            chipGroup.invalidate();
            chipGroup.requestLayout();

            int maximumChipAllowed = Integer.MAX_VALUE;
            if (limit != null && limit > 0) {
                maximumChipAllowed = limit;
            }

            for (int i = 0; i < tags.size(); ++i) {
                if (i == maximumChipAllowed) {
                    break;
                }
                Chip chip = (Chip) TagChipBinding.inflate(LayoutInflater.from(chipGroup.getContext()),
                        chipGroup,
                        true).getRoot();
                if (i == maximumChipAllowed - 1 && (tags.size() - maximumChipAllowed) > 0) {
                    String nChipRemaining = (tags.size() - maximumChipAllowed + 1) + "+";
                    chip.setText(nChipRemaining);
                } else {
                    chip.setText(tags.get(i).getName());
                }
                if (clickListener != null) {
                    chip.setOnClickListener(clickListener);
                }
            }
        }
    }
}