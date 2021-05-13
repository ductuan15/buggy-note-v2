package com.hcmus.clc18se.buggynote2.data;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CheckListItem {
    int idx = -1;
    boolean isChecked = false;
    String content = "";

    public boolean isChecked() {
        return isChecked;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }

    public CheckListItem(int idx, boolean isChecked, String content) {
        this.idx = idx;
        this.isChecked = isChecked;
        this.content = content.replaceAll("\n", "");
    }

    public CheckListItem(int idx, String encodedText) {
        if (encodedText != null) {
            try {
                String[] strs = encodedText.split("\n");
                // "true", "false"
                this.isChecked = Boolean.parseBoolean(strs[0]);

                this.content = strs[1];

                this.idx = idx;
            } catch (IndexOutOfBoundsException ignored) {
            }

        }
    }

    public static DiffUtil.ItemCallback<CheckListItem> diffCallback = new DiffUtil.ItemCallback<CheckListItem>() {
        @Override
        public boolean areItemsTheSame(@NonNull CheckListItem oldItem, @NonNull CheckListItem newItem) {
            return oldItem.idx == newItem.idx;
        }

        @Override
        public boolean areContentsTheSame(@NonNull CheckListItem oldItem, @NonNull CheckListItem newItem) {
            return oldItem.equals(newItem);
        }
    };

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CheckListItem that = (CheckListItem) o;
        return idx == that.idx &&
                isChecked == that.isChecked &&
                Objects.equals(content, that.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idx, isChecked, content);
    }

    public static List<CheckListItem> compileFromNoteContent(String content) {
        if (content == null || content.isEmpty()) return new ArrayList<>();

        // first row "true", false
        // second row content
        String[] strs = content.split("\n");
        List<CheckListItem> list = new ArrayList<>();
        for (int i = 0; i < strs.length; i += 2) {
            if (i + 1 < strs.length) {
                list.add(new CheckListItem(i / 2, strs[i] + '\n' + strs[i + 1]));
            }
        }
        return list;
    }

    public static String toNoteContent(List<CheckListItem> items) {
        if (items == null) return "";

        StringBuilder builder = new StringBuilder();
        for (CheckListItem item :
                items) {
            builder.append(item.isChecked)
                    .append('\n')
                    .append(item.content.replaceAll("\n", ""))
                    .append('\n');
        }

        return builder.toString();
    }

    public static final char BALLOT_BOX = '☐';
    public static final char CHECK_BOX = '☑';

    public static String toReadableString(List<CheckListItem> items) {
        if (items == null) return "";

        StringBuilder builder = new StringBuilder();
        for (CheckListItem item :
                items) {
            builder.append(item.isChecked ? CHECK_BOX: BALLOT_BOX)
                    .append('\t')
                    .append(item.content.replaceAll("\n", ""))
                    .append('\n');
        }

        return builder.toString();
    }
}