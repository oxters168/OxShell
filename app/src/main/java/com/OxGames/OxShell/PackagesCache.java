package com.OxGames.OxShell;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.util.Log;

import com.google.gson.Gson;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

public class PackagesCache {
    private static final String INTENT_SHORTCUTS_DIR = Environment.getExternalStorageDirectory() + "/OxShell/Intents";

    private static Hashtable<String, Drawable> packageIcons = new Hashtable<>();
    private static Hashtable<String, ApplicationInfo> appInfos = new Hashtable<>();
    private static Hashtable<ApplicationInfo, String> appLabels = new Hashtable<>();
    private static ArrayList<IntentLaunchData> launchIntents = new ArrayList<>();

    public static void loadIntents() {
        //If dir doesn't exist, create it and the default intents
        if (!ExplorerBehaviour.dirExists(INTENT_SHORTCUTS_DIR)) {
            if (ExplorerBehaviour.hasWriteStoragePermission()) {
                ExplorerBehaviour.makeDir(INTENT_SHORTCUTS_DIR);
                saveDefaultLaunchIntents();
            }
        }

        //If dir exists, load intents stored in it
        if (ExplorerBehaviour.dirExists(INTENT_SHORTCUTS_DIR)) {
            if (ExplorerBehaviour.hasReadStoragePermission()) {
                launchIntents.clear(); //So we don't get duplicates
                Gson gson = new Gson();
                File[] intents = ExplorerBehaviour.listContents(INTENT_SHORTCUTS_DIR);
                for (File intent : intents) {
                    launchIntents.add(gson.fromJson(ExplorerBehaviour.readFile(intent.getAbsolutePath()), IntentLaunchData.class));
                }
            }
        }
    }
    private static void saveDefaultLaunchIntents() {
        //Cheat sheet: http://p.cweiske.de/221
        IntentLaunchData gbaLaunchIntent = new IntentLaunchData("GBA", Intent.ACTION_VIEW, "com.fastemulator.gba", "com.fastemulator.gba.EmulatorActivity", new String[] { "gba" }, Intent.FLAG_ACTIVITY_NEW_TASK);
        gbaLaunchIntent.setDataType(IntentLaunchData.DataType.AbsolutePath);
        saveIntentData(gbaLaunchIntent);

        IntentLaunchData ndsLaunchIntent = new IntentLaunchData("NDS", Intent.ACTION_VIEW, "com.dsemu.drastic", "com.dsemu.drastic.DraSticActivity", new String[] { "nds" }, Intent.FLAG_ACTIVITY_NEW_TASK);
        ndsLaunchIntent.addExtra(new IntentPutExtra("GAMEPATH", IntentLaunchData.DataType.AbsolutePath));
        saveIntentData(ndsLaunchIntent);

        IntentLaunchData pspLaunchIntent = new IntentLaunchData("PSP", Intent.ACTION_VIEW, "org.ppsspp.ppsspp", "org.ppsspp.ppsspp.PpssppActivity", new String[] { "iso", "cso" }, Intent.FLAG_ACTIVITY_NEW_TASK);
        pspLaunchIntent.addExtra(new IntentPutExtra("org.ppsspp.ppsspp.Shortcuts", IntentLaunchData.DataType.AbsolutePath));
        saveIntentData(pspLaunchIntent);

        IntentLaunchData ps2LaunchIntent = new IntentLaunchData("PS2", Intent.ACTION_VIEW, "xyz.aethersx2.android", "xyz.aethersx2.android.EmulationActivity", new String[] { "iso", "bin", "chd" }, Intent.FLAG_ACTIVITY_NEW_TASK);
        ps2LaunchIntent.addExtra(new IntentPutExtra("bootPath", IntentLaunchData.DataType.AbsolutePath));
        saveIntentData(ps2LaunchIntent);

        IntentLaunchData threedsLaunchIntent = new IntentLaunchData("3DS", Intent.ACTION_VIEW, "org.citra.emu", "org.citra.emu.ui.EmulationActivity", new String[] { "3ds", "cxi" }, Intent.FLAG_ACTIVITY_NEW_TASK);
        threedsLaunchIntent.addExtra(new IntentPutExtra("GamePath", IntentLaunchData.DataType.AbsolutePath));
        saveIntentData(threedsLaunchIntent);
    }
    private static void saveIntentData(IntentLaunchData intentData) {
        Gson gson = new Gson();
        String fileName = INTENT_SHORTCUTS_DIR + "/" + intentData.getDisplayName() + ".json";
        if (!ExplorerBehaviour.fileExists(fileName))
            ExplorerBehaviour.makeFile(fileName);
        ExplorerBehaviour.writeToFile(fileName, gson.toJson(intentData));
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
