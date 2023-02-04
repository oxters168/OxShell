package com.OxGames.OxShell.Views;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.ListView;

import com.OxGames.OxShell.Helpers.ActivityManager;
import com.OxGames.OxShell.Interfaces.CustomViewListener;
import com.OxGames.OxShell.Data.DetailItem;
import com.OxGames.OxShell.Helpers.SlideTouchHandler;
import com.OxGames.OxShell.Interfaces.InputReceiver;
import com.OxGames.OxShell.Interfaces.Refreshable;
import com.OxGames.OxShell.Interfaces.SlideTouchListener;
import com.OxGames.OxShell.OxShellApp;

import java.util.ArrayList;

public class SlideTouchListView extends ListView implements SlideTouchListener, InputReceiver, Refreshable {
    private SlideTouchHandler slideTouch = new SlideTouchHandler();
    private ArrayList<CustomViewListener> eventListeners = new ArrayList<>();
    int properPosition = 0;

    public SlideTouchListView(Context context) {
        super(context);
        slideTouch.addListener(this);
    }
    public SlideTouchListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        slideTouch.addListener(this);
    }
    public SlideTouchListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        slideTouch.addListener(this);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        int storedPos = properPosition;
        refresh();
        setProperPosition(storedPos);
    }
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return true;
    }
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        slideTouch.update(ev);
        return true;
    }
    @Override
    public void onClick() {
        makeSelection();
    }
    @Override
    public void onSwipeUp() {
        selectPrevItem();
    }
    @Override
    public void onSwipeDown() {
        selectNextItem();
    }
    @Override
    public void onSwipeRight() {

    }
    @Override
    public void onSwipeLeft() {

    }

    public void addListener(CustomViewListener listener) {
        eventListeners.add(listener);
    }
    public void clearListeners() {
        eventListeners.clear();
    }

    @Override
    public boolean receiveKeyEvent(KeyEvent key_event) {
        //Log.d("SlideTouchListView", "Received key event");
        if (key_event.getAction() == KeyEvent.ACTION_DOWN) {
            if (key_event.getKeyCode() == KeyEvent.KEYCODE_BUTTON_A) {
                makeSelection();
                return true;
            }
            if (key_event.getKeyCode() == KeyEvent.KEYCODE_DPAD_DOWN) {
                selectNextItem();
                return true;
            }
            if (key_event.getKeyCode() == KeyEvent.KEYCODE_DPAD_UP) {
                selectPrevItem();
                return true;
            }
        }
        return false;
    }

    private void highlightSelection() {
        for (int i = 0; i < getCount(); i++)
            ((DetailItem)getItemAtPosition(i)).isSelected = (i == properPosition);
        invalidateViews();
    }
    public void selectNextItem() {
        //Log.d("SlideTouchListView", "Selecting next item");
        int total = getCount();
        int nextIndex = properPosition + 1;
        if (nextIndex >= total)
            nextIndex = total - 1;
        setProperPosition(nextIndex);
    }
    public void selectPrevItem() {
        //Log.d("SlideTouchListView", "Selecting prev item");
        int prevIndex = properPosition - 1;
        if (prevIndex < 0)
            prevIndex = 0;
        setProperPosition(prevIndex);
    }
    public void makeSelection() {
        for (CustomViewListener el : eventListeners)
            el.onMakeSelection(properPosition);
    }
    public void setProperPosition(int pos) {
//        Log.d("Explorer", "Setting position to " + pos);
        properPosition = pos; //Probably should clamp properPosition here
        highlightSelection();
        //DisplayMetrics displayMetrics = ActivityManager.getCurrentActivity().getDisplayMetrics();
        setSelectionFromTop(pos, (int)(OxShellApp.getDisplayHeight() * 0.5));

    }
    @Override
    public void refresh() {
        setProperPosition(properPosition);
    }
}