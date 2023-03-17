package com.OxGames.OxShell;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Intent;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;

import com.OxGames.OxShell.Data.IntentLaunchData;
import com.OxGames.OxShell.Helpers.ActivityManager;
import com.OxGames.OxShell.Views.PromptView;

public class AccessService extends AccessibilityService {
    private static AccessService instance;
    @Override
    public void onServiceConnected() {
        super.onServiceConnected();
        instance = this;
        AccessibilityServiceInfo info = getServiceInfo();

        // Set the type of events that this service wants to listen to. Others
        // won't be passed to this service.
        info.eventTypes = ~0;

        // If you only want this service to work with specific applications, set their
        // package names here. Otherwise, when the service is activated, it will listen
        // to events from all applications.
        //info.packageNames = new String[] {"com.example.android.myFirstApp", "com.example.android.mySecondApp"};

        // Set the type of feedback your service will provide.
        info.feedbackType = ~0;

        // Comma separated package names from which this service would like to receive events (leave out for all packages).
        //info.packageNames = new String[] { "com.OxGames.OxShell" };

        // Default services are invoked only if no package-specific ones are present
        // for the type of AccessibilityEvent generated. This service *is*
        // application-specific, so the flag isn't necessary. If this was a
        // general-purpose service, it would be worth considering setting the
        // DEFAULT flag.

        info.notificationTimeout = 100;

        this.setServiceInfo(info);
    }
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        //Log.d("AccessService", event.toString());
        // get the source node of the event
        //AccessibilityNodeInfo nodeInfo = event.getSource();

        // Use the event and node information to determine
        // what action to take

        // take action on behalf of the user
        //nodeInfo.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);

        // recycle the nodeInfo object
        //nodeInfo.recycle();
    }
    @Override
    public void onInterrupt() {

    }
    public static boolean hasPermission() {
        return instance != null;
    }

    public static void showRecentApps() {
        if (!hasPermission()) {
            PromptView prompt = ActivityManager.getCurrentActivity().getPrompt();
            prompt.setCenteredPosition(Math.round(OxShellApp.getDisplayWidth() / 2f), Math.round(OxShellApp.getDisplayHeight() / 2f));
            prompt.setMessage("Ox Shell needs accessibility permission in order to show recent apps when pressing select");
            prompt.setStartBtn("Continue", () -> {
                prompt.setShown(false);
                IntentLaunchData.createFromAction(Settings.ACTION_ACCESSIBILITY_SETTINGS, Intent.FLAG_ACTIVITY_NEW_TASK).launch();
            }, KeyEvent.KEYCODE_ENTER, KeyEvent.KEYCODE_BUTTON_START);
            prompt.setEndBtn("Cancel", () -> {
                prompt.setShown(false);
            }, KeyEvent.KEYCODE_ESCAPE, KeyEvent.KEYCODE_BUTTON_B, KeyEvent.KEYCODE_BACK);
            prompt.setShown(true);
        } else
            instance.performGlobalAction(GLOBAL_ACTION_RECENTS);
    }
}
