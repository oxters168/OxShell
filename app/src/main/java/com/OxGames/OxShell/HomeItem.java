package com.OxGames.OxShell;

import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.core.content.ContextCompat;

public class HomeItem {
    public enum Type { none, explorer, app, }
    Type type;
    String packageName;
    View view;
//    HomeItemHolder holder;

    public HomeItem(Type _type, String _packageName) {
        type = _type;
        packageName = _packageName;
    }
    public Drawable GetIcon() {
        Drawable icon = null;
        if (type == Type.explorer)
            icon = ContextCompat.getDrawable(HomeActivity.GetInstance(), R.drawable.ic_baseline_source_24);
        return icon;
    }
}
