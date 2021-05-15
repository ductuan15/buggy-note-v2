package com.hcmus.clc18se.buggynote2.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.hcmus.clc18se.buggynote2.AlarmActivity;
import com.hcmus.clc18se.buggynote2.R;

import java.util.Calendar;


public class ReminderReceiver extends BroadcastReceiver {
    long noteID;
    String noteTitle;
    String reminderDateTimeString;
    NotificationManagerCompat notificationManager;
    String CHANNEL_ID = "Note_reminder_id";
    static String ACTION_REMINDER = "note_alarm";


    NotificationCompat.Action snoozeAction;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().compareTo(ACTION_REMINDER) == 0) {
            Bundle receivedData = intent.getExtras();
            getReceivedData(receivedData);

            notificationManager = NotificationManagerCompat.from(context);

            setUpNotificationActions(context);
            setUpNotification(context);
            ReminderMusicControl.getInstance(context).playMusic(context);

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
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name,NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription(description);
            channel.enableLights(true);
            channel.setShowBadge(true);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public void setUpNotificationActions(Context context) {
        Intent snoozeIntent = new Intent(context, SnoozeReceiver.class);
        snoozeIntent.setAction(SnoozeReceiver.ACTION_SNOOZE);
        snoozeIntent.putExtra("note_id",noteID);
        PendingIntent snoozePendingIntent =
                PendingIntent.getBroadcast(context, (int) noteID, snoozeIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        snoozeAction = new NotificationCompat.Action.Builder(R.drawable.ic_archive,
                "Snooze", snoozePendingIntent)
                .build();
    }

    public void setUpNotification(Context context) {
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        Intent fullScreenIntent = new Intent(context, AlarmActivity.class);
        PendingIntent fullScreenPendingIntent = PendingIntent.getActivity(context, 0,
                fullScreenIntent, PendingIntent.FLAG_ONE_SHOT);


        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_archive)
                .setContentTitle(noteTitle)
                .setContentText(reminderDateTimeString)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setFullScreenIntent(fullScreenPendingIntent, true)
                .setVibrate(new long[0])
                .addAction(snoozeAction);
        setChannel();
        builder.setSound(alarmSound);
        notificationManager.notify((int) noteID, builder.build());
    }

}