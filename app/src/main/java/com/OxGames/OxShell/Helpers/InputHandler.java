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
    private final Runnable runnable = new Runnable() {
        @Override
        public void run() {
            // TODO: fix issue where the action will run every LISTEN_DELAY milliseconds
            if (!actionHasRun && isDown()) {
                for (KeyComboAction comboAction : findComboActions(currentlyDownKeys))
                    if (comboAction.keyCombo.isOnDown() && SystemClock.uptimeMillis() - downStartTime >= comboAction.keyCombo.getHoldMillis()) {
                        comboAction.action.run();
                        actionHasRun = true;
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
        boolean hasCombo = findComboActions(keysHistory).size() > 0;
        if (event.getAction() == KeyEvent.ACTION_UP)
            onUp(event);
        //Log.d("InputHandler", "onInputEvent\ncurrentlyDown: " + currentlyDownKeys.toString() + "\nhistory: " + keysHistory.toString() + "\nhasCombo: " + hasCombo);
        return hasCombo;
    }
    private void onDown(KeyEvent event) {
        boolean firstPress = !isDown();
        if (!currentlyDownKeys.contains(event.getKeyCode())) {
            // a new key has been pressed
            downStartTime = SystemClock.uptimeMillis();
            currentlyDownKeys.add(event.getKeyCode());
            // reset history every time a new key is pressed, this way if they had pressed a key then stopped while other keys are pressed then that key
            // won't muddle the history
            keysHistory.clear();
            keysHistory.addAll(currentlyDownKeys);
        }
        if (firstPress) {
            // first time pressing a button
            actionHasRun = false;
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
