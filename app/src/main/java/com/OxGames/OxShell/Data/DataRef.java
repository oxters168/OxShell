package com.OxGames.OxShell.Data;

import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.OxGames.OxShell.BuildConfig;
import com.OxGames.OxShell.Helpers.AndroidHelpers;
import com.OxGames.OxShell.OxShellApp;
import com.OxGames.OxShell.R;

import org.nustaq.serialization.FSTBasicObjectSerializer;
import org.nustaq.serialization.FSTClazzInfo;
import org.nustaq.serialization.FSTObjectInput;
import org.nustaq.serialization.FSTObjectOutput;

import java.io.IOException;
import java.io.Serializable;
import java.util.function.Consumer;

public class DataRef implements Serializable {
    protected DataLocation locType;
    protected Object loc;

    private DataRef() { }
    protected DataRef(Object loc, DataLocation locType) {
        this.loc = loc;
        this.locType = locType;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        return obj instanceof DataRef && ((DataRef)obj).locType == locType && ((DataRef)obj).loc.equals(loc);
    }

    public static DataRef from(Object loc, DataLocation locType) {
        return new DataRef(loc, locType);
    }
    public boolean isValid() {
        return !(loc == null || locType == DataLocation.none || (locType == DataLocation.file && !AndroidHelpers.fileExists((String)loc)));
    }
    public DataLocation getLocType() {
        return locType;
    }
    public Object getLoc() {
        return loc;
    }

    public void getImage(Consumer<Drawable> onIconLoaded) {
        if (locType == DataLocation.resource) {
            try {
                onIconLoaded.accept(ContextCompat.getDrawable(OxShellApp.getContext(), OxShellApp.getCurrentActivity().getResources().getIdentifier((String)loc, "drawable", BuildConfig.APPLICATION_ID)));
            } catch (Exception e) {
                Log.e("ImageRef", "Failed to find resource " + loc + ": " + e);
                onIconLoaded.accept(ContextCompat.getDrawable(OxShellApp.getContext(), R.drawable.ic_baseline_question_mark_24));
            }
        }
        else if (locType == DataLocation.asset)
            onIconLoaded.accept(AndroidHelpers.bitmapToDrawable(OxShellApp.getContext(), AndroidHelpers.readAssetAsBitmap(OxShellApp.getContext(), (String)loc)));
        else if (locType == DataLocation.pkg)
            PackagesCache.requestPackageIcon((String)loc, onIconLoaded);
        else if (locType == DataLocation.file)
            onIconLoaded.accept(AndroidHelpers.bitmapToDrawable(OxShellApp.getContext(), AndroidHelpers.bitmapFromFile((String)loc)));
        else if (locType == DataLocation.resolverUri)
            onIconLoaded.accept(AndroidHelpers.bitmapToDrawable(OxShellApp.getContext(), AndroidHelpers.readResolverUriAsBitmap(OxShellApp.getContext(), (Uri)loc)));
        else if (locType == DataLocation.self) {
            if (loc instanceof Drawable)
                onIconLoaded.accept((Drawable)loc);
            else if (loc instanceof Bitmap)
                onIconLoaded.accept(AndroidHelpers.bitmapToDrawable(OxShellApp.getContext(), (Bitmap)loc));
        } else
            onIconLoaded.accept(null);
    }

    public Typeface getFont() {
        if (locType == DataLocation.resource || locType == DataLocation.resolverUri)
            throw new UnsupportedOperationException("Failed to reference font, resources and resolver uris are not supported at the moment");
        if (locType == DataLocation.asset)
            return Typeface.createFromAsset(OxShellApp.getContext().getAssets(), (String)loc);
        if (locType == DataLocation.file)
            return Typeface.createFromFile((String)loc);
        if (locType == DataLocation.self)
            return (Typeface)loc;
        return null;
    }
}
