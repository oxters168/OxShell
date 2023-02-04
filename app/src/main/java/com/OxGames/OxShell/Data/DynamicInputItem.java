package com.OxGames.OxShell.Data;

import android.text.TextWatcher;

import java.util.concurrent.Callable;

public class DynamicInputItem {
    public String title;
    //private Callable event;
    private TextWatcher watcher;

    public DynamicInputItem(String title, TextWatcher watcher) {
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
