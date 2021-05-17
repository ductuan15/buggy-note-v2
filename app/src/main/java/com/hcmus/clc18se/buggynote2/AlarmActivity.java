package com.hcmus.clc18se.buggynote2;

import androidx.appcompat.app.AppCompatActivity;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.chip.Chip;
import com.hcmus.clc18se.buggynote2.utils.ReminderMusicControl;
import com.hcmus.clc18se.buggynote2.utils.ReminderReceiver;
import com.hcmus.clc18se.buggynote2.utils.SnoozeReceiver;

import org.w3c.dom.Text;


public class AlarmActivity extends AppCompatActivity implements View.OnClickListener {
    Chip button;
    String noteTitle = "";
    String noteDateTime = "";
    Long noteID ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_alarm);
        getData();
        bindingView();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);


    }

    @Override
    public void onClick(View v) {
        if (v == button) {
            Intent intent = new Intent(getBaseContext(), SnoozeReceiver.class);
            Bundle bundle = new Bundle();
            bundle.putLong(ReminderReceiver.NOTE_ID_TIME_KEY,noteID);
            sendBroadcast(intent);
            Toast.makeText(getBaseContext(), "Dismissed", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    public void bindingView() {
        button = findViewById(R.id.stop_alarm);
        button.setOnClickListener(this);

        TextView reminderTitleTextView = findViewById(R.id.title_reminder);
        TextView dateTimeReminderTextView = findViewById(R.id.datetime_reminder);

        reminderTitleTextView.setText(noteTitle);
        dateTimeReminderTextView.setText(noteDateTime);
    }

    public void getData() {
        Intent intent = getIntent();
        if (intent != null) {
            Bundle bundle = intent.getExtras();
            noteTitle = bundle.getString(ReminderReceiver.NOTE_TITLE_KEY);
            noteDateTime = bundle.getString(ReminderReceiver.NOTE_DATE_TIME_KEY);
            noteID = bundle.getLong(ReminderReceiver.NOTE_ID_TIME_KEY);
        }
    }
}