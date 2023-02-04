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
import android.view.inputmethod.CompletionInfo;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.OxGames.OxShell.Adapters.DynamicInputAdapter;
import com.OxGames.OxShell.Data.DynamicInputItem;
import com.OxGames.OxShell.Helpers.AndroidHelpers;
import com.OxGames.OxShell.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class DynamicInputView extends FrameLayout {
    private boolean isShown = false;
    private final Context context;
    private TextView title;

    public DynamicInputView(@NonNull Context context) {
        super(context);
        this.context = context;
        init();
    }
    public DynamicInputView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
    }
    public DynamicInputView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init();
    }
    public DynamicInputView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.context = context;
        init();
    }

    private void init() {
        setShown(false);
        setClickable(true); // block out touch input to views behind
        //setBackgroundColor(Color.parseColor("#80323232"));

        LayoutParams layoutParams;

        FrameLayout header = new FrameLayout(context);
        layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Math.round(AndroidHelpers.dipToPixels(context, 40)));
        layoutParams.gravity = Gravity.TOP;
        header.setLayoutParams(layoutParams);
        header.setBackgroundColor(Color.parseColor("#232323"));
        addView(header);

        title = new TextView(context);
        layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        title.setLayoutParams(layoutParams);
        title.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        title.setAutoSizeTextTypeWithDefaults(TextView.AUTO_SIZE_TEXT_TYPE_UNIFORM);
        int dip = Math.round(AndroidHelpers.dipToPixels(context, 8));
        title.setPadding(dip, dip, dip, dip);
        title.setTextAlignment(TEXT_ALIGNMENT_GRAVITY);
        header.addView(title);

        ListView mainList = new ListView(context);
        layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        dip = Math.round(AndroidHelpers.dipToPixels(context, 40));
        layoutParams.topMargin = dip;
        layoutParams.bottomMargin = dip;
        mainList.setLayoutParams(layoutParams);
        dip = Math.round(AndroidHelpers.dipToPixels(context, 20));
        mainList.setPadding(dip, dip, dip, dip);
        mainList.setBackgroundColor(Color.parseColor("#323232"));
        addView(mainList);
        ListAdapter adapter = new DynamicInputAdapter(context,
                new DynamicInputItem("Folder Name", new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                        Log.d("DynamicInputAdapter", "beforeTextChanged " + s);
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        Log.d("DynamicInputAdapter", "onTextChanged " + s);
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        Log.d("DynamicInputAdapter", "afterTextChanged " + s);
                    }
                }),
                new DynamicInputItem("Folder Location", new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                        Log.d("DynamicInputAdapter", "beforeTextChanged " + s);
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        Log.d("DynamicInputAdapter", "onTextChanged " + s);
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        Log.d("DynamicInputAdapter", "afterTextChanged " + s);
                    }
                }),
                new DynamicInputItem("Folder df", null),
                new DynamicInputItem("Folder Nameqwe", null),
                new DynamicInputItem("Folder ergr", null),
                new DynamicInputItem("Folder adsfasdf", null),
                new DynamicInputItem("Folder cvcvb", null),
                new DynamicInputItem("Folder hjgfj", null),
                new DynamicInputItem("Folder uiuyt", null),
                new DynamicInputItem("Folder 56786", null),
                new DynamicInputItem("Folder nvx", null),
                new DynamicInputItem("Folder eyterg", null));
        mainList.setAdapter(adapter);

        FrameLayout footer = new FrameLayout(context);
        layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Math.round(AndroidHelpers.dipToPixels(context, 40)));
        layoutParams.gravity = Gravity.BOTTOM;
        footer.setLayoutParams(layoutParams);
        footer.setBackgroundColor(Color.parseColor("#232323"));
        addView(footer);
    }

    public void setTitle(String value) {
        title.setText(value);
    }
    public boolean isInputShown() {
        return isShown;
    }
    public void setShown(boolean onOff) {
        isShown = onOff;
        setVisibility(onOff ? VISIBLE : GONE);
    }
}
