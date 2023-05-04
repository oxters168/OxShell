package com.OxGames.OxShell.Views;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.OxGames.OxShell.Adapters.DetailAdapter;
import com.OxGames.OxShell.Data.DetailItem;
import com.OxGames.OxShell.Data.KeyComboAction;
import com.OxGames.OxShell.Data.SettingsKeeper;
import com.OxGames.OxShell.Helpers.AndroidHelpers;
import com.OxGames.OxShell.Helpers.InputHandler;
import com.OxGames.OxShell.OxShellApp;
import com.OxGames.OxShell.R;

import java.util.Arrays;

public class SettingsDrawer extends FrameLayout {// implements InputReceiver {
    private static final float SETTINGS_DRAWER_OPEN_Y = 0;
    private static final float SETTINGS_DRAWER_CLOSED_Y = 0;
    private static final long SETTINGS_DRAWER_ANIM_TIME = 300;

    private boolean isShown = false;
    private final Context context;

    private SlideTouchListView listView;
    //private InputHandler inputHandler;

    private static final String INPUT_TAG = "SETTINGS_DRAWER_INPUT";

    public SettingsDrawer(@NonNull Context context) {
        super(context);
        this.context = context;
        init();
    }
    public SettingsDrawer(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
    }
    public SettingsDrawer(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init();
    }
    public SettingsDrawer(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.context = context;
        init();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //Log.d("SettingsDrawer", event.toString());
        if (event.getAction() == MotionEvent.ACTION_UP)
            setShown(false);
        return true;
    }

    private int getDrawerWidth() {
        return Math.round(AndroidHelpers.getScaledDpToPixels(context, 200));
    }
    private void init() {
        //inputHandler = new InputHandler();
        //inputHandler.addKeyComboActions(Arrays.stream(SettingsKeeper.getCancelInput()).map(combo -> new KeyComboAction(combo, () -> setShown(false))).toArray(KeyComboAction[]::new));

        FrameLayout.LayoutParams layoutParams;

        layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        setLayoutParams(layoutParams);
        //setBackgroundColor(Color.parseColor("#88323232"));
        setX(OxShellApp.getDisplayWidth());
        setAlpha(0);

        listView = new SlideTouchListView(context);
        layoutParams = new FrameLayout.LayoutParams(getDrawerWidth(), ViewGroup.LayoutParams.MATCH_PARENT);
        layoutParams.gravity = Gravity.END;
        listView.setLayoutParams(layoutParams);
        listView.setScrollbarFadingEnabled(false);
        listView.setScrollBarStyle(SCROLLBARS_INSIDE_INSET);
        listView.setSelector(R.color.transparent);
        listView.setBackground(ContextCompat.getDrawable(context, R.drawable.fading_right_edge));
        listView.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#323232")));
        addView(listView);
    }

    public boolean isDrawerOpen() {
        //Log.d("SettingsDrawer", "isShown: " + isShown);
        return isShown;
    }
    public void setShown(boolean onOff) {
        isShown = onOff;
        float settingsDrawerOpenX = OxShellApp.getDisplayWidth() - getWidth();
        float settingsDrawerClosedX = OxShellApp.getDisplayWidth();
        float xDist = (isShown ? settingsDrawerOpenX : settingsDrawerClosedX) - getX();
        float yDist = (isShown ? SETTINGS_DRAWER_OPEN_Y : SETTINGS_DRAWER_CLOSED_Y) - getY();
        float alphaDist = (isShown ? 1 : 0) - getAlpha();
        long duration = Math.round(SETTINGS_DRAWER_ANIM_TIME * (Math.abs(xDist) / Math.abs(settingsDrawerClosedX - settingsDrawerOpenX)));
        //Log.d("SettingsDrawer", "Settings view open: " + isShown + " x: " + getX() + " xdist: " + xDist + " ydist: " + yDist + " alphadist: " + alphaDist + " duration: " + duration);
        animate().setDuration(duration);
        animate().xBy(xDist);
        animate().yBy(yDist);
        animate().alphaBy(alphaDist);
        if (onOff) {
            listView.setProperPosition(0);

            InputHandler.addKeyComboActions(INPUT_TAG, Arrays.stream(SettingsKeeper.getCancelInput()).map(combo -> new KeyComboAction(combo, () -> setShown(false))).toArray(KeyComboAction[]::new));
            InputHandler.addKeyComboActions(INPUT_TAG, listView.getKeyComboActions());
            int priorityLevel;
            InputHandler.setTagPriority(INPUT_TAG, priorityLevel = InputHandler.getHighestPriority() + 1);
            InputHandler.setCurrentPriorityLevel(priorityLevel);
            InputHandler.setTagEnabled(INPUT_TAG, true);
        } else {
            //InputHandler.removeTagFromHistory(INPUT_TAG);
            InputHandler.clearKeyComboActions(INPUT_TAG);
            InputHandler.setTagEnabled(INPUT_TAG, false);
        }
    }
//    @Override
//    public boolean receiveKeyEvent(KeyEvent keyEvent) {
//        //Log.d("SettingsDrawer", "Received key event");
//        if (inputHandler.onInputEvent(keyEvent))
//            return true;
//        return listView.receiveKeyEvent(keyEvent);
//    }

    public static class ContextBtn {
        String label;
        Runnable event;
        public ContextBtn(String label, Runnable event) {
            this.label = label;
            this.event = event;
        }
    }
    public void setButtons(ContextBtn... btns) {
        listView.clearListeners();
        DetailAdapter listAdapter = new DetailAdapter(context);
        for (int i = 0; i < btns.length; i++) {
            ContextBtn btn = btns[i];
            listAdapter.add(new DetailItem(null, btn.label, null, null));
            int btnIndex = i;
            listView.addListener(index -> {
                if (btnIndex == index) {
                    //try {
                        if (btn.event != null)
                            btn.event.run();
                        else
                            Log.e("SettingsDrawer", "Button event for " + btn.label + " is null");
//                    } catch (Exception ex) {
//                        Log.e("SettingsDrawer", "Failed to call context event: " + ex);
//                    }
                }
            });
        }
        listView.setAdapter(listAdapter);
    }
}
