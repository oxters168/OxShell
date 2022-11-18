package com.OxGames.OxShell.Data;

import com.OxGames.OxShell.AndroidHelpers;
import com.OxGames.OxShell.Serialaver;
import com.google.gson.Gson;

import java.util.HashMap;

public class SettingsKeeper {
    private static boolean fileDidExist;
    private static HashMap<String, Object> settingsCache;

    public static boolean fileDidNotExist() {
        return !fileDidExist;
    }
    public static void loadOrCreateSettings() {
        load();
        if (settingsCache == null) {
            settingsCache = new HashMap<>();
            save();
        } else
            fileDidExist = true;
    }
    public static void load() {
        if (AndroidHelpers.fileExists(Paths.SETTINGS_INTERNAL_PATH))
            settingsCache = Serialaver.loadFromJSON(Paths.SETTINGS_INTERNAL_PATH, HashMap.class);
    }
    public static void save() {
        if (!AndroidHelpers.fileExists(Paths.SETTINGS_INTERNAL_PATH))
            AndroidHelpers.makeFile(Paths.SETTINGS_INTERNAL_PATH);
        Serialaver.saveAsJSON(settingsCache, Paths.SETTINGS_INTERNAL_PATH);
    }
    public static void setValueAndSave(String key, Object value) {
        setValue(key, value);
        save();
    }
    public static void setValue(String key, Object value) {
        settingsCache.put(key, value);
    }
    public static Object getValue(String key) {
        return settingsCache.get(key);
    }
}
