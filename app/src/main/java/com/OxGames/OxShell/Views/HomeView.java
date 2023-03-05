package com.OxGames.OxShell.Views;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

import com.OxGames.OxShell.Adapters.XMBAdapter;
import com.OxGames.OxShell.Data.DynamicInputRow;
import com.OxGames.OxShell.Data.PackagesCache;
import com.OxGames.OxShell.Data.Paths;
import com.OxGames.OxShell.Data.XMBItem;
import com.OxGames.OxShell.Helpers.ActivityManager;
import com.OxGames.OxShell.Data.HomeItem;
import com.OxGames.OxShell.Data.IntentLaunchData;
import com.OxGames.OxShell.Helpers.AndroidHelpers;
import com.OxGames.OxShell.Helpers.MathHelpers;
import com.OxGames.OxShell.Helpers.Serialaver;
import com.OxGames.OxShell.Interfaces.Refreshable;
import com.OxGames.OxShell.PagedActivity;
import com.OxGames.OxShell.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class HomeView extends XMBView implements Refreshable {
    SettingsDrawer.ContextBtn moveBtn = new SettingsDrawer.ContextBtn("Move", () ->
    {
        toggleMoveMode(true);
        ActivityManager.getCurrentActivity().getSettingsDrawer().setShown(false);
        return null;
    });
    SettingsDrawer.ContextBtn deleteBtn = new SettingsDrawer.ContextBtn("Remove Item", () ->
    {
        deleteSelection();
        ActivityManager.getCurrentActivity().getSettingsDrawer().setShown(false);
        return null;
    });
    SettingsDrawer.ContextBtn deleteColumnBtn = new SettingsDrawer.ContextBtn("Remove Column", () ->
    {
        getAdapter().removeColumnAt(getPosition()[0]);
        save(getItems());
        ActivityManager.getCurrentActivity().getSettingsDrawer().setShown(false);
        return null;
    });
    SettingsDrawer.ContextBtn createColumnBtn = new SettingsDrawer.ContextBtn("Create Column", () ->
    {
        PagedActivity currentActivity = ActivityManager.getCurrentActivity();
        DynamicInputView dynamicInput = currentActivity.getDynamicInput();
        DynamicInputRow.TextInput titleInput = new DynamicInputRow.TextInput("Title");
        DynamicInputRow.ButtonInput okBtn = new DynamicInputRow.ButtonInput("Create", v -> {
            String title = titleInput.getText();
            getAdapter().createColumnAt(getPosition()[0], new XMBItem(null, title.length() > 0 ? title : "Unnamed"));
            save(getItems());
            dynamicInput.setShown(false);
        }, KeyEvent.KEYCODE_BUTTON_START, KeyEvent.KEYCODE_ENTER);
        DynamicInputRow.ButtonInput cancelBtn = new DynamicInputRow.ButtonInput("Cancel", v -> {
            dynamicInput.setShown(false);
        }, KeyEvent.KEYCODE_BUTTON_B, KeyEvent.KEYCODE_ESCAPE);
        dynamicInput.setItems(new DynamicInputRow(titleInput), new DynamicInputRow(okBtn, cancelBtn));

        currentActivity.getSettingsDrawer().setShown(false);
        dynamicInput.setShown(true);
        return null;
    });
    SettingsDrawer.ContextBtn cancelBtn = new SettingsDrawer.ContextBtn("Cancel", () ->
    {
        ActivityManager.getCurrentActivity().getSettingsDrawer().setShown(false);
        return null;
    });
    SettingsDrawer.ContextBtn uninstallBtn = new SettingsDrawer.ContextBtn("Uninstall", () ->
    {
        uninstallSelection();
        deleteSelection(); //TODO: only if uninstall was successful
        ActivityManager.getCurrentActivity().getSettingsDrawer().setShown(false);
        return null;
    });

    public HomeView(Context context) {
        super(context);
        refresh();
    }
    public HomeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        refresh();
    }
    public HomeView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
//        setLayoutParams(new GridView.LayoutParams(256, 256));
        refresh();
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
                    //ActivityManager.goTo(ActivityManager.Page.settings);
                    //return true;
//            HomeActivity.GetInstance().GoTo(HomeActivity.Page.addToHome);
                } else if (selectedItem.type == HomeItem.Type.assoc) {
                    IntentShortcutsView.setLaunchItem(selectedItem);
                    ActivityManager.goTo(ActivityManager.Page.intentShortcuts);
                    return true;
                } else if (selectedItem.type == HomeItem.Type.addApp) {
                    Adapter adapter = getAdapter();
                    adapter.createColumnAt(adapter.getColumnCount() - 1, new HomeItem(HomeItem.Type.app, selectedItem.obj, selectedItem.title));
                    save(getItems());
                    return true;
                } else if (selectedItem.type == HomeItem.Type.addExplorer) {
                    Adapter adapter = getAdapter();
                    adapter.createColumnAt(adapter.getColumnCount() - 1, new HomeItem(HomeItem.Type.explorer, "Explorer"));
                    save(getItems());
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
                Integer[] position = getPosition();
                boolean isNotSettings = position[0] < (getAdapter().getColumnCount() - 1);
                boolean hasColumnHead = getAdapter().isColumnHead(position[0], 0);
                XMBItem selectedItem = (XMBItem)getSelectedItem();
                HomeItem homeItem = null;
                // TODO: add move column option
                if (selectedItem instanceof HomeItem)
                    homeItem = (HomeItem)selectedItem;

                ArrayList<SettingsDrawer.ContextBtn> btns = new ArrayList<>();
                btns.add(createColumnBtn);
                if (homeItem != null) {
                    if (homeItem.type != HomeItem.Type.settings) {
                        btns.add(moveBtn);
                        if (homeItem.type != HomeItem.Type.explorer)
                            btns.add(uninstallBtn);
                        btns.add(deleteBtn);
                    }
                }
                if (isNotSettings && hasColumnHead)
                    btns.add(deleteColumnBtn);
                btns.add(cancelBtn);

                currentActivity.getSettingsDrawer().setButtons(btns.toArray(new SettingsDrawer.ContextBtn[0]));
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
        Integer[] position = getPosition();
        getAdapter().removeSubItem(position[0], position[1]);
        save(getItems());
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
//        Consumer<ArrayList<ArrayList<XMBItem>>> prosumer = items -> {
//
////            createSettingsColumn(settings -> {
////            });
//        };
        long loadHomeStart = SystemClock.uptimeMillis();

        ArrayList<ArrayList<XMBItem>> items;
        if (!cachedItemsExists()) {
            // if no file exists then add apps to the home
            // TODO: make optional?
            Log.d("HomeView", "Home items does not exist in data folder, creating...");
            items = createDefaultItems();
        }
        else {
            // if the file exists in the data folder then read it, if the read fails then create defaults
            Log.d("HomeView", "Home items exists in data folder, reading...");
            items = load();
            if (items == null)
                items = createDefaultItems();
        }
        save(items);
        items.add(createSettingsColumn());
        int cachedColIndex = colIndex;
        int cachedRowIndex = rowIndex;
        setAdapter(new XMBAdapter(getContext(), items));
        setIndex(cachedColIndex, cachedRowIndex, true);
        Log.i("HomeView", "Time to load home items: " + ((SystemClock.uptimeMillis() - loadHomeStart) / 1000f) + "s");
    }
    private static ArrayList<XMBItem> createSettingsColumn() {
        //XMBItem settings = new HomeItem(HomeItem.Type.settings, "Settings");
        ArrayList<XMBItem> settingsColumn = new ArrayList<>();
        XMBItem[] innerSettings;
        //XMBItem[] innerInnerSettings;
        //XMBItem[] innerInnerInnerSettings;
        //int colIndex = allHomeItems.size();
        //int localIndex = 0;

        XMBItem settingsItem = new XMBItem(null, "Settings", R.drawable.ic_baseline_settings_24);//, colIndex, localIndex++);
        settingsColumn.add(settingsItem);

        // TODO: add option to change icon alpha
        // TODO: add option to reset home items to default
        // TODO: add option to change home/explorer scale
        innerSettings = new XMBItem[2];
        innerSettings[0] = new HomeItem(HomeItem.Type.addExplorer, "Add explorer item to home");

        List<ResolveInfo> apps = PackagesCache.getInstalledPackages(Intent.ACTION_MAIN, Intent.CATEGORY_LAUNCHER);
        //long loadHomeStart = SystemClock.uptimeMillis();
        List<XMBItem> sortedApps = apps.stream().map(currentPkg -> new HomeItem(HomeItem.Type.addApp, currentPkg.activityInfo.packageName, PackagesCache.getAppLabel(currentPkg))).collect(Collectors.toList());
        //List<XMBItem> sortedApps = apps.stream().map(currentPkg -> new XMBItem(null, PackagesCache.getAppLabel(currentPkg), PackagesCache.getPackageIcon(currentPkg))).collect(Collectors.toList());
        //Log.d("HomeView", "Time to map apps: " + ((SystemClock.uptimeMillis() - loadHomeStart) / 1000f) + "s"); // mapping still runs slow on my S8 (removing getPackageIcon shaves off ~3s on my S8)
        sortedApps.sort(Comparator.comparing(o -> o.title.toLowerCase()));
        innerSettings[1] = new HomeItem(HomeItem.Type.settings, "Add application to home", sortedApps.toArray(new XMBItem[0]));
        //innerSettings[2] = new HomeItem(HomeItem.Type.settings, "Add new column to home");
        settingsItem = new XMBItem(null, "Home", R.drawable.ic_baseline_home_24, innerSettings);
        settingsColumn.add(settingsItem);

        innerSettings = new XMBItem[2];
        innerSettings[0] = new HomeItem(HomeItem.Type.settings, "Set picture as background");
        innerSettings[1] = new HomeItem(HomeItem.Type.settings, "Set shader as background");
        settingsItem = new XMBItem(null, "Background", R.drawable.ic_baseline_image_24, innerSettings);
        settingsColumn.add(settingsItem);

        //innerSettings = new XMBItem[0];
        //settingsItem = new XMBItem(null, "Explorer", R.drawable.ic_baseline_source_24, colIndex, localIndex++, innerSettings);
        //settingsColumn.add(settingsItem);

        innerSettings = new XMBItem[2];
        innerSettings[0] = new HomeItem(HomeItem.Type.settings, "Add association to home");
        innerSettings[1] = new HomeItem(HomeItem.Type.settings, "Create new association");
        settingsItem = new XMBItem(null, "Associations", R.drawable.ic_baseline_send_time_extension_24, innerSettings);
        settingsColumn.add(settingsItem);

        return settingsColumn;
    }

    @Override
    protected void onAppliedMove(int fromColIndex, int fromLocalIndex, int toColIndex, int toLocalIndex) {
        save(getItems());
    }

    public ArrayList<ArrayList<XMBItem>> getItems() {
        ArrayList<ArrayList<Object>> items = getAdapter().getItems();
        items.remove(items.size() - 1); // remove the settings
        return cast(items);
    }
    private static ArrayList<ArrayList<XMBItem>> cast(ArrayList<ArrayList<Object>> items) {
        ArrayList<ArrayList<XMBItem>> casted = new ArrayList<>();
        for (ArrayList<Object> column : items) {
            ArrayList<XMBItem> innerCasted = new ArrayList<>();
            for (Object item : column)
                innerCasted.add((XMBItem)item);
            casted.add(innerCasted);
        }
        return casted;
    }

    private static boolean cachedItemsExists() {
        return AndroidHelpers.fileExists(AndroidHelpers.combinePaths(Paths.HOME_ITEMS_DIR_INTERNAL, Paths.HOME_ITEMS_FILE_NAME));
    }
    private static void save(ArrayList<ArrayList<XMBItem>> items) {
        saveHomeItemsToFile(items, Paths.HOME_ITEMS_DIR_INTERNAL, Paths.HOME_ITEMS_FILE_NAME);
    }
    private static ArrayList<ArrayList<XMBItem>> load() {
        return loadHomeItemsFromFile(Paths.HOME_ITEMS_DIR_INTERNAL, Paths.HOME_ITEMS_FILE_NAME);
    }
    private static void saveHomeItemsToFile(ArrayList<ArrayList<XMBItem>> items, String parentDir, String fileName) {
        AndroidHelpers.makeDir(parentDir);
        String fullPath = AndroidHelpers.combinePaths(parentDir, fileName);
        //Serialaver.saveFile(items, fullPath);
        Serialaver.saveAsFSTJSON(items, fullPath);
    }
    private static ArrayList<ArrayList<XMBItem>> loadHomeItemsFromFile(String parentDir, String fileName) {
        ArrayList<ArrayList<XMBItem>> items = null;
        String path = AndroidHelpers.combinePaths(parentDir, fileName);
        if (AndroidHelpers.fileExists(path))
            //items = (ArrayList<ArrayList<XMBItem>>)Serialaver.loadFile(path);
            items = (ArrayList<ArrayList<XMBItem>>)Serialaver.loadFromFSTJSON(path);
        else
            Log.e("HomeView", "Attempted to read non-existant home items file @ " + path);
        return items;
    }
    private static ArrayList<ArrayList<XMBItem>> createDefaultItems() {
        Log.d("HomeView", "Retrieving default apps");
        long createDefaultStart = SystemClock.uptimeMillis();

        String[] categories = new String[] { "Games", "Audio", "Video", "Image", "Social", "News", "Maps", "Productivity", "Accessibility", "Other" };
        HashMap<Integer, ArrayList<XMBItem>> sortedApps = new HashMap<>();
        List<ResolveInfo> apps = PackagesCache.getInstalledPackages(Intent.ACTION_MAIN, Intent.CATEGORY_LAUNCHER);
        Log.d("HomeView", "Time to get installed packages: " + ((SystemClock.uptimeMillis() - createDefaultStart) / 1000f) + "s");
        createDefaultStart = SystemClock.uptimeMillis();
        ArrayList<ArrayList<XMBItem>> defaultItems = new ArrayList<>();
        // go through all apps creating HomeItems for them and sorting them into their categories
        int otherIndex = getOtherCategoryIndex();
        for (int i = 0; i < apps.size(); i++) {
            ResolveInfo currentPkg = apps.get(i);
            int category = -1;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
                category = currentPkg.activityInfo.applicationInfo.category;
            if (category < 0)
                category = otherIndex;
            if (!sortedApps.containsKey(category))
                sortedApps.put(category, new ArrayList<>());
            ArrayList<XMBItem> currentList = sortedApps.get(category);
            currentList.add(new HomeItem(HomeItem.Type.app, currentPkg.activityInfo.packageName, PackagesCache.getAppLabel(currentPkg)));
        }
        // separate the categories to avoid empty ones and order them into an arraylist so no game in indices occurs
        ArrayList<Integer> existingCategories = new ArrayList<>();
        for (Integer key : sortedApps.keySet())
            existingCategories.add(key);
        // add the categories and apps
        for (int index = 0; index < existingCategories.size(); index++) {
            int catIndex = existingCategories.get(index);
            if (catIndex == -1)
                catIndex = categories.length - 1;
            ArrayList<XMBItem> column = new ArrayList<>();
            // add the category item at the top
            column.add(new XMBItem(null, categories[catIndex], getDefaultIconForCategory(catIndex)));
            column.addAll(sortedApps.get(existingCategories.get(index)));
            defaultItems.add(column);
        }
        ArrayList<XMBItem> explorerColumn = new ArrayList<>();
        explorerColumn.add(new HomeItem(HomeItem.Type.explorer, "Explorer"));
        defaultItems.add(0, explorerColumn);
        Log.d("HomeView", "Time to sort packages: " + ((SystemClock.uptimeMillis() - createDefaultStart) / 1000f) + "s");
        return defaultItems;
    }
    public static int getDefaultIconForCategory(int category) {
        if (category == ApplicationInfo.CATEGORY_GAME)
            return R.drawable.ic_baseline_games_24;
        else if (category == ApplicationInfo.CATEGORY_AUDIO)
            return R.drawable.ic_baseline_headphones_24;
        else if (category == ApplicationInfo.CATEGORY_VIDEO)
            return R.drawable.ic_baseline_movie_24;
        else if (category == ApplicationInfo.CATEGORY_IMAGE)
            return R.drawable.ic_baseline_photo_camera_24;
        else if (category == ApplicationInfo.CATEGORY_SOCIAL)
            return R.drawable.ic_baseline_forum_24;
        else if (category == ApplicationInfo.CATEGORY_NEWS)
            return R.drawable.ic_baseline_newspaper_24;
        else if (category == ApplicationInfo.CATEGORY_MAPS)
            return R.drawable.ic_baseline_map_24;
        else if (category == ApplicationInfo.CATEGORY_PRODUCTIVITY)
            return R.drawable.ic_baseline_work_24;
        else if (category == ApplicationInfo.CATEGORY_ACCESSIBILITY)
            return R.drawable.ic_baseline_accessibility_24;
        else if (category == getOtherCategoryIndex())
            return R.drawable.ic_baseline_auto_awesome_24;
        else
            return R.drawable.ic_baseline_view_list_24;
    }
    private static int getOtherCategoryIndex() {
        return MathHelpers.max(ApplicationInfo.CATEGORY_GAME, ApplicationInfo.CATEGORY_AUDIO, ApplicationInfo.CATEGORY_IMAGE, ApplicationInfo.CATEGORY_SOCIAL, ApplicationInfo.CATEGORY_NEWS, ApplicationInfo.CATEGORY_MAPS, ApplicationInfo.CATEGORY_PRODUCTIVITY, ApplicationInfo.CATEGORY_ACCESSIBILITY) + 1;
    }
}