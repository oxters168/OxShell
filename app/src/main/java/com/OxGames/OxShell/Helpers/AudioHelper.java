package com.OxGames.OxShell.Helpers;

import android.net.Uri;

import java.util.HashMap;
import java.util.UUID;

public class AudioHelper {
    private static HashMap<UUID, AudioPool> pools = new HashMap<>();

    public static UUID loadFromAsset(String assetLoc) {
        UUID id = UUID.randomUUID();
        pools.put(id, AudioPool.fromAsset(assetLoc, 5));
        return id;
    }
    public static UUID loadFromFile(String filePath) {
        UUID id = UUID.randomUUID();
        pools.put(id, AudioPool.fromFile(filePath, 1));
        return id;
    }
    public static UUID loadFromUri(Uri uri) {
        UUID id = UUID.randomUUID();
        pools.put(id, AudioPool.fromUri(uri, 1));
        return id;
    }
}
