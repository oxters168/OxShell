package com.OxGames.OxShell.Helpers;

import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.util.Log;

import com.OxGames.OxShell.OxShellApp;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Queue;
import java.util.UUID;

public class AudioHelper {
    private static HashMap<UUID, AudioPool> pools = new HashMap<>();
    private static class AudioPool {
        private final List<MediaPlayer> beingPrepped;
        private final Queue<MediaPlayer> unusedPlayers;

        private AudioPool() {
            beingPrepped = new ArrayList<>();
            unusedPlayers = new ArrayDeque<>();
        }
        public boolean playerAvailable() {
            return !unusedPlayers.isEmpty();
        }

        public static AudioPool fromAsset(String assetLoc, int poolSize) {
            AudioPool pool = new AudioPool();
            new Thread(() -> {
                try {
                    AssetFileDescriptor afd = OxShellApp.getContext().getAssets().openFd(assetLoc);
                    for (int i = 0; i < poolSize; i++) {
                        MediaPlayer player = new MediaPlayer();
                        try {
                            player.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                            pool.beingPrepped.add(player);
                            player.prepare();
                        } catch (Exception e) {
                            Log.e("AudioHelper", "Failed to load asset into MediaPlayer: " + e);
                        }
                    }
                    afd.close();
                } catch (Exception e) {
                    Log.e("AudioHelper", "Failed to read asset: " + e);
                }
            }).start();
            return pool;
        }
    }

    public static UUID loadFromAsset(String assetLoc) {
        UUID id = UUID.randomUUID();
        pools.put(id, AudioPool.fromAsset(assetLoc, 1));
        return id;
    }
}
