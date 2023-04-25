package com.OxGames.OxShell.Data;

import static androidx.core.content.ContextCompat.startActivity;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.net.Uri;
import android.util.Log;

import com.OxGames.OxShell.Helpers.AndroidHelpers;
import com.OxGames.OxShell.OxShellApp;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

public class IntentLaunchData implements Serializable {
    //Might be best to implement a guid that other data structures can identify by so they don't store copies
    private UUID id;

    public enum DataType { None, Uri, AbsolutePath, FileNameWithExt, FileNameWithoutExt, Integer, Boolean, Float, String }
    private String displayName;
    private ArrayList<String> associatedExtensions;
    private String action;
    private String packageName;
    private String className;
    private ArrayList<IntentPutExtra> extras;
    private DataType dataType = DataType.None;
    private int flags;

    public IntentLaunchData() {
        this(null, null, null, null, null, 0);
    }
//    public IntentLaunchData(String _packageName) {
//        this(null, null, _packageName, null, null, 0);
//    }
    public IntentLaunchData(String _displayName, String _action, String _packageName, String _className, String[] _extensions) {
        this(_displayName, _action, _packageName, _className, _extensions, 0);
    }
    public IntentLaunchData(String _displayName, String _action, String _packageName, String _className, String[] _extensions, int _flags) {
        id = UUID.randomUUID();
        displayName = _displayName;
        action = _action;
        packageName = _packageName;
        className = _className;
        extras = new ArrayList<>();
        associatedExtensions = new ArrayList<>();
        if (_extensions != null && _extensions.length > 0)
            for (int i = 0; i < _extensions.length; i++)
                if (_extensions[i] != null && !_extensions[i].isEmpty())
                    associatedExtensions.add(_extensions[i].toLowerCase());
//            Collections.addAll(associatedExtensions, extensions);
        flags = _flags;
    }

    public static IntentLaunchData createFromPackage(String packageName) {
        return createFromPackage(packageName, 0);
    }
    public static IntentLaunchData createFromPackage(String packageName, int flags) {
        return new IntentLaunchData(null, null, packageName, null, null, flags);
    }
    public static IntentLaunchData createFromAction(String action) {
        return createFromAction(action, 0);
    }
    public static IntentLaunchData createFromAction(String action, int flags) {
        return new IntentLaunchData(null, action, null, null, null, flags);
    }
    @Override
    public String toString() {
        return "IntentLaunchData{" +
                "id=" + id +
                ", displayName='" + displayName + '\'' +
                ", associatedExtensions=" + associatedExtensions +
                ", action='" + action + '\'' +
                ", packageName='" + packageName + '\'' +
                ", className='" + className + '\'' +
                ", extras=" + extras +
                ", dataType=" + dataType +
                '}';
    }

    public UUID getId() {
        return id;
    }
    public void setAction(String value) {
        action = value;
    }
    public String getAction() {
        return action;
    }
    public void setPackageName(String value) {
        packageName = value;
    }
    public String getPackageName() {
        return packageName;
    }
    public void setClassName(String value) {
        className = value;
    }
    public String getClassName() {
        return className;
    }
    public void setDisplayName(String value) {
        displayName = value;
    }
    public String getDisplayName() {
        return displayName;
    }
    public void setExtensions(String[] values) {
        associatedExtensions = new ArrayList<>(Arrays.asList(values));
    }
    public String[] getExtensions() {
        String[] extensions = new String[associatedExtensions.size()];
        return associatedExtensions.toArray(extensions);
    }

//    public Intent buildIntent(String[] extrasValues) {
//        return buildIntent(null, extrasValues);
//    }
//    public Intent buildIntent(String data) {
//        return buildIntent(data, null);
//    }
//    public Intent buildIntent() {
//        return buildIntent(null, null);
//    }
    public Intent buildIntent(String path) {
        Intent intent;
        if (action != null && !action.isEmpty()) {
            intent = new Intent(action);
        } else {
            intent = OxShellApp.getContext().getPackageManager().getLaunchIntentForPackage(packageName);
            if (intent == null)
                intent = new Intent(Intent.ACTION_MAIN);
            //else
            //    Log.d("IntentLaunchData", "Package manager gave intent the action: " + intent.getAction());
        }

        if (packageName != null && !packageName.isEmpty()) {
            if (className != null && !className.isEmpty())
                intent.setComponent(new ComponentName(packageName, className));
            else
                intent.setPackage(packageName);
        }
        for (IntentPutExtra extra : extras) {
            DataType extraType = extra.getExtraType();
            if (extraType != DataType.None) {
                if (extraType == DataType.Uri || extraType == DataType.AbsolutePath || extraType == DataType.FileNameWithExt || extraType == DataType.FileNameWithoutExt)
                    intent.putExtra(extra.getName(), formatData(path, extra.getExtraType()));
                else
                    extra.putExtraInto(intent);
            }
        }
        if (dataType != DataType.None && path != null && !path.isEmpty()) {
//            PackageManager packageManager = OxShellApp.getContext().getPackageManager();
//            ProviderInfo providerInfo = packageManager.resolveContentProvider(packageName, PackageManager.GET_META_DATA);
//            Uri fileUri = new Uri.Builder()
//                    .scheme(ContentResolver.SCHEME_CONTENT)
//                    .authority(providerInfo.authority)
//                    .path(path)
//                    .build();
//            intent.setData(fileUri);
            //intent.setDataAndType(formatData(path, dataType), "application/octet-stream");
            //intent.setDataAndType(formatData(path, dataType), "video/*");
            intent.setData(formatData(path, dataType));
            //intent.setData(Uri.parse(data));
        }
        if (flags > 0)
            intent.setFlags(flags);

        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);

        return intent;
    }
    public Intent buildIntent() {
        return buildIntent(null);
    }

    public void clearExtras() {
        extras.clear();
    }
    public void addExtra(IntentPutExtra extra) {
        extras.add(extra);
    }
    public IntentPutExtra[] getExtras() {
        return extras.toArray(new IntentPutExtra[0]);
    }
//    public void SetData(String _data) {
//        data = _data;
//    }
    public void setDataType(DataType _dataType) {
        dataType = _dataType;
    }
    public DataType getDataType() {
        return dataType;
    }

    public boolean containsExtension(String extension) {
        return associatedExtensions.contains(extension.toLowerCase());
    }

    public void launch(String path) {
        Intent intent = buildIntent(path);
        Log.i("IntentLaunchData", intent + ", " + intent.getExtras());
        try {
            startActivity(OxShellApp.getContext(), intent, null);
        } catch (Exception e) { Log.e("IntentLaunchData", "Failed to launch " + getPackageName() + ": " + e); }
    }
    public void launch() {
        launch(null);
    }
//    public void launch(String data, String[] extrasValues) {
//        Intent intent = buildIntent(data, extrasValues);
//        Log.i("IntentLaunchData", intent.toString());
//        startActivity(OxShellApp.getContext(), intent, null);
//    }
//    public void launch(String data) {
//        String dataEntry = null;
//        if (dataType != DataType.None)
//            dataEntry = data;
//
//        String[] extrasValues = null;
//        if (extras != null && extras.size() > 0) {
//            extrasValues = new String[extras.size()];
//            for (int i = 0; i < extras.size(); i++)
//                extrasValues[i] = data;
//        }
//        launch(dataEntry, extrasValues);
//    }
//    public void launch() {
//        launch(null, null);
//    }

    public static Uri formatData(String data, DataType dataType) {
        if (dataType == DataType.Uri) {
            //final Uri uri = AndroidHelpers.uriFromPath(data);
            //OxShellApp.getCurrentActivity().grantUriPermission(targetPkg, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
            return AndroidHelpers.uriFromPath(data);
        }
        if (dataType == DataType.AbsolutePath) {
//            String updatedData = data;
//            if (!updatedData.startsWith("file://"))
//                updatedData = "file://" + updatedData;
//            Log.d("IntentLaunchData", data + " => OldUri: " + Uri.decode(Uri.parse(data).toString()) + ", " + updatedData + " => NewUri: " + Uri.decode(Uri.parse(updatedData).toString()));

            return Uri.parse(data);
        }
        if (dataType == DataType.FileNameWithoutExt)
            return Uri.parse(AndroidHelpers.removeExtension(new File(data).getName()));
        if (dataType == DataType.FileNameWithExt)
            return Uri.parse(new File(data).getName());
        return null;
    }
}
