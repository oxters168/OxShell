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
    private boolean moveMode;
    private int origMoveColIndex;
    private int origMoveLocalIndex;
    private int moveColIndex;
    private int moveLocalIndex;

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
        // TODO: add way to select items
        PagedActivity currentActivity = ActivityManager.getCurrentActivity();
        if (!currentActivity.getSettingsDrawer().isDrawerOpen()) {
            if (key_event.getAction() == KeyEvent.ACTION_DOWN) {
                if (!moveMode) {
                    //if (key_event.getKeyCode() == KeyEvent.KEYCODE_BUTTON_X)
                    //    checkAlphas();
                    if (key_event.getKeyCode() == KeyEvent.KEYCODE_BUTTON_Y) {
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
                } else {
                    if (key_event.getKeyCode() == KeyEvent.KEYCODE_BUTTON_B) {
                        // cancel move
                        moveMode = false;
                        refresh();
                        return true;
                    }
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
        if (!moveMode) {
            if (getSelectedItem() instanceof HomeItem) {
                HomeItem selectedItem = (HomeItem) getSelectedItem();
                //Log.d("HomeView", currentIndex + " selected " + selectedItem.title + " @(" + selectedItem.colIndex + ", " + selectedItem.localIndex + ")");
                if (selectedItem.type == HomeItem.Type.explorer) {
                    ActivityManager.goTo(ActivityManager.Page.explorer);
//            HomeActivity.GetInstance().GoTo(HomeActivity.Page.explorer);
                } else if (selectedItem.type == HomeItem.Type.app) {
                    (IntentLaunchData.createFromPackage((String) selectedItem.obj, Intent.FLAG_ACTIVITY_NEW_TASK)).launch();
                } else if (selectedItem.type == HomeItem.Type.settings) {
                    ActivityManager.goTo(ActivityManager.Page.settings);
//            HomeActivity.GetInstance().GoTo(HomeActivity.Page.addToHome);
                } else if (selectedItem.type == HomeItem.Type.assoc) {
                    IntentShortcutsView.setLaunchItem(selectedItem);
                    ActivityManager.goTo(ActivityManager.Page.intentShortcuts);
                }
            }
        } else
            applyMove();
    }
    @Override
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
        if (!moveMode) {
            allHomeItems = HomeManager.getItems();
            // add settings item
            XMBItem settings = new HomeItem(HomeItem.Type.settings, "Settings");
            settings.colIndex = allHomeItems.size();
            settings.localIndex = 0;
            ArrayList<XMBItem> settingsColumn = new ArrayList<>();
            settingsColumn.add(settings);
            allHomeItems.add(settingsColumn);
        } else
            removeEmptyItems();

        fillEmptyColumns();

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

        //int cachedIndex = currentIndex;
        int colIndex;
        int localIndex;
        if (moveMode) {
            colIndex = moveColIndex;
            localIndex = moveLocalIndex;
        } else {
            colIndex = getColIndex();
            localIndex = getLocalIndex();
        }
        setAdapter(new XMBAdapter(getContext(), homeItems), mapper);
        setIndex(colIndex, localIndex, true);
        //setIndex(cachedIndex, true);
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

    private void toggleMoveMode(boolean onOff) {
        moveMode = onOff;
        moveColIndex = getColIndex();
        moveLocalIndex = getLocalIndex();
        origMoveColIndex = moveColIndex;
        origMoveLocalIndex = catHasSubItems(moveColIndex) ? moveLocalIndex + 1 : moveLocalIndex;
    }
    private void applyMove() {
        boolean hasSubItems = catHasSubItems(moveColIndex);
        int newLocalIndex = moveLocalIndex + (hasSubItems ? 1 : 0);
        //Log.d("HomeView", "Attempting to move (" + origMoveColIndex + ", " + origMoveLocalIndex + ") => (" + newColIndex + ", " + newLocalIndex + ")");
        if (moveColIndex != origMoveColIndex || newLocalIndex != origMoveLocalIndex) {
            XMBItem moveItem = allHomeItems.get(moveColIndex).get(newLocalIndex);
            if (hasSubItems) {
                HomeManager.removeItemAt(origMoveColIndex, origMoveLocalIndex, false);
                HomeManager.addItemTo(moveItem, moveColIndex, newLocalIndex, false);
            } else {
                HomeManager.removeItemAt(origMoveColIndex, origMoveLocalIndex, false);
                HomeManager.addItemAt(moveItem, moveColIndex, false);
            }
        }
        moveMode = false;
        refresh();
    }

    @Override
    protected boolean onShiftHorizontally(int colIndex, int prevColIndex) {
        //Log.d("HomeView", "Shifted from " + prevColIndex + " to " + colIndex);
        if (moveMode) {
            if (colIndex > prevColIndex) {
                // moving right
                boolean hasSubItems = catHasSubItems(moveColIndex);
                int nextColIndex = Math.min(moveColIndex + 1, allHomeItems.size() - (hasSubItems ? 1 : 2)); // -2 due to settings
                if (moveColIndex != nextColIndex) {
                    int currentLocalIndex = hasSubItems ? moveLocalIndex + 1 : moveLocalIndex;
                    ArrayList<XMBItem> column = allHomeItems.get(moveColIndex);
                    if (hasSubItems) {
                        // we're in a column with other things, next column should be a new one with just us
                        XMBItem moveItem = column.get(currentLocalIndex);
                        column.remove(currentLocalIndex);
                        ArrayList<XMBItem> newColumn = new ArrayList<>();
                        newColumn.add(moveItem);
                        allHomeItems.add(nextColIndex, newColumn);
                        moveColIndex = nextColIndex;
                        moveLocalIndex = 0;
                    } else {
                        // we're in a column made up solely of us, should remove ourselves first before moving
                        boolean nextHasSubItems = catHasSubItems(nextColIndex);
                        if (nextHasSubItems) {
                            // next column has sub-items, so place within
                            XMBItem moveItem = allHomeItems.get(moveColIndex).get(moveLocalIndex);
                            // TODO: figure out way to keep column histories while moving items (very low priority)
                            int nextLocalIndex = getColLocalIndex(nextColIndex) + 1; // +1 since it has sub-items
                            allHomeItems.get(nextColIndex).add(nextLocalIndex, moveItem);
                            allHomeItems.remove(moveColIndex);
                            // don't set moveColIndex since we removed a column
                            moveLocalIndex = nextLocalIndex - 1; // -1 since XMBView uses traversable local index and not total
                        } else {
                            // next column does not have sub-items, so skip over
                            ArrayList<XMBItem> moveColumn = allHomeItems.get(moveColIndex);
                            allHomeItems.remove(moveColIndex);
                            allHomeItems.add(nextColIndex, moveColumn);
                            moveColIndex = nextColIndex;
                            moveLocalIndex = 0;
                        }
                    }
                    refresh();
                }
            } else {
                // moving left
                boolean hasSubItems = catHasSubItems(moveColIndex);
                int nextColIndex = Math.max(moveColIndex - 1, hasSubItems ? -1 : 0); // -1 to move out of the 0th column
                if (moveColIndex != nextColIndex) {
                    int currentLocalIndex = hasSubItems ? moveLocalIndex + 1 : moveLocalIndex;
                    ArrayList<XMBItem> column = allHomeItems.get(moveColIndex);
                    if (hasSubItems) {
                        // we're in a column with other things, next column should be a new one with just us
                        XMBItem moveItem = column.get(currentLocalIndex);
                        column.remove(currentLocalIndex);
                        ArrayList<XMBItem> newColumn = new ArrayList<>();
                        newColumn.add(moveItem);
                        allHomeItems.add(moveColIndex, newColumn);
                        //moveColIndex = nextColIndex;
                        moveLocalIndex = 0;
                    } else {
                        // we're in a column made up solely of us, should remove ourselves first before moving
                        boolean nextHasSubItems = catHasSubItems(nextColIndex);
                        if (nextHasSubItems) {
                            // next column has sub-items, so place within
                            XMBItem moveItem = allHomeItems.get(moveColIndex).get(moveLocalIndex);
                            // TODO: figure out way to keep column histories while moving items (very low priority)
                            int nextLocalIndex = getColLocalIndex(nextColIndex) + 1; // +1 since it has sub-items
                            allHomeItems.get(nextColIndex).add(nextLocalIndex, moveItem);
                            allHomeItems.remove(moveColIndex);
                            moveColIndex = nextColIndex;
                            moveLocalIndex = nextLocalIndex - 1; // -1 since XMBView uses traversable local index and not total
                        } else {
                            // next column does not have sub-items, so skip over
                            ArrayList<XMBItem> moveColumn = allHomeItems.get(moveColIndex);
                            allHomeItems.remove(moveColIndex);
                            allHomeItems.add(nextColIndex, moveColumn);
                            moveColIndex = nextColIndex;
                            moveLocalIndex = 0;
                        }
                    }
                    refresh();
                }
            }
            return true;
        }
        return false;
    }

    @Override
    protected boolean onShiftVertically(int colIndex, int localIndex, int prevLocalIndex) {
        //Log.d("HomeView", "Shifted from " + prevLocalIndex + " to " + localIndex + " on " + colIndex);
        if (moveMode) {
            if (localIndex > prevLocalIndex) {
                // going down
                int currentLocalIndex = catHasSubItems(moveColIndex) ? moveLocalIndex + 1 : moveLocalIndex;
                ArrayList<XMBItem> column = allHomeItems.get(moveColIndex);
                int nextLocalIndex = Math.min(currentLocalIndex + 1, column.size() - 1);
                if (currentLocalIndex != nextLocalIndex) {
                    XMBItem moveItem = column.get(currentLocalIndex);
                    column.remove(currentLocalIndex);
                    column.add(nextLocalIndex, moveItem);
                    moveLocalIndex += 1;
                    refresh();
                }
            } else {
                // going up
                boolean hasSubItems = catHasSubItems(moveColIndex);
                int currentLocalIndex = hasSubItems ? moveLocalIndex + 1 : moveLocalIndex;
                ArrayList<XMBItem> column = allHomeItems.get(moveColIndex);
                int nextLocalIndex = Math.max(currentLocalIndex - 1, hasSubItems ? 1 : 0);
                if (currentLocalIndex != nextLocalIndex) {
                    XMBItem moveItem = column.get(currentLocalIndex);
                    column.remove(currentLocalIndex);
                    column.add(nextLocalIndex, moveItem);
                    moveLocalIndex -= 1;
                    refresh();
                }
            }
            return true;
        }
        return false;
    }

//    @Override
//    public void selectLowerItem() {
//        if (!moveMode)
//            super.selectLowerItem();
//    }
//    @Override
//    public void selectUpperItem() {
//        if (!moveMode)
//            super.selectUpperItem();
//    }
//    @Override
//    public void selectRightItem() {
//        if (!moveMode)
//            super.selectRightItem();
//    }
//    @Override
//    public void selectLeftItem() {
//        if (!moveMode)
//            super.selectLeftItem();
//    }

//    private void sortItems(ArrayList<XMBItem> unsortedItems, ArrayList<XMBItem> columns, ArrayList<XMBItem> subItems) {
//        // reorder items based on their indices so there is no issue when adding them all at once
//        //ArrayList<XMBItem> columns = new ArrayList<>();
//        ArrayList<XMBItem> unsortedCols = new ArrayList<>();
//        HashMap<Integer, ArrayList<XMBItem>> sortedSubItems = new HashMap<>();
//        HashMap<Integer, ArrayList<XMBItem>> unsortedSubItems = new HashMap<>();
//        for (int i = 0; i < unsortedItems.size(); i++) {
//            XMBItem currentItem = unsortedItems.get(i);
//            //Log.d("HomeView", "Sorting " + currentItem.title + " col: " + currentItem.colIndex + " loc: " + currentItem.localIndex);
//            if (currentItem.colIndex >= 0) {
//                if (currentItem.localIndex == 0) {
//                    // if the current item is at the top of the sub items then it is a category item and should be added to the columns list
//                    if (currentItem.colIndex > columns.size())
//                        // if the current item is not within the current range of the columns list then insert into the end of the list
//                        columns.add(currentItem);
//                    else
//                        // if the col index is within the current range of the columns list then insert into the proper position
//                        columns.add(currentItem.colIndex, currentItem);
//                } else if (currentItem.localIndex > 0) {
//                    // if this item has a local index then start figuring out how to add it to sortedSubItems
//                    if (!sortedSubItems.containsKey(currentItem.colIndex))
//                        sortedSubItems.put(currentItem.colIndex, new ArrayList<>());
//                    ArrayList<XMBItem> colSubItems = sortedSubItems.get(currentItem.colIndex);
//
//                    if (currentItem.localIndex > colSubItems.size()) {
//                        // if the local index is not within the correct range then place the item before another item that has a larger local index, which if it does not exist then add the item at the end
//                        boolean inserted = false;
//                        for (int j = colSubItems.size() - 1; j >= 0; j--) {
//                            if (currentItem.localIndex < colSubItems.get(j).localIndex) {
//                                colSubItems.add(j, currentItem);
//                                inserted = true;
//                                break;
//                            }
//                        }
//                        if (!inserted)
//                            colSubItems.add(currentItem);
//                    }
//                    else
//                        // if the local index given is within the correct range then insert the item there
//                        colSubItems.add(currentItem.localIndex, currentItem);
//                } else {
//                    // this item does not have a local index so add it to the unsortedSubItems
//                    if (!unsortedSubItems.containsKey(currentItem.colIndex))
//                        unsortedSubItems.put(currentItem.colIndex, new ArrayList<>());
//                    ArrayList<XMBItem> colSubItems = unsortedSubItems.get(currentItem.colIndex);
//
//                    colSubItems.add(currentItem);
//                }
//            } else {
//                //Log.d("HomeView", "Adding " + currentItem.title + " to unsorted cols");
//                // if the col index has not been set then just add item as column in a separate list to be combined at the end of the main list later
//                unsortedCols.add(currentItem);
//            }
//        }
//        columns.addAll(unsortedCols);
//
//        //ArrayList<XMBItem> subItems = new ArrayList<>();
//        // get all column indices that exist
//        HashSet<Integer> keys = new HashSet<>(sortedSubItems.keySet());
//        keys.addAll(unsortedSubItems.keySet());
//        for (Integer key : keys) {
//            // add sub items with local indices first to the aggregated list
//            if (sortedSubItems.containsKey(key))
//                subItems.addAll(sortedSubItems.get(key));
//            // then add the sub items without local indices of the same col index to the aggregated list
//            if (unsortedSubItems.containsKey(key))
//                subItems.addAll(unsortedSubItems.get(key));
//        }
//        //Log.d("HomeView", "Total sub items is " + subItems.size());
//    }

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