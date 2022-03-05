package com.OxGames.OxShell;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class PackagesView extends SlideTouchListView {
    public PackagesView(Context context) {
        super(context);
        RefreshPackages();
    }
    public PackagesView(Context context, AttributeSet attrs) {
        super(context, attrs);
        RefreshPackages();
    }
    public PackagesView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        RefreshPackages();
    }

    @Override
    public boolean onKeyDown(int key_code, KeyEvent key_event) {
//        Log.d("ExplorerView", key_code + " " + key_event);
        if (key_code == KeyEvent.KEYCODE_BUTTON_B || key_code == KeyEvent.KEYCODE_BACK) {
//            ActivityManager.GoTo(ActivityManager.Page.home);
            HomeActivity.GetInstance().GoTo(HomeActivity.Page.home);
            return false;
        }

        return super.onKeyDown(key_code, key_event);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        int storedPos = properPosition;
        RefreshPackages();
        SetProperPosition(storedPos);

        // Checks the orientation of the screen
//        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
//            Toast.makeText(ActivityManager.GetActivityInstance(ActivityManager.GetCurrent()), "landscape", Toast.LENGTH_SHORT).show();
//        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
//            Toast.makeText(ActivityManager.GetActivityInstance(ActivityManager.GetCurrent()), "portrait", Toast.LENGTH_SHORT).show();
//        }
    }

    @Override
    public void MakeSelection() {
        DetailItem currentItem = (DetailItem)getItemAtPosition(properPosition);
        ResolveInfo rsvInfo = PackagesCache.GetResolveInfo((String)currentItem.obj);
        Log.d("PackagesView", (String)currentItem.obj);
        HomeManager.AddItemAndSave(new HomeItem(HomeItem.Type.app, PackagesCache.GetAppLabel(rsvInfo), (String)currentItem.obj));
        Toast.makeText(ActivityManager.GetActivityInstance(ActivityManager.GetCurrent()), "Added " + ((DetailItem)getItemAtPosition(properPosition)).leftAlignedText + " to home", Toast.LENGTH_SHORT).show();
    }

    public void RefreshPackages() {
        RefreshPackages(new String[] { Intent.CATEGORY_LAUNCHER });
    }
    public void RefreshPackages(String[] categories) {
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        for (int i = 0; i < categories.length; i++)
            mainIntent.addCategory(categories[i]);

        ArrayList<DetailItem> intentNames = new ArrayList<>();
        List<ResolveInfo> pkgAppsList = ActivityManager.GetActivityInstance(ActivityManager.GetCurrent()).getPackageManager().queryIntentActivities(mainIntent, 0);
        for (int i = 0; i < pkgAppsList.size(); i++) {
            ResolveInfo currentPkg = pkgAppsList.get(i);
            intentNames.add(new DetailItem(PackagesCache.GetPackageIcon(currentPkg), PackagesCache.GetAppLabel(currentPkg), null, currentPkg.activityInfo.packageName));
        }
        DetailAdapter intentsAdapter = new DetailAdapter(getContext(), intentNames);
        setAdapter(intentsAdapter);
    }
}
