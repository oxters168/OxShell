package com.OxGames.OxShell.Data;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.Log;

import com.OxGames.OxShell.Helpers.MathHelpers;
import com.OxGames.OxShell.OxShellApp;

import java.util.Hashtable;
import java.util.List;
import java.util.Stack;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PackagesCache {
    private static Hashtable<String, Drawable> packageIcons = new Hashtable<>();
    private static Hashtable<String, ApplicationInfo> appInfos = new Hashtable<>();
    private static Hashtable<ApplicationInfo, String> appLabels = new Hashtable<>();
    private static Stack<String> iconRequests = new Stack<>();

    public static boolean isRunning(String packageName) {
        return isRunning(getPackageInfo(packageName));
    }
    public static boolean isRunning(ApplicationInfo appInfo) {
        return ((appInfo.flags & ApplicationInfo.FLAG_STOPPED) == 0);
    }
    public static boolean isSystem(String packageName) {
        return isSystem(getPackageInfo(packageName));
    }
    public static boolean isSystem(ApplicationInfo appInfo) {
        return (appInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
    }
//    public static List<ApplicationInfo> getAllInstalledApplications() {
//        List<ApplicationInfo> appsList = OxShellApp.getContext().getPackageManager().getInstalledApplications(PackageManager.GET_META_DATA);
//        return appsList;
//    }
    public static boolean isPackageInstalled(String packageName) {
        try {
            OxShellApp.getContext().getPackageManager().getPackageInfo(packageName, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public static String[] getAllIntentActions() {
        // Must be guaranteed to be in the same order as getAllIntentActionNames
        return new String[] {
            Intent.ACTION_AIRPLANE_MODE_CHANGED,
            Intent.ACTION_ALL_APPS,
            Intent.ACTION_ANSWER,
            Intent.ACTION_APPLICATION_LOCALE_CHANGED,
            Intent.ACTION_APPLICATION_PREFERENCES,
            Intent.ACTION_APPLICATION_RESTRICTIONS_CHANGED,
            Intent.ACTION_APP_ERROR,
            Intent.ACTION_ASSIST,
            Intent.ACTION_ATTACH_DATA,
            Intent.ACTION_AUTO_REVOKE_PERMISSIONS,
            Intent.ACTION_BATTERY_CHANGED,
            Intent.ACTION_BATTERY_LOW,
            Intent.ACTION_BATTERY_OKAY,
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_BUG_REPORT,
            Intent.ACTION_CALL,
            Intent.ACTION_CALL_BUTTON,
            Intent.ACTION_CAMERA_BUTTON,
            Intent.ACTION_CARRIER_SETUP,
            Intent.ACTION_CHOOSER,
            Intent.ACTION_CLOSE_SYSTEM_DIALOGS,
            Intent.ACTION_CONFIGURATION_CHANGED,
            Intent.ACTION_CREATE_DOCUMENT,
            Intent.ACTION_CREATE_REMINDER,
            Intent.ACTION_CREATE_SHORTCUT,
            Intent.ACTION_DATE_CHANGED,
            Intent.ACTION_DEFAULT,
            Intent.ACTION_DEFINE,
            Intent.ACTION_DELETE,
            Intent.ACTION_DEVICE_STORAGE_LOW,
            Intent.ACTION_DEVICE_STORAGE_OK,
            Intent.ACTION_DIAL,
            Intent.ACTION_DOCK_EVENT,
            Intent.ACTION_DREAMING_STARTED,
            Intent.ACTION_DREAMING_STOPPED,
            Intent.ACTION_EDIT,
            Intent.ACTION_EXTERNAL_APPLICATIONS_AVAILABLE,
            Intent.ACTION_EXTERNAL_APPLICATIONS_UNAVAILABLE,
            Intent.ACTION_FACTORY_TEST,
            Intent.ACTION_GET_CONTENT,
            Intent.ACTION_GET_RESTRICTION_ENTRIES,
            Intent.ACTION_GTALK_SERVICE_CONNECTED,
            Intent.ACTION_GTALK_SERVICE_DISCONNECTED,
            Intent.ACTION_HEADSET_PLUG,
            Intent.ACTION_INPUT_METHOD_CHANGED,
            Intent.ACTION_INSERT,
            Intent.ACTION_INSERT_OR_EDIT,
            Intent.ACTION_INSTALL_FAILURE,
            Intent.ACTION_INSTALL_PACKAGE,
            Intent.ACTION_LOCALE_CHANGED,
            Intent.ACTION_LOCKED_BOOT_COMPLETED,
            Intent.ACTION_MAIN,
            Intent.ACTION_MANAGED_PROFILE_ADDED,
            Intent.ACTION_MANAGED_PROFILE_AVAILABLE,
            Intent.ACTION_MANAGED_PROFILE_REMOVED,
            Intent.ACTION_MANAGED_PROFILE_UNAVAILABLE,
            Intent.ACTION_MANAGED_PROFILE_UNLOCKED,
            Intent.ACTION_MANAGE_NETWORK_USAGE,
            Intent.ACTION_MANAGE_PACKAGE_STORAGE,
            Intent.ACTION_MANAGE_UNUSED_APPS,
            Intent.ACTION_MEDIA_BAD_REMOVAL,
            Intent.ACTION_MEDIA_BUTTON,
            Intent.ACTION_MEDIA_CHECKING,
            Intent.ACTION_MEDIA_EJECT,
            Intent.ACTION_MEDIA_MOUNTED,
            Intent.ACTION_MEDIA_NOFS,
            Intent.ACTION_MEDIA_REMOVED,
            Intent.ACTION_MEDIA_SCANNER_FINISHED,
            Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
            Intent.ACTION_MEDIA_SCANNER_STARTED,
            Intent.ACTION_MEDIA_SHARED,
            Intent.ACTION_MEDIA_UNMOUNTABLE,
            Intent.ACTION_MEDIA_UNMOUNTED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            Intent.ACTION_MY_PACKAGE_SUSPENDED,
            Intent.ACTION_MY_PACKAGE_UNSUSPENDED,
            Intent.ACTION_NEW_OUTGOING_CALL,
            Intent.ACTION_OPEN_DOCUMENT,
            Intent.ACTION_OPEN_DOCUMENT_TREE,
            Intent.ACTION_PACKAGES_SUSPENDED,
            Intent.ACTION_PACKAGES_UNSUSPENDED,
            Intent.ACTION_PACKAGE_ADDED,
            Intent.ACTION_PACKAGE_CHANGED,
            Intent.ACTION_PACKAGE_DATA_CLEARED,
            Intent.ACTION_PACKAGE_FIRST_LAUNCH,
            Intent.ACTION_PACKAGE_FULLY_REMOVED,
            Intent.ACTION_PACKAGE_INSTALL,
            Intent.ACTION_PACKAGE_NEEDS_VERIFICATION,
            Intent.ACTION_PACKAGE_REMOVED,
            Intent.ACTION_PACKAGE_REPLACED,
            Intent.ACTION_PACKAGE_RESTARTED,
            Intent.ACTION_PACKAGE_VERIFIED,
            Intent.ACTION_PASTE,
            Intent.ACTION_PICK,
            Intent.ACTION_PICK_ACTIVITY,
            Intent.ACTION_POWER_CONNECTED,
            Intent.ACTION_POWER_DISCONNECTED,
            Intent.ACTION_POWER_USAGE_SUMMARY,
            Intent.ACTION_PROCESS_TEXT,
            Intent.ACTION_PROFILE_ACCESSIBLE,
            Intent.ACTION_PROFILE_INACCESSIBLE,
            Intent.ACTION_PROVIDER_CHANGED,
            Intent.ACTION_QUICK_CLOCK,
            Intent.ACTION_QUICK_VIEW,
            Intent.ACTION_REBOOT,
            Intent.ACTION_RUN,
            Intent.ACTION_SAFETY_CENTER,
            Intent.ACTION_SCREEN_OFF,
            Intent.ACTION_SCREEN_ON,
            Intent.ACTION_SEARCH,
            Intent.ACTION_SEARCH_LONG_PRESS,
            Intent.ACTION_SEND,
            Intent.ACTION_SENDTO,
            Intent.ACTION_SEND_MULTIPLE,
            Intent.ACTION_SET_WALLPAPER,
            Intent.ACTION_SHOW_APP_INFO,
            Intent.ACTION_SHOW_WORK_APPS,
            Intent.ACTION_SHUTDOWN,
            Intent.ACTION_SYNC,
            Intent.ACTION_SYSTEM_TUTORIAL,
            Intent.ACTION_TIMEZONE_CHANGED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIME_TICK,
            Intent.ACTION_TRANSLATE,
            Intent.ACTION_UID_REMOVED,
            Intent.ACTION_UMS_CONNECTED,
            Intent.ACTION_UMS_DISCONNECTED,
            Intent.ACTION_UNINSTALL_PACKAGE,
            Intent.ACTION_USER_BACKGROUND,
            Intent.ACTION_USER_FOREGROUND,
            Intent.ACTION_USER_INITIALIZE,
            Intent.ACTION_USER_PRESENT,
            Intent.ACTION_USER_UNLOCKED,
            Intent.ACTION_VIEW,
            Intent.ACTION_VIEW_LOCUS,
            Intent.ACTION_VIEW_PERMISSION_USAGE,
            Intent.ACTION_VIEW_PERMISSION_USAGE_FOR_PERIOD,
            Intent.ACTION_VOICE_COMMAND,
            Intent.ACTION_WALLPAPER_CHANGED,
            Intent.ACTION_WEB_SEARCH
        };
    }
    public static String[] getAllIntentActionNames() {
        // Must be guaranteed to be in the same order as getAllIntentActionNames
        return new String[] {
            "ACTION_AIRPLANE_MODE_CHANGED",
            "ACTION_ALL_APPS",
            "ACTION_ANSWER",
            "ACTION_APPLICATION_LOCALE_CHANGED",
            "ACTION_APPLICATION_PREFERENCES",
            "ACTION_APPLICATION_RESTRICTIONS_CHANGED",
            "ACTION_APP_ERROR",
            "ACTION_ASSIST",
            "ACTION_ATTACH_DATA",
            "ACTION_AUTO_REVOKE_PERMISSIONS",
            "ACTION_BATTERY_CHANGED",
            "ACTION_BATTERY_LOW",
            "ACTION_BATTERY_OKAY",
            "ACTION_BOOT_COMPLETED",
            "ACTION_BUG_REPORT",
            "ACTION_CALL",
            "ACTION_CALL_BUTTON",
            "ACTION_CAMERA_BUTTON",
            "ACTION_CARRIER_SETUP",
            "ACTION_CHOOSER",
            "ACTION_CLOSE_SYSTEM_DIALOGS",
            "ACTION_CONFIGURATION_CHANGED",
            "ACTION_CREATE_DOCUMENT",
            "ACTION_CREATE_REMINDER",
            "ACTION_CREATE_SHORTCUT",
            "ACTION_DATE_CHANGED",
            "ACTION_DEFAULT",
            "ACTION_DEFINE",
            "ACTION_DELETE",
            "ACTION_DEVICE_STORAGE_LOW",
            "ACTION_DEVICE_STORAGE_OK",
            "ACTION_DIAL",
            "ACTION_DOCK_EVENT",
            "ACTION_DREAMING_STARTED",
            "ACTION_DREAMING_STOPPED",
            "ACTION_EDIT",
            "ACTION_EXTERNAL_APPLICATIONS_AVAILABLE",
            "ACTION_EXTERNAL_APPLICATIONS_UNAVAILABLE",
            "ACTION_FACTORY_TEST",
            "ACTION_GET_CONTENT",
            "ACTION_GET_RESTRICTION_ENTRIES",
            "ACTION_GTALK_SERVICE_CONNECTED",
            "ACTION_GTALK_SERVICE_DISCONNECTED",
            "ACTION_HEADSET_PLUG",
            "ACTION_INPUT_METHOD_CHANGED",
            "ACTION_INSERT",
            "ACTION_INSERT_OR_EDIT",
            "ACTION_INSTALL_FAILURE",
            "ACTION_INSTALL_PACKAGE",
            "ACTION_LOCALE_CHANGED",
            "ACTION_LOCKED_BOOT_COMPLETED",
            "ACTION_MAIN",
            "ACTION_MANAGED_PROFILE_ADDED",
            "ACTION_MANAGED_PROFILE_AVAILABLE",
            "ACTION_MANAGED_PROFILE_REMOVED",
            "ACTION_MANAGED_PROFILE_UNAVAILABLE",
            "ACTION_MANAGED_PROFILE_UNLOCKED",
            "ACTION_MANAGE_NETWORK_USAGE",
            "ACTION_MANAGE_PACKAGE_STORAGE",
            "ACTION_MANAGE_UNUSED_APPS",
            "ACTION_MEDIA_BAD_REMOVAL",
            "ACTION_MEDIA_BUTTON",
            "ACTION_MEDIA_CHECKING",
            "ACTION_MEDIA_EJECT",
            "ACTION_MEDIA_MOUNTED",
            "ACTION_MEDIA_NOFS",
            "ACTION_MEDIA_REMOVED",
            "ACTION_MEDIA_SCANNER_FINISHED",
            "ACTION_MEDIA_SCANNER_SCAN_FILE",
            "ACTION_MEDIA_SCANNER_STARTED",
            "ACTION_MEDIA_SHARED",
            "ACTION_MEDIA_UNMOUNTABLE",
            "ACTION_MEDIA_UNMOUNTED",
            "ACTION_MY_PACKAGE_REPLACED",
            "ACTION_MY_PACKAGE_SUSPENDED",
            "ACTION_MY_PACKAGE_UNSUSPENDED",
            "ACTION_NEW_OUTGOING_CALL",
            "ACTION_OPEN_DOCUMENT",
            "ACTION_OPEN_DOCUMENT_TREE",
            "ACTION_PACKAGES_SUSPENDED",
            "ACTION_PACKAGES_UNSUSPENDED",
            "ACTION_PACKAGE_ADDED",
            "ACTION_PACKAGE_CHANGED",
            "ACTION_PACKAGE_DATA_CLEARED",
            "ACTION_PACKAGE_FIRST_LAUNCH",
            "ACTION_PACKAGE_FULLY_REMOVED",
            "ACTION_PACKAGE_INSTALL",
            "ACTION_PACKAGE_NEEDS_VERIFICATION",
            "ACTION_PACKAGE_REMOVED",
            "ACTION_PACKAGE_REPLACED",
            "ACTION_PACKAGE_RESTARTED",
            "ACTION_PACKAGE_VERIFIED",
            "ACTION_PASTE",
            "ACTION_PICK",
            "ACTION_PICK_ACTIVITY",
            "ACTION_POWER_CONNECTED",
            "ACTION_POWER_DISCONNECTED",
            "ACTION_POWER_USAGE_SUMMARY",
            "ACTION_PROCESS_TEXT",
            "ACTION_PROFILE_ACCESSIBLE",
            "ACTION_PROFILE_INACCESSIBLE",
            "ACTION_PROVIDER_CHANGED",
            "ACTION_QUICK_CLOCK",
            "ACTION_QUICK_VIEW",
            "ACTION_REBOOT",
            "ACTION_RUN",
            "ACTION_SAFETY_CENTER",
            "ACTION_SCREEN_OFF",
            "ACTION_SCREEN_ON",
            "ACTION_SEARCH",
            "ACTION_SEARCH_LONG_PRESS",
            "ACTION_SEND",
            "ACTION_SENDTO",
            "ACTION_SEND_MULTIPLE",
            "ACTION_SET_WALLPAPER",
            "ACTION_SHOW_APP_INFO",
            "ACTION_SHOW_WORK_APPS",
            "ACTION_SHUTDOWN",
            "ACTION_SYNC",
            "ACTION_SYSTEM_TUTORIAL",
            "ACTION_TIMEZONE_CHANGED",
            "ACTION_TIME_CHANGED",
            "ACTION_TIME_TICK",
            "ACTION_TRANSLATE",
            "ACTION_UID_REMOVED",
            "ACTION_UMS_CONNECTED",
            "ACTION_UMS_DISCONNECTED",
            "ACTION_UNINSTALL_PACKAGE",
            "ACTION_USER_BACKGROUND",
            "ACTION_USER_FOREGROUND",
            "ACTION_USER_INITIALIZE",
            "ACTION_USER_PRESENT",
            "ACTION_USER_UNLOCKED",
            "ACTION_VIEW",
            "ACTION_VIEW_LOCUS",
            "ACTION_VIEW_PERMISSION_USAGE",
            "ACTION_VIEW_PERMISSION_USAGE_FOR_PERIOD",
            "ACTION_VOICE_COMMAND",
            "ACTION_WALLPAPER_CHANGED",
            "ACTION_WEB_SEARCH"
        };
    }
    public static String[] getClassesOfPkg(String packageName) {
        String[] classes = new String[0];
        try {
            PackageInfo packageInfo = OxShellApp.getContext().getPackageManager().getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            classes = Stream.of(packageInfo.activities).map(a -> a.name).toArray(String[]::new);
        } catch (PackageManager.NameNotFoundException e) { e.printStackTrace(); }
        return classes;
    }
    public static Drawable getPackageIcon(String packageName) {
        Drawable pkgIcon = null;
        if (!packageIcons.containsKey(packageName)) {
            try {
                pkgIcon = OxShellApp.getContext().getPackageManager().getApplicationIcon(packageName);
                packageIcons.put(packageName, pkgIcon);
            } catch (Exception e) {
                Log.e("PackageCache", "Failed to retrieve icon for " + packageName + ": " + e);
            }
        } else
            pkgIcon = packageIcons.get(packageName);

        return pkgIcon != null ? pkgIcon.getConstantState().newDrawable().mutate() : null;
    }
    public static Drawable getPackageIcon(ResolveInfo rslvInfo) {
        String pkgName = rslvInfo.activityInfo.packageName;
        return getPackageIcon(pkgName);
    }
//    public static void requestInstalledPackages(String action, PkgAppsListener pkgAppsListener, String... categories) {
//        Thread thread = new Thread(() -> {
//            List<ResolveInfo> pkgAppsList = getInstalledPackages(action, categories);
//            if (pkgAppsListener != null)
//                pkgAppsListener.onQueryApps(pkgAppsList);
//            //Log.d("PackagesView", "Listing apps");
//        });
//        thread.start();
//    }
    public static List<ResolveInfo> getLaunchableInstalledPackages() {
        return PackagesCache.getInstalledPackages(Intent.ACTION_MAIN, Intent.CATEGORY_LAUNCHER);
    }
    public static List<ResolveInfo> getInstalledPackages(String action, String... categories) {
        Intent mainIntent = new Intent(action, null);
        for (String category : categories)
            mainIntent.addCategory(category);
        List<ResolveInfo> pkgs = OxShellApp.getContext().getPackageManager().queryIntentActivities(mainIntent, 0);

        iconRequests.addAll(pkgs.stream().map(pkg -> pkg.activityInfo.packageName).collect(Collectors.toList()));
        int millis = MathHelpers.calculateMillisForFps(120);
        new Thread(() -> {
            while (!iconRequests.isEmpty()) {
                String pkgName = iconRequests.pop();
                //Log.d("PackagesCache", "Caching icon for " + pkgName);
                getPackageIcon(pkgName);
                try {
                    Thread.sleep(millis);
                } catch (InterruptedException e) {
                    Log.e("PackagesCache", "Loading pkg icons interrupted: " + e);
                }
            }
        }).start();
//        Handler loadIconsHandler = new Handler();
//        Runnable loadIcons = new Runnable() {
//            @Override
//            public void run() {
//                if (!iconRequests.isEmpty()) {
//                    String pkgName = iconRequests.pop();
//                    //Log.d("PackagesCache", "Caching icon for " + pkgName);
//                    getPackageIcon(pkgName);
//                    loadIconsHandler.postDelayed(this, millis);
//                }
//            }
//        };
//        loadIconsHandler.postDelayed(loadIcons, millis);
        return pkgs;
    }
    public static void requestPackageIcon(String packageName, Consumer<Drawable> pkgIconListener) {
        //Log.d("PackagesCache", "Requesting icon for " + packageName);
        if (!packageIcons.containsKey(packageName)) {
            int millis = MathHelpers.calculateMillisForFps(120);
            new Thread(() -> {
                if (pkgIconListener != null) {
                    //if (!iconRequests.contains(packageName))
                    //    getPackageIcon(packageName);
                    while (!packageIcons.containsKey(packageName) && iconRequests.contains(packageName)) {
                        //Log.d("PackagesCache", "Icon for " + packageName + " not cached, waiting...");
                        try {
                            Thread.sleep(millis);
                        } catch (InterruptedException e) {
                            Log.e("PackagesCache", "Waiting for icon interrupted: " + e);
                        }
                    }
                    //Log.d("PackagesCache", "Icon for " + packageName + " cached or not being requested, sending back");
                    Drawable icon = getPackageIcon(packageName);
                    OxShellApp.getCurrentActivity().runOnUiThread(() -> pkgIconListener.accept(icon));
                }
            }).start();
//            Handler waitForIconHandler = new Handler();
//            Runnable waitForIcon = new Runnable() {
//                @Override
//                public void run() {
//                    if (pkgIconListener != null) {
//                        if (!packageIcons.containsKey(packageName) && iconRequests.contains(packageName)) {
//                            //Log.d("PackagesCache", "Icon for " + packageName + " not cached, waiting...");
//                            waitForIconHandler.postDelayed(this, millis);
//                            return;
//                        }
//                        pkgIconListener.accept(getPackageIcon(packageName));
//                    }
//                }
//            };
//            waitForIconHandler.post(waitForIcon);
        } else
            pkgIconListener.accept(getPackageIcon(packageName));
    }
    public static void requestPackageIcon(ResolveInfo rslvInfo, Consumer<Drawable> pkgIconListener) {
        String pkgName = rslvInfo.activityInfo.packageName;
        requestPackageIcon(pkgName, pkgIconListener);
    }
    public static ApplicationInfo getPackageInfo(String packageName) {
        ApplicationInfo appInfo = null;
        if (!appInfos.containsKey(packageName)) {
            PackageManager packageManager = OxShellApp.getContext().getPackageManager();
            try {
                appInfo = packageManager.getApplicationInfo(packageName, 0);
                appInfos.put(packageName, appInfo);
            } catch (final PackageManager.NameNotFoundException e) {
                Log.e("PackageCache", e.getMessage());
            }
        } else
            appInfo = appInfos.get(packageName);
        return appInfo;
    }
    public static String getAppLabel(ApplicationInfo appInfo) {
        String appLabel = null;
        if (!appLabels.containsKey(appInfo)) {
            PackageManager packageManager = OxShellApp.getContext().getPackageManager();
            appLabel = (String)packageManager.getApplicationLabel(appInfo);
            if (appLabel != null)
                appLabels.put(appInfo, appLabel);
        }
        else
            appLabel = appLabels.get(appInfo);
        return appLabel;
    }
    public static String getAppLabel(ResolveInfo rslvInfo) {
        String appName = null;
        String pkgName = rslvInfo.activityInfo.packageName;
        if (pkgName != null) {
            ApplicationInfo pkgInfo = PackagesCache.getPackageInfo(pkgName);
            if (pkgInfo != null)
                appName = getAppLabel(pkgInfo);
//            intentNames.add(new DetailItem(PackagesCache.GetPackageIcon(pkgName), appName, null, pkgAppsList.get(i)));
        }
        return appName;
    }
    public static ResolveInfo getResolveInfo(String packageName) {
        ResolveInfo rsvInfo = null;
        try {
            rsvInfo = OxShellApp.getContext().getPackageManager().resolveActivity(IntentLaunchData.createFromPackage(packageName).buildIntent(), 0);
        } catch (NullPointerException ex) {
            Log.e("PackagesCache", "Unable to resolve package " + packageName);
        }
        return rsvInfo;
    }

    //        Intent.ACTION_AIRPLANE_MODE_CHANGED; //system
//        Intent.ACTION_ALL_APPS;
//        Intent.ACTION_ANSWER;
//        Intent.ACTION_APPLICATION_LOCALE_CHANGED; //system
//        Intent.ACTION_APPLICATION_PREFERENCES;
//        Intent.ACTION_APPLICATION_RESTRICTIONS_CHANGED; //system
//        Intent.ACTION_APP_ERROR;
//        Intent.ACTION_ASSIST;
//        Intent.ACTION_ATTACH_DATA;
//        Intent.ACTION_AUTO_REVOKE_PERMISSIONS;
//        Intent.ACTION_BATTERY_CHANGED; //system
//        Intent.ACTION_BATTERY_LOW; //system
//        Intent.ACTION_BATTERY_OKAY; //system
//        Intent.ACTION_BOOT_COMPLETED; //system
//        Intent.ACTION_BUG_REPORT;
//        Intent.ACTION_CALL;
//        Intent.ACTION_CALL_BUTTON;
//        Intent.ACTION_CAMERA_BUTTON;
//        Intent.ACTION_CARRIER_SETUP;
//        Intent.ACTION_CHOOSER;
//        Intent.ACTION_CLOSE_SYSTEM_DIALOGS;
//        Intent.ACTION_CONFIGURATION_CHANGED; //system
//        Intent.ACTION_CREATE_DOCUMENT;
//        Intent.ACTION_CREATE_NOTE;
//        Intent.ACTION_CREATE_REMINDER;
//        Intent.ACTION_CREATE_SHORTCUT;
//        Intent.ACTION_DATE_CHANGED;
//        Intent.ACTION_DEFAULT;
//        Intent.ACTION_DEFINE;
//        Intent.ACTION_DELETE;
//        Intent.ACTION_DEVICE_STORAGE_LOW; //system
//        Intent.ACTION_DEVICE_STORAGE_OK; //system
//        Intent.ACTION_DIAL;
//        Intent.ACTION_DOCK_EVENT;
//        Intent.ACTION_DREAMING_STARTED; //system
//        Intent.ACTION_DREAMING_STOPPED; //system
//        Intent.ACTION_EDIT;
//        Intent.ACTION_EXTERNAL_APPLICATIONS_AVAILABLE; //system
//        Intent.ACTION_EXTERNAL_APPLICATIONS_UNAVAILABLE; //system
//        Intent.ACTION_FACTORY_TEST;
//        Intent.ACTION_GET_CONTENT;
//        Intent.ACTION_GET_RESTRICTION_ENTRIES;
//        Intent.ACTION_GTALK_SERVICE_CONNECTED;
//        Intent.ACTION_GTALK_SERVICE_DISCONNECTED;
//        Intent.ACTION_HEADSET_PLUG;
//        Intent.ACTION_INPUT_METHOD_CHANGED;
//        Intent.ACTION_INSERT;
//        Intent.ACTION_INSERT_OR_EDIT;
//        Intent.ACTION_INSTALL_FAILURE;
//        Intent.ACTION_INSTALL_PACKAGE;
//        Intent.ACTION_LAUNCH_CAPTURE_CONTENT_ACTIVITY_FOR_NOTE;
//        Intent.ACTION_LOCALE_CHANGED;
//        Intent.ACTION_LOCKED_BOOT_COMPLETED;
//        Intent.ACTION_MAIN;
//        Intent.ACTION_MANAGED_PROFILE_ADDED;
//        Intent.ACTION_MANAGED_PROFILE_AVAILABLE;
//        Intent.ACTION_MANAGED_PROFILE_REMOVED;
//        Intent.ACTION_MANAGED_PROFILE_UNAVAILABLE;
//        Intent.ACTION_MANAGED_PROFILE_UNLOCKED;
//        Intent.ACTION_MANAGE_NETWORK_USAGE;
//        Intent.ACTION_MANAGE_PACKAGE_STORAGE;
//        Intent.ACTION_MANAGE_UNUSED_APPS;
//        Intent.ACTION_MEDIA_BAD_REMOVAL;
//        Intent.ACTION_MEDIA_BUTTON;
//        Intent.ACTION_MEDIA_CHECKING;
//        Intent.ACTION_MEDIA_EJECT;
//        Intent.ACTION_MEDIA_MOUNTED;
//        Intent.ACTION_MEDIA_NOFS;
//        Intent.ACTION_MEDIA_REMOVED;
//        Intent.ACTION_MEDIA_SCANNER_FINISHED;
//        Intent.ACTION_MEDIA_SCANNER_SCAN_FILE;
//        Intent.ACTION_MEDIA_SCANNER_STARTED;
//        Intent.ACTION_MEDIA_SHARED;
//        Intent.ACTION_MEDIA_UNMOUNTABLE;
//        Intent.ACTION_MEDIA_UNMOUNTED;
//        Intent.ACTION_MY_PACKAGE_REPLACED; //system
//        Intent.ACTION_MY_PACKAGE_SUSPENDED; //system
//        Intent.ACTION_MY_PACKAGE_UNSUSPENDED; //system
//        Intent.ACTION_NEW_OUTGOING_CALL;
//        Intent.ACTION_OPEN_DOCUMENT;
//        Intent.ACTION_OPEN_DOCUMENT_TREE;
//        Intent.ACTION_PACKAGES_SUSPENDED; //system
//        Intent.ACTION_PACKAGES_UNSUSPENDED; //system
//        Intent.ACTION_PACKAGE_ADDED; //system
//        Intent.ACTION_PACKAGE_CHANGED; //system
//        Intent.ACTION_PACKAGE_DATA_CLEARED; //system
//        Intent.ACTION_PACKAGE_FIRST_LAUNCH; //system
//        Intent.ACTION_PACKAGE_FULLY_REMOVED; //system
//        Intent.ACTION_PACKAGE_INSTALL; //system
//        Intent.ACTION_PACKAGE_NEEDS_VERIFICATION; //system
//        Intent.ACTION_PACKAGE_REMOVED; //system
//        Intent.ACTION_PACKAGE_REPLACED; //system
//        Intent.ACTION_PACKAGE_RESTARTED; //system
//        Intent.ACTION_PACKAGE_VERIFIED; //system
//        Intent.ACTION_PASTE;
//        Intent.ACTION_PICK;
//        Intent.ACTION_PICK_ACTIVITY;
//        Intent.ACTION_POWER_CONNECTED; //system
//        Intent.ACTION_POWER_DISCONNECTED; //system
//        Intent.ACTION_POWER_USAGE_SUMMARY;
//        Intent.ACTION_PROCESS_TEXT;
//        Intent.ACTION_PROFILE_ACCESSIBLE;
//        Intent.ACTION_PROFILE_ADDED;
//        Intent.ACTION_PROFILE_INACCESSIBLE;
//        Intent.ACTION_PROFILE_REMOVED;
//        Intent.ACTION_PROVIDER_CHANGED;
//        Intent.ACTION_QUICK_CLOCK;
//        Intent.ACTION_QUICK_VIEW;
//        Intent.ACTION_REBOOT; //system
//        Intent.ACTION_RUN;
//        Intent.ACTION_SAFETY_CENTER;
//        Intent.ACTION_SCREEN_OFF; //system
//        Intent.ACTION_SCREEN_ON; //system
//        Intent.ACTION_SEARCH;
//        Intent.ACTION_SEARCH_LONG_PRESS;
//        Intent.ACTION_SEND;
//        Intent.ACTION_SENDTO;
//        Intent.ACTION_SEND_MULTIPLE;
//        Intent.ACTION_SET_WALLPAPER;
//        Intent.ACTION_SHOW_APP_INFO;
//        Intent.ACTION_SHOW_WORK_APPS;
//        Intent.ACTION_SHUTDOWN; //system
//        Intent.ACTION_SYNC;
//        Intent.ACTION_SYSTEM_TUTORIAL;
//        Intent.ACTION_TIMEZONE_CHANGED; //system
//        Intent.ACTION_TIME_CHANGED;
//        Intent.ACTION_TIME_TICK; //system
//        Intent.ACTION_TRANSLATE;
//        Intent.ACTION_UID_REMOVED; //system
//        Intent.ACTION_UMS_CONNECTED;
//        Intent.ACTION_UMS_DISCONNECTED;
//        Intent.ACTION_UNINSTALL_PACKAGE;
//        Intent.ACTION_USER_BACKGROUND;
//        Intent.ACTION_USER_FOREGROUND;
//        Intent.ACTION_USER_INITIALIZE;
//        Intent.ACTION_USER_PRESENT; //system
//        Intent.ACTION_USER_UNLOCKED;
//        Intent.ACTION_VIEW;
//        Intent.ACTION_VIEW_LOCUS;
//        Intent.ACTION_VIEW_PERMISSION_USAGE;
//        Intent.ACTION_VIEW_PERMISSION_USAGE_FOR_PERIOD;
//        Intent.ACTION_VOICE_COMMAND; //In some cases, a matching Activity may not exist, so ensure you safeguard against this.
//        Intent.ACTION_WALLPAPER_CHANGED;
//        Intent.ACTION_WEB_SEARCH;
}
