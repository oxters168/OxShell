package com.OxGames.OxShell;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.appspell.shaderview.ShaderView;
import com.appspell.shaderview.gl.params.ShaderParamsBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class HomeActivity extends PagedActivity {
    public ActivityResultLauncher<String> getDir = registerForActivityResult(new ActivityResultContracts.GetContent(),
            uri -> {
                Log.d("ActivityResult", uri.toString());
                SelectDirsView dirsView = (SelectDirsView)allPages.get(ActivityManager.Page.selectDirs);
                dirsView.AddToList(uri.getPath());
//                    super.onActivityResult(requestCode, resultCode, data);
            });

    private long startTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currentPage = ActivityManager.Page.home;
        ActivityManager.SetCurrent(currentPage);
        setContentView(R.layout.activity_home);
        InitViewsTable();

        PackagesCache.PrepareDefaultLaunchIntents(); //Currently duplicates the defaults each time the activity is reloaded
        HomeManager.Init();
        GoTo(ActivityManager.Page.home);

        startTime = System.currentTimeMillis();
        ShaderView shaderView = findViewById(R.id.shaderView);
        shaderView.setUpdateContinuously(true);
        shaderView.setFragmentShader(readAssetAsString(this, "xmb.fsh"));
        shaderView.setVertexShader(readAssetAsString(this, "default.vsh"));
        ShaderParamsBuilder paramsBuilder = new ShaderParamsBuilder();
        paramsBuilder.addFloat("iTime", 0f);
        shaderView.setShaderParams(paramsBuilder.build());
        shaderView.setOnDrawFrameListener(shaderParams -> {
            float secondsElapsed = (System.currentTimeMillis() - startTime) / 1000f;
            shaderParams.updateValue("iTime", secondsElapsed);
            return null;
        });

        //new GLRenderer(this);

        Log.d("HomeActivity", "files-path " + getFilesDir());
        Log.d("HomeActivity", "cache-path " + getCacheDir());
        Log.d("HomeActivity", "external-path " + Environment.getExternalStorageDirectory());
    }

    @Override
    protected void InitViewsTable() {
        allPages.put(ActivityManager.Page.home, findViewById(R.id.home_view));
        allPages.put(ActivityManager.Page.addToHome, findViewById(R.id.add_view));
        allPages.put(ActivityManager.Page.packages, findViewById(R.id.packages_list));
        allPages.put(ActivityManager.Page.assoc, findViewById(R.id.assoc_view));
        allPages.put(ActivityManager.Page.selectDirs, findViewById(R.id.selectdirs_view));
        allPages.put(ActivityManager.Page.intentShortcuts, findViewById(R.id.shortcuts_view));
        allPages.put(ActivityManager.Page.runningApps, findViewById(R.id.runningapps_view));
    }

    private static String readAssetAsString(Context context, String asset) {
        String assetData = null;
        try {
            InputStream inputStream = context.getAssets().open(asset);
            BufferedReader r = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder total = new StringBuilder();
            String line;
            while ((line = r.readLine()) != null) {
                total.append(line).append('\n');
            }
            assetData = total.toString();
        } catch (IOException ex) {
            Log.e("FullscreenNativeActivity", ex.getMessage());
        }
        return assetData;
    }
//    @Override
//    public boolean onKeyDown(int key_code, KeyEvent key_event) {
//        Log.d("HomeActivity", key_code + " " + key_event);
//        return true;
//    }
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