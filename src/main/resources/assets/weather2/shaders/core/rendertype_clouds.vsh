#version 150

#moj_import <light.glsl>

in vec3 Position;
in vec2 UV0;
in vec3 Normal;

in mat4 ModelMatrix;
in float Brightness;
in vec4 Color;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;

out vec2 texCoord0;
out float vertexDistance;
out vec4 vertexColor;
out vec4 normal;

void main() {
    gl_Position = ProjMat * ModelViewMat * ModelMatrix * vec4(Position * Brightness, 1.0);

    texCoord0 = UV0;
    vertexDistance = length((ModelViewMat * ModelMatrix * vec4(Position, 1.0)).xyz);
    //vertexColor = Color;
    vec3 Light0_Direction = vec3(0.16145112, 0.80725557, -0.5650789);
    vec3 Light1_Direction = vec3(-0.16145112, 0.80725557, 0.5650789);
    //normal = ProjMat * ModelMatrix * vec4(Normal, 0.0);
    normal = ProjMat * ModelMatrix * vec4(Normal, 0.0);
    //normal = vec4(Normal, 0.0);
    vertexColor = minecraft_mix_light(Light0_Direction, Light1_Direction, normal.xyz, Color);
}


