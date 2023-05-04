package com.OxGames.OxShell.Helpers;


import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.view.KeyEvent;

import androidx.annotation.NonNull;

import com.OxGames.OxShell.Data.KeyComboAction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class InputHandler {
    private static class ComboActions {
        private boolean enabled;
        private int priority;
        private boolean ignorePriority;
        private List<KeyComboAction> comboActions;

        private ComboActions() {
            this(false);
        }
        private ComboActions(KeyComboAction... comboActions) {
            this(false, comboActions);
        }
        private ComboActions(boolean enabled, KeyComboAction... comboActions) {
            this(enabled, false, DEFAULT_PRIORITY, comboActions);
        }
        private ComboActions(boolean enabled, boolean ignorePriority, KeyComboAction... comboActions) {
            this(enabled, ignorePriority, DEFAULT_PRIORITY, comboActions);
        }
        private ComboActions(boolean enabled, boolean ignorePriority, int priority, KeyComboAction... comboActions) {
            this.enabled = enabled;
            this.priority = priority;
            this.ignorePriority = ignorePriority;
            this.comboActions = new ArrayList<>();
            addComboActions(comboActions);
        }

        @NonNull
        @Override
        public String toString() {
            return "Enabled: " + enabled + "\n" + comboActions.stream().map(comboAction -> comboAction.keyCombo.toString()).collect(Collectors.joining("\n"));
        }

        private void addComboActions(KeyComboAction... comboActions) {
            if (comboActions != null && comboActions.length > 0)
                Collections.addAll(this.comboActions, comboActions);
        }
        private void removeComboActions(KeyComboAction... comboActions) {
            for (KeyComboAction keyComboAction : comboActions) {
                //Log.d("InputHandler", "Contains: " + this.comboActions.contains(keyComboAction));
                this.comboActions.remove(keyComboAction);
            }
        }
        private void setEnabled(boolean onOff) {
            enabled = onOff;
        }
    }
    public static final int DEFAULT_PRIORITY = 0;
    private static final int LISTEN_DELAY = 5;
    public static final String ALWAYS_ON_TAG = "ALWAYS_ON";
    //private static final LinkedList<String> currentTagList = new LinkedList<>(); // the history of tags that have been set as current
    private static final HashMap<String, ComboActions> keyComboActions = new HashMap<>();
    private static final List<KeyEvent> currentlyDownKeys = new ArrayList<>();
    //private final List<KeyEvent> currentlyDownAltKeys; // the keys the android system gives back sometimes interchangeably with actual pressed keys
    private static final List<KeyEvent> keysHistory = new ArrayList<>();
    private static final List<KeyEvent> eventsHistory = new ArrayList<>();
    private static final List<Consumer<KeyEvent>> inputListeners = new ArrayList<>();
    //private final List<KeyEvent> altKeysHistory;
    private static final Handler handler = new Handler(Looper.getMainLooper());
    private static long downStartTime;
    private static boolean actionHasRun;
    private static int repeatCount;
    private static boolean isBlockingInput;
    private static int currentPriorityLevel = Integer.MIN_VALUE;
    private static final Runnable runnable = new Runnable() {
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
    private static void checkIfDown(KeyComboAction comboAction) {
        long timePassed = SystemClock.uptimeMillis() - downStartTime;
        int repeatMillis = comboAction.keyCombo.getRepeatMillis();
        if (!actionHasRun || repeatMillis >= 0) {
            int timeRepeatCount = ((int)timePassed - comboAction.keyCombo.getHoldMillis() - comboAction.keyCombo.getRepeatStartDelay()) / (repeatMillis > 0 ? repeatMillis : LISTEN_DELAY);
            if (comboAction.keyCombo.isOnDown() && ((!actionHasRun && timePassed >= comboAction.keyCombo.getHoldMillis()) || (repeatMillis == 0 || timeRepeatCount > repeatCount))) {
                if (!isBlockingInput) {
                    //Log.d("InputHandler", "Running down action");
                    comboAction.action.run();
                }
                actionHasRun = true;
                repeatCount = Math.max(timeRepeatCount, 0);
            }
        }
    }

    private InputHandler() {}
    //public InputHandler() {
        //handler = new Handler(Looper.getMainLooper());
        //currentlyDownKeys = new ArrayList<>();
        //keysHistory = new ArrayList<>();
        //keyComboActions = new HashMap<>();
        //keyComboActions.put(ALWAYS_ON_TAG, new ArrayList<>());
        //currentTagList = new LinkedList<>();
        //inputListeners = new ArrayList<>();
    //}

    public static void addInputListener(Consumer<KeyEvent> onInputEvent) {
        inputListeners.add(onInputEvent);
    }
    public static void removeInputListener(Consumer<KeyEvent> onInputEvent) {
        inputListeners.remove(onInputEvent);
    }
    public static void clearInputListeners() {
        inputListeners.clear();
    }
    private static void fireInputListeners(KeyEvent event) {
        for (Consumer<KeyEvent> iL : new ArrayList<>(inputListeners))
            iL.accept(event);
    }
    public static KeyEvent[] getHistory() {
        return keysHistory.toArray(new KeyEvent[0]);
    }
    public static void toggleBlockingInput(boolean onOff) {
        isBlockingInput = onOff;
    }
    public static void toggleBlockingInput() {
        toggleBlockingInput(!isBlockingInput());
    }
    public static boolean isBlockingInput() {
        return isBlockingInput;
    }

    private static boolean eventPassed(KeyEvent event) {
        for (KeyEvent passedEvent : eventsHistory)
            if (passedEvent.getKeyCode() == event.getKeyCode() && passedEvent.getAction() == event.getAction() && passedEvent.getEventTime() == event.getEventTime())
                return true;
        return false;
    }
    public static boolean onInputEvent(KeyEvent event) {
        //Log.d("InputHandler", "onInputEvent: " + event);
        //if (eventPassed(event)) {
        if (eventsHistory.contains(event)) {
            //Log.d("InputHandler", "Event passed, skipping");
            return true;
        }
        eventsHistory.add(event);

        if (event.getAction() == KeyEvent.ACTION_DOWN)
            onDown(event);
        List<KeyComboAction> toBeRun = null;
        if (event.getAction() == KeyEvent.ACTION_UP)
            toBeRun = onUp(event);
        boolean hasCombo = findComboActions(keysHistory).size() > 0;// || findComboActions(altKeysHistory).size() > 0; // this is here since there's a chance that on up will have an action to clear out combos
        fireInputListeners(event);
        if (toBeRun != null)
            for (KeyComboAction runThis : toBeRun) {
                //Log.d("InputHandler", "Running up action");
                runThis.action.run();
            }

        //if (currentlyDownKeys.size() <= 0)
        //    eventsHistory.clear();
        //Log.d("InputHandler", "onInputEvent\ncurrentlyDown: " + currentlyDownKeys.stream().map(e -> KeyEvent.keyCodeToString(e.getKeyCode())).collect(Collectors.toList()) + "\nhistory: " + keysHistory.stream().map(e -> KeyEvent.keyCodeToString(e.getKeyCode())).collect(Collectors.toList()) + "\nhasCombo: " + hasCombo);
        return hasCombo || isBlockingInput();
    }
    private static void onDown(KeyEvent event) {
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
    private static List<KeyComboAction> onUp(KeyEvent event) {
        int index = currentlyDownKeys.stream().map(KeyEvent::getKeyCode).collect(Collectors.toList()).indexOf(event.getKeyCode());
        if (index >= 0)
            currentlyDownKeys.remove(index);
//        index = currentlyDownAltKeys.stream().map(KeyEvent::getKeyCode).collect(Collectors.toList()).indexOf(event.getKeyCode());
//        if (index >= 0)
//            currentlyDownAltKeys.remove(index);
        //currentlyDownKeys.remove(event);//(Object)event.getKeyCode());
        //if (!isDown())
        List<KeyComboAction> toBeRun = new ArrayList<>();
        if (!actionHasRun)
            for (KeyComboAction comboAction : findComboActions(keysHistory)) {
                if (!isBlockingInput && !comboAction.keyCombo.isOnDown())
                    toBeRun.add(comboAction);
                //    comboAction.action.run();
                actionHasRun = true;
            }
        return toBeRun;
    }

    public static boolean isDown() {
        return currentlyDownKeys.size() > 0;
    }
    public static boolean actionHasRun() {
        return actionHasRun;
    }

    public static void addKeyComboActions(String tag, KeyComboAction... keyComboActions) {
        if (!InputHandler.keyComboActions.containsKey(tag))
            InputHandler.keyComboActions.put(tag, new ComboActions(tag.equals(ALWAYS_ON_TAG), keyComboActions));
        else
            InputHandler.keyComboActions.get(tag).addComboActions(keyComboActions);
        //Log.d("InputHandler", tag + " -> " + InputHandler.keyComboActions.get(tag).toString());
    }
    public static void addKeyComboActions(KeyComboAction... keyComboActions) {
        addKeyComboActions(ALWAYS_ON_TAG, keyComboActions);
    }
    public static void removeKeyComboActions(String tag, KeyComboAction... keyComboActions) {
        InputHandler.keyComboActions.get(tag).removeComboActions(keyComboActions);
    }
    public static void removeKeyComboActions(KeyComboAction... keyComboActions) {
        removeKeyComboActions(ALWAYS_ON_TAG, keyComboActions);
    }
    public static void clearKeyComboActions(String tag) {
        if (keyComboActions.containsKey(tag))
            keyComboActions.get(tag).comboActions.clear();
    }
    public static void clearKeyComboActions() {
        clearKeyComboActions(ALWAYS_ON_TAG);
    }
    public static boolean isTagEnabled(String tag) {
        return keyComboActions.get(tag).enabled;
    }
    public static void setTagEnabled(String tag, boolean onOff) {
        if (keyComboActions.containsKey(tag))
            keyComboActions.get(tag).setEnabled(onOff);
        else
            Log.w("InputHandler", "Failed to set enabled state of " + tag);
        //removeTagFromHistory(tag);
        //currentTagList.addLast(tag);
    }
//    public static String getActiveTag() {
//        return !currentTagList.isEmpty() ? currentTagList.getLast() : null;
//    }
//    public static void removeTagFromHistory(String tag) {
//        int indexOf = currentTagList.indexOf(tag);
//        if (indexOf >= 0)
//            currentTagList.remove(indexOf);
//    }
    public static boolean tagHasActions(String tag) {
        return keyComboActions.containsKey(tag);
    }

    public static int getHighestPriority() {
        int highestValue = Integer.MIN_VALUE;
        for (ComboActions comboActions : keyComboActions.values())
            if (comboActions.enabled)
                highestValue = Math.max(highestValue, comboActions.priority);
        return highestValue;
    }

    public static void setTagPriority(String tag, int value) {
        keyComboActions.get(tag).priority = value;
    }
    public static void setTagIgnorePriority(String tag, boolean onOff) {
        keyComboActions.get(tag).ignorePriority = onOff;
    }
    public static void resetCurrentPriorityLevel() {
        currentPriorityLevel = Integer.MIN_VALUE;
    }
    public static void setCurrentPriorityLevel(int value) {
        currentPriorityLevel = value;
    }
    public static int getCurrentPriorityLevel() {
        return currentPriorityLevel;
    }
    private static List<KeyComboAction> findComboActions(List<KeyEvent> combo) {
        List<KeyComboAction> fittingActions = new ArrayList<>();
        List<KeyComboAction> searchedActions = new ArrayList<>();
        int highestEnabledPriority = getHighestPriority();
        for (Map.Entry<String, ComboActions> entrySet : keyComboActions.entrySet())
            if (entrySet.getValue().enabled && (entrySet.getValue().ignorePriority || entrySet.getValue().priority >= Math.min(highestEnabledPriority, currentPriorityLevel))) {
                //Log.d("InputHandler", "Searching in: " + entrySet.getKey() + " -> " + entrySet.getValue().toString());
                searchedActions.addAll(entrySet.getValue().comboActions);
            }
//        if (tagsFilter != null && tagsFilter.length > 0) {
//            searchedActions = new ArrayList<>();
//            for (String tag : tagsFilter)
//                searchedActions.addAll(keyComboActions.get(tag));
//        } else {
//            searchedActions = new ArrayList<>(keyComboActions.get(ALWAYS_ON_TAG));
//            if (!currentTagList.isEmpty())
//                searchedActions.addAll(keyComboActions.get(getActiveTag()));
//        }

        for (KeyComboAction comboAction : searchedActions) {
            int[] inputCombo = comboAction.keyCombo.isOrdered() ? combo.stream().mapToInt(KeyEvent::getKeyCode).toArray() : combo.stream().mapToInt(KeyEvent::getKeyCode).sorted().toArray();
            int[] selfCombo = comboAction.keyCombo.isOrdered() ? comboAction.keyCombo.getKeys() : Arrays.stream(comboAction.keyCombo.getKeys()).sorted().toArray();
            if (Arrays.equals(selfCombo, inputCombo))
                fittingActions.add(comboAction);
        }
        //Log.d("InputHandler", "Found " + fittingActions.size() + " combo(s)");
        return fittingActions;
    }
}
