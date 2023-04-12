package com.OxGames.OxShell.Data;

import android.graphics.drawable.Drawable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class XMBItem<T> implements Serializable {
    public T obj;
    protected String title;
    //protected Object iconLoc;
    protected DataRef iconLoc;
    protected Function<XMBItem, List<XMBItem>> innerItemCreator;
    protected List<XMBItem> innerItems;

    protected transient Drawable icon;

    public XMBItem(T _obj, String _title, DataRef _iconLoc, XMBItem... innerItems) {
        obj = _obj;
        title = _title;
        iconLoc = _iconLoc;
        this.innerItems = new ArrayList<>();
        if (innerItems != null)
            Collections.addAll(this.innerItems, innerItems);
    }
    public XMBItem(T _obj, String _title, DataRef _iconLoc) {
        this(_obj, _title, _iconLoc, null);
    }
    public XMBItem(T _obj, String _title, XMBItem... innerItems) {
        this(_obj, _title, null, innerItems);
    }

    public void getIcon(Consumer<Drawable> onIconLoaded) {
        if (icon == null && iconLoc != null) {
            onIconLoaded.accept(icon = iconLoc.getImage());
//            if (iconLoc instanceof Integer) {
//                onIconLoaded.accept(icon = ContextCompat.getDrawable(OxShellApp.getContext(), (Integer)iconLoc));
//            } else if (iconLoc instanceof Drawable) {
//                onIconLoaded.accept(icon = (Drawable)iconLoc);
//            } else if (iconLoc instanceof String) {
//                onIconLoaded.accept(icon = AndroidHelpers.bitmapToDrawable(OxShellApp.getContext(), AndroidHelpers.bitmapFromFile((String)iconLoc)));
//            }
        }
        onIconLoaded.accept(icon);
        //return icon;
    }
    public DataRef getImgRef() {
        return iconLoc;
    }
    public void setImgRef(DataRef imgRef) {
        iconLoc = imgRef;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    public void add(int localIndex, XMBItem item) {
        this.innerItems.add(localIndex, item);
    }
    public void add(XMBItem item) {
        this.innerItems.add(item);
    }
    public void remove(int localIndex) {
        this.innerItems.remove(localIndex);
    }
    public void setInnerItems(Function<XMBItem, List<XMBItem>> creator) {
        this.innerItemCreator = creator;
        this.innerItems = null; // resetting inner items since the creator has changed
    }
    public void setInnerItems(XMBItem... innerItems) {
        this.innerItems = new ArrayList<>(Arrays.asList(innerItems));
        this.innerItemCreator = null; // resetting creator since inner items was directly set
    }
    public void refreshInnerItems() {
        if (innerItemCreator != null)
            innerItems = innerItemCreator.apply(this);
    }
    public boolean hasInnerItems() {
        return innerItems != null && innerItems.size() > 0;
    }
    public int getInnerItemCount() {
        return innerItems != null ? innerItems.size() : 0;
    }
    public XMBItem[] getInnerItems() {
        return innerItems != null ? innerItems.toArray(new XMBItem[0]) : null;
    }
    public XMBItem getInnerItem(int index) {
        return innerItems.get(index);
    }
    public void clearInnerItems() {
        if (innerItems != null)
            innerItems.clear();
        innerItemCreator = null;
    }
    public String getTitle() {
        return title;
    }
}
