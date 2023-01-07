package com.OxGames.OxShell.Views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.OxGames.OxShell.Data.XMBItem;
import com.OxGames.OxShell.Helpers.SlideTouchHandler;
import com.OxGames.OxShell.Interfaces.InputReceiver;
import com.OxGames.OxShell.Interfaces.Refreshable;
import com.OxGames.OxShell.Interfaces.SlideTouchListener;
import com.OxGames.OxShell.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

public class XMBView extends ViewGroup implements InputReceiver, SlideTouchListener, Refreshable {
    private final Context context;
    private final SlideTouchHandler slideTouch;

    private final Stack<XMBItemView> goneItemViews; //The views whose visibility are set to gone since they are not currently used
    private final HashMap<Integer, XMBItemView> usedItemViews; //The views that are currently displayed and the total index of the item they represent as their key

    private final ArrayList<Integer> catIndices;
    private final ArrayList<ArrayList<XMBItem>> items; //Each arraylist represents a column, the first item in the arraylist represents the category item

    private int iconSize = 196;
    private float textSize = 48; //Size of the text
    private int textCushion = 16; //Distance between item and text
    private float horSpacing = 64; //How much space to add between items horizontally
    private float verSpacing = 0; //How much space to add between items vertically
    //private float catShift = horSpacing + (iconSize + horSpacing) * 2; //How much to shift the categories bar horizontally
    private float catShift = horSpacing; //How much to shift the categories bar horizontally
    private int horShiftOffset = Math.round(iconSize + horSpacing); //How far apart each item is from center to center
    private int verShiftOffset = Math.round(iconSize + verSpacing); //How far apart each item is from center to center

    int currentIndex = 0;
    private int prevIndex = 0;

    private final Rect reusableRect = new Rect();
    private final ArrayList<Integer> reusableIndices = new ArrayList<>();
    private final Paint painter = new Paint();
    //private float xmbTrans = 1; //Animation transition value

    public XMBView(Context context) {
        this(context, null);
    }
    public XMBView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    public XMBView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        slideTouch = new SlideTouchHandler();
        slideTouch.addListener(this);

        goneItemViews = new Stack<>();
        usedItemViews = new HashMap<>();
        catIndices = new ArrayList<>();
        items = new ArrayList<>();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return true;
    }
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        slideTouch.update(ev);
        return true;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        removeViews();
        createViews();
        setViews();
    }

    private int getStartX() {
        int padding = getPaddingLeft();
        //padding = 0;
        return Math.round(padding + catShift); //Where the current item's column is along the x-axis
    }
    private int getStartY() {
        int vsy = getHeight(); //view size y
        float vey = vsy / 2f; //view extents y
        return Math.round(vey - iconSize / 2f); //Where the current item's column is along the y-axis
    }
    private int getColIndexFromTraversable(int traversableIndex) {
        int index = -1;
        if (items.size() > 0) {
            int currentPos = traversableIndex;
            // Go through each column subtracting the amount of items there from the index until the index becomes zero or less
            // meaning we've reached our column
            for (int i = 0; i < items.size(); i++) {
                ArrayList<XMBItem> cat = items.get(i);
                if (cat.size() == 1)
                    currentPos -= 1;
                else if (cat.size() > 1)
                    currentPos -= (cat.size() - 1);
                else
                    Log.e("XMBView", "Category list @" + i + " is empty");

                if (currentPos < 0) {
                    index = i;
                    break;
                }
            }
        }
        return index;
    }
    private int getColIndexFromTotal(int totalIndex) {
        int index = -1;
        if (items.size() > 0) {
            int currentPos = totalIndex;
            // Go through each column subtracting the amount of items there from the index until the index becomes zero or less
            // meaning we've reached our column
            for (int i = 0; i < items.size(); i++) {
                if (currentPos <= 0) {
                    index = i;
                    break;
                }
                ArrayList<XMBItem> cat = items.get(i);
                currentPos -= cat.size();
            }
        }
        return index;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        //Log.d("XMBView2", "onLayout called");

        for (int i = 0; i < getChildCount(); i++) {
            View view = getChildAt(i);

            if (view.getVisibility() == GONE)
                continue;

            if (view instanceof XMBItemView) {
                XMBItemView itemView = (XMBItemView)view;
                int expX = 0;
                int expY = 0;
                int right = expX + itemView.getFullWidth();
                int bottom = expY + itemView.getFullHeight();
                view.layout(expX, expY, right, bottom);
            } else
                Log.e("XMBView", "Child view @" + i + " is not of type XMBItemView");
        }
    }
    private void removeViews() {
        returnAllItemViews();
        while (!goneItemViews.isEmpty())
            removeView(goneItemViews.pop());
    }
    private void createViews() {
        //Log.d("XMBView2", getWidth() + ", " + getHeight());
        int colCount = (int)Math.ceil(getWidth() / (float)horShiftOffset) + 4; //+4 for off screen animating into on screen
        int rowCount = ((int)Math.ceil(getHeight() / (float)verShiftOffset) + 4) * 3; //+4 for off screen to on screen animating, *3 for column to column fade
        catShift = horSpacing + (iconSize + horSpacing) * (colCount / 6);
        for (int i = 0; i < (colCount + rowCount); i++) {
            //Log.d("XMBView2", "Creating item view");
            XMBItemView view;
            LayoutInflater layoutInflater = LayoutInflater.from(context);
            view = (XMBItemView) layoutInflater.inflate(R.layout.xmb_item, null);
            view.setVisibility(GONE);
            addView(view);
            goneItemViews.push(view);
        }
    }
    public XMBItem getSelectedItem() {
        return getTraversableItem(currentIndex);
    }
    private void returnItemView(int totalIndex) {
        if (usedItemViews.containsKey(totalIndex)) {
            //Log.d("XMBView", "Returning view id " + totalIndex);
            XMBItemView itemView = usedItemViews.get(totalIndex);
            itemView.setVisibility(GONE);
            goneItemViews.push(itemView);
            usedItemViews.remove(totalIndex);
        }
    }
    // Gets the total number of items without the category items that have sub items since they can't be 'highlighted' or in other words traversed
    private int getTraversableCount() {
        int count = 0;
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i) != null) {
                int size = items.get(i).size();
                count += size > 1 ? size - 1 : size;
            }
        }
        return count;
    }
    // Gets the actual total items count including category items
    private int getTotalCount() {
        int count = 0;
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i) != null)
                count += items.get(i).size();
        }
        return count;
    }
    private void returnAllItemViews() {
        for (XMBItemView itemView : usedItemViews.values())
            itemView.setVisibility(GONE);
        goneItemViews.addAll(usedItemViews.values());
        usedItemViews.clear();
    }
    private boolean getItemView(XMBItem item, XMBItemView[] itemView) {
        XMBItemView view = null;
        int index = getTotalIndex(item);
        boolean isNew = false;
        //Log.d("XMBView", "Retrieving view of " + item.title + " whose total index is " + index);
        if (usedItemViews.containsKey(index)) {
            //Log.d("XMBView", "View already visible");
            view = usedItemViews.get(index);
            //Log.d("XMBView", "Retrieving view for " + item.title + " whose value is already set to " + view.title);
        } else if (!goneItemViews.isEmpty()) {
            //Log.d("XMBView", "Requesting new view");
            //Log.d("XMBView", "Setting view values for " + item.title);
            isNew = true;
            view = goneItemViews.pop();
            view.title = item.title;
            view.icon = item.getIcon();
            view.setVisibility(VISIBLE);
            usedItemViews.put(index, view);
        }
        itemView[0] = view;
        return isNew;
    }
    private void setViews() {
        //Log.d("XMBView2", "Setting view positions");
        if (items.size() > 0) {
            setIndex(currentIndex);
            int colIndex = getColIndexFromTraversable(currentIndex);
            int startX = getStartX() - colIndex * horShiftOffset;
            int startY = getStartY();
            //Log.d("XMBView", "Current col index " + colIndex + " x: " + startX + " y: " + startY);
            //Log.d("XMBView", "Drawing views starting from " + getStartX());
            drawCategories(startX, startY, horShiftOffset);
            drawItems(currentIndex, startX, startY, horShiftOffset, verShiftOffset);
            //invalidate();
        }
    }
    private void drawCategories(int startXInt, int startYInt, int horShiftOffsetInt) {
        int viewWidth = getWidth();
        int viewHeight = getHeight();
        //Log.d("XMBView", "Drawing categories");
        for (int i = 0; i < getTotalColCount(); i++) {
            XMBItem cat = getCat(i);
            calcCatRect(startXInt, startYInt, horShiftOffsetInt, i, reusableRect);
            cat.setX(reusableRect.left);
            cat.setY(reusableRect.top);
            boolean inBounds = inView(reusableRect, viewWidth, viewHeight);
            Log.d("XMBView", "Setting category " + i + " title: " + cat.title + " inBounds: " + inBounds + " destX: " + cat.getX() + " destY: " + cat.getY());
            if (inBounds)
                drawItem(cat, true, FADE_VISIBLE);
            else
                returnItemView(getTotalIndexOfCol(i));
        }
    }
    private void drawItems(int itemIndex, int startXInt, int startYInt, int horShiftOffsetInt, int verShiftOffsetInt) {
        int origColIndex = getColIndexFromTraversable(itemIndex);
        int prevColIndex = getColIndexFromTraversable(prevIndex);
        int viewWidth = getWidth();
        int viewHeight = getHeight();
        int traversableCount = getTraversableCount();
        //Log.d("XMBView", "Drawing items");
        for (int i = 0; i < traversableCount; i++) {
            XMBItem item = getTraversableItem(i);
            int itemColIndex = getColIndexFromTraversable(i);
            // skip items that belong to a column with one item, those are drawn with the categories
            if (!columnHasSubItems(itemColIndex))
                continue;
            calcItemRect(startXInt, startYInt, horShiftOffsetInt, verShiftOffsetInt, i, reusableRect);
            int top = reusableRect.top;
            int width = reusableRect.width();
            int height = reusableRect.height();
            calcCatRect(startXInt, startYInt, horShiftOffsetInt, itemColIndex, reusableRect);
            reusableRect.right = reusableRect.left + width;
            reusableRect.top = top;
            reusableRect.bottom = reusableRect.top + height;
            item.setX(reusableRect.left);
            item.setY(reusableRect.top);

            boolean inBounds = inView(reusableRect, viewWidth, viewHeight);
            int fadeTransition = FADE_VISIBLE;
            if (itemColIndex == origColIndex && prevColIndex != origColIndex)
                fadeTransition = FADE_IN;
            else if (itemColIndex != origColIndex && itemColIndex == prevColIndex)
                fadeTransition = FADE_OUT;
            else if (itemColIndex != origColIndex && itemColIndex != prevColIndex)
                fadeTransition = FADE_INVISIBLE;
            //Log.d("XMBView", "item: " + item.title + " col: " + itemColIndex + " prevCol: " + prevColIndex + " transition: " + fadeTransition);
            if (itemColIndex >= origColIndex - 1 && itemColIndex <= origColIndex + 1 && inBounds)
                drawItem(item, false, fadeTransition);
            else
                returnItemView(traversableToTotalIndex(i));
        }
    }
    private static final int FADE_VISIBLE = 0;
    private static final int FADE_INVISIBLE = 1;
    private static final int FADE_OUT = 2;
    private static final int FADE_IN = 3;
    private void drawItem(XMBItem item, boolean isCat, int fadeTransition) {
        XMBItemView[] itemViewHolder = new XMBItemView[1];
        boolean isNewView = getItemView(item, itemViewHolder);
        XMBItemView itemView = itemViewHolder[0];
        if (itemView != null) {
            //Log.d("XMBView", "Setting item " + item.title);
            itemView.isCategory = isCat;
            if (isCat && isNewView) {
                // if the view just popped into existence then set its position to the final position rather than transitioning
                itemView.setX(item.getX());
                itemView.setY(item.getY());
            } else {
                // if this view already existed then animate it from where it was to the final position
                itemView.setX(item.getPrevX());
                itemView.setY(item.getPrevY());
                itemView.animate().setDuration(300);
                itemView.animate().xBy(item.getX() - item.getPrevX());
                itemView.animate().yBy(item.getY() - item.getPrevY());
            }
            switch (fadeTransition) {
                case FADE_VISIBLE:
                    itemView.setAlpha(1);
                    break;
                case FADE_INVISIBLE:
                    itemView.setAlpha(0);
                    break;
                case FADE_OUT:
                    itemView.setAlpha(1);
                    itemView.animate().setDuration(300);
                    itemView.animate().alphaBy(-1);
                    break;
                case FADE_IN:
                    itemView.setAlpha(0);
                    itemView.animate().setDuration(300);
                    itemView.animate().alphaBy(1);
                    break;
            }
        } else
            Log.w("XMBView", "Missing item view for " + item.title + ", skipped for now");
    }

    private void calcCatRect(int startX, int startY, int horShiftOffset, int colIndex, Rect rect) {
        //Gets the bounds of the category view
        XMBItem cat = getCat(colIndex);
        getTextBounds(painter, cat.title, textSize, rect);
        int expX = startX + horShiftOffset * colIndex;
        int expY = startY;
        int right = expX + Math.max(iconSize, rect.width());
        int bottom = expY + iconSize + textCushion + rect.height();
        rect.set(expX, expY, right, bottom);
    }
    private int traversableToTotalIndex(int traversableIndex) {
        int colIndex = getColIndexFromTraversable(traversableIndex);
        int index = traversableIndex;
        for (int i = 0; i <= colIndex; i++) {
            if (items.get(i).size() > 1)
                index += 1;
        }
        return index;
    }
    private int traversableToLocalIndex(int traversableIndex) {
        int index = -1;
        if (items.size() > 0) {
            int currentPos = traversableIndex;
            // Go through each column subtracting the amount of items there from the index until the index becomes zero or less
            // meaning we've reached our column
            for (int i = 0; i < items.size(); i++) {
                ArrayList<XMBItem> cat = items.get(i);
                boolean hasSubItems = cat.size() > 1;
                if ((hasSubItems && currentPos < cat.size() - 1) || (!hasSubItems && currentPos < cat.size())) {
                    index = currentPos;
                    break;
                } else if (cat.size() > 1)
                    currentPos -= (cat.size() - 1);
                else
                    currentPos -= cat.size();
            }
        }
        return index;
    }
    private int totalToLocalIndex(int totalIndex) {
        int index = -1;
        if (items.size() > 0) {
            int currentPos = totalIndex;
            // Go through each column subtracting the amount of items there from the index until the index becomes zero or less
            // meaning we've reached our column
            for (int i = 0; i < items.size(); i++) {
                ArrayList<XMBItem> cat = items.get(i);
                if (currentPos < cat.size()) {
                    index = currentPos;
                    break;
                } else
                    currentPos -= cat.size();
            }
        }
        return index;
    }
    private int localToTraversableIndex(int localIndex, int colIndex) {
        //This function expects that the column provided has sub items
        int index = localIndex;
        for (int i = 0; i < colIndex; i++) {
            ArrayList<XMBItem> cat = items.get(i);
            if (cat.size() > 1)
                index += cat.size() - 1;
            else
                index += cat.size();
        }
        return index;
    }
    private void calcItemRect(int startX, int startY, int horShiftOffset, int verShiftOffset, int itemIndex, Rect rect) {
        XMBItem item = getTraversableItem(itemIndex);
        int colIndex = getColIndexFromTraversable(itemIndex);
        int localIndex = traversableToLocalIndex(itemIndex);
        int itemCatIndex = getCachedIndexOfCat(colIndex); //What is actually highlighted currently within the column
        int expX = startX + horShiftOffset * colIndex;
        getTextBounds(painter, item.title, textSize, rect);
        int expY = (startY + rect.height()) + verShiftOffset * ((localIndex - itemCatIndex) + 1);
        if (localIndex < itemCatIndex)
            expY = startY - verShiftOffset * (((itemCatIndex - 1) - localIndex) + 1);
        int right = expX + iconSize + textCushion + rect.width();
        int bottom = expY + iconSize;
        rect.set(expX, expY, right, bottom);
    }
    private boolean inView(Rect rect, int viewWidth, int viewHeight) {
        int left = -horShiftOffset;
        int right = viewWidth + horShiftOffset;
        int top = -verShiftOffset;
        int bottom = viewHeight + verShiftOffset;
        return ((rect.left < right && rect.left > left) || (rect.right > left && rect.right < right)) && ((rect.top < bottom && rect.top > top) || (rect.bottom > top && rect.bottom < bottom));
    }
    public static void getTextBounds(Paint painter, String text, float textSize, Rect rect) {
        if (text != null) {
            painter.setTextSize(textSize);
            //painter.setTextAlign(Paint.Align.CENTER);
            painter.getTextBounds(text, 0, text.length(), rect);
        } else
            rect.set(0, 0, 0, 0);
    }

//    @Override
//    protected void dispatchDraw(Canvas canvas) {
//        //Log.d("XMBView2", "dispatchDraw called");
//        super.dispatchDraw(canvas);
//        painter.setColor(0xFFFF0000);
//        painter.setStrokeWidth(8);
//        canvas.drawLine(0, 0, getWidth(), getHeight(), painter);
//        canvas.drawLine(0, getHeight(), getWidth(), 0, painter);
//    }


    public void setIconSize(int size) {
        iconSize = size;
        //TODO: loop through all XMBItemViews and set their iconSize to this value
    }
    private XMBItem getCat(int colIndex) {
        XMBItem cat = null;
        ArrayList<XMBItem> column = items.get(colIndex);
        if (column != null && column.size() > 0) {
            cat = column.get(0);
        } else
            Log.e("XMBView", "Column @" + colIndex + " is null");
        return cat;
    }
    private int getTotalColCount() {
        return items.size();
    }
    private int getColCount(int colIndex) {
        if (columnHasSubItems(colIndex))
            return items.get(colIndex).size() - 1;
        else
            return items.get(colIndex).size();
    }
    private boolean columnHasSubItems(int colIndex) {
        return items.get(colIndex).size() > 1;
    }
    public void setIndex(int index) {
        // added index != currentIndex to allow alpha transition and potentially future transitions to work properly
        if (index != currentIndex && items.size() > 0) {
            int traversableSize = getTraversableCount();
            if (index < 0)
                index = 0;
            if (index >= traversableSize)
                index = traversableSize - 1;
            //Log.d("XMBView", "Setting index to " + index + " and prevIndex to " + currentIndex);
            prevIndex = currentIndex;
            currentIndex = index;
            int colIndex = getColIndexFromTraversable(currentIndex);
            //If the column has multiple items, set the index cache of the column as well
            if (columnHasSubItems(colIndex)) {
                int localIndex = traversableToLocalIndex(currentIndex);
                //Log.d("XMBView", "Setting traversable to " + index + " setting col " + colIndex + " cache to " + localIndex);
                catIndices.set(colIndex, localIndex);
            }
        }
    }
    private int getTotalIndexOfCol(int colIndex) {
        int totalIndex = 0;
        for (int i = 0; i < colIndex; i++)
            totalIndex += items.get(i).size();
        return totalIndex;
    }
    public int getIndex() {
        return currentIndex;
    }
    private XMBItem getTraversableItem(int traversableIndex) {
        XMBItem item = null;
        if (items.size() > 0) {
            int currentPos = traversableIndex;
            // Go through each column subtracting the amount of items there from the index until the index becomes smaller than the current column size
            // meaning we've reached our column, then use the remaining index to retrieve the item
            for (int i = 0; i < items.size(); i++) {
                ArrayList<XMBItem> cat = items.get(i);
                if (cat != null) {
                    boolean colHasSubItems = cat.size() > 1;
                    if ((colHasSubItems && currentPos < cat.size() - 1) || (!colHasSubItems && currentPos < cat.size())) {
                        item = cat.get(colHasSubItems ? currentPos + 1 : currentPos);
                        break;
                    } else if (cat.size() == 1)
                        currentPos -= 1;
                    else if (cat.size() > 1)
                        currentPos -= (cat.size() - 1);
                    else
                        Log.e("XMBView", "Category list @" + i + " is empty");
                } else {
                    Log.e("XMBView", "Category list @" + i + " is null");
                }
            }
        }
        return item;
    }
    private int getColIndex(XMBItem cat) {
        int index = -1;
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).get(0) == cat) {
                index = i;
                break;
            }
        }
        return index;
    }
    private int getTotalIndex(XMBItem item) {
        int index = 0;
        boolean found = false;
        for (int i = 0; i < items.size(); i++) {
            ArrayList<XMBItem> column = items.get(i);
            int localIndex = column.indexOf(item);
            if (localIndex >= 0) {
                found = true;
                index += localIndex;
                break;
            } else
                index += column.size();
        }
        if (!found)
            index = -1;
        return index;
    }
    private int getCachedIndexOfCat(int colIndex) {
        return catIndices.get(colIndex);
    }
    public void addSubItems(XMBItem[] items) {
        for (XMBItem item : items)
            addSubItem(item, item.colIndex);
    }
    public void addSubItems(List<XMBItem> items) {
        for (XMBItem item : items)
            addSubItem(item, item.colIndex, false);
    }
    public void addSubItem(XMBItem item, int colIndex) {
        addSubItem(item, colIndex, true);
    }
    public void addSubItem(XMBItem item, XMBItem cat) {
        addSubItem(item, getColIndex(cat));
    }
    private void addSubItem(XMBItem item, int colIndex, boolean refresh) {
        ArrayList<XMBItem> cat = items.get(colIndex);
        item.colIndex = colIndex;
        if (item.localIndex >= 0 && item.localIndex <= cat.size()) {
            // if the local index given is within the correct range then insert the item there
            cat.add(item.localIndex, item);
        } else {
            // if the local index is not within range then place the item at the end and set its local index to reflect that
            item.localIndex = cat.size();
            cat.add(item);
        }
        if (refresh)
            setViews();
    }
    public void addCatItems(XMBItem[] items) {
        for (int i = 0; i < items.length; i++)
            addCatItem(items[i], false);
    }
    public void addCatItems(List<XMBItem> items) {
        for (int i = 0; i < items.size(); i++)
            addCatItem(items.get(i), false);
    }
    public void addCatItem(XMBItem item) {
        addCatItem(item, true);
    }
    private void addCatItem(XMBItem item, boolean refresh) {
        ArrayList<XMBItem> cat = new ArrayList<>();
        cat.add(item);
        // since this item is a cat item then set its local index to 0
        item.localIndex = 0;
        if (item.colIndex >= 0 && item.colIndex <= items.size()) {
            // if the col index is within range then insert into the given position
            items.add(item.colIndex, cat);
            catIndices.add(item.colIndex, 0);
        } else {
            // if the col index is not within the range of the items list then add to the end and update value
            item.colIndex = items.size();
            items.add(cat);
            catIndices.add(0);
        }

        if (refresh)
            setViews();
    }
    public void removeItem(XMBItem item) {
        int totalIndex = getTotalIndex(item);
        removeItem(totalIndex);
    }
    public void removeItem(int totalIndex) {
        int colIndex = getColIndexFromTotal(totalIndex);
        int adjustedIndex = currentIndex;
        if (columnHasSubItems(colIndex)) {
            //If the column has sub items then possibly we are removing a sub item
            int localIndex = totalToLocalIndex(totalIndex);
            if (localIndex != 0) {
                //If the item being removed isn't the category item then just remove the item
                items.get(colIndex).remove(localIndex);
                //Readjust cached index
                int relIndex = catIndices.get(colIndex);
                if (relIndex >= localIndex) {
                    //If the current cached position exists after or at what was removed then decrease the cached index by 1
                    relIndex -= 1;
                    if (relIndex < 0)
                        relIndex = 0;
                    catIndices.set(colIndex, relIndex);
                }
            } else {
                //If the item being removed is the category item then remove the entire column
                items.remove(colIndex);
                catIndices.remove(colIndex);
            }
        } else {
            //If not then we are just removing the column item (so the whole column)
            items.remove(colIndex);
            catIndices.remove(colIndex);
        }
        returnItemView(totalIndex);
        setIndex(adjustedIndex);
        setViews();
    }
    public void clear() {
        catIndices.clear();
        items.clear();
        returnAllItemViews();
        setViews();
    }

    @Override
    public void onClick() {
        makeSelection();
    }
    @Override
    public void onSwipeDown() {
        selectLowerItem();
    }
    @Override
    public void onSwipeLeft() {
        selectLeftItem();
    }
    @Override
    public void onSwipeRight() {
        selectRightItem();
    }
    @Override
    public void onSwipeUp() {
        selectUpperItem();
    }
    @Override
    public boolean receiveKeyEvent(KeyEvent key_event) {
        //Log.d("XMBView2", key_event.toString());
        if (key_event.getAction() == KeyEvent.ACTION_DOWN) {
            if (key_event.getKeyCode() == KeyEvent.KEYCODE_BUTTON_A) {
                makeSelection();
                return true;
            }
            if (key_event.getKeyCode() == KeyEvent.KEYCODE_DPAD_DOWN) {
                selectLowerItem();
                return true;
            }
            if (key_event.getKeyCode() == KeyEvent.KEYCODE_DPAD_UP) {
                selectUpperItem();
                return true;
            }
            if (key_event.getKeyCode() == KeyEvent.KEYCODE_DPAD_LEFT) {
                selectLeftItem();
                return true;
            }
            if (key_event.getKeyCode() == KeyEvent.KEYCODE_DPAD_RIGHT) {
                selectRightItem();
                return true;
            }
        }

        //Block out default back events
        return key_event.getKeyCode() == KeyEvent.KEYCODE_BACK || key_event.getKeyCode() == KeyEvent.KEYCODE_BUTTON_B;
    }
    public void selectLowerItem() {
        int colIndex = getColIndexFromTraversable(currentIndex);
        if (columnHasSubItems(colIndex)) {
            int colSize = getColCount(colIndex);
            int localIndex = traversableToLocalIndex(currentIndex);
            if (localIndex + 1 < colSize) {
                setIndex(currentIndex + 1);
                setViews();
            }
        }
    }
    public void selectUpperItem() {
        int colIndex = getColIndexFromTraversable(currentIndex);
        if (columnHasSubItems(colIndex)) {
            int localIndex = traversableToLocalIndex(currentIndex);
            if (localIndex - 1 >= 0) {
                setIndex(currentIndex - 1);
                setViews();
            }
        }
    }
    public void selectRightItem() {
        int colIndex = getColIndexFromTraversable(currentIndex);
        if (colIndex + 1 < getTotalColCount()) {
            int localIndex = getCachedIndexOfCat(colIndex + 1);
            int nextIndex = localToTraversableIndex(localIndex, colIndex + 1);
            //Log.d("XMBView", "Right => colFrom: " + colIndex + " colTo: " + (colIndex + 1) + " localTo: " + localIndex + " traversableTo: " + nextIndex + " traversableFrom: " + currentIndex);
            setIndex(nextIndex);
            setViews();
        }
    }
    public void selectLeftItem() {
        int colIndex = getColIndexFromTraversable(currentIndex);
        if (colIndex - 1 >= 0) {
            int localIndex = getCachedIndexOfCat(colIndex - 1);
            int nextIndex = localToTraversableIndex(localIndex, colIndex - 1);
            //Log.d("XMBView", "Left => colFrom: " + colIndex + " colTo: " + (colIndex - 1) + " localTo: " + localIndex + " traversableTo: " + nextIndex + " traversableFrom: " + currentIndex);
            setIndex(nextIndex);
            setViews();
        }
    }
    public void makeSelection() {
    }
    public void deleteSelection() {
    }
    @Override
    public void refresh() {
        setViews();
    }
}
