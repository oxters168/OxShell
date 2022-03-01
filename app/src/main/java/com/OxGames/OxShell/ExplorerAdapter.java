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
    ArrayList<ExplorerItem> explorerItems;
    boolean hideExtensions;

    public ExplorerAdapter(Context _context, ArrayList<ExplorerItem> _explorerItems, boolean _hideExtensions) {
        context = _context;
        explorerItems = _explorerItems;
        hideExtensions = _hideExtensions;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        ExplorerItem explorerItem = explorerItems.get(position);
        if (view == null) {
            //I think this is when the view is being initialized for the first time
            LayoutInflater layoutInflater = LayoutInflater.from(context);
            view = layoutInflater.inflate(R.layout.explorer_row, null);
            explorerItem.view = view;

            String shownName = explorerItem.name;
            if (hideExtensions && !explorerItem.isDir && shownName.lastIndexOf(".") > 0)
                shownName = shownName.substring(0, shownName.lastIndexOf("."));
            TextView title = view.findViewById(R.id.title);
            title.setText(shownName);

            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) title.getLayoutParams();
            params.setMargins(explorerItem.HasIcon() ? 40 : 0, 0, 0, 0);

            TextView isDirText = view.findViewById(R.id.isDir);
            isDirText.setVisibility(explorerItem.isDir ? View.VISIBLE : View.INVISIBLE);

            ImageView typeIcon = view.findViewById(R.id.typeIcon);
            typeIcon.setImageDrawable(explorerItem.GetIcon());
            typeIcon.setVisibility(explorerItem.HasIcon() ? View.VISIBLE : View.GONE);
        }
//        else
//            explorerItem.view = parent.getChildAt(position); //Doing this here causes the explorer list to not highlight properly until a selection is made

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
        return explorerItems.size();
    }
    @Override
    public Object getItem(int position) {
        return explorerItems.get(position);
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
        return explorerItems.size();
    }
    @Override
    public boolean isEmpty() {
        return explorerItems.size() <= 0;
    }
}
