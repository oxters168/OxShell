package com.OxGames.OxShell.Helpers;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

public class ShellCommander {
    public static String run(String command) {
        String output = null;
        try {
            Process process = Runtime.getRuntime().exec(command);
            try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                output = bufferedReader.lines().collect(Collectors.joining(System.lineSeparator()));
            } catch (IOException ex) {
                Log.e("ShellCommander", ex.getMessage());
            }
        } catch (IOException ex) {
            Log.e("ShellCommander", ex.getMessage());
        }
        return output;
    }
}
