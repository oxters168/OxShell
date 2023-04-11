package com.OxGames.OxShell.Data;

import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.View;

import com.OxGames.OxShell.Views.DynamicInputItemView;
import com.OxGames.OxShell.Views.DynamicInputRowView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class DynamicInputRow {
    public DynamicInputRowView view;
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
            toggle,
            dropdown,
            image,
            slider
        }
        public int row = -1, col = -1;
        public DynamicInputItemView view;
        private int visibility = View.VISIBLE;
        private boolean enabled = true;
//        public void setSelected(boolean onOff) {
//            if (view != null)
//                view.refreshSelection(onOff);
//        }
        public InputType inputType;
        //protected List<DynamicInputListener> listeners = new ArrayList<>();
        protected List<BiConsumer<DynamicInput, Boolean>> focusChangeListeners = new ArrayList<>();
        protected List<Consumer<DynamicInput>> valuesChangedListeners = new ArrayList<>();
//        public void addListener(DynamicInputListener listener) {
//            listeners.add(listener);
//        }
//        public void removeListener(DynamicInputListener listener) {
//            listeners.remove(listener);
//        }
//        public void clearListeners() {
//            listeners.clear();
//        }
        public void addValuesChangedListener(Consumer<DynamicInput> listener) {
            valuesChangedListeners.add(listener);
        }
        public void removeValuesChangedListener(Consumer<DynamicInput> listener) {
            valuesChangedListeners.remove(listener);
        }
        public void clearValuesChangedListeners() {
            valuesChangedListeners.clear();
        }
        public void addFocusChangeListener(BiConsumer<DynamicInput, Boolean> listener) {
            focusChangeListeners.add(listener);
        }
        public void removeFocusChangeListener(BiConsumer<DynamicInput, Boolean> listener) {
            focusChangeListeners.remove(listener);
        }
        public void clearFocusChangeListeners() {
            focusChangeListeners.clear();
        }
        public void onFocusChange(boolean hasFocus) {
            for (BiConsumer<DynamicInput, Boolean> listener : focusChangeListeners)
                if (listener != null)
                    listener.accept(this, hasFocus);
        }
        protected void valuesChanged() {
            for (Consumer<DynamicInput> listener : valuesChangedListeners)
                if (listener != null)
                    listener.accept(this);
        }
        public boolean isEnabled() {
            return enabled;
            //return view != null && view.isEnabled();
        }
        public void setEnabled(boolean onOff) {
            enabled = onOff;
            valuesChanged();
            //if (view != null)
            //    view.setEnabled(onOff);
        }
        public int getVisibility() {
            return visibility;
        }
        public void setVisibility(int value) {
            visibility = value;
            valuesChanged();
            //if (view != null)
            //    view.setVisibility(value);
        }
        public boolean isFocusable() {
            return false;
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

        public void setText(String value) {
            //Log.d("TextInput", "Comparing " + text + " and " + value);
            if (!text.equals(value)) {
                //Log.d("TextInput", "They are different");
                text = value;
                valuesChanged();
            }
            //setText(value, true);fireEvent
        }
        public String getText() {
            return text;
        }
        // setText should be only accessible from the view, but whatever
//        public void setText(String value, boolean fireEvent) {
//            text = value;
//            if (fireEvent)
//                valuesChanged();
//        }
        public int getValueType() {
            return valueType;
        }

        @Override
        public boolean isFocusable() {
            return true;
        }
    }
    public static class ButtonInput extends DynamicInput {
        private String label;
        private Consumer<ButtonInput> onClick;

//        private boolean isKeycodeSet;
//        private int keycode;
        private List<KeyCombo> keyCombos;

        public ButtonInput(String label, Consumer<ButtonInput> onClick) {
            this.inputType = InputType.button;
            this.label = label;
            this.onClick = onClick;
            keyCombos = new ArrayList<>();
        }
        public ButtonInput(String label, Consumer<ButtonInput> onClick, KeyCombo... keyCombos) {
            this(label, onClick);
            Collections.addAll(this.keyCombos, keyCombos);
        }

        public Consumer<ButtonInput> getOnClick() {
            return onClick;
        }
        public void executeAction() {
            if (onClick != null)
                onClick.accept(this);
        }

//        public boolean hasKeycode(int keycode) {
//            return keycodes.contains(keycode);
//        }
        public KeyCombo[] getKeyCombos() {
            return keyCombos.toArray(new KeyCombo[0]);
        }
        public boolean isKeycodeSet() {
            return keyCombos.size() > 0;
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

        @Override
        public boolean isFocusable() {
            return true;
        }
    }
    public static class SliderInput extends DynamicInput {
        private float fromValue;
        private float toValue;
        private float currentValue;
        private float stepSize;
        private Consumer<SliderInput> onValueSet;

        public SliderInput(float fromValue, float toValue, float startValue, float stepSize, Consumer<SliderInput> onValueSet) {
            this.inputType = InputType.slider;
            this.fromValue = fromValue;
            this.toValue = toValue;
            this.currentValue = startValue;
            this.stepSize = stepSize;
            this.onValueSet = onValueSet;
        }

        public float getValueFrom() {
            return fromValue;
        }
        public float getValueTo() {
            return toValue;
        }
        public float getValue() {
            return currentValue;
        }
        public float getStepSize() {
            return stepSize;
        }
        public void setValueFrom(float fromValue) {
            this.fromValue = fromValue;
            valuesChanged();
        }
        public void setValueTo(float toValue) {
            this.toValue = toValue;
            valuesChanged();
        }
        public void setValue(float value) {
            this.currentValue = value;
            if (onValueSet != null)
                onValueSet.accept(this);
            valuesChanged();
        }
        public void setStepSize(float stepSize) {
            this.stepSize = stepSize;
            valuesChanged();
        }

        @Override
        public boolean isFocusable() {
            return true;
        }
    }
    public static class ToggleInput extends DynamicInput {
        private String onLabel;
        private String offLabel;
        private boolean onOff;
        private Consumer<ToggleInput> onClick;

        public ToggleInput(String onLabel, String offLabel, Consumer<ToggleInput> onClick) {
            this.inputType = InputType.toggle;
            this.onLabel = onLabel;
            this.offLabel = offLabel;
            this.onClick = onClick;
        }
        public ToggleInput(String onLabel, String offLabel) {
            this(onLabel, offLabel, null);
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
        public Consumer<ToggleInput> getOnClick() {
            return onClick;
        }

        @Override
        public boolean isFocusable() {
            return true;
        }
    }
    public static class Dropdown extends DynamicInput {
        private String[] options;
        private Consumer<Integer> onItemSelected;
        private int index;

        public Dropdown(Consumer<Integer> onItemSelected, String... options) {
            this.inputType = InputType.dropdown;
            this.onItemSelected = onItemSelected;
            //this.index = -1;
            setOptions(options);
        }

        public int getIndex() {
            return index;
        }
        public void setIndex(int index) {
            if (this.index != index) {
                this.index = index;
                valuesChanged();
                if (onItemSelected != null)
                    onItemSelected.accept(index);
            }
        }
        public void setOptions(String... options) {
            setOptions(true, options);
        }
        private void setOptions(boolean fireEvent, String... options) {
            this.options = options != null ? options.clone() : null;
            this.index = options != null && options.length > 0 ? 0 : -1;
            if (this.onItemSelected != null)
                onItemSelected.accept(this.index);
            if (fireEvent)
                valuesChanged();
        }
        public int getCount() {
            return options != null ? options.length : 0;
        }
        public String[] getOptions() {
            return options != null ? options.clone() : null;
        }
        public String getOption(int index) {
            return options[index];
        }
//        public Consumer<Integer> getOnItemSelected() {
//            return onItemSelected;
//        }

        @Override
        public boolean isFocusable() {
            return true;
        }
    }
    public static class ImageDisplay extends DynamicInput {
        private DataRef img;

        public ImageDisplay(DataRef img) {
            this.inputType = InputType.image;
            this.img = img;
        }

        public void setImage(DataRef img) {
            this.img = img;
            valuesChanged();
        }
        public DataRef getImageRef() {
            return img;
        }
        public Drawable getImage() {
            return img.getImage();
        }
        @Override
        public int getVisibility() {
            return img.isValid() ? super.getVisibility() : View.GONE;
        }
    }
    public static class Label extends DynamicInput {
        private String label;
        private int gravity;

        public Label(String label) {
            this.inputType = InputType.label;
            this.label = label;
            this.gravity = Gravity.LEFT | Gravity.TOP;
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
        public void setGravity(int gravity) {
            this.gravity = gravity;
            valuesChanged();
        }
        public int getGravity() {
            return this.gravity;
        }

        @Override
        public int getVisibility() {
            return label != null && !label.isEmpty() ? super.getVisibility() : View.GONE;
        }
    }
}
