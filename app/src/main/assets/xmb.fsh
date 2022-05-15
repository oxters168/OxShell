#version 300 es

precision highp float;

uniform float iTime;
uniform vec2 iResolution;

in vec2 textureCoord;
out vec4 fragColor;

vec3 lerp(vec3 color1, vec3 color2, float value)
{
	return value * (color2 - color1) + color1;
}
float wave(float x, float frequency, float speed, float midHeight, float maxHeight)
{
    return (sin(frequency * (x + speed * (((1. - (pow(cos(0.002 * (iTime + 400.)), 2.) + 1.) / 2.) + .1) * 2048.))) * (maxHeight - midHeight)) + midHeight;
    //return (sin(frequency * (x + speed * iTime)) * (maxHeight - midHeight)) + midHeight;
}
float percentHigh(float currentY, float waveHeight, float maxHeight, float power)
{
    float percentWave = max(waveHeight - currentY, 0.0) / maxHeight;
    return pow(1.0 - percentWave, power);
}
float waveColor(vec2 uv, float waveHeight, float maxHeight, float frequency, float power)
{
    float percentWave = percentHigh(uv.y, waveHeight, maxHeight, power);
    return clamp(percentWave + 0.8, 0.0, 1.0);
}

void main() {
    // Normalized pixel coordinates (from 0 to 1)
    vec2 uv = textureCoord;

    // Lerped background
    vec3 blue = vec3(0.0, 0.4, 1.0);
    vec3 blue2 = vec3(0.0, 0.7, 1.0);
    float amount = (uv.x + uv.y) / 2.0;
    vec3 bg = lerp(blue2, blue, amount);

    // Overlayed sine waves
    float midHeight1 = 0.6;
    float maxHeight1 = 0.7 + wave(0.0, 4.0, 0.02, 0.0, 0.02);
    float power1 = 50.0; //Higher power means thinner line
    float frequency1 = 2.0 + wave(0.0, 3.0, 0.03, 0.0, 0.02);
    float speed1 = 0.12 + wave(0.0, 2.2, 0.04, 0.0, 0.01);
    float waveHeight1 = wave(uv.x, frequency1, speed1, midHeight1, maxHeight1);
    float waveCol1 = waveColor(uv, waveHeight1, maxHeight1, frequency1, power1);

    float midHeight2 = 0.62;
    float maxHeight2 = 0.74 + wave(0.0, 3.0, 0.04, 0.0, 0.02);
    float power2 = 50.0; //Higher power means thinner line
    float frequency2 = 2.1 + wave(0.0, 4.0, 0.05, 0.0, 0.02);
    float speed2 = 0.08 + wave(0.0, 2.0, 0.02, 0.0, 0.01);
    float waveHeight2 = wave(uv.x, frequency2, speed2, midHeight2, maxHeight2);
    float waveCol2 = waveColor(uv, waveHeight2, maxHeight2, frequency2, power2);

    vec3 col = bg;
    if (uv.y < waveHeight1)
        col = waveCol1 * col;
    if (uv.y < waveHeight2)
        col = waveCol2 * col;

    // Output to screen
    fragColor = vec4(col,1.0);
}