package com.OxGames.OxShell;

import android.accessibilityservice.AccessibilityGestureEvent;
import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;

import androidx.annotation.NonNull;

import com.OxGames.OxShell.Data.IntentLaunchData;
import com.OxGames.OxShell.Data.KeyComboAction;
import com.OxGames.OxShell.Data.SettingsKeeper;
import com.OxGames.OxShell.Helpers.ActivityManager;
import com.OxGames.OxShell.Helpers.AndroidHelpers;
import com.OxGames.OxShell.Helpers.InputHandler;
import com.OxGames.OxShell.Views.PromptView;

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
        if (inputHandler == null)
            inputHandler = new InputHandler();
        if (!inputHandler.tagHasActions(InputHandler.ALWAYS_ON_TAG)) {
            inputHandler.addKeyComboActions(InputHandler.ALWAYS_ON_TAG, Arrays.stream(SettingsKeeper.getHomeCombos()).map(combo -> new KeyComboAction(combo, AccessService::goHome)).toArray(KeyComboAction[]::new));
            inputHandler.addKeyComboActions(InputHandler.ALWAYS_ON_TAG, Arrays.stream(SettingsKeeper.getRecentsCombos()).map(combo -> new KeyComboAction(combo, AccessService::showRecentApps)).toArray(KeyComboAction[]::new));
        }
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
        if (inputHandler.onInputEvent(event))
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
