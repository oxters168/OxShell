package com.OxGames.OxShell.Adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.OxGames.OxShell.Data.HomeItem;
import com.OxGames.OxShell.Data.XMBItem;
import com.OxGames.OxShell.Interfaces.XMBAdapterListener;
import com.OxGames.OxShell.R;
import com.OxGames.OxShell.Views.XMBView;

import java.util.ArrayList;
import java.util.List;

public class XMBAdapter extends XMBView.Adapter<XMBAdapter.XMBViewHolder> {
    private Context context;
    private ArrayList<ArrayList<XMBItem>> items;
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
    public XMBAdapter(Context context, ArrayList<ArrayList<XMBItem>> items) {
        this.context = context;
        //this.items = items;
        this.items = new ArrayList<>();
        for (ArrayList<XMBItem> column : items) {
            this.items.add((ArrayList<XMBItem>)column.clone());
        }
        font = Typeface.createFromAsset(context.getAssets(), "Fonts/exo.regular.otf");
    }

//    protected void moveItem(int fromColIndex, int fromLocalIndex, int toColIndex, int toLocalIndex) {
//        boolean hasSubItems = catHasSubItems(toColIndex);
//        XMBItem moveItem = allHomeItems.get(fromColIndex).get(fromLocalIndex);
//
//        HomeManager.removeItemAt(fromColIndex, fromLocalIndex, false);
//        if (hasSubItems)
//            HomeManager.addItemTo(moveItem, toColIndex, toLocalIndex, false);
//        else
//            HomeManager.addItemAt(moveItem, toColIndex, false);
//        refresh();
//    }

    @NonNull
    @Override
    public XMBViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View view = null;
        if (viewType == XMBView.CATEGORY_TYPE)
            view = layoutInflater.inflate(R.layout.xmb_cat, null);
        else if (viewType == XMBView.ITEM_TYPE)
            view = layoutInflater.inflate(R.layout.xmb_item, null);
        else if (viewType == XMBView.INNER_TYPE)
            view = layoutInflater.inflate(R.layout.xmb_inner_item, null);
//        XMBItemView view = new XMBItemView(context);
//        view.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        return new XMBViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull XMBViewHolder holder, Integer... position) {
        XMBItem item = null;
        if (position[1] < items.get(position[0]).size()) // empty item condition
            item = (XMBItem)getItem(position);
        holder.bindItem(item);
    }
    @Override
    public int getItemCount(boolean withInnerItems) {
        int size = 0;
        for (List<XMBItem> column : items) {
            if (column != null)
                size += column.size();
            if (withInnerItems)
                for (XMBItem item : column)
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
        return items.get(columnIndex).size();
    }

    @Override
    public void onViewAttachedToWindow(@NonNull XMBViewHolder holder) {

    }
    @Override
    public Object getItem(Integer... position) {
        XMBItem current = null;
        if (position != null && position.length > 0) {
            current = (XMBItem)items.get(position[0]).get(position[1]);
            for (int i = 2; i < position.length; i++)
                current = current.getInnerItem(position[i]);
        }
        return current;
    }

    @Override
    public boolean isColumnHead(Integer... position) {
        XMBItem item = (XMBItem)getItem(position);
        return item.obj == null && !(item instanceof HomeItem);
    }

    @Override
    public boolean hasInnerItems(Integer... position) {
        XMBItem current = (XMBItem)getItem(position);
        //Log.d("XMBView", "Checking if " + current.title + " has inner items? " + current.hasInnerItems());
        return current != null && current.hasInnerItems();
    }
    @Override
    public int getInnerItemCount(Integer... position) {
        XMBItem current = (XMBItem)getItem(position);
        return current != null ? current.getInnerItemCount() : 0;
    }

    public class XMBViewHolder extends XMBView.ViewHolder {
        public XMBViewHolder(@NonNull View itemView) {
            super(itemView);
        }
        public void bindItem(XMBItem item) {
            TextView title = itemView.findViewById(R.id.title);
            title.setText(item != null ? item.title : "Empty");
            title.setSelected(true);
            title.setTypeface(font);
            title.setVisibility(isHideTitleRequested() ? View.GONE : View.VISIBLE);

            //ImageView superIcon = itemView.findViewById(R.id.typeSuperIcon);
            //superIcon.setVisibility(View.GONE);
            //superIcon.setVisibility(((HomeItem)item).type == HomeItem.Type.assoc ? View.VISIBLE : View.GONE);

            ImageView img = itemView.findViewById(R.id.typeIcon);
            ImageView highlight = itemView.findViewById(R.id.iconGlow);
            Drawable icon = item != null ? item.getIcon() : ContextCompat.getDrawable(context, R.drawable.ic_baseline_block_24);
            if (icon == null)
                icon = ContextCompat.getDrawable(context, R.drawable.ic_baseline_question_mark_24);
//                switch (getItemViewType()) {
//                    case (XMBView.CATEGORY_TYPE):
//                        icon = ContextCompat.getDrawable(context, R.drawable.ic_baseline_view_list_24);
//                        break;
//                    case (XMBView.ITEM_TYPE):
//                        icon = ContextCompat.getDrawable(context, R.drawable.ic_baseline_hide_image_24);
//                        break;
//                    case (XMBView.INNER_TYPE):
//                        icon = ContextCompat.getDrawable(context, R.drawable.ic_baseline_construction_24);
//                        break;
//                    default:
//                        icon = ContextCompat.getDrawable(context, R.drawable.ic_baseline_question_mark_24);
//                        break;
//                }
            img.setBackground(icon);
            highlight.setBackground(icon.getConstantState().newDrawable());
            highlight.setVisibility(isHighlighted() ? View.VISIBLE : View.INVISIBLE);
            //highlight.setBackgroundTintList(ColorStateList.valueOf(isSelection() ? highlightColor : nonHighlightColor));
            //highlight.setColorFilter(isSelection() ? Color.parseColor("#FFFF0000") : Color.parseColor("#FF000000"));
            //highlight.setVisibility(isSelection() ? View.VISIBLE : View.INVISIBLE);
//            XMBItemView xmbItemView = (XMBItemView)itemView;
//            xmbItemView.isCategory = isCategory();
//            xmbItemView.title = item.title;
//            xmbItemView.icon = item.getIcon();
        }
    }

    public void shiftHorizontally(int toBeMovedColIndex, int toBeMovedLocalIndex, int moveToColIndex, int moveToLocalIndex, boolean createColumn) {
        Log.d("XMBAdapter", "Moving item [" + toBeMovedColIndex + ", " + toBeMovedLocalIndex + "] => [" + moveToColIndex + ", " + moveToLocalIndex + "] Create column: " + createColumn);
        ArrayList<XMBItem> originColumn = items.get(toBeMovedColIndex);
        XMBItem toBeMoved = originColumn.get(toBeMovedLocalIndex);
        if (createColumn) {
            ArrayList<XMBItem> newColumn = new ArrayList<>();
            newColumn.add(toBeMoved);
            items.add(moveToColIndex, newColumn);
            // fire event that says a new column was added
            for (XMBAdapterListener listener : listeners)
                if (listener != null)
                    listener.onColumnAdded(moveToColIndex);
            originColumn.remove(toBeMovedLocalIndex);
            // TODO: fire event that says an item was removed from a column
        } else {
            ArrayList<XMBItem> moveToColumn = items.get(moveToColIndex);
            moveToColumn.add(moveToLocalIndex, toBeMoved);
            // TODO: fire event that says an item was added to an existing column
            originColumn.remove(toBeMovedLocalIndex);
            // TODO: fire event that says an item was removed from a column
        }
        if (originColumn.size() <= 0) {
            items.remove(originColumn);
            // fire event that says a column was removed
            for (XMBAdapterListener listener : listeners)
                if (listener != null)
                    listener.onColumnRemoved(toBeMovedColIndex);
        }
    }
//    public void shiftVertically() {
//        // moving vertically
//        ArrayList<XMBItem> column = items.get(toBeMovedColIndex);
//        XMBItem toBeMoved = column.get(toBeMovedLocalIndex);
//        column.remove(toBeMovedLocalIndex);
//        // TODO: fire event that says an item was removed from an existing column
//        column.add(moveToColIndex, toBeMoved);
//        // TODO: fire event that says an item was added to an existing column
//    }
}
