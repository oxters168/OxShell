package com.OxGames.OxShell.Helpers;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
//import android.media.session.MediaSession;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaMetadata;
import android.net.Uri;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;
//import androidx.media.app.NotificationCompat;

import com.OxGames.OxShell.BuildConfig;
import com.OxGames.OxShell.Data.DataLocation;
import com.OxGames.OxShell.Data.DataRef;
import com.OxGames.OxShell.Data.Metadata;
import com.OxGames.OxShell.OxShellApp;
import com.OxGames.OxShell.R;

import java.io.File;

public class MusicPlayer {// extends MediaBrowserServiceCompat {
    public static final String PREV_INTENT = "ACTION_SKIP_TO_PREVIOUS";
    public static final String NEXT_INTENT = "ACTION_SKIP_TO_NEXT";
    public static final String PLAY_INTENT = "ACTION_PLAY";
    public static final String PAUSE_INTENT = "ACTION_PAUSE";
    public static final String STOP_INTENT = "ACTION_STOP";

    //private static final LinkedList<AudioPool> playlist = new LinkedList<>();
    private static Metadata currentTrackData;
    private static int trackIndex = 0;
    private static MediaSessionCompat session = null;
    private static DataRef[] refs = null;

//    private static Runnable currentCompletedListener = null;
//    private static Runnable currentLoopListener = null;
    private static final Player.Listener exoListener = new Player.Listener() {
        @Override
        public void onPlaybackStateChanged(int playbackState) {
            Player.Listener.super.onPlaybackStateChanged(playbackState);
            Log.d("MusicPlayer", "Exo playback state changed: " + playbackState);
            setTrackIndex(exo.getCurrentMediaItemIndex());
            refreshNotificationAndSession(exo.isPlaying());
        }
        @Override
        public void onMediaItemTransition(@Nullable MediaItem mediaItem, int reason) {
            Player.Listener.super.onMediaItemTransition(mediaItem, reason);
            Log.d("MusicPlayer", "Exo media item transitioned: " + reason);
            setTrackIndex(exo.getCurrentMediaItemIndex());
            refreshNotificationAndSession(exo.isPlaying());
        }
    };
    private static AudioFocusRequest audioFocusRequest = null;

    private static final int NOTIFICATION_ID = 12345;
    private static final String CHANNEL_ID = "54321";
    private static ExoPlayer exo = null;//new ExoPlayer.Builder(OxShellApp.getCurrentActivity()).build();

    //private static MusicPlayer instance = null;

//    @Override
//    public void onCreate() {
//        super.onCreate();
//        Log.d("MusicPlayer", "onCreate");
//        instance = this;
//        prepareSession();
//        showNotification();
//    }

//    public static boolean isServiceRunning() {
//        return instance != null;
//    }
//    private static void startService() {
//        Intent intent = new Intent(OxShellApp.getContext(), MusicPlayer.class);
//        OxShellApp.getContext().startService(intent);
//    }
//    private static void stopService() {
//        Intent intent = new Intent(OxShellApp.getContext(), MusicPlayer.class);
//        OxShellApp.getContext().stopService(intent);
//    }

    public static void setPlaylist(DataRef... trackLocs) {
        setPlaylist(0, trackLocs);
    }
    public static void setPlaylist(int startPos, DataRef... trackLocs) {
        clearPlaylist();
        boolean hasTracks = trackLocs != null && trackLocs.length > 0;
        if (hasTracks) {
            //MediaItem mediaItem = MediaItem.fromUri()
            exo = new ExoPlayer.Builder(OxShellApp.getContext()).build();
            refs = trackLocs;
            for (DataRef trackLoc : trackLocs)
                if (trackLoc.getLocType() == DataLocation.file)
                    exo.addMediaItem(MediaItem.fromUri((String)trackLoc.getLoc()));
                else if (trackLoc.getLocType() == DataLocation.resolverUri)
                    exo.addMediaItem(MediaItem.fromUri((Uri)trackLoc.getLoc()));
                //playlist.add(AudioPool.from(trackLoc, 1)); // had to set pool size to 1 since some files had an issue if more than one was loaded (maybe waiting until all are prepped would be worth trying)
            exo.prepare();
            exo.seekTo(startPos, 0);
            exo.addListener(exoListener);
            setTrackIndex(startPos);
            Log.d("MusicPlayer", "Setting playlist with " + trackLocs.length + " item(s), setting pos as " + trackIndex);
            refreshMetadata();
            prepareSession();
            showNotification(false);
        }
    }
    public static void clearPlaylist() {
//        if (playlist.size() > 0 && getCurrentTrack().isAnyPlaying()) {
//            getCurrentTrack().stopActive();
//            getCurrentTrack().removeOnCompletedListener(currentCompletedListener);
//            getCurrentTrack().removeOnLoopListener(currentLoopListener);
//        }
//        for (AudioPool track : playlist)
//            track.setPoolSize(0);
//        playlist.clear();
        if (exo != null) {
            exo.stop();
            exo.clearMediaItems();
            exo.removeListener(exoListener);
            exo.release();
            exo = null;
        }
        currentTrackData = null;

        abandonAudioFocus();
        hideNotification();
        releaseSession();
    }

    public static void play() {
        play(trackIndex);
    }
//    public static void togglePlayback() {
//        if (playlist.size() > 0) {
//            if (getCurrentTrack().isAnyPlaying())
//                pause();
//            else
//                play();
//        }
//    }
    public static void play(int index) {
        if (exo.getMediaItemCount() > 0) {
//            boolean differentTrack = index != trackIndex;
//            if (currentCompletedListener != null)
//                getCurrentTrack().removeOnCompletedListener(currentCompletedListener);
//            if (currentLoopListener != null)
//                getCurrentTrack().removeOnLoopListener(currentLoopListener);
//            if (differentTrack && getCurrentTrack().getActiveCount() > 0) {
//                Log.d("MusicPlayer", "Stopping current track since " + index + " != " + trackIndex);
//                getCurrentTrack().stopActive();
//            }

            requestAudioFocus();

            setTrackIndex(index);
            exo.seekTo(index, 0);
            if (!exo.isPlaying())
                exo.play();
//            AudioPool currentTrack = getCurrentTrack();
//            currentCompletedListener = MusicPlayer::playNext;
//            currentLoopListener = () -> refreshNotificationAndSession(true);
//            currentTrack.addOnCompletedListener(currentCompletedListener);
//            currentTrack.addOnLoopListener(currentLoopListener);
//            if (currentTrack.getActiveCount() > 0)
//                currentTrack.resumeActive();
//            else
//                currentTrack.playNew(false);

            refreshMetadata();
            refreshNotificationAndSession(true);
        }
    }
//    public static boolean hasNext() {
//        return trackIndex + 1 < playlist.size();
//    }
//    public static boolean hasPrev() {
//        return trackIndex - 1 >= 0;
//    }
    public static void seekToNext() {
        exo.seekToNext();
//        if (hasNext())
//            play(trackIndex + 1);
//        else {
//            pause();
//            seekTo(0);
//            //refreshNotificationAndSession(false);
//        }
    }
    public static void seekToPrev() {
//        if (hasPrev())
//            play(trackIndex - 1);
//        else {
//            pause();
//            seekTo(0);
//            //refreshNotificationAndSession(false);
//        }
        exo.seekToPrevious();
    }
    public static void pause() {
        //getCurrentTrack().pauseActive();
        exo.pause();
        abandonAudioFocus();
        refreshNotificationAndSession(false);
    }
    public static void stop() {
        clearPlaylist();
    }
    public static void seekTo(long ms) {
        exo.seekTo(ms);
//        if (playlist.size() > 0)
//            getCurrentTrack().seekTo(ms);
//        else
//            Log.e("MusicPlayer", "Failed to seek to " + ms + " ms, playlist is empty");
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

    private static void setTrackIndex(int index) {
        trackIndex = clampTrackIndex(index);
    }
    private static int clampTrackIndex(int index) {
        return Math.min(Math.max(0, index), exo.getMediaItemCount() - 1);
    }
    private static DataRef getCurrentDataRef() {
        return refs[exo.getCurrentMediaItemIndex()];
    }
//    private static AudioPool getCurrentTrack() {
//        return playlist.get(trackIndex);
//    }
//    private static AudioPool getNextTrack() {
//        return playlist.get(clampPos(currentPos + 1));
//    }
//    private static AudioPool getPrevTrack() {
//        return playlist.get(clampPos(currentPos - 1));
//    }
    private static void refreshMetadata() {
        currentTrackData = Metadata.getMediaMetadata(getCurrentDataRef());
    }
    private static String getCurrentTitle() {
        if (currentTrackData == null)
            refreshMetadata();

        String title = currentTrackData.getTitle();
        if (title == null || title.isEmpty()) {
            DataRef dataRef = getCurrentDataRef();
            if (dataRef.getLocType() == DataLocation.file)
                title = AndroidHelpers.removeExtension((new File((String)dataRef.getLoc())).getName());
            else
                title = "?";
        }
        return title;
    }
    private static String getCurrentArtist() {
        if (currentTrackData == null)
            refreshMetadata();
        String artist = currentTrackData.getArtist();
        if (artist == null || artist.isEmpty())
            artist = "Various Artists";
        return artist;
    }
    private static String getCurrentAlbum() {
        if (currentTrackData == null)
            refreshMetadata();
        String album = currentTrackData.getAlbum();
        if (album == null || album.isEmpty())
            album = "?";
        return album;
    }
    private static long getCurrentDuration() {
        if (currentTrackData == null)
            refreshMetadata();
        long duration = 0;
        try {
            duration = Long.valueOf(currentTrackData.getDuration());
        } catch (Exception e) {
            Log.e("MusicPlayer", "Failed to retrieve track duration from metadata");
        }
        return duration;
    }

    private static void refreshNotificationAndSession(boolean isPlaying) {
        setSessionState(isPlaying);
        showNotification(isPlaying);
    }
    private static void hideNotification() {
        // Get the NotificationManager.
        NotificationManager notificationManager = OxShellApp.getContext().getSystemService(NotificationManager.class);
        // Cancel the notification with the specified ID.
        notificationManager.cancel(NOTIFICATION_ID);
    }
    private static void showNotification(boolean isPlaying) {
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

//        NotificationCompat.Action prevAction = new NotificationCompat.Action(R.drawable.baseline_skip_previous_24, "Previous", MediaButtonReceiver.buildMediaButtonPendingIntent(OxShellApp.getContext(), PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS));
//        NotificationCompat.Action playAction = new NotificationCompat.Action(R.drawable.baseline_play_arrow_24, "Play", MediaButtonReceiver.buildMediaButtonPendingIntent(OxShellApp.getContext(), PlaybackStateCompat.ACTION_PLAY));
//        NotificationCompat.Action pauseAction = new NotificationCompat.Action(R.drawable.baseline_pause_24, "Pause", MediaButtonReceiver.buildMediaButtonPendingIntent(OxShellApp.getContext(), PlaybackStateCompat.ACTION_PAUSE));
//        NotificationCompat.Action nextAction = new NotificationCompat.Action(R.drawable.baseline_skip_next_24, "Next", MediaButtonReceiver.buildMediaButtonPendingIntent(OxShellApp.getContext(), PlaybackStateCompat.ACTION_SKIP_TO_NEXT));
//        NotificationCompat.Action stopAction = new NotificationCompat.Action(R.drawable.baseline_close_24, "Stop", MediaButtonReceiver.buildMediaButtonPendingIntent(OxShellApp.getContext(), PlaybackStateCompat.ACTION_STOP));
//        builder.addAction(prevAction);
//        builder.addAction(isPlaying ? pauseAction : playAction);
//        builder.addAction(nextAction);
//        builder.addAction(stopAction);

        // Create a prev button action.
        Intent prevIntent = new Intent(PREV_INTENT);
        PendingIntent prevPendingIntent = PendingIntent.getBroadcast(OxShellApp.getContext(), 0, prevIntent, PendingIntent.FLAG_MUTABLE);
        // Add the prev button action to the notification.
        //builder.addAction(R.drawable.baseline_skip_previous_24, "Prev", prevPendingIntent);

        //if (isPlaying) {
            // Create a pause button action.
            Intent pauseIntent = new Intent(PAUSE_INTENT);
            PendingIntent pausePendingIntent = PendingIntent.getBroadcast(OxShellApp.getContext(), 0, pauseIntent, PendingIntent.FLAG_MUTABLE);
            // Add the pause button action to the notification.
            //builder.addAction(R.drawable.baseline_pause_24, "Pause", pausePendingIntent);
        //} else {
            // Create a play button action.
            Intent playIntent = new Intent(PLAY_INTENT);
            PendingIntent playPendingIntent = PendingIntent.getBroadcast(OxShellApp.getContext(), 0, playIntent, PendingIntent.FLAG_MUTABLE);
            // Add the play button action to the notification.
            //builder.addAction(R.drawable.baseline_play_arrow_24, "Play", playPendingIntent);
        //}

        // Create a next button action.
        Intent nextIntent = new Intent(NEXT_INTENT);
        PendingIntent nextPendingIntent = PendingIntent.getBroadcast(OxShellApp.getContext(), 0, nextIntent, PendingIntent.FLAG_MUTABLE);
        // Add the next button action to the notification.
        //builder.addAction(R.drawable.baseline_skip_next_24, "Next", nextPendingIntent);

        // Add the cancel button action to the notification.
        //builder.addAction(R.drawable.baseline_close_24, "Stop", stopPendingIntent);

        // Create a NotificationCompat.MediaStyle object and set its properties
        androidx.media.app.NotificationCompat.MediaStyle mediaStyle = new androidx.media.app.NotificationCompat.MediaStyle()
                .setMediaSession(session.getSessionToken())
                .setShowActionsInCompactView(0, 1, 2) // Show prev, play, and next buttons in compact view
                .setShowCancelButton(true)
                .setCancelButtonIntent(stopPendingIntent);

        // Create the notification using the builder.
        NotificationCompat.Builder builder = new NotificationCompat.Builder(OxShellApp.getContext(), CHANNEL_ID)
                //.setSmallIcon(R.drawable.baseline_library_music_24)
                .setSmallIcon(R.drawable.ox_white)
                .setLargeIcon(currentTrackData.getAlbumArt())
                .setContentTitle(getCurrentTitle())
                .setContentText(getCurrentArtist() + " - " + getCurrentAlbum())
                //.setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                //.setAutoCancel(false)
                .setSilent(true)
                //.setDeleteIntent(stopPendingIntent)
                .setStyle(mediaStyle)
                .addAction(R.drawable.baseline_skip_previous_24, "Prev", prevPendingIntent)
                .addAction(isPlaying ? R.drawable.baseline_pause_24 : R.drawable.baseline_play_arrow_24, isPlaying ? "Pause" : "Play", isPlaying ? pausePendingIntent : playPendingIntent)
                .addAction(R.drawable.baseline_skip_next_24, "Next", nextPendingIntent)
                .addAction(R.drawable.baseline_close_24, "Stop", stopPendingIntent)
                .setOngoing(true);
//        if (hasPrev())
//            builder.addAction(R.drawable.baseline_skip_previous_24, "Prev", prevPendingIntent);
//        builder.addAction(isPlaying ? R.drawable.baseline_pause_24 : R.drawable.baseline_play_arrow_24, isPlaying ? "Pause" : "Play", isPlaying ? pausePendingIntent : playPendingIntent);
//        if (hasNext())
//            builder.addAction(R.drawable.baseline_skip_next_24, "Next", nextPendingIntent);
//        builder.addAction(R.drawable.baseline_close_24, "Stop", stopPendingIntent);

        //builder.setStyle(mediaStyle);

        //NotificationManager notificationManager = OxShellApp.getContext().getSystemService(NotificationManager.class);
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

//    public static boolean isPlaying() {
//        return playlist.size() > 0 && getCurrentTrack().isAnyPlaying();
//    }
    private static void releaseSession() {
        if (session != null) {
            session.release();
            session = null;
        }
    }
    private static void setSessionState(boolean isPlaying) {
        // source: https://android-developers.googleblog.com/2020/08/playing-nicely-with-media-controls.html
        session.setMetadata(new MediaMetadataCompat.Builder()
                // Title.
                .putString(MediaMetadata.METADATA_KEY_TITLE, currentTrackData.getTitle())
                // Artist.
                // Could also be the channel name or TV series.
                .putString(MediaMetadata.METADATA_KEY_ARTIST, currentTrackData.getArtist())
                // Album art.
                // Could also be a screenshot or hero image for video content
                // The URI scheme needs to be "content", "file", or "android.resource".
                .putBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART, currentTrackData.getAlbumArt())
                // Duration.
                // If duration isn't set, such as for live broadcasts, then the progress
                // indicator won't be shown on the seekbar.
                .putLong(MediaMetadata.METADATA_KEY_DURATION, getCurrentDuration())
                .build());
        session.setPlaybackState(new PlaybackStateCompat.Builder()
                .setActions(PlaybackStateCompat.ACTION_STOP | PlaybackStateCompat.ACTION_PLAY | PlaybackStateCompat.ACTION_PAUSE | PlaybackStateCompat.ACTION_SKIP_TO_NEXT | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS | PlaybackStateCompat.ACTION_SEEK_TO)
                .setState(isPlaying ? PlaybackStateCompat.STATE_PLAYING : PlaybackStateCompat.STATE_PAUSED, exo.getCurrentPosition(), 1.0f)
                .build());
//        getCurrentTrack().getCurrentPosition(position -> {
//            Log.d("MusicPlayer", "Received position as " + position);
////            PlaybackStateCompat.Builder playbackState = new PlaybackStateCompat.Builder();
////            playbackState.setActions(PlaybackStateCompat.ACTION_STOP | PlaybackStateCompat.ACTION_PLAY | PlaybackStateCompat.ACTION_PAUSE | PlaybackStateCompat.ACTION_SKIP_TO_NEXT | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS | PlaybackStateCompat.ACTION_SEEK_TO);
////            playbackState.setState(isPlaying ? PlaybackStateCompat.STATE_PLAYING : PlaybackStateCompat.STATE_PAUSED, position, 1.0f);
////            session.setPlaybackState(playbackState.build());
//            session.setPlaybackState(new PlaybackStateCompat.Builder()
//                    .setActions(PlaybackStateCompat.ACTION_STOP | PlaybackStateCompat.ACTION_PLAY | PlaybackStateCompat.ACTION_PAUSE | PlaybackStateCompat.ACTION_SKIP_TO_NEXT | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS | PlaybackStateCompat.ACTION_SEEK_TO)
//                    .setState(isPlaying ? PlaybackStateCompat.STATE_PLAYING : PlaybackStateCompat.STATE_PAUSED, position, 1.0f)
//                    .build());
//        });
    }
//    private static void setSessionButtons(PendingIntent prevButton, PendingIntent playButton, PendingIntent nextButton) {
//        session.setMediaButtonReceiver(prevButton);
//        session.setMediaButtonReceiver(playButton);
//        session.setMediaButtonReceiver(nextButton);
//    }
    private static void prepareSession() {
        session = new MediaSessionCompat(OxShellApp.getContext(), BuildConfig.APP_LABEL);
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
                play();
            }

            @Override
            public void onSkipToQueueItem(long id) {
                super.onSkipToQueueItem(id);
                Log.d("MusicPlayer", "onSkipToQueueItem " + id);
                play((int)id);
            }

            @Override
            public void onPause() {
                super.onPause();
                Log.d("MusicPlayer", "onPause");
                pause();
            }

            @Override
            public void onSkipToNext() {
                super.onSkipToNext();
                Log.d("MusicPlayer", "onSkipToNext");
                seekToNext();
            }

            @Override
            public void onSkipToPrevious() {
                super.onSkipToPrevious();
                Log.d("MusicPlayer", "onSkipToPrevious");
                seekToPrev();
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
                stop();
            }

            @Override
            public void onSeekTo(long pos) {
                super.onSeekTo(pos);
                Log.d("MusicPlayer", "onSeekTo " + pos);
                seekTo((int)pos);
                refreshNotificationAndSession(exo.isPlaying());
            }
        });
        session.setActive(true);
    }

//    @Nullable
//    @Override
//    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, @Nullable Bundle rootHints) {
//        return new BrowserRoot("root", null);
//    }
//    @Override
//    public void onLoadChildren(@NonNull String parentId, @NonNull Result<List<MediaBrowserCompat.MediaItem>> result) {
//        // Return a list of MediaBrowserCompat.MediaItem objects for the given parent ID
//        List<MediaBrowserCompat.MediaItem> mediaItems = new ArrayList<>();
//        // Add media items to the list
//        result.sendResult(mediaItems);
//    }
}
