package com.OxGames.OxShell;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.GridView;

public class HomeView extends GridView implements SlideTouchListener {
    SlideTouchHandler slideTouch = new SlideTouchHandler();

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

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        slideTouch.CheckForEvents();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
//        requestFocusFromTouch();

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
        Intent intent = new Intent(HomeActivity.GetInstance(), ExplorerActivity.class);
        HomeActivity.GetInstance().startActivity(intent);
    }
    @Override
    public void onSwipeDown() {

    }
    @Override
    public void onSwipeLeft() {

    }
    @Override
    public void onSwipeRight() {

    }
    @Override
    public void onSwipeUp() {

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