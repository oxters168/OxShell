package com.OxGames.OxShell;

import androidx.appcompat.app.ActionBar;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;

import android.os.Build;
import android.content.Intent;
import android.provider.Settings;
import android.content.pm.ResolveInfo;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class HomeActivity extends AppCompatActivity {
    public enum Page { home, explorer, packages }
    private Hashtable<Page, View> allPages;
    private static HomeActivity instance;
    private ArrayAdapter<String> intentsAdapter;
    public static DisplayMetrics displayMetrics;
    private List<PermissionsListener> permissionListeners = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instance = this;

        setContentView(R.layout.activity_home);

//        ((HomeView)findViewById(R.id.home_view)).setChoiceMode(AbsListView.CHOICE_MODE_NONE);
        //findViewById(R.id.home_view).requestFocusFromTouch();
        InitViewsTable();
        GoTo(Page.home);

        HideActionBar();

        PackagesCache.PrepareDefaultLaunchIntents();

        displayMetrics = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //Add an if statement later to optionally hide status bar
        //HideStatusBar();
    }

    private void InitViewsTable() {
        allPages = new Hashtable<>();
        allPages.put(Page.home, findViewById(R.id.home_view));
        allPages.put(Page.explorer, findViewById(R.id.explorer_list));
        allPages.put(Page.packages, findViewById(R.id.packages_list));
    }
    public void GoTo(Page page) {
        Set<Map.Entry<Page, View>> entrySet = allPages.entrySet();
        for (Map.Entry<Page, View> entry : entrySet) {
            entry.getValue().setVisibility(page == entry.getKey() ? View.VISIBLE : View.GONE);
            if (page == entry.getKey())
                entry.getValue().requestFocusFromTouch();
        }
    }

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

    @Override
    public boolean onKeyDown(int key_code, KeyEvent key_event) {
        Log.d("HomeActivity", key_code + " " + key_event);
        return true;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        for (PermissionsListener pl : permissionListeners) {
            pl.onPermissionResponse(requestCode, permissions, grantResults);
        }
    }

    public static HomeActivity GetInstance() {
        return instance;
    }

    public void AddPermissionListener(PermissionsListener listener) {
        permissionListeners.add(listener);
    }

    public void getOverlayPermissionBtn(View view) {
        // Check if Android M or higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Show alert dialog to the user saying a separate permission is needed
            // Launch the settings activity if the user prefers
            Intent myIntent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
            startActivity(myIntent);
        }
    }
}