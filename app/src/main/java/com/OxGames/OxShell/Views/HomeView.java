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
import java.util.HashMap;
import java.util.HashSet;

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

        // reorder items based on their indices so there is no issue when adding them all at once
        ArrayList<XMBItem> columns = new ArrayList<>();
        ArrayList<XMBItem> unsortedCols = new ArrayList<>();
        HashMap<Integer, ArrayList<XMBItem>> sortedSubItems = new HashMap<>();
        HashMap<Integer, ArrayList<XMBItem>> unsortedSubItems = new HashMap<>();
        for (int i = 0; i < homeItems.size(); i++) {
            XMBItem currentItem = homeItems.get(i);
            Log.d("HomeView", "Sorting " + currentItem.title + " col: " + currentItem.colIndex + " loc: " + currentItem.localIndex);
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
                        sortedSubItems.put(currentItem.localIndex, new ArrayList<>());
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
                        unsortedSubItems.put(currentItem.localIndex, new ArrayList<>());
                    ArrayList<XMBItem> colSubItems = unsortedSubItems.get(currentItem.colIndex);

                    colSubItems.add(currentItem);
                }
            } else {
                Log.d("HomeView", "Adding " + currentItem.title + " to unsorted cols");
                // if the col index has not been set then just add item as column in a separate list to be combined at the end of the main list later
                unsortedCols.add(currentItem);
            }
        }
        columns.addAll(unsortedCols);
        //homeItems.add(new HomeItem(HomeItem.Type.settings));
        columns.add(new HomeItem(HomeItem.Type.settings));

        ArrayList<XMBItem> subItems = new ArrayList<>();
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
        Log.d("HomeView", "Total sub items is " + subItems.size());

//        XMBCat mainCat = new XMBCat("Apps");
//        for (XMBItem homeItem : homeItems)
//            if (((HomeItem)homeItem).type == HomeItem.Type.app)
//                homeItem.category = mainCat;

        int cachedIndex = currentIndex;
        clear();
        addCatItems(columns);
        if (subItems.size() > 0)
            addSubItems(subItems);
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