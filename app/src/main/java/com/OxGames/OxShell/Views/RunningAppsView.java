package com.OxGames.OxShell.Views;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.util.AttributeSet;
import android.view.KeyEvent;

import com.OxGames.OxShell.Helpers.ActivityManager;
import com.OxGames.OxShell.Adapters.GridAdapter;
import com.OxGames.OxShell.Data.GridItem;
import com.OxGames.OxShell.Data.HomeItem;
import com.OxGames.OxShell.Data.PackagesCache;

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

    public void killSelection() {
//        Intent startMain = new Intent(Intent.ACTION_MAIN);
//        startMain.addCategory(Intent.CATEGORY_HOME);
//        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        ActivityManager.GetCurrentActivity().startActivity(startMain);

//        android.app.ActivityManager.AppTask appTask = (android.app.ActivityManager.AppTask)((HomeItem)getItemAtPosition(properPosition)).obj;
        android.app.ActivityManager am = (android.app.ActivityManager) ActivityManager.getCurrentActivity().getSystemService(Context.ACTIVITY_SERVICE);
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
        refresh();
    }

    @Override
    public void makeSelection() {
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
        android.app.ActivityManager am = (android.app.ActivityManager)ActivityManager.getCurrentActivity().getSystemService(Context.ACTIVITY_SERVICE);
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
    public boolean receiveKeyEvent(KeyEvent key_event) {
        if (key_event.getAction() == KeyEvent.ACTION_DOWN) {
            if (key_event.getKeyCode() == KeyEvent.KEYCODE_BUTTON_R2) {
                ActivityManager.goTo(ActivityManager.Page.home);
                return true;
            }
            if (key_event.getKeyCode() == KeyEvent.KEYCODE_BUTTON_X) {
                killSelection();
                return true;
            }
        }
        return super.receiveKeyEvent(key_event);
    }
//    @Override
//    public void refresh() {
////        android.app.ActivityManager am = (android.app.ActivityManager)ActivityManager.GetCurrentActivity().getSystemService(Context.ACTIVITY_SERVICE);
////        List<android.app.ActivityManager.RunningTaskInfo> apps = am.getRunningTasks(1000);
////        android.app.ActivityManager.RunningAppProcessInfo state = new android.app.ActivityManager.RunningAppProcessInfo();
////        android.app.
////        List<android.app.ActivityManager.AppTask> apps = am.getAppTasks();
////        List<android.app.ActivityManager.RunningAppProcessInfo> runningAppProcessInfo = am.getRunningAppProcesses();
//        List<ApplicationInfo> apps = PackagesCache.getAllInstalledApplications();
//
//        ArrayList<GridItem> runningApps = new ArrayList<>();
//        for (int i = 0; i < apps.size(); i++) {
//            if (PackagesCache.isRunning(apps.get(i)) && !PackagesCache.isSystem(apps.get(i)))
//                runningApps.add(new HomeItem(HomeItem.Type.app, PackagesCache.getAppLabel(PackagesCache.getResolveInfo(apps.get(i).packageName)), apps.get(i).packageName));
//        }
//        GridAdapter customAdapter = new GridAdapter(getContext(), runningApps);
//        setAdapter(customAdapter);
//        super.refresh();
//    }
}
