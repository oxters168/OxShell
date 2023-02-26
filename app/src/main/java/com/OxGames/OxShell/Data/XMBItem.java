package com.OxGames.OxShell.Data;

import android.graphics.drawable.Drawable;

import androidx.core.content.ContextCompat;

import com.OxGames.OxShell.Helpers.ActivityManager;
import com.OxGames.OxShell.Helpers.AndroidHelpers;
import com.OxGames.OxShell.OxShellApp;
import com.OxGames.OxShell.R;

import java.io.Serializable;
import java.util.List;

public class XMBItem<T> implements Serializable {
    public T obj;
    public String title;
    // meant for when reloading items from file to keep their correct positions in the menu (do not set this manually)
    public int colIndex;
    public int localIndex;
    protected Object iconLoc;
    protected boolean iconIsResource;
    private List<XMBItem> innerItems;

    //protected transient Drawable icon;
    protected transient Drawable icon;
    private transient float currentX;
    private transient float currentY;
    private transient float prevX;
    private transient float prevY;

    public XMBItem(T _obj, String _title, Object _iconLoc, int _colIndex, int _localIndex) {
        obj = _obj;
        title = _title;
        iconLoc = _iconLoc;
        iconIsResource = _iconLoc instanceof Integer;
        //icon = _icon;
        colIndex = _colIndex;
        localIndex = _localIndex;
    }
    public XMBItem(T _obj, String _title, int _colIndex, int _localIndex) {
        this(_obj, _title, null, _colIndex, _localIndex);
    }
    public XMBItem(T _obj, String _title) {
        this(_obj, _title, null, -1, -1);
    }

    public Drawable getIcon() {
        if (icon == null && iconLoc != null) {
            if (iconIsResource) {
                icon = ContextCompat.getDrawable(OxShellApp.getContext(), (Integer)iconLoc);
            } else {
                icon = AndroidHelpers.bitmapToDrawable(OxShellApp.getContext(), AndroidHelpers.bitmapFromFile((String)iconLoc));
            }
        }
        return icon;
    }

    public boolean hasInnerItems() {
        return innerItems != null && innerItems.size() > 0;
    }
    public int getInnerItemCount() { return innerItems != null ? innerItems.size() : 0; }
    public XMBItem getInnerItem(int index) {
        return innerItems.get(index);
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
