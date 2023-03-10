package com.OxGames.OxShell.Helpers;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.Locale;

public class ExplorerBehaviour {
    private LinkedList<String> history;
    private int maxHistory = 100;
    // where we currently are
    private File current;

    private String[] tempForCutOrCopy;
    private boolean isCopying;
    private boolean isCutting;

    public ExplorerBehaviour() {
        history = new LinkedList<>();

        //TODO: make default path be internal files and make internal and external directories quick access (probably with the use of getFilesDir() & getExternalMediaDirs())
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
        //Log.d("Files", "Path: " + path);
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
        if (!AndroidHelpers.hasReadStoragePermission())
            AndroidHelpers.requestReadStoragePermission();
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

    public boolean isCopying() {
        return isCopying;
    }
    public boolean isCutting() {
        return isCutting;
    }
    public void copy(String... paths) {
        tempForCutOrCopy = paths.clone();
        isCopying = true;
        isCutting = false;
    }
    public void cut(String... paths) {
        tempForCutOrCopy = paths.clone();
        isCopying = false;
        isCutting = true;
    }
    public void paste() {
        if (tempForCutOrCopy != null) {
            if (isCopying) {
                copyFiles(current.getAbsolutePath(), tempForCutOrCopy);
                isCopying = false;
            } else if (isCutting) {
                moveFiles(current.getAbsolutePath(), tempForCutOrCopy);
                isCutting = false;
            } else
                Log.e("ExplorerBehaviour", "Could not paste when not cut or copied");
            tempForCutOrCopy = null;
        }
    }
    public static void delete(String... files) {
        for (String path : files) {
            if (AndroidHelpers.isDirectory(path)) {
                File[] temp = new File(path).listFiles();
                String[] subFiles = new String[temp.length];
                for (int i = 0; i < temp.length; i++)
                    subFiles[i] = temp[i].getAbsolutePath();
                delete(subFiles);
            }
            new File(path).delete();
        }
    }
    public static void copyFiles(String destination, String... files) {
        // TODO: have a list of errors or something that can be shown to the user
        // TODO: possibly change getting all files/folders that will be copied then copy them to allow for copying into self
        for (String path : files) {
            if (!destination.toLowerCase().contains(path.toLowerCase())) {
                if (AndroidHelpers.isDirectory(path)) {
                    String newDir = AndroidHelpers.combinePaths(destination, new File(path).getName());
                    try {
                        // copy the directory first (this doesn't copy the contents)
                        Files.copy(Paths.get(path), Paths.get(newDir));
                    } catch (Exception e) {
                        Log.e("ExplorerBehaviour", e.toString());
                    }
                    if (AndroidHelpers.dirExists(newDir)) {
                        Log.d("ExplorerBehaviour", "Copying the contents of " + path + " to " + newDir);
                        File[] temp = new File(path).listFiles();
                        String[] subFiles = new String[temp.length];
                        for (int i = 0; i < temp.length; i++)
                            subFiles[i] = temp[i].getAbsolutePath();
                        copyFiles(newDir, subFiles);
                    }
                } else {
                    try {
                        // TODO: add copy options
                        String dest = destination;
                        if (AndroidHelpers.isDirectory(dest))
                            dest = AndroidHelpers.combinePaths(dest, new File(path).getName());
                        //Log.d("ExplorerBehaviour", "Copying " + path + " to " + dest);
                        Files.copy(Paths.get(path), Paths.get(dest));
                    } catch (Exception e) {
                        Log.e("ExplorerBehaviour", e.toString());
                    }
                }
            } else
                Log.e("ExplorerBehaviour", "Attempted to copy into self");
        }
    }
    public static void moveFiles(String destination, String... files) {
        // TODO: have a list of errors or something that can be shown to the user
        for (String path : files) {
            if (!destination.toLowerCase().contains(path.toLowerCase())) {
                if (AndroidHelpers.isDirectory(path)) {
                    String newDir = AndroidHelpers.combinePaths(destination, new File(path).getName());
                    try {
                        // copy the directory first (this doesn't copy the contents)
                        Files.copy(Paths.get(path), Paths.get(newDir));
                    } catch (Exception e) {
                        Log.e("ExplorerBehaviour", e.toString());
                    }
                    if (AndroidHelpers.dirExists(newDir)) {
                        File[] temp = new File(path).listFiles();
                        String[] subFiles = new String[temp.length];
                        for (int i = 0; i < temp.length; i++)
                            subFiles[i] = temp[i].getAbsolutePath();
                        moveFiles(newDir, subFiles);
                        // remove the old directory since its now empty
                        new File(path).delete();
                    }
                } else {
                    try {
                        // TODO: add copy options
                        String dest = destination;
                        if (AndroidHelpers.isDirectory(dest))
                            dest = AndroidHelpers.combinePaths(dest, new File(path).getName());
                        Files.move(Paths.get(path), Paths.get(dest));
                    } catch (Exception e) {
                        Log.e("ExplorerBehaviour", e.toString());
                    }
                }
            } else
                Log.e("ExplorerBehaviour", "Attempted to move into self");
        }
    }
}
