package com.OxGames.OxShell.Wallpaper;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.opengl.GLSurfaceView;
import android.view.SurfaceHolder;

public class OpenGLES3WallpaperService extends GLWallpaperService {
    @Override
    public Engine onCreateEngine() {
        return new OpenGLES3Engine();
    }

    public class OpenGLES3Engine extends  GLWallpaperService.GLEngine {
        @Override
        public void onCreate(SurfaceHolder surfaceHolder) {
            super.onCreate(surfaceHolder);

            //final ActivityManager activityManager = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
            //final ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();
            //final boolean supportsEs31 = configurationInfo.reqGlEsVersion >= 0x30001;
            //final boolean supportsEs3 = configurationInfo.reqGlEsVersion >= 0x30000;
            //final boolean supportsEs2 = configurationInfo.reqGlEsVersion >= 0x20000;

            GLRenderer glRenderer = new GLRenderer();
//            if (supportsEs3) {
//                // Request an OpenGL ES 3.0 compatible context.
//                setEGLContextClientVersion(3);
//                // On Honeycomb+ devices, this improves the performance when
//                // leaving and resuming the live wallpaper.
//                setPreserveEGLContextOnPause(true);
//                // Set the renderer to our user-defined renderer.
//                setRenderer(glRenderer);
            //if (supportsEs2) {
                // Request an OpenGL ES 2.0 compatible context.
                setEGLContextClientVersion(3);
                // On Honeycomb+ devices, this improves the performance when
                // leaving and resuming the live wallpaper.
                setPreserveEGLContextOnPause(true);
                // Set the renderer to our user-defined renderer.
                setRenderer(glRenderer);
            //} else {
//                // Request an OpenGL ES 1.0 compatible context.
//                setEGLContextClientVersion(1);
//                // On Honeycomb+ devices, this improves the performance when
//                // leaving and resuming the live wallpaper.
//                setPreserveEGLContextOnPause(true);
//                // Set the renderer to our user-defined renderer.
//                setRenderer(glRenderer);
            //}
        }
    }
    //abstract GLSurfaceView.Renderer getNewRenderer();
}
