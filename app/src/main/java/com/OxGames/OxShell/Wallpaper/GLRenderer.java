package com.OxGames.OxShell.Wallpaper;

import android.opengl.GLSurfaceView;
import android.util.Log;

import com.OxGames.OxShell.Data.Paths;
import com.OxGames.OxShell.Helpers.AndroidHelpers;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

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
        //Log.d("GLRenderer", AndroidHelpers.combinePaths(Paths.SHADER_ITEMS_DIR_INTERNAL, "vert.vsh") + ", " + AndroidHelpers.combinePaths(Paths.SHADER_ITEMS_DIR_INTERNAL, "frag.fsh"));
        String vert = AndroidHelpers.readFile(AndroidHelpers.combinePaths(Paths.SHADER_ITEMS_DIR_INTERNAL, "vert.vsh"));
        String frag = AndroidHelpers.readFile(AndroidHelpers.combinePaths(Paths.SHADER_ITEMS_DIR_INTERNAL, "frag.fsh"));
        shader = new Shader(glVersion, vert, frag);
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
