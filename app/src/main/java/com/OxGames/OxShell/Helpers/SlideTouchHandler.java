package com.OxGames.OxShell.Helpers;

import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.view.MotionEvent;

import com.OxGames.OxShell.Helpers.ActivityManager;
import com.OxGames.OxShell.Interfaces.SlideTouchListener;

import java.util.ArrayList;

public class SlideTouchHandler {
    public class TouchData {
        float currentTouchX = 0;
        float currentTouchY = 0;
        float prevTouchX = 0;
        float prevTouchY = 0;
        float startX = 0;
        float startY = 0;
        float movingStartTouchX = 0;
        float movingStartTouchY = 0;

        float slideSpreadDiv = 4f; //Lower number means it takes a larger area to reach max speed

//        int framesPassed = 0;
//        long prevScrollMilli = 0;
        long prevScrollUpMilli = 0;
        long prevScrollDownMilli = 0;
        long prevScrollLeftMilli = 0;
        long prevScrollRightMilli = 0;

        float deadzone = 0.2f;
//        int framesPerScroll = 24;
        int millisPerScroll = 1000; //How frequently repeated events should be sent (once per given milliseconds)
        boolean moved = false;
        boolean movedBeyondDeadZone = false;
    }
    private TouchData data;
    private ArrayList<SlideTouchListener> touchListeners = new ArrayList<>();

    public SlideTouchHandler() {
        data = new TouchData();
    }

    public void addListener(SlideTouchListener listener) {
        touchListeners.add(listener);
    }

    public TouchData getTouchData() {
        return data;
    }

    public void update(MotionEvent ev) {
        data.prevTouchX = data.currentTouchX;
        data.prevTouchY = data.currentTouchY;
        data.currentTouchX = ev.getRawX();
        data.currentTouchY = ev.getRawY();

        final int action = ev.getAction();
        switch (action) {
            case MotionEvent.ACTION_MOVE:
                data.moved = true;

//                float maxXPixels = HomeActivity.displayMetrics.widthPixels / data.slideSpreadDiv;
                float maxXPixels = getMaxPixels();
                float diffX = data.movingStartTouchX - data.currentTouchX;
//                float percentX = CalculatePercentWithDeadzone(diffX, HomeActivity.displayMetrics.widthPixels);
//                Log.d("Touch", "Start " + data.startTouchX + " current " + data.currentTouchX + " percent " + percentX + " diff " + diffX + " max " + maxXPixels);
                if (Math.abs(diffX) > maxXPixels)
                    data.movingStartTouchX = data.currentTouchX + Math.signum(diffX) * maxXPixels; //Don't let start point stray further than the max distance
                if (Math.abs(diffX) > maxXPixels * data.deadzone)
                    data.movedBeyondDeadZone = true;

//                float maxYPixels = HomeActivity.displayMetrics.heightPixels / data.slideSpreadDiv;
                float maxYPixels = getMaxPixels();
                float diffY = data.movingStartTouchY - data.currentTouchY;
                if (Math.abs(diffY) > maxYPixels)
                    data.movingStartTouchY = data.currentTouchY + Math.signum(diffY) * maxYPixels; //Don't let start point stray further than the max distance
                if (Math.abs(diffY) > maxYPixels * data.deadzone)
                    data.movedBeyondDeadZone = true;

                for (SlideTouchListener stl : touchListeners)
                    stl.onRequestInvalidate();
//                Log.d("Touch", "Action_Move (" + ev.getX() + ", " + ev.getY() + ")");
                break;
            case MotionEvent.ACTION_DOWN:
                data.moved = false;
                data.movedBeyondDeadZone = false;
                data.startX = data.currentTouchX;
                data.startY = data.currentTouchY;
                data.movingStartTouchX = data.currentTouchX;
                data.movingStartTouchY = data.currentTouchY;
//                Log.d("Touch", "Action_Down (" + ev.getX() + ", " + ev.getY() + ")");
                break;
            case MotionEvent.ACTION_UP:
                float offsetX = Math.abs(data.currentTouchX - data.startX);
                float offsetY = Math.abs(data.currentTouchY - data.startY);
                if (!(data.movedBeyondDeadZone || offsetX > data.deadzone * getMaxPixels() || offsetY > data.deadzone * getMaxPixels())) {
                    //Click
//                    Log.d("Touch", "Clicked");
                    for (SlideTouchListener stl : touchListeners)
                        stl.onClick();
                }
                data.moved = false;
                data.movedBeyondDeadZone = false;
//                Log.d("Touch", "Action_Up (" + ev.getX() + ", " + ev.getY() + ")");
                break;
        }
    }
    public void checkForEvents() {
        if (data.moved) {
//            Log.d("Touch", "x " + diffX + " / " + HomeActivity.displayMetrics.widthPixels + " = " + percentScrollX);
//            Log.d("Touch", "y " + diffY + " / " + HomeActivity.displayMetrics.heightPixels + " = " + percentScrollY);
            float diffX = data.currentTouchX - data.movingStartTouchX;
            float percentScrollX = calculatePercentWithDeadzone(diffX);
            float stretchedFramesPerScrollX = (1 - percentScrollX) * data.millisPerScroll;
            if (percentScrollX > 0 && diffX > 0) {
                //Go right
                if (SystemClock.uptimeMillis() - data.prevScrollRightMilli > stretchedFramesPerScrollX) {
                    data.prevScrollRightMilli = SystemClock.uptimeMillis();
                    for (SlideTouchListener stl : touchListeners)
                        stl.onSwipeRight();
                }
            } else if (percentScrollX > 0 && diffX < 0) {
                //Go left
                if (SystemClock.uptimeMillis() - data.prevScrollLeftMilli > stretchedFramesPerScrollX) {
                    data.prevScrollLeftMilli = SystemClock.uptimeMillis();
                    for (SlideTouchListener stl : touchListeners)
                        stl.onSwipeLeft();
                }
            }

            float diffY = data.currentTouchY - data.movingStartTouchY;
            float percentScrollY = calculatePercentWithDeadzone(diffY);
            float stretchedFramesPerScrollY = (1 - percentScrollY) * data.millisPerScroll;
            if (percentScrollY > 0 && diffY > 0) {
                //Go down
                if (SystemClock.uptimeMillis() - data.prevScrollDownMilli > stretchedFramesPerScrollY) {
                    data.prevScrollDownMilli = SystemClock.uptimeMillis();
                    for (SlideTouchListener stl : touchListeners)
                        stl.onSwipeDown();
                }
            } else if (percentScrollY > 0 && diffY < 0) {
                //Go up
                if (SystemClock.uptimeMillis() - data.prevScrollUpMilli > stretchedFramesPerScrollY) {
                    data.prevScrollUpMilli = SystemClock.uptimeMillis();
                    for (SlideTouchListener stl : touchListeners)
                        stl.onSwipeUp();
                }
            }
            for (SlideTouchListener stl : touchListeners)
                stl.onRequestInvalidate();
        }
    }

    private float getMaxPixels() {
        DisplayMetrics displayMetrics = ActivityManager.getCurrentActivity().getDisplayMetrics();
        return Math.min(displayMetrics.widthPixels, displayMetrics.heightPixels) / data.slideSpreadDiv;
    }

    private float calculatePercentWithDeadzone(float diff) {
//        float max = total / data.slideSpreadDiv;
        float max = getMaxPixels();
        float percentWithDeadzone = Math.abs(diff / max);
//        Log.d("TouchPercent", diff + " / " + max + " = " + percentWithDeadzone);
        if (percentWithDeadzone > 1)
            percentWithDeadzone = 1;

        percentWithDeadzone -= data.deadzone;
//        Log.d("TouchPercent", "shifted " + percentWithDeadzone);
        if (percentWithDeadzone < 0)
            percentWithDeadzone = 0;
        else
            percentWithDeadzone = percentWithDeadzone / (1f - data.deadzone);

        percentWithDeadzone = (float)Math.pow(percentWithDeadzone, 2);

        return percentWithDeadzone;
    }
}
