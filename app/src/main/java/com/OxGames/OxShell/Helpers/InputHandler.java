package com.OxGames.OxShell.Helpers;


import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.view.KeyEvent;

import com.OxGames.OxShell.Data.KeyComboAction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class InputHandler {
    private final int LISTEN_DELAY = 5;
    private final List<KeyComboAction> keyComboActions;
    private final List<Integer> currentlyDownKeys;
    private final List<Integer> keysHistory;
    private final Handler handler;
    private long downStartTime;
    private boolean actionHasRun;
    private int repeatCount;
    private final Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (isDown()) {
                for (KeyComboAction comboAction : findComboActions(currentlyDownKeys)) {
                    long timePassed = SystemClock.uptimeMillis() - downStartTime;
                    int repeatMillis = comboAction.keyCombo.getRepeatMillis();
                    if (!actionHasRun || repeatMillis >= 0) {
                        int timeRepeatCount = ((int) timePassed - comboAction.keyCombo.getHoldMillis()) / (repeatMillis > 0 ? repeatMillis : LISTEN_DELAY);
                        if (comboAction.keyCombo.isOnDown() && timePassed >= comboAction.keyCombo.getHoldMillis() && (repeatMillis == 0 || timeRepeatCount > repeatCount)) {
                            comboAction.action.run();
                            actionHasRun = true;
                            repeatCount = Math.max(timeRepeatCount, 0);
                        }
                    }
                }
                handler.postDelayed(runnable, LISTEN_DELAY);
            }
        }
    };

    public InputHandler() {
        handler = new Handler(Looper.getMainLooper());
        currentlyDownKeys = new ArrayList<>();
        keysHistory = new ArrayList<>();
        keyComboActions = new ArrayList<>();
    }

    public boolean onInputEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN)
            onDown(event);
        boolean hasCombo = findComboActions(keysHistory).size() > 0; // this is here since there's a chance that on up will have an action to clear out combos
        if (event.getAction() == KeyEvent.ACTION_UP)
            onUp(event);
        //Log.d("InputHandler", "onInputEvent\ncurrentlyDown: " + currentlyDownKeys.toString() + "\nhistory: " + keysHistory.toString() + "\nhasCombo: " + hasCombo);
        return hasCombo;
    }
    private void onDown(KeyEvent event) {
        boolean firstPress = !isDown();
        if (!currentlyDownKeys.contains(event.getKeyCode())) {
            // a new key has been pressed
            actionHasRun = false;
            repeatCount = 0;
            downStartTime = SystemClock.uptimeMillis();
            currentlyDownKeys.add(event.getKeyCode());
            // reset history every time a new key is pressed, this way if they had pressed a key then stopped while other keys are pressed then that key
            // won't muddle the history
            keysHistory.clear();
            keysHistory.addAll(currentlyDownKeys);
        }
        if (firstPress) {
            // first time pressing a button
            //actionHasRun = false;
            handler.post(runnable);
        }
    }
    private void onUp(KeyEvent event) {
        currentlyDownKeys.remove((Object)event.getKeyCode());
        //if (!isDown())
        if (!actionHasRun)
            for (KeyComboAction comboAction : findComboActions(keysHistory)) {
                comboAction.action.run();
                actionHasRun = true;
            }
    }

    public boolean isDown() {
        return currentlyDownKeys.size() > 0;
    }
    public boolean actionHasRun() {
        return actionHasRun;
    }

    public void addKeyComboActions(KeyComboAction... keyComboActions) {
        Collections.addAll(this.keyComboActions, keyComboActions);
    }
    public void removeKeyComboActions(KeyComboAction... keyComboActions) {
        for (KeyComboAction keyComboAction : keyComboActions)
            this.keyComboActions.remove(keyComboAction);
    }
    public void clearKeyComboActions() {
        keyComboActions.clear();
    }

    private List<KeyComboAction> findComboActions(List<Integer> combo) {
        // if we sort then order doesn't matter, but I think it's better if order does matter
        //int[] sortedCombo = combo.stream().sorted().mapToInt(v -> v).toArray();
        int[] notSortedCombo = combo.stream().mapToInt(v -> v).toArray();
        List<KeyComboAction> fittingActions = new ArrayList<>();
        for (KeyComboAction comboAction : keyComboActions) {
            //if (Arrays.equals(Arrays.stream(comboAction.keyCombo.get()).sorted().toArray(), sortedCombo))
            if (Arrays.equals(comboAction.keyCombo.getKeys(), notSortedCombo))
                fittingActions.add(comboAction);
        }
        return fittingActions;
    }
}
