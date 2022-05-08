package com.OxGames.OxShell;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
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
        ActivityManager.getCurrentActivity().addPermissionListener(this);
        explorerBehaviour = new ExplorerBehaviour();
        refresh();
    }
    public ExplorerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        ActivityManager.getCurrentActivity().addPermissionListener(this);
        explorerBehaviour = new ExplorerBehaviour();
        refresh();
    }
    public ExplorerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        ActivityManager.getCurrentActivity().addPermissionListener(this);
        explorerBehaviour = new ExplorerBehaviour();
        refresh();
    }

    @Override
    public void onPermissionResponse(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == ExplorerBehaviour.READ_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("Explorer", "Storage permission granted");
                refresh();
            }  else {
                Log.e("Explorer", "Storage permission denied");
            }
        }
    }

    @Override
    public boolean receiveKeyEvent(KeyEvent key_event) {
        Log.d("ExplorerView", key_event.toString());
        if (key_event.getAction() == KeyEvent.ACTION_DOWN) {
            if (key_event.getKeyCode() == KeyEvent.KEYCODE_BUTTON_Y || key_event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
                if (ActivityManager.getCurrent() != ActivityManager.Page.chooser) {
                    ActivityManager.goTo(ActivityManager.Page.home);
                    return true;
                }
            }
            if (key_event.getKeyCode() == KeyEvent.KEYCODE_BUTTON_B) {
                goUp();
                return true;
            }
        }

        return super.receiveKeyEvent(key_event);
    }

    @Override
    public void makeSelection() {
        DetailItem clickedItem = (DetailItem)getItemAtPosition(properPosition);
        if (clickedItem.obj == null) {
            ((FileChooserActivity)ActivityManager.getInstance(FileChooserActivity.class)).sendResult(explorerBehaviour.getDirectory());
        } else {
            File file = (File)clickedItem.obj;
            if (file.isDirectory()) {
//            ((ExplorerActivity)ExplorerActivity.GetInstance()).SendResult(file.getAbsolutePath());
//            startActivityForResult(intent, requestCode);
                explorerBehaviour.setDirectory(file.getAbsolutePath());
                refresh();
                tryHighlightPrevDir();
            } else {
                if (ActivityManager.getCurrent() == ActivityManager.Page.chooser)
                    ((FileChooserActivity)ActivityManager.getInstance(FileChooserActivity.class)).sendResult(file.getAbsolutePath());
                else
                    tryRun(clickedItem);
            }
        }
    }

    public void goUp() {
        explorerBehaviour.goUp();
        refresh();
//        SetProperPosition(0);
        tryHighlightPrevDir();
    }
    private void tryHighlightPrevDir() {
        String previousDir = explorerBehaviour.getLastItemInHistory();
        for (int i = 0; i < getCount(); i++) {
            File file = ((File)((DetailItem)getItemAtPosition(i)).obj);
            if (file != null) {
                String itemDir = file.getAbsolutePath();
                if (itemDir.equalsIgnoreCase(previousDir)) {
                    requestFocusFromTouch();
                    setProperPosition(i);
                    break;
                }
            }
        }
    }
    @Override
    public void refresh() {
        ArrayList<DetailItem> arrayList = new ArrayList<>();
        File[] files = explorerBehaviour.listContents();
        boolean isEmpty = files == null || files.length <= 0;
        boolean hasParent = explorerBehaviour.hasParent();
        if (!isEmpty || hasParent) {
            if (hasParent)
                arrayList.add(new DetailItem(ContextCompat.getDrawable(getContext(), R.drawable.ic_baseline_folder_24), "..", "<dir>", new File(explorerBehaviour.getParent())));
            if (ActivityManager.getCurrent() == ActivityManager.Page.chooser)
                arrayList.add(new DetailItem(null, "Choose current directory", null, null));
            if (explorerBehaviour.getDirectory().equalsIgnoreCase("/storage/emulated"))
                arrayList.add(new DetailItem(ContextCompat.getDrawable(getContext(), R.drawable.ic_baseline_folder_24), "0", "<dir>", new File("/storage/emulated/0")));
            if (!isEmpty) {
                for (int i = 0; i < files.length; i++) {
                    String absolutePath = files[i].getAbsolutePath();
                    Drawable icon = null;
                    if (!files[i].isDirectory()) {
                        String extension = ExplorerBehaviour.getExtension(absolutePath);
                        if (extension != null) {
                            String packageName = PackagesCache.getPackageNameForExtension(extension);
                            if (packageName != null)
                                icon = PackagesCache.getPackageIcon(packageName);
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
        super.refresh();
    }

    private void tryRun(DetailItem clickedItem) {
        String absPath = ((File)clickedItem.obj).getAbsolutePath();
        if (ExplorerBehaviour.hasExtension(absPath)) {
            String extension = ExplorerBehaviour.getExtension(absPath);
            IntentLaunchData fileLaunchIntent = PackagesCache.getLaunchDataForExtension(extension);

            if (fileLaunchIntent != null) {
                String nameWithExt = ((File)clickedItem.obj).getName();
                String nameWithoutExt = ExplorerBehaviour.removeExtension(nameWithExt);

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
            }
            else
                Log.e("Explorer", "No launch intent associated with extension " + extension);
        }
        else
            Log.e("Explorer", "Missing extension, could not identify file");
    }
}