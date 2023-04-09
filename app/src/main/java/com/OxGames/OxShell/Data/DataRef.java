package com.OxGames.OxShell.Data;

import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;

import androidx.core.content.ContextCompat;

import com.OxGames.OxShell.BuildConfig;
import com.OxGames.OxShell.Helpers.AndroidHelpers;
import com.OxGames.OxShell.OxShellApp;
import com.OxGames.OxShell.R;

import java.io.Serializable;

public class DataRef implements Serializable {
    protected DataLocation locType;
    protected Object loc;

    protected DataRef(Object loc, DataLocation locType) {
        this.loc = loc;
        this.locType = locType;
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

    public Drawable getImage() {
        if (locType == DataLocation.resource) {
            try {
                return ContextCompat.getDrawable(OxShellApp.getContext(), OxShellApp.getCurrentActivity().getResources().getIdentifier((String)loc, "drawable", BuildConfig.APPLICATION_ID));
            } catch (Exception e) {
                Log.e("ImageRef", "Failed to find resource " + loc + ": " + e);
                return ContextCompat.getDrawable(OxShellApp.getContext(), R.drawable.ic_baseline_question_mark_24);
            }
        }
        if (locType == DataLocation.asset)
            return AndroidHelpers.bitmapToDrawable(OxShellApp.getContext(), AndroidHelpers.readAssetAsBitmap(OxShellApp.getContext(), (String)loc));
        if (locType == DataLocation.file)
            return AndroidHelpers.bitmapToDrawable(OxShellApp.getContext(), AndroidHelpers.bitmapFromFile((String)loc));
        if (locType == DataLocation.resolverUri)
            return AndroidHelpers.bitmapToDrawable(OxShellApp.getContext(), AndroidHelpers.readResolverUriAsBitmap(OxShellApp.getContext(), (Uri)loc));
        if (locType == DataLocation.self) {
            if (loc instanceof Drawable)
                return (Drawable)loc;
            else if (loc instanceof Bitmap)
                return AndroidHelpers.bitmapToDrawable(OxShellApp.getContext(), (Bitmap)loc);
        }
        return null;
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
