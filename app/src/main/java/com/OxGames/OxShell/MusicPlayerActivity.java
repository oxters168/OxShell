package com.OxGames.OxShell;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;

import com.OxGames.OxShell.Data.SettingsKeeper;
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
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        int systemUi = SettingsKeeper.getSystemUiVisibility();
        setMarginsFor(SettingsKeeper.hasStatusBarVisible(systemUi), SettingsKeeper.hasNavBarVisible(systemUi), mpv);
    }
}
