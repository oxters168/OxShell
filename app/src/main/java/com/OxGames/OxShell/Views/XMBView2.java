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

import androidx.core.content.ContextCompat;

import com.OxGames.OxShell.Data.XMBCat;
import com.OxGames.OxShell.Data.XMBItem;
import com.OxGames.OxShell.Helpers.ActivityManager;
import com.OxGames.OxShell.Interfaces.InputReceiver;
import com.OxGames.OxShell.R;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class XMBView2 extends ViewGroup implements InputReceiver {
    private Context context;

    private Queue<XMBCategoryView> goneCatViews;
    private ArrayList<XMBCategoryView> visibleCatViews;
    private Queue<XMBItemView> goneItemViews;
    private ArrayList<XMBItemView> visibleItemViews;

    private ArrayList<XMBCat> categories;
    private ArrayList<Integer> catIndices;
    private ArrayList<XMBItem> items;

    private int iconSize = 196;
    private float textSize = 48; //Size of the text
    private int textCushion = 16; //Distance between item and text
    private float horSpacing = 64; //How much space to add between items horizontally
    private float verSpacing = 0; //How much space to add between items vertically
    private float catShift = (iconSize + horSpacing) * 2; //How much to shift the categories bar horizontally

    int currentIndex = 0;

    public XMBView2(Context context) {
        this(context, null);
    }
    public XMBView2(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    public XMBView2(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;

        goneCatViews = new ArrayDeque<>();
        visibleCatViews = new ArrayList<>();
        goneItemViews = new ArrayDeque<>();
        visibleItemViews = new ArrayList<>();
        categories = new ArrayList<>();
        catIndices = new ArrayList<>();
        items = new ArrayList<>();

        XMBCat cat1 = new XMBCat("Cat1");
        XMBCat cat2 = new XMBCat("Cat2");
        ArrayList<XMBItem> list = new ArrayList<>();
        list.add(new XMBItem(null, "Item1", cat1));
        list.add(new XMBItem(null, "Item2", cat1));
        list.add(new XMBItem(null, "Item3", cat1));
        list.add(new XMBItem(null, "Item1", cat2));
        list.add(new XMBItem(null, "Item2", cat2));
        list.add(new XMBItem(null, "Item3", cat2));
        list.add(new XMBItem(null, "Item4", cat2));
        list.add(new XMBItem(null, "Item1"));
        list.add(new XMBItem(null, "Item2"));
        addItems(list);
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
        XMBCat currentCat = items.get(currentIndex).category;
        return currentCat != null ? categories.indexOf(currentCat) : categories.size() + (currentIndex - getItemCatsStartIndex()); //The index of how far we are along the columns row
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        Log.d("XMBView2", "onLayout called");

        if (items.size() > 0) {
            int horShiftOffset = Math.round(iconSize + horSpacing); //How far apart each item is from center to center
            int verShiftOffset = Math.round(iconSize + verSpacing); //How far apart each item is from center to center
            drawCategories(getStartX(), getStartY(), horShiftOffset);
            drawItems(currentIndex, getStartX(), getStartY(), horShiftOffset, verShiftOffset);
        }
    }
    private void drawCategories(int startXInt, int startYInt, int horShiftOffsetInt) {
        int viewWidth = getWidth();
        int viewHeight = getHeight();
        Rect rect = new Rect();
        for (int i = 0; i < categories.size(); i++) {
            XMBCat cat = categories.get(i);
            getTextBounds(painter, cat.title, textSize, rect);
            int expX = startXInt + horShiftOffsetInt * i;
            int expY = startYInt;
            int right = expX + Math.max(iconSize, rect.width());
            int bottom = expY + iconSize + textCushion + rect.height();
            boolean inBounds = expX < viewWidth || bottom > 0 || right > 0 || expY < viewHeight;
            if (inBounds) {
                XMBCategoryView view = addViewForCategory(cat);
                view.layout(expX, expY, right, bottom);
            }
        }
        int startIndex = getItemCatsStartIndex();
        int remStartXInt = startXInt + categories.size() * horShiftOffsetInt;
        for (int i = startIndex; i < items.size(); i++) {
            XMBItem item = items.get(i);
            getTextBounds(painter, item.title, textSize, rect);
            int expX = remStartXInt + horShiftOffsetInt * (i - startIndex);
            int expY = startYInt;
            int right = expX + Math.max(iconSize, rect.width());
            int bottom = expY + iconSize + textCushion + rect.height();
            boolean inBounds = expX < viewWidth || bottom > 0 || right > 0 || expY < viewHeight;
            if (inBounds) {
                XMBCategoryView view = addViewForCategory(item);
                view.layout(expX, expY, right, bottom);
            }
        }
    }
    private void drawItems(int itemIndex, int startXInt, int startYInt, int horShiftOffsetInt, int verShiftOffsetInt) {
        XMBCat origCat = items.get(itemIndex).category;
        int viewWidth = getWidth();
        int viewHeight = getHeight();
        Rect rect = new Rect();
        for (int i = 0; i < items.size(); i++) {
            XMBItem item = items.get(i);
            XMBCat currentCat = item.category;
            if (currentCat == null) //This means it should be drawn with the categories, not here
                continue;
            int currentCatIndex = categories.indexOf(currentCat);
            int itemCatIndex = getCachedIndexOfCat(currentCat);
            int expX = startXInt + horShiftOffsetInt * currentCatIndex;
            getTextBounds(painter, item.title, textSize, rect);
            int expY = (startYInt + rect.height()) + verShiftOffsetInt * ((i - itemCatIndex) + 1);
            if (i < itemCatIndex)
                expY = startYInt - verShiftOffsetInt * (((itemCatIndex - 1) - i) + 1);
            int right = expX + iconSize + textCushion + rect.width();
            int bottom = expY + iconSize;
            boolean inBounds = expX < viewWidth || bottom > 0 || right > 0 || expY < viewHeight;
            if (item.category == origCat && inBounds) {
                XMBItemView view = addViewForItem(item);
                view.layout(expX, expY, right, bottom);
            }
        }
    }

    public static void getTextBounds(Paint painter, String text, float textSize, Rect rect) {
        painter.setTextSize(textSize);
        //painter.setTextAlign(Paint.Align.CENTER);
        painter.getTextBounds(text, 0, text.length(), rect);
    }

    Paint painter = new Paint();
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
            for (int i = 0; i < getChildCount(); i++) {
                XMBItemView child = (XMBItemView)getChildAt(i);
                float nextY = (i + currentIndex) * child.getItemHeight();
                float transY = (nextY - child.getY()) * xmbTrans + child.getY();
                child.setY(transY);
            }
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

    private int getItemCatsStartIndex() {
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
        for (int i = 0; i < items.size(); i++)
            if (items.get(i).category == cat)
                return i;
        return -1;
    }
    private int getCatSize(XMBCat cat) {
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
            removeViewForCategory(catIndex);
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

        removeViewForItem(itemIndex);
        items.remove(item);
        setIndex(adjustedIndex);
    }
    private XMBCategoryView addViewForCategory(XMBCat category) {
        XMBCategoryView view;
        if (goneCatViews.isEmpty()) {
            LayoutInflater layoutInflater = LayoutInflater.from(context);
            view = (XMBCategoryView) layoutInflater.inflate(R.layout.xmb_category, null);
            view.title = category.title;
            view.icon = category.icon;
            addView(view);
        } else {
            view = goneCatViews.poll();
            view.title = category.title;
            view.icon = category.icon;
            view.setVisibility(VISIBLE);
        }
        int index = categories.indexOf(category);
        if (index >= visibleCatViews.size())
            visibleCatViews.add(view);
        else
            visibleCatViews.add(index, view);
        return view;
    }
    private XMBCategoryView addViewForCategory(XMBItem category) {
        XMBCategoryView view;
        if (goneCatViews.isEmpty()) {
            LayoutInflater layoutInflater = LayoutInflater.from(context);
            view = (XMBCategoryView) layoutInflater.inflate(R.layout.xmb_category, null);
            view.title = category.title;
            view.icon = category.getIcon();
            addView(view);
        } else {
            view = goneCatViews.poll();
            view.title = category.title;
            view.icon = category.getIcon();
            view.setVisibility(VISIBLE);
        }
        //int index = categories.indexOf(category);
        //if (index >= visibleCatViews.size())
            visibleCatViews.add(view);
        //else
        //    visibleCatViews.add(index, view);
        return view;
    }
    private XMBItemView addViewForItem(XMBItem item) {
        XMBItemView view;
        if (goneItemViews.isEmpty()) {
            LayoutInflater layoutInflater = LayoutInflater.from(context);
            view = (XMBItemView) layoutInflater.inflate(R.layout.xmb_item, null);
            view.title = item.title;
            view.icon = item.getIcon();
            addView(view);
        } else {
            view = goneItemViews.poll();
            view.title = item.title;
            view.icon = item.getIcon();
            view.setVisibility(VISIBLE);
        }
        int index = items.indexOf(item);
        if (index >= visibleItemViews.size())
            visibleItemViews.add(view);
        else
            visibleItemViews.add(index, view);
        return view;
    }
    private void removeViewForItem(int itemIndex) {
        XMBItemView toBeRemoved = visibleItemViews.get(itemIndex);
        visibleItemViews.remove(itemIndex);
        toBeRemoved.setVisibility(GONE);
        goneItemViews.add(toBeRemoved);
    }
    private void removeViewForCategory(int catIndex) {
        XMBCategoryView toBeRemoved = visibleCatViews.get(catIndex);
        visibleCatViews.remove(catIndex);
        toBeRemoved.setVisibility(GONE);
        goneCatViews.add(toBeRemoved);
    }
    public void removeItem(int index) {
        removeItem(items.get(index));
    }
    public void clear() {
        for (int i = items.size() - 1; i >= 0; i--)
            removeViewForItem(i);
        for (int i = categories.size() - 1; i >= 0; i--)
            removeViewForCategory(i);
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
        XMBCat currentCat = items.get(currentIndex).category;
        if (currentCat != null && currentIndex + 1 < items.size() && items.get(currentIndex + 1).category == currentCat)
            setIndex(currentIndex + 1);
//        clearAnimation();
//        Animation animDown = AnimationUtils.loadAnimation(context, android.R.anim.slide_in_left);
//        setAnimation(animDown);
//        animDown.startNow();
        //animateXMB();
    }
    public void selectUpperItem() {
        //Log.d("XMBView", "Received move up signal");
        XMBCat currentCat = items.get(currentIndex).category;
        if (currentCat != null && currentIndex - 1 >= 0 && items.get(currentIndex - 1).category == currentCat)
            setIndex(currentIndex - 1);
//        clearAnimation();
//        Animation animUp = AnimationUtils.loadAnimation(context, android.R.anim.slide_in_left);
//        setAnimation(animUp);
//        animUp.startNow();
        //animateXMB();
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
    }
}
