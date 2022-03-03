package com.OxGames.OxShell;

import java.util.ArrayList;

public class HomeManager {
    private static ArrayList<HomeItem> homeItems = new ArrayList<>();

    public static void InitItems() {
        homeItems = new ArrayList<>();
        AddItem(new HomeItem(HomeItem.Type.explorer, "Explorer"));
    }
    public static ArrayList<HomeItem> GetItems() {
        return (ArrayList<HomeItem>)homeItems.clone();
    }
    public static void AddItem(HomeItem homeItem) {
        homeItems.add(homeItem);
        HomeActivity.GetInstance().RefreshHome();
    }
}
