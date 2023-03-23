package com.OxGames.OxShell.Data;

import java.io.Serializable;

public class KeyCombo implements Serializable {
    public static final int shortHoldTime = 50;
    public static final int mediumHoldTime = 100;
    public static final int longHoldTime = 150;
    public static final int defaultRepeatTime = 50;
    private final int[] keycodes;
    private final boolean onDown;
    private final int downHoldMillis;
    private final int repeatMillis;

    private KeyCombo(boolean onDown, int holdMillis, int repeatMillis, int[] keycodes) {
        this.keycodes = keycodes.clone();
        this.onDown = onDown;
        this.downHoldMillis = holdMillis;
        this.repeatMillis = repeatMillis;
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
    public int getRepeatMillis() {
        return repeatMillis;
    }

    public static KeyCombo createDownCombo(int holdMillis, int repeatMillis, int... keycodes) {
        return new KeyCombo(true, holdMillis, repeatMillis, keycodes);
    }
    public static KeyCombo createUpCombo(int... keycodes) {
        return new KeyCombo(false, 0, 0, keycodes);
    }
}
