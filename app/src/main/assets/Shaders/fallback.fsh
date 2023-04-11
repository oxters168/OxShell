#version 300 es
precision highp float;
uniform float iTime;
uniform vec2 iResolution;
in vec2 textureCoord;
out vec4 fragColor;
void main()   // The entry point for our fragment shader.
{
   float loop = sin(mod(iTime / 2., 3.14));
   vec3 col = vec3(vec2(1) - textureCoord.xy, (textureCoord.x + textureCoord.y) / 2.);
   fragColor = vec4(abs(vec3(loop) - col.xyz), 1);   // Pass the color directly through the pipeline.
}