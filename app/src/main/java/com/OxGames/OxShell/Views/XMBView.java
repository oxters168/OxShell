package com.OxGames.OxShell.Views;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;

import com.OxGames.OxShell.Data.XMBCat;
import com.OxGames.OxShell.Data.XMBItem;

import java.util.ArrayList;

public class XMBView extends View {
    private Context context;
    private ArrayList<XMBCat> categories;
    private ArrayList<Integer> catIndices;
    private ArrayList<XMBItem> items;
    private Paint painter;
    private int currentIndex = 1;

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
        this.context = context;
        painter = new Paint();
        categories = new ArrayList<>();
        catIndices = new ArrayList<>();
        items = new ArrayList<>();

        XMBCat cat1 = new XMBCat("Cat1");
        XMBCat cat2 = new XMBCat("Cat2");
        addItem(new XMBItem(null, "Item1", cat1));
        addItem(new XMBItem(null, "Item2", cat1));
        addItem(new XMBItem(null, "Item3", cat1));
        addItem(new XMBItem(null, "Item1", cat2));
        addItem(new XMBItem(null, "Item2", cat2));
        addItem(new XMBItem(null, "Item3", cat2));
        addItem(new XMBItem(null, "Item4", cat2));
        addItem(new XMBItem(null, "Item1"));
        addItem(new XMBItem(null, "Item2"));
    }

    private Rect textRect = new Rect();
    private int iconSize = 196;
    private float horSpacing = 64;
    private float verSpacing = 0;
    private float textSize = 48;
    private int textCushion = 16;
    private int padding = 64;
    private float catShift = (iconSize + horSpacing) * 2;
    private @ColorInt int textColor = 0xFFFFFFFF;
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int vsx = getWidth(); //view size x
        int vsy = getHeight(); //view size y
        float vex = vsx / 2f; //view extents x
        float vey = vsy / 2f; //view extents y

        float horShiftOffset = iconSize + horSpacing;
        int horShiftOffsetInt = Math.round(horShiftOffset);
        float verShiftOffset = iconSize + verSpacing;
        int verShiftOffsetInt = Math.round(verShiftOffset);

        XMBCat currentCat = items.get(currentIndex).category;
        float startX = padding + iconSize / 2f + catShift;
        float startY = vey - iconSize / 2f;
        int startXInt = Math.round(startX);
        int startYInt = Math.round(startY);
        int catIndex = currentCat != null ? categories.indexOf(currentCat) : categories.size() + (currentIndex - getItemCatsStartIndex());
        drawCategories(canvas, startXInt - catIndex * horShiftOffsetInt, startYInt, horShiftOffsetInt);
        if (currentCat != null)
            drawItems(canvas, currentIndex, startXInt, startYInt, verShiftOffsetInt);

//        painter.setColor(0xFF00FF00);
//        painter.setStrokeWidth(8);
//        canvas.drawLine(0, vey, vex - 64, vey, painter);
//        canvas.drawLine(vex * 2, vey, vex + 64, vey, painter);
//        canvas.drawLine(vex, 0, vex, vey - 64, painter);
//        canvas.drawLine(vex, vey * 2, vex, vey + 64, painter);
    }
    private int getItemCatsStartIndex() {
        int startIndex = items.size();
        for (int i = startIndex - 1; i >= 0; i--) {
            if (items.get(i).category != null) {
                startIndex = i + 1;
                break;
            }
        }
        return startIndex;
    }
    private void drawCategories(Canvas canvas, int startXInt, int startYInt, int horShiftOffsetInt) {
        for (int i = 0; i < categories.size(); i++) {
            XMBCat cat = categories.get(i);
            drawCategory(canvas, painter, textRect, cat.icon, cat.title, startXInt + horShiftOffsetInt * i, startYInt, iconSize, textSize, textCushion, textColor);
        }
        int startIndex = getItemCatsStartIndex();
        int remStartXInt = startXInt + categories.size() * horShiftOffsetInt;
        for (int i = startIndex; i < items.size(); i++) {
            XMBItem item = items.get(i);
            drawCategory(canvas, painter, textRect, item.icon, item.title, remStartXInt + horShiftOffsetInt * (i - startIndex), startYInt, iconSize, textSize, textCushion, textColor);
        }
    }
    private void drawItemsAsCategories(Canvas canvas, int startIndex, int startXInt, int startYInt, int horShiftOffsetInt) {
        for (int i = startIndex; i < items.size(); i++) {
            XMBItem item = items.get(i);
            drawCategory(canvas, painter, textRect, item.icon, item.title, startXInt + horShiftOffsetInt * (i - startIndex), startYInt, iconSize, textSize, textCushion, textColor);
        }
    }
    private void drawItems(Canvas canvas, int itemIndex, int startXInt, int startYInt, int verShiftOffsetInt) {
        XMBCat currentCat = items.get(itemIndex).category;
        for (int i = itemIndex - 1; i >= 0; i--) {
            XMBItem item = items.get(i);
            if (item.category != currentCat)
                break;
            drawItem(canvas, painter, textRect, item.icon, item.title, startXInt, startYInt - verShiftOffsetInt * (((itemIndex - 1) - i) + 1), iconSize, textSize, textCushion, textColor);
        }
        painter.getTextBounds(currentCat.title, 0, currentCat.title.length(), textRect);
        for (int i = itemIndex; i < items.size(); i++) {
            XMBItem item = items.get(i);
            if (item.category != currentCat)
                break;
            drawItem(canvas, painter, textRect, item.icon, item.title, startXInt, (startYInt + textRect.height()) + verShiftOffsetInt * ((i - itemIndex) + 1), iconSize, textSize, textCushion, textColor);
        }
    }
    private static void drawCategory(Canvas canvas, Paint painter, Rect reusableRect, Drawable icon, String title, int x, int y, int iconSize, float textSize, float cushion, @ColorInt int textColor) {
        int halfIconSize = Math.round(iconSize / 2f);
        icon.setBounds(x - halfIconSize, y - halfIconSize, x + halfIconSize, y + halfIconSize);
        icon.setAlpha(255);
        icon.draw(canvas);
        painter.setColor(textColor);
        painter.setTextSize(textSize);
        painter.setTextAlign(Paint.Align.CENTER);
        painter.getTextBounds(title, 0, title.length(), reusableRect);
        canvas.drawText(title, x, y + cushion + halfIconSize + reusableRect.height() / 2f, painter);
    }
    private static void drawItem(Canvas canvas, Paint painter, Rect reusableRect, Drawable icon, String title, int x, int y, int iconSize, float textSize, float cushion, @ColorInt int textColor) {
        int halfIconSize = Math.round(iconSize / 2f);
        icon.setBounds(x - halfIconSize, y - halfIconSize, x + halfIconSize, y + halfIconSize);
        icon.setAlpha(255);
        icon.draw(canvas);
        painter.setColor(textColor);
        painter.setTextSize(textSize);
        painter.setTextAlign(Paint.Align.LEFT);
        painter.getTextBounds(title, 0, title.length(), reusableRect);
        canvas.drawText(title, x + cushion + halfIconSize, y + reusableRect.height() / 2f, painter);
    }
//    @Override
//    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        setMeasuredDimension(500, 500);
//    }
    private int columnAlpha;
    private void animateColumnAlpha() {

        ValueAnimator animator = ValueAnimator.ofFloat(0, 255);
        animator.setDuration(300);

        animator.addUpdateListener(valueAnimator -> {
            columnAlpha = ((Float)valueAnimator.getAnimatedValue()).intValue();
            XMBView.this.invalidate();
        });

        animator.start();
        invalidate();

    }

    public int addItem(XMBItem item) {
        boolean hasCat = item.category != null;
        int itemIndex = items.size();
        if (hasCat) {
            boolean catDidntExist = !categories.contains(item.category);
            if (catDidntExist) {
                Log.d("XMBView", item.category.title + " category does not exist so appending to end");
                categories.add(item.category);
            }
            if (items.size() > 0) {
                for (int i = items.size() - 1; i >= 0; i--) { //going backwards to add at the end of the category list
                    if ((catDidntExist && items.get(i).category != null) || (!catDidntExist && items.get(i).category == item.category)) { //if category didn't exist then find first item that has some category and append after or if category did exist then find last item in that category then append after
                        if (i < items.size() - 1) { //if we're not at the end of all items then we can insert in the next spot
                            itemIndex = i + 1;
                            Log.d("XMBView", "Adding item " + item.title + " with category " + item.category.title + " at index " + itemIndex);
                            items.add(itemIndex, item);
                        } else {//or else just add at the end
                            Log.d("XMBView", "Adding item " + item.title + " with category " + item.category.title + " at the end");
                            items.add(item);
                        }
                        break;
                    }
                }
            } else {
                Log.d("XMBView", "No items exist so appending item " + item.title + " with category " + item.category.title + " to the end");
                items.add(item);
            }
        } else {
            Log.d("XMBView", item.title + " has no category so appending to end");
            items.add(item);
        }
        return itemIndex;
    }
    public void removeItem(XMBItem item) {
        int itemIndex = items.indexOf(item);
        boolean removeCat = item.category != null && (itemIndex + 1 < items.size() && items.get(itemIndex + 1).category == item.category) || (itemIndex - 1 >= 0 && items.get(itemIndex - 1).category == item.category);
        if (removeCat)
            categories.remove(item.category);
        items.remove(item);
    }
    public void removeItem(int index) {
        removeItem(items.get(index));
    }
}
