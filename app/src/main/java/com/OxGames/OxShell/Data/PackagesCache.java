package com.OxGames.OxShell.Data;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.OxGames.OxShell.Helpers.MathHelpers;
import com.OxGames.OxShell.OxShellApp;

import java.util.Hashtable;
import java.util.List;
import java.util.Stack;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class PackagesCache {
    private static Hashtable<String, Drawable> packageIcons = new Hashtable<>();
    private static Hashtable<String, ApplicationInfo> appInfos = new Hashtable<>();
    private static Hashtable<ApplicationInfo, String> appLabels = new Hashtable<>();
    private static Stack<String> iconRequests = new Stack<>();

    private static final Handler handler = new Handler(Looper.getMainLooper());

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
    public static List<ResolveInfo> getInstalledPackages(String action, String... categories) {
        Intent mainIntent = new Intent(action, null);
        for (String category : categories)
            mainIntent.addCategory(category);
        List<ResolveInfo> pkgs = OxShellApp.getContext().getPackageManager().queryIntentActivities(mainIntent, 0);

        iconRequests.addAll(pkgs.stream().map(pkg -> pkg.activityInfo.packageName).collect(Collectors.toList()));
        int millis = MathHelpers.calculateMillisForFps(120);
        Runnable loadIcons = new Runnable() {
            @Override
            public void run() {
                while (!iconRequests.isEmpty()) {
                    getPackageIcon(iconRequests.pop());
                    handler.postDelayed(this, millis);
                }
            }
        };
        handler.postDelayed(loadIcons, millis);
        return pkgs;
    }
    public static void requestPackageIcon(String packageName, Consumer<Drawable> pkgIconListener) {
        //Log.d("PackagesCache", "Requesting icon for " + packageName);
        int millis = MathHelpers.calculateMillisForFps(120);
        Runnable waitForIcon = new Runnable() {
            @Override
            public void run() {
                if (pkgIconListener != null) {
                    while (!packageIcons.containsKey(packageName) && iconRequests.contains(packageName))
                        handler.postDelayed(this, millis);
                    pkgIconListener.accept(getPackageIcon(packageName));
                }
            }
        };
        handler.post(waitForIcon);
//        Thread thread = new Thread(() -> {
//            Drawable icon = getPackageIcon(packageName);
//            if (pkgIconListener != null)
//                pkgIconListener.accept(icon);
//        });
//        thread.start();
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
}
