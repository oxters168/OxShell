package com.OxGames.OxShell.Data;

import android.graphics.drawable.Drawable;

import androidx.core.content.ContextCompat;

import com.OxGames.OxShell.Helpers.ActivityManager;
import com.OxGames.OxShell.R;

import java.util.ArrayList;

public class XMBCat {
    public String title;
    public Drawable icon;

    private transient float currentX;
    private transient float currentY;
    private transient float prevX;
    private transient float prevY;
    //public transient boolean skipAnim;

    public XMBCat(String _title, Drawable _icon) {
        title = _title;
        icon = _icon;
    }
    public XMBCat(String _title) {
        this(_title, ContextCompat.getDrawable(ActivityManager.getCurrentActivity(), R.drawable.ic_baseline_source_24));
    }

    public float getX() {
        return currentX;
    }
    public float getY() {
        return currentY;
    }
    public float getPrevX() {
        return prevX;
    }
    public float getPrevY() {
        return prevY;
    }
    public void setX(float x) {
        prevX = currentX;
        currentX = x;
    }
    public void setY(float y) {
        prevY = currentY;
        currentY = y;
    }
}
