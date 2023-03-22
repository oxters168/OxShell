package com.OxGames.OxShell.Data;

import java.io.Serializable;

public class KeyCombo implements Serializable {
    public static final int shortHoldTime = 50;
    public static final int mediumHoldTime = 100;
    public static final int longHoldTime = 150;
    private int[] keycodes;
    private boolean onDown;
    private int downHoldMillis;

    private KeyCombo(boolean onDown, int holdMillis, int[] keycodes) {
        this.keycodes = keycodes.clone();
        this.onDown = onDown;
        this.downHoldMillis = holdMillis;
    }
    public int[] getKeys() {
        return keycodes.clone();
    }
    public boolean isOnDown() {
        return onDown;
    }
    public int getHoldMillis() {
        return downHoldMillis;
    }

    public static KeyCombo createDownCombo(int holdMillis, int... keycodes) {
        return new KeyCombo(true, holdMillis, keycodes);
    }
    public static KeyCombo createUpCombo(int... keycodes) {
        return new KeyCombo(false, 0, keycodes);
    }
}
