package com.OxGames.OxShell;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;

import androidx.core.content.ContextCompat;

import java.io.File;
import java.util.ArrayList;

public class ExplorerView extends SlideTouchListView implements PermissionsListener {
    private ExplorerBehaviour explorerBehaviour;
    private SlideTouchHandler slideTouch = new SlideTouchHandler();
//    private ActivityManager.Page CURRENT_PAGE = ActivityManager.Page.explorer;

    public ExplorerView(Context context) {
        super(context);
        ActivityManager.GetCurrentActivity().AddPermissionListener(this);
        explorerBehaviour = new ExplorerBehaviour();
        Refresh();
    }

    public ExplorerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        PagedActivity currentActivity = ActivityManager.GetCurrentActivity();
//        Log.d("ExplorerView", currentActivity.toString());
        currentActivity.AddPermissionListener(this);
        explorerBehaviour = new ExplorerBehaviour();
        Refresh();
    }

    public ExplorerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        ActivityManager.GetCurrentActivity().AddPermissionListener(this);
        explorerBehaviour = new ExplorerBehaviour();
        Refresh();
    }

    @Override
    public void onPermissionResponse(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == ExplorerBehaviour.READ_EXTERNAL_STORAGE) {
            // If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission is granted. Continue the action or workflow
                // in your app.
                Log.d("Explorer", "Storage permission granted");
                Refresh();
            }  else {
                // Explain to the user that the feature is unavailable because
                // the features requires a permission that the user has denied.
                // At the same time, respect the user's decision. Don't link to
                // system settings in an effort to convince the user to change
                // their decision.
                Log.e("Explorer", "Storage permission denied");
            }
        }
    }

    @Override
    public boolean onKeyDown(int key_code, KeyEvent key_event) {
//        Log.d("ExplorerView", key_code + " " + key_event);
        if (key_code == KeyEvent.KEYCODE_BUTTON_B || key_code == KeyEvent.KEYCODE_BACK) {
            if (ActivityManager.GetCurrent() != ActivityManager.Page.chooser) {
                ActivityManager.GoTo(ActivityManager.Page.home);
                return false;
            } else
                return super.onKeyDown(key_code, key_event);
        }
        if (key_code == KeyEvent.KEYCODE_BUTTON_Y) {
            GoUp();
            return false;
        }

        return super.onKeyDown(key_code, key_event);
    }

    @Override
    public void MakeSelection() {
        DetailItem clickedItem = (DetailItem)getItemAtPosition(properPosition);
        if (clickedItem.obj == null) {
            ((FileChooserActivity)ActivityManager.GetInstance(FileChooserActivity.class)).SendResult(explorerBehaviour.GetDirectory());
        } else {
            File file = (File)clickedItem.obj;
            if (file.isDirectory()) {
//            ((ExplorerActivity)ExplorerActivity.GetInstance()).SendResult(file.getAbsolutePath());
//            startActivityForResult(intent, requestCode);
                explorerBehaviour.SetDirectory(file.getAbsolutePath());
                Refresh();
                TryHighlightPrevDir();
            } else {
                if (ActivityManager.GetCurrent() == ActivityManager.Page.chooser)
                    ((FileChooserActivity)ActivityManager.GetInstance(FileChooserActivity.class)).SendResult(file.getAbsolutePath());
                else
                    TryRun(clickedItem);
            }
        }
    }

    public void GoUp() {
        explorerBehaviour.GoUp();
        Refresh();
//        SetProperPosition(0);
        TryHighlightPrevDir();
    }
    private void TryHighlightPrevDir() {
        String previousDir = explorerBehaviour.GetLastItemInHistory();
        for (int i = 0; i < getCount(); i++) {
            File file = ((File)((DetailItem)getItemAtPosition(i)).obj);
            if (file != null) {
                String itemDir = file.getAbsolutePath();
                if (itemDir.equalsIgnoreCase(previousDir)) {
                    requestFocusFromTouch();
                    SetProperPosition(i);
                    break;
                }
            }
        }
    }
    @Override
    public void Refresh() {
        ArrayList<DetailItem> arrayList = new ArrayList<>();
        File[] files = explorerBehaviour.ListContents();
        boolean isEmpty = files == null || files.length <= 0;
        boolean hasParent = explorerBehaviour.HasParent();
        if (!isEmpty || hasParent) {
            if (hasParent)
                arrayList.add(new DetailItem(ContextCompat.getDrawable(getContext(), R.drawable.ic_baseline_folder_24), "..", "<dir>", new File(explorerBehaviour.GetParent())));
            if (ActivityManager.GetCurrent() == ActivityManager.Page.chooser)
                arrayList.add(new DetailItem(null, "Choose current directory", null, null));
            if (explorerBehaviour.GetDirectory().equalsIgnoreCase("/storage/emulated"))
                arrayList.add(new DetailItem(ContextCompat.getDrawable(getContext(), R.drawable.ic_baseline_folder_24), "0", "<dir>", new File("/storage/emulated/0")));
            if (!isEmpty) {
                for (int i = 0; i < files.length; i++) {
                    String absolutePath = files[i].getAbsolutePath();
                    Drawable icon = null;
                    if (!files[i].isDirectory()) {
                        String extension = ExplorerBehaviour.GetExtension(absolutePath);
                        if (extension != null) {
                            String packageName = PackagesCache.GetPackageNameForExtension(extension);
                            if (packageName != null)
                                icon = PackagesCache.GetPackageIcon(packageName);
                        }
                    }
                    else
                        icon = ContextCompat.getDrawable(getContext(), R.drawable.ic_baseline_folder_24);

                    arrayList.add(new DetailItem(icon, files[i].getName(), files[i].isDirectory() ? "<dir>" : null, new File(absolutePath)));
                }
            }
            DetailAdapter customAdapter = new DetailAdapter(getContext(), arrayList);
            setAdapter(customAdapter);
        }
        SetProperPosition(0);
    }

    private void TryRun(DetailItem clickedItem) {
        String absPath = ((File)clickedItem.obj).getAbsolutePath();
        if (absPath.contains(".")) {
            String extension = ExplorerBehaviour.GetExtension(absPath);
            IntentLaunchData fileLaunchIntent = PackagesCache.GetLaunchDataForExtension(extension);

            if (fileLaunchIntent != null) {
                String nameWithExt = ((File)clickedItem.obj).getName();
                String nameWithoutExt = nameWithExt.substring(0, nameWithExt.lastIndexOf("."));

                String[] extrasValues = null;
                IntentPutExtra[] extras = fileLaunchIntent.GetExtras();
                if (extras != null && extras.length > 0) {
                    extrasValues = new String[extras.length];
                    for (int i = 0; i < extras.length; i++) {
                        IntentPutExtra current = extras[i];
                        if (current.GetExtraType() == IntentLaunchData.DataType.AbsolutePath)
                            extrasValues[i] = absPath;
                        else if (current.GetExtraType() == IntentLaunchData.DataType.FileNameWithExt)
                            extrasValues[i] = nameWithExt;
                        else if (current.GetExtraType() == IntentLaunchData.DataType.FileNameWithoutExt)
                            extrasValues[i] = nameWithoutExt;
//                        else if (current.GetExtraType() == IntentLaunchData.DataType.Uri) {
//                            String uri = Uri.parse(absPath).getScheme();
//                            Log.d("Explorer", "Passing " + uri);
//                            extrasValues[i] = uri;
//                        }
                    }
                }

                String data = null;
                if (fileLaunchIntent.GetDataType() == IntentLaunchData.DataType.AbsolutePath)
                    data = absPath;
                else if (fileLaunchIntent.GetDataType() == IntentLaunchData.DataType.FileNameWithExt)
                    data = nameWithExt;
                else if (fileLaunchIntent.GetDataType() == IntentLaunchData.DataType.FileNameWithoutExt)
                    data = nameWithoutExt;
//                else if (fileLaunchIntent.GetDataType() == IntentLaunchData.DataType.Uri) {
//                    String uri = Uri.parse(absPath).getScheme();
//                    Log.d("Explorer", "Passing " + uri);
//                    data = uri;
//                }

                fileLaunchIntent.Launch(data, extrasValues);
            }
            else
                Log.e("Explorer", "No launch intent associated with extension " + extension);
        }
        else
            Log.e("Explorer", "Missing extension, could not identify file");
    }
}