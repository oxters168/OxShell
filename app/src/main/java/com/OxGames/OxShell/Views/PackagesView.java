package com.OxGames.OxShell.Views;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import com.OxGames.OxShell.Helpers.ActivityManager;
import com.OxGames.OxShell.Adapters.DetailAdapter;
import com.OxGames.OxShell.Data.DetailItem;
import com.OxGames.OxShell.Data.HomeItem;
import com.OxGames.OxShell.Data.HomeManager;
import com.OxGames.OxShell.Data.PackagesCache;
import com.OxGames.OxShell.Interfaces.PkgAppsListener;
import com.OxGames.OxShell.Interfaces.PkgIconListener;
import com.OxGames.OxShell.OxShellApp;
import com.OxGames.OxShell.PagedActivity;

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
                ActivityManager.goTo(ActivityManager.Page.customizeHome);
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
        //rsvInfo.activityInfo.applicationInfo.category

        Log.d("PackagesView", (String)currentItem.obj);
        HomeManager.addItem(new HomeItem(HomeItem.Type.app, PackagesCache.getAppLabel(rsvInfo), (String)currentItem.obj));
        Toast.makeText(getContext(), "Added " + ((DetailItem)getItemAtPosition(properPosition)).leftAlignedText + " to home", Toast.LENGTH_SHORT).show();
    }
    @Override
    public void refresh() {
        refresh(Intent.CATEGORY_LAUNCHER);
    }
    public void refresh(String... categories) {
        PackagesCache.requestInstalledPackages(Intent.ACTION_MAIN, apps -> ActivityManager.getCurrentActivity().runOnUiThread(() -> {
            DetailAdapter intentsAdapter = new DetailAdapter(getContext());
            setAdapter(intentsAdapter);
            for (int i = 0; i < apps.size(); i++) {
                ResolveInfo currentPkg = apps.get(i);
                intentsAdapter.add(new DetailItem(PackagesCache.getPackageIcon(currentPkg), PackagesCache.getAppLabel(currentPkg), null, currentPkg.activityInfo.packageName));
                PackagesView.this.invalidateViews();
            }
        }), categories);

        //Log.d("PackagesView", "Finished listing apps");
        super.refresh();
    }
}
