package com.OxGames.OxShell;

import static androidx.core.app.ActivityCompat.startActivityForResult;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.view.KeyEvent;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;

public class SelectDirsView extends SlideTouchListView {
    public static final int PICKFILE_REQUEST_CODE = 100;
    private ArrayList<String> dirs;

    public SelectDirsView(Context context) {
        super(context);
        ClearList();
    }
    public SelectDirsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        ClearList();
    }
    public SelectDirsView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        ClearList();
    }

    @Override
    public boolean onKeyDown(int key_code, KeyEvent key_event) {
//        Log.d("ExplorerView", key_code + " " + key_event);
        if (key_code == KeyEvent.KEYCODE_BUTTON_B || key_code == KeyEvent.KEYCODE_BACK) {
            CancelAndReturn();
            return false;
        }

        return super.onKeyDown(key_code, key_event);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        int storedPos = properPosition;
        Refresh();
        SetProperPosition(storedPos);
    }

    @Override
    public void MakeSelection() {
//        IntentLaunchData selectedItem = (IntentLaunchData)((DetailItem)getItemAtPosition(properPosition)).obj;
        Object obj = ((DetailItem)getItemAtPosition(properPosition)).obj;
        if (obj instanceof Integer && ((int)obj) == 0) {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("file/*");
            startActivityForResult(ActivityManager.GetCurrentActivity(), intent, PICKFILE_REQUEST_CODE, null);
        } else if (obj instanceof Integer && ((int)obj) == 1) {
            CancelAndReturn();
        } else if (obj instanceof Integer && ((int)obj) == 2) {
            //Done
        }
    }

    private void CancelAndReturn() {
        ActivityManager.GoTo(ActivityManager.Page.assoc);
        ClearList();
    }
    public void ClearList() {
        dirs = new ArrayList<>();
        Refresh();
    }
    public void AddToList(String dir) {
        dirs.add(dir);
        Refresh();
    }
    @Override
    public void Refresh() {
        ArrayList<DetailItem> addDirsItems = new ArrayList<>();
        for (int i = 0; i < dirs.size(); i++) {
            addDirsItems.add(new DetailItem(null, dirs.get(i), null, dirs.get(i)));
        }
        addDirsItems.add(new DetailItem(ContextCompat.getDrawable(ActivityManager.GetCurrentActivity(), R.drawable.ic_baseline_add_circle_outline_24), "Choose directory", null, 0));
        addDirsItems.add(new DetailItem(ContextCompat.getDrawable(ActivityManager.GetCurrentActivity(), R.drawable.ic_baseline_cancel_24), "Cancel", null, 1));
        addDirsItems.add(new DetailItem(ContextCompat.getDrawable(ActivityManager.GetCurrentActivity(), R.drawable.ic_baseline_check_24), "Done", null, 2));
        DetailAdapter addAdapter = new DetailAdapter(getContext(), addDirsItems);
        setAdapter(addAdapter);
    }
}
