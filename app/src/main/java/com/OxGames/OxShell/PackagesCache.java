package com.OxGames.OxShell;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.util.Log;

import java.util.ArrayList;
import java.util.Hashtable;

public class PackagesCache {
//    private static Activity context;
    private static Hashtable<String, Drawable> packageIcons = new Hashtable<>();
    private static Hashtable<String, ApplicationInfo> appInfos = new Hashtable<>();
    private static Hashtable<ApplicationInfo, String> appLabels = new Hashtable<>();
//    private static Hashtable<String, String> packageExtensionAssociations = new Hashtable<>();

    private static ArrayList<IntentLaunchData> launchIntents = new ArrayList<>();

    public static void PrepareDefaultLaunchIntents() {
        //Cheat sheet: http://p.cweiske.de/221
        IntentLaunchData gbaLaunchIntent = new IntentLaunchData(Intent.ACTION_VIEW, "com.fastemulator.gba", "com.fastemulator.gba.EmulatorActivity", new String[] { "gba" });
        gbaLaunchIntent.SetDataType(IntentLaunchData.DataType.AbsolutePath);
        //            Gson gson = new Gson();
        //            String gbaJSON = gson.toJson(gbaLaunchIntent);
        //            Log.d("Intent", gbaJSON);
        //            gbaLaunchIntent = gson.fromJson(gbaJSON, IntentLaunchData.class);
        launchIntents.add(gbaLaunchIntent);
        IntentLaunchData ndsLaunchIntent = new IntentLaunchData(Intent.ACTION_VIEW, "com.dsemu.drastic", "com.dsemu.drastic.DraSticActivity", new String[] { "nds" });
        ndsLaunchIntent.AddExtra(new IntentPutExtra("GAMEPATH", IntentLaunchData.DataType.AbsolutePath));
        launchIntents.add(ndsLaunchIntent);
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

//    public static void SetContext(Activity _context) {
//        context = _context;
//    }
    public static Drawable GetPackageIcon(String packageName) {
        if (!packageIcons.containsKey(packageName)) {
            try {
                Drawable icon = ActivityManager.GetActivityInstance(ActivityManager.GetCurrent()).getPackageManager().getApplicationIcon(packageName);
                packageIcons.put(packageName, icon);
            }
            catch (PackageManager.NameNotFoundException e) {
                Log.e("PackageCache", e.getMessage());
            }
            catch (Exception e) {
                Log.e("PackageCache", e.getMessage());
            }
        }

        return packageIcons.get(packageName);
    }
    public static ApplicationInfo GetPackageInfo(String packageName) {
        ApplicationInfo appInfo = null;
        if (!appInfos.containsKey(packageName)) {
            PackageManager packageManager = ActivityManager.GetActivityInstance(ActivityManager.GetCurrent()).getPackageManager();
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
            PackageManager packageManager = ActivityManager.GetActivityInstance(ActivityManager.GetCurrent()).getPackageManager();
            appLabel = (String)packageManager.getApplicationLabel(appInfo);
            if (appLabel != null)
                appLabels.put(appInfo, appLabel);
        }
        else
            appLabel = appLabels.get(appInfo);
        return appLabel;
    }
    public static String GetPackageNameForExtension(String extension) {
        for (int i = 0; i < launchIntents.size(); i++) {
            IntentLaunchData currentLaunchIntent = launchIntents.get(i);
            if (currentLaunchIntent.ContainsExtension(extension)) {
                return currentLaunchIntent.GetPackageName();
            }
        }
        return null;
//        if (!packageExtensionAssociations.containsKey(extension)) {
//
//        }
//        return packageExtensionAssociations.get(extension);
    }
}
