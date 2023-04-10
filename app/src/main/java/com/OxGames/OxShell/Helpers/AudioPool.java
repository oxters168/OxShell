package com.OxGames.OxShell.Helpers;

import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;

import com.OxGames.OxShell.Data.DataLocation;
import com.OxGames.OxShell.Data.DataRef;
import com.OxGames.OxShell.OxShellApp;
import com.OxGames.OxShell.Views.DebugView;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

public class AudioPool {
    private static final int COMPLETE_MILLIS = 500; // Through multiple tests, these values seemed to work well to remove the weird clicks/gaps between audio playback
    private static final int STOP_MILLIS = 300;
    private DataRef dataRef;
    private final List<MediaPlayer> beingPrepped;
    private final Queue<MediaPlayer> unusedPlayers;
    private final LinkedList<MediaPlayer> playingPlayers;

    private final List<Runnable> completedListeners;

    private AudioPool() {
        beingPrepped = new ArrayList<>();
        unusedPlayers = new ArrayDeque<>();
        playingPlayers = new LinkedList<>();

        completedListeners = new ArrayList<>();
    }

    public void addOnCompletedListener(Runnable listener) {
        completedListeners.add(listener);
    }
    public void removeOnCompletedListener(Runnable listener) {
        completedListeners.remove(listener);
    }
    public void clearOnCompletedListeners() {
        completedListeners.clear();
    }

    public boolean isPlayerAvailable() {
        return !unusedPlayers.isEmpty();
    }

    public void play(boolean loop) {
        Runnable play = () -> {
            MediaPlayer player = unusedPlayers.poll();
            playingPlayers.add(player);

            final int duration = player.getDuration();
            if (duration <= COMPLETE_MILLIS) {
                // if duration of the audio is short, then use the ordinary method of checking completion
                player.setOnCompletionListener(mp -> {
                    for (Runnable listener : completedListeners)
                        if (listener != null)
                            listener.run();
                    if (playingPlayers.contains(mp)) {
                        playingPlayers.remove(mp);
                        unusedPlayers.add(mp);
                    }
                });
                player.setLooping(loop);
            }

            player.setVolume(1, 1);
            player.start();

            if (duration > COMPLETE_MILLIS) {
                // if the duration of the audio is long enough, then use the better way of checking for completion
                final Handler completionHandler = new Handler(Looper.getMainLooper());
                completionHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        int position = player.getCurrentPosition();
                        //Log.d("AudioPool", position + " >= " + duration + " - " + COMPLETE_MILLIS);
                        if (position >= (duration - COMPLETE_MILLIS)) {
                            if (playingPlayers.contains(player)) {
                                // ready for loop
                                if (!loop) {
                                    for (Runnable listener : completedListeners)
                                        if (listener != null)
                                            listener.run();
                                } else
                                    play(true);
                                playingPlayers.remove(player);
                            }
                            if (position >= (duration - STOP_MILLIS)) {
                                //player.stop(); // can't play again after stop
                                player.pause();
                                player.seekTo(0);
                                unusedPlayers.add(player);
                            } else
                                completionHandler.postDelayed(this, MathHelpers.calculateMillisForFps(60));
                        } else if (playingPlayers.contains(player))
                            completionHandler.postDelayed(this, MathHelpers.calculateMillisForFps(60));
                    }
                });
            }
        };

        if (!isPlayerAvailable()) {
            setPoolSize(getPoolSize() + 5);
            new Thread(() -> {
                try {
                    long startTime = SystemClock.uptimeMillis();
                    while (!isPlayerAvailable()) {
                        if ((SystemClock.uptimeMillis() - startTime) / 1000f > 10)
                            throw new TimeoutException("Failed to play, no audio prepared within 10s");
                        Thread.sleep(10);
                    }
                    play.run();
                } catch (Exception e) {
                    Log.e("AudioPool", "Failed to play audio " + dataRef.getLoc() + ": " + e);
                }
            }).start();
        } else
            play.run();
    }

    public boolean isAnyPlaying() {
        boolean isPlaying = false;
        for (MediaPlayer mp : playingPlayers) {
            if (mp.isPlaying()) {
                isPlaying = true;
                break;
            }
        }
        return isPlaying;
    }
    public int getActiveCount() {
        return playingPlayers.size();
    }
    public void pauseActive() {
        for (MediaPlayer mp : playingPlayers)
            mp.pause();
    }
    public void resumeActive() {
        for (MediaPlayer mp : playingPlayers)
            mp.start();
    }
    public void stopActive() {
        for (MediaPlayer mp : playingPlayers) {
            mp.pause();
            mp.seekTo(0);
            playingPlayers.remove(mp);
            unusedPlayers.add(mp);
        }
    }

    public int getPoolSize() {
        return beingPrepped.size() + unusedPlayers.size() + playingPlayers.size();
    }
    public void setPoolSize(int size) {
        DebugView.print(dataRef.getLoc().toString(), dataRef.getLoc() + ": " + size);
        int diff = size - getPoolSize();
        if (diff > 0) {
            // add media players
            new Thread(() -> {
                try {
                    AssetFileDescriptor afd = null;
                    if (dataRef.getLocType() == DataLocation.asset)
                        afd = OxShellApp.getContext().getAssets().openFd((String)dataRef.getLoc());
                    for (int i = 0; i < diff; i++) {
                        MediaPlayer player = new MediaPlayer();
                        try {
                            if (dataRef.getLocType() == DataLocation.asset)
                                player.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                            else if (dataRef.getLocType() == DataLocation.file)
                                player.setDataSource((String)dataRef.getLoc());
                            else if (dataRef.getLocType() == DataLocation.resolverUri)
                                player.setDataSource(OxShellApp.getContext(), (Uri)dataRef.getLoc());
                            else
                                throw new UnsupportedOperationException("Cannot load data type: " + dataRef.getLocType());
                            beingPrepped.add(player);
                            new Thread(() -> {
                                try {
                                    player.prepare();
                                    if (beingPrepped.contains(player)) {
                                        // if its not in beingPrepped, that means its been 'cancelled'
                                        beingPrepped.remove(player);
                                        unusedPlayers.add(player);
                                    }
                                } catch (Exception e) {
                                    Log.e("AudioHelper", "Failed to prepare MediaPlayer: " + e);
                                }
                            }).start();
                        } catch (Exception e) {
                            Log.e("AudioHelper", "Failed to load asset into MediaPlayer: " + e);
                        }
                    }
                    if (afd != null)
                        afd.close();
                } catch (Exception e) {
                    Log.e("AudioHelper", "Failed to read asset: " + e);
                }
            }).start();
        } else if (diff < 0) {
            // remove media players
            int removeCount = Math.abs(diff);
            int removed = 0;
            if (beingPrepped.size() > 0) {
                for (int i = 0; i < Math.min(beingPrepped.size(), removeCount); i++) {
                    int lastIndex = (beingPrepped.size() - 1) - i;
                    MediaPlayer player = beingPrepped.get(lastIndex);
                    player.reset();
                    player.release();
                    beingPrepped.remove(lastIndex);
                    removed++;
                }
            }
            if (removed < removeCount && unusedPlayers.size() > 0) {
                for (int i = 0; i < Math.min(unusedPlayers.size(), removeCount); i++) {
                    MediaPlayer player = unusedPlayers.poll();
                    player.reset();
                    player.release();
                    removed++;
                }
            }
            if (removed < removeCount && playingPlayers.size() > 0) {
                for (int i = 0; i < Math.min(playingPlayers.size(), removeCount); i++) {
                    MediaPlayer player = playingPlayers.poll();
                    player.reset();
                    player.release();
                    removed++;
                }
            }
        }
    }
    public static AudioPool fromAsset(String assetLoc, int poolSize) {
        AudioPool pool = new AudioPool();
        pool.dataRef = DataRef.from(assetLoc, DataLocation.asset);
        pool.setPoolSize(poolSize);
        return pool;
    }
    public static AudioPool fromFile(String filePath, int poolSize) {
        AudioPool pool = new AudioPool();
        pool.dataRef = DataRef.from(filePath, DataLocation.file);
        pool.setPoolSize(poolSize);
        return pool;
    }
    public static AudioPool fromUri(Uri uri, int poolSize) {
        AudioPool pool = new AudioPool();
        pool.dataRef = DataRef.from(uri, DataLocation.resolverUri);
        pool.setPoolSize(poolSize);
        return pool;
    }
}
