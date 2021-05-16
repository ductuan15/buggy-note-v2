package com.hcmus.clc18se.buggynote2;

import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.chip.Chip;
import com.hcmus.clc18se.buggynote2.utils.ReminderMusicControl;
import com.hcmus.clc18se.buggynote2.utils.ReminderReceiver;

import org.w3c.dom.Text;


public class AlarmActivity extends AppCompatActivity implements View.OnClickListener {
    Chip button;
    String noteTitle = "";
    String noteDateTime= "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_alarm);
        ReminderMusicControl.getInstance(this).playMusic(this);
        getData();
        bindingView();


    }

    @Override
    public void onClick(View v) {
        if (v == button) {
            ReminderMusicControl.getInstance(this).stopMusic();
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

    public void getData(){
        Intent intent = getIntent();
        if(intent!= null){
            Bundle bundle = intent.getExtras();
            noteTitle = bundle.getString(ReminderReceiver.NOTE_TITLE_KEY);
            noteDateTime = bundle.getString(ReminderReceiver.NOTE_DATE_TIME_KEY);
        }
    }
}