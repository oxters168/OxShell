package com.OxGames.OxShell.Views;

import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.util.Log;
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
    private DynamicInputRow rowItems;
    private Context context;
    private RecyclerView row;

    private boolean queuedFocus;
    private int queuedFocusPosition;

    private InputRowAdapter adapter;
    int rowWidth;

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
        row = new RecyclerView(context);
        row.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        row.setLayoutManager(new LinearLayoutManager(context, RecyclerView.HORIZONTAL, false));
        row.setVisibility(VISIBLE);
        row.setFocusable(false);
        addView(row);
    }
    public void setInputItems(DynamicInputRow items) {
        rowItems = items;
        adapter = new InputRowAdapter(context, rowItems.getAll());
        adapter.addListener(() -> {
            if (queuedFocus) {
                queuedFocus = false;
                requestFocusOnItem(queuedFocusPosition);
            }
        });

        recalculateRowWidth();
        //adapter.setParent(this);
        row.setAdapter(adapter);
    }
    public RecyclerView getRow() {
        return row;
    }

    private void recalculateRowWidth() {
        // this assumes the dynamic input view is taking up the whole screen
        int dip = Math.round(AndroidHelpers.getScaledDpToPixels(context, 40)); // TODO: change hardcoded dip value to properly reflect padding of parent recycler
        rowWidth = OxShellApp.getDisplayWidth() - dip;
        //measure(0, 0);
        //rowWidth = getMeasuredWidth();
        if (adapter != null)
            adapter.setRowWidth(rowWidth);
    }
    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        //Log.d("DynamicInputRowView", "onConfigurationChanged");
        super.onConfigurationChanged(newConfig);
        recalculateRowWidth();
        if (adapter != null)
            adapter.refreshItems();
    }

    public void unsafeRequestFocusOnItem(int position) {
        boolean success = ((InputRowAdapter.RowViewHolder)row.findViewHolderForAdapterPosition(position)).requestFocus();
        //Log.d("DynamicInputRowView", "Setting focus on " + position + " success: " + success);
    }
    public void requestFocusOnItem(int position) {
        //Log.d("DynamicInputRowView", "Attempting to focus on " + position);
        RecyclerView.ViewHolder holder = row.findViewHolderForAdapterPosition(position);
        if (holder != null)
            unsafeRequestFocusOnItem(position);
        else
            queueRequestFocusOnItem(position);
    }
    public void queueRequestFocusOnItem(int position) {
        queuedFocusPosition = position;
        queuedFocus = true;
    }
}
