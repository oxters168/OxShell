package com.OxGames.OxShell.Views;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
//import android.os.Debug;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.text.method.ScrollingMovementMethod;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.OxGames.OxShell.Data.SettingsKeeper;
import com.OxGames.OxShell.Helpers.AndroidHelpers;
import com.OxGames.OxShell.Helpers.MathHelpers;
import com.OxGames.OxShell.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DebugView extends FrameLayout {
    public static final float TTL_INDEFINITE = -1;
    private final Context context;
    private boolean isShown;
    private BetterTextView debugLabel;
    private int framesPerFpsCalculation = 16;

    private static class OutputData {
        String output;
        long timeSet; // in milliseconds
        float ttl; // in seconds

        private OutputData(String output, float ttl) {
            updateOutput(output, ttl);
            //this.output = output;
            //timeSet = SystemClock.uptimeMillis();
            //this.ttl = ttl;

        }
        private OutputData(String output) {
            this(output, -1);
        }
        private void updateOutput(String output) {
            this.output = output;
            timeSet = SystemClock.uptimeMillis();
        }
        private void updateOutput(String output, float ttl) {
            this.output = output;
            timeSet = SystemClock.uptimeMillis();
            this.ttl = ttl;
        }
    }
    private static final HashMap<String, OutputData> outputs = new HashMap<>();

    public DebugView(@NonNull Context context) {
        super(context);
        this.context = context;
        init();
    }
    public DebugView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
    }
    public DebugView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init();
    }
    public DebugView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.context = context;
        init();
    }

    private void init() {
        isShown = false;
        setVisibility(GONE);

        LayoutParams layoutParams;

        layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        layoutParams.gravity = Gravity.TOP | Gravity.LEFT;
        setLayoutParams(layoutParams);
        //setBackground(ContextCompat.getDrawable(context, R.drawable.rounded_outline_shape));
        //setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#BB323232")));
        setFocusable(false);

        debugLabel = new BetterTextView(context);
        layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        layoutParams.gravity = Gravity.TOP | Gravity.LEFT;
        int borderMargin = getBorderMargin();
        layoutParams.setMargins(borderMargin, borderMargin, borderMargin, borderMargin);
        debugLabel.setLayoutParams(layoutParams);
        //msg.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        debugLabel.setOverScrollMode(SCROLL_AXIS_VERTICAL);
        debugLabel.setMovementMethod(new ScrollingMovementMethod());
        //msg.setEllipsize(TextUtils.TruncateAt.END);
        debugLabel.setGravity(Gravity.TOP | Gravity.START);
        debugLabel.setTextAlignment(View.TEXT_ALIGNMENT_GRAVITY);
        debugLabel.setTextColor(context.getColor(R.color.text));
        debugLabel.setTextSize(getTextSize());
        debugLabel.setOutlineColor(Color.parseColor("#000000"));
        int textOutlineSize = Math.round(AndroidHelpers.getScaledDpToPixels(context, 3));
        debugLabel.setOutlineSize(textOutlineSize);
        //debugLabel.setText("Quos voluptas commodi maxime dolore eveniet enim commodi et. Et qui nobis est earum eum. Excepturi quis nostrum consectetur ipsum debitis nihil autem. Vitae maiores ducimus et aut voluptas. Est ipsa aliquam quibusdam id atque. Veritatis nisi non minus quo aut. Qui voluptate eos nihil dolores aut. Atque debitis quidem similique molestias perferendis eum numquam qui. Necessitatibus hic quia nulla minus occaecati occaecati est. Unde qui culpa distinctio ea repellat omnis cumque voluptatibus. Vel ut non iste. Numquam ut est temporibus eveniet et exercitationem maxime. Adipisci rerum magnam ipsa laudantium dolores. Vitae ea rem dicta molestiae ut rerum placeat. Repellat fugiat et quo corporis culpa facilis quia. Vel et rerum doloribus porro reiciendis est aut. Illum nihil non et molestiae nostrum. Molestiae dolor cupiditate a numquam adipisci nobis. Rerum saepe libero doloribus incidunt sunt molestias explicabo. Error inventore libero quam nostrum voluptates minima corporis voluptatem. Culpa illum vel ut qui aut in. Eligendi perferendis pariatur dolorum reiciendis sit. Ut et labore magnam quas debitis. Autem et enim enim quia nam voluptatibus illo.");
        debugLabel.setFocusable(false);
        debugLabel.setBlockTouchInput(false);
        Typeface font = SettingsKeeper.getFont();
        debugLabel.setTypeface(font);
        addView(debugLabel);
    }

    private int getBorderMargin() {
        return Math.round(AndroidHelpers.getScaledDpToPixels(context, 8));
    }
    private int getTextSize() {
        return Math.round(AndroidHelpers.getScaledSpToPixels(context, 8));
    }

    public static void print(String key, String output, float ttl) {
        if (outputs.containsKey(key))
            outputs.get(key).updateOutput(output, ttl);
        else
            outputs.put(key, new OutputData(output, ttl));
    }
    public static void print(String key, String output) {
        if (outputs.containsKey(key))
            outputs.get(key).updateOutput(output);
        else
            outputs.put(key, new OutputData(output));
    }
    public static void clear(String key) {
        if (key != null)
            outputs.remove(key);
    }
    public static void clear() {
        outputs.clear();
    }
    private static void cleanOutputData() {
        List<String> removeKeys = new ArrayList<>();
        for (Map.Entry<String, OutputData> entry : outputs.entrySet())
            if (entry.getValue().ttl >= 0 && (SystemClock.uptimeMillis() - entry.getValue().timeSet) / 1000f > entry.getValue().ttl)
                removeKeys.add(entry.getKey());
        for (String key : removeKeys)
            outputs.remove(key);
    }
    public void setShown(boolean onOff) {
        isShown = onOff;
        if (isShown) {
            final long[] prevTime = { SystemClock.uptimeMillis() };
            final int[] framesPassed = { 0 };
            final int[] fps = { -1 };
            final Handler smh = new Handler(Looper.getMainLooper());
            smh.post(new Runnable() {
                @SuppressLint("SetTextI18n")
                @Override
                public void run() {
                    if (framesPassed[0] >= framesPerFpsCalculation) {
                        long currentTime = SystemClock.uptimeMillis();
                        fps[0] = Math.round((1000f * framesPerFpsCalculation) / (currentTime - prevTime[0]));
                        framesPassed[0] = 0;
                        prevTime[0] = currentTime;
                    } else
                        framesPassed[0]++;

                    // source: https://stackoverflow.com/a/19267315/5430992
                    long bytesInMb = 1048576;
                    // device ram info
                    ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
                    ((ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE)).getMemoryInfo(memoryInfo);
                    long nativeHeapSize = memoryInfo.totalMem;
                    long nativeHeapFreeSize = memoryInfo.availMem;
                    long usedMemInBytes = nativeHeapSize - nativeHeapFreeSize;
                    float usedMemInPercentage = (float)MathHelpers.roundTo(usedMemInBytes * (100.0 / nativeHeapSize), 2);
                    // app heap info
                    final Runtime runtime = Runtime.getRuntime();
                    final long usedMem = (runtime.totalMemory() - runtime.freeMemory());
                    final long maxHeapSize = runtime.maxMemory();
                    float usedHeapInPercentage = (float)MathHelpers.roundTo(usedMem / ((double)maxHeapSize), 2);
//                    debugLabel.setText(
//                            "FPS: " + (fps[0] >= 0 ? fps[0] : "?")
//                            + "\nHeap: " + Math.round(usedMem / (double)bytesInMb) + " mb / " + Math.round(maxHeapSize / (double)bytesInMb) + " mb | " + usedHeapInPercentage + "% used"
//                            + "\nRAM: " + Math.round(usedMemInBytes / (double)bytesInMb) + " mb / " + Math.round(nativeHeapSize / (double)bytesInMb) + " mb | " + usedMemInPercentage + "% used"
//                    );

                    StringBuilder sb = new StringBuilder();
                    sb.append("FPS: ").append(fps[0] >= 0 ? fps[0] : "?").append("\nHeap: ").append(Math.round(usedMem / (double) bytesInMb)).append(" mb / ").append(Math.round(maxHeapSize / (double) bytesInMb)).append(" mb | ").append(usedHeapInPercentage).append("% used").append("\nRAM: ").append(Math.round(usedMemInBytes / (double) bytesInMb)).append(" mb / ").append(Math.round(nativeHeapSize / (double) bytesInMb)).append(" mb | ").append(usedMemInPercentage).append("% used");
                    cleanOutputData();
                    for (OutputData outputData : outputs.values())
                        sb.append("\n").append(outputData.output);
                    debugLabel.setText(sb);

                    if (isShown)
                        smh.post(this);
                }
            });
        }
        setVisibility(onOff ? VISIBLE : GONE);
    }
    public boolean isDebugShown() {
        return isShown;
    }
}
