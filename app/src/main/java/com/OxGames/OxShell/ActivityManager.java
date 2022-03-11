package com.OxGames.OxShell;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.View;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

public class ActivityManager {
    private static Hashtable<Class, Page[]> pagesOfActivities;
    public enum Page { home, explorer, addToHome, packages, assoc, selectdirs }
    private static Page current = Page.home;

    public static void GoTo(Page page) {
        PagedActivity currentActivity = GetCurrentActivity();
        Class nextActivity = GetActivityClass(page);
        current = page;
        if (currentActivity.getClass() != nextActivity) {
            Intent intent = new Intent(currentActivity, nextActivity);
            currentActivity.startActivity(intent);
        } else {
            currentActivity.GoTo(page);
        }
//        nextActivity.GoTo(page);
    }

    public static void Init() {
        if (pagesOfActivities == null) {
            pagesOfActivities = new Hashtable<>();
            pagesOfActivities.put(HomeActivity.class, new Page[]{ Page.home, Page.addToHome, Page.packages, Page.assoc, Page.selectdirs });
            pagesOfActivities.put(ExplorerActivity.class, new Page[]{ Page.explorer });
        }
    }
//    public static void AddActivity(PagedActivity activity, Page[] pages) {
//        Log.d("ActivityManager", "Putting " + activity.getClass());
//        pagesOfActivities.put(activity, pages);
//    }
    public static PagedActivity GetActivity(Page page) {
        PagedActivity activity = null;
        try {
            activity = (PagedActivity)FindEntry(page).getKey().getMethod("GetInstance").invoke(null);
        } catch (NoSuchMethodException ex) {
            Log.e("ActivityManager", ex.getMessage());
        } catch (IllegalAccessException ex) {
            Log.e("ActivityManager", ex.getMessage());
        } catch (InvocationTargetException ex) {
            Log.e("ActivityManager", ex.getMessage());
        }
        return activity;
    }
    public static Map.Entry<Class, Page[]> FindEntry(Page page) {
        Set<Map.Entry<Class, Page[]>> entrySet = pagesOfActivities.entrySet();
        for (Map.Entry<Class, Page[]> entry : entrySet) {
            if (Arrays.stream(entry.getValue()).anyMatch(otherPage -> (otherPage == page))) {
                return entry;
            }
        }
        return null;
    }
    public static PagedActivity GetCurrentActivity() {
        return GetActivity(GetCurrent());
    }
    public static Page GetCurrent() {
        return current;
    }
    public static Class GetActivityClass(Page page) {
        return FindEntry(page).getKey();
    }
}
