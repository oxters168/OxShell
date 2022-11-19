package com.OxGames.OxShell.Views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;

import com.OxGames.OxShell.Helpers.ActivityManager;
import com.OxGames.OxShell.Adapters.DetailAdapter;
import com.OxGames.OxShell.Data.DetailItem;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class SettingsView extends SlideTouchListView {
    public enum ButtonType { homeSettings, explorerSettings, assocSettings, bgSettings, } //Order of enum sets order of buttons in settings
    private HashMap<ButtonType, String> buttons;
//    private ActivityManager.Page CURRENT_PAGE = ActivityManager.Page.addToHome;
//    private final String[] btnLabels = new String[] { "Add Explorer", "Add Application", "Add Association" };

    public SettingsView(Context context) {
        super(context);
        refresh();
    }
    public SettingsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        refresh();
    }
    public SettingsView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        refresh();
    }

    @Override
    public boolean receiveKeyEvent(KeyEvent key_event) {
        if (key_event.getAction() == KeyEvent.ACTION_DOWN) {
            if (key_event.getKeyCode() == KeyEvent.KEYCODE_BUTTON_B || key_event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
                ActivityManager.goTo(ActivityManager.Page.home);
                return true;
            }
        }

        return super.receiveKeyEvent(key_event);
    }

//    @Override
//    public void onConfigurationChanged(Configuration newConfig) {
//        super.onConfigurationChanged(newConfig);
//        int storedPos = properPosition;
//        RefreshBtns();
//        SetProperPosition(storedPos);
//    }

    @Override
    public void makeSelection() {
        ButtonType selectedItem = (ButtonType)((DetailItem)getItemAtPosition(properPosition)).obj;
        if (selectedItem == ButtonType.homeSettings)
            ActivityManager.goTo(ActivityManager.Page.customizeHome);
        else if (selectedItem == ButtonType.assocSettings)
            ActivityManager.goTo(ActivityManager.Page.assocList);
    }

    private void initButtons() {
        buttons = new HashMap<>();
        buttons.put(ButtonType.homeSettings, "Home Settings");
        buttons.put(ButtonType.explorerSettings, "Explorer Settings");
        buttons.put(ButtonType.assocSettings, "Edit Associations");
        buttons.put(ButtonType.bgSettings, "Background Properties"); //frame rate, shader properties (texture, color, values, ...)
    }
    @Override
    public void refresh() {
        initButtons();
        DetailItem[] addBtns = new DetailItem[buttons.size()];
        Set<Map.Entry<ButtonType, String>> entrySet = buttons.entrySet();
        for (Map.Entry<ButtonType, String> entry : entrySet)
            addBtns[entry.getKey().ordinal()] = new DetailItem(null, entry.getValue(), null, entry.getKey());
        DetailAdapter addAdapter = new DetailAdapter(getContext(), Arrays.asList(addBtns));
        setAdapter(addAdapter);
        super.refresh();
    }
}
