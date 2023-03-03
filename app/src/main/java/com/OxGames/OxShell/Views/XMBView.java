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

import com.OxGames.OxShell.Helpers.MathHelpers;
import com.OxGames.OxShell.Interfaces.InputReceiver;
import com.OxGames.OxShell.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

public class XMBView extends ViewGroup implements InputReceiver {//, Refreshable {
    private static final float EPSILON = 0.0001f;
    private final Context context;
    public static final int CATEGORY_TYPE = 0;
    public static final int ITEM_TYPE = 1;
    public static final int INNER_TYPE = 2;

    // traversable index means the indices that can actually be stepped on
    // col index are the indices that represent the columns
    // total index includes all indices even the ones that cannot be stepped on (which are the top column items of columns with multiple items)
    // local index are the indices within a column
    int colIndex = 0;
    int prevColIndex = 0;
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
        return innerYToIndex(innerItemVerPos.peek(), innerItemEntryPos.toArray(new Integer[0]));
    }

    private final ArrayList<Float> catPos; // go from 0 to (getColCount(colIndex) - 1) * getVerShiftOffset()
    private boolean moveMode;
    private int origMoveColIndex;
    private int origMoveLocalIndex;
    private int moveColIndex;
    private int moveLocalIndex;

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
        @NonNull
        public abstract T onCreateViewHolder(@NonNull ViewGroup parent, int viewType);
        public abstract void onBindViewHolder(@NonNull T holder, Integer... position);
        public abstract int getItemCount(boolean withInnerItems);
        public abstract int getColumnCount();
        public abstract int getColumnSize(int columnIndex);
        public abstract void onViewAttachedToWindow(@NonNull T holder);
        public abstract Object getItem(Integer... position);
        public abstract boolean hasInnerItems(Integer... position);
        public abstract int getInnerItemCount(Integer... position);
        public abstract boolean isColumnHead(Integer... position);
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
        this.adapter = adapter;
        this.colIndex = 0;
        this.prevColIndex = 0;
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
    private float shiftX = 0; // goes from 0 to (getTotalColCount() - 1) * getHorShiftOffset()

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
            shiftY = clampYValue(yValue, colIndex);//Math.min(Math.max(yValue, 0), colCount * getVerShiftOffset());
            catPos.set(colIndex, shiftY);
        }

        int localIndex = yToIndex(shiftY, colIndex);
        //int newIndex = getTraversableIndexFromLocal(localIndex, colIndex);
        boolean changed = this.colIndex != colIndex || this.rowIndex != localIndex;
        if (changed) {
            this.prevColIndex = this.colIndex;
            this.colIndex = colIndex;
            this.rowIndex = localIndex;
            //prevIndex = currentIndex;
            //currentIndex = newIndex;
        }
        setViews(changed, instant);
    }
    private void setShiftX(float xValue) {
        if (!isInsideItem()) {
            if (Math.abs(shiftX - xValue) > EPSILON) {
                shiftX = Math.min(Math.max(xValue, 0), (adapter.getColumnCount() - 1) * getHorShiftOffset());
                int colIndex = xToIndex(xValue);
                if (xValue < 0)
                    colIndex = -1; // just for touch input since the amount a user can move by per frame is not enough to move a whole index
                int properColIndex = clampColIndex(colIndex);
                boolean changed = properColIndex != this.colIndex;
                int newRowIndex = getCachedIndexOfCat(properColIndex);
                //int newIndex = getTraversableIndexFromLocal(getCachedIndexOfCat(properColIndex), properColIndex);
                //boolean changed = newIndex != currentIndex;
                if (changed || (moveMode && getColIndex() != colIndex))
                    onShiftHorizontally(properColIndex, getColIndex());
                if (changed) {
                    if (moveMode) {
                        //newIndex = getTraversableIndexFromLocal(getCachedIndexOfCat(moveColIndex), moveColIndex);
                        newRowIndex = getCachedIndexOfCat(moveColIndex);
                        shiftX = getShiftX(moveColIndex);
                    }
                    //prevIndex = currentIndex;
                    //currentIndex = newIndex;
                    this.prevColIndex = this.colIndex;
                    this.colIndex = properColIndex;
                    this.rowIndex = newRowIndex;
                }
                setViews(changed, false);
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
//    private int shiftXDisc(float amount) {
//        return shiftXDisc(currentIndex, amount);
//    }
//    private int shiftXDisc(int fromIndex, float amount) {
//        int offsetIndex = (int)(amount / itemSize); // convert the user drag amount to indices offset
//        return shiftXDisc(fromIndex, offsetIndex);
//    }
    private void shiftXDisc(int amount) {
        if (Math.abs(amount) > 0) {
            int currentColIndex = this.colIndex;//getColIndexFromTraversable(fromIndex);
            // get the offset column's local index then convert it to x position
            setShiftX(getShiftX(currentColIndex + amount));
        }
    }
    private float clampYValue(float yValue, int colIndex) {
        return Math.min(Math.max(yValue, 0), (getColTraversableCount(colIndex) - 1) * getVerShiftOffset());
    }
    private void setShiftY(float yValue, int colIndex) {
        if (!isInsideItem()) {
            float shiftY = catPos.get(colIndex);
            if (Math.abs(shiftY - yValue) > EPSILON) {
                float newYValue = clampYValue(yValue, colIndex);//Math.min(Math.max(yValue, 0), colCount * getVerShiftOffset());
                catPos.set(colIndex, newYValue);
                int currentCol = this.colIndex;//getColIndexFromTraversable(currentIndex);
                // if the shifted column is the one we are currently on
                if (currentCol == colIndex) {
                    int newLocalIndex = yToIndex(newYValue, colIndex);
                    boolean changed = newLocalIndex != this.rowIndex;
                    //int newIndex = getTraversableIndexFromLocal(newLocalIndex, colIndex);
                    //boolean changed = newIndex != currentIndex;
                    if (changed) {
                        int prevLocalIndex = getLocalIndex();
                        onShiftVertically(currentCol, newLocalIndex, prevLocalIndex);
                        this.rowIndex = newLocalIndex;
                        //prevIndex = currentIndex;
                        //currentIndex = newIndex;
                    }
                    setViews(changed, false);
                }
            }
        } else {
            float currentY = innerItemVerPos.peek();
            if (Math.abs(currentY - yValue) > EPSILON) {
                float adjustedY = Math.min(Math.max(yValue, 0), (adapter.getInnerItemCount(innerItemEntryPos.toArray(new Integer[0])) - 1) * (innerItemSize + innerVerSpacing));
                innerItemVerPos.pop();
                innerItemVerPos.push(adjustedY);
                setViews(false, false);
            }
        }
    }
    private float getShiftY(int localIndex, int colIndex) {
        if (catHasSubItems(colIndex))
            localIndex -= 1; // since we don't want to count the column head
        return getVerShiftOffset() * Math.min(Math.max(localIndex, 0), getColTraversableCount(colIndex) - 1);//getColTraversableCount(colIndex) - 1);
    }
    private void shiftY(float amount, int colIndex) {
        float origAmount;
        if (isInsideItem())
            origAmount = innerItemVerPos.peek();
        else
            origAmount = catPos.get(colIndex);
        setShiftY(origAmount + amount, colIndex);
    }
    private int yToIndex(float yValue, int colIndex) {
        // finds the local index closest to the current y value then clamps it to be within the proper range
        return Math.min(Math.max(Math.round(yValue / getVerShiftOffset()), 0), adapter.getColumnSize(colIndex) - 1);//getColTraversableCount(colIndex) - 1);
    }
    protected int getColLocalIndex(int colIndex) {
        return yToIndex(catPos.get(colIndex), colIndex);
    }
    private void setShiftYToNearestItem(int colIndex) {
        setShiftY(toNearestColumnItem(catPos.get(colIndex), colIndex), colIndex);
    }
    private float toNearestColumnItem(float yValue, int colIndex) {
        // finds the local index nearest to the pixel location then turns the index back into pixel location
        float nearestY;
        if (isInsideItem())
            nearestY = innerYToIndex() * (innerItemSize + innerVerSpacing);
        else
            nearestY = yToIndex(yValue, colIndex) * getVerShiftOffset();
        return nearestY;
    }
    private void shiftYDisc(int amount) {
        shiftYDisc(this.colIndex, this.rowIndex, amount);
    }
    private void shiftYDisc(int colIndex, int localIndex, int amount) {
        if (Math.abs(amount) > 0) {
            if (!isInsideItem()) {
                setShiftY(getShiftY(localIndex + amount, colIndex), colIndex);
            } else {
                float nextY = Math.min(Math.max(innerYToIndex() + amount, 0), adapter.getInnerItemCount(innerItemEntryPos.toArray(new Integer[0])) - 1) * (innerVerSpacing + innerItemSize);
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
    private float momentumTravelDistX = 0;
    private float momentumTravelDistY = 0;
    private float startTouchX = 0;
    private float pseudoStartX = 0;
    private float momentumX = 0;
    private float prevX = 0;
    private float startTouchY = 0;
    private float pseudoStartY = 0;
    private float momentumY = 0;
    private float prevY = 0;
    //private int startTouchIndex = 0;
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
        Log.d("XMBView", "Pointer event " + event);
        return super.onCapturedPointerEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        //Log.d("XMBView", "Touch event " + ev);
        //slideTouch.update(ev);
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            //Log.d("XMBView", "Touchdown");
            touchMoveStartTime = SystemClock.uptimeMillis();
            longPressed = false;
            isPressing = true;
            startTouchX = ev.getRawX();
            startTouchY = ev.getRawY();
            prevX = startTouchX;
            prevY = startTouchY;
            touchInsideBorders = startTouchX > touchMarginLeft && startTouchX < getWidth() - touchMarginRight && startTouchY > touchMarginTop && startTouchY < getHeight() - touchMarginBottom;
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
            if (!isInsideItem() && !touchVer && !touchHor && Math.abs(currentX - startTouchX) >= touchDeadzone) {
                //startTouchIndex = currentIndex;
                startTouchColIndex = this.colIndex;
                startTouchRowIndex = this.rowIndex;
                touchMoveDir = Math.signum(currentX - startTouchX);
                touchMoveStartTime = SystemClock.uptimeMillis();
                pseudoStartX = touchMoveDir * touchDeadzone + startTouchX;
                touchHor = true;
            }
            // only acknowledge moving vertically if there are items to move vertically through
            if (!touchHor && !touchVer && Math.abs(currentY - startTouchY) >= touchDeadzone && catHasSubItems(colIndex)) {//columnHasSubItems(getColIndexFromTraversable(currentIndex))) {
                //startTouchIndex = currentIndex;
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
                //Log.d("XMBView", "Diffx " + diffX);
                shiftX(diffX);// * 0.2f);
                //int newIndex = shiftXDisc(startTouchIndex, diffX);
                if (this.colIndex != startTouchColIndex) {//currentIndex != startTouchIndex) {
                    // if the index changed then set our drag start value to be where we are right now
                    pseudoStartX = currentX;
                    ///startTouchIndex = currentIndex;
                    startTouchColIndex = this.colIndex;
                }
                //Log.d("XMBView", "Moving hor " + diffX + " offsetIndex " + offsetIndex + ", " + startTouchIndex + " => " + nextColIndex);
            }
            if (touchVer) {
                float diffY = prevY - currentY;
                if (touchMoveDir != Math.signum(pseudoStartY - currentY)) {
                    // if the movement direction changed, then update the start time to reflect when the change happened
                    touchMoveDir = Math.signum(pseudoStartY - currentY);
                    touchMoveStartTime = SystemClock.uptimeMillis();
                }
                shiftY(diffY, startTouchColIndex);//getColIndexFromTraversable(startTouchIndex));
                //int newIndex = shiftYDisc(startTouchIndex, diffY);
                if (this.rowIndex != startTouchRowIndex) {//currentIndex != startTouchIndex) {
                    // if the index changed then set our drag start value to be where we are right now
                    pseudoStartY = currentY;
                    //startTouchIndex = currentIndex;
                    startTouchRowIndex = this.rowIndex;
                }
                //Log.d("XMBView", "Moving ver " + diffY + " offsetIndex " + offsetIndex + ", " + startTouchIndex + " => " + nextIndex);
            }
            prevX = currentX;
            prevY = currentY;
        } else if (ev.getAction() == MotionEvent.ACTION_UP && touchInsideBorders) {
            //Log.d("XMBView", "Touchup");
            isPressing = false;
            stopMomentum();
            if (longPressed)
                return true;
            //float movedDiffX;
            //float movedDiffY;
            if (!touchHor && !touchVer && Math.abs(startTouchX - ev.getRawX()) < touchDeadzone && Math.abs(startTouchY - ev.getRawY()) < touchDeadzone) {
                // if the user did not scroll horizontally or vertically and they're still within the deadzone then make selection
                //if (SystemClock.uptimeMillis() - touchMoveStartTime < longPressTime)
                    affirmativeAction();
//                else
//                    secondaryAction();
            } else if (Math.abs(startTouchX - ev.getRawX()) >= itemSize || Math.abs(startTouchY - ev.getRawY()) >= itemSize) {
                // else keep the momentum of where the user was scrolling
                long touchupTime = SystemClock.uptimeMillis();
                float totalTime = (touchupTime - touchMoveStartTime) / 1000f;
                if (touchHor)
                    momentumX = (startTouchX - ev.getRawX()) / totalTime; // pixels per second
                if (touchVer)
                    momentumY = (startTouchY - ev.getRawY()) / totalTime; // pixels per second
                if ((touchHor && Math.abs(momentumX) > 0) || (touchVer && Math.abs(momentumY) > 0))
                    handler.post(momentumRunner);
                //Log.d("XMBView", momentumX + ", " + momentumY);
            } else {
                if (touchHor)
                    setShiftXToNearestColumn();
                if (touchVer)
                    setShiftYToNearestItem(colIndex);//getColIndexFromTraversable(currentIndex));
            }
            touchHor = false;
            touchVer = false;
        }
        return true;
    }
    private void stopMomentum() {
        momentumX = 0;
        momentumY = 0;
        momentumTravelDistX = 0;
        momentumTravelDistY = 0;
        handler.removeCallbacks(momentumRunner);
    }
    private Handler handler = new Handler();
    private Runnable momentumRunner = new Runnable() {
        @Override
        public void run() {
            int milliInterval = Math.round((1f / 120) * 1000);

            boolean hasMomentumX = Math.abs(momentumX) > 0;
            boolean hasMomentumY = Math.abs(momentumY) > 0;
            if (hasMomentumX) {
                // how many items have we passed already
                int preMomentumOffset = (int)(momentumTravelDistX / itemSize);
                // calculate travel distance based on current momentum
                float momentumOffsetX = momentumX * (milliInterval / 1000f);
                momentumTravelDistX += momentumOffsetX;
                // decelerate the momentum
                if (momentumX > 0)
                    momentumX = Math.max(momentumX - momentumDeceleration * (milliInterval / 1000f), 0);
                else
                    momentumX = Math.min(momentumX + momentumDeceleration * (milliInterval / 1000f), 0);
                //momentumX -= Math.signum(momentumX) * momentumDeceleration * (milliInterval / 1000f);
                // how many items we've passed now that the momentum has been applied
                //int postMomentumOffset = (int)(momentumTravelDistX / iconSize);
                //Log.d("XMBView", "dist " + momentumTravelDistX + " momentum " + momentumX + " offset " + postMomentumOffset);
                // get the difference between the items passed to see what should be applied this moment
                shiftX(momentumOffsetX);
                //shiftXDisc(postMomentumOffset - preMomentumOffset);
            }
            if (hasMomentumY) {
                // how many items have we passed already
                int preMomentumOffset = (int)(momentumTravelDistY / itemSize);
                // calculate travel distance based on current momentum
                float momentumOffsetY = momentumY * (milliInterval / 1000f);
                momentumTravelDistY += momentumOffsetY;
                // decelerate the momentum
                if (momentumY > 0)
                    momentumY = Math.max(momentumY - momentumDeceleration * (milliInterval / 1000f), 0);
                else
                    momentumY = Math.min(momentumY + momentumDeceleration * (milliInterval / 1000f), 0);
                // how many items we've passed now that the momentum has been applied
                //int postMomentumOffset = (int)(momentumTravelDistY / iconSize);
                // get the difference between the items passed to see what should be applied this moment
                shiftY(momentumOffsetY, XMBView.this.colIndex);//getColIndexFromTraversable(currentIndex));
                //shiftYDisc(postMomentumOffset - preMomentumOffset);
            }
            if (hasMomentumX || hasMomentumY) {
                //long millis = SystemClock.uptimeMillis();
                handler.postDelayed(this, milliInterval);
                //handler.postAtTime(this, millis + milliInterval);
            } else {
                setShiftXToNearestColumn();
                setShiftYToNearestItem(XMBView.this.colIndex);//getColIndexFromTraversable(currentIndex));
            }
        }
    };

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        removeViews();
        createItemViews();
        int colCount = (int)Math.ceil(getWidth() / getHorShiftOffset()) + 4; //+4 for off screen animating into on screen
        createCatViews(); // to get catSize
        createItemViews();
        createInnerItemViews();
        catHorShift = horSpacing + (catSize + horSpacing) * (colCount / 6);
        Log.d("XMBView", "Size changed, setting cat shift to " + catHorShift);
        setViews(false, true);
    }

    private int getStartX() {
        int padding = getPaddingLeft();
        //padding = 0;
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
        if (adapter.getItemCount(false) > 0) {
            int startX = getStartX() - Math.round(shiftX);// - colIndex * horShiftOffset;
            int startY = getStartY();
            int horShiftOffset = Math.round(getHorShiftOffset());
            int verShiftOffset = Math.round(getVerShiftOffset());
            drawCategories(indexChanged, startX, startY, horShiftOffset, instant);
            drawItems(indexChanged, instant, startX, startY, horShiftOffset, verShiftOffset);
            drawInnerItems(instant);
        }
    }
    private void drawCategories(boolean indexChanged, int startXInt, int startYInt, int horShiftOffsetInt, boolean instant) {
        int viewWidth = getWidth();
        int viewHeight = getHeight();
        int origColIndex = this.colIndex;//getColIndexFromTraversable(currentIndex);
        int prevColIndex = this.prevColIndex;//getColIndexFromTraversable(prevIndex);

        //Rect emptyItemRect = null;
        //int totalEmptyColumns = 0;
        for (int colIndex = 0; colIndex < adapter.getColumnCount(); colIndex++) {
            //int catTotalIndex = getTotalIndexOfCol(i);
            calcCatRect(startXInt, startYInt, horShiftOffsetInt, colIndex, reusableRect);
            boolean inBounds = inView(reusableRect, viewWidth, viewHeight);
            //Log.d("XMBView", "Setting category " + i + " title: " + cat.title + " inBounds: " + inBounds + " destX: " + cat.getX() + " destY: " + cat.getY());
            if (inBounds)
                drawItem(reusableRect, FADE_VISIBLE, instant, colIndex, 0);
            else
                returnItemView(colIndex, 0);

            if (adapter.getColumnSize(colIndex) == 1 && adapter.isColumnHead(colIndex, 0)) {
                // this column has only 1 item and it is meant to be a column head, so add an empty item below it
                //totalEmptyColumns++;
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
                    drawItem(reusableRect, fadeTransition, instant, colIndex, 1);//-totalEmptyColumns);
                else
                    returnItemView(colIndex, 1);//-totalEmptyColumns);
            }
        }
    }
    private void drawItems(boolean indexChanged, boolean instant, int startXInt, int startYInt, int horShiftOffsetInt, int verShiftOffsetInt) {
        int origColIndex = this.colIndex;//getColIndexFromTraversable(currentIndex);
        int prevColIndex = this.rowIndex;//getColIndexFromTraversable(prevIndex);
        int viewWidth = getWidth();
        int viewHeight = getHeight();
        //int traversableCount = getTraversableCount();
        //Log.d("XMBView", "Drawing items");
        for (int itemColIndex = 0; itemColIndex < adapter.getColumnCount(); itemColIndex++) {
            // skip items that belong to a column with one item, those are drawn with the categories
            if (!catHasSubItems(itemColIndex))
                continue;
            // start from one since the 0th item will always be the column head
            for (int itemRowIndex = 1; itemRowIndex < adapter.getColumnSize(itemColIndex); itemRowIndex++) {
                //int totalIndex = getTotalIndexFromTraversable(i);
                //int itemColIndex = getColIndexFromTraversable(i);
                calcItemRect(startXInt, startYInt, horShiftOffsetInt, verShiftOffsetInt, itemColIndex, itemRowIndex, reusableRect);

                boolean inBounds = inView(reusableRect, viewWidth, viewHeight);
                //if (isPartOfPosition(getPosition(), totalIndex))
                //    Log.d("XMBView", "Item is in bounds: " + inBounds + " bounds: " + reusableRect);
                int fadeTransition = FADE_VISIBLE;
                if (!instant && indexChanged && itemColIndex == origColIndex && prevColIndex != origColIndex)
                    fadeTransition = FADE_IN;
                else if (!instant && indexChanged && itemColIndex != origColIndex && itemColIndex == prevColIndex)
                    fadeTransition = FADE_OUT;
                else if (((!indexChanged || instant) && itemColIndex != origColIndex) || ((indexChanged || instant) && itemColIndex != origColIndex && itemColIndex != prevColIndex))
                    fadeTransition = FADE_INVISIBLE;
                //Log.d("XMBView", "item: " + item.title + " col: " + itemColIndex + " prevCol: " + prevColIndex + " transition: " + fadeTransition);
                if (itemColIndex >= origColIndex - 1 && itemColIndex <= origColIndex + 1 && inBounds)
                    drawItem(reusableRect, fadeTransition, instant, colIndex, rowIndex);
                else
                    returnItemView(colIndex, rowIndex);//traversableToTotalIndex(i));
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
        //Integer[] currentPos = getPosition();
        Integer[] currentEntry = getEntryPosition();//Arrays.copyOf(currentPos, currentPos.length - 1);//innerItemEntryPos.toArray(new Integer[0]);
        //Integer[] adapterEntry = new Integer[position.length];
        //Integer[] innerPosition = new Integer[position.length + 1];
        Integer[] innerPosition = Arrays.copyOf(position, position.length + 1);
        //Integer[] adapterInnerPosition = new Integer[position.length + 1];
        // copy array
//        for (int i = 0; i < position.length; i++) {
//            innerPosition[i] = position[i];
//            int mappedPos = mapTotalIndex(position[i]);
//            adapterInnerPosition[i] = (i == 0) ? mappedPos : position[i];
//            adapterEntry[i] = (i == 0) ? mappedPos : position[i];
//        }
        // draw list of inner items
        for (int innerItemIndex = 0; innerItemIndex < adapter.getInnerItemCount(position); innerItemIndex++) {
            innerPosition[innerPosition.length - 1] = innerItemIndex;
            //adapterInnerPosition[adapterInnerPosition.length - 1] = i;
            // if the inner item has inner items of its own, then draw them
            if (adapter.hasInnerItems(innerPosition))
                drawInnerItems(instant, innerPosition);
            // if the current inner item is where the user is right now, then draw it
            if (currentEntry != null && (currentEntry.length > 0 && (((innerPosition.length - 1) == currentEntry.length) && isPartOfPosition(innerPosition, currentEntry)) || isPartOfPosition(currentEntry, innerPosition))) {
                //Log.d("XMBView", Arrays.toString(innerPosition) + " is part of " + Arrays.toString(currentEntry));
                //Log.d("XMBView", "If in view, draw " + Arrays.toString(innerPosition) + " which is inside of " + Arrays.toString(currentEntry));
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
//    public void checkAlphas() {
//        for (Integer key : usedItemViews.keySet()) {
//            Log.d("XMBView", key + " alpha is " + usedItemViews.get(key).itemView.getAlpha());
//        }
//    }
    private static final int FADE_VISIBLE = 0;
    private static final int FADE_INVISIBLE = 1;
    private static final int FADE_OUT = 2;
    private static final int FADE_IN = 3;
    private void drawItem(Rect itemBounds, int fadeTransition, boolean instant, Integer... itemPosition) {
        //boolean isCat = itemPosition[0] >= 0 && itemPosition.length == 1 && getTotalIndexOfCol(getColIndexFromTotal(itemPosition[0])) == itemPosition[0];
        boolean isCat = itemPosition.length == 2 && itemPosition[1] == 0;
        boolean isInnerItem = itemPosition.length > 2;
        ViewHolder viewHolder = getViewHolder(isCat ? CATEGORY_TYPE : isInnerItem ? INNER_TYPE : ITEM_TYPE, itemPosition);
        //if (viewHolder != null) {
        viewHolder.setX(itemBounds.left);
        viewHolder.setY(itemBounds.top);
        //viewHolder.itemViewType = isCat ? CATEGORY_TYPE : ITEM_TYPE;
        Integer[] currentPosition = getPosition();
        boolean isSelection = isSamePosition(currentPosition, itemPosition);//getTotalIndexFromTraversable(currentIndex) == totalIndex;
        boolean isPartOfPosition = isPartOfPosition(currentPosition, itemPosition);
        viewHolder.isHighlighted = moveMode && isSelection;
        viewHolder.requestHideTitle = (moveMode && isSelection) || (isInsideItem() && isPartOfPosition && !isSelection);
        //int currentColIndex = getColIndexOf(totalIndex);
        //boolean isOurCat = isSamePosition(itemPosition, getTotalIndexOfCol(getColIndex()));
        boolean isOurCat = itemPosition[0] == this.colIndex;
        float itemAlpha = (isPartOfPosition || (!isInsideItem() && isOurCat) || isInnerItem) ? fullItemAlpha : (isInsideItem() ? innerItemOverlayTranslucent : translucentItemAlpha);
        //if (isSelection)
        //    Log.d("XMBView", "Current item alpha is " + itemAlpha);
        //viewHolder.isSelectionCategory = getColIndex() == getColIndexOf(totalIndex);
        //Log.d("XMBView", totalIndex + " alpha is " + viewHolder.itemView.getAlpha());
        viewHolder.itemView.animate().cancel();

//        Integer[] mappedPosition = itemPosition.clone();
//        if (mappedPosition[0] >= 0)
//            mappedPosition[0] = mapTotalIndex(mappedPosition[0]);
        adapter.onBindViewHolder(viewHolder, itemPosition);
        //Log.d("XMBView", "Setting item " + item.title);
        //viewHolder.isCategory = isCat;
        if (viewHolder.isNew || instant) {// || !indexChanged) {
            //Log.d("XMBView", "Placing item #" + totalIndex + " at (" + viewHolder.getX() + ", " + viewHolder.getY() + ")");
            // if the view just popped into existence then set its position to the final position rather than transitioning
            viewHolder.itemView.setX(viewHolder.getX());
            viewHolder.itemView.setY(viewHolder.getY());
//                viewHolder.setX(itemBounds.left);
//                viewHolder.setY(itemBounds.top);
        } else {
            //Log.d("XMBView", "Moving item #" + totalIndex + " from (" + viewHolder.getPrevX() + ", " + viewHolder.getPrevY() + ") to (" + viewHolder.getX() + ", " + viewHolder.getY() + ")");
            // if this view already existed then animate it from where it was to the final position
            viewHolder.itemView.setX(viewHolder.getPrevX());
            viewHolder.itemView.setY(viewHolder.getPrevY());
            viewHolder.itemView.animate().setDuration(300);
            viewHolder.itemView.animate().xBy(viewHolder.getX() - viewHolder.getPrevX());
            viewHolder.itemView.animate().yBy(viewHolder.getY() - viewHolder.getPrevY());
        }
        switch (fadeTransition) {
            case FADE_VISIBLE:
                //Log.d("XMBView", "Applying 1 alpha to " + totalIndex);
                viewHolder.itemView.setAlpha(itemAlpha);
                break;
            case FADE_INVISIBLE:
                //Log.d("XMBView", "Applying 0 alpha to " + totalIndex);
                viewHolder.itemView.setAlpha(0);
                break;
            case FADE_OUT:
                //Log.d("XMBView", "Fading out " + totalIndex);
                //viewHolder.itemView.setAlpha(itemAlpha);
                viewHolder.itemView.animate().setDuration(300);
                viewHolder.itemView.animate().alphaBy(-viewHolder.itemView.getAlpha());
                break;
            case FADE_IN:
                //Log.d("XMBView", "Fading in " + totalIndex);
                viewHolder.itemView.setAlpha(0);
                viewHolder.itemView.animate().setDuration(300);
                viewHolder.itemView.animate().alphaBy(itemAlpha);
                break;
        }
            //requestLayout();
            //Log.d("XMBView", "Item #" + totalIndex + " alpha set to " + viewHolder.itemView.getAlpha());
            //Log.d("XMBView", "Item #" + totalIndex + " visibility: " + viewHolder.itemView.getVisibility());
        //} else
        //    Log.w("XMBView", "Missing view holder for item with index " + totalIndex + ", skipped for now");
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
        //Log.d("XMBView2", getWidth() + ", " + getHeight());
        for (int i = 0; i < 5; i++) {
            ViewHolder newHolder = adapter.onCreateViewHolder(this, ITEM_TYPE);
            newHolder.itemViewType = ITEM_TYPE;
            newHolder.itemView.setVisibility(GONE);
            addView(newHolder.itemView);
            newHolder.itemView.measure(getWidth(), getHeight());
            //Log.d("XMBView", "measured: (" + newHolder.itemView.getMeasuredWidth() + ", " + newHolder.itemView.getMeasuredHeight() + ")");
            itemSize = Math.max(newHolder.itemView.getMeasuredWidth(), newHolder.itemView.getMeasuredHeight());
            //horSpacing = iconSize * 0.33f;
            adapter.onViewAttachedToWindow(newHolder);
            goneItemViews.push(newHolder);
        }
    }
    private void createCatViews() {
        //Log.d("XMBView2", getWidth() + ", " + getHeight());
        for (int i = 0; i < 5; i++) {
            ViewHolder newHolder = adapter.onCreateViewHolder(this, CATEGORY_TYPE);
            newHolder.itemViewType = CATEGORY_TYPE;
            newHolder.itemView.setVisibility(GONE);
            addView(newHolder.itemView);
            newHolder.itemView.measure(getWidth(), getHeight());
            //Log.d("XMBView", "measured: (" + newHolder.itemView.getMeasuredWidth() + ", " + newHolder.itemView.getMeasuredHeight() + ")");
            catSize = Math.max(newHolder.itemView.getMeasuredWidth(), newHolder.itemView.getMeasuredHeight());
            //horSpacing = iconSize * 0.33f;
            adapter.onViewAttachedToWindow(newHolder);
            goneCatViews.push(newHolder);
        }
    }
    private void createInnerItemViews() {
        //Log.d("XMBView2", getWidth() + ", " + getHeight());
        for (int i = 0; i < 5; i++) {
            ViewHolder newHolder = adapter.onCreateViewHolder(this, INNER_TYPE);
            newHolder.itemViewType = INNER_TYPE;
            newHolder.itemView.setVisibility(GONE);
            addView(newHolder.itemView);
            newHolder.itemView.measure(getWidth(), getHeight());
            //Log.d("XMBView", "measured: (" + newHolder.itemView.getMeasuredWidth() + ", " + newHolder.itemView.getMeasuredHeight() + ")");
            innerItemSize = Math.min(newHolder.itemView.getMeasuredWidth(), newHolder.itemView.getMeasuredHeight());
            //horSpacing = iconSize * 0.33f;
            adapter.onViewAttachedToWindow(newHolder);
            goneInnerItemViews.push(newHolder);
        }
    }
    private void returnItemView(Integer... position) {
        int indexHash = MathHelpers.hash(position);//position[0] >= 0 ? mapPosition(position) : position);
        if (usedViews.containsKey(indexHash)) {
            //Log.d("XMBView", "Returning view id " + totalIndex);
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
        //goneItemViews.addAll(usedViews.values());
        usedViews.clear();
    }
    private ViewHolder getViewHolder(int viewType, Integer... position) {
        ViewHolder viewHolder = null;
        int indexHash = MathHelpers.hash(position);//position[0] >= 0 ? mapPosition(position) : position);
        //int index = getTotalIndex(item);
        //boolean isNew = false;
        //Log.d("XMBView", "Retrieving view of " + item.title + " whose total index is " + index);
        if (usedViews.containsKey(indexHash)) {
            //Log.d("XMBView", "View already visible");
            viewHolder = usedViews.get(indexHash);
            viewHolder.isNew = false;
            //Log.d("XMBView", "Retrieving view for " + item.title + " whose value is already set to " + view.title);
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
        //itemView[0] = viewHolder;
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
        Integer[] currentPosition = getPosition();
        // get the horizontal pixel position of the item
        int expX = (isInsideItem() && colIndex == getPosition()[0] ? getStartX() + (currentPosition.length - 1) * -innerItemSize : startX + horShiftOffset * colIndex);//getColIndexFromTotal(totalIndex));
        // the vertical pixel position is the same since the categories go along a straight line
        int expY = startY;
        // get the right and bottom values of the item relative to the left and top values and apply them to the rect
        int right = expX + catSize;// + Math.max(iconSize, rect.width());
        int bottom = expY + catSize;// + textCushion + rect.height();
        rect.set(expX, expY, right, bottom);
    }
    // calculates the item's rect size with the text to the right of the item
    private void calcItemRect(int startX, int startY, int horShiftOffset, int verShiftOffset, int colIndex, int localIndex, Rect rect) {
        // get what column the item is in
        //int colIndex = getColIndexFromTotal(totalIndex);
        // get the index within the column of the item we are currently calculating the rect for
        //int localIndex = getLocalIndexFromTotal(totalIndex);
        //int totalIndex = getTotalIndex(colIndex, localIndex);
        boolean isPartOfInsideItem = isPartOfPosition(getPosition(), colIndex, rowIndex);
        // get the index of what is actually highlighted currently within the column
        int itemCatIndex = getCachedIndexOfCat(colIndex);
        int halfCatDiff = Math.round(Math.abs(catSize - itemSize) / 2f);
        // get the horizontal pixel position of the item
        int expX = halfCatDiff + (isInsideItem() && isPartOfInsideItem ? getStartX() + (innerItemEntryPos.size() - 1) * -Math.round(innerItemSize + innerHorSpacing) : startX + horShiftOffset * colIndex);
        // get the vertical pixel position of the item
        int expY = halfCatDiff + (isInsideItem() && isPartOfInsideItem ? startY : Math.round((startY - catPos.get(colIndex)) + verShiftOffset * localIndex + (localIndex >= itemCatIndex ? catSize + subItemGap : 0)));
        // get the right and bottom values of the item relative to the left and top values and apply them to the rect
        int right = expX + itemSize;// + textCushion + rect.width();
        int bottom = expY + itemSize;
        rect.set(expX, expY, right, bottom);
    }
    private void calcInnerItemRect(Rect rect, int startX, int startY, int horSpacing, int verSpacing, Integer... position) {
        //XMBItem item = getTotalIndexFromTraversable(traversableIndex);

        int halfCatDiff = Math.round(Math.abs(catSize - innerItemSize) / 2f);

        // get the horizontal pixel position of the item
        int expX = startX + halfCatDiff + (position.length - innerItemEntryPos.size()) * (innerItemSize + horSpacing);
        // get the vertical pixel position of the item
        int expY = startY + halfCatDiff + (position.length > innerItemEntryPos.size() ? position[position.length - 1] * (innerItemSize + verSpacing) - Math.round(innerItemVerPos.peek()) : 0);
        // get the right and bottom values of the item relative to the left and top values and apply them to the rect
        int right = expX + innerItemSize;// + textCushion + rect.width();
        int bottom = expY + innerItemSize;
        rect.set(expX, expY, right, bottom);
    }

    public Object getSelectedItem() {
//        Integer[] position = getPosition();
//        position[0] = mapTotalIndex(position[0]);
//        return adapter.getItem(position);
        return adapter.getItem(getPosition());
    }

    public boolean catHasSubItems(int colIndex) {
        return adapter.getColumnSize(colIndex) > 1;
    }
    public boolean isColumnHead(int colIndex) {
        return adapter.isColumnHead(colIndex, 0);//mapTotalIndex(getTotalIndexOfCol(colIndex)));
    }

    protected void setIndex(int colIndex, int localIndex, boolean instant) {
        if ((colIndex != this.colIndex || localIndex != this.rowIndex) && adapter.getItemCount(false) > 0) {
            //changed = true;
//            int traversableSize = getTraversableCount();
//            if (index < 0)
//                index = 0;
//            if (index >= traversableSize)
//                index = traversableSize - 1;
            //Log.d("XMBView", "Setting index to " + index + " and prevIndex to " + currentIndex);
            //int colIndex = getColIndexFromTraversable(index);
            //int localIndex = getLocalIndexFromTraversable(index);
            //Log.d("XMBView", "Setting index to col#: " + colIndex + " item#: " + localIndex);
            setShiftXAndY(getShiftX(colIndex), getShiftY(localIndex, colIndex), instant);
            //prevIndex = currentIndex;
            //currentIndex = index;
            //int prevColIndex = getColIndexFromTraversable(prevIndex);
            //If the column has multiple items, set the index cache of the column as well
            //if (columnHasSubItems(prevColIndex)) {

            //int localIndex = traversableToLocalIndex(currentIndex);
            //Log.d("XMBView", "Setting traversable to " + index + " setting col " + colIndex + " cache to " + localIndex);
            //catIndices.set(colIndex, localIndex);
            //}
        }
    }
//    protected void setIndex(int colIndex, int localIndex, boolean instant) {
//        setIndex(getTraversableIndexFromLocal(localIndex, colIndex), instant);
//    }
//    protected void setIndex(int index, boolean instant) {
//        //boolean changed = false;
//        // added index != currentIndex to allow alpha transition and potentially future transitions to work properly
//        if (index != currentIndex && mapper.size() > 0) {
//            //changed = true;
//            int traversableSize = getTraversableCount();
//            if (index < 0)
//                index = 0;
//            if (index >= traversableSize)
//                index = traversableSize - 1;
//            //Log.d("XMBView", "Setting index to " + index + " and prevIndex to " + currentIndex);
//            int colIndex = getColIndexFromTraversable(index);
//            int localIndex = getLocalIndexFromTraversable(index);
//            //Log.d("XMBView", "Setting index to col#: " + colIndex + " item#: " + localIndex);
//            setShiftXAndY(getShiftX(colIndex), getShiftY(localIndex, colIndex), instant);
//            //prevIndex = currentIndex;
//            //currentIndex = index;
//            //int prevColIndex = getColIndexFromTraversable(prevIndex);
//            //If the column has multiple items, set the index cache of the column as well
//            //if (columnHasSubItems(prevColIndex)) {
//
//                //int localIndex = traversableToLocalIndex(currentIndex);
//                //Log.d("XMBView", "Setting traversable to " + index + " setting col " + colIndex + " cache to " + localIndex);
//                //catIndices.set(colIndex, localIndex);
//            //}
//        }
//        //setViews(changed);
//        //return changed;
//    }
    protected Integer[] getPosition() {
        Integer[] position;
        if (isInsideItem()) {
            position = new Integer[innerItemEntryPos.size() + 3]; // 2 for colIndex and rowIndex and 1 for the local index within the current item
            //innerItemEntryPos.copyInto(position);
            position[0] = this.colIndex;
            position[1] = this.rowIndex;
            if (innerItemEntryPos.size() > 0) {
                Stack<Integer> entryClone = (Stack<Integer>) innerItemEntryPos.clone();
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
                Stack<Integer> entryClone = (Stack<Integer>) innerItemEntryPos.clone();
                for (int i = position.length - 1; i > 1; i--)
                    position[i] = entryClone.pop();
            }
        }
        return position;
    }
//    private int getTotalIndexOfCol(int colIndex) {
//        int totalIndex = -1;
//        if (colIndex >= 0) {
//            totalIndex = 0;
//            for (int i = 0; i < colIndex; i++)
//                totalIndex += mapper.get(i).size();
//        }
//        return totalIndex;
//    }
//    protected int getIndex() {
//        return currentIndex;
//    }
    protected int getColIndex() {
        return this.colIndex;//getColIndexFromTraversable(currentIndex);
    }
    protected int getLocalIndex() {
        return this.rowIndex;//getLocalIndexFromTraversable(currentIndex);
    }
//    private int getTotalIndexFromTraversable(int traversableIndex) {
//        int totalIndex = -1;
//        //if (mapper.size() > 0) {
//        if (traversableIndex >= 0) {
//            totalIndex = traversableIndex;
//            int colIndex = getColIndexFromTraversable(traversableIndex);
//            for (int i = 0; i <= colIndex; i++)
//                totalIndex += mapper.get(i).size() > 1 ? 1 : 0;
//        }
//        // Go through each column subtracting the amount of items there from the index until the index becomes smaller than the current column size
//        // meaning we've reached our column, then use the remaining index to retrieve the item
////        for (int i = 0; i < mapper.size(); i++) {
////            List<Integer> cat = mapper.get(i);
////            if (cat != null) {
////                boolean colHasSubItems = cat.size() > 1;
////                if ((colHasSubItems && currentPos < cat.size() - 1) || (!colHasSubItems && currentPos < cat.size())) {
////                    totalIndex = cat.get(colHasSubItems ? currentPos + 1 : currentPos);
////                    break;
////                } else if (cat.size() == 1)
////                    currentPos -= 1;
////                else if (cat.size() > 1)
////                    currentPos -= (cat.size() - 1);
////                else
////                    Log.e("XMBView", "Category list @" + i + " is empty");
////            } else {
////                Log.e("XMBView", "Category list @" + i + " is null");
////            }
////        }
//        //}
//        return totalIndex;
//    }
//    private int getColIndexFromTotal(int totalIndex) {
//        int colIndex = -1;
//        int index = totalIndex;
//        for (int i = 0; i < mapper.size(); i++) {
//            int columnSize = mapper.get(i).size();
//            if (index < columnSize) {
//                colIndex = i;
//                break;
//            }
//            index -= columnSize;
////            for (int j = 0; j < mapper.get(i).size(); j++) {
////                if (mapper.get(i).get(j) == totalIndex) {
////                    index = i;
////                    break;
////                }
////            }
////            if (index >= 0)
////                break;
//        }
//        return colIndex;
//    }

    private int getCachedIndexOfCat(int colIndex) {
        return yToIndex(catPos.get(colIndex), colIndex);
        //return catIndices.get(colIndex);
    }
    private float getCachedPosOfCat(int colIndex) {
        return catPos.get(colIndex);
    }

    @Override
    public boolean receiveKeyEvent(KeyEvent key_event) {
        //Log.d("XMBView", key_event.toString());
        if (key_event.getAction() == KeyEvent.ACTION_DOWN) {
            if (key_event.getKeyCode() == KeyEvent.KEYCODE_BUTTON_A) {
                stopMomentum();
                affirmativeAction();
                return true;
            }
            if (key_event.getKeyCode() == KeyEvent.KEYCODE_BUTTON_Y) {
                stopMomentum();
                secondaryAction();
                return true;
            }
            if (key_event.getKeyCode() == KeyEvent.KEYCODE_BUTTON_B || key_event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
                stopMomentum();
                cancelAction();
                return true;
            }
            if (key_event.getKeyCode() == KeyEvent.KEYCODE_DPAD_DOWN) {
                stopMomentum();
                selectLowerItem();
                return true;
            }
            if (key_event.getKeyCode() == KeyEvent.KEYCODE_DPAD_UP) {
                stopMomentum();
                selectUpperItem();
                return true;
            }
            if (key_event.getKeyCode() == KeyEvent.KEYCODE_DPAD_LEFT) {
                stopMomentum();
                selectLeftItem();
                return true;
            }
            if (key_event.getKeyCode() == KeyEvent.KEYCODE_DPAD_RIGHT) {
                stopMomentum();
                selectRightItem();
                return true;
            }
        }

        //Block out default back events
        return key_event.getKeyCode() == KeyEvent.KEYCODE_BACK || key_event.getKeyCode() == KeyEvent.KEYCODE_BUTTON_B;
    }
    public void selectLowerItem() {
        shiftYDisc(1);
    }
    public void selectUpperItem() {
        shiftYDisc(-1);
    }
    public void selectRightItem() {
        shiftXDisc(1);
    }
    public void selectLeftItem() {
        shiftXDisc(-1);
    }
    public boolean affirmativeAction() {
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
        return false;
    }
    public boolean cancelAction() {
        if (moveMode) {
            toggleMoveMode(false);
            //setViews(false, true);
            return true;
        } else if (isInsideItem()) {
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
    public void toggleMoveMode(boolean onOff) {
        moveMode = onOff;
        if (moveMode) {
            moveColIndex = getColIndex();
            moveLocalIndex = getLocalIndex();
            origMoveColIndex = moveColIndex;
            origMoveLocalIndex = moveLocalIndex + (catHasSubItems(moveColIndex) ? 1 : 0);
        }
        //setAdapterHighlightColor(moveMode ? moveHighlightColor : normalHighlightColor);
        setViews(false, true);
        //refresh();
    }
//    private void setAdapterHighlightColor(int color) {
//        getAdapter().highlightColor = color;
//    }
    private void applyMove() {
        boolean hasSubItems = catHasSubItems(moveColIndex);
        int newLocalIndex = moveLocalIndex + (hasSubItems ? 1 : 0);
        //Log.d("HomeView", "Attempting to move (" + origMoveColIndex + ", " + origMoveLocalIndex + ") => (" + newColIndex + ", " + newLocalIndex + ")");
        if (moveColIndex != origMoveColIndex || newLocalIndex != origMoveLocalIndex)
            onAppliedMove(origMoveColIndex, origMoveLocalIndex, moveColIndex, newLocalIndex);
        toggleMoveMode(false);
        //setViews(true, true);
        //refresh();
    }
    protected void onAppliedMove(int fromColIndex, int fromLocalIndex, int toColIndex, int toLocalIndex) {
        // local indices are total within the columns they represent and not traversable
    }
    protected void onShiftHorizontally(int colIndex, int prevColIndex) {
//        if (moveMode) {
//            if (colIndex > prevColIndex) {
//                // moving right
//                boolean isInColumn = isColumnHead(moveColIndex);
//                int nextColIndex = Math.min(moveColIndex + 1, mapper.size() - (isInColumn ? 1 : 2)); // -2 due to settings
//                //Log.d("XMBView", "Attempting to move right from " + moveColIndex + " to " + nextColIndex + ", in column: " + isInColumn);
//                if (moveColIndex != nextColIndex) {
//                    int currentLocalIndex = isInColumn ? moveLocalIndex + 1 : moveLocalIndex;
//                    List<Integer> column = mapper.get(moveColIndex);
//                    if (isInColumn) {
//                        // we're in a column with other things, next column should be a new one with just us
//                        int moveItem = column.get(currentLocalIndex);
//                        column.remove(currentLocalIndex);
//                        List<Integer> newColumn = new ArrayList<>();
//                        newColumn.add(moveItem);
//                        mapper.add(nextColIndex, newColumn);
//                        catPos.add(nextColIndex, 0f);
//                        moveColIndex = nextColIndex;
//                        moveLocalIndex = 0;
//                    } else {
//                        // we're in a column made up solely of us, should remove ourselves first before moving
//                        boolean nextIsColumn = isColumnHead(nextColIndex);
//                        if (nextIsColumn) {
//                            // next column has sub-items, so place within
//                            int moveItem = mapper.get(moveColIndex).get(moveLocalIndex);
//                            int nextLocalIndex = getColLocalIndex(nextColIndex) + 1; // +1 since it has sub-items
//                            mapper.get(nextColIndex).add(nextLocalIndex, moveItem);
//                            mapper.remove(moveColIndex);
//                            catPos.remove(moveColIndex);
//                            // don't set moveColIndex since we removed a column
//                            moveLocalIndex = nextLocalIndex - 1; // -1 since XMBView uses traversable local index and not total
//                        } else {
//                            // next column does not have sub-items, so skip over
//                            List<Integer> moveColumn = mapper.get(moveColIndex);
//                            mapper.remove(moveColIndex);
//                            catPos.remove(moveColIndex);
//                            mapper.add(nextColIndex, moveColumn);
//                            catPos.add(nextColIndex, 0f);
//                            moveColIndex = nextColIndex;
//                            moveLocalIndex = 0;
//                        }
//                    }
//                    //refresh();
//                }
//            } else {
//                // moving left
//                boolean isInColumn = isColumnHead(moveColIndex);
//                int nextColIndex = Math.max(moveColIndex - 1, isInColumn ? -1 : 0); // -1 to move out of the 0th column
//                //Log.d("XMBView", "Attempting to move left from " + moveColIndex + " to " + nextColIndex + ", in column: " + isInColumn);
//                if (moveColIndex != nextColIndex) {
//                    int currentLocalIndex = isInColumn ? moveLocalIndex + 1 : moveLocalIndex;
//                    List<Integer> column = mapper.get(moveColIndex);
//                    //Log.d("XMBView", "Moving left from " + moveColIndex + " to " + nextColIndex + ", in sub-item column: " + hasSubItems);
//                    if (isInColumn) {
//                        //Log.d("XMBView", moveColIndex + ", " + nextColIndex);
//                        // we're in a column with other things, next column should be a new one with just us
//                        int moveItem = column.get(currentLocalIndex);
//                        column.remove(currentLocalIndex);
//                        List<Integer> newColumn = new ArrayList<>();
//                        newColumn.add(moveItem);
//                        mapper.add(moveColIndex, newColumn);
//                        catPos.add(moveColIndex, 0f);
//                        //moveColIndex = nextColIndex;
//                        moveLocalIndex = 0;
//                    } else {
//                        // we're in a column made up solely of us, should remove ourselves first before moving
//                        boolean nextIsColumn = isColumnHead(nextColIndex);
//                        if (nextIsColumn) {
//                            // next column has sub-items, so place within
//                            int moveItem = mapper.get(moveColIndex).get(moveLocalIndex);
//                            int nextLocalIndex = getColLocalIndex(nextColIndex) + 1; // +1 since it has sub-items
//                            mapper.get(nextColIndex).add(nextLocalIndex, moveItem);
//                            mapper.remove(moveColIndex);
//                            catPos.remove(moveColIndex);
//                            moveColIndex = nextColIndex;
//                            moveLocalIndex = nextLocalIndex - 1; // -1 since XMBView uses traversable local index and not total
//                        } else {
//                            // next column does not have sub-items, so skip over
//                            List<Integer> moveColumn = mapper.get(moveColIndex);
//                            mapper.remove(moveColIndex);
//                            catPos.remove(moveColIndex);
//                            mapper.add(nextColIndex, moveColumn);
//                            catPos.add(nextColIndex, 0f);
//                            moveColIndex = nextColIndex;
//                            moveLocalIndex = 0;
//                        }
//                    }
//                    //refresh();
//                }
//            }
//            //return true;
//        }
        //return -1;
    }
    protected void onShiftVertically(int colIndex, int localIndex, int prevLocalIndex) {
//        if (moveMode) {
//            if (localIndex > prevLocalIndex) {
//                // going down
//                int currentLocalIndex = isColumnHead(moveColIndex) ? moveLocalIndex + 1 : moveLocalIndex;
//                List<Integer> column = mapper.get(moveColIndex);
//                int nextLocalIndex = Math.min(currentLocalIndex + 1, column.size() - 1);
//                if (currentLocalIndex != nextLocalIndex) {
//                    int moveItem = column.get(currentLocalIndex);
//                    column.remove(currentLocalIndex);
//                    column.add(nextLocalIndex, moveItem);
//                    moveLocalIndex += 1;
//                    //refresh();
//                }
//            } else {
//                // going up
//                boolean hasSubItems = isColumnHead(moveColIndex);
//                int currentLocalIndex = hasSubItems ? moveLocalIndex + 1 : moveLocalIndex;
//                List<Integer> column = mapper.get(moveColIndex);
//                int nextLocalIndex = Math.max(currentLocalIndex - 1, hasSubItems ? 1 : 0);
//                if (currentLocalIndex != nextLocalIndex) {
//                    int moveItem = column.get(currentLocalIndex);
//                    column.remove(currentLocalIndex);
//                    column.add(nextLocalIndex, moveItem);
//                    moveLocalIndex -= 1;
//                    //refresh();
//                }
//            }
//            //return true;
//        }
        //return false;
    }
//    public void deleteSelection() {
//    }
//    @Override
//    public void refresh() {
//        setViews(false);
//    }
}
