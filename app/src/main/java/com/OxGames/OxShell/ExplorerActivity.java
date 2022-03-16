package com.OxGames.OxShell;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.KeyEvent;

import java.io.File;
import java.util.Hashtable;

public class ExplorerActivity extends PagedActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currentPage = ActivityManager.Page.explorer;
        ActivityManager.SetCurrent(currentPage);
        setContentView(R.layout.activity_explorer);
        InitViewsTable();
    }

    @Override
    protected void InitViewsTable() {
        allPages = new Hashtable<>();
        allPages.put(ActivityManager.Page.explorer, findViewById(R.id.explorer_list));
    }

    @Override
    public boolean onKeyDown(int key_code, KeyEvent key_event) {
        Log.d("ExplorerActivity", key_code + " " + key_event);
//        if (key_code == KeyEvent.KEYCODE_BUTTON_B) {
//            keyDownStart = SystemClock.uptimeMillis();
//            return false;
//        }
//        if (key_code == KeyEvent.KEYCODE_BACK && key_event.getScanCode() == 0) {
//            if (ActivityManager.GetCurrent() != ActivityManager.Page.chooser) {
//                touchDownStart = SystemClock.uptimeMillis();
//                return false;
//            }
//        }

        return super.onKeyDown(key_code, key_event);
    }
}