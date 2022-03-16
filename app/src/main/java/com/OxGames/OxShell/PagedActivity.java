package com.OxGames.OxShell;

import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PagedActivity extends AppCompatActivity {
    protected Hashtable<ActivityManager.Page, View> allPages = new Hashtable<>();
//    private static PagedActivity instance;
//    public static DisplayMetrics displayMetrics;
    private List<PermissionsListener> permissionListeners = new ArrayList<>();
    protected ActivityManager.Page currentPage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        instance = this;
        ActivityManager.Init();
        ActivityManager.InstanceCreated(this);

        HideActionBar();

//        InitViewsTable();

//        RefreshDisplayMetrics();


//        HomeManager.Init();
        Log.d("PagedActivity", "OnCreate " + this);
    }

//    @Override
//    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
//        super.onPostCreate(savedInstanceState);
//        InitViewsTable();
//    }

    @Override
    protected void onResume() {
        ActivityManager.SetCurrent(currentPage);
        GoTo(currentPage);
        super.onResume();
        //Add an if statement later to have a setting for hiding status bar
        //HideStatusBar();
        Log.d("PagedActivity", "OnResume " + this);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        for (PermissionsListener pl : permissionListeners) {
            pl.onPermissionResponse(requestCode, permissions, grantResults);
        }
    }

    public void AddPermissionListener(PermissionsListener listener) {
        permissionListeners.add(listener);
    }

//    public static PagedActivity GetInstance() {
//        return instance;
//    }

    private void HideStatusBar() {
        View decorView = getWindow().getDecorView();
        // Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
    }
    private void HideActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.hide();
    }

//    public void RefreshDisplayMetrics() {
//        displayMetrics = new DisplayMetrics();
//        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
//    }
    public DisplayMetrics GetDisplayMetrics() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics;
    }

    protected void InitViewsTable() {
    }
    public void GoTo(ActivityManager.Page page) {
        if (currentPage != page) {
            Set<Map.Entry<ActivityManager.Page, View>> entrySet = allPages.entrySet();
            for (Map.Entry<ActivityManager.Page, View> entry : entrySet) {
                entry.getValue().setVisibility(page == entry.getKey() ? View.VISIBLE : View.GONE);
                if (page == entry.getKey()) {
                    View nextPage = entry.getValue();
                    if (nextPage instanceof SlideTouchListView)
                        ((SlideTouchListView) nextPage).Refresh();
                    else if (nextPage instanceof HomeView)
                        ((HomeView) nextPage).Refresh();
                    nextPage.requestFocusFromTouch();
                    currentPage = page;
                }
            }
        }
    }
}
