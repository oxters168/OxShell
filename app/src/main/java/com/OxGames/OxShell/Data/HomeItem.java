package com.OxGames.OxShell.Data;

import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.OxGames.OxShell.Helpers.AndroidHelpers;
import com.OxGames.OxShell.Helpers.ExplorerBehaviour;
import com.OxGames.OxShell.Helpers.MathHelpers;
import com.OxGames.OxShell.Interfaces.DirsCarrier;
import com.OxGames.OxShell.Interfaces.TriConsumer;
import com.OxGames.OxShell.OxShellApp;
import com.OxGames.OxShell.R;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import kotlin.jvm.functions.Function2;

public class HomeItem<T> extends XMBItem<T> implements DirsCarrier {
    public enum Type { explorer, musicTree, musicFolder, musicArtist, musicAlbum, musicTrack, videoTree, videoTrack, addMusicFolder, addVideoFolder, addExplorer, app, addAppOuter, addApp, resetHomeItems, assoc, addAssocOuter, addAssoc, createAssoc, assocExe, setImageBg, setShaderBg, setUiScale, setSystemUi, setAudioVolume, settings, nonDescriptSetting, setControls, appInfo, saveLogs, placeholder, }
    public Type type;
    public ArrayList<String> extraData;
    //private boolean innerItemsLoaded;
    private transient Thread musicGenThread = null;

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

    @NonNull
    @Override
    public Object clone() {
        DataRef origImg = getImgRef();
        HomeItem<T> other = new HomeItem<>(obj, type, title, origImg != null ? DataRef.from(origImg.getLoc(), origImg.getLocType()) : null);
        if (innerItems != null) {
            List<XMBItem> clonedItems = new ArrayList<>();
            for (int i = 0; i < innerItems.size(); i++)
                clonedItems.add((XMBItem)getInnerItem(i).clone());
            other.innerItems = clonedItems;
        }
        if (extraData != null)
            other.extraData = new ArrayList<>(extraData);
        return other;
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
                intent.getImgRef().getImage(onIconLoaded);
                //PackagesCache.requestPackageIcon(intent.getPackageName(), drawable -> onIconLoaded.accept(icon = drawable));
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
                    //iconLoc = DataRef.from(launchData.getPackageName(), DataLocation.pkg);
                    iconLoc = launchData.getImgRef();
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
                type == Type.resetHomeItems ||
                type == Type.addMusicFolder ||
                type == Type.addVideoFolder ||
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
            String pkgName = intent.getPackageName();
            String pkgLabel = null;
            if (pkgName != null && !pkgName.isEmpty()) {
                ResolveInfo rsv = PackagesCache.getResolveInfo(pkgName);
                if (rsv != null)
                    pkgLabel = PackagesCache.getAppLabel(rsv);
                else
                    pkgLabel = "not_installed";
            }
            return intent.getDisplayName() + (pkgLabel != null ? " (" + pkgLabel + ")" : "");
        }
        return super.getTitle();
    }

    public boolean isColumnHead() {
        return type == Type.musicTree || type == Type.musicFolder || type == Type.musicAlbum || type == Type.musicArtist || type == HomeItem.Type.assoc || type == HomeItem.Type.settings;
    }

    @Override
    public void release() {
        stopMusicTreeGen();
        super.release();
    }

    private void stopMusicTreeGen() {
        //Log.d("HomeItem", "Attempting to interrupt music gen for " + title);
        if (musicGenThread != null && musicGenThread.isAlive()) {
            //Log.d("HomeItem", "Interrupting music gen for " + title);
            musicGenThread.interrupt();
        }
        //else
        //    Log.d("HomeItem", "No music gen thread found for " + title);
    }

    public boolean isReloadable() {
        return type == Type.assoc || type == Type.musicTree || type == Type.settings;
    }
    public void reload(Runnable onReloaded) {
        //Log.d("HomeItem", "reload " + title);
        if (type == Type.assoc) {
            //innerItemsLoaded = true;
            IntentLaunchData intent = ShortcutsCache.getIntent((UUID) obj);
            if (intent != null) {
                Log.d("HomeItem", "Reloading from " + extraData.toString());
                List<XMBItem> assocExecs = generateInnerItemsFrom(Type.assocExe, extraData, intent);
                setInnerItems(assocExecs != null ? assocExecs.toArray(new XMBItem[0]) : null);
                //innerItems = generateInnerItemsFrom(Type.assocExe, extraData, intent);
            }
            if (onReloaded != null)
                onReloaded.run();
        } else if (type == Type.musicTree) {
            //innerItemsLoaded = true;
            stopMusicTreeGen();
            clearImgCache();
            applyToInnerItems(XMBItem::release, false);
            HomeItem loadingItem = new HomeItem(Type.placeholder, "Loading...", DataRef.from(ResImage.get(R.drawable.baseline_hourglass_empty_24).getId(), DataLocation.resource));
            setInnerItems(loadingItem);
            musicGenThread = generateMusicTree((currentIndex, totalTracks, musicItems) -> {
                int percent = Math.round((currentIndex / (float)(totalTracks - 1)) * 100);
                loadingItem.setTitle("Loading (" + percent + "%)");
                if (musicItems != null) {
                    musicGenThread = null;
                    setInnerItems(musicItems.toArray(new XMBItem[0]));
                    //innerItems = musicItems;
                    if (onReloaded != null)
                        onReloaded.run();
                }
            }, getDirsList());
            musicGenThread.start();
        } else if (type == Type.videoTree) {
            setInnerItems(generateVideoTree(getDirsList()).toArray(new XMBItem[0]));
            if (onReloaded != null)
                onReloaded.run();
        } else if (type == Type.settings) {
            //innerItems = generateSettings();
            setInnerItems(generateSettings().toArray(new XMBItem[0]));
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
    private static List<XMBItem> generateVideoTree(String... dirs) {
        List<String> allVideoPaths = AndroidHelpers.getFilesInDirWithExt(true, ShortcutsCache.getVideoExtensions(), dirs);
        HashMap<String, List<String>> sortedVideos = new HashMap<>();
        for (String videoPath : allVideoPaths) {
            String parentPath = new File(videoPath).getParent();
            if (!sortedVideos.containsKey(parentPath))
                sortedVideos.put(parentPath, new ArrayList<>());
            sortedVideos.get(parentPath).add(videoPath);
        }
        return sortedVideos.entrySet().stream().map(entry -> new XMBItem(null, new File(entry.getKey()).getName(), DataRef.from(ResImage.get(R.drawable.ic_baseline_folder_24).getId(), DataLocation.resource), entry.getValue().stream().map(videoPath -> new HomeItem(videoPath, Type.videoTrack, AndroidHelpers.removeExtension(new File(videoPath).getName()), DataRef.from(ResImage.get(R.drawable.ic_baseline_video_file_24).getId(), DataLocation.resource))).toArray(HomeItem[]::new))).collect(Collectors.toList());
    }
    private static Thread generateMusicTree(TriConsumer<Integer, Integer, List<XMBItem>> onGenerated, String... dirs) {
        return new Thread(() -> {
            // TODO: include sort type (by folder, by artist, by album, by artist and album)
            List<String> allMusicPaths = AndroidHelpers.getFilesInDirWithExt(true, ShortcutsCache.getAudioExtensions(), dirs);

            // by default sorted as artist (alphabetically) -> album (by year) -> track (by number)
            List<XMBItem> innerMusic = new ArrayList<>();
            class Track {
                String trackName;
                String trackPath;
                int trackIndex;
                Track(String trackPath, String name, int index) {
                    this.trackPath = trackPath;
                    this.trackName = name;
                    this.trackIndex = index;
                }
            }
            class Album {
                String albumName;
                String albumArtPath;
                int year;
                List<Track> tracks;
                Album(String name, int year) {
                    this.albumName = name;
                    this.year = year;
                    this.tracks = new ArrayList<>();
                }
                void addTrack(Track track) {
                    tracks.add(track);
                }
            }
            class Artist {
                String artistName;
                HashMap<String, Album> albums;
                Artist(String name) {
                    this.artistName = name;
                    albums = new HashMap<>();
                }
                Album addOrGetAlbum(Album album) {
                    if (!albums.containsKey(album.albumName))
                        albums.put(album.albumName, album);
                    return albums.get(album.albumName);
                }
            }
            HashMap<String, Artist> artists = new HashMap<>();
            Runnable deleteAlbumArt = () -> {
                Log.d("HomeItem", "Music gen thread interrupted, deleting album art");
                // since music gen was interrupted, we are assuming the results will not be used
                artists.values().forEach(artist -> artist.albums.values().forEach(album -> {
                    if (album.albumArtPath != null) {
                        //Log.d("HomeItem", "Deleting " + album.albumArtPath);
                        ExplorerBehaviour.delete(album.albumArtPath);
                    }
                }));
            };
            int trackIndex = 0;
            int totalTracks = allMusicPaths.size();
            AtomicBoolean cancelled = new AtomicBoolean(false);
            try {
                Function2<String, Metadata, String> getTrackName = (trackPath, metadata) -> metadata == null || metadata.getTitle() == null || metadata.getTitle().isEmpty() ? AndroidHelpers.removeExtension((new File(trackPath)).getName()) : metadata.getTitle();
                Function<Metadata, String> getAlbumName = metadata -> metadata == null || metadata.getAlbum() == null || metadata.getAlbum().isEmpty() ? "Other" : metadata.getAlbum();
                Function<Metadata, String> getArtistName = metadata -> metadata == null || metadata.getArtist() == null || metadata.getArtist().isEmpty() ? "Various Artists" : metadata.getArtist();
                Function<String, Artist> addOrGetArtist = artistName -> {
                    if (!artists.containsKey(artistName))
                        artists.put(artistName, new Artist(artistName));
                    return artists.get(artistName);
                };
                for (; trackIndex < totalTracks; trackIndex++) {
                    cancelled.set(Thread.interrupted());
                    if (!cancelled.get()) {
                        if (onGenerated != null)
                            onGenerated.accept(trackIndex, totalTracks, null);
                        String trackPath = allMusicPaths.get(trackIndex);
                        Thread.sleep(MathHelpers.calculateMillisForFps(30));
                        Metadata metadata = Metadata.getMediaMetadata(DataRef.from(trackPath, DataLocation.file));
                        String artist = getArtistName.apply(metadata);
                        int trackNum = -1;
                        try {
                            String firstPortion = metadata.getTrackNumber();
                            if (firstPortion.contains("/"))
                                firstPortion = firstPortion.substring(0, firstPortion.indexOf("/"));
                            trackNum = Integer.parseInt(firstPortion);
                        } catch (Exception e) {}
                        int year = -1;
                        try {
                            year = Integer.parseInt(metadata.getYear());
                        } catch (Exception e) {}
                        Album album = addOrGetArtist.apply(artist).addOrGetAlbum(new Album(getAlbumName.apply(metadata), year));
                        //Log.d("HomeItem", artist + ", " + album.albumName + ", (" + metadata.getTrackNumber() + " => " + trackNum + "), " + year + ", " + trackPath);
                        Bitmap albumArt;
                        if (album.albumArtPath == null && (albumArt = metadata.getAlbumArt()) != null) {
                            String albumArtPath = AndroidHelpers.combinePaths(Paths.HOME_ITEMS_DIR_INTERNAL, UUID.randomUUID().toString());
                            AndroidHelpers.saveBitmapToFile(albumArt, albumArtPath);
                            album.albumArtPath = albumArtPath;
                        }
                        album.addTrack(new Track(trackPath, getTrackName.invoke(trackPath, metadata), trackNum));
                    } else
                        break;
                }
            } catch (Exception e) {
                Log.e("HomeItem", "Failed to read all music: " + e);
                cancelled.set(true);
                deleteAlbumArt.run();
            }
            if (!cancelled.get()) {
                Log.d("HomeItem", "Music gen thread sending back inner items since it was not interrupted");
                innerMusic.addAll(
                    artists.values().stream().sorted(Comparator.comparing(artist -> artist.artistName)).map(artist ->
                        new HomeItem(Type.musicArtist, artist.artistName, DataRef.from(ResImage.get(R.drawable.baseline_person_24).getId(), DataLocation.resource), artist.albums.values().stream().sorted(Comparator.comparingInt(album -> album.year)).map(album ->
                        new HomeItem(Type.musicAlbum, album.albumName, DataRef.from(album.albumArtPath != null ? album.albumArtPath : ResImage.get(R.drawable.ic_baseline_hide_image_24).getId(), album.albumArtPath != null ? DataLocation.file : DataLocation.resource), album.tracks.stream().sorted(Comparator.comparingInt(track -> track.trackIndex)).map(track ->
                        new HomeItem(track.trackPath, Type.musicTrack, track.trackName, DataRef.from(ResImage.get(R.drawable.ic_baseline_audio_file_24).getId(), DataLocation.resource))).toArray(HomeItem[]::new))).toArray(HomeItem[]::new))).collect(Collectors.toList())
                );
                int returnIndex = trackIndex;
                OxShellApp.getCurrentActivity().runOnUiThread(() -> {
                    if (onGenerated != null)
                        onGenerated.accept(returnIndex, totalTracks, innerMusic);
                });
            } else {
                //Log.d("HomeItem", "Music gen thread interrupted");
                deleteAlbumArt.run();
            }
        });//.start();
    }
    private static List<XMBItem> generateSettings() {
        ArrayList<XMBItem> settingsItems = new ArrayList<>();
        List<XMBItem> innerSettings = new ArrayList<>();
        List<XMBItem> innerInnerSettings = new ArrayList<>();

        // TODO: add option to change icon alpha
        // TODO: move add association to home settings?

        XMBItem currentSettingsItem;
        innerSettings.clear();
        innerSettings.add(new HomeItem(Type.addExplorer, "Add explorer item to home"));
        innerSettings.add(new HomeItem(Type.addAppOuter, "Add application to home"));
        innerSettings.add(new HomeItem(Type.addMusicFolder, "Add music from directory to home"));
        innerSettings.add(new HomeItem(Type.addVideoFolder, "Add videos from directory to home"));
        innerSettings.add(new HomeItem(Type.resetHomeItems, "Reset home items to default"));
        currentSettingsItem = new HomeItem(Type.nonDescriptSetting, "Home", DataRef.from(ResImage.get(R.drawable.ic_baseline_home_24).getId(), DataLocation.resource), innerSettings.toArray(new XMBItem[0]));
        settingsItems.add(currentSettingsItem);

        innerSettings.clear();
        innerSettings.add(new HomeItem(Type.setImageBg, "Set picture as background"));
        innerSettings.add(new HomeItem(Type.setShaderBg, "Set shader as background"));
        innerSettings.add(new HomeItem(Type.setUiScale, "Change UI scale"));
        if (!AndroidHelpers.isRunningOnTV())
            innerSettings.add(new HomeItem(Type.setSystemUi, "Change system UI visibility"));
        currentSettingsItem = new HomeItem(Type.nonDescriptSetting, "Display", DataRef.from(ResImage.get(R.drawable.ic_baseline_image_24).getId(), DataLocation.resource), innerSettings.toArray(new XMBItem[0]));
        settingsItems.add(currentSettingsItem);

        innerSettings.clear();
        innerSettings.add(new HomeItem(Type.setAudioVolume, "Set volume levels"));
        currentSettingsItem = new HomeItem(Type.nonDescriptSetting, "Audio", DataRef.from(ResImage.get(R.drawable.ic_baseline_headphones_24).getId(), DataLocation.resource), innerSettings.toArray(new XMBItem[0]));
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
        innerSettings.add(new HomeItem(Type.nonDescriptSetting, "General", DataRef.from(ResImage.get(R.drawable.ic_baseline_home_24).getId(), DataLocation.resource), innerInnerSettings.toArray(new XMBItem[0])));
        innerInnerSettings.clear();
        innerInnerSettings.add(new HomeItem(SettingsKeeper.MUSIC_PLAYER_TOGGLE_PLAY_INPUT, Type.setControls, "Change play/pause input"));
        innerInnerSettings.add(new HomeItem(SettingsKeeper.MUSIC_PLAYER_STOP_INPUT, Type.setControls, "Change stop input"));
        innerInnerSettings.add(new HomeItem(SettingsKeeper.MUSIC_PLAYER_SKIP_NEXT_INPUT, Type.setControls, "Change skip to next input"));
        innerInnerSettings.add(new HomeItem(SettingsKeeper.MUSIC_PLAYER_SKIP_PREV_INPUT, Type.setControls, "Change skip to previous input"));
        innerInnerSettings.add(new HomeItem(SettingsKeeper.MUSIC_PLAYER_SEEK_FORWARD_INPUT, Type.setControls, "Change seek forward input"));
        innerInnerSettings.add(new HomeItem(SettingsKeeper.MUSIC_PLAYER_SEEK_BACK_INPUT, Type.setControls, "Change seek back input"));
        innerSettings.add(new HomeItem(Type.nonDescriptSetting, "Music Player", DataRef.from(ResImage.get(R.drawable.ic_baseline_headphones_24).getId(), DataLocation.resource), innerInnerSettings.toArray(new XMBItem[0])));
        innerInnerSettings.clear();
        innerInnerSettings.add(new HomeItem(SettingsKeeper.EXPLORER_GO_UP_INPUT, Type.setControls, "Change go up input"));
        innerInnerSettings.add(new HomeItem(SettingsKeeper.EXPLORER_GO_BACK_INPUT, Type.setControls, "Change go back input"));
        innerInnerSettings.add(new HomeItem(SettingsKeeper.EXPLORER_HIGHLIGHT_INPUT, Type.setControls, "Change highlight input"));
        innerInnerSettings.add(new HomeItem(SettingsKeeper.EXPLORER_EXIT_INPUT, Type.setControls, "Change exit input"));
        innerSettings.add(new HomeItem(Type.nonDescriptSetting, "File Explorer", DataRef.from(ResImage.get(R.drawable.ic_baseline_source_24).getId(), DataLocation.resource), innerInnerSettings.toArray(new XMBItem[0])));
        innerInnerSettings.clear();
        innerInnerSettings.add(new HomeItem(SettingsKeeper.HOME_COMBOS, Type.setControls, "Change go home input"));
        innerInnerSettings.add(new HomeItem(SettingsKeeper.RECENTS_COMBOS, Type.setControls, "Change view recent apps input"));
        innerSettings.add(new HomeItem(Type.nonDescriptSetting, "Android System", DataRef.from(ResImage.get(R.drawable.baseline_adb_24).getId(), DataLocation.resource), innerInnerSettings.toArray(new XMBItem[0])));
        currentSettingsItem = new HomeItem(Type.nonDescriptSetting, "Controls", DataRef.from(ResImage.get(R.drawable.ic_baseline_games_24).getId(), DataLocation.resource), innerSettings.toArray(new XMBItem[0]));
        settingsItems.add(currentSettingsItem);

        innerSettings.clear();
        innerSettings.add(new HomeItem(Type.addAssocOuter, "Add association to home"));
        innerSettings.add(new HomeItem(Type.createAssoc, "Create new association"));
        currentSettingsItem = new HomeItem(Type.nonDescriptSetting, "Associations", DataRef.from(ResImage.get(R.drawable.ic_baseline_send_time_extension_24).getId(), DataLocation.resource), innerSettings.toArray(new XMBItem[0]));
        settingsItems.add(currentSettingsItem);

        innerSettings.clear();
        innerSettings.add(new HomeItem(Type.appInfo, "App info"));
        innerSettings.add(new HomeItem(Type.saveLogs, "Save logs to file"));
        currentSettingsItem = new HomeItem(Type.nonDescriptSetting, "About", DataRef.from(ResImage.get(R.drawable.baseline_info_24).getId(), DataLocation.resource), innerSettings.toArray(new XMBItem[0]));
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
