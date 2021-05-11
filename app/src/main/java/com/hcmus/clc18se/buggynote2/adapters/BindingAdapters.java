package com.hcmus.clc18se.buggynote2.adapters;

import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.BindingAdapter;
import androidx.recyclerview.widget.ConcatAdapter;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.hcmus.clc18se.buggynote2.R;
import com.hcmus.clc18se.buggynote2.data.CheckListItem;
import com.hcmus.clc18se.buggynote2.data.Note;
import com.hcmus.clc18se.buggynote2.data.NoteWithTags;
import com.hcmus.clc18se.buggynote2.data.Tag;
import com.hcmus.clc18se.buggynote2.databinding.TagChipBinding;
import com.hcmus.clc18se.buggynote2.utils.TextFormatter;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import io.noties.markwon.Markwon;
import timber.log.Timber;

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

    @BindingAdapter("loadFilterTags")
    public static void loadFilterTags(@NonNull RecyclerView recyclerView,
                                      @Nullable List<Tag> tags) {
        if (recyclerView.getAdapter() instanceof TagFilterAdapter && tags != null) {
            ((TagFilterAdapter) recyclerView.getAdapter()).submitList(tags);
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

    @BindingAdapter(value = {"pinnedNotes", "unpinnedNotes"}, requireAll = false)
    public static void loadNotes2(@NonNull RecyclerView recyclerView,
                                  @Nullable List<NoteWithTags> pinnedNotes,
                                  @Nullable List<NoteWithTags> unpinnedNotes) {
        if (!(recyclerView.getAdapter() instanceof ConcatAdapter)) {
            Timber.w("Use the concat adapter to load 2 note lists");
            return;
        }

        List<?> adapters = ((ConcatAdapter) recyclerView.getAdapter()).getAdapters();
        if (adapters.isEmpty()) {
            Timber.w("ConcatAdapter does not contain any NoteAdapter");
        }

        if (pinnedNotes != null) {
            ((NoteAdapter) adapters.get(NoteAdapter.PINNED_POSITION)).submitList(pinnedNotes);
        }
        if (unpinnedNotes != null) {
            ((NoteAdapter) adapters.get(NoteAdapter.UNPINNED_POSITION)).submitList(unpinnedNotes);
        }
    }

    @BindingAdapter("visibilityWhenNoteInTrash")
    public static void setVisibilityWhenNoteInTrash(@NonNull View view,
                                                    @Nullable Long removingDate) {
        if (removingDate != null) {
            view.setVisibility(View.VISIBLE);
        } else {
            view.setVisibility(View.GONE);
        }
    }

    @BindingAdapter("removeDateWarning")
    public static void setTextRemoveDateWarning(@NonNull TextView textView,
                                                @Nullable Long removingDate) {
        if (removingDate != null) {
            String date = new SimpleDateFormat("MMM/dd/yyyy", Locale.getDefault())
                    .format(removingDate);

            textView.setText(textView.getContext().getString(
                    R.string.removing_date_warining, date
            ));
        }
    }

    @BindingAdapter("backgroundColor")
    public static void setBackgroundColor(@NonNull View view,
                                          @Nullable @ColorInt Integer color) {
        if (color != null) {
            view.setBackgroundColor(color);
        }
    }

    @BindingAdapter("visibleWhenNoteIsCheckList")
    public static void setVisibleWhenNoteIsCheckList(@NonNull View view,
                                                     @Nullable Note note) {

        setVisibilityBasedOnNoteType(view,
                note,
                Note::isCheckList
        );

    }

    @BindingAdapter("visibleWhenNoteIsNotCheckList")
    public static void setVisibleWhenNoteIsNotCheckList(@NonNull View view,
                                                        @Nullable Note note) {
        setVisibilityBasedOnNoteType(view,
                note,
                n -> !n.isCheckList()
        );

    }

    @BindingAdapter("visibleWhenNoteIsPlainText")
    public static void setVisibleWhenNoteIsPlainText(@NonNull View view,
                                                     @Nullable Note note) {
        setVisibilityBasedOnNoteType(view,
                note,
                Note::isPlainText
        );

    }

    @BindingAdapter("visibleWhenNoteIsMarkdown")
    public static void setVisibleWhenNoteIsMarkdown(@NonNull View view,
                                                    @Nullable Note note) {
        setVisibilityBasedOnNoteType(view,
                note,
                Note::isMarkdown
        );
    }

    public interface NoteValidationSAM {
        boolean validateNoteType(Note note);
    }

    public static void setVisibilityBasedOnNoteType(@NonNull View view,
                                                    @Nullable Note note,
                                                    NoteValidationSAM noteValidateFunc
    ) {
        int visibility;
        if (note != null) {
            visibility = noteValidateFunc.validateNoteType(note) ? View.VISIBLE : View.GONE;
        } else {
            visibility = View.GONE;
        }
        view.setVisibility(visibility);

    }

    @BindingAdapter("displayCheckList")
    public static void displayCheckList(@NonNull RecyclerView recyclerView,
                                        @Nullable Note note
    ) {
        if (note != null && note.isCheckList()) {
            if (recyclerView.getAdapter() == null ||
                    !(recyclerView.getAdapter() instanceof CheckListPreviewAdapter)) {
                recyclerView.setAdapter(new CheckListPreviewAdapter());
            }

            ((CheckListPreviewAdapter) recyclerView.getAdapter()).submitList(
                    CheckListItem.compileFromNoteContent(note.getNoteContent())
            );
        }
    }

    @BindingAdapter(value = {"markwon", "noteContent"})
    public static void setMarkdown(@NonNull TextView textView,
                                   @Nullable Markwon markwon,
                                   @Nullable String noteContent) {
        if (markwon != null && noteContent != null) {
            markwon.setMarkdown(textView, noteContent);
        } else if (noteContent != null) {
            textView.setText(noteContent);
        }
    }
}