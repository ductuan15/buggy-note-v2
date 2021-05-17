package com.hcmus.clc18se.buggynote2.utils;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import androidx.core.app.NotificationManagerCompat;

public class SnoozeReceiver extends BroadcastReceiver {
    static  String ACTION_SNOOZE = "note_snooze";
    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().compareTo(ACTION_SNOOZE) == 0){
            long noteID = intent.getLongExtra(ReminderReceiver.NOTE_ID_TIME_KEY,0);
            ReminderMusicControl.getInstance(context).stopMusic();
            NotificationManagerCompat  notificationManager =  NotificationManagerCompat.from(context);
            notificationManager.cancel((int)noteID);
            Toast.makeText(context,"Dismiss",Toast.LENGTH_LONG).show();
        }
    }
}