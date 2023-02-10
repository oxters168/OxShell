package com.OxGames.OxShell.Adapters;

import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.OxGames.OxShell.Data.DetailItem;
import com.OxGames.OxShell.R;

import java.util.ArrayList;
import java.util.List;

public class DetailAdapter implements ListAdapter {
    Context context;
    List<DetailItem> detailItems;
    //private int highlightedIndex = -1;
    private final int currentItemColor = Color.parseColor("#33EAF0CE");
    private final int selectedColor = Color.parseColor("#33CEEAF0");
    private final int currentAndSelectedColor = Color.parseColor("#33F0CEEA");
    private final int noneColor = Color.parseColor("#00000000");
//    boolean hideExtensions;

    public DetailAdapter(Context _context) {
        context = _context;
        detailItems = new ArrayList<>();
    }
    public DetailAdapter(Context _context, List<DetailItem> _detailItems) {
        context = _context;
        detailItems = _detailItems;
//        hideExtensions = _hideExtensions;
    }

    public void add(DetailItem detailItem) {
        detailItems.add(detailItem);
        //notifyDataSetChanged();
    }

//    public void setHighlightedIndex(int index) {
//        highlightedIndex = index;
//    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        DetailItem detailItem = detailItems.get(position);
        //Log.d("DetailAdapter", "Item at " + position + ": " + detailItem.leftAlignedText);
        if (view == null) {
            LayoutInflater layoutInflater = LayoutInflater.from(context);
            view = layoutInflater.inflate(R.layout.detail_row, null);
        }

        int color = noneColor;
        if (detailItem.isCurrentItem && detailItem.isSelected)
            color = currentAndSelectedColor;
        else if (detailItem.isCurrentItem)
            color = currentItemColor;
        else if (detailItem.isSelected)
            color = selectedColor;
        view.setBackgroundColor(color); //TODO: implement color theme that can take custom theme from file

        TextView title = view.findViewById(R.id.title);
        title.setText(detailItem.leftAlignedText);

        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) title.getLayoutParams();
        params.setMargins(detailItem.hasIcon() ? 40 : 0, 0, 0, 0);

        TextView rightText = view.findViewById(R.id.isDir);
        rightText.setVisibility(detailItem.rightAlignedText != null && !detailItem.rightAlignedText.isEmpty() ? View.VISIBLE : View.INVISIBLE);
        rightText.setText(detailItem.rightAlignedText);

        ImageView typeIcon = view.findViewById(R.id.typeIcon);
        typeIcon.setImageDrawable(detailItem.getIcon());
        typeIcon.setVisibility(detailItem.hasIcon() ? View.VISIBLE : View.GONE);

        return view;
    }
    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }
    @Override
    public boolean isEnabled(int position) {
        return true;
    }
    @Override
    public void registerDataSetObserver(DataSetObserver observer) { }
    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) { }
    @Override
    public int getCount() {
        return detailItems.size();
    }
    @Override
    public Object getItem(int position) {
        return detailItems.get(position);
    }
    @Override
    public long getItemId(int position) {
        return position;
    }
    @Override
    public boolean hasStableIds() {
        return false;
    }
    @Override
    public int getItemViewType(int position) {
        return 0;
    }
    @Override
    public int getViewTypeCount() {
        //return detailItems.size();
        return 1;
    }
    @Override
    public boolean isEmpty() {
        return detailItems.size() <= 0;
    }
}
