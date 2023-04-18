package com.OxGames.OxShell.Helpers;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.media.session.MediaSession;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.OxGames.OxShell.BuildConfig;
import com.OxGames.OxShell.Data.DataRef;
import com.OxGames.OxShell.HomeActivity;
import com.OxGames.OxShell.OxShellApp;
import com.OxGames.OxShell.R;

import java.util.LinkedList;

public class MusicPlayer {
    public static final String PLAY_INTENT = "com.OxGames.OxShell.PLAY";
    public static final String STOP_INTENT = "com.OxGames.OxShell.STOP";

    private static LinkedList<AudioPool> playlist = new LinkedList<>();
    private static int currentPos = 0;
    private static MediaSession session = null;

    private static final int NOTIFICATION_ID = 12345;
    private static final String CHANNEL_ID = "54321";

    public static void setPlaylist(DataRef... trackLocs) {
        setPlaylist(0, trackLocs);
    }
    public static void setPlaylist(int startPos, DataRef... trackLocs) {
        clearPlaylist();
        boolean hasTracks = trackLocs != null && trackLocs.length > 0;
        if (hasTracks) {
            for (DataRef trackLoc : trackLocs)
                playlist.add(AudioPool.from(trackLoc, 2));
            currentPos = Math.min(Math.max(0, startPos), trackLocs.length - 1);
            Log.d("MusicPlayer", "Setting playlist with " + trackLocs.length + " item(s), setting pos as " + currentPos);
            showNotification();
            prepareSession();
        }
        if (session != null) {
            session.setActive(hasTracks);
            if (!hasTracks)
                session.release();
        }
    }
    public static void clearPlaylist() {
        for (AudioPool track : playlist)
            track.setPoolSize(0);
        playlist.clear();
        if (session != null) {
            session.release();
            session = null;
        }
    }

    private static void showNotification() {
        // Create the notification channel.
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Music Player",
                NotificationManager.IMPORTANCE_DEFAULT
        );
        channel.setDescription("Music Player Notification Channel");
        NotificationManager notificationManager = OxShellApp.getContext().getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);

        //Intent intent = new Intent(OxShellApp.getContext(), HomeActivity.class);
        //PendingIntent pendingIntent = PendingIntent.getActivity(OxShellApp.getContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Create a stop action.
        Intent stopIntent = new Intent(STOP_INTENT);
        PendingIntent stopPendingIntent = PendingIntent.getBroadcast(OxShellApp.getContext(), 0, stopIntent, PendingIntent.FLAG_MUTABLE);

        // Create the notification using the builder.
        NotificationCompat.Builder builder = new NotificationCompat.Builder(OxShellApp.getContext(), CHANNEL_ID)
                .setSmallIcon(R.drawable.baseline_library_music_24)
                .setContentTitle("Music Player")
                .setContentText("Now playing: Song Title")
                //.setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setSilent(true)
                .setDeleteIntent(stopPendingIntent);

        // Create a play button action.
        Intent playIntent = new Intent(PLAY_INTENT);
        PendingIntent playPendingIntent = PendingIntent.getBroadcast(OxShellApp.getContext(), 0, playIntent, PendingIntent.FLAG_MUTABLE);
        NotificationCompat.Action playAction = new NotificationCompat.Action(R.drawable.baseline_arrow_drop_down_24, "Play", playPendingIntent);
        // Add the play button action to the notification.
        builder.addAction(playAction);

        //NotificationManager notificationManager = OxShellApp.getContext().getSystemService(NotificationManager.class);
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    private static void prepareSession() {
        session = new MediaSession(OxShellApp.getContext(), BuildConfig.APP_LABEL);
        session.setCallback(new MediaSession.Callback() {
            @Override
            public boolean onMediaButtonEvent(@NonNull Intent mediaButtonIntent) {
                Log.d("MusicPlayer", mediaButtonIntent + ", " + (mediaButtonIntent.getExtras() != null ? mediaButtonIntent.getExtras().toString() : "null"));
                return super.onMediaButtonEvent(mediaButtonIntent);
            }

            @Override
            public void onPlay() {
                super.onPlay();
                Log.d("MusicPlayer", "onPlay");
            }

            @Override
            public void onSkipToQueueItem(long id) {
                super.onSkipToQueueItem(id);
                Log.d("MusicPlayer", "onSkipToQueueItem " + id);
            }

            @Override
            public void onPause() {
                super.onPause();
                Log.d("MusicPlayer", "onPause");
            }

            @Override
            public void onSkipToNext() {
                super.onSkipToNext();
                Log.d("MusicPlayer", "onSkipToNext");
            }

            @Override
            public void onSkipToPrevious() {
                super.onSkipToPrevious();
                Log.d("MusicPlayer", "onSkipToPrevious");
            }

            @Override
            public void onFastForward() {
                super.onFastForward();
                Log.d("MusicPlayer", "onFastForward");
            }

            @Override
            public void onRewind() {
                super.onRewind();
                Log.d("MusicPlayer", "onRewind");
            }

            @Override
            public void onStop() {
                super.onStop();
                Log.d("MusicPlayer", "onStop");
            }

            @Override
            public void onSeekTo(long pos) {
                super.onSeekTo(pos);
                Log.d("MusicPlayer", "onSeekTo " + pos);
            }
        });
    }
}
