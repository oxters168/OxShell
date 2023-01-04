package com.OxGames.OxShell.Data;

import com.OxGames.OxShell.Helpers.AndroidHelpers;
import com.OxGames.OxShell.Helpers.Serialaver;

import java.util.HashMap;

public class SettingsKeeper {
    public static final String TIMES_LOADED = "times_loaded";
    private static boolean fileDidExist;
    private static HashMap<String, Object> settingsCache;

    public static boolean fileDidNotExist() {
        return !fileDidExist;
    }
    public static void loadOrCreateSettings() {
        load();
        if (settingsCache == null) {
            settingsCache = new HashMap<>();
            SettingsKeeper.setValueAndSave(TIMES_LOADED, Integer.valueOf(1));
            //save();
        } else {
            fileDidExist = true;
            if (SettingsKeeper.hasValue(TIMES_LOADED)) {
                int timesLoaded = ((Double)SettingsKeeper.getValue(TIMES_LOADED)).intValue(); // even if saved as Integer for some reason it comes back as Double
                SettingsKeeper.setValueAndSave(TIMES_LOADED, timesLoaded + 1);
            }
        }
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
    public static boolean hasValue(String key) {
        return settingsCache.containsKey(key);
    }
}
