package com.OxGames.OxShell.Data;

import android.content.Intent;

import com.OxGames.OxShell.Helpers.AndroidHelpers;
import com.OxGames.OxShell.Helpers.Serialaver;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ShortcutsCache {
    private static ArrayList<IntentLaunchData> launchIntents = new ArrayList<>();

    public static void clear() {
        launchIntents.clear();
    }
    public static void readIntentsFromDisk() {
        if (AndroidHelpers.dirExists(Paths.SHORTCUTS_DIR_INTERNAL)) {
            launchIntents = new ArrayList<>();
            File[] intents = AndroidHelpers.listContents(Paths.SHORTCUTS_DIR_INTERNAL);
            for (File intent : intents) {
                launchIntents.add((IntentLaunchData)Serialaver.loadFromFSTJSON(intent.getAbsolutePath()));
            }
        }
    }
    public static void createAndStoreDefaults() {
        launchIntents.addAll(createDefaultLaunchIntents());
        writeIntentsToDisk();
    }
    private static void writeIntentsToDisk() {
        if (!AndroidHelpers.dirExists(Paths.SHORTCUTS_DIR_INTERNAL))
            AndroidHelpers.makeDir(Paths.SHORTCUTS_DIR_INTERNAL);

        for (IntentLaunchData intent : launchIntents) {
            saveIntentData(intent, Paths.SHORTCUTS_DIR_INTERNAL);
            if (AndroidHelpers.hasWriteStoragePermission())
                saveIntentData(intent, Paths.SHORTCUTS_DIR_EXTERNAL);
        }
    }
    private static List<IntentLaunchData> createDefaultLaunchIntents() {
        List<IntentLaunchData> defaults = new ArrayList<>();

        //Cheat sheet: http://p.cweiske.de/221
        IntentLaunchData gbaLaunchIntent = new IntentLaunchData("GBA", Intent.ACTION_VIEW, "com.fastemulator.gba", "com.fastemulator.gba.EmulatorActivity", new String[] { "gba" }, Intent.FLAG_ACTIVITY_NEW_TASK);
        gbaLaunchIntent.setDataType(IntentLaunchData.DataType.AbsolutePath);
        defaults.add(gbaLaunchIntent);
        //saveIntentData(gbaLaunchIntent, dir);

        IntentLaunchData ndsLaunchIntent = new IntentLaunchData("NDS", Intent.ACTION_VIEW, "com.dsemu.drastic", "com.dsemu.drastic.DraSticActivity", new String[] { "nds" }, Intent.FLAG_ACTIVITY_NEW_TASK);
        ndsLaunchIntent.addExtra(new IntentPutExtra("GAMEPATH", IntentLaunchData.DataType.AbsolutePath));
        defaults.add(ndsLaunchIntent);
        //saveIntentData(ndsLaunchIntent, dir);

        IntentLaunchData pspLaunchIntent = new IntentLaunchData("PSP", Intent.ACTION_VIEW, "org.ppsspp.ppsspp", "org.ppsspp.ppsspp.PpssppActivity", new String[] { "iso", "cso" }, Intent.FLAG_ACTIVITY_NEW_TASK);
        pspLaunchIntent.addExtra(new IntentPutExtra("org.ppsspp.ppsspp.Shortcuts", IntentLaunchData.DataType.AbsolutePath));
        defaults.add(pspLaunchIntent);
        //saveIntentData(pspLaunchIntent, dir);

        IntentLaunchData ps2LaunchIntent = new IntentLaunchData("PS2", Intent.ACTION_VIEW, "xyz.aethersx2.android", "xyz.aethersx2.android.EmulationActivity", new String[] { "iso", "bin", "chd" }, Intent.FLAG_ACTIVITY_NEW_TASK);
        ps2LaunchIntent.addExtra(new IntentPutExtra("bootPath", IntentLaunchData.DataType.AbsolutePath));
        defaults.add(ps2LaunchIntent);
        //saveIntentData(ps2LaunchIntent, dir);

        IntentLaunchData threedsLaunchIntent = new IntentLaunchData("3DS", Intent.ACTION_VIEW, "org.citra.emu", "org.citra.emu.ui.EmulationActivity", new String[] { "3ds", "cxi" }, Intent.FLAG_ACTIVITY_NEW_TASK);
        threedsLaunchIntent.addExtra(new IntentPutExtra("GamePath", IntentLaunchData.DataType.AbsolutePath));
        defaults.add(threedsLaunchIntent);
        //saveIntentData(threedsLaunchIntent, dir);

        return defaults;
    }
    private static void saveIntentData(IntentLaunchData intentData, String dir) {
        String fileName = AndroidHelpers.combinePaths(dir, intentData.getDisplayName() + ".json");
        Serialaver.saveAsFSTJSON(intentData, fileName);
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
    public static List<IntentLaunchData> getLaunchDatasForExtension(String extension) {
        ArrayList<IntentLaunchData> launchDatas = new ArrayList<>();
        for (int i = 0; i < launchIntents.size(); i++) {
            IntentLaunchData currentLaunchIntent = launchIntents.get(i);
            if (currentLaunchIntent.containsExtension(extension))
                launchDatas.add(currentLaunchIntent);
        }
        return launchDatas;
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
