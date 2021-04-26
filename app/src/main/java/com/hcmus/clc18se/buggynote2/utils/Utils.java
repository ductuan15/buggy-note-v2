package com.hcmus.clc18se.buggynote2.utils;

import androidx.annotation.NonNull;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class Utils {
    public static String convertLongToDateString(@NonNull Long systemTime) {
        return new SimpleDateFormat("MMM-dd-yyyy HH:mm", Locale.getDefault())
                .format(systemTime);
    }
}
