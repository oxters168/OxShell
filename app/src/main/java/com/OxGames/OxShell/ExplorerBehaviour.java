package com.OxGames.OxShell;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.util.LinkedList;

public class ExplorerBehaviour {

    private LinkedList<String> history;
    private int maxHistory = 100;
    private Activity context;
    private final int READ_EXTERNAL_STORAGE = 100;
    private File current;

    public ExplorerBehaviour(Activity _context) {
        context = _context;
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
        GrantStoragePermission();
        return current.listFiles();
    }
    public File[] ListRoots() {
        return File.listRoots();
    }

    public String GetLastItemInHistory() {
        return history.get(history.size() - 2);
    }
    private void AppendHistory(String path) {
        history.add(path);
        while (history.size() > maxHistory)
            history.removeFirst();
    }

    public void GrantStoragePermission() {
        /*---------------------------- GRANT PERMISSIONS START-------------------------*/
        // Main part to grant permission. Handle other cases of permission denied
        //   yourself.
        if(ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(context, new String[] { Manifest.permission.READ_EXTERNAL_STORAGE }, READ_EXTERNAL_STORAGE);
        /*---------------------------- GRANT PERMISSIONS OVER-------------------------*/
    }
}
