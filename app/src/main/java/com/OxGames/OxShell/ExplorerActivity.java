package com.OxGames.OxShell;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import java.util.Hashtable;

public class ExplorerActivity extends PagedActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
//        Log.d("ExplorerActivity", "Pre Create");
        super.onCreate(savedInstanceState);
        PagedActivity self = GetInstance();
//        Log.d("ExplorerActivity", "Adding " + self.getClass() + " into activity manager");
//        ActivityManager.AddActivity(self, new ActivityManager.Page[] { ActivityManager.Page.explorer });
//        Log.d("ExplorerActivity", "Added Activity");
        setContentView(R.layout.activity_explorer);
//        Log.d("ExplorerActivity", "Set Content View");
        InitViewsTable();
//        Log.d("ExplorerActivity", "Init View Tables");
    }

    @Override
    protected void InitViewsTable() {
        allPages = new Hashtable<>();
        allPages.put(ActivityManager.Page.explorer, findViewById(R.id.explorer_list));
    }
}