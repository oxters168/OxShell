package com.OxGames.OxShell.Helpers;

import android.util.Log;

public class MathHelpers {
    public static int calculateMillisForFps(int fps) {
        return Math.round((1f / fps) * 1000);
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
}
