package com.OxGames.OxShell.Wallpaper;

import android.opengl.GLES20;
import android.opengl.GLES30;
import android.opengl.Matrix;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Shader {
    private static final int UNKNOWN_ATTRIBUTE = -1;
    // built-in shader attribute names
    private static final String VERTEX_SHADER_IN_POSITION = "inPosition";
    private static final String VERTEX_SHADER_IN_TEXTURE_COORD = "inTextureCoord";
    // built-in shader uniform names
    private static final String VERTEX_SHADER_UNIFORM_MATRIX_MVP = "uMVPMatrix";
    private static final String VERTEX_SHADER_UNIFORM_MATRIX_STM = "uSTMatrix";
    private static final String FRAGMENT_SHADER_UNIFORM_TIME = "iTime";
    private static final String FRAGMENT_SHADER_UNIFORM_RESOLUTION = "iResolution";

    private FloatBuffer quadVertices;
    private final float[] matrixMVP = new float[16];
    private final float[] matrixSTM = new float[16];
    private static final int FLOAT_SIZE_BYTES = 4;
    private static final int TRIANGLE_VERTICES_DATA_STRIDE_BYTES = 5 * FLOAT_SIZE_BYTES;
    private static final int TRIANGLE_VERTICES_DATA_POS_OFFSET = 0;
    private static final int TRIANGLE_VERTICES_DATA_UV_OFFSET = 3;

    // built-in shader attribute handles
    //private int inPositionHandle = UNKNOWN_ATTRIBUTE;
    //private int inTextureHandle = UNKNOWN_ATTRIBUTE;
    // built-in shader uniform handles
    private int mMVPMatrixHandle = UNKNOWN_ATTRIBUTE;
    private int mSTMatrixHandle = UNKNOWN_ATTRIBUTE;
    private int iTimeHandle = UNKNOWN_ATTRIBUTE;
    private int iResolutionHandle = UNKNOWN_ATTRIBUTE;

    private int targetFPS = 30;
    private long startTime;
    //private long prevTime;

    private int programHandle;
    private static final String fallbackVertex =
            "#version 300 es                                                       \n"
            + "uniform mat4 uMVPMatrix;                                            \n"
            + "uniform mat4 uSTMatrix;                                             \n"
            + "in vec3 inPosition;                                                 \n"
            + "in vec2 inTextureCoord;                                             \n"
            + "out vec2 textureCoord;                                              \n"
            + "void main()                                                         \n"   // The entry point for our vertex shader.
            + "{                                                                   \n"
            + "   gl_Position = uMVPMatrix * vec4(inPosition.xyz, 1);              \n"   // gl_Position is a special variable used to store the final position.
            + "   textureCoord = (uSTMatrix * vec4(inTextureCoord.xy, 0, 0)).xy;   \n"
            + "}                                                                   \n";
    // when precision is set to mediump, animating with mod on iTime causes 'slowdown' after a few minutes
    private static final String fallbackFragment =
            "#version 300 es                                                                          \n"
            + "precision highp float;                                                                 \n"   // Set the default precision to medium. We don't need as high of a
                                                                                                            // precision in the fragment shader.
            + "uniform float iTime;                                                                   \n"
            + "uniform vec2 iResolution;                                                              \n"
            + "in vec2 textureCoord;                                                                  \n"
            + "out vec4 fragColor;                                                                    \n"
            + "void main()                                                                            \n"   // The entry point for our fragment shader.
            + "{                                                                                      \n"
            + "   float loop = sin(mod(iTime / 2., 3.14));                                            \n"
            + "   vec3 col = vec3(vec2(1) - textureCoord.xy, (textureCoord.x + textureCoord.y) / 2.); \n"
            + "   fragColor = vec4(abs(vec3(loop) - col.xyz), 1);                                     \n"   // Pass the color directly through the pipeline.
            + "}                                                                                      \n";

    public Shader() {
        initValues();
        createProgram(null);
    }
    public Shader(String vertexCode, String fragmentCode) {
        initValues();

        HashMap<Integer, String> shaderCode = new HashMap<>();
        shaderCode.put(GLES30.GL_VERTEX_SHADER, vertexCode);
        shaderCode.put(GLES30.GL_FRAGMENT_SHADER, fragmentCode);
        createProgram(shaderCode);
    }
    public Shader(HashMap<Integer, String> shaderCode) {
        initValues();
        createProgram(shaderCode);
    }

    protected int getProgramHandle() {
        return programHandle;
    }
    public void setTargetFPS(int value) {
        targetFPS = value;
    }
    public int getTargetFPS() {
        return targetFPS;
    }

    protected void draw() {
        long currentTime = System.currentTimeMillis() - startTime;
        //float deltaTime = (currentTime - prevTime) / 1000f;
        //prevTime = currentTime;
        //float fps = 1 / deltaTime;
        float secondsElapsed = (currentTime) / 1000f; // will be input into shader as iTime
        //Log.d("GLRenderer", "Drawing frame, timeElapsed: " + secondsElapsed + "s deltaTime: " + deltaTime + "s fps: " + fps);
        GLES30.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        GLES30.glClear(GLES30.GL_DEPTH_BUFFER_BIT | GLES30.GL_COLOR_BUFFER_BIT);

        GLES30.glUseProgram(getProgramHandle());
        checkGlError("glUseProgram");

        // built-in matrix uniforms
        GLES30.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, matrixMVP, 0);
        GLES30.glUniformMatrix4fv(mSTMatrixHandle, 1, false, matrixSTM, 0);

        // built-in helper uniforms (similar to Shadertoy)
        if (iTimeHandle != UNKNOWN_ATTRIBUTE)
            GLES30.glUniform1f(iTimeHandle, secondsElapsed);

        GLES30.glBlendFunc(GLES30.GL_SRC_ALPHA, GLES30.GL_ONE_MINUS_SRC_ALPHA);
        GLES30.glEnable(GLES20.GL_BLEND);

        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, 4);
        checkGlError("glDrawArrays");

        GLES30.glFinish();

        try {
            long sleepMillis = (long)((1f / targetFPS) * 1000f * 0.88f); //Added *0.88f since for some reason the sleep is slightly off
            //Log.d("GLRenderer", "Sleep time " + sleepMillis + "ms");
            Thread.sleep(sleepMillis);
        } catch(Exception e) { Log.e("GLRenderer", e.toString()); }
    }
    protected void setViewportSize(int width, int height) {
        // Set the OpenGL viewport to the same size as the surface.
        //Log.d("Shader", width + ", " + height);
        GLES30.glViewport(0, 0, width, height);
        if (iResolutionHandle != UNKNOWN_ATTRIBUTE)
            GLES30.glUniform2f(iResolutionHandle, width, height);
    }

    /**
     * get location of some input attribute for shader
     */
    private int glGetAttribLocation(String attrName) {
        int attrLocation = GLES30.glGetAttribLocation(getProgramHandle(), attrName);
        checkGlError("glGetAttribLocation " + attrName);
        return attrLocation;
    }
    /**
     * set values for attributes of input
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

    private void initValues() {
        startTime = System.currentTimeMillis();
        //prevTime = 0;

        // set array of Quad vertices
        float[] quadVerticesData = new float[] {
                -1.0f, -1.0f, 0f, 0f, 1f,
                1.0f, -1.0f, 0f, 1f, 1f,
                -1.0f, 1.0f, 0f, 0f, 0f,
                1.0f, 1.0f, 0f, 1f, 0f
        };
        quadVertices = ByteBuffer.allocateDirect(quadVerticesData.length * FLOAT_SIZE_BYTES).order(ByteOrder.nativeOrder()).asFloatBuffer();
        quadVertices.put(quadVerticesData).position(0);
    }
    private void createProgram(HashMap<Integer, String> shaderCode) {
        List<Integer> handles = new ArrayList<>();
        if (shaderCode != null) {
            for (Integer shaderType : shaderCode.keySet())
                handles.add(loadShader(shaderType, shaderCode.get(shaderType)));
        } else {
            handles.add(loadShader(GLES30.GL_VERTEX_SHADER, fallbackVertex));
            handles.add(loadShader(GLES30.GL_FRAGMENT_SHADER, fallbackFragment));
        }

        // Create a program object and store the handle to it.
        programHandle = GLES30.glCreateProgram();
        if (programHandle != 0)
        {
            // Bind the vertex,fragment,etc shader to the program.
            for (Integer handle : handles)
                GLES30.glAttachShader(programHandle, handle);

            // Link the two shaders together into a program.
            GLES30.glLinkProgram(programHandle);

            // Get the link status.
            final int[] linkStatus = new int[1];
            GLES30.glGetProgramiv(programHandle, GLES30.GL_LINK_STATUS, linkStatus, 0);

            // If the link failed, delete the program.
            if (linkStatus[0] != GLES30.GL_TRUE)
            {
                GLES30.glDeleteProgram(programHandle);
                programHandle = 0;
                throw new RuntimeException("Error creating program");
            } else
                prepHandles();
        }
    }
    private void prepHandles() {
        // shader input (built-in attributes)
        int inPositionHandle = glGetAttribLocation(VERTEX_SHADER_IN_POSITION);
        int inTextureHandle = glGetAttribLocation(VERTEX_SHADER_IN_TEXTURE_COORD);
        setAttribute(inPositionHandle, VERTEX_SHADER_IN_POSITION, 3, TRIANGLE_VERTICES_DATA_POS_OFFSET);
        setAttribute(inTextureHandle, VERTEX_SHADER_IN_TEXTURE_COORD, 2, TRIANGLE_VERTICES_DATA_UV_OFFSET);

        // built-in matrix uniforms
        Matrix.setIdentityM(matrixMVP, 0);
        Matrix.setIdentityM(matrixSTM, 0);
        mMVPMatrixHandle = GLES30.glGetUniformLocation(getProgramHandle(), VERTEX_SHADER_UNIFORM_MATRIX_MVP);
        mSTMatrixHandle = GLES30.glGetUniformLocation(getProgramHandle(), VERTEX_SHADER_UNIFORM_MATRIX_STM);
        //GLES30.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, matrixMVP, 0);
        //GLES30.glUniformMatrix4fv(mSTMatrixHandle, 1, false, matrixSTM, 0);

        // built-in helper uniforms (similar to Shadertoy)
        iTimeHandle = GLES30.glGetUniformLocation(getProgramHandle(), FRAGMENT_SHADER_UNIFORM_TIME);
        iResolutionHandle = GLES30.glGetUniformLocation(getProgramHandle(), FRAGMENT_SHADER_UNIFORM_RESOLUTION);
        //Log.d("Shader", "mvpMatrixHandle: " + mMVPMatrixHandle + " stMatrixHandle: " + mSTMatrixHandle + " iTimeHandle: " + iTimeHandle + " iResolutionHandle: " + iResolutionHandle);
    }
    private int loadShader(int shaderType, String shaderCode) {
        // Load in the vertex shader.
        int shaderHandle = GLES30.glCreateShader(shaderType);
        if (shaderHandle != 0)
        {
            // Pass in the shader source.
            GLES30.glShaderSource(shaderHandle, shaderCode);
            // Compile the shader.
            GLES30.glCompileShader(shaderHandle);
            // Get the compilation status.
            final int[] compileStatus = new int[1];
            GLES30.glGetShaderiv(shaderHandle, GLES30.GL_COMPILE_STATUS, compileStatus, 0);
            // If the compilation failed, delete the shader.
            if (compileStatus[0] == 0)
            {
                GLES30.glDeleteShader(shaderHandle);
                shaderHandle = 0;
                throw new RuntimeException("Error compiling shader (type " + shaderType + ")");
            }
        }
        return shaderHandle;
    }

    private void checkGlError(String op) {
        int error = GLES30.glGetError();
        if (error != GLES30.GL_NO_ERROR) {
            //Log.e("GLRenderer", op + ": glError " + error);
            throw new RuntimeException(op + ": glError " + error);
        }
    }
}
