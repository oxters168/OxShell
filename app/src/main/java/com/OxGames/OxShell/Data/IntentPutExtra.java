package com.OxGames.OxShell.Data;

import android.content.Intent;
import android.util.Log;

import java.io.Serializable;

public class IntentPutExtra implements Serializable {
    private IntentLaunchData.DataType extraType;
    private String name;
    private Object value;

    public IntentPutExtra(String _name, Object _value, IntentLaunchData.DataType type) {
        Log.d("IntentPutExtra", _value + " is " + type);
        extraType = type;
        name = _name;
        value = _value;
    }
    public IntentPutExtra(String _name, int _value) {
        this(_name, _value, IntentLaunchData.DataType.Integer);
    }
    public IntentPutExtra(String _name, boolean _value) {
        this(_name, _value, IntentLaunchData.DataType.Boolean);
    }
    public IntentPutExtra(String _name, float _value) {
        this(_name, _value, IntentLaunchData.DataType.Float);
    }
    public IntentPutExtra(String _name, String _value) {
        this(_name, _value, IntentLaunchData.DataType.String);
    }
    public IntentPutExtra(String _name, IntentLaunchData.DataType _extraType) {
        name = _name;
        extraType = _extraType;
    }
    public String getName() {
        return name;
    }
//    public void setExtraType(IntentLaunchData.DataType _extraType) { extraType = _extraType; }
    public IntentLaunchData.DataType getExtraType() { return extraType; }
    public Object getValue() {
        return value;
    }
    public void putExtraInto(Intent intent) {
        if (extraType == IntentLaunchData.DataType.Integer)
            intent.putExtra(name, (int)value);
        if (extraType == IntentLaunchData.DataType.Boolean)
            intent.putExtra(name, (boolean)value);
        if (extraType == IntentLaunchData.DataType.Float)
            intent.putExtra(name, (float)value);
        if (extraType == IntentLaunchData.DataType.String)
            intent.putExtra(name, (String)value);
    }

    public static IntentPutExtra parseFrom(String name, String toBeParsed) {
        try {
            return new IntentPutExtra(name, Integer.parseInt(toBeParsed), IntentLaunchData.DataType.Integer);
        } catch (Exception e) {}
        try {
            return new IntentPutExtra(name, Float.parseFloat(toBeParsed), IntentLaunchData.DataType.Float);
        } catch (Exception e) {}
        if (toBeParsed.equalsIgnoreCase("true"))
            return new IntentPutExtra(name, true, IntentLaunchData.DataType.Boolean);
        if (toBeParsed.equalsIgnoreCase("false"))
            return new IntentPutExtra(name, false, IntentLaunchData.DataType.Boolean);
//        try {
//            value = Boolean.parseBoolean(toBeParsed);
//            return new IntentPutExtra(name, value, IntentLaunchData.DataType.Boolean);
//        } catch (Exception e) {}

        IntentPutExtra extra;
        if (toBeParsed.equals(IntentLaunchData.DataType.AbsolutePath.toString()))
            extra = new IntentPutExtra(name, null, IntentLaunchData.DataType.AbsolutePath);
        else if (toBeParsed.equals(IntentLaunchData.DataType.FileNameWithExt.toString()))
            extra = new IntentPutExtra(name, null, IntentLaunchData.DataType.FileNameWithExt);
        else if (toBeParsed.equals(IntentLaunchData.DataType.FileNameWithoutExt.toString()))
            extra = new IntentPutExtra(name, null, IntentLaunchData.DataType.FileNameWithoutExt);
        else
            extra = new IntentPutExtra(name, toBeParsed, IntentLaunchData.DataType.String);
        return extra;
    }
}
