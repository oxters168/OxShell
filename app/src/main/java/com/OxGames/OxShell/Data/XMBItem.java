package com.OxGames.OxShell.Data;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import androidx.core.content.ContextCompat;

import com.OxGames.OxShell.Helpers.ActivityManager;
import com.OxGames.OxShell.Helpers.AndroidHelpers;
import com.OxGames.OxShell.OxShellApp;
import com.OxGames.OxShell.R;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class XMBItem<T> implements Serializable {
    public T obj;
    protected String title;
    protected Object iconLoc;
    protected List<XMBItem> innerItems;

    protected transient Drawable icon;

    public XMBItem(T _obj, String _title, Object _iconLoc, XMBItem... innerItems) {
        obj = _obj;
        title = _title;
        iconLoc = _iconLoc;
        this.innerItems = new ArrayList<>();
        if (innerItems != null)
            Collections.addAll(this.innerItems, innerItems);
    }
    public XMBItem(T _obj, String _title, Object _iconLoc) {
        this(_obj, _title, _iconLoc, null);
    }
    public XMBItem(T _obj, String _title, XMBItem... innerItems) {
        this(_obj, _title, null, innerItems);
    }

    public Drawable getIcon() {
        if (icon == null && iconLoc != null) {
            if (iconLoc instanceof Integer) {
                icon = ContextCompat.getDrawable(OxShellApp.getContext(), (Integer)iconLoc);
            } else if (iconLoc instanceof Drawable) {
                icon = (Drawable)iconLoc;
            } else if (iconLoc instanceof String) {
                icon = AndroidHelpers.bitmapToDrawable(OxShellApp.getContext(), AndroidHelpers.bitmapFromFile((String)iconLoc));
            }
        }
        return icon;
    }

    public boolean hasInnerItems() {
        return innerItems != null && innerItems.size() > 0;
    }
    public int getInnerItemCount() {
        return innerItems != null ? innerItems.size() : 0;
    }
    public XMBItem getInnerItem(int index) {
        return innerItems.get(index);
    }
    public void clearInnerItems() {
        if (innerItems != null)
            innerItems.clear();
    }
    public String getTitle() {
        return title;
    }
}
