package com.OxGames.OxShell.Data;

public class KeyComboAction {
    public final KeyCombo keyCombo;
    public final Runnable action;
    public final String actionDesc;

    public KeyComboAction(KeyCombo keyCombo, Runnable action, String actionDesc) {
        this.keyCombo = keyCombo;
        this.action = action;
        this.actionDesc = actionDesc;
    }
    public KeyComboAction(KeyCombo keyCombo, Runnable action) {
        this(keyCombo, action, null);
    }

//    public KeyCombo getKeyCombo() {
//        return keyCombo;
//    }
//    public Runnable getAction() {
//        return action;
//    }
}
