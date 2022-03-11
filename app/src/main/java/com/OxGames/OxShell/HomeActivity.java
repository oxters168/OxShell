package com.OxGames.OxShell;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;

import java.util.Hashtable;

public class HomeActivity extends PagedActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        InitViewsTable();

        PackagesCache.PrepareDefaultLaunchIntents(); //Currently duplicates the defaults each time the activity is reloaded
        HomeManager.Init();
        ActivityManager.GoTo(ActivityManager.Page.home);
    }
    @Override
    protected void InitViewsTable() {
        allPages.put(ActivityManager.Page.home, findViewById(R.id.home_view));
        allPages.put(ActivityManager.Page.addToHome, findViewById(R.id.add_view));
        allPages.put(ActivityManager.Page.packages, findViewById(R.id.packages_list));
        allPages.put(ActivityManager.Page.assoc, findViewById(R.id.assoc_view));
        allPages.put(ActivityManager.Page.selectdirs, findViewById(R.id.selectdirs_view));
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("ActivityResult", requestCode + " " + resultCode + " " + data);
        SelectDirsView dirsView = (SelectDirsView)allPages.get(ActivityManager.Page.selectdirs);
        if (requestCode == dirsView.PICKFILE_REQUEST_CODE) {
            String folderPath = data.getDataString();
            dirsView.AddToList(folderPath);
            //TODO
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    @Override
    public boolean onKeyDown(int key_code, KeyEvent key_event) {
//        Log.d("HomeActivity", key_code + " " + key_event);
        return true;
    }
    public void RefreshHome() {
        ((HomeView)allPages.get(ActivityManager.Page.home)).Refresh();
    }

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