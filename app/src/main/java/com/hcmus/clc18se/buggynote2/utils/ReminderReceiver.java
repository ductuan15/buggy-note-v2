package com.hcmus.clc18se.buggynote2.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.text.style.BackgroundColorSpan;
import android.text.style.BulletSpan;
import android.text.style.QuoteSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;
import android.text.style.TypefaceSpan;
import android.widget.RemoteViews;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.hcmus.clc18se.buggynote2.AlarmActivity;
import com.hcmus.clc18se.buggynote2.R;
import com.hcmus.clc18se.buggynote2.data.CheckListItem;
import com.hcmus.clc18se.buggynote2.data.Note;
import com.hcmus.clc18se.buggynote2.database.BuggyNoteDao;
import com.hcmus.clc18se.buggynote2.database.BuggyNoteDatabase;

import org.commonmark.ext.gfm.strikethrough.Strikethrough;
import org.commonmark.node.BlockQuote;
import org.commonmark.node.Code;
import org.commonmark.node.Emphasis;
import org.commonmark.node.Heading;
import org.commonmark.node.ListItem;
import org.commonmark.node.StrongEmphasis;
import org.jetbrains.annotations.NotNull;

import java.util.Calendar;
import java.util.concurrent.ExecutionException;

import io.noties.markwon.AbstractMarkwonPlugin;
import io.noties.markwon.Markwon;
import io.noties.markwon.MarkwonSpansFactory;
import io.noties.markwon.core.CoreProps;
import io.noties.markwon.core.MarkwonTheme;
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin;
import io.noties.markwon.ext.tables.TablePlugin;


public class ReminderReceiver extends BroadcastReceiver {
    long noteID;
    String noteTitle;
    String noteContent;
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
            BuggyNoteDao buggyNoteDao = BuggyNoteDatabase.getInstance(context).buggyNoteDatabaseDao();
            try {
                Note note = BuggyNoteDatabase.databaseWriteExecutor.submit(() -> {
                    return buggyNoteDao.getPlainNoteFromId(noteID);
                }).get();

                notificationManager = NotificationManagerCompat.from(context);
                builder = new NotificationCompat.Builder(context, CHANNEL_ID);

                setFullScreenIntent(context);
                setUpNotificationActions(context);
                setUpNotification(context, note);


            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }

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

    public void setUpNotification(Context context, Note note) {

        if (note != null) {
            noteTitle = note.title;
            if (note.isCheckList()) {
                noteContent = CheckListItem.toReadableString(CheckListItem.compileFromNoteContent(note.noteContent));
            }

            if (note.isMarkdown()) {



            } else {
               builder.setStyle(new NotificationCompat.BigTextStyle().bigText(noteContent));
            }
            builder.setSmallIcon(R.drawable.ic_baseline_mode_edit_24)
                    .setContentTitle(noteTitle)
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
    }

    @NotNull
    private Markwon makeMarkwonInstance(Context context, float[] headingSizes, int bulletGapWidth) {
        Markwon markwon = Markwon.builder(context)
                .usePlugin(StrikethroughPlugin.create())
                .usePlugin(TablePlugin.create(context))
                .usePlugin(new AbstractMarkwonPlugin() {
                    @Override
                    public void configureSpansFactory(@NonNull MarkwonSpansFactory.Builder builder) {
                        builder
                                .setFactory(Heading.class, (configuration, props) -> new Object[]{
                                        new StyleSpan(Typeface.BOLD),
                                        new RelativeSizeSpan(headingSizes[CoreProps.HEADING_LEVEL.require(props) - 1])
                                })
                                .setFactory(StrongEmphasis.class, (configuration, props) -> new StyleSpan(Typeface.BOLD))
                                .setFactory(Emphasis.class, (configuration, props) -> new StyleSpan(Typeface.ITALIC))
                                .setFactory(Code.class, (configuration, props) -> new Object[]{
                                        new BackgroundColorSpan(Color.GRAY),
                                        new TypefaceSpan("monospace")
                                })
                                .setFactory(Strikethrough.class, (configuration, props) -> new StrikethroughSpan())
                                .setFactory(ListItem.class, (configuration, props) -> new BulletSpan(bulletGapWidth))
                                .setFactory(BlockQuote.class, (configuration, props) -> new QuoteSpan());
                    }
                })
                .build();
        return markwon;
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
