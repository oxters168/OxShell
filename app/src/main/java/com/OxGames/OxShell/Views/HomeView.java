package com.OxGames.OxShell.Views;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.text.InputType;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;

import com.OxGames.OxShell.AccessService;
import com.OxGames.OxShell.Adapters.XMBAdapter;
import com.OxGames.OxShell.BuildConfig;
import com.OxGames.OxShell.Data.DataLocation;
import com.OxGames.OxShell.Data.DynamicInputRow;
import com.OxGames.OxShell.Data.DataRef;
import com.OxGames.OxShell.Data.Executable;
import com.OxGames.OxShell.Data.IntentPutExtra;
import com.OxGames.OxShell.Data.KeyCombo;
import com.OxGames.OxShell.Data.PackagesCache;
import com.OxGames.OxShell.Data.Paths;
import com.OxGames.OxShell.Data.ResImage;
import com.OxGames.OxShell.Data.SettingsKeeper;
import com.OxGames.OxShell.Data.ShortcutsCache;
import com.OxGames.OxShell.Data.XMBItem;
import com.OxGames.OxShell.ExplorerActivity;
import com.OxGames.OxShell.FileChooserActivity;
import com.OxGames.OxShell.Data.HomeItem;
import com.OxGames.OxShell.Data.IntentLaunchData;
import com.OxGames.OxShell.Helpers.AndroidHelpers;
import com.OxGames.OxShell.Helpers.AudioPool;
import com.OxGames.OxShell.Helpers.ExplorerBehaviour;
import com.OxGames.OxShell.Helpers.InputHandler;
import com.OxGames.OxShell.Helpers.MathHelpers;
import com.OxGames.OxShell.Helpers.MusicPlayer;
import com.OxGames.OxShell.Helpers.Serialaver;
import com.OxGames.OxShell.HomeActivity;
import com.OxGames.OxShell.Interfaces.Refreshable;
import com.OxGames.OxShell.OxShellApp;
import com.OxGames.OxShell.PagedActivity;
import com.OxGames.OxShell.R;
import com.OxGames.OxShell.Wallpaper.GLWallpaperService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class HomeView extends XMBView implements Refreshable {
    private AudioPool musicPool;
    private AudioPool movePool;
    private Consumer<String> pkgInstalledListener = pkgName -> {
        if (pkgName != null) {
            getAdapter().createColumnAt(getAdapter().getColumnCount() - 1, new HomeItem(pkgName, HomeItem.Type.app, PackagesCache.getAppLabel(pkgName)));
            save(getItems());
        }
    };

    public HomeView(Context context) {
        super(context);
        init();
    }
    public HomeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    public HomeView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
//        setLayoutParams(new GridView.LayoutParams(256, 256));
        init();
    }

    private void init() {
        musicPool = AudioPool.fromAsset("Audio/xmb_musac.mp3", 2);
        movePool = AudioPool.fromAsset("Audio/cow_G7.wav", 5);
        OxShellApp.addPkgInstalledListener(pkgInstalledListener);
        refresh();
    }
    //private boolean isInHome;
    public void onResume() {
        //playBgMusic();
//        isInHome = true;
//        Handler musicHandler = new Handler(Looper.getMainLooper());
//        musicHandler.post(new Runnable() {
//            boolean checkOtherApps;
//            boolean waitedAFrame;
//            @Override
//            public void run() {
//                if (isInHome) {
//                    //Log.d("HomeView", "Checking for other apps playing audio");
//                    if (waitedAFrame) {
//                        checkOtherApps = false;
//                        waitedAFrame = false;
//                        if (!OxShellApp.getAudioManager().isMusicActive())
//                            playBgMusic();
//                        else
//                            Log.d("HomeView", "Another app is playing audio");
//                        musicHandler.post(this);
//                        //musicHandler.postDelayed(this, MathHelpers.calculateMillisForFps(60));
//                    } else if (checkOtherApps) {
//                        pauseBgMusic();
//                        waitedAFrame = true;
//                        musicHandler.post(this);
//                        //musicHandler.postDelayed(this, MathHelpers.calculateMillisForFps(60));
//                    } else {
//                        checkOtherApps = true;
//                        musicHandler.postDelayed(this, MathHelpers.calculateMillisForFps(20));
//                    }
//                }
//            }
//        });
//        if (OxShellApp.getAudioManager().isMusicActive()) {
//
//        }
//        AudioFocusRequest.Builder b = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
//        b.setAcceptsDelayedFocusGain(true);
//        AudioAttributes.Builder b2 = new AudioAttributes.Builder();
//        b2.setContentType(AudioAttributes.CONTENT_TYPE_MUSIC);
//        b.setAudioAttributes(b2.build());
//        b.setOnAudioFocusChangeListener(new AudioManager.OnAudioFocusChangeListener() {
//            @Override
//            public void onAudioFocusChange(int focusChange) {
//                //Log.d("HomeView", "OnAudioFocusChangeListener " + focusChange);
//                if (focusChange == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
//                    Log.d("HomeView", "OnAudioFocusChangeListener request granted: " + focusChange);
//                    //OxShellApp.getAudioManager().requestAudioFocus(b.build());
//                    //playBgMusic();
//                } else {
//                    Log.d("HomeView", "OnAudioFocusChangeListener request denied: " + focusChange);
//                }
//            }
//        });
//        OxShellApp.getAudioManager().requestAudioFocus(b.build());
//        OxShellApp.getAudioManager().requestAudioFocus(new AudioManager.OnAudioFocusChangeListener() {
//            @Override
//            public void onAudioFocusChange(int focusChange) {
//                if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT || focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {
//                    Log.d("HomeView", "OnAudioFocusChangeListener AUDIOFOCUS_LOSS_TRANSIENT || AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK");
//                } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
//                    Log.d("HomeView", "OnAudioFocusChangeListener AUDIOFOCUS_GAIN");
//                } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
//                    Log.d("HomeView", "OnAudioFocusChangeListener AUDIOFOCUS_LOSS");
//                }
//            }
//        }, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
    }
    private void playBgMusic() {
        refreshAudioPools();
        if (musicPool.getActiveCount() > 0)
            musicPool.resumeActive();
        else
            musicPool.playNew(true);
    }
    private void pauseBgMusic() {
        musicPool.pauseActive();
    }
    public void onPause() {
        //isInHome = false;
        pauseBgMusic();
    }
    public void onDestroy() {
        OxShellApp.removePkgInstalledListener(pkgInstalledListener);
        musicPool.setPoolSize(0);
        movePool.setPoolSize(0);
        MusicPlayer.clearPlaylist();
    }
    public void refreshAudioPools() {
        musicPool.setVolume(SettingsKeeper.getMusicVolume());
        movePool.setVolume(SettingsKeeper.getSfxVolume());
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        PagedActivity currentActivity = OxShellApp.getCurrentActivity();
        if (!currentActivity.isInAContextMenu())
            return super.onInterceptTouchEvent(ev);
        else
            return false;
    }
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        PagedActivity currentActivity = OxShellApp.getCurrentActivity();
        if (!currentActivity.isInAContextMenu())
            return super.onTouchEvent(ev);
        else
            return false;
    }

    @Override
    public boolean affirmativeAction() {
        // this is so that we don't both go into the inner items of an item and try to execute it at the same time
        //if (super.affirmativeAction())
        //    return true;
        playMoveSfx();

        if (!isInMoveMode()) {
            //Log.d("HomeView", "Pressed on item with " + ((XMBItem)getSelectedItem()).getInnerItemCount() + " inner item(s)");
            if (getSelectedItem() instanceof HomeItem) {
                HomeItem selectedItem = (HomeItem)getSelectedItem();
                //Log.d("HomeView", currentIndex + " selected " + selectedItem.title + " @(" + selectedItem.colIndex + ", " + selectedItem.localIndex + ")");
                if (selectedItem.type == HomeItem.Type.explorer) {
                    // TODO: show pop up explaining permissions?
                    //ActivityManager.goTo(ActivityManager.Page.explorer);
                    AndroidHelpers.startActivity(ExplorerActivity.class);
                    return true;
//            HomeActivity.GetInstance().GoTo(HomeActivity.Page.explorer);
                } else if (selectedItem.type == HomeItem.Type.app) {
                    (IntentLaunchData.createFromPackage((String)selectedItem.obj, Intent.FLAG_ACTIVITY_NEW_TASK)).launch();
                    return true;
                } else if (selectedItem.type == HomeItem.Type.musicTrack) {
                    Integer[] position = getPosition();
                    int initialPos = position[position.length - 1];
                    List<DataRef> trackLocs = new ArrayList<>();
                    if (position.length > 2 || position[1] != 0) {
                        Integer[] parent;
                        if (position.length > 2) {
                            parent = new Integer[position.length - 1];
                            for (int i = 0; i < parent.length; i++)
                                parent[i] = position[i];
                        } else {
                            parent = new Integer[2];
                            parent[0] = position[0];
                            parent[1] = 0;
                        }
                        int count = position.length > 2 ? getAdapter().getInnerItemCount(parent) : getAdapter().getColumnSize(parent[0]);
                        //List<DataRef> trackLocs = new ArrayList<>();
                        for (int i = position.length > 2 ? 0 : 1; i < count + (position.length > 2 ? 0 : 1); i++) {
                            position[position.length - 1] = i;
                            Object item = getAdapter().getItem(position);
                            if (item instanceof HomeItem && ((HomeItem) item).type == HomeItem.Type.musicTrack) {
                                if (i == initialPos) // this should be fine since we are ++ and we can only be losing items, not gaining
                                    initialPos = trackLocs.size(); // so it should only set once and it will either be the same or a lesser than value
                                trackLocs.add(DataRef.from(((HomeItem)item).obj, DataLocation.file));
                            }
                        }
                    } else
                        trackLocs.add(DataRef.from(selectedItem.obj, DataLocation.file));
                    //String trackPath = (String)selectedItem.obj;
                    //AudioPool.fromFile(trackPath, 1).play(false);
                    MusicPlayer.setPlaylist(initialPos, trackLocs.toArray(new DataRef[0]));
                    MusicPlayer.play();
                    return true;
                } else if (selectedItem.type == HomeItem.Type.addAppOuter) {
                    List<ResolveInfo> apps = PackagesCache.getLaunchableInstalledPackages();
                    XMBItem[] sortedApps = apps.stream().map(currentPkg -> new HomeItem(currentPkg.activityInfo.packageName, HomeItem.Type.addApp, PackagesCache.getAppLabel(currentPkg))).collect(Collectors.toList()).toArray(new XMBItem[0]);
                    Arrays.sort(sortedApps, Comparator.comparing(o -> o.getTitle().toLowerCase()));
                    selectedItem.setInnerItems(sortedApps);
                } else if (selectedItem.type == HomeItem.Type.addApp) {
                    Adapter adapter = getAdapter();
                    adapter.createColumnAt(adapter.getColumnCount(), new HomeItem(selectedItem.obj, HomeItem.Type.app, selectedItem.getTitle(), DataRef.from(selectedItem.obj, DataLocation.pkg)));
                    Toast.makeText(OxShellApp.getCurrentActivity(), selectedItem.getTitle() + " added to home", Toast.LENGTH_SHORT).show();
                    save(getItems());
                    return true;
                } else if (selectedItem.type == HomeItem.Type.addExplorer) {
                    Adapter adapter = getAdapter();
                    adapter.createColumnAt(adapter.getColumnCount(), new HomeItem(HomeItem.Type.explorer, "Explorer", DataRef.from(ResImage.get(R.drawable.ic_baseline_source_24).getId(), DataLocation.resource)));
                    Toast.makeText(OxShellApp.getCurrentActivity(), "Explorer added to home", Toast.LENGTH_SHORT).show();
                    save(getItems());
                    return true;
                } else if (selectedItem.type == HomeItem.Type.addMusicFolder) {
                    PagedActivity currentActivity = OxShellApp.getCurrentActivity();
                    DynamicInputView dynamicInput = currentActivity.getDynamicInput();
                    dynamicInput.setTitle("Add Music from Directory to Home");
                    DynamicInputRow.TextInput titleInput = new DynamicInputRow.TextInput("Path");
                    DynamicInputRow.ButtonInput selectDirBtn = new DynamicInputRow.ButtonInput("Choose", v -> {
                        Intent intent = new Intent();
                        //intent.setPackage(context.getPackageName());
                        intent.setClass(context, FileChooserActivity.class);
                        intent.putExtra("AsAuthority", false);
                        currentActivity.requestResult(intent, result -> {
                            Log.d("HomeView", result.toString() + ", " + (result.getData() != null ? result.getData().toString() : null) + ", " + (result.getData() != null && result.getData().getExtras() != null ? result.getData().getExtras().toString() : null));
                            if (result.getResultCode() == Activity.RESULT_OK) {
                                titleInput.setText(Uri.decode(result.getData().getData().toString()));
                            }
                        });
                    });
                    DynamicInputRow.ButtonInput okBtn = new DynamicInputRow.ButtonInput("Done", v -> {
                        // TODO: show some kind of error when input is invalid
                        HomeItem musicHead = new HomeItem(HomeItem.Type.musicTree, "Music", DataRef.from(ResImage.get(R.drawable.baseline_library_music_24).getId(), DataLocation.resource));
                        musicHead.addToDirsList(titleInput.getText());
                        getAdapter().createColumnAt(getAdapter().getColumnCount(), musicHead);
                        musicHead.reload(() -> {
                            save(getItems());
                            refresh();
                        });
                        dynamicInput.setShown(false);
                    }, SettingsKeeper.getSuperPrimaryInput());
                    DynamicInputRow.ButtonInput cancelBtn = new DynamicInputRow.ButtonInput("Cancel", v -> {
                        dynamicInput.setShown(false);
                    }, SettingsKeeper.getCancelInput());
                    dynamicInput.setItems(new DynamicInputRow(titleInput, selectDirBtn), new DynamicInputRow(okBtn, cancelBtn));

                    dynamicInput.setShown(true);
                    return true;
                } else if (selectedItem.type == HomeItem.Type.addAssocOuter) {
                    XMBItem[] intentItems;
                    IntentLaunchData[] intents = ShortcutsCache.getStoredIntents();
                    if (intents.length > 0) {
                        intentItems = new XMBItem[intents.length];
                        for (int i = 0; i < intents.length; i++)
                            intentItems[i] = new HomeItem(intents[i].getId(), HomeItem.Type.addAssoc);
                    } else {
                        intentItems = new XMBItem[1];
                        intentItems[0] = new XMBItem(null, "None created", DataRef.from("ic_baseline_block_24", DataLocation.resource));
                    }
                    selectedItem.setInnerItems(intentItems);
                } else if (selectedItem.type == HomeItem.Type.appInfo) {
                    DynamicInputView dynamicInput = OxShellApp.getCurrentActivity().getDynamicInput();
                    dynamicInput.setTitle("Ox Shell Info");
                    DynamicInputRow.Label versionLabel = new DynamicInputRow.Label("Version: " + BuildConfig.VERSION_NAME);
                    versionLabel.setGravity(Gravity.CENTER);
                    DynamicInputRow.Label goldLabel = new DynamicInputRow.Label("Running in gold");
                    goldLabel.setGravity(Gravity.CENTER);
                    DynamicInputRow.Label byLabel = new DynamicInputRow.Label("Created by: Oxters Wyzgowski");
                    byLabel.setGravity(Gravity.CENTER);
                    DynamicInputRow.ButtonInput okBtn = new DynamicInputRow.ButtonInput("Ok", v -> {
                        dynamicInput.setShown(false);
                    }, SettingsKeeper.getPrimaryInput());

                    List<DynamicInputRow> rows = new ArrayList<>();
                    rows.add(new DynamicInputRow(versionLabel));
                    if (BuildConfig.GOLD)
                        rows.add(new DynamicInputRow(goldLabel));
                    rows.add(new DynamicInputRow(byLabel));
                    rows.add(new DynamicInputRow(okBtn));
                    dynamicInput.setItems(rows.toArray(new DynamicInputRow[0]));
                    dynamicInput.setShown(true);
                } else if (selectedItem.type == HomeItem.Type.saveLogs) {
                    PagedActivity currentActivity = OxShellApp.getCurrentActivity();
                    String[] logs = Arrays.stream(AndroidHelpers.listContents(Paths.LOGCAT_DIR_INTERNAL)).map(file -> file.getName().endsWith(".log") ? file.getAbsolutePath() : null).toArray(String[]::new);
                    if (logs.length > 0) {
                        currentActivity.requestCreateZipFile(uri -> {
                            AndroidHelpers.writeToUriAsZip(uri, logs);
                            Toast.makeText(currentActivity, "Saved " + logs.length + " log(s)", Toast.LENGTH_LONG).show();
                        }, "logs.zip");
                    } else
                        Toast.makeText(currentActivity, "No logs to save", Toast.LENGTH_LONG).show();
                } else if (selectedItem.type == HomeItem.Type.addAssoc) {
                    PagedActivity currentActivity = OxShellApp.getCurrentActivity();
                    DynamicInputView dynamicInput = currentActivity.getDynamicInput();
                    dynamicInput.setTitle("Add " + ShortcutsCache.getIntent(((UUID)selectedItem.obj)).getDisplayName() + " Association to Home");
                    DynamicInputRow.TextInput titleInput = new DynamicInputRow.TextInput("Path");
                    DynamicInputRow.ButtonInput selectDirBtn = new DynamicInputRow.ButtonInput("Choose", v -> {
                        Intent intent = new Intent();
                        //intent.setPackage(context.getPackageName());
                        intent.setClass(context, FileChooserActivity.class);
                        intent.putExtra("AsAuthority", false);
                        currentActivity.requestResult(intent, result -> {
                            Log.d("HomeView", result.toString() + ", " + (result.getData() != null ? result.getData().toString() : null) + ", " + (result.getData() != null && result.getData().getExtras() != null ? result.getData().getExtras().toString() : null));
                            if (result.getResultCode() == Activity.RESULT_OK) {
                                titleInput.setText(Uri.decode(result.getData().getData().toString()));
                            }
                        });
                    });
                    DynamicInputRow.ButtonInput okBtn = new DynamicInputRow.ButtonInput("Done", v -> {
                        // TODO: show some kind of error when input is invalid
                        Adapter adapter = getAdapter();
                        IntentLaunchData launchData = ShortcutsCache.getIntent((UUID)selectedItem.obj);
                        HomeItem assocItem = new HomeItem(selectedItem.obj, HomeItem.Type.assoc, launchData != null ? launchData.getDisplayName() : "Missing", launchData != null ? DataRef.from(launchData.getPackageName(), DataLocation.pkg) : null);
                        assocItem.addToDirsList(titleInput.getText());
                        adapter.createColumnAt(adapter.getColumnCount(), assocItem);
                        save(getItems());
                        dynamicInput.setShown(false);
                    }, SettingsKeeper.getSuperPrimaryInput());
                    DynamicInputRow.ButtonInput cancelBtn = new DynamicInputRow.ButtonInput("Cancel", v -> {
                        dynamicInput.setShown(false);
                    }, SettingsKeeper.getCancelInput());
                    dynamicInput.setItems(new DynamicInputRow(titleInput, selectDirBtn), new DynamicInputRow(okBtn, cancelBtn));

                    dynamicInput.setShown(true);
                    return true;
                } else if (selectedItem.type == HomeItem.Type.assocExe) {
                    ((Executable)selectedItem.obj).run();
//                    String path = (String)selectedItem.obj;
//                    IntentLaunchData launcher = ShortcutsCache.getIntent((UUID)((HomeItem)getAdapter().getItem(getEntryPosition())).obj);
//                    if (PackagesCache.isPackageInstalled(launcher.getPackageName()))
//                        launcher.launch(path);
//                    else
//                        Log.e("IntentShortcutsView", "Failed to launch, " + launcher.getPackageName() + " is not installed on the device");
                    return true;
                } else if (selectedItem.type == HomeItem.Type.createAssoc) {
                    showAssocEditor("Create Association", null);
                    return true;
                } else if (selectedItem.type == HomeItem.Type.setImageBg) {
                    PagedActivity currentActivity = OxShellApp.getCurrentActivity();
                    DynamicInputView dynamicInput = currentActivity.getDynamicInput();
                    dynamicInput.setTitle("Set Image as Background");
                    //DynamicInputRow.TextInput titleInput = new DynamicInputRow.TextInput("Image File Path");
                    AtomicReference<Uri> permittedUri = new AtomicReference<>();

                    DynamicInputRow.ButtonInput selectFileBtn = new DynamicInputRow.ButtonInput("Choose", v -> {
                        currentActivity.requestContent(permittedUri::set, "image/*");
                    });
                    DynamicInputRow.ButtonInput okBtn = new DynamicInputRow.ButtonInput("Apply", v -> {
                        // TODO: show some kind of error when image/path invalid
                        if (permittedUri.get() != null) {
                            if (AndroidHelpers.isRunningOnTV()) {
                                String bgDest = AndroidHelpers.combinePaths(Paths.SHADER_ITEMS_DIR_INTERNAL, "bg.png");
                                if (AndroidHelpers.fileExists(bgDest))
                                    ExplorerBehaviour.delete(bgDest);
                                AndroidHelpers.saveBitmapToFile(AndroidHelpers.readResolverUriAsBitmap(context, permittedUri.get()), bgDest);
                                SettingsKeeper.setValueAndSave(SettingsKeeper.TV_BG_TYPE, SettingsKeeper.BG_TYPE_IMAGE);
                                currentActivity.applyTvBg();
                            } else
                                AndroidHelpers.setWallpaper(context, AndroidHelpers.readResolverUriAsBitmap(context, permittedUri.get()));
                            dynamicInput.setShown(false);
                        }
                    }, SettingsKeeper.getSuperPrimaryInput());
                    DynamicInputRow.ButtonInput cancelBtn = new DynamicInputRow.ButtonInput("Cancel", v -> {
                        dynamicInput.setShown(false);
                    }, SettingsKeeper.getCancelInput());
                    dynamicInput.setItems(new DynamicInputRow(selectFileBtn), new DynamicInputRow(okBtn, cancelBtn));

                    dynamicInput.setShown(true);
                    return true;
                } else if (selectedItem.type == HomeItem.Type.setShaderBg) {
                    PagedActivity currentActivity = OxShellApp.getCurrentActivity();
                    DynamicInputView dynamicInput = currentActivity.getDynamicInput();
                    dynamicInput.setTitle("Set Shader as Background");
                    //String[] options = { "Blue Dune", "The Other Dune", "Planet", "Custom" };
                    List<String> options = new ArrayList<>();
                    options.add("Blue Dune");
                    options.add("The Other Dune");
                    //if (!AndroidHelpers.isRunningOnTV())
                        options.add("Planet");
                    options.add("Custom");
                    //DynamicInputRow.TextInput titleInput = new DynamicInputRow.TextInput("Fragment Shader Path");
                    AtomicReference<Uri> permittedUri = new AtomicReference<>();
                    String fragDest = AndroidHelpers.combinePaths(Paths.SHADER_ITEMS_DIR_INTERNAL, "frag.fsh");
                    String fragTemp = AndroidHelpers.combinePaths(Paths.SHADER_ITEMS_DIR_INTERNAL, "frag.tmp");
                    String vertDest = AndroidHelpers.combinePaths(Paths.SHADER_ITEMS_DIR_INTERNAL, "vert.vsh");
                    String channel0Dest = AndroidHelpers.combinePaths(Paths.SHADER_ITEMS_DIR_INTERNAL, "channel0.png");
                    String channel1Dest = AndroidHelpers.combinePaths(Paths.SHADER_ITEMS_DIR_INTERNAL, "channel1.png");
                    String channel2Dest = AndroidHelpers.combinePaths(Paths.SHADER_ITEMS_DIR_INTERNAL, "channel2.png");
                    String channel3Dest = AndroidHelpers.combinePaths(Paths.SHADER_ITEMS_DIR_INTERNAL, "channel3.png");
                    final boolean[] alreadyBackedUp = { false };
                    Runnable backupExistingShader = () -> {
                        // if a background shader file already exists
                        if (AndroidHelpers.fileExists(fragDest)) {
                            // move background shader to a temporary file if we haven't already or else delete since its the previews the user has been trying out
                            if (!alreadyBackedUp[0]) {
                                alreadyBackedUp[0] = true;
                                ExplorerBehaviour.moveFiles(fragTemp, fragDest);
                            } else
                                ExplorerBehaviour.delete(fragDest);
                        }
                    };
                    if (AndroidHelpers.fileExists(fragTemp))
                        ExplorerBehaviour.delete(fragTemp);

                    DynamicInputRow.ButtonInput selectFileBtn = new DynamicInputRow.ButtonInput("Choose", v -> {
                        // TODO: add way to choose certain values within chosen shader
                        currentActivity.requestContent(uri -> {
                            String fileName = AndroidHelpers.queryUriDisplayName(uri);
                            if (fileName != null && fileName.endsWith(".fsh"))
                                permittedUri.set(uri);
                            else
                                Toast.makeText(currentActivity, "File must end with .fsh", Toast.LENGTH_LONG).show();
                        }, "*/*");
                    });
                    DynamicInputRow.Dropdown dropdown = new DynamicInputRow.Dropdown(index -> {
                        //titleInput.setVisibility(index == options.length - 1 ? View.VISIBLE : View.GONE);
                        selectFileBtn.setVisibility(index == options.size() - 1 ? View.VISIBLE : View.GONE);
                    }, options.toArray(new String[0]));
                    DynamicInputRow.ButtonInput okBtn = new DynamicInputRow.ButtonInput(AndroidHelpers.isRunningOnTV() ? "Apply" : "Preview", v -> {
                        // TODO: show some kind of error when input is invalid
                        // TODO: add scoped storage alternative for when no storage access is granted
                        AndroidHelpers.writeToFile(vertDest, AndroidHelpers.readAssetAsString(context, "Shaders/vert.vsh"));
                        boolean readyForPreview = false;
                        if (dropdown.getIndex() == options.size() - 1) {
                            //dropdown.getIndex();
                            if (permittedUri.get() != null) {
                                backupExistingShader.run();
                                AndroidHelpers.saveStringToFile(fragDest, AndroidHelpers.readResolverUriAsString(context, permittedUri.get()));
                                readyForPreview = true;
                            }
//                            String path = titleInput.getText();
//                            if (AndroidHelpers.uriExists(Uri.parse(path))) {
//                                // if the chosen file is not the destination we want to copy to
//                                //if (!new File(path).getAbsolutePath().equalsIgnoreCase(new File(fragDest).getAbsolutePath())) {
//                                    //Log.d("HomeView", path + " != " + fragDest);
//                                    backupExistingShader.run();
//                                    // copy the chosen file to the destination
//                                    //ExplorerBehaviour.copyFiles(fragDest, path);
//                                    AndroidHelpers.saveStringToFile(fragDest, AndroidHelpers.readResolverUriAsString(context, Uri.parse(path)));
//                                    readyForPreview = true;
//                                    //Log.d("HomeView", "Copied new shader to destination");
//                                //}
//                            }
                        }
                        else if (dropdown.getIndex() == 0) {
                            backupExistingShader.run();
                            AndroidHelpers.writeToFile(fragDest, AndroidHelpers.readAssetAsString(context, "Shaders/blue_dune.fsh"));
                            readyForPreview = true;
                        }
                        else if (dropdown.getIndex() == 1) {
                            backupExistingShader.run();
                            AndroidHelpers.writeToFile(fragDest, AndroidHelpers.readAssetAsString(context, "Shaders/other_dune.fsh"));
                            readyForPreview = true;
                        }
                        else if (dropdown.getIndex() == 2) {
                            backupExistingShader.run();
                            AndroidHelpers.writeToFile(fragDest, AndroidHelpers.readAssetAsString(context, "Shaders/planet.fsh"));
                            //Log.d("HomeView", "Saving channel0 to " + channel0Dest);
                            AndroidHelpers.saveBitmapToFile(AndroidHelpers.readAssetAsBitmap(context, "Shaders/channel0.png"), channel0Dest);
                            AndroidHelpers.saveBitmapToFile(AndroidHelpers.readAssetAsBitmap(context, "Shaders/channel1.png"), channel1Dest);
                            AndroidHelpers.saveBitmapToFile(AndroidHelpers.readAssetAsBitmap(context, "Shaders/channel2.png"), channel2Dest);
                            AndroidHelpers.saveBitmapToFile(AndroidHelpers.readAssetAsBitmap(context, "Shaders/channel3.png"), channel3Dest);
                            readyForPreview = true;
                        }
                        if (readyForPreview) {
                            if (AndroidHelpers.isRunningOnTV()) {
                                if (AndroidHelpers.fileExists(fragTemp))
                                    ExplorerBehaviour.delete(fragTemp);
                                dynamicInput.setShown(false);
                                //currentActivity.resetShaderViewBg();
                                SettingsKeeper.setValueAndSave(SettingsKeeper.TV_BG_TYPE, SettingsKeeper.BG_TYPE_SHADER);
                                currentActivity.applyTvBg();
                            } else {
                                AndroidHelpers.setWallpaper(currentActivity, currentActivity.getPackageName(), ".Wallpaper.GLWallpaperService", result -> {
                                    if (result.getResultCode() == Activity.RESULT_OK) {
                                        // delete the old background shader
                                        if (AndroidHelpers.fileExists(fragTemp))
                                            ExplorerBehaviour.delete(fragTemp);
                                        GLWallpaperService.requestReload();
                                        dynamicInput.setShown(false);
                                    }
                                });
                            }
                        }
                    }, SettingsKeeper.getSuperPrimaryInput());
                    DynamicInputRow.ButtonInput cancelBtn = new DynamicInputRow.ButtonInput("Cancel", v -> {
                        if (AndroidHelpers.fileExists(fragTemp)) {
                            // delete what was being previewed if anything
                            if (AndroidHelpers.fileExists(fragDest))
                                ExplorerBehaviour.delete(fragDest);
                            // return the old background shader
                            ExplorerBehaviour.moveFiles(fragDest, fragTemp);
                            GLWallpaperService.requestReload();
                        }
                        dynamicInput.setShown(false);
                    }, SettingsKeeper.getCancelInput());
                    // so that they will only show up when the custom option is selected in the dropdown
                    //titleInput.setVisibility(View.GONE);
                    selectFileBtn.setVisibility(View.GONE);
                    dynamicInput.setItems(new DynamicInputRow(dropdown), new DynamicInputRow(selectFileBtn), new DynamicInputRow(okBtn, cancelBtn));

                    dynamicInput.setShown(true);
                    return true;
                } else if (selectedItem.type == HomeItem.Type.setUiScale) {
                    DynamicInputView dynamicInput = OxShellApp.getCurrentActivity().getDynamicInput();
                    dynamicInput.setTitle("Set UI Scale");

                    DynamicInputRow.TextInput uiScaleInput = new DynamicInputRow.TextInput("Main scale multiplier", InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                    uiScaleInput.setText(String.valueOf(SettingsKeeper.getUiScale()));
                    DynamicInputRow.TextInput textScaleInput = new DynamicInputRow.TextInput("Text scale multiplier", InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                    textScaleInput.setText(String.valueOf(SettingsKeeper.getTextScale()));

                    DynamicInputRow.ButtonInput applyBtn = new DynamicInputRow.ButtonInput("Apply", self -> {
                        float uiScale = 1f;
                        try {
                            uiScale = Float.parseFloat(uiScaleInput.getText());
                        } catch (Exception e) {}
                        float textScale = 1f;
                        try {
                            textScale = Float.parseFloat(textScaleInput.getText());
                        } catch (Exception e) {}
                        SettingsKeeper.setUiScale(uiScale);
                        SettingsKeeper.setTextScale(textScale);
                        refresh();
                        dynamicInput.setShown(false);
                    }, SettingsKeeper.getSuperPrimaryInput());
                    DynamicInputRow.ButtonInput cancelBtn = new DynamicInputRow.ButtonInput("Cancel", self -> dynamicInput.setShown(false), SettingsKeeper.getCancelInput());

                    dynamicInput.setItems(new DynamicInputRow(uiScaleInput), new DynamicInputRow(textScaleInput), new DynamicInputRow(applyBtn, cancelBtn));
                    dynamicInput.setShown(true);
                } else if (selectedItem.type == HomeItem.Type.setSystemUi) {
                    DynamicInputView dynamicInput = OxShellApp.getCurrentActivity().getDynamicInput();
                    dynamicInput.setTitle("Set System UI Visibility");

                    int systemUi = SettingsKeeper.getSystemUiVisibility();
                    //DynamicInputRow.ToggleInput fullscreenToggle = new DynamicInputRow.ToggleInput("Fill screen", "Do not fill screen");
                    //fullscreenToggle.setOnOff((systemUi & SettingsKeeper.FULLSCREEN_FLAGS) != 0, true);
                    DynamicInputRow.ToggleInput statusBarToggle = new DynamicInputRow.ToggleInput("Status bar visible", "Status bar hidden");
                    statusBarToggle.setOnOff(SettingsKeeper.hasStatusBarVisible(systemUi), true);
                    DynamicInputRow.ToggleInput navBarToggle = new DynamicInputRow.ToggleInput("Navigation bar visible", "Navigation bar hidden");
                    navBarToggle.setOnOff(SettingsKeeper.hasNavBarVisible(systemUi), true);

                    DynamicInputRow.ButtonInput applyBtn = new DynamicInputRow.ButtonInput("Apply", self -> {
                        //Log.d("HomeView", "fullscreen: " + fullscreenToggle.getOnOff() + " status: " + statusBarToggle.getOnOff() + " nav: " + navBarToggle.getOnOff());
                        //SettingsKeeper.setStoredFullscreen(fullscreenToggle.getOnOff());
                        SettingsKeeper.setStoredStatusBarHidden(!statusBarToggle.getOnOff());
                        SettingsKeeper.setStoredNavBarHidden(!navBarToggle.getOnOff());
                        dynamicInput.setShown(false);
                    }, SettingsKeeper.getSuperPrimaryInput());
                    DynamicInputRow.ButtonInput cancelBtn = new DynamicInputRow.ButtonInput("Cancel", self -> dynamicInput.setShown(false), SettingsKeeper.getCancelInput());

                    dynamicInput.setItems(new DynamicInputRow(statusBarToggle), new DynamicInputRow(navBarToggle), new DynamicInputRow(applyBtn, cancelBtn));
                    dynamicInput.setShown(true);
                } else if (selectedItem.type == HomeItem.Type.setAudioVolume) {
                    DynamicInputView dynamicInput = OxShellApp.getCurrentActivity().getDynamicInput();
                    dynamicInput.setTitle("Set Volume Levels");
                    //DynamicInputRow.Label musicLabel = new DynamicInputRow.Label("Music Volume");
                    //musicLabel.setGravity(Gravity.LEFT | Gravity.BOTTOM);
                    //DynamicInputRow.SliderInput musicSlider = new DynamicInputRow.SliderInput(0, 1, SettingsKeeper.getMusicVolume(), 0.01f, null);
                    DynamicInputRow.Label sfxLabel = new DynamicInputRow.Label("SFX Volume");
                    sfxLabel.setGravity(Gravity.LEFT | Gravity.BOTTOM);
                    DynamicInputRow.SliderInput sfxSlider = new DynamicInputRow.SliderInput(0, 1, SettingsKeeper.getSfxVolume(), 0.01f, null);
                    DynamicInputRow.ButtonInput applyBtn = new DynamicInputRow.ButtonInput("Apply", (selfBtn) -> {
                        //SettingsKeeper.setMusicVolume(musicSlider.getValue());
                        SettingsKeeper.setSfxVolume(sfxSlider.getValue());
                        refreshAudioPools();
                        dynamicInput.setShown(false);
                    }, SettingsKeeper.getSuperPrimaryInput());
                    DynamicInputRow.ButtonInput cancelBtn = new DynamicInputRow.ButtonInput("Cancel", (selfBtn) -> {
                        dynamicInput.setShown(false);
                    }, SettingsKeeper.getCancelInput());
                    //dynamicInput.setItems(new DynamicInputRow(musicLabel), new DynamicInputRow(musicSlider), new DynamicInputRow(sfxLabel), new DynamicInputRow(sfxSlider), new DynamicInputRow(applyBtn, cancelBtn));
                    dynamicInput.setItems(new DynamicInputRow(sfxLabel), new DynamicInputRow(sfxSlider), new DynamicInputRow(applyBtn, cancelBtn));
                    dynamicInput.setShown(true);
                } else if (selectedItem.type == HomeItem.Type.setControls) {
                    //Log.d("HomeView", "Modifying " + selectedItem.obj);
                    DynamicInputView dynamicInput = OxShellApp.getCurrentActivity().getDynamicInput();
                    dynamicInput.setTitle("Modifying " + selectedItem.obj);
                    List<DynamicInputRow.TextInput> comboInputs = new ArrayList<>();
                    List<DynamicInputRow.ButtonInput> pollBtns = new ArrayList<>();
                    List<DynamicInputRow.ButtonInput> clearBtns = new ArrayList<>();
                    List<DynamicInputRow.ButtonInput> removeBtns = new ArrayList<>();
                    List<DynamicInputRow.ToggleInput> onDownToggles = new ArrayList<>();
                    List<DynamicInputRow.TextInput> holdTimeInputs = new ArrayList<>();
                    List<DynamicInputRow.TextInput> repeatStartDelayInputs = new ArrayList<>();
                    List<DynamicInputRow.TextInput> repeatDelayInputs = new ArrayList<>();
                    List<DynamicInputRow.ToggleInput> orderedToggles = new ArrayList<>();
                    InputHandler mainInputter = OxShellApp.getInputHandler();
                    Consumer<KeyCombo[]>[] refreshDynamicInput = new Consumer[1];
                    AtomicBoolean customizing = new AtomicBoolean(true);
                    // TODO: add ondown/onup options

                    Consumer<KeyCombo> addComboRow = (combo) -> {
                        AtomicBoolean polling = new AtomicBoolean(false);
                        DynamicInputRow.TextInput keyComboInput = new DynamicInputRow.TextInput("Key Combo");
                        DynamicInputRow.ButtonInput pollBtn = new DynamicInputRow.ButtonInput("Poll", (selfBtn) -> {
                            // TODO: add timeout
                            int pollTtl = 5000;
                            HashMap<Integer, String> keycodes = KeyCombo.getKeyCodesIntMap();
                            Consumer<KeyEvent>[] pollListener = new Consumer[1];
                            Runnable endPoll = () -> {
                                selfBtn.setLabel("Poll");
                                polling.set(false);
                                mainInputter.toggleBlockingInput(false);
                                AccessService.toggleBlockingInput(false);
                                mainInputter.removeInputListener(pollListener[0]);
                            };
                            pollListener[0] = key_event -> {
                                keyComboInput.setText(Arrays.stream(mainInputter.getHistory()).map(ev -> keycodes.getOrDefault(ev.getKeyCode(), Integer.toString(ev.getKeyCode()))).collect(Collectors.joining(" + ")));
                                if (!mainInputter.isDown())
                                    endPoll.run();
                            };
                            if (!polling.get()) {
                                // start listening for this row
                                long startListenTime = SystemClock.uptimeMillis();
                                polling.set(true);
                                Handler timeoutHandler = new Handler(Looper.getMainLooper());
                                timeoutHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        //Log.d("HomeView", "Checking if timed out");
                                        if (customizing.get() && polling.get() && !mainInputter.isDown() && SystemClock.uptimeMillis() - startListenTime < pollTtl) {
                                            timeoutHandler.postDelayed(this, MathHelpers.calculateMillisForFps(60));
                                            return;
                                        }
                                        if (polling.get() && (!customizing.get() || !mainInputter.isDown()))
                                            endPoll.run();
                                    }
                                });
                                mainInputter.toggleBlockingInput(true);
                                AccessService.toggleBlockingInput(true);
                                mainInputter.addInputListener(pollListener[0]);
                                selfBtn.setLabel("Polling..");
                            } else
                                endPoll.run();
                        });
                        DynamicInputRow.TextInput holdTimeInput = new DynamicInputRow.TextInput("Hold Time (ms)");
                        holdTimeInput.setVisibility(GONE);
                        DynamicInputRow.TextInput repeatStartDelayInput = new DynamicInputRow.TextInput("Repeat Start Delay (ms)");
                        repeatStartDelayInput.setVisibility(GONE);
                        DynamicInputRow.TextInput repeatDelayInput = new DynamicInputRow.TextInput("Repeat Delay (ms)");
                        repeatDelayInput.setVisibility(GONE);
                        DynamicInputRow.ToggleInput orderedToggle = new DynamicInputRow.ToggleInput("Ordered", "Not Ordered", null);
                        DynamicInputRow.ToggleInput onDownToggle = new DynamicInputRow.ToggleInput("On Press", "On Release");
                        onDownToggle.addValuesChangedListener(selfToggle -> {
                            holdTimeInput.setVisibility(((DynamicInputRow.ToggleInput)selfToggle).getOnOff() ? VISIBLE : GONE);
                            repeatStartDelayInput.setVisibility(((DynamicInputRow.ToggleInput)selfToggle).getOnOff() ? VISIBLE : GONE);
                            repeatDelayInput.setVisibility(((DynamicInputRow.ToggleInput)selfToggle).getOnOff() ? VISIBLE : GONE);
                        });
                        DynamicInputRow.ButtonInput clearBtn = new DynamicInputRow.ButtonInput("Clear", (selfBtn) -> {
                            keyComboInput.setText("");
                        });
                        DynamicInputRow.ButtonInput removeBtn = new DynamicInputRow.ButtonInput("Remove", (selfBtn) -> {
                            comboInputs.remove(keyComboInput);
                            pollBtns.remove(pollBtn);
                            clearBtns.remove(clearBtn);
                            removeBtns.remove(selfBtn);
                            refreshDynamicInput[0].accept(null);
                        });
                        if (combo != null) {
                            HashMap<Integer, String> keycodes = KeyCombo.getKeyCodesIntMap();
                            keyComboInput.setText(Arrays.stream(combo.getKeys()).mapToObj(keycode -> keycodes.getOrDefault(keycode, Integer.toString(keycode))).collect(Collectors.joining(" + ")));
                            onDownToggle.setOnOff(combo.isOnDown(), true);
                            holdTimeInput.setText(Integer.toString(combo.getHoldMillis()));
                            repeatStartDelayInput.setText(Integer.toString(combo.getRepeatStartDelay()));
                            repeatDelayInput.setText(Integer.toString(combo.getRepeatMillis()));
                            orderedToggle.setOnOff(combo.isOrdered(), true);
                        }
                        comboInputs.add(keyComboInput);
                        pollBtns.add(pollBtn);
                        clearBtns.add(clearBtn);
                        removeBtns.add(removeBtn);
                        onDownToggles.add(onDownToggle);
                        holdTimeInputs.add(holdTimeInput);
                        repeatStartDelayInputs.add(repeatStartDelayInput);
                        repeatDelayInputs.add(repeatDelayInput);
                        orderedToggles.add(orderedToggle);
                        //return keyComboInput;
                    };
                    DynamicInputRow.ButtonInput addComboBtn = new DynamicInputRow.ButtonInput("Add Combo", v -> {
                        addComboRow.accept(null);
                        refreshDynamicInput[0].accept(null);
                    });
                    DynamicInputRow.ButtonInput cancelBtn = new DynamicInputRow.ButtonInput("Cancel", v -> {
                        dynamicInput.setShown(false);
                        // stop listening to input
                        customizing.set(false);
                    }, SettingsKeeper.getCancelInput());
                    DynamicInputRow.ButtonInput resetToDefaultsBtn = new DynamicInputRow.ButtonInput("Reset to Default", selfBtn -> {
                        refreshDynamicInput[0].accept(SettingsKeeper.getDefaultInputValueFor(selectedItem.obj.toString()));
                    });
                    DynamicInputRow.ButtonInput applyBtn = new DynamicInputRow.ButtonInput("Apply", selfBtn -> {
                        // make the key combo array from inputs then save them to the settings
                        KeyCombo[] createdCombos = new KeyCombo[comboInputs.size()];
                        HashMap<String, Integer> keycodes = KeyCombo.getKeyCodesStringMap();
                        for (int i = 0; i < comboInputs.size(); i++) {
                            int[] keys = Arrays.stream(comboInputs.get(i).getText().split("[+]")).mapToInt(value -> { value = value.trim(); try { return Integer.parseInt(value); } catch (Exception e) { return keycodes.getOrDefault(value, -1); } }).toArray();
                            if (onDownToggles.get(i).getOnOff()) {
                                int holdMillis;
                                try { holdMillis = Integer.parseInt(holdTimeInputs.get(i).getText()); } catch(Exception e) { holdMillis = -1; }
                                int repeatStartDelay;
                                try { repeatStartDelay = Integer.parseInt(repeatStartDelayInputs.get(i).getText()); } catch(Exception e) { repeatStartDelay = -1; }
                                int repeatDelay;
                                try { repeatDelay = Integer.parseInt(repeatDelayInputs.get(i).getText()); } catch(Exception e) { repeatDelay = -1; }
                                createdCombos[i] = KeyCombo.createDownCombo(holdMillis, repeatStartDelay, repeatDelay, orderedToggles.get(i).getOnOff(), keys);
                            } else
                                createdCombos[i] = KeyCombo.createUpCombo(orderedToggles.get(i).getOnOff(), keys);
                        }
                        SettingsKeeper.setValueAndSave(selectedItem.obj.toString(), createdCombos);
                        dynamicInput.setShown(false);
                        customizing.set(false);

                        if (OxShellApp.getCurrentActivity() instanceof HomeActivity)
                            ((HomeActivity)OxShellApp.getCurrentActivity()).refreshXMBInput();
                        OxShellApp.getCurrentActivity().refreshAccessibilityInput();
                        OxShellApp.getCurrentActivity().refreshShowDebugInput();
                        AccessService.refreshInputCombos();
                    }, SettingsKeeper.getSuperPrimaryInput());
                    refreshDynamicInput[0] = (placedValues) -> {
                        if (placedValues != null) {
                            comboInputs.clear();
                            pollBtns.clear();
                            clearBtns.clear();
                            removeBtns.clear();
                            onDownToggles.clear();
                            holdTimeInputs.clear();
                            repeatStartDelayInputs.clear();
                            repeatDelayInputs.clear();
                            orderedToggles.clear();
                            for (KeyCombo combo : placedValues)
                                addComboRow.accept(combo);
                        }
                        List<DynamicInputRow> rows = new ArrayList<>();
                        for (int i = 0; i < comboInputs.size(); i++) {
                            rows.add(new DynamicInputRow(comboInputs.get(i), pollBtns.get(i), clearBtns.get(i), removeBtns.get(i)));
                            rows.add(new DynamicInputRow(onDownToggles.get(i), orderedToggles.get(i)));
                            rows.add(new DynamicInputRow(holdTimeInputs.get(i), repeatStartDelayInputs.get(i), repeatDelayInputs.get(i)));
                        }
                        rows.add(new DynamicInputRow(applyBtn, addComboBtn, resetToDefaultsBtn, cancelBtn));
                        dynamicInput.setItems(rows.toArray(new DynamicInputRow[0]));
                    };

                    KeyCombo[] originalSetting = null;
                    if (SettingsKeeper.hasValue(selectedItem.obj.toString()))
                        originalSetting = (KeyCombo[])SettingsKeeper.getValue(selectedItem.obj.toString());
                    refreshDynamicInput[0].accept(originalSetting);
                    dynamicInput.setShown(true);
                }
            }
        }// else
        //    applyMove();

        return super.affirmativeAction();
    }
    @Override
    public boolean secondaryAction() {
        if (super.secondaryAction())
            return true;

        if (!isInMoveMode()) {
            PagedActivity currentActivity = OxShellApp.getCurrentActivity();
            if (!currentActivity.getSettingsDrawer().isDrawerOpen()) {
                Integer[] position = getPosition();
                Integer[] parentPos = new Integer[position.length - 1];
                for (int i = 0; i < parentPos.length; i++)
                    parentPos[i] = position[i];
                //boolean isNotSettings = position[0] < (getAdapter().getColumnCount() - 1);
                boolean hasColumnHead = getAdapter().isColumnHead(position[0], 0);
                boolean isColumnHead = getAdapter().isColumnHead(position);
                boolean hasInnerItems = getAdapter().hasInnerItems(position);
                boolean isInnerItem = position.length > 2;
                XMBItem selectedItem = (XMBItem)getSelectedItem();
                XMBItem parentItem = (XMBItem)getAdapter().getItem(parentPos);
                HomeItem homeItem = null;
                HomeItem parentHomeItem = null;
                if (selectedItem instanceof HomeItem)
                    homeItem = (HomeItem)selectedItem;
                if (parentItem instanceof HomeItem)
                    parentHomeItem = (HomeItem)parentItem;
                boolean isNotSettings = homeItem == null || !HomeItem.isSetting(homeItem.type);
                boolean isNotInnerSettings = homeItem == null || (homeItem.type != HomeItem.Type.nonDescriptSetting && !HomeItem.isInnerSettingType(homeItem.type));
                boolean isPlaceholder = homeItem != null && homeItem.type == HomeItem.Type.placeholder;

                ArrayList<SettingsDrawer.ContextBtn> btns = new ArrayList<>();
                if (!isPlaceholder && isNotInnerSettings && !isInnerItem)// && (parentHomeItem == null || parentHomeItem.type != HomeItem.Type.assoc))
                    btns.add(moveItemBtn);
                if (hasColumnHead && !isInnerItem)
                    btns.add(moveColumnBtn);
                if (isNotSettings && ((homeItem != null && homeItem.isReloadable()) || (parentHomeItem != null && parentHomeItem.isReloadable())))
                    btns.add(reloadBtn);
                if (!isInnerItem)
                    btns.add(createColumnBtn);
                if (!isPlaceholder && isNotInnerSettings)
                    btns.add(editItemBtn);
                if (homeItem != null && (homeItem.type == HomeItem.Type.addAssoc || homeItem.type == HomeItem.Type.assoc || homeItem.type == HomeItem.Type.assocExe))
                    btns.add(editAssocBtn);
                if (hasColumnHead && !isInnerItem)// && parentHomeItem == null)
                    btns.add(editColumnBtn);
                if (!isPlaceholder && isNotSettings && !isInnerItem)// && (parentHomeItem == null || parentHomeItem.type != HomeItem.Type.assoc))
                    btns.add(deleteBtn);
                if (isNotSettings && hasColumnHead && !isInnerItem)
                    btns.add(deleteColumnBtn);
                if (homeItem != null && homeItem.type == HomeItem.Type.addAssoc)
                    btns.add(deleteAssocBtn);
                if (!isPlaceholder && isNotSettings && !isColumnHead && !isInnerItem && !hasInnerItems && homeItem.type != HomeItem.Type.explorer)
                    btns.add(uninstallBtn);
                btns.add(cancelBtn);

                currentActivity.getSettingsDrawer().setButtons(btns.toArray(new SettingsDrawer.ContextBtn[0]));
                currentActivity.getSettingsDrawer().setShown(true);
                return true;
            }
        }
        return false;
    }
    @Override
    public boolean cancelAction() {
        playMoveSfx();
        if (super.cancelAction())
            return true;

        if (!isInMoveMode()) {
            PagedActivity currentActivity = OxShellApp.getCurrentActivity();
            if (!currentActivity.getSettingsDrawer().isDrawerOpen()) {
                currentActivity.getSettingsDrawer().setShown(false);
                return true;
            }
        }
        return false;
    }

    public void deleteSelection() {
        Integer[] position = getPosition();
        //((XMBItem)getAdapter().getItem(position)).clearImgCache();
        getAdapter().removeSubItem(position[0], position[1]);
        save(getItems());
        refresh();
    }
    public void uninstallSelection(Consumer<ActivityResult> onResult) {
        HomeItem selectedItem = (HomeItem)getSelectedItem();
        String packageName = selectedItem.type == HomeItem.Type.app ? (String)selectedItem.obj : selectedItem.type == HomeItem.Type.assoc ? ShortcutsCache.getIntent((UUID)selectedItem.obj).getPackageName() : selectedItem.type == HomeItem.Type.assocExe ? ((Executable)(selectedItem.obj)).getLaunchIntent().getPackageName() : null;
        if (packageName != null)
            AndroidHelpers.uninstallApp(OxShellApp.getCurrentActivity(), packageName, onResult);
    }

    @Override
    protected void onShiftHorizontally(int fromColIndex, int fromRowIndex, int toColIndex) {
        super.onShiftHorizontally(fromColIndex, fromRowIndex, toColIndex);
        playMoveSfx();
    }
    @Override
    protected void onShiftVertically(int fromColIndex, int fromLocalIndex, int toLocalIndex) {
        super.onShiftVertically(fromColIndex, fromLocalIndex, toLocalIndex);
        playMoveSfx();
    }
    private void playMoveSfx() {
        if (musicPool.isAnyPlaying() || movePool.isAnyPlaying() || !OxShellApp.getAudioManager().isMusicActive()) {
            refreshAudioPools();
            movePool.playNew(false);
        }
    }

    @Override
    public void refresh() {
        Log.d("HomeView", "Refreshing home view");
//        Consumer<ArrayList<ArrayList<XMBItem>>> prosumer = items -> {
//
////            createSettingsColumn(settings -> {
////            });
//        };
        long loadHomeStart = SystemClock.uptimeMillis();

        ArrayList<XMBItem> items;
        if (!cachedItemsExists()) {
            // if no file exists then add apps to the home
            // TODO: make optional?
            Log.d("HomeView", "Home items does not exist in data folder, creating...");
            items = createDefaultItems();
        }
        else {
            // if the file exists in the data folder then read it, if the read fails then create defaults
            Log.d("HomeView", "Home items exists in data folder, reading...");
            items = load();
            if (items == null)
                items = createDefaultItems();
        }
        save(items);
        //items.add(createSettingsColumn());
        int cachedColIndex = colIndex;
        int cachedRowIndex = rowIndex;
        setAdapter(new XMBAdapter(getContext(), items));
        setFont(SettingsKeeper.getFont());
        setIndex(cachedColIndex, cachedRowIndex, true);
        Log.i("HomeView", "Time to load home items: " + ((SystemClock.uptimeMillis() - loadHomeStart) / 1000f) + "s");
    }

    @Override
    protected void onAppliedMove(int fromColIndex, int fromLocalIndex, int toColIndex, int toLocalIndex) {
        save(getItems());
    }

    // TODO: remove assoc inner items
    public ArrayList<XMBItem> getItems() {
        //Log.d("HomeView", "Getting items");
        ArrayList<Object> items = getAdapter().getItems();
        ArrayList<XMBItem> casted = new ArrayList<>();
        //items.remove(items.size() - 1); // remove the settings
        BiConsumer<XMBItem, XMBItem> clearIfNeeded = (xmbItem, parent) -> {
            if (xmbItem instanceof HomeItem) {
                //Log.d("HomeView", "Clearing " + xmbItem.getTitle());
                if (((HomeItem)xmbItem).type == HomeItem.Type.settings)// || ((HomeItem)xmbItem).type == HomeItem.Type.assoc)
                    xmbItem.clearInnerItems();
                if (((HomeItem)xmbItem).type == HomeItem.Type.placeholder)
                    if (parent != null)
                        parent.remove(xmbItem);
                    else
                        casted.remove(xmbItem);
            }
        };
        Consumer<XMBItem> goInto = new Consumer<XMBItem>() {
            @Override
            public void accept(XMBItem xmbItem) {
                for (int j = 0; j < xmbItem.getInnerItemCount(); j++) {
                    XMBItem innerItem = xmbItem.getInnerItem(j);
                    //Log.d("HomeView", "Found " + innerItem.getTitle() + ", " + (innerItem instanceof HomeItem ? ((HomeItem)innerItem).type : "xmbItem"));
                    if (innerItem.hasInnerItems())
                        accept(innerItem);
                    clearIfNeeded.accept(innerItem, xmbItem); // call after to make sure not to recreate inner items
                }
            }
        };
        for (int i = 0; i < items.size(); i++) {
            XMBItem item = (XMBItem)items.get(i);
            //Log.d("HomeView", "Found " + item.getTitle());
            casted.add(item);
            goInto.accept(item);
            clearIfNeeded.accept(item, null); // call after to make sure not to recreate inner items
        }
        return casted;
    }
//    private static ArrayList<ArrayList<XMBItem>> cast(ArrayList<ArrayList<Object>> items) {
//        ArrayList<ArrayList<XMBItem>> casted = new ArrayList<>();
//        for (ArrayList<Object> column : items) {
//            ArrayList<XMBItem> innerCasted = new ArrayList<>();
//            for (Object item : column)
//                innerCasted.add((XMBItem)item);
//            casted.add(innerCasted);
//        }
//        return casted;
//    }

    private static boolean cachedItemsExists() {
        return AndroidHelpers.fileExists(AndroidHelpers.combinePaths(Paths.HOME_ITEMS_DIR_INTERNAL, Paths.HOME_ITEMS_FILE_NAME));
    }
    private static void save(ArrayList<XMBItem> items) {
        saveHomeItemsToFile(items, Paths.HOME_ITEMS_DIR_INTERNAL, Paths.HOME_ITEMS_FILE_NAME);
    }
    private static boolean updatedIcons = false;
    private static boolean updatedExes = false;
    private static void upgradeItemsIfNecessary(ArrayList<ArrayList<XMBItem>> items) {
        //int currentVersion = SettingsKeeper.getVersionCode();
        int prevVersion = SettingsKeeper.getPrevVersionCode();
        for (ArrayList<XMBItem> column : items) {
            for (int i = 0; i < column.size(); i++) {
                XMBItem item = column.get(i);
                if (prevVersion < 7 && !updatedIcons)
                    item.upgradeImgRef(prevVersion);
                if (prevVersion < 7 && !updatedExes && item instanceof HomeItem && ((HomeItem)item).type == HomeItem.Type.assocExe)
                    column.set(i, new HomeItem(new Executable((UUID)column.get(0).obj, item.obj.toString()), HomeItem.Type.assocExe, item.getTitle(), DataRef.from(ResImage.get(R.drawable.ic_baseline_auto_awesome_24).getId(), DataLocation.resource)));
            }
        }
        updatedIcons = true;
        updatedExes = true;
    }
    private static boolean loadedOldItems = false;
    private static ArrayList<XMBItem> load() {
        //int currentVersion = SettingsKeeper.getVersionCode();
        int prevVersion = SettingsKeeper.getPrevVersionCode();
        // load items the old way and save them in the new way
        if (prevVersion < 7 && !loadedOldItems) {
            ArrayList<XMBItem> homeItems = oldLoadHomeItemsFromFile(Paths.HOME_ITEMS_DIR_INTERNAL, Paths.HOME_ITEMS_FILE_NAME);
            save(homeItems);
            loadedOldItems = true;
            return homeItems;
        } else
            return loadHomeItemsFromFile(Paths.HOME_ITEMS_DIR_INTERNAL, Paths.HOME_ITEMS_FILE_NAME);
    }
    private static void saveHomeItemsToFile(ArrayList<XMBItem> items, String parentDir, String fileName) {
        AndroidHelpers.makeDir(parentDir);
        String fullPath = AndroidHelpers.combinePaths(parentDir, fileName);
        //Serialaver.saveFile(items, fullPath);
        Serialaver.saveAsFSTJSON(items, fullPath);
    }
    private static ArrayList<XMBItem> loadHomeItemsFromFile(String parentDir, String fileName) {
        ArrayList<XMBItem> items = null;
        String path = AndroidHelpers.combinePaths(parentDir, fileName);
        if (AndroidHelpers.fileExists(path)) {
            try {
                items = (ArrayList<XMBItem>)Serialaver.loadFromFSTJSON(path);
            } catch (Exception e) { Log.e("HomeView", "Failed to load home items: " + e); }
        } else
            Log.e("HomeView", "Attempted to read non-existant home items file @ " + path);

        // TODO: set up another upgrade system
        //if (items != null)
        //    upgradeItemsIfNecessary(items);
        return items;
    }
    private static ArrayList<XMBItem> oldLoadHomeItemsFromFile(String parentDir, String fileName) {
        ArrayList<ArrayList<XMBItem>> items = null;
        String path = AndroidHelpers.combinePaths(parentDir, fileName);
        if (AndroidHelpers.fileExists(path)) {
            try {
                //FSTConfiguration conf = FSTConfiguration.createJsonConfiguration();
                //conf.registerSerializer(ImageRef.class, new DataRef.DataRefSerializer(), false);
                items = (ArrayList<ArrayList<XMBItem>>)Serialaver.loadFromFSTJSON(path);//, conf);
            } catch (Exception e) { Log.e("HomeView", "Failed to load old home items: " + e); }
        } else
            Log.e("HomeView", "Attempted to read non-existant old home items file @ " + path);

        if (items != null) {
            // upgrade icons
            upgradeItemsIfNecessary(items);
            // upgrade to inner items system
            ArrayList<XMBItem> reformed = new ArrayList<>();
            for (int i = 0; i < items.size(); i++) {
                ArrayList<XMBItem> column = items.get(i);
                XMBItem columnHead = column.get(0);
                column.remove(0);
                columnHead.setInnerItems(column.toArray(new XMBItem[0]));
                reformed.add(columnHead);
            }
            reformed.add(new HomeItem(HomeItem.Type.settings, "Settings", DataRef.from(ResImage.get(R.drawable.ic_baseline_settings_24).getId(), DataLocation.resource)));
            return reformed;
        } else
            return null;
    }
    private static ArrayList<XMBItem> createDefaultItems() {
        Log.d("HomeView", "Retrieving default apps");
        long createDefaultStart = SystemClock.uptimeMillis();

        String[] categories = new String[] { "Games", "Audio", "Video", "Image", "Social", "News", "Maps", "Productivity", "Accessibility", "Other" };
        HashMap<Integer, ArrayList<XMBItem>> sortedApps = new HashMap<>();
        List<ResolveInfo> apps = PackagesCache.getLaunchableInstalledPackages();
        Log.d("HomeView", "Time to get installed packages: " + ((SystemClock.uptimeMillis() - createDefaultStart) / 1000f) + "s");
        createDefaultStart = SystemClock.uptimeMillis();
        ArrayList<XMBItem> defaultItems = new ArrayList<>();
        // go through all apps creating HomeItems for them and sorting them into their categories
        int otherIndex = getOtherCategoryIndex();
        for (int i = 0; i < apps.size(); i++) {
            ResolveInfo currentPkg = apps.get(i);
            if (currentPkg.activityInfo.packageName.equals(OxShellApp.getContext().getPackageName()))
                continue;
            int category = currentPkg.activityInfo.applicationInfo.category;
            if (category < 0) {
                if ((currentPkg.activityInfo.applicationInfo.flags & ApplicationInfo.FLAG_IS_GAME) != 0)
                    category = 0;
                else
                    category = otherIndex;
            }
            if (!sortedApps.containsKey(category))
                sortedApps.put(category, new ArrayList<>());
            ArrayList<XMBItem> currentList = sortedApps.get(category);
            currentList.add(new HomeItem(currentPkg.activityInfo.packageName, HomeItem.Type.app, PackagesCache.getAppLabel(currentPkg), DataRef.from(currentPkg.activityInfo.packageName, DataLocation.pkg)));
        }
        // separate the categories to avoid empty ones and order them into an arraylist so no game in indices occurs
        ArrayList<Integer> existingCategories = new ArrayList<>();
        for (Integer key : sortedApps.keySet())
            existingCategories.add(key);
        // add the categories and apps
        for (int index = 0; index < existingCategories.size(); index++) {
            int catIndex = existingCategories.get(index);
            if (catIndex == -1)
                catIndex = categories.length - 1;
            //ArrayList<XMBItem> column = new ArrayList<>();
            // add the category item at the top
            XMBItem columnHead = new XMBItem(null, categories[catIndex], DataRef.from(getDefaultIconForCategory(catIndex), DataLocation.resource), sortedApps.get(existingCategories.get(index)).toArray(new XMBItem[0]));
            //column.add(new XMBItem(null, categories[catIndex], DataRef.from(getDefaultIconForCategory(catIndex), DataLocation.resource)));
            //column.addAll(sortedApps.get(existingCategories.get(index)));
            defaultItems.add(columnHead);
        }
        //ArrayList<XMBItem> explorerColumn = new ArrayList<>();
        //explorerColumn.add(new HomeItem(HomeItem.Type.explorer, "Explorer"));
        defaultItems.add(0, new HomeItem(HomeItem.Type.explorer, "Explorer", DataRef.from(ResImage.get(R.drawable.ic_baseline_source_24).getId(), DataLocation.resource)));
        defaultItems.add(new HomeItem(HomeItem.Type.settings, "Settings", DataRef.from(ResImage.get(R.drawable.ic_baseline_settings_24).getId(), DataLocation.resource)));
        Log.d("HomeView", "Time to sort packages: " + ((SystemClock.uptimeMillis() - createDefaultStart) / 1000f) + "s");
        return defaultItems;
    }
    public static String getDefaultIconForCategory(int category) {
        if (category == ApplicationInfo.CATEGORY_GAME)
            return ResImage.get(R.drawable.ic_baseline_games_24).getId();
        else if (category == ApplicationInfo.CATEGORY_AUDIO)
            return ResImage.get(R.drawable.ic_baseline_headphones_24).getId();
        else if (category == ApplicationInfo.CATEGORY_VIDEO)
            return ResImage.get(R.drawable.ic_baseline_movie_24).getId();
        else if (category == ApplicationInfo.CATEGORY_IMAGE)
            return ResImage.get(R.drawable.ic_baseline_photo_camera_24).getId();
        else if (category == ApplicationInfo.CATEGORY_SOCIAL)
            return ResImage.get(R.drawable.ic_baseline_forum_24).getId();
        else if (category == ApplicationInfo.CATEGORY_NEWS)
            return ResImage.get(R.drawable.ic_baseline_newspaper_24).getId();
        else if (category == ApplicationInfo.CATEGORY_MAPS)
            return ResImage.get(R.drawable.ic_baseline_map_24).getId();
        else if (category == ApplicationInfo.CATEGORY_PRODUCTIVITY)
            return ResImage.get(R.drawable.ic_baseline_work_24).getId();
        else if (category == ApplicationInfo.CATEGORY_ACCESSIBILITY)
            return ResImage.get(R.drawable.ic_baseline_accessibility_24).getId();
        else if (category == getOtherCategoryIndex())
            return ResImage.get(R.drawable.ic_baseline_auto_awesome_24).getId();
        else
            return ResImage.get(R.drawable.ic_baseline_view_list_24).getId();
    }
    private static int getOtherCategoryIndex() {
        return MathHelpers.max(ApplicationInfo.CATEGORY_GAME, ApplicationInfo.CATEGORY_AUDIO, ApplicationInfo.CATEGORY_IMAGE, ApplicationInfo.CATEGORY_SOCIAL, ApplicationInfo.CATEGORY_NEWS, ApplicationInfo.CATEGORY_MAPS, ApplicationInfo.CATEGORY_PRODUCTIVITY, ApplicationInfo.CATEGORY_ACCESSIBILITY) + 1;
    }

//    private void editAssocBtn(IntentLaunchData toBeEdited) {
//        showAssocEditor("Edit Association", toBeEdited);
//    }
//    private void createAssocBtn() {
//        showAssocEditor("Create Association", null);
//    }
    private void showAssocEditor(String title, IntentLaunchData toBeEdited) {
        PagedActivity currentActivity = OxShellApp.getCurrentActivity();
        DynamicInputView dynamicInput = currentActivity.getDynamicInput();
        dynamicInput.setTitle(title);
        DynamicInputRow.TextInput displayNameInput = new DynamicInputRow.TextInput("Display Name");
        List<ResolveInfo> pkgs = PackagesCache.getLaunchableInstalledPackages();
        pkgs.sort(Comparator.comparing(PackagesCache::getAppLabel));
        List<String> pkgNames = pkgs.stream().map(PackagesCache::getAppLabel).collect(Collectors.toList());
        pkgNames.add(0, "Unlisted");
        DynamicInputRow.TextInput pkgNameInput = new DynamicInputRow.TextInput("Package Name");
        DynamicInputRow.Dropdown pkgsDropdown = new DynamicInputRow.Dropdown(index -> {
            //Log.d("HomeView", "Dropdown index changed to " + index);
            if (index >= 1) {
                String pkgName = pkgs.get(index - 1).activityInfo.packageName;
                pkgNameInput.setText(pkgName);
            }
        });
        final String[][] classNames = new String[1][];
        DynamicInputRow.TextInput classNameInput = new DynamicInputRow.TextInput("Class Name");
        DynamicInputRow.Dropdown classesDropdown = new DynamicInputRow.Dropdown(index -> {
            if (index >= 1) {
                String className = classNames[0][index];
                classNameInput.setText(className);
            }
        });
        classesDropdown.setVisibility(GONE);
        classNameInput.addValuesChangedListener(self -> {
            //Log.d("HomeView", "Looking for package in list");
            // populate next list with class names
            String[] currentClassNames = classesDropdown.getOptions();
            if (currentClassNames != null) {
                int index = 0;
                for (int i = 0; i < currentClassNames.length; i++)
                    if (currentClassNames[i].equals(classNameInput.getText())) {
                        index = i;
                        break;
                    }
                // if the user is typing and they type a valid class name, then select it in the drop down
                classesDropdown.setIndex(index);
            }
        });
        pkgNameInput.addValuesChangedListener(self -> {
            //Log.d("HomeView", "Looking for package in list");
            // populate next list with class names
            int index = 0;
            for (int i = 0; i < pkgs.size(); i++)
                if (pkgs.get(i).activityInfo.packageName.equals(pkgNameInput.getText())) {
                    index = i + 1;
                    break;
                }
            //Log.d("HomeView", "Index of " + pkgNameInput.getText() + " is " + index);
            // if the user is typing and they type a valid package name, then select it in the drop down
            String[] classes = new String[0];
            pkgsDropdown.setIndex(index);
            if (index >= 1) { // 0 is unlisted, so skip
                //Log.d("HomeView", "Package exists");

                String[] tmp = PackagesCache.getClassesOfPkg(pkgNameInput.getText());
                if (tmp.length > 0) {
                    classes = new String[tmp.length + 1];
                    System.arraycopy(tmp, 0, classes, 1, tmp.length);
                    classes[0] = "Unlisted";
                }
            }
            classNames[0] = classes;
            classesDropdown.setOptions(classes);
            classesDropdown.setVisibility(classes.length > 0 ? VISIBLE : GONE);
            //Log.d("HomeView", "pkgNameInput value changed to " + pkgNameInput.getText());
        });
        pkgsDropdown.setOptions(pkgNames.toArray(new String[0]));
        String[] intentActions = PackagesCache.getAllIntentActions();
        String[] actionsTmp = PackagesCache.getAllIntentActionNames();
        String[] intentActionNames = new String[actionsTmp.length + 1];
        System.arraycopy(actionsTmp, 0, intentActionNames, 1, actionsTmp.length);
        intentActionNames[0] = "Unlisted";
        DynamicInputRow.TextInput actionInput = new DynamicInputRow.TextInput("Intent Action");
        DynamicInputRow.Dropdown actionsDropdown = new DynamicInputRow.Dropdown(index -> {
            if (index > 0) {
                String actionName = intentActions[index - 1];
                actionInput.setText(actionName);
            }
        }, intentActionNames);
        actionInput.addValuesChangedListener(self -> {
            int index = 0;
            for (int i = 0; i < intentActions.length; i++)
                if (actionInput.getText().equals(intentActions[i])) {
                    index = i + 1;
                    break;
                }
            actionsDropdown.setIndex(index);
        });
        DynamicInputRow.TextInput extensionsInput = new DynamicInputRow.TextInput("Associated Extensions (comma separated)");
        String[] dataTypes = { IntentLaunchData.DataType.None.toString(), IntentLaunchData.DataType.Uri.toString(), IntentLaunchData.DataType.AbsolutePath.toString(), IntentLaunchData.DataType.FileNameWithExt.toString(), IntentLaunchData.DataType.FileNameWithoutExt.toString() };
        DynamicInputRow.Label dataLabel = new DynamicInputRow.Label("Data");
        dataLabel.setGravity(Gravity.LEFT | Gravity.BOTTOM);
        DynamicInputRow.Dropdown dataDropdown = new DynamicInputRow.Dropdown(null, dataTypes);
        DynamicInputRow.TextInput extrasInput = new DynamicInputRow.TextInput("Extras (comma separated pairs, second values can be same as data type [case sensitive])");
        DynamicInputRow.Label errorLabel = new DynamicInputRow.Label("");
        //errorLabel.setVisibility(GONE);

        DynamicInputRow.ButtonInput okBtn = new DynamicInputRow.ButtonInput(toBeEdited != null ? "Apply" : "Create", v -> {
            String displayName = displayNameInput.getText();
            displayName = displayName.isEmpty() ? "Unnamed" : displayName;
            String pkgName = pkgNameInput.getText();
            String actionName = actionInput.getText();
            String className = classNameInput.getText();
            String extensionsRaw = extensionsInput.getText();
            String[] extras = !extrasInput.getText().isEmpty() ? Arrays.stream(extrasInput.getText().split(",")).map(String::trim).toArray(String[]::new) : new String[0];
            if (pkgName.isEmpty())
                errorLabel.setLabel("Must provide package name");
            else if (className.isEmpty())
                errorLabel.setLabel("Must provide class name");
            else if (actionName.isEmpty())
                errorLabel.setLabel("Must provide action");
            else if (extensionsRaw.isEmpty())
                errorLabel.setLabel("Must provide associated extensions");
            else if (extras.length % 2 != 0)
                errorLabel.setLabel("Extras must be a multiple of two");
            else {
                // TODO: add ability to choose flags
                String[] extensions = Stream.of(extensionsRaw.split(",")).map(ext -> { String result = ext.trim(); if(result.charAt(0) == '.') result = result.substring(1, result.length() - 1); return result; }).toArray(String[]::new);
                IntentLaunchData newAssoc = toBeEdited;
                if (newAssoc != null) {
                    newAssoc.setDisplayName(displayName);
                    newAssoc.setAction(actionName);
                    newAssoc.setPackageName(pkgName);
                    newAssoc.setClassName(className);
                    newAssoc.setExtensions(extensions);
                    newAssoc.clearExtras();
                } else
                    newAssoc = new IntentLaunchData(displayName, actionName, pkgName, className, extensions, Intent.FLAG_ACTIVITY_NEW_TASK);

                newAssoc.setDataType(IntentLaunchData.DataType.valueOf(dataDropdown.getOption(dataDropdown.getIndex())));
                for (int i = 0; i < extras.length; i += 2)
                    newAssoc.addExtra(IntentPutExtra.parseFrom(extras[i], extras[i + 1]));
                ShortcutsCache.saveIntentAndReload(newAssoc);
                dynamicInput.setShown(false);
            }
//                        String path = displayNameInput.getText();
//                        if (path != null && AndroidHelpers.fileExists(path)) {
//                            AndroidHelpers.setWallpaper(context, AndroidHelpers.bitmapFromFile(path));
//                            dynamicInput.setShown(false);
//                        }
        }, SettingsKeeper.getSuperPrimaryInput());
        DynamicInputRow.ButtonInput cancelBtn = new DynamicInputRow.ButtonInput("Cancel", v -> {
            dynamicInput.setShown(false);
        }, SettingsKeeper.getCancelInput());

        if (toBeEdited != null) {
            // set values to assoc being edited
            displayNameInput.setText(toBeEdited.getDisplayName());
            pkgNameInput.setText(toBeEdited.getPackageName());
            classNameInput.setText(toBeEdited.getClassName());
            actionInput.setText(toBeEdited.getAction());
            extensionsInput.setText(Arrays.toString(toBeEdited.getExtensions()).replace("[", "").replace("]", ""));
            for (int i = 0; i < dataTypes.length; i++) {
                if (dataTypes[i].equals(toBeEdited.getDataType().toString())) {
                    dataDropdown.setIndex(i);
                    break;
                }
            }
            extrasInput.setText(Arrays.stream(toBeEdited.getExtras()).map(extra -> extra.getName() + ", " + (extra.getValue() != null ? extra.getValue() : extra.getExtraType())).collect(Collectors.joining(", ")));
        }
        dynamicInput.setItems(new DynamicInputRow(displayNameInput), new DynamicInputRow(pkgNameInput, pkgsDropdown), new DynamicInputRow(classNameInput, classesDropdown), new DynamicInputRow(actionInput, actionsDropdown), new DynamicInputRow(extensionsInput), new DynamicInputRow(dataLabel), new DynamicInputRow(dataDropdown), new DynamicInputRow(extrasInput), new DynamicInputRow(errorLabel), new DynamicInputRow(okBtn, cancelBtn));
        dynamicInput.setShown(true);
    }
    private void showItemEditor(String title, XMBItem toBeEdited) {
        PagedActivity currentActivity = OxShellApp.getCurrentActivity();
        DynamicInputView dynamicInput = currentActivity.getDynamicInput();
        dynamicInput.setTitle(title);
        ResImage[] resourceImages = ResImage.getResourceImages();
        List<String> dropdownItems = Arrays.stream(resourceImages).map(ResImage::getName).collect(Collectors.toList());
        dropdownItems.add("From App");
        dropdownItems.add("Custom");
        //XMBItem colItem = toBeEdited;
        DataRef origColIcon = toBeEdited != null ? toBeEdited.getImgRef() : null;
        int origDropdownIndex = resourceImages.length + 1;
        if (origColIcon != null && origColIcon.getLocType() == DataLocation.resource) {
            for (int i = 0; i < resourceImages.length; i++) {
                if (resourceImages[i].getId().equals(origColIcon.getLoc())) {
                    origDropdownIndex = i;
                    break;
                }
            }
        } else if (origColIcon != null && origColIcon.getLocType() == DataLocation.pkg)
            origDropdownIndex = resourceImages.length;

        DynamicInputRow.TextInput pkgNameInput = new DynamicInputRow.TextInput("Package Name");
        DynamicInputRow.ImageDisplay imageDisplay = new DynamicInputRow.ImageDisplay(DataRef.from(null, DataLocation.none));
        Runnable setImgAsPkg = () -> {
            Log.d("HomeView", "Setting pkg as icon " + pkgNameInput.getText());
            if (PackagesCache.isPackageInstalled(pkgNameInput.getText()))
                imageDisplay.setImage(DataRef.from(pkgNameInput.getText(), DataLocation.pkg));
            else
                imageDisplay.setImage(DataRef.from("ic_baseline_question_mark_24", DataLocation.resource));
        };
        AtomicReference<Uri> permittedUri = new AtomicReference<>();
        Runnable setImgAsCustom = () -> {
            if (permittedUri.get() != null)
                imageDisplay.setImage(DataRef.from(permittedUri.get(), DataLocation.resolverUri));
            else
                imageDisplay.setImage(DataRef.from("ic_baseline_question_mark_24", DataLocation.resource));
        };

        List<ResolveInfo> pkgs = PackagesCache.getLaunchableInstalledPackages();
        pkgs.sort(Comparator.comparing(PackagesCache::getAppLabel));
        List<String> pkgNames = pkgs.stream().map(PackagesCache::getAppLabel).collect(Collectors.toList());
        pkgNames.add(0, "Unlisted");
        DynamicInputRow.Dropdown pkgsDropdown = new DynamicInputRow.Dropdown(index -> {
            if (index >= 1) {
                String pkgName = pkgs.get(index - 1).activityInfo.packageName;
                pkgNameInput.setText(pkgName);
            }
            setImgAsPkg.run();
        }, pkgNames.toArray(new String[0]));
        pkgNameInput.addValuesChangedListener(selfTxt -> {
            //currentPkgName.set(((DynamicInputRow.TextInput) selfTxt).getText());
            int index = 0;
            for (int i = 0; i < pkgs.size(); i++)
                if (pkgs.get(i).activityInfo.packageName.equals(pkgNameInput.getText())) {
                    index = i + 1;
                    break;
                }
            pkgsDropdown.setIndex(index);
        });
        if (origColIcon != null && origColIcon.getLocType() == DataLocation.pkg)
            pkgNameInput.setText((String)origColIcon.getLoc());
        //DynamicInputRow pkgRow = new DynamicInputRow(pkgNameInput, pkgsDropdown);
        //DynamicInputRow.TextInput filePathInput = new DynamicInputRow.TextInput("File Path");
//        filePathInput.addListener(new DynamicInputListener() {
//            @Override
//            public void onFocusChanged(View view, boolean hasFocus) {
//
//            }
//
//            @Override
//            public void onValuesChanged() {
//                if (AndroidHelpers.uriExists(Uri.parse(filePathInput.getText())))
//                    imageDisplay.setImage(ImageRef.from(filePathInput.getText(), DataLocation.resolverUri));
//                else
//                    imageDisplay.setImage(ImageRef.from(R.drawable.ic_baseline_question_mark_24, DataLocation.resource));
//            }
//        });
        DynamicInputRow.ButtonInput chooseFileBtn = new DynamicInputRow.ButtonInput("Choose", v -> {
            currentActivity.requestContent(uri -> {
                permittedUri.set(uri);
                setImgAsCustom.run();
            }, "image/*");
            setImgAsCustom.run();
        });
        DynamicInputRow.Dropdown resourcesDropdown = new DynamicInputRow.Dropdown(index -> {
            Log.d("HomeView", "Resources dropdown index set to " + index);
            boolean isResource = index < resourceImages.length;
            boolean isPkg = index == dropdownItems.size() - 2;
            if (!isResource) {
                if (isPkg)
                    setImgAsPkg.run();
                else
                    setImgAsCustom.run();
            }
            pkgNameInput.setVisibility((!isResource && isPkg) ? VISIBLE : GONE);
            pkgsDropdown.setVisibility((!isResource && isPkg) ? VISIBLE : GONE);
            //filePathInput.setVisibility(isResource ? GONE : VISIBLE);
            chooseFileBtn.setVisibility((!isResource && !isPkg) ? VISIBLE : GONE);
            if (index >= 0 && isResource)
                imageDisplay.setImage(DataRef.from(resourceImages[index].getId(), DataLocation.resource));
        }, dropdownItems.toArray(new String[0]));
        DynamicInputRow.TextInput titleInput = new DynamicInputRow.TextInput("Title");
        DynamicInputRow.ButtonInput okBtn = new DynamicInputRow.ButtonInput("Done", v -> {
            String itemTitle = titleInput.getText();
            DataRef imgRef = imageDisplay.getImageRef();
            // delete old icon
            if (toBeEdited != null && toBeEdited.getImgRef() != null && imgRef != toBeEdited.getImgRef() && toBeEdited.getImgRef().getLocType() == DataLocation.file)
                ExplorerBehaviour.delete((String)toBeEdited.getImgRef().getLoc());
            if (imgRef.getLocType() == DataLocation.resolverUri) {
                String iconPath = AndroidHelpers.combinePaths(Paths.ICONS_DIR_INTERNAL, UUID.randomUUID().toString());
                AndroidHelpers.saveBitmapToFile(AndroidHelpers.readResolverUriAsBitmap(context, permittedUri.get()), iconPath);
                imgRef = DataRef.from(iconPath, DataLocation.file);
            }
            if (toBeEdited != null) {
                toBeEdited.setTitle(itemTitle.length() > 0 ? itemTitle : "Unnamed");
                toBeEdited.setImgRef(imgRef);
            } else
                getAdapter().createColumnAt(getPosition()[0], new XMBItem(null, itemTitle.length() > 0 ? itemTitle : "Unnamed", imgRef));
            save(getItems());
            refresh();
            dynamicInput.setShown(false);
        }, SettingsKeeper.getSuperPrimaryInput());
        DynamicInputRow.ButtonInput cancelBtn = new DynamicInputRow.ButtonInput("Cancel", v -> {
            dynamicInput.setShown(false);
        }, SettingsKeeper.getCancelInput());

        if (toBeEdited != null) {
            titleInput.setText(toBeEdited.getTitle());
            resourcesDropdown.setIndex(origDropdownIndex);
            imageDisplay.setImage(toBeEdited.getImgRef());
        }
        dynamicInput.setItems(new DynamicInputRow(imageDisplay, resourcesDropdown), new DynamicInputRow(pkgNameInput, pkgsDropdown), new DynamicInputRow(chooseFileBtn), new DynamicInputRow(titleInput), new DynamicInputRow(okBtn, cancelBtn));

        dynamicInput.setShown(true);
    }

    SettingsDrawer.ContextBtn reloadBtn = new SettingsDrawer.ContextBtn("Reload Inner Items", () ->
    {
        Integer[] position = getPosition();
        Integer[] parentPos = new Integer[position.length - 1];
        for (int i = 0; i < parentPos.length; i++)
            parentPos[i] = position[i];

        Runnable reloadCurrent = () -> {
            Object currentItem = getAdapter().getItem(position);
            if (currentItem instanceof HomeItem  && ((HomeItem)currentItem).isReloadable())
                ((HomeItem) currentItem).reload(() -> {
                    save(getItems());
                    refresh();
                });
        };

        Object parentItem = getAdapter().getItem(parentPos);
        if (parentItem instanceof HomeItem && ((HomeItem)parentItem).isReloadable()) {
            ((HomeItem) parentItem).reload(() -> {
                save(getItems());
                refresh();

                reloadCurrent.run();
            });
        } else
            reloadCurrent.run();

        OxShellApp.getCurrentActivity().getSettingsDrawer().setShown(false);
    });
    SettingsDrawer.ContextBtn moveColumnBtn = new SettingsDrawer.ContextBtn("Move Column", () ->
    {
        toggleMoveMode(true, true);
        OxShellApp.getCurrentActivity().getSettingsDrawer().setShown(false);
    });
    SettingsDrawer.ContextBtn moveItemBtn = new SettingsDrawer.ContextBtn("Move Item", () ->
    {
        toggleMoveMode(true, false);
        OxShellApp.getCurrentActivity().getSettingsDrawer().setShown(false);
    });
    SettingsDrawer.ContextBtn deleteBtn = new SettingsDrawer.ContextBtn("Remove Item", () ->
    {
        deleteSelection();
        OxShellApp.getCurrentActivity().getSettingsDrawer().setShown(false);
    });
    SettingsDrawer.ContextBtn deleteColumnBtn = new SettingsDrawer.ContextBtn("Remove Column", () ->
    {
        //XMBItem colHead = ((XMBItem)getAdapter().getItem(getPosition()[0], 0));
        //colHead.clearImgCache();
        //colHead.clearInnerItemImgCache(true);
        getAdapter().removeColumnAt(getPosition()[0]); // this calls release on the item already
        save(getItems());
        OxShellApp.getCurrentActivity().getSettingsDrawer().setShown(false);
    });
    SettingsDrawer.ContextBtn deleteAssocBtn = new SettingsDrawer.ContextBtn("Delete Association", () ->
    {
        HomeItem assocItem = (HomeItem)getSelectedItem();
        ShortcutsCache.deleteIntent((UUID)assocItem.obj);
        refresh();
        OxShellApp.getCurrentActivity().getSettingsDrawer().setShown(false);
    });
    SettingsDrawer.ContextBtn editAssocBtn = new SettingsDrawer.ContextBtn("Edit Association", () ->
    {
        HomeItem assocItem = (HomeItem)getSelectedItem();
        showAssocEditor("Edit Association", ShortcutsCache.getIntent(assocItem.type == HomeItem.Type.assocExe ? ((Executable)assocItem.obj).getLaunchIntent().getId() : (UUID)assocItem.obj));
        //refresh();
        OxShellApp.getCurrentActivity().getSettingsDrawer().setShown(false);
    });
    SettingsDrawer.ContextBtn editItemBtn = new SettingsDrawer.ContextBtn("Edit Item", () -> {
        XMBItem item = (XMBItem)getSelectedItem();
        showItemEditor("Edit Item", item);
        OxShellApp.getCurrentActivity().getSettingsDrawer().setShown(false);
    });
    SettingsDrawer.ContextBtn editColumnBtn = new SettingsDrawer.ContextBtn("Edit Column", () -> {
        showItemEditor("Edit Column", (XMBItem)getAdapter().getItem(getPosition()[0], 0));
        OxShellApp.getCurrentActivity().getSettingsDrawer().setShown(false);
    });
    SettingsDrawer.ContextBtn createColumnBtn = new SettingsDrawer.ContextBtn("Create Column", () -> {
        showItemEditor("Create Column", null);
        OxShellApp.getCurrentActivity().getSettingsDrawer().setShown(false);
    });
    SettingsDrawer.ContextBtn cancelBtn = new SettingsDrawer.ContextBtn("Cancel", () ->
    {
        OxShellApp.getCurrentActivity().getSettingsDrawer().setShown(false);
    });
    SettingsDrawer.ContextBtn uninstallBtn = new SettingsDrawer.ContextBtn("Uninstall App", () ->
    {
        uninstallSelection((result) -> {
            if (result.getResultCode() == Activity.RESULT_OK)
                deleteSelection();
        });
        OxShellApp.getCurrentActivity().getSettingsDrawer().setShown(false);
    });
}