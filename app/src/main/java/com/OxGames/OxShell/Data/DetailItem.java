package com.OxGames.OxShell.Data;

import android.graphics.drawable.Drawable;
import android.view.View;

public class DetailItem {
    public Object obj; //object
    public String leftAlignedText; //string
    public String rightAlignedText;
    public Drawable icon; //stay
    public boolean isSelected;

    public DetailItem(Drawable _icon, String _leftAlignedText, String _rightAlignedText, Object _obj) {
        icon = _icon;
        obj = _obj;
        leftAlignedText = _leftAlignedText;
        rightAlignedText = _rightAlignedText;
    }
    public boolean hasIcon() {
        return icon != null;
    }
    public Drawable getIcon() {
        return icon;
    }
}
