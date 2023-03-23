package com.OxGames.OxShell.Views;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.OxGames.OxShell.Adapters.DynamicInputAdapter;
import com.OxGames.OxShell.Data.DynamicInputRow;
import com.OxGames.OxShell.Data.FontRef;
import com.OxGames.OxShell.Data.KeyComboAction;
import com.OxGames.OxShell.Data.SettingsKeeper;
import com.OxGames.OxShell.Helpers.ActivityManager;
import com.OxGames.OxShell.Helpers.AndroidHelpers;
import com.OxGames.OxShell.Helpers.InputHandler;
import com.OxGames.OxShell.Interfaces.DynamicInputListener;
import com.OxGames.OxShell.Interfaces.InputReceiver;
import com.OxGames.OxShell.PagedActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class DynamicInputView extends FrameLayout implements InputReceiver {
    private boolean isShown = false;
    private final Context context;
    private BetterTextView title;
    private RecyclerView mainList;
    private int prevUIState;

    private DynamicInputRow[] rows;

    //private int row;
    //private int col;
    //private List<DynamicInputRow.ButtonInput> gamepadable;
    //private boolean firstRun;
    //private int directionKeyCode = -1;

    private final List<Consumer<Boolean>> onShownListeners = new ArrayList<>();
    private InputHandler inputHandler;

    public void addShownListener(Consumer<Boolean> onShownListener) {
        onShownListeners.add(onShownListener);
    }
    public void removeShownListener(Consumer<Boolean> onShownListener) {
        onShownListeners.remove(onShownListener);
    }
    public void clearShownListeners() {
        onShownListeners.clear();
    }

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
        inputHandler = new InputHandler();
        setShown(false);
        setFocusable(false);
        //setClickable(true); // block out touch input to views behind
        //setBackgroundColor(Color.parseColor("#80323232"));

        LayoutParams layoutParams;

        layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        setLayoutParams(layoutParams);
        setFocusable(false);
        //getViewTreeObserver().addOnGlobalFocusChangeListener(onFocusChange);

        FrameLayout header = new FrameLayout(context);
        layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Math.round(AndroidHelpers.getScaledDpToPixels(context, 40)));
        layoutParams.gravity = Gravity.TOP;
        header.setLayoutParams(layoutParams);
        header.setBackgroundColor(Color.parseColor("#66646464"));
        header.setFocusable(false);
        addView(header);

        title = new BetterTextView(context);
        layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        title.setLayoutParams(layoutParams);
        title.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        //title.setAutoSizeTextTypeWithDefaults(TextView.AUTO_SIZE_TEXT_TYPE_UNIFORM);
        int dip = Math.round(AndroidHelpers.getScaledDpToPixels(context, 8));
        title.setPadding(dip, 0, dip, 0);
        title.setTextAlignment(TEXT_ALIGNMENT_GRAVITY);
        title.setTextColor(Color.WHITE);
        title.setTypeface(SettingsKeeper.getFont());
        title.setOutlineColor(Color.BLACK);
        title.setOutlineSize(Math.round(AndroidHelpers.getScaledDpToPixels(context, 3)));
        title.setFocusable(false);
        header.addView(title);

        mainList = new RecyclerView(context);
        RecyclerView.LayoutParams recyclerParams = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        dip = Math.round(AndroidHelpers.getScaledDpToPixels(context, 40));
        recyclerParams.topMargin = dip;
        recyclerParams.bottomMargin = dip;
        mainList.setLayoutParams(recyclerParams);
        dip = Math.round(AndroidHelpers.getScaledDpToPixels(context, 20));
        mainList.setPadding(dip, dip, dip, dip);
        mainList.setBackgroundColor(Color.parseColor("#66323232"));
        mainList.setLayoutManager(new LinearLayoutManager(context));
        mainList.setVisibility(VISIBLE);
        mainList.setFocusable(false);
        addView(mainList);

        FrameLayout footer = new FrameLayout(context);
        layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Math.round(AndroidHelpers.getScaledDpToPixels(context, 40)));
        layoutParams.gravity = Gravity.BOTTOM;
        footer.setLayoutParams(layoutParams);
        footer.setBackgroundColor(Color.parseColor("#66646464"));
        footer.setFocusable(false);
        addView(footer);
    }
//    ViewTreeObserver.OnGlobalFocusChangeListener onFocusChange = (oldFocus, newFocus) -> {
//        //Log.d("DynamicInputView", "onGlobalFocusChange");
//
//    };

    public void setTitle(String value) {
        title.setText(value);
    }
    public void setItems(DynamicInputRow... items) {
        if (mainList.getAdapter() != null)
            ((DynamicInputAdapter)mainList.getAdapter()).clearListeners();
        DynamicInputAdapter adapter = new DynamicInputAdapter(context, items);
        mainList.setAdapter(adapter);
        rows = items;

        //gamepadable = new ArrayList<>();
        for (int i = 0; i < rows.length; i++) {
            DynamicInputRow.DynamicInput[] inputItems = rows[i].getAll();
            for (int j = 0; j < inputItems.length; j++) {
                DynamicInputRow.DynamicInput item = inputItems[j];
                item.row = i;
                item.col = j;
            }
        }

        // TODO: figure out how to request focus on the first item
    }

    public boolean isOverlayShown() {
        return isShown;
    }
    public void setShown(boolean onOff) {
        for (Consumer<Boolean> onShownListener : onShownListeners)
            if (onShownListener != null)
                onShownListener.accept(onOff);

        isShown = onOff;
        setVisibility(onOff ? VISIBLE : GONE);
        PagedActivity current = ActivityManager.getCurrentActivity();
        if (onOff) {
            prevUIState = current.getSystemUIState();
            current.setNavBarHidden(true);
            current.setStatusBarHidden(true);

            for (DynamicInputRow row : rows) {
                for (DynamicInputRow.DynamicInput item : row.getAll()) {
                    if (item instanceof DynamicInputRow.ButtonInput) {
                        DynamicInputRow.ButtonInput btn = (DynamicInputRow.ButtonInput) item;
                        inputHandler.addKeyComboActions(Arrays.stream(btn.getKeyCombos()).map(combo -> new KeyComboAction(combo, btn::executeAction)).toArray(KeyComboAction[]::new));
                    }
                }
            }
        } else {
            current.setSystemUIState(prevUIState);
            if (mainList != null) {
                RecyclerView.Adapter adapter = mainList.getAdapter();
                if (adapter != null)
                    ((DynamicInputAdapter)adapter).clear();
            }
            inputHandler.clearKeyComboActions();
        }
    }

    @Override
    public boolean receiveKeyEvent(KeyEvent key_event) {
        //Log.d("DynamicInputView", key_event.toString());
        boolean isDpadKey = key_event.getKeyCode() == KeyEvent.KEYCODE_DPAD_LEFT || key_event.getKeyCode() == KeyEvent.KEYCODE_DPAD_RIGHT || key_event.getKeyCode() == KeyEvent.KEYCODE_DPAD_UP || key_event.getKeyCode() == KeyEvent.KEYCODE_DPAD_DOWN;
        if (key_event.getAction() == KeyEvent.ACTION_DOWN) {
            if (isDpadKey) {
                //directionKeyCode = key_event.getKeyCode();
                moveFocus(key_event.getKeyCode());
                return true;
            }
        }
        return inputHandler.onInputEvent(key_event);
    }

    private void moveFocus(int keycode_direction) {
        int nextRow = -1;
        int nextCol = -1;
        for (int i = 0; i < rows.length; i++) {
            DynamicInputRow.DynamicInput[] inputItems = rows[i].getAll();
            boolean foundFocusedItem = false;
            for (int j = 0; j < inputItems.length; j++) {
                DynamicInputRow.DynamicInput item = inputItems[j];
                if (item.view != null && item.view.hasFocus()) {
                    //Log.d("DynamicInputView", "Found currently focused item on row: " + i + " col: " + j);
                    nextRow = i;
                    nextCol = j;
                    foundFocusedItem = true;
                    break;
                }
            }
            if (foundFocusedItem)
                break;
        }

        if (keycode_direction == KeyEvent.KEYCODE_DPAD_UP) {
            nextRow = searchVer(nextRow - 1, nextCol, -1);
            int potCol = searchHor(nextRow, nextCol, -1);
            if (!canFocusOn(nextRow, potCol))
                nextCol = searchHor(nextRow, nextCol, 1);
            else
                nextCol = potCol;
        }
        if (keycode_direction == KeyEvent.KEYCODE_DPAD_DOWN) {
            //int startRow = nextRow;
            nextRow = searchVer(nextRow + 1, nextCol, 1);
            //Log.d("DynamicInputView", "Moving down to row: " + nextRow + " from: " + startRow);
            int potCol = searchHor(nextRow, nextCol, -1);
            if (!canFocusOn(nextRow, potCol))
                nextCol = searchHor(nextRow, nextCol, 1);
            else
                nextCol = potCol;
        }
        nextRow = Math.min(Math.max(nextRow, 0), rows.length - 1);
        if (keycode_direction == KeyEvent.KEYCODE_DPAD_LEFT)
            nextCol = searchHor(nextRow, nextCol - 1, -1);
        if (keycode_direction == KeyEvent.KEYCODE_DPAD_RIGHT)
            nextCol = searchHor(nextRow, nextCol + 1, 1);
        nextCol = Math.min(Math.max(nextCol, 0), rows[nextRow].getCount() - 1);
        //Log.d("DynamicInputView", "Attempting to focus on row: " + nextRow + " col: " + nextCol);
        View view = rows[nextRow].get(nextCol).view;
        if (view != null) {
            //Log.d("DynamicInputView", "Requesting focus on [" + nextRow + ", " + nextCol + "]");
            scrollToItem(nextRow, nextCol);
            view.requestFocus();
        } else
            Log.e("DynamicInputView", "Failed to focus on null view at [" + nextRow + ", " + nextCol + "]");
    }
    private void scrollToItem(int row, int col) {
        if (row >= 0 && row < rows.length)
            mainList.scrollToPosition(row);
        if (col >= 0 && col < rows[row].getCount())
            rows[row].view.getRow().scrollToPosition(col);
    }
    private boolean canFocusOn(DynamicInputRow.DynamicInput item) {
        return item != null && item.getVisibility() == VISIBLE && item.isEnabled() && item.inputType != DynamicInputRow.DynamicInput.InputType.label && item.inputType != DynamicInputRow.DynamicInput.InputType.image;
    }
    private boolean canFocusOn(int row, int col) {
        boolean canFocusOn = false;
        if (row >= 0 && row < rows.length)
            if (col >= 0 && col < rows[row].getCount())
                canFocusOn = canFocusOn(rows[row].get(col));
        return canFocusOn;
    }
    private int searchHor(int row, int startCol, int dir) {
        if (row >= 0 && row < rows.length) {
            DynamicInputRow currentRow = rows[row];
            boolean foundFocusable = false;
            while (!foundFocusable && startCol >= 0 && startCol < currentRow.getCount()) {
                DynamicInputRow.DynamicInput nextItem = currentRow.get(startCol);
                foundFocusable = canFocusOn(nextItem);
                if (!foundFocusable)
                    startCol += dir;
            }
        }
        //else
        //    Log.e("DynamicInputView", "Failed to search horizontally, bad row " + row + " count: " + rows.length);
        return startCol;
    }
    private int searchVer(int startRow, int startCol, int dir) {
        int rowsCount = rows.length;
        boolean foundFocusable = false;
        while (!foundFocusable && startRow >= 0 && startRow < rowsCount) {
            int preppedCol = Math.min(Math.max(startCol, 0), rows[startRow].getCount() - 1);
            int potCol = searchHor(startRow, preppedCol, -1);
            foundFocusable = canFocusOn(startRow, potCol);
            if (!foundFocusable) {
                potCol = searchHor(startRow, preppedCol, 1);
                foundFocusable = canFocusOn(startRow, potCol);
                if (!foundFocusable)
                    startRow += dir;
            }
        }
        return startRow;
    }
}
