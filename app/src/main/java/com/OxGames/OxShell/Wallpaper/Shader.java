package com.OxGames.OxShell.Wallpaper;

import android.opengl.GLES30;

public class Shader {
    private int programHandle;
    private String vertexCode;
    private String fragmentCode;

    public Shader() {
        vertexCode =
            "#version 300 es                                                          \n"
            + "uniform mat4 uMVPMatrix;                                              \n"
            + "uniform mat4 uSTMatrix;                                             \n"
            + "in vec3 inPosition;                                                 \n"
            + "in vec2 inTextureCoord;                                             \n"
            + "out vec2 textureCoord;                                              \n"
            + "void main()                                                         \n"   // The entry point for our vertex shader.
            + "{                                                                   \n"
            + "   gl_Position = uMVPMatrix * vec4(inPosition.xyz, 1);              \n"   // gl_Position is a special variable used to store the final position.
            + "   textureCoord = (uSTMatrix * vec4(inTextureCoord.xy, 0, 0)).xy;   \n"
            + "}                                                                   \n";
        fragmentCode =
            "#version 300 es                      \n"
            + "precision mediump float;           \n"   // Set the default precision to medium. We don't need as high of a
            // precision in the fragment shader.
            + "in vec2 textureCoord;            \n"
            + "out vec4 fragColor;              \n"
            + "void main()                      \n"   // The entry point for our fragment shader.
            + "{                                \n"
            + "   fragColor = vec4(0,1,0,1); \n"   // Pass the color directly through the pipeline.
            + "}                                \n";

        createProgram();
    }
    public Shader(String vertexCode, String fragmentCode) {
        this.vertexCode = vertexCode;
        this.fragmentCode = fragmentCode;
        createProgram();
    }

    public int getProgramHandle() {
        return programHandle;
    }

    private void createProgram() {
        int vertexShaderHandle = loadShader(GLES30.GL_VERTEX_SHADER, vertexCode);
        int fragmentShaderHandle = loadShader(GLES30.GL_FRAGMENT_SHADER, fragmentCode);

        // Create a program object and store the handle to it.
        programHandle = GLES30.glCreateProgram();
        if (programHandle != 0)
        {
            // Bind the vertex shader to the program.
            GLES30.glAttachShader(programHandle, vertexShaderHandle);

            // Bind the fragment shader to the program.
            GLES30.glAttachShader(programHandle, fragmentShaderHandle);

            // Bind attributes
            //GLES30.glBindAttribLocation(programHandle, 0, "a_Position");
            //GLES30.glBindAttribLocation(programHandle, 1, "a_Color");

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
            }
        }
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
}
