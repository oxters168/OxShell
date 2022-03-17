package com.OxGames.OxShell;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.GridView;

import java.util.ArrayList;

public class SlideTouchGridView extends GridView implements SlideTouchListener {
    SlideTouchHandler slideTouch = new SlideTouchHandler();
    protected int properPosition = 0;

    public SlideTouchGridView(Context context) {
        super(context);
        slideTouch.AddListener(this);
        Refresh();
    }
    public SlideTouchGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
        slideTouch.AddListener(this);
        Refresh();
    }
    public SlideTouchGridView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        slideTouch.AddListener(this);
        Refresh();
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
        MakeSelection();
    }
    @Override
    public void onSwipeDown() {
        SelectLowerItem();
    }
    @Override
    public void onSwipeLeft() {
        SelectLeftItem();
    }
    @Override
    public void onSwipeRight() {
        SelectRightItem();
    }
    @Override
    public void onSwipeUp() {
        SelectUpperItem();
    }

    @Override
    public boolean onKeyDown(int key_code, KeyEvent key_event) {
//        Log.d("HomeView", key_code + " " + key_event);
        if (key_code == KeyEvent.KEYCODE_BUTTON_A) {
            MakeSelection();
            return false;
        }
        if (key_code == KeyEvent.KEYCODE_BUTTON_X) {
            DeleteSelection();
            return false;
        }
        if (key_code == KeyEvent.KEYCODE_BUTTON_B) {
            return false;
        }
        if (key_code == KeyEvent.KEYCODE_DPAD_DOWN) {
            SelectLowerItem();
            return false;
        }
        if (key_code == KeyEvent.KEYCODE_DPAD_UP) {
            SelectUpperItem();
            return false;
        }
        if (key_code == KeyEvent.KEYCODE_DPAD_LEFT) {
            SelectLeftItem();
            return false;
        }
        if (key_code == KeyEvent.KEYCODE_DPAD_RIGHT) {
            SelectRightItem();
            return false;
        }
        return true;
    }
    public void HighlightSelection() {
        for (int i = 0; i < getChildCount(); i++) {
//            View view = ((HomeItem)getItemAtPosition(i)).view;
            View view = getChildAt(i);
//            Log.d("HomeView", i + " " + view);

            if (view != null) {
                int bgColor = (i == properPosition) ? R.color.scheme1 : R.color.light_blue_400;
                view.setBackgroundResource(bgColor);
            }
        }
    }
    public void SelectLowerItem() {
        int columns = getNumColumns();
        int total = getChildCount();
        int nextIndex = properPosition + columns;
        if (nextIndex >= total)
            nextIndex = properPosition;
//        Log.d("Home", "Down " + properPosition + " => " + nextIndex);
        SetProperPosition(nextIndex);
    }
    public void SelectUpperItem() {
        int columns = getNumColumns();
        int prevIndex = properPosition - columns;
        if (prevIndex < 0)
            prevIndex = properPosition;
//        Log.d("Home", "Up " + properPosition + " => " + prevIndex);
        SetProperPosition(prevIndex);
    }
    public void SelectRightItem() {
        int columns = getNumColumns();
        int total = getChildCount();
        int nextIndex = properPosition + 1;
        if (nextIndex >= total)
            nextIndex = total - 1;
        if (nextIndex % columns == 0)
            nextIndex = properPosition;
//        Log.d("Home", "Right " + properPosition + " => " + nextIndex);
        SetProperPosition(nextIndex);
    }
    public void SelectLeftItem() {
        int columns = getNumColumns();
        int prevIndex = properPosition - 1;
        if (prevIndex < 0)
            prevIndex = 0;
        if (prevIndex % columns == columns - 1)
            prevIndex = properPosition;
//        Log.d("Home", "Left " + properPosition + " => " + prevIndex);
        SetProperPosition(prevIndex);
    }
    public void SetProperPosition(int pos) {
        properPosition = pos;
        DisplayMetrics displayMetrics = ActivityManager.GetCurrentActivity().GetDisplayMetrics();
        setSelectionFromTop(pos, displayMetrics != null ? (int)(displayMetrics.heightPixels * 0.5) : 0);
    }
    public void MakeSelection() {
    }
    public void DeleteSelection() {
    }

    public void Refresh() {
    }
}
