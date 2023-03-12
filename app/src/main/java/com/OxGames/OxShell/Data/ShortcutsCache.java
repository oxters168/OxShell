package com.OxGames.OxShell.Data;

import android.content.Intent;

import com.OxGames.OxShell.Helpers.AndroidHelpers;
import com.OxGames.OxShell.Helpers.Serialaver;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class ShortcutsCache {
    private static HashMap<UUID, IntentLaunchData> intents = new HashMap<>();

    public static void readIntentsFromDisk() {
        if (AndroidHelpers.dirExists(Paths.SHORTCUTS_DIR_INTERNAL)) {
            intents = new HashMap<>();
            File[] files = AndroidHelpers.listContents(Paths.SHORTCUTS_DIR_INTERNAL);
            for (File file : files) {
                IntentLaunchData intent = (IntentLaunchData)Serialaver.loadFromFSTJSON(file.getAbsolutePath());
                intents.put(intent.getId(), intent);
            }
        }
    }
    public static void createAndStoreDefaults() {
        for (IntentLaunchData intent : createDefaultLaunchIntents())
            intents.put(intent.getId(), intent);
        writeIntentsToDisk();
    }
    private static void writeIntentsToDisk() {
        if (!AndroidHelpers.dirExists(Paths.SHORTCUTS_DIR_INTERNAL))
            AndroidHelpers.makeDir(Paths.SHORTCUTS_DIR_INTERNAL);

        for (IntentLaunchData intent : intents.values()) {
            saveIntentData(intent, Paths.SHORTCUTS_DIR_INTERNAL);
            //if (AndroidHelpers.hasWriteStoragePermission())
            //    saveIntentData(intent, Paths.SHORTCUTS_DIR_EXTERNAL);
        }
    }
    private static List<IntentLaunchData> createDefaultLaunchIntents() {
        List<IntentLaunchData> defaults = new ArrayList<>();

        //Cheat sheet: http://p.cweiske.de/221
        IntentLaunchData gbaLaunchIntent = new IntentLaunchData("GBA", Intent.ACTION_VIEW, "com.fastemulator.gba", "com.fastemulator.gba.EmulatorActivity", new String[] { "gba" }, Intent.FLAG_ACTIVITY_NEW_TASK);
        gbaLaunchIntent.setDataType(IntentLaunchData.DataType.AbsPathAsProvider);
        defaults.add(gbaLaunchIntent);
        //saveIntentData(gbaLaunchIntent, dir);

        IntentLaunchData ndsLaunchIntent = new IntentLaunchData("NDS", Intent.ACTION_VIEW, "com.dsemu.drastic", "com.dsemu.drastic.DraSticActivity", new String[] { "nds" }, Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NO_HISTORY);
        ndsLaunchIntent.addExtra(new IntentPutExtra("GAMEPATH", IntentLaunchData.DataType.AbsPathAsProvider));
        //ndsLaunchIntent.setDataType(IntentLaunchData.DataType.AbsPathAsProvider);
        defaults.add(ndsLaunchIntent);
        //saveIntentData(ndsLaunchIntent, dir);

        IntentLaunchData pspLaunchIntent = new IntentLaunchData("PSP", Intent.ACTION_VIEW, "org.ppsspp.ppsspp", "org.ppsspp.ppsspp.PpssppActivity", new String[] { "iso", "cso" }, Intent.FLAG_ACTIVITY_NEW_TASK);
        pspLaunchIntent.addExtra(new IntentPutExtra("org.ppsspp.ppsspp.Shortcuts", IntentLaunchData.DataType.AbsPathAsProvider));
        defaults.add(pspLaunchIntent);
        //saveIntentData(pspLaunchIntent, dir);

        IntentLaunchData ps2LaunchIntent = new IntentLaunchData("PS2", Intent.ACTION_VIEW, "xyz.aethersx2.android", "xyz.aethersx2.android.EmulationActivity", new String[] { "iso", "bin", "chd" }, Intent.FLAG_ACTIVITY_NEW_TASK);
        ps2LaunchIntent.addExtra(new IntentPutExtra("bootPath", IntentLaunchData.DataType.AbsPathAsProvider));
        defaults.add(ps2LaunchIntent);
        //saveIntentData(ps2LaunchIntent, dir);

        IntentLaunchData threedsLaunchIntent = new IntentLaunchData("3DS", Intent.ACTION_VIEW, "org.citra.emu", "org.citra.emu.ui.EmulationActivity", new String[] { "3ds", "cxi" }, Intent.FLAG_ACTIVITY_NEW_TASK);
        threedsLaunchIntent.addExtra(new IntentPutExtra("GamePath", IntentLaunchData.DataType.AbsPathAsProvider));
        defaults.add(threedsLaunchIntent);
        //saveIntentData(threedsLaunchIntent, dir);

        return defaults;
    }
    private static void saveIntentData(IntentLaunchData intentData, String dir) {
        String fileName = AndroidHelpers.combinePaths(dir, intentData.getDisplayName() + ".json");
        Serialaver.saveAsFSTJSON(intentData, fileName);
    }

    public static IntentLaunchData getIntent(UUID id) {
        return intents.get(id);
    }
    public static IntentLaunchData[] getStoredIntents() {
        return intents.values().toArray(new IntentLaunchData[0]);
    }
    public static IntentLaunchData getLaunchDataForExtension(String extension) {
        for (IntentLaunchData intent : intents.values())
            if (intent.containsExtension(extension))
                return intent;
        return null;
    }
    public static List<IntentLaunchData> getLaunchDatasForExtension(String extension) {
        ArrayList<IntentLaunchData> launchDatas = new ArrayList<>();
        for (IntentLaunchData intent : intents.values())
            if (intent.containsExtension(extension))
                launchDatas.add(intent);
        return launchDatas;
    }

    public static String getPackageNameForExtension(String extension) {
        for (IntentLaunchData intent : intents.values())
            if (intent.containsExtension(extension))
                return intent.getPackageName();
        return null;
    }
}
