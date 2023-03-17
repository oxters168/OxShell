package com.OxGames.OxShell.Views;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.text.InputType;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;

import androidx.core.content.ContextCompat;

import com.OxGames.OxShell.Data.DynamicInputRow;
import com.OxGames.OxShell.Data.IntentLaunchData;
import com.OxGames.OxShell.ExplorerActivity;
import com.OxGames.OxShell.Helpers.ActivityManager;
import com.OxGames.OxShell.Helpers.AndroidHelpers;
import com.OxGames.OxShell.Adapters.DetailAdapter;
import com.OxGames.OxShell.Data.DetailItem;
import com.OxGames.OxShell.Helpers.ExplorerBehaviour;
import com.OxGames.OxShell.FileChooserActivity;
import com.OxGames.OxShell.Data.PackagesCache;
import com.OxGames.OxShell.PagedActivity;
import com.OxGames.OxShell.R;
import com.OxGames.OxShell.Data.ShortcutsCache;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ExplorerView extends SlideTouchListView {//implements PermissionsListener {
    private ExplorerBehaviour explorerBehaviour;

    public ExplorerView(Context context) {
        super(context);
        init();
    }
    public ExplorerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    public ExplorerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        //Log.d("ExplorerView", "Creating");
        //SettingsKeeper.hasValue()
        //setMargins();
        //ActivityManager.getCurrentActivity().addPermissionListener(this);
        if (AndroidHelpers.hasReadStoragePermission()) {
            explorerBehaviour = new ExplorerBehaviour();
            refresh();
        } else
            AndroidHelpers.requestReadStoragePermission(granted -> {
                if (!granted) {
                    Log.e("ExplorerView", "Failed to get permissions, exiting...");
                    PagedActivity currentActivity = ActivityManager.getCurrentActivity();
                    Intent returnIntent = new Intent();
                    currentActivity.setResult(Activity.RESULT_CANCELED, returnIntent);
                    currentActivity.finish();
                } else {
                    explorerBehaviour = new ExplorerBehaviour();
                    refresh();
                }
            });
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        PagedActivity currentActivity = ActivityManager.getCurrentActivity();
        if (!currentActivity.isInAContextMenu())
            return super.onInterceptTouchEvent(ev);
        else
            return false;
    }
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        PagedActivity currentActivity = ActivityManager.getCurrentActivity();
        if (!currentActivity.isInAContextMenu())
            return super.onTouchEvent(ev);
        else
            return false;
    }

//    @Override
//    public void onPermissionResponse(int requestCode, String[] permissions, int[] grantResults) {
//        if (requestCode == AndroidHelpers.READ_EXTERNAL_STORAGE) {
//            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                Log.i("Explorer", "Storage permission granted");
//                refresh();
//            }  else {
//                Log.e("Explorer", "Storage permission denied");
//            }
//        }
//    }

    @Override
    public boolean receiveKeyEvent(KeyEvent key_event) {
        PagedActivity currentActivity = ActivityManager.getCurrentActivity();
        //Log.d("ExplorerView", key_event.toString());
        // TODO: add option for creating launch intent for file type
        // TODO: add option for select all
        // TODO: add launch with option that lets you pick from a list of assocs that have the proper extension
        if (!currentActivity.isInAContextMenu()) {
            if (key_event.getAction() == KeyEvent.ACTION_DOWN) {
                // within action down since we want the repeat when held
                if (key_event.getKeyCode() == KeyEvent.KEYCODE_BUTTON_X) {
                    DetailItem currentItem = (DetailItem)getItemAtPosition(properPosition);
                    if (currentItem.obj != null && !currentItem.leftAlignedText.equals(".."))
                        setItemSelected(properPosition, !isItemSelected(properPosition));
                    selectNextItem();
                    return true;
                }
            }
            if (key_event.getAction() == KeyEvent.ACTION_UP) {
                if (key_event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
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
        return false;
    }

    public static int getDigitCount(int value) {
        int count = 1;
        while (Math.abs(value) >= 10) {
            value /= 10;
            count++;
        }
        return count;
    }

    @Override
    public void primaryAction() {
        PagedActivity currentActivity = ActivityManager.getCurrentActivity();
        if (!(currentActivity instanceof ExplorerActivity || currentActivity instanceof FileChooserActivity))
            return;

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

    @Override
    public void secondaryAction() {
        PagedActivity currentActivity = ActivityManager.getCurrentActivity();
        if (!(currentActivity instanceof ExplorerActivity || currentActivity instanceof FileChooserActivity))
            return;

        super.secondaryAction();
        showSettingsDrawer();
    }

    private void setupBtns() {
        DetailItem currentItem = (DetailItem)getItemAtPosition(properPosition);
        boolean isCurrentValid = currentItem.obj != null && !currentItem.leftAlignedText.equals("..");
        List<DetailItem> selection = getSelectedItems();
        boolean isValidSelection = true;
        if (selection.size() == 1)
            isValidSelection = selection.get(0).obj != null && !selection.get(0).leftAlignedText.equals("..");

        List<SettingsDrawer.ContextBtn> btns = new ArrayList<>();
        btns.add(newFolderBtn);
        btns.add(newFileBtn);
        if (getCount() > 1)
            btns.add(selectAllBtn);
        if (selection.size() > 1)
            btns.add(deselectBtn);
        if (selection.size() > 1 && selection.size() < getCount() - 1)
            btns.add(invertSelectionBtn);
        if (isCurrentValid)
            btns.add(toggleSelection);
        if (isValidSelection)
            btns.add(renameBtn);
        if (isValidSelection)
            btns.add(copyBtn);
        if (isValidSelection)
            btns.add(cutBtn);
        if (explorerBehaviour.isCopying() || explorerBehaviour.isCutting())
            btns.add(pasteBtn);
        if (isValidSelection)
            btns.add(deleteBtn);
        btns.add(cancelBtn);

        ActivityManager.getCurrentActivity().getSettingsDrawer().setButtons(btns.toArray(new SettingsDrawer.ContextBtn[0]));
    }
    private void showSettingsDrawer() {
        setupBtns();
        ActivityManager.getCurrentActivity().getSettingsDrawer().setShown(true);
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
                    if (PackagesCache.isPackageInstalled(fileLaunchIntent.getPackageName()))
                        fileLaunchIntent.launch(absPath);
                    else
                        Log.e("Explorer", "Failed to launch, " + fileLaunchIntent.getPackageName() + " is not installed on the device");
                } else if (extension.equalsIgnoreCase("apk") || extension.equalsIgnoreCase("xapk")) {
                    String pkgName = AndroidHelpers.getPkgNameFromApk(absPath);
                    if (pkgName != null) {
                        PromptView prompt = ActivityManager.getCurrentActivity().getPrompt();
                        prompt.setMessage("Attempting to install " + pkgName);
                        prompt.setStartBtn("Continue", () -> {
                            prompt.setShown(false);
                            if (!AndroidHelpers.hasInstallPermission()) {
                                AndroidHelpers.requestInstallPermission(granted -> {
                                    if (granted)
                                        AndroidHelpers.install(absPath);
                                });
                            } else
                                AndroidHelpers.install(absPath);
                        });
                        prompt.setEndBtn("Cancel", () -> {
                            prompt.setShown(false);
                        });
                        prompt.setCenterOfScreen();
                        prompt.setShown(true);
                    }
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
        tryHighlightItem(explorerBehaviour.getPrevDirectory());
    }
    private void tryHighlightItem(String path) {
        for (int i = 0; i < getCount(); i++) {
            File file = ((File)((DetailItem)getItemAtPosition(i)).obj);
            if (file != null) {
                String itemDir = file.getAbsolutePath();
                if (itemDir.equalsIgnoreCase(path)) {
                    requestFocusFromTouch();
                    setProperPosition(i);
                    break;
                }
            }
        }
    }
    @Override
    public void refresh() {
        //Log.d("ExplorerView", "Refreshing");
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
                // sort the contents alphabetically
                List<File> sortedFiles = new ArrayList();
                for (int i = 0; i < files.length; i++) {
                    File sortee = files[i];
                    int insertIndex = -1;
                    for (int j = 0; j < sortedFiles.size(); j++) {
                        if (sortedFiles.get(j).getName().toLowerCase().compareTo(sortee.getName().toLowerCase()) > 0) {
                            insertIndex = j;
                            break;
                        }
                    }
                    if (insertIndex >= 0)
                        sortedFiles.add(insertIndex, sortee);
                    else
                        sortedFiles.add(sortee);
                }
                // sort the contents by directory
                int dirsFound = 0;
                for (int i = sortedFiles.size() - 1; i >= dirsFound; i--) {
                    File sortee = sortedFiles.get(i);
                    while (sortee.isDirectory()) {
                        dirsFound++;
                        sortedFiles.remove(i);
                        sortedFiles.add(0, sortee);
                        if (i < dirsFound)
                            break;
                        sortee = sortedFiles.get(i);
                    }
                }
                // put sorted items back into original array
                sortedFiles.toArray(files);
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

    SettingsDrawer.ContextBtn cancelBtn = new SettingsDrawer.ContextBtn("Cancel", () -> {
        ActivityManager.getCurrentActivity().getSettingsDrawer().setShown(false);
    });
    SettingsDrawer.ContextBtn selectAllBtn = new SettingsDrawer.ContextBtn("Select All", () -> {
        for (int i = 0; i < getCount(); i++) {
            DetailItem currentItem = (DetailItem)getItemAtPosition(i);
            if (currentItem.obj != null && !currentItem.leftAlignedText.equals(".."))
                setItemSelected(i, true);
        }
        ActivityManager.getCurrentActivity().getSettingsDrawer().setShown(false);
    });
    SettingsDrawer.ContextBtn deselectBtn = new SettingsDrawer.ContextBtn("Deselect All", () -> {
        for (int i = 0; i < getCount(); i++) {
            DetailItem currentItem = (DetailItem)getItemAtPosition(i);
            if (currentItem.obj != null && !currentItem.leftAlignedText.equals(".."))
                setItemSelected(i, false);
        }
        ActivityManager.getCurrentActivity().getSettingsDrawer().setShown(false);
    });
    SettingsDrawer.ContextBtn invertSelectionBtn = new SettingsDrawer.ContextBtn("Invert Selection", () -> {
        for (int i = 0; i < getCount(); i++) {
            DetailItem currentItem = (DetailItem)getItemAtPosition(i);
            if (currentItem.obj != null && !currentItem.leftAlignedText.equals(".."))
                setItemSelected(i, !isItemSelected(i));
        }
        ActivityManager.getCurrentActivity().getSettingsDrawer().setShown(false);
    });
    SettingsDrawer.ContextBtn toggleSelection = new SettingsDrawer.ContextBtn("Toggle Selection", () -> {
        DetailItem currentItem = (DetailItem)getItemAtPosition(properPosition);
        if (currentItem.obj != null && !currentItem.leftAlignedText.equals(".."))
            setItemSelected(properPosition, !isItemSelected(properPosition));
        selectNextItem();
        //ActivityManager.getCurrentActivity().getSettingsDrawer().setShown(false);
        setupBtns();
    });
    SettingsDrawer.ContextBtn newFolderBtn = new SettingsDrawer.ContextBtn("New Folder", () -> {
        PagedActivity currentActivity = ActivityManager.getCurrentActivity();
        DynamicInputRow.TextInput folderNameTxtInput = new DynamicInputRow.TextInput("Folder Name");
        DynamicInputRow.Label errorLabel = new DynamicInputRow.Label("");
        errorLabel.setGravity(Gravity.BOTTOM | Gravity.LEFT);
        currentActivity.getDynamicInput().setTitle("Create Folder");
        currentActivity.getDynamicInput().setItems
                (
                        new DynamicInputRow(folderNameTxtInput),
                        new DynamicInputRow(errorLabel),
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
                                        }, KeyEvent.KEYCODE_ENTER, KeyEvent.KEYCODE_BUTTON_START),
                                        new DynamicInputRow.ButtonInput("Cancel", v ->
                                        {
                                            //Log.d("ExplorerDynamicView", "Clicked cancel");
                                            currentActivity.getDynamicInput().setShown(false);
                                        }, KeyEvent.KEYCODE_ESCAPE, KeyEvent.KEYCODE_BACK, KeyEvent.KEYCODE_BUTTON_B)
                                )
                );
        currentActivity.getSettingsDrawer().setShown(false);
        currentActivity.getDynamicInput().setShown(true);
    });
    SettingsDrawer.ContextBtn newFileBtn = new SettingsDrawer.ContextBtn("New File", () -> {
        PagedActivity currentActivity = ActivityManager.getCurrentActivity();
        DynamicInputRow.TextInput fileNameTxtInput = new DynamicInputRow.TextInput("File Name");
        DynamicInputRow.Label errorLabel = new DynamicInputRow.Label("");
        errorLabel.setGravity(Gravity.BOTTOM | Gravity.LEFT);
        currentActivity.getDynamicInput().setTitle("Create File");
        currentActivity.getDynamicInput().setItems
                (
                        new DynamicInputRow(fileNameTxtInput),
                        new DynamicInputRow(errorLabel),
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
                                        }, KeyEvent.KEYCODE_ENTER, KeyEvent.KEYCODE_BUTTON_START),
                                        new DynamicInputRow.ButtonInput("Cancel", v ->
                                        {
                                            //Log.d("ExplorerDynamicView", "Clicked cancel");
                                            currentActivity.getDynamicInput().setShown(false);
                                        }, KeyEvent.KEYCODE_ESCAPE, KeyEvent.KEYCODE_BACK, KeyEvent.KEYCODE_BUTTON_B)
                                )
                );
        currentActivity.getSettingsDrawer().setShown(false);
        currentActivity.getDynamicInput().setShown(true);
    });
    SettingsDrawer.ContextBtn renameBtn = new SettingsDrawer.ContextBtn("Rename", () -> {
        // TODO: add option to only add on and not outright change (ex. abc.txt, def.txt, ghi.txt addon prefix of demo_ => demo_abc.txt, demo_def.txt, demo_ghi.txt)
        PagedActivity currentActivity = ActivityManager.getCurrentActivity();
        List<DetailItem> selection = getSelectedItems();
        // there will always be at least one item in the selection
        File firstFile = (File)selection.get(0).obj;
        boolean isMulti = selection.size() > 1;
        boolean isDir = firstFile.isDirectory();
        DynamicInputRow.TextInput renamedTxtInput = new DynamicInputRow.TextInput(isMulti ? "Prefix" : isDir ? "Folder Name" : "File Name");
        DynamicInputRow.TextInput suffixTxtInput = new DynamicInputRow.TextInput("Suffix");
        DynamicInputRow.TextInput startTxtInput = new DynamicInputRow.TextInput("Index Start", InputType.TYPE_CLASS_NUMBER);
        renamedTxtInput.setText(isMulti ? (AndroidHelpers.hasExtension(firstFile.getName()) ? AndroidHelpers.removeExtension(firstFile.getName()) : firstFile.getName()) : firstFile.getName());
        suffixTxtInput.setText(AndroidHelpers.hasExtension(firstFile.getName()) ? "." + AndroidHelpers.getExtension(firstFile.getName()) : "");
        startTxtInput.setText("0");
        DynamicInputRow.Label errorLabel = new DynamicInputRow.Label(isMulti ? "Renaming " + selection.size() + " items" : "");
        DynamicInputRow.ToggleInput fillZerosToggle = new DynamicInputRow.ToggleInput("Place zeros", "Don't place zeros", null);
        DynamicInputRow.ButtonInput okBtn = new DynamicInputRow.ButtonInput("Ok", v ->
        {
            if (isMulti) {
                String value = startTxtInput.getText();
                boolean fillZeros = fillZerosToggle.getOnOff();
                int startIndex = 0;
                if (value.length() > 0) {
                    try {
                        startIndex = Integer.parseInt(value);
                    } catch(Exception e) { Log.e("ExplorerView", e.toString()); }
                }
                int maxDigitCount = getDigitCount(startIndex + (selection.size() - 1));
                for (int i = 0; i < selection.size(); i++) {
                    File current = (File)selection.get(i).obj;
                    int index = (startIndex + i);
                    String zeros = "";
                    if (fillZeros) {
                        int currentDigitCount = getDigitCount(index);
                        for (int j = 0; j < (maxDigitCount - currentDigitCount); j++)
                            zeros += "0";
                    }
                    String newName = renamedTxtInput.getText() + zeros + index + suffixTxtInput.getText();
                    current.renameTo(new File(AndroidHelpers.combinePaths(current.getParent(), newName)));
                }
                refresh();
                currentActivity.getDynamicInput().setShown(false);
            } else {
                String newName = renamedTxtInput.getText();
                if (newName != null && newName.length() > 0) {
                    firstFile.renameTo(new File(AndroidHelpers.combinePaths(firstFile.getParent(), newName)));
                    refresh();
                    currentActivity.getDynamicInput().setShown(false);
                } else
                    errorLabel.setLabel("Name is invalid");
            }
        }, KeyEvent.KEYCODE_ENTER, KeyEvent.KEYCODE_BUTTON_START);
        DynamicInputRow.ButtonInput cancelBtn = new DynamicInputRow.ButtonInput("Cancel", v ->
        {
            //Log.d("ExplorerDynamicView", "Clicked cancel");
            currentActivity.getDynamicInput().setShown(false);
        }, KeyEvent.KEYCODE_ESCAPE, KeyEvent.KEYCODE_BACK, KeyEvent.KEYCODE_BUTTON_B);
        currentActivity.getDynamicInput().setTitle("Rename" + (isMulti ? " Items" : (isDir ? " Folder" : " File")));
        if (isMulti)
            currentActivity.getDynamicInput().setItems(new DynamicInputRow(errorLabel), new DynamicInputRow(renamedTxtInput, suffixTxtInput), new DynamicInputRow(startTxtInput), new DynamicInputRow(fillZerosToggle), new DynamicInputRow(okBtn, cancelBtn));
        else
            currentActivity.getDynamicInput().setItems(new DynamicInputRow(errorLabel), new DynamicInputRow(renamedTxtInput), new DynamicInputRow(okBtn, cancelBtn));
        currentActivity.getSettingsDrawer().setShown(false);
        currentActivity.getDynamicInput().setShown(true);
    });
    SettingsDrawer.ContextBtn copyBtn = new SettingsDrawer.ContextBtn("Copy", () -> {
        PagedActivity currentActivity = ActivityManager.getCurrentActivity();
        List<DetailItem> selection = getSelectedItems();
        currentActivity.getDynamicInput().setTitle("Copy");
        String[] filePaths = new String[selection.size()];
        for (int i = 0; i < selection.size(); i++)
            filePaths[i] = ((File)selection.get(i).obj).getAbsolutePath();
        explorerBehaviour.copy(filePaths);
        currentActivity.getSettingsDrawer().setShown(false);
        //currentActivity.getDynamicInput().setShown(true);
    });
    SettingsDrawer.ContextBtn cutBtn = new SettingsDrawer.ContextBtn("Cut", () -> {
        PagedActivity currentActivity = ActivityManager.getCurrentActivity();
        List<DetailItem> selection = getSelectedItems();
        String[] filePaths = new String[selection.size()];
        for (int i = 0; i < selection.size(); i++)
            filePaths[i] = ((File)selection.get(i).obj).getAbsolutePath();
        explorerBehaviour.cut(filePaths);
        currentActivity.getSettingsDrawer().setShown(false);
    });
    SettingsDrawer.ContextBtn pasteBtn = new SettingsDrawer.ContextBtn("Paste", () -> {
        PagedActivity currentActivity = ActivityManager.getCurrentActivity();
        explorerBehaviour.paste();
        refresh();
        currentActivity.getSettingsDrawer().setShown(false);
    });
    SettingsDrawer.ContextBtn deleteBtn = new SettingsDrawer.ContextBtn("Delete", () -> {
        PagedActivity currentActivity = ActivityManager.getCurrentActivity();
        List<DetailItem> selection = getSelectedItems();
        String[] filePaths = new String[selection.size()];
        for (int i = 0; i < selection.size(); i++)
            filePaths[i] = ((File)selection.get(i).obj).getAbsolutePath();
        ExplorerBehaviour.delete(filePaths);
        refresh();
        currentActivity.getSettingsDrawer().setShown(false);
    });
}