package com.OxGames.OxShell.Views;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.WindowManager;

import com.OxGames.OxShell.Data.XMBItem;
import com.OxGames.OxShell.Helpers.ActivityManager;
import com.OxGames.OxShell.Data.HomeItem;
import com.OxGames.OxShell.Data.HomeManager;
import com.OxGames.OxShell.Data.IntentLaunchData;

import java.util.ArrayList;

public class HomeView extends XMBView {
    private ContextMenu overlay;

    public HomeView(Context context) {
        super(context);
    }
    public HomeView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public HomeView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
//        setLayoutParams(new GridView.LayoutParams(256, 256));
    }

    @Override
    public boolean receiveKeyEvent(KeyEvent key_event) {
        if (key_event.getAction() == KeyEvent.ACTION_DOWN) {
            if (key_event.getKeyCode() == KeyEvent.KEYCODE_BUTTON_R2) {
                //ActivityManager.GoTo(ActivityManager.Page.runningApps);
                return true;
            }
            if (key_event.getKeyCode() == KeyEvent.KEYCODE_BUTTON_X) {
                //HomeItem selectedItem = (HomeItem)getItemAtPosition(properPosition);
//                int columns = getNumColumns();
//                int col = properPosition % columns;
//                int row = properPosition / columns;
//                int x = getPaddingLeft() + (getColumnWidth() + getHorizontalSpacing()) * col;
//                int y = getPaddingTop() + (getColumnWidth() + getVerticalSpacing()) * row;
//                Log.d("HomeView", "Opening context menu at (" + x + ", " + y + ")");

                // int x = AndroidHelpers.getRelativeLeft(view);
                // int y = AndroidHelpers.getRelativeTop(view);
                // int width = view.getWidth();
                // int height = view.getHeight();
                showCustomContextMenu(0, 0);
                return true;
            }
        }
        return super.receiveKeyEvent(key_event);
    }
    @Override
    public void makeSelection() {
        HomeItem selectedItem = (HomeItem) getSelectedItem();
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
        HomeItem selectedItem = (HomeItem) getSelectedItem();
        HomeManager.removeItem(selectedItem);
        //refresh();
    }
    public void uninstallSelection() {
        HomeItem selectedItem = (HomeItem) getSelectedItem();
        if (selectedItem.type == HomeItem.Type.app) {
            Intent intent = new Intent(Intent.ACTION_DELETE);
            intent.setData(Uri.parse("package:" + selectedItem.obj));
            ActivityManager.getCurrentActivity().startActivity(intent);
            //TODO: Figure out how to get on uninstalled event and remove item when fired
        }
    }
    @Override
    public void refresh() {
        Log.d("HomeView", "Refreshing home view");
        ArrayList<XMBItem> homeItems = HomeManager.getItems();
        if (homeItems == null)
            homeItems = new ArrayList<>();

        homeItems.add(new HomeItem(HomeItem.Type.settings));

//        XMBCat mainCat = new XMBCat("Apps");
//        for (XMBItem homeItem : homeItems)
//            if (((HomeItem)homeItem).type == HomeItem.Type.app)
//                homeItem.category = mainCat;

        int cachedIndex = currentIndex;
        clear();
        addCatItems(homeItems);
        //addItems(homeItems);
        setIndex(cachedIndex);

        super.refresh();
    }

    private void showCustomContextMenu(int x, int y) {
        overlay = new ContextMenu(ActivityManager.getCurrentActivity());
        overlay.setCancelable(true);
        overlay.addButton("Move", () -> {
            Log.d("DialogItemSelection", "Move");
            return null;
        });
        overlay.addButton("Remove", () -> {
            deleteSelection();
            overlay.dismiss();
            return null;
        });
        HomeItem selectedItem = (HomeItem) getSelectedItem();
        if (selectedItem.type != HomeItem.Type.explorer && selectedItem.type != HomeItem.Type.settings)
            overlay.addButton("Uninstall", () -> {
                uninstallSelection();
                deleteSelection(); //only if uninstall was successful
                overlay.dismiss();
                return null;
            });
        overlay.addButton("Cancel", () -> {
            overlay.dismiss();
            return null;
        });
        WindowManager.LayoutParams params = overlay.getWindow().getAttributes();
        params.x = x;
        params.y = y;
        overlay.getWindow().setAttributes(params);
        overlay.show();
    }
}