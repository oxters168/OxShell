package com.OxGames.OxShell;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.util.Log;

import java.util.ArrayList;
import java.util.Hashtable;

public class PackagesCache {
    private static Hashtable<String, Drawable> packageIcons = new Hashtable<>();
    private static Hashtable<String, ApplicationInfo> appInfos = new Hashtable<>();
    private static Hashtable<ApplicationInfo, String> appLabels = new Hashtable<>();
    private static ArrayList<IntentLaunchData> launchIntents = new ArrayList<>();

    public static void PrepareDefaultLaunchIntents() {
        //Cheat sheet: http://p.cweiske.de/221
        IntentLaunchData gbaLaunchIntent = new IntentLaunchData("GBA", Intent.ACTION_VIEW, "com.fastemulator.gba", "com.fastemulator.gba.EmulatorActivity", new String[] { "gba" });
        gbaLaunchIntent.SetDataType(IntentLaunchData.DataType.AbsolutePath);
        //            Gson gson = new Gson();
        //            String gbaJSON = gson.toJson(gbaLaunchIntent);
        //            Log.d("Intent", gbaJSON);
        //            gbaLaunchIntent = gson.fromJson(gbaJSON, IntentLaunchData.class);
        launchIntents.add(gbaLaunchIntent);

        IntentLaunchData ndsLaunchIntent = new IntentLaunchData("NDS", Intent.ACTION_VIEW, "com.dsemu.drastic", "com.dsemu.drastic.DraSticActivity", new String[] { "nds" });
        ndsLaunchIntent.AddExtra(new IntentPutExtra("GAMEPATH", IntentLaunchData.DataType.AbsolutePath));
        launchIntents.add(ndsLaunchIntent);

        IntentLaunchData pspLaunchIntent = new IntentLaunchData("PSP", Intent.ACTION_VIEW, "org.ppsspp.ppsspp", "org.ppsspp.ppsspp.PpssppActivity", new String[] { "iso" });
        pspLaunchIntent.AddExtra(new IntentPutExtra("org.ppsspp.ppsspp.Shortcuts", IntentLaunchData.DataType.AbsolutePath));
        launchIntents.add(pspLaunchIntent);

        IntentLaunchData ps2LaunchIntent = new IntentLaunchData("PS2", Intent.ACTION_VIEW, "xyz.aethersx2.android", "xyz.aethersx2.android.EmulationActivity", new String[] { "chd" });
        ps2LaunchIntent.AddExtra(new IntentPutExtra("bootPath", IntentLaunchData.DataType.AbsolutePath));
        launchIntents.add(ps2LaunchIntent);

        IntentLaunchData threedsLaunchIntent = new IntentLaunchData("3DS", Intent.ACTION_VIEW, "org.citra.emu", "org.citra.emu.ui.EmulationActivity", new String[] { "cxi" });
        threedsLaunchIntent.AddExtra(new IntentPutExtra("GamePath", IntentLaunchData.DataType.AbsolutePath));
        launchIntents.add(threedsLaunchIntent);
    }
    public static IntentLaunchData[] GetStoredIntents() {
        IntentLaunchData[] intents = new IntentLaunchData[launchIntents.size()];
        intents = launchIntents.toArray(intents);
        return intents;
    }
    public static IntentLaunchData GetLaunchDataForExtension(String extension) {
        for (int i = 0; i < launchIntents.size(); i++) {
            IntentLaunchData currentLaunchIntent = launchIntents.get(i);
            if (currentLaunchIntent.ContainsExtension(extension)) {
                return currentLaunchIntent;
            }
        }
        return null;
    }

    public static Drawable GetPackageIcon(String packageName) {
        Drawable pkgIcon = null;
        if (!packageIcons.containsKey(packageName)) {
            try {
                pkgIcon = ActivityManager.GetCurrentActivity().getPackageManager().getApplicationIcon(packageName);
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
    public static Drawable GetPackageIcon(ResolveInfo rslvInfo) {
        String pkgName = rslvInfo.activityInfo.packageName;
        return GetPackageIcon(pkgName);
    }
    public static ApplicationInfo GetPackageInfo(String packageName) {
        ApplicationInfo appInfo = null;
        if (!appInfos.containsKey(packageName)) {
            PackageManager packageManager = ActivityManager.GetCurrentActivity().getPackageManager();
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
    public static String GetAppLabel(ApplicationInfo appInfo) {
        String appLabel = null;
        if (!appLabels.containsKey(appInfo)) {
            PackageManager packageManager = ActivityManager.GetCurrentActivity().getPackageManager();
            appLabel = (String)packageManager.getApplicationLabel(appInfo);
            if (appLabel != null)
                appLabels.put(appInfo, appLabel);
        }
        else
            appLabel = appLabels.get(appInfo);
        return appLabel;
    }
    public static String GetAppLabel(ResolveInfo rslvInfo) {
        String appName = null;
        String pkgName = rslvInfo.activityInfo.packageName;
        if (pkgName != null) {
            ApplicationInfo pkgInfo = PackagesCache.GetPackageInfo(pkgName);
            if (pkgInfo != null)
                appName = GetAppLabel(pkgInfo);
//            intentNames.add(new DetailItem(PackagesCache.GetPackageIcon(pkgName), appName, null, pkgAppsList.get(i)));
        }
        return appName;
    }
    public static ResolveInfo GetResolveInfo(String packageName) {
        return ActivityManager.GetCurrentActivity().getPackageManager().resolveActivity(new IntentLaunchData(packageName).BuildIntent(), 0);
    }
    public static String GetPackageNameForExtension(String extension) {
        for (int i = 0; i < launchIntents.size(); i++) {
            IntentLaunchData currentLaunchIntent = launchIntents.get(i);
            if (currentLaunchIntent.ContainsExtension(extension)) {
                return currentLaunchIntent.GetPackageName();
            }
        }
        return null;
    }
}
