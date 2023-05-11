package com.OxGames.OxShell.Views;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.AttributeSet;
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
import com.OxGames.OxShell.Data.SettingsKeeper;
import com.OxGames.OxShell.Helpers.AndroidHelpers;
import com.OxGames.OxShell.OxShellApp;
import com.OxGames.OxShell.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TooltipBar extends FrameLayout {
    private final Context context;
    private BetterTextView descriptionText;
    private FrameLayout inputDisplay;
    private NonConsumableRecyclerView inputTipsRecycler;
    private int barHeight;

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
//        inputsHolderPanel.addOnItemTouchListener(new RecyclerView.SimpleOnItemTouchListener() {
//            @Override
//            public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
//                // true: consume touch event
//                // false: dispatch touch event
//                return true;
//            }
//        });
            // TODO: turn off/on this runnable when leaving/entering the activity
            // Enable auto-scrolling behavior
            final int speedScroll = 10000; // Adjust this value to control how long to wait between scrolls
            final Handler handler = new Handler();
            final Runnable runnable = new Runnable() {
                int count = 1; // since the recycler view already starts at 0

                @Override
                public void run() {
                    if (count >= inputTipsRecycler.getAdapter().getItemCount())
                        count = 0;
                    if (count < inputTipsRecycler.getAdapter().getItemCount()) {
                        //((InputTipsRecyclerAdapter)inputTipsRecycler.getAdapter()).setSelected(count);
                        inputTipsRecycler.smoothScrollToPosition(count++);
                        handler.postDelayed(this, speedScroll);
                    }
                }
            };
            handler.postDelayed(runnable, speedScroll);

            // for testing
//        InputTip[] inputTips = new InputTip[20];
//        for (int i = 0; i < inputTips.length; i++)
//            inputTips[i] = InputTip.of(DataRef.from("Image/inputs/asset_touch_down_hold.png", DataLocation.asset), "Open context menu");
            inputTipsRecycler.setAdapter(new InputTipsRecyclerAdapter(
                    InputTip.of(DataRef.from("Image/inputs/asset_touch_up.png", DataLocation.asset), "Select current item"),
                    InputTip.of(DataRef.from("Image/inputs/asset_touch_drag_left.png", DataLocation.asset), "Select next item on right"),
                    InputTip.of(DataRef.from("Image/inputs/asset_touch_drag_right.png", DataLocation.asset), "Select next item on left"),
                    InputTip.of(DataRef.from("Image/inputs/asset_touch_drag_up.png", DataLocation.asset), "Select next item below"),
                    InputTip.of(DataRef.from("Image/inputs/asset_touch_drag_down.png", DataLocation.asset), "Select next item above"),
                    InputTip.of(DataRef.from("Image/inputs/asset_touch_down_hold.png", DataLocation.asset), "Open context menu")
            ));
        }
        if (inputTipsRecycler.getAdapter() != null)
            ((InputTipsRecyclerAdapter)inputTipsRecycler.getAdapter()).refreshViews();
    }
//    public void refreshViews() {
//        refreshDescAndInputSizes();
//    }

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
        private final DataRef img;
        private final String actionDesc;

        private InputTip(DataRef img, String actionDesc) {
            this.img = img;
            this.actionDesc = actionDesc;
        }

        public static InputTip of(DataRef img, String actionDesc) {
            return new InputTip(img, actionDesc);
        }
    }
    private class InputTipView extends LinearLayout {
        private ImageView img;
        private BetterTextView actionDesc;

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
            LayoutParams params;

            if (img == null) {
                img = new ImageView(context);
                img.setFocusable(false);
                addView(img);
            }
            params = new LayoutParams(barHeight - getEdgeMargins() * 2, barHeight - getEdgeMargins() * 2);
            params.gravity = Gravity.START | Gravity.CENTER_VERTICAL;
            img.setLayoutParams(params);

            if (actionDesc == null) {
                actionDesc = new BetterTextView(context);
                params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
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
                actionDesc.setText("Action Description");
                actionDesc.setFocusable(false);
                actionDesc.setTypeface(SettingsKeeper.getFont());
                addView(actionDesc);
            }
            actionDesc.setOutlineSize(getTextOutlineSize());
            actionDesc.setTextSize(getTextSize());
        }

        public void setImg(Drawable img) {
            this.img.setImageDrawable(img);
        }
        public void setActionDesc(String actionDesc) {
            this.actionDesc.setText(actionDesc);
        }
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
            item.img.getImage(inputTipView::setImg);
            inputTipView.setActionDesc(item.actionDesc);
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

        // TODO: readjust when orientation changes
        @Override
        public void onBindViewHolder(@NonNull InputTipViewHolder holder, int position) {
            //Log.d("InputRowAdapter", "Placing item @" + position + " in row");
            InputTip item = items.get(position);
            holder.bindItem(item);
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
            for (InputTipViewHolder holder : viewHolders)
                holder.refresh();
        }
        public void setSelected(int index) {
            for (int i = 0; i < viewHolders.size(); i++) {
                viewHolders.get(i).setSelected(i == index);
            }
        }
//        public void refreshItems() {
//            for (RowViewHolder holder : viewHolders)
//                refreshHolder(holder);
//        }
//        private void refreshHolder(RowViewHolder holder) {
//            int maxVisibleItems = Math.min(MAX_VISIBLE_ITEMS, items.size());
//            DynamicInputRow.DynamicInput item = ((DynamicInputItemView)holder.itemView).getInputItem();
//            int relativeIndex = item.col % maxVisibleItems;
//            // TODO: don't consider items that are not visible
//            int currentVisibleItems = Math.min(MAX_VISIBLE_ITEMS, items.size() - (item.col - relativeIndex));
//            //int maxWidth = Math.round((rowWidth - ((currentVisibleItems - 1) * paddingPx)) / (float)currentVisibleItems);
//            int buttonPx = Math.round(AndroidHelpers.getScaledDpToPixels(context, BUTTON_DIP));
//            int maxWidth;
//            if (item.inputType == DynamicInputRow.DynamicInput.InputType.button || item.inputType == DynamicInputRow.DynamicInput.InputType.image)
//                maxWidth = buttonPx;
//            else {
//                maxWidth = Math.round(rowWidth / (float)currentVisibleItems);
//                // of the visible items alongside us, figure out which ones are buttons and use that information to more properly set the width
//                int visibleBtnCount = 0;
//                for (int i = 0; i < currentVisibleItems; i++) {
//                    DynamicInputRow.DynamicInput currentItem = items.get(item.col + (i - relativeIndex));
//                    if (currentItem.inputType == DynamicInputRow.DynamicInput.InputType.button || currentItem.inputType == DynamicInputRow.DynamicInput.InputType.image)
//                        visibleBtnCount++;
//                }
//                if (visibleBtnCount > 0)
//                    maxWidth = Math.round((rowWidth - (visibleBtnCount * buttonPx)) / (float) (currentVisibleItems - visibleBtnCount));
//                //maxWidth = Math.round((rowWidth - (visibleBtnCount * buttonPx) - ((currentVisibleItems - 1) * paddingPx)) / (float) (currentVisibleItems - visibleBtnCount));
//            }
//
//            int paddingPx = Math.round(AndroidHelpers.getScaledDpToPixels(context, PADDING));
//            //Log.d("InputRowAdapter", "Parent width at " + rowWidth + " current visible items: " + currentVisibleItems + " current item width: " + maxWidth + " resummed: " + (maxWidth * currentVisibleItems + (currentVisibleItems - 1) * paddingPx));
//            holder.setWidth(maxWidth);
//            //holder.setPadding(0, 0, relativeIndex < currentVisibleItems - 1 ? paddingPx : 0, 0);
//            holder.setPadding(0, 0, item.col < items.size() - 1 ? paddingPx : 0, 0);
//        }
//
//        public void setRowWidth(int width) {
//            //Log.d("InputRowAdapter", "Row width set to " + width);
//            this.rowWidth = width;
//        }
    }
}
