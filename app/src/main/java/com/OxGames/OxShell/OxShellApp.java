package com.OxGames.OxShell;

import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;

// source: https://stackoverflow.com/questions/9445661/how-to-get-the-context-from-anywhere
public class OxShellApp extends Application {
    private static OxShellApp instance;
    private static int displayWidth = 0;
    private static int displayHeight = 0;

    public static OxShellApp getInstance() {
        return instance;
    }

    public static Context getContext(){
        return instance;
        // or return instance.getApplicationContext();
    }

    @Override
    public void onCreate() {
        Log.i("OxShellApp", "onCreate");
        instance = this;
        super.onCreate();
        getDisplayInfo();
    }
    @Override
    public void onTerminate() {
        Log.i("OxShellApp", "onTerminate");
        super.onTerminate();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        getDisplayInfo();
    }

    @Override
    public void onLowMemory() {
        Log.i("OxShellApp", "onLowMemory");
        super.onLowMemory();
    }

    private void getDisplayInfo() {
        DisplayMetrics dm = getResources().getDisplayMetrics();
        displayWidth = dm.widthPixels;
        displayHeight = dm.heightPixels;
    }
    public static int getDisplayWidth() {
        return displayWidth;
    }
    public static int getDisplayHeight() {
        return displayHeight;
    }

//    public DisplayMetrics getDisplayMetrics() {
//        //DisplayMetrics displayMetrics = new DisplayMetrics();
//        //getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
//        return getResources().getDisplayMetrics();
//    }
}
