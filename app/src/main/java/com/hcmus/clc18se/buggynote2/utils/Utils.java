package com.hcmus.clc18se.buggynote2.utils;

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
    public static String setReminder(Date date, Context context, NoteWithTags noteWithTags, long noteID){

        // get save time
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
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
        sendData.putLong("note_id", noteID);
        sendData.putSerializable("calendar", calendar);
        sendData.putString("note_title", noteTitle);
        intent.putExtras(sendData);

        // set up AlarmManager
        // TODO: bug here if ID is to large
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, (int)noteID, intent, PendingIntent.FLAG_ONE_SHOT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        }
        //get time reminder
        String reminderDateTimeString = Utils.getDateTimeStringFromCalender(calendar);
        return reminderDateTimeString;
    }
}
