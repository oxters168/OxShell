package com.OxGames.OxShell.Data;

import android.text.TextWatcher;
import android.view.View;

public class DynamicInputRow {
    private DynamicInput[] inputs;

    public DynamicInputRow(DynamicInput... inputs) {
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
        public String hint;
        private String text;

        public TextInput(String hint) {
            this.inputType = InputType.text;
            this.hint = hint;
            this.text = "";
        }

        // setText should be only accessible from the view, but whatever
        public void setText(String value) {
            text = value;
        }
        public String getText() {
            return text;
        }
    }
    public static class ButtonInput extends DynamicInput {
        public String label;
        private View.OnClickListener onClick;

        public ButtonInput(String label, View.OnClickListener onClick) {
            this.inputType = InputType.button;
            this.label = label;
            this.onClick = onClick;
        }

        public View.OnClickListener getOnClick() {
            return onClick;
        }
    }
    public static class Label extends DynamicInput {
        public String label;

        public Label(String label) {
            this.inputType = InputType.label;
            this.label = label;
        }
    }
}
