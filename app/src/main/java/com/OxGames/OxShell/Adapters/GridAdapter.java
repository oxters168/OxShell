package com.OxGames.OxShell.Adapters;

import android.database.DataSetObserver;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.widget.BaseAdapter;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.OxGames.OxShell.Data.GridItem;
import com.OxGames.OxShell.R;

import java.util.ArrayList;

public class GridAdapter extends BaseAdapter {
    private final Context context;
    private final ArrayList<GridItem> homeItems;

    public GridAdapter(Context _context, ArrayList<GridItem> _homeItems) {
        context = _context;
        homeItems = _homeItems;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        GridItem homeItem = homeItems.get(position);
        if (view == null) {
            LayoutInflater layoutInflater = LayoutInflater.from(context);
            view = layoutInflater.inflate(R.layout.grid_cell, null);
        }

        view.setBackgroundColor(homeItem.isSelected ? Color.parseColor("#33EAF0CE") : Color.parseColor("#00000000")); //TODO: implement color theme that can take custom theme from file

        TextView title = view.findViewById(R.id.cat_title);
        title.setText(homeItem.title);

        ImageView typeIcon = view.findViewById(R.id.typeIcon);
        typeIcon.setImageDrawable(homeItem.getIcon());

        ImageView typeSuperIcon = view.findViewById(R.id.typeSuperIcon);
        typeSuperIcon.setImageDrawable(homeItem.getSuperIcon());

        //int[] loc = new int[2];
        //view.getLocationInWindow(loc);
        //view.getLocationOnScreen(loc);
//        int x = AndroidHelpers.getRelativeLeft(view);
//        int y = AndroidHelpers.getRelativeTop(view);
//        int width = view.getWidth();
//        int height = view.getHeight();
//        homeItem.dim = new Rect(x, y, x + width, y + height);

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
        return homeItems.size();
    }
    @Override
    public long getItemId(int position) {
        return position;
    }
    @Override
    public Object getItem(int position) {
        return homeItems.get(position);
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
        return homeItems.size();
    }
    @Override
    public boolean isEmpty() {
        return homeItems.size() <= 0;
    }
}
