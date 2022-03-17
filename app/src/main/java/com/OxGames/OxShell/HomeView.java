package com.OxGames.OxShell;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class HomeView extends SlideTouchGridView {
    public HomeView(Context context) {
        super(context);
    }
    public HomeView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public HomeView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean onKeyDown(int key_code, KeyEvent key_event) {
        if (key_code == KeyEvent.KEYCODE_BUTTON_R2) {
            ActivityManager.GoTo(ActivityManager.Page.runningApps);
            return false;
        }
        return super.onKeyDown(key_code, key_event);
    }
    @Override
    public void MakeSelection() {
        HomeItem selectedItem = (HomeItem)getItemAtPosition(properPosition);
        if (selectedItem.type == HomeItem.Type.explorer) {
            ActivityManager.GoTo(ActivityManager.Page.explorer);
//            HomeActivity.GetInstance().GoTo(HomeActivity.Page.explorer);
        } else if (selectedItem.type == HomeItem.Type.app) {
            (new IntentLaunchData((String)selectedItem.obj)).Launch();
        } else if (selectedItem.type == HomeItem.Type.add) {
            ActivityManager.GoTo(ActivityManager.Page.addToHome);
//            HomeActivity.GetInstance().GoTo(HomeActivity.Page.addToHome);
        } else if (selectedItem.type == HomeItem.Type.assoc) {
            IntentShortcutsView.SetLaunchItem(selectedItem);
            ActivityManager.GoTo(ActivityManager.Page.intentShortcuts);
        }
    }
    @Override
    public void DeleteSelection() {
        HomeItem selectedItem = (HomeItem)getItemAtPosition(properPosition);
        HomeManager.RemoveItem(selectedItem);
    }
    @Override
    public void Refresh() {
        ArrayList<GridItem> homeItems = HomeManager.GetItems();
        if (homeItems == null)
            homeItems = new ArrayList<>();

        homeItems.add(new HomeItem(HomeItem.Type.add));

        GridAdapter customAdapter = new GridAdapter(getContext(), homeItems);
        setAdapter(customAdapter);
    }
}