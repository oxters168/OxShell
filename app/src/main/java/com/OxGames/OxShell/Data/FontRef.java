package com.OxGames.OxShell.Data;

import org.nustaq.serialization.FSTBasicObjectSerializer;
import org.nustaq.serialization.FSTClazzInfo;
import org.nustaq.serialization.FSTObjectInput;
import org.nustaq.serialization.FSTObjectOutput;

import java.io.IOException;
import java.io.Serializable;

// do not use this class, this is just for older versions of the app
public class FontRef implements Serializable {
    public DataLocation dataType;
    public Object fontLoc;

//    public static class FontRefSerializer extends FSTBasicObjectSerializer {
//        @Override
//        public boolean willHandleClass(Class cl) {
//            return cl.equals(FontRef.class);
//        }
//        @Override
//        public void writeObject(FSTObjectOutput out, Object toWrite, FSTClazzInfo clzInfo, FSTClazzInfo.FSTFieldInfo referencedBy, int streamPosition) throws IOException {
//            FontRef fontRef = (FontRef)toWrite;
//            out.writeObject(fontRef.dataType);
//            out.writeObject(fontRef.fontLoc);
//        }
//        @Override
//        public Object instantiate(Class objectClass, FSTObjectInput in, FSTClazzInfo serializationInfo, FSTClazzInfo.FSTFieldInfo referencee, int streamPosition) throws Exception {
//            // Map the old fully qualified class name to the new one
//            String className = objectClass.getName().replace("FontRef", "DataRef");
//            Class<?> newClass = Class.forName(className);
//
//            // Deserialize the object using the new class
//            DataLocation locType = (DataLocation)in.readObject();
//            Object loc = in.readObject();
//            return newClass.getConstructor(Object.class, DataLocation.class).newInstance(loc, locType);
//        }
//    }
}
