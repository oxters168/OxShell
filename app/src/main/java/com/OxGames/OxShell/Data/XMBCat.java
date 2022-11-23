package com.OxGames.OxShell.Data;

import android.graphics.drawable.Drawable;

import androidx.core.content.ContextCompat;

import com.OxGames.OxShell.Helpers.ActivityManager;
import com.OxGames.OxShell.R;

import java.util.ArrayList;

public class XMBCat {
    public String title;
    public Drawable icon;

    public float currentX;
    public float currentY;

    public XMBCat(String _title, Drawable _icon) {
        title = _title;
        icon = _icon;
    }
    public XMBCat(String _title) {
        this(_title, ContextCompat.getDrawable(ActivityManager.getCurrentActivity(), R.drawable.ic_baseline_source_24));
    }
}
