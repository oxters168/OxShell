package com.OxGames.OxShell.Views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.OxGames.OxShell.Data.DynamicInputRow;
import com.OxGames.OxShell.Data.SettingsKeeper;
import com.OxGames.OxShell.Helpers.AndroidHelpers;
import com.OxGames.OxShell.OxShellApp;
import com.OxGames.OxShell.R;
import com.google.android.material.slider.Slider;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.function.Consumer;

public class DynamicInputItemView extends FrameLayout {
    private Context context;
    private DynamicInputRow.DynamicInput inputItem;
    private Consumer<DynamicInputRow.DynamicInput> valuesChangedListener;

    private Slider.OnSliderTouchListener sliderTouchListener;
    private TextWatcher inputWatcher;
    private TextInputLayout inputLayout;
    private Button button;
    private TextView label;
    private CheckBox toggle;
    private Spinner dropdown;
    private ImageView image;
    private Slider slider;


    public DynamicInputItemView(@NonNull Context context) {
        super(context);
        this.context = context;
    }

    public DynamicInputItemView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    public DynamicInputItemView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
    }

    public DynamicInputItemView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.context = context;
    }

    public DynamicInputRow.DynamicInput getInputItem() {
        return inputItem;
    }
//    public void refreshSelection(boolean onOff) {
//        if (button != null)
//            button.setBackgroundTintList(ColorStateList.valueOf(onOff ? Color.parseColor("#88CEEAF0") : Color.parseColor("#88323232")));
//        if (dropdown != null)
//            dropdown.setBackgroundTintList(onOff ? ColorStateList.valueOf(Color.parseColor("#88CEEAF0")) : null);
//    }
    public int getItemHeight() {
        return Math.round(AndroidHelpers.getScaledDpToPixels(context, 48));
    }
    private int getTextSize() {
        return Math.round(AndroidHelpers.getScaledSpToPixels(context, 8));
    }
    private int getBtnTextSize() {
        return Math.round(AndroidHelpers.getScaledSpToPixels(context, 5));
    }
    public void clickItem() {
        if (button != null && button.getVisibility() == VISIBLE)
            button.performClick();
        if (dropdown != null && dropdown.getVisibility() == VISIBLE)
            dropdown.performClick();
        if (inputLayout != null && inputLayout.getVisibility() == VISIBLE) {
            InputMethodManager imm = (InputMethodManager)OxShellApp.getCurrentActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(inputLayout.getEditText(), InputMethodManager.SHOW_IMPLICIT);
        }
        if (toggle != null &&  toggle.getVisibility() == VISIBLE)
            toggle.performClick();
    }
    @SuppressLint("ClickableViewAccessibility")
    public void setInputItem(DynamicInputRow.DynamicInput item) {
        item.view = this;

        // remove previous listeners from the views if any
        if (inputLayout != null && inputLayout.getEditText() != null) {
            EditText editText = inputLayout.getEditText();
            editText.setOnFocusChangeListener(null);
            if (inputWatcher != null)
                editText.removeTextChangedListener(inputWatcher);
        }
        if (button != null) {
            button.setOnFocusChangeListener(null);
            button.setOnClickListener(null);
            button.setOnTouchListener(null);
        }
        if (toggle != null) {
            //toggle.setOnFocusChangeListener(null);
            toggle.setOnClickListener(null);
        }
        if (dropdown != null) {
            dropdown.setOnFocusChangeListener(null);
            dropdown.setOnItemSelectedListener(null);
            dropdown.setOnTouchListener(null);
        }
        if (slider != null) {
            if (sliderTouchListener != null)
                slider.removeOnSliderTouchListener(sliderTouchListener);
            //slider.setOnFocusChangeListener(null);
            //slider.setOnClickListener(null);
            //slider.setOnTouchListener(null);
        }

        // hide all views that exist
        if (inputLayout != null)
            inputLayout.setVisibility(GONE);
        if (button != null)
            button.setVisibility(GONE);
        if (label != null)
            label.setVisibility(GONE);
        if (toggle != null)
            toggle.setVisibility(GONE);
        if (dropdown != null)
            dropdown.setVisibility(GONE);
        if (image != null)
            image.setVisibility(GONE);
        if (slider != null)
            slider.setVisibility(GONE);

        // remove the previous item's listener
        if (inputItem != null && valuesChangedListener != null)
            inputItem.removeValuesChangedListener(valuesChangedListener);
        // set up the current item's listener to change the view when its values are changed
        valuesChangedListener = self -> {
                if (inputLayout != null) {
                    if (item.inputType == DynamicInputRow.DynamicInput.InputType.text) {
                        EditText textEdit = inputLayout.getEditText();
                        if (textEdit != null) {
                            String newText = ((DynamicInputRow.TextInput)item).getText();
                            if (!textEdit.getText().toString().equals(newText)) {
                            //    Log.d("DynamicInputItemView", "Setting edit text from " + textEdit.getText() + " to " + newText);
                                textEdit.setText(newText);
                            }
                        }
                        inputLayout.setVisibility(item.getVisibility());
                        inputLayout.setEnabled(item.isEnabled());
                    }
                }
                if (button != null) {
                    if (item.inputType == DynamicInputRow.DynamicInput.InputType.button) {
                        button.setText(((DynamicInputRow.ButtonInput)item).getLabel());
                        button.setVisibility(item.getVisibility());
                        button.setEnabled(item.isEnabled());
                    }
                }
                if (toggle != null) {
                    if (item.inputType == DynamicInputRow.DynamicInput.InputType.toggle) {
                        DynamicInputRow.ToggleInput innerItem = (DynamicInputRow.ToggleInput)item;
                        toggle.setText(innerItem.getOnOff() ? innerItem.getOnLabel() : innerItem.getOffLabel());
                        toggle.setVisibility(item.getVisibility());
                        toggle.setEnabled(item.isEnabled());
                    }
                }
                if (dropdown != null) {
                    if (item.inputType == DynamicInputRow.DynamicInput.InputType.dropdown) {
                        DynamicInputRow.Dropdown innerItem = (DynamicInputRow.Dropdown)item;
                        dropdown.setAdapter(new DropdownAdapter(innerItem.getOptions()));
                        // getSelectedItemPosition was always returning 0
                        //if (dropdown.getSelectedItemPosition() != innerItem.getIndex()) {
                        //    Log.d("DynamicInputItemView", dropdown.getSelectedItemPosition() + " != " + innerItem.getIndex());
                        if (innerItem.getCount() > 0)
                            dropdown.setSelection(innerItem.getIndex());
                        //}
                        dropdown.setVisibility(item.getVisibility());
                        dropdown.setEnabled(item.isEnabled());
                    }
                }
                if (image != null) {
                    if (item.inputType == DynamicInputRow.DynamicInput.InputType.image) {
                        ((DynamicInputRow.ImageDisplay)item).getImage(img -> image.setImageDrawable(img));
                        image.setVisibility(item.getVisibility());
                        image.setEnabled(item.isEnabled());
                    }
                }
                if (label != null) {
                    if (item.inputType == DynamicInputRow.DynamicInput.InputType.label) {
                        DynamicInputRow.Label innerItem = (DynamicInputRow.Label)item;
                        label.setText(innerItem.getLabel());
                        label.setGravity(innerItem.getGravity());
                        label.setVisibility(item.getVisibility());
                        label.setEnabled(item.isEnabled());
                    }
                }
                if (slider != null) {
                    if (item.inputType == DynamicInputRow.DynamicInput.InputType.slider) {
                        DynamicInputRow.SliderInput innerItem = (DynamicInputRow.SliderInput)item;
                        slider.setValueFrom(innerItem.getValueFrom());
                        slider.setValueTo(innerItem.getValueTo());
                        slider.setValue(innerItem.getValue());
                        slider.setStepSize(innerItem.getStepSize());
                    }
                }
        };
        item.addValuesChangedListener(valuesChangedListener);

        int itemHeight = getItemHeight();
        Typeface font = SettingsKeeper.getFont();
        if (item.inputType == DynamicInputRow.DynamicInput.InputType.text) {
            DynamicInputRow.TextInput innerItem = (DynamicInputRow.TextInput)item;
            if (inputLayout == null) {
                inputLayout = new TextInputLayout(context);
                LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                layoutParams.gravity = Gravity.START | Gravity.CENTER_VERTICAL;
                inputLayout.setLayoutParams(layoutParams);
                inputLayout.setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_NONE);
                inputLayout.setBackground(ContextCompat.getDrawable(context, R.drawable.rounded_outline_shape));
                inputLayout.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#44323232")));
                addView(inputLayout);
            }
            inputLayout.setHint(innerItem.hint);
            // if the edit text does not exist, create it
            EditText textEdit = inputLayout.getEditText();
            if (textEdit == null) {
                textEdit = new TextInputEditText(context);
                inputLayout.addView(textEdit, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, itemHeight));
            }
            inputLayout.setTypeface(font);
            // sets the input type of the edit text (number/password/email/etc)
            if (innerItem.getValueType() >= 0)
                textEdit.setInputType(innerItem.getValueType());
            // set the edit text to fire onFocusChange on the current item
            textEdit.setOnFocusChangeListener((view, hasFocus) -> {
                //Log.d("DynamicInputItemView", "onFocusChange [" + inputItem.row + ", " + inputItem.col + "] hasFocus: " + hasFocus);
                innerItem.onFocusChange(hasFocus);
            });
            //textEdit.setImeOptions(EditorInfo.IME_ACTION_NEXT);
            // set the starting value of the view to what the item already had
            textEdit.setText(innerItem.getText());
            textEdit.setTextSize(getTextSize());
            textEdit.setTextColor(Color.WHITE);
            textEdit.setTypeface(font);
            //textEdit.bringToFront();
            // update text value of the item this view currently represents based on user changes
            inputWatcher = new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    innerItem.setText(s.toString());
                }

                @Override
                public void afterTextChanged(Editable s) {
                    //innerItem.setText(s.toString());
                }
            };
            textEdit.addTextChangedListener(inputWatcher);
            inputLayout.setHintTextColor(ColorStateList.valueOf(Color.WHITE));            //inputLayout.setHintEnabled(false);
            //inputLayout.setExpandedHintEnabled(false);
            //inputLayout.setHintEnabled(false);
            inputLayout.setDefaultHintTextColor(ColorStateList.valueOf(Color.WHITE));

            inputLayout.setVisibility(innerItem.getVisibility());
        } else if (item.inputType == DynamicInputRow.DynamicInput.InputType.button) {
            DynamicInputRow.ButtonInput innerItem = (DynamicInputRow.ButtonInput)item;
            if (button == null) {
                button = new Button(context);
                LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, itemHeight);
                params.gravity = Gravity.START | Gravity.CENTER_VERTICAL;
                button.setLayoutParams(params);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
                    button.setOutlineSpotShadowColor(Color.TRANSPARENT);
                button.setBackground(AndroidHelpers.createStateListDrawable(ContextCompat.getDrawable(context, R.drawable.rounded_outline_shape)));
                button.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#88323232")));
                addView(button);
            }
            // highlight button when pressed by touch
            button.setOnTouchListener((view, event) -> {
                boolean isDown = event.getAction() == KeyEvent.ACTION_DOWN;
                button.setBackgroundTintList(ColorStateList.valueOf((isDown || button.hasFocus()) ? Color.parseColor("#88CEEAF0") : Color.parseColor("#88323232")));
                return false;
            });
            // highlight button when has focus
            button.setOnFocusChangeListener((view, hasFocus) -> {
                //Log.d("DynamicInputItemView", "onFocusChange [" + inputItem.row + ", " + inputItem.col + "] hasFocus: " + hasFocus);
                button.setBackgroundTintList(ColorStateList.valueOf((hasFocus || button.isPressed()) ? Color.parseColor("#88CEEAF0") : Color.parseColor("#88323232")));
                innerItem.onFocusChange(hasFocus);
            });
            //button.setImeOptions(EditorInfo.IME_ACTION_NEXT);
            button.setText(innerItem.getLabel());
            button.setTextSize(getTextSize());
            button.setAllCaps(false);
            button.setTypeface(font);
            button.setTextColor(Color.WHITE);
            if (innerItem.getOnClick() != null)
                button.setOnClickListener(v -> innerItem.getOnClick().accept(innerItem));
            button.setVisibility(innerItem.getVisibility());
            button.setEnabled(innerItem.isEnabled());
        } else if (item.inputType == DynamicInputRow.DynamicInput.InputType.toggle) {
            DynamicInputRow.ToggleInput innerItem = (DynamicInputRow.ToggleInput)item;
            if (toggle == null) {
                toggle = new CheckBox(context);
                LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, itemHeight);
                params.gravity = Gravity.START | Gravity.CENTER_VERTICAL;
                toggle.setLayoutParams(params);
                toggle.setFocusable(true);
                toggle.setFocusableInTouchMode(true);
                addView(toggle);
            }
            //toggle.setOnFocusChangeListener(innerItem::onFocusChange);
            //toggle.setImeOptions(EditorInfo.IME_ACTION_NEXT);
            toggle.setText(innerItem.getOnOff() ? innerItem.getOnLabel() : innerItem.getOffLabel());
            toggle.setTextSize(getTextSize());
            toggle.setTextColor(Color.WHITE);
            toggle.setTypeface(font);
            toggle.setChecked(innerItem.getOnOff());
            toggle.setOnClickListener((view) -> {
                innerItem.setOnOff(toggle.isChecked(), true);
                toggle.setText(innerItem.getOnOff() ? innerItem.getOnLabel() : innerItem.getOffLabel());
                if (innerItem.getOnClick() != null)
                    innerItem.getOnClick().accept(innerItem);
            });
            toggle.setVisibility(innerItem.getVisibility());
            toggle.setEnabled(innerItem.isEnabled());
        } else if (item.inputType == DynamicInputRow.DynamicInput.InputType.dropdown) {
            DynamicInputRow.Dropdown innerItem = (DynamicInputRow.Dropdown)item;
            if (dropdown == null) {
                // create a Spinner widget
                dropdown = new Spinner(context);
                LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, itemHeight);
                params.gravity = Gravity.START | Gravity.CENTER_VERTICAL;
                dropdown.setLayoutParams(params);
                dropdown.setBackground(AndroidHelpers.createStateListDrawable(ContextCompat.getDrawable(context, R.drawable.rounded_spinner)));
                //dropdown.setBackground(ContextCompat.getDrawable(context, R.drawable.rounded_outline_shape));
                //dropdown.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#44323232")));
                // I don't actually know what this is for, couldn't get it to do anything, but I'm keeping it anyways just in case
                dropdown.setPrompt("");
                // add the Spinner widget to your layout
                addView(dropdown);
            }
            dropdown.setOnFocusChangeListener((view, hasFocus) -> {
                dropdown.setBackgroundTintList((hasFocus || dropdown.isPressed()) ? ColorStateList.valueOf(Color.parseColor("#88CEEAF0")) : null);
            });
            dropdown.setOnTouchListener((view, event) -> {
                boolean isDown = event.getAction() == KeyEvent.ACTION_DOWN;
                dropdown.setBackgroundTintList((isDown || dropdown.hasFocus()) ? ColorStateList.valueOf(Color.parseColor("#88CEEAF0")) : null);
                return false;
            });
            dropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    //Log.d("DynamicInputItemView", "Raw index changed " + position);
                    innerItem.setIndex(position);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });
            dropdown.setPopupBackgroundDrawable(new ColorDrawable(Color.DKGRAY));
            dropdown.setAdapter(new DropdownAdapter(innerItem.getOptions()));
            if (innerItem.getCount() >= 0)
                dropdown.setSelection(innerItem.getIndex());
            dropdown.setVisibility(innerItem.getVisibility());
            dropdown.setEnabled(innerItem.isEnabled());
        } else if (item.inputType == DynamicInputRow.DynamicInput.InputType.slider) {
            DynamicInputRow.SliderInput innerItem = (DynamicInputRow.SliderInput)item;
            if (slider == null) {
                // create a Spinner widget
                slider = new Slider(context);
                LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, itemHeight);
                params.gravity = Gravity.START | Gravity.CENTER_VERTICAL;
                slider.setLayoutParams(params);
                slider.setBackground(AndroidHelpers.createStateListDrawable(ContextCompat.getDrawable(context, R.drawable.rounded_outline_shape)));
                slider.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#44323232")));
                //slider.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#88CEEAF0")));
                addView(slider);
            }
            slider.setValueFrom(innerItem.getValueFrom());
            slider.setValueTo(innerItem.getValueTo());
            slider.setValue(innerItem.getValue());
            slider.setStepSize(innerItem.getStepSize());
            sliderTouchListener = new Slider.OnSliderTouchListener() {
                @Override
                public void onStartTrackingTouch(@NonNull Slider slider) {
                    //Log.d("DynamicInputItemView", "onStartTrackingTouch: " + slider.getValue());
                    //innerItem.setValue(slider.getValue());
                }

                @Override
                public void onStopTrackingTouch(@NonNull Slider slider) {
                    //Log.d("DynamicInputItemView", "onStopTrackingTouch: " + slider.getValue());
                    innerItem.setValue(slider.getValue());
                }
            };
            slider.addOnSliderTouchListener(sliderTouchListener);
//            slider.setOnFocusChangeListener((view, hasFocus) -> {
//                slider.setBackgroundTintList((hasFocus || slider.isPressed()) ? ColorStateList.valueOf(Color.parseColor("#88CEEAF0")) : null);
//            });
//            slider.setOnTouchListener((view, event) -> {
//                boolean isDown = event.getAction() == KeyEvent.ACTION_DOWN;
//                slider.setBackgroundTintList((isDown || slider.hasFocus()) ? ColorStateList.valueOf(Color.parseColor("#88CEEAF0")) : null);
//                return false;
//            });
            slider.setVisibility(innerItem.getVisibility());
            slider.setEnabled(innerItem.isEnabled());
        } else if (item.inputType == DynamicInputRow.DynamicInput.InputType.image) {
            DynamicInputRow.ImageDisplay innerItem = (DynamicInputRow.ImageDisplay)item;
            if (image == null) {
                image = new ImageView(context);
                LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, itemHeight);
                params.gravity = Gravity.START | Gravity.CENTER_VERTICAL;
                image.setLayoutParams(params);
                image.setFocusable(false);
                image.setClickable(false);
                addView(image);
            }
            innerItem.getImage(img -> image.setImageDrawable(img));
            image.setVisibility(innerItem.getVisibility());
            image.setEnabled(innerItem.isEnabled());
        } else if (item.inputType == DynamicInputRow.DynamicInput.InputType.label) {
            DynamicInputRow.Label innerItem = (DynamicInputRow.Label)item;
            if (label == null) {
                label = new TextView(context);
                LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params.gravity = Gravity.START | Gravity.CENTER_VERTICAL;
                label.setLayoutParams(params);
                label.setFocusable(false);
                label.setClickable(false);
                addView(label);
            }
            //label.setOnFocusChangeListener(innerItem::onFocusChange);
            //label.setImeOptions(EditorInfo.IME_ACTION_NEXT);
            label.setText(innerItem.getLabel());
            label.setTextSize(getTextSize());
            label.setTextColor(Color.WHITE);
            label.setTextAlignment(TEXT_ALIGNMENT_GRAVITY);
            label.setGravity(innerItem.getGravity());
            label.setTypeface(font);
            //label.setVisibility(VISIBLE);
            label.setVisibility(innerItem.getVisibility());
            label.setEnabled(innerItem.isEnabled());
        }

        inputItem = item;
    }

    private class DropdownAdapter extends BaseAdapter {
        private String[] items;
        private final int TITLE_ID = View.generateViewId();
        public DropdownAdapter(String[] items) {
            this.items = items.clone();
        }

        public int getMarginSize() {
            return Math.round(AndroidHelpers.getScaledDpToPixels(context, 8));
        }

        @Override
        public int getCount() {
            return items.length;
        }
        @Override
        public Object getItem(int position) {
            return position;
        }
        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            FrameLayout view = (FrameLayout)convertView;
            if (view == null) {
                view = new FrameLayout(context);
                LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, getItemHeight());
                view.setLayoutParams(params);
                //view.setBackgroundColor(Color.GREEN);

                TextView label = new TextView(context);
                label.setId(TITLE_ID);
                params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                int mrgn = getMarginSize();
                params.setMargins(mrgn, 0, mrgn, 0);
                params.gravity = Gravity.CENTER;
                label.setLayoutParams(params);
                label.setFocusable(false);
                label.setClickable(false);
                label.setTextSize(getTextSize());
                label.setTextColor(Color.WHITE);
                label.setTextAlignment(TEXT_ALIGNMENT_GRAVITY);
                label.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
                label.setTypeface(SettingsKeeper.getFont());
                view.addView(label);
            }

            TextView label = view.findViewById(TITLE_ID);
            if (label != null)
                label.setText(items[position]);

            return view;
        }
    }
}
