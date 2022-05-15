package com.OxGames.OxShell;

import android.content.Intent;

import java.util.Arrays;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

public class ActivityManager {
    private static Hashtable<Class, Page[]> pagesOfActivities;
    private static Hashtable<Class, PagedActivity> activityInstances;
    public enum Page { home, explorer, chooser, settings, customizeHome, pkgList, assocList, selectDirs, intentShortcuts, runningApps }
    private static Page current = Page.home;

    public static void goTo(Page page) {
        PagedActivity currentActivity = getCurrentActivity(); //When going back from selectdirsview after choosing a directory the current directory seems to stay as chooseractivity
        Class nextActivity = getActivityClass(page);
//        Class prevActivity = GetActivityClass(from);
//        current = page;
//        PagedActivity instance = GetInstance(prevActivity);
        if (currentActivity.getClass() != nextActivity) {
            Intent intent = new Intent(currentActivity, nextActivity);
            currentActivity.startActivity(intent);
        } else {
            currentActivity.goTo(page);
        }
//        nextActivity.GoTo(page);
    }
    public static void setCurrent(Page page) {
        current = page;
    }

    public static void init() {
        if (pagesOfActivities == null) {
            pagesOfActivities = new Hashtable<>();
            pagesOfActivities.put(HomeActivity.class, new Page[] { Page.home, Page.settings, Page.customizeHome, Page.pkgList, Page.assocList, Page.selectDirs, Page.intentShortcuts, Page.runningApps });
            pagesOfActivities.put(ExplorerActivity.class, new Page[] { Page.explorer });
            pagesOfActivities.put(FileChooserActivity.class, new Page[] { Page.chooser });
        }
        if (activityInstances == null)
            activityInstances = new Hashtable<>();
    }
//    public static void AddActivity(PagedActivity activity, Page[] pages) {
//        Log.d("ActivityManager", "Putting " + activity.getClass());
//        pagesOfActivities.put(activity, pages);
//    }
    public static void instanceCreated(PagedActivity instance) {
        activityInstances.put(instance.getClass(), instance);
    }
    public static PagedActivity getInstance(Class type) {
        PagedActivity instance = null;
        if (activityInstances.containsKey(type))
            instance = activityInstances.get(type);
        return instance;
    }
    public static PagedActivity getActivity(Page page) {
        PagedActivity activity = activityInstances.get(findEntry(page).getKey());
//        PagedActivity activity = null;
//        try {
//            activity = (PagedActivity)FindEntry(page).getKey().getMethod("GetInstance").invoke(null);
//        } catch (NoSuchMethodException ex) {
//            Log.e("ActivityManager", ex.getMessage());
//        } catch (IllegalAccessException ex) {
//            Log.e("ActivityManager", ex.getMessage());
//        } catch (InvocationTargetException ex) {
//            Log.e("ActivityManager", ex.getMessage());
//        }
        return activity;
    }
    public static Map.Entry<Class, Page[]> findEntry(Page page) {
        Set<Map.Entry<Class, Page[]>> entrySet = pagesOfActivities.entrySet();
        for (Map.Entry<Class, Page[]> entry : entrySet) {
            if (Arrays.stream(entry.getValue()).anyMatch(otherPage -> (otherPage == page))) {
                return entry;
            }
        }
        return null;
    }
    public static PagedActivity getCurrentActivity() {
        return getActivity(getCurrent());
    }
    public static Page getCurrent() {
        return current; //Can be wrong
    }
    public static Class getActivityClass(Page page) {
        return findEntry(page).getKey();
    }
}
