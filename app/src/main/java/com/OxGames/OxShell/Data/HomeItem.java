package com.OxGames.OxShell.Data;

import android.graphics.drawable.Drawable;

import androidx.core.content.ContextCompat;

import com.OxGames.OxShell.Helpers.ActivityManager;
import com.OxGames.OxShell.Interfaces.DirsCarrier;
import com.OxGames.OxShell.R;

import java.io.Serializable;
import java.util.ArrayList;

public class HomeItem<T> extends XMBItem<T> implements Serializable, DirsCarrier {
    public enum Type { explorer, app, assoc, settings, }
    public Type type;
    public ArrayList<String> extraData;

    public HomeItem(Type _type) {
        this(_type, null);
    }
    public HomeItem(Type _type, String _title) {
        this(_type, _title, null);
    }
    public HomeItem(Type _type, String _title, T _obj) {
        super(_obj, _title);
        //super(_title, _obj);
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
        else if (type == Type.settings)
            icon = ContextCompat.getDrawable(ActivityManager.getCurrentActivity(), R.drawable.ic_baseline_settings_24);
        else if (type == Type.assoc)
            icon = PackagesCache.getPackageIcon(((IntentLaunchData)obj).getPackageName());
        return icon;
    }
    @Override
    public String toString() {
        return "title: " + title + " item type: " + type.toString() + " type of obj: " + (obj != null ? obj.getClass() : "null");
    }
//    @Override
//    public Drawable getSuperIcon() {
//        Drawable icon = null;
//        if (type == Type.assoc)
//            icon = ContextCompat.getDrawable(ActivityManager.getCurrentActivity(), R.drawable.ic_baseline_view_list_24);
//        return icon;
//    }

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
