package com.OxGames.OxShell.Helpers;

import android.util.Log;

import com.google.gson.Gson;

import org.nustaq.serialization.FSTConfiguration;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
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
    public static void saveAsJSON(Object data, String absPath) {
        Gson gson = new Gson();
        if (!AndroidHelpers.fileExists(absPath))
            AndroidHelpers.makeFile(absPath);
        String json = gson.toJson(data);
        //Log.d("Serialaver", "Saving json to " + absPath + ":\n" + json);
        AndroidHelpers.writeToFile(absPath, json);
    }
    public static <T> T loadFromJSON(String absPath, Class<T> tClass) {
        Gson gson = new Gson();
        String json = AndroidHelpers.readFile(absPath);
        //Log.d("Serialaver", "Read json from " + absPath + ":\n" + json);
        return gson.fromJson(json, tClass);
    }
    public static <T> T loadFromJSON(String absPath, Type tType) {
        Gson gson = new Gson();
        String json = AndroidHelpers.readFile(absPath);
        //Log.d("Serialaver", "Read json from " + absPath + ":\n" + json);
        return gson.fromJson(json, tType);
    }
}
