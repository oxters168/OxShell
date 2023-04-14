package com.OxGames.OxShell.Data;

import android.util.Log;

import org.nustaq.serialization.FSTBasicObjectSerializer;
import org.nustaq.serialization.FSTClazzInfo;
import org.nustaq.serialization.FSTObjectInput;
import org.nustaq.serialization.FSTObjectOutput;

import java.io.IOException;
import java.io.Serializable;

// do not use this class, this is just for older versions of the app
public class ImageRef implements Serializable {
    public DataLocation dataType;
    public Object imageLoc;

//    public static class ImageRefSerializer extends FSTBasicObjectSerializer {
//        @Override
//        public boolean willHandleClass(Class cl) {
//            return cl.equals(ImageRef.class);
//        }
//        @Override
//        public void writeObject(FSTObjectOutput out, Object toWrite, FSTClazzInfo clzInfo, FSTClazzInfo.FSTFieldInfo referencedBy, int streamPosition) throws IOException {
//            ImageRef imageRef = (ImageRef)toWrite;
//            out.writeObject(imageRef.dataType);
//            out.writeObject(imageRef.imageLoc);
//        }
//        @Override
//        public void readObject(FSTObjectInput in, Object toRead, FSTClazzInfo clzInfo, FSTClazzInfo.FSTFieldInfo referencedBy) throws Exception {
//            //super.readObject(in, toRead, clzInfo, referencedBy);
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
////            Log.d("ImageRef", "Reading String");
////            String fieldName = in.readStringUTF();
////            Log.d("ImageRef", "Field name " + fieldName);
////            in.registerObject(fieldName, streamPosition, serializationInfo, referencee);
////            Log.d("ImageRef", "Reading locType");
////            DataLocation locType = (DataLocation)in.readObjectInternal(DataLocation.class);
////            Log.d("ImageRef", "Reading String");
////            fieldName = in.readStringUTF();
////            Log.d("ImageRef", "Field name " + fieldName);
////            in.registerObject(fieldName, streamPosition, serializationInfo, referencee);
////            Log.d("ImageRef", "Reading loc");
////            Object loc = in.readObjectInternal(null);
////            //in.registerObject(loc, streamPosition, serializationInfo, referencee);
////            //return newClass.getConstructor(Object.class, DataLocation.class).newInstance(loc, locType);
////            //return newClass.getMethod("from", Object.class, DataLocation.class).invoke(null, loc, locType);
////            return DataRef.from(loc, locType);
////            ImageRef imgRef = (ImageRef)super.instantiate(objectClass, in, serializationInfo, referencee, streamPosition);
////            return imgRef != null ? DataRef.from(imgRef.imageLoc, imgRef.dataType) : null;
//            //return new DataRef();
//        }
//    }
}
