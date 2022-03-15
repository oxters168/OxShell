package com.OxGames.OxShell;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.util.LinkedList;

public class ExplorerBehaviour {
    public static final int READ_EXTERNAL_STORAGE = 100;
    public static final int WRITE_EXTERNAL_STORAGE = 101;
    private LinkedList<String> history;
    private int maxHistory = 100;
    private File current;

    public ExplorerBehaviour() {
        history = new LinkedList<>();

        String startPath = Environment.getExternalStorageDirectory().toString();
        SetDirectory(startPath);
    }

    public boolean HasParent() {
        String parentDir = current.getParent();
        try {
            File parent = new File(parentDir);
            return parent.exists();
        }
        catch (Exception e) {
            return false;
        }
    }
    public String GetParent() {
        return current.getParent();
    }
    public void GoUp() {
        SetDirectory(GetParent());
    }
    public void SetDirectory(String path) {
        Log.d("Files", "Path: " + path);
        try {
            current = new File(path);
            AppendHistory(path);

            if (!current.canRead())
                Log.e("Files", "Cannot read contents of directory");
            if (!current.exists())
                Log.e("Files", "Current directory does not exist somehow");
        }
        catch (Exception e) {
            Log.e("Files", e.toString());
        }
    }
    public String GetDirectory() {
        return current.getAbsolutePath();
    }
    public File[] ListContents() {
        if (!HasReadStoragePermission())
            RequestReadStoragePermission();
        return current.listFiles();
    }

    public String GetLastItemInHistory() {
        return history.get(history.size() - 2);
    }
    private void AppendHistory(String path) {
        history.add(path);
        while (history.size() > maxHistory)
            history.removeFirst();
    }

    public static String GetExtension(String path) {
        String extension = null;
        if (path.contains("."))
            extension = path.substring(path.lastIndexOf(".") + 1);
        return extension;
    }

    public static boolean HasPermission(String permType) {
        return ContextCompat.checkSelfPermission(ActivityManager.GetCurrentActivity(), permType) == PackageManager.PERMISSION_GRANTED;
    }
    public static boolean HasReadStoragePermission() {
        return HasPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
    }
    public static boolean HasWriteStoragePermission() {
        return HasPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }
    public static void RequestReadStoragePermission() {
        //From here https://stackoverflow.com/questions/47292505/exception-writing-exception-to-parcel
//        if(!HasReadStoragePermission())
            ActivityCompat.requestPermissions(ActivityManager.GetCurrentActivity(), new String[] { Manifest.permission.READ_EXTERNAL_STORAGE }, READ_EXTERNAL_STORAGE);
//        ActivityCompat.shouldShowRequestPermissionRationale(ActivityManager.GetCurrentActivity(), Manifest.permission.READ_EXTERNAL_STORAGE);
    }
    public static void RequestWriteStoragePermission() {
//        if(!HasWriteStoragePermission())
            ActivityCompat.requestPermissions(ActivityManager.GetCurrentActivity(), new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE }, WRITE_EXTERNAL_STORAGE);
    }
}
