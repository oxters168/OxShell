package com.OxGames.OxShell;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ListView;

import androidx.core.content.ContextCompat;

import java.io.File;
import java.util.ArrayList;

public class ExplorerView extends ListView implements PermissionsListener, SlideTouchListener {

    private ExplorerBehaviour explorerBehaviour;
    private SlideTouchHandler slideTouch = new SlideTouchHandler();
    int properPosition = 0;

    public ExplorerView(Context context) {
        super(context);
        slideTouch.AddListener(this);
        ExplorerActivity.GetInstance().AddPermissionListener(this);
        explorerBehaviour = new ExplorerBehaviour();
        RefreshExplorerList();
    }

    public ExplorerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        slideTouch.AddListener(this);
        ExplorerActivity.GetInstance().AddPermissionListener(this);
        explorerBehaviour = new ExplorerBehaviour();
        RefreshExplorerList();
    }

    public ExplorerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        slideTouch.AddListener(this);
        ExplorerActivity.GetInstance().AddPermissionListener(this);
        explorerBehaviour = new ExplorerBehaviour();
        RefreshExplorerList();
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
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        slideTouch.CheckForEvents();
        HighlightSelection();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return true;
    }
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        slideTouch.Update(ev);
        return true;
    }

    @Override
    public void onRequestInvalidate() {
        invalidate();
    }
    @Override
    public void onClick() {
        MakeSelection();
    }
    @Override
    public void onSwipeUp() {
        SelectPrevItem();
    }
    @Override
    public void onSwipeDown() {
        SelectNextItem();
    }
    @Override
    public void onSwipeRight() {

    }
    @Override
    public void onSwipeLeft() {

    }


    @Override
    public boolean onKeyDown(int key_code, KeyEvent key_event) {
        Log.d("ExplorerView", key_code + " " + key_event);
        if (key_code == KeyEvent.KEYCODE_BUTTON_START || key_code == KeyEvent.KEYCODE_BACK) {
            ActivityManager.GoTo(ActivityManager.Page.home);
            return false;
        }

        if (key_code == KeyEvent.KEYCODE_BUTTON_A) {
            MakeSelection();
            return false;
        }
        if (key_code == KeyEvent.KEYCODE_BUTTON_B) {
            GoUp();
            return false;
        }
        if (key_code == KeyEvent.KEYCODE_DPAD_DOWN) {
            SelectNextItem();
            return false;
        }
        if (key_code == KeyEvent.KEYCODE_DPAD_UP) {
            SelectPrevItem();
            return false;
        }
        return true;
    }
    private void HighlightSelection() {
        for (int i = 0; i < getCount(); i++) {
            View view = ((ExplorerItem)getItemAtPosition(i)).view;
            if (view != null)
                view.setBackgroundResource((i == properPosition) ? R.color.scheme1 : R.color.light_blue_400);
        }
    }
    public void SelectNextItem() {
        int total = getCount();
        int nextIndex = properPosition + 1;
        if (nextIndex >= total)
            nextIndex = total - 1;
        SetProperPosition(nextIndex);
    }
    public void SelectPrevItem() {
        int prevIndex = properPosition - 1;
        if (prevIndex < 0)
            prevIndex = 0;
        SetProperPosition(prevIndex);
    }
    public void MakeSelection() {
        ExplorerItem clickedItem = (ExplorerItem)getItemAtPosition(properPosition);
        if (clickedItem.isDir) {
            explorerBehaviour.SetDirectory(clickedItem.absolutePath);
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
            String itemDir = ((ExplorerItem)getItemAtPosition(i)).absolutePath;
            if (itemDir.equalsIgnoreCase(previousDir)) {
                requestFocusFromTouch();
                SetProperPosition(i);
                break;
            }
        }
    }
    public void SetProperPosition(int pos) {
//        Log.d("Explorer", "Setting position to " + pos);
        properPosition = pos;
        setSelectionFromTop(pos, HomeActivity.displayMetrics != null ? (int)(HomeActivity.displayMetrics.heightPixels * 0.5) : 0);
//        setSelection(pos);
//        HighlightSelection();
    }

    private void RefreshExplorerList() {
        ArrayList<ExplorerItem> arrayList = new ArrayList<>();
        File[] files = explorerBehaviour.ListContents();
        boolean isEmpty = files == null || files.length <= 0;
        boolean hasParent = explorerBehaviour.HasParent();
        if (!isEmpty || hasParent) {
            if (hasParent)
                arrayList.add(new ExplorerItem(ContextCompat.getDrawable(getContext(), R.drawable.ic_baseline_folder_24), explorerBehaviour.GetParent(), "..", true));
            if (explorerBehaviour.GetDirectory().equalsIgnoreCase("/storage/emulated"))
                arrayList.add(new ExplorerItem(ContextCompat.getDrawable(getContext(), R.drawable.ic_baseline_folder_24), "/storage/emulated/0", "0", true));
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

                    arrayList.add(new ExplorerItem(icon, absolutePath, files[i].getName(), files[i].isDirectory()));
                }
            }
            ExplorerAdapter customAdapter = new ExplorerAdapter(getContext(), arrayList, true);
            setAdapter(customAdapter);
        }
        SetProperPosition(0);

//        binding.emptyView.setVisibility((isEmpty && !hasParent) ? View.VISIBLE : View.GONE);
//        setVisibility((isEmpty && !hasParent) ? View.GONE : View.VISIBLE);
    }

    private void TryRun(ExplorerItem clickedItem) {
        if (clickedItem.absolutePath.contains(".")) {
            String extension = ExplorerBehaviour.GetExtension(clickedItem.absolutePath);

            IntentLaunchData fileLaunchIntent = PackagesCache.GetLaunchDataForExtension(extension);

            if (fileLaunchIntent != null) {
                fileLaunchIntent.Launch(clickedItem.absolutePath);
            }
            else
                Log.e("Explorer", "No launch intent associated with extension " + extension);
        }
        else
            Log.e("Explorer", "Missing extension, could not identify file");
    }
//    private void LaunchIntent(ExplorerItem clickedItem) {
//        //Cheat sheet: http://p.cweiske.de/221
////            IntentLaunchData launchData = new IntentLaunchData(Intent.ACTION_VIEW, "com.dsemu.drastic", "com.dsemu.drastic.DraSticActivity");
////            launchData.AddExtra(new IntentPutExtra("GAMEPATH", clickedItem.absolutePath));
//        if (clickedItem.absolutePath.contains(".")) {
//            String extension = ExplorerBehaviour.GetExtension(clickedItem.absolutePath);
//
//            IntentLaunchData fileLaunchIntent = PackagesCache.GetLaunchDataForExtension(extension);
//
//            if (fileLaunchIntent != null) {
//                IntentLaunchData.DataType dataType = fileLaunchIntent.GetDataType();
//                String data = null;
//                if (dataType == IntentLaunchData.DataType.AbsolutePath)
//                    data = clickedItem.absolutePath;
//
//                IntentPutExtra[] extras = fileLaunchIntent.GetExtras();
//                String[] extrasValues = null;
//                if (extras != null && extras.length > 0) {
//                    extrasValues = new String[extras.length];
//                    for (int i = 0; i < extras.length; i++)
//                        if (extras[i].GetExtraType() == IntentLaunchData.DataType.AbsolutePath)
//                            extrasValues[i] = clickedItem.absolutePath;
//                }
//                startActivity(getContext(), fileLaunchIntent.BuildIntent(data, extrasValues), null);
//            }
//            else
//                Log.e("Explorer", "No launch intent associated with extension " + extension);
//        }
//        else
//            Log.e("Explorer", "Missing extension, could not identify file");
//    }
}