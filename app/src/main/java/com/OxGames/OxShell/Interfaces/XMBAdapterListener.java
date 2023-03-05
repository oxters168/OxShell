package com.OxGames.OxShell.Interfaces;

public interface XMBAdapterListener {
    void onColumnAdded(int columnIndex);
    void onColumnRemoved(int columnIndex);
    void onSubItemAdded(int columnIndex, int localIndex);
    void onSubItemRemoved(int columnIndex, int localIndex);
}
