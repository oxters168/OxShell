package com.OxGames.OxShell;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class PackagesView extends SlideTouchListView {
//    private ActivityManager.Page CURRENT_PAGE = ActivityManager.Page.packages;

    public PackagesView(Context context) {
        super(context);
        refresh();
    }
    public PackagesView(Context context, AttributeSet attrs) {
        super(context, attrs);
        refresh();
    }
    public PackagesView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        refresh();
    }

//    @Override
//    public boolean onKeyDown(int key_code, KeyEvent key_event) {
////        Log.d("ExplorerView", key_code + " " + key_event);
//        if (key_code == KeyEvent.KEYCODE_BUTTON_B || key_code == KeyEvent.KEYCODE_BACK) {
//            ActivityManager.GoTo(ActivityManager.Page.addToHome);
////            HomeActivity.GetInstance().GoTo(HomeActivity.Page.addToHome);
//            return false;
//        }
//
//        return super.onKeyDown(key_code, key_event);
//    }
    @Override
    public boolean receiveKeyEvent(KeyEvent key_event) {
    //        Log.d("ExplorerView", key_code + " " + key_event);
        if (key_event.getAction() == KeyEvent.ACTION_DOWN) {
            if (key_event.getKeyCode() == KeyEvent.KEYCODE_BUTTON_B || key_event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
                ActivityManager.goTo(ActivityManager.Page.addToHome);
                //            HomeActivity.GetInstance().GoTo(HomeActivity.Page.addToHome);
                return true;
            }
        }

        return super.receiveKeyEvent(key_event);
    }

//    @Override
//    public void onConfigurationChanged(Configuration newConfig) {
//        super.onConfigurationChanged(newConfig);
//        int storedPos = properPosition;
//        Refresh();
//        SetProperPosition(storedPos);
//    }

    @Override
    public void makeSelection() {
        DetailItem currentItem = (DetailItem)getItemAtPosition(properPosition);
        ResolveInfo rsvInfo = PackagesCache.getResolveInfo((String)currentItem.obj);
        Log.d("PackagesView", (String)currentItem.obj);
        HomeManager.addItemAndSave(new HomeItem(HomeItem.Type.app, PackagesCache.getAppLabel(rsvInfo), (String)currentItem.obj));
        Toast.makeText(ActivityManager.getCurrentActivity(), "Added " + ((DetailItem)getItemAtPosition(properPosition)).leftAlignedText + " to home", Toast.LENGTH_SHORT).show();
    }
    @Override
    public void refresh() {
        refresh(new String[] { Intent.CATEGORY_LAUNCHER });
    }
    public void refresh(String[] categories) {
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        for (int i = 0; i < categories.length; i++)
            mainIntent.addCategory(categories[i]);

        //Needs some clean up (use common PackagesCache function to get all apps)
        ArrayList<DetailItem> intentNames = new ArrayList<>();
        PagedActivity currentActivity = ActivityManager.getCurrentActivity();
        Log.d("PackagesView", currentActivity.toString());
        List<ResolveInfo> pkgAppsList = currentActivity.getPackageManager().queryIntentActivities(mainIntent, 0);
        for (int i = 0; i < pkgAppsList.size(); i++) {
            ResolveInfo currentPkg = pkgAppsList.get(i);
            intentNames.add(new DetailItem(PackagesCache.getPackageIcon(currentPkg), PackagesCache.getAppLabel(currentPkg), null, currentPkg.activityInfo.packageName));
        }
        DetailAdapter intentsAdapter = new DetailAdapter(getContext(), intentNames);
        setAdapter(intentsAdapter);
        super.refresh();
    }
}
