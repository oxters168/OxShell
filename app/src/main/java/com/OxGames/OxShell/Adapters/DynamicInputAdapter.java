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

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

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

public class DynamicInputAdapter extends RecyclerView.Adapter<DynamicInputAdapter.DynamicViewHolder> {
    private List<DynamicInputItem> items;
    private Context context;

    public DynamicInputAdapter(Context context, DynamicInputItem... items) {
        this.context = context;
        this.items = new ArrayList();
        if (items != null)
            this.items.addAll(Arrays.asList(items));
    }

    @NonNull
    @Override
    public DynamicViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        DynamicInputItemView view = new DynamicInputItemView(context);
        view.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        return new DynamicViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DynamicViewHolder holder, int position) {
        DynamicInputItem currentItem = items.get(position);
        holder.bindItem(currentItem);
        int dip = Math.round(AndroidHelpers.dipToPixels(context, 20));
        holder.setPadding(0, 0, 0, position < items.size() - 1 ? dip : 0);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void clear() {
        if (items != null)
            items.clear();
    }

    public class DynamicViewHolder extends RecyclerView.ViewHolder {
        private View itemView;
        public DynamicViewHolder(@NonNull View itemView) {
            super(itemView);
            this.itemView = itemView;
        }
        public void bindItem(DynamicInputItem item) {
            ((DynamicInputItemView)itemView).setInputItem(item);
        }
        public void setPadding(int left, int top, int right, int bottom) {
            itemView.setPadding(left, top, right, bottom);
        }
    }
}
