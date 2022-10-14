package com.OxGames.OxShell;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.BaseInputConnection;
import android.widget.ListView;

import java.util.ArrayList;

public class SlideTouchListView extends ListView implements SlideTouchListener {
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
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        slideTouch.checkForEvents();
        highlightSelection();
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
    public void onRequestInvalidate() {
        invalidate();
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

    public boolean receiveKeyEvent(KeyEvent key_event) {
        //Log.d("SlideTouchListView", key_event.toString());
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
//            if (key_event.getKeyCode() == KeyEvent.KEYCODE_BUTTON_Y) {
//                //Log.d("SlideTouchListView", "Attempting to bring up app switcher");
//                sendKeyEvent(this, KeyEvent.KEYCODE_APP_SWITCH, KeyEvent.ACTION_DOWN);
//                //sendKeyEvent(this, KeyEvent.KEYCODE_APP_SWITCH, KeyEvent.ACTION_UP, 0);
//                return true;
//            }
        }
//        if (key_event.getAction() == KeyEvent.ACTION_UP) {
//            if (key_event.getKeyCode() == KeyEvent.KEYCODE_BUTTON_Y) {
//                Log.d("SlideTouchListView", "Attempting to bring up app switcher");
//                //sendKeyEvent(this, KeyEvent.KEYCODE_APP_SWITCH, KeyEvent.ACTION_DOWN, 0);
//                sendKeyEvent(this, KeyEvent.KEYCODE_APP_SWITCH, KeyEvent.ACTION_UP);
//                return true;
//            }
//        }
        return false;
    }
//    private void sendKeyEvent(View targetView, int keyeventcode, int action) {
//        //Reference: https://developer.android.com/reference/android/view/inputmethod/InputConnection#sendKeyEvent(android.view.KeyEvent)
//        //& https://stackoverflow.com/questions/13026505/how-can-i-send-key-events-in-android
//        //BaseInputConnection mInputConnection = new BaseInputConnection(targetView, true);
//        long eventTime = SystemClock.uptimeMillis();
//        //mInputConnection.sendKeyEvent(new KeyEvent(eventTime, eventTime, action, keyeventcode, 0));
//        dispatchKeyEvent(new KeyEvent(eventTime, eventTime, action, keyeventcode, 0));
//    }

    private void highlightSelection() {
        for (int i = 0; i < getCount(); i++) {
            View view = ((DetailItem)getItemAtPosition(i)).view;
            if (view != null)
                view.setBackgroundColor((i == properPosition) ? Color.parseColor("#33EAF0CE") : Color.parseColor("#00000000")); //TODO implement color theme that can take custom theme from file
        }
    }
    public void selectNextItem() {
        int total = getCount();
        int nextIndex = properPosition + 1;
        if (nextIndex >= total)
            nextIndex = total - 1;
        setProperPosition(nextIndex);
    }
    public void selectPrevItem() {
        int prevIndex = properPosition - 1;
        if (prevIndex < 0)
            prevIndex = 0;
        setProperPosition(prevIndex);
    }
    public void makeSelection() {
        for (CustomViewListener el : eventListeners)
            el.onMakeSelection(properPosition);
        //ExplorerItem clickedItem = (ExplorerItem)getItemAtPosition(properPosition);
    }
    public void setProperPosition(int pos) {
//        Log.d("Explorer", "Setting position to " + pos);
        properPosition = pos; //Probably should clamp properPosition here
        DisplayMetrics displayMetrics = ActivityManager.getCurrentActivity().getDisplayMetrics();
        setSelectionFromTop(pos, (int)(displayMetrics.heightPixels * 0.5));
    }
    public void refresh() {
        setProperPosition(properPosition);
    }
}