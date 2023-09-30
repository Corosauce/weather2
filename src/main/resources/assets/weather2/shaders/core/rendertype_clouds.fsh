#version 150

#moj_import <fog.glsl>
#moj_import <weather2:classicnoise3d.glsl>

uniform sampler2D Sampler0;

uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;

in vec2 texCoord0;
in float vertexDistance;
in vec4 vertexColor;
in vec4 normal;

out vec4 fragColor;

void main() {
    vec4 color = texture(Sampler0, texCoord0) * vertexColor * ColorModulator;
    //vec4 color = vertexColor;
    if (color.a < 0.1) {
        discard;
    }
    //if (color.a == normal.x) {
        //discard;
    //}
    fragColor = linear_fog(color, vertexDistance, FogStart, FogEnd, FogColor);
    //fragColor = linear_fog(color, vertexDistance, 200, 1200, FogColor);
    //fragColor = linear_fog(color, vertexDistance, 0, 150, FogColor);
    //fragColor = color;
}
