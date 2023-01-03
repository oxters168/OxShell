package com.OxGames.OxShell.Data;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.OxGames.OxShell.Helpers.ActivityManager;
import com.OxGames.OxShell.Interfaces.PkgAppsListener;
import com.OxGames.OxShell.Interfaces.PkgIconListener;
import com.OxGames.OxShell.PagedActivity;

import java.util.Hashtable;
import java.util.List;

public class PackagesCache {
    private static Hashtable<String, Drawable> packageIcons = new Hashtable<>();
    private static Hashtable<String, ApplicationInfo> appInfos = new Hashtable<>();
    private static Hashtable<ApplicationInfo, String> appLabels = new Hashtable<>();

//    public abstract class OnIconLoaded implements Runnable {
//        private Drawable drawable;
//        @Override
//        public abstract void run();
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
            } catch (Exception e) {
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
    public static void requestInstalledPackages(String action, String[] categories, PkgAppsListener pkgAppsListener) {
        Thread thread = new Thread(() -> {
            Intent mainIntent = new Intent(action, null);
            for (String category : categories)
                mainIntent.addCategory(category);
            PagedActivity currentActivity = ActivityManager.getCurrentActivity();
            List<ResolveInfo> pkgAppsList = currentActivity.getPackageManager().queryIntentActivities(mainIntent, 0);
            if (pkgAppsListener != null)
                pkgAppsListener.onQueryApps(pkgAppsList);
            //Log.d("PackagesView", "Listing apps");
        });
        thread.start();
    }
    public static void requestPackageIcon(String packageName, PkgIconListener pkgIconListener) {
        //Log.d("PackagesCache", "Requesting icon for " + packageName);
        Thread thread = new Thread(() -> {
            Drawable icon = getPackageIcon(packageName);
            if (pkgIconListener != null)
                pkgIconListener.onIconLoaded(icon);
        });
        thread.start();
    }
    public static void requestPackageIcon(ResolveInfo rslvInfo, PkgIconListener pkgIconListener) {
        String pkgName = rslvInfo.activityInfo.packageName;
        requestPackageIcon(pkgName, pkgIconListener);
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
