package com.hcmus.clc18se.buggynote2.utils;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.hcmus.clc18se.buggynote2.AlarmActivity;
import com.hcmus.clc18se.buggynote2.R;
import com.hcmus.clc18se.buggynote2.data.CheckListItem;
import com.hcmus.clc18se.buggynote2.data.Note;
import com.hcmus.clc18se.buggynote2.data.NoteWithTags;
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
import java.util.Date;
import java.util.concurrent.ExecutionException;

import io.noties.markwon.AbstractMarkwonPlugin;
import io.noties.markwon.Markwon;
import io.noties.markwon.MarkwonSpansFactory;
import io.noties.markwon.core.CoreProps;
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin;
import io.noties.markwon.ext.tables.TablePlugin;


public class ReminderReceiver extends BroadcastReceiver {
    long noteID;
    String noteTitle;
    String noteContent;
    int reminderRepeatType;
    String reminderDateTimeString;
    Calendar calendar;
    NoteWithTags noteWithTags;

    public final static String NOTE_ID_KEY = "note_id";
    public final static String NOTE_TITLE_KEY = "note_title";
    public final static String NOTE_DATE_TIME_KEY = "note_datetime";
    public final static String NOTE_DATE_REPEAT_TYPE = "note_repeat_type";

    public final static int REMINDER_REPEAT_DAY = 0;
    public final static int REMINDER_REPEAT_WEEK = 1;
    public final static int REMINDER_REPEAT_MONTH = 2;
    public final static int REMINDER_REPEAT_YEAR = 3;
    public final static int REMINDER_REPEAT_NONE = 4;


    final static String CHANNEL_ID = "Note_reminder_id";
    public final static String ACTION_REMINDER = "note_alarm";

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
                noteWithTags = BuggyNoteDatabase.databaseWriteExecutor.submit(() -> buggyNoteDao.getNoteFromId(noteID)).get();

                notificationManager = NotificationManagerCompat.from(context);
                builder = new NotificationCompat.Builder(context, CHANNEL_ID);

                setFullScreenIntent(context);
                setUpNotificationActions(context);
                setUpNotification(context, noteWithTags);

                updateReminderTime(context);


            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void updateReminderTime(Context context) {
        Calendar updatedReminderCalendar = (Calendar) calendar.clone();
        switch (reminderRepeatType) {
            case REMINDER_REPEAT_DAY:
                updatedReminderCalendar.add(Calendar.DAY_OF_MONTH, 1);
                break;
            case REMINDER_REPEAT_WEEK:
                updatedReminderCalendar.add(Calendar.WEEK_OF_MONTH, 1);
                break;
            case REMINDER_REPEAT_MONTH:
                updatedReminderCalendar.add(Calendar.MONTH, 1);
                break;
            case REMINDER_REPEAT_YEAR:
                updatedReminderCalendar.add(Calendar.YEAR, 1);
                break;
            default:
                return;
        }
        Date updatedReminderDate = updatedReminderCalendar.getTime();
        updatedReminderCalendar = Utils.setReminder(updatedReminderDate, context, noteWithTags, noteID, reminderRepeatType);
        if (updatedReminderCalendar != null) {
            SharedPreferences sharedPreferences = context.getSharedPreferences("REMINDER", Context.MODE_PRIVATE);
            long a = sharedPreferences.getLong(String.valueOf(noteID), 100);

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putLong(String.valueOf(noteID), updatedReminderDate.getTime())
                    .apply();
            long b = sharedPreferences.getLong(String.valueOf(noteID), 100);
            Toast.makeText(context, "Reset time's reminder to: " + Utils.getDateTimeStringFromCalender(updatedReminderCalendar), Toast.LENGTH_LONG).show();
        }
    }

    private void getReceivedData(Bundle bundle) {
        noteID = bundle.getLong(NOTE_ID_KEY);
        reminderRepeatType = bundle.getInt(NOTE_DATE_REPEAT_TYPE);
        calendar = (Calendar) bundle.getSerializable("calendar");
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
        dismissIntent.putExtra(NOTE_ID_KEY, noteID);
        PendingIntent dismissPendingIntent =
                PendingIntent.getBroadcast(context, (int) noteID, dismissIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        dismissAction = new NotificationCompat.Action.Builder(R.drawable.ic_baseline_cancel_24,
                "Dismiss", dismissPendingIntent)
                .build();

        Intent noteDetailIntent = new Intent(context, ReminderActionReceiver.class);
        noteDetailIntent.setAction(ReminderActionReceiver.ACTION_DETAIL);
        noteDetailIntent.putExtra(NOTE_ID_KEY, noteID);

        PendingIntent noteDetailPendingIntent =
                PendingIntent.getBroadcast(context, (int) noteID, noteDetailIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        noteDetailAction = new NotificationCompat.Action.Builder(R.drawable.ic_baseline_sticky_note_2_24,
                "Detail", noteDetailPendingIntent)
                .build();

    }

    public void setUpNotification(Context context, NoteWithTags noteWithTags) {
        Note note = noteWithTags.note;
        if (note != null) {
            noteTitle = note.title;
            noteContent = note.noteContent;
            if (note.isCheckList()) {
                noteContent = CheckListItem.toReadableString(CheckListItem.compileFromNoteContent(note.noteContent));
            }

            if (note.isMarkdown()) {
                noteContent = note.noteContent;
                String[] contents = noteContent.split("\n", 2);
                if (contents.length != 0) {
                    noteTitle = contents[0];
                }
                final float[] headingSizes = {
                        2.F, 1.5F, 1.17F, 1.F, .83F, .67F,
                };

                final int bulletGapWidth = (int) (8 * context.getResources().getDisplayMetrics().density + 0.5F);
                Markwon markwon = makeMarkwon(context, headingSizes, bulletGapWidth);

                RemoteViews notificationLayoutExpanded = new RemoteViews(context.getPackageName(),
                        R.layout.notification_note_expanded);

                notificationLayoutExpanded.setTextViewText(R.id.content, markwon.toMarkdown(noteContent));

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    builder.setCustomBigContentView(notificationLayoutExpanded)
                            .setStyle(new NotificationCompat.DecoratedCustomViewStyle());
                } else {
                    builder.setContent(notificationLayoutExpanded);
                }

            } else {
                builder.setStyle(new NotificationCompat.BigTextStyle().bigText(noteContent));
            }

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

    @NotNull
    private Markwon makeMarkwon(Context context, float[] headingSizes, int bulletGapWidth) {
        return Markwon.builder(context)
                .usePlugin(StrikethroughPlugin.create())
                .usePlugin(TablePlugin.create(context))
                .usePlugin(new AbstractMarkwonPlugin() {
                    @Override
                    public void configureSpansFactory(@NonNull MarkwonSpansFactory.Builder builder1) {
                        builder1
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
    }

    public void setFullScreenIntent(Context context) {
        Intent fullScreenIntent = new Intent(context, AlarmActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString(NOTE_TITLE_KEY, noteWithTags.note.title);
        bundle.putString(NOTE_DATE_TIME_KEY, reminderDateTimeString);
        bundle.putLong(NOTE_ID_KEY, noteID);

        fullScreenIntent.putExtras(bundle);
        PendingIntent fullScreenPendingIntent = PendingIntent.getActivity(context, (int) noteID, fullScreenIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setFullScreenIntent(fullScreenPendingIntent, true);

    }

}
