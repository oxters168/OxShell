package com.OxGames.OxShell;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;

public class HomeContextMenu extends Dialog {
    public HomeView currentHomeView; //Should change to an interface and implement the interfaces wherever context menus are needed (HomeView, ExplorerView, PackagesView...)

    public HomeContextMenu(@NonNull Context context) {
        super(context, android.R.style.ThemeOverlay);
        setContentView(R.layout.home_context_menu);
        hideSystemUI();

        setBackgroundColor(Color.parseColor("#00000000"));
        findViewById(R.id.remove_btn).setOnClickListener(view -> {
            currentHomeView.deleteSelection();
            dismiss();
        });
        findViewById(R.id.uninstall_btn).setOnClickListener(view -> {
            currentHomeView.uninstallSelection();
            currentHomeView.deleteSelection();
            dismiss();
        });
    }

    public void setBackgroundColor(int bgColor) {
        final Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        window.setBackgroundDrawable(new ColorDrawable(bgColor));
    }

    @Override
    public boolean dispatchKeyEvent(@NonNull KeyEvent key_event) {
        Log.d("DialogDispatchKey", key_event.toString());

        if (key_event.getKeyCode() == KeyEvent.KEYCODE_BUTTON_B) {
            dismiss();
            return true;
        }

        return super.dispatchKeyEvent(key_event);
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
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }
}
