package com.OxGames.OxShell;

import android.util.Log;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class Serialaver {
    public static void saveFile(Serializable obj, String path) {
        try {
            FileOutputStream file = new FileOutputStream(path);
            ObjectOutputStream out = new ObjectOutputStream(file);
            out.writeObject(obj);
            out.close();
            file.close();
        }
        catch(IOException ex) {
            Log.e("Serialize", ex.getMessage());
        }
    }
    public static Object loadFile(String path) {
        Object obj = null;

        try {
            FileInputStream file = new FileInputStream(path);
            ObjectInputStream in = new ObjectInputStream(file);
            obj = in.readObject();
            in.close();
            file.close();
        }
        catch(IOException ex) {
            Log.e("Deserialize", ex.getMessage());
        }
        catch(ClassNotFoundException ex) {
            Log.e("Deserialize", ex.getMessage());
        }
        return obj;
    }
}
