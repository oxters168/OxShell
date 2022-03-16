package com.OxGames.OxShell;

import java.util.ArrayList;

public interface DirsCarrier {
    void ClearDirsList();
    void AddToDirsList(String dir);
    void RemoveFromDirsList(String dir);
    String[] GetDirsList();
}
