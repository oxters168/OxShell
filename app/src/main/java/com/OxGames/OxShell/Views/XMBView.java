package com.OxGames.OxShell.Views;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;

import com.OxGames.OxShell.Data.XMBCat;
import com.OxGames.OxShell.Data.XMBItem;
import com.OxGames.OxShell.R;

import java.util.ArrayList;

public class XMBView extends View {
    private Context context;
    private ArrayList<XMBCat> categories;
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
        this.context = context;
        painter = new Paint();
        items = new ArrayList<>();
        categories = new ArrayList<>();
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
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int vsx = getWidth(); //view size x
        int vsy = getHeight(); //view size y
        float vex = vsx / 2f; //view extents x
        float vey = vsy / 2f; //view extents y

        int iconSize = 128;
        float textSize = 48;
        float cushion = 16;
        int padding = 64;
        @ColorInt int textColor = 0xFFFFFFFF;

        float shiftOffset = iconSize + cushion;
        int shiftOffsetInt = Math.round(shiftOffset);

        float startX = padding + iconSize / 2f;
        float startY = vey - shiftOffset;
        int startXInt = Math.round(startX);
        int startYInt = Math.round(startY);
        //XMBItem currentItem = items.get(currentIndex);
        XMBCat currentCat = items.get(currentIndex).category;
        int catIndex = categories.indexOf(currentCat);
        if (catIndex < 0) {
            for (int i = currentIndex; i < items.size(); i++) {
                XMBItem currentItem = items.get(i);
                drawCategory(canvas, currentItem.icon, currentItem.title, startXInt + shiftOffsetInt * (i - currentIndex), startYInt, iconSize, textSize, cushion, textColor);  //drawn as categories since they are on the same line
            }
        } else {
            for (int i = currentIndex - 1; i >= 0; i--) {
                XMBItem item = items.get(i);
                if (item.category != currentCat)
                    break;
                drawItem(canvas, item.icon, item.title, startXInt, startYInt - shiftOffsetInt * (((currentIndex - 1) - i) + 1), iconSize, textSize, cushion, textColor);
            }
            painter.getTextBounds(currentCat.title, 0, currentCat.title.length(), textRect);
            for (int i = currentIndex; i < items.size(); i++) {
                XMBItem item = items.get(i);
                if (item.category != currentCat)
                    break;
                drawItem(canvas, item.icon, item.title, startXInt, (startYInt + textRect.height()) + shiftOffsetInt * ((i - currentIndex) + 1), iconSize, textSize, cushion, textColor);
            }
            for (int i = catIndex; i < categories.size(); i++) {
                XMBCat cat = categories.get(i);
                drawCategory(canvas, cat.icon, cat.title, startXInt + shiftOffsetInt * (i - catIndex), startYInt, iconSize, textSize, cushion, textColor);
            }
            int categoriesDrawn = categories.size() - catIndex;
            Log.d("XMBView", "Cats drawn " + categoriesDrawn);
            int remStartXInt = startXInt + categoriesDrawn * shiftOffsetInt; //adding one since we want to start after the categories
            int startIndex = items.size();
            for (int i = startIndex - 1; i >= 0; i--) {
                if (items.get(i).category != null) {
                    startIndex = i + 1;
                    break;
                }
            }
            for (int i = startIndex; i < items.size(); i++) {
                XMBItem item = items.get(i);
                drawCategory(canvas, item.icon, item.title, remStartXInt + shiftOffsetInt * (i - startIndex), startYInt, iconSize, textSize, cushion, textColor); //drawn as categories since they are on the same line
            }
        }

//        painter.setColor(0xFF00FF00);
//        painter.setStrokeWidth(8);
//        canvas.drawLine(0, vey, vex - 64, vey, painter);
//        canvas.drawLine(vex * 2, vey, vex + 64, vey, painter);
//        canvas.drawLine(vex, 0, vex, vey - 64, painter);
//        canvas.drawLine(vex, vey * 2, vex, vey + 64, painter);
    }
    private void drawCategory(Canvas canvas, Drawable icon, String title, int x, int y, int iconSize, float textSize, float cushion, @ColorInt int textColor) {
        int halfIconSize = Math.round(iconSize / 2f);
        icon.setBounds(x - halfIconSize, y - halfIconSize, x + halfIconSize, y + halfIconSize);
        icon.draw(canvas);
        painter.setColor(textColor);
        painter.setTextSize(textSize);
        painter.setTextAlign(Paint.Align.CENTER);
        painter.getTextBounds(title, 0, title.length(), textRect);
        canvas.drawText(title, x, y + cushion + halfIconSize + textRect.height() / 2f, painter);
    }
    private void drawItem(Canvas canvas, Drawable icon, String title, int x, int y, int iconSize, float textSize, float cushion, @ColorInt int textColor) {
        int halfIconSize = Math.round(iconSize / 2f);
        icon.setBounds(x - halfIconSize, y - halfIconSize, x + halfIconSize, y + halfIconSize);
        icon.draw(canvas);
        painter.setColor(textColor);
        painter.setTextSize(textSize);
        painter.setTextAlign(Paint.Align.LEFT);
        painter.getTextBounds(title, 0, title.length(), textRect);
        canvas.drawText(title, x + cushion + halfIconSize, y + textRect.height() / 2f, painter);
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
            //newPos = ((Float) valueAnimator.getAnimatedValue()).intValue();

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
