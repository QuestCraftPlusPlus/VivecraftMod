#version 150 core

uniform sampler2DArray Sampler0;
uniform int layer;

#define SAMPLE(uv) texture(Sampler0, vec3(uv, float(layer)))

#moj_import <postprocessing_vr.glsl>
