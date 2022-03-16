package com.OxGames.OxShell;

import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.core.content.ContextCompat;

import java.io.Serializable;
import java.util.ArrayList;

public class HomeItem implements Serializable, DirsCarrier {
    public enum Type { explorer, app, assoc, add, }
    Type type;
    String title;
    Object obj;
    transient View view;
    ArrayList<String> extraData;

    public HomeItem(Type _type) {
        type = _type;
        extraData = new ArrayList<>();
    }
    public HomeItem(Type _type, String _title) {
        type = _type;
        title = _title;
        extraData = new ArrayList<>();
    }
    public HomeItem(Type _type, String _title, Object _obj) {
        type = _type;
        title = _title;
        obj = _obj;
        extraData = new ArrayList<>();
    }
    public Drawable GetIcon() {
        Drawable icon = null;
        if (type == Type.explorer)
            icon = ContextCompat.getDrawable(ActivityManager.GetCurrentActivity(), R.drawable.ic_baseline_source_24);
        else if (type == Type.app)
            icon = PackagesCache.GetPackageIcon((String)obj);
        else if (type == Type.add)
            icon = ContextCompat.getDrawable(ActivityManager.GetCurrentActivity(), R.drawable.ic_baseline_add_circle_outline_24);
        else if (type == Type.assoc)
            icon = ContextCompat.getDrawable(ActivityManager.GetCurrentActivity(), R.drawable.ic_baseline_app_shortcut_24);
        return icon;
    }
    public Drawable GetSuperIcon() {
        Drawable icon = null;
        if (type == Type.assoc)
            icon = PackagesCache.GetPackageIcon(((IntentLaunchData)obj).GetPackageName());
        return icon;
    }

    public void ClearDirsList() {
        extraData = new ArrayList<>();
    }
    public void AddToDirsList(String dir) {
        extraData.add(dir);
    }
    public void RemoveFromDirsList(String dir) {
        extraData.remove(dir);
    }
    public String[] GetDirsList() {
        String[] arrayed = new String[extraData.size()];
        return extraData.toArray(arrayed);
    }
}
