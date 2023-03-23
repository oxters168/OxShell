package com.OxGames.OxShell.Data;

import java.io.Serializable;

public class KeyCombo implements Serializable {
    public static final int shortHoldTime = 50;
    public static final int mediumHoldTime = 100;
    public static final int longHoldTime = 150;
    public static final int defaultRepeatStartDelay = 200;
    public static final int defaultRepeatTime = 50;
    private final int[] keycodes;
    private final boolean onDown;
    private final int downHoldMillis;
    private final int repeatMillis;
    private final int repeatStartDelay;
    private final boolean ordered;

    private KeyCombo(boolean onDown, int holdMillis, int repeatStartDelay, int repeatMillis, boolean ordered, int[] keycodes) {
        this.keycodes = keycodes.clone();
        this.onDown = onDown;
        this.downHoldMillis = holdMillis;
        this.repeatMillis = repeatMillis;
        this.repeatStartDelay = repeatStartDelay;
        this.ordered = ordered;
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
    public int getRepeatStartDelay() {
        return repeatStartDelay;
    }
    public boolean isOrdered() {
        return ordered;
    }

    public static KeyCombo createDownCombo(int holdMillis, int repeatStartDelay, int repeatMillis, boolean ordered, int... keycodes) {
        return new KeyCombo(true, holdMillis, repeatStartDelay, repeatMillis, ordered, keycodes);
    }
    public static KeyCombo createUpCombo(boolean ordered, int... keycodes) {
        return new KeyCombo(false, 0, 0, 0, ordered, keycodes);
    }
    public static KeyCombo createDownCombo(int holdMillis, int repeatStartDelay, int repeatMillis, int keycode) {
        return new KeyCombo(true, holdMillis, repeatStartDelay, repeatMillis, true, new int[] { keycode });
    }
    public static KeyCombo createUpCombo(int keycode) {
        return new KeyCombo(false, 0, 0, 0, true, new int[] { keycode });
    }
}
