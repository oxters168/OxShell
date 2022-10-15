package com.OxGames.OxShell;

import android.content.pm.PackageManager;
import android.os.Environment;
import android.util.Log;

import java.util.ArrayList;

public class HomeManager {
    private static boolean initialized = false;
    private static ArrayList<HomeItem> homeItems = new ArrayList<>();

    private static final String HOME_ITEMS_FILE_NAME = "HomeItems";
    private static final String STORAGE_DIR_EXTERNAL = Environment.getExternalStorageDirectory() + "/OxShell";
    private static final String STORAGE_DIR_INTERNAL = ActivityManager.getCurrentActivity().getFilesDir().toString();

    private static final String HOME_ITEMS_EXTERNAL_PATH;
    static {
        HOME_ITEMS_EXTERNAL_PATH = AndroidHelpers.combinePaths(STORAGE_DIR_EXTERNAL, HOME_ITEMS_FILE_NAME);
    }
    private static final String HOME_ITEMS_INTERNAL_PATH;
    static {
        HOME_ITEMS_INTERNAL_PATH = AndroidHelpers.combinePaths(STORAGE_DIR_INTERNAL, HOME_ITEMS_FILE_NAME);
    }

    public static void init() {
        if (!initialized) {
            initialized = true;
            boolean requestingAccess = false;
            if (AndroidHelpers.fileExists(HOME_ITEMS_EXTERNAL_PATH)) {
                if (!AndroidHelpers.hasReadStoragePermission()) {
                    Log.d("HomeManager", "HomeItems exists but we do not have permission to read, requesting permission");
                    addExplorer(); //Temp explorer in case no permissions granted

                    requestingAccess = true;
                    //TODO: show pop up message explaining why storage access is being requested
                    AndroidHelpers.requestReadStoragePermission();
                    ActivityManager.getCurrentActivity().addPermissionListener(new PermissionsListener() {
                        @Override
                        public void onPermissionResponse(int requestCode, String[] permissions, int[] grantResults) {
                            if (requestCode == AndroidHelpers.READ_EXTERNAL_STORAGE) {
                                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                                    Log.d("HomeManager", "Read permission granted, loading HomeItems from disk");
                                    loadHomeItemsFromFile(STORAGE_DIR_EXTERNAL, HOME_ITEMS_FILE_NAME);
                                    saveHomeItemsToFile(STORAGE_DIR_INTERNAL, HOME_ITEMS_FILE_NAME);
                                } else {
                                    Log.e("HomeManager", "Read storage permission denied");
                                    initInternal();
                                }
                            }
                        }
                    });
                } else {
                    Log.d("HomeManager", "HomeItems exists and we have permission to read, loading HomeItems from disk");
                    loadHomeItemsFromFile(STORAGE_DIR_EXTERNAL, HOME_ITEMS_FILE_NAME);
                    saveHomeItemsToFile(STORAGE_DIR_INTERNAL, HOME_ITEMS_FILE_NAME);
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
        if (AndroidHelpers.fileExists(HOME_ITEMS_INTERNAL_PATH))
            loadHomeItemsFromFile(STORAGE_DIR_INTERNAL, HOME_ITEMS_FILE_NAME);
        else
            saveHomeItemsToFile(STORAGE_DIR_INTERNAL, HOME_ITEMS_FILE_NAME);
    }
    public static ArrayList<GridItem> getItems() {
        return new ArrayList<>(homeItems);
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
        saveHomeItemsToFile(STORAGE_DIR_INTERNAL, HOME_ITEMS_FILE_NAME);
        //TODO: add settings toggle for storing data externally and use that as a condition as well
        if (AndroidHelpers.hasWriteStoragePermission())
            saveHomeItemsToFile(STORAGE_DIR_EXTERNAL, HOME_ITEMS_FILE_NAME);
    }
    public static void removeItem(HomeItem homeItem) {
        homeItems.remove(homeItem);
        refreshHomeItems();
        saveHomeItemsToFile(STORAGE_DIR_INTERNAL, HOME_ITEMS_FILE_NAME);
        if (AndroidHelpers.hasWriteStoragePermission())
            saveHomeItemsToFile(STORAGE_DIR_EXTERNAL, HOME_ITEMS_FILE_NAME);
    }

    private static void loadHomeItemsFromFile(String parentDir, String fileName) {
        homeItems = new ArrayList<>();
        Object[] savedItems = (Object[])Serialaver.loadFile(AndroidHelpers.combinePaths(parentDir, fileName));
        if (savedItems != null) {
            for (Object savedItem : savedItems)
                addItem((HomeItem) savedItem);
        } else
            Log.e("HomeManager", "Could not load home items, format unknown");
        refreshHomeItems();
    }
    private static void saveHomeItemsToFile(String parentDir, String fileName) {
        AndroidHelpers.makeDir(parentDir);
        String fullPath = AndroidHelpers.combinePaths(parentDir, fileName);
        AndroidHelpers.makeFile(fullPath);
        Serialaver.saveFile(homeItems.toArray(), fullPath);
    }
    private static void refreshHomeItems() {
        ((HomeActivity)ActivityManager.getInstance(HomeActivity.class)).refreshHome();
    }
}
