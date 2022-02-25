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
        this.arrayList=arrayList;
        this.context=context;
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
    public void registerDataSetObserver(DataSetObserver observer) {
    }
    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {
    }
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
    public View getView(int position, View convertView, ViewGroup parent) {
        ExplorerItem subjectData=arrayList.get(position);
        if(convertView==null) {
            LayoutInflater layoutInflater = LayoutInflater.from(context);
            convertView=layoutInflater.inflate(R.layout.list_row, null);
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                }
            });
            TextView title = convertView.findViewById(R.id.title);
            TextView isDirText = convertView.findViewById(R.id.isDir);
            ImageView typeIcon = convertView.findViewById(R.id.typeIcon);
            title.setText(subjectData.name);
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)title.getLayoutParams();
            params.setMargins(subjectData.HasIcon() ? 40 : 0, 0, 0, 0);
            isDirText.setVisibility(subjectData.isDir ? View.VISIBLE : View.INVISIBLE);
            typeIcon.setVisibility(subjectData.HasIcon() ? View.VISIBLE : View.GONE);
//            Picasso.with(context)
//                    .load(subjectData.Image)
//                    .into(imag);
        }
        return convertView;
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
