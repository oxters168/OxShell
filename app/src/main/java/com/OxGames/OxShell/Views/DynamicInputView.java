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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.OxGames.OxShell.Adapters.DynamicInputAdapter;
import com.OxGames.OxShell.Data.DynamicInputItem;
import com.OxGames.OxShell.Helpers.ActivityManager;
import com.OxGames.OxShell.Helpers.AndroidHelpers;
import com.OxGames.OxShell.PagedActivity;
import com.OxGames.OxShell.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class DynamicInputView extends FrameLayout {
    private boolean isShown = false;
    private final Context context;
    private TextView title;
    //private ListView mainList;
    private RecyclerView mainList;
    private int prevUIState;

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

        mainList = new RecyclerView(context);
        RecyclerView.LayoutParams recyclerParams = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        dip = Math.round(AndroidHelpers.dipToPixels(context, 40));
        recyclerParams.topMargin = dip;
        recyclerParams.bottomMargin = dip;
        mainList.setLayoutParams(recyclerParams);
        dip = Math.round(AndroidHelpers.dipToPixels(context, 20));
        mainList.setPadding(dip, dip, dip, dip);
        mainList.setBackgroundColor(Color.parseColor("#323232"));
        mainList.setLayoutManager(new LinearLayoutManager(context));
        mainList.setVisibility(VISIBLE);
//        mainList = new ListView(context);
//        layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
//        dip = Math.round(AndroidHelpers.dipToPixels(context, 40));
//        layoutParams.topMargin = dip;
//        layoutParams.bottomMargin = dip;
//        mainList.setLayoutParams(layoutParams);
//        dip = Math.round(AndroidHelpers.dipToPixels(context, 20));
//        mainList.setPadding(dip, dip, dip, dip);
//        mainList.setBackgroundColor(Color.parseColor("#323232"));
//        mainList.setDivider(null);
        //mainList.setDividerHeight(Math.round(AndroidHelpers.dipToPixels(context, 16)));
        //mainList.setDividerHeight(16);
        addView(mainList);

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
    public void setItems(DynamicInputItem... items) {
        DynamicInputAdapter adapter = new DynamicInputAdapter(context, items);
        mainList.setAdapter(adapter);
    }

    public boolean isInputShown() {
        return isShown;
    }
    public void setShown(boolean onOff) {
        isShown = onOff;
        setVisibility(onOff ? VISIBLE : GONE);
        PagedActivity current = ActivityManager.getCurrentActivity();
        if (onOff) {
            prevUIState = current.getSystemUIState();
            current.setNavBarHidden(true);
            current.setStatusBarHidden(true);
        } else {
            current.setSystemUIState(prevUIState);
            if (mainList != null) {
                RecyclerView.Adapter adapter = mainList.getAdapter();
                if (adapter != null)
                    ((DynamicInputAdapter)adapter).clear();
            }
        }
    }
}
