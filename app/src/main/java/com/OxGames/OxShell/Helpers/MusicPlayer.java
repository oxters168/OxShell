package com.OxGames.OxShell.Helpers;

import android.content.Intent;
import android.media.session.MediaSession;
import android.util.Log;

import androidx.annotation.NonNull;

import com.OxGames.OxShell.BuildConfig;
import com.OxGames.OxShell.Data.DataRef;
import com.OxGames.OxShell.OxShellApp;

import java.util.LinkedList;

public class MusicPlayer {
    private static LinkedList<AudioPool> playlist = new LinkedList<>();
    private static int currentPos = 0;
    private static MediaSession session = null;

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
