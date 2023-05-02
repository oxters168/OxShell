package com.OxGames.OxShell.Helpers;

import android.util.Log;

public class MathHelpers {
    public static final long MS_PER_SEC = 1000L;
    public static final long MS_PER_MIN = MS_PER_SEC * 60;
    public static final long MS_PER_HR = MS_PER_MIN * 60;
    public static final long MS_PER_DAY = MS_PER_HR * 24;
    public static final long MS_PER_YEAR = MS_PER_DAY * 365;

    public static int calculateMillisForFps(int fps) {
        return Math.round((1f / fps) * 1000);
    }
    public static String msToTimestamp(long ms) {
        long totalTime = ms;
        long years = totalTime / MS_PER_YEAR;
        totalTime %= MS_PER_YEAR;
        long days = totalTime / MS_PER_DAY;
        totalTime %= MS_PER_DAY;
        long hours = totalTime / MS_PER_HR;
        totalTime %= MS_PER_HR;
        long minutes = totalTime / MS_PER_MIN;
        totalTime %= MS_PER_MIN;
        long seconds = totalTime / MS_PER_SEC;
        return (years > 0 ? years + ":" : "") + (days > 0 ? (days < 10 ? "00" : (days < 100 ? "0" : "")) + days + ":" : "") + (hours > 0 ? (hours < 10 ? "0" : "") + hours + ":" : "") + (minutes < 10 ? "0" : "") + minutes + ":" + (seconds < 10 ? "0" : "") + seconds;
    }

    public static int max(int... values) {
        if (values.length <= 0)
            throw new IllegalArgumentException("Cannot get max of nothing");
        int max = values[0];
        for (int i = 1; i < values.length; i++)
            max = Math.max(max, values[i]);
        return max;
    }
    public static int min(int... values) {
        if (values.length <= 0)
            throw new IllegalArgumentException("Cannot get min of nothing");
        int min = values[0];
        for (int i = 1; i < values.length; i++)
            min = Math.min(min, values[i]);
        return min;
    }
    public static int hash(Integer... values) {
        StringBuilder hashMedium = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            hashMedium.append(values[i]);
            if (i < values.length - 1)
                hashMedium.append(",");
        }
        int hash = hashMedium.toString().hashCode();
        //Log.d("MathHelpers", hashMedium + " => " + hash);
        return hash;
    }
    public static double roundTo(double value, int places) {
        double placesMult = Math.pow(10, places);
        return Math.round(value * placesMult) / placesMult;
    }
    public static float roundTo(float value, int places) {
        return (float)roundTo((double)value, places);
    }
    public static int clamp(int value, int min, int max) {
        return Math.min(Math.max(min, value), max);
    }
    public static float clamp(float value, float min, float max) {
        return Math.min(Math.max(min, value), max);
    }
}
