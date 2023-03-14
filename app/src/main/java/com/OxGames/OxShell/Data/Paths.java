package com.OxGames.OxShell.Data;

import android.os.Environment;

import com.OxGames.OxShell.Helpers.ActivityManager;
import com.OxGames.OxShell.Helpers.AndroidHelpers;
import com.OxGames.OxShell.OxShellApp;

public class Paths {
    public static final String STORAGE_DIR_EXTERNAL = AndroidHelpers.combinePaths(Environment.getExternalStorageDirectory().toString(), "/OxShell");
    public static final String STORAGE_DIR_INTERNAL = OxShellApp.getContext().getExternalFilesDir(null).toString();
    //public static final String STORAGE_DIR_INTERNAL = ActivityManager.getCurrentActivity().getFilesDir().toString();

    public static final String SHADER_ITEMS_DIR_EXTERNAL = AndroidHelpers.combinePaths(STORAGE_DIR_EXTERNAL, "Shader");
    public static final String SHADER_ITEMS_DIR_INTERNAL = AndroidHelpers.combinePaths(STORAGE_DIR_INTERNAL, "Shader");

    public static final String LOGCAT_DIR_EXTERNAL = AndroidHelpers.combinePaths(STORAGE_DIR_EXTERNAL, "Logs");
    public static final String LOGCAT_DIR_INTERNAL = AndroidHelpers.combinePaths(STORAGE_DIR_INTERNAL, "Logs");

    public static final String SHORTCUTS_DIR_EXTERNAL = AndroidHelpers.combinePaths(STORAGE_DIR_EXTERNAL, "Intents");
    public static final String SHORTCUTS_DIR_INTERNAL = AndroidHelpers.combinePaths(STORAGE_DIR_INTERNAL, "Intents");

    public static final String HOME_ITEMS_DIR_EXTERNAL = AndroidHelpers.combinePaths(STORAGE_DIR_EXTERNAL, "Home");
    public static final String HOME_ITEMS_DIR_INTERNAL = AndroidHelpers.combinePaths(STORAGE_DIR_INTERNAL, "Home");
    public static final String ICONS_DIR_INTERNAL = AndroidHelpers.combinePaths(STORAGE_DIR_INTERNAL, "Icons");
    public static final String HOME_ITEMS_FILE_NAME = "HomeItems";
    public static final String HOME_ITEMS_CATS = "_cats";
    public static final String HOME_ITEMS_DEFAULTS = "_defaults";
    public static final String HOME_ITEMS_APPS = "_apps";
    public static final String HOME_ITEMS_ASSOCS = "_assocs";

    public static final String SETTINGS_INTERNAL_PATH = AndroidHelpers.combinePaths(STORAGE_DIR_INTERNAL, "Settings");
}
