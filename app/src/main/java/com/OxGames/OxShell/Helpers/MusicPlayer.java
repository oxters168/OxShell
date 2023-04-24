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
import com.OxGames.OxShell.Data.SettingsKeeper;
import com.OxGames.OxShell.OxShellApp;
import com.OxGames.OxShell.R;

import java.io.File;

public class MusicPlayer {
    public static final String PREV_INTENT = "ACTION_SKIP_TO_PREVIOUS";
    public static final String NEXT_INTENT = "ACTION_SKIP_TO_NEXT";
    public static final String PLAY_INTENT = "ACTION_PLAY";
    public static final String PAUSE_INTENT = "ACTION_PAUSE";
    public static final String STOP_INTENT = "ACTION_STOP";

    private static Metadata currentTrackData;
    private static int trackIndex = 0;
    private static MediaSessionCompat session = null;
    private static DataRef[] refs = null;

    private static final Player.Listener exoListener = new Player.Listener() {
        @Override
        public void onPlaybackStateChanged(int playbackState) {
            Player.Listener.super.onPlaybackStateChanged(playbackState);
            //Log.d("MusicPlayer", "Exo playback state changed: " + playbackState);
            setTrackIndex(exo.getCurrentMediaItemIndex());
            refreshMetadata();
            refreshNotificationAndSession(exo.isPlaying());
        }
        @Override
        public void onMediaItemTransition(@Nullable MediaItem mediaItem, int reason) {
            Player.Listener.super.onMediaItemTransition(mediaItem, reason);
            //Log.d("MusicPlayer", "Exo media item transitioned: " + reason);
            setTrackIndex(exo.getCurrentMediaItemIndex());
            refreshMetadata();
            refreshNotificationAndSession(exo.isPlaying());
        }
    };
    private static AudioFocusRequest audioFocusRequest = null;

    private static final int NOTIFICATION_ID = 12345;
    private static final String CHANNEL_ID = "54321";
    private static ExoPlayer exo = null;

    public static void setPlaylist(DataRef... trackLocs) {
        setPlaylist(0, trackLocs);
    }
    public static void setPlaylist(int startPos, DataRef... trackLocs) {
        boolean hasTracks = trackLocs != null && trackLocs.length > 0;
        if (hasTracks) {
            refs = trackLocs;

            if (exo != null) {
                exo.stop();
                exo.clearMediaItems();
            } else {
                exo = new ExoPlayer.Builder(OxShellApp.getContext()).build();
                exo.addListener(exoListener);
            }

            for (DataRef trackLoc : trackLocs)
                if (trackLoc.getLocType() == DataLocation.file)
                    exo.addMediaItem(MediaItem.fromUri((String)trackLoc.getLoc()));
                else if (trackLoc.getLocType() == DataLocation.resolverUri)
                    exo.addMediaItem(MediaItem.fromUri((Uri)trackLoc.getLoc()));
            exo.prepare();
            exo.seekTo(startPos, 0);
            setTrackIndex(startPos);
            //Log.d("MusicPlayer", "Setting playlist with " + trackLocs.length + " item(s), setting pos as " + trackIndex);
            refreshMetadata();
            refreshNotificationAndSession(false);
        } else
            clearPlaylist();
    }
    public static void clearPlaylist() {
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

    public static void togglePlay() {
        if (isPlaying())
            pause();
        else
            play();
    }
    public static void play() {
        play(trackIndex);
    }
    public static void play(int index) {
        if (exo != null && exo.getMediaItemCount() > 0) {
            requestAudioFocus();

            setTrackIndex(index);
            setVolume(SettingsKeeper.getMusicVolume());
            if (index != exo.getCurrentMediaItemIndex())
                exo.seekTo(index, 0);
            if (!exo.isPlaying())
                exo.play();

            refreshMetadata();
            refreshNotificationAndSession(true);
        } else
            Log.e("MusicPlayer", "Failed to play, exoplayer is null or has no tracks");
    }
    public static boolean isPlaying() {
        return exo != null && exo.isPlaying();
    }
    public static void seekToNext() {
        if (exo != null) {
            exo.seekToNext();

            setTrackIndex(exo.getPreviousMediaItemIndex());
            refreshMetadata();
            refreshNotificationAndSession(exo.isPlaying());
        } else
            Log.e("MusicPlayer", "Failed to seek to next, exoplayer is null");
    }
    public static void seekToPrev() {
        if (exo != null) {
            exo.seekToPrevious();

            setTrackIndex(exo.getPreviousMediaItemIndex());
            refreshMetadata();
            refreshNotificationAndSession(exo.isPlaying());
        } else
            Log.e("MusicPlayer", "Failed to seek to previous, exoplayer is null");
    }
    public static void pause() {
        if (exo != null) {
            exo.pause();
            abandonAudioFocus();
            refreshNotificationAndSession(false);
        } else
            Log.e("MusicPlayer", "Failed to pause, exoplayer is null");
    }
    public static void stop() {
        clearPlaylist();
    }
    public static void seekTo(long ms) {
        if (exo != null) {
            exo.seekTo(ms);
            refreshNotificationAndSession(isPlaying());
        } else
            Log.e("MusicPlayer", "Failed to seek, exoplayer is null");
    }
    public static void seekForward() {
        if (exo != null) {
            exo.seekForward();
            refreshNotificationAndSession(isPlaying());
        } else
            Log.e("MusicPlayer", "Failed to seek forward, exoplayer is null");
    }
    public static void seekBack() {
        if (exo != null) {
            exo.seekBack();
            refreshNotificationAndSession(isPlaying());
        } else
            Log.e("MusicPlayer", "Failed to seek back, exoplayer is null");
    }
    public static void setVolume(float value) {
        if (exo != null)
            exo.setVolume(value);
        else
            Log.e("MusicPlayer", "Failed to set volume, exoplayer is null");
    }
    public static long getCurrentPosition() {
        if (exo != null)
            return exo.getCurrentPosition();
        else
            return PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN;
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
        trackIndex = MathHelpers.clamp(index, 0, exo.getMediaItemCount() - 1);
    }
    private static DataRef getCurrentDataRef() {
        return refs[MathHelpers.clamp(exo.getCurrentMediaItemIndex(), 0, refs.length - 1)];
    }
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
        NotificationManager notificationManager = OxShellApp.getContext().getSystemService(NotificationManager.class);
        notificationManager.cancel(NOTIFICATION_ID);
    }
    private static void showNotification(boolean isPlaying) {
        // Create the notification channel.
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Music Player",
                NotificationManager.IMPORTANCE_DEFAULT
        );
        channel.setDescription("Music Player Notification Channel");
        NotificationManager notificationManager = OxShellApp.getContext().getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);

        Intent stopIntent = new Intent(STOP_INTENT);
        PendingIntent stopPendingIntent = PendingIntent.getBroadcast(OxShellApp.getContext(), 0, stopIntent, PendingIntent.FLAG_MUTABLE);
        Intent prevIntent = new Intent(PREV_INTENT);
        PendingIntent prevPendingIntent = PendingIntent.getBroadcast(OxShellApp.getContext(), 0, prevIntent, PendingIntent.FLAG_MUTABLE);
        Intent pauseIntent = new Intent(PAUSE_INTENT);
        PendingIntent pausePendingIntent = PendingIntent.getBroadcast(OxShellApp.getContext(), 0, pauseIntent, PendingIntent.FLAG_MUTABLE);
        Intent playIntent = new Intent(PLAY_INTENT);
        PendingIntent playPendingIntent = PendingIntent.getBroadcast(OxShellApp.getContext(), 0, playIntent, PendingIntent.FLAG_MUTABLE);
        Intent nextIntent = new Intent(NEXT_INTENT);
        PendingIntent nextPendingIntent = PendingIntent.getBroadcast(OxShellApp.getContext(), 0, nextIntent, PendingIntent.FLAG_MUTABLE);

        // Create a NotificationCompat.MediaStyle object and set its properties
        androidx.media.app.NotificationCompat.MediaStyle mediaStyle = new androidx.media.app.NotificationCompat.MediaStyle()
                .setMediaSession(session.getSessionToken())
                .setShowActionsInCompactView(0, 1, 2) // Show prev, play, and next buttons in compact view
                .setShowCancelButton(true)
                .setCancelButtonIntent(stopPendingIntent);

        // Create the notification using the builder.
        NotificationCompat.Builder builder = new NotificationCompat.Builder(OxShellApp.getContext(), CHANNEL_ID)
                .setSmallIcon(R.drawable.ox_white)
                .setLargeIcon(currentTrackData.getAlbumArt())
                .setContentTitle(getCurrentTitle())
                .setContentText(getCurrentArtist() + " - " + getCurrentAlbum())
                //.setContentIntent(pendingIntent) // TODO: get this to open the music player activity of the app
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setSilent(true)
                .setStyle(mediaStyle)
                .addAction(R.drawable.baseline_skip_previous_24, "Prev", prevPendingIntent)
                .addAction(isPlaying ? R.drawable.baseline_pause_24 : R.drawable.baseline_play_arrow_24, isPlaying ? "Pause" : "Play", isPlaying ? pausePendingIntent : playPendingIntent)
                .addAction(R.drawable.baseline_skip_next_24, "Next", nextPendingIntent)
                .addAction(R.drawable.baseline_close_24, "Stop", stopPendingIntent)
                .setOngoing(true);

        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    private static void releaseSession() {
        if (session != null) {
            session.release();
            session = null;
        }
    }
    private static void setSessionState(boolean isPlaying) {
        if (session == null)
            prepareSession();
        // source: https://android-developers.googleblog.com/2020/08/playing-nicely-with-media-controls.html
        session.setMetadata(new MediaMetadataCompat.Builder()
                .putString(MediaMetadata.METADATA_KEY_TITLE, currentTrackData.getTitle())
                .putString(MediaMetadata.METADATA_KEY_ARTIST, currentTrackData.getArtist())
                .putBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART, currentTrackData.getAlbumArt())
                .putLong(MediaMetadata.METADATA_KEY_DURATION, getCurrentDuration())
                .build());
        session.setPlaybackState(new PlaybackStateCompat.Builder()
                .setActions(PlaybackStateCompat.ACTION_STOP | PlaybackStateCompat.ACTION_PLAY | PlaybackStateCompat.ACTION_PAUSE | PlaybackStateCompat.ACTION_SKIP_TO_NEXT | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS | PlaybackStateCompat.ACTION_SEEK_TO)
                .setState(isPlaying ? PlaybackStateCompat.STATE_PLAYING : PlaybackStateCompat.STATE_PAUSED, getCurrentPosition(), 1.0f)
                .build());
    }
    private static void prepareSession() {
        session = new MediaSessionCompat(OxShellApp.getContext(), BuildConfig.APP_LABEL);
        session.setCallback(new MediaSessionCompat.Callback() {
//            @Override
//            public boolean onMediaButtonEvent(@NonNull Intent mediaButtonIntent) {
//                Log.d("MusicPlayer", mediaButtonIntent + ", " + (mediaButtonIntent.getExtras() != null ? mediaButtonIntent.getExtras().toString() : "null"));
//                return super.onMediaButtonEvent(mediaButtonIntent);
//            }

            @Override
            public void onPlay() {
                super.onPlay();
                //Log.d("MusicPlayer", "onPlay");
                play();
            }

            @Override
            public void onSkipToQueueItem(long id) {
                super.onSkipToQueueItem(id);
                //Log.d("MusicPlayer", "onSkipToQueueItem " + id);
                play((int)id);
            }

            @Override
            public void onPause() {
                super.onPause();
                //Log.d("MusicPlayer", "onPause");
                pause();
            }

            @Override
            public void onSkipToNext() {
                super.onSkipToNext();
                //Log.d("MusicPlayer", "onSkipToNext");
                seekToNext();
            }

            @Override
            public void onSkipToPrevious() {
                super.onSkipToPrevious();
                //Log.d("MusicPlayer", "onSkipToPrevious");
                seekToPrev();
            }

            @Override
            public void onFastForward() {
                super.onFastForward();
                //Log.d("MusicPlayer", "onFastForward");
                seekForward();
            }

            @Override
            public void onRewind() {
                super.onRewind();
                //Log.d("MusicPlayer", "onRewind");
                seekBack();
            }

            @Override
            public void onStop() {
                super.onStop();
                //Log.d("MusicPlayer", "onStop");
                stop();
            }

            @Override
            public void onSeekTo(long pos) {
                super.onSeekTo(pos);
                //Log.d("MusicPlayer", "onSeekTo " + pos);
                seekTo((int)pos);
                refreshNotificationAndSession(isPlaying());
            }
        });
        session.setActive(true);
    }
}
