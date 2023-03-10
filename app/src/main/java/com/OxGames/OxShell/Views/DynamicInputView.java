package com.OxGames.OxShell.Views;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
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
import com.OxGames.OxShell.Interfaces.DynamicInputListener;
import com.OxGames.OxShell.Interfaces.InputReceiver;
import com.OxGames.OxShell.PagedActivity;

import java.util.ArrayList;
import java.util.List;

public class DynamicInputView extends FrameLayout implements InputReceiver {
    private boolean isShown = false;
    private final Context context;
    private TextView title;
    private RecyclerView mainList;
    private int prevUIState;

    private List<DynamicInputRow.ButtonInput> gamepadable;
    private DynamicInputRow[] rows;
    // TODO: change rowIndex and colIndex when an item gets focus from touch
    private int rowIndex = 0;
    private int colIndex = 0;
    private boolean queuedCol = false;
    private int queuedColIndex = 0;
    private int queuedRowIndex = 0;
    private boolean queuedRequestFocus = false;
    private int queuedFocusRowIndex = 0;
    private int queuedFocusColIndex = 0;

    //private boolean firstRun;

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

        layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        setLayoutParams(layoutParams);

        FrameLayout header = new FrameLayout(context);
        layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Math.round(AndroidHelpers.getScaledDpToPixels(context, 40)));
        layoutParams.gravity = Gravity.TOP;
        header.setLayoutParams(layoutParams);
        header.setBackgroundColor(Color.parseColor("#232323"));
        addView(header);

        title = new TextView(context);
        layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        title.setLayoutParams(layoutParams);
        title.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        title.setAutoSizeTextTypeWithDefaults(TextView.AUTO_SIZE_TEXT_TYPE_UNIFORM);
        int dip = Math.round(AndroidHelpers.getScaledDpToPixels(context, 8));
        title.setPadding(dip, dip, dip, dip);
        title.setTextAlignment(TEXT_ALIGNMENT_GRAVITY);
        header.addView(title);

        mainList = new RecyclerView(context);
        RecyclerView.LayoutParams recyclerParams = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        dip = Math.round(AndroidHelpers.getScaledDpToPixels(context, 40));
        recyclerParams.topMargin = dip;
        recyclerParams.bottomMargin = dip;
        mainList.setLayoutParams(recyclerParams);
        dip = Math.round(AndroidHelpers.getScaledDpToPixels(context, 20));
        mainList.setPadding(dip, dip, dip, dip);
        mainList.setBackgroundColor(Color.parseColor("#323232"));
        mainList.setLayoutManager(new LinearLayoutManager(context));
        mainList.setVisibility(VISIBLE);
        addView(mainList);

        FrameLayout footer = new FrameLayout(context);
        layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Math.round(AndroidHelpers.getScaledDpToPixels(context, 40)));
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
        mainList.setAdapter(adapter);
        rows = items;

        gamepadable = new ArrayList<>();
        for (int i = 0; i < rows.length; i++) {
            DynamicInputRow.DynamicInput[] inputItems = rows[i].getAll();
            for (int j = 0; j < inputItems.length; j++) {
                DynamicInputRow.DynamicInput item = inputItems[j];
                if (item.inputType == DynamicInputRow.DynamicInput.InputType.button && ((DynamicInputRow.ButtonInput)item).isKeycodeSet())
                    gamepadable.add((DynamicInputRow.ButtonInput)item);
                int finalI = i;
                int finalJ = j;
                // when the items have their focus set by touch, then update rowIndex and colIndex to reflect what has focus
                item.addListener(new DynamicInputListener() {
                    @Override
                    public void onFocusChanged(View view, boolean hasFocus) {
                        if (hasFocus) {
                            //Log.d("DynamicInputView", "Focus changed to " + inputItems[finalJ].inputType + " @(" + finalI + ", " + finalJ + ")");
                            rowIndex = finalI;
                            colIndex = finalJ;
                        }
                    }

                    @Override
                    public void onValuesChanged() {

                    }
                });
            }
        }

        //firstRun = true;
        adapter.addListener(() -> {
            if (queuedCol) {
                queuedCol = false;
                setColIndex(queuedRowIndex, queuedColIndex);
            }
            if (queuedRequestFocus) {
                queuedRequestFocus = false;
                requestFocus(queuedFocusRowIndex, queuedFocusColIndex);
            }
//            if (firstRun) {
//                firstRun = false;
//                int firstRowIndex = 0;
//                int firstColIndex = 0;
//                for (int i = 0; i < rows.length; i++) {
//                    DynamicInputRow.DynamicInput[] inputs = rows[i].getAll();
//                    boolean foundFocusable = false;
//                    for (int j = 0; j < inputs.length; j++) {
//                        if (inputs[j].inputType != DynamicInputRow.DynamicInput.InputType.label) {
//                            firstRowIndex = i;
//                            firstColIndex = j;
//                            foundFocusable = true;
//                            break;
//                        }
//                    }
//                    if (foundFocusable)
//                        break;
//                }
//                setColIndex(firstRowIndex, firstColIndex);
//                requestFocus(firstRowIndex, firstColIndex);
//            }
        });
        // TODO: figure out why requesting focus here does not work
        colIndex = 0;
        rowIndex = 0;
        //requestFocus(0, 0);
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
            //rowIndex = 0;
            //colIndex = 0;
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
    private void setColIndex(int rowIndex, int index) {
        //Log.d("DynamicInputView", "Scrolling from " + colIndex + " to " + index + " on " + rowIndex);
        View rowView = mainList.getChildAt(rowIndex);
        if (rowView != null)
            unsafeSetColIndex(rowIndex, index);
        else
            queueColIndex(rowIndex, index);
    }
    private void queueColIndex(int rowIndex, int colIndex) {
        queuedRowIndex = rowIndex;
        queuedColIndex = colIndex;
        queuedCol = true;
    }

    private void unsafeRequestFocus(int rowIndex, int colIndex) {
        ((DynamicInputRowView)mainList.getChildAt(rowIndex)).requestFocusOnItem(colIndex);
    }
    private void requestFocus(int rowIndex, int colIndex) {
        View row = mainList.getChildAt(rowIndex);
        if (row != null)
            unsafeRequestFocus(rowIndex, colIndex);
        else
            queueRequestFocus(rowIndex, colIndex);
    }
    private void queueRequestFocus(int rowIndex, int colIndex) {
        queuedFocusRowIndex = rowIndex;
        queuedFocusColIndex = colIndex;
        queuedRequestFocus = true;
    }

    @Override
    public boolean receiveKeyEvent(KeyEvent key_event) {
        if (key_event.getAction() == KeyEvent.ACTION_UP) {
            if (key_event.getKeyCode() == KeyEvent.KEYCODE_DPAD_DOWN) {
                int nextRowIndex = rowIndex;
                int nextColIndex = colIndex;
                for (int i = rowIndex + 1; i < rows.length; i++) {
                    DynamicInputRow.DynamicInput[] inputs = rows[i].getAll();
                    for (int j = 0; j < inputs.length; j++) {
                        if (inputs[j].inputType != DynamicInputRow.DynamicInput.InputType.label) {
                            nextRowIndex = i;
                            nextColIndex = j;
                            break;
                        }
                    }
                    if (nextRowIndex != rowIndex)
                        break;
                }
                //if (nextRowIndex != rowIndex) {
                    //Log.d("DynamicInputView", "Scrolling from " + rowIndex + " to " + nextRowIndex);
                    mainList.scrollToPosition(nextRowIndex);
                    setColIndex(nextRowIndex, nextColIndex);
                    requestFocus(nextRowIndex, nextColIndex);
                    rowIndex = nextRowIndex;
                //}
                return true;
            }
            if (key_event.getKeyCode() == KeyEvent.KEYCODE_DPAD_UP) {
                int nextRowIndex = rowIndex;
                int nextColIndex = colIndex;
                for (int i = rowIndex - 1; i >= 0; i--) {
                    DynamicInputRow.DynamicInput[] inputs = rows[i].getAll();
                    for (int j = 0; j < inputs.length; j++) {
                        if (inputs[j].inputType != DynamicInputRow.DynamicInput.InputType.label) {
                            nextRowIndex = i;
                            nextColIndex = j;
                            break;
                        }
                    }
                    if (nextRowIndex != rowIndex)
                        break;
                }
                //if (nextRowIndex != rowIndex) {
                    //Log.d("DynamicInputView", "Scrolling from " + rowIndex + " to " + nextRowIndex);
                    mainList.scrollToPosition(nextRowIndex);
                    setColIndex(nextRowIndex, nextColIndex);
                    requestFocus(nextRowIndex, nextColIndex);
                    rowIndex = nextRowIndex;
                //}
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
                //if (nextColIndex != colIndex) {
                    setColIndex(rowIndex, nextColIndex);
                    requestFocus(rowIndex, nextColIndex);
                //}
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
                //if (nextColIndex != colIndex) {
                    setColIndex(rowIndex, nextColIndex);
                    requestFocus(rowIndex, nextColIndex);
                //}
                return true;
            }
            for (DynamicInputRow.ButtonInput button : gamepadable) {
                if (button.hasKeycode(key_event.getKeyCode())) {
                    button.executeAction();
                    return true;
                }
            }
            if (key_event.getKeyCode() == KeyEvent.KEYCODE_BACK)
                return true; // in case its not mapped to anything, then don't quit OxShell
        }
        return false;
    }
}
