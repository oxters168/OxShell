package com.OxGames.OxShell;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;

import com.OxGames.OxShell.Data.SettingsKeeper;
import com.OxGames.OxShell.Helpers.MusicPlayer;
import com.OxGames.OxShell.Views.MediaPlayerView;

public class MusicPlayerActivity extends PagedActivity {
    private MediaPlayerView mpv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FrameLayout parentView = new FrameLayout(this);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        layoutParams.gravity = Gravity.TOP | Gravity.LEFT;
        parentView.setLayoutParams(layoutParams);
        parentView.setFocusable(false);
        parentView.setId(R.id.parent_layout);
        setContentView(parentView);

        parentView.addView(mpv = new MediaPlayerView(this));
        mpv.addMediaBtnListener(this::onMediaButtonPressed);
        mpv.setIsPlaying(MusicPlayer.isPlaying());
        mpv.setTitle(MusicPlayer.getCurrentTitle());

        Log.i("MusicPlayerActivity", "Received data: " + (getIntent() != null ? getIntent().getData() : "null") + " extras: " + (getIntent() != null ? getIntent().getExtras() : "null"));
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        int systemUi = SettingsKeeper.getSystemUiVisibility();
        setMarginsFor(SettingsKeeper.hasStatusBarVisible(systemUi), SettingsKeeper.hasNavBarVisible(systemUi), mpv);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mpv.onDestroy();
    }

    private void onMediaButtonPressed(MediaPlayerView.MediaButton btn) {
        switch (btn) {
            case end:
                MusicPlayer.stop();
            case back:
                finish();
                break;
            case play:
                MusicPlayer.play();
                mpv.setIsPlaying(true);
                break;
            case pause:
                MusicPlayer.pause();
                mpv.setIsPlaying(false);
                break;
            case skipNext:
                MusicPlayer.seekToNext();
                mpv.setTitle(MusicPlayer.getCurrentTitle());
                break;
            case skipPrev:
                MusicPlayer.seekToPrev();
                mpv.setTitle(MusicPlayer.getCurrentTitle());
                break;
            case seekFwd:
                MusicPlayer.seekForward();
                break;
            case seekBck:
                MusicPlayer.seekBack();
                break;
        }
    }
}
