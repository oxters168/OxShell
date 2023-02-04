package com.OxGames.OxShell.Views;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.OxGames.OxShell.Adapters.DetailAdapter;
import com.OxGames.OxShell.Data.DetailItem;
import com.OxGames.OxShell.OxShellApp;
import com.OxGames.OxShell.R;

import java.util.concurrent.Callable;

public class SettingsDrawer extends FrameLayout {
    private static final float SETTINGS_DRAWER_OPEN_Y = 0;
    private static final float SETTINGS_DRAWER_CLOSED_Y = 0;
    private static final long SETTINGS_DRAWER_ANIM_TIME = 300;

    private boolean isShown = false;
    private final Context context;

    private SlideTouchListView listView;

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

    private void init() {
        setX(OxShellApp.getDisplayWidth());
        setBackgroundColor(Color.parseColor("#80323232"));
        setAlpha(0);

        listView = new SlideTouchListView(context);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        listView.setLayoutParams(layoutParams);
        listView.setScrollbarFadingEnabled(false);
        listView.setScrollBarStyle(SCROLLBARS_INSIDE_INSET);
        listView.setSelector(R.color.transparent);
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
    }
    public boolean receiveKeyEvent(KeyEvent keyEvent) {
        //Log.d("SettingsDrawer", "Received key event");
        return listView.receiveKeyEvent(keyEvent);
    }

    public static class ContextBtn {
        String label;
        Callable event;
        public ContextBtn(String label, Callable event) {
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
                    try {
                        if (btn.event != null)
                            btn.event.call();
                        else
                            Log.e("PagedActivity", "Button event for " + btn.label + " is null");
                    } catch (Exception ex) {
                        Log.e("PagedActivity", "Failed to call context event: " + ex);
                    }
                }
            });
        }
        listView.setAdapter(listAdapter);
    }
}
