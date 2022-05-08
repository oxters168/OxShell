package com.OxGames.OxShell;

import android.graphics.drawable.Drawable;

import androidx.core.content.ContextCompat;

import java.io.Serializable;
import java.util.ArrayList;

public class HomeItem extends GridItem implements Serializable, DirsCarrier {
    public enum Type { explorer, app, assoc, add, }
    Type type;
    ArrayList<String> extraData;

    public HomeItem(Type _type) {
        super(null, null);
        type = _type;
        extraData = new ArrayList<>();
    }
    public HomeItem(Type _type, String _title) {
        super(_title, null);
        type = _type;
        extraData = new ArrayList<>();
    }
    public HomeItem(Type _type, String _title, Object _obj) {
        super(_title, _obj);
        type = _type;
        extraData = new ArrayList<>();
    }
    @Override
    public Drawable getIcon() {
        Drawable icon = null;
        if (type == Type.explorer)
            icon = ContextCompat.getDrawable(ActivityManager.getCurrentActivity(), R.drawable.ic_baseline_source_24);
        else if (type == Type.app)
            icon = PackagesCache.getPackageIcon((String)obj);
        else if (type == Type.add)
            icon = ContextCompat.getDrawable(ActivityManager.getCurrentActivity(), R.drawable.ic_baseline_settings_24);
        else if (type == Type.assoc)
            icon = PackagesCache.getPackageIcon(((IntentLaunchData)obj).getPackageName());
        return icon;
    }
    @Override
    public Drawable getSuperIcon() {
        Drawable icon = null;
        if (type == Type.assoc)
            icon = ContextCompat.getDrawable(ActivityManager.getCurrentActivity(), R.drawable.ic_baseline_view_list_24);
        return icon;
    }

    public void clearDirsList() {
        extraData = new ArrayList<>();
    }
    public void addToDirsList(String dir) {
        extraData.add(dir);
    }
    public void removeFromDirsList(String dir) {
        extraData.remove(dir);
    }
    public String[] getDirsList() {
        String[] arrayed = new String[extraData.size()];
        return extraData.toArray(arrayed);
    }
}
