package com.OxGames.OxShell.Data;

import android.content.Intent;

import com.OxGames.OxShell.Helpers.AndroidHelpers;
import com.OxGames.OxShell.Helpers.ExplorerBehaviour;
import com.OxGames.OxShell.Helpers.Serialaver;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class ShortcutsCache {
    private static HashMap<UUID, IntentLaunchData> intents = new HashMap<>();

    public static void readIntentsFromDisk() {
        //HashMap<UUID, IntentLaunchData> intents = new HashMap<>();
        if (AndroidHelpers.dirExists(Paths.SHORTCUTS_DIR_INTERNAL)) {
            intents = new HashMap<>();
            File[] files = AndroidHelpers.listContents(Paths.SHORTCUTS_DIR_INTERNAL);
            for (File file : files) {
                IntentLaunchData intent = (IntentLaunchData)Serialaver.loadFromFSTJSON(file.getAbsolutePath());
                intents.put(intent.getId(), intent);
            }
        }
        //return intents;
    }
    public static void createAndStoreDefaults() {
        //for (IntentLaunchData intent : createDefaultLaunchIntents())
        //    intents.put(intent.getId(), intent);
        writeIntentsToDisk(createDefaultLaunchIntents());
    }
    public static void addIntent(IntentLaunchData intent) {
        //intents.put(intent.getId(), intent);
        saveIntentData(intent);
        readIntentsFromDisk();
    }
    private static void writeIntentsToDisk(IntentLaunchData... intents) {
        for (IntentLaunchData intent : intents)
            saveIntentData(intent);
            //if (AndroidHelpers.hasWriteStoragePermission())
            //    saveIntentData(intent, Paths.SHORTCUTS_DIR_EXTERNAL);
        readIntentsFromDisk();
    }
    private static IntentLaunchData[] createDefaultLaunchIntents() {
        List<IntentLaunchData> defaults = new ArrayList<>();

        //Cheat sheet: http://p.cweiske.de/221
        String myBoyPkg = "com.fastemulator.gba";
        if (PackagesCache.isPackageInstalled(myBoyPkg)) {
            IntentLaunchData gbaLaunchIntent = new IntentLaunchData("GBA", Intent.ACTION_VIEW, myBoyPkg, "com.fastemulator.gba.EmulatorActivity", new String[]{"gba"}, Intent.FLAG_ACTIVITY_NEW_TASK);
            gbaLaunchIntent.setDataType(IntentLaunchData.DataType.AbsolutePath);
            defaults.add(gbaLaunchIntent);
        }

        // not working anymore
//        String drasticPkg = "com.dsemu.drastic";
//        if (PackagesCache.isPackageInstalled(drasticPkg)) {
//            IntentLaunchData ndsLaunchIntent = new IntentLaunchData("NDS", Intent.ACTION_VIEW, drasticPkg, "com.dsemu.drastic.DraSticActivity", new String[]{"nds"}, Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NO_HISTORY);
//            ndsLaunchIntent.addExtra(new IntentPutExtra("GAMEPATH", IntentLaunchData.DataType.AbsPathAsProvider));
//            //ndsLaunchIntent.setDataType(IntentLaunchData.DataType.AbsolutePath);
//            defaults.add(ndsLaunchIntent);
//        }

        String ppssppPkg = "org.ppsspp.ppsspp";
        if (PackagesCache.isPackageInstalled(ppssppPkg)) {
            IntentLaunchData pspLaunchIntent = new IntentLaunchData("PSP", Intent.ACTION_VIEW, ppssppPkg, "org.ppsspp.ppsspp.PpssppActivity", new String[]{"iso", "cso"}, Intent.FLAG_ACTIVITY_NEW_TASK);
            pspLaunchIntent.addExtra(new IntentPutExtra("org.ppsspp.ppsspp.Shortcuts", IntentLaunchData.DataType.AbsolutePath));
            defaults.add(pspLaunchIntent);
        }

        String aetherSx2Pkg = "xyz.aethersx2.android";
        if (PackagesCache.isPackageInstalled(aetherSx2Pkg)) {
            IntentLaunchData ps2LaunchIntent = new IntentLaunchData("PS2", Intent.ACTION_VIEW, aetherSx2Pkg, "xyz.aethersx2.android.EmulationActivity", new String[]{"iso", "bin", "chd"}, Intent.FLAG_ACTIVITY_NEW_TASK);
            ps2LaunchIntent.addExtra(new IntentPutExtra("bootPath", IntentLaunchData.DataType.AbsolutePath));
            defaults.add(ps2LaunchIntent);
        }

        String citraPkg = "org.citra.emu";
        if (PackagesCache.isPackageInstalled(citraPkg)) {
            IntentLaunchData threedsLaunchIntent = new IntentLaunchData("3DS", Intent.ACTION_VIEW, citraPkg, "org.citra.emu.ui.EmulationActivity", new String[]{"3ds", "cxi"}, Intent.FLAG_ACTIVITY_NEW_TASK);
            threedsLaunchIntent.addExtra(new IntentPutExtra("GamePath", IntentLaunchData.DataType.AbsolutePath));
            defaults.add(threedsLaunchIntent);
        }

        return defaults.toArray(new IntentLaunchData[0]);
    }
    private static void saveIntentData(IntentLaunchData intentData) {
        Serialaver.saveAsFSTJSON(intentData, getIntentPath(intentData.getId()));
    }
    private static String getIntentPath(UUID id) {
        return AndroidHelpers.combinePaths(Paths.SHORTCUTS_DIR_INTERNAL, id.toString());
    }
    public static void deleteIntent(UUID id) {
        String intentPath = getIntentPath(id);
        if (AndroidHelpers.fileExists(intentPath)) {
            ExplorerBehaviour.delete(intentPath);
            readIntentsFromDisk();
        }
    }

    public static IntentLaunchData getIntent(UUID id) {
        //HashMap<UUID, IntentLaunchData> intents = readIntentsFromDisk();
        return intents.containsKey(id) ? intents.get(id) : null;
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
