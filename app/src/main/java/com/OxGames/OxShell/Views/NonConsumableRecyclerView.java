package com.OxGames.OxShell.Views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.recyclerview.widget.RecyclerView;

public class NonConsumableRecyclerView extends RecyclerView {

    public NonConsumableRecyclerView(Context context) {
        super(context);
    }

    public NonConsumableRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NonConsumableRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        // Do not intercept touch events, allow them to pass through to the view below
        return false;
    }
}
