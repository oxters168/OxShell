package com.OxGames.OxShell.Adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

import com.OxGames.OxShell.Data.XMBItem;
import com.OxGames.OxShell.Views.XMBItemView;
import com.OxGames.OxShell.Views.XMBView;

import java.util.List;

public class XMBAdapter extends XMBView.Adapter<XMBAdapter.XMBViewHolder> {
    private Context context;
    private XMBItem[] items;

    public XMBAdapter(Context context, XMBItem... items) {
        this.context = context;
        this.items = items.clone();
    }
    public XMBAdapter(Context context, List<XMBItem> items) {
        this.context = context;
        this.items = items.toArray(new XMBItem[0]);
    }

    @NonNull
    @Override
    public XMBViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        XMBItemView view = new XMBItemView(context);
        view.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        return new XMBViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull XMBViewHolder holder, int position) {
        XMBItem item = items[position];
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
    public Object getItem(int position) {
        return items[position];
    }

    public class XMBViewHolder extends XMBView.ViewHolder {
        public XMBViewHolder(@NonNull View itemView) {
            super(itemView);
        }
        public void bindItem(XMBItem item) {
            XMBItemView xmbItemView = (XMBItemView)itemView;
            xmbItemView.isCategory = isCategory();
            xmbItemView.title = item.title;
            xmbItemView.icon = item.getIcon();
        }
    }
}
