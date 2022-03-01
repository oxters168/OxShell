package com.OxGames.OxShell;

import android.content.Context;
import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class ExplorerAdapter implements ListAdapter {
    Context context;
    ArrayList<ExplorerItem> arrayList;

    public ExplorerAdapter(Context context, ArrayList<ExplorerItem> arrayList) {
        this.arrayList = arrayList;
        this.context = context;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        if (view == null) {
            ExplorerItem explorerItem = arrayList.get(position);
            //I think this is when the view is being initialized for the first time
            LayoutInflater layoutInflater = LayoutInflater.from(context);
            view = layoutInflater.inflate(R.layout.explorer_row, null);
            explorerItem.view = view;

            TextView title = view.findViewById(R.id.title);
            title.setText(explorerItem.name);

            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) title.getLayoutParams();
            params.setMargins(explorerItem.HasIcon() ? 40 : 0, 0, 0, 0);

            TextView isDirText = view.findViewById(R.id.isDir);
            isDirText.setVisibility(explorerItem.isDir ? View.VISIBLE : View.INVISIBLE);

            ImageView typeIcon = view.findViewById(R.id.typeIcon);
            typeIcon.setImageDrawable(explorerItem.GetIcon());
            typeIcon.setVisibility(explorerItem.HasIcon() ? View.VISIBLE : View.GONE);
        }

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
        return arrayList.size();
    }
    @Override
    public Object getItem(int position) {
        return arrayList.get(position);
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
        return position;
    }
    @Override
    public int getViewTypeCount() {
        return arrayList.size();
    }
    @Override
    public boolean isEmpty() {
        return arrayList.size() <= 0;
    }
}
