package com.hcmus.clc18se.buggynote2.utils;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.hcmus.clc18se.buggynote2.data.NoteWithTags;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class Utils {
    static String[] DAY_OF_WEEK = {"Mon", "Tue", "Wes", "Thu", "Fri", "Sat", "Sun"};
    public static String convertLongToDateString(@NonNull Long systemTime) {
        return new SimpleDateFormat("MMM-dd-yyyy HH:mm", Locale.getDefault())
                .format(systemTime);
    }
    @SuppressLint("DefaultLocale")
    public static String getDateTimeStringFromCalender(Calendar calendar) {
        return new SimpleDateFormat("EEE, dd MMM yyyy HH:mm",Locale.getDefault()).format(calendar.getTime());
    }

    public static Calendar setReminder(Date date, Context context, NoteWithTags noteWithTags, long noteID, int repeatType) {
        // get save time
        if (!isReminderTimeValid(date)) return null;

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        // set intent to call alarm action
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, ReminderReceiver.class);

        Bundle sendData = new Bundle();


        // get note title
        String noteTitle = "";
        if (noteWithTags != null)
            noteTitle = noteWithTags.note.title;


        // put data into Intent
        intent.setAction("note_alarm");
        sendData.putLong(ReminderReceiver.NOTE_ID_KEY, noteID);
        sendData.putInt(ReminderReceiver.NOTE_DATE_REPEAT_TYPE, repeatType);
        sendData.putSerializable("calendar", calendar);
        intent.putExtras(sendData);

        // set up AlarmManager
        // TODO: bug here if ID is to large
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, (int) noteID, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        }
        //get time reminder

        return calendar;
    }

    public static boolean isReminderTimeValid(Date reminderDate) {
        return reminderDate.getTime() + 60 * 1000 > System.currentTimeMillis();
    }

    public static boolean isAlarmOfNoteExisted(Context context, long noteID){
        Intent intent= new Intent(context, ReminderReceiver.class);
        PendingIntent pendingIntentCheck = PendingIntent.getBroadcast(context, (int) noteID, intent, PendingIntent.FLAG_NO_CREATE);

        Toast.makeText(context,String.valueOf(noteID),Toast.LENGTH_LONG).show();
        return pendingIntentCheck != null;
    }
}
