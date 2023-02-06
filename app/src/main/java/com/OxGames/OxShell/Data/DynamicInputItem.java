package com.OxGames.OxShell.Data;

import android.text.TextWatcher;

import java.util.concurrent.Callable;

public class DynamicInputItem {
    public enum InputType {
        text,
        button,
        label
    }
    public InputType inputType;
    public String title;
    //private Callable event;
    private TextWatcher watcher;

    public DynamicInputItem(InputType inputType, String title, TextWatcher watcher) {
        this.inputType = inputType;
        this.title = title;
        //this.event = event;
        this.watcher = watcher;
    }

    public TextWatcher getWatcher() {
        return watcher;
    }
//    public void setWatcher(TextWatcher watcher) {
//        this.watcher = watcher;
//    }
}
