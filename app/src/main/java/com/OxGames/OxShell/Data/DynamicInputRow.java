package com.OxGames.OxShell.Data;

import android.text.TextWatcher;
import android.view.View;

import com.OxGames.OxShell.Interfaces.DynamicInputListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
        protected List<DynamicInputListener> listeners = new ArrayList<>();
        public void addListener(DynamicInputListener listener) {
            listeners.add(listener);
        }
        public void removeListener(DynamicInputListener listener) {
            listeners.remove(listener);
        }
        public void clearListeners() {
            listeners.clear();
        }
        public void onFocusChange(View view, boolean hasFocus) {
            for (DynamicInputListener listener : listeners)
                if (listener != null)
                    listener.onFocusChanged(view, hasFocus);
        }
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
            setText(value, true);
        }
        public String getText() {
            return text;
        }
        public void setText(String value, boolean fireEvent) {
            text = value;
            if (fireEvent)
                for (DynamicInputListener listener : listeners)
                    if (listener != null)
                        listener.onValuesChanged();
        }
    }
    public static class ButtonInput extends DynamicInput {
        private String label;
        private View.OnClickListener onClick;

//        private boolean isKeycodeSet;
//        private int keycode;
        List<Integer> keycodes;

        public ButtonInput(String label, View.OnClickListener onClick) {
            this.inputType = InputType.button;
            this.label = label;
            this.onClick = onClick;
            keycodes = new ArrayList<>();
        }
        public ButtonInput(String label, View.OnClickListener onClick, Integer... keycodes) {
            this(label, onClick);
            Collections.addAll(this.keycodes, keycodes);
        }

        public View.OnClickListener getOnClick() {
            return onClick;
        }
        public void executeAction() {
            if (onClick != null)
                onClick.onClick(null);
        }

        public boolean hasKeycode(int keycode) {
            return keycodes.contains(keycode);
        }
        public boolean isKeycodeSet() {
            return keycodes.size() > 0;
        }

        public void setLabel(String value) {
            setLabel(value, true);
        }
        public void setLabel(String value, boolean fireEvent) {
            label = value;
            if (fireEvent)
                for (DynamicInputListener listener : listeners)
                    if (listener != null)
                        listener.onValuesChanged();
        }
        public String getLabel() {
            return label;
        }
    }
    public static class Label extends DynamicInput {
        private String label;

        public Label(String label) {
            this.inputType = InputType.label;
            this.label = label;
        }

        public void setLabel(String value) {
            setLabel(value, true);
        }
        public void setLabel(String value, boolean fireEvent) {
            label = value;
            if (fireEvent)
                for (DynamicInputListener listener : listeners)
                    if (listener != null)
                        listener.onValuesChanged();
        }
        public String getLabel() {
            return label;
        }
    }
}
