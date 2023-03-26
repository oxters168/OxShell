package com.OxGames.OxShell.Data;

public class KeyComboAction {
    public final KeyCombo keyCombo;
    public final Runnable action;

    public KeyComboAction(KeyCombo keyCombo, Runnable action) {
        this.keyCombo = keyCombo;
        this.action = action;
    }

//    public KeyCombo getKeyCombo() {
//        return keyCombo;
//    }
//    public Runnable getAction() {
//        return action;
//    }
}
