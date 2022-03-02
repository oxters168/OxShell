package com.OxGames.OxShell;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;

import java.util.ArrayList;
import java.util.List;

public class PackagesView extends SlideTouchListView {
    public PackagesView(Context context) {
        super(context);
        RefreshPackages(new String[] { Intent.CATEGORY_LAUNCHER });
    }
    public PackagesView(Context context, AttributeSet attrs) {
        super(context, attrs);
        RefreshPackages(new String[] { Intent.CATEGORY_LAUNCHER });
    }
    public PackagesView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        RefreshPackages(new String[] { Intent.CATEGORY_LAUNCHER });
    }

    @Override
    public boolean onKeyDown(int key_code, KeyEvent key_event) {
        Log.d("ExplorerView", key_code + " " + key_event);
        if (key_code == KeyEvent.KEYCODE_BUTTON_START || key_code == KeyEvent.KEYCODE_BACK) {
//            ActivityManager.GoTo(ActivityManager.Page.home);
            HomeActivity.GetInstance().GoTo(HomeActivity.Page.home);
            return false;
        }

        return super.onKeyDown(key_code, key_event);
    }


    @Override
    public void MakeSelection() {
        ResolveInfo rsvInfo = (ResolveInfo)((DetailItem)getItemAtPosition(properPosition)).obj;
//        HomeActivity.GetInstance().getPackageManager().getLaunchIntentForPackage()
        IntentLaunchData selectedPkg = new IntentLaunchData(rsvInfo.activityInfo.packageName);
        selectedPkg.Launch();
    }

    public void RefreshPackages(String[] categories) {
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        for (int i = 0; i < categories.length; i++)
            mainIntent.addCategory(categories[i]);

        List<ResolveInfo> pkgAppsList = HomeActivity.GetInstance().getPackageManager().queryIntentActivities( mainIntent, 0);
        ArrayList<DetailItem> intentNames = new ArrayList<>();
        for (int i = 0; i < pkgAppsList.size(); i++) {
            String pkgName = pkgAppsList.get(i).activityInfo.packageName;
            if (pkgName != null) {
                ApplicationInfo pkgInfo = PackagesCache.GetPackageInfo(pkgName);
                String appName = "???";
                if (pkgInfo != null)
                    appName = PackagesCache.GetAppLabel(pkgInfo);
                intentNames.add(new DetailItem(PackagesCache.GetPackageIcon(pkgName), appName, null, pkgAppsList.get(i)));
            }
        }
        DetailAdapter intentsAdapter = new DetailAdapter(getContext(), intentNames);
        setAdapter(intentsAdapter);
    }
}
