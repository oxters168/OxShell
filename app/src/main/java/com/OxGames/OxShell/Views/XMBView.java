package com.OxGames.OxShell.Views;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.DragEvent;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;

import com.OxGames.OxShell.Data.XMBCat;
import com.OxGames.OxShell.Data.XMBItem;
import com.OxGames.OxShell.Helpers.SlideTouchHandler;
import com.OxGames.OxShell.Interfaces.InputReceiver;
import com.OxGames.OxShell.Interfaces.SlideTouchListener;

import java.util.ArrayList;
import java.util.List;

public class XMBView extends View implements InputReceiver, SlideTouchListener {
    //private Context context;
    private SlideTouchHandler slideTouch = new SlideTouchHandler();

    private ArrayList<XMBCat> categories;
    private ArrayList<Integer> catIndices;
    private ArrayList<XMBItem> items;
    private Paint painter;
    private int currentIndex = 0;

    public XMBView(Context context) {
        this(context, null);
    }
    public XMBView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }
    public XMBView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }
    public XMBView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        //this.context = context;
        slideTouch.addListener(this);

        painter = new Paint();
        categories = new ArrayList<>();
        catIndices = new ArrayList<>();
        items = new ArrayList<>();

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

    private Rect reusableRect = new Rect(); //A shared rect used internally for getting the size of stuff (mostly text)
    private int iconSize = 196; //Size of each item's icon
    private float horSpacing = 64; //How much space to add between items horizontally
    private float verSpacing = 0; //How much space to add between items vertically
    private float textSize = 48; //Size of the text
    private int textCushion = 16; //Distance between item and text
    private int padding = 64; //Distance from the edge of the screen
    private float catShift = (iconSize + horSpacing) * 2; //How much to shift the categories bar horizontally
    private @ColorInt int textColor = 0xFFFFFFFF; //The color of text
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Log.d("XMBView", "Drawing view");

        if (items.size() > 0) { //If for whatever reason there are no items
            //int vsx = getWidth(); //view size x
            //float vex = vsx / 2f; //view extents x
            int vsy = getHeight(); //view size y
            float vey = vsy / 2f; //view extents y

            int horShiftOffset = Math.round(iconSize + horSpacing); //How far apart each item is from center to center
            int verShiftOffset = Math.round(iconSize + verSpacing); //How far apart each item is from center to center

            XMBCat currentCat = items.get(currentIndex).category;
            int startX = Math.round(padding + iconSize / 2f + catShift); //Where the current item's column is along the x-axis
            int startY = Math.round(vey - iconSize / 2f); //Where the current item's column is along the y-axis
            int horIndex = currentCat != null ? categories.indexOf(currentCat) : categories.size() + (currentIndex - getItemCatsStartIndex()); //The index of how far we are along the columns row
            drawCategories(canvas, startX - horIndex * horShiftOffset, startY, horShiftOffset);
            //if (currentCat != null)
            drawItems(canvas, currentIndex, startX - horIndex * horShiftOffset, startY, horShiftOffset, verShiftOffset);
        }
    }
    private void drawCategories(Canvas canvas, int startXInt, int startYInt, int horShiftOffsetInt) {
        for (int i = 0; i < categories.size(); i++) {
            XMBCat cat = categories.get(i);
            int expX = startXInt + horShiftOffsetInt * i;
            cat.currentX = ((expX - cat.currentX) * xmbTrans) + cat.currentX;
            cat.currentY = ((startYInt - cat.currentY) * xmbTrans) + cat.currentY;
            drawCategory(canvas, painter, reusableRect, cat.icon, cat.title, Math.round(cat.currentX), Math.round(cat.currentY), iconSize, textSize, textCushion, textColor, getWidth(), getHeight());
        }
        int startIndex = getItemCatsStartIndex();
        int remStartXInt = startXInt + categories.size() * horShiftOffsetInt;
        for (int i = startIndex; i < items.size(); i++) {
            XMBItem item = items.get(i);
            int expX = remStartXInt + horShiftOffsetInt * (i - startIndex);
            item.currentX = ((expX - item.currentX) * xmbTrans) + item.currentX;
            item.currentY = ((startYInt - item.currentY) * xmbTrans) + item.currentY;
            drawCategory(canvas, painter, reusableRect, item.getIcon(), item.title, Math.round(item.currentX), Math.round(item.currentY), iconSize, textSize, textCushion, textColor, getWidth(), getHeight());
        }
    }
    private void drawItems(Canvas canvas, int itemIndex, int startXInt, int startYInt, int horShiftOffsetInt, int verShiftOffsetInt) {
        XMBCat origCat = items.get(itemIndex).category;
        for (int i = 0; i < items.size(); i++) {
            XMBItem item = items.get(i);
            XMBCat currentCat = items.get(i).category;
            if (currentCat == null) //This means it should be drawn with the categories, not here
                continue;
            int currentCatIndex = categories.indexOf(currentCat);
            int itemCatIndex = getCachedIndexOfCat(currentCat);
            int expX = startXInt + horShiftOffsetInt * currentCatIndex;
            int expY = (startYInt + reusableRect.height()) + verShiftOffsetInt * ((i - itemCatIndex) + 1);
            if (i < itemCatIndex)
                expY = startYInt - verShiftOffsetInt * (((itemCatIndex - 1) - i) + 1);
            item.currentX = ((expX - item.currentX) * xmbTrans) + item.currentX;
            item.currentY = ((expY - item.currentY) * xmbTrans) + item.currentY;
            if (item.category == origCat)
                drawItem(canvas, painter, reusableRect, item.getIcon(), item.title, Math.round(item.currentX), Math.round(item.currentY), iconSize, textSize, textCushion, textColor, getWidth(), getHeight());
        }
    }
    private static void drawCategory(Canvas canvas, Paint painter, Rect reusableRect, Drawable icon, String title, int x, int y, int iconSize, float textSize, float cushion, @ColorInt int textColor, int viewSizeX, int viewSizeY) {
        int halfIconSize = Math.round(iconSize / 2f);
        int left = x - halfIconSize;
        int top = y - halfIconSize;
        int right = x + halfIconSize;
        int bottom = y + halfIconSize;
        boolean inBounds = left < viewSizeX || bottom > 0 || right > 0 || top < viewSizeY;
        if (inBounds) {
            icon.setBounds(left, top, right, bottom);
            icon.setAlpha(255);
            icon.draw(canvas);
            Log.d("XMBView", "Drew category icon");
        }
        if (title != null) {
            painter.setColor(textColor);
            painter.setTextSize(textSize);
            painter.setTextAlign(Paint.Align.CENTER);
            painter.getTextBounds(title, 0, title.length(), reusableRect);
            inBounds = reusableRect.left < viewSizeX || reusableRect.bottom > 0 || reusableRect.right > 0 || reusableRect.top < viewSizeY;
            if (inBounds)
                canvas.drawText(title, x, y + cushion + halfIconSize + reusableRect.height() / 2f, painter);
        }
    }
    private static void drawItem(Canvas canvas, Paint painter, Rect reusableRect, Drawable icon, String title, int x, int y, int iconSize, float textSize, float cushion, @ColorInt int textColor, int viewSizeX, int viewSizeY) {
        int halfIconSize = Math.round(iconSize / 2f);
        int left = x - halfIconSize;
        int top = y - halfIconSize;
        int right = x + halfIconSize;
        int bottom = y + halfIconSize;
        boolean inBounds = left < viewSizeX || bottom > 0 || right > 0 || top < viewSizeY;
        if (inBounds) {
            icon.setBounds(left, top, right, bottom);
            icon.setAlpha(255);
            icon.draw(canvas);
            Log.d("XMBView", "Drew item icon");
        }
        if (title != null) {
            painter.setColor(textColor);
            painter.setTextSize(textSize);
            painter.setTextAlign(Paint.Align.LEFT);
            painter.getTextBounds(title, 0, title.length(), reusableRect);
            inBounds = reusableRect.left < viewSizeX || reusableRect.bottom > 0 || reusableRect.right > 0 || reusableRect.top < viewSizeY;
            if (inBounds)
                canvas.drawText(title, x + cushion + halfIconSize, y + reusableRect.height() / 2f, painter);
        }
    }
    private float xmbTrans = 1;
    ValueAnimator xmbimator = null;
    private void animateXMB() {
        if (xmbimator != null)
            xmbimator.cancel();

        //catTransX = 0;
        xmbimator = ValueAnimator.ofFloat(0, 1);
        xmbimator.setDuration(300);
        xmbimator.addUpdateListener(valueAnimator -> {
            xmbTrans = ((Float)valueAnimator.getAnimatedValue());
            //Log.d("XMBView", "Animating " + catTransX);
            XMBView.this.invalidate();
        });
        //Log.d("XMBView", "Starting animation");
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
        invalidate();
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
    private int addItem(XMBItem item, boolean invalidate) {
        boolean hasCat = item.category != null;
        int itemIndex = items.size();
        if (hasCat) {
            boolean catDidntExist = !categories.contains(item.category);
            if (catDidntExist) {
                //Log.d("XMBView", item.category.title + " category does not exist so appending to end");
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
        if (invalidate)
            invalidate();
        return itemIndex;
    }
    public int addItem(XMBItem item) {
        return addItem(item, true);
    }
    public void addItems(XMBItem[] items) {
        for (int i = 0; i < items.length; i++)
            addItem(items[i], i == items.length - 1);
    }
    public void addItems(List items) {
        for (int i = 0; i < items.size(); i++)
            addItem((XMBItem)items.get(i), i == items.size() - 1);
    }
    public void removeItem(XMBItem item) {
        int itemIndex = items.indexOf(item);
        boolean removeCat = item.category != null && (itemIndex + 1 < items.size() && items.get(itemIndex + 1).category == item.category) || (itemIndex - 1 >= 0 && items.get(itemIndex - 1).category == item.category);
        int adjustedIndex = currentIndex;
        if (removeCat) {
            catIndices.remove(categories.indexOf(item.category));
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
        items.remove(item);
        setIndex(adjustedIndex);
    }
    public void removeItem(int index) {
        removeItem(items.get(index));
    }
    public void clear() {
        categories.clear();
        catIndices.clear();
        items.clear();
        setIndex(0);
    }

    @Override
    public boolean receiveKeyEvent(KeyEvent key_event) {
        //Log.d("SlideTouchGridView", key_event.toString());
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
        invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        slideTouch.update(ev);
        return true;
    }
    @Override
    public void onSwipeUp() {
        selectUpperItem();
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
    public void onClick() {
        makeSelection();
    }
}
