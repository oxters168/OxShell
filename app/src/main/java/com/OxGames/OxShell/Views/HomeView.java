package com.OxGames.OxShell.Views;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

import com.OxGames.OxShell.Adapters.XMBAdapter;
import com.OxGames.OxShell.Data.PackagesCache;
import com.OxGames.OxShell.Data.Paths;
import com.OxGames.OxShell.Data.XMBItem;
import com.OxGames.OxShell.Helpers.ActivityManager;
import com.OxGames.OxShell.Data.HomeItem;
import com.OxGames.OxShell.Data.HomeManager;
import com.OxGames.OxShell.Data.IntentLaunchData;
import com.OxGames.OxShell.Helpers.AndroidHelpers;
import com.OxGames.OxShell.Helpers.MathHelpers;
import com.OxGames.OxShell.Helpers.Serialaver;
import com.OxGames.OxShell.Interfaces.Refreshable;
import com.OxGames.OxShell.PagedActivity;
import com.OxGames.OxShell.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

public class HomeView extends XMBView implements Refreshable {
    //private ArrayList<ArrayList<XMBItem>> allHomeItems;
//    private boolean moveMode;
//    private int origMoveColIndex;
//    private int origMoveLocalIndex;
//    private int moveColIndex;
//    private int moveLocalIndex;

//    @ColorInt
//    private int normalHighlightColor = Color.parseColor("#FF808080");
//    @ColorInt
//    private int moveHighlightColor = Color.parseColor("#FF808000");
    SettingsDrawer.ContextBtn moveBtn = new SettingsDrawer.ContextBtn("Move", () ->
    {
        toggleMoveMode(true);
        ActivityManager.getCurrentActivity().getSettingsDrawer().setShown(false);
        return null;
    });
    SettingsDrawer.ContextBtn deleteBtn = new SettingsDrawer.ContextBtn("Remove", () ->
    {
        deleteSelection();
        ActivityManager.getCurrentActivity().getSettingsDrawer().setShown(false);
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
                XMBItem selectedItem = (XMBItem)getSelectedItem();
                HomeItem homeItem = null;
                // TODO: remove delete option for settings and empty
                // TODO: remove move option for settings and empty
                // TODO: add move column option
                // TODO: add delete column option
                // TODO: add new column option
                if (selectedItem instanceof HomeItem)
                    homeItem = (HomeItem)selectedItem;

                ArrayList<SettingsDrawer.ContextBtn> btns = new ArrayList<>();
                if (homeItem != null) {
                    if (homeItem.type != HomeItem.Type.settings) {
                        btns.add(moveBtn);
                        btns.add(deleteBtn);
                        if (homeItem.type != HomeItem.Type.explorer)
                            btns.add(uninstallBtn);
                    }
                }
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
        Consumer<ArrayList<ArrayList<XMBItem>>> prosumer = items -> {
            items.add(createSettingsColumn());
            int cachedColIndex = colIndex;
            int cachedRowIndex = rowIndex;
            setAdapter(new XMBAdapter(getContext(), items));
            setIndex(cachedColIndex, cachedRowIndex, true);
        };

        if (!cachedItemsExists()) {
            // if no file exists then add apps to the home
            // TODO: make optional?
            Log.d("HomeManager", "Home items does not exist in data folder, creating...");
            createDefaultItems(prosumer);
        }
        else {
            // if the file exists in the data folder then read it, if the read fails then create defaults
            Log.d("HomeManager", "Home items exists in data folder, reading...");
            ArrayList<ArrayList<XMBItem>> items = load();
            if (items == null)
                createDefaultItems(prosumer);
            else
                prosumer.accept(items);
        }
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
        innerSettings = new XMBItem[3];
        innerSettings[0] = new HomeItem(HomeItem.Type.settings, "Add explorer item to home");
        List<ResolveInfo> apps = PackagesCache.getInstalledPackages(Intent.ACTION_MAIN, Intent.CATEGORY_LAUNCHER);
        List<XMBItem> sortedApps = new ArrayList<>();
        for (int i = 0; i < apps.size(); i++) {
            ResolveInfo currentPkg = apps.get(i);
            XMBItem newItem = new XMBItem(null, PackagesCache.getAppLabel(currentPkg), PackagesCache.getPackageIcon(currentPkg));
            if (sortedApps.size() > 0)
                for (int j = sortedApps.size() - 1; j >= 0; j--) {
                    if (newItem.title.compareToIgnoreCase(sortedApps.get(j).title) > 0) {
                        sortedApps.add(j + 1, newItem);
                        break;
                    }
                    if (j == 0)
                        sortedApps.add(j, newItem);
                }
            else
                sortedApps.add(newItem);
        }
        innerSettings[1] = new HomeItem(HomeItem.Type.settings, "Add application to home", sortedApps.toArray(new XMBItem[0]));
        innerSettings[2] = new HomeItem(HomeItem.Type.settings, "Add new column to home");
        settingsItem = new XMBItem(null, "Home", R.drawable.ic_baseline_home_24, innerSettings);//, colIndex, localIndex++, innerSettings);
        settingsColumn.add(settingsItem);

        innerSettings = new XMBItem[2];
        innerSettings[0] = new HomeItem(HomeItem.Type.settings, "Set picture as background");
        innerSettings[1] = new HomeItem(HomeItem.Type.settings, "Set shader as background");
        settingsItem = new XMBItem(null, "Background", R.drawable.ic_baseline_image_24, innerSettings);//, colIndex, localIndex++, innerSettings);
        settingsColumn.add(settingsItem);

        //innerSettings = new XMBItem[0];
        //settingsItem = new XMBItem(null, "Explorer", R.drawable.ic_baseline_source_24, colIndex, localIndex++, innerSettings);
        //settingsColumn.add(settingsItem);

        innerSettings = new XMBItem[2];
        innerSettings[0] = new HomeItem(HomeItem.Type.settings, "Add association to home");
        innerSettings[1] = new HomeItem(HomeItem.Type.settings, "Create new association");
        settingsItem = new XMBItem(null, "Associations", R.drawable.ic_baseline_send_time_extension_24, innerSettings);//, colIndex, localIndex++, innerSettings);
        settingsColumn.add(settingsItem);

        //allHomeItems.add(settingsColumn);
        return settingsColumn;
    }

    @Override
    protected void onAppliedMove(int fromColIndex, int fromLocalIndex, int toColIndex, int toLocalIndex) {
        ArrayList<ArrayList<Object>> items = getAdapter().getItems();
        items.remove(items.size() - 1); // remove the settings
        ArrayList<ArrayList<XMBItem>> casted = new ArrayList<>();
        for (ArrayList<Object> column : items) {
            ArrayList<XMBItem> innerCasted = new ArrayList<>();
            for (Object item : column)
                innerCasted.add((XMBItem)item);
            casted.add(innerCasted);
        }
        HomeManager.setHomeItems(casted);
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
        Serialaver.saveFile(items, fullPath);
    }
    private static ArrayList<ArrayList<XMBItem>> loadHomeItemsFromFile(String parentDir, String fileName) {
        ArrayList<ArrayList<XMBItem>> items = null;
        String path = AndroidHelpers.combinePaths(parentDir, fileName);
        if (AndroidHelpers.fileExists(path))
            items = (ArrayList<ArrayList<XMBItem>>)Serialaver.loadFile(path);
        else
            Log.e("HomeManager", "Attempted to read non-existant home items file @ " + path);
        return items;
    }
    private static void createDefaultItems(Consumer<ArrayList<ArrayList<XMBItem>>> onComplete) {
        Log.d("HomeManager", "Retrieving default apps");
        //addExplorer();

        String[] categories = new String[] { "Games", "Audio", "Video", "Image", "Social", "News", "Maps", "Productivity", "Accessibility", "Other" };
        HashMap<Integer, ArrayList<XMBItem>> sortedApps = new HashMap<>();
        PagedActivity currentActivity = ActivityManager.getCurrentActivity();
        PackagesCache.requestInstalledPackages(Intent.ACTION_MAIN, apps -> currentActivity.runOnUiThread(() -> {
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
                currentList.add(new HomeItem(HomeItem.Type.app, PackagesCache.getAppLabel(currentPkg), currentPkg.activityInfo.packageName));
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
                // add the category item at the top
                //int colIndex = createCategory(getDefaultIconForCategory(catIndex), categories[catIndex], false);
                ArrayList<XMBItem> column = new ArrayList<>();
                column.add(new XMBItem(null, categories[catIndex], getDefaultIconForCategory(catIndex)));
                //addItem(new XMBItem(null, categories[catIndex], index + 2, 0), false);
                // add the apps into the category as items
                //ArrayList<XMBItem> column = sortedApps.get(existingCategories.get(index));
                for (XMBItem app : sortedApps.get(existingCategories.get(index))) {
                    //XMBItem app = column.get(i);
                    //app.colIndex = index + 2;
                    //app.localIndex = i + 1;
                    //addItemTo(app, colIndex, false);
                    column.add(app);
                }
                defaultItems.add(column);
            }
            ArrayList<XMBItem> explorerColumn = new ArrayList<>();
            explorerColumn.add(new HomeItem(HomeItem.Type.explorer, "Explorer"));
            defaultItems.add(0, explorerColumn);
            if (onComplete != null)
                onComplete.accept(defaultItems);
            //addItemAt(createExplorerItem(), 0, false);
            //refreshHomeItems();
            //saveHomeItemsToFile(Paths.HOME_ITEMS_DIR_INTERNAL, Paths.HOME_ITEMS_FILE_NAME);
        }), Intent.CATEGORY_LAUNCHER);
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