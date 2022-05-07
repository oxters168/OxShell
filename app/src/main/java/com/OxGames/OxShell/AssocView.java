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
        refresh();
    }
    public AssocView(Context context, AttributeSet attrs) {
        super(context, attrs);
        refresh();
    }
    public AssocView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        refresh();
    }

    @Override
    public boolean receiveKeyEvent(KeyEvent key_event) {
        //        Log.d("ExplorerView", key_code + " " + key_event);
        if (key_event.getAction() == KeyEvent.ACTION_DOWN) {
            if (key_event.getKeyCode() == KeyEvent.KEYCODE_BUTTON_B || key_event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
                ActivityManager.goTo(ActivityManager.Page.addToHome);
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
        IntentLaunchData selectedItem = (IntentLaunchData)((DetailItem)getItemAtPosition(properPosition)).obj;
        if (selectedItem == null)
            refresh(); //Create new assoc
        else {
            HomeItem addedItem = new HomeItem(HomeItem.Type.assoc, selectedItem.getDisplayName(), selectedItem);
            SelectDirsView.setDirsCarrier(addedItem);
            SelectDirsView.setReturnPage(ActivityManager.Page.assoc);
            ActivityManager.goTo(ActivityManager.Page.selectDirs);
            SelectDirsView.addResultListener(new DirsViewListener() {
                @Override
                public void onDirsResult(int resultCode, DirsCarrier output) {
                    if (resultCode == SelectDirsView.RESULT_DONE) {
                        HomeManager.addItemAndSave(addedItem);
                        Toast.makeText(ActivityManager.getCurrentActivity(), "Added " + selectedItem.getDisplayName() + " to home", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
//            HomeManager.AddItemAndSave(new HomeItem(HomeItem.Type.assoc, selectedItem.GetDisplayName(), selectedItem));
    }
    @Override
    public void refresh() {
        IntentLaunchData[] intents = PackagesCache.getStoredIntents();
        ArrayList<DetailItem> intentItems = new ArrayList<>();
        for (int i = 0; i < intents.length; i++) {
            ResolveInfo rsv = PackagesCache.getResolveInfo(intents[i].getPackageName());
            if (rsv != null)
                intentItems.add(new DetailItem(PackagesCache.getPackageIcon(rsv), intents[i].getDisplayName(), "<" + PackagesCache.getAppLabel(rsv) + ">", intents[i]));
        }
        intentItems.add(new DetailItem(ContextCompat.getDrawable(ActivityManager.getCurrentActivity(), R.drawable.ic_baseline_add_circle_outline_24), "Create new", null, null));
        DetailAdapter addAdapter = new DetailAdapter(getContext(), intentItems);
        setAdapter(addAdapter);
    }
}
