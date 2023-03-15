package com.OxGames.OxShell.Data;

import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;

import androidx.core.content.ContextCompat;

import com.OxGames.OxShell.Helpers.AndroidHelpers;
import com.OxGames.OxShell.OxShellApp;

import java.io.Serializable;

public class FontRef implements Serializable {
    private DataLocation dataType;
    private Object fontLoc;

    private FontRef(Object fontLoc, DataLocation dataType) {
        this.fontLoc = fontLoc;
        this.dataType = dataType;
    }

    public static FontRef from(Object fontLoc, DataLocation dataType) {
        return new FontRef(fontLoc, dataType);
    }
    public boolean isValid() {
        return !(fontLoc == null || dataType == DataLocation.none || (dataType == DataLocation.file && !AndroidHelpers.fileExists((String)fontLoc)));
    }
    public DataLocation getRefType() {
        return dataType;
    }
    public Object getFontObj() {
        return fontLoc;
    }
    public Typeface getFont() {
        if (dataType == DataLocation.resource || dataType == DataLocation.resolverUri)
            throw new UnsupportedOperationException("Failed to reference font, resources and resolver uris are not supported at the moment");
        if (dataType == DataLocation.asset)
            return Typeface.createFromAsset(OxShellApp.getContext().getAssets(), (String)fontLoc);
        if (dataType == DataLocation.file)
            return Typeface.createFromFile((String)fontLoc);
        if (dataType == DataLocation.self)
            return (Typeface)fontLoc;
        return null;
    }
}
