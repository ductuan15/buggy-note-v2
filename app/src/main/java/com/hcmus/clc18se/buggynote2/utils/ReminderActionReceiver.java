package com.hcmus.clc18se.buggynote2.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import androidx.core.app.NotificationManagerCompat;

public class ReminderActionReceiver extends BroadcastReceiver {
    public static  String ACTION_DISMISS = "note_dismiss";
    public static  String ACTION_DETAIL = "note_detail";

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().compareTo(ACTION_DISMISS) == 0){
            long noteID = intent.getLongExtra(ReminderReceiver.NOTE_ID_KEY,0);
            ReminderMusicControl.getInstance(context).stopMusic();
            NotificationManagerCompat  notificationManager =  NotificationManagerCompat.from(context);
            notificationManager.cancel((int)noteID);
            Toast.makeText(context,"Dismiss",Toast.LENGTH_LONG).show();
        }else if(intent.getAction().compareTo(ACTION_DETAIL) == 0){
            //TODO: cần thận rick_roll không ngừng
            long noteID = intent.getLongExtra(ReminderReceiver.NOTE_ID_KEY,0);
            Toast.makeText(context,"Detail",Toast.LENGTH_LONG).show();
        }
    }
}