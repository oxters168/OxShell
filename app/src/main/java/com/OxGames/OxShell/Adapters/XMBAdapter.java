package com.OxGames.OxShell.Adapters;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.OxGames.OxShell.Data.HomeItem;
import com.OxGames.OxShell.Data.SettingsKeeper;
import com.OxGames.OxShell.Data.XMBItem;
import com.OxGames.OxShell.Helpers.AndroidHelpers;
import com.OxGames.OxShell.Interfaces.XMBAdapterListener;
import com.OxGames.OxShell.R;
import com.OxGames.OxShell.Views.BetterTextView;
import com.OxGames.OxShell.Views.XMBView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class XMBAdapter extends XMBView.Adapter<XMBAdapter.XMBViewHolder> {
    private Context context;
    //private ArrayList<ArrayList<XMBItem>> items;
    private ArrayList<XMBItem> items;

    private static final int TITLE_ID = View.generateViewId();
    private static final int ICON_HIGHLIGHT_ID = View.generateViewId();
    private static final int ICON_ID = View.generateViewId();
    //private static Drawable QUESTION_MARK_DRAWABLE;
    private Typeface font;

    //    public XMBAdapter(Context context, XMBItem... items) {
//        this.context = context;
//        this.items = items.clone();
//        font = Typeface.createFromAsset(context.getAssets(), "Fonts/exo.regular.otf");
//    }
//    public XMBAdapter(Context context, List<XMBItem> items) {
//        this.context = context;
//        this.items = items.toArray(new XMBItem[0]);
//        font = Typeface.createFromAsset(context.getAssets(), "Fonts/exo.regular.otf");
//    }
    public XMBAdapter(Context context, ArrayList<XMBItem> items) {
        this.context = context;
        this.items = new ArrayList<>(items);
        //QUESTION_MARK_DRAWABLE = ContextCompat.getDrawable(context, R.drawable.ic_baseline_question_mark_24);
        //font = SettingsKeeper.getFont();
        //if (SettingsKeeper.hasValue(SettingsKeeper.FONT_REF))
        //    font = ((FontRef)SettingsKeeper.getValue(SettingsKeeper.FONT_REF)).getFont();
        //font = Typeface.createFromAsset(context.getAssets(), "Fonts/exo.regular.otf");
    }

    @NonNull
    @Override
    public XMBViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View view = null;
        if (viewType == XMBView.CATEGORY_TYPE)
            view = createCatView();//layoutInflater.inflate(R.layout.xmb_cat, null);
        else if (viewType == XMBView.ITEM_TYPE)
            view = createItemView();//layoutInflater.inflate(R.layout.xmb_item, null);
        else if (viewType == XMBView.INNER_TYPE)
            view = createInnerItemView();//layoutInflater.inflate(R.layout.xmb_inner_item, null);
        return new XMBViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull XMBViewHolder holder, Integer... position) {
//        XMBItem item = null;
//        if (position[1] < items.get(position[0]).getInnerItemCount()) // empty item condition
//            item = (XMBItem)getItem(position);
        holder.bindItem((XMBItem)getItem(position));
    }
    @Override
    public int getItemCount(boolean withInnerItems) {
        int size = 0;
        for (XMBItem column : items) {
            if (column != null)
                size += column.getInnerItemCount();
            if (withInnerItems && column.hasInnerItems())
                for (XMBItem item : column.getInnerItems())
                    if (item != null)
                        size += item.getInnerItemCount();
        }
        return size;
    }

    @Override
    public int getColumnCount() {
        return items.size();
    }

    @Override
    public int getColumnSize(int columnIndex) {
        return items.get(columnIndex).getInnerItemCount();
    }

    @Override
    public void onViewAttachedToWindow(@NonNull XMBViewHolder holder) {

    }
    @Override
    public Object getItem(Integer... position) {
        XMBItem current = null;
        if (position != null && position.length > 0) {
            int index = position[0];
            current = items.get(index);
            for (int i = 1; i < position.length; i++) {
                index = position[i];
                current = current.getInnerItem(index);
            }
        }
        return current;
    }

    @Override
    public Integer[] getPosition(Object item) {
        List<Integer> position = new ArrayList<>();
        Function<XMBItem, Boolean> findInnerItem = new Function<XMBItem, Boolean>() {
            @Override
            public Boolean apply(XMBItem entryPoint) {
                boolean output = false;
                for (int i = 0; i < entryPoint.getInnerItemCount(); i++) {
                    XMBItem currentItem = entryPoint.getInnerItem(i);
                    if (currentItem == item) {
                        output = true;
                        position.add(i);
                        break;
                    } else {
                        output = apply(currentItem);
                        if (output) {
                            position.add(i);
                            break;
                        }
                    }
                }
                return output;
            }
        };
        for (int i = 0; i < items.size(); i++) {
            XMBItem currentItem = items.get(i);
            if (currentItem == item) {
                position.add(i);
                position.add(0);
                break;
            } else {
                boolean foundItem = findInnerItem.apply(currentItem);
                if (foundItem) {
                    position.add(i);
                    Collections.reverse(position);
                    break;
                }
            }
        }
        return position.size() > 0 ? position.toArray(new Integer[0]) : null;
    }

    @Override
    public ArrayList<Object> getItems() {
        ArrayList<Object> cloned = new ArrayList<>();
        for (int i = 0; i < items.size(); i++)
            cloned.add(items.get(i).clone());
        return cloned;
    }

    @Override
    public void setItems(ArrayList<Object> items) {
        this.items = new ArrayList<>();
        for (Object item : items)
            this.items.add((XMBItem)item);
    }

    @Override
    public boolean isColumnHead(Integer... position) {
        XMBItem item = (XMBItem)getItem(position);
        return item != null && (!(item instanceof HomeItem) || ((HomeItem)item).isColumnHead());
    }

    @Override
    public boolean canPlaceItemsIn(Integer... position) {
        XMBItem item = (XMBItem)getItem(position);
        return item != null && (!(item instanceof HomeItem) || (((HomeItem)item).isColumnHead() && ((HomeItem)item).type != HomeItem.Type.settings));
    }

    @Override
    public boolean hasInnerItems(Integer... position) {
        XMBItem current = (XMBItem)getItem(position);
        //Log.d("XMBView", "Checking if " + current.title + " has inner items? " + current.hasInnerItems());
        return (position.length == 2 || position.length > 2) && (current != null && current.hasInnerItems());
    }
    @Override
    public int getInnerItemCount(Integer... position) {
        XMBItem current = (XMBItem)getItem(position);
        return hasInnerItems(position) ? current.getInnerItemCount() : 0;
    }

    public class XMBViewHolder extends XMBView.ViewHolder {
        XMBItem prevItem;
        private final Runnable requestXmbRedraw = () -> {
            Integer[] position = getPosition(prevItem);
            if (position != null)
                fireInnerItemsChangedEvent(position);
            else
                Log.w("XMBAdapter", "Received inner items changed event for non-existent item");
        };
        private final Runnable drawItem = () -> {
                TextView title = itemView.findViewById(TITLE_ID);
                title.setVisibility(isHideTitleRequested() ? View.GONE : View.VISIBLE);
                title.setSelected(true);
                title.setTypeface(font);
                //AndroidHelpers.setTextAsync(title, item != null ? item.getTitle() : "Empty");
                title.setText(prevItem != null ? prevItem.getTitle() : "Empty");

                //ImageView superIcon = itemView.findViewById(R.id.typeSuperIcon);
                //superIcon.setVisibility(View.GONE);
                //superIcon.setVisibility(((HomeItem)item).type == HomeItem.Type.assoc ? View.VISIBLE : View.GONE);

                ImageView img = itemView.findViewById(ICON_ID);
                ImageView highlight = itemView.findViewById(ICON_HIGHLIGHT_ID);
                //Drawable icon = null;
                if (prevItem != null) {
//                img.setImageDrawable(QUESTION_MARK_DRAWABLE);
//                highlight.setImageDrawable(QUESTION_MARK_DRAWABLE);
                    prevItem.getIcon((Consumer<Drawable>) drawable -> {
                        if (drawable != null) {
                            //img.setBackground(drawable);
                            img.setImageDrawable(drawable);
                            highlight.setBackground(drawable.getConstantState().newDrawable());
                        } else {
                            //img.setBackground(ContextCompat.getDrawable(context, R.drawable.ic_baseline_question_mark_24));
                            img.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_baseline_question_mark_24));
                            highlight.setBackground(ContextCompat.getDrawable(context, R.drawable.ic_baseline_question_mark_24));
                        }
                    });
                } else {
                    //img.setBackground(ContextCompat.getDrawable(context, R.drawable.ic_baseline_block_24));
                    img.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_baseline_block_24));
                    highlight.setBackground(ContextCompat.getDrawable(context, R.drawable.ic_baseline_block_24));
                }
                //if (isHighlighted())
                //    Log.d("XMBAdapter", "Highlighting " + item.getTitle());
                highlight.setVisibility(isHighlighted() ? View.VISIBLE : View.INVISIBLE);
        };
        public XMBViewHolder(@NonNull View itemView) {
            super(itemView);
        }
        public void bindItem(XMBItem item) {
            if (isDirty || item != prevItem || item == null) {
                if (prevItem != null) {
                    prevItem.removeValuesChangedListener(drawItem);
                    prevItem.removeInnerItemsChangedListener(requestXmbRedraw);
                }
                if (item != null) {
                    item.addValuesChangedListener(drawItem);
                    item.addInnerItemsChangedListener(requestXmbRedraw);
                }
                isDirty = false;
                prevItem = item;
                drawItem.run();
            }
        }
    }

    @Override
    protected int getTextSize() {
        return Math.round(AndroidHelpers.getScaledSpToPixels(context, 6));
    }

    @Override
    public void setFont(Typeface font) {
        this.font = font;
    }

    @Override
    protected void shiftItemHorizontally(int toBeMovedColIndex, int toBeMovedLocalIndex, int moveToColIndex, int moveToLocalIndex, boolean createColumn) {
        // Log.d("XMBAdapter", "Moving item [" + toBeMovedColIndex + ", " + toBeMovedLocalIndex + "] => [" + moveToColIndex + ", " + moveToLocalIndex + "] Create column: " + createColumn);
        //XMBItem toBeMoved = items.get(toBeMovedColIndex).getInnerItem(toBeMovedLocalIndex - 1);
        XMBItem toBeMoved = (XMBItem)getItem(toBeMovedColIndex, toBeMovedLocalIndex);
        if (createColumn) {
//            removeSubItem(toBeMovedColIndex, toBeMovedLocalIndex, false);
            removeItem(false, toBeMovedColIndex, toBeMovedLocalIndex);
            createColumnAt(moveToColIndex, toBeMoved);
        } else {
            addSubItem(moveToColIndex, moveToLocalIndex, toBeMoved);
//            removeSubItem(toBeMovedColIndex, toBeMovedLocalIndex, false);
            removeItem(false, toBeMovedColIndex, toBeMovedLocalIndex);
        }
    }
    @Override
    protected void shiftItemVertically(int startColIndex, int fromLocalIndex, int toLocalIndex) {
        //XMBItem toBeMoved = items.get(startColIndex).getInnerItem(fromLocalIndex - 1);
        XMBItem toBeMoved = (XMBItem)getItem(startColIndex, fromLocalIndex);
//        removeSubItem(startColIndex, fromLocalIndex, false);
        removeItem(false, startColIndex, fromLocalIndex);
        addSubItem(startColIndex, toLocalIndex, toBeMoved);
    }
    @Override
    public void addSubItem(int columnIndex, int localIndex, Object toBeAdded) {
        XMBItem item = items.get(columnIndex);
        if (item.getInnerItemCount() <= 0)
            item.add((XMBItem)toBeAdded);
        else
            item.add(localIndex - 1, (XMBItem)toBeAdded);
        fireSubItemAddedEvent(columnIndex, localIndex);
    }
//    @Override
//    public void removeSubItem(int columnIndex, int localIndex) {
//        removeSubItem(columnIndex, localIndex, true);
//    }
//    private void removeSubItem(int columnIndex, int localIndex, boolean dispose) {
//        //Log.d("XMBAdapter", "Attempting to remove sub item from [" + columnIndex + ", " + localIndex + "]");
//        // the local index is expected to be 0 for the column head then anything greater is an inner item, and since they are in a list of their own, we need to subtract by 1 to account for that
//        if (localIndex > 0) {
////            XMBItem toBeRemoved = items.get(columnIndex).getInnerItem(localIndex - 1);
////            if (toBeRemoved != null && dispose) {
////                //toBeRemoved.clearImgCache();
////                //toBeRemoved.applyToInnerItems((Consumer<XMBItem>)XMBItem::clearImgCache, true);
////                toBeRemoved.release();
////            }
//            items.get(columnIndex).remove(localIndex - 1, dispose);
//            fireSubItemRemovedEvent(columnIndex, localIndex);
//        }
//        if (localIndex == 0)
//            removeColumnAt(columnIndex, dispose);
//            //removeColIfEmpty(columnIndex);
//    }
    @Override
    public void removeItem(Integer... position) {
        removeItem(true, position);
    }
    public void removeItem(boolean dispose, Integer... position) {
        if (position.length > 1) {
            // remove sub-item or inner item
            Integer[] parent = new Integer[position.length - 1];
            for (int i = 0; i < parent.length; i++) {
                parent[i] = position[i];
            }
            ((XMBItem)getItem(parent)).remove(position[position.length - 1]);
        } else {
            // remove column
            int columnIndex = position[0];
            XMBItem toBeRemoved = items.get(columnIndex);
            if (toBeRemoved != null && dispose)
                toBeRemoved.release();
            items.remove(columnIndex);
        }
        fireItemRemoved(position);
    }
    @Override
    public void createColumnAt(int columnIndex, Object head) {
        //ArrayList<XMBItem> newColumn = new ArrayList<>();
        //newColumn.add((XMBItem)head);
        items.add(columnIndex, (XMBItem)head);
        fireColumnAddedEvent(columnIndex);
        fireSubItemAddedEvent(columnIndex, 0);
    }
//    @Override
//    public void removeColumnAt(int columnIndex) {
//        removeColumnAt(columnIndex, true);
//    }
//    private void removeColumnAt(int columnIndex, boolean dispose) {
//        XMBItem toBeRemoved = items.get(columnIndex);
//        if (toBeRemoved != null && dispose)
//            toBeRemoved.release();
//        items.remove(columnIndex);
//        fireColumnRemovedEvent(columnIndex);
//    }

//    @Override
//    public void shiftColumnTo(int fromColIndex, int toColIndex) {
//        Log.d("XMBAdapter", "Shifting " + fromColIndex + " => " + toColIndex);
//        XMBItem origItems = items.get(fromColIndex);
//        if (toColIndex > fromColIndex) {
//            items.add(toColIndex + 1, origItems);
//            items.remove(fromColIndex);
//        } else {
//            items.remove(fromColIndex);
//            items.add(toColIndex, origItems);
//        }
//        fireColumnShiftedEvent(fromColIndex, toColIndex);
//    }

//    private void removeColIfEmpty(int columnIndex) {
//        if (items.get(columnIndex).getInnerItemCount() <= 0)
//            removeColumnAt(columnIndex);
//    }
    private void fireColumnAddedEvent(int columnIndex) {
        for (XMBAdapterListener listener : listeners)
            if (listener != null)
                listener.onColumnAdded(columnIndex);
    }
//    private void fireColumnRemovedEvent(int columnIndex) {
//        for (XMBAdapterListener listener : listeners)
//            if (listener != null)
//                listener.onColumnRemoved(columnIndex);
//    }
    private void fireSubItemAddedEvent(int columnIndex, int localIndex) {
        for (XMBAdapterListener listener : listeners)
            if (listener != null)
                listener.onSubItemAdded(columnIndex, localIndex);
    }
//    private void fireSubItemRemovedEvent(int columnIndex, int localIndex) {
//        for (XMBAdapterListener listener : listeners)
//            if (listener != null)
//                listener.onSubItemRemoved(columnIndex, localIndex);
//    }
    private void fireColumnShiftedEvent(int fromColIndex, int toColIndex) {
        for (XMBAdapterListener listener : listeners)
            if (listener != null)
                listener.onColumnShifted(fromColIndex, toColIndex);
    }
    private void fireInnerItemsChangedEvent(Integer... position) {
        for (XMBAdapterListener listener : listeners)
            if (listener != null)
                listener.onInnerItemsChanged(position);
    }
    private void fireItemRemoved(Integer... position) {
        for (XMBAdapterListener listener : listeners)
            if (listener != null)
                listener.onItemRemoved(position);
    }

    private View createCatView() {
        float scale = 1;
        if (SettingsKeeper.hasValue(SettingsKeeper.HOME_ITEM_SCALE))
            scale *= (Float)SettingsKeeper.getValue(SettingsKeeper.HOME_ITEM_SCALE);
        int catSize = Math.round(AndroidHelpers.getScaledDpToPixels(context, 64) * scale);
        int iconSize = catSize - Math.round(AndroidHelpers.getScaledDpToPixels(context, 4) * scale);
        int textSize = getTextSize();
        int textOutlineSize = Math.round(AndroidHelpers.getScaledDpToPixels(context, 3) * scale);

        RelativeLayout catView = new RelativeLayout(context);
        RelativeLayout.LayoutParams relativeParams = new RelativeLayout.LayoutParams(catSize, catSize);
        catView.setLayoutParams(relativeParams);
        catView.setClipChildren(false);

        FrameLayout.LayoutParams frameParams;

        FrameLayout backFrame = new FrameLayout(context);
        frameParams = new FrameLayout.LayoutParams(catSize, catSize);
        backFrame.setLayoutParams(frameParams);
        catView.addView(backFrame);

        ImageView highlighter = new ImageView(context);
        highlighter.setId(ICON_HIGHLIGHT_ID);
        frameParams = new FrameLayout.LayoutParams(catSize, catSize);
        frameParams.gravity = Gravity.TOP | Gravity.LEFT;
        highlighter.setLayoutParams(frameParams);
        highlighter.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FFFF00")));
        highlighter.setBackgroundTintMode(PorterDuff.Mode.SRC_ATOP);
        highlighter.setScaleType(ImageView.ScaleType.CENTER_CROP);
        highlighter.setVisibility(View.INVISIBLE);
        backFrame.addView(highlighter);

        BetterTextView title = new BetterTextView(context);
        title.setId(TITLE_ID);
        frameParams = new FrameLayout.LayoutParams(catSize, ViewGroup.LayoutParams.WRAP_CONTENT);
        frameParams.gravity = Gravity.CENTER_HORIZONTAL;
        title.setLayoutParams(frameParams);
        title.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        title.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.TOP);
        title.setMarqueeRepeatLimit(-1);
        title.setSingleLine(true);
        title.setTextAlignment(View.TEXT_ALIGNMENT_GRAVITY);
        title.setTextColor(context.getColor(R.color.text));
        title.setTextSize(textSize);
        title.setTranslationY(catSize);
        title.setOutlineColor(Color.BLACK);
        title.setOutlineSize(textOutlineSize);
        backFrame.addView(title);

        FrameLayout frontFrame = new FrameLayout(context);
        frameParams = new FrameLayout.LayoutParams(catSize, catSize);
        frontFrame.setLayoutParams(frameParams);
        catView.addView(frontFrame);

        ImageView icon = new ImageView(context);
        icon.setId(ICON_ID);
        frameParams = new FrameLayout.LayoutParams(iconSize, iconSize);
        frameParams.gravity = Gravity.CENTER;
        icon.setLayoutParams(frameParams);
        icon.setScaleType(ImageView.ScaleType.CENTER_CROP);
        frontFrame.addView(icon);

        return catView;
    }
    private View createItemView() {
        float scale = 1;
        if (SettingsKeeper.hasValue(SettingsKeeper.HOME_ITEM_SCALE))
            scale *= (Float)SettingsKeeper.getValue(SettingsKeeper.HOME_ITEM_SCALE);
        int itemSize = Math.round(AndroidHelpers.getScaledDpToPixels(context, 48) * scale);
        int iconSize = itemSize - Math.round(AndroidHelpers.getScaledDpToPixels(context, 4) * scale);
        int textSize = getTextSize();
        int textWidth = Math.round(AndroidHelpers.getScaledDpToPixels(context, 128) * scale);
        int textOutlineSize = Math.round(AndroidHelpers.getScaledDpToPixels(context, 3) * scale);
        int textStartMargin = Math.round(AndroidHelpers.getScaledDpToPixels(context, 6) * scale);

        RelativeLayout itemView = new RelativeLayout(context);
        RelativeLayout.LayoutParams relativeParams = new RelativeLayout.LayoutParams(itemSize, itemSize);
        itemView.setLayoutParams(relativeParams);
        itemView.setClipChildren(false);

        FrameLayout.LayoutParams frameParams;

        FrameLayout backFrame = new FrameLayout(context);
        frameParams = new FrameLayout.LayoutParams(itemSize, itemSize);
        backFrame.setLayoutParams(frameParams);
        itemView.addView(backFrame);

        ImageView highlighter = new ImageView(context);
        highlighter.setId(ICON_HIGHLIGHT_ID);
        frameParams = new FrameLayout.LayoutParams(itemSize, itemSize);
        frameParams.gravity = Gravity.TOP | Gravity.LEFT;
        highlighter.setLayoutParams(frameParams);
        highlighter.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FFFF00")));
        highlighter.setBackgroundTintMode(PorterDuff.Mode.SRC_ATOP);
        highlighter.setScaleType(ImageView.ScaleType.CENTER_CROP);
        highlighter.setVisibility(View.INVISIBLE);
        backFrame.addView(highlighter);

        BetterTextView title = new BetterTextView(context);
        title.setId(TITLE_ID);
        frameParams = new FrameLayout.LayoutParams(textWidth, ViewGroup.LayoutParams.WRAP_CONTENT);
        frameParams.gravity = Gravity.CENTER_VERTICAL;
        frameParams.setMarginStart(textStartMargin);
        title.setLayoutParams(frameParams);
        title.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        title.setGravity(Gravity.CENTER_VERTICAL | Gravity.START);
        title.setMarqueeRepeatLimit(-1);
        title.setSingleLine(true);
        title.setTextAlignment(View.TEXT_ALIGNMENT_GRAVITY);
        title.setTextColor(context.getColor(R.color.text));
        title.setTextSize(textSize);
        title.setTranslationX(itemSize);
        title.setOutlineColor(Color.parseColor("#000000"));
        title.setOutlineSize(textOutlineSize);
        backFrame.addView(title);

        FrameLayout frontFrame = new FrameLayout(context);
        frameParams = new FrameLayout.LayoutParams(itemSize, itemSize);
        frontFrame.setLayoutParams(frameParams);
        itemView.addView(frontFrame);

        ImageView icon = new ImageView(context);
        icon.setId(ICON_ID);
        frameParams = new FrameLayout.LayoutParams(iconSize, iconSize);
        frameParams.gravity = Gravity.CENTER;
        icon.setLayoutParams(frameParams);
        icon.setScaleType(ImageView.ScaleType.CENTER_CROP);
        frontFrame.addView(icon);

        return itemView;
    }
    private View createInnerItemView() {
        float scale = 1;
        if (SettingsKeeper.hasValue(SettingsKeeper.HOME_ITEM_SCALE))
            scale *= (Float)SettingsKeeper.getValue(SettingsKeeper.HOME_ITEM_SCALE);
        int innerItemSize = Math.round(AndroidHelpers.getScaledDpToPixels(context, 32) * scale);
        int iconSize = innerItemSize - Math.round(AndroidHelpers.getScaledDpToPixels(context, 4) * scale);
        int textSize = getTextSize();
        int textWidth = Math.round(AndroidHelpers.getScaledDpToPixels(context, 256) * scale);
        int textOutlineSize = Math.round(AndroidHelpers.getScaledDpToPixels(context, 3) * scale);
        int textStartMargin = Math.round(AndroidHelpers.getScaledDpToPixels(context, 6) * scale);

        RelativeLayout innerItemView = new RelativeLayout(context);
        RelativeLayout.LayoutParams relativeParams = new RelativeLayout.LayoutParams(innerItemSize, innerItemSize);
        innerItemView.setLayoutParams(relativeParams);
        innerItemView.setClipChildren(false);

        FrameLayout.LayoutParams frameParams;

        FrameLayout backFrame = new FrameLayout(context);
        frameParams = new FrameLayout.LayoutParams(innerItemSize, innerItemSize);
        backFrame.setLayoutParams(frameParams);
        innerItemView.addView(backFrame);

        ImageView highlighter = new ImageView(context);
        highlighter.setId(ICON_HIGHLIGHT_ID);
        frameParams = new FrameLayout.LayoutParams(innerItemSize, innerItemSize);
        frameParams.gravity = Gravity.TOP | Gravity.LEFT;
        highlighter.setLayoutParams(frameParams);
        highlighter.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FFFF00")));
        highlighter.setBackgroundTintMode(PorterDuff.Mode.SRC_ATOP);
        highlighter.setScaleType(ImageView.ScaleType.CENTER_CROP);
        highlighter.setVisibility(View.INVISIBLE);
        backFrame.addView(highlighter);

        ImageView icon = new ImageView(context);
        icon.setId(ICON_ID);
        frameParams = new FrameLayout.LayoutParams(iconSize, iconSize);
        frameParams.gravity = Gravity.CENTER;
        icon.setLayoutParams(frameParams);
        icon.setScaleType(ImageView.ScaleType.CENTER_CROP);
        backFrame.addView(icon);

        BetterTextView title = new BetterTextView(context);
        title.setId(TITLE_ID);
        frameParams = new FrameLayout.LayoutParams(textWidth, ViewGroup.LayoutParams.WRAP_CONTENT);
        frameParams.gravity = Gravity.CENTER_VERTICAL;
        frameParams.setMarginStart(textStartMargin);
        title.setLayoutParams(frameParams);
        title.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        title.setGravity(Gravity.CENTER_VERTICAL | Gravity.START);
        title.setMarqueeRepeatLimit(-1);
        title.setSingleLine(true);
        title.setTextAlignment(View.TEXT_ALIGNMENT_GRAVITY);
        title.setTextColor(context.getColor(R.color.text));
        title.setTextSize(textSize);
        title.setTranslationX(innerItemSize);
        title.setOutlineColor(Color.parseColor("#000000"));
        title.setOutlineSize(textOutlineSize);
        backFrame.addView(title);

        return innerItemView;
    }
}
