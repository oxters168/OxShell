package com.OxGames.OxShell;

import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class HomeManager {
    private static ArrayList<HomeItem> homeItems = new ArrayList<>();
    private static final String HOME_ITEMS_DIR = "/storage/emulated/0/OxShell";
    private static final String HOME_ITEMS_FILE = "/storage/emulated/0/OxShell/HomeItems";

    public static void InitItems() {
        homeItems = new ArrayList<>();
        if ((new File(HOME_ITEMS_FILE)).exists()) {
            ExplorerBehaviour.GrantReadStoragePermission();
            Object[] savedItems = (Object[])Serialaver.LoadFile(HOME_ITEMS_FILE);
            for (int i = 0; i < savedItems.length; i++)
                AddItem((HomeItem)savedItems[i]);
        } else
            AddItem(new HomeItem(HomeItem.Type.explorer, "Explorer"));
    }
    public static ArrayList<HomeItem> GetItems() {
        return (ArrayList<HomeItem>)homeItems.clone();
    }
    public static void AddItem(HomeItem homeItem) {
        homeItems.add(homeItem);
        HomeActivity.GetInstance().RefreshHome();
    }
    public static void AddItemAndSave(HomeItem homeItem) {
        AddItem(homeItem);
        try {
            ExplorerBehaviour.GrantWriteStoragePermission();
            File homeItemsDir = new File(HOME_ITEMS_DIR);
            homeItemsDir.mkdirs();
            File homeItemsFile = new File(HOME_ITEMS_FILE);
//            Files.createDirectories(homeItemsFile.getAbsolutePath());
//            homeItemsFile.mkdirs();
            if (!homeItemsFile.exists())
                homeItemsFile.createNewFile();
        } catch (IOException ex) {
            Log.e("HomeManager", ex.getMessage());
        }
        Serialaver.SaveFile(homeItems.toArray(), HOME_ITEMS_FILE);
    }
}
