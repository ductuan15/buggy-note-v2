package com.hcmus.clc18se.buggynote2.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.hcmus.clc18se.buggynote2.fragments.MarkdownEditorFragment;
import com.hcmus.clc18se.buggynote2.fragments.MarkdownViewFragment;

public class MarkdownPagerAdapter extends FragmentStateAdapter {

    public static final int N_FRAGMENTS = 2;
    public static final int PAGE_PREVIEW = 0;
    public static final int PAGE_EDITOR = 1;

    public MarkdownPagerAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {

            // Markdown view page
            case PAGE_PREVIEW: return new MarkdownViewFragment();

            // Markdown editor page
            case PAGE_EDITOR: return new MarkdownEditorFragment();

            default: return null;
        }
    }

    @Override
    public int getItemCount() {
        return N_FRAGMENTS;
    }
}
