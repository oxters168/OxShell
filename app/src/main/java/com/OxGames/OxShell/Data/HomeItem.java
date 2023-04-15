package com.OxGames.OxShell.Data;

import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.net.Uri;

import androidx.core.content.ContextCompat;

import com.OxGames.OxShell.Helpers.AndroidHelpers;
import com.OxGames.OxShell.Interfaces.DirsCarrier;
import com.OxGames.OxShell.OxShellApp;
import com.OxGames.OxShell.R;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class HomeItem<T> extends XMBItem<T> implements DirsCarrier {
    public enum Type { explorer, addExplorer, app, addAppOuter, addApp, assoc, addAssocOuter, addAssoc, createAssoc, assocExe, setImageBg, setShaderBg, setUiScale, setSystemUi, setAudioVolume, settings, setControls, appInfo, saveLogs, }
    public Type type;
    public ArrayList<String> extraData;
    private boolean innerItemsLoaded;

    public HomeItem(Type _type) {
        this(_type, null);
    }
    public HomeItem(Type _type, String _title) {
        this(_type, _title, null);
    }
    public HomeItem(T _obj, Type _type, String _title, DataRef _iconLoc, XMBItem... innerItems) {
        super(_obj, _title, _iconLoc, innerItems);
        type = _type;
        extraData = new ArrayList<>();
    }
    public HomeItem(Type _type, String _title, XMBItem... innerItems) {
        this(null, _type, _title, null, innerItems);
    }
    public HomeItem(T _obj, Type _type, XMBItem... innerItems) {
        this(_obj, _type, null, null, innerItems);
    }
    public HomeItem(T _obj, Type _type, String _title, XMBItem... innerItems) {
        this(_obj, _type, _title, null, innerItems);
    }
    @Override
    public void getIcon(Consumer<Drawable> onIconLoaded) {
        //Drawable icon = null;
        if (type == Type.explorer)
            onIconLoaded.accept(icon = ContextCompat.getDrawable(OxShellApp.getContext(), R.drawable.ic_baseline_source_24));
        else if (type == Type.addApp)// || type == Type.app)
            PackagesCache.requestPackageIcon((String) obj, drawable -> onIconLoaded.accept(icon = drawable));
        else if (isInnerSettingType(type))
            onIconLoaded.accept(icon = ContextCompat.getDrawable(OxShellApp.getContext(), R.drawable.ic_baseline_construction_24));
        else if (type == Type.assocExe)
            onIconLoaded.accept(icon = ContextCompat.getDrawable(OxShellApp.getContext(), R.drawable.ic_baseline_auto_awesome_24));
        else if (type == Type.addAssoc) {// || type == Type.assoc) {
            IntentLaunchData intent = ShortcutsCache.getIntent((UUID)obj);
            if (intent != null)
                PackagesCache.requestPackageIcon(intent.getPackageName(), drawable -> onIconLoaded.accept(icon = drawable));
                //onIconLoaded.accept(icon = PackagesCache.getPackageIcon(intent.getPackageName()));
            else
                onIconLoaded.accept(null);
        } else {
            super.getIcon(onIconLoaded);
        }
    }

    @Override
    public void upgradeImgRef(int prevVersion) {
        super.upgradeImgRef(prevVersion);
        if (iconLoc == null) {
            if (type == Type.app) {
                iconLoc = DataRef.from(obj, DataLocation.pkg);
            } else if (type == Type.assoc) {
                IntentLaunchData launchData = ShortcutsCache.getIntent((UUID)obj);
                if (launchData != null)
                    iconLoc = DataRef.from(launchData.getPackageName(), DataLocation.pkg);
            }
        }
    }

    public boolean isInnerSettingType(Type type) {
        return type == Type.appInfo ||
                type == Type.saveLogs ||
                type == Type.settings ||
                type == Type.addExplorer ||
                type == Type.addAppOuter ||
                type == Type.setImageBg ||
                type == Type.setShaderBg ||
                type == Type.setUiScale ||
                type == Type.setSystemUi ||
                type == Type.setAudioVolume ||
                type == Type.setControls ||
                type == Type.addAssocOuter ||
                type == Type.createAssoc;
    }

    @Override
    public String getTitle() {
//        if (type == Type.assoc) {
//            IntentLaunchData intent = ShortcutsCache.getIntent((UUID)obj);
//            return intent != null ? intent.getDisplayName() : "Missing";
//        } else if
        if (type == Type.addAssoc) {
            IntentLaunchData intent = ShortcutsCache.getIntent((UUID)obj);
            if (intent == null)
                return "Missing";
            ResolveInfo rsv = PackagesCache.getResolveInfo(intent.getPackageName());
            String pkgLabel;
            if (rsv != null)
                pkgLabel = PackagesCache.getAppLabel(rsv);
            else
                pkgLabel = "not_installed";
            return intent.getDisplayName() + " (" + pkgLabel + ")";
        }
        return super.getTitle();
    }

    public void reload() {
        if (type == Type.assoc) {
            IntentLaunchData intent = ShortcutsCache.getIntent((UUID) obj);
            if (intent != null)
                innerItems = generateInnerItemsFrom(Type.assocExe, extraData, intent);
        }
        innerItemsLoaded = true;
    }
    @Override
    public XMBItem getInnerItem(int index) {
        if (!innerItemsLoaded && (innerItems == null || innerItems.size() <= 0))
            reload();
        return super.getInnerItem(index);
    }
    @Override
    public boolean hasInnerItems() {
        if (!innerItemsLoaded && (innerItems == null || innerItems.size() <= 0))
            reload();
        return super.hasInnerItems();
    }
    @Override
    public int getInnerItemCount() {
        if (!innerItemsLoaded && (innerItems == null || innerItems.size() <= 0))
            reload();
        return super.getInnerItemCount();
    }

    private static List<XMBItem> generateInnerItemsFrom(Type type, ArrayList<String> dirs, IntentLaunchData intent) {
        if (dirs != null && dirs.size() > 0) {
            // gets the files in the directories as streams then flattens them into one stream then maps them to home items then sorts them then turns the resulting stream into a list
            return dirs.stream().flatMap(dir -> AndroidHelpers.getItemsInDirWithExt(dir, intent.getExtensions()).stream()).map(exe -> new HomeItem(new Executable(intent.getId(), exe.toString()), type, AndroidHelpers.removeExtension(exe.getName()))).sorted(Comparator.comparing(item -> item.title)).collect(Collectors.toList());
        }
        return null;
    }

    @Override
    public String toString() {
        return "title: " + title + " item type: " + type.toString() + " type of obj: " + (obj != null ? obj.getClass() : "null");
    }
//    @Override
//    public Drawable getSuperIcon() {
//        Drawable icon = null;
//        if (type == Type.assoc)
//            icon = ContextCompat.getDrawable(OxShellApp.getCurrentActivity(), R.drawable.ic_baseline_view_list_24);
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
