package com.OxGames.OxShell;

import android.annotation.SuppressLint;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.ComponentName;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import android.os.Build;
import android.content.Intent;
import android.provider.Settings;
import android.content.pm.ResolveInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.OxGames.OxShell.databinding.ActivityFullscreenBinding;
import com.google.gson.Gson;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class FullscreenActivity extends AppCompatActivity implements AdapterView.OnItemClickListener, AdapterView.OnItemSelectedListener {
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private View mContentView;
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };
    private View mControlsView;
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
            mControlsView.setVisibility(View.VISIBLE);
        }
    };
    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };
    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (AUTO_HIDE) {
                        delayedHide(AUTO_HIDE_DELAY_MILLIS);
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    view.performClick();
                    break;
                default:
                    break;
            }
            return false;
        }
    };
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mControlsView.setVisibility(View.GONE);
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    private void show() {
        // Show the system bar
        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    /**
     * Schedules a call to hide() in delay milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    private ActivityFullscreenBinding binding;
    private ArrayAdapter<String> intentsAdapter;
    private ExplorerBehaviour explorerBehaviour;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityFullscreenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mVisible = true;
        mControlsView = binding.fullscreenContentControls;
        mContentView = binding.fullscreenContent;

        // Set up the user interaction to manually show or hide the system UI.
//        mContentView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                toggle();
//            }
//        });

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
//        binding.overlayButton.setOnTouchListener(mDelayHideTouchListener);
//        binding.findAllIntents.setOnTouchListener(mDelayHideTouchListener);
        PackagesCache.SetContext(this);
        PackagesCache.PrepareDefaultLaunchIntents();

        binding.explorerList.setChoiceMode(binding.explorerList.CHOICE_MODE_SINGLE);
        binding.explorerList.setOnItemClickListener(this);
        binding.explorerList.setOnItemSelectedListener(this);
        explorerBehaviour = new ExplorerBehaviour(this);
        RefreshExplorerList();
    }

    private void RefreshExplorerList() {
        ArrayList<ExplorerItem> arrayList = new ArrayList<>();
        File[] files = explorerBehaviour.ListContents();
        boolean isEmpty = files == null || files.length <= 0;
        boolean hasParent = explorerBehaviour.HasParent();
        if (!isEmpty || hasParent) {
            if (hasParent)
                arrayList.add(new ExplorerItem(ContextCompat.getDrawable(this, R.drawable.ic_baseline_folder_24), explorerBehaviour.GetParent(), "..", true));
            if (explorerBehaviour.GetDirectory().equalsIgnoreCase("/storage/emulated"))
                arrayList.add(new ExplorerItem(ContextCompat.getDrawable(this, R.drawable.ic_baseline_folder_24), "/storage/emulated/0", "0", true));
            if (!isEmpty) {
                for (int i = 0; i < files.length; i++) {
                    String absolutePath = files[i].getAbsolutePath();
                    Drawable icon = null;
                    if (!files[i].isDirectory()) {
                        String extension = GetExtension(absolutePath);
                        if (extension != null) {
                            String packageName = PackagesCache.GetPackageNameForExtension(extension);
                            if (packageName != null)
                                icon = PackagesCache.GetPackageIcon(packageName);
                        }
                    }
                    else
                        icon = ContextCompat.getDrawable(this, R.drawable.ic_baseline_folder_24);

                    arrayList.add(new ExplorerItem(icon, absolutePath, files[i].getName(), files[i].isDirectory()));
                }
            }
            ExplorerAdapter customAdapter = new ExplorerAdapter(this, arrayList);
            binding.explorerList.setAdapter(customAdapter);
        }

        binding.emptyView.setVisibility((isEmpty && !hasParent) ? View.VISIBLE : View.GONE);
        binding.explorerList.setVisibility((isEmpty && !hasParent) ? View.GONE : View.VISIBLE);
    }
    @Override
    public boolean onKeyDown(int key_code, KeyEvent key_event) {
        Log.d("Input", key_code + " " + key_event);
        if (key_code == KeyEvent.KEYCODE_BACK) {
//            super.onKeyDown(key_code, key_event);
            explorerBehaviour.GoUp();
            RefreshExplorerList();
            TryHighlightPrevDir();
            return true;
        }
        return false;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
    {
        Log.d("Explorer", "Item " + position + " selected");
    }
    @Override
    public void onNothingSelected(AdapterView<?> parent)
    {
        Log.d("Explorer", "Nothing selected");
    }
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {
        Log.d("Explorer", "Item " + position + " clicked");
        ExplorerItem clickedItem = (ExplorerItem)binding.explorerList.getSelectedItem();
        if (clickedItem.isDir) {
            explorerBehaviour.SetDirectory(clickedItem.absolutePath);
            RefreshExplorerList();
            TryHighlightPrevDir();
        }
        else {
            LaunchIntent(clickedItem);
        }
    }

    public static String GetExtension(String path) {
        String extension = null;
        if (path.contains("."))
            extension = path.substring(path.lastIndexOf(".") + 1);
        return extension;
    }

    private void LaunchIntent(ExplorerItem clickedItem) {
        //Cheat sheet: http://p.cweiske.de/221
//            IntentLaunchData launchData = new IntentLaunchData(Intent.ACTION_VIEW, "com.dsemu.drastic", "com.dsemu.drastic.DraSticActivity");
//            launchData.AddExtra(new IntentPutExtra("GAMEPATH", clickedItem.absolutePath));
        if (clickedItem.absolutePath.contains(".")) {
            String extension = GetExtension(clickedItem.absolutePath);

            IntentLaunchData fileLaunchIntent = PackagesCache.GetLaunchDataForExtension(extension);

            if (fileLaunchIntent != null) {
                IntentLaunchData.IntentType dataType = fileLaunchIntent.GetDataType();
                String data = null;
                if (dataType == IntentLaunchData.IntentType.AbsolutePath)
                    data = clickedItem.absolutePath;

                IntentPutExtra[] extras = fileLaunchIntent.GetExtras();
                String[] extrasValues = null;
                if (extras != null && extras.length > 0) {
                    extrasValues = new String[extras.length];
                    for (int i = 0; i < extras.length; i++)
                        if (extras[i].GetExtraType() == IntentLaunchData.IntentType.AbsolutePath)
                            extrasValues[i] = clickedItem.absolutePath;
                }
                startActivity(fileLaunchIntent.BuildIntent(data, extrasValues));
            }
            else
                Log.e("Explorer", "No launch intent associated with extension " + extension);
        }
        else
            Log.e("Explorer", "Missing extension, could not identify file");
    }

    private void TryHighlightPrevDir() {
        String previousDir = explorerBehaviour.GetLastItemInHistory();
        for (int i = 0; i < binding.explorerList.getCount(); i++) {
            String itemDir = ((ExplorerItem)binding.explorerList.getItemAtPosition(i)).absolutePath;
            if (itemDir.equalsIgnoreCase(previousDir)) {
                binding.explorerList.requestFocusFromTouch();
                binding.explorerList.setSelection(i);
                break;
            }
        }
    }

    public void getOverlayPermissionBtn(View view) {
        // Check if Android M or higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Show alert dialog to the user saying a separate permission is needed
            // Launch the settings activity if the user prefers
            Intent myIntent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
            startActivity(myIntent);
        }
    }

    public void listAllIntentsBtn(View view) {

        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
//        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
//        mainIntent.addCategory(Intent.CATEGORY_ALTERNATIVE);
//        mainIntent.addCategory(Intent.CATEGORY_APP_BROWSER);
//        mainIntent.addCategory(Intent.CATEGORY_APP_CALCULATOR);
//        mainIntent.addCategory(Intent.CATEGORY_APP_CALENDAR);
//        mainIntent.addCategory(Intent.CATEGORY_ACCESSIBILITY_SHORTCUT_TARGET);
//        mainIntent.addCategory(Intent.CATEGORY_APP_CONTACTS);
//        mainIntent.addCategory(Intent.CATEGORY_APP_EMAIL);
//        mainIntent.addCategory(Intent.CATEGORY_APP_FILES);
        List<ResolveInfo> pkgAppsList = this.getPackageManager().queryIntentActivities( mainIntent, 0);
        System.out.println("Found " + pkgAppsList.size() + " pkgs with given intent");
        binding.outputText.setText("Found " + pkgAppsList.size() + " pkgs");
//        ArrayList<View> buttons = new ArrayList<>();
        ArrayList<String> intentNames = new ArrayList<>();
        for (int i = 0; i < pkgAppsList.size(); i++) {
//            Button btnTag = new Button(this);
//            btnTag.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
//            btnTag.setText("Button");
//            btnTag.setId(i);
//            buttons.add(btnTag);
            String activityName = pkgAppsList.get(i).activityInfo.packageName;//.name;
            if (activityName != null)
            intentNames.add(activityName);
        }
        intentsAdapter = new ArrayAdapter(getApplicationContext(), android.R.layout.simple_list_item_1, intentNames);
        binding.intentsList.setAdapter(intentsAdapter);

//        Intent i = new Intent(Intent.ACTION_MAIN);
//        i.addCategory(Intent.CATEGORY_LAUNCHER);
//        mainIntent.setPackage("com.otherapp.package");
//        startActivity(mainIntent);
    }
    public void listLauncherIntentsBtn(View view) {

        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
//        mainIntent.addCategory(Intent.CATEGORY_ALTERNATIVE);
//        mainIntent.addCategory(Intent.CATEGORY_APP_BROWSER);
//        mainIntent.addCategory(Intent.CATEGORY_APP_CALCULATOR);
//        mainIntent.addCategory(Intent.CATEGORY_APP_CALENDAR);
//        mainIntent.addCategory(Intent.CATEGORY_ACCESSIBILITY_SHORTCUT_TARGET);
//        mainIntent.addCategory(Intent.CATEGORY_APP_CONTACTS);
//        mainIntent.addCategory(Intent.CATEGORY_APP_EMAIL);
//        mainIntent.addCategory(Intent.CATEGORY_APP_FILES);
        List<ResolveInfo> pkgAppsList = this.getPackageManager().queryIntentActivities( mainIntent, 0);
        System.out.println("Found " + pkgAppsList.size() + " pkgs with given intent");
        binding.outputText.setText("Found " + pkgAppsList.size() + " pkgs");
//        ArrayList<View> buttons = new ArrayList<>();
        ArrayList<String> intentNames = new ArrayList<>();
        for (int i = 0; i < pkgAppsList.size(); i++) {
//            Button btnTag = new Button(this);
//            btnTag.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
//            btnTag.setText("Button");
//            btnTag.setId(i);
//            buttons.add(btnTag);
            String activityName = pkgAppsList.get(i).activityInfo.packageName;//.name;
            if (activityName != null)
            intentNames.add(activityName);
        }
        intentsAdapter = new ArrayAdapter(getApplicationContext(), android.R.layout.simple_list_item_1, intentNames);
        binding.intentsList.setAdapter(intentsAdapter);

//        Intent i = new Intent(Intent.ACTION_MAIN);
//        i.addCategory(Intent.CATEGORY_LAUNCHER);
//        mainIntent.setPackage("com.otherapp.package");
//        startActivity(mainIntent);
    }
}