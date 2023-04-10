package com.OxGames.OxShell.Views;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.os.Build;
import android.text.method.ScrollingMovementMethod;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.OxGames.OxShell.Data.DataLocation;
import com.OxGames.OxShell.Data.KeyCombo;
import com.OxGames.OxShell.Data.KeyComboAction;
import com.OxGames.OxShell.Data.SettingsKeeper;
import com.OxGames.OxShell.Helpers.AndroidHelpers;
import com.OxGames.OxShell.Helpers.InputHandler;
import com.OxGames.OxShell.Interfaces.InputReceiver;
import com.OxGames.OxShell.OxShellApp;
import com.OxGames.OxShell.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class PromptView extends FrameLayout implements InputReceiver {
    private InputHandler inputHandler;
    private final Context context;
    private ImageView img;
    //private BetterTextView msg;
    private TextView msg;
    private Button startBtn;
    private Button middleBtn;
    private Button endBtn;
    private boolean isImageSet = false;
    private DataLocation imageLoc = DataLocation.none;
    private Object imgData;
    private boolean isMsgSet = false;
    private String message;
    private boolean isStartBtnSet = false;
    private String startBtnTxt;
    private Runnable startBtnAction;
    //private final List<KeyCombo> startBtnKeys = new ArrayList<>();
    private boolean isMiddleBtnSet = false;
    private String middleBtnTxt;
    private Runnable middleBtnAction;
    //private final List<KeyCombo> middleBtnKeys = new ArrayList<>();
    private boolean isEndBtnSet = false;
    private String endBtnTxt;
    private Runnable endBtnAction;
    //private final List<KeyCombo> endBtnKeys = new ArrayList<>();

    private boolean isShown = false;
    private float percentX = 0;
    private float percentY = 0;
    private int chosenWidth = 0;
    private int chosenHeight = 0;

    private static final String INPUT_TAG = "PROMPT_INPUT";

    public PromptView(@NonNull Context context) {
        super(context);
        this.context = context;
        init();
    }
    public PromptView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
    }
    public PromptView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init();
    }

    private int getBtnHeight() {
        return Math.round(chosenHeight * 0.22f);
    }
    private int getImageSize() {
        return Math.round(chosenHeight * 0.33f);
    }
    private int getBorderMargin() {
        return Math.round(AndroidHelpers.getScaledDpToPixels(context, 8));
    }
    private int getTextSize() {
        return Math.round(AndroidHelpers.getScaledSpToPixels(context, 8));
    }
    private int getBtnTextSize() {
        return Math.round(AndroidHelpers.getScaledSpToPixels(context, 5));
    }

    private void init() {
        isShown = false;
        inputHandler = new InputHandler();
        setVisibility(GONE);


        LayoutParams layoutParams;

        layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        layoutParams.gravity = Gravity.TOP | Gravity.LEFT;
        setLayoutParams(layoutParams);
        setSize(getDefaultWidth(), getDefaultHeight());
        setBackground(ContextCompat.getDrawable(context, R.drawable.rounded_outline_shape));
        setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#BB323232")));
        setFocusable(false);

        int borderMargin = getBorderMargin();
        int imgSize = getImageSize();
        int btnWidth = Math.round((chosenWidth - borderMargin * 4) / 3f); // *4 because left, right, and in between btns
        int btnHeight = getBtnHeight();
        int textSize = getTextSize();
        int textStartMargin = borderMargin * 2 + imgSize;
        int textLowerMargin = borderMargin * 2 + btnHeight;

        img = new ImageView(context);
        layoutParams = new LayoutParams(imgSize, imgSize);
        layoutParams.gravity = Gravity.TOP | Gravity.START;
        layoutParams.setMargins(0, borderMargin, 0, 0);
        layoutParams.setMarginStart(borderMargin);
        img.setLayoutParams(layoutParams);
        img.setBackground(ContextCompat.getDrawable(context, R.drawable.ic_baseline_auto_awesome_24));
        img.setFocusable(false);
        addView(img);

        msg = new TextView(context);
        layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        layoutParams.gravity = Gravity.TOP | Gravity.END;
        layoutParams.setMargins(0, borderMargin, 0, textLowerMargin);
        layoutParams.setMarginStart(textStartMargin);
        layoutParams.setMarginEnd(borderMargin);
        msg.setLayoutParams(layoutParams);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Create thumb drawable
            ShapeDrawable thumbDrawable = new ShapeDrawable();
            thumbDrawable.setShape(new RectShape());
            thumbDrawable.getPaint().setColor(Color.WHITE);
            thumbDrawable.setIntrinsicWidth(8);
            thumbDrawable.setIntrinsicHeight(8);
            msg.setVerticalScrollbarThumbDrawable(thumbDrawable);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Create track drawable
            ShapeDrawable trackDrawable = new ShapeDrawable();
            trackDrawable.setShape(new RectShape());
            trackDrawable.getPaint().setColor(Color.DKGRAY);
            trackDrawable.setIntrinsicWidth(8);
            trackDrawable.setIntrinsicHeight(8);
            msg.setVerticalScrollbarTrackDrawable(trackDrawable);
        }
        msg.setVerticalScrollBarEnabled(true);
        msg.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        msg.setScrollbarFadingEnabled(false);
        msg.setOverScrollMode(SCROLL_AXIS_VERTICAL);
        msg.setMovementMethod(new ScrollingMovementMethod());
        //msg.setEllipsize(TextUtils.TruncateAt.END);
        msg.setGravity(Gravity.TOP | Gravity.START);
        msg.setTextAlignment(View.TEXT_ALIGNMENT_GRAVITY);
        msg.setTextColor(context.getColor(R.color.text));
        msg.setTextSize(textSize);
        msg.setText("Quos voluptas commodi maxime dolore eveniet enim commodi et. Et qui nobis est earum eum. Excepturi quis nostrum consectetur ipsum debitis nihil autem. Vitae maiores ducimus et aut voluptas. Est ipsa aliquam quibusdam id atque. Veritatis nisi non minus quo aut. Qui voluptate eos nihil dolores aut. Atque debitis quidem similique molestias perferendis eum numquam qui. Necessitatibus hic quia nulla minus occaecati occaecati est. Unde qui culpa distinctio ea repellat omnis cumque voluptatibus. Vel ut non iste. Numquam ut est temporibus eveniet et exercitationem maxime. Adipisci rerum magnam ipsa laudantium dolores. Vitae ea rem dicta molestiae ut rerum placeat. Repellat fugiat et quo corporis culpa facilis quia. Vel et rerum doloribus porro reiciendis est aut. Illum nihil non et molestiae nostrum. Molestiae dolor cupiditate a numquam adipisci nobis. Rerum saepe libero doloribus incidunt sunt molestias explicabo. Error inventore libero quam nostrum voluptates minima corporis voluptatem. Culpa illum vel ut qui aut in. Eligendi perferendis pariatur dolorum reiciendis sit. Ut et labore magnam quas debitis. Autem et enim enim quia nam voluptatibus illo.");
        msg.setFocusable(false);
        Typeface font = SettingsKeeper.getFont();
        msg.setTypeface(font);
        addView(msg);

        startBtn = new Button(context);
        layoutParams = new LayoutParams(btnWidth, btnHeight);
        layoutParams.gravity = Gravity.BOTTOM | Gravity.START;
        layoutParams.setMargins(0, 0, 0, borderMargin);
        layoutParams.setMarginStart(borderMargin);
        startBtn.setLayoutParams(layoutParams);
        startBtn.setOnClickListener(v -> {
            if (startBtnAction != null)
                startBtnAction.run();
        });
        setBtnLook(startBtn);
        addView(startBtn);

        middleBtn = new Button(context);
        layoutParams = new LayoutParams(btnWidth, btnHeight);
        layoutParams.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        layoutParams.setMargins(0, 0, 0, borderMargin);
        middleBtn.setLayoutParams(layoutParams);
        middleBtn.setBackground(ContextCompat.getDrawable(context, R.drawable.rounded_outline_shape));
        middleBtn.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#484848")));
        middleBtn.setOnClickListener(v -> {
            if (middleBtnAction != null)
                middleBtnAction.run();
        });
        setBtnLook(middleBtn);
        addView(middleBtn);

        endBtn = new Button(context);
        layoutParams = new LayoutParams(btnWidth, btnHeight);
        layoutParams.gravity = Gravity.BOTTOM | Gravity.END;
        layoutParams.setMargins(0, 0, 0, borderMargin);
        layoutParams.setMarginEnd(borderMargin);
        endBtn.setLayoutParams(layoutParams);
        endBtn.setBackground(ContextCompat.getDrawable(context, R.drawable.rounded_outline_shape));
        endBtn.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#484848")));
        endBtn.setOnClickListener(v -> {
            if (endBtnAction != null)
                endBtnAction.run();
        });
        setBtnLook(endBtn);
        addView(endBtn);
    }
    private void setBtnLook(Button button) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
            button.setOutlineSpotShadowColor(Color.TRANSPARENT);
        StateListDrawable states = new StateListDrawable();
        states.addState(new int[] { android.R.attr.state_focused }, ContextCompat.getDrawable(context, R.drawable.rounded_outline_shape));
        states.addState(new int[] { android.R.attr.state_active }, ContextCompat.getDrawable(context, R.drawable.rounded_outline_shape));
        states.addState(new int[] { android.R.attr.state_pressed }, ContextCompat.getDrawable(context, R.drawable.rounded_outline_shape));
        states.addState(new int[] { android.R.attr.state_enabled }, ContextCompat.getDrawable(context, R.drawable.rounded_outline_shape));
        button.setBackground(states);
        button.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#484848")));
        //startBtn.setBackground(ContextCompat.getDrawable(context, R.drawable.rounded_outline_shape));
        //startBtn.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#484848")));
        button.setOnTouchListener((view, event) -> {
            boolean isDown = event.getAction() == KeyEvent.ACTION_DOWN;
            button.setBackgroundTintList(ColorStateList.valueOf((isDown || button.hasFocus()) ? Color.parseColor("#CEEAF0") : Color.parseColor("#484848")));
            return false;
        });
        // highlight button when has focus
        button.setOnFocusChangeListener((view, hasFocus) -> {
            //Log.d("DynamicInputItemView", "onFocusChange [" + inputItem.row + ", " + inputItem.col + "] hasFocus: " + hasFocus);
            button.setBackgroundTintList(ColorStateList.valueOf((hasFocus || button.isPressed()) ? Color.parseColor("#CEEAF0") : Color.parseColor("#484848")));
        });

        Typeface font = SettingsKeeper.getFont();
        button.setTypeface(font);
        button.setTextSize(getBtnTextSize());
        button.setTextColor(Color.WHITE);
        button.setAllCaps(false);
    }
    private boolean isAnyBtnSet() {
        return isStartBtnSet || isMiddleBtnSet || isEndBtnSet;
    }
    private void rearrangeViews() {
        img.setVisibility(isImageSet ? VISIBLE : GONE);
        startBtn.setVisibility(isStartBtnSet ? VISIBLE : GONE);
        middleBtn.setVisibility(isMiddleBtnSet ? VISIBLE : GONE);
        endBtn.setVisibility(isEndBtnSet ? VISIBLE : GONE);

        if (isImageSet)
            img.setBackground(getImageDrawable());
        if (isStartBtnSet)
            startBtn.setText(startBtnTxt);
        if (isMiddleBtnSet)
            middleBtn.setText(middleBtnTxt);
        if (isEndBtnSet)
            endBtn.setText(endBtnTxt);
        msg.setText(isMsgSet ? message : "");

        int borderMargin = getBorderMargin();
        int imgSize = getImageSize();
        int btnHeight = getBtnHeight();
        int textStartMargin = borderMargin * 2 + imgSize;
        int textLowerMargin = borderMargin * 2 + btnHeight;

        LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        layoutParams.gravity = Gravity.TOP | Gravity.END;
        layoutParams.setMargins(0, borderMargin, 0, isAnyBtnSet() ? textLowerMargin : borderMargin);
        layoutParams.setMarginStart(isImageSet ? textStartMargin : borderMargin);
        layoutParams.setMarginEnd(borderMargin);
        msg.setLayoutParams(layoutParams);
    }
    private void refreshSize() {
        LayoutParams layoutParams = (FrameLayout.LayoutParams)getLayoutParams();
        layoutParams.width = chosenWidth;
        layoutParams.height = chosenHeight;
        setLayoutParams(layoutParams);
    }

    public void setCenterOfScreen() {
        setCenteredPosition(Math.round(OxShellApp.getDisplayWidth() / 2f), Math.round(OxShellApp.getDisplayHeight() / 2f));
    }
    public void setShown(boolean onOff) {
        isShown = onOff;
        if (isShown) {
            refreshSize();
            updatePosition();
            rearrangeViews();
            if (!isAnyBtnSet())
                OxShellApp.getInputHandler().addKeyComboActions(INPUT_TAG, Arrays.stream(SettingsKeeper.getCancelInput()).map(keycode -> new KeyComboAction(keycode, () -> setShown(false))).toArray(KeyComboAction[]::new));
            OxShellApp.getInputHandler().addKeyComboActions(INPUT_TAG, Arrays.stream(SettingsKeeper.getNavigateLeft()).map(keycode -> new KeyComboAction(keycode, this::selectLeft)).toArray(KeyComboAction[]::new));
            OxShellApp.getInputHandler().addKeyComboActions(INPUT_TAG, Arrays.stream(SettingsKeeper.getNavigateRight()).map(keycode -> new KeyComboAction(keycode, this::selectRight)).toArray(KeyComboAction[]::new));
            OxShellApp.getInputHandler().addKeyComboActions(INPUT_TAG, Arrays.stream(SettingsKeeper.getPrimaryInput()).map(keycode -> new KeyComboAction(keycode, this::pressItem)).toArray(KeyComboAction[]::new));
            OxShellApp.getInputHandler().setActiveTag(INPUT_TAG);
        } else {
            resetValues();
            OxShellApp.getInputHandler().removeTagFromHistory(INPUT_TAG);
            OxShellApp.getInputHandler().clearKeyComboActions(INPUT_TAG);
        }
        setVisibility(onOff ? VISIBLE : GONE);
    }
    private void selectLeft() {
        if (isMiddleBtnSet && middleBtn.hasFocus())
            startBtn.requestFocus();
        else if (isEndBtnSet && endBtn.hasFocus())
            if (isMiddleBtnSet)
                middleBtn.requestFocus();
            else
                startBtn.requestFocus();
        else if (!(isStartBtnSet && startBtn.hasFocus()))
            endBtn.requestFocus();
    }
    private void selectRight() {
        if (isMiddleBtnSet && middleBtn.hasFocus())
            endBtn.requestFocus();
        else if (isStartBtnSet && startBtn.hasFocus())
            if (isMiddleBtnSet)
                middleBtn.requestFocus();
            else
                endBtn.requestFocus();
        else if (!(isEndBtnSet && endBtn.hasFocus()))
            startBtn.requestFocus();
    }
    private void pressItem() {
        if (startBtn.hasFocus())
            startBtn.performClick();
        else if (middleBtn.hasFocus())
            middleBtn.performClick();
        else if (endBtn.hasFocus())
            endBtn.performClick();
    }

    public void setPromptImage(int resourceId) {
        isImageSet = true;
        imageLoc = DataLocation.resource;
        imgData = resourceId;
    }
    public void setStartBtn(String text, Runnable onClick, KeyCombo... keycodes) {
        isStartBtnSet = true;
        startBtnTxt = text;
        startBtnAction = onClick;
        OxShellApp.getInputHandler().addKeyComboActions(INPUT_TAG, Arrays.stream(keycodes).map(keycode -> new KeyComboAction(keycode, startBtnAction)).toArray(KeyComboAction[]::new));
//        if (keycodes != null)
//            Collections.addAll(startBtnKeys, keycodes);
    }
    public void setMiddleBtn(String text, Runnable onClick, KeyCombo... keycodes) {
        isMiddleBtnSet = true;
        middleBtnTxt = text;
        middleBtnAction = onClick;
        OxShellApp.getInputHandler().addKeyComboActions(INPUT_TAG, Arrays.stream(keycodes).map(keycode -> new KeyComboAction(keycode, middleBtnAction)).toArray(KeyComboAction[]::new));
//        if (keycodes != null)
//            Collections.addAll(middleBtnKeys, keycodes);
    }
    public void setEndBtn(String text, Runnable onClick, KeyCombo... keycodes) {
        isEndBtnSet = true;
        endBtnTxt = text;
        endBtnAction = onClick;
        OxShellApp.getInputHandler().addKeyComboActions(INPUT_TAG, Arrays.stream(keycodes).map(keycode -> new KeyComboAction(keycode, endBtnAction)).toArray(KeyComboAction[]::new));
        //if (keycodes != null)
        //    Collections.addAll(endBtnKeys, keycodes);
    }
    public void setMessage(String text) {
        isMsgSet = true;
        message = text;
    }
    private Drawable getImageDrawable() {
        if (imageLoc == DataLocation.resource)
            return ContextCompat.getDrawable(context, (Integer)imgData);
        return null;
    }
    public void resetValues() {
        percentX = 0;
        percentY = 0;
        chosenWidth = getDefaultWidth();
        chosenHeight = getDefaultHeight();
        isImageSet = false;
        imageLoc = DataLocation.none;
        imgData = null;
        isMsgSet = false;
        message = null;
        isStartBtnSet = false;
        startBtnTxt = null;
        startBtnAction = null;
        //startBtnKeys.clear();
        isMiddleBtnSet = false;
        middleBtnTxt = null;
        middleBtnAction = null;
        //middleBtnKeys.clear();
        isEndBtnSet = false;
        endBtnTxt = null;
        endBtnAction = null;
        //endBtnKeys.clear();
        //inputHandler.clearKeyComboActions();
    }
    public boolean isPromptShown() {
        return isShown;
    }
    public void setSize(int width, int height) {
        chosenWidth = width;
        chosenHeight = height;
        refreshSize();
    }
    public void setCenteredPosition(int x, int y) {
        percentX = x / (float)OxShellApp.getDisplayWidth();
        percentY = y / (float)OxShellApp.getDisplayHeight();
        updatePosition();
    }
    private void updatePosition() {
        float x = (percentX * OxShellApp.getDisplayWidth()) - (chosenWidth / 2f);
        float y = (percentY * OxShellApp.getDisplayHeight()) - (chosenHeight / 2f);
        //Log.d("PromptView", "Updating position to [" + x + ", " + y + "]");
        setX(x);
        setY(y);
    }
    public int getDefaultWidth() {
        return Math.round(AndroidHelpers.getScaledDpToPixels(context, 300));
    }
    public int getDefaultHeight() {
        return Math.round(AndroidHelpers.getScaledDpToPixels(context, 150));
    }

    @Override
    public boolean receiveKeyEvent(KeyEvent keyEvent) {
        return inputHandler.onInputEvent(keyEvent);
    }
}
