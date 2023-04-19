package com.OxGames.OxShell.Helpers;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
//import android.media.session.MediaSession;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.media.MediaBrowserServiceCompat;
import androidx.media.session.MediaButtonReceiver;
//import androidx.media.app.NotificationCompat;

import com.OxGames.OxShell.BuildConfig;
import com.OxGames.OxShell.Data.DataLocation;
import com.OxGames.OxShell.Data.DataRef;
import com.OxGames.OxShell.Data.Metadata;
import com.OxGames.OxShell.HomeActivity;
import com.OxGames.OxShell.OxShellApp;
import com.OxGames.OxShell.R;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class MusicPlayer extends MediaBrowserServiceCompat {
    public static final String PREV_INTENT = "com.OxGames.OxShell.PREV";
    public static final String NEXT_INTENT = "com.OxGames.OxShell.NEXT";
    public static final String PLAY_INTENT = "com.OxGames.OxShell.PLAY";
    public static final String STOP_INTENT = "com.OxGames.OxShell.STOP";

    private static LinkedList<AudioPool> playlist = new LinkedList<>();
    private static Metadata currentTrackData;
    private static int currentPos = 0;
    private static MediaSessionCompat session = null;

    private static Runnable currentCompletedListener = null;
    private static AudioFocusRequest audioFocusRequest = null;

    private static final int NOTIFICATION_ID = 12345;
    private static final String CHANNEL_ID = "54321";

    private static MusicPlayer instance = null;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("MusicPlayer", "onCreate");
        instance = this;
        prepareSession();
        showNotification();
    }

    public static boolean isServiceRunning() {
        return instance != null;
    }
    private static void startService() {
        Intent intent = new Intent(OxShellApp.getContext(), MusicPlayer.class);
        OxShellApp.getContext().startService(intent);
    }
    private static void stopService() {
        Intent intent = new Intent(OxShellApp.getContext(), MusicPlayer.class);
        OxShellApp.getContext().stopService(intent);
    }

    public static void setPlaylist(DataRef... trackLocs) {
        setPlaylist(0, trackLocs);
    }
    public static void setPlaylist(int startPos, DataRef... trackLocs) {
        clearPlaylist();
        boolean hasTracks = trackLocs != null && trackLocs.length > 0;
        if (hasTracks) {
            if (!isServiceRunning())
                startService();

            for (DataRef trackLoc : trackLocs)
                playlist.add(AudioPool.from(trackLoc, 2));
            //currentPos = Math.min(Math.max(0, startPos), trackLocs.length - 1);
            setCurrentPos(startPos);
            Log.d("MusicPlayer", "Setting playlist with " + trackLocs.length + " item(s), setting pos as " + currentPos);
            refreshMetadata();
            //prepareSession();
            //if (session != null)
            //    session.setActive(true);
            //showNotification();
        }
    }
    public static void clearPlaylist() {
        for (AudioPool track : playlist)
            track.setPoolSize(0);
        playlist.clear();
        currentTrackData = null;

        abandonAudioFocus();
        hideNotification();
        releaseSession();
        stopService();
    }

    public static void play() {
        play(currentPos);
    }
    public static void togglePlayback() {
        if (playlist.size() > 0) {
            if (getCurrentTrack().isAnyPlaying())
                pause();
            else
                play();
        }
    }
    public static void play(int index) {
        if (playlist.size() > 0) {
            if (currentCompletedListener != null)
                getCurrentTrack().removeOnCompletedListener(currentCompletedListener);

            requestAudioFocus();

            setCurrentPos(index);
            refreshMetadata();
            //showNotification();
            AudioPool currentTrack = getCurrentTrack();
            currentCompletedListener = MusicPlayer::playNext;
            currentTrack.addOnCompletedListener(currentCompletedListener);
            currentTrack.play(false);
            //setSessionState();
        }
    }
    public static void playNext() {
        play(currentPos + 1);
    }
    public static void playPrev() {
        play(currentPos - 1);
    }
    public static void pause() {
        getCurrentTrack().pauseActive();
        setSessionState();
        abandonAudioFocus();
        showNotification();
    }
    public static void stop() {
        clearPlaylist();
    }

    private static void requestAudioFocus() {
        AudioManager audioManager = OxShellApp.getAudioManager();
        AudioFocusRequest.Builder requestBuilder = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN);
        AudioAttributes.Builder attribBuilder = new AudioAttributes.Builder();
        attribBuilder.setContentType(AudioAttributes.CONTENT_TYPE_MUSIC);
        requestBuilder.setAudioAttributes(attribBuilder.build());
        audioManager.requestAudioFocus(audioFocusRequest = requestBuilder.build());
    }
    private static void abandonAudioFocus() {
        if (audioFocusRequest != null) {
            OxShellApp.getAudioManager().abandonAudioFocusRequest(audioFocusRequest);
            audioFocusRequest = null;
        }
    }

    private static void setCurrentPos(int index) {
        currentPos = clampPos(index);
    }
    private static int clampPos(int index) {
        return Math.min(Math.max(0, index), playlist.size() - 1);
    }
    private static AudioPool getCurrentTrack() {
        return playlist.get(currentPos);
    }
//    private static AudioPool getNextTrack() {
//        return playlist.get(clampPos(currentPos + 1));
//    }
//    private static AudioPool getPrevTrack() {
//        return playlist.get(clampPos(currentPos - 1));
//    }
    private static void refreshMetadata() {
        currentTrackData = Metadata.getMediaMetadata(getCurrentTrack().getDataRef());
    }
    private static String getCurrentTitle() {
        if (currentTrackData == null)
            refreshMetadata();

        String title = currentTrackData.getTitle();
        if (title == null || title.isEmpty()) {
            DataRef dataRef = getCurrentTrack().getDataRef();
            if (dataRef.getLocType() == DataLocation.file)
                title = AndroidHelpers.removeExtension((new File((String)dataRef.getLoc())).getName());
            else
                title = "?";
        }
        return title;
    }

    private static void hideNotification() {
        // Get the NotificationManager.
        NotificationManager notificationManager = OxShellApp.getContext().getSystemService(NotificationManager.class);
        // Cancel the notification with the specified ID.
        notificationManager.cancel(NOTIFICATION_ID);
    }
    private static void showNotification() {
//        MediaMetadataCompat mediaMetadata = new MediaMetadataCompat.Builder()
//                .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE, getCurrentTitle())
//                .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, currentTrackData.getAlbumArt())
//                .build();
//        session.setMetadata(mediaMetadata);

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
                .setLargeIcon(currentTrackData.getAlbumArt())
                .setContentTitle("Music Player")
                .setContentText("Now playing: " + getCurrentTitle())
                //.setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                //.setAutoCancel(false)
                .setSilent(true)
                //.setDeleteIntent(stopPendingIntent)
                .setOngoing(true);

        // Create a NotificationCompat.MediaStyle object and set its properties
        androidx.media.app.NotificationCompat.MediaStyle mediaStyle = new androidx.media.app.NotificationCompat.MediaStyle()
                .setMediaSession(session.getSessionToken())
                .setShowActionsInCompactView(0, 1, 2) // Show prev, play, and next buttons in compact view
                .setShowCancelButton(true)
                .setCancelButtonIntent(stopPendingIntent);
        builder.setStyle(mediaStyle);

        NotificationCompat.Action prevAction = new NotificationCompat.Action(R.drawable.baseline_skip_previous_24, "Previous", MediaButtonReceiver.buildMediaButtonPendingIntent(instance, PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS));
        NotificationCompat.Action playAction = new NotificationCompat.Action(R.drawable.baseline_play_arrow_24, "Play", MediaButtonReceiver.buildMediaButtonPendingIntent(instance, PlaybackStateCompat.ACTION_PLAY));
        //NotificationCompat.Action pauseAction = new NotificationCompat.Action(R.drawable.baseline_pause_24, "Pause", MediaButtonReceiver.buildMediaButtonPendingIntent(instance, PlaybackStateCompat.ACTION_PAUSE));
        NotificationCompat.Action nextAction = new NotificationCompat.Action(R.drawable.baseline_skip_next_24, "Next", MediaButtonReceiver.buildMediaButtonPendingIntent(instance, PlaybackStateCompat.ACTION_SKIP_TO_NEXT));
        NotificationCompat.Action stopAction = new NotificationCompat.Action(R.drawable.baseline_close_24, "Stop", MediaButtonReceiver.buildMediaButtonPendingIntent(instance, PlaybackStateCompat.ACTION_STOP));
        builder.addAction(prevAction);
        builder.addAction(playAction);
        builder.addAction(nextAction);
        builder.addAction(stopAction);

//        // Create a prev button action.
//        Intent prevIntent = new Intent(PREV_INTENT);
//        PendingIntent prevPendingIntent = PendingIntent.getBroadcast(OxShellApp.getContext(), 0, prevIntent, PendingIntent.FLAG_MUTABLE);
//        // Add the prev button action to the notification.
//        builder.addAction(R.drawable.baseline_skip_previous_24, "Prev", prevPendingIntent);
//
//        // Create a play button action.
//        Intent playIntent = new Intent(PLAY_INTENT);
//        PendingIntent playPendingIntent = PendingIntent.getBroadcast(OxShellApp.getContext(), 0, playIntent, PendingIntent.FLAG_MUTABLE);
//        // Add the play button action to the notification.
//        builder.addAction(R.drawable.baseline_play_arrow_24, "Play", playPendingIntent);
//
//        // Create a next button action.
//        Intent nextIntent = new Intent(NEXT_INTENT);
//        PendingIntent nextPendingIntent = PendingIntent.getBroadcast(OxShellApp.getContext(), 0, nextIntent, PendingIntent.FLAG_MUTABLE);
//        // Add the next button action to the notification.
//        builder.addAction(R.drawable.baseline_skip_next_24, "Next", nextPendingIntent);
//
//        // Add the cancel button action to the notification.
//        builder.addAction(R.drawable.baseline_close_24, "Stop", stopPendingIntent);

        //setSessionButtons(prevPendingIntent, playPendingIntent, nextPendingIntent);

        //NotificationManager notificationManager = OxShellApp.getContext().getSystemService(NotificationManager.class);
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    private static void releaseSession() {
        if (session != null) {
            session.release();
            session = null;
        }
    }
    private static void setSessionState() {
        PlaybackStateCompat.Builder playbackState = new PlaybackStateCompat.Builder();
        playbackState.setActions(PlaybackStateCompat.ACTION_PLAY | PlaybackStateCompat.ACTION_PAUSE | PlaybackStateCompat.ACTION_SKIP_TO_NEXT | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS);
        playbackState.setState(PlaybackStateCompat.STATE_PAUSED, PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1.0f);
        session.setPlaybackState(playbackState.build());
    }
//    private static void setSessionButtons(PendingIntent prevButton, PendingIntent playButton, PendingIntent nextButton) {
//        session.setMediaButtonReceiver(prevButton);
//        session.setMediaButtonReceiver(playButton);
//        session.setMediaButtonReceiver(nextButton);
//    }
    private static void prepareSession() {
        session = new MediaSessionCompat(instance, BuildConfig.APP_LABEL);
        session.setCallback(new MediaSessionCompat.Callback() {
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
        session.setActive(true);
    }

    @Nullable
    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, @Nullable Bundle rootHints) {
        return new BrowserRoot("root", null);
    }
    @Override
    public void onLoadChildren(@NonNull String parentId, @NonNull Result<List<MediaBrowserCompat.MediaItem>> result) {
        // Return a list of MediaBrowserCompat.MediaItem objects for the given parent ID
        List<MediaBrowserCompat.MediaItem> mediaItems = new ArrayList<>();
        // Add media items to the list
        result.sendResult(mediaItems);
    }
}
