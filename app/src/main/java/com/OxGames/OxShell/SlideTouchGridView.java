package com.OxGames.OxShell;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.GridView;

public class SlideTouchGridView extends GridView implements SlideTouchListener {
    SlideTouchHandler slideTouch = new SlideTouchHandler();
    protected int properPosition = 0;

    public SlideTouchGridView(Context context) {
        super(context);
        slideTouch.addListener(this);
        refresh();
    }
    public SlideTouchGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
        slideTouch.addListener(this);
        refresh();
    }
    public SlideTouchGridView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        slideTouch.addListener(this);
        refresh();
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
//    @Override
//    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        super.onMeasure(widthMeasureSpec, widthMeasureSpec); // This is the key that will make the height equivalent to its width
//    }

    @Override
    public void onRequestInvalidate() {
        invalidate();
    }
    @Override
    public void onClick() {
        makeSelection();
    }
    @Override
    public void onSwipeDown() {
        selectLowerItem();
    }
    @Override
    public void onSwipeLeft() {
        selectLeftItem();
    }
    @Override
    public void onSwipeRight() {
        selectRightItem();
    }
    @Override
    public void onSwipeUp() {
        selectUpperItem();
    }

    public boolean receiveKeyEvent(KeyEvent key_event) {
        //Log.d("SlideTouchGridView", key_event.toString());
        if (key_event.getAction() == KeyEvent.ACTION_DOWN) {
            if (key_event.getKeyCode() == KeyEvent.KEYCODE_BUTTON_A) {
                makeSelection();
                return true;
            }
            if (key_event.getKeyCode() == KeyEvent.KEYCODE_BUTTON_X) {
                deleteSelection();
                return true;
            }
            if (key_event.getKeyCode() == KeyEvent.KEYCODE_DPAD_DOWN) {
                selectLowerItem();
                return true;
            }
            if (key_event.getKeyCode() == KeyEvent.KEYCODE_DPAD_UP) {
                selectUpperItem();
                return true;
            }
            if (key_event.getKeyCode() == KeyEvent.KEYCODE_DPAD_LEFT) {
                selectLeftItem();
                return true;
            }
            if (key_event.getKeyCode() == KeyEvent.KEYCODE_DPAD_RIGHT) {
                selectRightItem();
                return true;
            }
        }

        //Block out default back events
        if (key_event.getKeyCode() == KeyEvent.KEYCODE_BACK || key_event.getKeyCode() == KeyEvent.KEYCODE_BUTTON_B)
            return true;

        return false;
    }
//    public boolean ReceiveKeyUp(int key_code, KeyEvent key_event) {
//        return true;
//    }
    public void highlightSelection() {
        for (int i = 0; i < getChildCount(); i++) {
//            View view = ((HomeItem)getItemAtPosition(i)).view;
            View view = getChildAt(i);
//            Log.d("HomeView", i + " " + view);

            if (view != null) {
                int bgColor = (i == properPosition) ? Color.parseColor("#33EAF0CE") : Color.parseColor("#00000000"); //TODO implement color theme that can take custom theme from file
                view.setBackgroundColor(bgColor);
            }
        }
    }
    public void selectLowerItem() {
        int columns = getNumColumns();
        int total = getChildCount();
        int nextIndex = properPosition + columns;
        if (nextIndex >= total)
            nextIndex = properPosition;
//        Log.d("Home", "Down " + properPosition + " => " + nextIndex);
        setProperPosition(nextIndex);
    }
    public void selectUpperItem() {
        int columns = getNumColumns();
        int prevIndex = properPosition - columns;
        if (prevIndex < 0)
            prevIndex = properPosition;
//        Log.d("Home", "Up " + properPosition + " => " + prevIndex);
        setProperPosition(prevIndex);
    }
    public void selectRightItem() {
        int columns = getNumColumns();
        int total = getChildCount();
        int nextIndex = properPosition + 1;
        if (nextIndex >= total)
            nextIndex = total - 1;
        if (nextIndex % columns == 0)
            nextIndex = properPosition;
//        Log.d("Home", "Right " + properPosition + " => " + nextIndex);
        setProperPosition(nextIndex);
    }
    public void selectLeftItem() {
        int columns = getNumColumns();
        int prevIndex = properPosition - 1;
        if (prevIndex < 0)
            prevIndex = 0;
        if (prevIndex % columns == columns - 1)
            prevIndex = properPosition;
//        Log.d("Home", "Left " + properPosition + " => " + prevIndex);
        setProperPosition(prevIndex);
    }
    public void setProperPosition(int pos) {
        properPosition = pos;
        DisplayMetrics displayMetrics = ActivityManager.getCurrentActivity().getDisplayMetrics();
        setSelectionFromTop(pos, displayMetrics != null ? (int)(displayMetrics.heightPixels * 0.5) : 0);
    }
    public void makeSelection() {
    }
    public void deleteSelection() {
    }

    public void refresh() {
    }
}
