package com.hcmus.clc18se.buggynote2.utils;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.os.PowerManager;
import android.widget.Toast;

import com.hcmus.clc18se.buggynote2.R;

public class ReminderMusicControl {
    private static ReminderMusicControl sInstance;
    private  MediaPlayer mMediaPlayer;

    public static ReminderMusicControl getInstance(Context context) {
        if(sInstance == null){
            sInstance = new ReminderMusicControl();
        }
        return sInstance;
    }

    public void playMusic(Context context) {
        if(mMediaPlayer == null){
            mMediaPlayer = MediaPlayer.create(context, R.raw.rick_roll);
        }
        mMediaPlayer.start();
    }

    public void stopMusic() {
        if(mMediaPlayer != null) {
            mMediaPlayer.stop();
        }
    }
}
