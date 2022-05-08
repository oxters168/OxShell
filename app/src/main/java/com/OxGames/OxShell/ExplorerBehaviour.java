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
    public static final int READ_EXTERNAL_STORAGE = 100;
    public static final int WRITE_EXTERNAL_STORAGE = 101;
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
        if (!hasReadStoragePermission())
            requestReadStoragePermission();
        //Might be how to do it for android 11+
        //IntentLaunchData ild = new IntentLaunchData();
        //ild.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
        //ild.launch();
        return current.listFiles();
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

    public String getLastItemInHistory() {
        return history.get(history.size() - 2);
    }
    private void appendHistory(String path) {
        history.add(path);
        while (history.size() > maxHistory)
            history.removeFirst();
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

    public static boolean hasPermission(String permType) {
        return ContextCompat.checkSelfPermission(ActivityManager.getCurrentActivity(), permType) == PackageManager.PERMISSION_GRANTED;
    }
    public static boolean hasReadStoragePermission() {
        return hasPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
    }
    public static boolean hasWriteStoragePermission() {
        return hasPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }
    public static void requestReadStoragePermission() {
        //From here https://stackoverflow.com/questions/47292505/exception-writing-exception-to-parcel
//        if(!HasReadStoragePermission())
            ActivityCompat.requestPermissions(ActivityManager.getCurrentActivity(), new String[] { Manifest.permission.READ_EXTERNAL_STORAGE }, READ_EXTERNAL_STORAGE);
//        ActivityCompat.shouldShowRequestPermissionRationale(ActivityManager.GetCurrentActivity(), Manifest.permission.READ_EXTERNAL_STORAGE);
    }
    public static void requestWriteStoragePermission() {
//        if(!HasWriteStoragePermission())
            ActivityCompat.requestPermissions(ActivityManager.getCurrentActivity(), new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE }, WRITE_EXTERNAL_STORAGE);
    }
}
