package com.OxGames.OxShell;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.LinkedList;

public class ExplorerBehaviour {
    private LinkedList<String> history;
    private int maxHistory = 100;
    private File current;

    public ExplorerBehaviour() {
        history = new LinkedList<>();

        String startPath = Environment.getExternalStorageDirectory().toString();
        setDirectory(startPath);
    }

    public boolean hasParent() {
        String parentDir = current.getParent();
        try {
            File parent = new File(parentDir);
            return parent.exists();
        }
        catch (Exception e) {
            return false;
        }
    }
    public String getParent() {
        return current.getParent();
    }
    public void goUp() {
        setDirectory(getParent());
    }
    public void setDirectory(String path) {
        Log.d("Files", "Path: " + path);
        try {
            current = new File(path);
            appendHistory(path);

            if (!current.canRead())
                Log.e("Files", "Cannot read contents of directory");
            if (!current.exists())
                Log.e("Files", "Current directory does not exist somehow");
        }
        catch (Exception e) {
            Log.e("Files", e.toString());
        }
    }
    public String getDirectory() {
        return current.getAbsolutePath();
    }
    public File[] listContents() {
        if (!FileHelpers.hasReadStoragePermission())
            FileHelpers.requestReadStoragePermission();
        //Might be how to do it for android 11+
        //IntentLaunchData ild = new IntentLaunchData();
        //ild.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
        //ild.launch();
        return current.listFiles();
    }

    public String getLastItemInHistory() {
        return history.get(history.size() - 2);
    }
    private void appendHistory(String path) {
        history.add(path);
        while (history.size() > maxHistory)
            history.removeFirst();
    }
}
