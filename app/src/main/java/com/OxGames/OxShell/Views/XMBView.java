package com.OxGames.OxShell.Views;

import android.content.Context;
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

    //private final Stack<XMBCategoryView> goneCatViews;
    //private final HashMap<Integer, XMBCategoryView> usedCatViews;
    private final Stack<XMBItemView> goneItemViews; //The views whose visibility are set to gone since they are not currently used
    private final HashMap<Integer, XMBItemView> usedItemViews; //The views that are currently displayed and the total index of the item they represent as their key

    //private final ArrayList<XMBCat> categories;
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

    private final Rect reusableRect = new Rect();
    //private final ArrayList<Integer> reusableIndices = new ArrayList<>();
    private final Paint painter = new Paint();
    //private float xmbTrans = 1; //Animation transition value

    //XMBItemView testView;
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

        //goneCatViews = new Stack<>();
        //usedCatViews = new HashMap<>();
        goneItemViews = new Stack<>();
        usedItemViews = new HashMap<>();
        //categories = new ArrayList<>();
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
                if (currentPos <= 0) {
                    index = i;
                    break;
                }
                ArrayList<XMBItem> cat = items.get(i);
                if (cat.size() == 1)
                    currentPos -= 1;
                else if (cat.size() > 1)
                    currentPos -= (cat.size() - 1);
                else
                    Log.e("XMBView", "Category list @" + i + " is empty");
            }
            //XMBCat currentCat = items.get(currentIndex).category;
            //index = currentCat != null ? categories.indexOf(currentCat) : toCatIndex(currentIndex); //The index of how far we are along the columns row
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
            //XMBCat currentCat = items.get(currentIndex).category;
            //index = currentCat != null ? categories.indexOf(currentCat) : toCatIndex(currentIndex); //The index of how far we are along the columns row
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

            //TODO: Possibly make a base class for both views that share some methods
            if (view instanceof XMBItemView) {
                XMBItemView itemView = (XMBItemView)view;
                int expX = 0;
                int expY = 0;
                int right = expX + itemView.getFullWidth();
                int bottom = expY + itemView.getFullHeight();
                view.layout(expX, expY, right, bottom);
            } else
                Log.e("XMBView", "Child view @" + i + " is not of type XMBItemView");
//            else if (view instanceof XMBCategoryView) {
//                XMBCategoryView catView = (XMBCategoryView)view;
//                int expX = 0;
//                int expY = 0;
//                int right = expX + catView.getFullWidth();
//                int bottom = expY + catView.getFullHeight();
//                view.layout(expX, expY, right, bottom);
//            }
        }
    }
    private void removeViews() {
        //returnAllCatViews();
        returnAllItemViews();
        //while (!goneCatViews.isEmpty())
        //    removeView(goneCatViews.pop());
        while (!goneItemViews.isEmpty())
            removeView(goneItemViews.pop());
    }
    private void createViews() {
        //Log.d("XMBView2", getWidth() + ", " + getHeight());
        int colCount = (int)Math.ceil(getWidth() / (float)horShiftOffset) + 4; //+4 for off screen animating into on screen
        int rowCount = ((int)Math.ceil(getHeight() / (float)verShiftOffset) + 4) * 3; //+4 for off screen to on screen animating, *3 for column to column fade
        catShift = horSpacing + (iconSize + horSpacing) * (colCount / 6);
//        for (int i = 0; i < colCount; i++) {
//            //Log.d("XMBView2", "Creating cat view");
//            XMBCategoryView view;
//            LayoutInflater layoutInflater = LayoutInflater.from(context);
//            view = (XMBCategoryView) layoutInflater.inflate(R.layout.xmb_category, null);
//            view.setVisibility(GONE);
//            addView(view);
//            goneCatViews.push(view);
//        }
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
//    private void returnCatView(int catIndex) {
//        if (usedCatViews.containsKey(catIndex)) {
//            XMBCategoryView catView = usedCatViews.get(catIndex);
//            catView.setVisibility(GONE);
//            goneCatViews.push(catView);
//            usedCatViews.remove(catIndex);
//        }
//    }
    public XMBItem getSelectedItem() {
        return getTraversableItem(currentIndex);
    }
    private void returnItemView(int totalIndex) {
        if (usedItemViews.containsKey(totalIndex)) {
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
//    private void returnUnusedViews() {
//        //For when items have been removed and their views still are shown
//        ArrayList<Integer> keys = new ArrayList<>(usedItemViews.keySet());
//        int size = getTotalCount();
//        for (int key : keys) {
//            if (key < 0 || key >= size)
//                returnItemView(key);
//        }
////        size = getCatCount();
////        keys = new ArrayList<>(usedCatViews.keySet());
////        for (int key : keys) {
////            if (key < 0 || key >= size)
////                returnCatView(key);
////        }
//    }
//    private void returnAllCatViews() {
//        for (XMBCategoryView catView : usedCatViews.values())
//            catView.setVisibility(GONE);
//        //for (int i = usedCatViews.size() - 1; i >= 0; i--)
//        //    usedCatViews.get(i).setVisibility(GONE);
//        goneCatViews.addAll(usedCatViews.values());
//        usedCatViews.clear();
//    }
    private void returnAllItemViews() {
        for (XMBItemView itemView : usedItemViews.values())
            itemView.setVisibility(GONE);
        //for (int i = usedItemViews.size() - 1; i >= 0; i--)
        //    usedItemViews.get(i).setVisibility(GONE);
        goneItemViews.addAll(usedItemViews.values());
        usedItemViews.clear();
    }
//    private XMBCategoryView getCatView(XMBCat cat) {
//        XMBCategoryView view = null;
//        int index = categories.indexOf(cat);
//        if (usedCatViews.containsKey(index)) {
//            //Log.d("XMBView2", "Requested cat on " + index);
//            //cat.skipAnim = false;
//            view = usedCatViews.get(index);
//        } else if (!goneCatViews.isEmpty()) {
//            //Log.d("XMBView2", "Requested nonexistant cat");
//            //cat.skipAnim = true;
//            view = goneCatViews.pop();
//            view.title = cat.title;
//            view.icon = cat.icon;
//            view.setVisibility(VISIBLE);
//            usedCatViews.put(index, view);
//        }
//
//        return view;
//    }
//    private XMBCategoryView getCatView(XMBItem item) {
//        XMBCategoryView view = null;
//        int index = getItemCatIndex(item);
//        if (usedCatViews.containsKey(index)) {
//            //Log.d("XMBView2", "Requested itemcat on " + index);
//            //item.skipAnim = false;
//            view = usedCatViews.get(index);
//        } else if (!goneCatViews.isEmpty()) {
//            //Log.d("XMBView2", "Requested nonexistant itemcat");
//            //item.skipAnim = true;
//            view = goneCatViews.pop();
//            view.title = item.title;
//            view.icon = item.getIcon();
//            view.setVisibility(VISIBLE);
//            usedCatViews.put(index, view);
//        }
//
//        return view;
//    }
    private XMBItemView getItemView(XMBItem item) {
        XMBItemView view = null;
        int index = getTotalIndex(item);
        //Log.d("XMBView", "Total index of " + item.title + " is " + index);
        //int index = items.indexOf(item);
        if (usedItemViews.containsKey(index)) {
            //Log.d("XMBView2", "Requested item on " + index);
            //item.skipAnim = false;
            view = usedItemViews.get(index);
        } else if (!goneItemViews.isEmpty()) {
            //Log.d("XMBView2", "Requested nonexistant item");
            //item.skipAnim = true;
            view = goneItemViews.pop();
            view.title = item.title;
            view.icon = item.getIcon();
            view.setVisibility(VISIBLE);
            usedItemViews.put(index, view);
        }
        return view;
    }
    private void setViews() {
        //Log.d("XMBView2", "Setting view positions");
        //returnAllCatViews();
        //returnAllItemViews();
        //returnUnusedViews();
        if (items.size() > 0) {
            setIndex(currentIndex);
            int colIndex = getColIndexFromTraversable(currentIndex);
            int startX = getStartX() - colIndex * horShiftOffset;
            int startY = getStartY();
            //Log.d("XMBView", "Current col index " + colIndex + " x: " + startX + " y: " + startY);
            //Log.d("XMBView", "Drawing views starting from " + getStartX());
            drawCategories(startX, startY, horShiftOffset);
            drawItems(currentIndex, startX, startY, horShiftOffset, verShiftOffset);
        }
    }
    private void drawCategories(int startXInt, int startYInt, int horShiftOffsetInt) {
        int viewWidth = getWidth();
        int viewHeight = getHeight();
        for (int i = 0; i < getTotalColCount(); i++) {
            XMBItem cat = getCat(i);
            calcCatRect(startXInt, startYInt, horShiftOffsetInt, i, reusableRect);
            cat.setX(reusableRect.left);
            cat.setY(reusableRect.top);
            //Log.d("XMBView", "Setting category " + i + " title: " + cat.title);
            boolean inBounds = inView(reusableRect, viewWidth, viewHeight);
            if (inBounds) {
                XMBItemView catView = getItemView(cat);
                if (catView != null) {
                    //Log.d("XMBView", "Retrieved view for " + cat.title + " whose title was set to " + catView.title);
                    catView.isCategory = true;
                    catView.setX(cat.getPrevX());
                    catView.setY(cat.getPrevY());
                    catView.animate().setDuration(300);
                    catView.animate().xBy(cat.getX() - cat.getPrevX());
                    catView.animate().yBy(cat.getY() - cat.getPrevY());
                } else
                    Log.w("XMBView", "Missing item view for category @" + i + ", skipped for now");
            } else
                returnItemView(getTotalIndexOfCol(i));
        }
//        // Prepare categories
//        reusableIndices.clear();
//        for (int i = 0; i < categories.size(); i++) {
//            calcCatRect(startXInt, startYInt, horShiftOffsetInt, i, reusableRect);
//            XMBCat cat = categories.get(i);
//            cat.setX(reusableRect.left);
//            cat.setY(reusableRect.top);
//            boolean inBounds = inView(reusableRect, viewWidth, viewHeight);
//            if (!inBounds)
//                returnCatView(i);
//            else
//                reusableIndices.add(i);
//        }
//        // Prepare items which show up as categories
//        int itemsIndexStart = reusableIndices.size();
//        int startIndex = getItemCatsStartIndex();
//        for (int i = startIndex; i < items.size(); i++) {
//            calcItemCatRect(startXInt, startYInt, horShiftOffsetInt, i, reusableRect);
//            XMBItem itemCat = items.get(i);
//            itemCat.setX(reusableRect.left);
//            itemCat.setY(reusableRect.top);
//            boolean inBounds = inView(reusableRect, viewWidth, viewHeight);
//            if (!inBounds)
//                returnCatView(toCatIndex(i));
//            else
//                reusableIndices.add(i);
//        }
//
//        // Animate and place actual categories
//        for (int i = 0; i < itemsIndexStart; i++) {
//            int catIndex = reusableIndices.get(i);
//            XMBCat cat = categories.get(catIndex);
//            XMBCategoryView catView = getCatView(cat);
//            if (catView != null) {
//                catView.setX(cat.getPrevX());
//                catView.setY(cat.getPrevY());
//                catView.animate().setDuration(300);
//                catView.animate().xBy(cat.getX() - cat.getPrevX());
//                catView.animate().yBy(cat.getY() - cat.getPrevY());
//            } else
//                Log.w("XMBView", "Missing catView, waiting to receive one");
//        }
//        // Animate and place items shown as categories
//        for (int i = itemsIndexStart; i < reusableIndices.size(); i++) {
//            int itemCatIndex = reusableIndices.get(i);
//            XMBItem itemCat = items.get(itemCatIndex);
//            XMBCategoryView itemCatView = getCatView(itemCat);
//            if (itemCatView != null) {
//                itemCatView.setX(itemCat.getPrevX());
//                itemCatView.setY(itemCat.getPrevY());
//                itemCatView.animate().setDuration(300);
//                itemCatView.animate().xBy(itemCat.getX() - itemCat.getPrevX());
//                itemCatView.animate().yBy(itemCat.getY() - itemCat.getPrevY());
//            } else
//                Log.w("XMBView", "Missing itemCatView, waiting to receive one");
//        }
    }
    private void drawItems(int itemIndex, int startXInt, int startYInt, int horShiftOffsetInt, int verShiftOffsetInt) {
        //XMBCat origCat = items.get(itemIndex).category;
        int origColIndex = getColIndexFromTraversable(itemIndex);
        int viewWidth = getWidth();
        int viewHeight = getHeight();
        int traversableCount = getTraversableCount();
        for (int i = 0; i < traversableCount; i++) {
            XMBItem item = getTraversableItem(i);
            int itemColIndex = getColIndexFromTraversable(i);
            if (!columnHasSubItems(itemColIndex))
                continue;
            calcItemRect(startXInt, startYInt, horShiftOffsetInt, verShiftOffsetInt, i, reusableRect);
            item.setX(reusableRect.left);
            item.setY(reusableRect.top);
            //Log.d("XMBView", "Setting item " + i + " title: " + item.title);
            boolean inBounds = inView(reusableRect, viewWidth, viewHeight);
            //TODO: include immediate columns to allow for alpha transition
            if (itemColIndex == origColIndex && inBounds) {
                XMBItemView itemView = getItemView(item);
                if (itemView != null) {
                    itemView.isCategory = false;
                    itemView.setX(item.getPrevX());
                    itemView.setY(item.getPrevY());
                    itemView.animate().setDuration(300);
                    itemView.animate().xBy(item.getX() - item.getPrevX());
                    itemView.animate().yBy(item.getY() - item.getPrevY());
                } else
                    Log.w("XMBView", "Missing item view for item @" + i + ", skipped for now");
            } else
                returnItemView(traversableToTotalIndex(i));
        }

//        reusableIndices.clear();
//        for (int i = 0; i < items.size(); i++) {
//            XMBItem item = items.get(i);
//            XMBCat currentCat = item.category;
//            if (currentCat == null) //This means it should be drawn with the categories, not here
//                continue;
//            calcItemRect(startXInt, startYInt, horShiftOffsetInt, verShiftOffsetInt, i, reusableRect);
//            item.setX(reusableRect.left);
//            item.setY(reusableRect.top);
//            boolean inBounds = inView(reusableRect, viewWidth, viewHeight);
//            if (item.category != origCat || !inBounds)
//                returnItemView(i);
//            else
//                reusableIndices.add(i);
//        }
//        for (int i = 0; i < reusableIndices.size(); i++) {
//            int index = reusableIndices.get(i);
//            XMBItem item = items.get(index);
//            XMBItemView itemView = getItemView(item);
//            if (itemView != null) {
//                itemView.setX(item.getPrevX());
//                itemView.setY(item.getPrevY());
//                itemView.animate().setDuration(300);
//                itemView.animate().xBy(item.getX() - item.getPrevX());
//                itemView.animate().yBy(item.getY() - item.getPrevY());
//            } else
//                Log.w("XMBView", "Missing itemView, waiting to receive one");
//        }
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
//    private void calcItemCatRect(int startX, int startY, int horShiftOffset, int itemIndex, Rect rect) {
//        //Gets the bounds of the item category view
//        XMBItem itemCat = items.get(itemIndex);
//        getTextBounds(painter, itemCat.title, textSize, rect);
//        int expX = startX + horShiftOffset * toCatIndex(itemIndex);
//        int expY = startY;
//        int right = expX + Math.max(iconSize, rect.width());
//        int bottom = expY + iconSize + textCushion + rect.height();
//        rect.set(expX, expY, right, bottom);
//    }
    private int traversableToTotalIndex(int traversableIndex) {
        int colIndex = getColIndexFromTraversable(traversableIndex);
        int index = traversableIndex;
        for (int i = 0; i < colIndex; i++) {
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
                if (currentPos < cat.size()) {
                    index = currentPos;
                    break;
                } else if (cat.size() > 1)
                    currentPos -= (cat.size() - 1);
                else
                    currentPos -= cat.size();
            }
            //XMBCat currentCat = items.get(currentIndex).category;
            //index = currentCat != null ? categories.indexOf(currentCat) : toCatIndex(currentIndex); //The index of how far we are along the columns row
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
            //XMBCat currentCat = items.get(currentIndex).category;
            //index = currentCat != null ? categories.indexOf(currentCat) : toCatIndex(currentIndex); //The index of how far we are along the columns row
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
        //XMBCat currentCat = item.category;
        //int currentCatIndex = categories.indexOf(currentCat);
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
//    private int getItemCatIndex(XMBItem item) {
//        //Gets the item's index relative to the categories (items are categories when they do not have a category themselves)
//        return toCatIndex(items.indexOf(item));
//    }
//    private int toCatIndex(int itemIndex) {
//        //Converts an item category's index to be relative to the categories
//        return categories.size() + (itemIndex - getItemCatsStartIndex());
//    }
//    private int getItemCatsStartIndex() {
//        //Gets the first item without a category (item categories are grouped together at the end of the items list)
//        int startIndex = items.size();
//        for (int i = startIndex - 1; i >= 0; i--) {
//            if (items.get(i).category != null) {
//                startIndex = i + 1;
//                break;
//            } else
//                startIndex = i;
//        }
//        return startIndex;
//    }
    private int getTotalColCount() {
        return items.size();
        //return categories.size() + (items.size() - getItemCatsStartIndex());
    }
    private int getColCount(int colIndex) {
        if (columnHasSubItems(colIndex))
            return items.get(colIndex).size() - 1;
        else
            return items.get(colIndex).size();
    }
    private boolean columnHasSubItems(int colIndex) {
        boolean isColumn = false;
        ArrayList<XMBItem> column = items.get(colIndex);
        if (column != null)
            isColumn = column.size() > 1;
        else
            Log.e("XMBView", "Column @" + colIndex + " is null");
        return isColumn;
    }
//    private int getCatStartIndex(XMBCat cat) {
//        //Gets the first item's index with the specified category (items with the same category are grouped together in the list)
//        for (int i = 0; i < items.size(); i++)
//            if (items.get(i).category == cat)
//                return i;
//        return -1;
//    }
//    private XMBItem getCatOfItem(int traversableIndex) {
//        return getCat(getColIndexFromTraversable(traversableIndex));
//        //return items.get(traversableIndex).category;
//    }
    public void setIndex(int index) {
        //currentIndex = index;
        if (items.size() > 0) {
            int traversableSize = getTraversableCount();
            if (index < 0)
                index = 0;
            if (index >= traversableSize)
                index = traversableSize - 1;
            currentIndex = index;
            int colIndex = getColIndexFromTraversable(currentIndex);
            //If the column has multiple items, set the index cache of the column as well
            if (columnHasSubItems(colIndex)) {
                int localIndex = traversableToLocalIndex(currentIndex);
                catIndices.set(colIndex, localIndex);
            }
//            XMBCat currentCat = getCatOfItem(currentIndex);
//            if (currentCat != null)
//                catIndices.set(categories.indexOf(currentCat), currentIndex - getCatStartIndex(currentCat)); //Cache current position in category
        }

        //setViews();
        //animateXMB();
        //invalidate();
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
                    if (currentPos < cat.size()) {
                        item = cat.get(currentPos);
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
            //XMBCat currentCat = items.get(currentIndex).category;
            //index = currentCat != null ? categories.indexOf(currentCat) : toCatIndex(currentIndex); //The index of how far we are along the columns row
        }
        return item;
    }
//    public XMBItem getTraversableItem(int traversableIndex) {
//        int index = traversableIndex;
//        for (int i = 0; i < items.size(); i++) {
//
//        }
//        return items.get(getIndex());
//    }
//    private int getCachedIndexOfCat(XMBCat cat) {
//        //Retrieves the position the user was on within the category
//        return getCatStartIndex(cat) + catIndices.get(categories.indexOf(cat));
//    }
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
            int localIndex = items.get(i).indexOf(item);
            if (localIndex >= 0) {
                found = true;
                index += localIndex;
                break;
            } else
                index += items.size();
        }
        if (!found)
            index = -1;
        return index;
    }
    private int getCachedIndexOfCat(int colIndex) {
        return catIndices.get(colIndex);
    }
    public void addSubItem(XMBItem item, int colIndex) {
        addSubItem(item, colIndex, true);
    }
    public void addSubItem(XMBItem item, XMBItem cat) {
        addSubItem(item, getColIndex(cat));
    }
    private void addSubItem(XMBItem item, int colIndex, boolean refresh) {
        items.get(colIndex).add(item);
        if (refresh)
            setViews();
    }
    public void addCatItems(XMBItem[] items) {
        for (int i = 0; i < items.length; i++)
            addCatItem(items[i], false);
        setViews();
    }
    public void addCatItems(List<XMBItem> items) {
        for (int i = 0; i < items.size(); i++)
            addCatItem(items.get(i), false);
        setViews();
    }
    public void addCatItem(XMBItem item) {
        addCatItem(item, true);
    }
    private void addCatItem(XMBItem item, boolean refresh) {
        ArrayList<XMBItem> cat = new ArrayList<>();
        cat.add(item);
        catIndices.add(0);
        items.add(cat);
        if (refresh)
            setViews();
    }
//    private int addItem(XMBItem item) {
//        boolean hasCat = item.category != null;
//        int itemIndex = items.size();
//        if (hasCat) {
//            boolean catDidntExist = !categories.contains(item.category);
//            if (catDidntExist) {
//                categories.add(item.category);
//                catIndices.add(0);
//            }
//            if (items.size() > 0) {
//                for (int i = items.size() - 1; i >= 0; i--) { //going backwards to add at the end of the category list
//                    if ((catDidntExist && items.get(i).category != null) || (!catDidntExist && items.get(i).category == item.category)) { //if category didn't exist then find first item that has some category and append after or if category did exist then find last item in that category then append after
//                        if (i < items.size() - 1) { //if we're not at the end of all items then we can insert in the next spot
//                            itemIndex = i + 1;
//                            items.add(itemIndex, item);
//                        } else { //or else just add at the end
//                            items.add(item);
//                        }
//                        break;
//                    } else if (i == 0) {
//                        //In case if there aren't any items that have categories, then add the item before everything else
//                        items.add(i, item);
//                    }
//                }
//            } else {
//                items.add(item);
//            }
//        } else {
//            items.add(item);
//        }
//        setViews();
//        return itemIndex;
//    }
//    public void addItems(XMBItem[] items) {
//        for (int i = 0; i < items.length; i++)
//            addItem(items[i]);//, i == items.length - 1);
//    }
//    public void addItems(List items) {
//        for (int i = 0; i < items.size(); i++)
//            addItem((XMBItem)items.get(i));//, i == items.size() - 1);
//    }
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
//    public void removeItem(XMBItem item) {
//        int itemIndex = items.indexOf(item);
//        boolean removeCat = item.category != null && (itemIndex + 1 < items.size() && items.get(itemIndex + 1).category == item.category) || (itemIndex - 1 >= 0 && items.get(itemIndex - 1).category == item.category);
//        int adjustedIndex = currentIndex;
//        if (removeCat) {
//            int catIndex = categories.indexOf(item.category);
//            //removeViewForCategory(catIndex);
//            catIndices.remove(catIndex);
//            categories.remove(item.category);
//            if (adjustedIndex >= items.size())
//                adjustedIndex = items.size() - 1;
//            adjustedIndex = getCachedIndexOfCat(getCatOfItem(adjustedIndex));
//        } else if (item.category != null) {
//            int catIndex = categories.indexOf(item.category);
//            int catStartIndex = getCatStartIndex(item.category);
//            int relIndex = catIndices.get(catIndex) - 1;
//            if (relIndex < 0)
//                relIndex = 0;
//            catIndices.set(catIndex, relIndex); //readjust cached index
//            adjustedIndex = catStartIndex + relIndex;
//        }
//
//        //removeViewForItem(itemIndex);
//        items.remove(item);
//        setIndex(adjustedIndex);
//        setViews();
//    }
//    public void removeItem(int index) {
//        removeItem(items.get(index));
//    }
    public void clear() {
        //categories.clear();
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
//        XMBCat currentCat = items.get(currentIndex).category;
//        if (currentCat != null && currentIndex + 1 < items.size() && items.get(currentIndex + 1).category == currentCat) {
//            setIndex(currentIndex + 1);
//            setViews();
//        }
    }
    public void selectUpperItem() {
        int colIndex = getColIndexFromTraversable(currentIndex);
        if (columnHasSubItems(colIndex)) {
            //int colSize = getColCount(colIndex);
            int localIndex = traversableToLocalIndex(currentIndex);
            if (localIndex - 1 >= 0) {
                setIndex(currentIndex - 1);
                setViews();
            }
        }
//        XMBCat currentCat = items.get(currentIndex).category;
//        if (currentCat != null && currentIndex - 1 >= 0 && items.get(currentIndex - 1).category == currentCat) {
//            setIndex(currentIndex - 1);
//            setViews();
//        }
    }
    public void selectRightItem() {
        int colIndex = getColIndexFromTraversable(currentIndex);
        if (colIndex + 1 < getTotalColCount()) {
            int localIndex = getCachedIndexOfCat(colIndex + 1);
            int nextIndex = localToTraversableIndex(localIndex, colIndex + 1);
            setIndex(nextIndex);
            setViews();
        }
//        int adjustedIndex = -1;
//        XMBCat currentCat = items.get(currentIndex).category;
//        if (currentCat != null) {
//            int nextIndex = currentIndex + 1;
//            while (nextIndex < items.size()) {
//                XMBCat nextCat = items.get(nextIndex).category;
//                if (nextCat != currentCat) {
//                    if (nextCat != null)
//                        adjustedIndex = getCachedIndexOfCat(nextCat);
//                    else
//                        adjustedIndex = nextIndex;
//                    break;
//                }
//                nextIndex++;
//            }
//        }
//        else if (currentIndex + 1 < items.size()) {
//            adjustedIndex = currentIndex + 1;
//            XMBCat nextCat = items.get(adjustedIndex).category;
//            if (nextCat != null)
//                adjustedIndex = getCachedIndexOfCat(nextCat);
//        }
//
//        if (adjustedIndex >= 0) {
//            setIndex(adjustedIndex);
//            setViews();
//        }
    }
    public void selectLeftItem() {
        int colIndex = getColIndexFromTraversable(currentIndex);
        if (colIndex - 1 >= 0) {
            int localIndex = getCachedIndexOfCat(colIndex - 1);
            int nextIndex = localToTraversableIndex(localIndex, colIndex - 1);
            setIndex(nextIndex);
            setViews();
        }
//        int adjustedIndex = -1;
//        XMBCat currentCat = items.get(currentIndex).category;
//        if (currentCat != null) {
//            int nextIndex = currentIndex - 1;
//            while (nextIndex >= 0) {
//                XMBCat prevCat = items.get(nextIndex).category;
//                if (prevCat != currentCat) {
//                    if (prevCat != null)
//                        adjustedIndex = getCachedIndexOfCat(prevCat);
//                    else
//                        adjustedIndex = nextIndex;
//                    break;
//                }
//                nextIndex--;
//            }
//        }
//        else if (currentIndex - 1 >= 0) {
//            adjustedIndex = currentIndex - 1;
//            XMBCat prevCat = items.get(adjustedIndex).category;
//            if (prevCat != null)
//                adjustedIndex = getCachedIndexOfCat(prevCat);
//        }
//
//        if (adjustedIndex >= 0) {
//            setIndex(adjustedIndex);
//            setViews();
//        }
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
