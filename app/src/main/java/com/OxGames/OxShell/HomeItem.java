package com.OxGames.OxShell;

import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.core.content.ContextCompat;

public class HomeItem {
    public enum Type { none, explorer, app, add, }
    Type type;
    String title;
    View view;

    public HomeItem(Type _type) {
        type = _type;
    }
    public HomeItem(Type _type, String _title) {
        type = _type;
        title = _title;
    }
    public Drawable GetIcon() {
        Drawable icon = null;
        if (type == Type.explorer)
            icon = ContextCompat.getDrawable(HomeActivity.GetInstance(), R.drawable.ic_baseline_source_24);
        else if (type == Type.add)
            icon = ContextCompat.getDrawable(HomeActivity.GetInstance(), R.drawable.ic_baseline_add_circle_outline_24);
        return icon;
    }
}
