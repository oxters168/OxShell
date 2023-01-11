package com.OxGames.OxShell.Helpers;

import android.Manifest;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.view.View;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.OxGames.OxShell.BuildConfig;
import com.OxGames.OxShell.Data.IntentLaunchData;
import com.OxGames.OxShell.Data.IntentPutExtra;
import com.OxGames.OxShell.Data.ShortcutsCache;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class AndroidHelpers {
    public static final char[] ILLEGAL_FAT_CHARS = new char[] { '"', '*', '/', ':', '<', '>', '?', '\\', '|', 0x7F };
    public static final char[] ILLEGAL_EXT_CHARS = new char[] { '\0', '/' };
    public static final int UNKNOWN_FORMAT = 0;
    public static final int FAT_FORMAT = 1;
    public static final int EXT_FORMAT = 2;

    public static final int READ_EXTERNAL_STORAGE = 100;
    public static final int WRITE_EXTERNAL_STORAGE = 101;
    public static final int MANAGE_EXTERNAL_STORAGE = 102;
    private static WallpaperManager wallpaperManager;

    public static final String RECENT_ACTIVITY;
    static {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            RECENT_ACTIVITY = "com.android.launcher3.RecentsActivity";
        }
        else if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            RECENT_ACTIVITY = "com.android.systemui.recents.RecentsActivity";
        } else if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR1) {
            RECENT_ACTIVITY = "com.android.systemui.recent.RecentsActivity";
        } else {
            RECENT_ACTIVITY = "com.android.internal.policy.impl.RecentApplicationDialog";
        }
    }

    public static void setWallpaper(Context context, int resId) {
        if (wallpaperManager == null)
            wallpaperManager = WallpaperManager.getInstance(context);
        try { wallpaperManager.setResource(resId); } catch (IOException e) { e.printStackTrace(); }
    }
    public static void setWallpaper(Context context, Bitmap bitmap) {
        if (wallpaperManager == null)
            wallpaperManager = WallpaperManager.getInstance(context);
        try { wallpaperManager.setBitmap(bitmap); } catch (IOException e) { e.printStackTrace(); }
    }
    public static void setWallpaper(Context context, Bitmap bitmap, Rect visibleCropHint, boolean allowBackup) {
        if (wallpaperManager == null)
            wallpaperManager = WallpaperManager.getInstance(context);
        try { wallpaperManager.setBitmap(bitmap, visibleCropHint, allowBackup); } catch (IOException e) { e.printStackTrace(); }
    }
    public static void setWallpaper(Context context, Bitmap bitmap, Rect visibleCropHint, boolean allowBackup, int which) {
        // source: https://stackoverflow.com/questions/53466302/is-there-any-way-of-changing-lockscreen-wallpaper-photo-in-android-programmatica
        // the 'which' parameter can be set to WallpaperManager.FLAG_LOCK to set the lockscreen wallpaper, the rect can be set to null and allowBackup to true
        if (wallpaperManager == null)
            wallpaperManager = WallpaperManager.getInstance(context);
        try { wallpaperManager.setBitmap(bitmap, visibleCropHint, allowBackup, which); } catch (IOException e) { e.printStackTrace(); }
    }
    public static Bitmap bitmapFromResource(Resources res, int resId) {
        return BitmapFactory.decodeResource(res, resId);
    }

    public static String readAssetAsString(Context context, String asset) {
        String assetData = null;
        try {
            InputStream inputStream = context.getAssets().open(asset);
            BufferedReader r = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder total = new StringBuilder();
            String line;
            while ((line = r.readLine()) != null) {
                total.append(line).append('\n');
            }
            assetData = total.toString();
        } catch (IOException ex) {
            Log.e("Reading_Asset_Error", ex.toString());
        }
        //Log.d("Asset", assetData);
        return assetData;
    }

    public static int getRelativeLeft(View view) {
        if (view.getParent() == view.getRootView())
            return view.getLeft();
        else
            return view.getLeft() + getRelativeLeft((View) view.getParent());
    }
    public static int getRelativeTop(View view) {
        if (view.getParent() == view.getRootView())
            return view.getTop();
        else
            return view.getTop() + getRelativeTop((View) view.getParent());
    }

    public static String combinePaths(String... subPaths) {
        StringBuilder combined = new StringBuilder();
        for (String subPath : subPaths) {
            boolean firstEndsWithSeparator = combined.toString().lastIndexOf('/') == combined.length() - 1;
            boolean secondStartsWithSeparator = subPath.indexOf('/') == 0;
            if (firstEndsWithSeparator && secondStartsWithSeparator)
                combined.append(subPath.substring(1));
            else if (firstEndsWithSeparator || secondStartsWithSeparator)
                combined.append(subPath);
            else
                combined.append("/").append(subPath);
        }
//        boolean firstEndsWithSeparator = first.lastIndexOf('/') == first.length() - 1;
//        boolean secondStartsWithSeparator = second.indexOf('/') == 0;
//        if (firstEndsWithSeparator && secondStartsWithSeparator)
//            combined = first + second.substring(1);
//        else if (firstEndsWithSeparator || secondStartsWithSeparator)
//            combined = first + second;
//        else
//            combined = first + "/" + second;
        return combined.toString();
    }
    public static boolean hasPermission(String permType) {
        return ContextCompat.checkSelfPermission(ActivityManager.getCurrentActivity(), permType) == PackageManager.PERMISSION_GRANTED;
    }
    public static boolean hasReadStoragePermission() {
        Log.d("FileHelpers", "Checking has read permission");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
            return hasPermission(Manifest.permission.MANAGE_EXTERNAL_STORAGE);
        else
            return hasPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE);
    }
    public static boolean hasWriteStoragePermission() {
        Log.d("FileHelpers", "Checking has write permission");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
            return hasPermission(Manifest.permission.MANAGE_EXTERNAL_STORAGE);
        else
            return hasPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }
    public static void requestReadStoragePermission() {
        Log.d("FileHelpers", "Requesting read permission");
        //From here https://stackoverflow.com/questions/47292505/exception-writing-exception-to-parcel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
            ActivityCompat.requestPermissions(ActivityManager.getCurrentActivity(), new String[] { android.Manifest.permission.MANAGE_EXTERNAL_STORAGE }, MANAGE_EXTERNAL_STORAGE);
        else
            ActivityCompat.requestPermissions(ActivityManager.getCurrentActivity(), new String[] { android.Manifest.permission.READ_EXTERNAL_STORAGE }, READ_EXTERNAL_STORAGE);
        //ActivityCompat.shouldShowRequestPermissionRationale(ActivityManager.GetCurrentActivity(), Manifest.permission.READ_EXTERNAL_STORAGE);
    }
    public static void requestWriteStoragePermission() {
        Log.d("FileHelpers", "Requesting write permission");
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
        if (!isDirectory(absPath)) {
            String extension = AndroidHelpers.getExtension(absPath);
            if (extension != null) {
                IntentLaunchData fileLaunchIntent = ShortcutsCache.getLaunchDataForExtension(extension);

                if (fileLaunchIntent != null) {
                    String nameWithExt = file.getName();
                    String nameWithoutExt = AndroidHelpers.removeExtension(nameWithExt);

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
                } else if (extension.equalsIgnoreCase("apk") || extension.equalsIgnoreCase("xapk")) {
                    AndroidHelpers.install(absPath);
                } else
                    Log.e("Explorer", "No launch intent associated with extension " + extension);
            } else
                Log.e("Explorer", "Missing extension, could not identify file");
        } else
            Log.e("Explorer", "Cannot run a directory");
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
            File file = new File(fileName);
            if (!file.exists())
                file.createNewFile();
        } catch (IOException ex) {
            Log.e("HomeManager", ex.getMessage());
        }
    }
    public static boolean isNameFSLegal(String fileName, int filesystem) {
        switch (filesystem) {
            case FAT_FORMAT:
                for (char illegalChar : ILLEGAL_FAT_CHARS)
                    if (fileName.indexOf(illegalChar) >= 0)
                        return false;
                break;
            case EXT_FORMAT:
                for (char illegalChar : ILLEGAL_EXT_CHARS)
                    if (fileName.indexOf(illegalChar) >= 0)
                        return false;
                break;
            default:
                for (char illegalChar : ILLEGAL_FAT_CHARS)
                    if (fileName.indexOf(illegalChar) >= 0)
                        return false;
                for (char illegalChar : ILLEGAL_EXT_CHARS)
                    if (fileName.indexOf(illegalChar) >= 0)
                        return false;
                break;
        }
        return true;
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
        if (hasExtension(path))
            extension = path.substring(path.lastIndexOf(".") + 1);
        return extension;
    }
    public static boolean hasExtension(String path) {
        int dotIndex = path.lastIndexOf(".");
        int spaceIndex = path.lastIndexOf(" ");
        return dotIndex > 0 && spaceIndex < dotIndex;
    }
    public static boolean isDirectory(String path) {
        return new File(path).isDirectory();
    }
    public static String removeExtension(String path) {
        String fileName = (new File(path)).getName();
        if (!isDirectory(path))
            while (hasExtension(fileName))
                fileName = fileName.substring(0, fileName.lastIndexOf("."));
        return fileName;
    }
}
