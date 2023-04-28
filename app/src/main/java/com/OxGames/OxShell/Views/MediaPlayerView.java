package com.OxGames.OxShell.Views;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.OxGames.OxShell.Data.SettingsKeeper;
import com.OxGames.OxShell.Helpers.AndroidHelpers;
import com.OxGames.OxShell.R;
import com.google.android.material.slider.Slider;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class MediaPlayerView extends FrameLayout {
    public enum MediaButton { back, end, play, pause, seekFwd, seekBck, skipNext, skipPrev }
    private final Context context;
    private BetterTextView titleLabel;
    private Button backBtn;
    private Button endBtn;
    private Button playBtn;
    private Button seekFwd;
    private Button skipFwd;
    private Button seekBck;
    private Button skipPrv;
    private Slider seekBar;

    private boolean isPlaying;

    private final List<Consumer<MediaButton>> mediaBtnListeners;
    private final List<Consumer<Float>> seekBarListeners;

    public MediaPlayerView(@NonNull Context context) {
        super(context);
        this.context = context;
        mediaBtnListeners = new ArrayList<>();
        seekBarListeners = new ArrayList<>();
        init();
    }
    public MediaPlayerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        mediaBtnListeners = new ArrayList<>();
        seekBarListeners = new ArrayList<>();
        init();
    }
    public MediaPlayerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        mediaBtnListeners = new ArrayList<>();
        seekBarListeners = new ArrayList<>();
        init();
    }
    public MediaPlayerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.context = context;
        mediaBtnListeners = new ArrayList<>();
        seekBarListeners = new ArrayList<>();
        init();
    }

    public void setIsPlaying(boolean onOff) {
        isPlaying = onOff;
        playBtn.setBackground(ContextCompat.getDrawable(context, isPlaying ? R.drawable.baseline_pause_24 : R.drawable.baseline_play_arrow_24));
    }
    public void setTitle(String value) {
        titleLabel.setText(value);
    }
    public void onDestroy() {
        backBtn.setOnClickListener(null);
        endBtn.setOnClickListener(null);
        playBtn.setOnClickListener(null);
        seekFwd.setOnClickListener(null);
        skipFwd.setOnClickListener(null);
        seekBck.setOnClickListener(null);
        skipPrv.setOnClickListener(null);
        seekBar.clearOnSliderTouchListeners();
        clearMediaBtnListeners();
        clearSeekBarListeners();
    }

    public void addMediaBtnListener(Consumer<MediaButton> mediaBtnListener) {
        mediaBtnListeners.add(mediaBtnListener);
    }
    public void removeMediaBtnListener(Consumer<MediaButton> mediaBtnListener) {
        mediaBtnListeners.remove(mediaBtnListener);
    }
    public void clearMediaBtnListeners() {
        mediaBtnListeners.clear();
    }
    private void fireMediaBtnEvent(MediaButton btn) {
        for (Consumer<MediaButton> mediaBtnListener : mediaBtnListeners)
            mediaBtnListener.accept(btn);
    }
    public void addSeekBarListener(Consumer<Float> seekBarListener) {
        seekBarListeners.add(seekBarListener);
    }
    public void removeSeekBarListener(Consumer<Float> seekBarListener) {
        seekBarListeners.remove(seekBarListener);
    }
    public void clearSeekBarListeners() {
        seekBarListeners.clear();
    }
    private void fireSeekBarEvent(float value) {
        for (Consumer<Float> seekBarListener : seekBarListeners)
            seekBarListener.accept(value);
    }

    private void init() {
        LayoutParams layoutParams;

        int actionBarHeight = Math.round(AndroidHelpers.getScaledDpToPixels(context, 64));
        int textOutlineSize = Math.round(AndroidHelpers.getScaledDpToPixels(context, 3));
        int titleTextSize = Math.round(AndroidHelpers.getScaledSpToPixels(context, 16));
        int smallCushion = Math.round(AndroidHelpers.getScaledDpToPixels(context, 16));
        int btnSize = Math.round(AndroidHelpers.getScaledDpToPixels(context, 32));
        int btnEdgeMargin = (actionBarHeight - btnSize) / 2;

        layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        layoutParams.gravity = Gravity.TOP | Gravity.LEFT;
        setLayoutParams(layoutParams);
        setBackgroundColor(Color.GREEN);
        setFocusable(false);

        FrameLayout customActionBar = new FrameLayout(context);
        layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, actionBarHeight);
        layoutParams.gravity = Gravity.TOP | Gravity.LEFT;
        customActionBar.setLayoutParams(layoutParams);
        customActionBar.setBackgroundColor(Color.parseColor("#BB323232"));
        customActionBar.setFocusable(false);
        addView(customActionBar);

        titleLabel = new BetterTextView(context);
        layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        layoutParams.gravity = Gravity.START | Gravity.CENTER_VERTICAL;
        layoutParams.setMarginStart(btnSize + btnEdgeMargin + smallCushion);
        layoutParams.setMarginEnd(btnSize + btnEdgeMargin + smallCushion);
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

        backBtn = new Button(context);
        layoutParams = new LayoutParams(btnSize, btnSize);
        layoutParams.gravity = Gravity.START | Gravity.CENTER_VERTICAL;
        layoutParams.setMarginStart(btnEdgeMargin);
        backBtn.setLayoutParams(layoutParams);
        backBtn.setBackground(ContextCompat.getDrawable(context, R.drawable.baseline_arrow_back_24));
        backBtn.setOnClickListener((btn) -> fireMediaBtnEvent(MediaButton.back));
        customActionBar.addView(backBtn);

        endBtn = new Button(context);
        layoutParams = new LayoutParams(btnSize, btnSize);
        layoutParams.gravity = Gravity.END | Gravity.CENTER_VERTICAL;
        layoutParams.setMarginEnd(btnEdgeMargin);
        endBtn.setLayoutParams(layoutParams);
        endBtn.setBackground(ContextCompat.getDrawable(context, R.drawable.baseline_close_24));
        endBtn.setOnClickListener((btn) -> fireMediaBtnEvent(MediaButton.end));
        customActionBar.addView(endBtn);

        FrameLayout controlsBar = new FrameLayout(context);
        layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, actionBarHeight * 2 + smallCushion);
        layoutParams.gravity = Gravity.LEFT | Gravity.BOTTOM;
        controlsBar.setLayoutParams(layoutParams);
        controlsBar.setBackgroundColor(Color.parseColor("#BB323232"));
        controlsBar.setFocusable(false);
        addView(controlsBar);

        playBtn = new Button(context);
        layoutParams = new LayoutParams(btnSize, btnSize);
        layoutParams.gravity = Gravity.CENTER;
        layoutParams.setMargins(0, btnSize + smallCushion, 0, btnEdgeMargin);
        playBtn.setLayoutParams(layoutParams);
        playBtn.setBackground(ContextCompat.getDrawable(context, isPlaying ? R.drawable.baseline_pause_24 : R.drawable.baseline_play_arrow_24));
        playBtn.setOnClickListener((btn) -> fireMediaBtnEvent(isPlaying ? MediaButton.pause : MediaButton.play));
        controlsBar.addView(playBtn);

        seekFwd = new Button(context);
        layoutParams = new LayoutParams(btnSize, btnSize);
        layoutParams.gravity = Gravity.CENTER;
        layoutParams.setMargins(btnSize + smallCushion, btnSize + smallCushion, 0,btnEdgeMargin);
        seekFwd.setLayoutParams(layoutParams);
        seekFwd.setBackground(ContextCompat.getDrawable(context, R.drawable.baseline_fast_forward_24));
        seekFwd.setOnClickListener((btn) -> fireMediaBtnEvent(MediaButton.seekFwd));
        controlsBar.addView(seekFwd);

        skipFwd = new Button(context);
        layoutParams = new LayoutParams(btnSize, btnSize);
        layoutParams.gravity = Gravity.CENTER;
        layoutParams.setMargins((btnSize + smallCushion) * 2, btnSize + smallCushion, 0,btnEdgeMargin);
        skipFwd.setLayoutParams(layoutParams);
        skipFwd.setBackground(ContextCompat.getDrawable(context, R.drawable.baseline_skip_next_24));
        skipFwd.setOnClickListener((btn) -> fireMediaBtnEvent(MediaButton.skipNext));
        controlsBar.addView(skipFwd);

        seekBck = new Button(context);
        layoutParams = new LayoutParams(btnSize, btnSize);
        layoutParams.gravity = Gravity.CENTER;
        layoutParams.setMargins(0, btnSize + smallCushion, btnSize + smallCushion,btnEdgeMargin);
        seekBck.setLayoutParams(layoutParams);
        seekBck.setBackground(ContextCompat.getDrawable(context, R.drawable.baseline_fast_rewind_24));
        seekBck.setOnClickListener((btn) -> fireMediaBtnEvent(MediaButton.seekBck));
        controlsBar.addView(seekBck);

        skipPrv = new Button(context);
        layoutParams = new LayoutParams(btnSize, btnSize);
        layoutParams.gravity = Gravity.CENTER;
        layoutParams.setMargins(0, btnSize + smallCushion, (btnSize + smallCushion) * 2,btnEdgeMargin);
        skipPrv.setLayoutParams(layoutParams);
        skipPrv.setBackground(ContextCompat.getDrawable(context, R.drawable.baseline_skip_previous_24));
        skipPrv.setOnClickListener((btn) -> fireMediaBtnEvent(MediaButton.skipPrev));
        controlsBar.addView(skipPrv);

        // create a Spinner widget
        seekBar = new Slider(context);
        LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, btnSize);
        params.gravity = Gravity.CENTER;
        params.setMargins(btnEdgeMargin, btnEdgeMargin, btnEdgeMargin, btnSize + smallCushion);
        seekBar.setLayoutParams(params);
        seekBar.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {
            }

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                //Log.d("DynamicInputItemView", "onStopTrackingTouch: " + slider.getValue());
                fireSeekBarEvent(slider.getValue());
            }
        });
        controlsBar.addView(seekBar);
    }
}
