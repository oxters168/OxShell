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

import org.nustaq.serialization.FSTBasicObjectSerializer;
import org.nustaq.serialization.FSTClazzInfo;
import org.nustaq.serialization.FSTObjectInput;
import org.nustaq.serialization.FSTObjectOutput;

import java.io.IOException;
import java.io.Serializable;

public class DataRef implements Serializable {
    protected DataLocation locType;
    protected Object loc;

    private DataRef() { }
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

//    public static class DataRefSerializer extends FSTBasicObjectSerializer {
//        @Override
//        public boolean willHandleClass(Class cl) {
//            return cl.equals(ImageRef.class) || cl.equals(FontRef.class) || cl.equals(DataRef.class);
//        }
//        @Override
//        public void writeObject(FSTObjectOutput out, Object toWrite, FSTClazzInfo clzInfo, FSTClazzInfo.FSTFieldInfo referencedBy, int streamPosition) throws IOException {
//            DataRef obj = new DataRef();
//            Log.d("DataRef", "Writing object " + toWrite.getClass());
//            if (toWrite instanceof ImageRef) {
//                Log.d("DataRef", "Writing ImageRef");
//                ImageRef imageRef = (ImageRef)toWrite;
//                obj.locType = imageRef.dataType;
//                obj.loc = imageRef.imageLoc;
//                //out.writeObject(imageRef.dataType);
//                //out.writeObject(imageRef.imageLoc);
//            } else if (toWrite instanceof FontRef) {
//                Log.d("DataRef", "Writing FontRef");
//                FontRef fontRef = (FontRef)toWrite;
//                obj.locType = fontRef.dataType;
//                obj.loc = fontRef.fontLoc;
//                //out.writeObject(fontRef.dataType);
//                //out.writeObject(fontRef.fontLoc);
//            } else
//                Log.d("DataRef", "Writing " + toWrite.getClass());
//            out.writeObject(obj);
//        }
//        @Override
//        public void readObject(FSTObjectInput in, Object toRead, FSTClazzInfo clzInfo, FSTClazzInfo.FSTFieldInfo referencedBy) throws Exception {
//            Log.d("DataRef", "Reading object " + toRead.getClass());
//            DataRef obj = new DataRef();
//            if (toRead instanceof ImageRef) {
//                Log.d("DataRef", "Reading ImageRef");
//                ImageRef imageRef = (ImageRef)toRead;
//                obj.locType = imageRef.dataType;
//                obj.loc = imageRef.imageLoc;
//            } else if (toRead instanceof FontRef) {
//                Log.d("DataRef", "Reading FontRef");
//                FontRef fontRef = (FontRef)toRead;
//                obj.locType = fontRef.dataType;
//                obj.loc = fontRef.fontLoc;
//            } else
//                Log.d("DataRef", "Reading " + toRead.getClass());
//
//            //super.readObject(in, toRead, clzInfo, referencedBy);
////            DataRef dataRef = (DataRef)toRead;
////            Log.d("ImageRef", "Reading String");
////            String fieldName = in.readStringUTF();
////            Log.d("ImageRef", "Field name " + fieldName);
////            Log.d("ImageRef", "Reading locType");
////            dataRef.locType = (DataLocation)in.readObjectInternal(DataLocation.class);
////            Log.d("ImageRef", "Reading String");
////            fieldName = in.readStringUTF();
////            Log.d("ImageRef", "Field name " + fieldName);
////            dataRef.loc = in.readObjectInternal(null);
//        }
//        @Override
//        public Object instantiate(Class objectClass, FSTObjectInput in, FSTClazzInfo serializationInfo, FSTClazzInfo.FSTFieldInfo referencee, int streamPosition) throws Exception {
////            // Map the old fully qualified class name to the new one
////            //String className = objectClass.getName().replace("ImageRef", "DataRef");
////            //Class<?> newClass = Class.forName(className);
////            Log.d("ImageRef", referencee.getName());
////
////            // Deserialize the object using the new class
//////            Log.d("ImageRef", "Reading locType");
//////            DataLocation locType = (DataLocation)in.readObjectInternal(DataLocation.class);
//////            Log.d("ImageRef", "Reading loc");
//////            Object loc = in.readObjectInternal(null);
////            //in.registerObject(loc, streamPosition, serializationInfo, referencee);
////            //return newClass.getConstructor(Object.class, DataLocation.class).newInstance(loc, locType);
////            //return newClass.getMethod("from", Object.class, DataLocation.class).invoke(null, loc, locType);
////            return DataRef.from(loc, locType);
////            ImageRef imgRef = (ImageRef)super.instantiate(objectClass, in, serializationInfo, referencee, streamPosition);
////            return imgRef != null ? DataRef.from(imgRef.imageLoc, imgRef.dataType) : null;
//            Object superObj = null;
//            if (objectClass != DataRef.class) {
//                Log.d("DataRef", "Instantiating super");
//                superObj = super.instantiate(objectClass, in, serializationInfo, referencee, streamPosition);
//            }
//
//            Log.d("DataRef", "Checking " + objectClass);
//            if (objectClass == ImageRef.class) {
////                Object loc = null;
////                DataLocation locType = null;
////                Log.d("ImageRef", "Reading String");
////                String fieldName = in.readStringUTF();
////                Log.d("ImageRef", "Field name " + fieldName);
////                in.registerObject(fieldName, streamPosition, serializationInfo, referencee);
////                if (fieldName.equals("imageLoc") || fieldName.equals("fontLoc")) {
////                    Log.d("ImageRef", "Reading loc");
////                    loc = in.readObjectInternal(null);
////                } else if (fieldName.equals("dataType")) {
////                    Log.d("ImageRef", "Reading locType");
////                    locType = (DataLocation) in.readObjectInternal(DataLocation.class);
////                }
////                Log.d("ImageRef", "Reading String");
////                fieldName = in.readStringUTF();
////                Log.d("ImageRef", "Field name " + fieldName);
////                in.registerObject(fieldName, streamPosition, serializationInfo, referencee);
////                if (fieldName.equals("imageLoc") || fieldName.equals("fontLoc")) {
////                    Log.d("ImageRef", "Reading loc");
////                    loc = in.readObjectInternal(null);
////                } else if (fieldName.equals("dataType")) {
////                    Log.d("ImageRef", "Reading locType");
////                    locType = (DataLocation) in.readObjectInternal(DataLocation.class);
////                }
//                Log.d("DataRef", "Returning DataRef object from ImageRef");
//                return superObj != null ? new DataRef(((ImageRef)superObj).imageLoc, ((ImageRef)superObj).dataType) : new DataRef();
//            } else if (objectClass == FontRef.class) {
//                Log.d("DataRef", "Returning DataRef object from FontRef");
//                return superObj != null ? new DataRef(((FontRef)superObj).fontLoc, ((FontRef)superObj).dataType) : new DataRef();
//            } else {
//                Log.d("DataRef", "Returning super");
//                return superObj;
//            }
//        }
//    }
}
