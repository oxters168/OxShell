package com.OxGames.OxShell.Data;

import android.graphics.drawable.Drawable;
import android.util.Log;

import androidx.annotation.NonNull;

import com.OxGames.OxShell.Helpers.ExplorerBehaviour;
import com.OxGames.OxShell.OxShellApp;

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
    protected Object iconLoc; // had to change back to Object since FST wasn't letting me load ImageRef into DataRef anymore for some reason
    //protected DataRef iconLoc;
    protected List<XMBItem> innerItems;

    protected transient Drawable icon;
    private transient List<Runnable> valuesChangedListeners;
    private transient List<Runnable> innerItemsChangedListeners;

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
//    public XMBItem(XMBItem<? extends T> other) {
//        if (other.innerItems != null) {
//            innerItems = new ArrayList<>();
//            for (int i = 0; i < other.innerItems.size(); i++)
//                innerItems.add(new XMBItem(other.getInnerItem(i)));
//        }
//        obj = other.obj.clone();
//    }

    @NonNull
    @Override
    public Object clone() {
        DataRef origImg = getImgRef();
        XMBItem<T> other = new XMBItem<>(obj, title, origImg != null ? DataRef.from(origImg.getLoc(), origImg.getLocType()) : null);
        if (innerItems != null) {
            List<XMBItem> clonedItems = new ArrayList<>();
            for (int i = 0; i < innerItems.size(); i++)
                clonedItems.add((XMBItem)getInnerItem(i).clone());
            other.innerItems = clonedItems;
        }
        return other;
    }

    public void addValuesChangedListener(Runnable listener) {
        if (valuesChangedListeners == null)
            valuesChangedListeners = new ArrayList<>();
        valuesChangedListeners.add(listener);
    }
    public void removeValuesChangedListener(Runnable listener) {
        if (valuesChangedListeners != null)
            valuesChangedListeners.remove(listener);
    }
    public void clearValuesChangedListeners() {
        if (valuesChangedListeners != null)
            valuesChangedListeners.clear();
    }
    public void fireValuesChanged() {
        OxShellApp.getCurrentActivity().runOnUiThread(() -> {
            if (valuesChangedListeners != null)
                for (int i = 0; i < valuesChangedListeners.size(); i++)
                    valuesChangedListeners.get(i).run();
        });
    }
    public void addInnerItemsChangedListener(Runnable listener) {
        if (innerItemsChangedListeners == null)
            innerItemsChangedListeners = new ArrayList<>();
        innerItemsChangedListeners.add(listener);
    }
    public void removeInnerItemsChangedListener(Runnable listener) {
        if (innerItemsChangedListeners != null)
            innerItemsChangedListeners.remove(listener);
    }
    public void clearInnerItemsChangedListeners() {
        if (innerItemsChangedListeners != null)
            innerItemsChangedListeners.clear();
    }
    private void fireInnerItemsChanged() {
        OxShellApp.getCurrentActivity().runOnUiThread(() -> {
            if (innerItemsChangedListeners != null)
                for (int i = 0; i < innerItemsChangedListeners.size(); i++)
                    innerItemsChangedListeners.get(i).run();
        });
    }

    public void getIcon(Consumer<Drawable> onIconLoaded) {
        if (icon == null && iconLoc != null) {
            ((DataRef)iconLoc).getImage(img -> onIconLoaded.accept(icon = img));
            //onIconLoaded.accept(icon = ((DataRef)iconLoc).getImage());
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
    public void applyToInnerItems(Consumer<XMBItem> action, boolean recursive) {
        if (innerItems != null) {
            for (int i = 0; i < innerItems.size(); i++) {
                XMBItem innerItem = innerItems.get(i);
                if (action != null)
                    action.accept(innerItem);
                if (recursive && innerItem != null)
                    innerItem.applyToInnerItems(action, true);
            }
        }
    }
    public void release() {
        applyToInnerItems(XMBItem::release, false); // this call is inherently recursive
        clearValuesChangedListeners();
        clearImgCache();
        //clearInnerItemImgCache(true);
    }
    public void clearImgCache() {
        //Log.d("XMBItem", "Attempting to clear image of " + title);
        DataRef colImgRef = getImgRef();
        if (colImgRef != null && colImgRef.getLocType() == DataLocation.file) {
            //Log.d("XMBItem", title + " has file icon (@" + colImgRef.getLoc() + "), deleting");
            ExplorerBehaviour.delete((String)colImgRef.getLoc());
        }
    }
    public void clearInnerItemImgCache(boolean recursive) {
        if (innerItems != null)
            for (XMBItem innerItem : innerItems) {
                innerItem.clearImgCache();
                if (recursive)
                    innerItem.clearInnerItemImgCache(true);
            }
    }
    public void upgradeImgRef(int prevVersion) {
        // only for upgrading from older versions of the app
        if (iconLoc instanceof ImageRef) {
            ImageRef imgRef = (ImageRef)iconLoc;
            if (imgRef.dataType == DataLocation.resource) {
                String resName;
                if (prevVersion < 5) {
                    int oldIndex = (int)imgRef.imageLoc;
                    int newIndex = oldIndex + (prevVersion > 1 ? 11 : 12);
                    resName = OxShellApp.getCurrentActivity().getResources().getResourceName(newIndex);
                    Log.i("HomeView", "Switching out " + oldIndex + " => " + newIndex + " => " + resName);
                } else {
                    resName = (String)imgRef.imageLoc;
                    if (!resName.startsWith("com.OxGames.OxShell:drawable/"))
                        resName = "com.OxGames.OxShell:drawable/" + resName;
                }
                iconLoc = DataRef.from(resName, imgRef.dataType);
            }
        } else if (iconLoc instanceof DataRef) {
            DataRef imgRef = (DataRef)iconLoc;
            if (imgRef.locType == DataLocation.resource) {
                String resName = (String) imgRef.getLoc();
                if (!resName.startsWith("com.OxGames.OxShell:drawable/"))
                    resName = "com.OxGames.OxShell:drawable/" + resName;
                iconLoc = DataRef.from(resName, imgRef.getLocType());
            }
        }
    }
    public DataRef getImgRef() {
        return (DataRef)iconLoc;
    }
    public void setImgRef(DataRef imgRef) {
        iconLoc = imgRef;
        fireValuesChanged();
    }
    public void setTitle(String title) {
        this.title = title;
        fireValuesChanged();
    }

    public void add(int localIndex, XMBItem item) {
        this.innerItems.add(localIndex, item);
        fireInnerItemsChanged();
    }
    public void add(XMBItem item) {
        this.innerItems.add(item);
        fireInnerItemsChanged();
    }
    public void remove(int localIndex) {
        remove(localIndex, true);
    }
    public void remove(XMBItem xmbItem) {
        remove(xmbItem, true);
    }
    public void remove(int localIndex, boolean dispose) {
        if (dispose)
            this.innerItems.get(localIndex).release();
        this.innerItems.remove(localIndex);
        fireInnerItemsChanged();
    }
    public void remove(XMBItem xmbItem, boolean dispose) {
        if (dispose)
            xmbItem.release();
        this.innerItems.remove(xmbItem);
        fireInnerItemsChanged();
    }
    public void setInnerItems(XMBItem... innerItems) {
        if (this.innerItems != null)
            applyToInnerItems(XMBItem::release, false);
        this.innerItems = innerItems != null ? new ArrayList<>(Arrays.asList(innerItems)) : null;
        fireInnerItemsChanged();
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
        if (innerItems != null) {
            applyToInnerItems(XMBItem::release, false);
            innerItems.clear();
            fireInnerItemsChanged();
        }
    }
    public String getTitle() {
        return title;
    }
}
