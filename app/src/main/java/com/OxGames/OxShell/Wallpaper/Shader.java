package com.OxGames.OxShell.Wallpaper;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLES30;
import android.opengl.GLES31;
import android.opengl.GLES32;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public class Shader {
    private static final int GL_DEFAULT_VERSION = 0x30002;
    public final static int MAX_TEXTURE_COUNT = 32; // Couldn't find a good way to get the count
    private static final int UNKNOWN_ATTRIBUTE = -1;
    private static final int UNKNOWN_UNIFORM = 0;
    private static final int UNKNOWN_PROGRAM = 0;
    // built-in shader attribute names
    private static final String VERTEX_SHADER_IN_POSITION = "inPosition";
    private static final String VERTEX_SHADER_IN_TEXTURE_COORD = "inTextureCoord";
    // built-in shader uniform names
    private static final String VERTEX_SHADER_UNIFORM_MATRIX_MVP = "uMVPMatrix";
    private static final String VERTEX_SHADER_UNIFORM_MATRIX_STM = "uSTMatrix";
    private static final String FRAGMENT_SHADER_UNIFORM_RESOLUTION = "iResolution";
    private static final String FRAGMENT_SHADER_UNIFORM_TIME = "iTime";
    private static final String FRAGMENT_SHADER_UNIFORM_TIME_DELTA = "iTimeDelta";
    private static final String FRAGMENT_SHADER_UNIFORM_FRAME_RATE = "iFrameRate";
    private static final String FRAGMENT_SHADER_UNIFORM_MOUSE = "iMouse";
    private static final String FRAGMENT_SHADER_UNIFORM_FRAME = "iFrame";
    private static final String FRAGMENT_SHADER_UNIFORM_DATE = "iDate";
    //private static final String FRAGMENT_SHADER_UNIFORM_CHANNEL_TIME = "iChannelTime";
    private static final String FRAGMENT_SHADER_UNIFORM_CHANNEL_RES = "iChannelResolution";
    private static final String FRAGMENT_SHADER_UNIFORM_BATTERY = "iBattery";

    private static final int FLOAT_SIZE_BYTES = 4;
    private static final int TRIANGLE_VERTICES_DATA_STRIDE_BYTES = 5 * FLOAT_SIZE_BYTES;
    private static final int TRIANGLE_VERTICES_DATA_POS_OFFSET = 0;
    private static final int TRIANGLE_VERTICES_DATA_UV_OFFSET = 3;

    private int targetFPS = 30;
    private long startTime;
    private int frame;
    private long prevTime;

    private FloatBuffer quadVertices;
    private final float[] matrixMVP = new float[16];
    private final float[] matrixSTM = new float[16];
    // built-in shader attribute handles
    //private int inPositionHandle = UNKNOWN_ATTRIBUTE;
    //private int inTextureHandle = UNKNOWN_ATTRIBUTE;

    //    Shadertoy Inputs
    //
    //    uniform vec3      iResolution;           // viewport resolution (in pixels) (the z value is the pixel aspect ratio https://stackoverflow.com/questions/27888323/what-does-iresolution-mean-in-a-shader)
    //    uniform float     iTime;                 // shader playback time (in seconds)
    //    uniform float     iTimeDelta;            // render time (in seconds)
    //    uniform float     iFrameRate;            // shader frame rate
    //    uniform int       iFrame;                // shader playback frame
    //    uniform float     iChannelTime[4];       // channel playback time (in seconds)
    //    uniform vec3      iChannelResolution[4]; // channel resolution (in pixels)
    //    uniform vec4      iMouse;                // mouse pixel coords. xy: current (if MLB down), zw: click
    //    uniform samplerXX iChannel0..3;          // input channel. XX = 2D/Cube
    //    uniform vec4      iDate;                 // (year, month, day, time in seconds)
    //    uniform float     iSampleRate;           // sound sample rate (i.e., 44100)
    //
    //    OxShell Added Inputs
    //
    //    uniform vec3     iBattery;               // x=battery percent 0-1, y=[not charging -1, charging 1, full battery 2], z=[not charging -1, ac 1, usb 2, wireless_or_otherwise 3]
    //    uniform vec3?     iGyro;                 // values taken directly from the gyroscope/accelerometer

    // built-in shader uniform handles
    private int mMVPMatrixHandle = UNKNOWN_UNIFORM;
    private int mSTMatrixHandle = UNKNOWN_UNIFORM;
    private int iResolutionHandle = UNKNOWN_UNIFORM;
    private int iTimeHandle = UNKNOWN_UNIFORM;
    private int iTimeDeltaHandle = UNKNOWN_UNIFORM;
    private int iFrameRateHandle = UNKNOWN_UNIFORM;
    private int iFrameHandle = UNKNOWN_UNIFORM;
    //private int iChannelTimeHandle = UNKNOWN_UNIFORM;
    private int iChannelResolutionHandle = UNKNOWN_UNIFORM;
    private int iMouseHandle = UNKNOWN_UNIFORM;
    private int iDateHandle = UNKNOWN_UNIFORM;
    private int iBattery = UNKNOWN_UNIFORM;

    private Class<? extends GLES20> glClass;

    private Queue<Integer> availableTexUnits;
    private HashMap<String, TextureInfo> texHandleUnits;
    //private int setWidth = 1, setHeight = 1;
    private float mousex = 0, mousey = 0, mousez = -1, mousew = -1;
    private BatteryInfo batteryInfo = new BatteryInfo();

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
            + "   textureCoord = vec2(textureCoord.x, 1. - textureCoord.y);        \n"   // Flipped to have parity with Shadertoy
            + "}                                                                   \n";
    // when precision is set to mediump, animating with mod on iTime causes 'slowdown' after a few minutes
    private static final String fallbackFragment =
            "#version 300 es                                                                          \n"
            + "precision highp float;                                                                 \n"
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
//    private static final String fallbackFragment =
//            "#version 300 es                                                                                              \n"
//            + "precision highp float;                                                                                     \n"
//            //+ "uniform sampler2D iChannel0;                                                                               \n"
//            //+ "uniform sampler2D iChannel1;                                                                               \n"
//            //+ "uniform sampler2D iChannel2;                                                                               \n"
//            //+ "uniform sampler2D iChannel3;                                                                               \n"
//            //+ "uniform float iTime;                                                                                       \n"
//            + "uniform vec4 iMouse;                                                                                       \n"
//            + "uniform vec2 iResolution;                                                                                  \n"
//            + "in vec2 textureCoord;                                                                                      \n"
//            + "out vec4 fragColor;                                                                                        \n"
//            + "void main()                                                                                                \n"   // The entry point for our fragment shader.
//            + "{                                                                                                          \n"
//            + "   //float multX = 1.;                                                                                       \n"
//            + "   //float multY = 1.;                                                                                       \n"
//            + "   //if (iResolution.x > iResolution.y) {                                                                    \n"
//            + "   //   multX = iResolution.x / iResolution.y;                                                               \n"
//            + "   //} else if (iResolution.y > iResolution.x) {                                                             \n"
//            + "   //   multY = iResolution.y / iResolution.x;                                                               \n"
//            + "   //}                                                                                                       \n"
//            + "   //if (textureCoord.x * multX > 1. || textureCoord.y * multY > 1.) {                                       \n"
//            + "   //   fragColor = vec4(1.);                                                                                \n"   // Set bg color to white.
//            + "   //} else {                                                                                                \n"
//            + "   //   fragColor = vec4(texture(iChannel2, vec2(textureCoord.x * multX, textureCoord.y * multY)).rgb, 1.);  \n"
//            + "   //}                                                                                                       \n"
//            + "   fragColor = vec4(iMouse.xy / iResolution.xy, 0., 1.);                                                   \n"
//            + "}                                                                                                          \n";

    private class TextureInfo {
        public int unit;
        public int width;
        public int height;
        public TextureInfo(int _unit, int _width, int _height) {
            unit = _unit;
            width = _width;
            height = _height;
        }
    }
    public static class BatteryInfo {
        float percent;
        boolean full;
        boolean charging;
        boolean wall;
        boolean usb;
        boolean wireless;
        public BatteryInfo() {
            percent = 0.5f;
            full = false;
            charging = false;
            wall = false;
            usb = false;
            wireless = false;
        }
        public void set(float percent, boolean full, boolean charging, boolean wall, boolean usb, boolean wireless) {
            this.percent = percent;
            this.full = full;
            this.charging = charging;
            this.wall = wall;
            this.usb = usb;
            this.wireless = wireless;
        }
    }

    public Shader(int glVersion) {
        initValues(glVersion);

        programHandle = createProgram(glClass,null);
        prepHandles();
    }
    public Shader(int glVersion, String vertexCode, String fragmentCode) {
        initValues(glVersion);

        HashMap<Integer, String> shaderCode = new HashMap<>();
        try { shaderCode.put((int)glClass.getField("GL_VERTEX_SHADER").get(null), vertexCode); } catch(Exception e) { Log.e("Shader", "Failed to retrieve constant GL_VERTEX_SHADER: " + e.toString()); }
        try { shaderCode.put((int)glClass.getField("GL_FRAGMENT_SHADER").get(null), fragmentCode); } catch(Exception e) { Log.e("Shader", "Failed to retrieve constant GL_FRAGMENT_SHADER: " + e.toString()); }

        programHandle = createProgram(glClass, shaderCode);
        if (programHandle == UNKNOWN_PROGRAM) {
            Log.w("Shader", "Creating shader failed, using fallback shader");
            programHandle = createProgram(glClass, null);
        }
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
        frame++;
        long currentTime = System.currentTimeMillis() - startTime;
        float deltaTime = (currentTime - prevTime) / 1000f;
        prevTime = currentTime;
        float fps = 1 / deltaTime;
        float secondsElapsed = (currentTime) / 1000f; // will be input into shader as iTime
        //if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
        LocalDateTime localDateTime = LocalDateTime.now();
        int year = localDateTime.getYear();
        int month = localDateTime.getMonthValue();
        int day = localDateTime.getDayOfMonth();
        int seconds = localDateTime.getHour() * 3600 + localDateTime.getMinute() * 60 + localDateTime.getSecond();
        //Log.d("Shader", month + "/" + day + "/" + year + " " + seconds);
        //}
        //Log.d("GLRenderer", "Drawing frame, timeElapsed: " + secondsElapsed + "s deltaTime: " + deltaTime + "s fps: " + fps);
        try {
            glClass.getMethod("glClearColor", float.class, float.class, float.class, float.class).invoke(null, 0.0f, 0.0f, 0.0f, 0.0f);
            glClass.getMethod("glClear", int.class).invoke(null, ((int) glClass.getField("GL_DEPTH_BUFFER_BIT").get(null)) | ((int) glClass.getField("GL_COLOR_BUFFER_BIT").get(null)));
        } catch(Exception e) { Log.e("Shader", "Failed to clear during draw: " + e.toString()); }
        try {
            glClass.getMethod("glUseProgram", int.class).invoke(null, getProgramHandle());
            checkGlError("glUseProgram");
        } catch(Exception e) { Log.e("Shader", "Failed to reference shader in draw: " + e.toString()); }
        try {
            // built-in matrix uniforms
            glClass.getMethod("glUniformMatrix4fv", int.class, int.class, boolean.class, float[].class, int.class).invoke(null, mMVPMatrixHandle, 1, false, matrixMVP, 0);
            glClass.getMethod("glUniformMatrix4fv", int.class, int.class, boolean.class, float[].class, int.class).invoke(null, mSTMatrixHandle, 1, false, matrixSTM, 0);
        } catch(Exception e) { Log.e("Shader", "Failed to set matrices during draw: " + e.toString()); }
        try {
            // built-in helper uniforms (similar to Shadertoy)
            if (iTimeHandle != UNKNOWN_UNIFORM)
                glClass.getMethod("glUniform1f", int.class, float.class).invoke(null, iTimeHandle, secondsElapsed);
            if (iFrameHandle != UNKNOWN_UNIFORM)
                glClass.getMethod("glUniform1i", int.class, int.class).invoke(null, iFrameHandle, frame);
            if (iMouseHandle != UNKNOWN_UNIFORM)
                glClass.getMethod("glUniform4f", int.class, float.class, float.class, float.class, float.class).invoke(null, iMouseHandle, mousex, mousey, mousez, mousew);
            if (iTimeDeltaHandle != UNKNOWN_UNIFORM)
                glClass.getMethod("glUniform1f", int.class, float.class).invoke(null, iTimeDeltaHandle, deltaTime);
            if (iFrameRateHandle != UNKNOWN_UNIFORM)
                glClass.getMethod("glUniform1f", int.class, float.class).invoke(null, iFrameRateHandle, fps);
            if (iDateHandle != UNKNOWN_UNIFORM)
                glClass.getMethod("glUniform4f", int.class, float.class, float.class, float.class, float.class).invoke(null, iDateHandle, year, month, day, seconds);
            if (iBattery != UNKNOWN_UNIFORM)
                glClass.getMethod("glUniform3f", int.class, float.class, float.class, float.class).invoke(null, iBattery, batteryInfo.percent, batteryInfo.charging ? 1 : batteryInfo.full ? 2 : -1, batteryInfo.charging || batteryInfo.full ? batteryInfo.wall ? 1 : batteryInfo.usb ? 2 : 3 : -1);
            //iChannelTimeHandle;
        } catch(Exception e) { Log.e("Shader", "Failed to set built-in uniforms during draw: " + e.toString()); }
        try {
            glClass.getMethod("glBlendFunc", int.class, int.class).invoke(null, glClass.getField("GL_SRC_ALPHA").get(null), glClass.getField("GL_ONE_MINUS_SRC_ALPHA").get(null));
            glClass.getMethod("glEnable", int.class).invoke(null, glClass.getField("GL_BLEND").get(null));
        } catch(Exception e) { Log.e("Shader", "Failed to set and enable blend func during draw: " + e.toString()); }
        try {
            glClass.getMethod("glDrawArrays", int.class, int.class, int.class).invoke(null, glClass.getField("GL_TRIANGLE_STRIP").get(null), 0, 4);
            checkGlError("glDrawArrays");
        } catch(Exception e) { Log.e("Shader", "Failed to draw arrays: " + e.toString()); }
        try {
            glClass.getMethod("glFinish").invoke(null);
        } catch(Exception e) { Log.e("Shader", "Failed to finish draw: " + e.toString()); }

        try {
            long sleepMillis = (long)((1f / targetFPS) * 1000f * 0.88f); //Added *0.88f since for some reason the sleep is slightly off
            //Log.d("GLRenderer", "Sleep time " + sleepMillis + "ms");
            Thread.sleep(sleepMillis);
        } catch(Exception e) { Log.e("Shader", "Failed to sleep: " + e.toString()); }
    }
    public void setViewportSize(int width, int height) {
        // Set the OpenGL viewport to the same size as the surface.
        //Log.d("Shader", width + ", " + height);
        double scale = 1.0;
        int resizedWidth = (int)Math.floor(width * scale);
        int resizedHeight = (int)Math.floor(height * scale);
        //setWidth = resizedWidth;
        //setHeight = resizedHeight;
        try {
            glClass.getMethod("glViewport", int.class, int.class, int.class, int.class).invoke(null, 0, 0, resizedWidth, resizedHeight);
        } catch(Exception e) { Log.e("Shader", "Failed to set viewport: " + e.toString()); }

        if (iResolutionHandle != UNKNOWN_UNIFORM) {
            try {
                glClass.getMethod("glUniform3f", int.class, float.class, float.class, float.class).invoke(null, iResolutionHandle, resizedWidth, resizedHeight, 1); //TODO: figure out how to calculate pixel aspect ratio
            } catch (Exception e) { Log.e("Shader", "Failed to set resolution: " + e.toString()); }
        }
    }
    public void setMousePos(float x, float y, float z, float w) {
        mousex = x;
        mousey = y;
        mousez = z;
        mousew = w;
    }
    public void setBatteryInfo(float percent, boolean full, boolean charging, boolean wall, boolean usb, boolean wireless) {
        batteryInfo.set(percent, full, charging, wall, usb, wireless);
    }
//    public int getMaxTextureCount() {
//        int count = 0;
//        try {
//            //count = GLES32.GL_MAX_COMBINED_TEXTURE_IMAGE_UNITS;
//            count = (int)glClass.getField("GL_MAX_COMBINED_TEXTURE_IMAGE_UNITS").get(null);
//        } catch(Exception e) { Log.e("Shader", e.toString()); }
//        return count;
//    }
    // source: https://www.learnopengles.com/android-lesson-four-introducing-basic-texturing/
    public void bindTexture(Bitmap texture, String handleName) {
        Log.i("Shader", "Binding " + handleName);
        try {
            // Check to see the handle name exists in the shader
            int texHandle = (int)glClass.getMethod("glGetUniformLocation", int.class, String.class).invoke(null, getProgramHandle(), handleName);
            if (texHandle != UNKNOWN_UNIFORM) {
                if (!availableTexUnits.isEmpty()) {
                    // Get a unit number for the texture that the shader can use to reference it
                    int texIndex = availableTexUnits.poll();
                    // Cache the unit number of the texture in case we need it later (to delete or change the texture or something)
                    texHandleUnits.put(handleName, new TextureInfo(texIndex, texture.getWidth(), texture.getHeight()));

                    // Bind to the texture in OpenGL
                    glClass.getMethod("glBindTexture", int.class, int.class).invoke(null, glClass.getField("GL_TEXTURE_2D").get(null), texHandle);
//                    String minFilter = "GL_NEAREST";
//                    int width = texture.getWidth();
//                    int height = texture.getHeight();
//                    if (width == height) {
//                        double log2Result = Math.log(width) / Math.log(2);
//                        boolean isPowerOfTwo = log2Result == Math.floor(log2Result);
//                        //Log.d("Shader", width + " == " + height + " 2^" + log2Result + " isPowerOfTwo: " + isPowerOfTwo);
//                        if (isPowerOfTwo) {
//                            // if the texture's dimensions are power of two then generate mip maps
//                            GLES32.glGenerateMipmap(GLES20.GL_TEXTURE_2D);
//                            minFilter = "GL_LINEAR_MIPMAP_LINEAR";
//                            //Log.d("Shader", "Generating mipmaps");
//                        }
//                    }
                    // Set filtering
                    glClass.getMethod("glTexParameteri", int.class, int.class, int.class).invoke(null, glClass.getField("GL_TEXTURE_2D").get(null), glClass.getField("GL_TEXTURE_MIN_FILTER").get(null), glClass.getField("GL_NEAREST").get(null));
                    glClass.getMethod("glTexParameteri", int.class, int.class, int.class).invoke(null, glClass.getField("GL_TEXTURE_2D").get(null), glClass.getField("GL_TEXTURE_MAG_FILTER").get(null), glClass.getField("GL_LINEAR").get(null));

                    // Load the bitmap into the bound texture
                    GLUtils.texImage2D((int)glClass.getField("GL_TEXTURE_2D").get(null), 0, texture, 0);
                    // update resolutions of channels in shader
                    if (iChannelResolutionHandle != UNKNOWN_UNIFORM) {
                        float[] iChannelRes = new float[MAX_TEXTURE_COUNT * 3];
                        for (Map.Entry<String, TextureInfo> entry : texHandleUnits.entrySet()) {
                            TextureInfo texInfo = entry.getValue();
                            iChannelRes[texInfo.unit] = texInfo.width;
                            iChannelRes[texInfo.unit + 1] = texInfo.height;
                            iChannelRes[texInfo.unit + 2] = 1; // I believe this is similar to iResolution where the third value is the pixel aspect ratio, need to check further
                        }
                        glClass.getMethod("glUniform2fv", int.class, int.class, FloatBuffer.class).invoke(null, iChannelResolutionHandle, MAX_TEXTURE_COUNT * 3, FloatBuffer.wrap(iChannelRes));
                    }
                } else
                    throw new UnsupportedOperationException("Could not bind a new texture to " + handleName + ", exceeded max texture count of " + MAX_TEXTURE_COUNT);
            } else
                Log.w("Shader", "Could not bind texture to non-existent handle " + handleName);
        } catch (Exception e) { Log.e("Shader", "Error binding texture: " + e.toString()); }
    }

    /**
     * get location of some input attribute for shader
     */
    private int glGetAttribLocation(String attrName) {
        int attrLocation = UNKNOWN_ATTRIBUTE;
        try {
            attrLocation = (int)glClass.getMethod("glGetAttribLocation", int.class, String.class).invoke(null, getProgramHandle(), attrName);
            checkGlError("glGetAttribLocation " + attrName);
        } catch(Exception e) { Log.e("Shader", "Failed to get attribute location of " + attrName + ": " + e.toString()); }
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
        } catch(Exception e) { Log.e("Shader", "Failed to set attribute " + attrName + ": " + e.toString()); }
    }

    private void initValues(int glVersion) {
        switch(glVersion) {
            case 0x30002:
                Log.i("Shader", "Using GLES32");
                glClass = GLES32.class;
                break;
            case 0x30001:
                Log.i("Shader", "Using GLES31");
                glClass = GLES31.class;
                break;
            case 0x30000:
                Log.i("Shader", "Using GLES30");
                glClass = GLES30.class;
                break;
            case 0x20000:
                Log.i("Shader", "Using GLES20");
                glClass = GLES20.class;
                break;
            default:
                throw new UnsupportedOperationException("OpenGL version " + glVersion + " (convert to hex) is not supported");
        }

        availableTexUnits = new ArrayDeque<>();
        texHandleUnits = new HashMap<>();
        for (int i = 0; i < MAX_TEXTURE_COUNT; i++)
            availableTexUnits.offer(i);

        startTime = System.currentTimeMillis();
        frame = 0;
        prevTime = 0;

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
            } catch(Exception e) { Log.e("Shader", "Failed to load fallback shaders: " + e.toString()); }
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
        } catch(Exception e) { Log.e("Shader", "Failed to create shader program: " + e.toString()); }
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
                    Log.e("Shader", "Error compiling shader (type " + shaderType + ")");
                    shaderHandle = 0;
                    glClass.getMethod("glDeleteShader", int.class).invoke(null, shaderHandle);
                    //throw new RuntimeException("Error compiling shader (type " + shaderType + ")");
                }
            }
        } catch (Exception e) { Log.e("Shader", "Failed to load shader of type " + shaderType + ": " + e.toString() + "\n" + shaderCode); }
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
            iMouseHandle = (int)glClass.getMethod("glGetUniformLocation", int.class, String.class).invoke(null, getProgramHandle(), FRAGMENT_SHADER_UNIFORM_MOUSE);
            iFrameHandle = (int)glClass.getMethod("glGetUniformLocation", int.class, String.class).invoke(null, getProgramHandle(), FRAGMENT_SHADER_UNIFORM_FRAME);
            iTimeDeltaHandle = (int)glClass.getMethod("glGetUniformLocation", int.class, String.class).invoke(null, getProgramHandle(), FRAGMENT_SHADER_UNIFORM_TIME_DELTA);
            iFrameRateHandle = (int)glClass.getMethod("glGetUniformLocation", int.class, String.class).invoke(null, getProgramHandle(), FRAGMENT_SHADER_UNIFORM_FRAME_RATE);
            iDateHandle = (int)glClass.getMethod("glGetUniformLocation", int.class, String.class).invoke(null, getProgramHandle(), FRAGMENT_SHADER_UNIFORM_DATE);
            iChannelResolutionHandle = (int)glClass.getMethod("glGetUniformLocation", int.class, String.class).invoke(null, getProgramHandle(), FRAGMENT_SHADER_UNIFORM_CHANNEL_RES);
            iBattery = (int)glClass.getMethod("glGetUniformLocation", int.class, String.class).invoke(null, getProgramHandle(), FRAGMENT_SHADER_UNIFORM_BATTERY);
            //Log.d("Shader", "mvpMatrixHandle: " + mMVPMatrixHandle + " stMatrixHandle: " + mSTMatrixHandle + " iTimeHandle: " + iTimeHandle + " iResolutionHandle: " + iResolutionHandle);
        } catch(Exception e) { Log.e("Shader", "An issue occurred while retrieving handles: " + e.toString()); }
    }

    private void checkGlError(String op) {
        int error = 0;
        int glNoErrorCode = 0;
        try {
            error = (int)glClass.getMethod("glGetError").invoke(null);
            glNoErrorCode = (int)glClass.getField("GL_NO_ERROR").get(null);
        } catch(Exception e) { Log.e("Shader", "Failed to check error for " + op + ": " + e.toString()); }
        if (error != glNoErrorCode) {
            //Log.e("GLRenderer", op + ": glError " + error);
            throw new RuntimeException(op + ": glError " + error);
        }
    }
}
