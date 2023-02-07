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
    private Object[] inputs;

    public DynamicInputItem(TextInput... inputs) {
        this.inputType = InputType.text;
        this.inputs = inputs;
        //this.inputType = inputType;
        //this.title = title;
        //this.event = event;
        //this.watcher = watcher;
    }

    public Object get(int index) {
        return inputs[index];
    }

    public static class TextInput {
        public String title;
        //private Callable event;
        private TextWatcher watcher;

        public TextInput(String title, TextWatcher watcher) {
            this.title = title;
            this.watcher = watcher;
        }

        public TextWatcher getWatcher() {
            return watcher;
        }
    }
//    public TextWatcher getWatcher() {
//        return watcher;
//    }
//    public void setWatcher(TextWatcher watcher) {
//        this.watcher = watcher;
//    }
}
