package com.OxGames.OxShell.Interfaces;

public interface DirsCarrier {
    void clearDirsList();
    void addToDirsList(String dir);
    void removeFromDirsList(String dir);
    String[] getDirsList();
}
