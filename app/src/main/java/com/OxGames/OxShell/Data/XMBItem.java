package com.OxGames.OxShell.Data;

import android.graphics.drawable.Drawable;
import android.util.Log;

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
    private void fireValuesChanged() {
        OxShellApp.getCurrentActivity().runOnUiThread(() -> {
            if (valuesChangedListeners != null)
                for (Runnable listener : valuesChangedListeners)
                    listener.run();
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
        clearValuesChangedListeners();
        clearImgCache();
        clearInnerItemImgCache(true);
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
                    int newIndex = oldIndex + (prevVersion > 1 ? 2 : 3);
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
    }
    public void add(XMBItem item) {
        this.innerItems.add(item);
    }
    public void remove(int localIndex) {
        this.innerItems.remove(localIndex);
    }
    public void setInnerItems(XMBItem... innerItems) {
        this.innerItems = new ArrayList<>(Arrays.asList(innerItems));
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
    }
    public String getTitle() {
        return title;
    }
}
