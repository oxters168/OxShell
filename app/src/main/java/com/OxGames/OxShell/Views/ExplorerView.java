package com.OxGames.OxShell.Views;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;

import androidx.core.content.ContextCompat;

import com.OxGames.OxShell.Data.DynamicInputRow;
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
        if (!currentActivity.getSettingsDrawer().isDrawerOpen() && !currentActivity.getDynamicInput().isOverlayShown()) {
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
                    File file = (File)selectedItem.obj;
                    boolean isValidSelection = file != null && !selectedItem.leftAlignedText.equals("..");
                    SettingsDrawer.ContextBtn newFolderBtn = new SettingsDrawer.ContextBtn("New Folder", () ->
                    {
                        DynamicInputRow.TextInput folderNameTxtInput = new DynamicInputRow.TextInput("Folder Name");
                        DynamicInputRow.Label errorLabel = new DynamicInputRow.Label("");
                        currentActivity.getDynamicInput().setTitle("Create Folder");
                        currentActivity.getDynamicInput().setItems
                        (
                            new DynamicInputRow(errorLabel),
                            new DynamicInputRow(folderNameTxtInput),
                            new DynamicInputRow
                            (
                                new DynamicInputRow.ButtonInput("Ok", v ->
                                {
                                    //Log.d("ExplorerDynamicView", "Clicked ok, folder name is " + folderName.getText());
                                    String folderName = folderNameTxtInput.getText();
                                    if (folderName != null && folderName.length() > 0) {
                                        String newPath = AndroidHelpers.combinePaths(explorerBehaviour.getDirectory(), folderName);
                                        boolean success = new File(newPath).mkdir();
                                        Log.d("ExplorerView", "Creating " + newPath + " success: " + success);
                                        refresh();
                                        currentActivity.getDynamicInput().setShown(false);
                                    } else
                                        errorLabel.setLabel("Folder name is invalid");
                                }),
                                new DynamicInputRow.ButtonInput("Cancel", v ->
                                {
                                    //Log.d("ExplorerDynamicView", "Clicked cancel");
                                    currentActivity.getDynamicInput().setShown(false);
                                })
                            )
                        );
                        currentActivity.getSettingsDrawer().setShown(false);
                        currentActivity.getDynamicInput().setShown(true);
                        return null;
                    });
                    SettingsDrawer.ContextBtn newFileBtn = new SettingsDrawer.ContextBtn("New File", () ->
                    {
                        DynamicInputRow.TextInput fileNameTxtInput = new DynamicInputRow.TextInput("File Name");
                        DynamicInputRow.Label errorLabel = new DynamicInputRow.Label("");
                        currentActivity.getDynamicInput().setTitle("Create File");
                        currentActivity.getDynamicInput().setItems
                        (
                            new DynamicInputRow(errorLabel),
                            new DynamicInputRow(fileNameTxtInput),
                            new DynamicInputRow
                            (
                                new DynamicInputRow.ButtonInput("Ok", v ->
                                {
                                    //Log.d("ExplorerDynamicView", "Clicked ok, folder name is " + folderName.getText());
                                    String fileName = fileNameTxtInput.getText();
                                    if (fileName != null && fileName.length() > 0) {
                                        String newPath = AndroidHelpers.combinePaths(explorerBehaviour.getDirectory(), fileName);
                                        if (!AndroidHelpers.fileExists(newPath)) {
                                            try {
                                                boolean success = new File(newPath).createNewFile();
                                                Log.d("ExplorerView", "Creating " + newPath + " success: " + success);
                                            } catch (Exception e) {
                                                Log.e("ExplorerView", e.toString());
                                            }
                                            refresh();
                                            currentActivity.getDynamicInput().setShown(false);
                                        } else
                                            errorLabel.setLabel("File already exists");
                                    } else
                                        errorLabel.setLabel("File name is invalid");
                                }),
                                new DynamicInputRow.ButtonInput("Cancel", v ->
                                {
                                    //Log.d("ExplorerDynamicView", "Clicked cancel");
                                    currentActivity.getDynamicInput().setShown(false);
                                })
                            )
                        );
                        currentActivity.getSettingsDrawer().setShown(false);
                        currentActivity.getDynamicInput().setShown(true);
                        return null;
                    });
                    SettingsDrawer.ContextBtn renameBtn = new SettingsDrawer.ContextBtn("Rename", () ->
                    {
                        boolean isDir = file.isDirectory();
                        DynamicInputRow.TextInput renamedTxtInput = new DynamicInputRow.TextInput(isDir ? "Folder Name" : "File Name");
                        renamedTxtInput.setText(file.getName(), false);
                        DynamicInputRow.Label errorLabel = new DynamicInputRow.Label("");
                        currentActivity.getDynamicInput().setTitle("Rename " + (isDir ? "Folder" : "File"));
                        currentActivity.getDynamicInput().setItems
                        (
                            new DynamicInputRow(errorLabel),
                            new DynamicInputRow(renamedTxtInput),
                            new DynamicInputRow
                            (
                                new DynamicInputRow.ButtonInput("Ok", v ->
                                {
                                    //Log.d("ExplorerDynamicView", "Clicked ok, folder name is " + folderName.getText());
                                    String newName = renamedTxtInput.getText();
                                    if (newName != null && newName.length() > 0) {
                                        file.renameTo(new File(AndroidHelpers.combinePaths(file.getParent(), newName)));
                                        refresh();
                                        currentActivity.getDynamicInput().setShown(false);
                                    } else
                                        errorLabel.setLabel("Name is invalid");
                                }),
                                new DynamicInputRow.ButtonInput("Cancel", v ->
                                {
                                    //Log.d("ExplorerDynamicView", "Clicked cancel");
                                    currentActivity.getDynamicInput().setShown(false);
                                })
                            )
                        );
                        currentActivity.getSettingsDrawer().setShown(false);
                        currentActivity.getDynamicInput().setShown(true);
                        return null;
                    });
                    SettingsDrawer.ContextBtn copyBtn = new SettingsDrawer.ContextBtn("Copy", () ->
                    {
                        currentActivity.getDynamicInput().setTitle("Copy");
//                        currentActivity.getDynamicInput().setItems
//                        (
//                                new DynamicInputRow
//                                        (
//                                                new DynamicInputRow.Label("Ok"),
//                                                new DynamicInputRow.Label("Cancel")
//                                        ),
//                                new DynamicInputRow
//                                        (
//                                                new DynamicInputRow.Label("Ok"),
//                                                new DynamicInputRow.Label("Cancel")
//                                        ),
//                                new DynamicInputRow
//                                        (
//                                                new DynamicInputRow.Label("Ok"),
//                                                new DynamicInputRow.Label("Cancel")
//                                        ),
//                                new DynamicInputRow
//                                        (
//                                                new DynamicInputRow.Label("Ok"),
//                                                new DynamicInputRow.Label("Cancel")
//                                        ),
//                                new DynamicInputRow
//                                        (
//                                                new DynamicInputRow.Label("Ok"),
//                                                new DynamicInputRow.Label("Cancel")
//                                        ),
//                                new DynamicInputRow
//                                        (
//                                                new DynamicInputRow.Label("Ok"),
//                                                new DynamicInputRow.Label("Cancel")
//                                        ),
//                                new DynamicInputRow
//                                        (
//                                                new DynamicInputRow.Label("Ok"),
//                                                new DynamicInputRow.Label("Cancel")
//                                        ),
//                                new DynamicInputRow
//                                        (
//                                                new DynamicInputRow.Label("Ok"),
//                                                new DynamicInputRow.Label("Cancel")
//                                        ),
//                                new DynamicInputRow
//                                        (
//                                                new DynamicInputRow.Label("Ok"),
//                                                new DynamicInputRow.Label("Cancel")
//                                        ),
//                                new DynamicInputRow
//                                        (
//                                                new DynamicInputRow.Label("Ok"),
//                                                new DynamicInputRow.Label("Cancel")
//                                        ),
//                                new DynamicInputRow
//                                        (
//                                                new DynamicInputRow.Label("Ok"),
//                                                new DynamicInputRow.Label("Cancel")
//                                        ),
//                                new DynamicInputRow
//                                        (
//                                                new DynamicInputRow.Label("Ok"),
//                                                new DynamicInputRow.Label("Cancel")
//                                        ),
//                            new DynamicInputRow
//                            (
//                                new DynamicInputRow.TextInput("Thing"),
//                                new DynamicInputRow.TextInput("Majing"),
//                                new DynamicInputRow.Label("Hey"),
//                                new DynamicInputRow.ButtonInput("Not ok", null),
//                                new DynamicInputRow.Label("Hey hey"),
//                                new DynamicInputRow.TextInput("Majing"),
//                                new DynamicInputRow.ButtonInput("Maybe ok", null)
//                            ),
//                            new DynamicInputRow
//                            (
//                                new DynamicInputRow.ButtonInput("Ok", null),
//                                new DynamicInputRow.ButtonInput("Cancel", null)
//                            ),
//                            new DynamicInputRow
//                            (
//                                new DynamicInputRow.TextInput("Thing"),
//                                new DynamicInputRow.TextInput("Majing"),
//                                new DynamicInputRow.Label("Hey"),
//                                new DynamicInputRow.ButtonInput("Not ok", null),
//                                new DynamicInputRow.ButtonInput("Very ok", null),
//                                new DynamicInputRow.TextInput("Ding"),
//                                new DynamicInputRow.Label("Ha"),
//                                new DynamicInputRow.Label("Haha"),
//                                new DynamicInputRow.Label("Hahaha"),
//                                new DynamicInputRow.Label("Hahahaha"),
//                                new DynamicInputRow.Label("Hahahahaha"),
//                                new DynamicInputRow.Label("Hahahahahaha"),
//                                new DynamicInputRow.Label("Hahahahahahaha"),
//                                new DynamicInputRow.ButtonInput("Very bad", null)
//                            ),
//                            new DynamicInputRow
//                            (
//                                new DynamicInputRow.Label("Ok"),
//                                new DynamicInputRow.Label("Cancel")
//                            ),
//                            new DynamicInputRow
//                            (
//                                new DynamicInputRow.Label("Ok"),
//                                new DynamicInputRow.Label("Cancel")
//                            ),
//                            new DynamicInputRow
//                            (
//                                new DynamicInputRow.Label("Ok"),
//                                new DynamicInputRow.Label("Cancel")
//                            ),
//                            new DynamicInputRow
//                            (
//                                new DynamicInputRow.Label("Ok"),
//                                new DynamicInputRow.Label("Cancel")
//                            ),
//                            new DynamicInputRow
//                            (
//                                new DynamicInputRow.Label("Ok"),
//                                new DynamicInputRow.ButtonInput("Cancel", null)
//                            ),
//                            new DynamicInputRow
//                            (
//                                new DynamicInputRow.Label("Ok"),
//                                new DynamicInputRow.Label("Cancel")
//                            ),
//                            new DynamicInputRow
//                            (
//                                new DynamicInputRow.Label("Ok"),
//                                new DynamicInputRow.Label("Cancel")
//                            ),
//                            new DynamicInputRow
//                            (
//                                new DynamicInputRow.Label("Ok"),
//                                new DynamicInputRow.Label("Cancel")
//                            ),
//                            new DynamicInputRow
//                            (
//                                new DynamicInputRow.Label("Ok"),
//                                new DynamicInputRow.Label("Cancel")
//                            ),
//                            new DynamicInputRow
//                            (
//                                new DynamicInputRow.Label("Ok"),
//                                new DynamicInputRow.Label("Cancel")
//                            ),
//                            new DynamicInputRow
//                            (
//                                new DynamicInputRow.Label("Ok"),
//                                new DynamicInputRow.Label("Cancel")
//                            ),
//                            new DynamicInputRow
//                            (
//                                new DynamicInputRow.Label("Ok"),
//                                new DynamicInputRow.Label("Cancel")
//                            )
//                        );
                        explorerBehaviour.copy(file.getAbsolutePath());
                        currentActivity.getSettingsDrawer().setShown(false);
                        //currentActivity.getDynamicInput().setShown(true);
                        return null;
                    });
                    SettingsDrawer.ContextBtn cutBtn = new SettingsDrawer.ContextBtn("Cut", () ->
                    {
                        explorerBehaviour.cut(file.getAbsolutePath());
                        currentActivity.getSettingsDrawer().setShown(false);
                        return null;
                    });
                    SettingsDrawer.ContextBtn pasteBtn = new SettingsDrawer.ContextBtn("Paste", () ->
                    {
                        explorerBehaviour.paste();
                        refresh();
                        currentActivity.getSettingsDrawer().setShown(false);
                        return null;
                    });
                    SettingsDrawer.ContextBtn deleteBtn = new SettingsDrawer.ContextBtn("Delete", () ->
                    {
                        explorerBehaviour.delete(file.getAbsolutePath());
                        refresh();
                        currentActivity.getSettingsDrawer().setShown(false);
                        return null;
                    });
                    if (isValidSelection) {
                        if (explorerBehaviour.isCopying() || explorerBehaviour.isCutting())
                            currentActivity.getSettingsDrawer().setButtons(newFolderBtn, newFileBtn, renameBtn, copyBtn, cutBtn, pasteBtn, deleteBtn);
                        else
                            currentActivity.getSettingsDrawer().setButtons(newFolderBtn, newFileBtn, renameBtn, copyBtn, cutBtn, deleteBtn);
                    } else
                        if (explorerBehaviour.isCopying() || explorerBehaviour.isCutting())
                            currentActivity.getSettingsDrawer().setButtons(newFolderBtn, newFileBtn, pasteBtn);
                        else
                            currentActivity.getSettingsDrawer().setButtons(newFolderBtn, newFileBtn);

                    currentActivity.getSettingsDrawer().setShown(true);
                    return true;
                }
            }

            return super.receiveKeyEvent(key_event);
        }
//        else if (currentActivity.getSettingsDrawer().isDrawerOpen()) {
//            if (key_event.getAction() == KeyEvent.ACTION_DOWN) {
//                if ((key_event.getKeyCode() == KeyEvent.KEYCODE_BUTTON_B || key_event.getKeyCode() == KeyEvent.KEYCODE_BACK)) {
//                    currentActivity.getSettingsDrawer().setShown(false);
//                    return true;
//                }
//            }
//        }
//        else if (currentActivity.getDynamicInput().isOverlayShown()) {
//            if (key_event.getAction() == KeyEvent.ACTION_DOWN) {
//                if (key_event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
//                    currentActivity.getDynamicInput().setShown(false);
//                    return true;
//                }
//            }
//        }
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