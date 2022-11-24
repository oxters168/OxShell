package com.OxGames.OxShell.Views;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.core.content.ContextCompat;

import com.OxGames.OxShell.Data.XMBCat;
import com.OxGames.OxShell.Helpers.ActivityManager;
import com.OxGames.OxShell.Interfaces.InputReceiver;
import com.OxGames.OxShell.R;

public class XMBView2 extends ViewGroup implements InputReceiver {
    //private Context context;

    public XMBView2(Context context) {
        this(context, null);
    }
    public XMBView2(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    public XMBView2(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        //this.context = context;

//        setLayoutAnimationListener(new Animation.AnimationListener() {
//            @Override
//            public void onAnimationStart(Animation animation) {
//                Log.d("XMBView2", "Animation started");
//            }
//
//            @Override
//            public void onAnimationEnd(Animation animation) {
//                Log.d("XMBView2", "Animation ended");
//            }
//
//            @Override
//            public void onAnimationRepeat(Animation animation) {
//                Log.d("XMBView2", "Animation repeated");
//            }
//        });

        XMBItemView view;
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        view = (XMBItemView)layoutInflater.inflate(R.layout.xmb_item, null);
        view.title = "Test";
        view.icon = ContextCompat.getDrawable(ActivityManager.getCurrentActivity(), R.drawable.ic_baseline_source_24);
        addView(view);
        view.setX(getWidth() / 2f);
        view = (XMBItemView)layoutInflater.inflate(R.layout.xmb_item, null);
        view.title = "Test2";
        view.icon = ContextCompat.getDrawable(ActivityManager.getCurrentActivity(), R.drawable.ic_baseline_hide_image_24);
        addView(view);
        view.setX(getWidth() / 2f);
    }

    int tempIndex = 0;
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        Log.d("XMBView2", "onLayout called");

        final int count = getChildCount();

        for (int i = 0; i < count; i++) {
            XMBItemView child = (XMBItemView)getChildAt(i);

            if (child.getVisibility() == GONE)
                return;

            //Doing child.layout sets the output of child.getWidth and child.getHeight, and if a custom view tries to draw outside of these bounds it will get clipped
            //So to get a specific width/height out of a custom view I added some functions to help me choose the size I want
            float x = child.getX();
            float y = child.getY();
            int left = Math.round(x - child.getItemWidth() / 2f);
            int top = Math.round(y - child.getItemHeight() / 2f - i * child.getItemHeight());
            int right = Math.round(x + (child.getFullWidth() - (child.getItemWidth() / 2f)));
            int bottom = Math.round(y + (child.getFullHeight() - (child.getItemHeight() / 2f)) - i * child.getItemHeight());
            child.layout(left, top, right, bottom);
        }
    }

    Paint painter = new Paint();
    @Override
    protected void dispatchDraw(Canvas canvas) {
        //Log.d("XMBView2", "dispatchDraw called");
        super.dispatchDraw(canvas);
        painter.setColor(0xFFFF0000);
        painter.setStrokeWidth(8);
        canvas.drawLine(0, 0, getWidth(), getHeight(), painter);
        canvas.drawLine(0, getHeight(), getWidth(), 0, painter);
    }

    private float xmbTrans = 1;
    ValueAnimator xmbimator = null;
    private void animateXMB() {
        if (xmbimator != null)
            xmbimator.cancel();

        //catTransX = 0;
        xmbimator = ValueAnimator.ofFloat(0, 1);
        xmbimator.addUpdateListener(valueAnimator -> {
            xmbTrans = ((Float)valueAnimator.getAnimatedValue());
            for (int i = 0; i < getChildCount(); i++) {
                XMBItemView child = (XMBItemView)getChildAt(i);
                float nextY = (i + tempIndex) * child.getItemHeight();
                float transY = (nextY - child.getY()) * xmbTrans + child.getY();
                child.setY(transY);
            }
            //Log.d("XMBView2", "Animation value " + xmbTrans);
            //Log.d("XMBView", "Animating " + catTransX);
            //XMBView.this.invalidate();
        });
        //Log.d("XMBView", "Starting animation");
        //setAnimation(xmbimator);
        xmbimator.setDuration(300);
        xmbimator.start();
        //invalidate();
    }

    @Override
    public boolean receiveKeyEvent(KeyEvent key_event) {
        //Log.d("XMBView2", key_event.toString());
        if (key_event.getAction() == KeyEvent.ACTION_DOWN) {
            if (key_event.getKeyCode() == KeyEvent.KEYCODE_BUTTON_A) {
                makeSelection();
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
    public void selectLowerItem() {
        tempIndex--;
        //Log.d("XMBView", "Received move down signal");
//        XMBCat currentCat = items.get(currentIndex).category;
//        if (currentCat != null && currentIndex + 1 < items.size() && items.get(currentIndex + 1).category == currentCat)
//            setIndex(currentIndex + 1);
//        clearAnimation();
//        Animation animDown = AnimationUtils.loadAnimation(context, android.R.anim.slide_in_left);
//        setAnimation(animDown);
//        animDown.startNow();
        animateXMB();
    }
    public void selectUpperItem() {
        tempIndex++;
        //Log.d("XMBView", "Received move up signal");
//        XMBCat currentCat = items.get(currentIndex).category;
//        if (currentCat != null && currentIndex - 1 >= 0 && items.get(currentIndex - 1).category == currentCat)
//            setIndex(currentIndex - 1);
//        clearAnimation();
//        Animation animUp = AnimationUtils.loadAnimation(context, android.R.anim.slide_in_left);
//        setAnimation(animUp);
//        animUp.startNow();
        animateXMB();
    }
    public void selectRightItem() {
        //Log.d("XMBView", "Received move right signal");
//        int adjustedIndex = -1;
//        XMBCat currentCat = items.get(currentIndex).category;
//        if (currentCat != null) {
//            int nextIndex = currentIndex + 1;
//            while (nextIndex < items.size()) {
//                XMBCat nextCat = items.get(nextIndex).category;
//                if (nextCat != currentCat) {
//                    if (nextCat != null)
//                        adjustedIndex = getCachedIndexOfCat(nextCat);
//                    else
//                        adjustedIndex = nextIndex;
//                    break;
//                }
//                nextIndex++;
//            }
//        }
//        else if (currentIndex + 1 < items.size()) {
//            adjustedIndex = currentIndex + 1;
//            XMBCat nextCat = items.get(adjustedIndex).category;
//            if (nextCat != null)
//                adjustedIndex = getCachedIndexOfCat(nextCat);
//        }
//
//        if (adjustedIndex >= 0) {
//            setIndex(adjustedIndex);
//        }
    }
    public void selectLeftItem() {
        //Log.d("XMBView", "Received move left signal");
//        int adjustedIndex = -1;
//        XMBCat currentCat = items.get(currentIndex).category;
//        if (currentCat != null) {
//            int nextIndex = currentIndex - 1;
//            while (nextIndex >= 0) {
//                XMBCat prevCat = items.get(nextIndex).category;
//                if (prevCat != currentCat) {
//                    if (prevCat != null)
//                        adjustedIndex = getCachedIndexOfCat(prevCat);
//                    else
//                        adjustedIndex = nextIndex;
//                    break;
//                }
//                nextIndex--;
//            }
//        }
//        else if (currentIndex - 1 >= 0) {
//            adjustedIndex = currentIndex - 1;
//            XMBCat prevCat = items.get(adjustedIndex).category;
//            if (prevCat != null)
//                adjustedIndex = getCachedIndexOfCat(prevCat);
//        }
//
//        if (adjustedIndex >= 0)
//            setIndex(adjustedIndex);
    }
    public void makeSelection() {
    }
    public void deleteSelection() {
    }
    public void refresh() {
        invalidate();
    }
}
