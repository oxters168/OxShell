package com.OxGames.OxShell;

import android.content.pm.PackageManager;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class HomeManager {
    private static ArrayList<HomeItem> homeItems;
    private static final String HOME_ITEMS_DIR = Environment.getExternalStorageDirectory() + "/OxShell";
    private static final String HOME_ITEMS_FILE = Environment.getExternalStorageDirectory() + "/OxShell/HomeItems";

    public static void init() {
        if (homeItems == null) {
            if ((new File(HOME_ITEMS_FILE)).exists()) {
                if (!ExplorerBehaviour.hasReadStoragePermission()) {
                    addTempExplorer(); //Temp explorer in case no permissions granted

                    ExplorerBehaviour.requestReadStoragePermission();
                    ActivityManager.getCurrentActivity().addPermissionListener(new PermissionsListener() {
                        @Override
                        public void onPermissionResponse(int requestCode, String[] permissions, int[] grantResults) {
                            if (requestCode == ExplorerBehaviour.READ_EXTERNAL_STORAGE) {
                                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                                    loadHomeItems();
                                else
                                    Log.e("HomeManager", "Read storage permission denied");
                            }
                        }
                    });
                }
                else
                    loadHomeItems();
            } else
                addTempExplorer(); //Since nothing is saved anyway no need to save this because it is the default (this way the user isn't always bombarded with permission request on start, only when they want to add something to the home)
        }
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
        addItem(homeItem);
        if (!ExplorerBehaviour.hasWriteStoragePermission()) {
            ExplorerBehaviour.requestWriteStoragePermission();
            ActivityManager.getCurrentActivity().addPermissionListener(new PermissionsListener() {
                @Override
                public void onPermissionResponse(int requestCode, String[] permissions, int[] grantResults) {
                    if (requestCode == ExplorerBehaviour.WRITE_EXTERNAL_STORAGE) {
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
        try {
            File homeItemsDir = new File(HOME_ITEMS_DIR);
            homeItemsDir.mkdirs();
            File homeItemsFile = new File(HOME_ITEMS_FILE);
            if (!homeItemsFile.exists())
                homeItemsFile.createNewFile();
        } catch (IOException ex) {
            Log.e("HomeManager", ex.getMessage());
        }
        Serialaver.saveFile(homeItems.toArray(), HOME_ITEMS_FILE);
    }
    private static void refreshHomeItems() {
        ((HomeActivity)ActivityManager.getInstance(HomeActivity.class)).refreshHome();
    }
}
