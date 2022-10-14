package com.OxGames.OxShell;

import android.content.pm.PackageManager;
import android.os.Environment;
import android.util.Log;

import java.util.ArrayList;

public class HomeManager {
    private static ArrayList<HomeItem> homeItems;
    private static final String HOME_ITEMS_DIR = Environment.getExternalStorageDirectory() + "/OxShell";
    private static final String HOME_ITEMS_FILE = Environment.getExternalStorageDirectory() + "/OxShell/HomeItems";

    public static void init() {
        if (homeItems == null) {
            if (AndroidHelpers.fileExists(HOME_ITEMS_FILE)) {
                if (!AndroidHelpers.hasReadStoragePermission()) {
                    Log.d("HomeManager", "HomeItems exists but we do not have permission to read, requesting permission");
                    addTempExplorer(); //Temp explorer in case no permissions granted

                    AndroidHelpers.requestReadStoragePermission();
                    ActivityManager.getCurrentActivity().addPermissionListener(new PermissionsListener() {
                        @Override
                        public void onPermissionResponse(int requestCode, String[] permissions, int[] grantResults) {
                            if (requestCode == AndroidHelpers.READ_EXTERNAL_STORAGE) {
                                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                                    Log.d("HomeManager", "Read permission granted, loading HomeItems from disk");
                                    loadHomeItems();
                                } else
                                    Log.e("HomeManager", "Read storage permission denied");
                            }
                        }
                    });
                }
                else {
                    Log.d("HomeManager", "HomeItems exists and we have permission to read, loading HomeItems from disk");
                    loadHomeItems();
                }
            } else {
                Log.d("HomeManager", "HomeItems did not exist, placing default home items");
                addTempExplorer(); //Since nothing is saved anyway no need to save this because it is the default (this way the user isn't always bombarded with permission request on start, only when they want to add something to the home)
            }
        } else
            Log.d("HomeManager", "homeItems not null, so not reading from disk");
    }
    public static ArrayList<GridItem> getItems() {
        if (homeItems != null) {
            ArrayList<GridItem> convertedItems = new ArrayList<>();
            for (int i = 0; i < homeItems.size(); i++) {
                convertedItems.add(homeItems.get(i));
            }
            return convertedItems;
        } else
            return null;
    }
    public static void addExplorer() {
        addItemAndSave(new HomeItem(HomeItem.Type.explorer, "Explorer"));
    }
    private static void addTempExplorer() {
        homeItems = new ArrayList<>();
        addItem(new HomeItem(HomeItem.Type.explorer, "Explorer"));
    }
    public static void addItem(HomeItem homeItem) {
        homeItems.add(homeItem);
        refreshHomeItems();
    }
    public static void addItemAndSave(HomeItem homeItem) {
        Log.d("HomeManager", "Adding item and saving to disk");
        addItem(homeItem);
        if (!AndroidHelpers.hasWriteStoragePermission()) {
            AndroidHelpers.requestWriteStoragePermission();
            ActivityManager.getCurrentActivity().addPermissionListener(new PermissionsListener() {
                @Override
                public void onPermissionResponse(int requestCode, String[] permissions, int[] grantResults) {
                    if (requestCode == AndroidHelpers.WRITE_EXTERNAL_STORAGE) {
                        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                            saveHomeItems();
                        else
                            Log.e("HomeManager", "Write storage permission denied");
                    }
                }
            });
        }
        else
            saveHomeItems();
    }
    public static void removeItem(HomeItem homeItem) {
        homeItems.remove(homeItem);
        refreshHomeItems();
        saveHomeItems();
    }
//    public static void RemoveItem(int index) {
//        homeItems.remove(index);
//        RefreshHomeItems();
//        SaveHomeItems();
//    }

    private static void loadHomeItems() {
        homeItems = new ArrayList<>();
        Object[] savedItems = (Object[])Serialaver.loadFile(HOME_ITEMS_FILE);
        //Removed if statement since now this function is expected to be called when there are permissions for sure, so exception/error is welcome now
//        if (savedItems != null) {
            for (int i = 0; i < savedItems.length; i++)
                addItem((HomeItem) savedItems[i]);
//        }
        refreshHomeItems();
    }
    private static void saveHomeItems() {
        AndroidHelpers.makeDir(HOME_ITEMS_DIR);
        AndroidHelpers.makeFile(HOME_ITEMS_FILE);
        Serialaver.saveFile(homeItems.toArray(), HOME_ITEMS_FILE);
    }
    private static void refreshHomeItems() {
        ((HomeActivity)ActivityManager.getInstance(HomeActivity.class)).refreshHome();
    }
}
