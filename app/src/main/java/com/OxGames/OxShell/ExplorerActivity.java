package com.OxGames.OxShell;

import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.OxGames.OxShell.Helpers.ActivityManager;
import com.OxGames.OxShell.Views.ExplorerView;

import java.util.Hashtable;

public class ExplorerActivity extends PagedActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Log.d("ExplorerActivity", "onCreate");
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

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        setMarginsFor(R.id.parent_layout);
    }
}