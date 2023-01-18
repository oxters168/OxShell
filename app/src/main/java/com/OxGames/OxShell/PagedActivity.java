package com.OxGames.OxShell;

import android.app.WallpaperInfo;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.BaseInputConnection;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.OxGames.OxShell.Data.SettingsKeeper;
import com.OxGames.OxShell.Data.ShortcutsCache;
import com.OxGames.OxShell.Helpers.ActivityManager;
import com.OxGames.OxShell.Helpers.AndroidHelpers;
import com.OxGames.OxShell.Helpers.LogcatHelper;
import com.OxGames.OxShell.Interfaces.InputReceiver;
import com.OxGames.OxShell.Interfaces.PermissionsListener;
import com.OxGames.OxShell.Interfaces.Refreshable;
import com.OxGames.OxShell.Views.SlideTouchGridView;
import com.OxGames.OxShell.Views.SlideTouchListView;
import com.appspell.shaderview.ShaderView;
import com.appspell.shaderview.gl.params.ShaderParamsBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PagedActivity extends AppCompatActivity {
    protected Hashtable<ActivityManager.Page, View> allPages = new Hashtable<>();
    //    private static PagedActivity instance;
//    public static DisplayMetrics displayMetrics;
    private List<PermissionsListener> permissionListeners = new ArrayList<>();
    protected ActivityManager.Page currentPage;

    private static boolean startTimeSet;
    private static long startTime;
    //private static long prevFrameTime;

    //private ShaderView shaderView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        trySetStartTime();
        super.onCreate(savedInstanceState);
//        instance = this;
        ActivityManager.init();
        ActivityManager.instanceCreated(this);

        SettingsKeeper.loadOrCreateSettings();
        if (SettingsKeeper.fileDidNotExist()) {
            ShortcutsCache.createAndStoreDefaults();
            Log.i("PagedActivity", "Settings did not exist, first time launch");
        } else {
            ShortcutsCache.readIntentsFromDisk();
            int timesLoaded = 0;
            if (SettingsKeeper.hasValue(SettingsKeeper.TIMES_LOADED))
                timesLoaded = (int) SettingsKeeper.getValue(SettingsKeeper.TIMES_LOADED);
            Log.i("PagedActivity", "Settings existed, activity launched " + timesLoaded + " time(s)");
        }

        LogcatHelper.getInstance(this).start();
        //int mPId = android.os.Process.myPid();
        //Log.d("PagedActivity", "pid: " + mPId);

        //HideActionBar();

//        InitViewsTable();

//        RefreshDisplayMetrics();


//        HomeManager.Init();
        Log.i("PagedActivity", "OnCreate " + this);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        //initBackground();
    }

    @Override
    protected void onResume() {
        ActivityManager.setCurrent(currentPage);
        goTo(currentPage);
        super.onResume();
        //Add an if statement later to have a setting for hiding status bar
        //HideStatusBar();
        hideActionBar();
        hideSystemUI();
        //resumeBackground();
//        final WallpaperManager wallpaperManager = WallpaperManager.getInstance(this);
//        if (AndroidHelpers.hasReadStoragePermission()) {
//            //Log.d("PagedActivity", "Getting wallpaper drawable");
//            WallpaperInfo info = wallpaperManager.getWallpaperInfo();
//            Log.d("PagedActivity", "Wallpaper info: " + info);
//            final Drawable wallpaperDrawable;
//            if (info != null) {
//                wallpaperDrawable = info.loadIcon(getPackageManager());
//            } else
//                wallpaperDrawable = wallpaperManager.getDrawable();
//
//            ImageView bg = findViewById(R.id.bgView);
//            bg.setBackground(wallpaperDrawable);
//        } else {
//            final Drawable wallpaperDrawable = wallpaperManager.getBuiltInDrawable();
//            ImageView bg = findViewById(R.id.bgView);
//            bg.setBackground(wallpaperDrawable);
//        }

        Log.i("PagedActivity", "OnResume " + this);
    }
    @Override
    protected void onPause() {
        Log.i("PagedActivity", "OnPause " + this);
        //pauseBackground();
        super.onPause();
    }
    @Override
    protected void onStop() {
        Log.i("PagedActivity", "OnStop " + this);
        super.onStop();
    }
    @Override
    protected void onDestroy() {
        Log.i("PagedActivity", "OnDestroy " + this);
        LogcatHelper.getInstance(this).stop();
        super.onDestroy();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        Log.i("PagedActivity", "OnWindowFocusChanged " + this);
        super.onWindowFocusChanged(hasFocus);
        hideSystemUI();
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent key_event) {
        //Log.d("PagedActivity", key_event.toString());
        View currentView = allPages.get(currentPage);

        if (key_event.getAction() == KeyEvent.ACTION_DOWN) {
            if (key_event.getKeyCode() == KeyEvent.KEYCODE_BUTTON_Y) {
                Log.d("PagedActivity", "Attempting to convert button keycode to app switch");
                AccessService.showRecentApps();
                //com.android.systemui.recents.Recents.showRecentApps(false);
//                Intent intent = new Intent ("com.android.systemui.recents.action.TOGGLE_RECENTS");
//                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
//                intent.setComponent(new ComponentName("com.android.launcher3", AndroidHelpers.RECENT_ACTIVITY));
//                startActivity (intent);
                //IntentLaunchData recents = new IntentLaunchData(AndroidHelpers.RECENT_ACTIVITY);
                //recents.launch();
                //dispatchKeyEvent(new KeyEvent(key_event.getAction(), KeyEvent.KEYCODE_BUTTON_MODE));
                //dispatchKeyEvent(new KeyEvent(key_event.getDownTime(), key_event.getEventTime(), key_event.getAction(), KeyEvent.KEYCODE_BUTTON_MODE, key_event.getRepeatCount(), key_event.getMetaState(), key_event.getDeviceId(), key_event.getScanCode(), key_event.getFlags(), key_event.getSource()));
                //BaseInputConnection mInputConnection = new BaseInputConnection(currentView, false);
                //mInputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ALT_LEFT));
                //mInputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_TAB));
                //mInputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_TAB));
                //mInputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ALT_LEFT));
                //dispatchKeyEvent(new KeyEvent(key_event.getDownTime(), key_event.getEventTime(), KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_TAB, key_event.getRepeatCount(), KeyEvent.META_ALT_ON, key_event.getDeviceId(), key_event.getScanCode(), key_event.getFlags(), key_event.getSource()));
                //sendKeyEvent(currentView, new KeyEvent(key_event.getDownTime(), key_event.getEventTime(), key_event.getAction(), KeyEvent.KEYCODE_APP_SWITCH, key_event.getRepeatCount(), key_event.getMetaState(), key_event.getDeviceId(), 704, key_event.getFlags(), key_event.getSource()));
                //sendKeyEvent(KeyEvent.KEYCODE_APP_SWITCH, KeyEvent.ACTION_DOWN);
                //sendKeyEvent(KeyEvent.KEYCODE_APP_SWITCH, KeyEvent.ACTION_UP);
                //sendKeyEvent(this, KeyEvent.KEYCODE_APP_SWITCH, KeyEvent.ACTION_UP, 0);
                return true;
            }
        }
//        if (key_event.getAction() == KeyEvent.ACTION_UP) {
//            if (key_event.getKeyCode() == KeyEvent.KEYCODE_BUTTON_Y) {
//                Log.d("PagedActivity", "Attempting to bring up app switcher");
//                //sendKeyEvent(this, KeyEvent.KEYCODE_APP_SWITCH, KeyEvent.ACTION_DOWN, 0);
//                sendKeyEvent(KeyEvent.KEYCODE_APP_SWITCH, KeyEvent.ACTION_UP);
//                return true;
//            }
//        }

        boolean childsPlay = false;
        if (currentView instanceof InputReceiver)
            childsPlay = ((InputReceiver)currentView).receiveKeyEvent(key_event);
        if (childsPlay)
            return true;
        return super.dispatchKeyEvent(key_event);
    }
    private static void sendKeyEvent(View targetView, KeyEvent keyEvent) {
        //Reference: https://developer.android.com/reference/android/view/inputmethod/InputConnection#sendKeyEvent(android.view.KeyEvent)
        //& https://stackoverflow.com/questions/13026505/how-can-i-send-key-events-in-android
        //long eventTime = SystemClock.uptimeMillis();
        BaseInputConnection mInputConnection = new BaseInputConnection(targetView, true);
        mInputConnection.sendKeyEvent(keyEvent);
        //dispatchKeyEvent(new KeyEvent(eventTime, eventTime, action, keyeventcode, 0));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        for (PermissionsListener pl : permissionListeners) {
            pl.onPermissionResponse(requestCode, permissions, grantResults);
        }
    }

    private void trySetStartTime() {
        if (!startTimeSet) {
            startTimeSet = true;
            startTime = System.currentTimeMillis();
            //prevFrameTime = startTime;
        }
    }

    public ShaderView getBackground() {
        return findViewById(R.id.bgShader);
    }
    private void pauseBackground() {
        ShaderView shaderView = getBackground();
        shaderView.setUpdateContinuously(false);
    }
    private void resumeBackground() {
        //Later will have more logic based on options set by user
        ShaderView shaderView = getBackground();
        shaderView.setUpdateContinuously(true);
        shaderView.setFramerate(30);
    }
    private void initBackground() {
        ShaderView shaderView = getBackground();
        Log.d("Paged Activity", "Shader view null: " + (shaderView == null));
        if (shaderView != null) {
            shaderView.setFragmentShader(AndroidHelpers.readAssetAsString(this, "xmb.fsh"));
            shaderView.setVertexShader(AndroidHelpers.readAssetAsString(this, "default.vsh"));
            ShaderParamsBuilder paramsBuilder = new ShaderParamsBuilder();
            paramsBuilder.addFloat("iTime", 0f);
            DisplayMetrics displayMetrics = ActivityManager.getCurrentActivity().getDisplayMetrics();
            paramsBuilder.addVec2i("iResolution", new int[] { displayMetrics.widthPixels, displayMetrics.heightPixels });
            shaderView.setShaderParams(paramsBuilder.build());
            shaderView.setOnDrawFrameListener(shaderParams -> {
                //float deltaTime = (System.currentTimeMillis() - prevFrameTime) / 1000f;
                //float fps = 1f / deltaTime;
                //Log.d("FPS", String.valueOf(fps));
                //prevFrameTime = System.currentTimeMillis();
                float secondsElapsed = (System.currentTimeMillis() - startTime) / 1000f;
                shaderParams.updateValue("iTime", secondsElapsed);
                return null;
            });
        }
    }

    public void addPermissionListener(PermissionsListener listener) {
        permissionListeners.add(listener);
    }

//    public static PagedActivity GetInstance() {
//        return instance;
//    }

    private void hideStatusBar() {
        View decorView = getWindow().getDecorView();
        // Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
    }
    private void hideActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.hide();
    }
    private void hideSystemUI() {
        // Set the IMMERSIVE flag.
        // Set the content to appear under the system bars so that the content
        // doesn't resize when the system bars hide and show.
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

//    public void RefreshDisplayMetrics() {
//        displayMetrics = new DisplayMetrics();
//        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
//    }
    public DisplayMetrics getDisplayMetrics() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics;
    }

    protected void initViewsTable() {
    }
    public View getView(ActivityManager.Page page) {
        return allPages.get(page);
    }
    public void goTo(ActivityManager.Page page) {
        if (currentPage != page) {
            Set<Map.Entry<ActivityManager.Page, View>> entrySet = allPages.entrySet();
            for (Map.Entry<ActivityManager.Page, View> entry : entrySet) {
                entry.getValue().setVisibility(page == entry.getKey() ? View.VISIBLE : View.GONE);
                if (page == entry.getKey()) {
                    View nextPage = entry.getValue();
                    if (nextPage instanceof Refreshable)
                        ((Refreshable)nextPage).refresh();
                    nextPage.requestFocusFromTouch();

                    if (nextPage instanceof Refreshable)
                        ((Refreshable)nextPage).refresh();
                    currentPage = page;
                }
            }
        }
    }
}
