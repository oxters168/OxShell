package com.OxGames.OxShell.Data;

import android.graphics.Typeface;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;

import com.OxGames.OxShell.BuildConfig;
import com.OxGames.OxShell.Helpers.AndroidHelpers;
import com.OxGames.OxShell.Helpers.Serialaver;
import com.OxGames.OxShell.OxShellApp;

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
    public static final String PREV_VERSION_CODE = "prev_version_code";
    public static final String VERSION_NAME = "version_name";
    public static final String PREV_VERSION_NAME = "prev_version_name";

    public static final String SYSTEM_UI_VISIBILITY = "system_ui_visibility";

    private static HashMap<String, Object> settingsCache;

    private static int currentSysUIVisibility = View.SYSTEM_UI_FLAG_VISIBLE;
    public static final int FULLSCREEN_FLAGS = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
    public static final int STATUS_BAR_FLAGS = View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
    public static final int NAV_BAR_FLAGS = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
    public static boolean hasStatusBarVisible(int systemUiState) {
        return !((systemUiState & STATUS_BAR_FLAGS) == STATUS_BAR_FLAGS);
    }
    public static boolean hasNavBarVisible(int systemUiState) {
        return !((systemUiState & NAV_BAR_FLAGS) == NAV_BAR_FLAGS);
    }
    private static int toggleFullscreen(boolean onOff, int sysUiVisibility) {
        if (onOff)
            sysUiVisibility |= FULLSCREEN_FLAGS;
        else
            sysUiVisibility &= ~FULLSCREEN_FLAGS;
        return sysUiVisibility;
    }
    private static int toggleStatusBarHidden(boolean onOff, int sysUiVisibility) {
        if (onOff)
            sysUiVisibility |= STATUS_BAR_FLAGS;
        else if ((sysUiVisibility & View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) == 0) // since nav bar also uses sticky
            sysUiVisibility &= ~STATUS_BAR_FLAGS;
        else
            sysUiVisibility &= ~View.SYSTEM_UI_FLAG_FULLSCREEN;
        return sysUiVisibility;
    }
    private static int toggleNavBarHidden(boolean onOff, int sysUiVisibility) {
        if (onOff)
            sysUiVisibility |= NAV_BAR_FLAGS;
        else if ((sysUiVisibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) // since status bar also uses sticky
            sysUiVisibility &= ~NAV_BAR_FLAGS;
        else
            sysUiVisibility &= ~View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        return sysUiVisibility;
    }
    public static void setFullscreen(boolean onOff, boolean save) {
        setSystemUIState(toggleFullscreen(onOff, currentSysUIVisibility), true, save);
    }
    public static void setStatusBarHidden(boolean onOff, boolean save) {
        setSystemUIState(toggleStatusBarHidden(onOff, currentSysUIVisibility), true, save);
    }
    public static void setNavBarHidden(boolean onOff, boolean save) {
        setSystemUIState(toggleNavBarHidden(onOff, currentSysUIVisibility), true, save);
    }
    public static void setStoredFullscreen(boolean onOff) {
        setSystemUIState(toggleFullscreen(onOff, getSystemUiVisibility()), false, true);
    }
    public static void setStoredStatusBarHidden(boolean onOff) {
        setSystemUIState(toggleStatusBarHidden(onOff, getSystemUiVisibility()), false, true);
    }
    public static void setStoredNavBarHidden(boolean onOff) {
        setSystemUIState(toggleNavBarHidden(onOff, getSystemUiVisibility()), false, true);
    }
    public static int getCurrentSysUIState() {
        return currentSysUIVisibility;
    }
    public static void setSystemUIState(int uiState, boolean applyNow, boolean save) {
        if (applyNow) {
            currentSysUIVisibility = uiState;
            OxShellApp.getCurrentActivity().getWindow().getDecorView().setSystemUiVisibility(uiState);
        }
        if (save)
            setValueAndSave(SYSTEM_UI_VISIBILITY, uiState);
    }
    public static void reloadCurrentSystemUiState() {
        setSystemUIState(currentSysUIVisibility, true, false);
    }
    public static void reloadSystemUiState() {
        setSystemUIState(getSystemUiVisibility(), true, false);
    }
    public static int getSystemUiVisibility() {
        // create default if not existing
        if (!hasValue(SYSTEM_UI_VISIBILITY))
            setValueAndSave(SYSTEM_UI_VISIBILITY, View.SYSTEM_UI_FLAG_VISIBLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);

        return (int)getValue(SYSTEM_UI_VISIBILITY);
    }

    public static void loadOrCreateSettings() {
        load();
        if (settingsCache == null)
            settingsCache = new HashMap<>();
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

    public static void updateVersion() {
        int prevVersionCode = getVersionCode();
        setValueAndSave(VERSION_CODE, BuildConfig.VERSION_CODE);
        setValueAndSave(PREV_VERSION_CODE, prevVersionCode);

        String prevVersionName = getVersionName();
        setValueAndSave(VERSION_NAME, BuildConfig.VERSION_NAME);
        setValueAndSave(PREV_VERSION_NAME, prevVersionName);
    }
    public static int getVersionCode() {
        int versionCode = BuildConfig.VERSION_CODE;
        if (hasValue(VERSION_CODE))
            versionCode = (int)getValue(VERSION_CODE);
        return versionCode;
    }
    public static int getPrevVersionCode() {
        int prevVersion = BuildConfig.VERSION_CODE;
        if (hasValue(PREV_VERSION_CODE))
            prevVersion = (int)getValue(PREV_VERSION_CODE);
        return prevVersion;
    }
    public static String getVersionName() {
        String versionName = BuildConfig.VERSION_NAME;
        if (hasValue(VERSION_NAME))
            versionName = (String)getValue(VERSION_NAME);
        return versionName;
    }
    public static String getPrevVersionName() {
        String prevVersion = BuildConfig.VERSION_NAME;
        if (hasValue(PREV_VERSION_NAME))
            prevVersion = (String)getValue(PREV_VERSION_NAME);
        return prevVersion;
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
