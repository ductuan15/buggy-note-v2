package com.hcmus.clc18se.buggynote2.adapters;

import android.text.Editable;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.hcmus.clc18se.buggynote2.data.NoteWithTags;
import com.hcmus.clc18se.buggynote2.fragments.MarkdownEditorFragment;
import com.hcmus.clc18se.buggynote2.fragments.MarkdownViewFragment;

public class MarkdownPagerAdapter extends FragmentStateAdapter {

    public static final int N_FRAGMENTS = 2;
    public static final int PAGE_PREVIEW = 0;
    public static final int PAGE_EDITOR = 1;

    NoteWithTags noteWithTags;
    Fragment[] fragments;

    public MarkdownPagerAdapter(@NonNull Fragment fragment, NoteWithTags noteWithTags) {
        super(fragment);
        this.noteWithTags = noteWithTags;
        fragments = new Fragment[]{
                new MarkdownViewFragment(), new MarkdownEditorFragment()
        };

        ((MarkdownViewFragment) fragments[PAGE_PREVIEW]).noteWithTags = noteWithTags;
        ((MarkdownEditorFragment) fragments[PAGE_EDITOR]).noteWithTags = noteWithTags;
    }

    public Editable getEditTextContent() throws NullPointerException {
        return ((MarkdownEditorFragment) fragments[PAGE_EDITOR]).binding.noteContent.getText();
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {

            // Markdown view page
            case PAGE_PREVIEW: {
                return fragments[PAGE_PREVIEW];
            }

            // Markdown editor page
            case PAGE_EDITOR: {
                return fragments[PAGE_EDITOR];
            }

            default:
                return null;
        }
    }

    @Override
    public int getItemCount() {
        return N_FRAGMENTS;
    }
}