#version 150

#moj_import <light.glsl>

in vec3 Position;
in vec2 UV0;
in vec4 Color;
in vec3 Normal;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;

uniform vec3 Light0_Direction;
uniform vec3 Light1_Direction;

out vec2 texCoord0;
out float vertexDistance;
out vec4 vertexColor;
out vec4 normal;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);

    texCoord0 = UV0;
    vertexDistance = length((ModelViewMat * vec4(Position, 1.0)).xyz);
    vertexColor = Color;
    normal = ProjMat * ModelViewMat * vec4(Normal, 0.0);
    normal = vec4(Normal, 0.0);
    //vec4 normal2 = ModelViewMat * vec4(Normal, 0.0);
    //vertexColor = minecraft_mix_light(Light0_Direction, Light1_Direction, normal.xyz, Color);
    //[0.16145112, 0.80725557, -0.5650789]
    //[-0.16145112, 0.80725557, 0.5650789]
    vec4 l0 = vec4(Light0_Direction, 0) * -ModelViewMat;
    vec4 l1 = vec4(Light1_Direction, 0) * -ModelViewMat;
    vec3 l00 = vec3(0.16145112, 0.80725557, -0.5650789);
    vec3 l11 = vec3(-0.16145112, 0.80725557, 0.5650789);
    //vertexColor = minecraft_mix_light(l0.xyz, l1.xyz, Normal, Color);
    vertexColor = minecraft_mix_light(l00, l11, Normal, Color);
}
