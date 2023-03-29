package com.OxGames.OxShell.Views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.appcompat.widget.AppCompatTextView;

import com.OxGames.OxShell.R;

// src: https://stackoverflow.com/a/55156928/5430992
public class BetterTextView extends AppCompatTextView {
    // constants
    private static final int DEFAULT_OUTLINE_SIZE = 0;
    private static final int DEFAULT_OUTLINE_COLOR = Color.TRANSPARENT;

    // data
    private int mOutlineSize;
    private int mOutlineColor;
    private int mTextColor;
    private float mShadowRadius;
    private float mShadowDx;
    private float mShadowDy;
    private int mShadowColor;

    private boolean passTouchEvents; // should pass on touch events to views behind us?
    private boolean ignoreTouchEvents; // should ignore touch events on self?

    public BetterTextView(Context context) {
        this(context, null);
    }

    public BetterTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setAttributes(attrs);
    }

    private void setAttributes(AttributeSet attrs) {
        // set defaults
        mOutlineSize = DEFAULT_OUTLINE_SIZE;
        mOutlineColor = DEFAULT_OUTLINE_COLOR;
        // text color
        mTextColor = getCurrentTextColor();
        if (attrs != null) {
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.BetterTextView);
            // outline size
            if (a.hasValue(R.styleable.BetterTextView_outlineSize)) {
                mOutlineSize = (int) a.getDimension(R.styleable.BetterTextView_outlineSize, DEFAULT_OUTLINE_SIZE);
            }
            // outline color
            if (a.hasValue(R.styleable.BetterTextView_outlineColor)) {
                mOutlineColor = a.getColor(R.styleable.BetterTextView_outlineColor, DEFAULT_OUTLINE_COLOR);
            }
            // shadow (the reason we take shadow from attributes is because we use API level 15 and only from 16 we have the get methods for the shadow attributes)
            if (a.hasValue(R.styleable.BetterTextView_android_shadowRadius)
                    || a.hasValue(R.styleable.BetterTextView_android_shadowDx)
                    || a.hasValue(R.styleable.BetterTextView_android_shadowDy)
                    || a.hasValue(R.styleable.BetterTextView_android_shadowColor)) {
                mShadowRadius = a.getFloat(R.styleable.BetterTextView_android_shadowRadius, 0);
                mShadowDx = a.getFloat(R.styleable.BetterTextView_android_shadowDx, 0);
                mShadowDy = a.getFloat(R.styleable.BetterTextView_android_shadowDy, 0);
                mShadowColor = a.getColor(R.styleable.BetterTextView_android_shadowColor, Color.TRANSPARENT);
            }

            a.recycle();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setPaintToOutline();
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    private void setPaintToOutline() {
        Paint paint = getPaint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(mOutlineSize);
        super.setTextColor(mOutlineColor);
        super.setShadowLayer(0, 0, 0, Color.TRANSPARENT);
    }

    private void setPaintToRegular() {
        Paint paint = getPaint();
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(0);
        super.setTextColor(mTextColor);
        super.setShadowLayer(mShadowRadius, mShadowDx, mShadowDy, mShadowColor);
    }


    @Override
    public void setTextColor(int color) {
        super.setTextColor(color);
        mTextColor = color;
    }


    public void setOutlineSize(int size) {
        mOutlineSize = size;
    }

    public void setOutlineColor(int color) {
        mOutlineColor = color;
    }
    public void setBlockTouchInput(boolean onOff) {
        passTouchEvents = !onOff;
    }
    public void setIgnoreTouchInput(boolean onOff) {
        ignoreTouchEvents = onOff;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        setPaintToOutline();
        super.onDraw(canvas);

        setPaintToRegular();
        super.onDraw(canvas);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean usedTouchEvent = false;
        if (!ignoreTouchEvents)
            usedTouchEvent = super.onTouchEvent(event);
        return !passTouchEvents && usedTouchEvent;
    }

//    @Override
//    public boolean onFilterTouchEventForSecurity(MotionEvent event) {
//        return false;
//        //return super.onFilterTouchEventForSecurity(event);
//    }
}
