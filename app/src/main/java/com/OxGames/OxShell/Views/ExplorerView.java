package com.OxGames.OxShell.Views;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;

import androidx.core.content.ContextCompat;

import com.OxGames.OxShell.Data.DynamicInputItem;
import com.OxGames.OxShell.Data.IntentLaunchData;
import com.OxGames.OxShell.Data.IntentPutExtra;
import com.OxGames.OxShell.Helpers.ActivityManager;
import com.OxGames.OxShell.Helpers.AndroidHelpers;
import com.OxGames.OxShell.Adapters.DetailAdapter;
import com.OxGames.OxShell.Data.DetailItem;
import com.OxGames.OxShell.Helpers.ExplorerBehaviour;
import com.OxGames.OxShell.FileChooserActivity;
import com.OxGames.OxShell.Data.PackagesCache;
import com.OxGames.OxShell.Interfaces.PermissionsListener;
import com.OxGames.OxShell.PagedActivity;
import com.OxGames.OxShell.R;
import com.OxGames.OxShell.Data.ShortcutsCache;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ExplorerView extends SlideTouchListView implements PermissionsListener {
    private ExplorerBehaviour explorerBehaviour;
    //private SlideTouchHandler slideTouch = new SlideTouchHandler();
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
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        PagedActivity currentActivity = ActivityManager.getCurrentActivity();
        if (!currentActivity.getSettingsDrawer().isDrawerOpen())
            return super.onInterceptTouchEvent(ev);
        else
            return false;
    }
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        PagedActivity currentActivity = ActivityManager.getCurrentActivity();
        if (!currentActivity.getSettingsDrawer().isDrawerOpen())
            return super.onTouchEvent(ev);
        else
            return false;
    }

    @Override
    public void onPermissionResponse(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == AndroidHelpers.READ_EXTERNAL_STORAGE) {
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
        PagedActivity currentActivity = ActivityManager.getCurrentActivity();
        //Log.d("ExplorerView", key_event.toString());
        // TODO: make context menu that allows for copying/cutting/pasting/deleting and creating launch intent for file type
        if (!currentActivity.getSettingsDrawer().isDrawerOpen() && !currentActivity.getDynamicInput().isInputShown()) {
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
                if (key_event.getKeyCode() == KeyEvent.KEYCODE_BUTTON_X) {
                    DetailItem selectedItem = (DetailItem)getItemAtPosition(properPosition);
                    File file = (File) selectedItem.obj;
                    boolean isValidSelection = file != null && !selectedItem.leftAlignedText.equals("..");
                    SettingsDrawer.ContextBtn newFolderBtn = new SettingsDrawer.ContextBtn("New Folder", () ->
                    {
                        currentActivity.getDynamicInput().setTitle("Create Folder");
                        currentActivity.getDynamicInput().setItems(new DynamicInputItem(new DynamicInputItem.TextInput("Folder Name", new TextWatcher() {
                            @Override
                            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                                Log.d("ExplorerView", "beforeTextChanged " + s);
                            }

                            @Override
                            public void onTextChanged(CharSequence s, int start, int before, int count) {
                                Log.d("ExplorerView", "onTextChanged " + s);
                            }

                            @Override
                            public void afterTextChanged(Editable s) {
                                Log.d("ExplorerView", "afterTextChanged " + s);
                            }
                        })),
                        new DynamicInputItem(new DynamicInputItem.TextInput("abc", null)),
                        new DynamicInputItem(new DynamicInputItem.TextInput("def", null)),
                        new DynamicInputItem(new DynamicInputItem.TextInput("ghi", null)),
                        new DynamicInputItem(new DynamicInputItem.TextInput("jkl", null)),
                        new DynamicInputItem(new DynamicInputItem.TextInput("mno", null)),
                        new DynamicInputItem(new DynamicInputItem.TextInput("pqr", null)),
                        new DynamicInputItem(new DynamicInputItem.TextInput("stu", null)),
                        new DynamicInputItem(new DynamicInputItem.TextInput("vwx", null)),
                        new DynamicInputItem(new DynamicInputItem.TextInput("yzz", null)),
                        new DynamicInputItem(new DynamicInputItem.TextInput("123", null)),
                        new DynamicInputItem(new DynamicInputItem.TextInput("456", null)),
                        new DynamicInputItem(new DynamicInputItem.TextInput("789", null)),
                        new DynamicInputItem(new DynamicInputItem.TextInput("0-=", null)),
                        new DynamicInputItem(new DynamicInputItem.TextInput("!@#", null)),
                        new DynamicInputItem(new DynamicInputItem.TextInput("$%^", null)),
                        new DynamicInputItem(new DynamicInputItem.TextInput("&*(", null)),
                        new DynamicInputItem(new DynamicInputItem.TextInput(")_+", null)),
                        new DynamicInputItem(new DynamicInputItem.TextInput(",./", null)),
                        new DynamicInputItem(new DynamicInputItem.TextInput("<>?", null)));
                        currentActivity.getDynamicInput().setShown(true);
                        String newPath = AndroidHelpers.combinePaths(explorerBehaviour.getDirectory(), "New Folder");
                        boolean success = new File(newPath).mkdir();
                        Log.d("ExplorerView", "Creating " + newPath + " success: " + success);
                        refresh();
                        currentActivity.getSettingsDrawer().setShown(false);
                        return null;
                    });
                    SettingsDrawer.ContextBtn copyBtn = new SettingsDrawer.ContextBtn("Copy", () ->
                    {
                        currentActivity.getDynamicInput().setTitle("Copy");
                        currentActivity.getDynamicInput().setShown(true);
                        currentActivity.getSettingsDrawer().setShown(false);
                        return null;
                    });
                    SettingsDrawer.ContextBtn cutBtn = new SettingsDrawer.ContextBtn("Cut", () ->
                    {
                        currentActivity.getSettingsDrawer().setShown(false);
                        return null;
                    });
                    SettingsDrawer.ContextBtn pasteBtn = new SettingsDrawer.ContextBtn("Paste", () ->
                    {
                        currentActivity.getSettingsDrawer().setShown(false);
                        return null;
                    });
                    SettingsDrawer.ContextBtn deleteBtn = new SettingsDrawer.ContextBtn("Delete", () ->
                    {
                        boolean success = file.delete();
                        Log.d("ExplorerView", "Deleting " + file.getAbsolutePath() + " success: " + success);
                        refresh();
                        currentActivity.getSettingsDrawer().setShown(false);
                        return null;
                    });
                    if (isValidSelection) {
                        currentActivity.getSettingsDrawer().setButtons(newFolderBtn, copyBtn, cutBtn, pasteBtn, deleteBtn);
                    } else
                        currentActivity.getSettingsDrawer().setButtons(newFolderBtn, pasteBtn);

                    currentActivity.getSettingsDrawer().setShown(true);
                    return true;
                }
            }

            return super.receiveKeyEvent(key_event);
        } else if (currentActivity.getSettingsDrawer().isDrawerOpen()) {
            if (key_event.getAction() == KeyEvent.ACTION_DOWN) {
                if ((key_event.getKeyCode() == KeyEvent.KEYCODE_BUTTON_B || key_event.getKeyCode() == KeyEvent.KEYCODE_BACK)) {
                    currentActivity.getSettingsDrawer().setShown(false);
                    return true;
                }
            }
        } else if (currentActivity.getDynamicInput().isInputShown()) {
            if (key_event.getAction() == KeyEvent.ACTION_DOWN) {
                if (key_event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
                    currentActivity.getDynamicInput().setShown(false);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void makeSelection() {
        DetailItem clickedItem = (DetailItem)getItemAtPosition(properPosition);
        if (clickedItem.obj == null) {
            if (ActivityManager.getCurrent() == ActivityManager.Page.chooser)
                ((FileChooserActivity)ActivityManager.getInstance(FileChooserActivity.class)).sendResult(explorerBehaviour.getDirectory());
            else
                Log.e("ExplorerView", "Chosen item is null");
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
                    tryRun(((File)clickedItem.obj));
            }
        }
    }
    public static void tryRun(File file) {
        String absPath = file.getAbsolutePath();
        if (!AndroidHelpers.isDirectory(absPath)) {
            String extension = AndroidHelpers.getExtension(absPath);
            if (extension != null) {
                List<IntentLaunchData> fileLaunchIntents = ShortcutsCache.getLaunchDatasForExtension(extension);
                if (fileLaunchIntents.size() > 0) {
                    // TODO: show options if more than one launch intent found and none are set as default
                    IntentLaunchData fileLaunchIntent = fileLaunchIntents.get(0);
                    if (PackagesCache.isPackageInstalled(fileLaunchIntent.getPackageName())) {
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
                    } else
                        Log.e("Explorer", "Failed to launch, " + fileLaunchIntent.getPackageName() + " is not installed on the device");
                } else if (extension.equalsIgnoreCase("apk") || extension.equalsIgnoreCase("xapk")) {
                    AndroidHelpers.install(absPath);
                } else
                    Log.e("Explorer", "No launch intent associated with extension " + extension);
            } else
                Log.e("Explorer", "Missing extension, could not identify file");
        } else
            Log.e("Explorer", "Cannot run a directory");
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
                        String extension = AndroidHelpers.getExtension(absolutePath);
                        if (extension != null) {
                            String packageName = ShortcutsCache.getPackageNameForExtension(extension);
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
}