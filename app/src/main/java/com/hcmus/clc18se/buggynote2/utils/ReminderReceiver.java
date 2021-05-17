package com.hcmus.clc18se.buggynote2.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.hcmus.clc18se.buggynote2.AlarmActivity;
import com.hcmus.clc18se.buggynote2.R;

import java.util.Calendar;


public class ReminderReceiver extends BroadcastReceiver {
    long noteID;
    String noteTitle;
    String reminderDateTimeString;

    public final static String NOTE_ID_KEY = "note_id";
    public final static String NOTE_TITLE_KEY = "note_title";
    public final static String NOTE_DATE_TIME_KEY = "note_datetime";

    final static String CHANNEL_ID = "Note_reminder_id";
    final static String ACTION_REMINDER = "note_alarm";

    NotificationManagerCompat notificationManager;
    NotificationCompat.Builder builder;

    NotificationCompat.Action dismissAction;
    NotificationCompat.Action noteDetailAction;


    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().compareTo(ACTION_REMINDER) == 0) {
            Bundle receivedData = intent.getExtras();
            getReceivedData(receivedData);

            notificationManager = NotificationManagerCompat.from(context);

            builder = new NotificationCompat.Builder(context, CHANNEL_ID);

            setFullScreenIntent(context);
            setUpNotificationActions(context);
            setUpNotification(context);


        }
    }

    private void getReceivedData(Bundle bundle) {
        noteID = bundle.getLong("note_id");
        noteTitle = bundle.getString("note_title");
        Calendar calendar = (Calendar) bundle.getSerializable("calendar");
        reminderDateTimeString = Utils.getDateTimeStringFromCalender(calendar);
    }

    public void setChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            CharSequence name = "Note notification";
            String description = "Note notification";
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription(description);
            channel.enableLights(true);
            channel.setShowBadge(true);
            channel.setVibrationPattern(new long[5]);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public void setUpNotificationActions(Context context) {
        Intent dismissIntent = new Intent(context, ReminderActionReceiver.class);
        dismissIntent.setAction(ReminderActionReceiver.ACTION_DISMISS);
        dismissIntent.putExtra("note_id", noteID);
        PendingIntent dismissPendingIntent =
                PendingIntent.getBroadcast(context, (int) noteID, dismissIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        dismissAction = new NotificationCompat.Action.Builder(R.drawable.ic_baseline_cancel_24,
                "Dismiss", dismissPendingIntent)
                .build();

        Intent noteDetailIntent = new Intent(context, ReminderActionReceiver.class);
        noteDetailIntent.setAction(ReminderActionReceiver.ACTION_DETAIL);
        noteDetailIntent.putExtra("note_id", noteID);
        PendingIntent noteDetailPendingIntent =
                PendingIntent.getBroadcast(context, (int) noteID, noteDetailIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        noteDetailAction = new NotificationCompat.Action.Builder(R.drawable.ic_baseline_sticky_note_2_24,
                "Detail", noteDetailPendingIntent)
                .build();

    }

    public void setUpNotification(Context context) {

        builder.setSmallIcon(R.drawable.ic_baseline_mode_edit_24)
                .setContentTitle(noteTitle)
                .setContentText(reminderDateTimeString)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .addAction(dismissAction)
                .addAction(noteDetailAction);
        setChannel();
        builder.setAutoCancel(true);
        builder.setOngoing(true);
        notificationManager.notify((int) noteID, builder.build());


        ReminderMusicControl.getInstance(context).playMusic(context);
    }

    public void setFullScreenIntent(Context context) {
        Intent fullScreenIntent = new Intent(context, AlarmActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString(NOTE_TITLE_KEY, noteTitle);
        bundle.putString(NOTE_DATE_TIME_KEY, reminderDateTimeString);
        bundle.putLong(NOTE_ID_KEY, noteID);

        fullScreenIntent.putExtras(bundle);
        PendingIntent fullScreenPendingIntent = PendingIntent.getActivity(context, (int) noteID, fullScreenIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setFullScreenIntent(fullScreenPendingIntent, true);
    }

}
