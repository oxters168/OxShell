package com.OxGames.OxShell;

import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;
import android.util.DisplayMetrics;
import android.util.Log;

import androidx.annotation.NonNull;

// source: https://stackoverflow.com/questions/9445661/how-to-get-the-context-from-anywhere
public class OxShellApp extends Application {
    private static OxShellApp instance;
    private static int displayWidth = 0;
    private static int displayHeight = 0;
    private static int smallestScreenWidthDp = 0;
    private static int densityDpi = 0;

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
        Log.e("OxShellApp", "Low memory");
        super.onLowMemory();
    }

    public static int getNavBarHeight() {
        int result = 0;
        int resourceId = instance.getResources().getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0)
            result = instance.getResources().getDimensionPixelSize(resourceId);
        else
            Log.e("OxShellApp", "Failed to retrieve resource id for android.dimen.navigation_bar_height");
        //Log.i("OxShellApp", "Navbar Height: " + result);
        return result;
    }
    public static int getStatusBarHeight() {
        int result = 0;
        int resourceId = instance.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0)
            result = instance.getResources().getDimensionPixelSize(resourceId);
        else
            Log.e("OxShellApp", "Failed to retrieve resource id for android.dimen.status_bar_height");
        //Log.i("OxShellApp", "StatusBar Height: " + result);
        return result;
    }
    private void getDisplayInfo() {
        DisplayMetrics dm = getResources().getDisplayMetrics();
        displayWidth = dm.widthPixels;
        displayHeight = dm.heightPixels;
        Configuration cfg = getResources().getConfiguration();
        smallestScreenWidthDp = cfg.smallestScreenWidthDp;
        densityDpi = cfg.densityDpi;
        Log.i("OxShellApp", "Display width: " + displayWidth + "\nDisplay height: " + displayHeight + "\nSmallest screen width: " + smallestScreenWidthDp + "\nDensity DPI: " + densityDpi);
    }
    public static int getDisplayWidth() {
        return displayWidth;
    }
    public static int getDisplayHeight() {
        return displayHeight;
    }
    public static int getSmallestScreenWidthDp() {
        return smallestScreenWidthDp;
    }
    public static int getDensityDpi() {
        return densityDpi;
    }
}
