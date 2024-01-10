package com.OxGames.OxShell.Views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.OxGames.OxShell.Helpers.MathHelpers;
import com.OxGames.OxShell.Interfaces.XMBAdapterListener;
import com.OxGames.OxShell.OxShellApp;
import com.OxGames.OxShell.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;

public class XMBView extends ViewGroup {// implements InputReceiver {//, Refreshable {
    private static final float EPSILON = 0.0001f;
    protected static final int ROOT_INDEX = -1;
    protected final Context context;
    public static final int CATEGORY_TYPE = 0;
    public static final int ITEM_TYPE = 1;
    public static final int INNER_TYPE = 2;
    public static final float ITEM_Z = -1;
    public static final float CAT_Z = 0;
    public static final float INNER_Z = 1;


    // col index represents which column we are currently in
//    int colIndex = 0;
//    int prevColIndex = 0;
    // row index represents which item we are at within a column
//    int rowIndex = 0;
//    private final Stack<Float> posShift;
//    private final Stack<Float> prevPosShift;
    // keeps track of where we were in each item so when we go back to it, it is still the same
    private final HashMap<Integer, Float> currentShifts;
    private final HashMap<Integer, Float> prevShifts;
    // how deep to travel in prevShifts to create our current position
    // 0=colItems 1=subItems 2=1stInnerItems ...
    private int currentDepth;
    private int prevDepth;

    private float fullItemAlpha = 1f; // the alpha value of the item(s) selected
    private float translucentItemAlpha = 0.33f; // the alpha value of the item(s) not in selection
    private float innerItemOverlayTranslucent = 0.066f; // the alpha value of items not within the inner items

    private Adapter adapter;
    private final Stack<ViewHolder> goneCatViews;
    private final Stack<ViewHolder> goneItemViews; //The views whose visibility are set to gone since they are not currently used
    private final Stack<ViewHolder> goneInnerItemViews;
    private final HashMap<Integer, ViewHolder> usedViews; //The views that are currently displayed and the hashed total index of the item they represent as their key
//    private final Stack<Integer> innerItemEntryPos; // The indices that represent where we entered from, when empty the entry is colIndex and rowIndex
//    private final Stack<Float> innerItemVerPos; // The y scroll value of the inner items menu
//    private final ArrayList<Float> catPos; // go from 0 to (adapter.getColumnSize(colIndex) - 1) * getVerShiftOffset()
    private boolean moveMode;
    private final Stack<Integer> moveIndex;
    private final Stack<Integer> origMoveIndex;
//    private boolean columnMode;
//    private int moveLocalIndex; // the current index of the column in move mode
//    private int origMoveColIndex;
//    private int origMoveLocalIndex;
    //private int moveColIndex;
    //private int moveLocalIndex;

    private XMBAdapterListener adapterListener = new XMBAdapterListener() {
        @Override
        public void onItemAdded(Integer... position) {
//            if (position.length == 1) {
//                // column added
//                int columnIndex = position[0];
//                catPos.add(columnIndex, 0f);
//                if (posIndex.firstElement() >= columnIndex) {
//                    int maxIndex = getAdapter().getColumnCount() - 1;
//                    colIndex = Math.min(colIndex + 1, maxIndex);
////                    prevColIndex += Math.min(prevColIndex + 1, maxIndex);
//                }
//                rowIndex = getCachedIndexOfCat(colIndex);
//                shiftX = getShiftX(colIndex);
//                //returnAllViews();
//                setViews(false, true);
//            } else {
//                // sub-item or inner item added
//                int columnIndex = position[0];
//                if (columnIndex == colIndex)
//                    rowIndex = getCachedIndexOfCat(colIndex);
//                //returnAllViews();
//                //getViewHolder(columnIndex, localIndex).setDirty();
//                setViews(false, false);
//            }
        }
        @Override
        public void onInnerItemsChanged(Integer... position) {
            Log.d("XMBView", "Inner items changed of " + Arrays.toString(position) + " current position is " + Arrays.toString(getPosition()));
            // in case the column was empty and the user was on the column, change our position to be the first item of the column rather than the column head
//            if (!isInsideItem() && isSamePosition(position, getPosition()) && adapter.getColumnSize(position[0]) > 0)
//                rowIndex = 0;
//            setViews(false, false);
        }

        @Override
        public void onItemRemoved(Integer... position) {
//            if (position.length > 1) {
//                // sub-item or inner item removed
//                if (position[0] == colIndex)
//                    rowIndex = getCachedIndexOfCat(colIndex);
//                setViews(false, false);
//            } else {
//                // column removed
//                int columnIndex = position[0];
//                catPos.remove(columnIndex);
//                if (columnIndex <= colIndex) {
//                    colIndex = Math.max(colIndex - 1, 0);
////                    prevColIndex = Math.max(prevColIndex - 1, 0);
//                }
//                rowIndex = getCachedIndexOfCat(colIndex);
//                shiftX = getShiftX(colIndex);
//                //returnAllViews();
//                setViews(false, true);
//            }
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
        //catPos = new ArrayList<>();
        //innerItemEntryPos = new Stack<>();
        //innerItemVerPos = new Stack<>();
        //posShift = new Stack<>();
        currentShifts = new HashMap<>();
        prevShifts = new HashMap<>();
        moveIndex = new Stack<>();
        origMoveIndex = new Stack<>();

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
    public void setFont(Typeface font) {
        if (adapter != null)
            adapter.setFont(font);
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
        public abstract Integer[] getPosition(Object item);
        public abstract ArrayList<Object> getItems();
        public abstract void setItems(ArrayList<Object> items);
        public abstract boolean hasChildren(Integer... position);
        public abstract int getChildCount(Integer... position);
        public abstract boolean isColumnHead(Integer... position);
        public abstract boolean canPlaceItemsIn(Integer... position);

        public abstract void setFont(Typeface font);
        protected abstract int getTextSize();
        protected abstract Integer[] shiftItemHorizontally(int amount, Integer... position);
        protected abstract Integer[] shiftItemVertically(int amount, Integer... position);
        public abstract void addItem(Object toBeAdded, Integer... position);
        public abstract void removeItem(Integer... position);
//        public abstract void createColumnAt(int columnIndex, Object head);
    }
    public abstract static class ViewHolder {
        protected View itemView;
        protected boolean isDirty;
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
        public void setDirty() {
            isDirty = true;
        }
        public void setHighlighted(boolean onOff) {
            if (isHighlighted != onOff)
                setDirty();
            isHighlighted = onOff;
        }
        public void setHideTitle(boolean onOff) {
            if (requestHideTitle != onOff)
                setDirty();
            requestHideTitle = onOff;
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
//        this.catPos.clear();
        this.currentShifts.clear();
        if (this.adapter != null)
            this.adapter.removeListener(adapterListener);
        this.adapter = adapter;
        this.adapter.addListener(adapterListener);
//        this.colIndex = 0;
//        this.prevColIndex = 0;
        this.moveMode = false;
//        this.columnMode = false;
//        this.innerItemEntryPos.clear();
//        this.innerItemVerPos.clear();
//        this.rowIndex = (adapter != null && adapter.getColumnCount() > 0 && adapter.getColumnSize(this.colIndex) > 0) ? 1 : 0;
//        this.rowIndex = 0;
        removeViews();
        this.currentDepth = 0;
        this.prevDepth = 0;
        this.currentShifts.clear();
        this.prevShifts.clear();
        //for (int i = 0; i < adapter.getColumnCount(); i++)
        //    this.catPos.add(0f);
        //setShiftXAndY(0, 0, true);
    }
    public Adapter getAdapter() {
        return adapter;
    }

    // for fine control of the menu
//    private float shiftX = 0; // goes from 0 to (adapter.getColumnCount() - 1) * getHorShiftOffset()

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
    private float getOffsetSize(Integer... parentIndex) {
        float offsetSize;
        if (parentIndex == null || parentIndex.length == 0) {
            // column items
            offsetSize = getHorShiftOffset();
        } else if (parentIndex.length == 1) {
            // sub-items
            offsetSize = getVerShiftOffset();
        } else {
            // inner items
            offsetSize = (innerItemSize + innerVerSpacing);
        }
        return offsetSize;
    }
    // finds the child index closest to the given shift value within the parent index
    // shift value is expected to be given between 0 and 1 inclusive
    private int shiftValToIndex(float shiftValue, Integer... parentIndex) {
        float offsetSize = getOffsetSize(parentIndex);
        return Math.round(denormalizeValue(shiftValue, parentIndex) / offsetSize);
    }
    // converts the given child index to the shift value it would be within the parent index
    // shift value is returned as between 0 and 1 inclusive
    private float indexToShiftVal(int index, Integer... parentIndex) {
        return normalizeValue(index * getOffsetSize(parentIndex), parentIndex);
    }
    private float normalizeValue(float value, Integer... parentIndex) {
        return value / ((adapter.getChildCount(parentIndex) - 1) * getOffsetSize(parentIndex));
    }
    private float denormalizeValue(float value, Integer... parentIndex) {
        return value * ((adapter.getChildCount(parentIndex) - 1) * getOffsetSize(parentIndex));
    }
    private void shiftBy(float amount, Integer... parentIndex) {
        float currentShift = getShiftOf(parentIndex);
        setShiftOf(currentShift + amount, parentIndex);
    }
    private void shiftByDisc(int amount, Integer... parentIndex) {
        setShiftOf(
            indexToShiftVal(
            shiftValToIndex(
                    getShiftOf(parentIndex),
                    parentIndex
                ) + amount,
                parentIndex
            ),
            parentIndex
        );
    }
    private int getHashOf(Integer... index) {
        int indexHash;
        if (index != null && index.length > 0)
            indexHash = MathHelpers.hash(index);
        else
            indexHash = MathHelpers.hash(ROOT_INDEX);
        return indexHash;
    }
    private float getPrevShiftOf(Integer... parentIndex) {
        int parentHash = getHashOf(parentIndex);
        float prevShift = 0;
        if (prevShifts.containsKey(parentHash))
            prevShift = prevShifts.get(parentHash);
        return prevShift;
    }
    private float getShiftOf(Integer... parentIndex) {
        int parentHash = getHashOf(parentIndex);
        float currentShift = 0;
        if (currentShifts.containsKey(parentHash))
            currentShift = currentShifts.get(parentHash);
        return currentShift;
    }
    private void setShiftOfToNearestChild(Integer... parentIndex) {
        setShiftOf(
            indexToShiftVal(
                shiftValToIndex(
                    getShiftOf(parentIndex),
                    parentIndex),
                parentIndex
            ),
            parentIndex
        );
    }
    // sets the shift amount of the children of the given index
    // shift values are clamped between 0 and 1 inclusive
    private void setShiftOf(float shiftValue, Integer... parentIndex) {
//        float clampedShift = MathHelpers.clamp(shiftValue, 0, 1);
        float prevShift = getShiftOf(parentIndex);
        if (Math.abs(prevShift - shiftValue) > EPSILON) {
            Integer[] prevPos = getPosition();
            int parentHash = getHashOf(parentIndex);
            Log.d("XMBView", "Setting shift of " + Arrays.toString(parentIndex) + " who has a hash of " + parentHash + " to " + shiftValue);
            prevShifts.put(parentHash, prevShift);
            currentShifts.put(parentHash, MathHelpers.clamp(shiftValue, 0, 1));
            prevDepth = currentDepth;
            if (!isInsideItem()) {
                // if we are not currently inside an item then set our depth based on
                // whether the cat we are currently on has any sub-items or not
                currentDepth = adapter.hasChildren(shiftValToIndex(getShiftOf())) ? 1 : 0;
            }
            Integer[] newPos = getPosition();
            Log.d("XMBView", Arrays.toString(prevPos) + " => " + Arrays.toString(newPos));
            boolean changed = !isSamePosition(newPos, prevPos);
            if (changed)
                onShifted(newPos[newPos.length - 1] - prevPos[prevPos.length - 1], prevPos);
            setViews(changed, false);
        }
    }
    public boolean isInsideItem() {
        return currentDepth > 1;
//        return posShift.size() > 2;
    //        return innerItemVerPos.size() > 0;
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
    //private float minDiffX = 10f;
    private float startTouchY = 0;
    private float pseudoStartY = 0;
    private float momentumY = 0;
    private float prevY = 0;
    //private float minDiffY = 10f;
    //private int startTouchColIndex = 0;
    //private int startTouchRowIndex = 0;
    private int startTouchIndex = 0;
    private Integer[] startEntryIndex = null;
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
            startEntryIndex = getEntryPosition();
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
                startTouchIndex = getPosition()[0];
                //startTouchColIndex = this.colIndex;
                //startTouchRowIndex = this.rowIndex;
                touchMoveDir = Math.signum(currentX - startTouchX);
                touchMoveStartTime = SystemClock.uptimeMillis();
                pseudoStartX = touchMoveDir * touchDeadzone + startTouchX;
                touchHor = true;
            }
            // only acknowledge moving vertically if there are items to move vertically through
            //if (!touchHor && !touchVer && Math.abs(currentY - startTouchY) >= touchDeadzone && (isInsideItem() || catHasSubItems(colIndex))) {
            if (!touchHor && !touchVer && Math.abs(currentY - startTouchY) >= touchDeadzone && adapter.hasChildren(startEntryIndex)) {
                Integer[] position = getPosition();
                startTouchIndex = position[position.length - 1];
                //startTouchColIndex = this.colIndex;
                //startTouchRowIndex = this.rowIndex;
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
                //shiftX(diffX);// * 0.2f);
                shiftBy(normalizeValue(diffX));
                if (getPosition()[0] != startTouchIndex) {
                    // if the index changed then set our drag start value to be where we are right now
                    pseudoStartX = currentX;
                    startTouchIndex = getPosition()[0];
                    //startTouchColIndex = this.colIndex;
                }
            }// else
            prevX = currentX;
            if (touchVer) {
                if (touchMoveDir != Math.signum(pseudoStartY - currentY)) {
                    // if the movement direction changed, then update the start time to reflect when the change happened
                    touchMoveDir = Math.signum(pseudoStartY - currentY);
                    touchMoveStartTime = SystemClock.uptimeMillis();
                }
                float diffY = prevY - currentY;
                //shiftY(diffY, startTouchColIndex);
                shiftBy(normalizeValue(diffY, startEntryIndex), startEntryIndex);
                Integer[] position = getPosition();
                if (position[position.length - 1] != startTouchIndex) {
                    // if the index changed then set our drag start value to be where we are right now
                    pseudoStartY = currentY;
                    startTouchIndex = position[position.length - 1];
                    //startTouchRowIndex = this.rowIndex;
                }
            }// else
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
                    setShiftOfToNearestChild();
                if (touchVer)
                    setShiftOfToNearestChild(startEntryIndex);
            }
            touchHor = false;
            touchVer = false;
            startEntryIndex = null;
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
                shiftBy(normalizeValue(momentumOffsetX));
                //shiftX(momentumOffsetX);
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
                Integer[] entryIndex = getEntryPosition();
                shiftBy(normalizeValue(momentumOffsetY, entryIndex), entryIndex);
                //shiftY(momentumOffsetY, XMBView.this.colIndex);
            }
            if (hasMomentumX || hasMomentumY) {
                momentumHandler.postDelayed(this, milliInterval);
            } else {
                setShiftOfToNearestChild();
                setShiftOfToNearestChild(getEntryPosition());
            }
        }
    };

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        removeViews();
        //createExpectedAmountOfViews();
        createItemViews(1);
        createCatViews(1);
        createInnerItemViews(1);
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
    private final List<Integer> hashTracker = new ArrayList<>();
    private void setViews(boolean indexChanged, boolean instant) {
        // indexChanged is true when currentIndex actually changes and false when otherwise
        // it is used to know when to apply fade transitions
        if (adapter != null && adapter.getItemCount(false) > 0) {
            float rootShift = getShiftOf();

            int startX = getStartX() - Math.round(denormalizeValue(rootShift));
            int startY = getStartY();
            int horShiftOffset = Math.round(getHorShiftOffset());
            int verShiftOffset = Math.round(getVerShiftOffset());
            //if (moveMode && indexChanged)
            //    returnAllViews();
            long startTime = SystemClock.uptimeMillis();
            //returnUnusedViews();
            hashTracker.clear();
            drawCategories(hashTracker, indexChanged, startX, startY, horShiftOffset, instant || moveMode);
            long catTime = SystemClock.uptimeMillis() - startTime;
            startTime = SystemClock.uptimeMillis();
            drawItems(hashTracker, indexChanged, instant || moveMode, startX, startY, horShiftOffset, verShiftOffset);
            long itemTime = SystemClock.uptimeMillis() - startTime;
            startTime = SystemClock.uptimeMillis();
            drawInnerItems(hashTracker, instant || moveMode);
            Set<Integer> extraneous = new TreeSet<>(usedViews.keySet());
            hashTracker.forEach(extraneous::remove);
            extraneous.forEach(this::returnItemViewHash);
            long innerTime = SystemClock.uptimeMillis() - startTime;
            DebugView.print("SET_VIEWS", "xmb_time: cats(" + catTime + " ms) | items(" + itemTime + " ms) | inner(" + innerTime + " ms)", 2);
        }
    }
    private void drawCategories(List<Integer> usedHashes, boolean indexChanged, int startXInt, int startYInt, int horShiftOffsetInt, boolean instant) {
        int viewWidth = getWidth();
        int viewHeight = getHeight();
        int origColIndex = shiftValToIndex(getShiftOf());
        int prevColIndex = shiftValToIndex(getPrevShiftOf());

        for (int colIndex = 0; colIndex < adapter.getColumnCount(); colIndex++) {
            calcCatRect(startXInt, startYInt, horShiftOffsetInt, colIndex, reusableRect);
            boolean inBounds = inView(reusableRect, viewWidth, viewHeight);
            if (inBounds) {
                drawItem(reusableRect, FADE_VISIBLE, instant, colIndex);
                usedHashes.add(MathHelpers.hash(colIndex));
            } else
                returnItemView(colIndex);

            if (adapter.getColumnSize(colIndex) <= 0 && adapter.isColumnHead(colIndex)) {
                // this column has no items and it is meant to be a column head, so add an empty item below it
                calcItemRect(startXInt, startYInt, horShiftOffsetInt, 0, colIndex, 0, reusableRect);
                inBounds = inView(reusableRect, viewWidth, viewHeight);

                int fadeTransition = FADE_VISIBLE;
                if (!instant && indexChanged && colIndex == origColIndex && prevColIndex != origColIndex)
                    fadeTransition = FADE_IN;
                else if (!instant && indexChanged && colIndex != origColIndex && colIndex == prevColIndex)
                    fadeTransition = FADE_OUT;
                else if (((!indexChanged || instant) && colIndex != origColIndex) || ((indexChanged || instant) && colIndex != origColIndex && colIndex != prevColIndex))
                    fadeTransition = FADE_INVISIBLE;
                if (colIndex >= origColIndex - 1 && colIndex <= origColIndex + 1 && inBounds) {
                    drawItem(reusableRect, fadeTransition, instant, colIndex, 0);
                    usedHashes.add(MathHelpers.hash(colIndex, 0));
                } else
                    returnItemView(colIndex, 0);
            }
        }
    }
    private void drawItems(List<Integer> usedHashes, boolean indexChanged, boolean instant, int startXInt, int startYInt, int horShiftOffsetInt, int verShiftOffsetInt) {
        int origColIndex = shiftValToIndex(getShiftOf());
        int prevColIndex = shiftValToIndex(getPrevShiftOf());
        int viewWidth = getWidth();
        int viewHeight = getHeight();
        for (int itemColIndex = 0; itemColIndex < adapter.getColumnCount(); itemColIndex++) {
            int childCount = adapter.getChildCount(itemColIndex);
            // skip items that do not have children since there is nothing to draw
            if (childCount == 0)
                continue;
            for (int itemRowIndex = 0; itemRowIndex < childCount; itemRowIndex++) {
                //Log.d("XMBView", "Checking [" + itemColIndex + ", " + itemRowIndex + "]");
                calcItemRect(startXInt, startYInt, horShiftOffsetInt, verShiftOffsetInt, itemColIndex, itemRowIndex, reusableRect);

                boolean inBounds = inView(reusableRect, viewWidth, viewHeight);
                int fadeTransition = FADE_VISIBLE;
                if (!instant && indexChanged && itemColIndex == origColIndex && prevColIndex != origColIndex)
                    fadeTransition = FADE_IN;
                else if (!instant && indexChanged && itemColIndex != origColIndex && itemColIndex == prevColIndex)
                    fadeTransition = FADE_OUT;
                else if (((!indexChanged || instant) && itemColIndex != origColIndex) || ((indexChanged || instant) && itemColIndex != origColIndex && itemColIndex != prevColIndex))
                    fadeTransition = FADE_INVISIBLE;
                if (itemColIndex >= origColIndex - 1 && itemColIndex <= origColIndex + 1 && inBounds) {
                    drawItem(reusableRect, fadeTransition, instant, itemColIndex, itemRowIndex);
                    usedHashes.add(MathHelpers.hash(itemColIndex, itemRowIndex));
                } else
                    returnItemView(itemColIndex, itemRowIndex);
            }
        }
    }
    private void drawInnerItems(List<Integer> usedHashes, boolean instant) {
        Integer[] currentEntry = getEntryPosition();
        for (int itemColIndex = 0; itemColIndex < adapter.getColumnCount(); itemColIndex++)
            for (int itemRowIndex = 0; itemRowIndex < adapter.getColumnSize(itemColIndex); itemRowIndex++)
                if (adapter.hasChildren(itemColIndex, itemRowIndex))
                    drawInnerItems(usedHashes, instant, currentEntry, itemColIndex, itemRowIndex);
    }
    private void drawInnerItems(List<Integer> usedHashes, boolean instant, Integer[] currentEntry, Integer... position) {
        int viewWidth = getWidth();
        int viewHeight = getHeight();
        Integer[] innerPosition = Arrays.copyOf(position, position.length + 1);
        // draw list of inner items
        for (int innerItemIndex = 0; innerItemIndex < adapter.getChildCount(position); innerItemIndex++) {
            innerPosition[innerPosition.length - 1] = innerItemIndex;
            // if the inner item has inner items of its own, then draw them
            if (adapter.hasChildren(innerPosition)) {
                drawInnerItems(usedHashes, instant, currentEntry, innerPosition);
                usedHashes.add(MathHelpers.hash(innerPosition));
            }
            // if the current inner item is where the user is right now, then draw it
            if (currentEntry != null && (currentEntry.length > 0 && ((((innerPosition.length - 1) == currentEntry.length) && isPartOfPosition(innerPosition, currentEntry)) || isPartOfPosition(currentEntry, innerPosition)))) {
                //Log.d("XMBView", Arrays.toString(innerPosition) + " entered first if");
                calcInnerItemRect(reusableRect, getStartX(), getStartY(), Math.round(innerHorSpacing), Math.round(innerVerSpacing), innerPosition);
                boolean inBounds = inView(reusableRect, viewWidth, viewHeight);
                if (inBounds) {
                    //Log.d("XMBView", Arrays.toString(innerPosition) + " being drawn");
                    drawItem(reusableRect, FADE_VISIBLE, instant, innerPosition);
                    usedHashes.add(MathHelpers.hash(innerPosition));
                } else {
                    //Log.d("XMBView", Arrays.toString(innerPosition) + " being returned");
                    returnItemView(innerPosition);
                }
            } else {
                //Log.d("XMBView", Arrays.toString(innerPosition) + " being returned");
                returnItemView(innerPosition);
            }
        }
    }
    private static final int FADE_VISIBLE = 0;
    private static final int FADE_INVISIBLE = 1;
    private static final int FADE_OUT = 2;
    private static final int FADE_IN = 3;
    private void drawItem(Rect itemBounds, int fadeTransition, boolean instant, Integer... itemPosition) {
        ViewHolder viewHolder = getViewHolder(itemPosition);
        boolean isCat = itemPosition.length == 1;
        boolean isInnerItem = itemPosition.length > 2;
//        if (isInnerItem)
//            Log.d("XMBView", "Drawing inner item " + Arrays.toString(itemPosition));
//        else if (isCat)
//            Log.d("XMBView", "Drawing cat " + Arrays.toString(itemPosition));
//        else
//            Log.d("XMBView", "Drawing sub item " + Arrays.toString(itemPosition));
        viewHolder.setX(itemBounds.left);
        viewHolder.setY(itemBounds.top);
        Integer[] currentPosition = getPosition();
        //Log.d("XMBView", "Comparing " + Arrays.toString(currentPosition) + " to " + Arrays.toString(itemPosition));
        boolean isSelection = isSamePosition(currentPosition, itemPosition);//getTotalIndexFromTraversable(currentIndex) == totalIndex;
        boolean isPartOfPosition = isPartOfPosition(currentPosition, itemPosition);
        // set z positions
        float z = ITEM_Z;
        if (isInnerItem || (isPartOfPosition && isInsideItem()))
            z = INNER_Z;
        if (isCat)
            z = CAT_Z;
        viewHolder.itemView.setTranslationZ(z);
        boolean isOurCat = isCat && itemPosition[0] == currentPosition[0];
        viewHolder.setHighlighted(moveMode && isSamePosition(moveIndex.toArray(new Integer[0]), itemPosition));
        viewHolder.setHideTitle(isInsideItem() && isPartOfPosition && !isSelection);
        float itemAlpha = ((isPartOfPosition && !isCat) || (!isInsideItem() && isOurCat) || isInnerItem) ? fullItemAlpha : (isInsideItem() ? innerItemOverlayTranslucent : translucentItemAlpha);
        if (isSelection)
            viewHolder.setDirty();
//        Log.d("XMBView", "Binding: " + Arrays.toString(itemPosition));
        adapter.onBindViewHolder(viewHolder, itemPosition);

        viewHolder.itemView.animate().cancel();
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
            //viewHolder.itemView.setX(viewHolder.getX());
            //viewHolder.itemView.setY(viewHolder.getY());
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
                if (!position[i].equals(position2[i]))
                    return false;
        return true;
    }
    private boolean isPartOfPosition(Integer[] fullPosition, Integer... part) {
        if (part.length > fullPosition.length || fullPosition.length <= 0 || part.length <= 0)
            return false;
        else
            for (int i = 0; i < part.length; i++)
                if (!fullPosition[i].equals(part[i]))
                    return false;
        return true;
    }
    private void removeViews() {
        returnAllViews();
        while (!goneCatViews.isEmpty())
            removeView(goneCatViews.pop().itemView);
        while (!goneItemViews.isEmpty())
            removeView(goneItemViews.pop().itemView);
        while (!goneInnerItemViews.isEmpty())
            removeView(goneInnerItemViews.pop().itemView);
    }
    private void createItemViews(int amount) {
        if (adapter == null)
            return;
        for (int i = 0; i < amount; i++) {
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
    private void createCatViews(int amount) {
        if (adapter == null)
            return;
        for (int i = 0; i < amount; i++) {
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
    private void createInnerItemViews(int amount) {
        if (adapter == null)
            return;
        for (int i = 0; i < amount; i++) {
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
    private void returnItemViewHash(int hash) {
        if (usedViews.containsKey(hash)) {
            ViewHolder viewHolder = usedViews.get(hash);
            viewHolder.itemView.setVisibility(GONE);
            if (viewHolder.getItemViewType() == CATEGORY_TYPE)
                goneCatViews.push(viewHolder);
            else if (viewHolder.getItemViewType() == ITEM_TYPE)
                goneItemViews.push(viewHolder);
            else if (viewHolder.getItemViewType() == INNER_TYPE)
                goneInnerItemViews.push(viewHolder);
            usedViews.remove(hash);
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
    private ViewHolder getViewHolder(Integer... position) {
        boolean isCat = position.length == 1;
        boolean isInnerItem = position.length > 2;
        int viewType = isCat ? CATEGORY_TYPE : isInnerItem ? INNER_TYPE : ITEM_TYPE;
        ViewHolder viewHolder = null;
        int indexHash = MathHelpers.hash(position);
        //if (isInnerItem)
        //    Log.d("XMBView", Arrays.toString(position) + " => " + indexHash);
        if (usedViews.containsKey(indexHash)) {
            //if (isInnerItem)
            //    Log.d("XMBView", Arrays.toString(position) + " => " + indexHash + " exists");
            viewHolder = usedViews.get(indexHash);
            viewHolder.isNew = false;
        } else {
            //if (isInnerItem)
            //    Log.d("XMBView", Arrays.toString(position) + " => " + indexHash + " does not exist");
            if (viewType == CATEGORY_TYPE) {
                if (goneCatViews.isEmpty())
                    createCatViews(1);

                viewHolder = goneCatViews.pop();
                viewHolder.isNew = true;
                viewHolder.itemView.setVisibility(VISIBLE);
                usedViews.put(indexHash, viewHolder);
            } else if (viewType == ITEM_TYPE) {
                if (goneItemViews.isEmpty())
                    createItemViews(1);

                viewHolder = goneItemViews.pop();
                viewHolder.isNew = true;
                viewHolder.itemView.setVisibility(VISIBLE);
                usedViews.put(indexHash, viewHolder);
            } else if (viewType == INNER_TYPE) {
                if (goneInnerItemViews.isEmpty())
                    createInnerItemViews(1);

                viewHolder = goneInnerItemViews.pop();
                viewHolder.isNew = true;
                viewHolder.itemView.setVisibility(VISIBLE);
                usedViews.put(indexHash, viewHolder);
            }
            //adapter.onBindViewHolder(viewHolder, position);
            //viewHolder.setDirty();
        }
        return viewHolder;
        //return getViewHolder(isCat ? CATEGORY_TYPE : isInnerItem ? INNER_TYPE : ITEM_TYPE, position);
    }
    //private ViewHolder getViewHolder(int viewType, Integer... position) {
    //}

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
        // get the horizontal pixel position of the item (using getStartX() instead of startX because startX has shiftX in it)
        int expX = (isInsideItem() && isPartOfPosition(getPosition(), colIndex, 0) ? getStartX() + (currentDepth - 1) * -Math.round(innerItemSize + innerHorSpacing) : startX + horShiftOffset * colIndex);//getColIndexFromTotal(totalIndex));
        // the vertical pixel position is the same since the categories go along a straight line
        int expY = startY;
        // get the right and bottom values of the item relative to the left and top values and apply them to the rect
        int right = expX + catSize;// + Math.max(iconSize, rect.width());
        int bottom = expY + catSize;// + textCushion + rect.height();
        rect.set(expX, expY, right, bottom);
    }
    // calculates the item's rect size with the text to the right of the item
    private void calcItemRect(int startX, int startY, int horShiftOffset, int verShiftOffset, int colIndex, int localIndex, Rect rect) {
        Integer[] entryPos = new Integer[] { colIndex };
        float rawShiftVal = getShiftOf(entryPos);
        int currentLocalIndex = shiftValToIndex(rawShiftVal, entryPos);
        float shiftVal = denormalizeValue(rawShiftVal, entryPos);

        boolean isPartOfInsideItem = isPartOfPosition(getPosition(), colIndex, localIndex);
        // get the index of what is actually highlighted currently within the column
        //int itemCatIndex = getCachedIndexOfCat(colIndex);
        int halfCatDiff = Math.round(Math.abs(catSize - itemSize) / 2f);
        // get the horizontal pixel position of the item (using getStartX() instead of startX because startX has shiftX in it)
        int expX = halfCatDiff + (isInsideItem() && isPartOfInsideItem ? getStartX() + (currentDepth - 1) * -Math.round(innerItemSize + innerHorSpacing) : startX + horShiftOffset * colIndex);
        // get the vertical pixel position of the item
        int expY = halfCatDiff + (isInsideItem() && isPartOfInsideItem ? startY : Math.round((startY - shiftVal) + verShiftOffset * (localIndex - 0) + ((localIndex - 0) >= currentLocalIndex ? catSize + adapter.getTextSize() + subItemGap : 0)));
        // get the right and bottom values of the item relative to the left and top values and apply them to the rect
        int right = expX + itemSize;// + textCushion + rect.width();
        int bottom = expY + itemSize;
        rect.set(expX, expY, right, bottom);
    }
    private void calcInnerItemRect(Rect rect, int startX, int startY, int horSpacing, int verSpacing, Integer... position) {
        Integer[] entryPos = getParentIndex(position);
        float shiftVal = denormalizeValue(getShiftOf(entryPos), entryPos);

        int halfCatDiff = Math.round(Math.abs(catSize - innerItemSize) / 2f);

        //int discX = ((position.length - 2) - innerItemEntryPos.size());
        int discX = ((position.length - 2) - (currentDepth - 1));
        // get the horizontal pixel position of the item
        int expX = startX + halfCatDiff + discX * innerItemSize + (discX + 1) * horSpacing; // discX + 1 so that the first inner items can be offset as well
        //boolean isCurrentInner = (position.length - 2) > innerItemEntryPos.size();
        //boolean isCurrentInner = (position.length - 2) > (currentDepth - 1);
        boolean isCurrentInner = isPartOfPosition(position, entryPos);
        // get the vertical pixel position of the item
        //int expY = startY + halfCatDiff + (isCurrentInner ? position[position.length - 1] * (innerItemSize + verSpacing) - Math.round(innerItemVerPos.peek()) : 0);
        int expY = startY + halfCatDiff + (isCurrentInner ? position[position.length - 1] * (innerItemSize + verSpacing) - Math.round(shiftVal) : 0);
        // get the right and bottom values of the item relative to the left and top values and apply them to the rect
        int right = expX + innerItemSize;// + textCushion + rect.width();
        int bottom = expY + innerItemSize;
        rect.set(expX, expY, right, bottom);
    }

    public Object getSelectedItem() {
        return adapter.getItem(getPosition());
    }

    public void clearShiftCache() {
        prevShifts.clear();
        currentShifts.clear();
    }

    protected void setPosition(Integer... position) {
        prevShifts.clear();
        prevShifts.putAll(currentShifts);
        prevDepth = currentDepth;
        currentDepth = position.length;
        Stack<Integer> builtPos = new Stack<>();
        for (int i = 0; i < position.length; i++) {
            Integer[] currentEntry = builtPos.toArray(new Integer[0]);
            int parentHash = MathHelpers.hash(currentEntry);
            currentShifts.put(parentHash, indexToShiftVal(position[i], currentEntry));
            builtPos.push(position[i]);
        }
        setViews(isSamePosition(getPosition(), getPrevPosition()), isSamePosition(getEntryPosition(), getPrevEntryPosition()));
    }
    protected Integer[] getPrevPosition() {
        return getPositionOf(prevShifts, prevDepth);
    }
    protected Integer[] getPosition() {
        return getPositionOf(currentShifts, currentDepth);
    }
    private Integer[] getPositionOf(HashMap<Integer, Float> shifts, int depth) {
        Stack<Integer> position = new Stack<>();
        int root_hash = MathHelpers.hash(ROOT_INDEX);
        if (shifts.containsKey(root_hash)) {
            position.push(shiftValToIndex(shifts.get(root_hash)));
        } else {
            position.push(0);
        }
        
        Integer[] posArr = position.toArray(new Integer[0]);
        for (int i = 0; i < depth; i++) {
            int currentHash = MathHelpers.hash(posArr);
            if (shifts.containsKey(currentHash)) {
                position.push(shiftValToIndex(shifts.get(currentHash), posArr));
            } else {
                position.push(0);
            }
            posArr = position.toArray(new Integer[0]);
        }
        return posArr;
    }
    protected Integer[] getPrevEntryPosition() {
        return getEntryPositionOf(prevShifts, prevDepth);
    }
    protected Integer[] getEntryPosition() {
        return getEntryPositionOf(currentShifts, currentDepth);
    }
    private Integer[] getEntryPositionOf(HashMap<Integer, Float> shifts, int depth) {
        if (depth > 0) {
            Stack<Integer> entryPos = new Stack<>();
            int root_hash = MathHelpers.hash(ROOT_INDEX);
            if (shifts.containsKey(root_hash)) {
                entryPos.push(shiftValToIndex(shifts.get(root_hash)));
            } else {
                entryPos.push(0);
            }

            Integer[] posArr = entryPos.toArray(new Integer[0]);
            for (int i = 0; i < depth - 1; i++) {
                int currentHash = MathHelpers.hash(posArr);
                if (shifts.containsKey(currentHash)) {
                    entryPos.push(shiftValToIndex(shifts.get(currentHash), posArr));
                } else {
                    entryPos.push(0);
                }
                posArr = entryPos.toArray(new Integer[0]);
            }
            return posArr;
        } else {
            return new Integer[0];
        }
    }

    public void selectLowerItem() {
        stopMomentum();
        if (currentDepth >= 1)
            shiftByDisc(1, getEntryPosition());
        //shiftYDisc(1);
    }
    public void selectUpperItem() {
        stopMomentum();
        if (currentDepth >= 1)
            shiftByDisc(-1, getEntryPosition());
        //shiftYDisc(-1);
    }
    public void selectRightItem() {
        stopMomentum();
        if (currentDepth <= 1)
            shiftByDisc(1);
        //shiftXDisc(1);
    }
    public void selectLeftItem() {
        stopMomentum();
        if (currentDepth <= 1)
            shiftByDisc(-1);
        //shiftXDisc(-1);
    }
    public boolean affirmativeAction() {
        stopMomentum();
        if (moveMode) {
            applyMove();
            return true;
        } else {
            Integer[] position = getPosition();
            //int nextEntry = position[position.length - 1]; // this has to come before the mapping since it is what is pushed into innerItemEntryPos and we want to keep total index in there, not the mapped index
            //position[0] = mapTotalIndex(position[0]);
            boolean hasInnerItems = adapter.hasChildren(position);

            if (hasInnerItems) {
                int posHash = MathHelpers.hash(position);
                if (!currentShifts.containsKey(posHash))
                    currentShifts.put(posHash, 0f);
                currentDepth++;
//                if (isInsideItem())
//                    innerItemEntryPos.push(nextEntry);
//                innerItemVerPos.push(0f);
                //getViewHolder(position).setDirty();
                setViews(false, false);
                //adapter.onBindViewHolder(getViewHolder(position), position);
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
//            if (innerItemEntryPos.size() > 0) // need this check since first entry is colIndex and rowIndex
//                innerItemEntryPos.pop();
//            innerItemVerPos.pop();
            currentDepth--;
            //getViewHolder(getPosition()).setDirty();
            setViews(false, false);
            //Integer[] position = getPosition();
            //adapter.onBindViewHolder(getViewHolder(position), position);
            return true;
        }
        return false;
    }
    public static Integer[] getParentIndex(Integer... position) {
        Integer[] parentIndex = new Integer[position.length - 1];
        for (int i = 0; i < parentIndex.length; i++) {
            parentIndex[i] = position[i];
        }
        return parentIndex;
    }
    public boolean isInMoveMode() {
        return moveMode;
    }
    public void toggleMoveMode(boolean onOff, boolean columnMode) {
        if (moveMode && columnMode)
            throw new RuntimeException("Cannot enter column move mode from move mode");

        moveMode = onOff;
//        Integer[] position = getPosition();
//        if (this.columnMode || columnMode)
//            position = new Integer[] { position[0], 0 };
//        getViewHolder(position).setDirty();
//        this.columnMode = columnMode;
        if (moveMode) {
            //moveColIndex = getColIndex();
            //moveLocalIndex = getLocalIndex();
            origMoveIndex.clear();
            moveIndex.clear();
            if (columnMode) {
                origMoveIndex.add(getPosition()[0]);
                moveIndex.add(getPosition()[0]);
            } else {
                origMoveIndex.addAll(Arrays.asList(getPosition()));
                moveIndex.addAll(Arrays.asList(getPosition()));
            }

//            origMoveColIndex = this.colIndex;
//            origMoveLocalIndex = this.rowIndex;// + (catHasSubItems(moveColIndex) ? 1 : 0);
////            moveLocalIndex = columnMode ? 0 : this.rowIndex;
//            moveLocalIndex = this.rowIndex;
        }
//        else
//            this.columnMode = false;
        //setAdapterHighlightColor(moveMode ? moveHighlightColor : normalHighlightColor);
        setViews(false, true);
        //refresh();
    }
    private void applyMove() {
        //if (this.colIndex != origMoveColIndex || this.rowIndex != origMoveLocalIndex)
        if (moveMode)
            onAppliedMove(origMoveIndex.toArray(new Integer[0]), moveIndex.toArray(new Integer[0]));
        toggleMoveMode(false, false);
    }
    protected void onAppliedMove(Integer[] fromIndex, Integer[] toIndex) {

    }
    protected void onShifted(int amount, Integer... fromPos) {
        if (moveMode) {
            if (moveIndex.size() > 1) {
                Integer[] newPos = adapter.shiftItemVertically(amount, fromPos);
                moveIndex.clear();
                moveIndex.addAll(Arrays.asList(newPos));
                setPosition(newPos);
            } else {
                Integer[] newPos = adapter.shiftItemHorizontally(amount, moveIndex.toArray(new Integer[0]));
                moveIndex.clear();
                moveIndex.addAll(Arrays.asList(newPos));
                setPosition(newPos);
            }
        }
    }
}
