package com.OxGames.OxShell;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;

import java.util.ArrayList;
import java.util.List;

public class RunningAppsView extends SlideTouchGridView {
    public RunningAppsView(Context context) {
        super(context);
    }
    public RunningAppsView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public RunningAppsView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void MakeSelection() {
        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ActivityManager.GetCurrentActivity().startActivity(startMain);

        String pkgName = (String)((HomeItem)getItemAtPosition(properPosition)).obj;
        Log.d("RunningApps", "Killing " + pkgName);
        android.app.ActivityManager am = (android.app.ActivityManager)ActivityManager.GetCurrentActivity().getSystemService(Activity.ACTIVITY_SERVICE);
        am.killBackgroundProcesses(pkgName);
        Refresh();
    }

    @Override
    public boolean onKeyDown(int key_code, KeyEvent key_event) {
        if (key_code == KeyEvent.KEYCODE_BUTTON_R2) {
            ActivityManager.GoTo(ActivityManager.Page.home);
            return false;
        }
        return super.onKeyDown(key_code, key_event);
    }
    @Override
    public void Refresh() {
//        android.app.ActivityManager am = (android.app.ActivityManager)ActivityManager.GetCurrentActivity().getSystemService(Context.ACTIVITY_SERVICE);
//        List<android.app.ActivityManager.RunningAppProcessInfo> runningAppProcessInfo = am.getRunningAppProcesses();
        List<ApplicationInfo> apps = PackagesCache.GetAllInstalledApplications();

        ArrayList<GridItem> runningApps = new ArrayList<>();
        for (int i = 0; i < apps.size(); i++) {
            if (PackagesCache.IsRunning(apps.get(i)) && !PackagesCache.IsSystem(apps.get(i)))
                runningApps.add(new HomeItem(HomeItem.Type.app, PackagesCache.GetAppLabel(PackagesCache.GetResolveInfo(apps.get(i).packageName)), apps.get(i).packageName));
        }
        GridAdapter customAdapter = new GridAdapter(getContext(), runningApps);
        setAdapter(customAdapter);
    }
}
