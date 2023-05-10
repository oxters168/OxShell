package com.OxGames.OxShell.Views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
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

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.core.content.ContextCompat;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.ui.PlayerView;

import com.OxGames.OxShell.Data.KeyComboAction;
import com.OxGames.OxShell.Data.SettingsKeeper;
import com.OxGames.OxShell.Helpers.AndroidHelpers;
import com.OxGames.OxShell.Helpers.InputHandler;
import com.OxGames.OxShell.Helpers.MathHelpers;
import com.OxGames.OxShell.OxShellApp;
import com.OxGames.OxShell.R;
import com.google.android.material.slider.LabelFormatter;
import com.google.android.material.slider.Slider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class MediaPlayerView extends FrameLayout {
    private static final String INPUT_TAG = "MEDIA_PLAYER";
    public enum MediaButton { back, end, play, pause, seekFwd, seekBck, skipNext, skipPrev, fullscreen }
    private final Context context;
//    private FrameLayout imageBackdrop;
//    private FrameLayout imageView;
    private PlayerView playerView;
    private FrameLayout customActionBar;
    private FrameLayout controlsBar;
    private BetterTextView titleLabel;
    private BetterTextView currentTimeLabel;
    private BetterTextView totalTimeLabel;
    private Button backBtn;
    private Button endBtn;
    private Button playBtn;
    private Button seekFwdBtn;
    private Button skipFwdBtn;
    private Button seekBckBtn;
    private Button skipPrvBtn;
    private Button fullscreenBtn;
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
        //playBtn.setBackground(ContextCompat.getDrawable(context, isPlaying ? R.drawable.baseline_pause_24 : R.drawable.baseline_play_arrow_24));
        setBgStates(playBtn, isPlaying ? R.drawable.baseline_pause_24 : R.drawable.baseline_play_arrow_24, Color.WHITE, Color.RED);
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
        currentTimeLabel.setText(MathHelpers.msToTimestamp(ms));
    }
    public void setCurrentDuration(long ms) {
        totalTimeLabel.setText(MathHelpers.msToTimestamp(ms));
    }
//    public boolean getVideoMode() {
//        return isVideoMode;
//    }
//    public void setVideoMode(boolean onOff) {
//        isVideoMode = onOff;
//        playerView.setVisibility(isVideoMode ? VISIBLE : GONE);
//        imageBackdrop.setVisibility(isVideoMode ? GONE : VISIBLE);
//    }
    public PlayerView getPlayerView() {
        return playerView;
    }

    @SuppressLint("ClickableViewAccessibility")
    public void onDestroy() {
        backBtn.setOnClickListener(null);
        backBtn.setOnFocusChangeListener(null);
        endBtn.setOnClickListener(null);
        playBtn.setOnClickListener(null);
        seekFwdBtn.setOnClickListener(null);
        skipFwdBtn.setOnClickListener(null);
        seekBckBtn.setOnClickListener(null);
        skipPrvBtn.setOnClickListener(null);
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

//    public void setImage(Drawable drawable) {
//        if (drawable == null) {
//            imageView.setBackground(ContextCompat.getDrawable(context, R.drawable.ic_baseline_headphones_24));
//            imageView.setBackgroundTintList(ColorStateList.valueOf(Color.DKGRAY));
//        } else {
//            imageView.setBackground(drawable);
//            imageView.setBackgroundTintList(null);
//        }
//    }
    @OptIn(markerClass = UnstableApi.class)
    public void refreshArtworkSize() {
        // because for some reason the cover art gets stretched incorrectly
        // sometimes on first time loading the view. And even when called
        // on play or on media changed, it sometimes does not work first time
        // so I had to put it in the seek bar position setter code
        playerView.setUseArtwork(false);
        playerView.setUseArtwork(true);
    }
    public void setFullscreen(boolean onOff) {
        boolean fullscreenChanged = isFullscreen != onOff;
        isFullscreen = onOff;
        customActionBar.setVisibility(isFullscreen ? GONE : VISIBLE);
        controlsBar.setVisibility(isFullscreen ? GONE : VISIBLE);
        refreshArtworkSize();
        if (fullscreenChanged)
            fireMediaBtnEvent(MediaButton.fullscreen);
    }
    public boolean isFullscreen() {
        return isFullscreen;
    }
    public void setBackBtnVisible(boolean onOff) {
        backBtn.setVisibility(onOff ? VISIBLE : GONE);
    }

    public void onResume() {
        InputHandler.clearKeyComboActions(INPUT_TAG);
        InputHandler.addKeyComboActions(INPUT_TAG, Arrays.stream(SettingsKeeper.getNavigateLeft()).map(combo -> new KeyComboAction(combo, () -> {
            if (!isFullscreen()) {
                View currentFocused = getCurrentFocusedView();
                if (currentFocused == null)
                    currentFocused = endBtn;
                if (currentFocused != seekBar) {
                    currentFocused.clearFocus();
                    getViewLeftOf(currentFocused).requestFocus();
                } else
                    seekBckBtn.performClick();
            } else
                setFullscreen(false);
        })).toArray(KeyComboAction[]::new));
        InputHandler.addKeyComboActions(INPUT_TAG, Arrays.stream(SettingsKeeper.getNavigateRight()).map(combo -> new KeyComboAction(combo, () -> {
            if (!isFullscreen()) {
                View currentFocused = getCurrentFocusedView();
                if (currentFocused == null)
                    currentFocused = backBtn;
                if (currentFocused != seekBar) {
                    currentFocused.clearFocus();
                    getViewRightOf(currentFocused).requestFocus();
                } else
                    seekFwdBtn.performClick();
            } else
                setFullscreen(false);
        })).toArray(KeyComboAction[]::new));
        InputHandler.addKeyComboActions(INPUT_TAG, Arrays.stream(SettingsKeeper.getNavigateUp()).map(combo -> new KeyComboAction(combo, () -> {
            if (!isFullscreen()) {
                View currentFocused = getCurrentFocusedView();
                if (currentFocused == null)
                    currentFocused = playBtn;
                currentFocused.clearFocus();
                getViewAbove(currentFocused).requestFocus();
            } else
                setFullscreen(false);
        })).toArray(KeyComboAction[]::new));
        InputHandler.addKeyComboActions(INPUT_TAG, Arrays.stream(SettingsKeeper.getNavigateDown()).map(combo -> new KeyComboAction(combo, () -> {
            if (!isFullscreen()) {
                View currentFocused = getCurrentFocusedView();
                if (currentFocused == null)
                    currentFocused = backBtn;
                currentFocused.clearFocus();
                getViewBelow(currentFocused).requestFocus();
            } else
                setFullscreen(false);
        })).toArray(KeyComboAction[]::new));
        InputHandler.addKeyComboActions(INPUT_TAG, Arrays.stream(SettingsKeeper.getPrimaryInput()).map(combo -> new KeyComboAction(combo, () -> {
            if (!isFullscreen()) {
                View currentView = getCurrentFocusedView();
                if (currentView != null)
                    currentView.performClick();
            } else
                setFullscreen(false);
        })).toArray(KeyComboAction[]::new));
        InputHandler.addKeyComboActions(INPUT_TAG, Arrays.stream(SettingsKeeper.getMusicPlayerFullscreenInput()).map(combo -> new KeyComboAction(combo, () -> {
            setFullscreen(!isFullscreen());
        })).toArray(KeyComboAction[]::new));
        InputHandler.addKeyComboActions(INPUT_TAG, Arrays.stream(SettingsKeeper.getSecondaryInput()).map(combo -> new KeyComboAction(combo, backBtn::performClick)).toArray(KeyComboAction[]::new));
        InputHandler.addKeyComboActions(INPUT_TAG, Arrays.stream(SettingsKeeper.getCancelInput()).map(combo -> new KeyComboAction(combo, endBtn::performClick)).toArray(KeyComboAction[]::new));
        InputHandler.setTagEnabled(INPUT_TAG, true);
    }
    private View getViewLeftOf(View view) {
        if (view == endBtn)
            return backBtn;
        else if (view == fullscreenBtn)
            return skipFwdBtn;
        else if (view == skipFwdBtn)
            return seekFwdBtn;
        else if (view == seekFwdBtn)
            return playBtn;
        else if (view == playBtn)
            return seekBckBtn;
        else if (view == seekBckBtn)
            return skipPrvBtn;
        else
            return view;
    }
    private View getViewRightOf(View view) {
        if (view == backBtn)
            return endBtn;
        else if (view == skipPrvBtn)
            return seekBckBtn;
        else if (view == seekBckBtn)
            return playBtn;
        else if (view == playBtn)
            return seekFwdBtn;
        else if (view == seekFwdBtn)
            return skipFwdBtn;
        else if (view == skipFwdBtn)
            return fullscreenBtn;
        else
            return view;
    }
    private View getViewAbove(View view) {
        if (view == skipPrvBtn || view == seekBckBtn || view == playBtn || view == seekFwdBtn || view == skipFwdBtn || view == fullscreenBtn)
            return seekBar;
        else if (view == seekBar)
            return backBtn;
        else
            return view;
    }
    private View getViewBelow(View view) {
        if (view == backBtn || view == endBtn)
            return seekBar;
        else if (view == seekBar)
            return playBtn;
        else
            return view;
    }
    private View getCurrentFocusedView() {
        if (backBtn.hasFocus())
            return backBtn;
        else if (endBtn.hasFocus())
            return endBtn;
        else if (seekBar.hasFocus())
            return seekBar;
        else if (skipPrvBtn.hasFocus())
            return skipPrvBtn;
        else if (seekBckBtn.hasFocus())
            return seekBckBtn;
        else if (playBtn.hasFocus())
            return playBtn;
        else if (seekFwdBtn.hasFocus())
            return seekFwdBtn;
        else if (skipFwdBtn.hasFocus())
            return skipFwdBtn;
        else if (fullscreenBtn.hasFocus())
            return fullscreenBtn;
        else
            return null;
    }
    public void onPause() {
        //InputHandler.removeTagFromHistory(INPUT_TAG);
        //InputHandler.clearKeyComboActions(INPUT_TAG);
        InputHandler.setTagEnabled(INPUT_TAG, false);
    }

    private void init() {
        refreshLayouts();
    }
//    @OptIn(markerClass = UnstableApi.class)
//    public void setPlayerViewShown(boolean onOff) {
//        //playerView.setVisibility(onOff ? VISIBLE : GONE);
////        refreshLayouts();
////        playerView.setResizeMode(onOff ? AspectRatioFrameLayout.RESIZE_MODE_FIT : AspectRatioFrameLayout.RESIZE_MODE_ZOOM);
//        playerView.setUseArtwork(onOff);
//    }
//    public void refreshPlayerView() {
//        playerView.refreshDrawableState();
//    }

    public static void setBgStates(View view, @DrawableRes int icon, @ColorInt int normal, @ColorInt int pressed) {
        StateListDrawable stateListDrawable = new StateListDrawable();
        Drawable normalDrawable = ContextCompat.getDrawable(OxShellApp.getContext(), icon);
        normalDrawable.setColorFilter(normal, PorterDuff.Mode.MULTIPLY);
        Drawable selectedDrawable = ContextCompat.getDrawable(OxShellApp.getContext(), icon);
        selectedDrawable.setColorFilter(pressed, PorterDuff.Mode.MULTIPLY);
        stateListDrawable.addState(SELECTED_STATE_SET, selectedDrawable);
        stateListDrawable.addState(FOCUSED_STATE_SET, selectedDrawable);
        stateListDrawable.addState(PRESSED_STATE_SET, selectedDrawable);
        stateListDrawable.addState(ENABLED_SELECTED_STATE_SET, selectedDrawable);
        stateListDrawable.addState(ENABLED_FOCUSED_STATE_SET, selectedDrawable);
        stateListDrawable.addState(ENABLED_FOCUSED_SELECTED_STATE_SET, selectedDrawable);
        stateListDrawable.addState(PRESSED_SELECTED_STATE_SET, selectedDrawable);
        stateListDrawable.addState(PRESSED_FOCUSED_STATE_SET, selectedDrawable);
        stateListDrawable.addState(PRESSED_ENABLED_STATE_SET, selectedDrawable);
        stateListDrawable.addState(PRESSED_ENABLED_FOCUSED_STATE_SET, selectedDrawable);
        stateListDrawable.addState(new int[] {}, normalDrawable);
        view.setBackground(stateListDrawable);
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
        //int imageSize = Math.round(Math.min(OxShellApp.getDisplayWidth(), OxShellApp.getDisplayHeight()) * 0.8f);
        int timeTextSize = Math.round(AndroidHelpers.getScaledSpToPixels(context, 8));

        layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        layoutParams.gravity = Gravity.TOP | Gravity.LEFT;
        setLayoutParams(layoutParams);
        setBackgroundColor(Color.BLACK);
        setOnTouchListener((view, touchEvent) -> {
            //Log.d("MediaPlayerView", "encompassingView: " + touchEvent);
            if (touchEvent.getAction() == MotionEvent.ACTION_UP)
                setFullscreen(!isFullscreen);
            return true;
        });
        setFocusable(false);

//        if (imageBackdrop == null) {
//            imageBackdrop = new FrameLayout(context);
//            imageBackdrop.setBackgroundColor(Color.GRAY);
//            imageBackdrop.setFocusable(false);
//            addView(imageBackdrop);
//        }
//        layoutParams = new LayoutParams(imageSize, imageSize);
//        layoutParams.gravity = Gravity.CENTER;
//        imageBackdrop.setLayoutParams(layoutParams);
//
//        if (imageView == null) {
//            imageView = new FrameLayout(context);
//            imageView.setFocusable(false);
//            imageBackdrop.addView(imageView);
//        }
//        layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
//        layoutParams.gravity = Gravity.CENTER;
//        imageView.setLayoutParams(layoutParams);

        if (playerView == null) {
            playerView = new PlayerView(context);
            playerView.setUseController(false);
            //playerView.setVisibility(GONE);
            //surfaceView.setFocusable(false);
            addView(playerView);
        }
        //playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
        layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        layoutParams.gravity = Gravity.CENTER;
        playerView.setLayoutParams(layoutParams);

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
            //backBtn.setBackground(ContextCompat.getDrawable(context, R.drawable.baseline_arrow_back_24));
            setBgStates(backBtn, R.drawable.baseline_arrow_back_24, Color.WHITE, Color.RED);
            backBtn.setOnClickListener((btn) -> fireMediaBtnEvent(MediaButton.back));
            customActionBar.addView(backBtn);
        }
        layoutParams = new LayoutParams(btnSize, btnSize);
        layoutParams.gravity = Gravity.START | Gravity.CENTER_VERTICAL;
        layoutParams.setMarginStart(btnEdgeMargin);
        backBtn.setLayoutParams(layoutParams);

        if (endBtn == null) {
            endBtn = new Button(context);
            //endBtn.setBackground(ContextCompat.getDrawable(context, R.drawable.baseline_close_24));
            setBgStates(endBtn, R.drawable.baseline_close_24, Color.WHITE, Color.RED);
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
            //playBtn.setBackground(ContextCompat.getDrawable(context, isPlaying ? R.drawable.baseline_pause_24 : R.drawable.baseline_play_arrow_24));
            setBgStates(playBtn, isPlaying ? R.drawable.baseline_pause_24 : R.drawable.baseline_play_arrow_24, Color.WHITE, Color.RED);
            playBtn.setOnClickListener((btn) -> fireMediaBtnEvent(isPlaying ? MediaButton.pause : MediaButton.play));
            controlsBar.addView(playBtn);
            //InputHandler.addKeyComboActions(INPUT_TAG, Arrays.stream(SettingsKeeper.getSuperPrimaryInput()).map(combo -> new KeyComboAction(combo, playBtn::performClick)).toArray(KeyComboAction[]::new));
        }
        layoutParams = new LayoutParams(btnSize, btnSize);
        layoutParams.gravity = Gravity.CENTER;
        layoutParams.setMargins(0, controlsSeparationMargin, 0, btnEdgeMargin);
        playBtn.setLayoutParams(layoutParams);

        if (seekFwdBtn == null) {
            seekFwdBtn = new Button(context);
            //seekFwd.setBackground(ContextCompat.getDrawable(context, R.drawable.baseline_fast_forward_24));
            setBgStates(seekFwdBtn, R.drawable.baseline_fast_forward_24, Color.WHITE, Color.RED);
            seekFwdBtn.setOnClickListener((btn) -> fireMediaBtnEvent(MediaButton.seekFwd));
            controlsBar.addView(seekFwdBtn);
            //InputHandler.addKeyComboActions(INPUT_TAG, Arrays.stream(SettingsKeeper.getNavigateRight()).map(combo -> new KeyComboAction(combo, seekFwd::performClick)).toArray(KeyComboAction[]::new));
        }
        layoutParams = new LayoutParams(btnSize, btnSize);
        layoutParams.gravity = Gravity.CENTER;
        layoutParams.setMargins(btnSize + medCushion, controlsSeparationMargin, 0, btnEdgeMargin);
        seekFwdBtn.setLayoutParams(layoutParams);

        if (skipFwdBtn == null) {
            skipFwdBtn = new Button(context);
            //skipFwd.setBackground(ContextCompat.getDrawable(context, R.drawable.baseline_skip_next_24));
            setBgStates(skipFwdBtn, R.drawable.baseline_skip_next_24, Color.WHITE, Color.RED);
            skipFwdBtn.setOnClickListener((btn) -> fireMediaBtnEvent(MediaButton.skipNext));
            controlsBar.addView(skipFwdBtn);
        }
        layoutParams = new LayoutParams(btnSize, btnSize);
        layoutParams.gravity = Gravity.CENTER;
        layoutParams.setMargins((btnSize + medCushion) * 2, controlsSeparationMargin, 0, btnEdgeMargin);
        skipFwdBtn.setLayoutParams(layoutParams);

        if (seekBckBtn == null) {
            seekBckBtn = new Button(context);
            //seekBck.setBackground(ContextCompat.getDrawable(context, R.drawable.baseline_fast_rewind_24));
            setBgStates(seekBckBtn, R.drawable.baseline_fast_rewind_24, Color.WHITE, Color.RED);
            seekBckBtn.setOnClickListener((btn) -> fireMediaBtnEvent(MediaButton.seekBck));
            controlsBar.addView(seekBckBtn);
            //InputHandler.addKeyComboActions(INPUT_TAG, Arrays.stream(SettingsKeeper.getNavigateLeft()).map(combo -> new KeyComboAction(combo, seekBck::performClick)).toArray(KeyComboAction[]::new));
        }
        layoutParams = new LayoutParams(btnSize, btnSize);
        layoutParams.gravity = Gravity.CENTER;
        layoutParams.setMargins(0, controlsSeparationMargin, btnSize + medCushion, btnEdgeMargin);
        seekBckBtn.setLayoutParams(layoutParams);

        if (skipPrvBtn == null) {
            skipPrvBtn = new Button(context);
            //skipPrv.setBackground(ContextCompat.getDrawable(context, R.drawable.baseline_skip_previous_24));
            setBgStates(skipPrvBtn, R.drawable.baseline_skip_previous_24, Color.WHITE, Color.RED);
            skipPrvBtn.setOnClickListener((btn) -> fireMediaBtnEvent(MediaButton.skipPrev));
            controlsBar.addView(skipPrvBtn);
        }
        layoutParams = new LayoutParams(btnSize, btnSize);
        layoutParams.gravity = Gravity.CENTER;
        layoutParams.setMargins(0, controlsSeparationMargin, (btnSize + medCushion) * 2, btnEdgeMargin);
        skipPrvBtn.setLayoutParams(layoutParams);

        if (fullscreenBtn == null) {
            fullscreenBtn = new Button(context);
            //skipPrv.setBackground(ContextCompat.getDrawable(context, R.drawable.baseline_skip_previous_24));
            setBgStates(fullscreenBtn, R.drawable.baseline_fullscreen_24, Color.WHITE, Color.RED);
            fullscreenBtn.setOnClickListener((btn) -> setFullscreen(true));
            controlsBar.addView(fullscreenBtn);
        }
        layoutParams = new LayoutParams(btnSize, btnSize);
        layoutParams.gravity = Gravity.RIGHT | Gravity.CENTER_VERTICAL;
        layoutParams.setMargins(0, controlsSeparationMargin, btnEdgeMargin, btnEdgeMargin);
        fullscreenBtn.setLayoutParams(layoutParams);

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
            currentTimeLabel.setText("0:000:00:00:00");
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
            totalTimeLabel.setText("0:000:00:00:00");
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

        //setVideoMode(isVideoMode);
    }
}
