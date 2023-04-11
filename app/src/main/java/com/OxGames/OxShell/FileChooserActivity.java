package com.OxGames.OxShell;

import android.os.Bundle;

import androidx.annotation.Nullable;

import com.OxGames.OxShell.Data.SettingsKeeper;

public class FileChooserActivity extends PagedActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chooser);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        int systemUi = SettingsKeeper.getSystemUiVisibility();
        setMarginsFor(SettingsKeeper.hasStatusBarVisible(systemUi), SettingsKeeper.hasNavBarVisible(systemUi), R.id.explorer_list);
    }
}
