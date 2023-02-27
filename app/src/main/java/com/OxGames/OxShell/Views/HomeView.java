package com.OxGames.OxShell.Views;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;

import androidx.annotation.ColorInt;

import com.OxGames.OxShell.Adapters.XMBAdapter;
import com.OxGames.OxShell.Data.XMBItem;
import com.OxGames.OxShell.Helpers.ActivityManager;
import com.OxGames.OxShell.Data.HomeItem;
import com.OxGames.OxShell.Data.HomeManager;
import com.OxGames.OxShell.Data.IntentLaunchData;
import com.OxGames.OxShell.Interfaces.Refreshable;
import com.OxGames.OxShell.PagedActivity;
import com.OxGames.OxShell.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class HomeView extends XMBView implements Refreshable {
    private ArrayList<ArrayList<XMBItem>> allHomeItems;
//    private boolean moveMode;
//    private int origMoveColIndex;
//    private int origMoveLocalIndex;
//    private int moveColIndex;
//    private int moveLocalIndex;

//    @ColorInt
//    private int normalHighlightColor = Color.parseColor("#FF808080");
//    @ColorInt
//    private int moveHighlightColor = Color.parseColor("#FF808000");

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
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        PagedActivity currentActivity = ActivityManager.getCurrentActivity();
        if (!currentActivity.getSettingsDrawer().isDrawerOpen())
            return super.onInterceptTouchEvent(ev);
        else
            return false;
    }
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        PagedActivity currentActivity = ActivityManager.getCurrentActivity();
        if (!currentActivity.getSettingsDrawer().isDrawerOpen())
            return super.onTouchEvent(ev);
        else
            return false;
    }

    @Override
    public boolean affirmativeAction() {
        // this is so that we don't both go into the inner items of an item and try to execute it at the same time
        if (super.affirmativeAction())
            return true;

        if (!isInMoveMode()) {
            if (getSelectedItem() instanceof HomeItem) {
                HomeItem selectedItem = (HomeItem) getSelectedItem();
                //Log.d("HomeView", currentIndex + " selected " + selectedItem.title + " @(" + selectedItem.colIndex + ", " + selectedItem.localIndex + ")");
                if (selectedItem.type == HomeItem.Type.explorer) {
                    ActivityManager.goTo(ActivityManager.Page.explorer);
                    return true;
//            HomeActivity.GetInstance().GoTo(HomeActivity.Page.explorer);
                } else if (selectedItem.type == HomeItem.Type.app) {
                    (IntentLaunchData.createFromPackage((String)selectedItem.obj, Intent.FLAG_ACTIVITY_NEW_TASK)).launch();
                    return true;
                } else if (selectedItem.type == HomeItem.Type.settings) {
                    ActivityManager.goTo(ActivityManager.Page.settings);
                    return true;
//            HomeActivity.GetInstance().GoTo(HomeActivity.Page.addToHome);
                } else if (selectedItem.type == HomeItem.Type.assoc) {
                    IntentShortcutsView.setLaunchItem(selectedItem);
                    ActivityManager.goTo(ActivityManager.Page.intentShortcuts);
                    return true;
                }
            }
        }// else
        //    applyMove();

        return false;
    }
    @Override
    public boolean secondaryAction() {
        if (super.secondaryAction())
            return true;

        if (!isInMoveMode()) {
            PagedActivity currentActivity = ActivityManager.getCurrentActivity();
            if (!currentActivity.getSettingsDrawer().isDrawerOpen()) {
                SettingsDrawer.ContextBtn moveBtn = new SettingsDrawer.ContextBtn("Move", () ->
                {
                    toggleMoveMode(true);
                    currentActivity.getSettingsDrawer().setShown(false);
                    return null;
                });
                SettingsDrawer.ContextBtn deleteBtn = new SettingsDrawer.ContextBtn("Remove", () ->
                {
                    deleteSelection();
                    currentActivity.getSettingsDrawer().setShown(false);
                    return null;
                });
                SettingsDrawer.ContextBtn cancelBtn = new SettingsDrawer.ContextBtn("Cancel", () ->
                {
                    currentActivity.getSettingsDrawer().setShown(false);
                    return null;
                });
                XMBItem selectedItem = (XMBItem) getSelectedItem();
                HomeItem homeItem = null;
                // TODO: remove delete option for settings and empty
                // TODO: remove move option for settings and empty
                // TODO: add move column option
                // TODO: add delete column option
                // TODO: add new column option
                if (selectedItem instanceof HomeItem)
                    homeItem = (HomeItem) selectedItem;
                if (homeItem != null && homeItem.type != HomeItem.Type.explorer && homeItem.type != HomeItem.Type.settings) {
                    SettingsDrawer.ContextBtn uninstallBtn = new SettingsDrawer.ContextBtn("Uninstall", () ->
                    {
                        uninstallSelection();
                        deleteSelection(); //TODO: only if uninstall was successful
                        currentActivity.getSettingsDrawer().setShown(false);
                        return null;
                    });
                    currentActivity.getSettingsDrawer().setButtons(moveBtn, deleteBtn, uninstallBtn, cancelBtn);
                } else
                    currentActivity.getSettingsDrawer().setButtons(moveBtn, deleteBtn, cancelBtn);

                currentActivity.getSettingsDrawer().setShown(true);
                return true;
            }
        }
        return false;
    }
    @Override
    public boolean cancelAction() {
        if (super.cancelAction())
            return true;

        if (!isInMoveMode()) {
            PagedActivity currentActivity = ActivityManager.getCurrentActivity();
            if (!currentActivity.getSettingsDrawer().isDrawerOpen()) {
                currentActivity.getSettingsDrawer().setShown(false);
                return true;
            }
        }
        return false;
    }

    public void deleteSelection() {
        XMBItem selectedItem = (XMBItem)getSelectedItem();
        HomeManager.removeItem(selectedItem);
        //refresh();
    }
    public void uninstallSelection() {
        HomeItem selectedItem = (HomeItem)getSelectedItem();
        if (selectedItem.type == HomeItem.Type.app) {
            Intent intent = new Intent(Intent.ACTION_DELETE);
            intent.setData(Uri.parse("package:" + selectedItem.obj));
            getContext().startActivity(intent);
            //TODO: Figure out how to get on uninstalled event and remove item when fired
        }
    }
    @Override
    public void refresh() {
        //Log.d("HomeView", "Refreshing home view");
        //if (!isInMoveMode()) {
            allHomeItems = HomeManager.getItems();
            addSettings();
//        } else
//            removeEmptyItems();

        //fillEmptyColumns();

        ArrayList<XMBItem> homeItems = new ArrayList<>();
        int[][] mapper = new int[allHomeItems.size()][];
        for (int i = 0; i < allHomeItems.size(); i++) {
            ArrayList<XMBItem> column = allHomeItems.get(i);
            mapper[i] = new int[column.size()];
            for (int j = 0; j < column.size(); j++) {
                mapper[i][j] = homeItems.size();
                homeItems.add(column.get(j));
            }
        }

        int cachedIndex = currentIndex;
//        int colIndex;
//        int localIndex;
//        if (moveMode) {
//            colIndex = moveColIndex;
//            localIndex = moveLocalIndex;
//        } else {
//            colIndex = getColIndex();
//            localIndex = getLocalIndex();
//        }
        setAdapter(new XMBAdapter(getContext(), homeItems), mapper);
        //setAdapterColor();
        //setIndex(colIndex, localIndex, true);
        setIndex(cachedIndex, true);
    }
    private void removeEmptyItems() {
        for (int colIndex = 0; colIndex < allHomeItems.size(); colIndex++) {
            ArrayList<XMBItem> column = allHomeItems.get(colIndex);
            for (int localIndex = column.size() - 1; localIndex >= 0; localIndex--) {
                XMBItem item = column.get(localIndex);
                if (item.obj == null && !(item instanceof HomeItem) && item.title.equals("Empty"))
                    column.remove(localIndex);
            }
        }
    }
    private void fillEmptyColumns() {
        for (int colIndex = 0; colIndex < allHomeItems.size(); colIndex++) {
            ArrayList<XMBItem> column = allHomeItems.get(colIndex);
            if (column.size() <= 1) {
                XMBItem onlyItem = column.get(0);
                if (onlyItem.obj == null && !(onlyItem instanceof HomeItem)) {
                    // this column's only item is meant to be the column identifier
                    XMBItem emptyItem = new XMBItem(null, "Empty", R.drawable.ic_baseline_block_24, colIndex, 1);
                    column.add(emptyItem);
                }
            }
        }
    }
    private void addSettings() {
        //XMBItem settings = new HomeItem(HomeItem.Type.settings, "Settings");
        ArrayList<XMBItem> settingsColumn = new ArrayList<>();
        XMBItem[] innerSettings;
        XMBItem[] innerInnerSettings;
        XMBItem[] innerInnerInnerSettings;
        int colIndex = allHomeItems.size();
        int localIndex = 0;

        XMBItem settingsItem = new XMBItem(null, "Settings", R.drawable.ic_baseline_settings_24, colIndex, localIndex++);
        settingsColumn.add(settingsItem);

        innerSettings = new XMBItem[18];
        innerInnerSettings = new XMBItem[18];
        innerInnerSettings[0] = new XMBItem(null, "Add explorer item to home 2");
        innerInnerSettings[1] = new XMBItem(null, "Add application to home 2");
        innerInnerSettings[2] = new XMBItem(null, "Create new column 2");
        innerInnerSettings[3] = new XMBItem(null, "Add explorer item to home 2");
        innerInnerSettings[4] = new XMBItem(null, "Add application to home 2");
        innerInnerSettings[5] = new XMBItem(null, "Create new column 2");
        innerInnerSettings[6] = new XMBItem(null, "Add explorer item to home 2");
        innerInnerSettings[7] = new XMBItem(null, "Add application to home 2");
        innerInnerSettings[8] = new XMBItem(null, "Create new column 2");
        innerInnerSettings[9] = new XMBItem(null, "Add explorer item to home 2");
        innerInnerSettings[10] = new XMBItem(null, "Add application to home 2");
        innerInnerSettings[11] = new XMBItem(null, "Create new column 2");
        innerInnerSettings[12] = new XMBItem(null, "Create new column 2");
        innerInnerSettings[13] = new XMBItem(null, "Add explorer item to home 2");
        innerInnerSettings[14] = new XMBItem(null, "Add application to home 2");
        innerInnerSettings[15] = new XMBItem(null, "Create new column 2");
        innerInnerSettings[16] = new XMBItem(null, "Add explorer item to home 2");
        innerInnerInnerSettings = new XMBItem[18];
        innerInnerInnerSettings[0] = new XMBItem(null, "Add explorer item to home 3");
        innerInnerInnerSettings[1] = new XMBItem(null, "Add application to home 3");
        innerInnerInnerSettings[2] = new XMBItem(null, "Create new column 3");
        innerInnerInnerSettings[3] = new XMBItem(null, "Add explorer item to home 3");
        innerInnerInnerSettings[4] = new XMBItem(null, "Add application to home 3");
        innerInnerInnerSettings[5] = new XMBItem(null, "Create new column 3");
        innerInnerInnerSettings[6] = new XMBItem(null, "Add explorer item to home 3");
        innerInnerInnerSettings[7] = new XMBItem(null, "Add application to home 3");
        innerInnerInnerSettings[8] = new XMBItem(null, "Create new column 3");
        innerInnerInnerSettings[9] = new XMBItem(null, "Add explorer item to home 3");
        innerInnerInnerSettings[10] = new XMBItem(null, "Add application to home 3");
        innerInnerInnerSettings[11] = new XMBItem(null, "Create new column 3");
        innerInnerInnerSettings[12] = new XMBItem(null, "Create new column 3");
        innerInnerInnerSettings[13] = new XMBItem(null, "Add explorer item to home 3");
        innerInnerInnerSettings[14] = new XMBItem(null, "Add application to home 3");
        innerInnerInnerSettings[15] = new XMBItem(null, "Create new column 3");
        innerInnerInnerSettings[16] = new XMBItem(null, "Add explorer item to home 3");
        innerInnerInnerSettings[17] = new XMBItem(null, "Add application to home 3");
        innerInnerSettings[17] = new XMBItem(null, "Add application to home 2", innerInnerInnerSettings);
        innerSettings[0] = new XMBItem(null, "Add explorer item to home", innerInnerSettings);
        innerSettings[1] = new XMBItem(null, "Add application to home");
        innerSettings[2] = new XMBItem(null, "Create new column");
        innerSettings[3] = new XMBItem(null, "Add explorer item to home");
        innerSettings[4] = new XMBItem(null, "Add application to home");
        innerSettings[5] = new XMBItem(null, "Create new column");
        innerSettings[6] = new XMBItem(null, "Add explorer item to home");
        innerSettings[7] = new XMBItem(null, "Add application to home");
        innerSettings[8] = new XMBItem(null, "Create new column");
        innerSettings[9] = new XMBItem(null, "Add explorer item to home");
        innerSettings[10] = new XMBItem(null, "Add application to home");
        innerSettings[11] = new XMBItem(null, "Create new column");
        innerSettings[12] = new XMBItem(null, "Create new column");
        innerSettings[13] = new XMBItem(null, "Add explorer item to home");
        innerSettings[14] = new XMBItem(null, "Add application to home");
        innerSettings[15] = new XMBItem(null, "Create new column");
        innerSettings[16] = new XMBItem(null, "Add explorer item to home");
        innerSettings[17] = new XMBItem(null, "Add application to home");
        settingsItem = new XMBItem(null, "Home", R.drawable.ic_baseline_home_24, colIndex, localIndex++, innerSettings);
        settingsColumn.add(settingsItem);

        innerSettings = new XMBItem[18];
        innerSettings[0] = new XMBItem(null, "Add explorer item to home");
        innerInnerSettings = new XMBItem[18];
        innerInnerSettings[0] = new XMBItem(null, "Add explorer item to home 2");
        innerInnerSettings[1] = new XMBItem(null, "Add application to home 2");
        innerInnerSettings[2] = new XMBItem(null, "Create new column 2");
        innerInnerSettings[3] = new XMBItem(null, "Add explorer item to home 2");
        innerInnerSettings[4] = new XMBItem(null, "Add application to home 2");
        innerInnerSettings[5] = new XMBItem(null, "Create new column 2");
        innerInnerSettings[6] = new XMBItem(null, "Add explorer item to home 2");
        innerInnerSettings[7] = new XMBItem(null, "Add application to home 2");
        innerInnerSettings[8] = new XMBItem(null, "Create new column 2");
        innerInnerSettings[9] = new XMBItem(null, "Add explorer item to home 2");
        innerInnerSettings[10] = new XMBItem(null, "Add application to home 2");
        innerInnerSettings[11] = new XMBItem(null, "Create new column 2");
        innerInnerSettings[12] = new XMBItem(null, "Create new column 2");
        innerInnerSettings[13] = new XMBItem(null, "Add explorer item to home 2");
        innerInnerSettings[14] = new XMBItem(null, "Add application to home 2");
        innerInnerSettings[15] = new XMBItem(null, "Create new column 2");
        innerInnerInnerSettings = new XMBItem[18];
        innerInnerInnerSettings[0] = new XMBItem(null, "Add explorer item to home 3");
        innerInnerInnerSettings[1] = new XMBItem(null, "Add application to home 3");
        innerInnerInnerSettings[2] = new XMBItem(null, "Create new column 3");
        innerInnerInnerSettings[3] = new XMBItem(null, "Add explorer item to home 3");
        innerInnerInnerSettings[4] = new XMBItem(null, "Add application to home 3");
        innerInnerInnerSettings[5] = new XMBItem(null, "Create new column 3");
        innerInnerInnerSettings[6] = new XMBItem(null, "Add explorer item to home 3");
        innerInnerInnerSettings[7] = new XMBItem(null, "Add application to home 3");
        innerInnerInnerSettings[8] = new XMBItem(null, "Create new column 3");
        innerInnerInnerSettings[9] = new XMBItem(null, "Add explorer item to home 3");
        innerInnerInnerSettings[10] = new XMBItem(null, "Add application to home 3");
        innerInnerInnerSettings[11] = new XMBItem(null, "Create new column 3");
        innerInnerInnerSettings[12] = new XMBItem(null, "Create new column 3");
        innerInnerInnerSettings[13] = new XMBItem(null, "Add explorer item to home 3");
        innerInnerInnerSettings[14] = new XMBItem(null, "Add application to home 3");
        innerInnerInnerSettings[15] = new XMBItem(null, "Create new column 3");
        innerInnerInnerSettings[16] = new XMBItem(null, "Add explorer item to home 3");
        innerInnerInnerSettings[17] = new XMBItem(null, "Add application to home 3");
        innerInnerSettings[16] = new XMBItem(null, "Add explorer item to home 2", innerInnerInnerSettings);
        innerInnerSettings[17] = new XMBItem(null, "Add application to home 2");
        innerSettings[1] = new XMBItem(null, "Add application to home", innerInnerSettings);
        innerSettings[2] = new XMBItem(null, "Create new column");
        innerSettings[3] = new XMBItem(null, "Add explorer item to home");
        innerSettings[4] = new XMBItem(null, "Add application to home");
        innerSettings[5] = new XMBItem(null, "Create new column");
        innerSettings[6] = new XMBItem(null, "Add explorer item to home");
        innerSettings[7] = new XMBItem(null, "Add application to home");
        innerSettings[8] = new XMBItem(null, "Create new column");
        innerSettings[9] = new XMBItem(null, "Add explorer item to home");
        innerSettings[10] = new XMBItem(null, "Add application to home");
        innerSettings[11] = new XMBItem(null, "Create new column");
        innerSettings[12] = new XMBItem(null, "Create new column");
        innerSettings[13] = new XMBItem(null, "Add explorer item to home");
        innerSettings[14] = new XMBItem(null, "Add application to home");
        innerSettings[15] = new XMBItem(null, "Create new column");
        innerSettings[16] = new XMBItem(null, "Add explorer item to home");
        innerSettings[17] = new XMBItem(null, "Add application to home");
        settingsItem = new XMBItem(null, "Background", R.drawable.ic_baseline_image_24, colIndex, localIndex++, innerSettings);
        settingsColumn.add(settingsItem);

        innerSettings = new XMBItem[18];
        innerSettings[0] = new XMBItem(null, "Add explorer item to home");
        innerSettings[1] = new XMBItem(null, "Add application to home");
        innerInnerSettings = new XMBItem[18];
        innerInnerSettings[0] = new XMBItem(null, "Add explorer item to home 2");
        innerInnerSettings[1] = new XMBItem(null, "Add application to home 2");
        innerInnerSettings[2] = new XMBItem(null, "Create new column 2");
        innerInnerSettings[3] = new XMBItem(null, "Add explorer item to home 2");
        innerInnerSettings[4] = new XMBItem(null, "Add application to home 2");
        innerInnerSettings[5] = new XMBItem(null, "Create new column 2");
        innerInnerSettings[6] = new XMBItem(null, "Add explorer item to home 2");
        innerInnerSettings[7] = new XMBItem(null, "Add application to home 2");
        innerInnerSettings[8] = new XMBItem(null, "Create new column 2");
        innerInnerSettings[9] = new XMBItem(null, "Add explorer item to home 2");
        innerInnerSettings[10] = new XMBItem(null, "Add application to home 2");
        innerInnerSettings[11] = new XMBItem(null, "Create new column 2");
        innerInnerSettings[12] = new XMBItem(null, "Create new column 2");
        innerInnerSettings[13] = new XMBItem(null, "Add explorer item to home 2");
        innerInnerSettings[14] = new XMBItem(null, "Add application to home 2");
        innerInnerInnerSettings = new XMBItem[18];
        innerInnerInnerSettings[0] = new XMBItem(null, "Add explorer item to home 3");
        innerInnerInnerSettings[1] = new XMBItem(null, "Add application to home 3");
        innerInnerInnerSettings[2] = new XMBItem(null, "Create new column 3");
        innerInnerInnerSettings[3] = new XMBItem(null, "Add explorer item to home 3");
        innerInnerInnerSettings[4] = new XMBItem(null, "Add application to home 3");
        innerInnerInnerSettings[5] = new XMBItem(null, "Create new column 3");
        innerInnerInnerSettings[6] = new XMBItem(null, "Add explorer item to home 3");
        innerInnerInnerSettings[7] = new XMBItem(null, "Add application to home 3");
        innerInnerInnerSettings[8] = new XMBItem(null, "Create new column 3");
        innerInnerInnerSettings[9] = new XMBItem(null, "Add explorer item to home 3");
        innerInnerInnerSettings[10] = new XMBItem(null, "Add application to home 3");
        innerInnerInnerSettings[11] = new XMBItem(null, "Create new column 3");
        innerInnerInnerSettings[12] = new XMBItem(null, "Create new column 3");
        innerInnerInnerSettings[13] = new XMBItem(null, "Add explorer item to home 3");
        innerInnerInnerSettings[14] = new XMBItem(null, "Add application to home 3");
        innerInnerInnerSettings[15] = new XMBItem(null, "Create new column 3");
        innerInnerInnerSettings[16] = new XMBItem(null, "Add explorer item to home 3");
        innerInnerInnerSettings[17] = new XMBItem(null, "Add application to home 3");
        innerInnerSettings[15] = new XMBItem(null, "Create new column 2", innerInnerInnerSettings);
        innerInnerSettings[16] = new XMBItem(null, "Add explorer item to home 2");
        innerInnerSettings[17] = new XMBItem(null, "Add application to home 2");
        innerSettings[2] = new XMBItem(null, "Create new column", innerInnerSettings);
        innerSettings[3] = new XMBItem(null, "Add explorer item to home");
        innerSettings[4] = new XMBItem(null, "Add application to home");
        innerSettings[5] = new XMBItem(null, "Create new column");
        innerSettings[6] = new XMBItem(null, "Add explorer item to home");
        innerSettings[7] = new XMBItem(null, "Add application to home");
        innerSettings[8] = new XMBItem(null, "Create new column");
        innerSettings[9] = new XMBItem(null, "Add explorer item to home");
        innerSettings[10] = new XMBItem(null, "Add application to home");
        innerSettings[11] = new XMBItem(null, "Create new column");
        innerSettings[12] = new XMBItem(null, "Create new column");
        innerSettings[13] = new XMBItem(null, "Add explorer item to home");
        innerSettings[14] = new XMBItem(null, "Add application to home");
        innerSettings[15] = new XMBItem(null, "Create new column");
        innerSettings[16] = new XMBItem(null, "Add explorer item to home");
        innerSettings[17] = new XMBItem(null, "Add application to home");
        settingsItem = new XMBItem(null, "Explorer", R.drawable.ic_baseline_source_24, colIndex, localIndex++, innerSettings);
        settingsColumn.add(settingsItem);

        innerSettings = new XMBItem[18];
        innerSettings[0] = new XMBItem(null, "Add explorer item to home");
        innerSettings[1] = new XMBItem(null, "Add application to home");
        innerSettings[2] = new XMBItem(null, "Create new column");
        innerInnerSettings = new XMBItem[18];
        innerInnerSettings[0] = new XMBItem(null, "Add explorer item to home 2");
        innerInnerSettings[1] = new XMBItem(null, "Add application to home 2");
        innerInnerSettings[2] = new XMBItem(null, "Create new column 2");
        innerInnerSettings[3] = new XMBItem(null, "Add explorer item to home 2");
        innerInnerSettings[4] = new XMBItem(null, "Add application to home 2");
        innerInnerSettings[5] = new XMBItem(null, "Create new column 2");
        innerInnerSettings[6] = new XMBItem(null, "Add explorer item to home 2");
        innerInnerSettings[7] = new XMBItem(null, "Add application to home 2");
        innerInnerSettings[8] = new XMBItem(null, "Create new column 2");
        innerInnerSettings[9] = new XMBItem(null, "Add explorer item to home 2");
        innerInnerSettings[10] = new XMBItem(null, "Add application to home 2");
        innerInnerSettings[11] = new XMBItem(null, "Create new column 2");
        innerInnerSettings[12] = new XMBItem(null, "Create new column 2");
        innerInnerSettings[13] = new XMBItem(null, "Add explorer item to home 2");
        innerInnerInnerSettings = new XMBItem[18];
        innerInnerInnerSettings[0] = new XMBItem(null, "Add explorer item to home 3");
        innerInnerInnerSettings[1] = new XMBItem(null, "Add application to home 3");
        innerInnerInnerSettings[2] = new XMBItem(null, "Create new column 3");
        innerInnerInnerSettings[3] = new XMBItem(null, "Add explorer item to home 3");
        innerInnerInnerSettings[4] = new XMBItem(null, "Add application to home 3");
        innerInnerInnerSettings[5] = new XMBItem(null, "Create new column 3");
        innerInnerInnerSettings[6] = new XMBItem(null, "Add explorer item to home 3");
        innerInnerInnerSettings[7] = new XMBItem(null, "Add application to home 3");
        innerInnerInnerSettings[8] = new XMBItem(null, "Create new column 3");
        innerInnerInnerSettings[9] = new XMBItem(null, "Add explorer item to home 3");
        innerInnerInnerSettings[10] = new XMBItem(null, "Add application to home 3");
        innerInnerInnerSettings[11] = new XMBItem(null, "Create new column 3");
        innerInnerInnerSettings[12] = new XMBItem(null, "Create new column 3");
        innerInnerInnerSettings[13] = new XMBItem(null, "Add explorer item to home 3");
        innerInnerInnerSettings[14] = new XMBItem(null, "Add application to home 3");
        innerInnerInnerSettings[15] = new XMBItem(null, "Create new column 3");
        innerInnerInnerSettings[16] = new XMBItem(null, "Add explorer item to home 3");
        innerInnerInnerSettings[17] = new XMBItem(null, "Add application to home 3");
        innerInnerSettings[14] = new XMBItem(null, "Add application to home 2", innerInnerInnerSettings);
        innerInnerSettings[15] = new XMBItem(null, "Create new column 2");
        innerInnerSettings[16] = new XMBItem(null, "Add explorer item to home 2");
        innerInnerSettings[17] = new XMBItem(null, "Add application to home 2");
        innerSettings[3] = new XMBItem(null, "Add explorer item to home", innerInnerSettings);
        innerSettings[4] = new XMBItem(null, "Add application to home");
        innerSettings[5] = new XMBItem(null, "Create new column");
        innerSettings[6] = new XMBItem(null, "Add explorer item to home");
        innerSettings[7] = new XMBItem(null, "Add application to home");
        innerSettings[8] = new XMBItem(null, "Create new column");
        innerSettings[9] = new XMBItem(null, "Add explorer item to home");
        innerSettings[10] = new XMBItem(null, "Add application to home");
        innerSettings[11] = new XMBItem(null, "Create new column");
        innerSettings[12] = new XMBItem(null, "Create new column");
        innerSettings[13] = new XMBItem(null, "Add explorer item to home");
        innerSettings[14] = new XMBItem(null, "Add application to home");
        innerSettings[15] = new XMBItem(null, "Create new column");
        innerSettings[16] = new XMBItem(null, "Add explorer item to home");
        innerSettings[17] = new XMBItem(null, "Add application to home");
        settingsItem = new XMBItem(null, "Associations", R.drawable.ic_baseline_send_time_extension_24, colIndex, localIndex++, innerSettings);
        settingsColumn.add(settingsItem);

        allHomeItems.add(settingsColumn);
    }

    @Override
    protected void onAppliedMove(int fromColIndex, int fromLocalIndex, int toColIndex, int toLocalIndex) {
        boolean hasSubItems = catHasSubItems(toColIndex);
        XMBItem moveItem = allHomeItems.get(fromColIndex).get(fromLocalIndex);

        HomeManager.removeItemAt(fromColIndex, fromLocalIndex, false);
        if (hasSubItems)
            HomeManager.addItemTo(moveItem, toColIndex, toLocalIndex, false);
        else
            HomeManager.addItemAt(moveItem, toColIndex, false);
        refresh();
    }

//    @Override
//    protected boolean onShiftHorizontally(int colIndex, int prevColIndex) {
//        //Log.d("HomeView", "Shifted from " + prevColIndex + " to " + colIndex);
//        return super.onShiftHorizontally(colIndex, prevColIndex);
//    }
//
//    @Override
//    protected boolean onShiftVertically(int colIndex, int localIndex, int prevLocalIndex) {
//        //Log.d("HomeView", "Shifted from " + prevLocalIndex + " to " + localIndex + " on " + colIndex);
//        return super.onShiftVertically(colIndex, localIndex, prevLocalIndex);
//    }
}