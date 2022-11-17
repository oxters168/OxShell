package com.OxGames.OxShell;

import android.os.Environment;

public class Paths {
    public static final String STORAGE_DIR_EXTERNAL = Environment.getExternalStorageDirectory() + "/OxShell";
    public static final String STORAGE_DIR_INTERNAL = ActivityManager.getCurrentActivity().getFilesDir().toString();

    public static final String SHORTCUTS_DIR_EXTERNAL = AndroidHelpers.combinePaths(STORAGE_DIR_EXTERNAL, "Intents");
    public static final String SHORTCUTS_DIR_INTERNAL = AndroidHelpers.combinePaths(STORAGE_DIR_INTERNAL, "Intents");

    public static final String HOME_ITEMS_FILE_NAME = "HomeItems";
    public static final String HOME_ITEMS_EXTERNAL_PATH = AndroidHelpers.combinePaths(STORAGE_DIR_EXTERNAL, HOME_ITEMS_FILE_NAME);
    public static final String HOME_ITEMS_INTERNAL_PATH = AndroidHelpers.combinePaths(STORAGE_DIR_INTERNAL, HOME_ITEMS_FILE_NAME);

    public static final String SETTINGS_INTERNAL_PATH = AndroidHelpers.combinePaths(STORAGE_DIR_INTERNAL, "Settings");
}
