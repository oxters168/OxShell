package com.OxGames.OxShell.Data;

import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;

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
    public enum Type { explorer, addExplorer, app, addAppOuter, addApp, assoc, addAssocOuter, addAssoc, createAssoc, assocExe, setImageBg, setShaderBg, settings, }
    public Type type;
    public ArrayList<String> extraData;

    public HomeItem(Type _type) {
        this(_type, null);
    }
    public HomeItem(Type _type, String _title) {
        this(_type, _title, null);
    }
    public HomeItem(T _obj, Type _type, String _title, XMBItem... innerItems) {
        super(_obj, _title, innerItems);
        type = _type;
        extraData = new ArrayList<>();
    }
    public HomeItem(Type _type, String _title, XMBItem... innerItems) {
        this(null, _type, _title, innerItems);
    }
    public HomeItem(T _obj, Type _type, XMBItem... innerItems) {
        this(_obj, _type, null, innerItems);
    }
    @Override
    public void getIcon(Consumer<Drawable> onIconLoaded) {
        //Drawable icon = null;
        if (type == Type.explorer)
            onIconLoaded.accept(icon = ContextCompat.getDrawable(OxShellApp.getContext(), R.drawable.ic_baseline_source_24));
        else if (type == Type.app || type == Type.addApp)
            PackagesCache.requestPackageIcon((String) obj, drawable -> {
                onIconLoaded.accept(icon = drawable);
            });
        else if (type == Type.settings || type == Type.addExplorer || type == Type.addAppOuter || type == Type.setImageBg || type == Type.setShaderBg || type == Type.addAssocOuter || type == Type.createAssoc)
            onIconLoaded.accept(icon = ContextCompat.getDrawable(OxShellApp.getContext(), R.drawable.ic_baseline_construction_24));
        else if (type == Type.assocExe)
            onIconLoaded.accept(icon = ContextCompat.getDrawable(OxShellApp.getContext(), R.drawable.ic_baseline_auto_awesome_24));
        else if (type == Type.assoc || type == Type.addAssoc) {
            IntentLaunchData intent = ShortcutsCache.getIntent((UUID)obj);
            if (intent != null)
                onIconLoaded.accept(icon = PackagesCache.getPackageIcon(intent.getPackageName()));
            else
                onIconLoaded.accept(null);
        } else {
            super.getIcon(onIconLoaded);
        }
//        icon = super.getIcon();
//        if (icon == null) {
//            if (type == Type.explorer)
//                icon = ContextCompat.getDrawable(OxShellApp.getContext(), R.drawable.ic_baseline_source_24);
//            else if (type == Type.app || type == Type.addApp)
//                icon = PackagesCache.getPackageIcon((String)obj);
//            else if (type == Type.settings || type == Type.addExplorer || type == Type.setImageBg || type == Type.setShaderBg)
//                icon = ContextCompat.getDrawable(OxShellApp.getContext(), R.drawable.ic_baseline_construction_24);
//            else if (type == Type.assocExe)
//                icon = ContextCompat.getDrawable(OxShellApp.getContext(), R.drawable.ic_baseline_auto_awesome_24);
//            else if (type == Type.assoc || type == Type.addAssoc) {
//                IntentLaunchData intent = ShortcutsCache.getIntent((UUID)obj);
//                if (intent == null)
//                    return null;
//                icon = PackagesCache.getPackageIcon(intent.getPackageName());
//            }
//        }
//        return icon;
    }

    @Override
    public String getTitle() {
        if (type == Type.assoc) {
            IntentLaunchData intent = ShortcutsCache.getIntent((UUID)obj);
            return intent != null ? intent.getDisplayName() : "Missing";
        } else if (type == Type.addAssoc) {
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

    @Override
    public XMBItem getInnerItem(int index) {
        if (type == Type.assoc) {
            if (innerItems == null || innerItems.size() <= 0) {
                IntentLaunchData intent = ShortcutsCache.getIntent((UUID)obj);
                if (intent == null)
                    return null;
                innerItems = generateInnerItemsFrom(Type.assocExe, extraData, intent.getExtensions());
            }
        }
        return super.getInnerItem(index);
    }
    @Override
    public boolean hasInnerItems() {
        if (type == Type.assoc) {
            if (innerItems == null || innerItems.size() <= 0) {
                IntentLaunchData intent = ShortcutsCache.getIntent((UUID)obj);
                if (intent == null)
                    return false;
                innerItems = generateInnerItemsFrom(Type.assocExe, extraData, intent.getExtensions());
            }
        }
        return super.hasInnerItems();
    }
    @Override
    public int getInnerItemCount() {
        if (type == Type.assoc) {
            if (innerItems == null || innerItems.size() <= 0) {
                IntentLaunchData intent = ShortcutsCache.getIntent((UUID)obj);
                if (intent == null)
                    return 0;
                innerItems = generateInnerItemsFrom(Type.assocExe, extraData, intent.getExtensions());
            }
        }
        return super.getInnerItemCount();
    }

    private static List<XMBItem> generateInnerItemsFrom(Type type, ArrayList<String> dirs, String[] extensions) {
        if (dirs != null && dirs.size() > 0)
            // gets the files in the directories as streams then flattens them into one stream then maps them to home items then sorts them then turns the resulting stream into a list
            return dirs.stream().flatMap(dir -> AndroidHelpers.getItemsInDirWithExt(dir, extensions).stream()).map(exe -> new HomeItem(exe.toString(), type, AndroidHelpers.removeExtension(exe.getName()))).sorted(Comparator.comparing(item -> item.title)).collect(Collectors.toList());
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
