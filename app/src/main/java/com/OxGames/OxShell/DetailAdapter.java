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

public class DetailAdapter implements ListAdapter {
    Context context;
    ArrayList<DetailItem> detailItems;
//    boolean hideExtensions;

    public DetailAdapter(Context _context, ArrayList<DetailItem> _detailItems) {
        context = _context;
        detailItems = _detailItems;
//        hideExtensions = _hideExtensions;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        DetailItem detailItem = detailItems.get(position);
        if (view == null) {
            //I think this is when the view is being initialized for the first time
            LayoutInflater layoutInflater = LayoutInflater.from(context);
            view = layoutInflater.inflate(R.layout.detail_row, null);
            detailItem.view = view;

//            if (hideExtensions)
//                shownName = ExplorerBehaviour.RemoveExtension(shownName);
            TextView title = view.findViewById(R.id.title);
            title.setText(detailItem.leftAlignedText);

            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) title.getLayoutParams();
            params.setMargins(detailItem.HasIcon() ? 40 : 0, 0, 0, 0);

            TextView rightText = view.findViewById(R.id.isDir);
            rightText.setVisibility(detailItem.rightAlignedText != null && !detailItem.rightAlignedText.isEmpty() ? View.VISIBLE : View.INVISIBLE);
            rightText.setText(detailItem.rightAlignedText);

            ImageView typeIcon = view.findViewById(R.id.typeIcon);
            typeIcon.setImageDrawable(detailItem.GetIcon());
            typeIcon.setVisibility(detailItem.HasIcon() ? View.VISIBLE : View.GONE);
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
        return position;
    }
    @Override
    public int getViewTypeCount() {
        return detailItems.size();
    }
    @Override
    public boolean isEmpty() {
        return detailItems.size() <= 0;
    }
}
