package com.OxGames.OxShell.Data;

import android.graphics.Typeface;
import android.util.Log;
import android.view.KeyEvent;

import com.OxGames.OxShell.Helpers.AndroidHelpers;
import com.OxGames.OxShell.Helpers.Serialaver;

import java.util.HashMap;

public class SettingsKeeper {
    public static final int BG_TYPE_SHADER = 0;
    public static final int BG_TYPE_IMAGE = 1;
    public static final int BG_TYPE_VIDEO = 2;

    public static final String TIMES_LOADED = "times_loaded";

    public static final String SUPER_PRIMARY_INPUT = "super_primary_input";
    public static final String PRIMARY_INPUT = "primary_input";
    public static final String SECONDARY_INPUT = "secondary_input";
    public static final String CANCEL_INPUT = "cancel_input";
    public static final String HOME_COMBOS = "home_combos";
    public static final String RECENTS_COMBOS = "recents_combos";
    public static final String EXPLORER_HIGHLIGHT_INPUT = "explorer_highlight_input";
    public static final String EXPLORER_GO_UP_INPUT = "explorer_go_up_input";
    public static final String EXPLORER_GO_BACK_INPUT = "explorer_go_back_input";
    public static final String EXPLORER_EXIT_INPUT = "explorer_exit_input";
    public static final String NAVIGATE_UP = "navigate_up";
    public static final String NAVIGATE_DOWN = "navigate_down";
    public static final String NAVIGATE_LEFT = "navigate_left";
    public static final String NAVIGATE_RIGHT = "navigate_right";
    public static final String SHOW_DEBUG_INPUT = "show_debug_input";

    public static final String TV_BG_TYPE = "tv_bg_type";

    public static final String HOME_ITEM_SCALE = "home_item_scale";
    public static final String HOME_SELECTION_ALPHA = "home_selection_alpha";
    public static final String HOME_NON_SELECTION_ALPHA = "home_non_selection_alpha";
    public static final String HOME_BEHIND_INNER_ALPHA = "home_behind_inner_alpha";
    public static final String FONT_REF = "font_ref";
    public static final String VERSION_CODE = "version_code";

    //private static boolean fileDidExist;
    private static HashMap<String, Object> settingsCache;

//    public static boolean fileDidNotExist() {
//        return !fileDidExist;
//    }
    public static void loadOrCreateSettings() {
        load();
        if (settingsCache == null) {
            settingsCache = new HashMap<>();
            //SettingsKeeper.setValueAndSave(TIMES_LOADED, 1);
        }
//        else {
//            fileDidExist = true;
//            if (SettingsKeeper.hasValue(TIMES_LOADED)) {
//                int timesLoaded = (Integer)SettingsKeeper.getValue(TIMES_LOADED); // even if saved as Integer for some reason it comes back as Double
//                SettingsKeeper.setValueAndSave(TIMES_LOADED, timesLoaded + 1);
//            }
//        }
    }
    public static void incrementTimesLoaded() {
        setValueAndSave(TIMES_LOADED, getTimesLoaded() + 1);
    }
    public static int getTimesLoaded() {
        int timesLoaded = 0;
        if (hasValue(TIMES_LOADED))
            timesLoaded = (Integer)getValue(TIMES_LOADED);
        return timesLoaded;
    }
    public static void load() {
        if (AndroidHelpers.fileExists(Paths.SETTINGS_INTERNAL_PATH)) {
            try {
                settingsCache = (HashMap<String, Object>)Serialaver.loadFromFSTJSON(Paths.SETTINGS_INTERNAL_PATH);
            } catch (Exception e) {
                Log.e("SettingsKeeper", "Failed to read settings: " + e);
            }
        }
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
        if (settingsCache == null)
            loadOrCreateSettings();
        settingsCache.put(key, value);
    }
    public static Object getValue(String key) {
        if (settingsCache == null)
            loadOrCreateSettings();
        return settingsCache.get(key);
    }
    public static boolean hasValue(String key) {
        if (settingsCache == null)
            loadOrCreateSettings();
        return settingsCache.containsKey(key);
    }

    public static Typeface getFont() {
        if (hasValue(FONT_REF))
            return ((FontRef)getValue(FONT_REF)).getFont();
        return null;
    }

    public static int getTvBgType() {
        if (hasValue(TV_BG_TYPE))
            return (int)getValue(TV_BG_TYPE);
        return BG_TYPE_SHADER;
    }

    public static KeyCombo[] getDefaultInputValueFor(String key) {
        switch (key) {
            case (SUPER_PRIMARY_INPUT):
                return new KeyCombo[] { KeyCombo.createUpCombo(KeyEvent.KEYCODE_BUTTON_START), KeyCombo.createUpCombo(true, KeyEvent.KEYCODE_CTRL_LEFT, KeyEvent.KEYCODE_ENTER), KeyCombo.createUpCombo(true, KeyEvent.KEYCODE_CTRL_RIGHT, KeyEvent.KEYCODE_ENTER), KeyCombo.createUpCombo(true, KeyEvent.KEYCODE_CTRL_LEFT, KeyEvent.KEYCODE_NUMPAD_ENTER), KeyCombo.createUpCombo(true, KeyEvent.KEYCODE_CTRL_RIGHT, KeyEvent.KEYCODE_NUMPAD_ENTER) };
            case (PRIMARY_INPUT):
                return new KeyCombo[] { KeyCombo.createUpCombo(KeyEvent.KEYCODE_BUTTON_A), KeyCombo.createUpCombo(KeyEvent.KEYCODE_ENTER), KeyCombo.createUpCombo(KeyEvent.KEYCODE_NUMPAD_ENTER), KeyCombo.createUpCombo(KeyEvent.KEYCODE_DPAD_CENTER) };
            case (SECONDARY_INPUT):
                return new KeyCombo[] { KeyCombo.createUpCombo(KeyEvent.KEYCODE_BUTTON_Y), KeyCombo.createUpCombo(KeyEvent.KEYCODE_MENU), KeyCombo.createUpCombo(KeyEvent.KEYCODE_SPACE) };
            case (EXPLORER_HIGHLIGHT_INPUT):
                return new KeyCombo[] { KeyCombo.createDownCombo(0, KeyCombo.defaultRepeatStartDelay, KeyCombo.defaultRepeatTime, KeyEvent.KEYCODE_BUTTON_X) };
            case (EXPLORER_GO_UP_INPUT):
                return new KeyCombo[] { KeyCombo.createUpCombo(KeyEvent.KEYCODE_BUTTON_B) };
            case (EXPLORER_GO_BACK_INPUT):
                return new KeyCombo[] { KeyCombo.createUpCombo(true, KeyEvent.KEYCODE_BUTTON_L1, KeyEvent.KEYCODE_BUTTON_B) };
            case (EXPLORER_EXIT_INPUT):
                return new KeyCombo[] { KeyCombo.createUpCombo(false, KeyEvent.KEYCODE_BUTTON_L1, KeyEvent.KEYCODE_BUTTON_R1), KeyCombo.createUpCombo(KeyEvent.KEYCODE_BACK) };
            case (CANCEL_INPUT):
                return new KeyCombo[] { KeyCombo.createUpCombo(KeyEvent.KEYCODE_BUTTON_B), KeyCombo.createUpCombo(KeyEvent.KEYCODE_BACK), KeyCombo.createUpCombo(KeyEvent.KEYCODE_ESCAPE) };
            case (HOME_COMBOS):
                return new KeyCombo[] { KeyCombo.createUpCombo(true, KeyEvent.KEYCODE_BUTTON_SELECT, KeyEvent.KEYCODE_BUTTON_B) };
            case (RECENTS_COMBOS):
                return new KeyCombo[] { KeyCombo.createUpCombo(true, KeyEvent.KEYCODE_BUTTON_SELECT, KeyEvent.KEYCODE_BUTTON_X) };
            case (NAVIGATE_UP):
                return new KeyCombo[] { KeyCombo.createDownCombo(0, KeyCombo.defaultRepeatStartDelay, KeyCombo.defaultRepeatTime, KeyEvent.KEYCODE_DPAD_UP) };
            case (NAVIGATE_DOWN):
                return new KeyCombo[] { KeyCombo.createDownCombo(0, KeyCombo.defaultRepeatStartDelay, KeyCombo.defaultRepeatTime, KeyEvent.KEYCODE_DPAD_DOWN) };
            case (NAVIGATE_LEFT):
                return new KeyCombo[] { KeyCombo.createDownCombo(0, KeyCombo.defaultRepeatStartDelay, KeyCombo.defaultRepeatTime, KeyEvent.KEYCODE_DPAD_LEFT) };
            case (NAVIGATE_RIGHT):
                return new KeyCombo[] { KeyCombo.createDownCombo(0, KeyCombo.defaultRepeatStartDelay, KeyCombo.defaultRepeatTime, KeyEvent.KEYCODE_DPAD_RIGHT) };
            case (SHOW_DEBUG_INPUT):
                return new KeyCombo[] { KeyCombo.createUpCombo(KeyEvent.KEYCODE_GRAVE), KeyCombo.createUpCombo(false, KeyEvent.KEYCODE_BUTTON_L1, KeyEvent.KEYCODE_BUTTON_R1, KeyEvent.KEYCODE_BUTTON_SELECT, KeyEvent.KEYCODE_BUTTON_START) };
        }
        return new KeyCombo[0];
    }
    public static KeyCombo[] getSuperPrimaryInput() {
        // create default if not existing
        if (!hasValue(SUPER_PRIMARY_INPUT))
            setValueAndSave(SUPER_PRIMARY_INPUT, getDefaultInputValueFor(SUPER_PRIMARY_INPUT));

        return ((KeyCombo[])getValue(SUPER_PRIMARY_INPUT));
    }
    public static KeyCombo[] getPrimaryInput() {
        // create default if not existing
        if (!hasValue(PRIMARY_INPUT))
            setValueAndSave(PRIMARY_INPUT, getDefaultInputValueFor(PRIMARY_INPUT));

        return ((KeyCombo[])getValue(PRIMARY_INPUT));
    }
    public static KeyCombo[] getSecondaryInput() {
        // create default if not existing
        if (!hasValue(SECONDARY_INPUT))
            setValueAndSave(SECONDARY_INPUT, getDefaultInputValueFor(SECONDARY_INPUT));

        return ((KeyCombo[])getValue(SECONDARY_INPUT));
    }
    public static KeyCombo[] getExplorerHighlightInput() {
        // create default if not existing
        if (!hasValue(EXPLORER_HIGHLIGHT_INPUT))
            setValueAndSave(EXPLORER_HIGHLIGHT_INPUT, getDefaultInputValueFor(EXPLORER_HIGHLIGHT_INPUT));

        return ((KeyCombo[])getValue(EXPLORER_HIGHLIGHT_INPUT));
    }
    public static KeyCombo[] getExplorerGoUpInput() {
        // create default if not existing
        if (!hasValue(EXPLORER_GO_UP_INPUT))
            setValueAndSave(EXPLORER_GO_UP_INPUT, getDefaultInputValueFor(EXPLORER_GO_UP_INPUT));

        return ((KeyCombo[])getValue(EXPLORER_GO_UP_INPUT));
    }
    public static KeyCombo[] getExplorerGoBackInput() {
        // create default if not existing
        if (!hasValue(EXPLORER_GO_BACK_INPUT))
            setValueAndSave(EXPLORER_GO_BACK_INPUT, getDefaultInputValueFor(EXPLORER_GO_BACK_INPUT));

        return ((KeyCombo[])getValue(EXPLORER_GO_BACK_INPUT));
    }
    public static KeyCombo[] getExplorerExitInput() {
        // create default if not existing
        if (!hasValue(EXPLORER_EXIT_INPUT))
            setValueAndSave(EXPLORER_EXIT_INPUT, getDefaultInputValueFor(EXPLORER_EXIT_INPUT));

        return ((KeyCombo[])getValue(EXPLORER_EXIT_INPUT));
    }
    public static KeyCombo[] getCancelInput() {
        // create default if not existing
        if (!hasValue(CANCEL_INPUT))
            setValueAndSave(CANCEL_INPUT, getDefaultInputValueFor(CANCEL_INPUT));

        return ((KeyCombo[])getValue(CANCEL_INPUT));
    }
    public static KeyCombo[] getHomeCombos() {
        // create default if not existing
        if (!hasValue(HOME_COMBOS))
            setValueAndSave(HOME_COMBOS, getDefaultInputValueFor(HOME_COMBOS));

        return ((KeyCombo[])getValue(HOME_COMBOS));
    }
    public static KeyCombo[] getRecentsCombos() {
        // create default if not existing
        if (!hasValue(RECENTS_COMBOS))
            setValueAndSave(RECENTS_COMBOS, getDefaultInputValueFor(RECENTS_COMBOS));

        return ((KeyCombo[])getValue(RECENTS_COMBOS));
    }
    public static KeyCombo[] getNavigateUp() {
        // create default if not existing
        if (!hasValue(NAVIGATE_UP))
            setValueAndSave(NAVIGATE_UP, getDefaultInputValueFor(NAVIGATE_UP));

        return ((KeyCombo[])getValue(NAVIGATE_UP));
    }
    public static KeyCombo[] getNavigateDown() {
        // create default if not existing
        if (!hasValue(NAVIGATE_DOWN))
            setValueAndSave(NAVIGATE_DOWN, getDefaultInputValueFor(NAVIGATE_DOWN));

        return ((KeyCombo[])getValue(NAVIGATE_DOWN));
    }
    public static KeyCombo[] getNavigateLeft() {
        // create default if not existing
        if (!hasValue(NAVIGATE_LEFT))
            setValueAndSave(NAVIGATE_LEFT, getDefaultInputValueFor(NAVIGATE_LEFT));

        return ((KeyCombo[])getValue(NAVIGATE_LEFT));
    }
    public static KeyCombo[] getNavigateRight() {
        // create default if not existing
        if (!hasValue(NAVIGATE_RIGHT))
            setValueAndSave(NAVIGATE_RIGHT, getDefaultInputValueFor(NAVIGATE_RIGHT));

        return ((KeyCombo[])getValue(NAVIGATE_RIGHT));
    }
    public static KeyCombo[] getShowDebugInput() {
        // create default if not existing
        if (!hasValue(SHOW_DEBUG_INPUT))
            setValueAndSave(SHOW_DEBUG_INPUT, getDefaultInputValueFor(SHOW_DEBUG_INPUT));

        return ((KeyCombo[])getValue(SHOW_DEBUG_INPUT));
    }
}
