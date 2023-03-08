package com.OxGames.OxShell.Wallpaper;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.opengl.GLSurfaceView;
import android.os.BatteryManager;
import android.os.SystemClock;
import android.util.Log;
import android.view.MotionEvent;

import com.OxGames.OxShell.Data.Paths;
import com.OxGames.OxShell.Helpers.AndroidHelpers;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class GLRenderer implements GLSurfaceView.Renderer {
    //public final String STORAGE_DIR_EXTERNAL;
    //public final String STORAGE_DIR_INTERNAL;
    //public final String SHADER_ITEMS_DIR_EXTERNAL;
    //public final String SHADER_ITEMS_DIR_INTERNAL;

    private Shader shader;
    private int glVersion;
    private int width, height;
    private GLWallpaperService.GLEngine callingEngine;
    //private boolean mouseDown;
    private long lastChange;
    public boolean reload = false;

    public GLRenderer(Context context, int glVersion, GLWallpaperService.GLEngine callingEngine) {
        this.glVersion = glVersion;
        this.callingEngine = callingEngine;
        //STORAGE_DIR_EXTERNAL = AndroidHelpers.combinePaths(Environment.getExternalStorageDirectory().toString(), "/OxShell");
        //STORAGE_DIR_INTERNAL = context.getExternalFilesDir(null).toString();
        //SHADER_ITEMS_DIR_EXTERNAL = AndroidHelpers.combinePaths(STORAGE_DIR_EXTERNAL, "Shader");
        //SHADER_ITEMS_DIR_INTERNAL = AndroidHelpers.combinePaths(STORAGE_DIR_INTERNAL, "Shader");
        //SHADER_ITEMS_DIR_INTERNAL = Paths.SHADER_ITEMS_DIR_INTERNAL;
    }

    public int getGLVersion() {
        return glVersion;
    }

    public void onBatteryReceive(Context context, Intent intent) {
        int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        int plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        float percent = level / (float)scale;
        boolean charging = status == BatteryManager.BATTERY_STATUS_CHARGING;
        boolean full = status == BatteryManager.BATTERY_STATUS_FULL;
        boolean usb = plugged == BatteryManager.BATTERY_PLUGGED_USB;
        boolean wall = plugged == BatteryManager.BATTERY_PLUGGED_AC;
        boolean wireless = plugged == BatteryManager.BATTERY_PLUGGED_WIRELESS;

        //Log.d("GLWallpaperService", "battery " + percent + " charging " + charging + " ac " + wall + " usb " + usb + " wireless " + wireless);

        if (shader != null)
            shader.setBatteryInfo(percent, full, charging, wall, usb, wireless);
    }
    public void onTouchEvent(MotionEvent ev) {
        float z = -1;
        float w = -1;
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            // if the user is just pressing without moving their finger then onTouchEvent doesn't keep getting called
            // so z=1, w=1 will stay that way in the shader
            z = 1;
            w = 1;
        } else if (ev.getAction() == MotionEvent.ACTION_MOVE)
            z = 1;

        //Log.d("GLRenderer", z + ", " + w + ", " + ev.toString());
        if (shader != null)
            shader.setMousePos(ev.getX(), height - ev.getY(), z, w);
    }
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        Log.d("GLRenderer", "Surface created");
        //TODO: set up shader for when version is less than 2
//        if (glVersion < 0x20000)
//            throw new UnsupportedOperationException("OpenGL version 1 is unsupported");
        //Log.d("GLRenderer", AndroidHelpers.combinePaths(Paths.SHADER_ITEMS_DIR_INTERNAL, "vert.vsh") + ", " + AndroidHelpers.combinePaths(Paths.SHADER_ITEMS_DIR_INTERNAL, "frag.fsh"));
        shader = new Shader(glVersion);
        reloadFromFile(true);
//        shader = new Shader(glVersion);
    }
    public void reloadFromFile(boolean firstLoad) {
        if (shader != null) {
            String vert = null;
            String frag = null;
            final String shaderDir = Paths.SHADER_ITEMS_DIR_INTERNAL;
            String vertPath = AndroidHelpers.combinePaths(shaderDir, "vert.vsh");
            String fragPath = AndroidHelpers.combinePaths(shaderDir, "frag.fsh");
            if (AndroidHelpers.fileExists(vertPath))
                vert = AndroidHelpers.readFile(vertPath);
            if (AndroidHelpers.fileExists(fragPath))
                frag = AndroidHelpers.readFile(fragPath);
            //shader = new Shader(glVersion, vert, frag);
            String oldVert = shader.getVertexShaderCode();
            String oldFrag = shader.getFragmentShaderCode();
            boolean shadersChanged = oldVert == null || oldFrag == null || !oldVert.equals(vert) || !oldFrag.equals(frag);
//            if (!shadersChanged) {
//                Log.d("GLRenderer", frag);
//                Log.d("GLRenderer", oldFrag);
//            }
            long currentTime = SystemClock.uptimeMillis();
            //boolean reload = firstLoad || (currentTime - lastChange) > 200;
            Log.d("GLRenderer", "Attempting to reload " + GLWallpaperService.getEngineIndex(callingEngine) + ", firstLoad: " + firstLoad + ", reload: " + reload + ", timeDiff: " + (!firstLoad ? currentTime - lastChange : null) + ", visibility: " + callingEngine.isVisible() + ", preview: " + callingEngine.isPreview() + ", preview present: " + GLWallpaperService.isPreviewPresent());
            //if (reload) {
            if (firstLoad || reload) {
                Log.d("GLRenderer", "Applying reload on " + GLWallpaperService.getEngineIndex(callingEngine) + ", firstLoad: " + firstLoad + ", reload: " + reload + ", shadersChanged: " + shadersChanged + ", visibility: " + callingEngine.isVisible() + ", preview: " + callingEngine.isPreview() + ", preview present: " + GLWallpaperService.isPreviewPresent());
                //callingEngine.getSurfaceView().setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
                reload = false;
                lastChange = currentTime;
                shader.setShaderCode(vert, frag);

                for (int i = 0; i < Shader.MAX_TEXTURE_COUNT; i++) {
                    String currentImagePath = AndroidHelpers.combinePaths(shaderDir, "channel" + i + ".png");
                    if (AndroidHelpers.fileExists(currentImagePath)) {
                        Bitmap bitmap = AndroidHelpers.bitmapFromFile(currentImagePath);
                        shader.bindTexture(bitmap, "iChannel" + i);
                        bitmap.recycle();
                    }
                }
                //callingEngine.getSurfaceView().setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
            }
        }
    }
    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        Log.d("GLRenderer", "Surface changed");
        reloadFromFile(false);
        this.width = width;
        this.height = height;
        if (shader != null)
            shader.setViewportSize(width, height);
        else
            Log.e("GLRender", "Attempting to change values to null shader");
    }
    @Override
    public void onDrawFrame(GL10 gl) {
        if (shader != null)
            shader.draw();
        else
            Log.e("GLRenderer", "Cannot draw null shader");
    }
}
