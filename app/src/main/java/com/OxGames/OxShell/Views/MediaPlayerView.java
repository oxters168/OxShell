package com.OxGames.OxShell.Views;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.os.Build;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.OxGames.OxShell.Data.SettingsKeeper;
import com.OxGames.OxShell.Helpers.AndroidHelpers;
import com.OxGames.OxShell.Helpers.InputHandler;
import com.OxGames.OxShell.OxShellApp;
import com.OxGames.OxShell.R;

public class MediaPlayerView extends FrameLayout {
    private final Context context;
    private BetterTextView titleLabel;

    public MediaPlayerView(@NonNull Context context) {
        super(context);
        this.context = context;
        init();
    }
    public MediaPlayerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
    }
    public MediaPlayerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init();
    }
    public MediaPlayerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.context = context;
        init();
    }

    private void end() {
        OxShellApp.getCurrentActivity().finish();
    }

    private void init() {
        LayoutParams layoutParams;

        int actionBarHeight = Math.round(AndroidHelpers.getScaledDpToPixels(context, 64));
        int textOutlineSize = Math.round(AndroidHelpers.getScaledDpToPixels(context, 3));
        int titleTextSize = Math.round(AndroidHelpers.getScaledSpToPixels(context, 16));
        //int titleStartMargin = Math.round(AndroidHelpers.getScaledDpToPixels(context, 24));
        int titleMargins = Math.round(AndroidHelpers.getScaledDpToPixels(context, 16));
        int backBtnSize = Math.round(AndroidHelpers.getScaledDpToPixels(context, 32));
        int backBtnMargin = (actionBarHeight - backBtnSize) / 2;

        layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        layoutParams.gravity = Gravity.TOP | Gravity.LEFT;
        setLayoutParams(layoutParams);
        setBackgroundColor(Color.GREEN);
        setFocusable(false);

        FrameLayout customActionBar = new FrameLayout(context);
        layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, actionBarHeight);
        layoutParams.gravity = Gravity.TOP | Gravity.START;
        customActionBar.setLayoutParams(layoutParams);
        customActionBar.setBackgroundColor(Color.parseColor("#BB323232"));
        addView(customActionBar);

        titleLabel = new BetterTextView(context);
        layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        layoutParams.gravity = Gravity.START | Gravity.CENTER_VERTICAL;
        //layoutParams.setMargins(titleMargins, titleMargins, titleMargins, titleMargins);
        layoutParams.setMarginStart(backBtnSize + backBtnMargin + titleMargins);
        layoutParams.setMarginEnd(backBtnSize + backBtnMargin + titleMargins);
        titleLabel.setLayoutParams(layoutParams);
        titleLabel.setOverScrollMode(SCROLL_AXIS_VERTICAL);
        titleLabel.setMovementMethod(new ScrollingMovementMethod());
        titleLabel.setEllipsize(TextUtils.TruncateAt.END);
        titleLabel.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        titleLabel.setTextAlignment(View.TEXT_ALIGNMENT_GRAVITY);
        titleLabel.setTextColor(context.getColor(R.color.text));
        titleLabel.setOutlineColor(Color.BLACK);
        titleLabel.setOutlineSize(textOutlineSize);
        titleLabel.setTextSize(titleTextSize);
        titleLabel.setText("Title");
        titleLabel.setFocusable(false);
        Typeface font = SettingsKeeper.getFont();
        titleLabel.setTypeface(font);
        customActionBar.addView(titleLabel);

        Button backBtn = new Button(context);
        layoutParams = new LayoutParams(backBtnSize, backBtnSize);
        layoutParams.gravity = Gravity.START | Gravity.CENTER_VERTICAL;
        //layoutParams.setMargins(0, backBtnMargin, 0, 0);
        layoutParams.setMarginStart(backBtnMargin);
        backBtn.setLayoutParams(layoutParams);
        backBtn.setBackground(ContextCompat.getDrawable(context, R.drawable.baseline_arrow_back_24));
        backBtn.setOnClickListener((btn) -> end());
        customActionBar.addView(backBtn);

        Button endBtn = new Button(context);
        layoutParams = new LayoutParams(backBtnSize, backBtnSize);
        layoutParams.gravity = Gravity.END | Gravity.CENTER_VERTICAL;
        //layoutParams.setMargins(0, backBtnMargin, 0, 0);
        layoutParams.setMarginEnd(backBtnMargin);
        endBtn.setLayoutParams(layoutParams);
        endBtn.setBackground(ContextCompat.getDrawable(context, R.drawable.baseline_close_24));
        endBtn.setOnClickListener((btn) -> end());
        customActionBar.addView(endBtn);
    }
}
