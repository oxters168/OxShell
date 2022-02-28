package com.OxGames.OxShell;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.GridView;

public class HomeView extends GridView implements SlideTouchListener {
    SlideTouchHandler slideTouch = new SlideTouchHandler();
    int properPosition = 0;

    public HomeView(Context context) {
        super(context);
        slideTouch.AddListener(this);
        RefreshShownItems();
    }
    public HomeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        slideTouch.AddListener(this);
        RefreshShownItems();
    }
    public HomeView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        slideTouch.AddListener(this);
        RefreshShownItems();
    }

    private void OpenExplorer() {
        Intent intent = new Intent(HomeActivity.GetInstance(), ExplorerActivity.class);
        HomeActivity.GetInstance().startActivity(intent);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        slideTouch.CheckForEvents();
        HighlightSelection();
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
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec); // This is the key that will make the height equivalent to its width
    }

    @Override
    public void onRequestInvalidate() {
        invalidate();
    }
    @Override
    public void onClick() {
        OpenExplorer();
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
//        Log.d("Input", key_code + " " + key_event);
        if (key_code == KeyEvent.KEYCODE_BUTTON_A) {
            OpenExplorer();
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
    private void HighlightSelection() {
        for (int i = 0; i < getCount(); i++) {
            View view = ((HomeItem)getItemAtPosition(i)).view;

            if (view != null) {
                int bgColor = (i == properPosition) ? R.color.scheme1 : R.color.light_blue_400;
//                ColorDrawable bg = (ColorDrawable)view.getBackground();
//                int currentColor = -1;
//                if (bg != null)
//                    currentColor = bg.getColor();
                view.setBackgroundResource(bgColor);
//                Log.d("Home", i + " == " + properPosition + " " + currentColor + " => " + bgColor);
            }
        }
    }
    public void SelectLowerItem() {
        int columns = getNumColumns();
        int total = getCount();
        int nextIndex = properPosition + columns;
        if (nextIndex >= total)
            nextIndex = properPosition;
        SetProperPosition(nextIndex);
    }
    public void SelectUpperItem() {
        int columns = getNumColumns();
        int prevIndex = properPosition - columns;
        if (prevIndex < 0)
            prevIndex = properPosition;
        SetProperPosition(prevIndex);
    }
    public void SelectRightItem() {
        int columns = getNumColumns();
        int total = getCount();
        int nextIndex = properPosition + 1;
        if (nextIndex >= total)
            nextIndex = total - 1;
        if (nextIndex % columns == 0)
            nextIndex = properPosition;
        SetProperPosition(nextIndex);
    }
    public void SelectLeftItem() {
        int columns = getNumColumns();
        int prevIndex = properPosition - 1;
        if (prevIndex < 0)
            prevIndex = 0;
        if (prevIndex % columns == columns - 1)
            prevIndex = properPosition;
        SetProperPosition(prevIndex);
    }
    public void SetProperPosition(int pos) {
        properPosition = pos;
        setSelectionFromTop(pos, HomeActivity.displayMetrics != null ? (int)(HomeActivity.displayMetrics.heightPixels * 0.5) : 0);
    }

    public void RefreshShownItems() {
        HomeItem[] homeItems = new HomeItem[] {
                new HomeItem(HomeItem.Type.explorer, "Something"),
                new HomeItem(HomeItem.Type.explorer, "Wong"),
                new HomeItem(HomeItem.Type.explorer, "Wid"),
                new HomeItem(HomeItem.Type.explorer, "Dis"),
                new HomeItem(HomeItem.Type.explorer, "View"),
                new HomeItem(HomeItem.Type.explorer, "Lol"),
                new HomeItem(HomeItem.Type.explorer, "Out"),
                new HomeItem(HomeItem.Type.explorer, "Of"),
                new HomeItem(HomeItem.Type.explorer, "Stuff"),
                new HomeItem(HomeItem.Type.explorer, "GTG")
        };
        HomeAdapter customAdapter = new HomeAdapter(getContext(), homeItems);
        setAdapter(customAdapter);
    }
}