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

public class XMBItemView extends View {
    public Drawable icon;
    public String title;
    private final Paint painter;
    private final Rect reusableRect;

    public int iconSize = 196; //Size of each item's icon
    public float textSize = 48; //Size of the text
    public int textCushion = 16; //Distance between item and text
    public @ColorInt int textColor = 0xFFFFFFFF; //The color of text

    public XMBItemView(Context context) {
        this(context, null);
    }
    public XMBItemView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }
    public XMBItemView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
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
        painter.setTextSize(textSize);
        painter.setTextAlign(Paint.Align.LEFT);
        painter.getTextBounds(title, 0, title.length(), reusableRect);
        return iconSize + textCushion + reusableRect.width();
    }
    public int getFullHeight() {
        return iconSize;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawItem(canvas, painter, reusableRect, icon, title, 0, 0, iconSize, textSize, textCushion, textColor);
    }

//    @Override
//    public int getWidth() {
//        return iconSize + textCushion + reusableRect.width();
//    }
//    @Override
//    public int getHeight() {
//        return iconSize;
//    }

    private static void drawItem(Canvas canvas, Paint painter, Rect reusableRect, Drawable icon, String title, int x, int y, int iconSize, float textSize, float cushion, @ColorInt int textColor) {
        int halfIconSize = Math.round(iconSize / 2f);
        int left = x;
        int top = y;
        int right = x + iconSize;
        int bottom = y + iconSize;
        icon.setBounds(left, top, right, bottom);
        icon.setAlpha(255);
        icon.draw(canvas);
        if (title != null) {
            painter.setColor(textColor);
            painter.setTextSize(textSize);
            painter.setTextAlign(Paint.Align.LEFT);
            painter.getTextBounds(title, 0, title.length(), reusableRect);
            canvas.drawText(title, x + cushion + iconSize, y + iconSize / 2f + reusableRect.height() / 2f, painter);
        }
    }
}
