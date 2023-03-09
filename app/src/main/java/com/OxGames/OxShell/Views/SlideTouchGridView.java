package com.OxGames.OxShell.Views;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.GridView;

import com.OxGames.OxShell.Helpers.ActivityManager;
import com.OxGames.OxShell.Data.GridItem;
import com.OxGames.OxShell.Helpers.SlideTouchHandler;
import com.OxGames.OxShell.Interfaces.InputReceiver;
import com.OxGames.OxShell.Interfaces.Refreshable;
import com.OxGames.OxShell.Interfaces.SlideTouchListener;
import com.OxGames.OxShell.OxShellApp;

public class SlideTouchGridView extends GridView implements SlideTouchListener, InputReceiver, Refreshable {
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

    @Override
    public boolean receiveKeyEvent(KeyEvent key_event) {
        //Log.d("SlideTouchGridView", key_event.toString());
        if (key_event.getAction() == KeyEvent.ACTION_UP) {
            if (key_event.getKeyCode() == KeyEvent.KEYCODE_BUTTON_A) {
                makeSelection();
                return true;
            }
//            if (key_event.getKeyCode() == KeyEvent.KEYCODE_BUTTON_X) {
//                deleteSelection();
//                return true;
//            }
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
    public void highlightSelection() {
        for (int i = 0; i < getCount(); i++)
            ((GridItem)getItemAtPosition(i)).isSelected = i == properPosition;
        invalidateViews();
    }
    public void selectLowerItem() {
        int columns = getNumColumns();
        int total = getCount();
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
        int total = getCount();
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
        highlightSelection();
        //DisplayMetrics displayMetrics = ActivityManager.getCurrentActivity().getDisplayMetrics();
        setSelectionFromTop(pos, (int)(OxShellApp.getDisplayHeight() * 0.5));
    }
    public void makeSelection() {
    }
    public void deleteSelection() {
    }

    @Override
    public void refresh() {
        setProperPosition(properPosition);
    }
}
