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

import com.OxGames.OxShell.Data.XMBItem;
import com.OxGames.OxShell.R;
import com.OxGames.OxShell.Views.XMBView;

import java.util.List;

public class XMBAdapter extends XMBView.Adapter<XMBAdapter.XMBViewHolder> {
    private Context context;
    private XMBItem[] items;
    private Typeface font;

    public XMBAdapter(Context context, XMBItem... items) {
        this.context = context;
        this.items = items.clone();
        font = Typeface.createFromAsset(context.getAssets(), "Fonts/exo.regular.otf");
    }
    public XMBAdapter(Context context, List<XMBItem> items) {
        this.context = context;
        this.items = items.toArray(new XMBItem[0]);
        font = Typeface.createFromAsset(context.getAssets(), "Fonts/exo.regular.otf");
    }

    @NonNull
    @Override
    public XMBViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View view = null;
        if (viewType == XMBView.CATEGORY_TYPE)
            view = layoutInflater.inflate(R.layout.xmb_cat, null);
        else if (viewType == XMBView.ITEM_TYPE)
            view = layoutInflater.inflate(R.layout.xmb_item, null);
//        XMBItemView view = new XMBItemView(context);
//        view.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        return new XMBViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull XMBViewHolder holder, Integer... position) {
        XMBItem item = (XMBItem)getItem(position);
        //Log.d("XMBAdapter", "Binding " + item.title + " to " + position[0]);
        holder.bindItem(item);
    }
    @Override
    public int getItemCount() {
        return items.length;
    }
    @Override
    public void onViewAttachedToWindow(@NonNull XMBViewHolder holder) {

    }
    @Override
    public Object getItem(Integer... position) {
        XMBItem current = null;
        if (position != null && position.length > 0) {
            current = (XMBItem)items[position[0]];
            for (int i = 1; i < position.length; i++)
                current = current.getInnerItem(position[i]);
        }
        return current;
    }

    @Override
    public boolean hasInnerItems(Integer... position) {
        XMBItem current = (XMBItem)getItem(position);
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
            title.setText(item.title);
            title.setSelected(true);
            title.setTypeface(font);
            title.setVisibility(isHideTitleRequested() ? View.GONE : View.VISIBLE);

            //ImageView superIcon = itemView.findViewById(R.id.typeSuperIcon);
            //superIcon.setVisibility(View.GONE);
            //superIcon.setVisibility(((HomeItem)item).type == HomeItem.Type.assoc ? View.VISIBLE : View.GONE);

            ImageView img = itemView.findViewById(R.id.typeIcon);
            ImageView highlight = itemView.findViewById(R.id.iconGlow);
            Drawable icon = item.getIcon();
            if (icon == null)
                icon = getItemViewType() == XMBView.CATEGORY_TYPE ? ContextCompat.getDrawable(context, R.drawable.ic_baseline_view_list_24) : ContextCompat.getDrawable(context, R.drawable.ic_baseline_hide_image_24);
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
}
