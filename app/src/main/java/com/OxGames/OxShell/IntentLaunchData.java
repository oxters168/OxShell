package com.OxGames.OxShell;

import static androidx.core.content.ContextCompat.startActivity;

import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.google.gson.Gson;

import java.util.ArrayList;

public class IntentLaunchData {
    public enum DataType { None, AbsolutePath, FileNameWithExt, FileNameWithoutExt }
    private ArrayList<String> associatedExtensions;
    private String action;
    private String packageName;
    private String className;
    private ArrayList<IntentPutExtra> extras;
    private DataType dataType = DataType.None;

    public IntentLaunchData(String _packageName) {
        packageName = _packageName;
    }
    public IntentLaunchData(String _action, String _packageName, String _className, String[] extensions) {
        action = _action;
        packageName = _packageName;
        className = _className;
        extras = new ArrayList<>();
        associatedExtensions = new ArrayList<>();
        if (extensions != null && extensions.length > 0)
            for (int i = 0; i < extensions.length; i++)
                if (extensions[i] != null && !extensions[i].isEmpty())
                    associatedExtensions.add(extensions[i].toLowerCase());
//            Collections.addAll(associatedExtensions, extensions);
    }

    public String GetAction() {
        return action;
    }
    public String GetPackageName() {
        return packageName;
    }
    public String GetClassName() {
        return className;
    }

    public Intent BuildIntent(String[] extrasValues) {
        return BuildIntent(null, extrasValues);
    }
    public Intent BuildIntent(String data) {
        return BuildIntent(data, null);
    }
    public Intent BuildIntent() {
        return BuildIntent(null, null);
    }
    public Intent BuildIntent(String data, String[] extrasValues) {
        if (action != null && !action.isEmpty()) {
            Intent intent = new Intent();
            if (action != null && !action.isEmpty())
                intent.setAction(action);
            if (className != null && !className.isEmpty())
                intent.setComponent(new ComponentName(packageName, className));
            else if (packageName != null && !packageName.isEmpty())
                intent.setPackage(packageName);
            for (int i = 0; i < extras.size(); i++)
                intent.putExtra(extras.get(i).GetName(), extrasValues[i]);
            if (data != null && !data.isEmpty())
                intent.setData(Uri.parse(data));
            return intent;
        } else
            return ActivityManager.GetActivityInstance(ActivityManager.GetCurrent()).getPackageManager().getLaunchIntentForPackage(packageName);
    }

    public void AddExtra(IntentPutExtra extra) {
        extras.add(extra);
    }
    public IntentPutExtra[] GetExtras() {
        IntentPutExtra[] extrasArray = new IntentPutExtra[extras.size()];
        extrasArray = extras.toArray(extrasArray);
        return extrasArray;
    }
//    public void SetData(String _data) {
//        data = _data;
//    }
    public void SetDataType(DataType _dataType) {
        dataType = _dataType;
    }
    public DataType GetDataType() {
        return dataType;
    }

    public String ToJSON() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }
    public static IntentLaunchData FromJSON(String json) {
        Gson gson = new Gson();
        return gson.fromJson(json, IntentLaunchData.class);
    }
    public boolean ContainsExtension(String extension) {
        return associatedExtensions.contains(extension.toLowerCase());
    }

    public void Launch(String data, String[] extrasValues) {
        //IntentLaunchData launchData = new IntentLaunchData(Intent.ACTION_VIEW, "com.dsemu.drastic", "com.dsemu.drastic.DraSticActivity");
        //launchData.AddExtra(new IntentPutExtra("GAMEPATH", clickedItem.absolutePath));
        startActivity(ActivityManager.GetActivityInstance(ActivityManager.GetCurrent()), BuildIntent(data, extrasValues), null);
    }
    public void Launch(String data) {
        String dataEntry = null;
        if (dataType != DataType.None)
            dataEntry = data;

        String[] extrasValues = null;
        if (extras != null && extras.size() > 0) {
            extrasValues = new String[extras.size()];
            for (int i = 0; i < extras.size(); i++)
                extrasValues[i] = data;
        }
        Launch(dataEntry, extrasValues);
    }
    public void Launch() {
        Launch(null, null);
    }
}
