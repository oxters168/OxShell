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
    //private InputHandler inputHandler;
    private static final String INPUT_TAG = "ACCESS_SERVICE";
    @Override
    public void onServiceConnected() {
        super.onServiceConnected();
        Log.i("AccessService", "onServiceConnected");
        instance = this;
        refreshInputCombos();
    }
    public static void refreshInputCombos() {
        if (instance != null) {
            //if (instance.inputHandler == null)
            //    instance.inputHandler = new InputHandler();
            //if (!instance.inputHandler.tagHasActions(InputHandler.ALWAYS_ON_TAG)) {
            InputHandler.clearKeyComboActions();
            InputHandler.addKeyComboActions(INPUT_TAG, Arrays.stream(SettingsKeeper.getHomeCombos()).map(combo -> new KeyComboAction(combo, AccessService::goHome)).toArray(KeyComboAction[]::new));
            InputHandler.addKeyComboActions(INPUT_TAG, Arrays.stream(SettingsKeeper.getRecentsCombos()).map(combo -> new KeyComboAction(combo, AccessService::showRecentApps)).toArray(KeyComboAction[]::new));
//            InputHandler.addKeyComboActions(INPUT_TAG, Arrays.stream(SettingsKeeper.getMusicPlayerTogglePlayInput()).map(combo -> new KeyComboAction(combo, MusicPlayer::togglePlay)).toArray(KeyComboAction[]::new));
//            InputHandler.addKeyComboActions(INPUT_TAG, Arrays.stream(SettingsKeeper.getMusicPlayerStopInput()).map(combo -> new KeyComboAction(combo, MusicPlayer::stop)).toArray(KeyComboAction[]::new));
//            InputHandler.addKeyComboActions(INPUT_TAG, Arrays.stream(SettingsKeeper.getMusicPlayerSkipNextInput()).map(combo -> new KeyComboAction(combo, MusicPlayer::seekToNext)).toArray(KeyComboAction[]::new));
//            InputHandler.addKeyComboActions(INPUT_TAG, Arrays.stream(SettingsKeeper.getMusicPlayerSkipPrevInput()).map(combo -> new KeyComboAction(combo, MusicPlayer::seekToPrev)).toArray(KeyComboAction[]::new));
//            InputHandler.addKeyComboActions(INPUT_TAG, Arrays.stream(SettingsKeeper.getMusicPlayerSeekForwardInput()).map(combo -> new KeyComboAction(combo, MusicPlayer::seekForward)).toArray(KeyComboAction[]::new));
//            InputHandler.addKeyComboActions(INPUT_TAG, Arrays.stream(SettingsKeeper.getMusicPlayerSeekBackInput()).map(combo -> new KeyComboAction(combo, MusicPlayer::seekBack)).toArray(KeyComboAction[]::new));
            InputHandler.setTagIgnorePriority(INPUT_TAG, true);
            InputHandler.setTagEnabled(INPUT_TAG, true);
            //}
        }
    }

//    public static void toggleBlockingInput(boolean onOff) {
//        if (instance != null && instance.inputHandler != null)
//            instance.inputHandler.toggleBlockingInput(onOff);
//    }

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
        InputHandler.onInputEvent(event);
        return super.onKeyEvent(event); // since we don't want to block out any input to any other apps (since sometimes blocking out only the up and not down can apparently cause the android system to keep sending the down events to the apps)
//        if (InputHandler.onInputEvent(event, INPUT_TAG) && !InputHandler.isBlockingInput())
//            return false;
//        return super.onKeyEvent(event);
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
