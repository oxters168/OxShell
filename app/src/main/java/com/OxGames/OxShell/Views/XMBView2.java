package com.OxGames.OxShell.Views;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.OxGames.OxShell.Data.XMBCat;
import com.OxGames.OxShell.Data.XMBItem;
import com.OxGames.OxShell.Interfaces.InputReceiver;
import com.OxGames.OxShell.R;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Queue;
import java.util.Stack;

public class XMBView2 extends ViewGroup implements InputReceiver {
    private Context context;

    private Stack<XMBCategoryView> goneCatViews;
    private HashMap<Integer, XMBCategoryView> usedCatViews;
    private Stack<XMBItemView> goneItemViews;
    private HashMap<Integer, XMBItemView> usedItemViews;

    private ArrayList<XMBCat> categories;
    private ArrayList<Integer> catIndices;
    private ArrayList<XMBItem> items;

    private int iconSize = 196;
    private float textSize = 48; //Size of the text
    private int textCushion = 16; //Distance between item and text
    private float horSpacing = 64; //How much space to add between items horizontally
    private float verSpacing = 0; //How much space to add between items vertically
    private float catShift = (iconSize + horSpacing) * 2; //How much to shift the categories bar horizontally
    private int horShiftOffset = Math.round(iconSize + horSpacing); //How far apart each item is from center to center
    private int verShiftOffset = Math.round(iconSize + verSpacing); //How far apart each item is from center to center

    int currentIndex = 0;

    private final Rect reusableRect = new Rect();
    private final ArrayList<Integer> reusableIndices = new ArrayList<>();
    private final Paint painter = new Paint();

    //XMBItemView testView;
    public XMBView2(Context context) {
        this(context, null);
    }
    public XMBView2(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    public XMBView2(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;

        goneCatViews = new Stack<>();
        usedCatViews = new HashMap<>();
        goneItemViews = new Stack<>();
        usedItemViews = new HashMap<>();
        categories = new ArrayList<>();
        catIndices = new ArrayList<>();
        items = new ArrayList<>();

//        LayoutInflater layoutInflater = LayoutInflater.from(context);
//        testView = (XMBItemView) layoutInflater.inflate(R.layout.xmb_item, null);
//        testView.title = "Test";
//        testView.icon = ContextCompat.getDrawable(ActivityManager.getCurrentActivity(), R.drawable.ic_baseline_source_24);
//        //testView.setVisibility(GONE);
//        addView(testView);

//        XMBCat cat1 = new XMBCat("Cat1");
//        XMBCat cat2 = new XMBCat("Cat2");
//        ArrayList<XMBItem> list = new ArrayList<>();
//        list.add(new XMBItem(null, "Item1", cat1));
//        list.add(new XMBItem(null, "Item2", cat1));
//        list.add(new XMBItem(null, "Item3", cat1));
//        list.add(new XMBItem(null, "Item1", cat2));
//        list.add(new XMBItem(null, "Item2", cat2));
//        list.add(new XMBItem(null, "Item3", cat2));
//        list.add(new XMBItem(null, "Item4", cat2));
//        list.add(new XMBItem(null, "Item1"));
//        list.add(new XMBItem(null, "Item2"));
//        addItems(list);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        refresh();
    }

    private int getStartX() {
        int padding = getPaddingLeft();
        return Math.round(padding + iconSize / 2f + catShift); //Where the current item's column is along the x-axis
    }
    private int getStartY() {
        int vsy = getHeight(); //view size y
        float vey = vsy / 2f; //view extents y
        return Math.round(vey - iconSize / 2f); //Where the current item's column is along the y-axis
    }
    private int getHorIndex() {
        int index = -1;
        if (items.size() > 0) {
            XMBCat currentCat = items.get(currentIndex).category;
            index = currentCat != null ? categories.indexOf(currentCat) : toCatIndex(currentIndex); //The index of how far we are along the columns row
        }
        return index;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        Log.d("XMBView2", "onLayout called");

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
            } else if (view instanceof XMBCategoryView) {
                XMBCategoryView catView = (XMBCategoryView)view;
                int expX = 0;
                int expY = 0;
                int right = expX + catView.getFullWidth();
                int bottom = expY + catView.getFullHeight();
                view.layout(expX, expY, right, bottom);
            }
        }
    }
    private void removeViews() {
        returnAllCatViews();
        returnAllItemViews();
        while (!goneCatViews.isEmpty())
            removeView(goneCatViews.pop());
        while (!goneItemViews.isEmpty())
            removeView(goneItemViews.pop());
    }
    private void createViews() {
        Log.d("XMBView2", getWidth() + ", " + getHeight());
        int colCount = (int)Math.ceil(getWidth() / (float)horShiftOffset) + 1; //+1 for off screen animating into on screen
        int rowCount = ((int)Math.ceil(getHeight() / (float)verShiftOffset) + 1) * 3; //+1 for off screen to on screen animating, *3 for column to column fade
        for (int i = 0; i < colCount; i++) {
            Log.d("XMBView2", "Creating cat view");
            XMBCategoryView view;
            LayoutInflater layoutInflater = LayoutInflater.from(context);
            view = (XMBCategoryView) layoutInflater.inflate(R.layout.xmb_category, null);
            view.setVisibility(GONE);
            addView(view);
            goneCatViews.push(view);
        }
        for (int i = 0; i < rowCount; i++) {
            Log.d("XMBView2", "Creating item view");
            XMBItemView view;
            LayoutInflater layoutInflater = LayoutInflater.from(context);
            view = (XMBItemView) layoutInflater.inflate(R.layout.xmb_item, null);
            view.setVisibility(GONE);
            addView(view);
            goneItemViews.push(view);
        }
    }
    private void returnCatView(int catIndex) {
        if (usedCatViews.containsKey(catIndex)) {
            XMBCategoryView catView = usedCatViews.get(catIndex);
            catView.setVisibility(GONE);
            goneCatViews.push(catView);
            usedCatViews.remove(catIndex);
        }
    }
    private void returnItemView(int itemIndex) {
        if (usedItemViews.containsKey(itemIndex)) {
            XMBItemView itemView = usedItemViews.get(itemIndex);
            itemView.setVisibility(GONE);
            goneItemViews.push(itemView);
            usedItemViews.remove(itemIndex);
        }
    }
    private void returnAllCatViews() {
        for (XMBCategoryView catView : usedCatViews.values())
            catView.setVisibility(GONE);
        //for (int i = usedCatViews.size() - 1; i >= 0; i--)
        //    usedCatViews.get(i).setVisibility(GONE);
        goneCatViews.addAll(usedCatViews.values());
        usedCatViews.clear();
    }
    private void returnAllItemViews() {
        for (XMBItemView itemView : usedItemViews.values())
            itemView.setVisibility(GONE);
        //for (int i = usedItemViews.size() - 1; i >= 0; i--)
        //    usedItemViews.get(i).setVisibility(GONE);
        goneItemViews.addAll(usedItemViews.values());
        usedItemViews.clear();
    }
    private XMBCategoryView getCatView(XMBCat cat) {
        XMBCategoryView view = null;
        int index = categories.indexOf(cat);
        if (usedCatViews.containsKey(index)) {
            Log.d("XMBView2", "Requested cat on " + index);
            view = usedCatViews.get(index);
        } else if (!goneCatViews.isEmpty()) {
            Log.d("XMBView2", "Requested nonexistant cat");
            view = goneCatViews.pop();
            view.title = cat.title;
            view.icon = cat.icon;
            view.setVisibility(VISIBLE);
            usedCatViews.put(index, view);
        }

        return view;
    }
    private XMBCategoryView getCatView(XMBItem item) {
        XMBCategoryView view = null;
        int index = getItemCatIndex(item);
        if (usedCatViews.containsKey(index)) {
            //Log.d("XMBView2", "Requested itemcat on " + index);
            view = usedCatViews.get(index);
        } else if (!goneCatViews.isEmpty()) {
            //Log.d("XMBView2", "Requested nonexistant itemcat");
            view = goneCatViews.pop();
            view.title = item.title;
            view.icon = item.getIcon();
            view.setVisibility(VISIBLE);
            usedCatViews.put(index, view);
        }

        return view;
    }
    private XMBItemView getItemView(XMBItem item) {
        XMBItemView view = null;
        int index = items.indexOf(item);
        if (usedItemViews.containsKey(index)) {
            //Log.d("XMBView2", "Requested item on " + index);
            view = usedItemViews.get(index);
        } else if (!goneItemViews.isEmpty()) {
            //Log.d("XMBView2", "Requested nonexistant item");
            view = goneItemViews.pop();
            view.title = item.title;
            view.icon = item.getIcon();
            view.setVisibility(VISIBLE);
            usedItemViews.put(index, view);
        }
        return view;
    }
    private void setViews() {
        Log.d("XMBView2", "Setting view positions");
        //returnAllCatViews();
        //returnAllItemViews();
        if (items.size() > 0) {
            int startX = getStartX() - getHorIndex() * horShiftOffset;
            int startY = getStartY();
            drawCategories(startX, startY, horShiftOffset);
            drawItems(currentIndex, startX, startY, horShiftOffset, verShiftOffset);
        }
    }
    private void drawCategories(int startXInt, int startYInt, int horShiftOffsetInt) {
        int viewWidth = getWidth();
        int viewHeight = getHeight();
        reusableIndices.clear();
        for (int i = 0; i < categories.size(); i++) {
            getCatRect(startXInt, startYInt, horShiftOffsetInt, i, reusableRect);
            boolean inBounds = inView(reusableRect, viewWidth, viewHeight);
            if (!inBounds)
                returnCatView(i);
            else
                reusableIndices.add(i);
        }
        int itemsIndexStart = reusableIndices.size();
        int startIndex = getItemCatsStartIndex();
        for (int i = startIndex; i < items.size(); i++) {
            getItemCatRect(startXInt, startYInt, horShiftOffsetInt, i, reusableRect);
            boolean inBounds = inView(reusableRect, viewWidth, viewHeight);
            if (!inBounds)
                returnCatView(toCatIndex(i));
            else
                reusableIndices.add(i);
        }

        for (int i = 0; i < itemsIndexStart; i++) {
            int catIndex = reusableIndices.get(i);
            getCatRect(startXInt, startYInt, horShiftOffsetInt, catIndex, reusableRect);
            XMBCategoryView catView = getCatView(categories.get(catIndex));
            if (catView != null) {
                catView.setX(lerp(catView.getX(), reusableRect.left, xmbTrans));
                catView.setY(lerp(catView.getY(), reusableRect.top, xmbTrans));
            }
        }
        for (int i = itemsIndexStart; i < reusableIndices.size(); i++) {
            int itemCatIndex = reusableIndices.get(i);
            getItemCatRect(startXInt, startYInt, horShiftOffsetInt, itemCatIndex, reusableRect);
            XMBCategoryView itemCatView = getCatView(items.get(itemCatIndex));
            if (itemCatView != null) {
                itemCatView.setX(lerp(itemCatView.getX(), reusableRect.left, xmbTrans));
                itemCatView.setY(lerp(itemCatView.getY(), reusableRect.top, xmbTrans));
            }
        }
    }
    private void drawItems(int itemIndex, int startXInt, int startYInt, int horShiftOffsetInt, int verShiftOffsetInt) {
        XMBCat origCat = items.get(itemIndex).category;
        int viewWidth = getWidth();
        int viewHeight = getHeight();

        reusableIndices.clear();
        for (int i = 0; i < items.size(); i++) {
            XMBItem item = items.get(i);
            XMBCat currentCat = item.category;
            if (currentCat == null) //This means it should be drawn with the categories, not here
                continue;
            getItemRect(startXInt, startYInt, horShiftOffsetInt, verShiftOffsetInt, i, reusableRect);
            boolean inBounds = inView(reusableRect, viewWidth, viewHeight);
            if (item.category != origCat || !inBounds)
                returnItemView(i);
            else
                reusableIndices.add(i);
        }
        for (int i = 0; i < reusableIndices.size(); i++) {
            int index = reusableIndices.get(i);
            getItemRect(startXInt, startYInt, horShiftOffsetInt, verShiftOffsetInt, index, reusableRect);
            XMBItemView itemView = getItemView(items.get(index));
            if (itemView != null) {
                //TODO: change the from value to be able to translate column
                // (possible solution would be to get their column's x value and use that)
                // (another would be to draw the other columns invisibly)
                itemView.setX(lerp(itemView.getX(), reusableRect.left, xmbTrans));
                itemView.setY(lerp(itemView.getY(), reusableRect.top, xmbTrans));
            }
        }
    }

    private static float lerp(float from, float to, float t) {
        return ((to - from) * t) + from;
    }

    private void getCatRect(int startX, int startY, int horShiftOffset, int catIndex, Rect rect) {
        //Gets the bounds of the category view
        XMBCat cat = categories.get(catIndex);
        getTextBounds(painter, cat.title, textSize, rect);
        int expX = startX + horShiftOffset * catIndex;
        int expY = startY;
        int right = expX + Math.max(iconSize, rect.width());
        int bottom = expY + iconSize + textCushion + rect.height();
        rect.set(expX, expY, right, bottom);
    }
    private void getItemCatRect(int startX, int startY, int horShiftOffset, int catIndex, Rect rect) {
        //Gets the bounds of the item category view
        XMBItem itemCat = items.get(catIndex);
        getTextBounds(painter, itemCat.title, textSize, rect);
        int expX = startX + horShiftOffset * getItemCatIndex(itemCat);
        int expY = startY;
        int right = expX + Math.max(iconSize, rect.width());
        int bottom = expY + iconSize + textCushion + rect.height();
        rect.set(expX, expY, right, bottom);
    }
    private void getItemRect(int startX, int startY, int horShiftOffset, int verShiftOffset, int itemIndex, Rect rect) {
        XMBItem item = items.get(itemIndex);
        XMBCat currentCat = item.category;
        int currentCatIndex = categories.indexOf(currentCat);
        int itemCatIndex = getCachedIndexOfCat(currentCat);
        int expX = startX + horShiftOffset * currentCatIndex;
        getTextBounds(painter, item.title, textSize, rect);
        int expY = (startY + rect.height()) + verShiftOffset * ((itemIndex - itemCatIndex) + 1);
        if (itemIndex < itemCatIndex)
            expY = startY - verShiftOffset * (((itemCatIndex - 1) - itemIndex) + 1);
        int right = expX + iconSize + textCushion + rect.width();
        int bottom = expY + iconSize;
        rect.set(expX, expY, right, bottom);
    }
    private static boolean inView(Rect rect, int viewWidth, int viewHeight) {
        return ((rect.left < viewWidth && rect.left > 0) || (rect.right > 0 && rect.right < viewWidth)) && ((rect.top < viewHeight && rect.top > 0) || (rect.bottom > 0 && rect.bottom < viewHeight));
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

    private float xmbTrans = 1;
    ValueAnimator xmbimator = null;
    private void animateXMB() {
        if (xmbimator != null)
            xmbimator.cancel();

        //catTransX = 0;
        xmbimator = ValueAnimator.ofFloat(0, 1);
        xmbimator.addUpdateListener(valueAnimator -> {
            xmbTrans = ((Float)valueAnimator.getAnimatedValue());
            setViews();
//            for (int i = 0; i < getChildCount(); i++) {
//                XMBItemView child = (XMBItemView)getChildAt(i);
//                float nextY = (i + currentIndex) * child.getItemHeight();
//                float transY = (nextY - child.getY()) * xmbTrans + child.getY();
//                child.setY(transY);
//            }
            //Log.d("XMBView2", "Animation value " + xmbTrans);
            //Log.d("XMBView", "Animating " + catTransX);
            //XMBView.this.invalidate();
        });
        //Log.d("XMBView", "Starting animation");
        //setAnimation(xmbimator);
        xmbimator.setDuration(300);
        xmbimator.start();
        //invalidate();
    }

    private int getItemCatIndex(XMBItem item) {
        //Gets the item's index relative to the categories (items are categories when they do not have a category themselves)
        return toCatIndex(items.indexOf(item));
    }
    private int toCatIndex(int itemIndex) {
        //Converts an item category's index to be relative to the categories
        return categories.size() + (itemIndex - getItemCatsStartIndex());
    }
    private int getItemCatsStartIndex() {
        //Gets the first item without a category (item categories are grouped together at the end of the items list)
        int startIndex = items.size();
        for (int i = startIndex - 1; i >= 0; i--) {
            if (items.get(i).category != null) {
                startIndex = i + 1;
                break;
            } else
                startIndex = i;
        }
        return startIndex;
    }
    private int getCatStartIndex(XMBCat cat) {
        //Gets the first item's index with the specified category (items with the same category are grouped together in the list)
        for (int i = 0; i < items.size(); i++)
            if (items.get(i).category == cat)
                return i;
        return -1;
    }
    private int getCatSize(XMBCat cat) {
        //Gets the number of items that are a part of the given category
        int size = 0;
        for (int i = 0; i < items.size(); i++)
            if (items.get(i).category == cat)
                size++;
        return size;
    }
    private XMBCat getItemCat(int index) {
        return items.get(index).category;
    }
    public void setIndex(int index) {
        currentIndex = index;
        if (items.size() > 0) {
            XMBCat currentCat = getItemCat(currentIndex);
            if (currentCat != null)
                catIndices.set(categories.indexOf(currentCat), currentIndex - getCatStartIndex(currentCat)); //Cache current position in category
        }

        //setViews();
        animateXMB();
        //invalidate();
    }
    public int getIndex() {
        return currentIndex;
    }
    public XMBItem getSelectedItem() {
        return items.get(getIndex());
    }
    private int getCachedIndexOfCat(XMBCat cat) {
        //Retrieves the position the user was on within the category
        return getCatStartIndex(cat) + catIndices.get(categories.indexOf(cat));
    }
    private int addItem(XMBItem item) {//, boolean invalidate) {
        boolean hasCat = item.category != null;
        int itemIndex = items.size();
        if (hasCat) {
            boolean catDidntExist = !categories.contains(item.category);
            if (catDidntExist) {
                //Log.d("XMBView", item.category.title + " category does not exist so appending to end");
                //addViewForCategory(item.category, categories.size());
                categories.add(item.category);
                catIndices.add(0);
            }
            if (items.size() > 0) {
                for (int i = items.size() - 1; i >= 0; i--) { //going backwards to add at the end of the category list
                    if ((catDidntExist && items.get(i).category != null) || (!catDidntExist && items.get(i).category == item.category)) { //if category didn't exist then find first item that has some category and append after or if category did exist then find last item in that category then append after
                        if (i < items.size() - 1) { //if we're not at the end of all items then we can insert in the next spot
                            itemIndex = i + 1;
                            //Log.d("XMBView", "Adding item " + item.title + " with category " + item.category.title + " at index " + itemIndex);
                            items.add(itemIndex, item);
                        } else {//or else just add at the end
                            //Log.d("XMBView", "Adding item " + item.title + " with category " + item.category.title + " at the end");
                            items.add(item);
                        }
                        break;
                    }
                }
            } else {
                //Log.d("XMBView", "No items exist so appending item " + item.title + " with category " + item.category.title + " to the end");
                items.add(item);
            }
        } else {
            //Log.d("XMBView", item.title + " has no category so appending to end");
            items.add(item);
        }
        //if (invalidate)
        //    invalidate();
        //addViewForItem(item, itemIndex);
        return itemIndex;
    }
//    public int addItem(XMBItem item) {
//        return addItem(item, true);
//    }
    public void addItems(XMBItem[] items) {
        for (int i = 0; i < items.length; i++)
            addItem(items[i]);//, i == items.length - 1);
    }
    public void addItems(List items) {
        for (int i = 0; i < items.size(); i++)
            addItem((XMBItem)items.get(i));//, i == items.size() - 1);
    }
    public void removeItem(XMBItem item) {
        int itemIndex = items.indexOf(item);
        boolean removeCat = item.category != null && (itemIndex + 1 < items.size() && items.get(itemIndex + 1).category == item.category) || (itemIndex - 1 >= 0 && items.get(itemIndex - 1).category == item.category);
        int adjustedIndex = currentIndex;
        if (removeCat) {
            int catIndex = categories.indexOf(item.category);
            //removeViewForCategory(catIndex);
            catIndices.remove(catIndex);
            categories.remove(item.category);
            if (adjustedIndex >= items.size())
                adjustedIndex = items.size() - 1;
            adjustedIndex = getCachedIndexOfCat(getItemCat(adjustedIndex));
        } else if (item.category != null) {
            int catIndex = categories.indexOf(item.category);
            int catStartIndex = getCatStartIndex(item.category);
            int relIndex = catIndices.get(catIndex) - 1;
            if (relIndex < 0)
                relIndex = 0;
            catIndices.set(catIndex, relIndex); //readjust cached index
            adjustedIndex = catStartIndex + relIndex;
        }

        //removeViewForItem(itemIndex);
        items.remove(item);
        setIndex(adjustedIndex);
    }
    public void removeItem(int index) {
        removeItem(items.get(index));
    }
    public void clear() {
//        for (int i = items.size() - 1; i >= 0; i--)
//            removeViewForItem(i);
//        for (int i = categories.size() - 1; i >= 0; i--)
//            removeViewForCategory(i);
        categories.clear();
        catIndices.clear();
        items.clear();
        setIndex(0);
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
        if (key_event.getKeyCode() == KeyEvent.KEYCODE_BACK || key_event.getKeyCode() == KeyEvent.KEYCODE_BUTTON_B)
            return true;

        return false;
    }
    public void selectLowerItem() {
        //Log.d("XMBView", "Received move down signal");
        //testView.setVisibility(testView.getVisibility() == VISIBLE ? GONE : VISIBLE);
        //testView.setVisibility(GONE);
        XMBCat currentCat = items.get(currentIndex).category;
        if (currentCat != null && currentIndex + 1 < items.size() && items.get(currentIndex + 1).category == currentCat)
            setIndex(currentIndex + 1);
    }
    public void selectUpperItem() {
        //Log.d("XMBView", "Received move up signal");
        XMBCat currentCat = items.get(currentIndex).category;
        if (currentCat != null && currentIndex - 1 >= 0 && items.get(currentIndex - 1).category == currentCat)
            setIndex(currentIndex - 1);
    }
    public void selectRightItem() {
        //Log.d("XMBView", "Received move right signal");
        int adjustedIndex = -1;
        XMBCat currentCat = items.get(currentIndex).category;
        if (currentCat != null) {
            int nextIndex = currentIndex + 1;
            while (nextIndex < items.size()) {
                XMBCat nextCat = items.get(nextIndex).category;
                if (nextCat != currentCat) {
                    if (nextCat != null)
                        adjustedIndex = getCachedIndexOfCat(nextCat);
                    else
                        adjustedIndex = nextIndex;
                    break;
                }
                nextIndex++;
            }
        }
        else if (currentIndex + 1 < items.size()) {
            adjustedIndex = currentIndex + 1;
            XMBCat nextCat = items.get(adjustedIndex).category;
            if (nextCat != null)
                adjustedIndex = getCachedIndexOfCat(nextCat);
        }

        if (adjustedIndex >= 0) {
            setIndex(adjustedIndex);
        }
    }
    public void selectLeftItem() {
        //Log.d("XMBView", "Received move left signal");
        int adjustedIndex = -1;
        XMBCat currentCat = items.get(currentIndex).category;
        if (currentCat != null) {
            int nextIndex = currentIndex - 1;
            while (nextIndex >= 0) {
                XMBCat prevCat = items.get(nextIndex).category;
                if (prevCat != currentCat) {
                    if (prevCat != null)
                        adjustedIndex = getCachedIndexOfCat(prevCat);
                    else
                        adjustedIndex = nextIndex;
                    break;
                }
                nextIndex--;
            }
        }
        else if (currentIndex - 1 >= 0) {
            adjustedIndex = currentIndex - 1;
            XMBCat prevCat = items.get(adjustedIndex).category;
            if (prevCat != null)
                adjustedIndex = getCachedIndexOfCat(prevCat);
        }

        if (adjustedIndex >= 0)
            setIndex(adjustedIndex);
    }
    public void makeSelection() {
    }
    public void deleteSelection() {
    }
    public void refresh() {
        //invalidate();
        removeViews();
        createViews();
        setViews();
    }
}
