package com.OxGames.OxShell;

import android.graphics.drawable.Drawable;
import android.view.View;

import java.io.Serializable;

public class GridItem implements Serializable {
    protected String title;
    protected Object obj;
    protected transient View view;

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
