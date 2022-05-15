package com.OxGames.OxShell;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;

import java.util.ArrayList;

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
    public boolean receiveKeyEvent(KeyEvent key_event) {
        if (key_event.getAction() == KeyEvent.ACTION_DOWN) {
            if (key_event.getKeyCode() == KeyEvent.KEYCODE_BUTTON_R2) {
                //ActivityManager.GoTo(ActivityManager.Page.runningApps);
                return true;
            }
        }
        return super.receiveKeyEvent(key_event);
    }
    @Override
    public void makeSelection() {
        HomeItem selectedItem = (HomeItem)getItemAtPosition(properPosition);
        if (selectedItem.type == HomeItem.Type.explorer) {
            ActivityManager.goTo(ActivityManager.Page.explorer);
//            HomeActivity.GetInstance().GoTo(HomeActivity.Page.explorer);
        } else if (selectedItem.type == HomeItem.Type.app) {
            (new IntentLaunchData((String)selectedItem.obj)).launch();
        } else if (selectedItem.type == HomeItem.Type.settings) {
            ActivityManager.goTo(ActivityManager.Page.settings);
//            HomeActivity.GetInstance().GoTo(HomeActivity.Page.addToHome);
        } else if (selectedItem.type == HomeItem.Type.assoc) {
            IntentShortcutsView.setLaunchItem(selectedItem);
            ActivityManager.goTo(ActivityManager.Page.intentShortcuts);
        }
    }
    @Override
    public void deleteSelection() {
        HomeItem selectedItem = (HomeItem)getItemAtPosition(properPosition);
        HomeManager.removeItem(selectedItem);
    }
    @Override
    public void refresh() {
        ArrayList<GridItem> homeItems = HomeManager.getItems();
        if (homeItems == null)
            homeItems = new ArrayList<>();

        homeItems.add(new HomeItem(HomeItem.Type.settings));

        GridAdapter customAdapter = new GridAdapter(getContext(), homeItems);
        setAdapter(customAdapter);
        super.refresh();
    }
}