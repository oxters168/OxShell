package com.OxGames.OxShell;

import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;

import androidx.annotation.Nullable;

import com.OxGames.OxShell.Helpers.ActivityManager;
import com.OxGames.OxShell.Views.PromptView;

import java.io.File;
import java.util.function.Consumer;

public class HomeActivity extends PagedActivity {
    View homeView;
    private Consumer<Boolean> onDynamicInputShown = (onOff) -> {
        homeView.setVisibility(onOff ? View.GONE : View.VISIBLE);
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d("HomeActivity", "external-path " + Environment.getExternalStorageDirectory());
        Log.d("HomeActivity", "external-files-path " + getExternalFilesDir(null));
        Log.d("HomeActivity", "cache-path " + getCacheDir());
        Log.d("HomeActivity", "external-cache-path " + getExternalCacheDir());
        Log.d("HomeActivity", "files-path " + getFilesDir());
        File[] extMediaDirs = getExternalMediaDirs();
        for (int i = 0; i < extMediaDirs.length; i++)
            Log.d("HomeActivity", "external-media-path_" + i + ": " + extMediaDirs[i]);
        // Log.d("HomeActivity", ShellCommander.run("mount"));
//        ExplorerBehaviour beh = new ExplorerBehaviour();
//        beh.setDirectory(getFilesDir().getPath());
//        for (File subPath : beh.listContents())
//            Log.d("HomeActivity", "DataContents: " + subPath);

        currentPage = ActivityManager.Page.home;
        ActivityManager.setCurrent(currentPage);
        setContentView(R.layout.activity_home);
        initViewsTable();

        //HomeManager.init();
        goTo(ActivityManager.Page.home);
        //Log.d("HomeActivity", "onCreate");
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        //setMarginsFor(R.id.packages_list, R.id.settings_view, R.id.customize_home_view, R.id.assoc_list_view, R.id.selectdirs_view, R.id.shortcuts_view);
        showAnnoyingDialog();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getDynamicInput().addShownListener(onDynamicInputShown);
        //showAnnoyingDialog();
        //Log.d("HomeActivity", "onResume");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getDynamicInput().removeShownListener(onDynamicInputShown);
        //Log.d("HomeActivity", "onDestroy");
    }

    @Override
    protected void initViewsTable() {
        allPages.put(ActivityManager.Page.home, homeView = findViewById(R.id.home_view));
    }

    private void showAnnoyingDialog() {
        if (!BuildConfig.GOLD) {
            Log.i("HomeActivity", "Not running in gold");
            PromptView prompt = getPrompt();
            prompt.setCenteredPosition(Math.round(OxShellApp.getDisplayWidth() / 2f), Math.round(OxShellApp.getDisplayHeight() / 2f));
            prompt.setMessage("Thank you for using Ox Shell, please consider supporting us by purchasing the app from the store");
            prompt.setMiddleBtn("Got it", () -> { prompt.setShown(false); }, KeyEvent.KEYCODE_BUTTON_A, KeyEvent.KEYCODE_BUTTON_START);
            prompt.setShown(true);
        } else {
            Log.i("HomeActivity", "Running in gold");
        }
    }

//    public void getOverlayPermissionBtn(View view) {
//        // Check if Android M or higher
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            // Show alert dialog to the user saying a separate permission is needed
//            // Launch the settings activity if the user prefers
//            Intent myIntent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
//            startActivity(myIntent);
//        }
//    }
}