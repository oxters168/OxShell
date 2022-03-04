package com.OxGames.OxShell;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import java.io.File;
import java.util.ArrayList;

public class ExplorerView extends SlideTouchListView implements PermissionsListener {
    private ExplorerBehaviour explorerBehaviour;
    private SlideTouchHandler slideTouch = new SlideTouchHandler();

    public ExplorerView(Context context) {
        super(context);
        HomeActivity.GetInstance().AddPermissionListener(this);
        explorerBehaviour = new ExplorerBehaviour();
        RefreshExplorerList();
    }

    public ExplorerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        HomeActivity.GetInstance().AddPermissionListener(this);
        explorerBehaviour = new ExplorerBehaviour();
        RefreshExplorerList();
    }

    public ExplorerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        HomeActivity.GetInstance().AddPermissionListener(this);
        explorerBehaviour = new ExplorerBehaviour();
        RefreshExplorerList();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        int storedPos = properPosition;
        RefreshExplorerList();
        SetProperPosition(storedPos);

        // Checks the orientation of the screen
//        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
//            Toast.makeText(ActivityManager.GetActivityInstance(ActivityManager.GetCurrent()), "landscape", Toast.LENGTH_SHORT).show();
//        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
//            Toast.makeText(ActivityManager.GetActivityInstance(ActivityManager.GetCurrent()), "portrait", Toast.LENGTH_SHORT).show();
//        }
    }

    @Override
    public void onPermissionResponse(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == ExplorerBehaviour.READ_EXTERNAL_STORAGE) {
            // If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission is granted. Continue the action or workflow
                // in your app.
                Log.d("Explorer", "Storage permission granted");
                RefreshExplorerList();
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
        Log.d("ExplorerView", key_code + " " + key_event);
        if (key_code == KeyEvent.KEYCODE_BUTTON_START || key_code == KeyEvent.KEYCODE_BACK) {
//            ActivityManager.GoTo(ActivityManager.Page.home);
            HomeActivity.GetInstance().GoTo(HomeActivity.Page.home);
            return false;
        }
        if (key_code == KeyEvent.KEYCODE_BUTTON_B) {
            GoUp();
            return false;
        }

        return super.onKeyDown(key_code, key_event);
    }

    @Override
    public void MakeSelection() {
        DetailItem clickedItem = (DetailItem)getItemAtPosition(properPosition);
        File file = (File)clickedItem.obj;
        if (file.isDirectory()) {
            explorerBehaviour.SetDirectory(file.getAbsolutePath());
            RefreshExplorerList();
//            SetProperPosition(0);
            TryHighlightPrevDir();
        }
        else {
            TryRun(clickedItem);
        }
    }

    public void GoUp() {
        explorerBehaviour.GoUp();
        RefreshExplorerList();
//        SetProperPosition(0);
        TryHighlightPrevDir();
    }
    private void TryHighlightPrevDir() {
        String previousDir = explorerBehaviour.GetLastItemInHistory();
        for (int i = 0; i < getCount(); i++) {
            String itemDir = ((File)((DetailItem)getItemAtPosition(i)).obj).getAbsolutePath();
            if (itemDir.equalsIgnoreCase(previousDir)) {
                requestFocusFromTouch();
                SetProperPosition(i);
                break;
            }
        }
    }

    private void RefreshExplorerList() {
        ArrayList<DetailItem> arrayList = new ArrayList<>();
        File[] files = explorerBehaviour.ListContents();
        boolean isEmpty = files == null || files.length <= 0;
        boolean hasParent = explorerBehaviour.HasParent();
        if (!isEmpty || hasParent) {
            if (hasParent)
                arrayList.add(new DetailItem(ContextCompat.getDrawable(getContext(), R.drawable.ic_baseline_folder_24), "..", "<dir>", new File(explorerBehaviour.GetParent())));
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