package com.OxGames.OxShell.Helpers;

import android.util.Log;


import org.nustaq.serialization.FSTConfiguration;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

public class Serialaver {
    public static void saveFile(Serializable obj, String path) {
        try {
            FileOutputStream file = new FileOutputStream(path);
            ObjectOutputStream out = new ObjectOutputStream(file);
            out.writeObject(obj);
            out.close();
            file.close();
        } catch(Exception e) {
            Log.e("Serialaver", e.getMessage());
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
        } catch (Exception e) {
            Log.e("Serialaver", e.getMessage());
        }
        return obj;
    }
    public static void saveAsFSTJSON(Object data, String absPath) {
        FSTConfiguration conf = FSTConfiguration.createJsonConfiguration();

        byte[] bytes = conf.asByteArray(data);
        try {
            String json = new String(bytes,"UTF-8");
            AndroidHelpers.writeToFile(absPath, json);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        Object deser = conf.asObject(bytes);
    }
    public static Object loadFromFSTJSON(String absPath) {
        FSTConfiguration conf = FSTConfiguration.createJsonConfiguration();
        String json = AndroidHelpers.readFile(absPath);
        return conf.asObject(json.getBytes(StandardCharsets.UTF_8));
    }
}
