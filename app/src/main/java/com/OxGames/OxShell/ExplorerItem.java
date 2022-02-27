package com.OxGames.OxShell;

import android.graphics.drawable.Drawable;
import android.view.View;

public class ExplorerItem {

    String absolutePath;
    String name;
    boolean isDir;
    Drawable icon;
    View view;

    public ExplorerItem(Drawable _icon, String _absolutePath, String _name, boolean _isDir) {
        icon = _icon;
        absolutePath = _absolutePath;
        name = _name;
        isDir = _isDir;
    }
    public boolean HasIcon() {
        return isDir || icon != null;
    }
    public Drawable GetIcon() {

        return icon;
    }
}
