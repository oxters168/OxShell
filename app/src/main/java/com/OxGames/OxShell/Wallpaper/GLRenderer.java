package com.OxGames.OxShell.Wallpaper;

import android.opengl.GLES20;
import android.opengl.GLES30;
import android.opengl.GLES31;
import android.opengl.GLES32;
import android.opengl.GLSurfaceView;
import android.util.Log;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

public class GLRenderer implements GLSurfaceView.Renderer {
    private Shader shader;
    private int glVersion;

    public GLRenderer(int glVersion) {
        this.glVersion = glVersion;
    }

    public int getGLVersion() {
        return glVersion;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        //Log.d("GLRenderer", "Surface created");
        //TODO: need to set up shader for when version is less than 2
//        if (glVersion < 0x20000)
//            throw new UnsupportedOperationException("OpenGL version 1 is unsupported");
        shader = new Shader(glVersion);
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
