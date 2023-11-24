package com.OxGames.OxShell.Interfaces;

public interface XMBAdapterListener {
    void onColumnAdded(int columnIndex);
//    void onColumnRemoved(int columnIndex);
    void onColumnShifted(int fromColIndex, int toColIndex);
    void onSubItemAdded(int columnIndex, int localIndex);
//    void onSubItemRemoved(int columnIndex, int localIndex);
    void onInnerItemsChanged(Integer... position);
    void onItemRemoved(Integer... position);
}
