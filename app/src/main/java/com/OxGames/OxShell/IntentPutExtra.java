package com.OxGames.OxShell;

import java.io.Serializable;

public class IntentPutExtra implements Serializable {
    private IntentLaunchData.DataType extraType = IntentLaunchData.DataType.None;
    private String name;
//    private String value;

    public IntentPutExtra(String _name) {
        name = _name;
//        value = _value;
    }
    public IntentPutExtra(String _name, IntentLaunchData.DataType _extraType) {
        name = _name;
        extraType = _extraType;
    }
    public String GetName() {
        return name;
    }
//    public String GetValue() {
//        return value;
//    }
    public void SetExtraType(IntentLaunchData.DataType _extraType) { extraType = _extraType; }
    public IntentLaunchData.DataType GetExtraType() { return extraType; }
//    public void SetValue(String _value) { value = _value; }
}
