package com.OxGames.OxShell;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;

import androidx.annotation.RequiresApi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

    public void KillSelection() {
//        Intent startMain = new Intent(Intent.ACTION_MAIN);
//        startMain.addCategory(Intent.CATEGORY_HOME);
//        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        ActivityManager.GetCurrentActivity().startActivity(startMain);

//        android.app.ActivityManager.AppTask appTask = (android.app.ActivityManager.AppTask)((HomeItem)getItemAtPosition(properPosition)).obj;
        android.app.ActivityManager am = (android.app.ActivityManager)ActivityManager.GetCurrentActivity().getSystemService(Context.ACTIVITY_SERVICE);
//        android.app.ActivityManager.RunningTaskInfo taskInfo = (android.app.ActivityManager.RunningTaskInfo)((GridItem)getItemAtPosition(properPosition)).obj;
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//            Log.d("RunningApps", "Killing " + taskInfo.baseIntent.getPackage());
//        }

//        appTask.finishAndRemoveTask();
        String pkgName = (String)((HomeItem)getItemAtPosition(properPosition)).obj;
//        try {
//            Method method = am.getClass().getMethod("killBackgroundProcesses", String.class);
//            method.invoke(am, pkgName);
//        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException ex) {
//            Log.e("RunningApps", ex.getMessage());
//        }
//        int pid = Integer.parseInt(ShellCommander.Run("pidof " + pkgName));
//        int pid = 29140;
//        android.app.ActivityManager am = (android.app.ActivityManager)ActivityManager.GetCurrentActivity().getSystemService(Context.ACTIVITY_SERVICE);
//        android.os.Process.sendSignal(pid, android.os.Process.SIGNAL_KILL);
//        android.os.Process.killProcess(pid);
//        am.killBackgroundProcesses(pkgName);
        am.restartPackage(pkgName);
        Refresh();
    }

    @Override
    public void MakeSelection() {
//        android.app.ActivityManager.AppTask appTask = (android.app.ActivityManager.AppTask)((HomeItem)getItemAtPosition(properPosition)).obj;
//        android.app.ActivityManager am = (android.app.ActivityManager)ActivityManager.GetCurrentActivity().getSystemService(Context.ACTIVITY_SERVICE);
//        android.app.ActivityManager.RunningTaskInfo taskInfo = (android.app.ActivityManager.RunningTaskInfo)((GridItem)getItemAtPosition(properPosition)).obj;
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//            Log.d("RunningApps", "Switching to " + taskInfo.baseIntent.getPackage());
//            am.moveTaskToFront(taskInfo.taskId, 0);
//        }
//        appTask.moveToFront();
        String pkgName = (String)((HomeItem)getItemAtPosition(properPosition)).obj;
//        int pid = Integer.parseInt(ShellCommander.Run("pidof " + pkgName));
        int pid = 29140;
        android.app.ActivityManager am = (android.app.ActivityManager)ActivityManager.GetCurrentActivity().getSystemService(Context.ACTIVITY_SERVICE);
//        int tid = PackagesCache.GetPackageInfo(pkgName).uid;
        am.moveTaskToFront(pid, 0, null);
    }

//    @Override
//    public boolean onKeyDown(int key_code, KeyEvent key_event) {
//        if (key_code == KeyEvent.KEYCODE_BUTTON_R2) {
//            ActivityManager.GoTo(ActivityManager.Page.home);
//            return false;
//        }
//        return super.onKeyDown(key_code, key_event);
//    }
    @Override
    public boolean ReceiveKeyEvent(KeyEvent key_event) {
        if (key_event.getAction() == KeyEvent.ACTION_DOWN) {
            if (key_event.getKeyCode() == KeyEvent.KEYCODE_BUTTON_R2) {
                ActivityManager.GoTo(ActivityManager.Page.home);
                return true;
            }
            if (key_event.getKeyCode() == KeyEvent.KEYCODE_BUTTON_X) {
                KillSelection();
                return true;
            }
        }
        return super.ReceiveKeyEvent(key_event);
    }
    @Override
    public void Refresh() {
//        android.app.ActivityManager am = (android.app.ActivityManager)ActivityManager.GetCurrentActivity().getSystemService(Context.ACTIVITY_SERVICE);
//        List<android.app.ActivityManager.RunningTaskInfo> apps = am.getRunningTasks(1000);
//        android.app.ActivityManager.RunningAppProcessInfo state = new android.app.ActivityManager.RunningAppProcessInfo();
//        android.app.
//        List<android.app.ActivityManager.AppTask> apps = am.getAppTasks();
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
