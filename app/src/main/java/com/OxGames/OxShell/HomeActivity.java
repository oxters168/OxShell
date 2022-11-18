package com.OxGames.OxShell;

import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import java.io.File;

public class HomeActivity extends PagedActivity {
    public ActivityResultLauncher<String> getDir = registerForActivityResult(new ActivityResultContracts.GetContent(),
            uri -> {
                Log.d("ActivityResult", uri.toString());
                SelectDirsView dirsView = (SelectDirsView)allPages.get(ActivityManager.Page.selectDirs);
                //dirsView.addToList(ProviderHelpers.getRealPathFromURI(this, uri));
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

        HomeManager.init();
        goTo(ActivityManager.Page.home);
        showAnnoyingDialog();

        Log.d("HomeActivity", "external-path " + Environment.getExternalStorageDirectory());
        Log.d("HomeActivity", "external-files-path " + getExternalFilesDir(null));
        Log.d("HomeActivity", "cache-path " + getCacheDir());
        Log.d("HomeActivity", "external-cache-path " + getExternalCacheDir());
        Log.d("HomeActivity", "files-path " + getFilesDir());
        File[] extMediaDirs = getExternalMediaDirs();
        for (int i = 0; i < extMediaDirs.length; i++)
            Log.d("HomeActivity", "external-media-path_" + i + ": " + extMediaDirs[i]);

        Log.d("HomeActivity", ShellCommander.run("mount"));
    }

    @Override
    protected void initViewsTable() {
        allPages.put(ActivityManager.Page.home, findViewById(R.id.home_view));
        allPages.put(ActivityManager.Page.settings, findViewById(R.id.settings_view));
        allPages.put(ActivityManager.Page.customizeHome, findViewById(R.id.customize_home_view));
        allPages.put(ActivityManager.Page.pkgList, findViewById(R.id.packages_list));
        allPages.put(ActivityManager.Page.assocList, findViewById(R.id.assoc_list_view));
        allPages.put(ActivityManager.Page.selectDirs, findViewById(R.id.selectdirs_view));
        allPages.put(ActivityManager.Page.intentShortcuts, findViewById(R.id.shortcuts_view));
    }

    private void showAnnoyingDialog() {
        //TODO: Add dialog for 'free' version to ask for support
        if (!BuildConfig.GOLD) {
            Log.d("HomeActivity", "Not running in gold");
        } else {
            Log.d("HomeActivity", "Running in gold");
        }
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