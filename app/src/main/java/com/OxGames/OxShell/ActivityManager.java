package com.OxGames.OxShell;

import android.app.Activity;
import android.content.Intent;

public class ActivityManager {
    public enum Page { home, explorer }
    private static Page current = Page.home;

    public static void GoTo(Page page) {
        Activity currentActivity = GetActivityInstance(current);
        Intent intent = new Intent(currentActivity, GetActivityClass(page));
        currentActivity.startActivity(intent);
    }

    public static Page GetCurrent() {
        return current;
    }
    public static Activity GetActivityInstance(Page page) {
        Activity activity = null;
        if (page == Page.home)
            activity = HomeActivity.GetInstance();
        else if (page == Page.explorer)
            activity = ExplorerActivity.GetInstance();
        return activity;
    }
    public static Class GetActivityClass(Page page) {
        Class cls = null;
        if (page == Page.home)
            cls = HomeActivity.class;
        if (page == Page.explorer)
            cls = ExplorerActivity.class;
        return cls;
    }
}
