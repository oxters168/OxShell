package com.OxGames.OxShell;

import android.content.Context;
import android.content.pm.ResolveInfo;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import java.util.ArrayList;

public class AssocView extends SlideTouchListView {
//    private ActivityManager.Page CURRENT_PAGE = ActivityManager.Page.assoc;

    public AssocView(Context context) {
        super(context);
        Refresh();
    }
    public AssocView(Context context, AttributeSet attrs) {
        super(context, attrs);
        Refresh();
    }
    public AssocView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        Refresh();
    }

    @Override
    public boolean onKeyDown(int key_code, KeyEvent key_event) {
//        Log.d("ExplorerView", key_code + " " + key_event);
        if (key_code == KeyEvent.KEYCODE_BUTTON_B || key_code == KeyEvent.KEYCODE_BACK) {
            ActivityManager.GoTo(ActivityManager.Page.addToHome);
//            HomeActivity.GetInstance().GoTo(HomeActivity.Page.addToHome);
            return false;
        }

        return super.onKeyDown(key_code, key_event);
    }

//    @Override
//    public void onConfigurationChanged(Configuration newConfig) {
//        super.onConfigurationChanged(newConfig);
//        int storedPos = properPosition;
//        Refresh();
//        SetProperPosition(storedPos);
//    }

    @Override
    public void MakeSelection() {
        IntentLaunchData selectedItem = (IntentLaunchData)((DetailItem)getItemAtPosition(properPosition)).obj;
        if (selectedItem == null)
            Refresh(); //Create new assoc
        else {
            HomeItem addedItem = new HomeItem(HomeItem.Type.assoc, selectedItem.GetDisplayName(), selectedItem);
            SelectDirsView.SetDirsCarrier(addedItem);
            SelectDirsView.SetReturnPage(ActivityManager.Page.assoc);
            ActivityManager.GoTo(ActivityManager.Page.selectDirs);
            SelectDirsView.AddResultListener(new DirsViewListener() {
                @Override
                public void onDirsResult(int resultCode, DirsCarrier output) {
                    if (resultCode == SelectDirsView.RESULT_DONE) {
                        HomeManager.AddItemAndSave(addedItem);
                        Toast.makeText(ActivityManager.GetCurrentActivity(), "Added " + selectedItem.GetDisplayName() + " to home", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
//            HomeManager.AddItemAndSave(new HomeItem(HomeItem.Type.assoc, selectedItem.GetDisplayName(), selectedItem));
    }
    @Override
    public void Refresh() {
        IntentLaunchData[] intents = PackagesCache.GetStoredIntents();
        ArrayList<DetailItem> intentItems = new ArrayList<>();
        for (int i = 0; i < intents.length; i++) {
            ResolveInfo rsv = PackagesCache.GetResolveInfo(intents[i].GetPackageName());
            intentItems.add(new DetailItem(PackagesCache.GetPackageIcon(rsv), intents[i].GetDisplayName(), "<" + PackagesCache.GetAppLabel(rsv) + ">", intents[i]));
        }
        intentItems.add(new DetailItem(ContextCompat.getDrawable(ActivityManager.GetCurrentActivity(), R.drawable.ic_baseline_add_circle_outline_24), "Create new", null, null));
        DetailAdapter addAdapter = new DetailAdapter(getContext(), intentItems);
        setAdapter(addAdapter);
    }
}
