package com.OxGames.OxShell;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class FileHelpers {
    public static final int READ_EXTERNAL_STORAGE = 100;
    public static final int WRITE_EXTERNAL_STORAGE = 101;
    public static final int MANAGE_EXTERNAL_STORAGE = 102;

    public static boolean hasPermission(String permType) {
        return ContextCompat.checkSelfPermission(ActivityManager.getCurrentActivity(), permType) == PackageManager.PERMISSION_GRANTED;
    }
    public static boolean hasReadStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
            return hasPermission(Manifest.permission.MANAGE_EXTERNAL_STORAGE);
        else
            return hasPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE);
    }
    public static boolean hasWriteStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
            return hasPermission(Manifest.permission.MANAGE_EXTERNAL_STORAGE);
        else
            return hasPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }
    public static void requestReadStoragePermission() {
        //From here https://stackoverflow.com/questions/47292505/exception-writing-exception-to-parcel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
            ActivityCompat.requestPermissions(ActivityManager.getCurrentActivity(), new String[] { android.Manifest.permission.MANAGE_EXTERNAL_STORAGE }, MANAGE_EXTERNAL_STORAGE);
        else
            ActivityCompat.requestPermissions(ActivityManager.getCurrentActivity(), new String[] { android.Manifest.permission.READ_EXTERNAL_STORAGE }, READ_EXTERNAL_STORAGE);
        //ActivityCompat.shouldShowRequestPermissionRationale(ActivityManager.GetCurrentActivity(), Manifest.permission.READ_EXTERNAL_STORAGE);
    }
    public static void requestWriteStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
            ActivityCompat.requestPermissions(ActivityManager.getCurrentActivity(), new String[] { android.Manifest.permission.MANAGE_EXTERNAL_STORAGE }, MANAGE_EXTERNAL_STORAGE);
        else
            ActivityCompat.requestPermissions(ActivityManager.getCurrentActivity(), new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE }, WRITE_EXTERNAL_STORAGE);
    }

    public static Uri uriFromFile(Context context, File file) {
        //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) { //Unnecessary since version code is always over 24
            return FileProvider.getUriForFile(context, BuildConfig.DOCUMENTS_AUTHORITY, file);
        //} else {
        //    return Uri.fromFile(file);
        //}
    }

    public static void install(String path) {
        final Uri contentUri = uriFromFile(ActivityManager.getCurrentActivity(), new File(path));
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(contentUri, "application/vnd.android.package-archive");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        ActivityManager.getCurrentActivity().startActivity(intent);
    }
    public static void tryRun(File file) {
        String absPath = file.getAbsolutePath();
        if (FileHelpers.hasExtension(absPath)) {
            String extension = FileHelpers.getExtension(absPath);
            IntentLaunchData fileLaunchIntent = PackagesCache.getLaunchDataForExtension(extension);

            if (fileLaunchIntent != null) {
                String nameWithExt = file.getName();
                String nameWithoutExt = FileHelpers.removeExtension(nameWithExt);

                String[] extrasValues = null;
                IntentPutExtra[] extras = fileLaunchIntent.getExtras();
                if (extras != null && extras.length > 0) {
                    extrasValues = new String[extras.length];
                    for (int i = 0; i < extras.length; i++) {
                        IntentPutExtra current = extras[i];
                        if (current.getExtraType() == IntentLaunchData.DataType.AbsolutePath)
                            extrasValues[i] = absPath;
                        else if (current.getExtraType() == IntentLaunchData.DataType.FileNameWithExt)
                            extrasValues[i] = nameWithExt;
                        else if (current.getExtraType() == IntentLaunchData.DataType.FileNameWithoutExt)
                            extrasValues[i] = nameWithoutExt;
//                        else if (current.GetExtraType() == IntentLaunchData.DataType.Uri) {
//                            String uri = Uri.parse(absPath).getScheme();
//                            Log.d("Explorer", "Passing " + uri);
//                            extrasValues[i] = uri;
//                        }
                    }
                }

                String data = null;
                if (fileLaunchIntent.getDataType() == IntentLaunchData.DataType.AbsolutePath)
                    data = absPath;
                else if (fileLaunchIntent.getDataType() == IntentLaunchData.DataType.FileNameWithExt)
                    data = nameWithExt;
                else if (fileLaunchIntent.getDataType() == IntentLaunchData.DataType.FileNameWithoutExt)
                    data = nameWithoutExt;
//                else if (fileLaunchIntent.GetDataType() == IntentLaunchData.DataType.Uri) {
//                    String uri = Uri.parse(absPath).getScheme();
//                    Log.d("Explorer", "Passing " + uri);
//                    data = uri;
//                }

                fileLaunchIntent.launch(data, extrasValues);
            }
            else if (extension.equalsIgnoreCase("apk") || extension.equalsIgnoreCase("xapk")) {
                FileHelpers.install(absPath);
            }
            else
                Log.e("Explorer", "No launch intent associated with extension " + extension);
        }
        else
            Log.e("Explorer", "Missing extension, could not identify file");
    }

    public static File[] listContents(String dirName) {
        return (new File(dirName)).listFiles();
    }
    public static void makeDir(String dirName) {
        File homeItemsDir = new File(dirName);
        homeItemsDir.mkdirs();
    }
    public static void makeFile(String fileName) {
        try {
            File homeItemsFile = new File(fileName);
            if (!homeItemsFile.exists())
                homeItemsFile.createNewFile();
        } catch (IOException ex) {
            Log.e("HomeManager", ex.getMessage());
        }
    }
    public static boolean dirExists(String dirName) {
        return (new File(dirName)).isDirectory();
    }
    public static boolean fileExists(String dirName) {
        return (new File(dirName)).isFile();
    }
    public static void writeToFile(String fileName, String text) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
            writer.write(text);
            writer.close();
        } catch (IOException ex) {
            Log.e("HomeManager", ex.getMessage());
        }
    }
    public static String readFile(String fileName) {
        String fileData = null;
        try {
            BufferedReader r = new BufferedReader(new FileReader(fileName));
            StringBuilder total = new StringBuilder();
            String line;
            while ((line = r.readLine()) != null) {
                total.append(line).append('\n');
            }
            fileData = total.toString();
        } catch (IOException ex) {
            Log.e("ExplorerBehaviour", ex.getMessage());
        }
        //Log.d("Asset", assetData);
        return fileData;
    }

    public static String getExtension(String path) {
        String extension = null;
        if (path.contains("."))
            extension = path.substring(path.lastIndexOf(".") + 1);
        return extension;
    }
    public static boolean hasExtension(String path) {
        return !(new File(path).isDirectory()) && path.lastIndexOf(".") > 0;
    }
    public static String removeExtension(String path) {
        String fileName = (new File(path)).getName();
        while (hasExtension(fileName))
            fileName = fileName.substring(0, fileName.lastIndexOf("."));
        return fileName;
    }
}
