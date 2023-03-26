package com.OxGames.OxShell;

import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;

import com.OxGames.OxShell.Data.KeyComboAction;
import com.OxGames.OxShell.Data.SettingsKeeper;
import com.OxGames.OxShell.Helpers.InputHandler;

import java.util.Arrays;

public class AccessService extends AccessibilityService {
    private static AccessService instance;
    private InputHandler inputHandler;
    //private static final String INPUT_TAG = "ACCESS_SERVICE";
    @Override
    public void onServiceConnected() {
        super.onServiceConnected();
        Log.i("AccessService", "onServiceConnected");
        instance = this;
        refreshInputCombos();
    }
    public static void refreshInputCombos() {
        if (instance != null) {
            if (instance.inputHandler == null)
                instance.inputHandler = new InputHandler();
            //if (!instance.inputHandler.tagHasActions(InputHandler.ALWAYS_ON_TAG)) {
            instance.inputHandler.clearKeyComboActions();
            instance.inputHandler.addKeyComboActions(Arrays.stream(SettingsKeeper.getHomeCombos()).map(combo -> new KeyComboAction(combo, AccessService::goHome)).toArray(KeyComboAction[]::new));
            instance.inputHandler.addKeyComboActions(Arrays.stream(SettingsKeeper.getRecentsCombos()).map(combo -> new KeyComboAction(combo, AccessService::showRecentApps)).toArray(KeyComboAction[]::new));
            //}
        }
    }

    public static void toggleBlockingInput(boolean onOff) {
        if (instance != null && instance.inputHandler != null)
            instance.inputHandler.toggleBlockingInput(onOff);
    }

//    @Override
//    public void onDestroy() {
//        super.onDestroy();
//        Log.i("AccessService", "onDestroy");
//        inputHandler.clearKeyComboActions(INPUT_TAG);
//        inputHandler = null;
//    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        //Log.d("AccessService", event.toString());
    }
    @Override
    public void onInterrupt() {

    }

    @Override
    protected boolean onKeyEvent(KeyEvent event) {
        //Log.d("AccessService", event.toString());
        if (inputHandler.onInputEvent(event) && !inputHandler.isBlockingInput())
            return true;
        return super.onKeyEvent(event);
    }

    public static boolean isEnabled() {
        Context context = OxShellApp.getContext();
        String prefString = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
        return prefString!= null && prefString.contains(context.getPackageName() + "/" + AccessService.class.getName());
    }

    public static void showRecentApps() {
        instance.performGlobalAction(GLOBAL_ACTION_RECENTS);
    }
    public static void goHome() {
        instance.performGlobalAction(GLOBAL_ACTION_HOME);
    }
}
