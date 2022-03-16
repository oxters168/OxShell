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

    public static void Init() {
        if (homeItems == null) {
            if ((new File(HOME_ITEMS_FILE)).exists()) {
                if (!ExplorerBehaviour.HasReadStoragePermission()) {
                    AddTempExplorer(); //Temp explorer in case no permissions granted

                    ExplorerBehaviour.RequestReadStoragePermission();
                    ActivityManager.GetCurrentActivity().AddPermissionListener(new PermissionsListener() {
                        @Override
                        public void onPermissionResponse(int requestCode, String[] permissions, int[] grantResults) {
                            if (requestCode == ExplorerBehaviour.READ_EXTERNAL_STORAGE) {
                                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                                    LoadHomeItems();
                                else
                                    Log.e("HomeManager", "Read storage permission denied");
                            }
                        }
                    });
                }
                else
                    LoadHomeItems();
            } else
                AddTempExplorer(); //Since nothing is saved anyway no need to save this because it is the default (this way the user isn't always bombarded with permission request on start, only when they want to add something to the home)
        }
    }
    public static ArrayList<HomeItem> GetItems() {
        ArrayList<HomeItem> clonedItems = null;
        if (homeItems != null)
            clonedItems = (ArrayList<HomeItem>)homeItems.clone();
        return clonedItems;
    }
    public static void AddExplorer() {
        AddItemAndSave(new HomeItem(HomeItem.Type.explorer, "Explorer"));
    }
    private static void AddTempExplorer() {
        homeItems = new ArrayList<>();
        AddItem(new HomeItem(HomeItem.Type.explorer, "Explorer"));
    }
    public static void AddItem(HomeItem homeItem) {
        homeItems.add(homeItem);
        RefreshHomeItems();
    }
    public static void AddItemAndSave(HomeItem homeItem) {
        AddItem(homeItem);
        if (!ExplorerBehaviour.HasWriteStoragePermission()) {
            ExplorerBehaviour.RequestWriteStoragePermission();
            ActivityManager.GetCurrentActivity().AddPermissionListener(new PermissionsListener() {
                @Override
                public void onPermissionResponse(int requestCode, String[] permissions, int[] grantResults) {
                    if (requestCode == ExplorerBehaviour.WRITE_EXTERNAL_STORAGE) {
                        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                            SaveHomeItems();
                        else
                            Log.e("HomeManager", "Write storage permission denied");
                    }
                }
            });
        }
        else
            SaveHomeItems();
    }
    public static void RemoveItem(HomeItem homeItem) {
        homeItems.remove(homeItem);
        RefreshHomeItems();
        SaveHomeItems();
    }
//    public static void RemoveItem(int index) {
//        homeItems.remove(index);
//        RefreshHomeItems();
//        SaveHomeItems();
//    }

    private static void LoadHomeItems() {
        homeItems = new ArrayList<>();
        Object[] savedItems = (Object[])Serialaver.LoadFile(HOME_ITEMS_FILE);
        //Removed if statement since now this function is expected to be called when there are permissions for sure, so exception/error is welcome now
//        if (savedItems != null) {
            for (int i = 0; i < savedItems.length; i++)
                AddItem((HomeItem) savedItems[i]);
//        }
        RefreshHomeItems();
    }
    private static void SaveHomeItems() {
        try {
            File homeItemsDir = new File(HOME_ITEMS_DIR);
            homeItemsDir.mkdirs();
            File homeItemsFile = new File(HOME_ITEMS_FILE);
            if (!homeItemsFile.exists())
                homeItemsFile.createNewFile();
        } catch (IOException ex) {
            Log.e("HomeManager", ex.getMessage());
        }
        Serialaver.SaveFile(homeItems.toArray(), HOME_ITEMS_FILE);
    }
    private static void RefreshHomeItems() {
        ((HomeActivity)ActivityManager.GetInstance(HomeActivity.class)).RefreshHome();
    }
}
