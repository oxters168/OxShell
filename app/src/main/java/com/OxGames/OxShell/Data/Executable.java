package com.OxGames.OxShell.Data;

import android.util.Log;

import java.io.Serializable;
import java.util.UUID;

public class Executable implements Serializable {
    private UUID launchDataRef;
    private String path;

    public Executable(UUID launchDataRef, String path) {
        this.launchDataRef = launchDataRef;
        this.path = path;
    }
    public void run() {
        IntentLaunchData launcher = getLaunchIntent();
        if (launcher != null) {
            if (PackagesCache.isPackageInstalled(launcher.getPackageName()))
                launcher.launch(path);
            else
                Log.e("IntentShortcutsView", "Failed to launch, " + launcher.getPackageName() + " is not installed on the device");
        } else
            Log.e("IntentShortcutsView", "Failed to launch, launch intent (" + launchDataRef + ") not found");
    }
    public IntentLaunchData getLaunchIntent() {
        return ShortcutsCache.getIntent(launchDataRef);
    }
}
