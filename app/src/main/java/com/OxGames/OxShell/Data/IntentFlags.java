package com.OxGames.OxShell.Data;

import android.content.Intent;
import android.os.Build;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IntentFlags {
    public static HashMap<Integer, String> getAllAsIntToStr() {
        HashMap<Integer, String> flags = new HashMap<>();

        flags.put(Intent.FLAG_GRANT_READ_URI_PERMISSION, "FLAG_GRANT_READ_URI_PERMISSION");
        flags.put(Intent.FLAG_GRANT_WRITE_URI_PERMISSION, "FLAG_GRANT_WRITE_URI_PERMISSION");
        flags.put(Intent.FLAG_FROM_BACKGROUND, "FLAG_FROM_BACKGROUND");
        flags.put(Intent.FLAG_DEBUG_LOG_RESOLUTION, "FLAG_DEBUG_LOG_RESOLUTION");
        flags.put(Intent.FLAG_EXCLUDE_STOPPED_PACKAGES, "FLAG_EXCLUDE_STOPPED_PACKAGES");
        flags.put(Intent.FLAG_INCLUDE_STOPPED_PACKAGES, "FLAG_INCLUDE_STOPPED_PACKAGES");
        flags.put(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION, "FLAG_GRANT_PERSISTABLE_URI_PERMISSION");
        flags.put(Intent.FLAG_GRANT_PREFIX_URI_PERMISSION, "FLAG_GRANT_PREFIX_URI_PERMISSION");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            flags.put(Intent.FLAG_DIRECT_BOOT_AUTO, "FLAG_DIRECT_BOOT_AUTO");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            flags.put(Intent.FLAG_ACTIVITY_REQUIRE_DEFAULT, "FLAG_ACTIVITY_REQUIRE_DEFAULT");
            flags.put(Intent.FLAG_ACTIVITY_REQUIRE_NON_BROWSER, "FLAG_ACTIVITY_REQUIRE_NON_BROWSER");
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
            flags.put(Intent.FLAG_ACTIVITY_MATCH_EXTERNAL, "FLAG_ACTIVITY_MATCH_EXTERNAL");
        flags.put(Intent.FLAG_ACTIVITY_LAUNCH_ADJACENT, "FLAG_ACTIVITY_LAUNCH_ADJACENT");
        flags.put(Intent.FLAG_ACTIVITY_RETAIN_IN_RECENTS, "FLAG_ACTIVITY_RETAIN_IN_RECENTS");
        flags.put(Intent.FLAG_ACTIVITY_TASK_ON_HOME, "FLAG_ACTIVITY_TASK_ON_HOME");
        flags.put(Intent.FLAG_ACTIVITY_CLEAR_TASK, "FLAG_ACTIVITY_CLEAR_TASK");
        flags.put(Intent.FLAG_ACTIVITY_NO_ANIMATION, "FLAG_ACTIVITY_NO_ANIMATION");
        flags.put(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT, "FLAG_ACTIVITY_REORDER_TO_FRONT");
        flags.put(Intent.FLAG_ACTIVITY_NO_USER_ACTION, "FLAG_ACTIVITY_NO_USER_ACTION");
        flags.put(Intent.FLAG_ACTIVITY_NEW_DOCUMENT, "FLAG_ACTIVITY_NEW_DOCUMENT");
        flags.put(Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY, "FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY");
        flags.put(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED, "FLAG_ACTIVITY_RESET_TASK_IF_NEEDED");
        //Intent.FLAG_RECEIVER_VISIBLE_TO_INSTANT_APPS, // <-- for broadcasts
        flags.put(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT, "FLAG_ACTIVITY_BROUGHT_TO_FRONT");
        flags.put(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS, "FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS");
        flags.put(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP, "FLAG_ACTIVITY_PREVIOUS_IS_TOP");
        flags.put(Intent.FLAG_ACTIVITY_FORWARD_RESULT, "FLAG_ACTIVITY_FORWARD_RESULT");
        flags.put(Intent.FLAG_ACTIVITY_CLEAR_TOP, "FLAG_ACTIVITY_CLEAR_TOP");
        flags.put(Intent.FLAG_ACTIVITY_MULTIPLE_TASK, "FLAG_ACTIVITY_MULTIPLE_TASK");
        //Intent.FLAG_RECEIVER_NO_ABORT, // <-- for broadcasts
        flags.put(Intent.FLAG_ACTIVITY_NEW_TASK, "FLAG_ACTIVITY_NEW_TASK");
        //Intent.FLAG_RECEIVER_FOREGROUND, // <-- for broadcasts
        flags.put(Intent.FLAG_ACTIVITY_SINGLE_TOP, "FLAG_ACTIVITY_SINGLE_TOP");
        //Intent.FLAG_RECEIVER_REPLACE_PENDING, // <-- for broadcasts
        flags.put(Intent.FLAG_ACTIVITY_NO_HISTORY, "FLAG_ACTIVITY_NO_HISTORY");
        //Intent.FLAG_RECEIVER_REGISTERED_ONLY // <-- for broadcasts
        return flags;
    }
    public static HashMap<String, Integer> getAllAsStrToInt() {
        HashMap<String, Integer> flags = new HashMap<>();

        flags.put("FLAG_GRANT_READ_URI_PERMISSION", Intent.FLAG_GRANT_READ_URI_PERMISSION);
        flags.put("FLAG_GRANT_WRITE_URI_PERMISSION", Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        flags.put("FLAG_FROM_BACKGROUND", Intent.FLAG_FROM_BACKGROUND);
        flags.put("FLAG_DEBUG_LOG_RESOLUTION", Intent.FLAG_DEBUG_LOG_RESOLUTION);
        flags.put("FLAG_EXCLUDE_STOPPED_PACKAGES", Intent.FLAG_EXCLUDE_STOPPED_PACKAGES);
        flags.put("FLAG_INCLUDE_STOPPED_PACKAGES", Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        flags.put("FLAG_GRANT_PERSISTABLE_URI_PERMISSION", Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        flags.put("FLAG_GRANT_PREFIX_URI_PERMISSION", Intent.FLAG_GRANT_PREFIX_URI_PERMISSION);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            flags.put("FLAG_DIRECT_BOOT_AUTO", Intent.FLAG_DIRECT_BOOT_AUTO);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            flags.put("FLAG_ACTIVITY_REQUIRE_DEFAULT", Intent.FLAG_ACTIVITY_REQUIRE_DEFAULT);
            flags.put("FLAG_ACTIVITY_REQUIRE_NON_BROWSER", Intent.FLAG_ACTIVITY_REQUIRE_NON_BROWSER);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
            flags.put("FLAG_ACTIVITY_MATCH_EXTERNAL", Intent.FLAG_ACTIVITY_MATCH_EXTERNAL);
        flags.put("FLAG_ACTIVITY_LAUNCH_ADJACENT", Intent.FLAG_ACTIVITY_LAUNCH_ADJACENT);
        flags.put("FLAG_ACTIVITY_RETAIN_IN_RECENTS", Intent.FLAG_ACTIVITY_RETAIN_IN_RECENTS);
        flags.put("FLAG_ACTIVITY_TASK_ON_HOME", Intent.FLAG_ACTIVITY_TASK_ON_HOME);
        flags.put("FLAG_ACTIVITY_CLEAR_TASK", Intent.FLAG_ACTIVITY_CLEAR_TASK);
        flags.put("FLAG_ACTIVITY_NO_ANIMATION", Intent.FLAG_ACTIVITY_NO_ANIMATION);
        flags.put("FLAG_ACTIVITY_REORDER_TO_FRONT", Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        flags.put("FLAG_ACTIVITY_NO_USER_ACTION", Intent.FLAG_ACTIVITY_NO_USER_ACTION);
        flags.put("FLAG_ACTIVITY_NEW_DOCUMENT", Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        flags.put("FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY", Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY);
        flags.put("FLAG_ACTIVITY_RESET_TASK_IF_NEEDED", Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        //Intent.FLAG_RECEIVER_VISIBLE_TO_INSTANT_APPS, // <-- for broadcasts
        flags.put("FLAG_ACTIVITY_BROUGHT_TO_FRONT", Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
        flags.put("FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS", Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        flags.put("FLAG_ACTIVITY_PREVIOUS_IS_TOP", Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
        flags.put("FLAG_ACTIVITY_FORWARD_RESULT", Intent.FLAG_ACTIVITY_FORWARD_RESULT);
        flags.put("FLAG_ACTIVITY_CLEAR_TOP", Intent.FLAG_ACTIVITY_CLEAR_TOP);
        flags.put("FLAG_ACTIVITY_MULTIPLE_TASK", Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        //Intent.FLAG_RECEIVER_NO_ABORT, // <-- for broadcasts
        flags.put("FLAG_ACTIVITY_NEW_TASK", Intent.FLAG_ACTIVITY_NEW_TASK);
        //Intent.FLAG_RECEIVER_FOREGROUND, // <-- for broadcasts
        flags.put("FLAG_ACTIVITY_SINGLE_TOP", Intent.FLAG_ACTIVITY_SINGLE_TOP);
        //Intent.FLAG_RECEIVER_REPLACE_PENDING, // <-- for broadcasts
        flags.put("FLAG_ACTIVITY_NO_HISTORY", Intent.FLAG_ACTIVITY_NO_HISTORY);
        //Intent.FLAG_RECEIVER_REGISTERED_ONLY // <-- for broadcasts
        return flags;
    }
    public static int[] separate(int flags) {
        List<Integer> individualFlags = new ArrayList<>();
        HashMap<Integer, String> allFlags = getAllAsIntToStr();
        for (Integer flag : allFlags.keySet())
            if ((flags & flag) == flag)
                individualFlags.add(flag);
        return individualFlags.stream().mapToInt(i -> i).toArray();
    }
    public static String[] separateAsStr(int flags) {
        List<String> individualFlags = new ArrayList<>();
        HashMap<Integer, String> allFlags = getAllAsIntToStr();
        for (Map.Entry<Integer, String> flagEntry : allFlags.entrySet())
            if ((flags & flagEntry.getKey()) == flagEntry.getKey())
                individualFlags.add(flagEntry.getValue());
        return individualFlags.toArray(new String[0]);
    }
}
