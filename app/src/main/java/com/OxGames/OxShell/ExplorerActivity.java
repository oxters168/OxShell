package com.OxGames.OxShell;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;

import com.OxGames.OxShell.Data.SettingsKeeper;

import java.util.function.Consumer;

public class ExplorerActivity extends PagedActivity {
    View explorerView;
    private Consumer<Boolean> onDynamicInputShown = (onOff) -> {
        explorerView.setVisibility(onOff ? View.GONE : View.VISIBLE);
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Log.d("ExplorerActivity", "onCreate");
        setContentView(R.layout.activity_explorer);
        explorerView = findViewById(R.id.explorer_list);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        int systemUi = SettingsKeeper.getSystemUiVisibility();
        setMarginsFor(SettingsKeeper.hasStatusBarVisible(systemUi), SettingsKeeper.hasNavBarVisible(systemUi), R.id.parent_layout);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getDynamicInput().addShownListener(onDynamicInputShown);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        getDynamicInput().removeShownListener(onDynamicInputShown);
    }
}