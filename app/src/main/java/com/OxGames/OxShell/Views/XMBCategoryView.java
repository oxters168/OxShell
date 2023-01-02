package com.OxGames.OxShell.Views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;

public class XMBCategoryView extends View {
    public Drawable icon;
    public String title;
    private final Paint painter;
    private final Rect reusableRect;

    public int iconSize = 196; //Size of each item's icon
    public float textSize = 48; //Size of the text
    public int textCushion = 16; //Distance between item and text
    public @ColorInt
    int textColor = 0xFFFFFFFF; //The color of text

    public XMBCategoryView(Context context) {
        this(context, null);
    }
    public XMBCategoryView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }
    public XMBCategoryView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        painter = new Paint();
        reusableRect = new Rect();
    }

    public int getItemWidth() {
        return iconSize;
    }
    public int getItemHeight() {
        return iconSize;
    }
    public int getFullWidth() {
        getTextBounds(reusableRect);
        int width = Math.max(iconSize, reusableRect.width());
        return width;
    }
    public int getFullHeight() {
        getTextBounds(reusableRect);
        int height = iconSize + textCushion + reusableRect.height();
        return height;
    }
    public void getTextBounds(Rect rect) {
        if (title != null) {
            painter.setTextSize(textSize);
            painter.setTextAlign(Paint.Align.CENTER);
            painter.getTextBounds(title, 0, title.length(), rect);
        } else
            rect.set(0, 0, 0, 0);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
//        painter.setColor(0xFFFF0000);
//        painter.setStrokeWidth(8);
//        canvas.drawLine(0, 0, getWidth(), getHeight(), painter);
//        canvas.drawLine(0, getHeight(), getWidth(), 0, painter);

        drawCategory(canvas, painter, reusableRect, icon, title, 0, 0, iconSize, textSize, textCushion, textColor);
    }

    private static void drawCategory(Canvas canvas, Paint painter, Rect reusableRect, Drawable icon, String title, int x, int y, int iconSize, float textSize, float cushion, @ColorInt int textColor) {
        int right = x + iconSize;
        int bottom = y + iconSize;
        if (icon != null) {
            icon.setBounds(x, y, right, bottom);
            icon.setAlpha(255);
            icon.draw(canvas);
        }
        if (title != null) {
            painter.setColor(textColor);
            painter.setTextSize(textSize);
            painter.setTextAlign(Paint.Align.CENTER);
            painter.getTextBounds(title, 0, title.length(), reusableRect);
            canvas.drawText(title, x + iconSize / 2f, y + cushion + iconSize + reusableRect.height() / 2f, painter);
        }
    }
}
