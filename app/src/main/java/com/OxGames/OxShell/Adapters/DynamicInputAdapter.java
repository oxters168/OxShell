package com.OxGames.OxShell.Adapters;

import android.content.Context;
import android.database.DataSetObserver;
import android.telecom.Call;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListAdapter;

import com.OxGames.OxShell.Data.DynamicInputItem;
import com.OxGames.OxShell.Helpers.AndroidHelpers;
import com.OxGames.OxShell.R;
import com.OxGames.OxShell.Views.DynamicInputItemView;
import com.OxGames.OxShell.Views.DynamicInputView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

public class DynamicInputAdapter implements ListAdapter {
    private List<DynamicInputItem> items;
    private Context context;

    @Override
    public void registerDataSetObserver(DataSetObserver observer) {
        //throw new UnsupportedOperationException("Function not implemented");
    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {
        //throw new UnsupportedOperationException("Function not implemented");
    }

    public DynamicInputAdapter(Context context, DynamicInputItem... items) {
        this.context = context;
        this.items = new ArrayList();
        if (items != null)
            this.items.addAll(Arrays.asList(items));
    }

    public void clear() {
        if (items != null)
            items.clear();
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        //throw new UnsupportedOperationException("Function not implemented");
        return 0;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        DynamicInputItemView view = (DynamicInputItemView)convertView;
        //Log.d("DynamicInputAdapter", position + " is null " + (view == null));
        if (view == null) {
            view = new DynamicInputItemView(context);
            view.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            //LayoutInflater layoutInflater = LayoutInflater.from(context);
            //view = (DynamicInputItemView)layoutInflater.inflate(R.layout.dynamic_input_item, null);
        }
        DynamicInputItem currentItem = items.get(position);
        view.setInputItem(currentItem);
        int dip = Math.round(AndroidHelpers.dipToPixels(context, 20));
        view.setPadding(0, 0, 0, position < items.size() - 1 ? dip : 0);

        return view;
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return items == null || items.size() <= 0;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return true;
    }
    @Override
    public boolean isEnabled(int position) {
        // every other position will be a separator
        //return position % 2 != 0;
        return true;
    }
}
