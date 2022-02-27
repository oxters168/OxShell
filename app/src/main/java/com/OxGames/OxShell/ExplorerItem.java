package com.OxGames.OxShell;

import android.graphics.drawable.Drawable;

public class ExplorerItem {

    String absolutePath;
    String name;
    boolean isDir;
    Drawable icon;

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
