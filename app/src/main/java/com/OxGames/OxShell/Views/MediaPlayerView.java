package com.OxGames.OxShell.Views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.OxGames.OxShell.Data.SettingsKeeper;
import com.OxGames.OxShell.Helpers.AndroidHelpers;
import com.OxGames.OxShell.Helpers.MathHelpers;
import com.OxGames.OxShell.OxShellApp;
import com.OxGames.OxShell.R;
import com.google.android.material.slider.LabelFormatter;
import com.google.android.material.slider.Slider;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class MediaPlayerView extends FrameLayout {
    private static final long MS_PER_SEC = 1000L;
    private static final long MS_PER_MIN = MS_PER_SEC * 60;
    private static final long MS_PER_HR = MS_PER_MIN * 60;
    private static final long MS_PER_DAY = MS_PER_HR * 24;
    private static final long MS_PER_YEAR = MS_PER_DAY * 365;
    public enum MediaButton { back, end, play, pause, seekFwd, seekBck, skipNext, skipPrev, fullscreen }
    private final Context context;
    private FrameLayout imageBackdrop;
    private FrameLayout imageView;
    private FrameLayout customActionBar;
    private FrameLayout controlsBar;
    private BetterTextView titleLabel;
    private BetterTextView currentTimeLabel;
    private BetterTextView totalTimeLabel;
    private Button backBtn;
    private Button endBtn;
    private Button playBtn;
    private Button seekFwd;
    private Button skipFwd;
    private Button seekBck;
    private Button skipPrv;
    private Slider seekBar;

    private boolean isPlaying;
    private boolean isSeeking;
    private boolean isFullscreen;

    private final List<Consumer<MediaButton>> mediaBtnListeners;
    private final List<Consumer<Float>> seekBarListeners;

    public MediaPlayerView(@NonNull Context context) {
        super(context);
        this.context = context;
        mediaBtnListeners = new ArrayList<>();
        seekBarListeners = new ArrayList<>();
        refreshLayouts();
    }
    public MediaPlayerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        mediaBtnListeners = new ArrayList<>();
        seekBarListeners = new ArrayList<>();
        refreshLayouts();
    }
    public MediaPlayerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        mediaBtnListeners = new ArrayList<>();
        seekBarListeners = new ArrayList<>();
        refreshLayouts();
    }
    public MediaPlayerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.context = context;
        mediaBtnListeners = new ArrayList<>();
        seekBarListeners = new ArrayList<>();
        refreshLayouts();
    }

    public void setIsPlaying(boolean onOff) {
        isPlaying = onOff;
        playBtn.setBackground(ContextCompat.getDrawable(context, isPlaying ? R.drawable.baseline_pause_24 : R.drawable.baseline_play_arrow_24));
    }
    public void setTitle(String value) {
        titleLabel.setText(value);
    }
    public void setSeekBarPosition(float value) {
        if (!isSeeking)
            seekBar.setValue(MathHelpers.clamp(value, seekBar.getValueFrom(), seekBar.getValueTo()));
        else
            Log.w("MediaPlayerView", "Failed to set seek bar value since it is being manipulated");
    }
    public void setCurrentTime(long ms) {
        currentTimeLabel.setText(msToTimestamp(ms));
    }
    public void setCurrentDuration(long ms) {
        totalTimeLabel.setText(msToTimestamp(ms));
    }
    private static String msToTimestamp(long ms) {
        long totalTime = ms;
        long years = totalTime / MS_PER_YEAR;
        totalTime %= MS_PER_YEAR;
        long days = totalTime / MS_PER_DAY;
        totalTime %= MS_PER_DAY;
        long hours = totalTime / MS_PER_HR;
        totalTime %= MS_PER_HR;
        long minutes = totalTime / MS_PER_MIN;
        totalTime %= MS_PER_MIN;
        long seconds = totalTime / MS_PER_SEC;
        return (years > 0 ? years + ":" : "") + (days > 0 ? (days < 10 ? "00" : (days < 100 ? "0" : "")) + days + ":" : "") + (hours > 0 ? (hours < 10 ? "0" : "") + hours + ":" : "") + (minutes < 10 ? "0" : "") + minutes + ":" + (seconds < 10 ? "0" : "") + seconds;
    }
    @SuppressLint("ClickableViewAccessibility")
    public void onDestroy() {
        backBtn.setOnClickListener(null);
        endBtn.setOnClickListener(null);
        playBtn.setOnClickListener(null);
        seekFwd.setOnClickListener(null);
        skipFwd.setOnClickListener(null);
        seekBck.setOnClickListener(null);
        skipPrv.setOnClickListener(null);
        seekBar.clearOnSliderTouchListeners();
        setOnTouchListener(null);
        customActionBar.setOnTouchListener(null);
        controlsBar.setOnTouchListener(null);
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

    public void setImage(Drawable drawable) {
        if (drawable == null) {
            imageView.setBackground(ContextCompat.getDrawable(context, R.drawable.ic_baseline_headphones_24));
            imageView.setBackgroundTintList(ColorStateList.valueOf(Color.DKGRAY));
        } else {
            imageView.setBackground(drawable);
            imageView.setBackgroundTintList(null);
        }
    }
    public void setFullscreen(boolean onOff) {
        boolean fullscreenChanged = isFullscreen != onOff;
        isFullscreen = onOff;
        customActionBar.setVisibility(isFullscreen ? GONE : VISIBLE);
        controlsBar.setVisibility(isFullscreen ? GONE : VISIBLE);
        if (fullscreenChanged)
            fireMediaBtnEvent(MediaButton.fullscreen);
    }
    public boolean isFullscreen() {
        return isFullscreen;
    }

    @SuppressLint("ClickableViewAccessibility")
    public void refreshLayouts() {
        LayoutParams layoutParams;

        int actionBarHeight = Math.round(AndroidHelpers.getScaledDpToPixels(context, 64));
        int textOutlineSize = Math.round(AndroidHelpers.getScaledDpToPixels(context, 3));
        int titleTextSize = Math.round(AndroidHelpers.getScaledSpToPixels(context, 16));
        int medCushion = Math.round(AndroidHelpers.getScaledDpToPixels(context, 16));
        int smallCushion = Math.round(AndroidHelpers.getScaledDpToPixels(context, 8));
        int btnSize = Math.round(AndroidHelpers.getScaledDpToPixels(context, 32));
        int seekBarThumbSize = Math.round(AndroidHelpers.getScaledDpToPixels(context, 5));
        int btnEdgeMargin = (actionBarHeight - btnSize) / 2;
        int controlsSeparationMargin = btnSize + medCushion / 2;
        int imageSize = Math.round(Math.min(OxShellApp.getDisplayWidth(), OxShellApp.getDisplayHeight()) * 0.8f);
        int timeTextSize = Math.round(AndroidHelpers.getScaledSpToPixels(context, 8));

        layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        layoutParams.gravity = Gravity.TOP | Gravity.LEFT;
        setLayoutParams(layoutParams);
        setBackgroundColor(Color.DKGRAY);
        setOnTouchListener((view, touchEvent) -> {
            //Log.d("MediaPlayerView", "encompassingView: " + touchEvent);
            if (touchEvent.getAction() == MotionEvent.ACTION_UP)
                setFullscreen(!isFullscreen);
            return true;
        });
        setFocusable(false);

        if (imageBackdrop == null) {
            imageBackdrop = new FrameLayout(context);
            imageBackdrop.setBackgroundColor(Color.GRAY);
            imageBackdrop.setFocusable(false);
            addView(imageBackdrop);
        }
        layoutParams = new LayoutParams(imageSize, imageSize);
        layoutParams.gravity = Gravity.CENTER;
        imageBackdrop.setLayoutParams(layoutParams);

        if (imageView == null) {
            imageView = new FrameLayout(context);
            imageView.setFocusable(false);
            imageBackdrop.addView(imageView);
        }
        layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        layoutParams.gravity = Gravity.CENTER;
        imageView.setLayoutParams(layoutParams);

        if (customActionBar == null) {
            customActionBar = new FrameLayout(context);
            customActionBar.setBackgroundColor(Color.parseColor("#BB323232"));
            customActionBar.setFocusable(false);
            customActionBar.setOnTouchListener((view, touchEvent) -> true);
            addView(customActionBar);
        }
        layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, actionBarHeight);
        layoutParams.gravity = Gravity.TOP | Gravity.LEFT;
        customActionBar.setLayoutParams(layoutParams);

        if (titleLabel == null) {
            titleLabel = new BetterTextView(context);
            titleLabel.setIgnoreTouchInput(true);
            titleLabel.setOverScrollMode(SCROLL_AXIS_VERTICAL);
            titleLabel.setMovementMethod(new ScrollingMovementMethod());
            titleLabel.setSingleLine(true);
            titleLabel.setEllipsize(TextUtils.TruncateAt.MARQUEE);
            titleLabel.setMarqueeRepeatLimit(-1);
            titleLabel.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
            titleLabel.setTextAlignment(View.TEXT_ALIGNMENT_GRAVITY);
            titleLabel.setTextColor(context.getColor(R.color.text));
            titleLabel.setOutlineColor(Color.BLACK);
            titleLabel.setOutlineSize(textOutlineSize);
            titleLabel.setTextSize(titleTextSize);
            titleLabel.setText("Title");
            titleLabel.setFocusable(false);
            titleLabel.setTypeface(SettingsKeeper.getFont());
            titleLabel.setSelected(true);
            customActionBar.addView(titleLabel);
        }
        layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        layoutParams.gravity = Gravity.START | Gravity.CENTER_VERTICAL;
        layoutParams.setMarginStart(btnSize + btnEdgeMargin + medCushion);
        layoutParams.setMarginEnd(btnSize + btnEdgeMargin * 3 + medCushion);
        titleLabel.setLayoutParams(layoutParams);

        if (backBtn == null) {
            backBtn = new Button(context);
            backBtn.setBackground(ContextCompat.getDrawable(context, R.drawable.baseline_arrow_back_24));
            backBtn.setOnClickListener((btn) -> fireMediaBtnEvent(MediaButton.back));
            customActionBar.addView(backBtn);
        }
        layoutParams = new LayoutParams(btnSize, btnSize);
        layoutParams.gravity = Gravity.START | Gravity.CENTER_VERTICAL;
        layoutParams.setMarginStart(btnEdgeMargin);
        backBtn.setLayoutParams(layoutParams);

        if (endBtn == null) {
            endBtn = new Button(context);
            endBtn.setBackground(ContextCompat.getDrawable(context, R.drawable.baseline_close_24));
            endBtn.setOnClickListener((btn) -> fireMediaBtnEvent(MediaButton.end));
            customActionBar.addView(endBtn);
        }
        layoutParams = new LayoutParams(btnSize, btnSize);
        layoutParams.gravity = Gravity.END | Gravity.CENTER_VERTICAL;
        layoutParams.setMarginEnd(btnEdgeMargin * 3);
        endBtn.setLayoutParams(layoutParams);

        if (controlsBar == null) {
            controlsBar = new FrameLayout(context);
            controlsBar.setBackgroundColor(Color.parseColor("#BB323232"));
            controlsBar.setFocusable(false);
            controlsBar.setOnTouchListener((view, touchEvent) -> true);
            addView(controlsBar);
        }
        layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, actionBarHeight * 2 + medCushion);
        layoutParams.gravity = Gravity.LEFT | Gravity.BOTTOM;
        controlsBar.setLayoutParams(layoutParams);

        if (playBtn == null) {
            playBtn = new Button(context);
            playBtn.setBackground(ContextCompat.getDrawable(context, isPlaying ? R.drawable.baseline_pause_24 : R.drawable.baseline_play_arrow_24));
            playBtn.setOnClickListener((btn) -> fireMediaBtnEvent(isPlaying ? MediaButton.pause : MediaButton.play));
            controlsBar.addView(playBtn);
        }
        layoutParams = new LayoutParams(btnSize, btnSize);
        layoutParams.gravity = Gravity.CENTER;
        layoutParams.setMargins(0, controlsSeparationMargin, 0, btnEdgeMargin);
        playBtn.setLayoutParams(layoutParams);

        if (seekFwd == null) {
            seekFwd = new Button(context);
            seekFwd.setBackground(ContextCompat.getDrawable(context, R.drawable.baseline_fast_forward_24));
            seekFwd.setOnClickListener((btn) -> fireMediaBtnEvent(MediaButton.seekFwd));
            controlsBar.addView(seekFwd);
        }
        layoutParams = new LayoutParams(btnSize, btnSize);
        layoutParams.gravity = Gravity.CENTER;
        layoutParams.setMargins(btnSize + medCushion, controlsSeparationMargin, 0,btnEdgeMargin);
        seekFwd.setLayoutParams(layoutParams);

        if (skipFwd == null) {
            skipFwd = new Button(context);
            skipFwd.setBackground(ContextCompat.getDrawable(context, R.drawable.baseline_skip_next_24));
            skipFwd.setOnClickListener((btn) -> fireMediaBtnEvent(MediaButton.skipNext));
            controlsBar.addView(skipFwd);
        }
        layoutParams = new LayoutParams(btnSize, btnSize);
        layoutParams.gravity = Gravity.CENTER;
        layoutParams.setMargins((btnSize + medCushion) * 2, controlsSeparationMargin, 0,btnEdgeMargin);
        skipFwd.setLayoutParams(layoutParams);

        if (seekBck == null) {
            seekBck = new Button(context);
            seekBck.setBackground(ContextCompat.getDrawable(context, R.drawable.baseline_fast_rewind_24));
            seekBck.setOnClickListener((btn) -> fireMediaBtnEvent(MediaButton.seekBck));
            controlsBar.addView(seekBck);
        }
        layoutParams = new LayoutParams(btnSize, btnSize);
        layoutParams.gravity = Gravity.CENTER;
        layoutParams.setMargins(0, controlsSeparationMargin, btnSize + medCushion,btnEdgeMargin);
        seekBck.setLayoutParams(layoutParams);

        if (skipPrv == null) {
            skipPrv = new Button(context);
            skipPrv.setBackground(ContextCompat.getDrawable(context, R.drawable.baseline_skip_previous_24));
            skipPrv.setOnClickListener((btn) -> fireMediaBtnEvent(MediaButton.skipPrev));
            controlsBar.addView(skipPrv);
        }
        layoutParams = new LayoutParams(btnSize, btnSize);
        layoutParams.gravity = Gravity.CENTER;
        layoutParams.setMargins(0, controlsSeparationMargin, (btnSize + medCushion) * 2,btnEdgeMargin);
        skipPrv.setLayoutParams(layoutParams);

        if (currentTimeLabel == null) {
            currentTimeLabel = new BetterTextView(context);
            currentTimeLabel.setIgnoreTouchInput(true);
            currentTimeLabel.setSingleLine(true);
            currentTimeLabel.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
            currentTimeLabel.setTextAlignment(View.TEXT_ALIGNMENT_GRAVITY);
            currentTimeLabel.setTextColor(context.getColor(R.color.text));
            currentTimeLabel.setOutlineColor(Color.BLACK);
            currentTimeLabel.setOutlineSize(textOutlineSize);
            currentTimeLabel.setTextSize(timeTextSize);
            currentTimeLabel.setText("00:00:00");
            currentTimeLabel.setFocusable(false);
            currentTimeLabel.setTypeface(SettingsKeeper.getFont());
            currentTimeLabel.setSelected(true);
            controlsBar.addView(currentTimeLabel);
        }
        layoutParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, btnSize);
        layoutParams.gravity = Gravity.START | Gravity.CENTER_VERTICAL;
        layoutParams.setMargins(0, btnEdgeMargin, 0, controlsSeparationMargin);
        layoutParams.setMarginStart(btnEdgeMargin);
        currentTimeLabel.setLayoutParams(layoutParams);

        if (totalTimeLabel == null) {
            totalTimeLabel = new BetterTextView(context);
            totalTimeLabel.setIgnoreTouchInput(true);
            totalTimeLabel.setSingleLine(true);
            totalTimeLabel.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
            totalTimeLabel.setTextAlignment(View.TEXT_ALIGNMENT_GRAVITY);
            totalTimeLabel.setTextColor(context.getColor(R.color.text));
            totalTimeLabel.setOutlineColor(Color.BLACK);
            totalTimeLabel.setOutlineSize(textOutlineSize);
            totalTimeLabel.setTextSize(timeTextSize);
            totalTimeLabel.setText("00:00:00");
            totalTimeLabel.setFocusable(false);
            totalTimeLabel.setTypeface(SettingsKeeper.getFont());
            totalTimeLabel.setSelected(true);
            controlsBar.addView(totalTimeLabel);
        }
        layoutParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, btnSize);
        layoutParams.gravity = Gravity.END | Gravity.CENTER_VERTICAL;
        layoutParams.setMargins(0, btnEdgeMargin, 0, controlsSeparationMargin);
        layoutParams.setMarginEnd(btnEdgeMargin);
        totalTimeLabel.setLayoutParams(layoutParams);

        if (seekBar == null) {
            seekBar = new Slider(context);
            seekBar.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
                @Override
                public void onStartTrackingTouch(@NonNull Slider slider) {
                    isSeeking = true;
                }

                @Override
                public void onStopTrackingTouch(@NonNull Slider slider) {
                    isSeeking = false;
                    fireSeekBarEvent(slider.getValue());
                }
            });
            seekBar.setLabelBehavior(LabelFormatter.LABEL_GONE);
            seekBar.setTrackActiveTintList(ColorStateList.valueOf(Color.WHITE));
            seekBar.setThumbTintList(ColorStateList.valueOf(Color.WHITE));
            seekBar.setThumbRadius(seekBarThumbSize);
            seekBar.setHaloRadius(seekBarThumbSize * 2);
            controlsBar.addView(seekBar);
        }
        layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, btnSize);
        layoutParams.gravity = Gravity.CENTER;
        layoutParams.setMargins(0, btnEdgeMargin, 0, controlsSeparationMargin);
        currentTimeLabel.measure(0, 0);
        layoutParams.setMarginStart(btnEdgeMargin + currentTimeLabel.getMeasuredWidth() + smallCushion);
        totalTimeLabel.measure(0, 0);
        layoutParams.setMarginEnd(btnEdgeMargin + totalTimeLabel.getMeasuredWidth() + smallCushion);
        seekBar.setLayoutParams(layoutParams);
    }
}
