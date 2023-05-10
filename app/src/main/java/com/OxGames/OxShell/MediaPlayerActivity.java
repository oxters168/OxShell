package com.OxGames.OxShell;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.OxGames.OxShell.Data.DataLocation;
import com.OxGames.OxShell.Data.DataRef;
import com.OxGames.OxShell.Data.SettingsKeeper;
import com.OxGames.OxShell.Helpers.MathHelpers;
import com.OxGames.OxShell.Helpers.MediaPlayer;
import com.OxGames.OxShell.Views.MediaPlayerView;

import java.util.concurrent.atomic.AtomicBoolean;

public class MediaPlayerActivity extends PagedActivity {
    private MediaPlayerView mpv;
    private Handler trackPositionHandler;
    private final AtomicBoolean isTrackingPosition = new AtomicBoolean(false);
    private boolean isPaused;
    private final Runnable trackPositionListener = new Runnable() {
        @Override
        public void run() {
            if (MediaPlayer.isPlaying() && !isPaused) {
                //mpv.refreshArtworkSize(); // major slow down for some tracks
                isTrackingPosition.set(true);
                setMediaPlayerViewPosition();
                trackPositionHandler.postDelayed(this, MathHelpers.calculateMillisForFps(60));
            } else
                isTrackingPosition.set(false);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Uri receivedPath = getIntent().getData();
        boolean receivedUri = receivedPath != null;
        if (receivedUri) {
            Log.i("MusicPlayerActivity", "Received data: " + (getIntent() != null ? getIntent().getData() : "null") + " extras: " + (getIntent() != null ? getIntent().getExtras() : "null"));
            //getContentResolver().takePersistableUriPermission(receivedPath, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            MediaPlayer.setPlaylist(DataRef.from(receivedPath, DataLocation.resolverUri));
            MediaPlayer.play();
        }

        FrameLayout parentView = new FrameLayout(this);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        layoutParams.gravity = Gravity.TOP | Gravity.LEFT;
        parentView.setLayoutParams(layoutParams);
        parentView.setFocusable(false);
        parentView.setId(R.id.parent_layout);
        setContentView(parentView);

        parentView.addView(mpv = new MediaPlayerView(this));
        mpv.addMediaBtnListener(this::onMediaButtonPressed);
        mpv.addSeekBarListener(this::onSeekBarSuk);
        mpv.setIsPlaying(MediaPlayer.isPlaying());
        mpv.setTitle(MediaPlayer.getCurrentTitle());
        mpv.setBackBtnVisible(!receivedUri);
//        setMediaPlayerViewImage();
        setMediaPlayerViewPosition();
        setMediaPlayerViewSurface();
        MediaPlayer.addIsPlayingListener(this::onMusicPlayerIsPlaying);
        MediaPlayer.addMediaItemChangedListener(this::onMusicPlayerMediaChanged);
        MediaPlayer.addSeekEventListener(this::onSeekEvent);
        MediaPlayer.addMediaPlayerPreparingListener(this::setMediaPlayerViewSurface);
        startTrackingPosition();
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        int systemUi = SettingsKeeper.getSystemUiVisibility();
        setMarginsFor(SettingsKeeper.hasStatusBarVisible(systemUi), SettingsKeeper.hasNavBarVisible(systemUi), mpv);
    }

    @Override
    protected void onResume() {
        super.onResume();
        isPaused = false;
        mpv.onResume();
        mpv.refreshArtworkSize();
        startTrackingPosition();
        //mpv.setVideoMode(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mpv.onPause();
        isPaused = true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mpv.onDestroy();
        MediaPlayer.removeIsPlayingListener(this::onMusicPlayerIsPlaying);
        MediaPlayer.removeMediaItemChangedListener(this::onMusicPlayerMediaChanged);
        MediaPlayer.removeSeekEventListener(this::onSeekEvent);
        MediaPlayer.removeMediaPlayerPreparingListener(this::setMediaPlayerViewSurface);
    }
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        refreshSystemUiState();
    }
    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        refreshSystemUiState();
    }

    private void setMediaPlayerViewSurface() {
        MediaPlayer.setPlayerView(mpv.getPlayerView());
    }
    private void setMediaPlayerViewPosition() {
        //Log.d("MusicPlayerActivity", "Setting position of seekbar");
        long currentPosition = MediaPlayer.getCurrentPosition();
        long currentDuration = MediaPlayer.getCurrentDuration();
        if (currentDuration > 0 && currentPosition != PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN)
            mpv.setSeekBarPosition(currentPosition / (float)currentDuration);
        setMediaPlayerViewTimes();
    }
    private void setMediaPlayerViewTimes() {
        long currentPosition = MediaPlayer.getCurrentPosition();
        long currentDuration = MediaPlayer.getCurrentDuration();
        mpv.setCurrentTime(currentPosition);
        mpv.setCurrentDuration(currentDuration);
    }
//    private void setMediaPlayerViewImage() {
//        Bitmap albumArt = MediaPlayer.getCurrentAlbumArt();
//        mpv.setImage(albumArt != null ? AndroidHelpers.bitmapToDrawable(this, albumArt) : null);
//    }
    private void refreshSystemUiState() {
        if (hasWindowFocus()) {
            SettingsKeeper.setNavBarHidden(true, false);
            SettingsKeeper.setStatusBarHidden(mpv.isFullscreen(), false);
            SettingsKeeper.setFullscreen(mpv.isFullscreen(), false);
            getWindow().setStatusBarColor(Color.BLACK);
            mpv.refreshLayouts();
        }
    }

    private void onMusicPlayerIsPlaying(boolean onOff) {
        mpv.setIsPlaying(onOff);
        mpv.refreshArtworkSize(); // without this the artwork gets stretched incorrectly
        if (onOff)
            startTrackingPosition();
        //mpv.setPlayerViewShown(onOff);
        //mpv.refreshPlayerView();
    }
    private void startTrackingPosition() {
        if (trackPositionHandler == null)
            trackPositionHandler = new Handler();
        if (!isTrackingPosition.get()) {
            isTrackingPosition.set(true);
            trackPositionHandler.post(trackPositionListener);
        }
    }
    private void onMusicPlayerMediaChanged(int index) {
        mpv.setTitle(MediaPlayer.getCurrentTitle());
        mpv.refreshArtworkSize();
        //mpv.enableArtwork();
        //setMediaPlayerViewImage();
        setMediaPlayerViewPosition();
    }
    private void onSeekEvent(long position) {
        setMediaPlayerViewPosition();
    }
    private void onMediaButtonPressed(MediaPlayerView.MediaButton btn) {
        switch (btn) {
            case end:
                MediaPlayer.stop();
            case back:
                finish();
                break;
            case play:
                MediaPlayer.play();
                break;
            case pause:
                MediaPlayer.pause();
                break;
            case skipNext:
                MediaPlayer.seekToNext();
                break;
            case skipPrev:
                MediaPlayer.seekToPrev();
                break;
            case seekFwd:
                MediaPlayer.seekForward();
                break;
            case seekBck:
                MediaPlayer.seekBack();
                break;
            case fullscreen:
                refreshSystemUiState();
                break;
        }
    }
    private void onSeekBarSuk(float value) {
        MediaPlayer.seekTo((long)(MediaPlayer.getCurrentDuration() * value));
        setMediaPlayerViewTimes();
    }
}
