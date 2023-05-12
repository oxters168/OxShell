package com.OxGames.OxShell.Views;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.OxGames.OxShell.Data.DataLocation;
import com.OxGames.OxShell.Data.DataRef;
import com.OxGames.OxShell.Data.InputType;
import com.OxGames.OxShell.Data.KeyComboAction;
import com.OxGames.OxShell.Data.SettingsKeeper;
import com.OxGames.OxShell.Helpers.AndroidHelpers;
import com.OxGames.OxShell.Helpers.InputHandler;
import com.OxGames.OxShell.OxShellApp;
import com.OxGames.OxShell.R;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TooltipBar extends FrameLayout {
    private static final int speedScroll = 10000; // Adjust this value to control how long to wait between scrolls (in ms)
    private int count;
    private final Context context;
    private BetterTextView descriptionText;
    private FrameLayout inputDisplay;
    private NonConsumableRecyclerView inputTipsRecycler;
    private int barHeight;

    private InputType currentInputType;
    private final Handler scrollHandler = new Handler();

    public TooltipBar(@NonNull Context context) {
        super(context);
        this.context = context;
        refreshViews();
    }
    public TooltipBar(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        refreshViews();
    }
    public TooltipBar(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        refreshViews();
    }
    public TooltipBar(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.context = context;
        refreshViews();
    }

    public int getTextSize() {
        return Math.round(AndroidHelpers.getScaledSpToPixels(context, 10));
    }
    public int getTextOutlineSize() {
        return Math.round(AndroidHelpers.getScaledDpToPixels(context, 3));
    }
    public int getEdgeMargins() {
        return Math.round(AndroidHelpers.getScaledDpToPixels(context, 4));
    }

    public void setShownInputType(InputType inputType) {
        this.currentInputType = inputType;
    }
    public void refreshInputs() {
        List<InputTip> tips = new ArrayList<>();
        if (currentInputType != InputType.Touch) {
            InputHandler.ComboActions[] allCombos = InputHandler.getAllCombos();
            for (InputHandler.ComboActions actions : allCombos) {
                if (InputHandler.isTagEnabled(actions.tag)) {
                    for (KeyComboAction action : actions.getActions()) {
                        if (action.actionDesc != null) {
                            // add action since it has a description
                            List<DataRef> iconRefs = new ArrayList<>();
                            // TODO: figure out a good way to exclude combos that do not comply with current input type but still include unknown inputs of that type
                            for (int key : action.keyCombo.getKeys()) {
                                if (currentInputType == InputType.Keyboard)
                                    iconRefs.add(AndroidHelpers.keyboardKeyToIconRef(key));
                                else
                                    iconRefs.add(AndroidHelpers.gamepadKeyToIconRef(key, currentInputType));
                            }
                            if (iconRefs.size() > 0)
                                tips.add(InputTip.of(action.actionDesc, iconRefs.toArray(new DataRef[0])));
                        }
                    }
                }
            }
        } else {
            // set to touch inputs
            // TODO: figure out way for touch input tips to be relevant to where user is (which is what tag enabling/disabling is meant to do for non-touch but does not seem to be)
            tips.add(InputTip.of("Make selection", new DataRef[] { DataRef.from("Image/inputs/asset_touch_up.png", DataLocation.asset) }));
            tips.add(InputTip.of("Open context menu", new DataRef[] { DataRef.from("Image/inputs/asset_touch_down_hold.png", DataLocation.asset) }));
            tips.add(InputTip.of("Navigate left", new DataRef[] { DataRef.from("Image/inputs/asset_touch_drag_right.png", DataLocation.asset) }));
            tips.add(InputTip.of("Navigate right", new DataRef[] { DataRef.from("Image/inputs/asset_touch_drag_left.png", DataLocation.asset) }));
            tips.add(InputTip.of("Navigate up", new DataRef[] { DataRef.from("Image/inputs/asset_touch_drag_down.png", DataLocation.asset) }));
            tips.add(InputTip.of("Navigate down", new DataRef[] { DataRef.from("Image/inputs/asset_touch_drag_up.png", DataLocation.asset) }));
        }
        setInputTips(tips.size() > 0 ? tips.toArray(new InputTip[0]) : null);
    }
    private void setInputTips(InputTip... tips) {
        stopScroll();
        inputTipsRecycler.setAdapter(tips != null ? new InputTipsRecyclerAdapter(tips) : null);
        if (tips != null) {
            //((InputTipsRecyclerAdapter)inputTipsRecycler.getAdapter()).refreshViews();
            //inputTipsRecycler.scrollToPosition(0);
            startScroll();
        }
    }
    private void startScroll() {
        stopScroll(); // just in case it is on
        count = 1;
        inputTipsRecycler.scrollToPosition(0);
        scrollHandler.postDelayed(scrollRunnable, speedScroll);
    }
    private void resumeScroll() {
        stopScroll(); // just in case it is on
        inputTipsRecycler.scrollToPosition(count);
        scrollHandler.postDelayed(scrollRunnable, speedScroll);
    }
    private void stopScroll() {
        scrollHandler.removeCallbacks(scrollRunnable);
    }
    private Runnable scrollRunnable = new Runnable() {
        //int count = 1;
        @Override
        public void run() {
            if (count >= inputTipsRecycler.getAdapter().getItemCount())
                count = 0;
            if (count < inputTipsRecycler.getAdapter().getItemCount()) {
                //((InputTipsRecyclerAdapter)inputTipsRecycler.getAdapter()).setSelected(count);
                inputTipsRecycler.smoothScrollToPosition(count++);
                scrollHandler.postDelayed(this, speedScroll);
            }
        }
    };

    public void onPause() {
        stopScroll();
    }
    public void onResume() {
        resumeScroll();
    }

    public void refreshViews() {
        int textSize = getTextSize();
        int textOutlineSize = getTextOutlineSize();
        int edgeMargins = getEdgeMargins();
        LayoutParams params;

        if (descriptionText == null) {
            descriptionText = new BetterTextView(context);
            descriptionText.setFocusable(false);
            descriptionText.setIgnoreTouchInput(true);
            descriptionText.setOverScrollMode(SCROLL_AXIS_VERTICAL);
            descriptionText.setMovementMethod(new ScrollingMovementMethod());
            descriptionText.setSingleLine(true);
            descriptionText.setEllipsize(TextUtils.TruncateAt.MARQUEE);
            descriptionText.setMarqueeRepeatLimit(-1);
            descriptionText.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
            descriptionText.setTextAlignment(View.TEXT_ALIGNMENT_GRAVITY);
            descriptionText.setTextColor(context.getColor(R.color.text));
            descriptionText.setOutlineColor(Color.BLACK);
            descriptionText.setText("Description");
            descriptionText.setFocusable(false);
            descriptionText.setTypeface(SettingsKeeper.getFont());
            descriptionText.setSelected(true);
            addView(descriptionText);
        }
        descriptionText.setOutlineSize(textOutlineSize);
        descriptionText.setTextSize(textSize);

        descriptionText.measure(0, 0);

        params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, barHeight = descriptionText.getMeasuredHeight() + edgeMargins * 2);
        params.gravity = Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM;
        setLayoutParams(params);
        setBackgroundColor(Color.parseColor("#44323232"));
        setFocusable(false);

        if (inputDisplay == null) {
            inputDisplay = new FrameLayout(context);
            inputDisplay.setFocusable(false);
            //inputDisplay.setBackgroundColor(Color.GREEN);
            addView(inputDisplay);
        }

        refreshDescAndInputSizes();

        if (inputTipsRecycler == null) {
            inputTipsRecycler = new NonConsumableRecyclerView(context);
            RecyclerView.LayoutParams recyclerParams = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            recyclerParams.topMargin = edgeMargins;
            inputTipsRecycler.setLayoutParams(recyclerParams);
            inputTipsRecycler.setLayoutManager(new LinearLayoutManager(context, RecyclerView.HORIZONTAL, false));
            inputTipsRecycler.setOverScrollMode(OVER_SCROLL_NEVER);
            inputTipsRecycler.setFocusable(false);
            inputTipsRecycler.suppressLayout(true);
            inputDisplay.addView(inputTipsRecycler);
            // when using a regular RecyclerView, this can be used to stop scrolling with touch input
            // but it will stop the touch events from reaching the view below
//            inputsHolderPanel.addOnItemTouchListener(new RecyclerView.SimpleOnItemTouchListener() {
//                @Override
//                public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
//                    // true: consume touch event
//                    // false: dispatch touch event
//                    return true;
//                }
//            });
        }
    }

    private void refreshDescAndInputSizes() {
        int edgeMargins = getEdgeMargins();
        LayoutParams params;

        descriptionText.measure(0, 0); // based on text size
        int descriptionTextWidth = Math.min(OxShellApp.getDisplayWidth() / 2, descriptionText.getMeasuredWidth()); // if the text is longer than half the screen width then shorten to that
        params = new LayoutParams(descriptionTextWidth, ViewGroup.LayoutParams.MATCH_PARENT);
        params.gravity = Gravity.START | Gravity.CENTER_VERTICAL;
        params.setMargins(edgeMargins, edgeMargins, edgeMargins, edgeMargins);
        descriptionText.setLayoutParams(params);

        params = new LayoutParams(OxShellApp.getDisplayWidth() - (descriptionTextWidth + edgeMargins * 2), ViewGroup.LayoutParams.MATCH_PARENT);
        params.gravity = Gravity.END | Gravity.CENTER_VERTICAL;
        inputDisplay.setLayoutParams(params);
    }

    public static class InputTip {
        private final DataRef[][] img;
        private final String actionDesc;

        private InputTip(String actionDesc, DataRef[]... img) {
            this.img = img;
            this.actionDesc = actionDesc;
        }

        public static InputTip of(String actionDesc, DataRef[]... img) {
            return new InputTip(actionDesc, img);
        }

        @NonNull
        @Override
        public String toString() {
            return actionDesc + "\n" + (img != null ? img.length : 0);
        }
    }
    private class InputTipView extends LinearLayout {
//        private ImageView img;
        private BetterTextView actionDesc;
        private final ArrayDeque<ImageView> unusedImgs = new ArrayDeque<>();
        private final ArrayDeque<ImageView> usedImgs = new ArrayDeque<>();
        private final ArrayDeque<BetterTextView> unusedSlashes = new ArrayDeque<>();
        private final ArrayDeque<BetterTextView> usedSlashes = new ArrayDeque<>();
        private InputTip inputTip;

        public InputTipView(@NonNull Context context) {
            super(context);
            refresh();
        }
        public InputTipView(@NonNull Context context, @Nullable AttributeSet attrs) {
            super(context, attrs);
            refresh();
        }
        public InputTipView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
            refresh();
        }
        public InputTipView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
            super(context, attrs, defStyleAttr, defStyleRes);
            refresh();
        }

        private void refresh() {
            //Log.d("TooltipBar", "Refreshing tip view, input tip null: " + (inputTip == null));
            returnImgs();
            returnSlashes();
            if (inputTip != null && inputTip.img != null) {
                for (int i = 0; i < inputTip.img.length; i++) {
                    for (int j = 0; j < inputTip.img[i].length; j++) {
                        // add img
                        ImageView img = getImg();
                        inputTip.img[i][j].getImage(img::setImageDrawable);
//                    removeView(img);
//                    addView(img);
                        if (j < inputTip.img[i].length - 1) {
                            // add plus
                            BetterTextView plus = getSlash();
                            plus.setText("+");
//                        removeView(plus);
//                        addView(plus);
                        }
                    }
                    if (i < inputTip.img.length - 1) {
                        // add slash
                        BetterTextView slash = getSlash();
                        slash.setText("/");
//                    removeView(slash);
//                    addView(slash);
                    }
                }
            }
            refreshActionDesc();
        }

        private void refreshActionDesc() {
            if (actionDesc == null) {
                actionDesc = new BetterTextView(context);
                LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params.gravity = Gravity.END | Gravity.CENTER_VERTICAL;
                actionDesc.setLayoutParams(params);
                actionDesc.setFocusable(false);
                actionDesc.setIgnoreTouchInput(true);
                actionDesc.setOverScrollMode(SCROLL_AXIS_VERTICAL);
                actionDesc.setMovementMethod(new ScrollingMovementMethod());
                actionDesc.setEllipsize(TextUtils.TruncateAt.MARQUEE);
                actionDesc.setMarqueeRepeatLimit(-1);
                actionDesc.setSelected(true);
                actionDesc.setSingleLine(true);
                actionDesc.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
                actionDesc.setTextAlignment(View.TEXT_ALIGNMENT_GRAVITY);
                actionDesc.setTextColor(context.getColor(R.color.text));
                actionDesc.setOutlineColor(Color.BLACK);
                //actionDesc.setText("Action Description");
                actionDesc.setFocusable(false);
                actionDesc.setTypeface(SettingsKeeper.getFont());
                //addView(actionDesc);
            }
            removeView(actionDesc);
            addView(actionDesc);
            actionDesc.setText(inputTip != null ? inputTip.actionDesc : "Action Description");
            actionDesc.setOutlineSize(getTextOutlineSize());
            actionDesc.setTextSize(getTextSize());
        }
        private void returnImgs() {
            for (int i = 0; i < usedImgs.size(); i++) {
                ImageView img = usedImgs.pop();
                //img.setVisibility(GONE);
                removeView(img);
                unusedImgs.add(img);
            }
        }
        private void createImgs(int amount) {
            for (int i = 0; i < amount; i++) {
                ImageView img = new ImageView(context);
                img.setFocusable(false);
                //addView(img);
                //img.setVisibility(GONE);
                unusedImgs.add(img);
            }
        }
        private ImageView getImg() {
            if (unusedImgs.isEmpty())
                createImgs(5);
            ImageView img = unusedImgs.pop();
            usedImgs.add(img);
            LayoutParams params = new LayoutParams(barHeight - getEdgeMargins() * 2, barHeight - getEdgeMargins() * 2);
            params.gravity = Gravity.START | Gravity.CENTER_VERTICAL;
            img.setLayoutParams(params);
            //img.setVisibility(VISIBLE);
            addView(img);
            return img;
        }
        private void returnSlashes() {
            for (int i = 0; i < usedSlashes.size(); i++) {
                BetterTextView slash = usedSlashes.pop();
                //slash.setVisibility(GONE);
                removeView(slash);
                unusedSlashes.add(slash);
            }
        }
        private void createSlashes(int amount) {
            for (int i = 0; i < amount; i++) {
                BetterTextView newSlash = new BetterTextView(context);
                LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params.gravity = Gravity.START | Gravity.CENTER_VERTICAL;
                newSlash.setLayoutParams(params);
                newSlash.setFocusable(false);
                newSlash.setIgnoreTouchInput(true);
//            newSlash.setOverScrollMode(SCROLL_AXIS_VERTICAL);
//            newSlash.setMovementMethod(new ScrollingMovementMethod());
//            newSlash.setEllipsize(TextUtils.TruncateAt.MARQUEE);
//            newSlash.setMarqueeRepeatLimit(-1);
//            newSlash.setSelected(true);
                newSlash.setSingleLine(true);
                newSlash.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
                newSlash.setTextAlignment(View.TEXT_ALIGNMENT_GRAVITY);
                newSlash.setTextColor(context.getColor(R.color.text));
                newSlash.setOutlineColor(Color.BLACK);
                newSlash.setText("/");
                newSlash.setFocusable(false);
                newSlash.setTypeface(SettingsKeeper.getFont());
                //addView(newSlash);
                unusedSlashes.add(newSlash);
            }
        }
        private BetterTextView getSlash() {
            if (unusedSlashes.isEmpty())
                createSlashes(5);
            BetterTextView slash = unusedSlashes.pop();
            usedSlashes.add(slash);
            //slash.setVisibility(VISIBLE);
            addView(slash);
            return slash;
        }

        public void setInputTip(InputTip inputTip) {
            //Log.d("TooltipBar", "Setting input tip to: " + inputTip);
            this.inputTip = inputTip;
        }
//        public void setImg(Drawable img) {
//            this.img.setImageDrawable(img);
//        }
//        public void setActionDesc(String actionDesc) {
//            this.actionDesc.setText(actionDesc);
//        }
        public void setSelected(boolean onOff) {
            this.actionDesc.setSelected(onOff);
        }
    }
    private class InputTipViewHolder extends RecyclerView.ViewHolder {
        //private View itemView;
        public InputTipViewHolder(@NonNull InputTipView itemView) {
            super(itemView);
            //this.itemView = itemView;
        }
        public void bindItem(InputTip item) {
            InputTipView inputTipView = (InputTipView)itemView;
            inputTipView.setInputTip(item);
//            item.img.getImage(inputTipView::setImg);
//            inputTipView.setActionDesc(item.actionDesc);
            //((DynamicInputItemView)itemView).setInputItem(item);
        }
        public void refresh() {
            ((InputTipView)itemView).refresh();
        }
        public void setSelected(boolean onOff) {
            ((InputTipView)itemView).setSelected(onOff);
        }
//        public void setPadding(int left, int top, int right, int bottom) {
//            itemView.setPadding(left, top, right, bottom);
//        }
//        public void setWidth(int px) {
//            ViewGroup.LayoutParams params = itemView.getLayoutParams();
//            params.width = px;
//            itemView.setLayoutParams(params);
//        }
//        public boolean requestFocus() {
//            return itemView.requestFocus();
//        }
    }
    private class InputTipsRecyclerAdapter extends RecyclerView.Adapter<InputTipViewHolder> {
//        private Context context;
        private List<InputTip> items;
//        private static final int BUTTON_DIP = 128;
//        private static final int MAX_VISIBLE_ITEMS = 4;
//        private static final int PADDING = 20;
//        private int rowWidth;
        private List<InputTipViewHolder> viewHolders;
//
//        private List<AdapterListener> listeners;

        public InputTipsRecyclerAdapter(InputTip... inputTips) {
            this.items = new ArrayList<>();
            if (inputTips != null)
                Collections.addAll(this.items, inputTips);
//            this.context = context;
            this.viewHolders = new ArrayList<>();
//            //Log.d("InputRowAdapter", "Creating row with " + items.length + " item(s)");
//            listeners = new ArrayList<>();
        }

//        // TODO: remove listeners when dynamic view hidden (potential memory leak)
//        public void addListener(AdapterListener listener) {
//            listeners.add(listener);
//        }
//        public void removeListener(AdapterListener listener) {
//            listeners.remove(listener);
//        }
//        public void clearListeners() {
//            listeners.clear();
//        }

        @NonNull
        @Override
        public InputTipViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            InputTipView view = new InputTipView(context);
            view.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            InputTipViewHolder viewHolder = new InputTipViewHolder(view);
            viewHolders.add(viewHolder);
            return viewHolder;
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        @Override
        public void onBindViewHolder(@NonNull InputTipViewHolder holder, int position) {
            //Log.d("InputRowAdapter", "Placing item @" + position + " in row");
            InputTip item = items.get(position);
            holder.bindItem(item);
            holder.refresh();
//            refreshHolder(holder);
        }

        @Override
        public void onViewAttachedToWindow(@NonNull InputTipViewHolder holder) {
//            super.onViewAttachedToWindow(holder);
//            for (AdapterListener listener : listeners)
//                if (listener != null)
//                    listener.onViewsReady();
        }

        public void refreshViews() {
            //Log.d("TooltipBar", "Refreshing recycler's views");
            for (InputTipViewHolder holder : viewHolders)
                holder.refresh();
        }
        public void setSelected(int index) {
            for (int i = 0; i < viewHolders.size(); i++) {
                viewHolders.get(i).setSelected(i == index);
            }
        }
    }
}
