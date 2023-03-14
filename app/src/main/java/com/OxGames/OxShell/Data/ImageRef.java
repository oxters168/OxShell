package com.OxGames.OxShell.Data;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;

import androidx.core.content.ContextCompat;

import com.OxGames.OxShell.Helpers.AndroidHelpers;
import com.OxGames.OxShell.OxShellApp;

import java.io.Serializable;

public class ImageRef implements Serializable {
    private DataLocation dataType;
    private Object imageLoc;

    private ImageRef(Object imageLoc, DataLocation dataType) {
        this.imageLoc = imageLoc;
        this.dataType = dataType;
    }

//    public void setImage(Object imageLoc, DataLocation dataType) {
//        this.imageLoc = imageLoc;
//        this.dataType = dataType;
//    }
    public static ImageRef from(Object imageLoc, DataLocation dataType) {
        return new ImageRef(imageLoc, dataType);
    }
    public boolean isValid() {
        return !(imageLoc == null || dataType == DataLocation.none || (dataType == DataLocation.file && !AndroidHelpers.fileExists((String)imageLoc)));
    }
    public DataLocation getRefType() {
        return dataType;
    }
    public Object getImageObj() {
        return imageLoc;
    }
    public Drawable getImage() {
        if (dataType == DataLocation.resource)
            return ContextCompat.getDrawable(OxShellApp.getContext(), (int)imageLoc);
        if (dataType == DataLocation.asset)
            return AndroidHelpers.bitmapToDrawable(OxShellApp.getContext(), AndroidHelpers.readAssetAsBitmap(OxShellApp.getContext(), (String)imageLoc));
        if (dataType == DataLocation.file)
            return AndroidHelpers.bitmapToDrawable(OxShellApp.getContext(), AndroidHelpers.bitmapFromFile((String)imageLoc));
        if (dataType == DataLocation.resolverUri)
            return AndroidHelpers.bitmapToDrawable(OxShellApp.getContext(), AndroidHelpers.readResolverUriAsBitmap(OxShellApp.getContext(), Uri.parse((String)imageLoc)));
        if (dataType == DataLocation.self) {
            if (imageLoc instanceof Drawable)
                return (Drawable) imageLoc;
            else if (imageLoc instanceof Bitmap)
                return AndroidHelpers.bitmapToDrawable(OxShellApp.getContext(), (Bitmap) imageLoc);
        }
        return null;
    }
}
