package com.OxGames.OxShell;

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
        if (AndroidHelpers.fileExists(Paths.SETTINGS_INTERNAL_PATH)) {
            Gson gson = new Gson();
            settingsCache = gson.fromJson(AndroidHelpers.readFile(Paths.SETTINGS_INTERNAL_PATH), HashMap.class);
        }
    }
    public static void save() {
        Gson gson = new Gson();
        if (!AndroidHelpers.fileExists(Paths.SETTINGS_INTERNAL_PATH))
            AndroidHelpers.makeFile(Paths.SETTINGS_INTERNAL_PATH);
        AndroidHelpers.writeToFile(Paths.SETTINGS_INTERNAL_PATH, gson.toJson(settingsCache));
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
