package com.OxGames.OxShell.Views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.os.Handler;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.OxGames.OxShell.Data.KeyComboAction;
import com.OxGames.OxShell.Data.SettingsKeeper;
import com.OxGames.OxShell.Helpers.MathHelpers;
import com.OxGames.OxShell.Interfaces.InputReceiver;
import com.OxGames.OxShell.Interfaces.XMBAdapterListener;
import com.OxGames.OxShell.OxShellApp;
import com.OxGames.OxShell.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;
import java.util.stream.Collectors;

public class XMBView extends ViewGroup {// implements InputReceiver {//, Refreshable {
    private static final float EPSILON = 0.0001f;
    protected final Context context;
    public static final int CATEGORY_TYPE = 0;
    public static final int ITEM_TYPE = 1;
    public static final int INNER_TYPE = 2;
    public static final float ITEM_Z = -1;
    public static final float CAT_Z = 0;
    public static final float INNER_Z = 1;


    // col index represents which column we are currently in
    int colIndex = 0;
    int prevColIndex = 0;
    // row index represents which item we are at within a column
    int rowIndex = 0;

    private float fullItemAlpha = 1f; // the alpha value of the item(s) selected
    private float translucentItemAlpha = 0.33f; // the alpha value of the item(s) not in selection
    private float innerItemOverlayTranslucent = 0.066f; // the alpha value of items not within the inner items

    private Adapter adapter;
    private final Stack<ViewHolder> goneCatViews;
    private final Stack<ViewHolder> goneItemViews; //The views whose visibility are set to gone since they are not currently used
    private final Stack<ViewHolder> goneInnerItemViews;
    private final HashMap<Integer, ViewHolder> usedViews; //The views that are currently displayed and the hashed total index of the item they represent as their key
    private final Stack<Integer> innerItemEntryPos; // The indices that represent where we entered from, when empty the entry is colIndex and rowIndex
    private final Stack<Float> innerItemVerPos; // The y scroll value of the inner items menu
    public boolean isInsideItem() {
        return innerItemVerPos.size() > 0;
    }
    private int innerYToIndex(float yValue, Integer... position) {
        // finds the inner index closest to the current y value then clamps it to be within the proper range
        return Math.min(Math.max(Math.round(yValue / (innerItemSize + innerVerSpacing)), 0), adapter.getInnerItemCount(position) - 1);
    }
    private int innerYToIndex() {
        return innerYToIndex(innerItemVerPos.peek(), getEntryPosition());
    }

    private final ArrayList<Float> catPos; // go from 0 to (adapter.getColumnSize(colIndex) - 1) * getVerShiftOffset()
    private boolean moveMode;
    private boolean columnMode;
    private int origMoveColIndex;
    private int origMoveLocalIndex;
    //private int moveColIndex;
    //private int moveLocalIndex;
    private XMBAdapterListener adapterListener = new XMBAdapterListener() {
        @Override
        public void onColumnAdded(int columnIndex) {
            catPos.add(columnIndex, 0f);
            if (colIndex >= columnIndex) {
                int maxIndex = getAdapter().getColumnCount() - 1;
                colIndex = Math.min(colIndex + 1, maxIndex);
                prevColIndex += Math.min(prevColIndex + 1, maxIndex);
            }
            rowIndex = getCachedIndexOfCat(colIndex);
            shiftX = getShiftX(colIndex);
            returnAllViews();
            setViews(false, true);
        }
        @Override
        public void onColumnRemoved(int columnIndex) {
            catPos.remove(columnIndex);
            if (columnIndex <= colIndex) {
                colIndex = Math.max(colIndex - 1, 0);
                prevColIndex = Math.max(prevColIndex - 1, 0);
            }
            rowIndex = getCachedIndexOfCat(colIndex);
            shiftX = getShiftX(colIndex);
            returnAllViews();
            setViews(false, true);
        }

        @Override
        public void onColumnShifted(int fromColIndex, int toColIndex) {
            float origCatPos = catPos.get(fromColIndex);
            if (toColIndex > fromColIndex) {
                catPos.add(toColIndex + 1, origCatPos);
                catPos.remove(fromColIndex);
            } else {
                catPos.remove(fromColIndex);
                catPos.add(toColIndex, origCatPos);
            }
            colIndex = toColIndex;
            rowIndex = getCachedIndexOfCat(colIndex);
            shiftX = getShiftX(colIndex);
            returnAllViews();
            setViews(false, true);
        }

        @Override
        public void onSubItemAdded(int columnIndex, int localIndex) {
            if (columnIndex == colIndex)
                rowIndex = getCachedIndexOfCat(colIndex);
            returnAllViews();
            setViews(false, true);
        }
        @Override
        public void onSubItemRemoved(int columnIndex, int localIndex) {
            if (columnIndex == colIndex)
                rowIndex = getCachedIndexOfCat(colIndex);
            returnAllViews();
            setViews(false, true);
        }
    };

    public XMBView(Context context) {
        this(context, null);
    }
    public XMBView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    public XMBView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;

        goneCatViews = new Stack<>();
        goneItemViews = new Stack<>();
        goneInnerItemViews = new Stack<>();
        usedViews = new HashMap<>();
        catPos = new ArrayList<>();
        innerItemEntryPos = new Stack<>();
        innerItemVerPos = new Stack<>();

        setAttributes(attrs);
    }
    private void setAttributes(AttributeSet attrs) {
        if (attrs != null) {
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.XMBView);
            // outline size
            if (a.hasValue(R.styleable.XMBView_subItemGap))
                subItemGap = (int) a.getDimension(R.styleable.XMBView_subItemGap, 0);
            if (a.hasValue(R.styleable.XMBView_horizontalSpacing))
                horSpacing = (int) a.getDimension(R.styleable.XMBView_horizontalSpacing, 0);
            if (a.hasValue(R.styleable.XMBView_verticalSpacing))
                verSpacing = (int) a.getDimension(R.styleable.XMBView_verticalSpacing, 0);
            if (a.hasValue(R.styleable.XMBView_innerHorizontalSpacing))
                innerHorSpacing = (int) a.getDimension(R.styleable.XMBView_innerHorizontalSpacing, 0);
            if (a.hasValue(R.styleable.XMBView_innerVerticalSpacing))
                innerVerSpacing = (int) a.getDimension(R.styleable.XMBView_innerVerticalSpacing, 0);

            a.recycle();
        }
    }
    public abstract static class Adapter<T extends ViewHolder> {
        protected List<XMBAdapterListener> listeners = new ArrayList<>();
        public void addListener(XMBAdapterListener listener) {
            listeners.add(listener);
        }
        public void removeListener(XMBAdapterListener listener) {
            listeners.remove(listener);
        }
        public void clearListeners() {
            listeners.clear();
        }

        @NonNull
        public abstract T onCreateViewHolder(@NonNull ViewGroup parent, int viewType);
        public abstract void onBindViewHolder(@NonNull T holder, Integer... position);
        public abstract int getItemCount(boolean withInnerItems);
        public abstract int getColumnCount();
        public abstract int getColumnSize(int columnIndex);
        public abstract void onViewAttachedToWindow(@NonNull T holder);
        public abstract Object getItem(Integer... position);
        public abstract ArrayList<ArrayList<Object>> getItems();
        public abstract void setItems(ArrayList<ArrayList<Object>> items);
        public abstract boolean hasInnerItems(Integer... position);
        public abstract int getInnerItemCount(Integer... position);
        public abstract boolean isColumnHead(Integer... position);

        protected abstract int getTextSize();
        protected abstract void shiftItemHorizontally(int toBeMovedColIndex, int toBeMovedLocalIndex, int moveToColIndex, int moveToLocalIndex, boolean createColumn);
        protected abstract void shiftItemVertically(int startColIndex, int fromLocalIndex, int toLocalIndex);
        public abstract void addSubItem(int columnIndex, int localIndex, Object toBeAdded);
        public abstract void removeSubItem(int columnIndex, int localIndex);
        public abstract void createColumnAt(int columnIndex, Object head);
        public abstract void removeColumnAt(int columnIndex);
        public abstract void shiftColumnTo(int fromColIndex, int toColIndex);
    }
    public abstract static class ViewHolder {
        protected View itemView;
        private int itemViewType;

        private boolean isHighlighted;
        private boolean requestHideTitle;
        private boolean isNew;
        private float currentX;
        private float currentY;
        private float prevX;
        private float prevY;

        public ViewHolder(@NonNull View itemView) {
            this.itemView = itemView;
        }

        public boolean isHighlighted() {
            return isHighlighted;
        }
        public boolean isHideTitleRequested() { return requestHideTitle; }
        public int getItemViewType() {
            return itemViewType;
        }

        private float getX() {
            return currentX;
        }
        private float getY() {
            return currentY;
        }
        private float getPrevX() {
            return prevX;
        }
        private float getPrevY() {
            return prevY;
        }
        private void setX(float x) {
            prevX = currentX;
            currentX = x;
        }
        private void setY(float y) {
            prevY = currentY;
            currentY = y;
        }
    }

    public void setAdapter(Adapter adapter) {
        this.catPos.clear();
        if (this.adapter != null)
            this.adapter.removeListener(adapterListener);
        this.adapter = adapter;
        this.adapter.addListener(adapterListener);
        this.colIndex = 0;
        this.prevColIndex = 0;
        this.innerItemEntryPos.clear();
        this.innerItemVerPos.clear();
        this.rowIndex = (adapter != null && adapter.getColumnCount() > 0 && adapter.getColumnSize(this.colIndex) > 1) ? 1 : 0;
        removeViews();
        for (int i = 0; i < adapter.getColumnCount(); i++)
            this.catPos.add(0f);
        setShiftXAndY(0, 0, true);
    }
    public Adapter getAdapter() {
        return adapter;
    }

    // for fine control of the menu
    private float shiftX = 0; // goes from 0 to (adapter.getColumnCount() - 1) * getHorShiftOffset()

    private int itemSize = 196;
    private int catSize = 196;
    private int innerItemSize = 196;
    private float horSpacing;
    private float verSpacing; //How much space to add between items vertically
    private float subItemGap; //The gap between the column items and their sub items
    private float innerHorSpacing;
    private float innerVerSpacing;
    private float catHorShift = horSpacing; //How much to shift the categories bar horizontally
    private float getHorShiftOffset() {
        // how far apart each item is from center to center
        return catSize + horSpacing;
    }
    private float getVerShiftOffset() {
        // how far apart each item is from center to center
        return itemSize + verSpacing;
    }

    private int xToIndex(float xValue) {
        // finds the column index closest to the current x value
        return Math.round(xValue / getHorShiftOffset());
    }
    private int clampColIndex(int colIndex) {
        return Math.min(Math.max(colIndex, 0), adapter.getColumnCount() - 1);
    }
    private float toNearestColumn(float xValue) {
        // finds the column index nearest to the pixel location then turns the index back into pixel location
        return xToIndex(xValue) * getHorShiftOffset();
    }
    private void setShiftXAndY(float xValue, float yValue, boolean instant) {
        boolean xChanged = Math.abs(shiftX - xValue) > EPSILON;
        if (xChanged)
            shiftX = Math.min(Math.max(xValue, 0), (adapter.getColumnCount() - 1) * getHorShiftOffset());
        int colIndex = xToIndex(shiftX);

        float shiftY = catPos.get(colIndex);
        boolean yChanged = Math.abs(shiftY - yValue) > EPSILON;
        if (yChanged) {
            shiftY = clampYValue(yValue, colIndex);
            catPos.set(colIndex, shiftY);
        }

        int localIndex = yToIndex(shiftY, colIndex);// + (catHasSubItems(colIndex) ? 1 : 0);
        boolean changed = this.colIndex != colIndex || this.rowIndex != localIndex;
        if (changed) {
            this.prevColIndex = this.colIndex;
            this.colIndex = colIndex;
            this.rowIndex = localIndex;
        }
        setViews(changed, instant);
    }
    private void setShiftX(float xValue) {
        if (adapter == null)
            return;
        if (!isInsideItem()) {
            if (Math.abs(shiftX - xValue) > EPSILON) {
                shiftX = Math.min(Math.max(xValue, 0), (adapter.getColumnCount() - 1) * getHorShiftOffset());
                int moveColIndex = xToIndex(xValue);
                if (xValue < 0)
                    moveColIndex = -1; // just for touch input since the amount a user can move by per frame is not enough to move a whole index
                int properColIndex = clampColIndex(moveColIndex);
                boolean changed = properColIndex != this.colIndex;
                int newRowIndex = getCachedIndexOfCat(properColIndex);

                int fromColIndex = this.colIndex;
                int fromRowIndex = this.rowIndex;
                int toColIndex = moveColIndex;
                boolean moveChanged = changed || (moveMode && this.colIndex != moveColIndex);
                if (changed) {
                    catPos.set(properColIndex, getShiftY(newRowIndex, properColIndex));
                    this.prevColIndex = this.colIndex;
                    this.colIndex = properColIndex;
                    this.rowIndex = newRowIndex;
                }
                if (moveChanged)
                    onShiftHorizontally(fromColIndex, fromRowIndex, toColIndex);
                setViews(moveChanged, false);
            }
        }
    }
    private float getShiftX(int colIndex) {
        return colIndex * getHorShiftOffset();
    }
    private void setShiftXToNearestColumn() {
        setShiftX(toNearestColumn(shiftX));
    }
    private void shiftX(float amount) {
        setShiftX(shiftX + amount);
    }
    private void shiftXDisc(int amount) {
        if (Math.abs(amount) > 0) {
            int currentColIndex = this.colIndex;
            //Log.d("XMBView", "Shifting by " + amount + " from " + currentColIndex);
            // get the offset column's local index then convert it to x position
            setShiftX(getShiftX(currentColIndex + amount));
        }
    }
    private float clampYValue(float yValue, int colIndex) {
        return Math.min(Math.max(yValue, 0), (getColTraversableCount(colIndex) - 1) * getVerShiftOffset());
    }
    private void setShiftY(float yValue, int colIndex) {
        //Log.d("XMBView", "Shift Y " + colIndex + ", " + yValue);
        if (!isInsideItem()) {
            float shiftY = catPos.get(colIndex);
            if (Math.abs(shiftY - yValue) > EPSILON) {
                float newYValue = clampYValue(yValue, colIndex);
                catPos.set(colIndex, newYValue);
                int currentCol = this.colIndex;
                // if the shifted column is the one we are currently on
                if (currentCol == colIndex) {
                    int newLocalIndex = yToIndex(newYValue, colIndex);// + (catHasSubItems(colIndex) ? 1 : 0);
                    boolean changed = newLocalIndex != this.rowIndex;
                    if (changed) {
                        int prevLocalIndex = getLocalIndex();
                        this.prevColIndex = this.colIndex;
                        this.colIndex = colIndex;
                        this.rowIndex = newLocalIndex;
                        onShiftVertically(currentCol, prevLocalIndex, newLocalIndex);
                    }
                    setViews(changed, false);
                }
            }
        } else {
            float currentY = innerItemVerPos.peek();
            //Log.d("XMBView", "Attempting to move vertically when inside an item from " + currentY + " to " + yValue);
            if (Math.abs(currentY - yValue) > EPSILON) {
                float adjustedY = Math.min(Math.max(yValue, 0), (adapter.getInnerItemCount(getEntryPosition()) - 1) * (innerItemSize + innerVerSpacing));
                innerItemVerPos.pop();
                innerItemVerPos.push(adjustedY);
                setViews(false, false);
            }
        }
    }
    private float getShiftY(int localIndex, int colIndex) {
        // determines the pixel position of the sub-item within the given column
        if (catHasSubItems(colIndex))
            localIndex -= 1; // since we don't want to count the column head
        return getVerShiftOffset() * Math.min(Math.max(localIndex, 0), getColTraversableCount(colIndex));
    }
    private void shiftY(float amount, int colIndex) {
        //Log.d("XMBView", "Shift Y on " + colIndex + " by " + amount);
        float origAmount;
        if (isInsideItem())
            origAmount = innerItemVerPos.peek();
        else
            origAmount = catPos.get(colIndex);
        setShiftY(origAmount + amount, colIndex);
    }
    private int yToIndex(float yValue, int colIndex) {
        // finds the local index closest to the current y value then clamps it to be within the proper range
        boolean hasSubItems = catHasSubItems(colIndex);
        return Math.min(Math.max(Math.round(yValue / getVerShiftOffset()), 0), adapter.getColumnSize(colIndex) - (hasSubItems ? 2 : 1)) + (hasSubItems ? 1 : 0);
    }
    private void setShiftYToNearestItem(int colIndex) {
        if (colIndex < 0 || colIndex >= catPos.size())
            return;
        setShiftY(toNearestColumnItem(catPos.get(colIndex), colIndex), colIndex);
    }
    private float toNearestColumnItem(float yValue, int colIndex) {
        // finds the local index nearest to the pixel location then turns the index back into pixel location
        float nearestY;
        if (isInsideItem())
            nearestY = innerYToIndex() * (innerItemSize + innerVerSpacing);
        else
            nearestY = (yToIndex(yValue, colIndex) - (catHasSubItems(colIndex) ? 1 : 0)) * getVerShiftOffset();
        return nearestY;
    }
    private void shiftYDisc(int amount) {
        //Log.d("XMBView", "Moving vertically from " + this.rowIndex + " on " + this.colIndex + " by " + amount);
        shiftYDisc(this.colIndex, this.rowIndex, amount);
    }
    private void shiftYDisc(int colIndex, int localIndex, int amount) {
        if (Math.abs(amount) > 0) {
            if (!isInsideItem()) {
                setShiftY(getShiftY(localIndex + amount, colIndex), colIndex);
            } else {
                float nextY = Math.min(Math.max(innerYToIndex() + amount, 0), adapter.getInnerItemCount(getEntryPosition()) - 1) * (innerVerSpacing + innerItemSize);
                setShiftY(nextY, -1);
            }
        }
    }
    private int getColTraversableCount(int colIndex) {
        int columnSize = adapter.getColumnSize(colIndex);
        return columnSize - (columnSize > 1 ? 1 : 0);
    }

    private float touchMarginTop = 50;
    private float touchMarginLeft = 50;
    private float touchMarginRight = 50;
    private float touchMarginBottom = 50;
    private float momentumDeceleration = 10000; // pixels per second per second
    private float touchDeadzone = 50;
    private float longPressTime = 300; // in milliseconds
    private boolean touchInsideBorders = false;
    private boolean touchHor = false;
    private boolean touchVer = false;
    private long touchMoveStartTime = 0;
    private float touchMoveDir = 0;
    //private float momentumTravelDistX = 0;
    //private float momentumTravelDistY = 0;
    private float startTouchX = 0;
    private float pseudoStartX = 0;
    private float momentumX = 0;
    private float prevX = 0;
    private float startTouchY = 0;
    private float pseudoStartY = 0;
    private float momentumY = 0;
    private float prevY = 0;
    private int startTouchColIndex = 0;
    private int startTouchRowIndex = 0;
    private boolean longPressed = false;
    private boolean isPressing = false;

    private final Rect reusableRect = new Rect();

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return true;
    }

    @Override
    public boolean onCapturedPointerEvent(MotionEvent event) {
        Log.i("XMBView", "Pointer event " + event);
        return super.onCapturedPointerEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            touchMoveStartTime = SystemClock.uptimeMillis();
            longPressed = false;
            isPressing = true;
            startTouchX = ev.getRawX();
            startTouchY = ev.getRawY();
            prevX = startTouchX;
            prevY = startTouchY;
            touchInsideBorders = startTouchX > touchMarginLeft && startTouchX < OxShellApp.getDisplayWidth() - touchMarginRight && startTouchY > touchMarginTop && startTouchY < OxShellApp.getDisplayHeight() - touchMarginBottom;
            if (touchInsideBorders) {
                stopMomentum();
                Handler longPressHandler = new Handler();
                Runnable checkLongPress = new Runnable() {
                    @Override
                    public void run() {
                        if (isPressing && !touchHor && !touchVer && Math.abs(startTouchX - ev.getRawX()) < touchDeadzone && Math.abs(startTouchY - ev.getRawY()) < touchDeadzone) {
                            if (SystemClock.uptimeMillis() - touchMoveStartTime >= longPressTime) {
                                longPressed = true;
                                secondaryAction();
                            } else
                                longPressHandler.postDelayed(this, 10);
                        }
                    }
                };
                longPressHandler.postDelayed(checkLongPress, 10);
            }
        } else if (ev.getAction() == MotionEvent.ACTION_MOVE && touchInsideBorders) {
            if (longPressed)
                return true;
            float currentX = ev.getRawX();
            float currentY = ev.getRawY();
            // only acknowledge moving horizontally if we're not inside an item
            if (!touchVer && !touchHor && Math.abs(currentX - startTouchX) >= touchDeadzone && !isInsideItem()) {
                startTouchColIndex = this.colIndex;
                startTouchRowIndex = this.rowIndex;
                touchMoveDir = Math.signum(currentX - startTouchX);
                touchMoveStartTime = SystemClock.uptimeMillis();
                pseudoStartX = touchMoveDir * touchDeadzone + startTouchX;
                touchHor = true;
            }
            // only acknowledge moving vertically if there are items to move vertically through
            if (!touchHor && !touchVer && Math.abs(currentY - startTouchY) >= touchDeadzone && (isInsideItem() || catHasSubItems(colIndex))) {
                startTouchColIndex = this.colIndex;
                startTouchRowIndex = this.rowIndex;
                touchMoveDir = Math.signum(currentY - startTouchY);
                touchMoveStartTime = SystemClock.uptimeMillis();
                pseudoStartY = touchMoveDir * touchDeadzone + startTouchY;
                touchVer = true;
            }

            if (touchHor) {
                if (touchMoveDir != Math.signum(pseudoStartX - currentX)) {
                    // if the movement direction changed, then update the start time to reflect when the change happened
                    touchMoveDir = Math.signum(pseudoStartX - currentX);
                    touchMoveStartTime = SystemClock.uptimeMillis();
                }
                float diffX = prevX - currentX;
                shiftX(diffX);// * 0.2f);
                if (this.colIndex != startTouchColIndex) {
                    // if the index changed then set our drag start value to be where we are right now
                    pseudoStartX = currentX;
                    startTouchColIndex = this.colIndex;
                }
            }
            if (touchVer) {
                float diffY = prevY - currentY;
                if (touchMoveDir != Math.signum(pseudoStartY - currentY)) {
                    // if the movement direction changed, then update the start time to reflect when the change happened
                    touchMoveDir = Math.signum(pseudoStartY - currentY);
                    touchMoveStartTime = SystemClock.uptimeMillis();
                }
                shiftY(diffY, startTouchColIndex);
                if (this.rowIndex != startTouchRowIndex) {
                    // if the index changed then set our drag start value to be where we are right now
                    pseudoStartY = currentY;
                    startTouchRowIndex = this.rowIndex;
                }
            }
            prevX = currentX;
            prevY = currentY;
        } else if (ev.getAction() == MotionEvent.ACTION_UP && touchInsideBorders) {
            isPressing = false;
            stopMomentum();
            if (longPressed)
                return true;
            if (!touchHor && !touchVer && Math.abs(startTouchX - ev.getRawX()) < touchDeadzone && Math.abs(startTouchY - ev.getRawY()) < touchDeadzone) {
                // if the user did not scroll horizontally or vertically and they're still within the deadzone
                affirmativeAction();
            } else if (Math.abs(startTouchX - ev.getRawX()) >= itemSize || Math.abs(startTouchY - ev.getRawY()) >= itemSize) {
                // else keep the momentum of where the user was scrolling
                long touchupTime = SystemClock.uptimeMillis();
                float totalTime = (touchupTime - touchMoveStartTime) / 1000f;
                if (touchHor)
                    momentumX = (startTouchX - ev.getRawX()) / totalTime; // pixels per second
                if (touchVer)
                    momentumY = (startTouchY - ev.getRawY()) / totalTime; // pixels per second
                if ((touchHor && Math.abs(momentumX) > 0) || (touchVer && Math.abs(momentumY) > 0))
                    momentumHandler.post(momentumRunner);
            } else {
                if (touchHor)
                    setShiftXToNearestColumn();
                if (touchVer)
                    setShiftYToNearestItem(colIndex);
            }
            touchHor = false;
            touchVer = false;
        }
        return true;
    }
    private void stopMomentum() {
        momentumX = 0;
        momentumY = 0;
        //momentumTravelDistX = 0;
        //momentumTravelDistY = 0;
        momentumHandler.removeCallbacks(momentumRunner);
    }
    private Handler momentumHandler = new Handler();
    private Runnable momentumRunner = new Runnable() {
        @Override
        public void run() {
            int milliInterval = MathHelpers.calculateMillisForFps(120);

            boolean hasMomentumX = Math.abs(momentumX) > 0;
            boolean hasMomentumY = Math.abs(momentumY) > 0;
            if (hasMomentumX) {
                // calculate travel distance based on current momentum
                float momentumOffsetX = momentumX * (milliInterval / 1000f);
                //momentumTravelDistX += momentumOffsetX;
                // decelerate the momentum
                if (momentumX > 0)
                    momentumX = Math.max(momentumX - momentumDeceleration * (milliInterval / 1000f), 0);
                else
                    momentumX = Math.min(momentumX + momentumDeceleration * (milliInterval / 1000f), 0);
                // get the difference between the items passed to see what should be applied this moment
                shiftX(momentumOffsetX);
            }
            if (hasMomentumY) {
                // calculate travel distance based on current momentum
                float momentumOffsetY = momentumY * (milliInterval / 1000f);
                //momentumTravelDistY += momentumOffsetY;
                // decelerate the momentum
                if (momentumY > 0)
                    momentumY = Math.max(momentumY - momentumDeceleration * (milliInterval / 1000f), 0);
                else
                    momentumY = Math.min(momentumY + momentumDeceleration * (milliInterval / 1000f), 0);
                // get the difference between the items passed to see what should be applied this moment
                shiftY(momentumOffsetY, XMBView.this.colIndex);
            }
            if (hasMomentumX || hasMomentumY) {
                momentumHandler.postDelayed(this, milliInterval);
            } else {
                setShiftXToNearestColumn();
                setShiftYToNearestItem(XMBView.this.colIndex);
            }
        }
    };

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        removeViews();
        createItemViews();
        createCatViews(); // to get catSize
        createItemViews();
        createInnerItemViews();
        int screenColCount = (int)Math.ceil(getWidth() / getHorShiftOffset()) + 4; //+4 for off screen animating into on screen
        catHorShift = horSpacing + (catSize + horSpacing) * (screenColCount / 6);
        Log.d("XMBView", "Size changed, setting cat shift to " + catHorShift);
        setViews(false, true);
    }

    private int getStartX() {
        int padding = getPaddingLeft();
        return Math.round(padding + catHorShift); //Where the current item's column is along the x-axis
    }
    private int getStartY() {
        int vsy = getHeight(); //view size y
        float vey = vsy / 2f; //view extents y
        return Math.round(vey - itemSize / 2f); //Where the current item's column is along the y-axis
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        for (int i = 0; i < getChildCount(); i++) {
            View view = getChildAt(i);

            if (view.getVisibility() == GONE)
                continue;

            int expX = 0;
            int expY = 0;
            int right = expX + getWidth();
            int bottom = expY + getHeight();
            view.measure(getWidth(), getHeight());
            view.layout(expX, expY, right, bottom);
        }
    }
    private void setViews(boolean indexChanged, boolean instant) {
        // indexChanged is true when currentIndex actually changes and false when otherwise
        // it is used to know when to apply fade transitions
        if (adapter != null && adapter.getItemCount(false) > 0) {
            int startX = getStartX() - Math.round(shiftX);
            int startY = getStartY();
            int horShiftOffset = Math.round(getHorShiftOffset());
            int verShiftOffset = Math.round(getVerShiftOffset());
            //if (moveMode && indexChanged)
            //    returnAllViews();
            drawCategories(indexChanged, startX, startY, horShiftOffset, instant || moveMode);
            drawItems(indexChanged, instant || moveMode, startX, startY, horShiftOffset, verShiftOffset);
            drawInnerItems(instant || moveMode);
        }
    }
    private void drawCategories(boolean indexChanged, int startXInt, int startYInt, int horShiftOffsetInt, boolean instant) {
        int viewWidth = getWidth();
        int viewHeight = getHeight();
        int origColIndex = this.colIndex;
        int prevColIndex = this.prevColIndex;

        for (int colIndex = 0; colIndex < adapter.getColumnCount(); colIndex++) {
            calcCatRect(startXInt, startYInt, horShiftOffsetInt, colIndex, reusableRect);
            boolean inBounds = inView(reusableRect, viewWidth, viewHeight);
            if (inBounds)
                drawItem(reusableRect, FADE_VISIBLE, instant, colIndex, 0);
            else
                returnItemView(colIndex, 0);

            if (adapter.getColumnSize(colIndex) == 1 && adapter.isColumnHead(colIndex, 0)) {
                // this column has only 1 item and it is meant to be a column head, so add an empty item below it
                calcItemRect(startXInt, startYInt, horShiftOffsetInt, 0, colIndex, 1, reusableRect);
                inBounds = inView(reusableRect, viewWidth, viewHeight);

                int fadeTransition = FADE_VISIBLE;
                if (!instant && indexChanged && colIndex == origColIndex && prevColIndex != origColIndex)
                    fadeTransition = FADE_IN;
                else if (!instant && indexChanged && colIndex != origColIndex && colIndex == prevColIndex)
                    fadeTransition = FADE_OUT;
                else if (((!indexChanged || instant) && colIndex != origColIndex) || ((indexChanged || instant) && colIndex != origColIndex && colIndex != prevColIndex))
                    fadeTransition = FADE_INVISIBLE;
                if (colIndex >= origColIndex - 1 && colIndex <= origColIndex + 1 && inBounds)
                    drawItem(reusableRect, fadeTransition, instant, colIndex, 1);
                else
                    returnItemView(colIndex, 1);
            }
        }
    }
    private void drawItems(boolean indexChanged, boolean instant, int startXInt, int startYInt, int horShiftOffsetInt, int verShiftOffsetInt) {
        int origColIndex = this.colIndex;
        int prevColIndex = this.prevColIndex;
        int viewWidth = getWidth();
        int viewHeight = getHeight();
        for (int itemColIndex = 0; itemColIndex < adapter.getColumnCount(); itemColIndex++) {
            // skip items that belong to a column with one item, those are drawn as categories
            if (!catHasSubItems(itemColIndex))
                continue;
            // start from one since the 0th item will always be the column head
            for (int itemRowIndex = 1; itemRowIndex < adapter.getColumnSize(itemColIndex); itemRowIndex++) {
                calcItemRect(startXInt, startYInt, horShiftOffsetInt, verShiftOffsetInt, itemColIndex, itemRowIndex, reusableRect);

                boolean inBounds = inView(reusableRect, viewWidth, viewHeight);
                int fadeTransition = FADE_VISIBLE;
                if (!instant && indexChanged && itemColIndex == origColIndex && prevColIndex != origColIndex)
                    fadeTransition = FADE_IN;
                else if (!instant && indexChanged && itemColIndex != origColIndex && itemColIndex == prevColIndex)
                    fadeTransition = FADE_OUT;
                else if (((!indexChanged || instant) && itemColIndex != origColIndex) || ((indexChanged || instant) && itemColIndex != origColIndex && itemColIndex != prevColIndex))
                    fadeTransition = FADE_INVISIBLE;
                if (itemColIndex >= origColIndex - 1 && itemColIndex <= origColIndex + 1 && inBounds)
                    drawItem(reusableRect, fadeTransition, instant, itemColIndex, itemRowIndex);
                else
                    returnItemView(itemColIndex, itemRowIndex);
            }
        }
    }
    private void drawInnerItems(boolean instant) {
        for (int itemColIndex = 0; itemColIndex < adapter.getColumnCount(); itemColIndex++)
            for (int itemRowIndex = 0; itemRowIndex < adapter.getColumnSize(itemColIndex); itemRowIndex++)
                if (adapter.hasInnerItems(itemColIndex, itemRowIndex))
                    drawInnerItems(instant, itemColIndex, itemRowIndex);
    }
    private void drawInnerItems(boolean instant, Integer... position) {
        int viewWidth = getWidth();
        int viewHeight = getHeight();
        Integer[] currentEntry = getEntryPosition();
        Integer[] innerPosition = Arrays.copyOf(position, position.length + 1);
        // draw list of inner items
        for (int innerItemIndex = 0; innerItemIndex < adapter.getInnerItemCount(position); innerItemIndex++) {
            innerPosition[innerPosition.length - 1] = innerItemIndex;
            // if the inner item has inner items of its own, then draw them
            if (adapter.hasInnerItems(innerPosition))
                drawInnerItems(instant, innerPosition);
            // if the current inner item is where the user is right now, then draw it
            if (currentEntry != null && (currentEntry.length > 0 && (((innerPosition.length - 1) == currentEntry.length) && isPartOfPosition(innerPosition, currentEntry)) || isPartOfPosition(currentEntry, innerPosition))) {
                calcInnerItemRect(reusableRect, getStartX(), getStartY(), Math.round(innerHorSpacing), Math.round(innerVerSpacing), innerPosition);
                boolean inBounds = inView(reusableRect, viewWidth, viewHeight);
                if (inBounds)
                    drawItem(reusableRect, FADE_VISIBLE, instant, innerPosition);
                else
                    returnItemView(innerPosition);
            } else
                returnItemView(innerPosition);
        }
    }
    private static final int FADE_VISIBLE = 0;
    private static final int FADE_INVISIBLE = 1;
    private static final int FADE_OUT = 2;
    private static final int FADE_IN = 3;
    private void drawItem(Rect itemBounds, int fadeTransition, boolean instant, Integer... itemPosition) {
        boolean isCat = itemPosition.length == 2 && itemPosition[1] == 0;
        boolean isInnerItem = itemPosition.length > 2;
        ViewHolder viewHolder = getViewHolder(isCat ? CATEGORY_TYPE : isInnerItem ? INNER_TYPE : ITEM_TYPE, itemPosition);
        viewHolder.setX(itemBounds.left);
        viewHolder.setY(itemBounds.top);
        Integer[] currentPosition = getPosition();
        boolean isSelection = isSamePosition(currentPosition, itemPosition);//getTotalIndexFromTraversable(currentIndex) == totalIndex;
        boolean isPartOfPosition = isPartOfPosition(currentPosition, itemPosition);
        // set z positions
        float z = ITEM_Z;
        if (isInnerItem || (isPartOfPosition && isInsideItem()))
            z = INNER_Z;
        if (isCat)
            z = CAT_Z;
        viewHolder.itemView.setTranslationZ(z);
        boolean isOurCat = itemPosition[0] == this.colIndex && itemPosition[1] == 0;
        viewHolder.isHighlighted = moveMode && ((!columnMode && isSelection) || (columnMode && isOurCat));
        viewHolder.requestHideTitle = isInsideItem() && isPartOfPosition && !isSelection;
        float itemAlpha = (isPartOfPosition || (!isInsideItem() && isOurCat) || isInnerItem) ? fullItemAlpha : (isInsideItem() ? innerItemOverlayTranslucent : translucentItemAlpha);
        viewHolder.itemView.animate().cancel();

        adapter.onBindViewHolder(viewHolder, itemPosition);
        if (viewHolder.isNew || instant) {
            // if the view just popped into existence then set its position to the final position rather than transitioning
            viewHolder.itemView.setX(viewHolder.getX());
            viewHolder.itemView.setY(viewHolder.getY());
        } else {
            // if this view already existed then animate it from where it was to the final position
            viewHolder.itemView.setX(viewHolder.getPrevX());
            viewHolder.itemView.setY(viewHolder.getPrevY());
            viewHolder.itemView.animate().setDuration(300);
            viewHolder.itemView.animate().xBy(viewHolder.getX() - viewHolder.getPrevX());
            viewHolder.itemView.animate().yBy(viewHolder.getY() - viewHolder.getPrevY());
        }
        switch (fadeTransition) {
            case FADE_VISIBLE:
                viewHolder.itemView.setAlpha(itemAlpha);
                break;
            case FADE_INVISIBLE:
                viewHolder.itemView.setAlpha(0);
                break;
            case FADE_OUT:
                viewHolder.itemView.animate().setDuration(300);
                viewHolder.itemView.animate().alphaBy(-viewHolder.itemView.getAlpha());
                break;
            case FADE_IN:
                viewHolder.itemView.setAlpha(0);
                viewHolder.itemView.animate().setDuration(300);
                viewHolder.itemView.animate().alphaBy(itemAlpha);
                break;
        }
    }
    private boolean isSamePosition(Integer[] position, Integer... position2) {
        if (position.length != position2.length)
            return false;
        else
            for (int i = 0; i < position.length; i++)
                if (position[i] != position2[i])
                    return false;
        return true;
    }
    private boolean isPartOfPosition(Integer[] fullPosition, Integer... part) {
        if (part.length > fullPosition.length || fullPosition.length <= 0 || part.length <= 0)
            return false;
        else
            for (int i = 0; i < part.length; i++)
                if (fullPosition[i] != part[i])
                    return false;
        return true;
    }
    private void removeViews() {
        returnAllViews();
        while (!goneCatViews.isEmpty())
            removeView(goneCatViews.pop().itemView);
        while (!goneItemViews.isEmpty())
            removeView(goneItemViews.pop().itemView);
    }
    private void createItemViews() {
        if (adapter == null)
            return;
        for (int i = 0; i < 5; i++) {
            ViewHolder newHolder = adapter.onCreateViewHolder(this, ITEM_TYPE);
            newHolder.itemViewType = ITEM_TYPE;
            newHolder.itemView.setVisibility(GONE);
            addView(newHolder.itemView);
            newHolder.itemView.measure(getWidth(), getHeight());
            itemSize = Math.max(newHolder.itemView.getMeasuredWidth(), newHolder.itemView.getMeasuredHeight());
            adapter.onViewAttachedToWindow(newHolder);
            goneItemViews.push(newHolder);
        }
    }
    private void createCatViews() {
        if (adapter == null)
            return;
        for (int i = 0; i < 5; i++) {
            ViewHolder newHolder = adapter.onCreateViewHolder(this, CATEGORY_TYPE);
            newHolder.itemViewType = CATEGORY_TYPE;
            newHolder.itemView.setVisibility(GONE);
            addView(newHolder.itemView);
            newHolder.itemView.measure(getWidth(), getHeight());
            catSize = Math.max(newHolder.itemView.getMeasuredWidth(), newHolder.itemView.getMeasuredHeight());
            adapter.onViewAttachedToWindow(newHolder);
            goneCatViews.push(newHolder);
        }
    }
    private void createInnerItemViews() {
        if (adapter == null)
            return;
        for (int i = 0; i < 5; i++) {
            ViewHolder newHolder = adapter.onCreateViewHolder(this, INNER_TYPE);
            newHolder.itemViewType = INNER_TYPE;
            newHolder.itemView.setVisibility(GONE);
            addView(newHolder.itemView);
            newHolder.itemView.measure(getWidth(), getHeight());
            innerItemSize = Math.min(newHolder.itemView.getMeasuredWidth(), newHolder.itemView.getMeasuredHeight());
            adapter.onViewAttachedToWindow(newHolder);
            goneInnerItemViews.push(newHolder);
        }
    }
    private void returnItemView(Integer... position) {
        int indexHash = MathHelpers.hash(position);//position[0] >= 0 ? mapPosition(position) : position);
        if (usedViews.containsKey(indexHash)) {
            ViewHolder viewHolder = usedViews.get(indexHash);
            viewHolder.itemView.setVisibility(GONE);
            if (viewHolder.getItemViewType() == CATEGORY_TYPE)
                goneCatViews.push(viewHolder);
            else if (viewHolder.getItemViewType() == ITEM_TYPE)
                goneItemViews.push(viewHolder);
            else if (viewHolder.getItemViewType() == INNER_TYPE)
                goneInnerItemViews.push(viewHolder);
            usedViews.remove(indexHash);
        }
    }
    private void returnAllViews() {
        for (ViewHolder viewHolder : usedViews.values()) {
            viewHolder.itemView.setVisibility(GONE);
            if (viewHolder.getItemViewType() == CATEGORY_TYPE)
                goneCatViews.push(viewHolder);
            else if (viewHolder.getItemViewType() == ITEM_TYPE)
                goneItemViews.push(viewHolder);
            else if (viewHolder.getItemViewType() == INNER_TYPE)
                goneInnerItemViews.push(viewHolder);
        }
        usedViews.clear();
    }
    private ViewHolder getViewHolder(int viewType, Integer... position) {
        ViewHolder viewHolder = null;
        int indexHash = MathHelpers.hash(position);
        if (usedViews.containsKey(indexHash)) {
            viewHolder = usedViews.get(indexHash);
            viewHolder.isNew = false;
        } else {
            if (viewType == CATEGORY_TYPE) {
                if (goneCatViews.isEmpty())
                    createCatViews();

                viewHolder = goneCatViews.pop();
                viewHolder.isNew = true;
                viewHolder.itemView.setVisibility(VISIBLE);
                usedViews.put(indexHash, viewHolder);
            } else if (viewType == ITEM_TYPE) {
                if (goneItemViews.isEmpty())
                    createItemViews();

                viewHolder = goneItemViews.pop();
                viewHolder.isNew = true;
                viewHolder.itemView.setVisibility(VISIBLE);
                usedViews.put(indexHash, viewHolder);
            } else if (viewType == INNER_TYPE) {
                if (goneInnerItemViews.isEmpty())
                    createInnerItemViews();

                viewHolder = goneInnerItemViews.pop();
                viewHolder.isNew = true;
                viewHolder.itemView.setVisibility(VISIBLE);
                usedViews.put(indexHash, viewHolder);
            }
        }
        return viewHolder;
    }

    private boolean inView(Rect rect, int viewWidth, int viewHeight) {
        int horShiftOffset = Math.round(getHorShiftOffset());
        int verShiftOffset = Math.round(getVerShiftOffset());
        int left = -horShiftOffset;
        int right = viewWidth + horShiftOffset;
        int top = -verShiftOffset;
        int bottom = viewHeight + verShiftOffset;
        return ((rect.left < right && rect.left > left) || (rect.right > left && rect.right < right)) && ((rect.top < bottom && rect.top > top) || (rect.bottom > top && rect.bottom < bottom));
    }

    // calculates the item's rect with the text below the item
    private void calcCatRect(int startX, int startY, int horShiftOffset, int colIndex, Rect rect) {
        Integer[] currentPosition = getEntryPosition();
        // get the horizontal pixel position of the item (using getStartX() instead of startX because startX has shiftX in it)
        int expX = (isInsideItem() && isPartOfPosition(getPosition(), colIndex, 0) ? getStartX() + innerItemEntryPos.size() * -Math.round(innerItemSize + innerHorSpacing) : startX + horShiftOffset * colIndex);//getColIndexFromTotal(totalIndex));
        // the vertical pixel position is the same since the categories go along a straight line
        int expY = startY;
        // get the right and bottom values of the item relative to the left and top values and apply them to the rect
        int right = expX + catSize;// + Math.max(iconSize, rect.width());
        int bottom = expY + catSize;// + textCushion + rect.height();
        rect.set(expX, expY, right, bottom);
    }
    // calculates the item's rect size with the text to the right of the item
    private void calcItemRect(int startX, int startY, int horShiftOffset, int verShiftOffset, int colIndex, int localIndex, Rect rect) {
        boolean isPartOfInsideItem = isPartOfPosition(getPosition(), colIndex, localIndex);
        // get the index of what is actually highlighted currently within the column
        int itemCatIndex = getCachedIndexOfCat(colIndex);
        int halfCatDiff = Math.round(Math.abs(catSize - itemSize) / 2f);
        // get the horizontal pixel position of the item (using getStartX() instead of startX because startX has shiftX in it)
        int expX = halfCatDiff + (isInsideItem() && isPartOfInsideItem ? getStartX() + innerItemEntryPos.size() * -Math.round(innerItemSize + innerHorSpacing) : startX + horShiftOffset * colIndex);
        // get the vertical pixel position of the item (localIndex is set to -1 since this is a sub-item)
        int expY = halfCatDiff + (isInsideItem() && isPartOfInsideItem ? startY : Math.round((startY - catPos.get(colIndex)) + verShiftOffset * (localIndex - 1) + ((localIndex - 0) >= itemCatIndex ? catSize + adapter.getTextSize() + subItemGap : 0)));
        // get the right and bottom values of the item relative to the left and top values and apply them to the rect
        int right = expX + itemSize;// + textCushion + rect.width();
        int bottom = expY + itemSize;
        rect.set(expX, expY, right, bottom);
    }
    private void calcInnerItemRect(Rect rect, int startX, int startY, int horSpacing, int verSpacing, Integer... position) {
        int halfCatDiff = Math.round(Math.abs(catSize - innerItemSize) / 2f);

        int discX = ((position.length - 2) - innerItemEntryPos.size());
        // get the horizontal pixel position of the item
        int expX = startX + halfCatDiff + discX * innerItemSize + (discX + 1) * horSpacing; // discX + 1 so that the first inner items can be offset as well
        boolean isCurrentInner = (position.length - 2) > innerItemEntryPos.size();
        // get the vertical pixel position of the item
        int expY = startY + halfCatDiff + (isCurrentInner ? position[position.length - 1] * (innerItemSize + verSpacing) - Math.round(innerItemVerPos.peek()) : 0);
        // get the right and bottom values of the item relative to the left and top values and apply them to the rect
        int right = expX + innerItemSize;// + textCushion + rect.width();
        int bottom = expY + innerItemSize;
        rect.set(expX, expY, right, bottom);
    }

    public Object getSelectedItem() {
        return adapter.getItem(getPosition());
    }

    public boolean catHasSubItems(int colIndex) {
        return adapter != null && adapter.getColumnSize(colIndex) > 1;
    }

    protected void setIndex(int colIndex, int localIndex, boolean instant) {
        if ((colIndex != this.colIndex || localIndex != this.rowIndex) && adapter.getItemCount(false) > 0)
            setShiftXAndY(getShiftX(colIndex), getShiftY(localIndex, colIndex), instant);
    }
    protected Integer[] getPosition() {
        Integer[] position;
        if (isInsideItem()) {
            position = new Integer[innerItemEntryPos.size() + 3]; // 2 for colIndex and rowIndex and 1 for the local index within the current item
            //innerItemEntryPos.copyInto(position);
            position[0] = this.colIndex;
            position[1] = this.rowIndex;
            if (innerItemEntryPos.size() > 0) {
                Stack<Integer> entryClone = (Stack<Integer>)innerItemEntryPos.clone();
                for (int i = position.length - 2; i > 1; i--)
                    position[i] = entryClone.pop();
            }
            int nextEntry = innerYToIndex();//Arrays.copyOf(innerItemEntryPos.toArray(), innerItemEntryPos.size(), Integer[].class));
            position[position.length - 1] = nextEntry;
        } else {
            //int colIndex = getColIndex();
            //int localIndex = getLocalIndex() + (catHasSubItems(colIndex) ? 1 : 0);
            position = new Integer[] { colIndex, rowIndex };//getTotalIndexFromTraversable(currentIndex) };
        }
        return position;
    }
    protected Integer[] getEntryPosition() {
        Integer[] position = null;
        if (isInsideItem()) {
            position = new Integer[innerItemEntryPos.size() + 2]; // 2 for colIndex and rowIndex and 1 for the local index within the current item
            //innerItemEntryPos.copyInto(position);
            position[0] = this.colIndex;
            position[1] = this.rowIndex;
            if (innerItemEntryPos.size() > 0) {
                Stack<Integer> entryClone = (Stack<Integer>)innerItemEntryPos.clone();
                for (int i = position.length - 1; i > 1; i--)
                    position[i] = entryClone.pop();
            }
        }
        return position;
    }
    protected int getColIndex() {
        return this.colIndex;
    }
    protected int getLocalIndex() {
        return this.rowIndex;
    }

    private int getCachedIndexOfCat(int colIndex) {
        return yToIndex(catPos.get(colIndex), colIndex);// + (catHasSubItems(colIndex) ? 1 : 0);
    }

    public void selectLowerItem() {
        stopMomentum();
        shiftYDisc(1);
    }
    public void selectUpperItem() {
        stopMomentum();
        shiftYDisc(-1);
    }
    public void selectRightItem() {
        stopMomentum();
        shiftXDisc(1);
    }
    public void selectLeftItem() {
        stopMomentum();
        shiftXDisc(-1);
    }
    public boolean affirmativeAction() {
        stopMomentum();
        if (moveMode) {
            applyMove();
            return true;
        } else {
            Integer[] position = getPosition();
            int nextEntry = position[position.length - 1]; // this has to come before the mapping since it is what is pushed into innerItemEntryPos and we want to keep total index in there, not the mapped index
            //position[0] = mapTotalIndex(position[0]);
            boolean hasInnerItems = adapter.hasInnerItems(position);

            if (hasInnerItems) {
                if (isInsideItem())
                    innerItemEntryPos.push(nextEntry);
                innerItemVerPos.push(0f);
                setViews(false, false);
                return true;
            }
        }
        return false;
    }
    public boolean secondaryAction() {
        stopMomentum();
        return false;
    }
    public boolean cancelAction() {
        stopMomentum();
        if (moveMode) {
            // TODO: add way to revert changes
            toggleMoveMode(false, false);
            //setViews(false, true);
            return true;
        } else if (isInsideItem()) {
            if (innerItemEntryPos.size() > 0) // need this check since first entry is colIndex and rowIndex
                innerItemEntryPos.pop();
            innerItemVerPos.pop();
            setViews(false, false);
            return true;
        }
        return false;
    }
    public boolean isInMoveMode() {
        return moveMode;
    }
    public void toggleMoveMode(boolean onOff, boolean columnMode) {
        if (moveMode && columnMode)
            throw new RuntimeException("Cannot enter column move mode from move mode");

        moveMode = onOff;
        this.columnMode = columnMode;
        if (moveMode) {
            //moveColIndex = getColIndex();
            //moveLocalIndex = getLocalIndex();
            origMoveColIndex = this.colIndex;
            origMoveLocalIndex = this.rowIndex;// + (catHasSubItems(moveColIndex) ? 1 : 0);
        } else
            this.columnMode = false;
        //setAdapterHighlightColor(moveMode ? moveHighlightColor : normalHighlightColor);
        setViews(false, true);
        //refresh();
    }
    private void applyMove() {
        if (this.colIndex != origMoveColIndex || this.rowIndex != origMoveLocalIndex)
            onAppliedMove(origMoveColIndex, origMoveLocalIndex, this.colIndex, this.rowIndex);
        toggleMoveMode(false, false);
    }
    protected void onAppliedMove(int fromColIndex, int fromLocalIndex, int toColIndex, int toLocalIndex) {
        // local indices are total within the columns they represent and not traversable
    }
    protected void onShiftHorizontally(int fromColIndex, int fromRowIndex, int toColIndex) {
        if (moveMode) {
            if (columnMode) {
                int nextColIndex = Math.min(Math.max(toColIndex, 0), adapter.getColumnCount() - 2);
                //if (nextColIndex != fromColIndex)
                    adapter.shiftColumnTo(fromColIndex, nextColIndex);
            } else {
                //Log.d("XMBView", "OnShift [" + fromColIndex + ", " + fromRowIndex + "] => " + toColIndex);
                boolean isInColumn = catHasSubItems(fromColIndex);
                // -1 to move out of the 0th column, columnCount - 2 to not go past the settings
                int nextColIndex = Math.min(Math.max(toColIndex, isInColumn ? -1 : 0), adapter.getColumnCount() - (isInColumn ? 1 : 2));
                //Log.d("XMBView", "Attempting to move right from " + moveColIndex + " to " + nextColIndex + ", in column: " + isInColumn);
                if (fromColIndex != nextColIndex) {
                    nextColIndex = Math.max(nextColIndex, 0);
                    boolean nextIsColumn = adapter.isColumnHead(nextColIndex, 0);
                    boolean isGoingRight = nextColIndex > fromColIndex;
                    int nextLocalIndex = isInColumn ? 0 : getCachedIndexOfCat(nextColIndex);
                    // the index we will set the XMBView to once the shift is done
                    int setColIndex = nextColIndex;
                    if (isGoingRight) {
                        if (!isInColumn && nextIsColumn)
                            // since the item is not in a column and its about to enter a column, then we need to account for the removal of the items column
                            setColIndex = fromColIndex;
                    } else {
                        if (isInColumn) {
                            // if the item is being moved to the left and it is currently a sub-item, then set the next index to be the same index we are coming from
                            nextColIndex = fromColIndex;
                            setColIndex = fromColIndex;
                        }
                    }
                    if (nextIsColumn && adapter.getColumnSize(nextColIndex) == 1)
                        nextLocalIndex = 1;
                    adapter.shiftItemHorizontally(fromColIndex, fromRowIndex, nextColIndex, nextLocalIndex, isInColumn || !nextIsColumn);

                    this.colIndex = setColIndex;
                } else
                    this.colIndex = fromColIndex;

                this.rowIndex = getCachedIndexOfCat(this.colIndex);
                this.shiftX = getShiftX(this.colIndex);
            }
        }
    }
    protected void onShiftVertically(int fromColIndex, int fromLocalIndex, int toLocalIndex) {
        if (moveMode)
            adapter.shiftItemVertically(fromColIndex, fromLocalIndex, toLocalIndex);
    }
}
