package com.OxGames.OxShell.Adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.OxGames.OxShell.Data.DynamicInputRow;
import com.OxGames.OxShell.Helpers.AndroidHelpers;
import com.OxGames.OxShell.Interfaces.AdapterListener;
import com.OxGames.OxShell.Views.DynamicInputRowView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DynamicInputAdapter extends RecyclerView.Adapter<DynamicInputAdapter.DynamicViewHolder> {
    private List<DynamicInputRow> items;
    private Context context;
    private List<AdapterListener> listeners;

    public DynamicInputAdapter(Context context, DynamicInputRow... items) {
        this.context = context;
        this.items = new ArrayList();
        if (items != null)
            this.items.addAll(Arrays.asList(items));
        listeners = new ArrayList<>();
    }

    public void addListener(AdapterListener listener) {
        listeners.add(listener);
    }
    public void removeListener(AdapterListener listener) {
        listeners.remove(listener);
    }
    public void clearListeners() {
        listeners.clear();
    }

    @NonNull
    @Override
    public DynamicViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        DynamicInputRowView view = new DynamicInputRowView(context);
        view.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        return new DynamicViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DynamicViewHolder holder, int position) {
        holder.bindItem(items.get(position));
        int dip = Math.round(AndroidHelpers.dpToPixels(context, 20));
        holder.setPadding(0, 0, 0, position < items.size() - 1 ? dip : 0);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public void onViewAttachedToWindow(@NonNull DynamicViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        //Log.d("DynamicInputAdapter", "onViewAttachedToWindow");
        for (AdapterListener listener : listeners)
            if (listener != null)
                listener.onViewsReady();
    }

    public void clear() {
        if (items != null)
            items.clear();
    }

    public static class DynamicViewHolder extends RecyclerView.ViewHolder {
        public DynamicViewHolder(@NonNull View itemView) {
            super(itemView);
        }
        public void bindItem(DynamicInputRow item) {
            ((DynamicInputRowView)itemView).setInputItems(item);
        }
        public void setPadding(int left, int top, int right, int bottom) {
            itemView.setPadding(left, top, right, bottom);
        }
    }
}
