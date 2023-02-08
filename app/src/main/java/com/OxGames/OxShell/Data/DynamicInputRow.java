package com.OxGames.OxShell.Data;

import android.text.TextWatcher;
import android.util.Log;

public class DynamicInputRow {
    private DynamicInput[] inputs;

    public DynamicInputRow(TextInput... inputs) {
//        Log.d("DynamicInputRow", "Creating row with " + inputs.length + " item(s)");
//        for (int i = 0; i < inputs.length; i++) {
//            Log.d("DynamicInputRow", "@" + i + " is null: " + (inputs[i] == null));
//        }
        this.inputs = inputs;
    }

    public DynamicInput get(int index) {
        return inputs[index];
    }
    public DynamicInput[] getAll() {
        // intentionally a shallow copy
        return inputs.clone();
    }

    public abstract static class DynamicInput {
        public enum InputType {
            text,
            button,
            label
        }
        public InputType inputType;
    }
    public static class TextInput extends DynamicInput {
        public String title;
        private TextWatcher watcher;

        public TextInput(String title, TextWatcher watcher) {
            this.inputType = InputType.text;
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
