package com.hcmus.clc18se.buggynote2.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.core.app.NotificationManagerCompat;

import com.hcmus.clc18se.buggynote2.BuggyNoteActivity;

public class ReminderActionReceiver extends BroadcastReceiver {
    public static String ACTION_DISMISS = "note_dismiss";
    public static String ACTION_DETAIL = "note_detail";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().compareTo(ACTION_DISMISS) == 0) {
            long noteID = intent.getLongExtra(ReminderReceiver.NOTE_ID_KEY, 0);
            ReminderMusicControl.getInstance(context).stopMusic();
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            notificationManager.cancel((int) noteID);
        } else if (intent.getAction().compareTo(ACTION_DETAIL) == 0) {
            //TODO: cần thận rick_roll không ngừng
            long noteID = intent.getLongExtra(ReminderReceiver.NOTE_ID_KEY, 0);
            Bundle extras = new Bundle();
            extras.putLong(BuggyNoteActivity.EXTRA_NOTE_ID, noteID);

            Intent toNoteDetails = new Intent(context, BuggyNoteActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    .putExtras(extras);

            context.startActivity(toNoteDetails);
        }
    }
}