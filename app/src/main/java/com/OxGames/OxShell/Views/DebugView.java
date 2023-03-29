package com.OxGames.OxShell.Views;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.method.ScrollingMovementMethod;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.OxGames.OxShell.Data.SettingsKeeper;
import com.OxGames.OxShell.Helpers.AndroidHelpers;
import com.OxGames.OxShell.R;

public class DebugView extends FrameLayout {
    private final Context context;
    private boolean isShown;
    private BetterTextView debugLabel;

    public DebugView(@NonNull Context context) {
        super(context);
        this.context = context;
        init();
    }
    public DebugView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
    }
    public DebugView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init();
    }
    public DebugView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.context = context;
        init();
    }

    private void init() {
        isShown = false;
        setVisibility(GONE);

        LayoutParams layoutParams;

        layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        layoutParams.gravity = Gravity.TOP | Gravity.LEFT;
        setLayoutParams(layoutParams);
        //setBackground(ContextCompat.getDrawable(context, R.drawable.rounded_outline_shape));
        //setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#BB323232")));
        setFocusable(false);

        debugLabel = new BetterTextView(context);
        layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        layoutParams.gravity = Gravity.TOP | Gravity.END;
        int borderMargin = getBorderMargin();
        layoutParams.setMargins(borderMargin, borderMargin, borderMargin, borderMargin);
        debugLabel.setLayoutParams(layoutParams);
        //msg.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        debugLabel.setOverScrollMode(SCROLL_AXIS_VERTICAL);
        debugLabel.setMovementMethod(new ScrollingMovementMethod());
        //msg.setEllipsize(TextUtils.TruncateAt.END);
        debugLabel.setGravity(Gravity.TOP | Gravity.START);
        debugLabel.setTextAlignment(View.TEXT_ALIGNMENT_GRAVITY);
        debugLabel.setTextColor(context.getColor(R.color.text));
        debugLabel.setTextSize(getTextSize());
        debugLabel.setOutlineColor(Color.parseColor("#000000"));
        int textOutlineSize = Math.round(AndroidHelpers.getScaledDpToPixels(context, 3));
        debugLabel.setOutlineSize(textOutlineSize);
        debugLabel.setText("Quos voluptas commodi maxime dolore eveniet enim commodi et. Et qui nobis est earum eum. Excepturi quis nostrum consectetur ipsum debitis nihil autem. Vitae maiores ducimus et aut voluptas. Est ipsa aliquam quibusdam id atque. Veritatis nisi non minus quo aut. Qui voluptate eos nihil dolores aut. Atque debitis quidem similique molestias perferendis eum numquam qui. Necessitatibus hic quia nulla minus occaecati occaecati est. Unde qui culpa distinctio ea repellat omnis cumque voluptatibus. Vel ut non iste. Numquam ut est temporibus eveniet et exercitationem maxime. Adipisci rerum magnam ipsa laudantium dolores. Vitae ea rem dicta molestiae ut rerum placeat. Repellat fugiat et quo corporis culpa facilis quia. Vel et rerum doloribus porro reiciendis est aut. Illum nihil non et molestiae nostrum. Molestiae dolor cupiditate a numquam adipisci nobis. Rerum saepe libero doloribus incidunt sunt molestias explicabo. Error inventore libero quam nostrum voluptates minima corporis voluptatem. Culpa illum vel ut qui aut in. Eligendi perferendis pariatur dolorum reiciendis sit. Ut et labore magnam quas debitis. Autem et enim enim quia nam voluptatibus illo.");
        debugLabel.setFocusable(false);
        Typeface font = SettingsKeeper.getFont();
        debugLabel.setTypeface(font);
        addView(debugLabel);
    }

    private int getBorderMargin() {
        return Math.round(AndroidHelpers.getScaledDpToPixels(context, 8));
    }
    private int getTextSize() {
        return Math.round(AndroidHelpers.getScaledSpToPixels(context, 8));
    }

    public void setShown(boolean onOff) {
        isShown = onOff;
//        if (isShown) {
//
//        } else {
//
//        }
        setVisibility(onOff ? VISIBLE : GONE);
    }
    public boolean isDebugShown() {
        return isShown;
    }
}
