package com.hcmus.clc18se.buggynote2.utils;

import androidx.annotation.NonNull;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class Utils {
    public static String convertLongToDateString(@NonNull Long systemTime) {
        return new SimpleDateFormat("MMM-dd-yyyy HH:mm", Locale.getDefault())
                .format(systemTime);
    }
    public static String getDateTimeStringFromCalender(Calendar calendar){
        int isAM =  calendar.get(Calendar.AM_PM);
        String AM_PM = isAM == 0 ? "AM": "PM";
        return calendar.get(Calendar.DATE) + "/"
                + calendar.get(Calendar.MONTH) + "/"
                + calendar.get(Calendar.YEAR) + " "
                + calendar.get(Calendar.HOUR) + ":"
                + calendar.get(Calendar.MINUTE)
                + AM_PM;
    }
}
