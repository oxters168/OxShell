package com.OxGames.OxShell;

import androidx.core.content.ContextCompat;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
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

public class FullscreenActivity extends Activity implements AdapterView.OnItemClickListener, AdapterView.OnItemSelectedListener {
    private ActivityFullscreenBinding binding;
    private ArrayAdapter<String> intentsAdapter;
    private ExplorerBehaviour explorerBehaviour;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityFullscreenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

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