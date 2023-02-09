package com.OxGames.OxShell.Views;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.OxGames.OxShell.Adapters.DynamicInputAdapter;
import com.OxGames.OxShell.Data.DynamicInputRow;
import com.OxGames.OxShell.Helpers.ActivityManager;
import com.OxGames.OxShell.Helpers.AndroidHelpers;
import com.OxGames.OxShell.Interfaces.AdapterListener;
import com.OxGames.OxShell.Interfaces.InputReceiver;
import com.OxGames.OxShell.PagedActivity;

public class DynamicInputView extends FrameLayout implements InputReceiver {
    private boolean isShown = false;
    private final Context context;
    private TextView title;
    //private ListView mainList;
    private RecyclerView mainList;
    private int prevUIState;

    private DynamicInputRow[] rows;
    // TODO: change rowIndex and colIndex when an item gets focus from touch
    private int rowIndex = 0;
    private int colIndex = 0;
    private boolean queuedCol = false;
    private int queuedColIndex = 0;

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
    public void setItems(DynamicInputRow... items) {
        if (mainList.getAdapter() != null)
            ((DynamicInputAdapter)mainList.getAdapter()).clearListeners();
        DynamicInputAdapter adapter = new DynamicInputAdapter(context, items);
        adapter.addListener(new AdapterListener() {
            @Override
            public void onViewsReady() {
                if (queuedCol) {
                    unsafeSetColIndex(rowIndex, queuedColIndex);
                    queuedCol = false;
                }
            }
        });
        mainList.setAdapter(adapter);
        rows = items;
    }

    public boolean isOverlayShown() {
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
            rowIndex = 0;
            colIndex = 0;
        } else {
            current.setSystemUIState(prevUIState);
            if (mainList != null) {
                RecyclerView.Adapter adapter = mainList.getAdapter();
                if (adapter != null)
                    ((DynamicInputAdapter)adapter).clear();
            }
        }
    }

    private void unsafeSetColIndex(int rowIndex, int index) {
        RecyclerView row = ((DynamicInputRowView)mainList.getChildAt(rowIndex)).getRow();
        row.scrollToPosition(index);
        colIndex = index;
    }
    private void setColIndex(int index) {
        //Log.d("DynamicInputView", "Scrolling from " + colIndex + " to " + index + " on " + rowIndex);
        View rowView = mainList.getChildAt(rowIndex);
        if (rowView != null)
            unsafeSetColIndex(rowIndex, index);
        else
            queueColIndex(index);
    }
    private void queueColIndex(int colIndex) {
        queuedColIndex = colIndex;
        queuedCol = true;
    }

    @Override
    public boolean receiveKeyEvent(KeyEvent key_event) {
        if (key_event.getAction() == KeyEvent.ACTION_DOWN) {
            if (key_event.getKeyCode() == KeyEvent.KEYCODE_DPAD_DOWN) {
                int nextRowIndex = rowIndex;
                for (int i = rowIndex + 1; i < rows.length; i++) {
                    DynamicInputRow.DynamicInput[] inputs = rows[i].getAll();
                    for (DynamicInputRow.DynamicInput input : inputs) {
                        if (input.inputType != DynamicInputRow.DynamicInput.InputType.label) {
                            nextRowIndex = i;
                            break;
                        }
                    }
                    if (nextRowIndex != rowIndex)
                        break;
                }
                if (nextRowIndex != rowIndex) {
                    //Log.d("DynamicInputView", "Scrolling from " + rowIndex + " to " + nextRowIndex);
                    mainList.scrollToPosition(nextRowIndex);
                    setColIndex(0);
                    rowIndex = nextRowIndex;
                }
                return true;
            }
            if (key_event.getKeyCode() == KeyEvent.KEYCODE_DPAD_UP) {
                int nextRowIndex = rowIndex;
                for (int i = rowIndex - 1; i >= 0; i--) {
                    DynamicInputRow.DynamicInput[] inputs = rows[i].getAll();
                    for (DynamicInputRow.DynamicInput input : inputs) {
                        if (input.inputType != DynamicInputRow.DynamicInput.InputType.label) {
                            nextRowIndex = i;
                            break;
                        }
                    }
                    if (nextRowIndex != rowIndex)
                        break;
                }
                if (nextRowIndex != rowIndex) {
                    //Log.d("DynamicInputView", "Scrolling from " + rowIndex + " to " + nextRowIndex);
                    mainList.scrollToPosition(nextRowIndex);
                    setColIndex(0);
                    rowIndex = nextRowIndex;
                }
                return true;
            }
            if (key_event.getKeyCode() == KeyEvent.KEYCODE_DPAD_LEFT) {
                DynamicInputRow.DynamicInput[] rowItems = rows[rowIndex].getAll();
                int nextColIndex = colIndex;
                for (int i = colIndex - 1; i >= 0; i--) {
                    if (rowItems[i].inputType != DynamicInputRow.DynamicInput.InputType.label) {
                        nextColIndex = i;
                        break;
                    }
                }
                if (nextColIndex != colIndex) {
                    setColIndex(nextColIndex);
                }
                return true;
            }
            if (key_event.getKeyCode() == KeyEvent.KEYCODE_DPAD_RIGHT) {
                DynamicInputRow.DynamicInput[] rowItems = rows[rowIndex].getAll();
                int nextColIndex = colIndex;
                for (int i = colIndex + 1; i < rowItems.length; i++) {
                    if (rowItems[i].inputType != DynamicInputRow.DynamicInput.InputType.label) {
                        nextColIndex = i;
                        break;
                    }
                }
                if (nextColIndex != colIndex) {
                    setColIndex(nextColIndex);
                }
                return true;
            }
        }
        return false;
    }
}
