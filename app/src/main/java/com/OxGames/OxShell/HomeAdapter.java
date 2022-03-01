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
//    public class HomeItem {
////        public enum Type { none, explorer, app, }
//        com.OxGames.OxShell.HomeItem.Type type;
//        String packageName;
//        private HomeItemHolder itemHolder;
////    View view;
//
//        public HomeItem(com.OxGames.OxShell.HomeItem.Type _type, String _packageName) {
//            type = _type;
//            packageName = _packageName;
//        }
//        public Drawable GetIcon() {
//            Drawable icon = null;
//            if (type == com.OxGames.OxShell.HomeItem.Type.explorer)
//                icon = ContextCompat.getDrawable(HomeActivity.GetInstance(), R.drawable.ic_baseline_source_24);
//            return icon;
//        }
//        public View GetView() {
//            return itemHolder.itemView;
//        }
//    }

    private final Context context;
    private final ArrayList<HomeItem> homeItems;

    public HomeAdapter(Context _context, ArrayList<HomeItem> _homeItems) {
        context = _context;
        homeItems = _homeItems;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        HomeItem homeItem = homeItems.get(position);
        View child = parent.getChildAt(position);
        if (view == null) {
            LayoutInflater layoutInflater = LayoutInflater.from(context);
            view = layoutInflater.inflate(R.layout.home_cell, null);
            homeItem.view = view;
//            HomeItemHolder itemHolder = new HomeItemHolder(view);
//            view.setTag(itemHolder);
//            homeItem.holder = itemHolder;

            TextView title = view.findViewById(R.id.title);
            title.setText(homeItem.packageName);

            ImageView typeIcon = view.findViewById(R.id.typeIcon);
            typeIcon.setImageDrawable(homeItem.GetIcon());
        }
        else
            homeItem.view = parent.getChildAt(position);
//        else
//            homeItem.holder = (HomeItemHolder)view.getTag();
        Log.d("HomeAdapter", position + " " + view + " " + child);

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
