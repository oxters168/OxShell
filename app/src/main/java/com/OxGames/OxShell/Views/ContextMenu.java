package com.OxGames.OxShell.Views;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;

import com.OxGames.OxShell.Data.DetailItem;
import com.OxGames.OxShell.Adapters.DetailAdapter;
import com.OxGames.OxShell.R;

import java.util.concurrent.Callable;

public class ContextMenu extends Dialog {
    private SlideTouchListView contextListView;
    private DetailAdapter listAdapter;

    public ContextMenu(@NonNull Context context) {
        super(context, android.R.style.ThemeOverlay);
        setContentView(R.layout.home_context_menu);
        hideSystemUI();
        refresh();

        getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.gravity = Gravity.TOP | Gravity.LEFT;
        // params.flags &= ~WindowManager.LayoutParams.FLAG_FULLSCREEN;
        getWindow().setAttributes(params);
        setBackgroundColor(Color.parseColor("#88000000"));
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent ev) {
        return contextListView.onTouchEvent(ev);
    }
    @Override
    public boolean dispatchKeyEvent(@NonNull KeyEvent key_event) {
        //Log.d("DialogDispatchKey", key_event.toString());

        if (key_event.getKeyCode() == KeyEvent.KEYCODE_BUTTON_B) {
            dismiss();
            return true;
        }

        return contextListView.receiveKeyEvent(key_event);
    }

    public void setBackgroundColor(int bgColor) {
        final Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        window.setBackgroundDrawable(new ColorDrawable(bgColor));
    }

    public void hideSystemUI() {
        // Set the IMMERSIVE flag.
        // Set the content to appear under the system bars so that the content
        // doesn't resize when the system bars hide and show.
        getWindow().getDecorView().setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
            | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );
    }

    public void addButton(String label, Callable btnEvent) {
        int matchingIndex = listAdapter.getCount();
        listAdapter.add(new DetailItem(null, label, null, null));
        contextListView.addListener(index -> {
            if (index == matchingIndex) {
                try {
                    btnEvent.call();
                } catch (Exception ex) {
                    Log.e("ContextMenu", ex.getMessage());
                }
            }
        });
    }
    public void refresh() {
        contextListView = findViewById(R.id.context_btns_list);
        listAdapter = new DetailAdapter(getContext());
        contextListView.setAdapter(listAdapter);
    }
}
