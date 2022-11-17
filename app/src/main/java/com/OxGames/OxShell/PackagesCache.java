package com.OxGames.OxShell;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.google.gson.Gson;

import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

public class PackagesCache {
    private static Hashtable<String, Drawable> packageIcons = new Hashtable<>();
    private static Hashtable<String, ApplicationInfo> appInfos = new Hashtable<>();
    private static Hashtable<ApplicationInfo, String> appLabels = new Hashtable<>();

//    public static void loadIntents() {
//        //If dir doesn't exist, create it and the default intents
//        if (!AndroidHelpers.dirExists(Paths.SHORTCUTS_DIR_EXTERNAL)) {
//            if (AndroidHelpers.hasWriteStoragePermission()) {
//                AndroidHelpers.makeDir(Paths.SHORTCUTS_DIR_EXTERNAL);
//                saveDefaultLaunchIntents(Paths.SHORTCUTS_DIR_EXTERNAL);
//            }
//        }
//
//        //If dir exists, load intents stored in it
//        if (AndroidHelpers.dirExists(Paths.SHORTCUTS_DIR_EXTERNAL)) {
//            if (AndroidHelpers.hasReadStoragePermission()) {
//                launchIntents.clear(); //So we don't get duplicates
//                Gson gson = new Gson();
//                File[] intents = AndroidHelpers.listContents(Paths.SHORTCUTS_DIR_EXTERNAL);
//                for (File intent : intents) {
//                    launchIntents.add(gson.fromJson(AndroidHelpers.readFile(intent.getAbsolutePath()), IntentLaunchData.class));
//                }
//            }
//        }
//    }


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
    public static List<ApplicationInfo> getAllInstalledApplications() {
        PagedActivity currentActivity = ActivityManager.getCurrentActivity();
        List<ApplicationInfo> appsList = currentActivity.getPackageManager().getInstalledApplications(PackageManager.GET_META_DATA);
        return appsList;
    }

    public static Drawable getPackageIcon(String packageName) {
        Drawable pkgIcon = null;
        if (!packageIcons.containsKey(packageName)) {
            try {
                pkgIcon = ActivityManager.getCurrentActivity().getPackageManager().getApplicationIcon(packageName);
                packageIcons.put(packageName, pkgIcon);
            }
            catch (PackageManager.NameNotFoundException e) {
                Log.e("PackageCache", e.getMessage());
            }
            catch (Exception e) {
                Log.e("PackageCache", e.getMessage());
            }
        } else
            pkgIcon = packageIcons.get(packageName);

        return pkgIcon;
    }
    public static Drawable getPackageIcon(ResolveInfo rslvInfo) {
        String pkgName = rslvInfo.activityInfo.packageName;
        return getPackageIcon(pkgName);
    }
    public static ApplicationInfo getPackageInfo(String packageName) {
        ApplicationInfo appInfo = null;
        if (!appInfos.containsKey(packageName)) {
            PackageManager packageManager = ActivityManager.getCurrentActivity().getPackageManager();
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
            PackageManager packageManager = ActivityManager.getCurrentActivity().getPackageManager();
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
            rsvInfo = ActivityManager.getCurrentActivity().getPackageManager().resolveActivity(IntentLaunchData.createFromPackage(packageName).buildIntent(), 0);
        } catch (NullPointerException ex) {
            Log.e("PackagesCache", "Unable to resolve package " + packageName);
        }
        return rsvInfo;
    }
}
