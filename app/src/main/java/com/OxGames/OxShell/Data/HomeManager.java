package com.OxGames.OxShell.Data;

import android.content.pm.PackageManager;
import android.util.Log;

import com.OxGames.OxShell.Helpers.ActivityManager;
import com.OxGames.OxShell.Helpers.AndroidHelpers;
import com.OxGames.OxShell.HomeActivity;
import com.OxGames.OxShell.Helpers.Serialaver;

import java.util.ArrayList;

public class HomeManager {
    //private static boolean initialized = false;
    private static ArrayList<HomeItem> homeItems;

    public static boolean isInitialized() {
        return homeItems != null;
    }
    public static void init() {
        if (!isInitialized()) {
            homeItems = new ArrayList<>();
            //initialized = true;
            boolean requestingAccess = false;
            if (AndroidHelpers.fileExists(Paths.HOME_ITEMS_EXTERNAL_PATH)) {
                if (!AndroidHelpers.hasReadStoragePermission()) {
                    Log.d("HomeManager", "HomeItems exists but we do not have permission to read, requesting permission");
                    addExplorer(); //Temp explorer in case no permissions granted

                    requestingAccess = true;
                    //TODO: show pop up message explaining why storage access is being requested
                    AndroidHelpers.requestReadStoragePermission();
                    ActivityManager.getCurrentActivity().addPermissionListener((requestCode, permissions, grantResults) -> {
                        if (requestCode == AndroidHelpers.READ_EXTERNAL_STORAGE) {
                            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                                Log.d("HomeManager", "Read permission granted, loading HomeItems from disk");
                                loadHomeItemsFromFile(Paths.STORAGE_DIR_EXTERNAL, Paths.HOME_ITEMS_FILE_NAME);
                                saveHomeItemsToFile(Paths.STORAGE_DIR_INTERNAL, Paths.HOME_ITEMS_FILE_NAME);
                            } else {
                                Log.e("HomeManager", "Read storage permission denied");
                                initInternal();
                            }
                        }
                    });
                } else {
                    Log.d("HomeManager", "HomeItems exists and we have permission to read, loading HomeItems from disk");
                    loadHomeItemsFromFile(Paths.STORAGE_DIR_EXTERNAL, Paths.HOME_ITEMS_FILE_NAME);
                    saveHomeItemsToFile(Paths.STORAGE_DIR_INTERNAL, Paths.HOME_ITEMS_FILE_NAME);
                }
            } else {
                Log.d("HomeManager", "HomeItems did not exist, placing default home items");
                addExplorer(); //Since nothing is saved anyway no need to save this because it is the default (this way the user isn't always bombarded with permission request on start, only when they want to add something to the home)
            }
            if (!requestingAccess)
                initInternal();
        } else
            Log.d("HomeManager", "homeItems not null, so not reading from disk");
    }
    private static void initInternal() {
        if (AndroidHelpers.fileExists(Paths.HOME_ITEMS_INTERNAL_PATH))
            loadHomeItemsFromFile(Paths.STORAGE_DIR_INTERNAL, Paths.HOME_ITEMS_FILE_NAME);
        else
            saveHomeItemsToFile(Paths.STORAGE_DIR_INTERNAL, Paths.HOME_ITEMS_FILE_NAME);
    }
    public static ArrayList<GridItem> getItems() {
        //Might be called before initialization
        return homeItems != null ? new ArrayList<>(homeItems) : null;
    }
    public static void addExplorerAndSave() {
        addItemAndSave(new HomeItem(HomeItem.Type.explorer, "Explorer"));
    }
    private static void addExplorer() {
        addItem(new HomeItem(HomeItem.Type.explorer, "Explorer"));
    }
    public static void addItem(HomeItem homeItem) {
        homeItems.add(homeItem);
        refreshHomeItems();
    }
    public static void addItemAndSave(HomeItem homeItem) {
        Log.d("HomeManager", "Adding item and saving to disk");
        addItem(homeItem);
        saveHomeItemsToFile(Paths.STORAGE_DIR_INTERNAL, Paths.HOME_ITEMS_FILE_NAME);
        //TODO: add settings toggle for storing data externally and use that as a condition as well
        if (AndroidHelpers.hasWriteStoragePermission())
            saveHomeItemsToFile(Paths.STORAGE_DIR_EXTERNAL, Paths.HOME_ITEMS_FILE_NAME);
    }
    public static void removeItem(HomeItem homeItem) {
        homeItems.remove(homeItem);
        refreshHomeItems();
        saveHomeItemsToFile(Paths.STORAGE_DIR_INTERNAL, Paths.HOME_ITEMS_FILE_NAME);
        if (AndroidHelpers.hasWriteStoragePermission())
            saveHomeItemsToFile(Paths.STORAGE_DIR_EXTERNAL, Paths.HOME_ITEMS_FILE_NAME);
    }

    private static void loadHomeItemsFromFile(String parentDir, String fileName) {
        homeItems = new ArrayList<>();
        // Object[] savedItems = (Object[])Serialaver.loadFile(AndroidHelpers.combinePaths(parentDir, fileName));
        HomeItem[] savedItems = Serialaver.loadFromJSON(AndroidHelpers.combinePaths(parentDir, fileName), HomeItem[].class);
        if (savedItems != null) {
            for (HomeItem savedItem : savedItems)
                addItem(savedItem);
        } else
            Log.e("HomeManager", "Could not load home items, format unknown");
        refreshHomeItems();
    }
    private static void saveHomeItemsToFile(String parentDir, String fileName) {
        AndroidHelpers.makeDir(parentDir);
        String fullPath = AndroidHelpers.combinePaths(parentDir, fileName);
        AndroidHelpers.makeFile(fullPath);
        // Serialaver.saveFile(homeItems.toArray(), fullPath);
        Serialaver.saveAsJSON(homeItems.toArray(), fullPath);
    }
    private static void refreshHomeItems() {
        ((HomeActivity)ActivityManager.getInstance(HomeActivity.class)).refreshHome();
    }
}
