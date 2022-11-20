package com.OxGames.OxShell.Data;

import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.View;

import java.io.Serializable;

public class GridItem implements Serializable {
    public String title;
    public Object obj;
    public transient View view;
    public transient boolean isSelected;
    //protected transient boolean breaker;
    //public transient Rect dim;

    public GridItem(String _title, Object _obj) {
        title = _title;
        obj = _obj;
    }
    public Drawable getIcon() {
        return null;
    }
    public Drawable getSuperIcon() {
        return null;
    }
}
