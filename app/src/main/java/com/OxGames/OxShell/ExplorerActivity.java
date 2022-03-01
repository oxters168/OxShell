package com.OxGames.OxShell;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;

import java.util.ArrayList;
import java.util.List;

public class ExplorerActivity extends Activity {
    private static ExplorerActivity instance;
    private ArrayList<PermissionsListener> permissionListeners = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instance = this;

        setContentView(R.layout.activity_explorer);

        findViewById(R.id.explorer_list).requestFocusFromTouch(); //Makes onKeyDown work in ExplorerView without the need of pressing on it
    }

    @Override
    public boolean onKeyDown(int key_code, KeyEvent key_event) {
        Log.d("ExplorerActivity", key_code + " " + key_event);

        return true;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        for (PermissionsListener pl : permissionListeners) {
            pl.onPermissionResponse(requestCode, permissions, grantResults);
        }
    }
    public void AddPermissionListener(PermissionsListener listener) {
        permissionListeners.add(listener);
    }

    public static ExplorerActivity GetInstance() {
        return instance;
    }
}