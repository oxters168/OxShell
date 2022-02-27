package com.OxGames.OxShell;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.util.Log;

import java.util.ArrayList;
import java.util.Hashtable;

public class PackagesCache {
    private static Activity context;
    private static Hashtable<String, Drawable> packageIcons = new Hashtable<>();
//    private static Hashtable<String, String> packageExtensionAssociations = new Hashtable<>();

    private static ArrayList<IntentLaunchData> launchIntents = new ArrayList<>();

    public static void PrepareDefaultLaunchIntents() {
        IntentLaunchData gbaLaunchIntent = new IntentLaunchData(Intent.ACTION_VIEW, "com.fastemulator.gba", "com.fastemulator.gba.EmulatorActivity", new String[] { "gba" });
        gbaLaunchIntent.SetDataType(IntentLaunchData.IntentType.AbsolutePath);
        //            Gson gson = new Gson();
        //            String gbaJSON = gson.toJson(gbaLaunchIntent);
        //            Log.d("Intent", gbaJSON);
        //            gbaLaunchIntent = gson.fromJson(gbaJSON, IntentLaunchData.class);
        launchIntents.add(gbaLaunchIntent);
        IntentLaunchData ndsLaunchIntent = new IntentLaunchData(Intent.ACTION_VIEW, "com.dsemu.drastic", "com.dsemu.drastic.DraSticActivity", new String[] { "nds" });
        ndsLaunchIntent.AddExtra(new IntentPutExtra("GAMEPATH", IntentLaunchData.IntentType.AbsolutePath));
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

    public static void SetContext(Activity _context) {
        context = _context;
    }
    public static Drawable GetPackageIcon(String packageName) {
        if (!packageIcons.containsKey(packageName)) {
            try {
                Drawable icon = context.getPackageManager().getApplicationIcon(packageName);
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
