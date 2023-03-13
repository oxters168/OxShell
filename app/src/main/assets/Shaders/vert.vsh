#version 300 es

uniform mat4 uMVPMatrix;
uniform mat4 uSTMatrix;

in vec3 inPosition;
in vec2 inTextureCoord;

out vec2 textureCoord;
out float iTime;

void main() {
    gl_Position = uMVPMatrix * vec4(inPosition.xyz, 1);
    textureCoord = (uSTMatrix * vec4(vec2(inTextureCoord.x, 1. - inTextureCoord.y), 0, 0)).xy;
}