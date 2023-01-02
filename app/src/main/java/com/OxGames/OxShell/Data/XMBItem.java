package com.OxGames.OxShell.Data;

import android.graphics.drawable.Drawable;

import androidx.core.content.ContextCompat;

import com.OxGames.OxShell.Helpers.ActivityManager;
import com.OxGames.OxShell.R;

public class XMBItem<T> {
    public T obj;
    public String title;
    // meant for when reloading items from file to keep their correct positions in the menu
    private int colIndex;
    private int localIndex;

    protected transient Drawable icon;
    private transient float currentX;
    private transient float currentY;
    private transient float prevX;
    private transient float prevY;

    public XMBItem(T _obj, String _title, Drawable _icon, int _colIndex, int _localIndex) {
        obj = _obj;
        title = _title;
        icon = _icon;
        colIndex = _colIndex;
        localIndex = _localIndex;
    }
    public XMBItem(T _obj, String _title) {
        this(_obj, _title, ContextCompat.getDrawable(ActivityManager.getCurrentActivity(), R.drawable.ic_baseline_hide_image_24), -1, -1);
    }

    public Drawable getIcon() {
        return icon;
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
