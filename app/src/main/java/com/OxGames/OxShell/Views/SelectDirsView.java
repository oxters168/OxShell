package com.OxGames.OxShell.Views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;

import androidx.core.content.ContextCompat;

import com.OxGames.OxShell.Helpers.ActivityManager;
import com.OxGames.OxShell.Adapters.DetailAdapter;
import com.OxGames.OxShell.Data.DetailItem;
import com.OxGames.OxShell.Interfaces.DirsCarrier;
import com.OxGames.OxShell.Interfaces.DirsViewListener;
import com.OxGames.OxShell.HomeActivity;
import com.OxGames.OxShell.R;

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

//    @Override
//    public boolean onKeyDown(int key_code, KeyEvent key_event) {
////        Log.d("ExplorerView", key_code + " " + key_event);
//        if (key_code == KeyEvent.KEYCODE_BUTTON_B || key_code == KeyEvent.KEYCODE_BACK) {
//            CancelAndReturn();
//            return false;
//        }
//        if (key_code == KeyEvent.KEYCODE_BUTTON_X) {
//            RemoveSelection();
//            return false;
//        }
//
//        return super.onKeyDown(key_code, key_event);
//    }
    @Override
    public boolean receiveKeyEvent(KeyEvent key_event) {
    //        Log.d("ExplorerView", key_code + " " + key_event);
        if (key_event.getAction() == KeyEvent.ACTION_DOWN) {
            if (key_event.getKeyCode() == KeyEvent.KEYCODE_BUTTON_B || key_event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
                cancelAndReturn();
                return true;
            }
            if (key_event.getKeyCode() == KeyEvent.KEYCODE_BUTTON_X) {
                removeSelection();
                return true;
            }
        }

        return super.receiveKeyEvent(key_event);
    }

    @Override
    public void makeSelection() {
//        IntentLaunchData selectedItem = (IntentLaunchData)((DetailItem)getItemAtPosition(properPosition)).obj;
        Object obj = ((DetailItem)getItemAtPosition(properPosition)).obj;
        if (obj instanceof Integer && ((int)obj) == 0) {
//            ActivityManager.GoTo(ActivityManager.Page.chooser);
//            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
//            intent.setType("file/*");
//            startActivityForResult(ActivityManager.GetCurrentActivity(), intent, PICKFILE_REQUEST_CODE, null);
            ((HomeActivity)ActivityManager.getInstance(HomeActivity.class)).getDir.launch("file/*");
        } else if (obj instanceof Integer && ((int)obj) == 1) {
            cancelAndReturn();
        } else if (obj instanceof Integer && ((int)obj) == 2) {
            //Done
            sendResult(RESULT_DONE);
            goBack();
        }
    }

    public static void addResultListener(DirsViewListener listener) {
        instance.resultListeners.add(listener);
    }
    private static void sendResult(int resultCode) {
        for (DirsViewListener rl : instance.resultListeners) {
            rl.onDirsResult(resultCode, instance.currentMod);
        }
    }
    private void goBack() {
        resultListeners.clear();
        currentMod = null;
        ActivityManager.goTo(returnPage);
        refresh();
    }

    public static void setReturnPage(ActivityManager.Page _returnPage) {
        instance.returnPage = _returnPage;
    }
    public static void setDirsCarrier(DirsCarrier carrier) {
        instance.currentMod = carrier;
        instance.refresh();
    }
    private void cancelAndReturn() {
        currentMod.clearDirsList();
        sendResult(RESULT_CANCELLED);
        goBack();
    }
    public void addToList(String dir) {
        currentMod.addToDirsList(dir);
        refresh();
    }
    public void removeSelection() {
        Object obj = ((DetailItem)getItemAtPosition(properPosition)).obj;
        if (obj instanceof String) {
            currentMod.removeFromDirsList((String)obj);
            refresh();
        }
    }
    @Override
    public void refresh() {
        ArrayList<DetailItem> addDirsItems = new ArrayList<>();
        if (currentMod != null) {
            String[] dirs = currentMod.getDirsList();
            for (int i = 0; i < dirs.length; i++) {
                addDirsItems.add(new DetailItem(null, dirs[i], null, dirs[i]));
            }
        }
        addDirsItems.add(new DetailItem(ContextCompat.getDrawable(ActivityManager.getCurrentActivity(), R.drawable.ic_baseline_add_circle_outline_24), "Choose directory", null, 0));
        addDirsItems.add(new DetailItem(ContextCompat.getDrawable(ActivityManager.getCurrentActivity(), R.drawable.ic_baseline_cancel_24), "Cancel", null, 1));
        addDirsItems.add(new DetailItem(ContextCompat.getDrawable(ActivityManager.getCurrentActivity(), R.drawable.ic_baseline_check_24), "Done", null, 2));
        DetailAdapter addAdapter = new DetailAdapter(getContext(), addDirsItems);
        setAdapter(addAdapter);
        super.refresh();
    }
}
