package com.OxGames.OxShell;

import android.database.DataSetObserver;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.BaseAdapter;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class HomeAdapter extends BaseAdapter {
    private final Context context;
    private final ArrayList<HomeItem> homeItems;

    public HomeAdapter(Context _context, ArrayList<HomeItem> _homeItems) {
        context = _context;
        homeItems = _homeItems;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        HomeItem homeItem = homeItems.get(position);
        if (view == null) {
            LayoutInflater layoutInflater = LayoutInflater.from(context);
            view = layoutInflater.inflate(R.layout.home_cell, null);
            homeItem.view = view;

            TextView title = view.findViewById(R.id.title);
            title.setText(homeItem.packageName);

            ImageView typeIcon = view.findViewById(R.id.typeIcon);
            typeIcon.setImageDrawable(homeItem.GetIcon());
        }
        else
            homeItem.view = parent.getChildAt(position);

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
        return position;
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
