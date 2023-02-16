package com.OxGames.OxShell.Views;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;

import com.OxGames.OxShell.Adapters.XMBAdapter;
import com.OxGames.OxShell.Data.XMBItem;
import com.OxGames.OxShell.Helpers.ActivityManager;
import com.OxGames.OxShell.Data.HomeItem;
import com.OxGames.OxShell.Data.HomeManager;
import com.OxGames.OxShell.Data.IntentLaunchData;
import com.OxGames.OxShell.PagedActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

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
    public boolean receiveKeyEvent(KeyEvent key_event) {
        PagedActivity currentActivity = ActivityManager.getCurrentActivity();
        if (!currentActivity.getSettingsDrawer().isDrawerOpen()) {
            if (key_event.getAction() == KeyEvent.ACTION_DOWN) {
                if (key_event.getKeyCode() == KeyEvent.KEYCODE_BUTTON_R2) {
                    //ActivityManager.GoTo(ActivityManager.Page.runningApps);
                    return true;
                }
                if (key_event.getKeyCode() == KeyEvent.KEYCODE_BUTTON_Y) {
                    SettingsDrawer.ContextBtn moveBtn = new SettingsDrawer.ContextBtn("Move", () ->
                    {
                        Log.d("HomeView", "Move selected");
                        //TODO: make move
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
                    HomeItem selectedItem = (HomeItem)getSelectedItem();
                    if (selectedItem.type != HomeItem.Type.explorer && selectedItem.type != HomeItem.Type.settings) {
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
            return super.receiveKeyEvent(key_event);
        } else {
            if (key_event.getAction() == KeyEvent.ACTION_DOWN) {
                if ((key_event.getKeyCode() == KeyEvent.KEYCODE_BUTTON_B || key_event.getKeyCode() == KeyEvent.KEYCODE_BACK)) {
                    currentActivity.getSettingsDrawer().setShown(false);
                    return true;
                }
            }
            return false;
        }
    }
    @Override
    public void makeSelection() {
        HomeItem selectedItem = (HomeItem)getSelectedItem();
        //Log.d("HomeView", currentIndex + " selected " + selectedItem.title + " @(" + selectedItem.colIndex + ", " + selectedItem.localIndex + ")");
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
        HomeItem selectedItem = (HomeItem)getSelectedItem();
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
        ArrayList<XMBItem> homeItems = HomeManager.getItems();
        if (homeItems == null)
            homeItems = new ArrayList<>();

        // using a column count index rather than getting the columns.size() in case if for whatever reason there are missing columns in between
        int columnCount = 0;
        HashMap<Integer, List<Integer>> columns = new HashMap<>();
        for (int index = 0; index < homeItems.size(); index++) {
            XMBItem item = homeItems.get(index);
            columnCount = Math.max(columnCount, item.colIndex + 1);
            //int currentColSize = 0;
            if (!columns.containsKey(item.colIndex))
                columns.put(item.colIndex, new ArrayList<>());
                //currentColSize = columnSizes.get(item.colIndex);
            //columnSizes.put(item.colIndex, Math.max(currentColSize, item.localIndex + 1));
            List<Integer> column = columns.get(item.colIndex);
            boolean added = false;
            //String output = "Placing " + item.localIndex + " in column " + item.colIndex;
            for (int i = 0; i < column.size(); i++) {
                if (item.localIndex >= 0 && item.localIndex < homeItems.get(column.get(i)).localIndex) {
                    //output += " at " + i;
                    column.add(i, index);
                    added = true;
                    break;
                }
            }
            if (!added) {
                //output += " at end";
                if (item.localIndex < 0)
                    item.localIndex = column.size();
                column.add(index);
            }
            Log.d("HomeView", output);
        }
//        ArrayList<XMBItem> columns = new ArrayList<>();
//        ArrayList<XMBItem> subItems = new ArrayList<>();
//        sortItems(homeItems, columns, subItems);
        XMBItem settings = new HomeItem(HomeItem.Type.settings);
        settings.colIndex = ++columnCount;
        List<Integer> settingsList = new ArrayList<>();
        settingsList.add(homeItems.size());
        homeItems.add(settings);
        columns.put(settings.colIndex, settingsList);
        //homeItems.add(new HomeItem(HomeItem.Type.settings));
        //columns.add(0, new HomeItem(HomeItem.Type.settings));
        int[][] mapper = new int[columns.size()][];
        int mapI = 0;
        for (Integer key : columns.keySet()) {
            if (columns.containsKey(key))
                mapper[mapI++] = columns.get(key).stream().mapToInt(value -> value).toArray();
            else
                Log.e("HomeView", "Missing column " + key);
        }
        for (int i = 0; i < mapper.length; i++) {
            String output = "";
            for (int j = 0; j < mapper[i].length; j++) {
                output += mapper[i][j] + ", ";
            }
            Log.d("HomeView", output);
        }

        int cachedIndex = currentIndex;
//        clear();
//        addCatItems(columns);
//        if (subItems.size() > 0)
//            addSubItems(subItems);
        setAdapter(new XMBAdapter(getContext(), homeItems), mapper);
        //addItems(homeItems);
        setIndex(cachedIndex);

        super.refresh();
    }

    private void sortItems(ArrayList<XMBItem> unsortedItems, ArrayList<XMBItem> columns, ArrayList<XMBItem> subItems) {
        // reorder items based on their indices so there is no issue when adding them all at once
        //ArrayList<XMBItem> columns = new ArrayList<>();
        ArrayList<XMBItem> unsortedCols = new ArrayList<>();
        HashMap<Integer, ArrayList<XMBItem>> sortedSubItems = new HashMap<>();
        HashMap<Integer, ArrayList<XMBItem>> unsortedSubItems = new HashMap<>();
        for (int i = 0; i < unsortedItems.size(); i++) {
            XMBItem currentItem = unsortedItems.get(i);
            //Log.d("HomeView", "Sorting " + currentItem.title + " col: " + currentItem.colIndex + " loc: " + currentItem.localIndex);
            if (currentItem.colIndex >= 0) {
                if (currentItem.localIndex == 0) {
                    // if the current item is at the top of the sub items then it is a category item and should be added to the columns list
                    if (currentItem.colIndex > columns.size())
                        // if the current item is not within the current range of the columns list then insert into the end of the list
                        columns.add(currentItem);
                    else
                        // if the col index is within the current range of the columns list then insert into the proper position
                        columns.add(currentItem.colIndex, currentItem);
                } else if (currentItem.localIndex > 0) {
                    // if this item has a local index then start figuring out how to add it to sortedSubItems
                    if (!sortedSubItems.containsKey(currentItem.colIndex))
                        sortedSubItems.put(currentItem.colIndex, new ArrayList<>());
                    ArrayList<XMBItem> colSubItems = sortedSubItems.get(currentItem.colIndex);

                    if (currentItem.localIndex > colSubItems.size()) {
                        // if the local index is not within the correct range then place the item before another item that has a larger local index, which if it does not exist then add the item at the end
                        boolean inserted = false;
                        for (int j = colSubItems.size() - 1; j >= 0; j--) {
                            if (currentItem.localIndex < colSubItems.get(j).localIndex) {
                                colSubItems.add(j, currentItem);
                                inserted = true;
                                break;
                            }
                        }
                        if (!inserted)
                            colSubItems.add(currentItem);
                    }
                    else
                        // if the local index given is within the correct range then insert the item there
                        colSubItems.add(currentItem.localIndex, currentItem);
                } else {
                    // this item does not have a local index so add it to the unsortedSubItems
                    if (!unsortedSubItems.containsKey(currentItem.colIndex))
                        unsortedSubItems.put(currentItem.colIndex, new ArrayList<>());
                    ArrayList<XMBItem> colSubItems = unsortedSubItems.get(currentItem.colIndex);

                    colSubItems.add(currentItem);
                }
            } else {
                //Log.d("HomeView", "Adding " + currentItem.title + " to unsorted cols");
                // if the col index has not been set then just add item as column in a separate list to be combined at the end of the main list later
                unsortedCols.add(currentItem);
            }
        }
        columns.addAll(unsortedCols);

        //ArrayList<XMBItem> subItems = new ArrayList<>();
        // get all column indices that exist
        HashSet<Integer> keys = new HashSet<>(sortedSubItems.keySet());
        keys.addAll(unsortedSubItems.keySet());
        for (Integer key : keys) {
            // add sub items with local indices first to the aggregated list
            if (sortedSubItems.containsKey(key))
                subItems.addAll(sortedSubItems.get(key));
            // then add the sub items without local indices of the same col index to the aggregated list
            if (unsortedSubItems.containsKey(key))
                subItems.addAll(unsortedSubItems.get(key));
        }
        //Log.d("HomeView", "Total sub items is " + subItems.size());
    }

//    private void showCustomContextMenu(int x, int y) {
//        overlay = new ContextMenu(getContext());
//        overlay.setCancelable(true);
//        overlay.addButton("Move", () -> {
//            Log.d("DialogItemSelection", "Move");
//            return null;
//        });
//        overlay.addButton("Remove", () -> {
//            deleteSelection();
//            overlay.dismiss();
//            return null;
//        });
//        HomeItem selectedItem = (HomeItem) getSelectedItem();
//        if (selectedItem.type != HomeItem.Type.explorer && selectedItem.type != HomeItem.Type.settings)
//            overlay.addButton("Uninstall", () -> {
//                uninstallSelection();
//                deleteSelection(); //only if uninstall was successful
//                overlay.dismiss();
//                return null;
//            });
//        overlay.addButton("Cancel", () -> {
//            overlay.dismiss();
//            return null;
//        });
//        WindowManager.LayoutParams params = overlay.getWindow().getAttributes();
//        params.x = x;
//        params.y = y;
//        overlay.getWindow().setAttributes(params);
//        overlay.show();
//    }
}