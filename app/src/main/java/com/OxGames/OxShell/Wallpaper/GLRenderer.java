package com.OxGames.OxShell.Wallpaper;

import android.opengl.GLSurfaceView;
import android.util.Log;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class GLRenderer implements GLSurfaceView.Renderer {
    private Shader shader;

    public GLRenderer() {
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        //Log.d("GLRenderer", "Surface created");
        shader = new Shader();
    }
    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        //Log.d("GLRenderer", "Surface changed");
        shader.setViewportSize(width, height);
    }
    @Override
    public void onDrawFrame(GL10 gl) {
        shader.draw();
    }
}
