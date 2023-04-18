package com.OxGames.OxShell.Data;

import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;

import androidx.core.content.ContextCompat;

import com.OxGames.OxShell.Helpers.AndroidHelpers;
import com.OxGames.OxShell.Helpers.MathHelpers;
import com.OxGames.OxShell.Interfaces.DirsCarrier;
import com.OxGames.OxShell.Interfaces.QuadConsumer;
import com.OxGames.OxShell.Interfaces.TriConsumer;
import com.OxGames.OxShell.OxShellApp;
import com.OxGames.OxShell.R;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class HomeItem<T> extends XMBItem<T> implements DirsCarrier {
    public enum Type { explorer, musicTree, musicFolder, musicArtist, musicAlbum, musicTrack, addMusicFolder, addExplorer, app, addAppOuter, addApp, assoc, addAssocOuter, addAssoc, createAssoc, assocExe, setImageBg, setShaderBg, setUiScale, setSystemUi, setAudioVolume, settings, nonDescriptSetting, setControls, appInfo, saveLogs, placeholder, }
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
                type == Type.addMusicFolder ||
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

    public boolean isColumnHead() {
        return type == Type.musicTree || type == Type.musicFolder || type == Type.musicAlbum || type == Type.musicArtist || type == HomeItem.Type.assoc || type == HomeItem.Type.settings;
    }
    public void reload(Runnable onReloaded) {
        //Log.d("HomeItem", "reload " + title);
        if (type == Type.assoc) {
            innerItemsLoaded = true;
            IntentLaunchData intent = ShortcutsCache.getIntent((UUID) obj);
            if (intent != null)
                innerItems = generateInnerItemsFrom(Type.assocExe, extraData, intent);
            if (onReloaded != null)
                onReloaded.run();
        } else if (type == Type.musicTree) {
            innerItemsLoaded = true;
            innerItems = new ArrayList<>();
            // TODO: clear cached images of previous inner items before reloading
            innerItems.add(new HomeItem(Type.placeholder, "Loading...", DataRef.from(ResImage.get(R.drawable.ic_baseline_block_24).getId(), DataLocation.resource)));
            generateMusicTree(musicItems -> {
                innerItems = musicItems;
                if (onReloaded != null)
                    onReloaded.run();
            }, getDirsList());
        } else if (type == Type.settings) {
            innerItems = generateSettings();
            if (onReloaded != null)
                onReloaded.run();
        }
    }
    @Override
    public XMBItem getInnerItem(int index) {
        //Log.d("HomeItem", "getInnerItem of " + title);
        //if (!innerItemsLoaded && (innerItems == null || innerItems.size() <= 0))
        if (type == Type.settings && (innerItems == null || innerItems.size() <= 0))
            reload(null);
        return super.getInnerItem(index);
    }
    @Override
    public boolean hasInnerItems() {
        //Log.d("HomeItem", "hasInnerItem of " + title);
        //if (!innerItemsLoaded && (innerItems == null || innerItems.size() <= 0))
        // had to switch over to just settings checking if it needs a reload since
        // music was being loaded twice and caching album art twice
        // plus this seems to make sense, only the things that don't get saved need a reload
        if (type == Type.settings && (innerItems == null || innerItems.size() <= 0))
            reload(null);
        return super.hasInnerItems();
    }
    @Override
    public int getInnerItemCount() {
        //Log.d("HomeItem", "getInnerItemCount of " + title);
        //if (!innerItemsLoaded && (innerItems == null || innerItems.size() <= 0))
        if (type == Type.settings && (innerItems == null || innerItems.size() <= 0))
            reload(null);
        return super.getInnerItemCount();
    }

    private static List<XMBItem> generateInnerItemsFrom(Type type, ArrayList<String> dirs, IntentLaunchData intent) {
        if (dirs != null && dirs.size() > 0) {
            // gets the files in the directories as streams then flattens them into one stream then maps them to home items then sorts them then turns the resulting stream into a list
            return dirs.stream().flatMap(dir -> AndroidHelpers.getItemsInDirWithExt(dir, intent.getExtensions()).stream()).map(exe -> new HomeItem(new Executable(intent.getId(), exe.toString()), type, AndroidHelpers.removeExtension(exe.getName()), DataRef.from(ResImage.get(R.drawable.ic_baseline_auto_awesome_24).getId(), DataLocation.resource))).sorted(Comparator.comparing(item -> item.title)).collect(Collectors.toList());
        }
        return null;
    }
    private static void generateMusicTree(Consumer<List<XMBItem>> onGenerated, String... dirs) {
        new Thread(() -> {
            // TODO: include sort type (by folder, by artist, by album, by artist and album)
            //HashMap<String, ArrayList<String>> allMusicPaths = new HashMap<>();
            List<String> allMusicPaths = new ArrayList<>();
            Consumer<String> addIfMusic = (path) -> {
                String pathCmp = path.toLowerCase();
                if (pathCmp.endsWith(".mp3") || pathCmp.endsWith(".flac")) {
                    //Log.d("HomeItem", path + " is music");
                    // add to music
                    allMusicPaths.add(path);
//                    String key = parent != null ? (new File(parent)).getName() : null;
//                    if (!allMusicPaths.containsKey(key))
//                        allMusicPaths.put(key, new ArrayList<>());
//                    allMusicPaths.get(key).add(path);
                }
            };
            Consumer<String> lookInsideOf = new Consumer<String>() {
                @Override
                public void accept(String path) {
                    //Log.d("HomeItem", "Entering " + path);
                    File f = new File(path);
                    if (f.isDirectory()) {
                        String[] contents = f.list();
                        if (contents != null) {
                            //Log.d("HomeItem", "Contains " + Arrays.toString(contents));
                            for (String innerPath : contents) {
                                String fullInnerPath = AndroidHelpers.combinePaths(path, innerPath);
                                if (AndroidHelpers.isDirectory(fullInnerPath))
                                    accept(fullInnerPath);
                                else
                                    addIfMusic.accept(fullInnerPath);
                            }
                        }
                    } else
                        addIfMusic.accept(path);
                }
            };
            for (String path : dirs)
                lookInsideOf.accept(path);

            // sorted by folder by default
            List<XMBItem> innerMusic = new ArrayList<>();
            try {
                // TODO: sort artists by name, sort albums by year, sort tracks by number
                HashMap<String, HomeItem> albums = new HashMap<>();
                HashMap<String, HomeItem> artists = new HashMap<>();
                BiConsumer<String, HomeItem> placeIntoArtist = (artist, item) -> {
                    if (!artists.containsKey(artist)) {
                        HomeItem artistItem = new HomeItem(Type.musicArtist, artist, DataRef.from(ResImage.get(R.drawable.baseline_person_24).getId(), DataLocation.resource));
                        artists.put(artist, artistItem);
                        innerMusic.add(artistItem);
                    }
                    artists.get(artist).add(item);
                };
                QuadConsumer<Bitmap, String, String, HomeItem> placeIntoAlbum = (albumArt, artist, album, track) -> {
                    if (!albums.containsKey(album)) {
                        String albumArtPath = null;
                        if (albumArt != null) {
                            UUID uuid = UUID.randomUUID();
                            AndroidHelpers.saveBitmapToFile(albumArt, albumArtPath = AndroidHelpers.combinePaths(Paths.HOME_ITEMS_DIR_INTERNAL, uuid.toString()));
                            //Log.d("HomeItem", album + " has album art, saving to " + albumArtPath);
                        }
                        HomeItem albumItem = new HomeItem(Type.musicAlbum, album, DataRef.from(albumArtPath != null ? albumArtPath : ResImage.get(R.drawable.baseline_library_music_24).getId(), albumArtPath != null ? DataLocation.file : DataLocation.resource));
                        albums.put(album, albumItem);
                        placeIntoArtist.accept(artist, albumItem);
                    }
                    albums.get(album).add(track);
                };
                for (String trackPath : allMusicPaths) {
                    Thread.sleep(MathHelpers.calculateMillisForFps(30));
                    //Log.d("HomeItem", "Looking into " + trackPath);
                    Metadata metadata = Metadata.getMediaMetadata(DataRef.from(trackPath, DataLocation.file));
                    HomeItem track = new HomeItem(trackPath, Type.musicTrack, (metadata.getTitle() == null || metadata.getTitle().isEmpty()) ? AndroidHelpers.removeExtension((new File(trackPath)).getName()) : metadata.getTitle(), DataRef.from(ResImage.get(R.drawable.ic_baseline_audio_file_24).getId(), DataLocation.resource));
                    String artist = metadata.getArtist() == null || metadata.getArtist().isEmpty() ? "Various Artists" : metadata.getArtist();
                    if (metadata.getAlbum() != null && !metadata.getAlbum().isEmpty()) {
                        //Log.d("HomeItem", "Placing " + trackPath + " into album: " + metadata.getAlbum());
                        placeIntoAlbum.accept(metadata.getAlbumArt(), artist, metadata.getAlbum(), track);
                    } else {
                        //Log.d("HomeItem", "Placing " + trackPath + " into artist: " + artist);
                        placeIntoArtist.accept(artist, track);
                    }
                }
            } catch (Exception e) {
                Log.e("HomeItem", "Failed to read all music: " + e);
            }

            //return innerMusic;
            OxShellApp.getCurrentActivity().runOnUiThread(() -> {
                if (onGenerated != null)
                    onGenerated.accept(innerMusic);
            });
        }).start();
    }
    private static List<XMBItem> generateSettings() {
        ArrayList<XMBItem> settingsItems = new ArrayList<>();
        List<XMBItem> innerSettings = new ArrayList<>();
        List<XMBItem> innerInnerSettings = new ArrayList<>();

        // TODO: add option to change icon alpha
        // TODO: add option to reset home items to default
        // TODO: move add association to home settings?

        XMBItem currentSettingsItem;
        innerSettings.clear();
        innerSettings.add(new HomeItem(Type.addExplorer, "Add explorer item to home"));
        innerSettings.add(new HomeItem(Type.addAppOuter, "Add application to home"));
        innerSettings.add(new HomeItem(Type.addMusicFolder, "Add music from directory to home"));
        currentSettingsItem = new HomeItem(Type.nonDescriptSetting, "Home", DataRef.from("ic_baseline_home_24", DataLocation.resource), innerSettings.toArray(new XMBItem[0]));
        settingsItems.add(currentSettingsItem);

        innerSettings.clear();
        innerSettings.add(new HomeItem(Type.setImageBg, "Set picture as background"));
        innerSettings.add(new HomeItem(Type.setShaderBg, "Set shader as background"));
        innerSettings.add(new HomeItem(Type.setUiScale, "Change UI scale"));
        if (!AndroidHelpers.isRunningOnTV())
            innerSettings.add(new HomeItem(Type.setSystemUi, "Change system UI visibility"));
        currentSettingsItem = new HomeItem(Type.nonDescriptSetting, "Display", DataRef.from("ic_baseline_image_24", DataLocation.resource), innerSettings.toArray(new XMBItem[0]));
        settingsItems.add(currentSettingsItem);

        innerSettings.clear();
        innerSettings.add(new HomeItem(Type.setAudioVolume, "Set volume levels"));
        currentSettingsItem = new HomeItem(Type.nonDescriptSetting, "Audio", DataRef.from("ic_baseline_headphones_24", DataLocation.resource), innerSettings.toArray(new XMBItem[0]));
        settingsItems.add(currentSettingsItem);

        innerSettings.clear();
        innerInnerSettings.clear();
        innerInnerSettings.add(new HomeItem(SettingsKeeper.PRIMARY_INPUT, Type.setControls, "Change primary input"));
        innerInnerSettings.add(new HomeItem(SettingsKeeper.SUPER_PRIMARY_INPUT, Type.setControls, "Change super primary input"));
        innerInnerSettings.add(new HomeItem(SettingsKeeper.SECONDARY_INPUT, Type.setControls, "Change secondary input"));
        innerInnerSettings.add(new HomeItem(SettingsKeeper.CANCEL_INPUT, Type.setControls, "Change cancel input"));
        innerInnerSettings.add(new HomeItem(SettingsKeeper.NAVIGATE_UP, Type.setControls, "Change navigate up input"));
        innerInnerSettings.add(new HomeItem(SettingsKeeper.NAVIGATE_DOWN, Type.setControls, "Change navigate down input"));
        innerInnerSettings.add(new HomeItem(SettingsKeeper.NAVIGATE_LEFT, Type.setControls, "Change navigate left input"));
        innerInnerSettings.add(new HomeItem(SettingsKeeper.NAVIGATE_RIGHT, Type.setControls, "Change navigate right input"));
        innerInnerSettings.add(new HomeItem(SettingsKeeper.SHOW_DEBUG_INPUT, Type.setControls, "Change show debug view input"));
        innerSettings.add(new HomeItem(Type.nonDescriptSetting, "General", DataRef.from("ic_baseline_home_24", DataLocation.resource), innerInnerSettings.toArray(new XMBItem[0])));
        innerInnerSettings.clear();
        innerInnerSettings.add(new HomeItem(SettingsKeeper.EXPLORER_GO_UP_INPUT, Type.setControls, "Change go up input"));
        innerInnerSettings.add(new HomeItem(SettingsKeeper.EXPLORER_GO_BACK_INPUT, Type.setControls, "Change go back input"));
        innerInnerSettings.add(new HomeItem(SettingsKeeper.EXPLORER_HIGHLIGHT_INPUT, Type.setControls, "Change highlight input"));
        innerInnerSettings.add(new HomeItem(SettingsKeeper.EXPLORER_EXIT_INPUT, Type.setControls, "Change exit input"));
        innerSettings.add(new HomeItem(Type.nonDescriptSetting, "File Explorer", DataRef.from("ic_baseline_source_24", DataLocation.resource), innerInnerSettings.toArray(new XMBItem[0])));
        innerInnerSettings.clear();
        innerInnerSettings.add(new HomeItem(SettingsKeeper.HOME_COMBOS, Type.setControls, "Change go home input"));
        innerInnerSettings.add(new HomeItem(SettingsKeeper.RECENTS_COMBOS, Type.setControls, "Change view recent apps input"));
        innerSettings.add(new HomeItem(Type.nonDescriptSetting, "Android System", DataRef.from("baseline_adb_24", DataLocation.resource), innerInnerSettings.toArray(new XMBItem[0])));
        currentSettingsItem = new HomeItem(Type.nonDescriptSetting, "Controls", DataRef.from("ic_baseline_games_24", DataLocation.resource), innerSettings.toArray(new XMBItem[0]));
        settingsItems.add(currentSettingsItem);

        innerSettings.clear();
        innerSettings.add(new HomeItem(Type.addAssocOuter, "Add association to home"));
        innerSettings.add(new HomeItem(Type.createAssoc, "Create new association"));
        currentSettingsItem = new HomeItem(Type.nonDescriptSetting, "Associations", DataRef.from("ic_baseline_send_time_extension_24", DataLocation.resource), innerSettings.toArray(new XMBItem[0]));
        settingsItems.add(currentSettingsItem);

        innerSettings.clear();
        innerSettings.add(new HomeItem(Type.appInfo, "App info"));
        innerSettings.add(new HomeItem(Type.saveLogs, "Save logs to file"));
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
