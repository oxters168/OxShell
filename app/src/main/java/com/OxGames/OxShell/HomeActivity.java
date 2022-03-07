package com.OxGames.OxShell;

import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;

import java.util.Hashtable;

public class HomeActivity extends PagedActivity {
//    public enum Page { home, addToHome, packages, assoc }
//    private Hashtable<ActivityManager.Page, View> allPages;
//    private static HomeActivity instance;
//    public static DisplayMetrics displayMetrics;
//    private List<PermissionsListener> permissionListeners = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        ActivityManager.AddActivity(GetInstance(), new ActivityManager.Page[] { ActivityManager.Page.home, ActivityManager.Page.addToHome, ActivityManager.Page.packages, ActivityManager.Page.assoc });
        setContentView(R.layout.activity_home);
        InitViewsTable();
//        instance = this;

//        HideActionBar();

//        ActivityManager.Init();
        PackagesCache.PrepareDefaultLaunchIntents();
        HomeManager.Init();
//        ActivityManager.GoTo(ActivityManager.Page.home);

//        RefreshDisplayMetrics();
    }

//    @Override
//    protected void onResume() {
//        super.onResume();
//        //Add an if statement later to optionally hide status bar
//        //HideStatusBar();
//    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        RefreshDisplayMetrics();

        // Checks the orientation of the screen
//        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
//            Toast.makeText(this, "landscape", Toast.LENGTH_SHORT).show();
//        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
//            Toast.makeText(this, "portrait", Toast.LENGTH_SHORT).show();
//        }
    }

//    public void RefreshDisplayMetrics() {
//        displayMetrics = new DisplayMetrics();
//        this.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
//    }

    @Override
    protected void InitViewsTable() {
//        allPages = new Hashtable<>();
//        Log.d("HomeActivity", allPages.toString() + ActivityManager.Page.home);
        allPages.put(ActivityManager.Page.home, findViewById(R.id.home_view));
        allPages.put(ActivityManager.Page.addToHome, findViewById(R.id.add_view));
        allPages.put(ActivityManager.Page.packages, findViewById(R.id.packages_list));
        allPages.put(ActivityManager.Page.assoc, findViewById(R.id.assoc_view));
    }
//    public void GoTo(ActivityManager.Page page) {
//        Set<Map.Entry<ActivityManager.Page, View>> entrySet = allPages.entrySet();
//        for (Map.Entry<ActivityManager.Page, View> entry : entrySet) {
//            entry.getValue().setVisibility(page == entry.getKey() ? View.VISIBLE : View.GONE);
//            if (page == entry.getKey()) {
//                View nextPage = entry.getValue();
//                nextPage.requestFocusFromTouch();
//                if (nextPage instanceof SlideTouchListView)
//                    ((SlideTouchListView)nextPage).Refresh();
//                else if (nextPage instanceof HomeView)
//                    ((HomeView)nextPage).Refresh();
//            }
//        }
//    }
    public void RefreshHome() {
        ((HomeView)allPages.get(ActivityManager.Page.home)).Refresh();
    }

//    private void HideStatusBar() {
//        View decorView = getWindow().getDecorView();
//        // Hide the status bar.
//        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
//        decorView.setSystemUiVisibility(uiOptions);
//    }
//    private void HideActionBar() {
//        ActionBar actionBar = getSupportActionBar();
//        if (actionBar != null)
//            actionBar.hide();
//    }

    @Override
    public boolean onKeyDown(int key_code, KeyEvent key_event) {
//        Log.d("HomeActivity", key_code + " " + key_event);
        return true;
    }
//    @Override
//    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        for (PermissionsListener pl : permissionListeners) {
//            pl.onPermissionResponse(requestCode, permissions, grantResults);
//        }
//    }

//    public static HomeActivity GetInstance() {
//        return instance;
//    }

//    public void AddPermissionListener(PermissionsListener listener) {
//        permissionListeners.add(listener);
//    }

//    public void getOverlayPermissionBtn(View view) {
//        // Check if Android M or higher
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            // Show alert dialog to the user saying a separate permission is needed
//            // Launch the settings activity if the user prefers
//            Intent myIntent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
//            startActivity(myIntent);
//        }
//    }
}