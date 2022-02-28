package com.OxGames.OxShell;

import android.app.Activity;
import android.os.Bundle;

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