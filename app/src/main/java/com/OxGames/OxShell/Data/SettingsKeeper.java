package com.OxGames.OxShell.Data;

import android.graphics.Typeface;
import android.view.KeyEvent;

import com.OxGames.OxShell.Helpers.AndroidHelpers;
import com.OxGames.OxShell.Helpers.Serialaver;

import java.util.HashMap;

public class SettingsKeeper {
    public static final String TIMES_LOADED = "times_loaded";

    public static final String PRIMARY_INPUT = "primary_input";
    public static final String SECONDARY_INPUT = "secondary_input";
    public static final String CANCEL_INPUT = "cancel_input";
    public static final String HOME_COMBOS = "home_combos";
    public static final String RECENTS_COMBOS = "recents_combos";

    public static final String HOME_ITEM_SCALE = "home_item_scale";
    public static final String HOME_SELECTION_ALPHA = "home_selection_alpha";
    public static final String HOME_NON_SELECTION_ALPHA = "home_non_selection_alpha";
    public static final String HOME_BEHIND_INNER_ALPHA = "home_behind_inner_alpha";
    public static final String FONT_REF = "font_ref";
    public static final String VERSION_CODE = "version_code";

    private static boolean fileDidExist;
    private static HashMap<String, Object> settingsCache;

    public static boolean fileDidNotExist() {
        return !fileDidExist;
    }
    public static void loadOrCreateSettings() {
        load();
        if (settingsCache == null) {
            settingsCache = new HashMap<>();
            SettingsKeeper.setValueAndSave(TIMES_LOADED, 1);
        } else {
            fileDidExist = true;
            if (SettingsKeeper.hasValue(TIMES_LOADED)) {
                int timesLoaded = (Integer)SettingsKeeper.getValue(TIMES_LOADED); // even if saved as Integer for some reason it comes back as Double
                SettingsKeeper.setValueAndSave(TIMES_LOADED, timesLoaded + 1);
            }
        }
    }
    public static void load() {
        if (AndroidHelpers.fileExists(Paths.SETTINGS_INTERNAL_PATH))
            settingsCache = (HashMap<String, Object>)Serialaver.loadFromFSTJSON(Paths.SETTINGS_INTERNAL_PATH);
    }
    public static void save() {
        if (!AndroidHelpers.fileExists(Paths.SETTINGS_INTERNAL_PATH))
            AndroidHelpers.makeFile(Paths.SETTINGS_INTERNAL_PATH);
        Serialaver.saveAsFSTJSON(settingsCache, Paths.SETTINGS_INTERNAL_PATH);
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

    public static Typeface getFont() {
        if (hasValue(FONT_REF))
            return ((FontRef)getValue(FONT_REF)).getFont();
        return null;
    }
    public static KeyCombo[] getPrimaryInput() {
        // create default if not existing
        if (!hasValue(PRIMARY_INPUT))
            setValue(PRIMARY_INPUT, new KeyCombo[] { KeyCombo.createUpCombo(KeyEvent.KEYCODE_BUTTON_A), KeyCombo.createUpCombo(KeyEvent.KEYCODE_ENTER), KeyCombo.createUpCombo(KeyEvent.KEYCODE_NUMPAD_ENTER) });

        return ((KeyCombo[])getValue(PRIMARY_INPUT));
    }
    public static KeyCombo[] getSecondaryInput() {
        // create default if not existing
        if (!hasValue(SECONDARY_INPUT))
            setValue(SECONDARY_INPUT, new KeyCombo[] { KeyCombo.createUpCombo(KeyEvent.KEYCODE_BUTTON_Y), KeyCombo.createUpCombo(KeyEvent.KEYCODE_MENU), KeyCombo.createUpCombo(KeyEvent.KEYCODE_SPACE) });

        return ((KeyCombo[])getValue(SECONDARY_INPUT));
    }
    public static KeyCombo[] getCancelInput() {
        // create default if not existing
        if (!hasValue(CANCEL_INPUT))
            setValue(CANCEL_INPUT, new KeyCombo[] { KeyCombo.createUpCombo(KeyEvent.KEYCODE_BUTTON_B), KeyCombo.createUpCombo(KeyEvent.KEYCODE_BACK), KeyCombo.createUpCombo(KeyEvent.KEYCODE_ESCAPE) });

        return ((KeyCombo[])getValue(CANCEL_INPUT));
    }
    public static KeyCombo[] getHomeCombos() {
        // create default if not existing
        if (!hasValue(HOME_COMBOS))
            setValue(HOME_COMBOS, new KeyCombo[] { KeyCombo.createUpCombo(KeyEvent.KEYCODE_BUTTON_SELECT, KeyEvent.KEYCODE_BUTTON_B) });

        return ((KeyCombo[])getValue(HOME_COMBOS));
    }
    public static KeyCombo[] getRecentsCombos() {
        // create default if not existing
        if (!hasValue(RECENTS_COMBOS))
            setValue(RECENTS_COMBOS, new KeyCombo[] { KeyCombo.createUpCombo(KeyEvent.KEYCODE_BUTTON_SELECT, KeyEvent.KEYCODE_BUTTON_X) });

        return ((KeyCombo[])getValue(RECENTS_COMBOS));
    }
}
