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
import android.widget.Toast;


public class AlarmActivity extends AppCompatActivity implements View.OnClickListener {
    Button button;
    Ringtone r;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);
        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        r = RingtoneManager.getRingtone(getApplicationContext(), notification);
        r.play();
       button = findViewById(R.id.stop_alarm);
        button.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if(v == button){
            r.stop();
        }
    }
}