package com.OxGames.OxShell.Views;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.OxGames.OxShell.Helpers.ActivityManager;
import com.OxGames.OxShell.Adapters.GridAdapter;
import com.OxGames.OxShell.Data.GridItem;
import com.OxGames.OxShell.Data.HomeItem;
import com.OxGames.OxShell.Data.HomeManager;
import com.OxGames.OxShell.Data.IntentLaunchData;

import java.util.ArrayList;

public class HomeView extends SlideTouchGridView {
    private HomeContextMenu overlay;

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
            if (key_event.getKeyCode() == KeyEvent.KEYCODE_BUTTON_X) {
                HomeItem selectedItem = (HomeItem)getItemAtPosition(properPosition);
                Log.d("HomeView", "Opening context menu for " + selectedItem.title + " with position (" + selectedItem.dim.left + ", " + selectedItem.dim.top + ")");

                showCustomContextMenu(selectedItem.dim.left, selectedItem.dim.top);
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
            (IntentLaunchData.createFromPackage((String)selectedItem.obj, Intent.FLAG_ACTIVITY_NEW_TASK)).launch();
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
    public void uninstallSelection() {
        HomeItem selectedItem = (HomeItem)getItemAtPosition(properPosition);
        if (selectedItem.type == HomeItem.Type.app) {
            Intent intent = new Intent(Intent.ACTION_DELETE);
            intent.setData(Uri.parse("package:" + selectedItem.obj));
            ActivityManager.getCurrentActivity().startActivity(intent);
        }
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

    private void showCustomContextMenu(int x, int y) {
        overlay = new HomeContextMenu(ActivityManager.getCurrentActivity());
        overlay.setCancelable(true);
        overlay.currentHomeView = this;
        //Add buttons specific for the selected item and handle button events here
        overlay.show();
        WindowManager.LayoutParams params = overlay.getWindow().getAttributes();
        params.x = x;
        params.y = y;
        overlay.getWindow().setAttributes(params);
    }
}