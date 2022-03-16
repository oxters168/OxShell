package com.OxGames.OxShell;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;

import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class SelectDirsView extends SlideTouchListView {
//    private ActivityManager.Page CURRENT_PAGE = ActivityManager.Page.selectdirs;
//    public static final int PICKFILE_REQUEST_CODE = 100;
//    private ArrayList<String> dirs;
    public static final int RESULT_CANCELLED = 0, RESULT_DONE = 1;
    private static SelectDirsView instance;
    private DirsCarrier currentMod;
    private ActivityManager.Page returnPage = ActivityManager.Page.home;
    private List<DirsViewListener> resultListeners = new ArrayList<>();

    public SelectDirsView(Context context) {
        super(context);
//        ClearList();
        instance = this;
    }
    public SelectDirsView(Context context, AttributeSet attrs) {
        super(context, attrs);
//        ClearList();
        instance = this;
    }
    public SelectDirsView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
//        ClearList();
        instance = this;
    }

    @Override
    public boolean onKeyDown(int key_code, KeyEvent key_event) {
//        Log.d("ExplorerView", key_code + " " + key_event);
        if (key_code == KeyEvent.KEYCODE_BUTTON_B || key_code == KeyEvent.KEYCODE_BACK) {
            CancelAndReturn();
            return false;
        }
        if (key_code == KeyEvent.KEYCODE_BUTTON_X) {
            RemoveSelection();
            return false;
        }

        return super.onKeyDown(key_code, key_event);
    }

    @Override
    public void MakeSelection() {
//        IntentLaunchData selectedItem = (IntentLaunchData)((DetailItem)getItemAtPosition(properPosition)).obj;
        Object obj = ((DetailItem)getItemAtPosition(properPosition)).obj;
        if (obj instanceof Integer && ((int)obj) == 0) {
//            ActivityManager.GoTo(ActivityManager.Page.chooser);
//            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
//            intent.setType("file/*");
//            startActivityForResult(ActivityManager.GetCurrentActivity(), intent, PICKFILE_REQUEST_CODE, null);
            ((HomeActivity)ActivityManager.GetInstance(HomeActivity.class)).getDir.launch("file/*");
        } else if (obj instanceof Integer && ((int)obj) == 1) {
            CancelAndReturn();
        } else if (obj instanceof Integer && ((int)obj) == 2) {
            //Done
            SendResult(RESULT_DONE);
            Return();
        }
    }

    public static void AddResultListener(DirsViewListener listener) {
        instance.resultListeners.add(listener);
    }
    private static void SendResult(int resultCode) {
        for (DirsViewListener rl : instance.resultListeners) {
            rl.onDirsResult(resultCode, instance.currentMod);
        }
    }
    private void Return() {
        resultListeners.clear();
        currentMod = null;
        ActivityManager.GoTo(returnPage);
        Refresh();
    }

    public static void SetReturnPage(ActivityManager.Page _returnPage) {
        instance.returnPage = _returnPage;
    }
    public static void SetDirsCarrier(DirsCarrier carrier) {
        instance.currentMod = carrier;
        instance.Refresh();
    }
    private void CancelAndReturn() {
        currentMod.ClearDirsList();
        SendResult(RESULT_CANCELLED);
        Return();
    }
    public void AddToList(String dir) {
        currentMod.AddToDirsList(dir);
        Refresh();
    }
    public void RemoveSelection() {
        Object obj = ((DetailItem)getItemAtPosition(properPosition)).obj;
        if (obj instanceof String) {
            currentMod.RemoveFromDirsList((String)obj);
            Refresh();
        }
    }
    @Override
    public void Refresh() {
        ArrayList<DetailItem> addDirsItems = new ArrayList<>();
        if (currentMod != null) {
            String[] dirs = currentMod.GetDirsList();
            for (int i = 0; i < dirs.length; i++) {
                addDirsItems.add(new DetailItem(null, dirs[i], null, dirs[i]));
            }
        }
        addDirsItems.add(new DetailItem(ContextCompat.getDrawable(ActivityManager.GetCurrentActivity(), R.drawable.ic_baseline_add_circle_outline_24), "Choose directory", null, 0));
        addDirsItems.add(new DetailItem(ContextCompat.getDrawable(ActivityManager.GetCurrentActivity(), R.drawable.ic_baseline_cancel_24), "Cancel", null, 1));
        addDirsItems.add(new DetailItem(ContextCompat.getDrawable(ActivityManager.GetCurrentActivity(), R.drawable.ic_baseline_check_24), "Done", null, 2));
        DetailAdapter addAdapter = new DetailAdapter(getContext(), addDirsItems);
        setAdapter(addAdapter);
    }
}
