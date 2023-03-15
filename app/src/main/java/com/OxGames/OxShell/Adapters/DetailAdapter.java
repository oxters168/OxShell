package com.OxGames.OxShell.Adapters;

import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.OxGames.OxShell.Data.DetailItem;
import com.OxGames.OxShell.Helpers.AndroidHelpers;
import com.OxGames.OxShell.R;
import com.OxGames.OxShell.Views.BetterTextView;

import java.util.ArrayList;
import java.util.List;

public class DetailAdapter implements ListAdapter {
    Context context;
    List<DetailItem> detailItems;
    //private int highlightedIndex = -1;
    private final int ICON_ID = View.generateViewId();
    private final int TITLE_ID = View.generateViewId();
    private final int IS_DIR_ID = View.generateViewId();
    private final int currentItemColor = Color.parseColor("#33EAF0CE");
    private final int selectedColor = Color.parseColor("#33CEEAF0");
    private final int currentAndSelectedColor = Color.parseColor("#33F0CEEA");
    private final int noneColor = Color.parseColor("#00000000");
//    boolean hideExtensions;

    public DetailAdapter(Context _context) {
        context = _context;
        detailItems = new ArrayList<>();
    }
    public DetailAdapter(Context _context, List<DetailItem> _detailItems) {
        context = _context;
        detailItems = _detailItems;
//        hideExtensions = _hideExtensions;
    }

    public void add(DetailItem detailItem) {
        detailItems.add(detailItem);
        //notifyDataSetChanged();
    }

//    public void setHighlightedIndex(int index) {
//        highlightedIndex = index;
//    }

    public int getTextSize() {
        return Math.round(AndroidHelpers.getScaledSpToPixels(context, 8));
    }
    public int getBtnHeight() {
        return Math.round(AndroidHelpers.getScaledDpToPixels(context, 32));
    }
    public int getBorderMargin() {
        return Math.round(AndroidHelpers.getScaledDpToPixels(context, 8));
    }
    public int getImgSize() {
        return Math.round(getBtnHeight() * 0.8f);
    }
    private View createDetailItem() {
        int textSize = getTextSize();
        int textOutlineSize = Math.round(AndroidHelpers.getScaledDpToPixels(context, 3));
        int btnHeight = getBtnHeight();
        int imgSize = getImgSize();
        int borderMargin = getBorderMargin();

        FrameLayout detailItem = new FrameLayout(context);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, btnHeight);
        detailItem.setLayoutParams(params);

        ImageView typeIcon = new ImageView(context);
        typeIcon.setId(ICON_ID);
        params = new FrameLayout.LayoutParams(imgSize, imgSize);
        params.gravity = Gravity.START | Gravity.CENTER_VERTICAL;
        params.setMarginStart(borderMargin);
        typeIcon.setLayoutParams(params);
        detailItem.addView(typeIcon);

        BetterTextView title = new BetterTextView(context);
        title.setId(TITLE_ID);
        params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.START | Gravity.CENTER_VERTICAL;
        params.setMarginStart(borderMargin * 2 + imgSize);
        title.setLayoutParams(params);
        title.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        title.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        title.setMarqueeRepeatLimit(-1);
        title.setSingleLine(true);
        title.setTextAlignment(View.TEXT_ALIGNMENT_GRAVITY);
        title.setTextColor(context.getColor(R.color.text));
        title.setTextSize(textSize);
        title.setOutlineColor(Color.parseColor("#000000"));
        title.setOutlineSize(textOutlineSize);
        detailItem.addView(title);

        BetterTextView isDir = new BetterTextView(context);
        isDir.setId(IS_DIR_ID);
        params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.END | Gravity.CENTER_VERTICAL;
        params.setMarginEnd(borderMargin);
        isDir.setLayoutParams(params);
        isDir.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        isDir.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        isDir.setMarqueeRepeatLimit(-1);
        isDir.setSingleLine(true);
        isDir.setTextAlignment(View.TEXT_ALIGNMENT_GRAVITY);
        isDir.setTextColor(context.getColor(R.color.text));
        isDir.setTextSize(textSize);
        isDir.setOutlineColor(Color.parseColor("#000000"));
        isDir.setOutlineSize(textOutlineSize);
        detailItem.addView(isDir);

        return detailItem;
    }
    @Override
    public View getView(int position, View view, ViewGroup parent) {
        DetailItem detailItem = detailItems.get(position);
        //Log.d("DetailAdapter", "Item at " + position + ": " + detailItem.leftAlignedText);
        if (view == null) {
            //LayoutInflater layoutInflater = LayoutInflater.from(context);
            //view = layoutInflater.inflate(R.layout.detail_row, null);
            view = createDetailItem();
        }

        int color = noneColor;
        if (detailItem.isCurrentItem && detailItem.isSelected)
            color = currentAndSelectedColor;
        else if (detailItem.isCurrentItem)
            color = currentItemColor;
        else if (detailItem.isSelected)
            color = selectedColor;
        view.setBackgroundColor(color); //TODO: implement color theme that can take custom theme from file

        BetterTextView title = view.findViewById(TITLE_ID);
        title.setText(detailItem.leftAlignedText);

        boolean hasRightText = detailItem.rightAlignedText != null && !detailItem.rightAlignedText.isEmpty();
        BetterTextView rightText = view.findViewById(IS_DIR_ID);
        rightText.setVisibility(hasRightText ? View.VISIBLE : View.INVISIBLE);
        rightText.setText(detailItem.rightAlignedText);

        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams)title.getLayoutParams();
        params.setMarginStart(detailItem.hasIcon() ? getBorderMargin() * 2 + getImgSize() : getBorderMargin());
        rightText.measure(0, 0);
        params.setMarginEnd(hasRightText ? getBorderMargin() * 2 + rightText.getMeasuredWidth() : getBorderMargin());
        title.setLayoutParams(params);

        ImageView typeIcon = view.findViewById(ICON_ID);
        typeIcon.setImageDrawable(detailItem.getIcon());
        typeIcon.setVisibility(detailItem.hasIcon() ? View.VISIBLE : View.GONE);

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
        return 0;
    }
    @Override
    public int getViewTypeCount() {
        //return detailItems.size();
        return 1;
    }
    @Override
    public boolean isEmpty() {
        return detailItems.size() <= 0;
    }
}
