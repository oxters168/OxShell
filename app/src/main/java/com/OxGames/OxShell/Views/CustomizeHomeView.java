package com.OxGames.OxShell.Views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.Toast;

import com.OxGames.OxShell.ActivityManager;
import com.OxGames.OxShell.DetailAdapter;
import com.OxGames.OxShell.Data.DetailItem;
import com.OxGames.OxShell.Data.HomeItem;
import com.OxGames.OxShell.HomeManager;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

public class CustomizeHomeView extends SlideTouchListView {
    private Hashtable<HomeItem.Type, String> buttons;
//    private ActivityManager.Page CURRENT_PAGE = ActivityManager.Page.addToHome;
//    private final String[] btnLabels = new String[] { "Add Explorer", "Add Application", "Add Association" };

    public CustomizeHomeView(Context context) {
        super(context);
        refresh();
    }
    public CustomizeHomeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        refresh();
    }
    public CustomizeHomeView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        refresh();
    }

//    @Override
//    public boolean onKeyDown(int key_code, KeyEvent key_event) {
////        Log.d("ExplorerView", key_code + " " + key_event);
//        if (key_code == KeyEvent.KEYCODE_BUTTON_B || key_code == KeyEvent.KEYCODE_BACK) {
//            ActivityManager.GoTo(ActivityManager.Page.home);
////            HomeActivity.GetInstance().GoTo(HomeActivity.Page.home);
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
                ActivityManager.goTo(ActivityManager.Page.settings);
                return true;
            }
        }

        return super.receiveKeyEvent(key_event);
    }

    @Override
    public void makeSelection() {
        HomeItem.Type selectedItem = (HomeItem.Type)((DetailItem)getItemAtPosition(properPosition)).obj;
        if (selectedItem == HomeItem.Type.explorer) {
            HomeManager.addExplorerAndSave();
            Toast.makeText(ActivityManager.getCurrentActivity(), "Added explorer to home", Toast.LENGTH_SHORT).show();
        }
        else if (selectedItem == HomeItem.Type.app)
            ActivityManager.goTo(ActivityManager.Page.pkgList);
        else if (selectedItem == HomeItem.Type.assoc)
            ActivityManager.goTo(ActivityManager.Page.assocList);
    }

    private void initButtons() {
        buttons = new Hashtable<>();
        //buttons.put(HomeItem.Type.assoc, "Add Association");
        buttons.put(HomeItem.Type.app, "Add Application");
        buttons.put(HomeItem.Type.explorer, "Add Explorer");
    }
    @Override
    public void refresh() {
        initButtons();
        ArrayList<DetailItem> addBtns = new ArrayList<>();
        Set<Map.Entry<HomeItem.Type, String>> entrySet = buttons.entrySet();
        for (Map.Entry<HomeItem.Type, String> entry : entrySet)
            addBtns.add(new DetailItem(null, entry.getValue(), null, entry.getKey()));
        DetailAdapter addAdapter = new DetailAdapter(getContext(), addBtns);
        setAdapter(addAdapter);
        super.refresh();
    }
}
