package com.OxGames.OxShell;

public class ExplorerItem {

    String absolutePath;
    String name;
    boolean isDir;
    public ExplorerItem(String _absolutePath, String _name, boolean _isDir) {
        absolutePath = _absolutePath;
        name = _name;
        isDir = _isDir;
    }
    public boolean HasIcon() {
        return isDir;
    }
}
