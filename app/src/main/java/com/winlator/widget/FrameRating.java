package com.winlator.widget;

import android.app.ActivityManager;
import android.content.Context;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import app.gamenative.R;
import timber.log.Timber;

import com.winlator.container.Container;
import com.winlator.container.Shortcut;
import com.winlator.core.GPUInformation;
import com.winlator.core.StringUtils;
import com.winlator.xenvironment.ImageFs;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FrameRating extends FrameLayout implements Runnable {
    private Context context;
    private long lastTime = 0;
    private int frameCount = 0;
    private float lastFPS = 0;
    private String totalRAM = null;
    private final TextView tvFPS;
    private final TextView tvRenderer;
    private final TextView tvGPU;
    private final TextView tvRAM;
    private HashMap graphicsDriverConfig;

    // FPS reading tracking
    private static final int READING_INTERVAL_MS = 1000;
    private int readingCount = 0;
    private long sessionStartTime = 0;
    private int maxFPS = 0;
    private int minFPS = Integer.MAX_VALUE;
    private long lastReadingTime = 0;
    private long fpsSum = 0;

    public FrameRating(Context context) {
        this(context, (HashMap)null);
    }

    public FrameRating(Context context, HashMap graphicsDriverConfig) {
        this(context, graphicsDriverConfig ,null);
    }

    public FrameRating(Context context, HashMap graphicsDriverConfig, AttributeSet attrs) {
        this(context, graphicsDriverConfig, attrs, 0);
    }

    public FrameRating(Context context, HashMap graphicsDriverConfig, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        View view = LayoutInflater.from(context).inflate(R.layout.frame_rating, this, false);
        tvFPS = view.findViewById(R.id.TVFPS);
        tvRenderer = view.findViewById(R.id.TVRenderer);
        tvRenderer.setText("OpenGL");
        tvGPU = view.findViewById(R.id.TVGPU);
        if (graphicsDriverConfig != null && graphicsDriverConfig.containsKey("version")) {
            tvGPU.setText(GPUInformation.getRenderer(graphicsDriverConfig.get("version").toString(), context));
        } else {
            tvGPU.setText(GPUInformation.getRenderer(context));
        }
        tvRAM = view.findViewById(R.id.TVRAM);
        totalRAM = getTotalRAM();
        this.graphicsDriverConfig = graphicsDriverConfig;
        addView(view);
    }
    
    private String getTotalRAM() {
        ActivityManager activityManager = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        activityManager.getMemoryInfo(memoryInfo);
        return StringUtils.formatBytes(memoryInfo.totalMem);
    }
    
    private String getAvailableRAM() {
        ActivityManager activityManager = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        activityManager.getMemoryInfo(memoryInfo);
        long usedMem = memoryInfo.totalMem - memoryInfo.availMem;
        return StringUtils.formatBytes(usedMem, false);
    }

    public void setRenderer(String renderer) {
        tvRenderer.setText(renderer);
    }

    public void setGpuName (String gpuName) {
        tvGPU.setText(gpuName);
    }

    public void reset() {
        tvRenderer.setText("OpenGL");
        if (graphicsDriverConfig != null && graphicsDriverConfig.containsKey("version")) {
            tvGPU.setText(GPUInformation.getRenderer(graphicsDriverConfig.get("version").toString(), context));
        }
    }

    public void update() {
        if (lastTime == 0) {
            lastTime = SystemClock.elapsedRealtime();
            sessionStartTime = SystemClock.elapsedRealtime();
        }
        long time = SystemClock.elapsedRealtime();
        if (time >= lastTime + 500) {
            lastFPS = ((float)(frameCount * 1000) / (time - lastTime));

            if (lastReadingTime == 0 || time >= lastReadingTime + READING_INTERVAL_MS) {
                int currentFPS = Math.round(lastFPS);
                readingCount++;
                fpsSum += currentFPS;

                if (currentFPS > maxFPS) maxFPS = currentFPS;
                if (currentFPS > 1 && currentFPS < minFPS) minFPS = currentFPS;

                lastReadingTime = time;
            }

            post(this);
            lastTime = time;
            frameCount = 0;
        }
        frameCount++;
    }

    public float getAvgFPS() {
        if (readingCount == 0) return 0;
        return (float) fpsSum / readingCount;
    }

    public float getSessionLengthSec() {
        if (sessionStartTime == 0) return 0;
        return (SystemClock.elapsedRealtime() - sessionStartTime) / 1000.0f;
    }

    public void writeSessionSummary() {
        if (readingCount == 0) return;

        final float sessionLengthSec = getSessionLengthSec();
        final int max = maxFPS;
        final int min = minFPS == Integer.MAX_VALUE ? 0 : minFPS;
        final float avgFPS = getAvgFPS();

        ImageFs imageFs = ImageFs.find(context);
        File fpsLogFile = new File(imageFs.getTmpDir(), "fps_session.json");
        ExecutorService fileWriteExecutor = Executors.newSingleThreadExecutor();

        fileWriteExecutor.execute(() -> {
            try {
                if (!fpsLogFile.exists()) fpsLogFile.createNewFile();
                String json = String.format(Locale.ENGLISH,
                    "{\n  \"length_sec\": %.2f,\n  \"avg_fps\": %.1f,\n  \"max_fps\": %d,\n  \"min_fps\": %d,\n  \"readings\": %d\n}\n",
                    sessionLengthSec, avgFPS, max, min, readingCount);
                try (FileWriter fw = new FileWriter(fpsLogFile, false)) {
                    fw.write(json);
                    fw.flush();
                }
                Timber.d("Session summary written to: %s", fpsLogFile.getAbsolutePath());
            } catch (IOException e) {
                Timber.e(e, "Failed to write session summary");
            } finally {
                fileWriteExecutor.shutdown();
            }
        });
    }

    @Override
    public void run() {
        if (getVisibility() == GONE) setVisibility(View.VISIBLE);
        tvFPS.setText(String.format(Locale.ENGLISH, "%.1f", lastFPS));
        tvRAM.setText(getAvailableRAM() + " Used / " + totalRAM + " Total");
    }
}
