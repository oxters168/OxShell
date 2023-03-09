package com.OxGames.OxShell.Views;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.OxGames.OxShell.Helpers.AndroidHelpers;
import com.OxGames.OxShell.Interfaces.InputReceiver;
import com.OxGames.OxShell.OxShellApp;
import com.OxGames.OxShell.R;

public class PromptView extends CardView implements InputReceiver {
    private Context context;
    private BetterTextView msg;
    private boolean isShown = false;
    private float percentX = 0;
    private float percentY = 0;

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
//    public PromptView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
//        super(context, attrs, defStyleAttr, defStyleRes);
//        this.context = context;
//        init();
//    }

    private void init() {
        isShown = false;
        setVisibility(GONE);

        LayoutParams layoutParams;

        layoutParams = new FrameLayout.LayoutParams(getDefaultWidth(), getDefaultHeight());
        layoutParams.gravity = Gravity.TOP | Gravity.LEFT;
        setLayoutParams(layoutParams);

        setBackground(ContextCompat.getDrawable(context, R.drawable.rounded_shape));
        setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#BB323232")));
        //setBackgroundColor(Color.parseColor("#323232"));

        int textSize = Math.round(AndroidHelpers.getScaledSpToPixels(context, 8));
        int textMargin = Math.round(AndroidHelpers.getScaledDpToPixels(context, 4));
        int textLowerMargin = textMargin + Math.round(getDefaultHeight() * 0.33f);
        msg = new BetterTextView(context);
        layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        layoutParams.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
        layoutParams.setMargins(textMargin, textMargin, textMargin, textLowerMargin);
        msg.setLayoutParams(layoutParams);
        msg.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        msg.setOverScrollMode(SCROLL_AXIS_VERTICAL);
        msg.setMovementMethod(new ScrollingMovementMethod());
        msg.setEllipsize(TextUtils.TruncateAt.END);
        msg.setGravity(Gravity.TOP | Gravity.START);
        msg.setTextAlignment(View.TEXT_ALIGNMENT_GRAVITY);
        msg.setTextColor(context.getColor(R.color.text));
        msg.setTextSize(textSize);
        msg.setText("Quos voluptas commodi maxime dolore eveniet enim commodi et. Et qui nobis est earum eum. Excepturi quis nostrum consectetur ipsum debitis nihil autem. Vitae maiores ducimus et aut voluptas. Est ipsa aliquam quibusdam id atque. Veritatis nisi non minus quo aut. Qui voluptate eos nihil dolores aut. Atque debitis quidem similique molestias perferendis eum numquam qui. Necessitatibus hic quia nulla minus occaecati occaecati est. Unde qui culpa distinctio ea repellat omnis cumque voluptatibus. Vel ut non iste. Numquam ut est temporibus eveniet et exercitationem maxime. Adipisci rerum magnam ipsa laudantium dolores. Vitae ea rem dicta molestiae ut rerum placeat. Repellat fugiat et quo corporis culpa facilis quia. Vel et rerum doloribus porro reiciendis est aut. Illum nihil non et molestiae nostrum. Molestiae dolor cupiditate a numquam adipisci nobis. Rerum saepe libero doloribus incidunt sunt molestias explicabo. Error inventore libero quam nostrum voluptates minima corporis voluptatem. Culpa illum vel ut qui aut in. Eligendi perferendis pariatur dolorum reiciendis sit. Ut et labore magnam quas debitis. Autem et enim enim quia nam voluptatibus illo.");
        addView(msg);
    }

    public void setShown(boolean onOff) {
        isShown = onOff;
        updatePosition();
        setVisibility(onOff ? VISIBLE : GONE);
    }
    public boolean isPromptShown() {
        return isShown;
    }
    public void setSize(int width, int height) {
        LayoutParams layoutParams = (FrameLayout.LayoutParams)getLayoutParams();
        layoutParams.width = width;
        layoutParams.height = height;
        setLayoutParams(layoutParams);
    }
    public void setCenteredPosition(int x, int y) {
        percentX = x / (float)OxShellApp.getDisplayWidth();
        percentY = y / (float)OxShellApp.getDisplayHeight();
        updatePosition();
    }
    private void updatePosition() {
        LayoutParams layoutParams = (FrameLayout.LayoutParams)getLayoutParams();
        int width = layoutParams.width;
        int height = layoutParams.height;
        setX(percentX * OxShellApp.getDisplayWidth() - width / 2f);
        setY(percentY * OxShellApp.getDisplayHeight() - height / 2f);
    }
    public int getDefaultWidth() {
        return Math.round(AndroidHelpers.getScaledDpToPixels(context, 300));
    }
    public int getDefaultHeight() {
        return Math.round(AndroidHelpers.getScaledDpToPixels(context, 150));
    }

    @Override
    public boolean receiveKeyEvent(KeyEvent keyEvent) {
        if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_BACK)
            return true; // in case its not mapped to anything, then don't quit OxShell
        return false;
    }
}
