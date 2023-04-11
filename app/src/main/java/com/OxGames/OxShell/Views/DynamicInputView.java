package com.OxGames.OxShell.Views;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.os.Build;
import android.text.method.ScrollingMovementMethod;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.OxGames.OxShell.Adapters.DynamicInputAdapter;
import com.OxGames.OxShell.Data.DynamicInputRow;
import com.OxGames.OxShell.Data.KeyComboAction;
import com.OxGames.OxShell.Data.SettingsKeeper;
import com.OxGames.OxShell.Helpers.AndroidHelpers;
import com.OxGames.OxShell.Helpers.InputHandler;
import com.OxGames.OxShell.OxShellApp;
import com.OxGames.OxShell.PagedActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class DynamicInputView extends FrameLayout {// implements InputReceiver {
    private boolean isShown = false;
    private final Context context;
    private BetterTextView title;
    private RecyclerView mainList;
    //private int prevUIState;

    private DynamicInputRow[] rows;

    private static final String INPUT_TAG = "DYNAMIC_INPUT_INPUT";

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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Create thumb drawable
            ShapeDrawable thumbDrawable = new ShapeDrawable();
            thumbDrawable.setShape(new RectShape());
            thumbDrawable.getPaint().setColor(Color.WHITE);
            thumbDrawable.setIntrinsicWidth(8);
            thumbDrawable.setIntrinsicHeight(8);
            mainList.setVerticalScrollbarThumbDrawable(thumbDrawable);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Create track drawable
            ShapeDrawable trackDrawable = new ShapeDrawable();
            trackDrawable.setShape(new RectShape());
            trackDrawable.getPaint().setColor(Color.DKGRAY);
            trackDrawable.setIntrinsicWidth(8);
            trackDrawable.setIntrinsicHeight(8);
            mainList.setVerticalScrollbarTrackDrawable(trackDrawable);
        }
        mainList.setVerticalScrollBarEnabled(true);
        mainList.setScrollBarStyle(View.SCROLLBARS_INSIDE_INSET);
        mainList.setScrollbarFadingEnabled(false);
        //mainList.setOverScrollMode(SCROLL_AXIS_VERTICAL);
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

        setVisibility(onOff ? VISIBLE : GONE);
        //PagedActivity current = OxShellApp.getCurrentActivity();
        if (onOff) {
            //if (!isShown)
            //    prevUIState = current.getSystemUIState();
            isShown = true;
            SettingsKeeper.setNavBarHidden(true, false);
            SettingsKeeper.setStatusBarHidden(true, false);

            int[] index = new int[2];
            OxShellApp.getInputHandler().addKeyComboActions(INPUT_TAG, Arrays.stream(SettingsKeeper.getNavigateUp()).map(combo -> new KeyComboAction(combo, () -> moveFocus(KeyEvent.KEYCODE_DPAD_UP))).toArray(KeyComboAction[]::new));
            OxShellApp.getInputHandler().addKeyComboActions(INPUT_TAG, Arrays.stream(SettingsKeeper.getNavigateDown()).map(combo -> new KeyComboAction(combo, () -> moveFocus(KeyEvent.KEYCODE_DPAD_DOWN))).toArray(KeyComboAction[]::new));
            OxShellApp.getInputHandler().addKeyComboActions(INPUT_TAG, Arrays.stream(SettingsKeeper.getNavigateLeft()).map(combo -> new KeyComboAction(combo, () -> {
                boolean foundItem = getCurrentlyFocusedItem(index);
                DynamicInputRow.DynamicInput item;
                DynamicInputRow.SliderInput innerItem;
                if (foundItem && (item = rows[index[0]].get(index[1])) instanceof DynamicInputRow.SliderInput && (innerItem = (DynamicInputRow.SliderInput)item).getValue() > innerItem.getValueFrom())
                    innerItem.setValue(innerItem.getValue() - innerItem.getStepSize());
                else
                    moveFocus(KeyEvent.KEYCODE_DPAD_LEFT);
            })).toArray(KeyComboAction[]::new));
            OxShellApp.getInputHandler().addKeyComboActions(INPUT_TAG, Arrays.stream(SettingsKeeper.getNavigateRight()).map(combo -> new KeyComboAction(combo, () -> {
                boolean foundItem = getCurrentlyFocusedItem(index);
                DynamicInputRow.DynamicInput item;
                DynamicInputRow.SliderInput innerItem;
                if (foundItem && (item = rows[index[0]].get(index[1])) instanceof DynamicInputRow.SliderInput && (innerItem = (DynamicInputRow.SliderInput)item).getValue() < innerItem.getValueTo())
                    innerItem.setValue(innerItem.getValue() + innerItem.getStepSize());
                else
                    moveFocus(KeyEvent.KEYCODE_DPAD_RIGHT);
            })).toArray(KeyComboAction[]::new));
            OxShellApp.getInputHandler().addKeyComboActions(INPUT_TAG, Arrays.stream(SettingsKeeper.getPrimaryInput()).map(combo -> new KeyComboAction(combo, () -> {
                int[] result = new int[2];
                if (getCurrentlyFocusedItem(result)) {
                    rows[result[0]].get(result[1]).view.clickItem();
                }
            })).toArray(KeyComboAction[]::new));
            for (DynamicInputRow row : rows) {
                for (DynamicInputRow.DynamicInput item : row.getAll()) {
                    if (item instanceof DynamicInputRow.ButtonInput) {
                        DynamicInputRow.ButtonInput btn = (DynamicInputRow.ButtonInput) item;
                        OxShellApp.getInputHandler().addKeyComboActions(INPUT_TAG, Arrays.stream(btn.getKeyCombos()).map(combo -> new KeyComboAction(combo, btn::executeAction)).toArray(KeyComboAction[]::new));
                    }
                }
            }
            OxShellApp.getInputHandler().setActiveTag(INPUT_TAG);
        } else {
            isShown = false;
            //SettingsKeeper.setSystemUIState(prevUIState, false);
            SettingsKeeper.reloadSystemUiState();
            if (mainList != null) {
                RecyclerView.Adapter adapter = mainList.getAdapter();
                if (adapter != null)
                    ((DynamicInputAdapter)adapter).clear();
            }
            OxShellApp.getInputHandler().removeTagFromHistory(INPUT_TAG);
            OxShellApp.getInputHandler().clearKeyComboActions(INPUT_TAG);

            // hide soft keyboard whether its shown or not
            InputMethodManager imm = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getWindowToken(), 0);
        }
    }

//    @Override
//    public boolean receiveKeyEvent(KeyEvent key_event) {
//        Log.d("DynamicInputView", key_event.toString());
//        return inputHandler.onInputEvent(key_event);
//    }

    private boolean getCurrentlyFocusedItem(int[] result) {
        result[0] = -1;
        result[1] = -1;
        boolean foundFocusedItem = false;
        for (int i = 0; i < rows.length; i++) {
            DynamicInputRow.DynamicInput[] inputItems = rows[i].getAll();
            for (int j = 0; j < inputItems.length; j++) {
                DynamicInputRow.DynamicInput item = inputItems[j];
                if (item.view != null && item.view.hasFocus()) {
                    //Log.d("DynamicInputView", "Found currently focused item on row: " + i + " col: " + j);
                    result[0] = i;
                    result[1] = j;
                    foundFocusedItem = true;
                    break;
                }
            }
            if (foundFocusedItem)
                break;
        }
        return foundFocusedItem;
    }
    private void moveFocus(int keycode_direction) {
        int nextRow = -1;
        int nextCol = -1;
        int[] result = new int[2];
        if (getCurrentlyFocusedItem(result)) {
            nextRow = result[0];
            nextCol = result[1];
        }
        int startRow = nextRow;
        int startCol = nextCol;

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
        //View view = rows[nextRow].get(nextCol).view;
        //if (view != null) {
            //Log.d("DynamicInputView", "Shifting focus [" + startRow + ", " + startCol + "] => [" + nextRow + ", " + nextCol + "]");
            int finalNextRow = nextRow;
            int finalNextCol = nextCol;
            scrollToPositionIfNeeded(mainList, nextRow, () -> {
                scrollToPositionIfNeeded(rows[finalNextRow].view.getRow(), finalNextCol, () -> {
                    rows[finalNextRow].get(finalNextCol).view.requestFocus();
                });
            });
            //scrollToItem(nextRow, nextCol);
            //((DynamicInputItemView)((DynamicInputRowView)mainList.findViewHolderForAdapterPosition(nextRow).itemView).getRow().findViewHolderForAdapterPosition(nextCol).itemView).requestFocus();
            //view.requestFocus();
        //} else
        //    Log.e("DynamicInputView", "Failed to shift focus [" + startRow + ", " + startCol + "] => [" + nextRow + ", " + nextCol + "]");
    }

    private static boolean shouldScrollToPosition(RecyclerView rv, int position) {
        if (rv.getLayoutManager() != null) {
            int firstVisibleItem = ((LinearLayoutManager)rv.getLayoutManager()).findFirstVisibleItemPosition();
            int lastVisibleItem = ((LinearLayoutManager)rv.getLayoutManager()).findLastVisibleItemPosition();
            return position < firstVisibleItem || position > lastVisibleItem;
        }
        return false;
    }
    private static void scrollToPositionIfNeeded(RecyclerView rv, int position, Runnable onScrolled) {
        if (shouldScrollToPosition(rv, position)) {
            rv.scrollToPosition(position);
            rv.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    //Log.d("DynamicInputView", "onScrolled " + recyclerView.getScrollState());
                    if(recyclerView.getScrollState() == RecyclerView.SCROLL_STATE_IDLE) {
                        //Log.d("DynamicInputView", "onScrolled to idle");
                        // Action to be performed after RecyclerView has finished scrolling to the desired position
                        // For example, you can update the UI, make a network call, etc.
                        //performAction();
                        onScrolled.run();
                        recyclerView.removeOnScrollListener(this); // Remove the listener after performing the action
                    }
                }
            });
        } else
            onScrolled.run();
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
