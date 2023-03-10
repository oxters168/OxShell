package com.OxGames.OxShell.Data;

import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;

import com.OxGames.OxShell.Interfaces.DynamicInputListener;
import com.OxGames.OxShell.Views.DynamicInputItemView;

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

    public int getCount() {
        return inputs.length;
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
            label,
            toggle
        }
        public int row = -1, col = -1;
        public DynamicInputItemView view;
        public void setSelected(boolean onOff) {
            if (view != null)
                view.refreshSelection(onOff);
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
        protected void valuesChanged() {
            for (DynamicInputListener listener : listeners)
                if (listener != null)
                    listener.onValuesChanged();
        }
    }
    public static class TextInput extends DynamicInput {
        public String hint;
        private String text;
        private int valueType;

        public TextInput(String hint) {
            this(hint, -1);
        }
        public TextInput(String hint, int valueType) {
            this.inputType = InputType.text;
            this.valueType = valueType;
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
                valuesChanged();
        }
        public int getValueType() {
            return valueType;
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
                valuesChanged();
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
                valuesChanged();
        }
        public String getLabel() {
            return label;
        }
    }
    public static class ToggleInput extends DynamicInput {
        private String onLabel;
        private String offLabel;
        private boolean onOff;
        private View.OnClickListener onClick;

        public ToggleInput(String onLabel, String offLabel, View.OnClickListener onClick) {
            this.inputType = InputType.toggle;
            this.onLabel = onLabel;
            this.offLabel = offLabel;
            this.onClick = onClick;
        }

        public void setOnLabel(String onLabel, boolean fireEvent) {
            this.onLabel = onLabel;
            if (fireEvent)
                valuesChanged();
        }
        public String getOnLabel() {
            return onLabel;
        }
        public void setOffLabel(String offLabel, boolean fireEvent) {
            this.offLabel = offLabel;
            if(fireEvent)
                valuesChanged();
        }
        public String getOffLabel() {
            return offLabel;
        }
        public boolean getOnOff() {
            return onOff;
        }
        public void setOnOff(boolean onOff, boolean fireEvent) {
            this.onOff = onOff;
            if (fireEvent)
                valuesChanged();
        }
        public View.OnClickListener getOnClick() {
            return onClick;
        }
    }
}
