package com.OxGames.OxShell;

import android.view.LayoutInflater;
import android.widget.BaseAdapter;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class HomeAdapter extends BaseAdapter {
    private final Context context;
    private final HomeItem[] homeItems;

    public HomeAdapter(Context _context, HomeItem[] _homeItems) {
        context = _context;
        homeItems = _homeItems;
    }

    @Override
    public int getCount() {
        return homeItems.length;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public Object getItem(int position) {
        return homeItems[position];
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        HomeItem homeItem = homeItems[position];

        LayoutInflater layoutInflater = LayoutInflater.from(context);
        view = layoutInflater.inflate(R.layout.home_cell, null);
        homeItem.view = view;

        TextView title = view.findViewById(R.id.title);
        title.setText(homeItems[position].packageName);

        ImageView typeIcon = view.findViewById(R.id.typeIcon);
        typeIcon.setImageDrawable(homeItem.GetIcon());
        return view;
    }
}
