package com.OxGames.OxShell;

import android.app.Activity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;

import android.os.Build;
import android.content.Intent;
import android.provider.Settings;
import android.content.pm.ResolveInfo;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends Activity {
    private static HomeActivity instance;
    private ArrayAdapter<String> intentsAdapter;
    public static DisplayMetrics displayMetrics;
    private List<PermissionsListener> permissionListeners = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instance = this;

        setContentView(R.layout.activity_home);

//        ((HomeView)instance.findViewById(R.id.home_view)).setChoiceMode(AbsListView.CHOICE_MODE_NONE);

        PackagesCache.SetContext(this);
        PackagesCache.PrepareDefaultLaunchIntents();

        displayMetrics = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        for (PermissionsListener pl : permissionListeners) {
            pl.onPermissionResponse(requestCode, permissions, grantResults);
        }
    }

    public static HomeActivity GetInstance() {
        return instance;
    }

    public void AddPermissionListener(PermissionsListener listener) {
        permissionListeners.add(listener);
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
//        binding.outputText.setText("Found " + pkgAppsList.size() + " pkgs");
        TextView outputText = findViewById(R.id.output_text);
        outputText.setText("Found " + pkgAppsList.size() + " pkgs");
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
//        binding.intentsList.setAdapter(intentsAdapter);
        ListView intentsList = findViewById(R.id.intents_list);
        intentsList.setAdapter(intentsAdapter);

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
//        binding.outputText.setText("Found " + pkgAppsList.size() + " pkgs");
        TextView outputText = findViewById(R.id.output_text);
        outputText.setText("Found " + pkgAppsList.size() + " pkgs");
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
//        binding.intentsList.setAdapter(intentsAdapter);
        ListView intentsList = findViewById(R.id.intents_list);
        intentsList.setAdapter(intentsAdapter);

//        Intent i = new Intent(Intent.ACTION_MAIN);
//        i.addCategory(Intent.CATEGORY_LAUNCHER);
//        mainIntent.setPackage("com.otherapp.package");
//        startActivity(mainIntent);
    }
}