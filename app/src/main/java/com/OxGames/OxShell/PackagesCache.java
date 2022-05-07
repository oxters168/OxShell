package com.OxGames.OxShell;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.util.Log;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

public class PackagesCache {
    private static Hashtable<String, Drawable> packageIcons = new Hashtable<>();
    private static Hashtable<String, ApplicationInfo> appInfos = new Hashtable<>();
    private static Hashtable<ApplicationInfo, String> appLabels = new Hashtable<>();
    private static ArrayList<IntentLaunchData> launchIntents = new ArrayList<>();

    public static void prepareDefaultLaunchIntents() {
        //Cheat sheet: http://p.cweiske.de/221
        //            Gson gson = new Gson();
        //            String gbaJSON = gson.toJson(gbaLaunchIntent);
        //            Log.d("Intent", gbaJSON);
        //            gbaLaunchIntent = gson.fromJson(gbaJSON, IntentLaunchData.class);
        IntentLaunchData gbaLaunchIntent = new IntentLaunchData("GBA", Intent.ACTION_VIEW, "com.fastemulator.gba", "com.fastemulator.gba.EmulatorActivity", new String[] { "gba" });
        gbaLaunchIntent.setDataType(IntentLaunchData.DataType.AbsolutePath);
        launchIntents.add(gbaLaunchIntent);

        IntentLaunchData ndsLaunchIntent = new IntentLaunchData("NDS", Intent.ACTION_VIEW, "com.dsemu.drastic", "com.dsemu.drastic.DraSticActivity", new String[] { "nds" });
        ndsLaunchIntent.addExtra(new IntentPutExtra("GAMEPATH", IntentLaunchData.DataType.AbsolutePath));
        launchIntents.add(ndsLaunchIntent);

        IntentLaunchData pspLaunchIntent = new IntentLaunchData("PSP", Intent.ACTION_VIEW, "org.ppsspp.ppsspp", "org.ppsspp.ppsspp.PpssppActivity", new String[] { "iso", "cso" });
        pspLaunchIntent.addExtra(new IntentPutExtra("org.ppsspp.ppsspp.Shortcuts", IntentLaunchData.DataType.AbsolutePath));
        launchIntents.add(pspLaunchIntent);

        IntentLaunchData ps2LaunchIntent = new IntentLaunchData("PS2", Intent.ACTION_VIEW, "xyz.aethersx2.android", "xyz.aethersx2.android.EmulationActivity", new String[] { "iso", "bin", "chd" });
        ps2LaunchIntent.addExtra(new IntentPutExtra("bootPath", IntentLaunchData.DataType.AbsolutePath));
        launchIntents.add(ps2LaunchIntent);

        IntentLaunchData threedsLaunchIntent = new IntentLaunchData("3DS", Intent.ACTION_VIEW, "org.citra.emu", "org.citra.emu.ui.EmulationActivity", new String[] { "3ds", "cxi" });
        threedsLaunchIntent.addExtra(new IntentPutExtra("GamePath", IntentLaunchData.DataType.AbsolutePath));
        launchIntents.add(threedsLaunchIntent);
    }
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
    public static IntentLaunchData[] getStoredIntents() {
        IntentLaunchData[] intents = new IntentLaunchData[launchIntents.size()];
        intents = launchIntents.toArray(intents);
        return intents;
    }
    public static IntentLaunchData getLaunchDataForExtension(String extension) {
        for (int i = 0; i < launchIntents.size(); i++) {
            IntentLaunchData currentLaunchIntent = launchIntents.get(i);
            if (currentLaunchIntent.containsExtension(extension)) {
                return currentLaunchIntent;
            }
        }
        return null;
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
            rsvInfo = ActivityManager.getCurrentActivity().getPackageManager().resolveActivity(new IntentLaunchData(packageName).buildIntent(), 0);
        } catch (NullPointerException ex) {
            Log.e("PackagesCache", "Unable to resolve package " + packageName);
        }
        return rsvInfo;
    }
    public static String getPackageNameForExtension(String extension) {
        for (int i = 0; i < launchIntents.size(); i++) {
            IntentLaunchData currentLaunchIntent = launchIntents.get(i);
            if (currentLaunchIntent.containsExtension(extension)) {
                return currentLaunchIntent.getPackageName();
            }
        }
        return null;
    }
}
