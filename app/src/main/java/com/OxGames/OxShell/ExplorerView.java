package com.OxGames.OxShell;

import static androidx.core.content.ContextCompat.startActivity;

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

public class ExplorerView extends ListView implements PermissionsListener {

    private ExplorerBehaviour explorerBehaviour;
    float startTouchY = 0;
    float currentTouchY = 0;
    float prevTouchY = 0;
    boolean moved = false;
    int properPosition = 0;
    float deadzone = 0.2f;

    int framesPassed = 0;
    int framesPerScroll = 24;

    public ExplorerView(Context context) {
        super(context);
        ExplorerActivity.GetInstance().AddPermissionListener(this);
        explorerBehaviour = new ExplorerBehaviour();
        RefreshExplorerList();
    }

    public ExplorerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        ExplorerActivity.GetInstance().AddPermissionListener(this);
        explorerBehaviour = new ExplorerBehaviour();
        RefreshExplorerList();
    }

    public ExplorerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
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
                Log.d("Explorer", "Storage permission denied");
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

//        requestFocusFromTouch();
        if (moved) {
            float diff = currentTouchY - startTouchY;
            float percentScroll = Math.abs(diff / ((float) HomeActivity.displayMetrics.heightPixels / 5f));
            if (percentScroll > 1)
                percentScroll = 1;
            if (percentScroll < deadzone)
                percentScroll = 0;
            else
                percentScroll = (percentScroll - deadzone) / (1 - deadzone);

//            Log.d("Touch", diff + " / " + FullscreenActivity.displayMetrics.heightPixels + " = " + percentScroll);

            float stretchedFramesPerScroll = (1 - percentScroll) * framesPerScroll;
            if (percentScroll > 0 && diff > 0) {
                //Go down
                if (framesPassed > stretchedFramesPerScroll) {
                    framesPassed = 0;
                    SelectNextItem();
                }
            } else if (percentScroll > 0 && diff < 0) {
                //Go up
                if (framesPassed > stretchedFramesPerScroll) {
                    framesPassed = 0;
                    SelectPrevItem();
                }
            }
            framesPassed++;
            invalidate();
        }

        HighlightSelection();
    }

    @Override
    public boolean onKeyDown(int key_code, KeyEvent key_event) {
//        Log.d("Input", key_code + " " + key_event);
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

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
//        requestFocusFromTouch();

        return true;
    }
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        currentTouchY = ev.getY();

        final int action = ev.getAction();
        switch (action) {
            case MotionEvent.ACTION_MOVE:
                moved = true;
                invalidate();
//                Log.d("Touch", "Diff = " + diff);
//                Log.d("Touch", "Action_Move (" + ev.getX() + ", " + ev.getY() + ")");
                break;
            case MotionEvent.ACTION_DOWN:
                moved = false;
                startTouchY = currentTouchY;
//                Log.d("Touch", "Action_Down (" + ev.getX() + ", " + ev.getY() + ")");
                break;
            case MotionEvent.ACTION_UP:
                if (!moved) {
                    //Click
//                    Log.d("Touch", "Clicked");
                    MakeSelection();
                }
                moved = false;
//                Log.d("Touch", "Action_Up (" + ev.getX() + ", " + ev.getY() + ")");
                break;
        }

        prevTouchY = currentTouchY;
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
            LaunchIntent(clickedItem);
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
            ExplorerAdapter customAdapter = new ExplorerAdapter(getContext(), arrayList);
            setAdapter(customAdapter);
        }
        SetProperPosition(0);

//        binding.emptyView.setVisibility((isEmpty && !hasParent) ? View.VISIBLE : View.GONE);
//        setVisibility((isEmpty && !hasParent) ? View.GONE : View.VISIBLE);
    }

    private void LaunchIntent(ExplorerItem clickedItem) {
        //Cheat sheet: http://p.cweiske.de/221
//            IntentLaunchData launchData = new IntentLaunchData(Intent.ACTION_VIEW, "com.dsemu.drastic", "com.dsemu.drastic.DraSticActivity");
//            launchData.AddExtra(new IntentPutExtra("GAMEPATH", clickedItem.absolutePath));
        if (clickedItem.absolutePath.contains(".")) {
            String extension = ExplorerBehaviour.GetExtension(clickedItem.absolutePath);

            IntentLaunchData fileLaunchIntent = PackagesCache.GetLaunchDataForExtension(extension);

            if (fileLaunchIntent != null) {
                IntentLaunchData.IntentType dataType = fileLaunchIntent.GetDataType();
                String data = null;
                if (dataType == IntentLaunchData.IntentType.AbsolutePath)
                    data = clickedItem.absolutePath;

                IntentPutExtra[] extras = fileLaunchIntent.GetExtras();
                String[] extrasValues = null;
                if (extras != null && extras.length > 0) {
                    extrasValues = new String[extras.length];
                    for (int i = 0; i < extras.length; i++)
                        if (extras[i].GetExtraType() == IntentLaunchData.IntentType.AbsolutePath)
                            extrasValues[i] = clickedItem.absolutePath;
                }
                startActivity(getContext(), fileLaunchIntent.BuildIntent(data, extrasValues), null);
            }
            else
                Log.e("Explorer", "No launch intent associated with extension " + extension);
        }
        else
            Log.e("Explorer", "Missing extension, could not identify file");
    }
}