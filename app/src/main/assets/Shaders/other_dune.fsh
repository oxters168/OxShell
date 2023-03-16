#version 300 es

precision mediump float;

uniform float iTime;
in vec2 textureCoord;
out vec4 fragColor;

#define map(a, min1, max1, min2, max2) (min2 + (a - min1) * (max2 - min2) / (max1 - min1))

struct wave_t 
{
  float freq;
  vec2 pos;
  float amplitude;
};

struct moving_wave_t
{
    wave_t wave;
    float speed;
};

struct hill_t
{
    float y;
    float decay;
};

struct half_hill_t
{
    float y;
    float decay;
};

struct two_sided_hill_t
{
    float y;
    float decayAbove;
    float decayBelow;
};

struct hill_wave_t
{
    moving_wave_t wave;
    float decayAbove;
    float decayBelow;
};

float query(wave_t wave, float x)
{
    return (sin(wave.freq * x + wave.pos.x) * wave.amplitude) + wave.pos.y;
}

float query(moving_wave_t moving, float x)
{
    moving.wave.pos.x += moving.speed*iTime;
    return query(moving.wave, x);
}

float query(hill_t hill, float y)
{
    return pow(1.0 - abs(hill.y - y), hill.decay);
}

float query(half_hill_t hhill, float y)
{
    return hhill.y > y ? query(hill_t(hhill.y, hhill.decay), y) : 0.0;
}

float query(two_sided_hill_t hill, float queriedY)
{
    return query(half_hill_t(hill.y, hill.decayBelow), queriedY)
         + query(half_hill_t(queriedY, hill.decayAbove), hill.y);
}

float query(hill_wave_t hw, vec2 pos)
{
    float waveY = query(hw.wave, pos.x);
    return query(two_sided_hill_t(waveY, hw.decayAbove, hw.decayBelow), pos.y);
}

hill_wave_t hill_wave1()
{
    float amplitude = map(sin(iTime*0.5), -1.0, 1.0, 0.03, 0.13);
    float frequency = map(sin(iTime*0.4+0.3), -1.0, 1.0, 7.0, 9.0);
    float speed = map(sin(iTime*0.1), -1.0, 1.0, 0.01, 0.02);
    float decayAbove = 0.5;
    float decayBelow = 15.0;
    vec2 pos = vec2(0.0, 0.55);
    return hill_wave_t(
        moving_wave_t(wave_t(frequency, pos, amplitude), speed)
        , decayAbove
        , decayBelow
    );
}

hill_wave_t hill_wave2()
{
    float amplitude = map(sin(iTime*0.4), -1.0, 1.0, 0.05, 0.1);
    float frequency = map(sin(iTime*0.3), -1.0, 1.0, 6.0, 8.0);
    float speed = map(sin(iTime*0.1+1.52), -1.0, 1.0, 0.01, 0.02);
    float decayAbove = 0.5;
    float decayBelow = 15.0;
    vec2 pos = vec2(0.2, 0.5);
    return hill_wave_t(
        moving_wave_t(wave_t(frequency, pos, amplitude), speed)
        , decayAbove
        , decayBelow
    );
}

hill_wave_t hill_wave3()
{
    float amplitude = map(sin(iTime*0.45), -1.0, 1.0, 0.05, 0.15);
    float frequency = map(sin(iTime*0.2), -1.0, 1.0, 5.0, 8.0);
    float speed = map(sin(iTime*0.05+0.9), -1.0, 1.0, 0.01, 0.02);
    float decayAbove = 0.5;
    float decayBelow = 15.0;
    vec2 pos = vec2(0.2, 0.6);
    return hill_wave_t(
        moving_wave_t(wave_t(frequency, pos, amplitude), speed)
        , decayAbove
        , decayBelow
    );
}

float g_mean(vec2 v, float p)
{
    float dim = 2.0;
    return abs(p) < 1e-5
        ? pow(v.x*v.y, 1.0/dim)
        : pow((pow(v.x, p) + pow(v.y, p))/dim, 1./p);
}

float g_mean(vec3 v, float p)
{
    float dim = 3.0;
    return abs(p) < 1e-5
        ? pow(v.x*v.y*v.z, 1.0/dim)
        : pow((pow(v.x, p) + pow(v.y, p)+ pow(v.z, p))/dim, 1./p);
}


void main()
{
    // Normalized pixel coordinates (from 0 to 1)
    vec2 uv = textureCoord;
    vec3 bgLow = vec3(map(sin(iTime*0.1), -1.0, 1.0, 0.05, 0.2),0.2,0.4);
    vec3 bgHigh = vec3(map(sin(iTime*0.15+0.6), -1.0, 1.0, 0.05, 0.2),0.35, 0.7);
    float queriedZ = g_mean(
        vec3(query(hill_wave1(), uv),
             query(hill_wave2(), uv),
             query(hill_wave3(), uv)),
        2.0 + 1.0*sin(iTime*0.5)
    );
    // Output to screen
    fragColor = vec4(mix(bgLow, bgHigh, queriedZ),1.0);
}