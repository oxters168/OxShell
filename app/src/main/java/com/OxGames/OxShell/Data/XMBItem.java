package com.OxGames.OxShell.Data;

import android.graphics.drawable.Drawable;

import androidx.core.content.ContextCompat;

import com.OxGames.OxShell.Helpers.ActivityManager;
import com.OxGames.OxShell.R;

public class XMBItem {
    public Object obj;
    public String title;
    public XMBCat category;

    protected transient Drawable icon;
    private transient float currentX;
    private transient float currentY;
    private transient float prevX;
    private transient float prevY;
    //public transient boolean skipAnim;

    public XMBItem(Object _obj, String _title, Drawable _icon, XMBCat _category) {
        obj = _obj;
        title = _title;
        icon = _icon;
        category = _category;
    }
    public XMBItem(Object _obj, String _title, XMBCat _category) {
        this(_obj, _title, ContextCompat.getDrawable(ActivityManager.getCurrentActivity(), R.drawable.ic_baseline_hide_image_24), _category);
    }
    public XMBItem(Object _obj, String _title, Drawable _icon) {
        this(_obj, _title, _icon, null);
    }
    public XMBItem(Object _obj, String _title) {
        this(_obj, _title, ContextCompat.getDrawable(ActivityManager.getCurrentActivity(), R.drawable.ic_baseline_hide_image_24));
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
