package com.OxGames.OxShell;

import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

public class HomeActivity extends PagedActivity {
    public ActivityResultLauncher<String> getDir = registerForActivityResult(new ActivityResultContracts.GetContent(),
            uri -> {
                Log.d("ActivityResult", uri.toString());
                SelectDirsView dirsView = (SelectDirsView)allPages.get(ActivityManager.Page.selectDirs);
                dirsView.addToList(uri.getPath());
//                    super.onActivityResult(requestCode, resultCode, data);
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currentPage = ActivityManager.Page.home;
        ActivityManager.setCurrent(currentPage);
        setContentView(R.layout.activity_home);
        initViewsTable();

        PackagesCache.prepareDefaultLaunchIntents(); //Currently duplicates the defaults each time the activity is reloaded
        HomeManager.init();
        goTo(ActivityManager.Page.home);

        Log.d("HomeActivity", "files-path " + getFilesDir());
        Log.d("HomeActivity", "cache-path " + getCacheDir());
        Log.d("HomeActivity", "external-path " + Environment.getExternalStorageDirectory());
    }

    @Override
    protected void initViewsTable() {
        allPages.put(ActivityManager.Page.home, findViewById(R.id.home_view));
        allPages.put(ActivityManager.Page.addToHome, findViewById(R.id.add_view));
        allPages.put(ActivityManager.Page.packages, findViewById(R.id.packages_list));
        allPages.put(ActivityManager.Page.assoc, findViewById(R.id.assoc_view));
        allPages.put(ActivityManager.Page.selectDirs, findViewById(R.id.selectdirs_view));
        allPages.put(ActivityManager.Page.intentShortcuts, findViewById(R.id.shortcuts_view));
        allPages.put(ActivityManager.Page.runningApps, findViewById(R.id.runningapps_view));
    }

    public void refreshHome() {
        ((HomeView)allPages.get(ActivityManager.Page.home)).refresh();
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