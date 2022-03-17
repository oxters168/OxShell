package com.OxGames.OxShell;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ListView;

public class SlideTouchListView extends ListView implements SlideTouchListener {
    private SlideTouchHandler slideTouch = new SlideTouchHandler();
    int properPosition = 0;

    public SlideTouchListView(Context context) {
        super(context);
        slideTouch.AddListener(this);
    }
    public SlideTouchListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        slideTouch.AddListener(this);
    }
    public SlideTouchListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        slideTouch.AddListener(this);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        slideTouch.CheckForEvents();
        HighlightSelection();
    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        int storedPos = properPosition;
        Refresh();
        SetProperPosition(storedPos);
    }
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return true;
    }
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        slideTouch.Update(ev);
        return true;
    }
    @Override
    public void onRequestInvalidate() {
        invalidate();
    }
    @Override
    public void onClick() {
        MakeSelection();
    }
    @Override
    public void onSwipeUp() {
        SelectPrevItem();
    }
    @Override
    public void onSwipeDown() {
        SelectNextItem();
    }
    @Override
    public void onSwipeRight() {

    }
    @Override
    public void onSwipeLeft() {

    }
//    @Override
//    public boolean onKeyDown(int key_code, KeyEvent key_event) {
//        if (key_code == KeyEvent.KEYCODE_BUTTON_A) {
//            MakeSelection();
//            return false;
//        }
//        if (key_code == KeyEvent.KEYCODE_DPAD_DOWN) {
//            SelectNextItem();
//            return false;
//        }
//        if (key_code == KeyEvent.KEYCODE_DPAD_UP) {
//            SelectPrevItem();
//            return false;
//        }
//        return true;
//    }
//    @Override
//    public boolean onKeyDown(int key_code, KeyEvent key_event) {
//        Log.d("SlideTouchListView", key_code + " " + key_event);
//        return ReceiveKeyDown(key_code, key_event);
//    }
//    @Override
//    public boolean onKeyUp(int key_code, KeyEvent key_event) {
//        Log.d("SlideTouchListView", key_code + " " + key_event);
//        return ReceiveKeyUp(key_code, key_event);
//    }
    public boolean ReceiveKeyEvent(KeyEvent key_event) {
        if (key_event.getAction() == KeyEvent.ACTION_DOWN) {
            if (key_event.getKeyCode() == KeyEvent.KEYCODE_BUTTON_A) {
                MakeSelection();
                return true;
            }
            if (key_event.getKeyCode() == KeyEvent.KEYCODE_DPAD_DOWN) {
                SelectNextItem();
                return true;
            }
            if (key_event.getKeyCode() == KeyEvent.KEYCODE_DPAD_UP) {
                SelectPrevItem();
                return true;
            }
        }
        return false;
    }
//    public boolean ReceiveKeyUp(int key_code, KeyEvent key_event) {
//        return true;
//    }

    private void HighlightSelection() {
        for (int i = 0; i < getCount(); i++) {
            View view = ((DetailItem)getItemAtPosition(i)).view;
            if (view != null)
                view.setBackgroundResource((i == properPosition) ? R.color.scheme1 : R.color.light_blue_400);
        }
    }
    public void SelectNextItem() {
        int total = getCount();
        int nextIndex = properPosition + 1;
        if (nextIndex >= total)
            nextIndex = total - 1;
        SetProperPosition(nextIndex);
    }
    public void SelectPrevItem() {
        int prevIndex = properPosition - 1;
        if (prevIndex < 0)
            prevIndex = 0;
        SetProperPosition(prevIndex);
    }
    public void MakeSelection() {
        //ExplorerItem clickedItem = (ExplorerItem)getItemAtPosition(properPosition);
    }
    public void SetProperPosition(int pos) {
//        Log.d("Explorer", "Setting position to " + pos);
        properPosition = pos;
        DisplayMetrics displayMetrics = ActivityManager.GetCurrentActivity().GetDisplayMetrics();
        setSelectionFromTop(pos, (int)(displayMetrics.heightPixels * 0.5));
    }
    public void Refresh() {

    }
}