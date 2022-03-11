package com.OxGames.OxShell;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import java.io.File;
import java.util.Hashtable;

public class ExplorerActivity extends PagedActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_explorer);
        InitViewsTable();
    }
//    @Override
//    public void startActivityForResult(Intent intent, int requestCode) {
//        super.startActivityForResult(intent, requestCode);
//    }

    @Override
    protected void InitViewsTable() {
        allPages = new Hashtable<>();
        allPages.put(ActivityManager.Page.explorer, findViewById(R.id.explorer_list));
    }

    public void SendResult(String path) {
//            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        Intent returnIntent = new Intent();
        returnIntent.putExtra("result", path);
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
    }
    public void SendResultAlt(String path) {
        final Uri data = FileProvider.getUriForFile(this, "com.OxGames.OxShell.Explorer", new File(path));
        grantUriPermission(getPackageName(), data, Intent.FLAG_GRANT_READ_URI_PERMISSION);
        final Intent intent = new Intent(Intent.ACTION_VIEW).setDataAndType(data, "video/*").addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(intent);
    }
}