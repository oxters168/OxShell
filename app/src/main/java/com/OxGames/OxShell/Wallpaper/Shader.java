package com.OxGames.OxShell.Wallpaper;

import android.opengl.GLES10;
import android.opengl.GLES20;
import android.opengl.GLES30;
import android.opengl.GLES31;
import android.opengl.GLES32;
import android.opengl.Matrix;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Shader {
    private static final int UNKNOWN_ATTRIBUTE = -1;
    private static final int UNKNOWN_PROGRAM = 0;
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

    private Class<? extends GLES20> glClass;
    private static final int GL_DEFAULT_VERSION = 0x30002;

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

    //TODO: when creating a new shader with given code, should use fallback if it fails
    public Shader(int glVersion) {
        initValues(glVersion);

        programHandle = createProgram(glClass,null);
        prepHandles();
    }
    public Shader(int glVersion, String vertexCode, String fragmentCode) {
        initValues(glVersion);

        HashMap<Integer, String> shaderCode = new HashMap<>();
        try { shaderCode.put((int)glClass.getField("GL_VERTEX_SHADER").get(null), vertexCode); } catch(Exception e) { Log.e("Shader", e.toString()); }
        try { shaderCode.put((int)glClass.getField("GL_FRAGMENT_SHADER").get(null), fragmentCode); } catch(Exception e) { Log.e("Shader", e.toString()); }

        programHandle = createProgram(glClass, shaderCode);
        if (programHandle == UNKNOWN_PROGRAM)
            programHandle = createProgram(glClass,null);
        prepHandles();
    }
    public Shader(int glVersion, HashMap<Integer, String> shaderCode) {
        initValues(glVersion);

        programHandle = createProgram(glClass, shaderCode);
        if (programHandle == UNKNOWN_PROGRAM)
            programHandle = createProgram(glClass,null);
        prepHandles();
    }
    public Shader() {
        this(GL_DEFAULT_VERSION);
    }
    public Shader(String vertexCode, String fragmentCode) {
        this(GL_DEFAULT_VERSION, vertexCode, fragmentCode);
    }
    public Shader(HashMap<Integer, String> shaderCode) {
        this(GL_DEFAULT_VERSION, shaderCode);
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
        try {
            long currentTime = System.currentTimeMillis() - startTime;
            //float deltaTime = (currentTime - prevTime) / 1000f;
            //prevTime = currentTime;
            //float fps = 1 / deltaTime;
            float secondsElapsed = (currentTime) / 1000f; // will be input into shader as iTime
            //Log.d("GLRenderer", "Drawing frame, timeElapsed: " + secondsElapsed + "s deltaTime: " + deltaTime + "s fps: " + fps);
            glClass.getMethod("glClearColor", float.class, float.class, float.class, float.class).invoke(null, 0.0f, 0.0f, 0.0f, 0.0f);
            glClass.getMethod("glClear", int.class).invoke(null, ((int)glClass.getField("GL_DEPTH_BUFFER_BIT").get(null)) | ((int)glClass.getField("GL_COLOR_BUFFER_BIT").get(null)));

            glClass.getMethod("glUseProgram", int.class).invoke(null, getProgramHandle());
            checkGlError("glUseProgram");

            // built-in matrix uniforms
            glClass.getMethod("glUniformMatrix4fv", int.class, int.class, boolean.class, float[].class, int.class).invoke(null, mMVPMatrixHandle, 1, false, matrixMVP, 0);
            glClass.getMethod("glUniformMatrix4fv", int.class, int.class, boolean.class, float[].class, int.class).invoke(null, mSTMatrixHandle, 1, false, matrixSTM, 0);

            // built-in helper uniforms (similar to Shadertoy)
            if (iTimeHandle != UNKNOWN_ATTRIBUTE)
                glClass.getMethod("glUniform1f", int.class, float.class).invoke(null, iTimeHandle, secondsElapsed);

            glClass.getMethod("glBlendFunc", int.class, int.class).invoke(null, glClass.getField("GL_SRC_ALPHA").get(null), glClass.getField("GL_ONE_MINUS_SRC_ALPHA").get(null));
            glClass.getMethod("glEnable", int.class).invoke(null, glClass.getField("GL_BLEND").get(null));

            glClass.getMethod("glDrawArrays", int.class, int.class, int.class).invoke(null, glClass.getField("GL_TRIANGLE_STRIP").get(null), 0, 4);
            checkGlError("glDrawArrays");

            glClass.getMethod("glFinish").invoke(null);
        } catch(Exception e) { Log.e("Shader", e.toString()); }

        try {
            long sleepMillis = (long)((1f / targetFPS) * 1000f * 0.88f); //Added *0.88f since for some reason the sleep is slightly off
            //Log.d("GLRenderer", "Sleep time " + sleepMillis + "ms");
            Thread.sleep(sleepMillis);
        } catch(Exception e) { Log.e("Shader", e.toString()); }
    }
    protected void setViewportSize(int width, int height) {
        // Set the OpenGL viewport to the same size as the surface.
        //Log.d("Shader", width + ", " + height);
        try {
            glClass.getMethod("glViewport", int.class, int.class, int.class, int.class).invoke(null, 0, 0, width, height);
            if (iResolutionHandle != UNKNOWN_ATTRIBUTE)
                glClass.getMethod("glUniform2f", int.class, float.class, float.class).invoke(null, iResolutionHandle, width, height);
        } catch(Exception e) { Log.e("Shader", e.toString()); }
    }

    /**
     * get location of some input attribute for shader
     */
    private int glGetAttribLocation(String attrName) {
        int attrLocation = UNKNOWN_ATTRIBUTE;
        try {
            attrLocation = (int)glClass.getMethod("glGetAttribLocation", int.class, String.class).invoke(null, getProgramHandle(), attrName);
            checkGlError("glGetAttribLocation " + attrName);
        } catch(Exception e) { Log.e("Shader", e.toString()); }
        return attrLocation;
    }
    /**
     * set values for attributes of input
     */
    private void setAttribute(int attrLocation, String attrName, int size, int offset) {
        // skip it if undefined
        if (attrLocation == UNKNOWN_ATTRIBUTE)
            return;
        try {
            quadVertices.position(offset);
            glClass.getMethod("glVertexAttribPointer", int.class, int.class, int.class, boolean.class, int.class, java.nio.Buffer.class).invoke(null, attrLocation, size, (int)glClass.getField("GL_FLOAT").get(null), false, TRIANGLE_VERTICES_DATA_STRIDE_BYTES, quadVertices);
            checkGlError("glVertexAttribPointer " + attrName);
            glClass.getMethod("glEnableVertexAttribArray", int.class).invoke(null, attrLocation);
            checkGlError("glEnableVertexAttribArray " + attrName);
        } catch(Exception e) { Log.e("Shader", e.toString()); }
    }

    private void initValues(int glVersion) {
        switch(glVersion) {
            case 0x30002:
                Log.d("Shader", "Using GLES32");
                glClass = GLES32.class;
                break;
            case 0x30001:
                Log.d("Shader", "Using GLES31");
                glClass = GLES31.class;
                break;
            case 0x30000:
                Log.d("Shader", "Using GLES30");
                glClass = GLES30.class;
                break;
            case 0x20000:
                Log.d("Shader", "Using GLES20");
                glClass = GLES20.class;
                break;
            default:
                throw new UnsupportedOperationException("OpenGL version " + glVersion + " (convert to hex) is not supported");
        }

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
    private static int createProgram(Class<? extends GLES20> glClass, HashMap<Integer, String> shaderCode) {
        int programHandle = UNKNOWN_PROGRAM;
        List<Integer> handles = new ArrayList<>();
        if (shaderCode != null) {
            for (Integer shaderType : shaderCode.keySet())
                handles.add(loadShader(glClass, shaderType, shaderCode.get(shaderType)));
        } else {
            try {
                handles.add(loadShader(glClass, (int)glClass.getField("GL_VERTEX_SHADER").get(null), fallbackVertex));
                handles.add(loadShader(glClass, (int)glClass.getField("GL_FRAGMENT_SHADER").get(null), fallbackFragment));
            } catch(Exception e) { Log.e("Shader", e.toString()); }
        }

        try {
            // Create a program object and store the handle to it.
            programHandle = (int)glClass.getMethod("glCreateProgram").invoke(null);
            if (programHandle != UNKNOWN_PROGRAM) {
                // Bind the vertex,fragment,etc shader to the program.
                for (Integer handle : handles)
                    glClass.getMethod("glAttachShader", int.class, int.class).invoke(null, programHandle, handle);

                // Link the two shaders together into a program.
                glClass.getMethod("glLinkProgram", int.class).invoke(null, programHandle);

                // Get the link status.
                final int[] linkStatus = new int[1];
                glClass.getMethod("glGetProgramiv", int.class, int.class, int[].class, int.class).invoke(null, programHandle, (int)glClass.getField("GL_LINK_STATUS").get(null), linkStatus, 0);

                // If the link failed, delete the program.
                if (linkStatus[0] != (int)glClass.getField("GL_TRUE").get(null)) {
                    glClass.getMethod("glDeleteProgram", int.class).invoke(null, programHandle);
                    programHandle = UNKNOWN_PROGRAM;
                    Log.e("Shader", "Failed to create shader program due to linking issue");
                    //throw new RuntimeException("Error creating program");
                }
            } else
                Log.e("Shader", "Failed to create shader program due to unknown issue");
        } catch(Exception e) { Log.e("Shader", e.toString()); }
        return programHandle;
    }
    private static int loadShader(Class<? extends GLES20> glClass, int shaderType, String shaderCode) {
        int shaderHandle = 0;
        try {
            // Load in the vertex shader.
            shaderHandle = (int)glClass.getMethod("glCreateShader", int.class).invoke(null, shaderType);
            if (shaderHandle != 0) {
                // Pass in the shader source.
                glClass.getMethod("glShaderSource", int.class, String.class).invoke(null, shaderHandle, shaderCode);
                // Compile the shader.
                glClass.getMethod("glCompileShader", int.class).invoke(null, shaderHandle);
                // Get the compilation status.
                final int[] compileStatus = new int[1];
                glClass.getMethod("glGetShaderiv", int.class, int.class, int[].class, int.class).invoke(null, shaderHandle, (int)glClass.getField("GL_COMPILE_STATUS").get(null), compileStatus, 0);
                // If the compilation failed, delete the shader.
                if (compileStatus[0] == 0) {
                    glClass.getMethod("glDeleteShader", int.class).invoke(null, shaderHandle);
                    shaderHandle = 0;
                    Log.e("Shader", "Error compiling shader (type " + shaderType + ")");
                    //throw new RuntimeException("Error compiling shader (type " + shaderType + ")");
                }
            }
        } catch (Exception e) { Log.e("Shader", e.toString()); }
        return shaderHandle;
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
        try {
            mMVPMatrixHandle = (int)glClass.getMethod("glGetUniformLocation", int.class, String.class).invoke(null, getProgramHandle(), VERTEX_SHADER_UNIFORM_MATRIX_MVP);
            mSTMatrixHandle = (int)glClass.getMethod("glGetUniformLocation", int.class, String.class).invoke(null, getProgramHandle(), VERTEX_SHADER_UNIFORM_MATRIX_STM);

            // built-in helper uniforms (similar to Shadertoy)
            iTimeHandle = (int)glClass.getMethod("glGetUniformLocation", int.class, String.class).invoke(null, getProgramHandle(), FRAGMENT_SHADER_UNIFORM_TIME);
            iResolutionHandle = (int)glClass.getMethod("glGetUniformLocation", int.class, String.class).invoke(null, getProgramHandle(), FRAGMENT_SHADER_UNIFORM_RESOLUTION);
            //Log.d("Shader", "mvpMatrixHandle: " + mMVPMatrixHandle + " stMatrixHandle: " + mSTMatrixHandle + " iTimeHandle: " + iTimeHandle + " iResolutionHandle: " + iResolutionHandle);
        } catch(Exception e) { Log.e("Shader", e.toString()); }
    }

    private void checkGlError(String op) {
        int error = 0;
        int glNoErrorCode = 0;
        try {
            error = (int)glClass.getMethod("glGetError").invoke(null);
            glNoErrorCode = (int)glClass.getField("GL_NO_ERROR").get(null);
        } catch(Exception e) { Log.e("Shader", e.toString()); }
        if (error != glNoErrorCode) {
            //Log.e("GLRenderer", op + ": glError " + error);
            throw new RuntimeException(op + ": glError " + error);
        }
    }
}
