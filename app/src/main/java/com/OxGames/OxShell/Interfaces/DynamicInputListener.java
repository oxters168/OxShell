package com.OxGames.OxShell.Interfaces;

import android.view.View;

public interface DynamicInputListener {
    void onFocusChanged(View view, boolean hasFocus);
    void onValuesChanged();
}
