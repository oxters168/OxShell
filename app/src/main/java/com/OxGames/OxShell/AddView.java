package com.OxGames.OxShell;

import android.content.Context;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

public class AddView extends SlideTouchListView {
    private Hashtable<HomeItem.Type, String> buttons;
//    private ActivityManager.Page CURRENT_PAGE = ActivityManager.Page.addToHome;
//    private final String[] btnLabels = new String[] { "Add Explorer", "Add Application", "Add Association" };

    public AddView(Context context) {
        super(context);
        Refresh();
    }
    public AddView(Context context, AttributeSet attrs) {
        super(context, attrs);
        Refresh();
    }
    public AddView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        Refresh();
    }

    @Override
    public boolean onKeyDown(int key_code, KeyEvent key_event) {
//        Log.d("ExplorerView", key_code + " " + key_event);
        if (key_code == KeyEvent.KEYCODE_BUTTON_B || key_code == KeyEvent.KEYCODE_BACK) {
            ActivityManager.GoTo(ActivityManager.Page.home);
//            HomeActivity.GetInstance().GoTo(HomeActivity.Page.home);
            return false;
        }

        return super.onKeyDown(key_code, key_event);
    }

//    @Override
//    public void onConfigurationChanged(Configuration newConfig) {
//        super.onConfigurationChanged(newConfig);
//        int storedPos = properPosition;
//        RefreshBtns();
//        SetProperPosition(storedPos);
//    }

    @Override
    public void MakeSelection() {
        HomeItem.Type selectedItem = (HomeItem.Type)((DetailItem)getItemAtPosition(properPosition)).obj;
        if (selectedItem == HomeItem.Type.explorer) {
            HomeManager.AddExplorer();
            Toast.makeText(ActivityManager.GetCurrentActivity(), "Added explorer to home", Toast.LENGTH_SHORT).show();
        }
        else if (selectedItem == HomeItem.Type.app)
            ActivityManager.GoTo(ActivityManager.Page.packages);
        else if (selectedItem == HomeItem.Type.assoc)
            ActivityManager.GoTo(ActivityManager.Page.assoc);
    }

    private void InitButtons() {
        buttons = new Hashtable<>();
        buttons.put(HomeItem.Type.assoc, "Add Association");
        buttons.put(HomeItem.Type.app, "Add Application");
        buttons.put(HomeItem.Type.explorer, "Add Explorer");
    }
    @Override
    public void Refresh() {
        InitButtons();
        ArrayList<DetailItem> addBtns = new ArrayList<>();
        Set<Map.Entry<HomeItem.Type, String>> entrySet = buttons.entrySet();
        for (Map.Entry<HomeItem.Type, String> entry : entrySet)
            addBtns.add(new DetailItem(null, entry.getValue(), null, entry.getKey()));
        DetailAdapter addAdapter = new DetailAdapter(getContext(), addBtns);
        setAdapter(addAdapter);
    }
}
