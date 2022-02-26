package com.OxGames.OxShell;

public class IntentPutExtra {
    private IntentLaunchData.IntentType extraType = IntentLaunchData.IntentType.None;
    private String name;
//    private String value;

    public IntentPutExtra(String _name) {
        name = _name;
//        value = _value;
    }
    public IntentPutExtra(String _name, IntentLaunchData.IntentType _extraType) {
        name = _name;
        extraType = _extraType;
    }
    public String GetName() {
        return name;
    }
//    public String GetValue() {
//        return value;
//    }
    public void SetExtraType(IntentLaunchData.IntentType _extraType) { extraType = _extraType; }
    public IntentLaunchData.IntentType GetExtraType() { return extraType; }
//    public void SetValue(String _value) { value = _value; }
}
