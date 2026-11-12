#version 150 core

uniform sampler2D Sampler0;

#define SAMPLE(uv) texture(Sampler0, uv)

#moj_import <postprocessing_vr.glsl>
