package com.OxGames.OxShell;

import android.graphics.drawable.Drawable;
import android.view.View;

public class DetailItem {
    Object obj; //object
    String leftAlignedText; //string
    String rightAlignedText;
    Drawable icon; //stay
    View view; //stay

    public DetailItem(Drawable _icon, String _leftAlignedText, String _rightAlignedText, Object _obj) {
        icon = _icon;
        obj = _obj;
        leftAlignedText = _leftAlignedText;
        rightAlignedText = _rightAlignedText;
    }
    public boolean HasIcon() {
        return icon != null;
    }
    public Drawable GetIcon() {

        return icon;
    }
}
