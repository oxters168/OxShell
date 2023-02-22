package com.OxGames.OxShell.Helpers;

public class MathHelpers {
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
}
