package com.OxGames.OxShell;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowInsets;
import android.view.inputmethod.BaseInputConnection;
import android.widget.FrameLayout;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.OxGames.OxShell.Data.DataLocation;
import com.OxGames.OxShell.Data.FontRef;
import com.OxGames.OxShell.Data.KeyCombo;
import com.OxGames.OxShell.Data.KeyComboAction;
import com.OxGames.OxShell.Data.SettingsKeeper;
import com.OxGames.OxShell.Data.ShortcutsCache;
import com.OxGames.OxShell.Helpers.ActivityManager;
import com.OxGames.OxShell.Helpers.AndroidHelpers;
import com.OxGames.OxShell.Helpers.LogcatHelper;
import com.OxGames.OxShell.Interfaces.Refreshable;
import com.OxGames.OxShell.Views.DynamicInputView;
import com.OxGames.OxShell.Views.PromptView;
import com.OxGames.OxShell.Views.SettingsDrawer;
import com.appspell.shaderview.ShaderView;
import com.appspell.shaderview.gl.params.ShaderParamsBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public class PagedActivity extends AppCompatActivity {
    private final ActivityResultLauncher<Intent> mStartForResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
        result -> {
            if (PagedActivity.this.onReceivedResult != null)
                PagedActivity.this.onReceivedResult.accept(result);
        });
    private final ActivityResultLauncher<String[]> mStartForContent = registerForActivityResult(new ActivityResultContracts.OpenDocument(),
        uri -> {
            Log.i("HomeActivity", "Received result from activity " + uri);
            if (uri != null) {
                getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                if (PagedActivity.this.onReceivedContent != null)
                    PagedActivity.this.onReceivedContent.accept(uri);
            }
        });
    // source: https://stackoverflow.com/a/70933975/5430992
    private final ActivityResultLauncher<Uri> mDirRequest = registerForActivityResult(new ActivityResultContracts.OpenDocumentTree(),
        uri -> {
            Log.i("HomeActivity", "Received result from activity " + uri);
            if (uri != null) {
                getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                //AndroidHelpers.queryUri(uri);
                if (PagedActivity.this.onReceivedDir != null)
                    PagedActivity.this.onReceivedDir.accept(uri);
            }
        });
    private final ActivityResultLauncher<String> mCreateRequest = registerForActivityResult(new ActivityResultContracts.CreateDocument("application/zip"),
            uri -> {
                Log.i("HomeActivity", "Received result from activity " + uri);
                if (uri != null) {
                    getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    if (PagedActivity.this.onReceivedContent != null)
                        PagedActivity.this.onReceivedContent.accept(uri);
                }
            });
    private Consumer<ActivityResult> onReceivedResult;
    private Consumer<Uri> onReceivedContent;
    private Consumer<Uri> onReceivedDir;

    public void requestResult(Intent intent, Consumer<ActivityResult> onReceived) {
        this.onReceivedResult = onReceived;
        mStartForResult.launch(intent);
    }
    public void requestContent(Consumer<Uri> onReceived, String... types) {
        // some mime types: https://stackoverflow.com/questions/23385520/android-available-mime-types
        this.onReceivedContent = onReceived;
        mStartForContent.launch(types);
    }
    public void requestDirectoryAccess(Uri initialPath, Consumer<Uri> onReceived) {
        this.onReceivedDir = onReceived;
        mDirRequest.launch(initialPath);
    }
    public void requestCreateZipFile(Consumer<Uri> onReceived, String type) {
        // some mime types: https://stackoverflow.com/questions/23385520/android-available-mime-types
        this.onReceivedContent = onReceived;
        mCreateRequest.launch(type);
    }

    private final HashMap<Integer, List<Consumer<Boolean>>> permissionListeners = new HashMap<>();
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        Log.i("PagedActivity", "Received result for " + requestCode + " permissions: " + Arrays.toString(permissions) + " grantResults: " + Arrays.toString(grantResults));
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (permissionListeners.containsKey(requestCode)) {
            List<Consumer<Boolean>> listeners = permissionListeners.get(requestCode);
            if (listeners != null) {
                for (Consumer<Boolean> listener : listeners)
                    if (listener != null)
                        listener.accept(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED);
                listeners.clear();
            }
        }
    }
    public void addOneTimePermissionListener(int requestCode, Consumer<Boolean> onResult) {
        if (!permissionListeners.containsKey(requestCode))
            permissionListeners.put(requestCode, new ArrayList<>());
        permissionListeners.get(requestCode).add(onResult);
    }

    private static final int SETTINGS_DRAWER_ID = View.generateViewId();
    private static final int DYNAMIC_INPUT_ID = View.generateViewId();
    private static final int PROMPT_ID = View.generateViewId();
    //private static final String PAGED_ACTIVITY_INPUT = "PAGED_ACTIVITY_INPUT";

    protected Hashtable<ActivityManager.Page, View> allPages = new Hashtable<>();
    //    private static PagedActivity instance;
//    public static DisplayMetrics displayMetrics;
    //private List<PermissionsListener> permissionListeners = new ArrayList<>();
    protected ActivityManager.Page currentPage;

    private static boolean startTimeSet;
    private static long startTime;
    //private static long prevFrameTime;

    //private ShaderView shaderView;
    private FrameLayout parentView;
    //private View dynamicInputView;
    private DynamicInputView dynamicInput;
    private SettingsDrawer settingsDrawer;
    private PromptView prompt;

    private int systemUIVisibility = View.SYSTEM_UI_FLAG_VISIBLE;

    //private InputHandler inputHandler;
    //private boolean isKeyboardShown;
    private ViewTreeObserver.OnGlobalLayoutListener keyboardListener;
    private List<KeyComboAction> accessPopupComboActions;

    private static final String homeAccessMsg = "Ox Shell needs accessibility permission in order to go home when pressing this key combo";
    private static final String recentsAccessMsg = "Ox Shell needs accessibility permission in order to show recent apps when pressing this key combo";

    private void showAccessibilityPopup(String msg) {
        if (!AccessService.isEnabled() && !prompt.isPromptShown()) {
            PromptView prompt = ActivityManager.getCurrentActivity().getPrompt();
            prompt.setCenterOfScreen();
            prompt.setMessage(msg);
            prompt.setStartBtn("Continue", () -> {
                prompt.setShown(false);
                AndroidHelpers.requestAccessibilityService(granted -> {
                    Log.d("PagedActivity", "Accessibility permission granted: " + granted);
                });
            }, SettingsKeeper.getSuperPrimaryInput());
            prompt.setEndBtn("Cancel", () -> {
                prompt.setShown(false);
            }, SettingsKeeper.getCancelInput());
            prompt.setShown(true);
        }
    }
    public void refreshAccessibilityInput() {
        if (accessPopupComboActions != null)
            OxShellApp.getInputHandler().removeKeyComboActions(accessPopupComboActions.toArray(new KeyComboAction[0]));
        KeyComboAction[] recentsComboAction = Arrays.stream(SettingsKeeper.getRecentsCombos()).map(combo -> new KeyComboAction(combo, () -> showAccessibilityPopup(recentsAccessMsg))).toArray(KeyComboAction[]::new);
        KeyComboAction[] homeComboAction = Arrays.stream(SettingsKeeper.getHomeCombos()).map(combo -> new KeyComboAction(combo, () -> showAccessibilityPopup(homeAccessMsg))).toArray(KeyComboAction[]::new);
        accessPopupComboActions = new ArrayList<>();
        Collections.addAll(accessPopupComboActions, recentsComboAction);
        Collections.addAll(accessPopupComboActions, homeComboAction);
        OxShellApp.getInputHandler().addKeyComboActions(recentsComboAction);
        OxShellApp.getInputHandler().addKeyComboActions(homeComboAction);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        refreshAccessibilityInput();

        trySetStartTime();
        super.onCreate(savedInstanceState);
//        instance = this;

        ActivityManager.init();
        ActivityManager.instanceCreated(this);

        SettingsKeeper.loadOrCreateSettings();
        // in the future we would use this value to upgrade the serialization
        SettingsKeeper.setValueAndSave(SettingsKeeper.VERSION_CODE, BuildConfig.VERSION_CODE);
        if (SettingsKeeper.fileDidNotExist()) {
            ShortcutsCache.createAndStoreDefaults();
            SettingsKeeper.setValueAndSave(SettingsKeeper.FONT_REF, FontRef.from("Fonts/exo.regular.otf", DataLocation.asset));
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
        prepareOtherViews();
        //initBackground();
    }

    protected void setMarginsFor(int... ids) {
        // sets the provided views' margins to avoid the status bar and navigation bar
        for (int id : ids) {
            View parent = findViewById(id);
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams)parent.getLayoutParams();
            params.setMargins(0, OxShellApp.getStatusBarHeight(), 0, OxShellApp.getNavBarHeight());
            parent.setLayoutParams(params);
        }
    }

    @Override
    protected void onResume() {
        ActivityManager.setCurrent(currentPage);
        goTo(currentPage);
        super.onResume();

        prepareOtherViews();
        //settingsDrawer.setShown(isContextDrawerOpen());
        //settingsDrawer.setX(settingsDrawerWidth);
        //Add an if statement later to have a setting for hiding status bar
        setActionBarHidden(true);
        setFullscreen(true);
        //setNavBarHidden(true);
        //setStatusBarHidden(true);
        //resumeBackground();

        Log.i("PagedActivity", "OnResume " + this);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        // TODO: store non-persistent data (like where we are in the app and other stuff) for when leaving the app temporarily
        // use outState.putFloat, putInt, putString...
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onStart() {
        Log.i("PagedActivity", "OnStart " + this);
        //startCheckIfKeyboardOpen();

        super.onStart();
    }
//    private void startCheckIfKeyboardOpen() {
//        View rootView = getWindow().getDecorView();
//        if (keyboardListener != null)
//            rootView.getViewTreeObserver().removeOnGlobalLayoutListener(keyboardListener);
//        keyboardListener = () -> {
//            Rect r = new Rect();
//            rootView.getWindowVisibleDisplayFrame(r);
//
//            int heightDiff = rootView.getHeight() - (r.bottom - r.top);
//            isKeyboardShown = heightDiff > 100;
//        };
//        rootView.getViewTreeObserver().addOnGlobalLayoutListener(keyboardListener);
//    }
    public boolean isKeyboardShown() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//            return getWindow().getDecorView().getRootWindowInsets().isVisible(WindowInsets.Type.ime());
//        }
        Rect windowRect = new Rect();
        getWindow().getDecorView().getWindowVisibleDisplayFrame(windowRect);
        //Log.d("PagedActivity", "WindowRect: " + windowRect.bottom + " =? " + OxShellApp.getDisplayHeight());
        return windowRect.bottom < OxShellApp.getDisplayHeight();
        //return isKeyboardShown;
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
        //inputHandler.clearKeyComboActions();
        super.onStop();
    }
    @Override
    protected void onDestroy() {
        Log.i("PagedActivity", "OnDestroy " + this);
        LogcatHelper.getInstance(this).stop();
        OxShellApp.getInputHandler().removeKeyComboActions(accessPopupComboActions.toArray(new KeyComboAction[0]));
        super.onDestroy();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        Log.d("PagedActivity", "onConfigurationChanged");
        super.onConfigurationChanged(newConfig);
        //fixDrawerLayout();
        prepareOtherViews();
        //getStatusBarHeight();
        //settingsDrawer.setShown(isContextDrawerOpen());
    }
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        Log.i("PagedActivity", "OnWindowFocusChanged " + this);
        super.onWindowFocusChanged(hasFocus);
        setActionBarHidden(true);
        setFullscreen(true);
        //setNavBarHidden(true);
        //setStatusBarHidden(true);
    }

    // TODO: add joystick controls
//    @Override
//    public boolean dispatchGenericMotionEvent(MotionEvent ev) {
//        // source: https://stackoverflow.com/questions/34536195/how-to-differentiate-a-d-pad-movement-from-a-joystick-movement/58510631#58510631
//        Log.d("PagedActivity", "x: " + ev.getAxisValue(MotionEvent.AXIS_X) + " y: " + ev.getAxisValue(MotionEvent.AXIS_Y) + " z: " + ev.getAxisValue(MotionEvent.AXIS_Z) + " rz: " + ev.getAxisValue(MotionEvent.AXIS_RZ));
//        return super.dispatchGenericMotionEvent(ev);
//    }

//    @Override
//    public boolean dispatchGenericMotionEvent(MotionEvent ev) {
//        Log.d("PagedActivity", ev.toString());
//        return super.dispatchGenericMotionEvent(ev);
//    }
    @Override
    public boolean dispatchKeyEvent(KeyEvent key_event) {
        //Log.d("PagedActivity", key_event.toString());
        boolean isDpadInput = key_event.getKeyCode() == KeyEvent.KEYCODE_DPAD_UP || key_event.getKeyCode() == KeyEvent.KEYCODE_DPAD_DOWN || key_event.getKeyCode() == KeyEvent.KEYCODE_DPAD_LEFT || key_event.getKeyCode() == KeyEvent.KEYCODE_DPAD_RIGHT;
        if (isKeyboardShown()) {// && (key_event.getKeyCode() == KeyEvent.KEYCODE_BACK || key_event.getKeyCode() == KeyEvent.KEYCODE_BUTTON_B))
            if (!isDpadInput) {
                Log.d("PagedActivity", "Passing to keyboard: " + key_event);
                return super.dispatchKeyEvent(key_event);
            } else
                return true;
        }

        if (OxShellApp.getInputHandler().onInputEvent(key_event))
            return true;

        if (!isNonPermissable(key_event)) {
            Log.d("PagedActivity", "Passing to system: " + key_event);
            return super.dispatchKeyEvent(key_event);
        }
        return true;
    }
    private boolean isNonPermissable(KeyEvent key_event) {
        return key_event.getKeyCode() == KeyEvent.KEYCODE_BUTTON_A ||
                key_event.getKeyCode() == KeyEvent.KEYCODE_BUTTON_B ||
                key_event.getKeyCode() == KeyEvent.KEYCODE_BUTTON_C ||
                key_event.getKeyCode() == KeyEvent.KEYCODE_BUTTON_X ||
                key_event.getKeyCode() == KeyEvent.KEYCODE_BUTTON_Y ||
                key_event.getKeyCode() == KeyEvent.KEYCODE_BUTTON_Z ||
                key_event.getKeyCode() == KeyEvent.KEYCODE_BUTTON_L1 ||
                key_event.getKeyCode() == KeyEvent.KEYCODE_BUTTON_L2 ||
                key_event.getKeyCode() == KeyEvent.KEYCODE_BUTTON_R1 ||
                key_event.getKeyCode() == KeyEvent.KEYCODE_BUTTON_R2 ||
                key_event.getKeyCode() == KeyEvent.KEYCODE_BUTTON_THUMBL ||
                key_event.getKeyCode() == KeyEvent.KEYCODE_BUTTON_THUMBR ||
                key_event.getKeyCode() == KeyEvent.KEYCODE_BUTTON_SELECT ||
                key_event.getKeyCode() == KeyEvent.KEYCODE_BUTTON_START ||
                key_event.getKeyCode() == KeyEvent.KEYCODE_DPAD_UP ||
                key_event.getKeyCode() == KeyEvent.KEYCODE_DPAD_DOWN ||
                key_event.getKeyCode() == KeyEvent.KEYCODE_DPAD_LEFT ||
                key_event.getKeyCode() == KeyEvent.KEYCODE_DPAD_RIGHT ||
                key_event.getKeyCode() == KeyEvent.KEYCODE_DPAD_DOWN_LEFT ||
                key_event.getKeyCode() == KeyEvent.KEYCODE_DPAD_DOWN_RIGHT ||
                key_event.getKeyCode() == KeyEvent.KEYCODE_DPAD_UP_LEFT ||
                key_event.getKeyCode() == KeyEvent.KEYCODE_DPAD_UP_RIGHT ||
                key_event.getKeyCode() == KeyEvent.KEYCODE_DPAD_CENTER;
    }
    private static void sendKeyEvent(View targetView, KeyEvent keyEvent) {
        //Reference: https://developer.android.com/reference/android/view/inputmethod/InputConnection#sendKeyEvent(android.view.KeyEvent)
        //& https://stackoverflow.com/questions/13026505/how-can-i-send-key-events-in-android
        //long eventTime = SystemClock.uptimeMillis();
        BaseInputConnection mInputConnection = new BaseInputConnection(targetView, true);
        mInputConnection.sendKeyEvent(keyEvent);
        //dispatchKeyEvent(new KeyEvent(eventTime, eventTime, action, keyeventcode, 0));
    }

//    public void showDynamicInput(boolean onOff) {
//        inputViewOpen = onOff;
//        dynamicInputView.setVisibility(inputViewOpen ? View.VISIBLE : View.GONE);
//    }
//    public boolean isDynamicInputShown() {
//        return inputViewOpen;
//    }
    public boolean isInAContextMenu() {
        return prompt.isPromptShown() || settingsDrawer.isDrawerOpen() || dynamicInput.isOverlayShown();
    }
    private void prepareOtherViews() {
        parentView = findViewById(R.id.parent_layout);
        initSettingsDrawer();
        settingsDrawer.setShown(settingsDrawer.isDrawerOpen());
        initDynamicInputView();
        dynamicInput.setShown(dynamicInput.isOverlayShown());
        initPromptView();
        prompt.setShown(prompt.isPromptShown());
    }
    private void initDynamicInputView() {
        dynamicInput = parentView.findViewById(DYNAMIC_INPUT_ID);
        if (dynamicInput == null) {
            Log.d("PagedActivity", "Dynamic input not found, creating...");
            dynamicInput = new DynamicInputView(this);
            dynamicInput.setId(DYNAMIC_INPUT_ID);
            parentView.addView(dynamicInput);
        }
    }
    private void initSettingsDrawer() {
        settingsDrawer = parentView.findViewById(SETTINGS_DRAWER_ID);
        if (settingsDrawer == null) {
            Log.d("PagedActivity", "Settings drawer not found, creating...");
            settingsDrawer = new SettingsDrawer(this);
            settingsDrawer.setId(SETTINGS_DRAWER_ID);
            parentView.addView(settingsDrawer);
        }
    }
    private void initPromptView() {
        prompt = parentView.findViewById(PROMPT_ID);
        if (prompt == null) {
            Log.d("PagedActivity", "Prompt not found, creating...");
            prompt = new PromptView(this);
            prompt.setId(PROMPT_ID);
            parentView.addView(prompt);
        }
    }
    public DynamicInputView getDynamicInput() {
        return dynamicInput;
    }
    public SettingsDrawer getSettingsDrawer() {
        return settingsDrawer;
    }
    public PromptView getPrompt() {
        return prompt;
    }

    private void trySetStartTime() {
        if (!startTimeSet) {
            startTimeSet = true;
            startTime = System.currentTimeMillis();
            //prevFrameTime = startTime;
        }
    }

    public ShaderView getBackground() {
        return null;
        //return findViewById(R.id.bgShader);
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
            shaderView.setFragmentShader(AndroidHelpers.readAssetAsString(this, "Shader/blue_dune.fsh"));
            shaderView.setVertexShader(AndroidHelpers.readAssetAsString(this, "Shader/vert.vsh"));
            ShaderParamsBuilder paramsBuilder = new ShaderParamsBuilder();
            paramsBuilder.addFloat("iTime", 0f);
            //DisplayMetrics displayMetrics = ActivityManager.getCurrentActivity().getDisplayMetrics();
            paramsBuilder.addVec2i("iResolution", new int[] { OxShellApp.getDisplayWidth(), OxShellApp.getDisplayHeight() });
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

//    public void addPermissionListener(PermissionsListener listener) {
//        permissionListeners.add(listener);
//    }

//    public static PagedActivity GetInstance() {
//        return instance;
//    }

    public void setActionBarHidden(boolean onOff) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            if (onOff)
                actionBar.hide();
            else
                actionBar.show();
        }
    }
    public void setFullscreen(boolean onOff) {
        if (onOff)
            systemUIVisibility |= View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
        else
            systemUIVisibility &= ~(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        setSystemUIState(systemUIVisibility);
    }
    public void setStatusBarHidden(boolean onOff) {
        if (onOff)
            systemUIVisibility |= View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        else if ((systemUIVisibility & View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) == 0)
            systemUIVisibility &= ~(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        else
            systemUIVisibility &= ~View.SYSTEM_UI_FLAG_FULLSCREEN;
        setSystemUIState(systemUIVisibility);
    }
    public void setNavBarHidden(boolean onOff) {
        if (onOff)
            systemUIVisibility |= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        else if ((systemUIVisibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0)
            systemUIVisibility &= ~(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        else
            systemUIVisibility &= ~View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        setSystemUIState(systemUIVisibility);
    }
    public int getSystemUIState() {
        return systemUIVisibility;
    }
    public void setSystemUIState(int uiState) {
        systemUIVisibility = uiState;
        getWindow().getDecorView().setSystemUiVisibility(uiState);
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
