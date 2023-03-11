package com.OxGames.OxShell.Views;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.OxGames.OxShell.Data.DataLocation;
import com.OxGames.OxShell.Helpers.AndroidHelpers;
import com.OxGames.OxShell.Interfaces.InputReceiver;
import com.OxGames.OxShell.OxShellApp;
import com.OxGames.OxShell.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PromptView extends FrameLayout implements InputReceiver {
    private Context context;
    private ImageView img;
    private BetterTextView msg;
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
    private final List<Integer> startBtnKeys = new ArrayList<>();
    private boolean isMiddleBtnSet = false;
    private String middleBtnTxt;
    private Runnable middleBtnAction;
    private final List<Integer> middleBtnKeys = new ArrayList<>();
    private boolean isEndBtnSet = false;
    private String endBtnTxt;
    private Runnable endBtnAction;
    private final List<Integer> endBtnKeys = new ArrayList<>();

    private boolean isShown = false;
    private float percentX = 0;
    private float percentY = 0;
    private int chosenWidth = 0;
    private int chosenHeight = 0;

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

    private void init() {
        isShown = false;
        setVisibility(GONE);

        LayoutParams layoutParams;

        layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
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
        layoutParams = new FrameLayout.LayoutParams(imgSize, imgSize);
        layoutParams.gravity = Gravity.TOP | Gravity.START;
        layoutParams.setMargins(0, borderMargin, 0, 0);
        layoutParams.setMarginStart(borderMargin);
        img.setLayoutParams(layoutParams);
        img.setBackground(ContextCompat.getDrawable(context, R.drawable.ic_baseline_auto_awesome_24));
        img.setFocusable(false);
        addView(img);

        msg = new BetterTextView(context);
        layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        layoutParams.gravity = Gravity.TOP | Gravity.END;
        layoutParams.setMargins(0, borderMargin, 0, textLowerMargin);
        layoutParams.setMarginStart(textStartMargin);
        layoutParams.setMarginEnd(borderMargin);
        msg.setLayoutParams(layoutParams);
        //msg.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        msg.setOverScrollMode(SCROLL_AXIS_VERTICAL);
        msg.setMovementMethod(new ScrollingMovementMethod());
        //msg.setEllipsize(TextUtils.TruncateAt.END);
        msg.setGravity(Gravity.TOP | Gravity.START);
        msg.setTextAlignment(View.TEXT_ALIGNMENT_GRAVITY);
        msg.setTextColor(context.getColor(R.color.text));
        msg.setTextSize(textSize);
        msg.setText("Quos voluptas commodi maxime dolore eveniet enim commodi et. Et qui nobis est earum eum. Excepturi quis nostrum consectetur ipsum debitis nihil autem. Vitae maiores ducimus et aut voluptas. Est ipsa aliquam quibusdam id atque. Veritatis nisi non minus quo aut. Qui voluptate eos nihil dolores aut. Atque debitis quidem similique molestias perferendis eum numquam qui. Necessitatibus hic quia nulla minus occaecati occaecati est. Unde qui culpa distinctio ea repellat omnis cumque voluptatibus. Vel ut non iste. Numquam ut est temporibus eveniet et exercitationem maxime. Adipisci rerum magnam ipsa laudantium dolores. Vitae ea rem dicta molestiae ut rerum placeat. Repellat fugiat et quo corporis culpa facilis quia. Vel et rerum doloribus porro reiciendis est aut. Illum nihil non et molestiae nostrum. Molestiae dolor cupiditate a numquam adipisci nobis. Rerum saepe libero doloribus incidunt sunt molestias explicabo. Error inventore libero quam nostrum voluptates minima corporis voluptatem. Culpa illum vel ut qui aut in. Eligendi perferendis pariatur dolorum reiciendis sit. Ut et labore magnam quas debitis. Autem et enim enim quia nam voluptatibus illo.");
        msg.setFocusable(false);
        addView(msg);

        startBtn = new Button(context);
        layoutParams = new FrameLayout.LayoutParams(btnWidth, btnHeight);
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
        layoutParams = new FrameLayout.LayoutParams(btnWidth, btnHeight);
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
        layoutParams = new FrameLayout.LayoutParams(btnWidth, btnHeight);
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

    public void setShown(boolean onOff) {
        isShown = onOff;
        if (isShown) {
            refreshSize();
            updatePosition();
            rearrangeViews();
        } else
            resetValues();
        setVisibility(onOff ? VISIBLE : GONE);
    }
    public void setPromptImage(int resourceId) {
        isImageSet = true;
        imageLoc = DataLocation.resource;
        imgData = resourceId;
    }
    public void setStartBtn(String text, Runnable onClick, Integer... keycodes) {
        isStartBtnSet = true;
        startBtnTxt = text;
        startBtnAction = onClick;
        if (keycodes != null)
            Collections.addAll(startBtnKeys, keycodes);
    }
    public void setMiddleBtn(String text, Runnable onClick, Integer... keycodes) {
        isMiddleBtnSet = true;
        middleBtnTxt = text;
        middleBtnAction = onClick;
        if (keycodes != null)
            Collections.addAll(middleBtnKeys, keycodes);
    }
    public void setEndBtn(String text, Runnable onClick, Integer... keycodes) {
        isEndBtnSet = true;
        endBtnTxt = text;
        endBtnAction = onClick;
        if (keycodes != null)
            Collections.addAll(endBtnKeys, keycodes);
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
        startBtnKeys.clear();
        isMiddleBtnSet = false;
        middleBtnTxt = null;
        middleBtnAction = null;
        middleBtnKeys.clear();
        isEndBtnSet = false;
        endBtnTxt = null;
        endBtnAction = null;
        endBtnKeys.clear();
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
        if (keyEvent.getAction() == KeyEvent.ACTION_UP) {
            if (isStartBtnSet && startBtnAction != null && startBtnKeys.contains(keyEvent.getKeyCode())) {
                startBtnAction.run();
                return true;
            }
            if (isMiddleBtnSet && middleBtnAction != null && middleBtnKeys.contains(keyEvent.getKeyCode())) {
                middleBtnAction.run();
                return true;
            }
            if (isEndBtnSet && endBtnAction != null && endBtnKeys.contains(keyEvent.getKeyCode())) {
                endBtnAction.run();
                return true;
            }
            if (!isAnyBtnSet() && (keyEvent.getKeyCode() == KeyEvent.KEYCODE_BACK || keyEvent.getKeyCode() == KeyEvent.KEYCODE_BUTTON_B || keyEvent.getKeyCode() == KeyEvent.KEYCODE_BUTTON_A || keyEvent.getKeyCode() == KeyEvent.KEYCODE_BUTTON_START)) {
                setShown(false);
                return true;
            }
        }
        if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_BACK)
            return true; // in case its not mapped to anything, then don't quit OxShell
        return false;
    }
}
