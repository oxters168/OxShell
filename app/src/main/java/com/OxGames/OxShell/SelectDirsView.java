package com.OxGames.OxShell;

import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.view.KeyEvent;

public class SelectDirsView extends SlideTouchListView {
    public SelectDirsView(Context context) {
        super(context);
        Refresh();
    }
    public SelectDirsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        Refresh();
    }
    public SelectDirsView(Context context, AttributeSet attrs, int defStyle) {
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

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        int storedPos = properPosition;
        Refresh();
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
//        IntentLaunchData selectedItem = (IntentLaunchData)((DetailItem)getItemAtPosition(properPosition)).obj;
    }
    @Override
    public void Refresh() {
//        DetailAdapter addAdapter = new DetailAdapter(getContext(), intentItems);
//        setAdapter(addAdapter);
    }
}
