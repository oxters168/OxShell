package com.OxGames.OxShell;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.GridView;

import java.util.ArrayList;

public class HomeView extends GridView implements SlideTouchListener {
    SlideTouchHandler slideTouch = new SlideTouchHandler();
    int properPosition = 0;
//    private final ActivityManager.Page CURRENT_PAGE = ActivityManager.Page.home;

    public HomeView(Context context) {
        super(context);
        slideTouch.AddListener(this);
        Refresh();
    }
    public HomeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        slideTouch.AddListener(this);
        Refresh();
    }
    public HomeView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        slideTouch.AddListener(this);
        Refresh();
    }

//    private void OpenExplorer() {
//        Intent intent = new Intent(HomeActivity.GetInstance(), ExplorerActivity.class);
//        HomeActivity.GetInstance().startActivity(intent);
//    }

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

        // Checks the orientation of the screen
//        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
//            Toast.makeText(ActivityManager.GetActivityInstance(ActivityManager.GetCurrent()), "landscape", Toast.LENGTH_SHORT).show();
//        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
//            Toast.makeText(ActivityManager.GetActivityInstance(ActivityManager.GetCurrent()), "portrait", Toast.LENGTH_SHORT).show();
//        }
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
        for (int i = 0; i < getCount(); i++) {
            View view = ((HomeItem)getItemAtPosition(i)).view;
//            Log.d("HomeView", i + " " + view);

            if (view != null) {
                int bgColor = (i == properPosition) ? R.color.scheme1 : R.color.light_blue_400;
                view.setBackgroundResource(bgColor);
            }
        }
    }
    public void SelectLowerItem() {
        int columns = getNumColumns();
        int total = getCount();
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
        int total = getCount();
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
    public void MakeSelection() {
        HomeItem selectedItem = (HomeItem)getItemAtPosition(properPosition);
        if (selectedItem.type == HomeItem.Type.explorer) {
            ActivityManager.GoTo(ActivityManager.Page.explorer);
//            HomeActivity.GetInstance().GoTo(HomeActivity.Page.explorer);
        } else if (selectedItem.type == HomeItem.Type.app) {
            (new IntentLaunchData((String)selectedItem.obj)).Launch();
        } else if (selectedItem.type == HomeItem.Type.add) {
            ActivityManager.GoTo(ActivityManager.Page.addToHome);
//            HomeActivity.GetInstance().GoTo(HomeActivity.Page.addToHome);
        } else if (selectedItem.type == HomeItem.Type.assoc) {
            IntentShortcutsView.SetLaunchItem(selectedItem);
            ActivityManager.GoTo(ActivityManager.Page.intentShortcuts);
        }
    }
    public void DeleteSelection() {
        HomeItem selectedItem = (HomeItem)getItemAtPosition(properPosition);
        HomeManager.RemoveItem(selectedItem);
    }
    public void SetProperPosition(int pos) {
        properPosition = pos;
        setSelectionFromTop(pos, HomeActivity.displayMetrics != null ? (int)(HomeActivity.displayMetrics.heightPixels * 0.5) : 0);
    }

    public void Refresh() {
        ArrayList<HomeItem> homeItems = HomeManager.GetItems();
        if (homeItems == null)
            homeItems = new ArrayList<>();

//        homeItems.add(new HomeItem(HomeItem.Type.explorer, "Explorer"));
        //Get added items and place in home
        homeItems.add(new HomeItem(HomeItem.Type.add));

        HomeAdapter customAdapter = new HomeAdapter(getContext(), homeItems);
        setAdapter(customAdapter);

//        SetProperPosition(0);
    }
}