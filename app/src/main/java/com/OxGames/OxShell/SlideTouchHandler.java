package com.OxGames.OxShell;

import android.view.MotionEvent;

import java.util.ArrayList;

public class SlideTouchHandler {
    public class TouchData {
        float currentTouchX = 0;
        float currentTouchY = 0;
        float prevTouchX = 0;
        float prevTouchY = 0;
        float startTouchX = 0;
        float startTouchY = 0;
        int framesPassed = 0;

        float deadzone = 0.2f;
        int framesPerScroll = 24;
        boolean moved = false;
    }
    private TouchData data;
    private ArrayList<SlideTouchListener> touchListeners = new ArrayList<>();

    public SlideTouchHandler() {
        data = new TouchData();
    }

    public void AddListener(SlideTouchListener listener) {
        touchListeners.add(listener);
    }

    public TouchData GetTouchData() {
        return data;
    }

    public void Update(MotionEvent ev) {
        data.currentTouchX = ev.getX();
        data.currentTouchY = ev.getY();

        final int action = ev.getAction();
        switch (action) {
            case MotionEvent.ACTION_MOVE:
                data.moved = true;
//                invalidate();
                for (SlideTouchListener stl : touchListeners)
                    stl.onRequestInvalidate();
//                Log.d("Touch", "Diff = " + diff);
//                Log.d("Touch", "Action_Move (" + ev.getX() + ", " + ev.getY() + ")");
                break;
            case MotionEvent.ACTION_DOWN:
                data.moved = false;
                data.startTouchX = data.currentTouchX;
                data.startTouchY = data.currentTouchY;
//                Log.d("Touch", "Action_Down (" + ev.getX() + ", " + ev.getY() + ")");
                break;
            case MotionEvent.ACTION_UP:
                if (!data.moved) {
                    //Click
//                    Log.d("Touch", "Clicked");
                    for (SlideTouchListener stl : touchListeners)
                        stl.onClick();
                }
                data.moved = false;
//                Log.d("Touch", "Action_Up (" + ev.getX() + ", " + ev.getY() + ")");
                break;
        }

        data.prevTouchX = data.currentTouchX;
        data.prevTouchY = data.currentTouchY;
    }
    public void CheckForEvents() {
        if (data.moved) {
            float diffX = data.currentTouchX - data.startTouchX;
            float diffY = data.currentTouchY - data.startTouchY;
            float percentScrollX = CalculatePercentWithDeadzone(diffX, HomeActivity.displayMetrics.widthPixels);
            float percentScrollY = CalculatePercentWithDeadzone(diffY, HomeActivity.displayMetrics.heightPixels);

//            Log.d("Touch", diff + " / " + FullscreenActivity.displayMetrics.heightPixels + " = " + percentScroll);

            float stretchedFramesPerScroll = (1 - percentScrollY) * data.framesPerScroll;
            if (percentScrollX > 0 && diffX > 0) {
                //Go right
                if (data.framesPassed > stretchedFramesPerScroll) {
                    data.framesPassed = 0;
                    for (SlideTouchListener stl : touchListeners)
                        stl.onSwipeRight();
                }
            } else if (percentScrollX > 0 && diffX < 0) {
                //Go left
                if (data.framesPassed > stretchedFramesPerScroll) {
                    data.framesPassed = 0;
                    for (SlideTouchListener stl : touchListeners)
                        stl.onSwipeLeft();
                }
            }

            if (percentScrollY > 0 && diffY > 0) {
                //Go down
                if (data.framesPassed > stretchedFramesPerScroll) {
                    data.framesPassed = 0;
                    for (SlideTouchListener stl : touchListeners)
                        stl.onSwipeDown();
//                    SelectNextItem();
                }
            } else if (percentScrollY > 0 && diffY < 0) {
                //Go up
                if (data.framesPassed > stretchedFramesPerScroll) {
                    data.framesPassed = 0;
                    for (SlideTouchListener stl : touchListeners)
                        stl.onSwipeUp();
//                    SelectPrevItem();
                }
            }
            data.framesPassed++;
            for (SlideTouchListener stl : touchListeners)
                stl.onRequestInvalidate();
//            invalidate();
        }
    }

    private float CalculatePercentWithDeadzone(float diff, float total) {
        float percentWithDeadzone = Math.abs(diff / (total / 5f));
        if (percentWithDeadzone > 1)
            percentWithDeadzone = 1;
        if (percentWithDeadzone < data.deadzone)
            percentWithDeadzone = 0;
        else
            percentWithDeadzone = (percentWithDeadzone - data.deadzone) / (1 - data.deadzone);
        return percentWithDeadzone;
    }
}
