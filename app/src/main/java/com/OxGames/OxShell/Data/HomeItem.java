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
    public enum Type { explorer, addExplorer, app, addAppOuter, addApp, assoc, addAssocOuter, addAssoc, createAssoc, assocExe, setImageBg, setShaderBg, setUiScale, setSystemUi, setAudioVolume, settings, nonDescriptSetting, setControls, appInfo, saveLogs, }
    public Type type;
    public ArrayList<String> extraData;
    private boolean innerItemsLoaded;

    public HomeItem(Type _type) {
        this(_type, null);
    }
    public HomeItem(Type _type, String _title) {
        this(null, _type, _title);
    }
    public HomeItem(T _obj, Type _type, String _title, DataRef _iconLoc, XMBItem... innerItems) {
        super(_obj, _title, _iconLoc, innerItems);
        type = _type;
        extraData = new ArrayList<>();
    }
    public HomeItem(Type _type, String _title, XMBItem... innerItems) {
        this(null, _type, _title, null, innerItems);
    }
    public HomeItem(Type _type, String _title, DataRef _iconLoc) {
        this(null, _type, _title, _iconLoc);
    }
    public HomeItem(Type _type, String _title, DataRef _iconLoc, XMBItem... innerItems) {
        this(null, _type, _title, _iconLoc, innerItems);
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
        //if (type == Type.explorer)
        //    onIconLoaded.accept(icon = ContextCompat.getDrawable(OxShellApp.getContext(), R.drawable.ic_baseline_source_24));
        if (type == Type.addApp)// || type == Type.app)
            PackagesCache.requestPackageIcon((String) obj, drawable -> onIconLoaded.accept(icon = drawable));
        else if (isInnerSettingType(type))
            onIconLoaded.accept(icon = ContextCompat.getDrawable(OxShellApp.getContext(), R.drawable.ic_baseline_construction_24));
        //else if (type == Type.assocExe)
        //    onIconLoaded.accept(icon = ContextCompat.getDrawable(OxShellApp.getContext(), R.drawable.ic_baseline_auto_awesome_24));
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
                if (launchData != null) {
                    iconLoc = DataRef.from(launchData.getPackageName(), DataLocation.pkg);
                    title = launchData.getDisplayName();
                }
            } else if (type == Type.assocExe) {
                iconLoc = DataRef.from(ResImage.get(R.drawable.ic_baseline_auto_awesome_24).getId(), DataLocation.resource);
            } else if (type == Type.explorer) {
                iconLoc = DataRef.from(ResImage.get(R.drawable.ic_baseline_source_24).getId(), DataLocation.resource);
            }
        }
    }

    public static boolean isInnerSettingType(Type type) {
        return type == Type.appInfo ||
                type == Type.saveLogs ||
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
    public static boolean isSetting(Type type) {
        return type == Type.settings || type == Type.nonDescriptSetting || isInnerSettingType(type);
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
            innerItemsLoaded = true;
        } else if (type == Type.settings) {
            innerItems = generateSettings();
        }
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
            return dirs.stream().flatMap(dir -> AndroidHelpers.getItemsInDirWithExt(dir, intent.getExtensions()).stream()).map(exe -> new HomeItem(new Executable(intent.getId(), exe.toString()), type, AndroidHelpers.removeExtension(exe.getName()), DataRef.from(ResImage.get(R.drawable.ic_baseline_auto_awesome_24).getId(), DataLocation.resource))).sorted(Comparator.comparing(item -> item.title)).collect(Collectors.toList());
        }
        return null;
    }
    private static List<XMBItem> generateSettings() {
        ArrayList<XMBItem> settingsItems = new ArrayList<>();
        List<XMBItem> innerSettings = new ArrayList<>();
        List<XMBItem> innerInnerSettings = new ArrayList<>();

        //XMBItem settingsItem = new XMBItem(null, "Settings", DataRef.from("ic_baseline_settings_24", DataLocation.resource));//, colIndex, localIndex++);
        //settingsColumn.add(settingsItem);

        // TODO: add option to change icon alpha
        // TODO: add option to reset home items to default
        // TODO: move add association to home settings?
//        innerSettings = new XMBItem[2];
//        innerSettings[0] = new HomeItem(HomeItem.Type.settings, "Set font size");
//        innerSettings[1] = new HomeItem(HomeItem.Type.settings, "Set typeface");
//        settingsItem = new XMBItem(null, "General", R.drawable.ic_baseline_view_list_24, innerSettings);
//        settingsColumn.add(settingsItem);

        XMBItem currentSettingsItem;
        innerSettings.clear();
        innerSettings.add(new HomeItem(HomeItem.Type.addExplorer, "Add explorer item to home"));
//        List<ResolveInfo> apps = PackagesCache.getLaunchableInstalledPackages();
//        List<XMBItem> sortedApps = apps.stream().map(currentPkg -> new HomeItem(currentPkg.activityInfo.packageName, HomeItem.Type.addApp, PackagesCache.getAppLabel(currentPkg))).collect(Collectors.toList());
//        sortedApps.sort(Comparator.comparing(o -> o.getTitle().toLowerCase()));
        innerSettings.add(new HomeItem(HomeItem.Type.addAppOuter, "Add application to home"));
        //innerSettings[2] = new HomeItem(HomeItem.Type.settings, "Add new column to home");
        currentSettingsItem = new HomeItem(Type.nonDescriptSetting, "Home", DataRef.from("ic_baseline_home_24", DataLocation.resource), innerSettings.toArray(new XMBItem[0]));
        settingsItems.add(currentSettingsItem);

        innerSettings.clear();
        innerSettings.add(new HomeItem(HomeItem.Type.setImageBg, "Set picture as background"));
        innerSettings.add(new HomeItem(HomeItem.Type.setShaderBg, "Set shader as background"));
        innerSettings.add(new HomeItem(HomeItem.Type.setUiScale, "Change UI scale"));
        if (!AndroidHelpers.isRunningOnTV())
            innerSettings.add(new HomeItem(HomeItem.Type.setSystemUi, "Change system UI visibility"));
        currentSettingsItem = new HomeItem(Type.nonDescriptSetting, "Display", DataRef.from("ic_baseline_image_24", DataLocation.resource), innerSettings.toArray(new XMBItem[0]));
        settingsItems.add(currentSettingsItem);

        innerSettings.clear();
        innerSettings.add(new HomeItem(HomeItem.Type.setAudioVolume, "Set volume levels"));
        currentSettingsItem = new HomeItem(Type.nonDescriptSetting, "Audio", DataRef.from("ic_baseline_headphones_24", DataLocation.resource), innerSettings.toArray(new XMBItem[0]));
        settingsItems.add(currentSettingsItem);

        innerSettings.clear();
        innerInnerSettings.clear();
        innerInnerSettings.add(new HomeItem(SettingsKeeper.PRIMARY_INPUT, HomeItem.Type.setControls, "Change primary input"));
        innerInnerSettings.add(new HomeItem(SettingsKeeper.SUPER_PRIMARY_INPUT, HomeItem.Type.setControls, "Change super primary input"));
        innerInnerSettings.add(new HomeItem(SettingsKeeper.SECONDARY_INPUT, HomeItem.Type.setControls, "Change secondary input"));
        innerInnerSettings.add(new HomeItem(SettingsKeeper.CANCEL_INPUT, HomeItem.Type.setControls, "Change cancel input"));
        innerInnerSettings.add(new HomeItem(SettingsKeeper.NAVIGATE_UP, HomeItem.Type.setControls, "Change navigate up input"));
        innerInnerSettings.add(new HomeItem(SettingsKeeper.NAVIGATE_DOWN, HomeItem.Type.setControls, "Change navigate down input"));
        innerInnerSettings.add(new HomeItem(SettingsKeeper.NAVIGATE_LEFT, HomeItem.Type.setControls, "Change navigate left input"));
        innerInnerSettings.add(new HomeItem(SettingsKeeper.NAVIGATE_RIGHT, HomeItem.Type.setControls, "Change navigate right input"));
        innerInnerSettings.add(new HomeItem(SettingsKeeper.SHOW_DEBUG_INPUT, HomeItem.Type.setControls, "Change show debug view input"));
        innerSettings.add(new HomeItem(Type.nonDescriptSetting, "General", DataRef.from("ic_baseline_home_24", DataLocation.resource), innerInnerSettings.toArray(new XMBItem[0])));
        innerInnerSettings.clear();
        innerInnerSettings.add(new HomeItem(SettingsKeeper.EXPLORER_GO_UP_INPUT, HomeItem.Type.setControls, "Change go up input"));
        innerInnerSettings.add(new HomeItem(SettingsKeeper.EXPLORER_GO_BACK_INPUT, HomeItem.Type.setControls, "Change go back input"));
        innerInnerSettings.add(new HomeItem(SettingsKeeper.EXPLORER_HIGHLIGHT_INPUT, HomeItem.Type.setControls, "Change highlight input"));
        innerInnerSettings.add(new HomeItem(SettingsKeeper.EXPLORER_EXIT_INPUT, HomeItem.Type.setControls, "Change exit input"));
        innerSettings.add(new HomeItem(Type.nonDescriptSetting, "File Explorer", DataRef.from("ic_baseline_source_24", DataLocation.resource), innerInnerSettings.toArray(new XMBItem[0])));
        innerInnerSettings.clear();
        innerInnerSettings.add(new HomeItem(SettingsKeeper.HOME_COMBOS, HomeItem.Type.setControls, "Change go home input"));
        innerInnerSettings.add(new HomeItem(SettingsKeeper.RECENTS_COMBOS, HomeItem.Type.setControls, "Change view recent apps input"));
        innerSettings.add(new HomeItem(Type.nonDescriptSetting, "Android System", DataRef.from("baseline_adb_24", DataLocation.resource), innerInnerSettings.toArray(new XMBItem[0])));
        currentSettingsItem = new HomeItem(Type.nonDescriptSetting, "Controls", DataRef.from("ic_baseline_games_24", DataLocation.resource), innerSettings.toArray(new XMBItem[0]));
        settingsItems.add(currentSettingsItem);

        //innerSettings = new XMBItem[0];
        //settingsItem = new XMBItem(null, "Explorer", R.drawable.ic_baseline_source_24, colIndex, localIndex++, innerSettings);
        //settingsColumn.add(settingsItem);

        innerSettings.clear();
//        IntentLaunchData[] intents = ShortcutsCache.getStoredIntents();
//        XMBItem[] intentItems = new XMBItem[intents.length];
//        for (int i = 0; i < intents.length; i++)
//            intentItems[i] = new HomeItem(intents[i].getId(), HomeItem.Type.addAssoc);
        innerSettings.add(new HomeItem(HomeItem.Type.addAssocOuter, "Add association to home"));
        innerSettings.add(new HomeItem(HomeItem.Type.createAssoc, "Create new association"));
        currentSettingsItem = new HomeItem(Type.nonDescriptSetting, "Associations", DataRef.from("ic_baseline_send_time_extension_24", DataLocation.resource), innerSettings.toArray(new XMBItem[0]));
        settingsItems.add(currentSettingsItem);

        innerSettings.clear();
        innerSettings.add(new HomeItem(HomeItem.Type.appInfo, "App info"));
        innerSettings.add(new HomeItem(HomeItem.Type.saveLogs, "Save logs to file"));
        currentSettingsItem = new HomeItem(Type.nonDescriptSetting, "About", DataRef.from("baseline_info_24", DataLocation.resource), innerSettings.toArray(new XMBItem[0]));
        settingsItems.add(currentSettingsItem);
        return settingsItems;
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
