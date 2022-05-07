package com.OxGames.OxShell;

import android.os.Bundle;

import java.util.Hashtable;

public class ExplorerActivity extends PagedActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currentPage = ActivityManager.Page.explorer;
        ActivityManager.setCurrent(currentPage);
        setContentView(R.layout.activity_explorer);
        initViewsTable();
    }

    @Override
    protected void initViewsTable() {
        allPages = new Hashtable<>();
        allPages.put(ActivityManager.Page.explorer, findViewById(R.id.explorer_list));
    }
}