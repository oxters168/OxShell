package com.OxGames.OxShell.Wallpaper;

import android.opengl.GLES20;
import android.opengl.GLES30;
import android.opengl.GLES31;
import android.opengl.GLES32;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import com.appspell.shaderview.log.LibLog;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class GLRenderer implements GLSurfaceView.Renderer {
    private static final int UNKNOWN_ATTRIBUTE = -1;
    private static final String VERTEX_SHADER_IN_POSITION = "inPosition";
    private static final String VERTEX_SHADER_IN_TEXTURE_COORD = "inTextureCoord";
    private static final String VERTEX_SHADER_UNIFORM_MATRIX_MVP = "uMVPMatrix";
    private static final String VERTEX_SHADER_UNIFORM_MATRIX_STM = "uSTMatrix";
//
    private FloatBuffer quadVertices;
    private float[] matrixMVP = new float[16];
    private float[] matrixSTM = new float[16];
    private static final int FLOAT_SIZE_BYTES = 4;
    private static final int TRIANGLE_VERTICES_DATA_STRIDE_BYTES = 5 * FLOAT_SIZE_BYTES;
    private static final int TRIANGLE_VERTICES_DATA_POS_OFFSET = 0;
    private static final int TRIANGLE_VERTICES_DATA_UV_OFFSET = 3;
//
//    // shader vertex attributes
    private static int inPositionHandle = UNKNOWN_ATTRIBUTE;
    private static int inTextureHandle = UNKNOWN_ATTRIBUTE;

    private Shader shader;

    private long startTime;
    private long prevTime;

//    private String vertexShader;
//    private String fragmentShader;
//    final String vertexShader =
//        "#version 300 es                                                          \n"
//        + "//uniform mat4 uMVPMatrix;                                              \n"
//        + "//uniform mat4 uSTMatrix;                                             \n"
//        + "//in vec3 inPosition;                                                 \n"
//        + "//in vec2 inTextureCoord;                                             \n"
//        + "//out vec2 textureCoord;                                              \n"
//        + "void main()                                                         \n"   // The entry point for our vertex shader.
//        + "{                                                                   \n"
//        + "   //gl_Position = uMVPMatrix * vec4(inPosition.xyz, 1);              \n"   // gl_Position is a special variable used to store the final position.
//        + "   //textureCoord = (uSTMatrix * vec4(inTextureCoord.xy, 0, 0)).xy;   \n"
//        + "}                                                                   \n";
//    final String fragmentShader =
//        "#version 300 es                      \n"
//        + "precision mediump float;           \n"   // Set the default precision to medium. We don't need as high of a
//                                                  // precision in the fragment shader.
//        + "//in vec2 textureCoord;            \n"
//        + "out vec4 fragColor;              \n"
//        + "void main()                      \n"   // The entry point for our fragment shader.
//        + "{                                \n"
//        + "   fragColor = vec4(0,1,0,1); \n"   // Pass the color directly through the pipeline.
//        + "}                                \n";

//    /** This will be used to pass in the transformation matrix. */
//    private int mMVPMatrixHandle;
//    /** This will be used to pass in model position information. */
//    private int mPositionHandle;
//    /** This will be used to pass in model color information. */
//    private int mColorHandle;
    //int programHandle;

    public GLRenderer() {
        // set array of Quad vertices
        float[] quadVerticesData = new float[] {
            -1.0f, -1.0f, 0f, 0f, 1f,
            1.0f, -1.0f, 0f, 1f, 1f,
            -1.0f, 1.0f, 0f, 0f, 0f,
            1.0f, 1.0f, 0f, 1f, 0f
        };
        quadVertices = ByteBuffer.allocateDirect(quadVerticesData.length * FLOAT_SIZE_BYTES).order(ByteOrder.nativeOrder()).asFloatBuffer();
        quadVertices.put(quadVerticesData).position(0);

//        Context context = ActivityManager.getCurrentActivity();
//        fragmentShader = AndroidHelpers.readAssetAsString(context, "xmb.fsh");
//        vertexShader = AndroidHelpers.readAssetAsString(context, "default.vsh");
    }


    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        startTime = System.currentTimeMillis();
        //prevTime = 0;
        shader = new Shader();
//        int vertexShaderHandle = loadShader(GLES30.GL_VERTEX_SHADER, vertexShader);
//        int fragmentShaderHandle = loadShader(GLES30.GL_FRAGMENT_SHADER, fragmentShader);
//
//        // Create a program object and store the handle to it.
//        programHandle = GLES30.glCreateProgram();
//        if (programHandle != 0)
//        {
//            // Bind the vertex shader to the program.
//            GLES30.glAttachShader(programHandle, vertexShaderHandle);
//
//            // Bind the fragment shader to the program.
//            GLES30.glAttachShader(programHandle, fragmentShaderHandle);
//
//            // Bind attributes
//            //GLES30.glBindAttribLocation(programHandle, 0, "a_Position");
//            //GLES30.glBindAttribLocation(programHandle, 1, "a_Color");
//
//            // Link the two shaders together into a program.
//            GLES30.glLinkProgram(programHandle);
//
//            // Get the link status.
//            final int[] linkStatus = new int[1];
//            GLES30.glGetProgramiv(programHandle, GLES30.GL_LINK_STATUS, linkStatus, 0);
//
//            // If the link failed, delete the program.
//            if (linkStatus[0] != GLES30.GL_TRUE)
//            {
//                GLES30.glDeleteProgram(programHandle);
//                programHandle = 0;
//                throw new RuntimeException("Error creating program");
//            }
//        }

        // Set program handles. These will later be used to pass in values to the program.
//        mMVPMatrixHandle = GLES20.glGetUniformLocation(programHandle, "u_MVPMatrix");
//        mPositionHandle = GLES20.glGetAttribLocation(programHandle, "a_Position");
//        mColorHandle = GLES20.glGetAttribLocation(programHandle, "a_Color");
        // set attributes (input for Vertex Shader)
//        inPositionHandle = glGetAttribLocation(VERTEX_SHADER_IN_POSITION);
//        inTextureHandle = glGetAttribLocation(VERTEX_SHADER_IN_TEXTURE_COORD);

        // Tell OpenGL to use this program when rendering.
//        GLES30.glUseProgram(programHandle);
    }
    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        // Set the OpenGL viewport to the same size as the surface.
        Log.d("GLRenderer", width + ", " + height);
        GLES30.glViewport(0, 0, width, height);
    }
    @Override
    public void onDrawFrame(GL10 gl) {
        long currentTime = System.currentTimeMillis() - startTime;
        //float deltaTime = (currentTime - prevTime) / 1000f;
        //prevTime = currentTime;
        //float fps = 1 / deltaTime;
        float secondsElapsed = (currentTime) / 1000f; // will be input into shader as iTime
        //Log.d("GLRenderer", "Drawing frame, timeElapsed: " + secondsElapsed + "s deltaTime: " + deltaTime + "s fps: " + fps);
        GLES30.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        GLES30.glClear(GLES30.GL_DEPTH_BUFFER_BIT | GLES30.GL_COLOR_BUFFER_BIT);

        GLES30.glUseProgram(shader.getProgramHandle());
        checkGlError("glUseProgram");

//        // shader input (built-in attributes)
        inPositionHandle = glGetAttribLocation(VERTEX_SHADER_IN_POSITION);
        inTextureHandle = glGetAttribLocation(VERTEX_SHADER_IN_TEXTURE_COORD);
        setAttribute(inPositionHandle, VERTEX_SHADER_IN_POSITION, 3, TRIANGLE_VERTICES_DATA_POS_OFFSET);
        setAttribute(inTextureHandle, VERTEX_SHADER_IN_TEXTURE_COORD, 2, TRIANGLE_VERTICES_DATA_UV_OFFSET);
//
//        // built-in uniforms
        Matrix.setIdentityM(matrixMVP, 0);
        Matrix.setIdentityM(matrixSTM, 0);
        int mMVPMatrixHandle = GLES30.glGetUniformLocation(shader.getProgramHandle(), VERTEX_SHADER_UNIFORM_MATRIX_MVP);
        int mSTMatrixHandle = GLES30.glGetUniformLocation(shader.getProgramHandle(), VERTEX_SHADER_UNIFORM_MATRIX_STM);
        GLES30.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, matrixMVP, 0);
        GLES30.glUniformMatrix4fv(mSTMatrixHandle, 1, false, matrixSTM, 0);
//
        GLES30.glBlendFunc(GLES30.GL_SRC_ALPHA, GLES30.GL_ONE_MINUS_SRC_ALPHA);
        GLES30.glEnable(GLES20.GL_BLEND);

        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, 4);
        checkGlError("glDrawArrays");

        GLES30.glFinish();

        try {
            int targetFPS = 30;
            long sleepMillis = (long)((1f / targetFPS) * 1000f * 0.88f); //Added *0.88f since for some reason the sleep is slightly off
            //Log.d("GLRenderer", "Sleep time " + sleepMillis + "ms");
            Thread.sleep(sleepMillis);
        } catch(Exception e) { Log.e("GLRenderer", e.toString()); }
    }

    /**
     * get location of some input attribute for shader
     */
    private int glGetAttribLocation(String attrName) {
        int attrLocation = GLES30.glGetAttribLocation(shader.getProgramHandle(), attrName);
        checkGlError("glGetAttribLocation " + attrName);
        return attrLocation;
    }
    /**
     * set values for attributes of input vertices
     */
    private void setAttribute(int attrLocation, String attrName, int size, int offset) {
        if (attrLocation == UNKNOWN_ATTRIBUTE) {
            // skip it if undefined
            return;
        }
        quadVertices.position(offset);
        GLES30.glVertexAttribPointer(attrLocation, size, GLES30.GL_FLOAT, false, TRIANGLE_VERTICES_DATA_STRIDE_BYTES, quadVertices);
        checkGlError("glVertexAttribPointer " + attrName);
        GLES30.glEnableVertexAttribArray(attrLocation);
        checkGlError("glEnableVertexAttribArray " + attrName);
    }

    private void checkGlError(String op) {
        int error = GLES30.glGetError();
        if (error != GLES30.GL_NO_ERROR) {
            //Log.e("GLRenderer", op + ": glError " + error);
            throw new RuntimeException(op + ": glError " + error);
        }
    }
}
