package com.OxGames.OxShell.Views;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.OxGames.OxShell.Adapters.InputRowAdapter;
import com.OxGames.OxShell.Data.DynamicInputRow;
import com.OxGames.OxShell.Helpers.AndroidHelpers;
import com.OxGames.OxShell.OxShellApp;

public class DynamicInputRowView extends FrameLayout {
    //private TextInputLayout inputLayout;
    private DynamicInputRow rowItems;
    private Context context;
    private RecyclerView row;

    public DynamicInputRowView(@NonNull Context context) {
        super(context);
        this.context = context;
        init();
    }
    public DynamicInputRowView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
    }
    public DynamicInputRowView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init();
    }
    public DynamicInputRowView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.context = context;
        init();
    }

    public void init() {
        //inputLayout = findViewById(R.id.input_layout);
        //editText = findViewById(R.id.input_text);
        row = new RecyclerView(context);
        row.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        row.setLayoutManager(new LinearLayoutManager(context, RecyclerView.HORIZONTAL, false));
        row.setVisibility(VISIBLE);
        addView(row);
    }
    public void setInputItems(DynamicInputRow items) {
        rowItems = items;
        InputRowAdapter adapter = new InputRowAdapter(context, rowItems.getAll());
        int dip = Math.round(AndroidHelpers.dipToPixels(context, 40));
        adapter.setRowWidth(OxShellApp.getDisplayWidth() - dip); // TODO: change hardcoded dip value to properly reflect padding of parent recycler
        //adapter.setRowWidth(getMeasuredWidth());
        //adapter.setParent(this);
        row.setAdapter(adapter);
//        DynamicInputItem.TextInput innerItem = (DynamicInputItem.TextInput)item.get(0);
//        Log.d("DynamicInputItemView", "Setting up item " + innerItem.title + " as " + innerItem.inputType);
//        // remove any already existing listeners
//        if (inputItem != null) {
//            if (inputLayout != null && inputLayout.getEditText() != null && innerItem.getWatcher() != null)
//                inputLayout.getEditText().removeTextChangedListener(innerItem.getWatcher());
//        }
//
//        if (innerItem.inputType == DynamicInputItem.DynamicInput.InputType.text) {
//            if (inputLayout == null) {
//                inputLayout = new TextInputLayout(getContext());
//                LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//                layoutParams.gravity = Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL;
//                inputLayout.setLayoutParams(layoutParams);
//                inputLayout.setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_NONE);
//                inputLayout.setBackgroundColor(Color.parseColor("#232323"));
//                addView(inputLayout);
//            }
//            inputLayout.setHint(innerItem.title);
//            if (inputLayout.getEditText() == null)
//                inputLayout.addView(new TextInputEditText(getContext()), new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
//            //Log.d("DynamicInputItemView", "editText is null: " + (inputLayout.getEditText() == null) + " watcher is null: " + (item.getWatcher() == null));
//            if (innerItem.getWatcher() != null)
//                inputLayout.getEditText().addTextChangedListener(innerItem.getWatcher());
//        }
//
//        inputItem = item;
    }
//    public void setPadding(int left, int top, int right, int bottom) {
//        if (inputLayout != null)
//            inputLayout.setPadding(left, top, right, bottom);
//    }
//    public TextInputLayout getInputLayout() {
//        return null;
////        return inputLayout;
//    }
}
