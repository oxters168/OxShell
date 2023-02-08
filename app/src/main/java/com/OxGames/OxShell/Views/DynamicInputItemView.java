package com.OxGames.OxShell.Views;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.OxGames.OxShell.Data.DynamicInputRow;
import com.OxGames.OxShell.Helpers.AndroidHelpers;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class DynamicInputItemView extends FrameLayout {
    private Context context;
    private DynamicInputRow.DynamicInput inputItem;
    private TextInputLayout inputLayout;

    public DynamicInputItemView(@NonNull Context context) {
        super(context);
    }

    public DynamicInputItemView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public DynamicInputItemView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public DynamicInputItemView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void setInputItem(DynamicInputRow.DynamicInput item) {
        //DynamicInputItem.TextInput innerItem = (DynamicInputItem.TextInput)item.get(0);
        DynamicInputRow.TextInput innerItem = (DynamicInputRow.TextInput)item;
        // remove any already existing listeners
        if (inputItem != null) {
            if (inputLayout != null && inputLayout.getEditText() != null && innerItem.getWatcher() != null)
                inputLayout.getEditText().removeTextChangedListener(innerItem.getWatcher());
        }

        if (innerItem.inputType == DynamicInputRow.DynamicInput.InputType.text) {
            if (inputLayout == null) {
                inputLayout = new TextInputLayout(getContext());
                LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                layoutParams.gravity = Gravity.START | Gravity.CENTER_VERTICAL;
                inputLayout.setLayoutParams(layoutParams);
                inputLayout.setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_NONE);
                inputLayout.setBackgroundColor(Color.parseColor("#232323"));
                addView(inputLayout);
            }
            inputLayout.setHint(innerItem.title);
            if (inputLayout.getEditText() == null)
                inputLayout.addView(new TextInputEditText(getContext()), new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            //Log.d("DynamicInputItemView", "editText is null: " + (inputLayout.getEditText() == null) + " watcher is null: " + (item.getWatcher() == null));
            if (innerItem.getWatcher() != null)
                inputLayout.getEditText().addTextChangedListener(innerItem.getWatcher());
        }

        inputItem = item;
    }
    //    public void setPadding(int left, int top, int right, int bottom) {
//        if (inputLayout != null)
//            inputLayout.setPadding(left, top, right, bottom);
//    }
//    public TextInputLayout getInputLayout() {
//        return inputLayout;
//    }
}
