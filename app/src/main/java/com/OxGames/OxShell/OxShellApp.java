package com.OxGames.OxShell;

import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;
import android.util.DisplayMetrics;
import android.util.Log;

import androidx.annotation.NonNull;

import com.OxGames.OxShell.Data.DataLocation;
import com.OxGames.OxShell.Data.FontRef;
import com.OxGames.OxShell.Data.SettingsKeeper;
import com.OxGames.OxShell.Data.ShortcutsCache;
import com.OxGames.OxShell.Helpers.InputHandler;

// source: https://stackoverflow.com/questions/9445661/how-to-get-the-context-from-anywhere
public class OxShellApp extends Application {
    private static OxShellApp instance;
    private static InputHandler inputHandler;

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
        inputHandler = new InputHandler();
        super.onCreate();

        SettingsKeeper.loadOrCreateSettings();
        // in the future we would use this value to upgrade the serialization
        SettingsKeeper.setValueAndSave(SettingsKeeper.VERSION_CODE, BuildConfig.VERSION_CODE);
        Log.i("PagedActivity", "Time(s) loaded: " + SettingsKeeper.getTimesLoaded());
        if (SettingsKeeper.getTimesLoaded() < 1) {
            ShortcutsCache.createAndStoreDefaults();
            SettingsKeeper.setValueAndSave(SettingsKeeper.FONT_REF, FontRef.from("Fonts/exo.regular.otf", DataLocation.asset));
            Log.i("PagedActivity", "First time launch");
        } else {
            ShortcutsCache.readIntentsFromDisk();
            Log.i("PagedActivity", "Not first time launch");
        }
        SettingsKeeper.incrementTimesLoaded();

        getDisplayInfo();
    }
    @Override
    public void onTerminate() {
        Log.i("OxShellApp", "onTerminate");
        super.onTerminate();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        // it seems when the app launches from an app that is forced landscape into portrait, the first time onConfigurationChanged should happen does not fire
        super.onConfigurationChanged(newConfig);
        getDisplayInfo();
    }

    @Override
    public void onLowMemory() {
        Log.e("OxShellApp", "Low memory");
        super.onLowMemory();
    }

    public static InputHandler getInputHandler() {
        return inputHandler;
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
        int displayWidth = dm.widthPixels;
        int displayHeight = dm.heightPixels;
        Configuration cfg = getResources().getConfiguration();
        int smallestScreenWidthDp = cfg.smallestScreenWidthDp;
        int densityDpi = cfg.densityDpi;
        Log.i("OxShellApp", "Display width: " + displayWidth + "\nDisplay height: " + displayHeight + "\nSmallest screen width: " + smallestScreenWidthDp + "\nDensity DPI: " + densityDpi);
    }
    public static int getDisplayWidth() {
        DisplayMetrics dm = instance.getResources().getDisplayMetrics();
        return dm.widthPixels;
    }
    public static int getDisplayHeight() {
        DisplayMetrics dm = instance.getResources().getDisplayMetrics();
        return dm.heightPixels;
    }
    public static int getSmallestScreenWidthDp() {
        Configuration cfg = instance.getResources().getConfiguration();
        return cfg.smallestScreenWidthDp;
    }
    public static int getDensityDpi() {
        Configuration cfg = instance.getResources().getConfiguration();
        return cfg.densityDpi;
    }
}
