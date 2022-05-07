package com.OxGames.OxShell;

public interface DirsCarrier {
    void clearDirsList();
    void addToDirsList(String dir);
    void removeFromDirsList(String dir);
    String[] getDirsList();
}
