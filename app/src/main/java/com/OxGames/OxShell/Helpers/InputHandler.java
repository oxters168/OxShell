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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class InputHandler {
    private static final int LISTEN_DELAY = 5;
    public static final String ALWAYS_ON_TAG = "ALWAYS_ON";
    private final LinkedList<String> currentTagList; // the history of tags that have been set as current
    private final HashMap<String, List<KeyComboAction>> keyComboActions;
    private final List<KeyEvent> currentlyDownKeys;
    //private final List<KeyEvent> currentlyDownAltKeys; // the keys the android system gives back sometimes interchangeably with actual pressed keys
    private final List<KeyEvent> keysHistory;
    //private final List<KeyEvent> altKeysHistory;
    private final Handler handler;
    private long downStartTime;
    private boolean actionHasRun;
    private int repeatCount;
    private final Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (isDown()) {
                for (KeyComboAction comboAction : findComboActions(currentlyDownKeys))
                    checkIfDown(comboAction);
                //for (KeyComboAction comboAction : findComboActions(currentlyDownAltKeys))
                //    checkIfDown(comboAction);
                handler.postDelayed(runnable, LISTEN_DELAY);
            }
        }
    };
    private void checkIfDown(KeyComboAction comboAction) {
        long timePassed = SystemClock.uptimeMillis() - downStartTime;
        int repeatMillis = comboAction.keyCombo.getRepeatMillis();
        if (!actionHasRun || repeatMillis >= 0) {
            int timeRepeatCount = ((int)timePassed - comboAction.keyCombo.getHoldMillis() - comboAction.keyCombo.getRepeatStartDelay()) / (repeatMillis > 0 ? repeatMillis : LISTEN_DELAY);
            if (comboAction.keyCombo.isOnDown() && ((!actionHasRun && timePassed >= comboAction.keyCombo.getHoldMillis()) || (repeatMillis == 0 || timeRepeatCount > repeatCount))) {
                comboAction.action.run();
                actionHasRun = true;
                repeatCount = Math.max(timeRepeatCount, 0);
            }
        }
    }

    public InputHandler() {
        handler = new Handler(Looper.getMainLooper());
        currentlyDownKeys = new ArrayList<>();
        //currentlyDownAltKeys = new ArrayList<>();
        keysHistory = new ArrayList<>();
        //altKeysHistory = new ArrayList<>();
        keyComboActions = new HashMap<>();
        keyComboActions.put(ALWAYS_ON_TAG, new ArrayList<>());
        currentTagList = new LinkedList<>();
    }

    public boolean onInputEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN)
            onDown(event);
        boolean hasCombo = findComboActions(keysHistory).size() > 0;// || findComboActions(altKeysHistory).size() > 0; // this is here since there's a chance that on up will have an action to clear out combos
        if (event.getAction() == KeyEvent.ACTION_UP)
            onUp(event);
        //Log.d("InputHandler", "onInputEvent -> " + getActiveTag() + "\n" + event + "\ncurrentlyDown: " + currentlyDownKeys.toString() + "\nhistory: " + keysHistory.toString() + "\nhasCombo: " + hasCombo);
        return hasCombo;
    }
    private void onDown(KeyEvent event) {
        boolean firstPress = !isDown();
        if (!currentlyDownKeys.stream().map(KeyEvent::getKeyCode).collect(Collectors.toList()).contains(event.getKeyCode())) {
            // check if the key is one given by the android system that it considers interchangeable for the one that what was just pressed
            if (currentlyDownKeys.size() > 0 && currentlyDownKeys.get(currentlyDownKeys.size() - 1).getEventTime() == event.getEventTime())
                return;
//            if (currentlyDownKeys.size() > 0 && currentlyDownKeys.get(currentlyDownKeys.size() - 1).getEventTime() == event.getEventTime()) {
//                // an alternative key was given by the android system for a key that was just pressed
//                currentlyDownAltKeys.add(event);
//                altKeysHistory.clear();
//                altKeysHistory.addAll(currentlyDownAltKeys);
//            } else {
            // a new key has been pressed
            actionHasRun = false;
            repeatCount = 0;
            downStartTime = SystemClock.uptimeMillis();
            currentlyDownKeys.add(event);//.getKeyCode());
            // reset history every time a new key is pressed, this way if they had pressed a key then stopped while other keys are pressed then that key
            // won't muddle the history (can't remove in onUp since there's a chance they won't press anything else after)
            keysHistory.clear();
            keysHistory.addAll(currentlyDownKeys);
            //}
        }
        if (firstPress) {
            // first time pressing a button
            //actionHasRun = false;
            handler.post(runnable);
        }
    }
    private void onUp(KeyEvent event) {
        int index = currentlyDownKeys.stream().map(KeyEvent::getKeyCode).collect(Collectors.toList()).indexOf(event.getKeyCode());
        if (index >= 0)
            currentlyDownKeys.remove(index);
//        index = currentlyDownAltKeys.stream().map(KeyEvent::getKeyCode).collect(Collectors.toList()).indexOf(event.getKeyCode());
//        if (index >= 0)
//            currentlyDownAltKeys.remove(index);
        //currentlyDownKeys.remove(event);//(Object)event.getKeyCode());
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

    public void addKeyComboActions(String tag, KeyComboAction... keyComboActions) {
        if (!this.keyComboActions.containsKey(tag))
            this.keyComboActions.put(tag, new ArrayList<>());
        Collections.addAll(this.keyComboActions.get(tag), keyComboActions);
    }
    public void addKeyComboActions(KeyComboAction... keyComboActions) {
        addKeyComboActions(ALWAYS_ON_TAG, keyComboActions);
    }
    public void removeKeyComboActions(String tag, KeyComboAction... keyComboActions) {
        for (KeyComboAction keyComboAction : keyComboActions)
            this.keyComboActions.get(tag).remove(keyComboAction);
    }
    public void removeKeyComboActions(KeyComboAction... keyComboActions) {
        removeKeyComboActions(ALWAYS_ON_TAG, keyComboActions);
    }
    public void clearKeyComboActions(String tag) {
        if (keyComboActions.containsKey(tag))
            keyComboActions.get(tag).clear();
    }
    public void setActiveTag(String tag) {
        removeTagFromHistory(tag);
        currentTagList.addLast(tag);
    }
    public String getActiveTag() {
        return currentTagList.getLast();
    }
    public void removeTagFromHistory(String tag) {
        int indexOf = currentTagList.indexOf(tag);
        if (indexOf >= 0)
            currentTagList.remove(indexOf);
    }
    public boolean tagHasActions(String tag) {
        return keyComboActions.containsKey(tag);
    }

    private List<KeyComboAction> findComboActions(List<KeyEvent> combo) {
        // if we sort then order doesn't matter, but I think it's better if order does matter
        //int[] sortedCombo = combo.stream().sorted().mapToInt(v -> v).toArray();
        //int[] notSortedCombo = combo.stream().mapToInt(v -> v).toArray();
        List<KeyComboAction> fittingActions = new ArrayList<>();
        List<KeyComboAction> searchedActions = new ArrayList<>(keyComboActions.get(ALWAYS_ON_TAG));
        if (!currentTagList.isEmpty())
            searchedActions.addAll(keyComboActions.get(getActiveTag()));

        for (KeyComboAction comboAction : searchedActions) {
            int[] inputCombo = comboAction.keyCombo.isOrdered() ? combo.stream().mapToInt(KeyEvent::getKeyCode).toArray() : combo.stream().mapToInt(KeyEvent::getKeyCode).sorted().toArray();
            int[] selfCombo = comboAction.keyCombo.isOrdered() ? comboAction.keyCombo.getKeys() : Arrays.stream(comboAction.keyCombo.getKeys()).sorted().toArray();
            if (Arrays.equals(selfCombo, inputCombo))
                fittingActions.add(comboAction);
        }
        return fittingActions;
    }
}
