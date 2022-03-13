package com.OxGames.OxShell;

import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class HomeManager {
    private static ArrayList<HomeItem> homeItems;
    private static final String HOME_ITEMS_DIR = "/storage/emulated/0/OxShell";
    private static final String HOME_ITEMS_FILE = "/storage/emulated/0/OxShell/HomeItems";

    public static void Init() {
        if (homeItems == null) {
            homeItems = new ArrayList<>();
            if ((new File(HOME_ITEMS_FILE)).exists()) {
                ExplorerBehaviour.GrantReadStoragePermission();
                //Need to add the permission event listener
                Object[] savedItems = (Object[]) Serialaver.LoadFile(HOME_ITEMS_FILE);
                if (savedItems != null)
                    for (int i = 0; i < savedItems.length; i++)
                        AddItem((HomeItem) savedItems[i]);
            } else
                AddExplorer();
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
    public static void AddItem(HomeItem homeItem) {
        homeItems.add(homeItem);
        ((HomeActivity)ActivityManager.GetInstance(HomeActivity.class)).RefreshHome();
    }
    public static void AddItemAndSave(HomeItem homeItem) {
        AddItem(homeItem);
        try {
            ExplorerBehaviour.GrantWriteStoragePermission();
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
}
