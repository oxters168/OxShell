package com.OxGames.OxShell;

import android.app.Application;
import android.content.Context;
import android.util.Log;

// source: https://stackoverflow.com/questions/9445661/how-to-get-the-context-from-anywhere
public class OxShellApp extends Application {
    private static OxShellApp instance;

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
    }
    @Override
    public void onTerminate() {
        Log.i("OxShellApp", "onTerminate");
        super.onTerminate();
    }

    @Override
    public void onLowMemory() {
        Log.i("OxShellApp", "onLowMemory");
        super.onLowMemory();
    }
}
