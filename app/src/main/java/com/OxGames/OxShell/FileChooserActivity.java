package com.OxGames.OxShell;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import java.io.File;
import java.util.Hashtable;

public class FileChooserActivity extends PagedActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currentPage = ActivityManager.Page.chooser;
        ActivityManager.setCurrent(currentPage);
        setContentView(R.layout.activity_chooser);
        initViewsTable();
    }
//    @Override
//    public void startActivityForResult(Intent intent, int requestCode) {
//        super.startActivityForResult(intent, requestCode);
//        Log.d("StartForResult", requestCode + " "  + intent.toString());
//    }

    @Override
    protected void initViewsTable() {
        allPages = new Hashtable<>();
        allPages.put(ActivityManager.Page.chooser, findViewById(R.id.explorer_list));
    }

    public void sendResult(String path) {
//        ExplorerBehaviour.GrantWriteStoragePermission();
//        final Uri data = FileProvider.getUriForFile(this, BuildConfig.DOCUMENTS_AUTHORITY, new File(path));
        //final Uri data = CustomFileProvider.getUriForFile(path);
        final Uri data = FileHelpers.uriFromFile(this, new File(path));
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
