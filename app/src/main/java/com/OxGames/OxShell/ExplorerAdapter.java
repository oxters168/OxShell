package com.OxGames.OxShell;

import android.content.Context;
import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class ExplorerAdapter implements ListAdapter {
    ArrayList<ExplorerItem> arrayList;
    Context context;
    public ExplorerAdapter(Context context, ArrayList<ExplorerItem> arrayList) {
        this.arrayList = arrayList;
        this.context = context;
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
    public View getView(int position, View view, ViewGroup parent) {
        ExplorerItem explorerItem = arrayList.get(position);
        int properPosition = ((ExplorerView)parent).properPosition;
        if(view == null) {
            //I think this is when the view is being initialized for the first time
            LayoutInflater layoutInflater = LayoutInflater.from(context);
            view = layoutInflater.inflate(R.layout.list_row, null);
            TextView title = view.findViewById(R.id.title);
            TextView isDirText = view.findViewById(R.id.isDir);
            ImageView typeIcon = view.findViewById(R.id.typeIcon);
            title.setText(explorerItem.name);
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)title.getLayoutParams();
            params.setMargins(explorerItem.HasIcon() ? 40 : 0, 0, 0, 0);
            isDirText.setVisibility(explorerItem.isDir ? View.VISIBLE : View.INVISIBLE);
            typeIcon.setImageDrawable(explorerItem.GetIcon());
            typeIcon.setVisibility(explorerItem.HasIcon() ? View.VISIBLE : View.GONE);
        }
        view.setBackgroundResource((position == properPosition) ? R.color.scheme1 : R.color.light_blue_400);
//        if (position == properPosition) {
//            int top = (convertView == null) ? 0 : convertView.getTop();
//            ((ExplorerView)parent).setSelectionFromTop(position, top);
//        }

        return view;
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
