package com.OxGames.OxShell;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelFileDescriptor;
import android.os.UserHandle;
import android.util.Log;
import android.view.Display;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;

public class FileChooserActivity extends PagedActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currentPage = ActivityManager.Page.chooser;
        ActivityManager.SetCurrent(currentPage);
        setContentView(R.layout.activity_chooser);
        InitViewsTable();
    }
//    @Override
//    public void startActivityForResult(Intent intent, int requestCode) {
//        super.startActivityForResult(intent, requestCode);
//        Log.d("StartForResult", requestCode + " "  + intent.toString());
//    }

    @Override
    protected void InitViewsTable() {
        allPages = new Hashtable<>();
        allPages.put(ActivityManager.Page.chooser, findViewById(R.id.explorer_list));
    }

    public void SendResult(String path) {
//        ExplorerBehaviour.GrantWriteStoragePermission();
//        final Uri data = FileProvider.getUriForFile(this, BuildConfig.DOCUMENTS_AUTHORITY, new File(path));
        final Uri data = CustomFileProvider.getUriForFile(path);
//        ParcelFileDescriptor data = null;
//        try {
//            data = ParcelFileDescriptor.open(new File(path), ParcelFileDescriptor.MODE_READ_ONLY);
//        } catch (FileNotFoundException ex) {
//            Log.d("FileChooser", ex.getMessage());
//        }

        Intent returnIntent = new Intent();
//        returnIntent.putExtra("result", data);
        returnIntent.setData(data);
        returnIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        setResult(Activity.RESULT_OK, returnIntent);
        Log.d("FileChooser", "Called from " + getCallingActivity() + " giving result " + data);
        finish();
    }
//    public void SendResultAlt(String path) {
//        final Uri data = FileProvider.getUriForFile(this, "com.OxGames.OxShell.Explorer", new File(path));
//        grantUriPermission(getPackageName(), data, Intent.FLAG_GRANT_READ_URI_PERMISSION);
//        final Intent intent = new Intent(Intent.ACTION_VIEW).setDataAndType(data, "video/*").addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//        startActivity(intent);
//    }
}
