package com.OxGames.OxShell.Views;

import android.content.Context;
import android.graphics.Color;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.OxGames.OxShell.Data.DynamicInputRow;
import com.OxGames.OxShell.Helpers.AndroidHelpers;
import com.OxGames.OxShell.Interfaces.DynamicInputListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class DynamicInputItemView extends FrameLayout {
    private Context context;
    private DynamicInputRow.DynamicInput inputItem;
    private DynamicInputListener itemListener;

    private TextWatcher inputWatcher;
    private TextInputLayout inputLayout;
    private Button button;
    private TextView label;


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

    public void setInputItem(DynamicInputRow.DynamicInput item) {
        // remove previous listeners from the views if any
        if (inputLayout != null && inputLayout.getEditText() != null && inputWatcher != null) {
            inputLayout.getEditText().removeTextChangedListener(inputWatcher);
            //inputLayout.getEditText().setOnFocusChangeListener(null);
        }
        if (button != null) {
            button.setOnClickListener(null);
            //button.setOnFocusChangeListener(null);
        }
//        if (label != null)
//            label.setOnFocusChangeListener(null);

        // hide all views that exist
        if (inputLayout != null)
            inputLayout.setVisibility(GONE);
        if (button != null)
            button.setVisibility(GONE);
        if (label != null)
            label.setVisibility(GONE);

        // remove the previous item's listener
        if (inputItem != null && itemListener != null)
            inputItem.removeListener(itemListener);
        // set up the current item's listener to change the view when its values are changed
        itemListener = new DynamicInputListener() {
            @Override
            public void onFocusChanged(View view, boolean hasFocus) {

            }

            @Override
            public void onValuesChanged() {
                if (item.inputType == DynamicInputRow.DynamicInput.InputType.text) {
                    if (inputLayout != null) {
                        EditText textEdit = inputLayout.getEditText();
                        if (textEdit != null)
                            textEdit.setText(((DynamicInputRow.TextInput)item).getText());
                    }
                }
                if (item.inputType == DynamicInputRow.DynamicInput.InputType.button) {
                    if (button != null) {
                        button.setText(((DynamicInputRow.ButtonInput)item).getLabel());
                    }
                }
                if (item.inputType == DynamicInputRow.DynamicInput.InputType.label) {
                    if (label != null) {
                        label.setText(((DynamicInputRow.Label)item).getLabel());
                    }
                }
            }
        };
        item.addListener(itemListener);

        if (item.inputType == DynamicInputRow.DynamicInput.InputType.text) {
            DynamicInputRow.TextInput innerItem = (DynamicInputRow.TextInput)item;
            if (inputLayout == null) {
                inputLayout = new TextInputLayout(context);
                LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                layoutParams.gravity = Gravity.START | Gravity.CENTER_VERTICAL;
                inputLayout.setLayoutParams(layoutParams);
                inputLayout.setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_NONE);
                inputLayout.setBackgroundColor(Color.parseColor("#232323"));
                addView(inputLayout);
            }
            inputLayout.setHint(innerItem.hint);
            // if the edit text does not exist, create it
            EditText textEdit = inputLayout.getEditText();
            if (textEdit == null) {
                textEdit = new TextInputEditText(context);
                inputLayout.addView(textEdit, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            }
            // set the edit text to fire onFocusChange on the current item
            textEdit.setOnFocusChangeListener(innerItem::onFocusChange);
            // set the starting value of the view to what the item already had
            textEdit.setText(innerItem.getText());
            // update text value of the item this view currently represents based on user changes
            inputWatcher = new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    innerItem.setText(s.toString(), false);
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            };
            textEdit.addTextChangedListener(inputWatcher);
            // make the view visible
            inputLayout.setVisibility(VISIBLE);
        } else if (item.inputType == DynamicInputRow.DynamicInput.InputType.button) {
            DynamicInputRow.ButtonInput innerItem = (DynamicInputRow.ButtonInput)item;
            if (button == null) {
                button = new Button(context);
                LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                params.gravity = Gravity.START | Gravity.CENTER_VERTICAL;
                button.setLayoutParams(params);
                addView(button);
            }
            button.setOnFocusChangeListener(innerItem::onFocusChange);
            button.setText(innerItem.getLabel());
            if (innerItem.getOnClick() != null)
                button.setOnClickListener(innerItem.getOnClick());
            button.setVisibility(VISIBLE);
        } else if (item.inputType == DynamicInputRow.DynamicInput.InputType.label) {
            DynamicInputRow.Label innerItem = (DynamicInputRow.Label)item;
            if (label == null) {
                label = new TextView(context);
                LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                params.gravity = Gravity.START | Gravity.CENTER_VERTICAL;
                label.setLayoutParams(params);
                addView(label);
                label.setOnFocusChangeListener(innerItem::onFocusChange);
            }
            label.setText(innerItem.getLabel());
            label.setVisibility(VISIBLE);
        }

        inputItem = item;
    }
}
